package com.projectkorra.ProjectKorra.firebending;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AvatarState;

public class Fireball {

	public static ConcurrentHashMap<Integer, Fireball> instances = new ConcurrentHashMap<Integer, Fireball>();
	private static ConcurrentHashMap<Entity, Fireball> explosions = new ConcurrentHashMap<Entity, Fireball>();

	private static long defaultchargetime = ProjectKorra.plugin.getConfig().getLong("Abilities.Fire.FireBlast.Charged.ChargeTime");
	private static long interval = 25;
	private static double radius = 1.5;
	private static int ID = Integer.MIN_VALUE;
	
	private static double MAX_DAMAGE = ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.FireBlast.Charged.Damage");
	private static double DAMAGE_RADIUS = ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.FireBlast.Charged.DamageRadius");
	private static double RANGE = ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.FireBlast.Charged.Range");
	private static double POWER = ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.FireBlast.Charged.Power");

	private int id;
	private double maxdamage = MAX_DAMAGE;
	private double range = RANGE;
	private double explosionradius = DAMAGE_RADIUS;
	private double power = POWER;
	private double innerradius = explosionradius / 2;
	private long starttime;
	private long time;
	private long chargetime = defaultchargetime;
	private boolean charged = false;
	private boolean launched = false;
	private Player player;
	private Location origin;
	private Location location;
	private Vector direction;
	private TNTPrimed explosion = null;

	public Fireball(Player player) {
		this.player = player;
		time = System.currentTimeMillis();
		starttime = time;
		if (Methods.isDay(player.getWorld())) {
			chargetime = (long) (chargetime / ProjectKorra.plugin.getConfig().getDouble("Properties.Fire.DayFactor"));
		}
		if (AvatarState.isAvatarState(player)) {
			chargetime = 0;
			maxdamage = AvatarState.getValue(maxdamage);
		}
		range = Methods.getFirebendingDayAugment(range, player.getWorld());
		if (!player.getEyeLocation().getBlock().isLiquid()) {
			id = ID;
			instances.put(id, this);
			if (ID == Integer.MAX_VALUE)
				ID = Integer.MIN_VALUE;
			ID++;
		}

	}

	private void progress() {
		if (Methods.getBoundAbility(player) == null) {
			remove();
			return;
		}
		
		if (!Methods.canBend(player.getName(), "FireBlast") && !launched) {
			remove();
			return;
		}
		
		if (!Methods.getBoundAbility(player).equalsIgnoreCase("FireBlast") && !launched) {
			remove();
			return;
		}

		if (System.currentTimeMillis() > starttime + chargetime) {
			charged = true;
		}

		if (!player.isSneaking() && !charged) {
			new FireBlast(player);
			remove();
			return;
		}

		if (!player.isSneaking() && !launched) {
			launched = true;
			location = player.getEyeLocation();
			origin = location.clone();
			direction = location.getDirection().normalize().multiply(radius);
		}

		if (System.currentTimeMillis() > time + interval) {
			if (launched) {
				if (Methods.isRegionProtectedFromBuild(player, "Blaze",	location)) {
					remove();
					return;
				}
			}

			time = System.currentTimeMillis();

			if (!launched && !charged)
				return;
			if (!launched) {
				player.getWorld().playEffect(player.getEyeLocation(), Effect.MOBSPAWNER_FLAMES, 0, 3);
				return;
			}

			location = location.clone().add(direction);
			if (location.distance(origin) > range) {
				remove();
				return;
			}

			if (Methods.isSolid(location.getBlock())) {
				explode();
				return;
			} else if (location.getBlock().isLiquid()) {
				remove();
				return;
			}

			fireball();

		}

	}

	public static Fireball getFireball(Entity entity) {
		if (explosions.containsKey(entity))
			return explosions.get(entity);
		return null;
	}

	public void dealDamage(Entity entity) {
		if (explosion == null)
			return;
		// if (Methods.isObstructed(explosion.getLocation(),
		// entity.getLocation())) {
		// return 0;
		// }
		double distance = entity.getLocation().distance(explosion.getLocation());
		if (distance > explosionradius)
			return;
		if (distance < innerradius) {
			Methods.damageEntity(player, entity, maxdamage);
			return;
		}
		double slope = -(maxdamage * .5) / (explosionradius - innerradius);
		double damage = slope * (distance - innerradius) + maxdamage;
		// Methods.verbose(damage);
		Methods.damageEntity(player, entity, damage);
		Methods.breakBreathbendingHold(entity);
	}

	private void fireball() {
		for (Block block : Methods.getBlocksAroundPoint(location, radius)) {
			block.getWorld().playEffect(block.getLocation(), Effect.MOBSPAWNER_FLAMES, 0, 20);
			if (Methods.rand.nextInt(4) == 0) {
				Methods.playFirebendingSound(location);
			}
			
		}

		for (Entity entity : Methods.getEntitiesAroundPoint(location, 2 * radius)) {
			if (entity.getEntityId() == player.getEntityId())
				continue;
			entity.setFireTicks(120);
			if (entity instanceof LivingEntity) {
				explode();
				dealDamage(entity);
				return;
			}
		}
	}

	public static boolean isCharging(Player player) {
		for (int id : instances.keySet()) {
			Fireball ball = instances.get(id);
			if (ball.player == player && !ball.launched)
				return true;
		}
		return false;
	}

	private void explode() {
		// List<Block> blocks = Methods.getBlocksAroundPoint(location, 3);
		// List<Block> blocks2 = new ArrayList<Block>();

		// Methods.verbose("Fireball Explode!");
		boolean explode = true;
		for (Block block : Methods.getBlocksAroundPoint(location, 3)) {
			if (Methods.isRegionProtectedFromBuild(player, "FireBlast",	block.getLocation())) {
				explode = false;
				break;
			}
		}
		if (explode) {
			explosion = player.getWorld().spawn(location, TNTPrimed.class);
			explosion.setFuseTicks(0);
			float yield = (float) power;
			if (!AvatarState.isAvatarState(player)) {
				if (Methods.isDay(player.getWorld())) {
					Methods.getFirebendingDayAugment(yield, player.getWorld());
				} else {
					yield *= 1.;
				}
			} else {
				yield *= AvatarState.factor;
//				yield = AvatarState.getValue(yield);
			}
//			switch (player.getWorld().getDifficulty()) {
//			case PEACEFUL:
//				yield *= 2.;
//				break;
//			case EASY:
//				yield *= 2.;
//				break;
//			case NORMAL:
//				yield *= 1.;
//				break;
//			case HARD:
//				yield *= 3. / 4.;
//				break;
//			}
			explosion.setYield(yield);
			explosions.put(explosion, this);
		}
		// location.getWorld().createExplosion(location, 1);

		ignite(location);
		remove();
	}

	private void ignite(Location location) {
		for (Block block : Methods.getBlocksAroundPoint(location, FireBlast.AFFECTING_RADIUS)) {
			if (FireStream.isIgnitable(player, block)) {
				block.setType(Material.FIRE);
				if (FireBlast.dissipate) {
					FireStream.ignitedblocks.put(block, player);
					FireStream.ignitedtimes.put(block, System.currentTimeMillis());
				}
			}
		}
	}

	public static void progressAll() {
		for (int id : instances.keySet())
			instances.get(id).progress();
	}

	private void remove() {
		instances.remove(id);
	}

	public static void removeAll() {
		for (int id : instances.keySet())
			instances.get(id).remove();
	}

	public static void removeFireballsAroundPoint(Location location, double radius) {
		for (int id : instances.keySet()) {
			Fireball fireball = instances.get(id);
			if (!fireball.launched)
				continue;
			Location fireblastlocation = fireball.location;
			if (location.getWorld() == fireblastlocation.getWorld()) {
				if (location.distance(fireblastlocation) <= radius)
					instances.remove(id);
			}
		}

	}

	public static boolean annihilateBlasts(Location location, double radius, Player source) {
		boolean broke = false;
		for (int id : instances.keySet()) {
			Fireball fireball = instances.get(id);
			if (!fireball.launched)
				continue;
			Location fireblastlocation = fireball.location;
			if (location.getWorld() == fireblastlocation.getWorld()
					&& !source.equals(fireball.player)) {
				if (location.distance(fireblastlocation) <= radius) {
					fireball.explode();
					broke = true;
				}
			}
		}

		return broke;

	}

	public double getMaxdamage() {
		return maxdamage;
	}

	public void setMaxdamage(double maxdamage) {
		this.maxdamage = maxdamage;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getExplosionradius() {
		return explosionradius;
	}

	public void setExplosionradius(double explosionradius) {
		this.explosionradius = explosionradius;
	}

	public double getPower() {
		return power;
	}

	public void setPower(double power) {
		this.power = power;
	}

	public double getInnerradius() {
		return innerradius;
	}

	public void setInnerradius(double innerradius) {
		this.innerradius = innerradius;
	}

	public long getChargetime() {
		return chargetime;
	}

	public void setChargetime(long chargetime) {
		this.chargetime = chargetime;
	}
}
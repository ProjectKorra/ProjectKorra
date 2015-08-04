package com.projectkorra.projectkorra.firebending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AvatarState;
import com.projectkorra.projectkorra.ability.api.AddonAbility;
import com.projectkorra.projectkorra.airbending.AirMethods;
import com.projectkorra.projectkorra.util.ParticleEffect;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.Vector;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Ability charged FireBlast
 */
public class Fireball extends AddonAbility {

	private static ConcurrentHashMap<Entity, Fireball> explosions = new ConcurrentHashMap<Entity, Fireball>();

	private static long defaultchargetime = config.get().getLong("Abilities.Fire.FireBlast.Charged.ChargeTime");
	private static long interval = 25;
	private static double radius = 1.5;

	private static double MAX_DAMAGE = config.get().getDouble("Abilities.Fire.FireBlast.Charged.Damage");
	private static double DAMAGE_RADIUS = config.get().getDouble("Abilities.Fire.FireBlast.Charged.DamageRadius");
	private static double RANGE = config.get().getDouble("Abilities.Fire.FireBlast.Charged.Range");
	private static double POWER = config.get().getDouble("Abilities.Fire.FireBlast.Charged.Power");
	private static double fireticks = config.get().getDouble("Abilities.Fire.FireBlast.Charged.FireTicks");

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
		reloadVariables();
		this.player = player;
		time = System.currentTimeMillis();
		starttime = time;
		if (FireMethods.isDay(player.getWorld())) {
			chargetime = (long) (chargetime / config.get().getDouble("Properties.Fire.DayFactor"));
		}
		if (AvatarState.isAvatarState(player)) {
			chargetime = 0;
			maxdamage = AvatarState.getValue(maxdamage);
		}
		range = FireMethods.getFirebendingDayAugment(range, player.getWorld());
		if (!player.getEyeLocation().getBlock().isLiquid()) {
			//instances.put(id, this);
			putInstance(player, this);
		}
	}

	public static boolean annihilateBlasts(Location location, double radius, Player source) {
		boolean broke = false;
		for (Integer id : getInstances(Fireball.class).keySet()) {
			Fireball fireball = (Fireball) getAbility(id);
			if (!fireball.launched)
				continue;
			Location fireblastlocation = fireball.location;
			if (location.getWorld() == fireblastlocation.getWorld() && !source.equals(fireball.player)) {
				if (location.distance(fireblastlocation) <= radius) {
					fireball.explode();
					broke = true;
				}
			}
		}

		return broke;

	}

	public static Fireball getFireball(Entity entity) {
		if (explosions.containsKey(entity))
			return explosions.get(entity);
		return null;
	}

	public static boolean isCharging(Player player) {
		for (Integer id : getInstances(Fireball.class).keySet()) {
			Fireball fireball = (Fireball) getAbility(id);
			if (fireball.player == player && !fireball.launched)
				return true;
		}
		return false;
	}

	public static void removeFireballsAroundPoint(Location location, double radius) {
		for (Integer id : getInstances(Fireball.class).keySet()) {
			Fireball fireball = (Fireball) getAbility(id);
			if (!fireball.launched)
				continue;
			Location fireblastlocation = fireball.location;
			if (location.getWorld() == fireblastlocation.getWorld()) {
				if (location.distance(fireblastlocation) <= radius)
					fireball.remove();
			}
		}

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
			GeneralMethods.damageEntity(player, entity, maxdamage);
			return;
		}
		double slope = -(maxdamage * .5) / (explosionradius - innerradius);
		double damage = slope * (distance - innerradius) + maxdamage;
		// Methods.verbose(damage);
		GeneralMethods.damageEntity(player, entity, damage);
		AirMethods.breakBreathbendingHold(entity);
	}

	public void explode() {
		// List<Block> blocks = Methods.getBlocksAroundPoint(location, 3);
		// List<Block> blocks2 = new ArrayList<Block>();

		// Methods.verbose("Fireball Explode!");
		boolean explode = true;
		for (Block block : GeneralMethods.getBlocksAroundPoint(location, 3)) {
			if (GeneralMethods.isRegionProtectedFromBuild(player, "FireBlast", block.getLocation())) {
				explode = false;
				break;
			}
		}
		if (explode) {
			explosion = player.getWorld().spawn(location, TNTPrimed.class);
			explosion.setFuseTicks(0);
			float yield = (float) power;
			if (!AvatarState.isAvatarState(player)) {
				if (FireMethods.isDay(player.getWorld())) {
					FireMethods.getFirebendingDayAugment(yield, player.getWorld());
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

	private void fireball() {
		for (Block block : GeneralMethods.getBlocksAroundPoint(location, radius)) {
			ParticleEffect.FLAME.display(block.getLocation(), 0.6F, 0.6F, 0.6F, 0, 5);
			ParticleEffect.SMOKE.display(block.getLocation(), 0.6F, 0.6F, 0.6F, 0, 2);
			if (GeneralMethods.rand.nextInt(4) == 0) {
				FireMethods.playFirebendingSound(location);
			}

		}

		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2 * radius)) {
			if (entity.getEntityId() == player.getEntityId())
				continue;
			entity.setFireTicks((int) (fireticks * 20));
			if (entity instanceof LivingEntity) {
				explode();
				dealDamage(entity);
				return;
			}
		}
	}

	public long getChargetime() {
		return chargetime;
	}

	public double getExplosionradius() {
		return explosionradius;
	}

	public double getInnerradius() {
		return innerradius;
	}

	@Override
	public InstanceType getInstanceType() {
		return InstanceType.MULTIPLE;
	}

	public double getMaxdamage() {
		return maxdamage;
	}

	public Player getPlayer() {
		return player;
	}

	public double getPower() {
		return power;
	}

	public double getRange() {
		return range;
	}

	private void ignite(Location location) {
		for (Block block : GeneralMethods.getBlocksAroundPoint(location, FireBlast.AFFECTING_RADIUS)) {
			if (FireStream.isIgnitable(player, block)) {
				block.setType(Material.FIRE);
				if (FireBlast.dissipate) {
					FireStream.ignitedblocks.put(block, player);
					FireStream.ignitedtimes.put(block, System.currentTimeMillis());
				}
			}
		}
	}

	@Override
	public boolean progress() {
		if (GeneralMethods.getBoundAbility(player) == null) {
			remove();
			return false;
		}

		if (!GeneralMethods.canBend(player.getName(), "FireBlast") && !launched) {
			remove();
			return false;
		}

		if (!GeneralMethods.getBoundAbility(player).equalsIgnoreCase("FireBlast") && !launched) {
			remove();
			return false;
		}

		if (System.currentTimeMillis() > starttime + chargetime) {
			charged = true;
		}

		if (!player.isSneaking() && !charged) {
			new FireBlast(player);
			remove();
			return false;
		}

		if (!player.isSneaking() && !launched) {
			launched = true;
			location = player.getEyeLocation();
			origin = location.clone();
			direction = location.getDirection().normalize().multiply(radius);
		}

		if (System.currentTimeMillis() > time + interval) {
			if (launched) {
				if (GeneralMethods.isRegionProtectedFromBuild(player, "Blaze", location)) {
					remove();
					return false;
				}
			}

			time = System.currentTimeMillis();

			if (!launched && !charged)
				return true;
			if (!launched) {
				player.getWorld().playEffect(player.getEyeLocation(), Effect.MOBSPAWNER_FLAMES, 0, 3);
				return true;
			}

			location = location.clone().add(direction);
			if (location.distance(origin) > range) {
				remove();
				return false;
			}

			if (GeneralMethods.isSolid(location.getBlock())) {
				explode();
				return false;
			} else if (location.getBlock().isLiquid()) {
				remove();
				return false;
			}

			fireball();
		}
		return true;
	}

	@Override
	public void reloadVariables() {
		defaultchargetime = config.get().getLong("Abilities.Fire.FireBlast.Charged.ChargeTime");
		interval = 25;
		radius = 1.5;

		MAX_DAMAGE = config.get().getDouble("Abilities.Fire.FireBlast.Charged.Damage");
		DAMAGE_RADIUS = config.get().getDouble("Abilities.Fire.FireBlast.Charged.DamageRadius");
		RANGE = config.get().getDouble("Abilities.Fire.FireBlast.Charged.Range");
		POWER = config.get().getDouble("Abilities.Fire.FireBlast.Charged.Power");
		fireticks = config.get().getDouble("Abilities.Fire.FireBlast.Charged.FireTicks");

		maxdamage = MAX_DAMAGE;
		range = RANGE;
		explosionradius = DAMAGE_RADIUS;
		power = POWER;
		chargetime = defaultchargetime;
	}

	public void setChargetime(long chargetime) {
		this.chargetime = chargetime;
	}

	public void setExplosionradius(double explosionradius) {
		this.explosionradius = explosionradius;
	}

	public void setInnerradius(double innerradius) {
		this.innerradius = innerradius;
	}

	public void setMaxdamage(double maxdamage) {
		this.maxdamage = maxdamage;
	}

	public void setPower(double power) {
		this.power = power;
	}

	public void setRange(double range) {
		this.range = range;
	}
}

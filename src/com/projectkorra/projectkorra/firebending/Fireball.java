package com.projectkorra.projectkorra.firebending;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AvatarState;
import com.projectkorra.projectkorra.airbending.AirMethods;
import com.projectkorra.projectkorra.configuration.ConfigLoadable;
import com.projectkorra.projectkorra.util.ParticleEffect;

/**
 * Ability charged FireBlast
 */
public class Fireball implements ConfigLoadable {

	public static ConcurrentHashMap<Integer, Fireball> instances = new ConcurrentHashMap<>();

	private static ConcurrentHashMap<Entity, Fireball> explosions = new ConcurrentHashMap<Entity, Fireball>();

	private static long defaultchargetime = config.get().getLong("Abilities.Fire.FireBlast.Charged.ChargeTime");
	private static long interval = 25;
	private static double radius = 1.5;
	private static int idCounter = 0;

	private static double MAX_DAMAGE = config.get().getDouble("Abilities.Fire.FireBlast.Charged.Damage");
	private static double DAMAGE_RADIUS = config.get().getDouble("Abilities.Fire.FireBlast.Charged.DamageRadius");
	private static double RANGE = config.get().getDouble("Abilities.Fire.FireBlast.Charged.Range");
	private static double EXPLOSIONRADIUS = config.get().getDouble("Abilities.Fire.FireBlast.Charged.ExplosionRadius");
	private static boolean DAMAGEBLOCKS = config.get().getBoolean("Abilities.Fire.FireBlast.Charged.DamageBlocks");
	private static double fireticks = config.get().getDouble("Abilities.Fire.FireBlast.Charged.FireTicks");

	private int id;
	private double maxdamage = MAX_DAMAGE;
	private double range = RANGE;
	private double damageradius = DAMAGE_RADIUS;
	private double explosionradius = EXPLOSIONRADIUS;
	private double innerradius = damageradius / 2;
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
	private boolean damage_blocks;

	public Fireball(Player player) {
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
			instances.put(idCounter, this);
			this.id = idCounter;
			idCounter = (idCounter + 1) % Integer.MAX_VALUE;
		}
	}

	public static boolean annihilateBlasts(Location location, double radius, Player source) {
		boolean broke = false;
		for (Fireball fireball : instances.values()) {
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
		for (Fireball fireball : instances.values()) {
			if (fireball.player == player && !fireball.launched)
				return true;
		}
		return false;
	}

	public static void removeFireballsAroundPoint(Location location, double radius) {
		for (Fireball fireball : instances.values()) {
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
		if (distance > damageradius)
			return;
		if (distance < innerradius) {
			GeneralMethods.damageEntity(player, entity, maxdamage, "FireBlast");
			return;
		}
		double slope = -(maxdamage * .5) / (damageradius - innerradius);
		double damage = slope * (distance - innerradius) + maxdamage;
		// Methods.verbose(damage);
		GeneralMethods.damageEntity(player, entity, damage, "FireBlast");
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
			if (damage_blocks && explosionradius > 0) {
				explosion = player.getWorld().spawn(location, TNTPrimed.class);
				explosion.setFuseTicks(0);
				float yield = (float) explosionradius;
				if (!AvatarState.isAvatarState(player)) {
					if (FireMethods.isDay(player.getWorld())) {
						yield = (float) FireMethods.getFirebendingDayAugment(yield, player.getWorld());
					}
				} else {
					yield *= AvatarState.factor;
				}
				explosion.setYield(yield);
				explosions.put(explosion, this);
			} else {
				List<Entity> l = GeneralMethods.getEntitiesAroundPoint(location, damageradius);
				for (Entity e : l) {
					if (e instanceof LivingEntity) {
						double slope = -(maxdamage * .5) / (damageradius - innerradius);
						double damage = slope * (e.getLocation().distance(location) - innerradius) + maxdamage;
						GeneralMethods.damageEntity(getPlayer(), e, damage, "FireBlast");
					}
				}
				location.getWorld().playSound(location, Sound.EXPLODE, 5, 1);
				ParticleEffect.EXPLOSION_HUGE.display(new Vector(0, 0, 0), 0, location, 256);
			}
		}

		ignite(location);
		remove();
	}

	private void fireball() {
		for (Block block : GeneralMethods.getBlocksAroundPoint(location, radius)) {
			ParticleEffect.FLAME.display(block.getLocation(), 0.5F, 0.5F, 0.5F, 0, 5);
			ParticleEffect.SMOKE.display(block.getLocation(), 0.5F, 0.5F, 0.5F, 0, 2);
			if (GeneralMethods.rand.nextInt(4) == 0) {
				FireMethods.playFirebendingSound(location);
			}

		}

		boolean exploded = false;
		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2 * radius)) {
			if (entity.getEntityId() == player.getEntityId())
				continue;
			entity.setFireTicks((int) (fireticks * 20));
			if (entity instanceof LivingEntity) {
				if (!exploded) {
					explode();
					exploded = true;
				}
				dealDamage(entity);
			}
		}
	}

	public long getChargetime() {
		return chargetime;
	}

	public double getDamageRadius() {
		return damageradius;
	}

	public double getExplosionRadius() {
		return explosionradius;
	}

	public boolean getDamageBlocks() {
		return this.damage_blocks;
	}

	public double getInnerradius() {
		return innerradius;
	}

	public double getMaxdamage() {
		return maxdamage;
	}

	public Player getPlayer() {
		return player;
	}

	public double getRange() {
		return range;
	}

	private void ignite(Location location) {
		for (Block block : GeneralMethods.getBlocksAroundPoint(location, FireBlast.AFFECTING_RADIUS)) {
			if (FireStream.isIgnitable(player, block)) {
				if (block.getType() != Material.FIRE) {
					FireStream.replacedBlocks.put(block.getLocation(), block.getState().getData());
				}
				block.setType(Material.FIRE);
				if (FireBlast.dissipate) {
					FireStream.ignitedblocks.put(block, player);
					FireStream.ignitedtimes.put(block, System.currentTimeMillis());
				}
			}
		}
	}

	public boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return false;
		}
		
		if (!player.getWorld().equals(location.getWorld())) {
			remove();
			return false;
		}
		
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

	public static void progressAll() {
		for (Fireball ability : instances.values()) {
			ability.progress();
		}
	}

	public void remove() {
		instances.remove(id);
	}

	public static void removeAll() {
		for (Fireball ability : instances.values()) {
			ability.remove();
		}
	}

	@Override
	public void reloadVariables() {
		defaultchargetime = config.get().getLong("Abilities.Fire.FireBlast.Charged.ChargeTime");
		interval = 25;
		radius = 1.5;

		MAX_DAMAGE = config.get().getDouble("Abilities.Fire.FireBlast.Charged.Damage");
		DAMAGE_RADIUS = config.get().getDouble("Abilities.Fire.FireBlast.Charged.DamageRadius");
		RANGE = config.get().getDouble("Abilities.Fire.FireBlast.Charged.Range");
		DAMAGEBLOCKS = config.get().getBoolean("Abilities.Fire.FireBlast.Charged.DamageBlocks");
		EXPLOSIONRADIUS = config.get().getDouble("Abilities.Fire.FireBlast.Charged.ExplosionRadius");
		fireticks = config.get().getDouble("Abilities.Fire.FireBlast.Charged.FireTicks");

		maxdamage = MAX_DAMAGE;
		range = RANGE;
		damageradius = DAMAGE_RADIUS;
		explosionradius = EXPLOSIONRADIUS;
		damage_blocks = DAMAGEBLOCKS;
		chargetime = defaultchargetime;
	}

	public void setChargetime(long chargetime) {
		this.chargetime = chargetime;
	}

	public void setDamageBlocks(boolean damageblocks) {
		this.damage_blocks = damageblocks;
	}

	public void setExplosionRadius(double radius) {
		this.explosionradius = radius;
	}

	public void setDamageRadius(double radius) {
		this.damageradius = radius;
	}

	public void setInnerradius(double innerradius) {
		this.innerradius = innerradius;
	}

	public void setMaxdamage(double maxdamage) {
		this.maxdamage = maxdamage;
	}

	public void setRange(double range) {
		this.range = range;
	}
}

package com.projectkorra.ProjectKorra.firebending;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.Utilities.ParticleEffect;
import com.projectkorra.ProjectKorra.airbending.AirMethods;

public class Combustion {

	static FileConfiguration config = ProjectKorra.plugin.getConfig();

	public static long chargeTime = ProjectKorra.plugin.getConfig().getLong("Abilities.Fire.Combustion.ChargeTime");
	public static long cooldown = ProjectKorra.plugin.getConfig().getLong("Abilities.Fire.Combustion.Cooldown");

	public static double speed = config.getDouble("Abilities.Fire.Combustion.Speed");
	public static double defaultrange = config.getDouble("Abilities.Fire.Combustion.Range");
	public static double defaultpower = config.getDouble("Abilities.Fire.Combustion.Power");
	public static boolean breakblocks = config.getBoolean("Abilities.Fire.Combustion.BreakBlocks");
	public static double radius = config.getDouble("Abilities.Fire.Combustion.Radius");
	public static double defaultdamage = config.getDouble("Abilities.Fire.Combustion.Damage");

	public Location location;
	private Location origin;
	private Vector direction;
	private double range = defaultrange;
	private double speedfactor;
	static final int maxticks = 10000;
	private int ticks = 0;
	private float power;
	private double damage;

	public Player player;
	@SuppressWarnings("unused")
	private long starttime;
	@SuppressWarnings("unused")
	private boolean charged = false;
	public static ConcurrentHashMap<Player, Combustion> instances = new ConcurrentHashMap<Player, Combustion>();

	public Combustion(Player player) {

		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());

		if (instances.containsKey(player)) return;
		if (bPlayer.isOnCooldown("Combustion")) return;

		this.player = player;
		starttime = System.currentTimeMillis();
		origin = player.getEyeLocation();
		direction = player.getEyeLocation().getDirection().normalize();
		location = origin.clone();
		if (AvatarState.isAvatarState(player)) {
			range = AvatarState.getValue(defaultrange);
			damage = AvatarState.getValue(defaultdamage);
		} else if (FireMethods.isDay(player.getWorld())) {
			range = FireMethods.getFirebendingDayAugment(defaultrange, player.getWorld());
			damage = FireMethods.getFirebendingDayAugment(defaultdamage, player.getWorld());
		} else {
			range = defaultrange;
			damage = defaultdamage;
		}

		if (GeneralMethods.isRegionProtectedFromBuild(player, "Combustion", GeneralMethods.getTargetedLocation(player, range))) {
			return;
		}

		instances.put(player, this);
		bPlayer.addCooldown("Combustion", cooldown);
	}

	private void progress() {

		if (!instances.containsKey(player)) {
			return;
		}

		if (player.isDead() || !player.isOnline()) {
			instances.remove(player);
			return;
		}

		if (!GeneralMethods.canBend(player.getName(), "Combustion")) {
			instances.remove(player);
			return;
		}

		if (GeneralMethods.getBoundAbility(player) == null || !GeneralMethods.getBoundAbility(player).equalsIgnoreCase("Combustion")) {
			instances.remove(player);
			return;
		}

		if (GeneralMethods.isRegionProtectedFromBuild(player, "Combustion", location)) {
			instances.remove(player);
			return;
		}

		speedfactor = speed * (ProjectKorra.time_step / 1000.);
		ticks++;
		if (ticks > maxticks) {
			instances.remove(player);
			return;
		}

		if (location.distance(origin) > range) {
			instances.remove(player);
			return;
		}

		Block block = location.getBlock();
		if (block != null) {
			if (block.getType() != Material.AIR && block.getType() != Material.WATER && block.getType() != Material.STATIONARY_WATER) {
				createExplosion(block.getLocation(), power, breakblocks);
			}
		}

		for (Entity entity : location.getWorld().getEntities()) {
			if (entity instanceof LivingEntity) {
				if (entity.getLocation().distance(location) <= 1) {
					createExplosion(location, power, breakblocks);
				}
			}
		}

		advanceLocation();
	}

	private void createExplosion(Location block, float power, boolean breakblocks) {
		block.getWorld().createExplosion(block.getX(), block.getY(), block.getZ(), (float) defaultpower, true, breakblocks);
		for (Entity entity : block.getWorld().getEntities()) {
			if (entity instanceof LivingEntity) {
				if (entity.getLocation().distance(block) < radius) { // They are close enough to the explosion.
					GeneralMethods.damageEntity(player, entity, damage);
					AirMethods.breakBreathbendingHold(entity);
				}
			}
		}
		instances.remove(player);

	}

	public static void explode(Player player) {
		if (instances.containsKey(player)) {
			Combustion combustion = instances.get(player);
			combustion.createExplosion(combustion.location, combustion.power, breakblocks);
			ParticleEffect.EXPLODE.display(combustion.location, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0, 3);
		}
	}

	private void advanceLocation() {
		ParticleEffect.FIREWORKS_SPARK.display(location, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0, 5);
		ParticleEffect.FLAME.display(location, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0, 2);
		//if (Methods.rand.nextInt(4) == 0) {
		FireMethods.playCombustionSound(location);
		//}
		location = location.add(direction.clone().multiply(speedfactor));
	}

	public static boolean removeAroundPoint(Location loc, double radius) {
		for (Player player : instances.keySet()) {
			Combustion combustion = instances.get(player);
			if (combustion.location.getWorld() == loc.getWorld()) {
				if (combustion.location.distance(loc) <= radius) {
					explode(player);
					instances.remove(player);
					return true;
				}
			}
		}
		return false;
	}

	public static void progressAll() {
		for (Player player : instances.keySet()) {
			instances.get(player).progress();
		}
	}

	//	private void launchFireball() {
	//		fireballs.add(player.launchProjectile(org.bukkit.entity.Fireball.class).getEntityId());
	//	}

}

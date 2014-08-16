package com.projectkorra.ProjectKorra.firebending;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.Utilities.ParticleEffect;

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


//	public static List<Integer> fireballs = new ArrayList<Integer>();

	private Location location;
	private Location origin;
	private Vector direction;
	private double range = defaultrange;
	private double speedfactor;
	static final int maxticks = 10000;
	private int ticks = 0;
	private float power;
	private double damage;

	private Player player;
	private long starttime;
	private boolean charged = false;
	public static ConcurrentHashMap<Player, Combustion> instances = new ConcurrentHashMap<Player, Combustion>();
	public static HashMap<String, Long> cooldowns = new HashMap<String, Long>();

	public Combustion(Player player) {
		if (instances.containsKey(player)) {
			return;
		}
		if (cooldowns.containsKey(player.getName())) {
			if (cooldowns.get(player.getName()) + cooldown >= System.currentTimeMillis()) {
				return;
			} else {
				cooldowns.remove(player.getName());
			}
		}
		this.player = player;
		starttime = System.currentTimeMillis();
		origin = player.getEyeLocation();
		direction = player.getEyeLocation().getDirection().normalize();
		location = origin.clone();
		if (AvatarState.isAvatarState(player)) {
			range = AvatarState.getValue(defaultrange);
			damage = AvatarState.getValue(defaultdamage);
		} else if (Methods.isDay(player.getWorld())) {
			range = Methods.getFirebendingDayAugment(defaultrange, player.getWorld());
			damage = Methods.getFirebendingDayAugment(defaultdamage, player.getWorld());
		} else {
			range = defaultrange;
			damage = defaultdamage;
		}
		
		if (Methods.isRegionProtectedFromBuild(player, "Combustion", Methods.getTargetedLocation(player, range))) {
			return;
		}

		instances.put(player, this);
		cooldowns.put(player.getName(), System.currentTimeMillis());
	}

	private void progress() {

		if (!instances.containsKey(player)) {
			return;
		}

		if (player.isDead() || !player.isOnline()) {
			instances.remove(player);
			return;
		}

		if (!Methods.canBend(player.getName(), "Combustion")) {
			instances.remove(player);
			return;
		}

		if (Methods.getBoundAbility(player) == null || !Methods.getBoundAbility(player).equalsIgnoreCase("Combustion")) {
			instances.remove(player);
			return;
		}
		
		if (Methods.isRegionProtectedFromBuild(player, "Combustion", location)) {
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
		
		for (Entity entity: location.getWorld().getEntities()) {
			if (entity instanceof LivingEntity) {
				if (entity.getLocation().distance(location) <= 1) {
					createExplosion(location, power, breakblocks);
				}
			}
		}
		
		
		advanceLocation();

		//		long warmup = chargeTime;
		//		if (AvatarState.isAvatarState(player)) {
		//			warmup = 0;
		//		}
		//		
		//		if (System.currentTimeMillis() > starttime + warmup) {
		//			charged = true;
		//		}
		//				
		//		if (charged) {
		//			if (player.isSneaking()) {
		//				player.getWorld().playEffect(player.getEyeLocation(), Effect.SMOKE, 4, 3);
		//			} else {
		//				launchFireball();
		//				cooldowns.put(player.getName(), System.currentTimeMillis());
		//				instances.remove(player);
		//			}
		//		} else {
		//			if (!player.isSneaking()) {
		//				instances.remove(player);
		//			}
		//		}
		//		
		//		for (Entity entity: player.getWorld().getEntities()) {
		//			if (fireballs.contains(entity.getEntityId())) {
		//				ParticleEffect.CLOUD.display(entity.getLocation(), 1.0F, 1.0F, 1.0F, 1.0F, 30);
		//			}
		//		}
	}
	
	private void createExplosion(Location block, float power, boolean breakblocks) {
		block.getWorld().createExplosion(block.getX(), block.getY(), block.getZ(), (float) defaultpower, true, breakblocks);
		for (Entity entity: block.getWorld().getEntities()) {
			if (entity instanceof LivingEntity) {
				if (entity.getLocation().distance(block) < radius) { // They are close enough to the explosion.
					Methods.damageEntity(player, entity, damage);
				}
			}
		}
		instances.remove(player);

	}
	
	public static void explode(Player player) {
		if (instances.containsKey(player)) {
			Combustion combustion = instances.get(player);
			combustion.createExplosion(combustion.location, combustion.power, breakblocks);
		}
	}

	private void advanceLocation() {
		ParticleEffect.FIREWORKS_SPARK.display(location, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0, 5);
		ParticleEffect.FLAME.display(location, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0, 2);
		location = location.add(direction.clone().multiply(speedfactor));
	}
	
	public static void removeAroundPoint(Location loc, double radius) {
		for (Player player: instances.keySet()) {
			Combustion combustion = instances.get(player);
			if (combustion.location.distance(loc) <= radius) {
				explode(player);
				instances.remove(player);
			}
		}
	}

	public static void progressAll() {
		for (Player player: instances.keySet()) {
			instances.get(player).progress();
		}
	}

//	private void launchFireball() {
//		fireballs.add(player.launchProjectile(org.bukkit.entity.Fireball.class).getEntityId());
//	}

}

package com.projectkorra.ProjectKorra.firebending;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AvatarState;

public class Lightning {

	public static int defaultdistance = ProjectKorra.plugin.getConfig().getInt("Abilities.Fire.Lightning.Distance");
	private static long defaultwarmup = ProjectKorra.plugin.getConfig().getLong("Abilities.Fire.Lightning.Warmup");
	private static double misschance = ProjectKorra.plugin.getConfig().getLong("Abilities.Fire.Lightning.MissChance");
	private static double threshold = 0.1;
	private static double blockdistance = 4;

	private int maxdamage = 6;
	private double strikeradius = 4;

	private Player player;
	private long starttime;
	private boolean charged = false;
	private LightningStrike strike = null;
	public static ConcurrentHashMap<Player, Lightning> instances = new ConcurrentHashMap<Player, Lightning>();
	private static ConcurrentHashMap<Entity, Lightning> strikes = new ConcurrentHashMap<Entity, Lightning>();
	private ArrayList<Entity> hitentities = new ArrayList<Entity>();

	public Lightning(Player player) {
		if (instances.containsKey(player)) {
			return;
		}
		this.player = player;
		starttime = System.currentTimeMillis();
		instances.put(player, this);

	}

	public static Lightning getLightning(Entity entity) {
		if (strikes.containsKey(entity))
			return strikes.get(entity);
		return null;
	}

	private void strike() {
		Location targetlocation = getTargetLocation();
		if (AvatarState.isAvatarState(player))
			maxdamage = AvatarState.getValue(maxdamage);
		if (!Methods.isRegionProtectedFromBuild(player, "Lightning",
				targetlocation)) {
			strike = player.getWorld().strikeLightning(targetlocation);
			strikes.put(strike, this);
		}
		instances.remove(player);
	}

	private Location getTargetLocation() {
		int distance = (int) Methods.getFirebendingDayAugment(defaultdistance,
				player.getWorld());

		Location targetlocation;
		targetlocation = Methods.getTargetedLocation(player, distance);
		Entity target = Methods.getTargetedEntity(player, distance, new ArrayList<Entity>());
		if (target != null) {
			if (target instanceof LivingEntity
					&& player.getLocation().distance(targetlocation) > target
					.getLocation().distance(player.getLocation())) {
				targetlocation = target.getLocation();
				if (target.getVelocity().length() < threshold)
					misschance = 0;
			}
		} else {
			misschance = 0;
		}

		if (targetlocation.getBlock().getType() == Material.AIR)
			targetlocation.add(0, -1, 0);
		if (targetlocation.getBlock().getType() == Material.AIR)
			targetlocation.add(0, -1, 0);

		if (misschance != 0 && !AvatarState.isAvatarState(player)) {
			double A = Math.random() * Math.PI * misschance * misschance;
			double theta = Math.random() * Math.PI * 2;
			double r = Math.sqrt(A) / Math.PI;
			double x = r * Math.cos(theta);
			double z = r * Math.sin(theta);

			targetlocation = targetlocation.add(x, 0, z);
		}

		return targetlocation;
	}

	private void progress() {
		if (player.isDead() || !player.isOnline()) {
			instances.remove(player);
			return;
		}

		if (Methods.getBoundAbility(player) == null || !Methods.getBoundAbility(player).equalsIgnoreCase("Lightning")) {
			instances.remove(player);
			return;
		}

		int distance = (int) Methods.getFirebendingDayAugment(defaultdistance,
				player.getWorld());
		long warmup = (int) ((double) defaultwarmup / ProjectKorra.plugin.getConfig().getDouble("Properties.Fire.DayFactor"));
		if (AvatarState.isAvatarState(player))
			warmup = 0;
		if (System.currentTimeMillis() > starttime + warmup)
			charged = true;

		if (charged) {
			if (player.isSneaking()) {
				player.getWorld().playEffect(
						player.getEyeLocation(),
						Effect.SMOKE,
						Methods.getIntCardinalDirection(player.getEyeLocation()
								.getDirection()), distance);
			} else {
				strike();
			}
		} else {
			if (!player.isSneaking()) {
				instances.remove(player);
			}
		}
	}

	public void dealDamage(Entity entity) {
		if (strike == null) {
			// Methods.verbose("Null strike");
			return;
		}
		// if (Methods.isObstructed(strike.getLocation(), entity.getLocation())) {
		// Methods.verbose("Is Obstructed");
		// return 0;
		// }
		if (hitentities.contains(entity)) {
			// Methods.verbose("Already hit");
			return;
		}
		double distance = entity.getLocation().distance(strike.getLocation());
		if (distance > strikeradius)
			return;
		double damage = maxdamage - (distance / strikeradius) * .5;
		hitentities.add(entity);
		Methods.damageEntity(player, entity, (int) damage);
	}

	public static boolean isNearbyChannel(Location location) {
		boolean value = false;
		for (Player player : instances.keySet()) {
			if (!player.getWorld().equals(location.getWorld()))
				continue;
			if (player.getLocation().distance(location) <= blockdistance) {
				value = true;
				instances.get(player).starttime = 0;
			}
		}
		return value;
	}

	public static void progressAll() {
		for (Player player : instances.keySet()) {
			instances.get(player).progress();
		}
	}

	public static String getDescription() {
		return "Hold sneak while selecting this ability to charge up a lightning strike. Once "
				+ "charged, release sneak to discharge the lightning to the targetted location.";
	}

}
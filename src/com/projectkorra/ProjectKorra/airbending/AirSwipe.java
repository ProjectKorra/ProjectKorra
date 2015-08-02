package com.projectkorra.ProjectKorra.airbending;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.Commands;
import com.projectkorra.ProjectKorra.Flight;
import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.earthbending.EarthBlast;
import com.projectkorra.ProjectKorra.firebending.Combustion;
import com.projectkorra.ProjectKorra.firebending.FireBlast;
import com.projectkorra.ProjectKorra.firebending.Illumination;
import com.projectkorra.ProjectKorra.waterbending.WaterManipulation;
import com.projectkorra.ProjectKorra.waterbending.WaterMethods;

public class AirSwipe {

	private static FileConfiguration config = ProjectKorra.plugin.getConfig();
	private final int MAX_AFFECTABLE_ENTITIES = 10;
	public static ConcurrentHashMap<Integer, AirSwipe> instances = new ConcurrentHashMap<Integer, AirSwipe>();

	private static int ID = Integer.MIN_VALUE;
	private static int stepsize = 4;
	private static int ARC = config.getInt("Abilities.Air.AirSwipe.Arc");
	private static int defaultdamage = config.getInt("Abilities.Air.AirSwipe.Damage");
	private static double PUSH_FACTOR = config.getDouble("Abilities.Air.AirSwipe.Push");
	private static double AFFECTING_RADIUS = config.getDouble("Abilities.Air.AirSwipe.Radius");
	private static double RANGE = config.getDouble("Abilities.Air.AirSwipe.Range");
	private static double SPEED = config.getDouble("Abilities.Air.AirSwipe.Speed");
	private static double MAX_FACTOR = config.getDouble("Abilities.Air.AirSwipe.ChargeFactor");
	private static byte full = AirBlast.full;
	private static long MAX_CHARGE_TIME = config.getLong("Abilities.Air.AirSwipe.MaxChargeTime");

	private double speedfactor;

	private static Integer[] breakables = { 6, 31, 32, 37, 38, 39, 40, 59, 81, 83, 106, 175 };

	private Location origin;
	private Player player;
	private boolean charging = false;
	private long time;
	private double damage = defaultdamage;
	private double pushfactor = PUSH_FACTOR;
	private double speed = SPEED;
	private double range = RANGE;
	private double maxfactor = MAX_FACTOR;
	private double affectingradius = AFFECTING_RADIUS;
	private int arc = ARC;
	private long maxchargetime = MAX_CHARGE_TIME;
	private int id;
	private ConcurrentHashMap<Vector, Location> elements = new ConcurrentHashMap<Vector, Location>();
	private ArrayList<Entity> affectedentities = new ArrayList<Entity>();

	public AirSwipe(Player player) {
		this(player, false);
	}

	public AirSwipe(Player player, boolean charging) {
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());

		if (bPlayer.isOnCooldown("AirSwipe")) return;

		if (player.getEyeLocation().getBlock().isLiquid()) {
			return;
		}
		this.player = player;
		this.charging = charging;
		origin = player.getEyeLocation();
		time = System.currentTimeMillis();

		if (ID == Integer.MAX_VALUE) {
			ID = Integer.MIN_VALUE;
		}
		id = ID++;

		instances.put(id, this);

		bPlayer.addCooldown("AirSwipe", ProjectKorra.plugin.getConfig().getLong("Abilities.Air.AirSwipe.Cooldown"));

		if (!charging) launch();
	}

	private void launch() {
		origin = player.getEyeLocation();
		for (int i = -arc; i <= arc; i += stepsize) {
			double angle = Math.toRadians((double) i);
			Vector direction = player.getEyeLocation().getDirection().clone();

			double x, z, vx, vz;
			x = direction.getX();
			z = direction.getZ();

			vx = x * Math.cos(angle) - z * Math.sin(angle);
			vz = x * Math.sin(angle) + z * Math.cos(angle);

			direction.setX(vx);
			direction.setZ(vz);

			elements.put(direction, origin);
		}
	}

	public static void progressAll() {
		for (int ID : instances.keySet()) {
			instances.get(ID).progress();
		}
	}

	private boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			instances.remove(id);
			return false;
		}
		speedfactor = speed * (ProjectKorra.time_step / 1000.);
		if (!charging) {
			if (elements.isEmpty()) {
				instances.remove(id);
				return false;
			}

			advanceSwipe();
		} else {
			if (GeneralMethods.getBoundAbility(player) == null) {
				instances.remove(id);
				return false;
			}
			if (!GeneralMethods.getBoundAbility(player).equalsIgnoreCase("AirSwipe") || !GeneralMethods.canBend(player.getName(), "AirSwipe")) {
				instances.remove(id);
				return false;
			}

			if (!player.isSneaking()) {
				double factor = 1;
				if (System.currentTimeMillis() >= time + maxchargetime) {
					factor = maxfactor;
				} else if (AvatarState.isAvatarState(player)) {
					factor = AvatarState.getValue(factor);
				} else {
					factor = maxfactor * (double) (System.currentTimeMillis() - time) / (double) maxchargetime;
				}
				charging = false;
				launch();
				if (factor < 1) factor = 1;
				damage *= factor;
				pushfactor *= factor;
				return true;
			} else if (System.currentTimeMillis() >= time + maxchargetime) {
				AirMethods.playAirbendingParticles(player.getEyeLocation(), 3);
			}
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	private void advanceSwipe() {
		affectedentities.clear();
		for (Vector direction : elements.keySet()) {
			Location location = elements.get(direction);
			if (direction != null && location != null) {
				location = location.clone().add(direction.clone().multiply(speedfactor));
				elements.replace(direction, location);

				if (location.distance(origin) > range || GeneralMethods.isRegionProtectedFromBuild(player, "AirSwipe", location)) {
					elements.remove(direction);
				} else {
					AirMethods.removeAirSpouts(location, player);
					WaterMethods.removeWaterSpouts(location, player);

					double radius = FireBlast.AFFECTING_RADIUS;
					Player source = player;
					if (EarthBlast.annihilateBlasts(location, radius, source) || WaterManipulation.annihilateBlasts(location, radius, source) || FireBlast.annihilateBlasts(location, radius, source) || Combustion.removeAroundPoint(location, radius)) {
						elements.remove(direction);
						damage = 0;
						instances.remove(id);
						continue;
					}

					Block block = location.getBlock();
					for (Block testblock : GeneralMethods.getBlocksAroundPoint(location, affectingradius)) {
						if (testblock.getType() == Material.FIRE) {
							testblock.setType(Material.AIR);
						}
						if (isBlockBreakable(testblock)) {
							GeneralMethods.breakBlock(testblock);
						}
					}

					if (block.getType() != Material.AIR) {
						if (isBlockBreakable(block)) {
							GeneralMethods.breakBlock(block);
						} else {
							elements.remove(direction);
						}
						if (block.getType() == Material.LAVA || block.getType() == Material.STATIONARY_LAVA) {
							if (block.getData() == full) {
								block.setType(Material.OBSIDIAN);
							} else {
								block.setType(Material.COBBLESTONE);
							}
						}
					} else {
						AirMethods.playAirbendingParticles(location, 2);
						if (GeneralMethods.rand.nextInt(4) == 0) {
							AirMethods.playAirbendingSound(location);
						}
						affectPeople(location, direction);
					}
				}
				// } else {
				// elements.remove(direction);
			}

		}

		if (elements.isEmpty()) {
			instances.remove(id);
		}
	}

	private void affectPeople(Location location, Vector direction) {
		WaterMethods.removeWaterSpouts(location, player);
		AirMethods.removeAirSpouts(location, player);
		final List<Entity> entities = GeneralMethods.getEntitiesAroundPoint(location, affectingradius);
		final List<Entity> surroundingEntities = GeneralMethods.getEntitiesAroundPoint(location, 4);
		final Vector fDirection = direction;

		for (int i = 0; i < entities.size(); i++) {
			final Entity entity = entities.get(i);
			new BukkitRunnable() {
				public void run() {
					if (GeneralMethods.isRegionProtectedFromBuild(player, "AirSwipe", entity.getLocation())) return;
					if (entity.getEntityId() != player.getEntityId()) {
						if (entity instanceof Player) {
							if (Commands.invincible.contains(((Player) entity).getName())) return;
						}
						if (surroundingEntities.size() < MAX_AFFECTABLE_ENTITIES) {
							if (AvatarState.isAvatarState(player)) {
								GeneralMethods.setVelocity(entity, fDirection.multiply(AvatarState.getValue(pushfactor)));
							} else {
								GeneralMethods.setVelocity(entity, fDirection.multiply(pushfactor));
							}
						}
						if (entity instanceof LivingEntity && !affectedentities.contains(entity)) {
							if (damage != 0) GeneralMethods.damageEntity(player, entity, damage);
							affectedentities.add(entity);
						}
						if (entity instanceof Player) {
							new Flight((Player) entity, player);
						}
						AirMethods.breakBreathbendingHold(entity);
						if (elements.containsKey(fDirection)) {
							elements.remove(fDirection);
						}
					}
				}
			}.runTaskLater(ProjectKorra.plugin, i / MAX_AFFECTABLE_ENTITIES);
		}
	}

	@SuppressWarnings("deprecation")
	private boolean isBlockBreakable(Block block) {
		Integer id = block.getTypeId();
		if (Arrays.asList(breakables).contains(id) && !Illumination.blocks.containsKey(block)) {
			return true;
		}
		return false;
	}

	public static void charge(Player player) {
		new AirSwipe(player, true);
	}

	public static boolean removeSwipesAroundPoint(Location loc, double radius) {
		boolean removed = false;
		for (int ID : instances.keySet()) {
			AirSwipe aswipe = instances.get(ID);

			for (Vector vec : aswipe.elements.keySet()) {
				Location vectorLoc = aswipe.elements.get(vec);
				if (vectorLoc != null && vectorLoc.getWorld().equals(loc.getWorld())) {
					if (vectorLoc.distance(loc) <= radius) {
						instances.remove(aswipe.id);
						removed = true;
					}
				}
			}
		}
		return removed;
	}

	public Player getPlayer() {
		return player;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getMaxfactor() {
		return maxfactor;
	}

	public void setMaxfactor(double maxfactor) {
		this.maxfactor = maxfactor;
	}

	public double getAffectingradius() {
		return affectingradius;
	}

	public void setAffectingradius(double affectingradius) {
		this.affectingradius = affectingradius;
	}

	public int getArc() {
		return arc;
	}

	public void setArc(int arc) {
		this.arc = arc;
	}

	public long getMaxchargetime() {
		return maxchargetime;
	}

	public void setMaxchargetime(long maxchargetime) {
		this.maxchargetime = maxchargetime;
	}

	public double getPushfactor() {
		return pushfactor;
	}

	public void setPushfactor(double pushfactor) {
		this.pushfactor = pushfactor;
	}
}
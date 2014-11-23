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
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.earthbending.EarthBlast;
import com.projectkorra.ProjectKorra.firebending.Combustion;
import com.projectkorra.ProjectKorra.firebending.FireBlast;
import com.projectkorra.ProjectKorra.firebending.Illumination;
import com.projectkorra.ProjectKorra.waterbending.WaterManipulation;

public class AirSwipe {

	private static FileConfiguration config = ProjectKorra.plugin.getConfig();
	private final int MAX_AFFECTABLE_ENTITIES = 10;	
	public static ConcurrentHashMap<Integer, AirSwipe> instances = new ConcurrentHashMap<Integer, AirSwipe>();

	private static int ID = Integer.MIN_VALUE;
	private static int stepsize = 4;
	private static int arc = config.getInt("Abilities.Air.AirSwipe.Arc");
	private static int defaultdamage = config.getInt("Abilities.Air.AirSwipe.Damage");
	private static double defaultpushfactor = config.getDouble("Abilities.Air.AirSwipe.Push");
	private static double affectingradius = config.getDouble("Abilities.Air.AirSwipe.Radius");
	private static double range = config.getDouble("Abilities.Air.AirSwipe.Range");
	private static double speed = config.getDouble("Abilities.Air.AirSwipe.Speed");
	private static double maxfactor = config.getDouble("Abilities.Air.AirSwipe.ChargeFactor");
	private static byte full = AirBlast.full;
	private static long maxchargetime = config.getDouble("Abilities.Air.AirSwipe.ChargeTime");

	private double speedfactor;

	private static Integer[] breakables = { 6, 31, 32, 37, 38, 39, 40, 59, 81,
		83, 106, 175 };

	private Location origin;
	private Player player;
	private boolean charging = false;
	private long time;
	private int damage = defaultdamage;
	private double pushfactor = defaultpushfactor;
	private int id;
	private ConcurrentHashMap<Vector, Location> elements = new ConcurrentHashMap<Vector, Location>();
	private ArrayList<Entity> affectedentities = new ArrayList<Entity>();

	public AirSwipe(Player player) {
		this(player, false);
	}

	public AirSwipe(Player player, boolean charging) {
		BendingPlayer bPlayer = Methods.getBendingPlayer(player.getName());
		
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

		if (!charging)
			launch();
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
			if (Methods.getBoundAbility(player) == null) {
				instances.remove(id);
				return false;
			}
			if (!Methods.getBoundAbility(player).equalsIgnoreCase("AirSwipe") || !Methods.canBend(player.getName(), "AirSwipe")) {
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
					factor = maxfactor
							* (double) (System.currentTimeMillis() - time)
							/ (double) maxchargetime;
				}
				charging = false;
				launch();
				if (factor < 1)
					factor = 1;
				damage *= factor;
				pushfactor *= factor;
				return true;
			} else if (System.currentTimeMillis() >= time + maxchargetime) {
				Methods.playAirbendingParticles(player.getEyeLocation(), 10);
			}
		}
		return true;
	}

	private void advanceSwipe() {
		affectedentities.clear();
		for (Vector direction : elements.keySet()) {
			Location location = elements.get(direction);
			if (direction != null && location != null) {
				location = location.clone().add(direction.clone().multiply(speedfactor));
				elements.replace(direction, location);

				if (location.distance(origin) > range || Methods.isRegionProtectedFromBuild(player, "AirSwipe", location)) {
					elements.remove(direction);
				} else {
					Methods.removeSpouts(location, player);

					double radius = FireBlast.affectingradius;
					Player source = player;
					if (EarthBlast.annihilateBlasts(location, radius, source)
							|| WaterManipulation.annihilateBlasts(location,radius, source)
							|| FireBlast.annihilateBlasts(location, radius, source)
							|| Combustion.removeAroundPoint(location, radius)) {
						elements.remove(direction);
						damage = 0;
						instances.remove(id);
						continue;
					}
					
					Block block = location.getBlock();
					for (Block testblock : Methods.getBlocksAroundPoint(location, affectingradius)) {
						if (testblock.getType() == Material.FIRE) {
							testblock.setType(Material.AIR);
						}
						if (isBlockBreakable(testblock)) {
							Methods.breakBlock(testblock);
						}
					}

					if (block.getType() != Material.AIR) {
						if (isBlockBreakable(block)) {
							Methods.breakBlock(block);
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
						Methods.playAirbendingParticles(location, 10);
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
		Methods.removeSpouts(location, player);
		final List<Entity> entities = Methods.getEntitiesAroundPoint(location, affectingradius);
		final List<Entity> surroundingEntities = Methods.getEntitiesAroundPoint(location, 4);
		final Vector fDirection = direction;
		
		for(int i = 0; i < entities.size(); i++){
			final Entity entity = entities.get(i);			
			new BukkitRunnable(){
				public void run(){		
					if (Methods.isRegionProtectedFromBuild(player, "AirSwipe", entity.getLocation()))
						return;
					if (entity.getEntityId() != player.getEntityId()) {
						if (entity instanceof Player) {
							if (Commands.invincible.contains(((Player) entity).getName())) 
								return;
						}
						if(surroundingEntities.size() < MAX_AFFECTABLE_ENTITIES){
							if (AvatarState.isAvatarState(player)) {
								Methods.setVelocity(entity, fDirection.multiply(AvatarState.getValue(pushfactor)));
							} else {
								Methods.setVelocity(entity, fDirection.multiply(pushfactor));
							}
						}
						if (entity instanceof LivingEntity
								&& !affectedentities.contains(entity)) {
							if (damage != 0)
								Methods.damageEntity(player, entity, damage);
							affectedentities.add(entity);
						}
						if (entity instanceof Player) {
							new Flight((Player) entity, player);
						}
						Methods.breakBreathbendingHold(entity);
						if (elements.containsKey(fDirection)) {
							elements.remove(fDirection);
						}
					}
				}
			}.runTaskLater(ProjectKorra.plugin, i / MAX_AFFECTABLE_ENTITIES);
		}
	}

	private boolean isBlockBreakable(Block block) {
		Integer id = block.getTypeId();
		if (Arrays.asList(breakables).contains(id)
				&& !Illumination.blocks.containsKey(block)) {
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
			
			for(Vector vec : aswipe.elements.keySet()) {
				Location vectorLoc = aswipe.elements.get(vec);
				if(vectorLoc != null && vectorLoc.distance(loc) <= radius){
					instances.remove(aswipe.id);
					removed = true;
				}
			}
		}
		return removed;
	}

}

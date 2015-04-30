package com.projectkorra.ProjectKorra.airbending;

import com.projectkorra.projectkorra.*;
import com.projectkorra.projectkorra.Ability.AvatarState;
import com.projectkorra.projectkorra.Objects.HorizontalVelocityTracker;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class AirBlast {

	private static FileConfiguration config = ProjectKorra.plugin.getConfig();
	
	public static ConcurrentHashMap<Integer, AirBlast> instances = new ConcurrentHashMap<Integer, AirBlast>();
	private static ConcurrentHashMap<Player, Location> origins = new ConcurrentHashMap<Player, Location>();

	private static int ID = Integer.MIN_VALUE;
	static final int maxticks = 10000;

	public static double speed = config.getDouble("Abilities.Air.AirBlast.Speed");
	public static double defaultrange = config.getDouble("Abilities.Air.AirBlast.Range");
	public static double affectingradius = config.getDouble("Abilities.Air.AirBlast.Radius");
	public static double defaultpushfactor = config.getDouble("Abilities.Air.AirBlast.Push");
	private static double originselectrange = 10;
	static final double maxspeed = 1. / defaultpushfactor;
	// public static long interval = 2000;
	public static byte full = 0x0;

	public Location location;
	private Location origin;
	private Vector direction;
	private Player player;
	private int id;
	private double speedfactor;
	private double range = defaultrange;
	private double pushfactor = defaultpushfactor;
	private double damage = 0;
	private boolean otherorigin = false;
	private int ticks = 0;
	private boolean showParticles = true;

	private ArrayList<Block> affectedlevers = new ArrayList<Block>();
	private ArrayList<Entity> affectedentities = new ArrayList<Entity>();

	@SuppressWarnings("unused")
	private AirBurst source = null;

	// private long time;

	public AirBlast(Player player) {
		// if (timers.containsKey(player)) {
		// if (System.currentTimeMillis() < timers.get(player) + soonesttime) {
		// return;
		// }
		// }
		
		BendingPlayer bPlayer = Methods.getBendingPlayer(player.getName());
		
		if (bPlayer.isOnCooldown("AirBlast")) return;

		if (player.getEyeLocation().getBlock().isLiquid()) {
			return;
		}
		// timers.put(player, System.currentTimeMillis());
		this.player = player;
		if (origins.containsKey(player)) {
			otherorigin = true;
			origin = origins.get(player);
			origins.remove(player);
			Entity entity = Methods.getTargetedEntity(player, range, new ArrayList<Entity>());
			if (entity != null) {
				direction = Methods.getDirection(origin, entity.getLocation()).normalize();
			} else {
				direction = Methods.getDirection(origin, Methods.getTargetedLocation(player, range)).normalize();
			}
		} else {
			origin = player.getEyeLocation();
			direction = player.getEyeLocation().getDirection().normalize();
		}
		location = origin.clone();
		id = ID;
		instances.put(id, this);
		bPlayer.addCooldown("AirBlast", Methods.getGlobalCooldown());

		if (ID == Integer.MAX_VALUE)
			ID = Integer.MIN_VALUE;
		ID++;
		// time = System.currentTimeMillis();
		// timers.put(player, System.currentTimeMillis());
	}

	public AirBlast(Location location, Vector direction, Player player, double factorpush, AirBurst burst) {
		if (location.getBlock().isLiquid()) {
			return;
		}

		source = burst;

		this.player = player;
		origin = location.clone();
		this.direction = direction.clone();
		this.location = location.clone();
		id = ID;
		pushfactor *= factorpush;
		instances.put(id, this);
		if (ID == Integer.MAX_VALUE)
			ID = Integer.MIN_VALUE;
		ID++;
	}

	public static void setOrigin(Player player) {
		Location location = Methods.getTargetedLocation(player, originselectrange, Methods.nonOpaque);
		if (location.getBlock().isLiquid() || Methods.isSolid(location.getBlock()))
			return;

		if (Methods.isRegionProtectedFromBuild(player, "AirBlast", location))
			return;

		if (origins.containsKey(player)) {
			origins.replace(player, location);
		} else {
			origins.put(player, location);
		}
	}

	@SuppressWarnings("deprecation")
	public boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			instances.remove(id);
			return false;
		}

		if (Methods.isRegionProtectedFromBuild(player, "AirBlast", location)) {
			instances.remove(id);
			return false;
		}

		speedfactor = speed * (ProjectKorra.time_step / 1000.);

		ticks++;

		if (ticks > maxticks) {
			instances.remove(id);
			return false;
		}
		Block block = location.getBlock();
		for (Block testblock : Methods.getBlocksAroundPoint(location, affectingradius)) {
			if (testblock.getType() == Material.FIRE) {
				testblock.setType(Material.AIR);
				testblock.getWorld().playEffect(testblock.getLocation(), Effect.EXTINGUISH, 0);
			}
			if (((block.getType() == Material.LEVER) || (block.getType() == Material.STONE_BUTTON))
					&& !affectedlevers.contains(block)) {
				// BlockState state = block.getState();
				// Lever lever = (Lever) (state.getData());
				// lever.setPowered(!lever.isPowered());
				// state.setData(lever);
				// state.update(true, true);
				//
				// Block relative = block.getRelative(((Attachable) block
				// .getState().getData()).getFacing(), -1);
				// relative.getState().update(true, true);
				//
				// for (Block block2 : Methods.getBlocksAroundPoint(
				// relative.getLocation(), 2))
				// block2.getState().update(true, true);

				affectedlevers.add(block);
			}
		}
		if ((Methods.isSolid(block) || block.isLiquid()) && !affectedlevers.contains(block)) {
			if (block.getType() == Material.LAVA || block.getType() == Material.STATIONARY_LAVA) {
				if (block.getData() == full) {
					block.setType(Material.OBSIDIAN);
				} else {
					block.setType(Material.COBBLESTONE);
				}
			}
			instances.remove(id);
			return false;
		}
		
		/*
		 *	If a player presses shift and AirBlasts straight down then
		 *	the AirBlast's location gets messed up and reading the distance
		 *	returns Double.NaN. If we don't remove this instance then
		 *	the AirBlast will never be removed. 
		 */
		double dist = location.distance(origin);
		if (Double.isNaN(dist) || dist > range) {
			instances.remove(id);
			return false;
		}
		
		for (Entity entity : Methods.getEntitiesAroundPoint(location, affectingradius)) {
			affect(entity);
		}

		advanceLocation();

		return true;
	}

	private void advanceLocation() {
		if (showParticles)
			Methods.playAirbendingParticles(location, 10);
		if (Methods.rand.nextInt(4) == 0) {
			Methods.playAirbendingSound(location);
		}
		location = location.add(direction.clone().multiply(speedfactor));
	}

	private void affect(Entity entity) {
		boolean isUser = entity.getEntityId() == player.getEntityId();

		if (!isUser || otherorigin) {
			Vector velocity = entity.getVelocity();
			// double mag = Math.abs(velocity.getY());
			double max = maxspeed;
			double factor = pushfactor;
			if (AvatarState.isAvatarState(player)) {
				max = AvatarState.getValue(maxspeed);
				factor = AvatarState.getValue(factor);
			}

			Vector push = direction.clone();
			if (Math.abs(push.getY()) > max && !isUser) {
				if (push.getY() < 0)
					push.setY(-max);
				else
					push.setY(max);
			}

			factor *= 1 - location.distance(origin) / (2 * range);

			if (isUser && Methods.isSolid(player.getLocation().add(0, -.5, 0).getBlock())) {
				factor *= .5;
			}

			double comp = velocity.dot(push.clone().normalize());
			if (comp > factor) {
				velocity.multiply(.5);
				velocity.add(push.clone().normalize().multiply(velocity.clone().dot(push.clone().normalize())));
			} else if (comp + factor * .5 > factor) {
				velocity.add(push.clone().multiply(factor - comp));
			} else {
				velocity.add(push.clone().multiply(factor * .5));
			}
			
			if (entity instanceof Player) {
				if (Commands.invincible.contains(((Player) entity).getName())) return;
			}
			
			if(Double.isNaN(velocity.length()))
				return;
			
			Methods.setVelocity(entity, velocity);
			new HorizontalVelocityTracker(entity, player, 200l);
			entity.setFallDistance(0);
			if (!isUser && entity instanceof Player) {
				new Flight((Player) entity, player);
			}
			if (entity.getFireTicks() > 0)
				entity.getWorld().playEffect(entity.getLocation(), Effect.EXTINGUISH, 0);
			entity.setFireTicks(0);
			Methods.breakBreathbendingHold(entity);
			
			if (damage > 0 && entity instanceof LivingEntity && !entity.equals(player) && !affectedentities.contains(entity)) {
				Methods.damageEntity(player, entity, damage);
				affectedentities.add(entity);
			}
		}
	}
	
	public void setDamage(double dmg) {
		this.damage = dmg;
	}
	
	public void setShowParticles(boolean show) {
		this.showParticles = show;
	}
	
	public boolean getShowParticles() {
		return this.showParticles;
	}

	public static void progressAll() {
		for (int id : instances.keySet())
			instances.get(id).progress();
		for (Player player : origins.keySet()) {
			playOriginEffect(player);
		}
	}

	private static void playOriginEffect(Player player) {
		if (!origins.containsKey(player))
			return;
		Location origin = origins.get(player);
		if (!origin.getWorld().equals(player.getWorld())) {
			origins.remove(player);
			return;
		}

		if (Methods.getBoundAbility(player) == null) {
			origins.remove(player);
			return;
		}
		
		if (!Methods.getBoundAbility(player).equalsIgnoreCase("AirBlast") || !Methods.canBend(player.getName(), "AirBlast")) {
			origins.remove(player);
			return;
		}

		if (origin.distance(player.getEyeLocation()) > originselectrange) {
			origins.remove(player);
			return;
		}

		Methods.playAirbendingParticles(origin, 10);
//		origin.getWorld().playEffect(origin, Effect.SMOKE, 4,
//				(int) originselectrange);
	}

	public static void removeAll() {
		for (int id : instances.keySet()) {
			instances.remove(id);
		}
	}

	public Player getPlayer() {
		return player;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getPushfactor() {
		return pushfactor;
	}

	public void setPushfactor(double pushfactor) {
		this.pushfactor = pushfactor;
	}
	
	
}
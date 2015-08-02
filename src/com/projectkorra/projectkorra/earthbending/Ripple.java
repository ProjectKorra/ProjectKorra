package com.projectkorra.projectkorra.earthbending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AvatarState;
import com.projectkorra.projectkorra.airbending.AirMethods;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Ripple {

	public static ConcurrentHashMap<Integer, Ripple> instances = new ConcurrentHashMap<Integer, Ripple>();
	private static ConcurrentHashMap<Integer[], Block> blocks = new ConcurrentHashMap<Integer[], Block>();

	static final double RADIUS = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.Shockwave.Range");
	private static final double DAMAGE = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.Shockwave.Damage");
	private static int ID = Integer.MIN_VALUE;
	private static double KNOCKBACK = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.Shockwave.Knockback");

	private Player player;
	private Vector direction;
	private Location origin, location;
	private Block block1, block2, block3, block4;
	private int id;
	private int step = 0;
	private int maxstep;
	private double radius = RADIUS;
	private double damage = DAMAGE;
	private double knockback = KNOCKBACK;
	private ArrayList<Location> locations = new ArrayList<Location>();
	private ArrayList<Entity> entities = new ArrayList<Entity>();

	public Ripple(Player player, Vector direction) {
		this(player, getInitialLocation(player, direction), direction);
	}

	public Ripple(Player player, Location origin, Vector direction) {
		this.player = player;
		if (origin == null)
			return;
		this.direction = direction.clone().normalize();
		this.origin = origin.clone();
		this.location = origin.clone();

		initializeLocations();
		maxstep = locations.size();

		if (EarthMethods.isEarthbendable(player, origin.getBlock())) {
			id = ID++;
			if (ID >= Integer.MAX_VALUE)
				ID = Integer.MIN_VALUE;
			instances.put(id, this);
		}

	}

	private static Location getInitialLocation(Player player, Vector direction) {
		Location location = player.getLocation().clone().add(0, -1, 0);
		direction = direction.normalize();

		Block block1 = location.getBlock();

		while (location.getBlock().equals(block1))
			location = location.clone().add(direction);
		for (int i : new int[] { 1, 2, 3, 0, -1 }) {
			Location loc;
			loc = location.clone().add(0, i, 0);
			Block topblock = loc.getBlock();
			Block botblock = loc.clone().add(0, -1, 0).getBlock();
			if (EarthMethods.isTransparentToEarthbending(player, topblock) && EarthMethods.isEarthbendable(player, botblock)) {
				location = loc.clone().add(0, -1, 0);
				return location;
			}
		}

		return null;
	}

	private void progress() {

		if (step < maxstep) {
			Location newlocation = locations.get(step);
			Block block = location.getBlock();
			location = newlocation.clone();
			if (!newlocation.getBlock().equals(block)) {
				// if (block2 != null)
				// block1 = block2;
				// if (block3 != null)
				// block2 = block3;
				// if (block4 != null)
				// block3 = block4;
				block1 = block2;
				block2 = block3;
				block3 = block4;
				block4 = newlocation.getBlock();

				if (block1 != null)
					if (hasAnyMoved(block1)) {
						block1 = null;
					}
				if (block2 != null)
					if (hasAnyMoved(block2)) {
						block2 = null;
					}
				if (block3 != null)
					if (hasAnyMoved(block3)) {
						block3 = null;
					}
				if (block4 != null)
					if (hasAnyMoved(block4)) {
						block4 = null;
					}

				if (step == 0) {

					if (increase(block4))
						block4 = block4.getRelative(BlockFace.UP);

				} else if (step == 1) {

					if (increase(block3))
						block3 = block3.getRelative(BlockFace.UP);
					if (increase(block4))
						block4 = block4.getRelative(BlockFace.UP);

				} else if (step == 2) {

					if (decrease(block2))
						block2 = block2.getRelative(BlockFace.DOWN);
					if (increase(block3))
						block3 = block3.getRelative(BlockFace.UP);
					if (increase(block4))
						block4 = block4.getRelative(BlockFace.UP);

				} else {

					if (decrease(block1))
						block1 = block1.getRelative(BlockFace.DOWN);
					if (decrease(block2))
						block2 = block2.getRelative(BlockFace.DOWN);
					if (increase(block3))
						block3 = block3.getRelative(BlockFace.UP);
					if (increase(block4))
						block4 = block4.getRelative(BlockFace.UP);

				}
			}
		} else if (step == maxstep) {

			if (decrease(block2))
				block2 = block2.getRelative(BlockFace.DOWN);
			if (decrease(block3))
				block3 = block3.getRelative(BlockFace.DOWN);
			if (increase(block4))
				block4 = block4.getRelative(BlockFace.UP);

		} else if (step == maxstep + 1) {

			if (decrease(block3))
				block3 = block3.getRelative(BlockFace.DOWN);
			if (decrease(block4))
				block4 = block4.getRelative(BlockFace.DOWN);

		} else if (step == maxstep + 2) {

			if (decrease(block4))
				block4 = block4.getRelative(BlockFace.DOWN);
			remove();

		}

		step += 1;

		for (Entity entity : entities)
			affect(entity);
		entities.clear();

	}

	private void remove() {
		instances.remove(id);
	}

	private void initializeLocations() {
		Location location = origin.clone();
		locations.add(location);

		while (location.distance(origin) < radius) {
			location = location.clone().add(direction);
			for (int i : new int[] { 1, 2, 3, 0, -1 }) {
				Location loc;
				loc = location.clone().add(0, i, 0);
				Block topblock = loc.getBlock();
				Block botblock = loc.clone().add(0, -1, 0).getBlock();
				if (EarthMethods.isTransparentToEarthbending(player, topblock) && !topblock.isLiquid() && EarthMethods.isEarthbendable(player, botblock)) {
					location = loc.clone().add(0, -1, 0);
					locations.add(location);
					break;
				} else if (i == -1) {
					return;
				}
			}
		}
	}

	private boolean decrease(Block block) {
		if (block == null)
			return false;
		if (hasAnyMoved(block))
			return false;
		setMoved(block);
		Block botblock = block.getRelative(BlockFace.DOWN);
		int length = 1;
		if (EarthMethods.isEarthbendable(player, botblock)) {
			length = 2;
			block = botblock;
		}
		return EarthMethods.moveEarth(player, block, new Vector(0, -1, 0), length, false);
	}

	private boolean increase(Block block) {
		if (block == null)
			return false;
		if (hasAnyMoved(block))
			return false;
		setMoved(block);
		Block botblock = block.getRelative(BlockFace.DOWN);
		int length = 1;
		if (EarthMethods.isEarthbendable(player, botblock)) {
			length = 2;
		}
		if (EarthMethods.moveEarth(player, block, new Vector(0, 1, 0), length, false)) {
			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(block.getLocation().clone().add(0, 1, 0), 2)) {
				if (entity.getEntityId() != player.getEntityId() && !entities.contains(entity)) {
					if (!(entity instanceof FallingBlock))
						entities.add(entity);
				}
			}
			return true;
		}
		return false;
	}

	private void affect(Entity entity) {

		if (entity instanceof LivingEntity) {
			GeneralMethods.damageEntity(player, entity, damage);
		}

		Vector vector = direction.clone();
		vector.setY(.5);
		double knock = AvatarState.isAvatarState(player) ? AvatarState.getValue(knockback) : knockback;
		entity.setVelocity(vector.clone().normalize().multiply(knock));

		AirMethods.breakBreathbendingHold(entity);

	}

	private static void setMoved(Block block) {
		int x = block.getX();
		int z = block.getZ();
		Integer[] pair = new Integer[] { x, z };
		blocks.put(pair, block);
	}

	private static boolean hasAnyMoved(Block block) {
		int x = block.getX();
		int z = block.getZ();
		Integer[] pair = new Integer[] { x, z };
		if (blocks.containsKey(pair))
			return true;
		return false;
	}

	public static void progressAll() {
		blocks.clear();
		for (int id : instances.keySet())
			instances.get(id).progress();
	}

	public static void removeAll() {
		instances.clear();

	}

	public Player getPlayer() {
		return player;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public double getKnockback() {
		return knockback;
	}

	public void setKnockback(double knockback) {
		this.knockback = knockback;
	}

}

package com.projectkorra.ProjectKorra.firebending;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.waterbending.Plantbending;
import com.projectkorra.ProjectKorra.waterbending.WaterMethods;

public class FireStream {

	public static ConcurrentHashMap<Integer, FireStream> instances = new ConcurrentHashMap<Integer, FireStream>();
	public static ConcurrentHashMap<Block, Player> ignitedblocks = new ConcurrentHashMap<Block, Player>();
	public static ConcurrentHashMap<Block, Long> ignitedtimes = new ConcurrentHashMap<Block, Long>();
	public static ConcurrentHashMap<LivingEntity, Player> ignitedentities = new ConcurrentHashMap<LivingEntity, Player>();

	static final long soonesttime = ProjectKorra.plugin.getConfig().getLong("Properties.GlobalCooldown");

	public static int firedamage = 3;
	public static int tickdamage = 2;

	private static int ID = Integer.MIN_VALUE;
	private static double speed = 15;
	private static long interval = (long) (1000. / speed);
	private static long dissipateAfter = 400;

	private Player player;
	private Location origin;
	private Location location;
	private Vector direction;
	private int id;
	private long time;
	private double range;

	public FireStream(Location location, Vector direction, Player player, int range) {
		this.range = FireMethods.getFirebendingDayAugment(range, player.getWorld());
		this.player = player;
		origin = location.clone();
		this.location = origin.clone();
		this.direction = direction.clone();
		this.direction.setY(0);
		this.direction = this.direction.clone().normalize();
		this.location = this.location.clone().add(this.direction);
		id = ID;
		if (ID >= Integer.MAX_VALUE) {
			ID = Integer.MIN_VALUE;
		}
		ID++;
		time = System.currentTimeMillis();
		instances.put(id, this);
	}

	public static void progressAll() {
		for (int ID : instances.keySet()) {
			instances.get(ID).progress();
		}
	}

	private boolean progress() {
		if (System.currentTimeMillis() - time >= interval) {
			location = location.clone().add(direction);
			time = System.currentTimeMillis();
			if (location.distance(origin) > range) {
				remove();
				return false;
			}
			Block block = location.getBlock();
			if (isIgnitable(player, block)) {
				ignite(block);
				return true;
			} else if (isIgnitable(player, block.getRelative(BlockFace.DOWN))) {
				ignite(block.getRelative(BlockFace.DOWN));
				location = block.getRelative(BlockFace.DOWN).getLocation();
				return true;
			} else if (isIgnitable(player, block.getRelative(BlockFace.UP))) {
				ignite(block.getRelative(BlockFace.UP));
				location = block.getRelative(BlockFace.UP).getLocation();
				return true;
			} else {
				remove();
				return false;
			}

		}
		return false;
	}

	private void ignite(Block block) {
		if (WaterMethods.isPlant(block)) {
			new Plantbending(block);
		}

		block.setType(Material.FIRE);
		ignitedblocks.put(block, this.player);
		ignitedtimes.put(block, System.currentTimeMillis());
	}

	public static boolean isIgnitable(Player player, Block block) {

		Material[] overwriteable = { Material.SAPLING, Material.LONG_GRASS, Material.THIN_GLASS, Material.DEAD_BUSH, Material.YELLOW_FLOWER, Material.RED_ROSE, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.FIRE, Material.SNOW, Material.TORCH };

		if (Arrays.asList(overwriteable).contains(block.getType())) {
			return true;
		} else if (block.getType() != Material.AIR) {
			return false;
		}

		Material[] ignitable = { Material.BEDROCK, Material.BOOKSHELF, Material.BRICK, Material.CLAY, Material.CLAY_BRICK, Material.COAL_ORE, Material.COBBLESTONE, Material.DIAMOND_ORE, Material.DIAMOND_BLOCK, Material.DIRT, Material.ENDER_STONE, Material.GLOWING_REDSTONE_ORE, Material.GOLD_BLOCK, Material.GRAVEL, Material.GRASS, Material.HUGE_MUSHROOM_1, Material.HUGE_MUSHROOM_2, Material.LAPIS_BLOCK, Material.LAPIS_ORE, Material.LOG, Material.MOSSY_COBBLESTONE, Material.MYCEL, Material.NETHER_BRICK, Material.NETHERRACK, Material.OBSIDIAN, Material.REDSTONE_ORE, Material.SAND, Material.SANDSTONE, Material.SMOOTH_BRICK, Material.STONE, Material.SOUL_SAND, Material.WOOD, // Material.SNOW_BLOCK, 
		Material.WOOL, Material.LEAVES, Material.LEAVES_2, Material.MELON_BLOCK, Material.PUMPKIN, Material.JACK_O_LANTERN, Material.NOTE_BLOCK, Material.GLOWSTONE, Material.IRON_BLOCK, Material.DISPENSER, Material.SPONGE, Material.IRON_ORE, Material.GOLD_ORE, Material.COAL_BLOCK, Material.WORKBENCH, Material.HAY_BLOCK, Material.REDSTONE_LAMP_OFF, Material.REDSTONE_LAMP_ON, Material.EMERALD_ORE, Material.EMERALD_BLOCK, Material.REDSTONE_BLOCK, Material.QUARTZ_BLOCK, Material.QUARTZ_ORE, Material.STAINED_CLAY, Material.HARD_CLAY };

		Block belowblock = block.getRelative(BlockFace.DOWN);
		if (Arrays.asList(ignitable).contains(belowblock.getType())) {
			return true;
		}

		return false;
	}

	private void remove() {
		instances.remove(id);
	}

	public static void removeAll() {
		for (Block block : ignitedblocks.keySet())
			remove(block);
	}

	public static void dissipateAll() {
		if (dissipateAfter != 0) for (Block block : ignitedtimes.keySet()) {
			if (block.getType() != Material.FIRE) {
				remove(block);
			} else {
				long time = ignitedtimes.get(block);
				if (System.currentTimeMillis() > time + dissipateAfter) {
					block.setType(Material.AIR);
					remove(block);
				}
			}
		}
	}

	public static boolean progress(int ID) {
		return instances.get(ID).progress();
	}

	public static String getDescription() {
		return "This ability no longer exists.";
	}

	public static void remove(Block block) {
		if (ignitedblocks.containsKey(block)) {
			ignitedblocks.remove(block);
		}
		if (ignitedtimes.containsKey(block)) {
			ignitedtimes.remove(block);
		}

	}

	public static void removeAroundPoint(Location location, double radius) {

		for (int id : instances.keySet()) {
			FireStream stream = instances.get(id);
			if (stream.location.getWorld().equals(location.getWorld())) if (stream.location.distance(location) <= radius) instances.remove(id);
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

}

package com.projectkorra.ProjectKorra.earthbending;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;

public class CompactColumn {

	public static ConcurrentHashMap<Integer, CompactColumn> instances = new ConcurrentHashMap<Integer, CompactColumn>();
	private static ConcurrentHashMap<Block, Block> alreadydoneblocks = new ConcurrentHashMap<Block, Block>();

	private static int ID = Integer.MIN_VALUE;
	private static int height = EarthColumn.standardheight;
	private static double range = Collapse.range;
	private static double speed = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.Collapse.Speed");
	private static final Vector direction = new Vector(0, -1, 0);
	private static long interval = (long) (1000. / speed);

	private Location origin;
	private Location location;
	private Block block;
	private Player player;
	private int distance;
	private int id;
	private long time;
	private ConcurrentHashMap<Block, Block> affectedblocks = new ConcurrentHashMap<Block, Block>();

	public CompactColumn(Player player) {
		if (Collapse.cooldowns.containsKey(player.getName())) {
			if (Collapse.cooldowns.get(player.getName()) + ProjectKorra.plugin.getConfig().getLong("Properties.GlobalCooldown") >= System.currentTimeMillis()) {
				return;
			} else {
				Collapse.cooldowns.remove(player.getName());
			}
		}

		block = Methods.getEarthSourceBlock(player, range);
		if (block == null)
			return;
		origin = block.getLocation();
		location = origin.clone();
		this.player = player;
		distance = Methods.getEarthbendableBlocksLength(player, block, direction
				.clone().multiply(-1), height);

		loadAffectedBlocks();

		if (distance != 0) {
			if (canInstantiate()) {
				id = ID;
				instances.put(id, this);
				Collapse.cooldowns.put(player.getName(), System.currentTimeMillis());
				if (ID >= Integer.MAX_VALUE) {
					ID = Integer.MIN_VALUE;
				}
				ID++;
				time = System.currentTimeMillis() - interval;
			}
		}
	}

	public CompactColumn(Player player, Location origin) {
		// Methods.verbose("New compact column");
		this.origin = origin;
		this.player = player;
		block = origin.getBlock();
		// Methods.verbose(block);
		// Methods.verbose(origin);
		location = origin.clone();
		distance = Methods.getEarthbendableBlocksLength(player, block, direction
				.clone().multiply(-1), height);

		loadAffectedBlocks();

		if (distance != 0) {
			if (canInstantiate()) {
				for (Block blocki : affectedblocks.keySet()) {
					EarthColumn.resetBlock(blocki);
				}
				id = ID;
				instances.put(id, this);
				if (ID >= Integer.MAX_VALUE) {
					ID = Integer.MIN_VALUE;
				}
				ID++;
				time = System.currentTimeMillis() - interval;
			}
		}
	}

	private void loadAffectedBlocks() {
		affectedblocks.clear();
		Block thisblock;
		for (int i = 0; i <= distance; i++) {
			thisblock = block.getWorld().getBlockAt(
					location.clone().add(direction.clone().multiply(-i)));
			affectedblocks.put(thisblock, thisblock);
			if (EarthColumn.blockInAllAffectedBlocks(thisblock))
				EarthColumn.revertBlock(thisblock);
		}
	}

	private boolean blockInAffectedBlocks(Block block) {
		if (affectedblocks.containsKey(block)) {
			return true;
		}
		return false;
	}

	public static boolean blockInAllAffectedBlocks(Block block) {
		for (int ID : instances.keySet()) {
			if (instances.get(ID).blockInAffectedBlocks(block))
				return true;
		}
		return false;
	}

	public static void revertBlock(Block block) {
		for (int ID : instances.keySet()) {
			if (instances.get(ID).blockInAffectedBlocks(block)) {
				instances.get(ID).affectedblocks.remove(block);
			}
		}
	}

	private boolean canInstantiate() {
		for (Block block : affectedblocks.keySet()) {
			if (blockInAllAffectedBlocks(block)
					|| alreadydoneblocks.containsKey(block)) {
				return false;
			}
		}
		return true;
	}

	public boolean progress() {
		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();
			if (!moveEarth()) {
				for (Block blocki : affectedblocks.keySet()) {
					EarthColumn.resetBlock(blocki);
				}
				instances.remove(id);
				// for (Block block : affectedblocks.keySet()) {
				// alreadydoneblocks.put(block, block);
				// }
				return false;
			}
		}
		return true;
	}

	private boolean moveEarth() {
		Block block = location.getBlock();
		location = location.add(direction);
		if (block == null || location == null || distance == 0) {
			return false;
		}
		Methods.moveEarth(player, block, direction, distance);
		loadAffectedBlocks();

		if (location.distance(origin) >= distance) {
			return false;
		}

		return true;
	}

	public static boolean progress(int ID) {
		return instances.get(ID).progress();
	}

	public static void removeAll() {
		for (int id : instances.keySet()) {
			instances.remove(id);
		}
	}
}
package com.projectkorra.ProjectKorra.earthbending;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Utilities.BlockSource;
import com.projectkorra.ProjectKorra.Utilities.ClickType;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.concurrent.ConcurrentHashMap;

public class CompactColumn {

    public static ConcurrentHashMap<Integer, CompactColumn> instances = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Block, Block> alreadydoneblocks = new ConcurrentHashMap<>();

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
    private ConcurrentHashMap<Block, Block> affectedblocks = new ConcurrentHashMap<>();

	public CompactColumn(Player player) {
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
		if (bPlayer.isOnCooldown("Collapse")) return;

		block = BlockSource.getEarthSourceBlock(player, range, ClickType.LEFT_CLICK);
		if (block == null)
			return;
		origin = block.getLocation();
		location = origin.clone();
		this.player = player;
		distance = EarthMethods.getEarthbendableBlocksLength(player, block, direction
				.clone().multiply(-1), height);

		loadAffectedBlocks();

		if (distance != 0) {
			if (canInstantiate()) {
				id = ID;
				instances.put(id, this);
				bPlayer.addCooldown("Collapse", GeneralMethods.getGlobalCooldown());
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
		distance = EarthMethods.getEarthbendableBlocksLength(player, block, direction
				.clone().multiply(-1), height);

		loadAffectedBlocks();

		if (distance != 0) {
			if (canInstantiate()) {
                affectedblocks.keySet().forEach(EarthColumn::resetBlock);
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
        return affectedblocks.containsKey(block);
    }

	public static boolean blockInAllAffectedBlocks(Block block) {
		for (int ID : instances.keySet()) {
			if (instances.get(ID).blockInAffectedBlocks(block))
				return true;
		}
		return false;
	}

	public static void revertBlock(Block block) {
        instances.keySet().stream()
                .filter(ID -> instances.get(ID).blockInAffectedBlocks(block))
                .forEach(ID -> instances.get(ID).affectedblocks.remove(block));
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

	public static void progressAll() {
		for (int ID : instances.keySet()) {
			instances.get(ID).progress();
		}
	}
	
	private boolean progress() {
		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();
			if (!moveEarth()) {
                affectedblocks.keySet().forEach(EarthColumn::resetBlock);
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
		EarthMethods.moveEarth(player, block, direction, distance);
		loadAffectedBlocks();

        return location.distance(origin) < distance;

	}

	public static void removeAll() {
        instances.keySet().forEach(instances::remove);
    }
}
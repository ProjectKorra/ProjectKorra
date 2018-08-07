package com.projectkorra.projectkorra.util;

import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;

public class TempBlock {

	public static Map<Block, TempBlock> instances = new ConcurrentHashMap<Block, TempBlock>();
	public static final PriorityQueue<TempBlock> REVERT_QUEUE = new PriorityQueue<>(100, new Comparator<TempBlock>() {
		@Override
		public int compare(final TempBlock t1, final TempBlock t2) {
			return (int) (t1.revertTime - t2.revertTime);
		}
	});

	private final Block block;
	private byte newdata;
	private BlockState state;
	private long revertTime;
	private boolean inRevertQueue;
	private RevertTask revertTask = null;

	public TempBlock(final Block block, final Material newtype, final byte newdata) {
		this.block = block;
		this.newdata = newdata;
		if (instances.containsKey(block)) {
			final TempBlock temp = instances.get(block);
			if (newtype != temp.block.getType()) {
				temp.block.setType(newtype);
			}
			if (newdata != temp.block.getData()) {
				temp.block.setData(newdata);
				temp.newdata = newdata;
			}
			this.state = temp.state;
			instances.put(block, temp);
		} else {
			this.state = block.getState();
			instances.put(block, this);
			block.setType(newtype);
			block.setData(newdata);
		}
		if (this.state.getType() == Material.FIRE) {
			this.state.setType(Material.AIR);
		}
	}

	public static TempBlock get(final Block block) {
		if (isTempBlock(block)) {
			return instances.get(block);
		}
		return null;
	}

	public static boolean isTempBlock(final Block block) {
		return block != null ? instances.containsKey(block) : false;
	}

	public static boolean isTouchingTempBlock(final Block block) {
		final BlockFace[] faces = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN };
		for (final BlockFace face : faces) {
			if (instances.containsKey(block.getRelative(face))) {
				return true;
			}
		}
		return false;
	}

	public static void removeAll() {
		for (final Block block : instances.keySet()) {
			revertBlock(block, Material.AIR);
		}
		for (final TempBlock tempblock : REVERT_QUEUE) {
			tempblock.revertBlock();
		}
	}

	public static void removeBlock(final Block block) {
		instances.remove(block);
	}

	public static void revertBlock(final Block block, final Material defaulttype) {
		if (instances.containsKey(block)) {
			instances.get(block).revertBlock();
		} else {
			if ((defaulttype == Material.LAVA || defaulttype == Material.STATIONARY_LAVA) && GeneralMethods.isAdjacentToThreeOrMoreSources(block)) {
				block.setType(Material.LAVA);
				block.setData((byte) 0x0);
			} else if ((defaulttype == Material.WATER || defaulttype == Material.STATIONARY_WATER) && GeneralMethods.isAdjacentToThreeOrMoreSources(block)) {
				block.setType(Material.WATER);
				block.setData((byte) 0x0);
			} else {
				block.setType(defaulttype);
			}
		}
	}

	public Block getBlock() {
		return this.block;
	}

	public Location getLocation() {
		return this.block.getLocation();
	}

	public BlockState getState() {
		return this.state;
	}

	public RevertTask getRevertTask() {
		return this.revertTask;
	}

	public void setRevertTask(final RevertTask task) {
		this.revertTask = task;
	}

	public long getRevertTime() {
		return this.revertTime;
	}

	public void setRevertTime(final long revertTime) {
		if (this.inRevertQueue) {
			REVERT_QUEUE.remove(this);
		}
		this.inRevertQueue = true;
		this.revertTime = revertTime + System.currentTimeMillis();
		REVERT_QUEUE.add(this);
	}

	public void revertBlock() {
		this.state.update(true);
		instances.remove(this.block);
		if (REVERT_QUEUE.contains(this)) {
			REVERT_QUEUE.remove(this);
		}
		if (this.revertTask != null) {
			this.revertTask.run();
		}
	}

	public void setState(final BlockState newstate) {
		this.state = newstate;
	}

	public void setType(final Material material) {
		this.setType(material, this.newdata);
	}

	public void setType(final Material material, final byte data) {
		this.newdata = data;
		this.block.setType(material);
		this.block.setData(data);
	}

	public static void startReversion() {
		new BukkitRunnable() {
			@Override
			public void run() {
				final long currentTime = System.currentTimeMillis();
				while (!REVERT_QUEUE.isEmpty()) {
					final TempBlock tempBlock = REVERT_QUEUE.peek();
					if (currentTime >= tempBlock.revertTime) {
						REVERT_QUEUE.poll();
						tempBlock.revertBlock();
					} else {
						break;
					}
				}
			}
		}.runTaskTimer(ProjectKorra.plugin, 0, 1);
	}

	public interface RevertTask {
		public void run();
	}
}

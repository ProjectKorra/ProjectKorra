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
import org.bukkit.block.Container;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;

import io.papermc.lib.PaperLib;

public class TempBlock {
	public static Map<Block, TempBlock> instances = new ConcurrentHashMap<Block, TempBlock>();
	public static final PriorityQueue<TempBlock> REVERT_QUEUE = new PriorityQueue<TempBlock>(100, new Comparator<TempBlock>() {
		@Override
		public int compare(final TempBlock t1, final TempBlock t2) {
			return (int) (t1.revertTime - t2.revertTime);
		}
	});

	private final Block block;
	private BlockData newData;
	private BlockState state;
	private long revertTime;
	private boolean inRevertQueue;
	private RevertTask revertTask = null;

	public TempBlock(final Block block, final Material newtype) {
		this(block, newtype.createBlockData(), 0);
	}

	@Deprecated
	public TempBlock(final Block block, final Material newtype, final BlockData newData) {
		this(block, newData, 0);
	}
	
	public TempBlock(final Block block, final BlockData newData) {
		this(block, newData, 0);
	}

	public TempBlock(final Block block, final BlockData newData, final long revertTime) {
		this.block = block;
		this.newData = newData;

		if (instances.containsKey(block)) {
			final TempBlock temp = instances.get(block);
			if (!newData.equals(temp.block.getBlockData())) {
				temp.block.setBlockData(newData, GeneralMethods.isLightEmitting(newData.getMaterial()));
				temp.newData = newData;
			}
			this.state = temp.state;
			instances.put(block, temp);
		} else {
			this.state = block.getState();

			if (this.state instanceof Container || this.state.getType() == Material.JUKEBOX) {
				return;
			}
			instances.put(block, this);
			block.setBlockData(newData, GeneralMethods.isLightEmitting(newData.getMaterial()));
		}
		
		this.setRevertTime(revertTime);
	}

	public static TempBlock get(final Block block) {
		if (isTempBlock(block)) {
			return instances.get(block);
		}
		return null;
	}

	public static boolean isTempBlock(final Block block) {
		return block != null && instances.containsKey(block);
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
			tempblock.state.update(true, GeneralMethods.isLightEmitting(tempblock.state.getType()));
			if (tempblock.revertTask != null) {
				tempblock.revertTask.run();
			}
		}
		REVERT_QUEUE.clear();
	}

	public static void removeBlock(final Block block) {
		REVERT_QUEUE.remove(instances.get(block));
		instances.remove(block);
	}

	public static void revertBlock(final Block block, final Material defaulttype) {
		if (instances.containsKey(block)) {
			instances.get(block).revertBlock();
		} else {
			if ((defaulttype == Material.LAVA) && GeneralMethods.isAdjacentToThreeOrMoreSources(block, true)) {
				final BlockData data = Material.LAVA.createBlockData();

				if (data instanceof Levelled) {
					((Levelled) data).setLevel(0);
				}

				block.setBlockData(data, GeneralMethods.isLightEmitting(data.getMaterial()));
			} else if ((defaulttype == Material.WATER) && GeneralMethods.isAdjacentToThreeOrMoreSources(block)) {
				final BlockData data = Material.WATER.createBlockData();

				if (data instanceof Levelled) {
					((Levelled) data).setLevel(0);
				}

				block.setBlockData(data, GeneralMethods.isLightEmitting(data.getMaterial()));
			} else {
				block.setType(defaulttype, GeneralMethods.isLightEmitting(defaulttype));
			}
		}
	}

	public Block getBlock() {
		return this.block;
	}

	public BlockData getBlockData() {
		return this.newData;
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
		if(revertTime <= 0 || state instanceof Container) {
			return;
		}
		
		if (this.inRevertQueue) {
			REVERT_QUEUE.remove(this);
		}
		this.inRevertQueue = true;
		this.revertTime = revertTime + System.currentTimeMillis();
		REVERT_QUEUE.add(this);
	}

	public void revertBlock() {
		PaperLib.getChunkAtAsync(this.block.getLocation()).thenAccept(result -> this.state.update(true, GeneralMethods.isLightEmitting(this.state.getType()) || !(state.getBlockData() instanceof Bisected)));
		instances.remove(this.block);
		REVERT_QUEUE.remove(this);
		if (this.revertTask != null) {
			this.revertTask.run();
		}
	}

	public void setState(final BlockState newstate) {
		this.state = newstate;
	}

	public void setType(final Material material) {
		this.setType(material.createBlockData());
	}

	@Deprecated
	public void setType(final Material material, final BlockData data) {
		this.setType(data);
	}

	public void setType(final BlockData data) {
		this.newData = data;
		this.block.setBlockData(data, GeneralMethods.isLightEmitting(data.getMaterial()));
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

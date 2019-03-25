package com.projectkorra.projectkorra.util;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import io.papermc.lib.PaperLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TempBlock {

	public static Map<Block, TempBlock> instances = new ConcurrentHashMap<Block, TempBlock>();
	public static final PriorityQueue<TempBlock> REVERT_QUEUE = new PriorityQueue<>(100, new Comparator<TempBlock>() {
		@Override
		public int compare(final TempBlock t1, final TempBlock t2) {
			return (int) (t1.revertTime - t2.revertTime);
		}
	});

	private final Block block;
	private BlockData newdata;
	private BlockState state;
	private long revertTime;
	private boolean inRevertQueue;
	private RevertTask revertTask = null;
	public static Set<Material> physicsblocks = new HashSet<>(Arrays.asList(Material.GLOWSTONE, Material.TORCH, Material.SEA_LANTERN, Material.BEACON, Material.REDSTONE_LAMP, Material.REDSTONE_TORCH, Material.MAGMA_BLOCK, Material.LAVA, Material.JACK_O_LANTERN, Material.END_ROD));
	
	public TempBlock(final Block block, final Material newtype) {
		this(block, newtype.createBlockData());
	}

	public TempBlock(final Block block, final Material newtype, final BlockData newdata) {
		this(block, newdata);
	}

	public TempBlock(final Block block, final BlockData newdata) {
		this.block = block;
		this.newdata = newdata;
		if (instances.containsKey(block)) {
			final TempBlock temp = instances.get(block);
			if (!newdata.equals(temp.block.getBlockData())) {
				temp.block.setBlockData(newdata, physicsblocks.contains(newdata.getMaterial()));
				temp.newdata = newdata;
			}
			this.state = temp.state;
			instances.put(block, temp);
		} else {
			this.state = block.getState();
			instances.put(block, this);
			block.setBlockData(newdata, physicsblocks.contains(newdata.getMaterial()));
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
			tempblock.state.update(true, physicsblocks.contains(tempblock.state.getType()));
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
				BlockData data = Material.LAVA.createBlockData();
				
				if (data instanceof Levelled) {
					((Levelled) data).setLevel(0);
				}
				
				block.setBlockData(data, physicsblocks.contains(data.getMaterial()));
			} else if ((defaulttype == Material.WATER) && GeneralMethods.isAdjacentToThreeOrMoreSources(block)) {
				BlockData data = Material.WATER.createBlockData();
				
				if (data instanceof Levelled) {
					((Levelled) data).setLevel(0);
				}
				
				block.setBlockData(data, physicsblocks.contains(data.getMaterial()));
			} else {
				block.setType(defaulttype, physicsblocks.contains(defaulttype));
			}
		}
	}

	public Block getBlock() {
		return this.block;
	}
	
	public BlockData getBlockData() {
		return this.newdata;
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
		PaperLib.getChunkAtAsync(block.getLocation()).thenAccept(result ->
			this.state.update(true, physicsblocks.contains(this.state.getType()))
		);
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

	public void setType(final Material material, final BlockData data) {
		this.setType(data);
	}

	public void setType(final BlockData data) {
		this.newdata = data;
		this.block.setBlockData(data, physicsblocks.contains(data.getMaterial()));
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

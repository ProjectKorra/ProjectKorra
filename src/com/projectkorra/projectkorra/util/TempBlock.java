package com.projectkorra.projectkorra.util;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.FireAbility;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Snowable;
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
	private Set<TempBlock> attachedTempBlocks; //Temp Block states that should be reverted as well when the temp block expires (e.g. double blocks)
	private long revertTime;
	private boolean inRevertQueue;
	private boolean reverted;
	private RevertTask revertTask = null;
	private Optional<Ability> ability = Optional.empty(); // If we want this TempBlock to have an assigned ability created from it

	public TempBlock(final Block block, final Material newtype) {
		this(block, newtype.createBlockData(), 0);
	}

	@Deprecated
	/**
	 * Deprecated. Using the newType here is pointless.
	 */
	public TempBlock(final Block block, final Material newtype, final BlockData newData) {
		this(block, newData, 0);
	}
	
	public TempBlock(final Block block, final BlockData newData) {
		this(block, newData, 0);
	}
	
	public TempBlock(final Block block, final BlockData newData, final long revertTime, final Ability ability) {
		this(block, newData, revertTime);
		this.ability = Optional.of(ability);
	}
	
	public TempBlock(final Block block, final BlockData newData, final Ability ability) {
		this(block, newData, 0, ability);
	}

	public TempBlock(final Block block, BlockData newData, final long revertTime) {
		this.block = block;
		this.newData = newData;
		this.attachedTempBlocks = new HashSet<>();

		//Fire griefing will make the state update on its own, so we don't need to update it ourselves
		if (!FireAbility.canFireGrief() && (newData.getMaterial() == Material.FIRE || newData.getMaterial() == Material.SOUL_FIRE)) {
			newData = FireAbility.createFireState(block, newData.getMaterial() == Material.SOUL_FIRE); //Fix the blockstate looking incorrect
		}
		if (block.getType() == Material.SNOW){
			if (newData.getMaterial() == Material.AIR){
				updateSnowableBlock(block.getRelative(BlockFace.DOWN),false);
			}
		}

		if (instances.containsKey(block)) {
			final TempBlock temp = instances.get(block);
			if (!newData.equals(temp.block.getBlockData())) {
				temp.block.setBlockData(newData, applyPhysics(newData.getMaterial()));
				temp.newData = newData;
			}
			this.state = temp.state; //Set the original blockstate of the tempblock
			instances.put(block, temp);
		} else {
			this.state = block.getState();

			if (this.state instanceof Container || this.state.getType() == Material.JUKEBOX) {
				return;
			}

			instances.put(block, this);

			block.setBlockData(newData, applyPhysics(newData.getMaterial()));
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
			tempblock.state.update(true, applyPhysics(tempblock.state.getType()));
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

				block.setBlockData(data, applyPhysics(data.getMaterial()));
			} else if ((defaulttype == Material.WATER) && GeneralMethods.isAdjacentToThreeOrMoreSources(block)) {
				final BlockData data = Material.WATER.createBlockData();

				if (data instanceof Levelled) {
					((Levelled) data).setLevel(0);
				}

				block.setBlockData(data, applyPhysics(data.getMaterial()));
			} else {
				block.setType(defaulttype, applyPhysics(defaulttype));
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
	
	public Optional<Ability> getAbility() {
		return this.ability;
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
		if (!this.reverted) {
			instances.remove(this.block);
			this.reverted = true;
			PaperLib.getChunkAtAsync(this.block.getLocation()).thenAccept(result -> revertState(this.state));

			REVERT_QUEUE.remove(this);
			if (this.revertTask != null) {
				this.revertTask.run();
			}

			for (TempBlock attached : attachedTempBlocks) {
				attached.revertBlock();
			}
		}
	}

	private void revertState(BlockState state) {
		Block block = state.getBlock();
		//If the block has been changed by the time we revert (e.g. block place). Also, we ignore fire since it isn't worth the time
		if (block.getType() != this.newData.getMaterial() && block.getType() != Material.FIRE && block.getType() != Material.SOUL_FIRE) {
			//Get the drops of the original block and drop them in the world
			GeneralMethods.dropItems(block, GeneralMethods.getDrops(block, this.state.getType(), this.state.getBlockData()));
		} else {
			//Previous Material was SNOW
			if (state.getType() == Material.SNOW){
				updateSnowableBlock(block.getRelative(BlockFace.DOWN),true);
			}

			//Revert the original blockstate
			state.update(true, applyPhysics(state.getType())
					&& !(state.getBlockData() instanceof Bisected));
		}
	}

	/**
	 * Make the provided tempblock revert at the same time as the current tempblock
	 * @param tempBlock The tempblock to attach to the current tempblock
	 */
	public void addAttachedBlock(TempBlock tempBlock) {
		this.attachedTempBlocks.add(tempBlock);
		tempBlock.attachedTempBlocks.add(this);
	}

	/**
	 * @return The list of attached tempblocks
	 */
	public Set<TempBlock> getAttachedTempBlocks() {
		return attachedTempBlocks;
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
		this.block.setBlockData(data, applyPhysics(data.getMaterial()));
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

	public boolean isReverted() {
		return reverted;
	}

	public interface RevertTask {
		public void run();
	}

	/**
	 * Whether the physics should be updated or not. Fire should be updated so it can burn and spread IF
	 * FireGrief is on
	 * @param material The material to check
	 * @return True if physics should be applied
	 */
	public static boolean applyPhysics(Material material) {
		return GeneralMethods.isLightEmitting(material) || (material == Material.FIRE && FireAbility.canFireGrief());
	}

	public void updateSnowableBlock(Block b, boolean snowy){
		if (b.getBlockData() instanceof Snowable){
			final Snowable snowable = (Snowable) b.getBlockData();
			snowable.setSnowy(snowy);
			b.setBlockData(snowable);
		}
	}

}

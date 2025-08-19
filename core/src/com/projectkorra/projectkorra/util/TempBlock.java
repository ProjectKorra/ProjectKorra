package com.projectkorra.projectkorra.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Snowable;

import com.projectkorra.projectkorra.GeneralMethods;

import io.papermc.lib.PaperLib;

public class TempBlock {

	private static final Map<Block, LinkedList<TempBlock>> INSTANCES = new HashMap<>();
	/**
	 * Doesn't do anything right now
	 */
	@Deprecated(forRemoval = true)
	public static Map<Block, TempBlock> instances = new ConcurrentHashMap<>();
	private static final PriorityQueue<TempBlock> REVERT_QUEUE = new PriorityQueue<>(128, (t1, t2) -> (int) (t1.revertTime - t2.revertTime));

	private final Set<TempBlock> attachedTempBlocks; //Temp Block states that should be reverted as well when the temp block expires (e.g. double blocks)
	private final Block block;
	private BlockData newData;
	private BlockState state;
	private long revertTime;
	private boolean inRevertQueue;
	private boolean reverted;
	private Runnable revertTask = null;
	private Optional<CoreAbility> ability = Optional.empty(); // If we want this TempBlock to have an assigned ability created from it
	private boolean isBendableSource = false;
	private boolean suffocate = true;

	public TempBlock(final Block block, final Material newtype) {
		this(block, newtype.createBlockData(), 0);
	}

	/**
	 * @deprecated Using the newType here is pointless.
	 */
	@Deprecated
	public TempBlock(final Block block, final Material newtype, final BlockData newData) {
		this(block, newData, 0);
	}
	
	public TempBlock(final Block block, final BlockData newData) {
		this(block, newData, 0);
	}
	
	public TempBlock(final Block block, final BlockData newData, final long revertTime, final CoreAbility ability) {
		this(block, newData, revertTime);
		this.ability = Optional.of(ability);
	}
	
	public TempBlock(final Block block, final BlockData newData, final CoreAbility ability) {
		this(block, newData, 0, ability);
	}

	public TempBlock(final Block block, BlockData newData, final long revertTime) {
		this.block = block;
		this.newData = newData;
		this.attachedTempBlocks = new HashSet<>(0);
		// TODO: address this because suffocate will ALWAYS be false in this case, not sure how to tweak it
		this.suffocate = ability.isPresent() ? !(ability.get() instanceof WaterAbility) : false;

		//Fire griefing will make the state update on its own, so we don't need to update it ourselves
		if (!FireAbility.canFireGrief() && (newData.getMaterial() == Material.FIRE || newData.getMaterial() == Material.SOUL_FIRE)) {
			newData = FireAbility.createFireState(block, newData.getMaterial() == Material.SOUL_FIRE); //Fix the blockstate looking incorrect
		}
		if (block.getType() == Material.SNOW){
			if (newData.getMaterial() == Material.AIR){
				updateSnowableBlock(block.getRelative(BlockFace.DOWN),false);
			}
		}

		List<TempBlock> original = getAll(block);
		if (original != null && !original.isEmpty()) {
			this.state = original.getFirst().getState();
        } else {
			this.state = block.getState();
			if (this.state instanceof Container || this.state.getType() == Material.JUKEBOX) {
				return;
			}
        }
        put(block, this);
        block.setBlockData(newData, applyPhysics(newData.getMaterial()));

        this.setRevertTime(revertTime);
	}

	/**
	 * Get a TempBlock at a location
	 * @param block The block location
	 * @return The topmost TempBlock
	 */
	public static TempBlock get(final Block block) {
		if (block != null) {
			LinkedList<TempBlock> tempBlocks = INSTANCES.get(block);
			return tempBlocks != null ? tempBlocks.getLast() : null;
		}
		return null;
	}

	/**
	 * Get all TempBlocks at the given location
	 * @param block The block location
	 * @return The list of TempBlocks
	 */
	public static LinkedList<TempBlock> getAll(Block block) {
		return block == null ? null : INSTANCES.get(block);
	}

	/**
	 * Place a TempBlock in the system
	 * @param block The block location
	 * @param tempBlock The TempBlock
	 */
	private static void put(Block block, TempBlock tempBlock) {
		if (!INSTANCES.containsKey(block)) {
			INSTANCES.put(block, new LinkedList<>());
		}
		INSTANCES.get(block).add(tempBlock);
	}

	public static boolean isTempBlock(final Block block) {
		return block != null && INSTANCES.get(block) != null;
	}

	/**
	 * Is the specified block touching a TempBlock? Used to prevent physics updates
	 * for things like Water
	 * @param block The block location
	 * @return True if there is a TempBlock beside it
	 */
	public static boolean isTouchingTempBlock(final Block block) {
		final BlockFace[] faces = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN };
		for (final BlockFace face : faces) {
			if (INSTANCES.containsKey(block.getRelative(face))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Remove and revert all TempBlocks on the server. Done at server shutdown or PK reload.
	 */
	public static void removeAll() {
		for (final Block block : new HashSet<>(INSTANCES.keySet())) {
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

	public static void removeAllInWorld(World world) {
		for (final Block block : new HashSet<>(INSTANCES.keySet())) {
			if (block.getWorld() == world) {
				revertBlock(block, Material.AIR);
			}
		}
	}

	/**
	 * Remove all TempBlocks at this location. Used for when a player places a block inside a TempBlock
	 * @param block The block location
	 * @return If any TempBlocks were removed
	 */
	public static boolean removeBlock(final Block block) {
		LinkedList<TempBlock> blocks = INSTANCES.remove(block);
		if (blocks != null) {
			for (TempBlock tempBlock : blocks) {
				REVERT_QUEUE.remove(tempBlock);
			}
			return true;
		}
		return false;
	}

	/**
	 * Remove this instance from the system
	 * @param tempBlock The TempBlock to remove
	 */
	private static void remove(TempBlock tempBlock) {
		LinkedList<TempBlock> blocks = INSTANCES.get(tempBlock.block);
		if (blocks != null) {
			blocks.remove(tempBlock);
			if (blocks.isEmpty()) {
				INSTANCES.remove(tempBlock.block);
			}
		}
	}

	/**
	 * Revert all TempBlocks at this location
	 * @param block The block location
	 * @param defaultType The default material to revert to if it can't
	 */
	public static void revertBlock(final Block block, final Material defaultType) {
		if (INSTANCES.containsKey(block)) {
			//We clone the list first, then remove before reverting. The tempblock list is cloned so we get no concurrent modification exceptions
			List<TempBlock> tempBlocks = new ArrayList<>(INSTANCES.get(block));
			tempBlocks.forEach((b) -> {
				TempBlock.remove(b);
				b.trueRevertBlock();
			});
		} else {
			if ((defaultType == Material.LAVA) && GeneralMethods.isAdjacentToThreeOrMoreSources(block, true)) {
				final BlockData data = Material.LAVA.createBlockData();

				if (data instanceof Levelled levelled) {
					levelled.setLevel(0);
				}

				block.setBlockData(data, applyPhysics(data.getMaterial()));
			} else if ((defaultType == Material.WATER) && GeneralMethods.isAdjacentToThreeOrMoreSources(block)) {
				final BlockData data = Material.WATER.createBlockData();

				if (data instanceof Levelled levelled) {
					levelled.setLevel(0);
				}

				block.setBlockData(data, applyPhysics(data.getMaterial()));
			} else {
				block.setType(defaultType, applyPhysics(defaultType));
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
	
	public Optional<CoreAbility> getAbility() {
		return this.ability;
	}

	public Runnable getRevertTask() {
		return this.revertTask;
	}

	public void setRevertTask(final Runnable task) {
		this.revertTask = task;
	}

	/**
	 * Use {@link #setRevertTask(Runnable)} instead
	 */
	@Deprecated
	public void setRevertTask(final RevertTask task) {
		this.revertTask = task;
	}

	public long getRevertTime() {
		return this.revertTime;
	}

	/**
	 * Make this TempBlock revert automatically after the specified amount of time
	 * @param revertTime The time it takes to revert. In milliseconds.
	 */
	public void setRevertTime(final long revertTime) {
		if (revertTime <= 0 || state instanceof Container) {
			return;
		}
		this.revertTime = revertTime + System.currentTimeMillis();
		if (!this.inRevertQueue) {
			this.inRevertQueue = true;
			REVERT_QUEUE.add(this);
		}
	}

	/**
	 * Revert this TempBlock
	 */
	public void revertBlock() {
		if (!this.reverted) {
			remove(this);
			trueRevertBlock();
		}
	}

	/**
	 * This is used to revert the block without removing the instances from memory. Used when multiple tempblocks are to be reverted at once
	 */
	private void trueRevertBlock() {
		this.trueRevertBlock(true);
	}

	/**
	 * This is used to revert the block without removing the instances from memory. Used when multiple tempblocks are to be reverted at once
	 * @param removeFromQueue If the TempBlock should be removed from the queue. Should be false when it has already been removed from the revert queue

	 */
	private void trueRevertBlock(boolean removeFromQueue) {
		this.reverted = true;
		if (INSTANCES.containsKey(this.block)) {
			PaperLib.getChunkAtAsync(this.block.getLocation()).thenAccept(result -> {
				TempBlock last = INSTANCES.get(this.block).getLast();
				this.block.setBlockData(last.newData); //Set the block to the next in line TempBlock
			});
		} else { //Set to the original blockstate
			PaperLib.getChunkAtAsync(this.block.getLocation()).thenAccept(result -> revertState());
		}

		if (removeFromQueue) { //Remove from the queue if it's in there. We only do this when required because it is an intensive action due to the collection type
			REVERT_QUEUE.remove(this);
		}
		if (this.revertTask != null) {
			this.revertTask.run();
		}

		for (TempBlock attached : attachedTempBlocks) {
			attached.revertBlock();
		}
	}

	/**
	 * Revert the TempBlock to the proper BlockState it should be
	 */
	private void revertState() {
		Block block = this.state.getBlock();
		//If the block has been changed by the time we revert (e.g. block place). Also, we ignore fire since it isn't worth the time
		if (block.getType() != this.newData.getMaterial() && block.getType() != Material.FIRE && block.getType() != Material.SOUL_FIRE) {
			//Get the drops of the original block and drop them in the world
			GeneralMethods.dropItems(block, GeneralMethods.getDrops(block, this.state.getType(), this.state.getBlockData()));
		} else {
			//Previous Material was SNOW
			if (this.state.getType() == Material.SNOW){
				updateSnowableBlock(block.getRelative(BlockFace.DOWN), true);
			}

			//Revert the original blockstate
			this.state.update(true, applyPhysics(state.getType())
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

	/**
	 * <b>Not yet implemented. For future use.</b>
	 * @return Can this TempBlock be used as a source block
	 */
	@Experimental
	public boolean isBendableSource() {
		return isBendableSource;
	}

	/**
	 * <b>Not yet implemented. For future use.</b>
	 * Set if the TempBlock can be used as a source block
	 * @param bool If it can be used as a source block
	 */
	@Experimental
	public TempBlock setBendableSource(boolean bool) {
		this.isBendableSource = bool;
		return this;
	}

	/**
	 * @return True if the block will suffocate entities inside it
	 */
	public boolean canSuffocate() {
		return suffocate;
	}

	/**
	 * Set if the TempBlock will suffocate entities inside of it
	 * @param suffocate True if they will suffocate, false if they won't
	 */
	public TempBlock setCanSuffocate(boolean suffocate) {
		this.suffocate = suffocate;
		return this;
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
		if (isReverted())
			return;
		this.newData = data;
		this.block.setBlockData(data, applyPhysics(data.getMaterial()));
	}

	/**
	 * @return If the TempBlock has reverted
	 */
	public boolean isReverted() {
		return reverted;
	}

	public TempBlock setAbility(CoreAbility ability) {
		this.ability = Optional.of(ability);
		return this;
	}

	@Deprecated
	/**
	 * Will be removed in future. Exactly the same as a Runnable so no point having a unique class for it
	 */
	public interface RevertTask extends Runnable { }

	/**
	 * Whether the physics should be updated or not. Fire should be updated so it can burn and spread IF
	 * FireGrief is on
	 * @param material The material to check
	 * @return True if physics should be applied
	 */
	public static boolean applyPhysics(Material material) {
		return GeneralMethods.isLightEmitting(material) || (material == Material.FIRE && FireAbility.canFireGrief());
	}

	/**
	 * Update grass blocks
	 * @param b The block
	 * @param snowy If its snowy
	 */
	public void updateSnowableBlock(Block b, boolean snowy){
		if (b.getBlockData() instanceof Snowable){
			final Snowable snowable = (Snowable) b.getBlockData();
			snowable.setSnowy(snowy);
			b.setBlockData(snowable);
		}
	}

	@Override
	public String toString() {
		return "TempBlock{" +
				"block=[" + block.getX() + "," + block.getY() + "," + block.getZ() + "]" +
				", newData=" + newData.getAsString() +
				", attachedTempBlocks=" + attachedTempBlocks.size() +
				", revertTime=" + (revertTime == 0 ? "N/A" : (revertTime - System.currentTimeMillis()) + "ms") +
				", reverted=" + reverted +
				", revertTask=" + (revertTask != null) +
				", ability=" + (ability.isPresent() ? ability.get().getClass() : "null") +
				", isBendableSource=" + isBendableSource +
				", suffocate=" + suffocate +
				'}';
	}

	public static class TempBlockRevertTask implements Runnable {
		@Override
		public void run() {
			final long currentTime = System.currentTimeMillis();
			while (!REVERT_QUEUE.isEmpty()) {
				final TempBlock tempBlock = REVERT_QUEUE.peek(); //Check if the top TempBlock is ready for reverting
				if (currentTime >= tempBlock.getRevertTime()) {
					REVERT_QUEUE.poll();
					if (!tempBlock.reverted) {
						remove(tempBlock);
						tempBlock.trueRevertBlock(false); //It's already been removed from the poll(), so don't try remove it again
					}
				} else {
					break;
				}
			}
		}
	}
}

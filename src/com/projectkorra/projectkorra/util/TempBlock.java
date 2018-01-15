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
import com.projectkorra.projectkorra.ability.Ability;

public class TempBlock {

	public static Map<Block, TempBlock> instances = new ConcurrentHashMap<Block, TempBlock>();
	public static final PriorityQueue<TempBlock> REVERT_QUEUE = new PriorityQueue<>(100, new Comparator<TempBlock>() {
		@Override
		public int compare(TempBlock t1, TempBlock t2) {
			return (int) (t1.revertTime - t2.revertTime);
		}
	});

	private Block block;
//	private Material newtype;
	private byte newdata;
	private BlockState state;
	private long revertTime;
	private boolean inRevertQueue;
	private RevertTask revertTask = null;
	private boolean preventPhysics;
	/**Ability that created this tempblock*/
	private Class<? extends Ability> ability;

	/**@deprecated preventPhysics and ability have not been fully implemented throughout the code yet*/
	@Deprecated
	@SuppressWarnings("deprecation")
	public TempBlock(Block block, Material newtype, byte newdata, boolean preventPhysics, Class<? extends Ability> ability) {
		this.block = block;
		this.newdata = newdata;
		this.preventPhysics = preventPhysics;
		this.ability = ability;
//		this.newtype = newtype;
		if (instances.containsKey(block)) {
			TempBlock temp = instances.get(block);
			if (newtype != temp.block.getType()) {
				temp.block.setType(newtype);
//				temp.newtype = newtype;
			}
			if (newdata != temp.block.getData()) {
				temp.block.setData(newdata);
				temp.newdata = newdata;
			}
			state = temp.state;
			instances.put(block, temp);
		} else {
			state = block.getState();
			instances.put(block, this);
			block.setType(newtype);
			block.setData(newdata);
		}
		if (state.getType() == Material.FIRE)
			state.setType(Material.AIR);
	}
	
	/**@deprecated preventPhysics and ability has not been fully implemented throughout the code yet*/
	@Deprecated
	public TempBlock(Block block, Material newtype, byte newdata, boolean preventPhysics) {
		this(block, newtype, newdata, preventPhysics, null);
	}
	
	public TempBlock(Block block, Material newtype, byte newdata) {
		this(block, newtype, newdata, true, null);
	}

	public static TempBlock get(Block block) {
		return instances.get(block);
	}

	public static boolean isTempBlock(Block block) {
		return block != null ? instances.containsKey(block) : false;
	}

	public static boolean isTouchingTempBlock(Block block) {
		BlockFace[] faces = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN };
		for (BlockFace face : faces) {
			if (instances.containsKey(block.getRelative(face)))
				return true;
		}
		return false;
	}

	public static void removeAll() {
		for (Block block : instances.keySet()) {
			revertBlock(block, Material.AIR);
		}
		for (TempBlock tempblock : REVERT_QUEUE) {
			tempblock.revertBlock();
		}
	}

	public static void removeBlock(Block block) {
		instances.remove(block);
	}

	@SuppressWarnings("deprecation")
	public static void revertBlock(Block block, Material defaulttype) {
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
		// block.setType(defaulttype);
	}

	public Block getBlock() {
		return block;
	}
	
	
	/**This method has not been fully implemented yet, and may require significant code changes.
	  *@return Whether or not physics should be prevented (melting, dropping as an item, etc)
	  *@deprecated As of now, physics is not prevented by checking this method in PKListener,
	  *but by checking many different moves with different method names to see if any of them
	  *specify to prevent certain physics.
	  */
	
	//remove deprecation when this has been properly added in PKListener and everywhere else needed
	@Deprecated
	public boolean preventPhysics() {
		return preventPhysics;
	}
	
	/**Gets the ability that created this tempblock
	  *@return The ability that created this tempblock, or null if no ability set this tempblock
	  *@deprecated Most abilities have not used this format yet.
	  */
	//remove deprecation when this is a requirement to make a tempblock
	@Deprecated
	public Class<? extends Ability> getAbility() {
		return ability;
	}

	public Location getLocation() {
		return block.getLocation();
	}

	public BlockState getState() {
		return state;
	}
	
	public RevertTask getRevertTask() {
		return revertTask;
	}
	
	public void setRevertTask(RevertTask task) {
		this.revertTask = task;
	}

	public long getRevertTime() {
		return revertTime;
	}

	public void setRevertTime(long revertTime) {
		if (inRevertQueue) {
			REVERT_QUEUE.remove(this);
		}
		this.inRevertQueue = true;
		this.revertTime = revertTime + System.currentTimeMillis();
		REVERT_QUEUE.add(this);
	}

	public void revertBlock() {
		state.update(true);
		instances.remove(block);
		if (REVERT_QUEUE.contains(this)) {
			REVERT_QUEUE.remove(this);
		}
		if (revertTask != null) {
			revertTask.run();
		}
	}

	public void setState(BlockState newstate) {
		state = newstate;
	}

	public void setType(Material material) {
		setType(material, newdata);
	}

	@SuppressWarnings("deprecation")
	public void setType(Material material, byte data) {
//		newtype = material;
		newdata = data;
		block.setType(material);
		block.setData(data);
	}

	public static void startReversion() {
		new BukkitRunnable() {
			@Override
			public void run() {
				long currentTime = System.currentTimeMillis();
				while (!REVERT_QUEUE.isEmpty()) {
					TempBlock tempBlock = REVERT_QUEUE.peek();
					if (currentTime >= tempBlock.revertTime) {
						REVERT_QUEUE.poll();
						tempBlock.revertBlock();
						//long finish = System.currentTimeMillis();
						//Bukkit.broadcastMessage(String.valueOf(finish - currentTime));
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

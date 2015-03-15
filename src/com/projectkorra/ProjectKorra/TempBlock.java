package com.projectkorra.ProjectKorra;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;

public class TempBlock {

	public static ConcurrentHashMap<Block, TempBlock> instances = new ConcurrentHashMap<Block, TempBlock>();

	Block block;
	Material newtype;
	byte newdata;
	BlockState state;

	@SuppressWarnings("deprecation")
	public TempBlock(Block block, Material newtype, byte newdata) {
		this.block = block;
		this.newdata = newdata;
		this.newtype = newtype;
		if (instances.containsKey(block)) {
			TempBlock temp = instances.get(block);
			if (newtype != temp.newtype) {
				temp.block.setType(newtype);
				temp.newtype = newtype;
			}
			if (newdata != temp.newdata) {
				temp.block.setData(newdata);
				temp.newdata = newdata;
			}
			state = temp.state;
			instances.replace(block, temp);
		} else {
			state = block.getState();
			block.setType(newtype);
			block.setData(newdata);
			instances.put(block, this);
		}
		if (state.getType() == Material.FIRE)
			state.setType(Material.AIR);
	}

	public void revertBlock() {
		state.update(true);
		instances.remove(block);
	}

	@SuppressWarnings("deprecation")
	public static void revertBlock(Block block, Material defaulttype) {
		if (instances.containsKey(block)) {
			instances.get(block).revertBlock();
		} else {
			if ((defaulttype == Material.LAVA
					|| defaulttype == Material.STATIONARY_LAVA)
					&& Methods.isAdjacentToThreeOrMoreSources(block)) {
				block.setType(Material.LAVA);
				block.setData((byte) 0x0);
			}
			else if ((defaulttype == Material.WATER
					|| defaulttype == Material.STATIONARY_WATER )
					&& Methods.isAdjacentToThreeOrMoreSources(block)) {
				block.setType(Material.WATER);
				block.setData((byte) 0x0);
			} else {
				block.setType(defaulttype);
			}
		}
		// block.setType(defaulttype);
	}

	public static void removeBlock(Block block) {
		if (instances.containsKey(block)) {
			instances.remove(block);
		}
	}

	public static boolean isTempBlock(Block block) {
		if (instances.containsKey(block))
			return true;
		return false;
	}

	public static boolean isTouchingTempBlock(Block block) {
		BlockFace[] faces = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST,
				BlockFace.WEST, BlockFace.UP, BlockFace.DOWN };
		for (BlockFace face : faces) {
			if (instances.containsKey(block.getRelative(face)))
				return true;
		}
		return false;
	}

	public static TempBlock get(Block block) {
		if (isTempBlock(block))
			return instances.get(block);
		return null;
	}

	public Location getLocation() {
		return block.getLocation();
	}

	public Block getBlock() {
		return block;
	}

	public static void removeAll() {
		for (Block block : instances.keySet()) {
			revertBlock(block, Material.AIR);
		}

	}

	public void setType(Material material) {
		setType(material, newdata);
	}

	@SuppressWarnings("deprecation")
	public void setType(Material material, byte data) {
		newtype = material;
		newdata = data;
		block.setType(material);
		block.setData(data);
	}

}
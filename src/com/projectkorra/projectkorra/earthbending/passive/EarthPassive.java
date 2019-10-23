package com.projectkorra.projectkorra.earthbending.passive;

import org.bukkit.block.Block;

import com.projectkorra.projectkorra.util.TempBlock;

public class EarthPassive {
	public static boolean canPhysicsChange(final Block block) {
		return !TempBlock.isTempBlock(block);
	}
}

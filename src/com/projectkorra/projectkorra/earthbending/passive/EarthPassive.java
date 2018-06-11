package com.projectkorra.projectkorra.earthbending.passive;

import com.projectkorra.projectkorra.earthbending.lava.LavaSurgeWall;
import com.projectkorra.projectkorra.earthbending.lava.LavaSurgeWave;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.block.Block;

public class EarthPassive {
	public static boolean canPhysicsChange(Block block) {
		if (LavaSurgeWall.getAffectedBlocks().containsKey(block)) {
			return false;
		} else if (LavaSurgeWall.getWallBlocks().containsKey(block)) {
			return false;
		} else if (LavaSurgeWave.isBlockWave(block)) {
			return false;
		} else if (TempBlock.isTempBlock(block)) {
			return false;
		}
		
		return true;
	}

	public static boolean canFlowFromTo(Block from, Block to) {
		if (LavaSurgeWall.getAffectedBlocks().containsKey(to) || LavaSurgeWall.getAffectedBlocks().containsKey(from)) {
			return false;
		} else if (LavaSurgeWall.getWallBlocks().containsKey(to) || LavaSurgeWall.getWallBlocks().containsKey(from)) {
			return false;
		} else if (LavaSurgeWave.isBlockWave(to) || LavaSurgeWave.isBlockWave(from)) {
			return false;
		}
		
		return true;
	}
}

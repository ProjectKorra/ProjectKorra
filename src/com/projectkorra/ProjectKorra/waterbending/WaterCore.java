package com.projectkorra.ProjectKorra.waterbending;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.TempBlock;

public class WaterCore {

	public static ConcurrentHashMap<Block, Byte> frozenblocks = new ConcurrentHashMap<Block, Byte>();
	public static ConcurrentHashMap<Block, Block> waterSpoutAffectedBlocks = new ConcurrentHashMap<Block, Block>();
	public static ConcurrentHashMap<Block, Block> waterManipulationAffectedBlocks = new ConcurrentHashMap<Block, Block>();

//	public static void removeSpouts(Location loc0, double radius,
//			Player sourceplayer) {
//		if (!Methods.isAbilityInstalled("WaterSpout", "orion304")) return;
//		for (Player player : WaterSpoutAbility.instances.keySet()) {
//			if (!player.equals(sourceplayer)) {
//				Location loc1 = player.getLocation().getBlock().getLocation();
//				loc0 = loc0.getBlock().getLocation();
//				double dx = loc1.getX() - loc0.getX();
//				double dy = loc1.getY() - loc0.getY();
//				double dz = loc1.getZ() - loc0.getZ();
//
//				double distance = Math.sqrt(dx * dx + dz * dz);
//
//				if (distance <= radius && dy > 0 && dy < WaterSpoutAbility.defaultheight)
//					WaterSpoutAbility.instances.get(player).remove();
//			}
//		}
//	}

	public static void thaw(Block block) {
		if (frozenblocks.containsKey(block)) {
			byte data = frozenblocks.get(block);
			frozenblocks.remove(block);
			block.setType(Material.WATER);
			block.setData(data);
		}
	}

	public static boolean canFlowFromTo(Block from, Block to) {
		if (WaterCore.waterManipulationAffectedBlocks.containsKey(to) || WaterCore.waterManipulationAffectedBlocks.containsKey(from)) {
			return false;
		}
		if (WaterCore.waterSpoutAffectedBlocks.containsKey(to) || WaterCore.waterSpoutAffectedBlocks.containsKey(from)) {
			return false;
		}

		if (TempBlock.isTempBlock(to) || TempBlock.isTempBlock(from)) {
			return false;
		}
		if (Methods.isAdjacentToFrozenBlock(to)
				|| Methods.isAdjacentToFrozenBlock(from)) {
			return false;
		}

		return true;
	}

	public static boolean canPhysicsChange(Block block) {
		if (waterManipulationAffectedBlocks.containsKey(block))
			return false;
		if (Methods.isAbilityInstalled("WaterSpout", "orion304")) {
			if (waterSpoutAffectedBlocks.containsKey(block))
				return false;
		}
//		if (Methods.isAbilityInstalled("Surge", "orion304")) {
//			if (waterWallAffectedBlocks.containsKey(block))
//				return false;
//			if (waterwallblocks.containsKey(block))
//				return false;
////			if (WaveAbility.isBlockWave(block))
////				return false;
//		}
		if (TempBlock.isTempBlock(block))
			return false;
		if (TempBlock.isTouchingTempBlock(block))
			return false;
		return true;
	}
}

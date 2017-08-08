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
	
	/*@SuppressWarnings("deprecation")
	public static void handleMetalPassives() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			if (bPlayer != null && bPlayer.canBendPassive(Element.EARTH) && bPlayer.canMetalbend()) {
				if (player.isSneaking() && !bPlayer.isOnCooldown("MetalPassive")) {
					Block block = player.getTargetBlock((HashSet<Material>) null, 5);
					if (block == null) {
						continue;
					}
					if (block.getType() == Material.IRON_DOOR_BLOCK && !GeneralMethods.isRegionProtectedFromBuild(player, block.getLocation())) {
						if (block.getData() >= 8) {
							block = block.getRelative(BlockFace.DOWN);
						}
						if (block.getData() < 4) {
							block.setData((byte) (block.getData() + 4));
							block.getWorld().playSound(block.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 10, 1);
						} else {
							block.setData((byte) (block.getData() - 4));
							block.getWorld().playSound(block.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 10, 1);
						}
						bPlayer.addCooldown("MetalPassive", 200);
					}
				}
			}
		}
	}*/
	


}
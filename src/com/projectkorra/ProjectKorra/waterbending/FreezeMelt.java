package com.projectkorra.ProjectKorra.waterbending;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.projectkorra.ProjectKorra.TempBlock;


public class FreezeMelt {

	public static ConcurrentHashMap<Block, Byte> frozenblocks = new ConcurrentHashMap<Block, Byte>();

	public static void freeze(Player player, Block block) {
		if (TempBlock.isTempBlock(block))
			return;
		byte data = block.getData();
		block.setType(Material.ICE);
		frozenblocks.put(block, data);
	}

	public static void thaw(Block block) {
		if (frozenblocks.containsKey(block)) {
			byte data = frozenblocks.get(block);
			frozenblocks.remove(block);
			block.setType(Material.WATER);
			block.setData(data);
		}
	}

//	public static void handleFrozenBlocks() {
//		for (Block block : frozenblocks.keySet()) {
//			if (canThaw(block))
//				thaw(block);
//		}
//	}

//	public static boolean canThaw(Block block) {
//		if (frozenblocks.containsKey(block)) {
//			for (Player player : block.getWorld().getPlayers()) {
//				//				if (Tools.getBendingAbility(player) == Abilities.OctopusForm) {
//				//					if (block.getLocation().distance(player.getLocation()) <= OctopusForm.radius + 2)
//				//						return false;
//				//				}
//				if (Tools.hasAbility(player, Abilities.PhaseChange) && Tools.canBend(player, Abilities.PhaseChange)) {
//					double range = Tools.waterbendingNightAugment(defaultrange,
//							player.getWorld());
//					if (AvatarState.isAvatarState(player)) {
//						range = AvatarState.getValue(range);
//					}
//					if (block.getLocation().distance(player.getLocation()) <= range)
//						return false;
//				}
//			}
//		}
//		if (!WaterManipulation.canPhysicsChange(block))
//			return false;
//		return true;
//	}

	private static void thawAll() {
		for (Block block : frozenblocks.keySet()) {
			if (block.getType() == Material.ICE) {
				byte data = frozenblocks.get(block);
				block.setType(Material.WATER);
				block.setData(data);
				frozenblocks.remove(block);
			}
		}
	}

	public static void removeAll() {
		thawAll();
	}
}
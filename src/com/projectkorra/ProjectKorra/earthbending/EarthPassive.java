package com.projectkorra.ProjectKorra.earthbending;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;

public class EarthPassive {
	
	public static ConcurrentHashMap<Block, Long> sandblocks = new ConcurrentHashMap<Block, Long>();
	public static ConcurrentHashMap<Block, Material> sandidentities = new ConcurrentHashMap<Block, Material>();
	private static final long duration = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.Passive.Duration");

	public static boolean softenLanding(Player player) {
		Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
		if (Methods.isEarthbendable(player, block) || Methods.isTransparentToEarthbending(player, block)) {
			if (!Methods.isTransparentToEarthbending(player, block)) {
				Material type = block.getType();
				if (Methods.isSolid(block.getRelative(BlockFace.DOWN))) {
					block.setType(Material.SAND);
					if (!sandblocks.containsKey(block)) {
						sandidentities.put(block, type);
						sandblocks.put(block, System.currentTimeMillis());
					}
				}
			}
			
			for (Block affectedBlock: Methods.getBlocksAroundPoint(block.getLocation(), 2)) {
				if (Methods.isEarthbendable(player, affectedBlock)) {
					if (Methods.isSolid(affectedBlock.getRelative(BlockFace.DOWN))) {
						Material type = affectedBlock.getType();
						affectedBlock.setType(Material.SAND);
						if (!sandblocks.containsKey(affectedBlock)) {
							sandidentities.putIfAbsent(affectedBlock, type);
							sandblocks.put(affectedBlock, System.currentTimeMillis());
						}
					}
				}
			}
			return true;
		}
		
		if (Methods.isEarthbendable(player, block) || Methods.isTransparentToEarthbending(player, block)) {
			return true;
		}
		return false;
	}
	
	public static boolean isPassiveSand(Block block) {
		return (sandblocks.containsKey(block));
	}
	
	public static void revertSand(Block block) {
		Material type = sandidentities.get(block);
		sandidentities.remove(block);
		sandblocks.remove(block);
		if (block.getType() == Material.SAND) {
			block.setType(type);
		}
	}
	
	public static void revertSands() {
		for (Block block: sandblocks.keySet()) {
			if (System.currentTimeMillis() >= sandblocks.get(block) + duration) {
				revertSand(block);
			}
		}
	}
	
	public static void revertAllSand() {
		for (Block block: sandblocks.keySet()) {
			revertSand(block);
		}
	}
	
	public static void removeAll() {
		revertAllSand();
	}
}

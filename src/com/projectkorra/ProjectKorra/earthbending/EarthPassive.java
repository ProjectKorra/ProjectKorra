package com.projectkorra.ProjectKorra.earthbending;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.material.Door;

import com.projectkorra.ProjectKorra.Element;
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;

public class EarthPassive {
	
	public static ConcurrentHashMap<Block, Long> sandblocks = new ConcurrentHashMap<Block, Long>();
	public static ConcurrentHashMap<Block, Material> sandidentities = new ConcurrentHashMap<Block, Material>();
	private static final long duration = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.Passive.Duration");

	public static boolean softenLanding(Player player) {
		Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
		if (Methods.canMetalbend(player) && Methods.isMetalBlock(block)) {
			return true;
		}
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
	
	public static void handleMetalPassives() {
		for (Player player: Bukkit.getOnlinePlayers()) {
			if (Methods.canBendPassive(player.getName(), Element.Earth) && Methods.canMetalbend(player)) {
				if (player.isSneaking()) {
					Block block = player.getTargetBlock(null, 5);
					if (block == null) continue;
					if (block.getType() == Material.IRON_DOOR_BLOCK && !Methods.isRegionProtectedFromBuild(player, null, block.getLocation())) {
						if (block.getData() >= 8) {
							block = block.getRelative(BlockFace.DOWN);
						}
						
						if (block.getData() < 4) {
							block.setData((byte) (block.getData() + 4));
							block.getWorld().playSound(block.getLocation(), Sound.DOOR_CLOSE, 10, 1);
						} else {
							block.setData((byte) (block.getData() - 4));
							block.getWorld().playSound(block.getLocation(), Sound.DOOR_OPEN, 10, 1);
						}
//						Door door = (Door) block.getState().getData();
//						if (door.isTopHalf()) {
//							block = block.getRelative(BlockFace.DOWN);
//							if (door.isOpen()) {
//								door.setOpen(false);
//							} else {
//								door.setOpen(true);
//							}
//						}
					}
				}
			}
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

package com.projectkorra.projectkorra.earthbending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EarthPassive {

	private static final Map<Block, Long> SAND_BLOCKS = new ConcurrentHashMap<>();
	private static final Map<Block, MaterialData> SAND_ID_ENTITIES = new ConcurrentHashMap<>();

	@SuppressWarnings("deprecation")
	public static boolean softenLanding(Player player) {
		if (Commands.isToggledForAll && ConfigManager.defaultConfig.get().getBoolean("Properties.TogglePassivesWithAllBending")) {
			return false;
		}
		
		Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return false;
		} else if (bPlayer.canMetalbend() && ElementalAbility.isMetalBlock(block)) {
			return true;
		}
		
		if (EarthAbility.isEarthbendable(player, block) || ElementalAbility.isTransparent(player, block)) {
			if (!ElementalAbility.isTransparent(player, block)) {
				MaterialData type = block.getState().getData();
				if (GeneralMethods.isSolid(block.getRelative(BlockFace.DOWN))) {
					if (type.getItemType() == Material.RED_SANDSTONE) {
						byte data = (byte) 0x1;
						block.setType(Material.SAND);
						block.setData(data);
					} else {
						block.setType(Material.SAND);
					}
					if (!SAND_BLOCKS.containsKey(block)) {
						SAND_ID_ENTITIES.put(block, type);
						SAND_BLOCKS.put(block, System.currentTimeMillis());
					}
				}
			}

			for (Block affectedBlock : GeneralMethods.getBlocksAroundPoint(block.getLocation(), 2)) {
				if (EarthAbility.isEarthbendable(player, affectedBlock)) {
					if (GeneralMethods.isSolid(affectedBlock.getRelative(BlockFace.DOWN))) {
						MaterialData type = affectedBlock.getState().getData();
						if (type.getItemType() == Material.RED_SANDSTONE) {
							byte data = (byte) 0x1;
							affectedBlock.setType(Material.SAND);
							affectedBlock.setData(data);
						} else {
							affectedBlock.setType(Material.SAND);
						}
						if (!SAND_BLOCKS.containsKey(affectedBlock)) {
							SAND_ID_ENTITIES.put(affectedBlock, type);
							SAND_BLOCKS.put(affectedBlock, System.currentTimeMillis());
						}
					}
				}
			}
			return true;
		}

		return (TempBlock.isTempBlock(block) && EarthAbility.isEarthbendable(TempBlock.get(block).getBlock().getType())) 
				|| EarthAbility.isEarthbendable(player, block) || EarthAbility.isTransparent(player, block);
	}

	public static boolean isPassiveSand(Block block) {
		return SAND_BLOCKS.containsKey(block);
	}

	@SuppressWarnings("deprecation")
	public static void revertSand(Block block) {
		MaterialData materialdata = SAND_ID_ENTITIES.get(block);
		SAND_ID_ENTITIES.remove(block);
		SAND_BLOCKS.remove(block);
		
		if (block.getType() == Material.SAND) {
			block.setType(materialdata.getItemType());
			block.setData(materialdata.getData());
		}
	}

	@SuppressWarnings("deprecation")
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
							block.getWorld().playSound(block.getLocation(), Sound.DOOR_CLOSE, 10, 1);
						} else {
							block.setData((byte) (block.getData() - 4));
							block.getWorld().playSound(block.getLocation(), Sound.DOOR_OPEN, 10, 1);
						}

						bPlayer.addCooldown("MetalPassive", 200);
					}
				}
			}
		}
	}

	public static void revertSands() {
		for (Block block : SAND_BLOCKS.keySet()) {
			if (System.currentTimeMillis() >= SAND_BLOCKS.get(block) + getDuration()) {
				revertSand(block);
			}
		}
	}

	public static void revertAllSand() {
		for (Block block : SAND_BLOCKS.keySet()) {
			revertSand(block);
		}
	}

	public static void removeAll() {
		revertAllSand();
	}

	public static boolean canPhysicsChange(Block block) {
		if (LavaSurgeWall.getAffectedBlocks().containsKey(block)) {
			return false;
		} else if (LavaSurgeWall.getWallBlocks().containsKey(block)) {
			return false;
		} else if (LavaSurgeWave.isBlockWave(block)) {
			return false;
		} else if (TempBlock.isTempBlock(block)) {
			return false;
		} else if (TempBlock.isTouchingTempBlock(block)) {
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
		} else if (TempBlock.isTempBlock(to) || TempBlock.isTempBlock(from)) {
			return false;
		}
		return true;
	}
	
	public static Map<Block, Long> getSandBlocks() {
		return SAND_BLOCKS;
	}

	public static Map<Block, MaterialData> getSandIdEntities() {
		return SAND_ID_ENTITIES;
	}

	public static long getDuration() {
		return ConfigManager.getConfig().getLong("Abilities.Earth.Passive.Duration");
	}

	public static int getSandRunSpeed() {
		return ConfigManager.getConfig().getInt("Abilities.Earth.Passive.SandRunSpeed");
	}
}

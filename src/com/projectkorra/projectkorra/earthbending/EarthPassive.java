package com.projectkorra.projectkorra.earthbending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class EarthPassive {

	public static ConcurrentHashMap<Block, Long> sandBlocks = new ConcurrentHashMap<Block, Long>();
	public static ConcurrentHashMap<Block, MaterialData> sandIdEntities = new ConcurrentHashMap<Block, MaterialData>();

	private static final long duration = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.Passive.Duration");
	private static final int sandSpeed = ProjectKorra.plugin.getConfig().getInt("Properties.Earth.Passive.SandRunPower");

	@SuppressWarnings("deprecation")
	public static boolean softenLanding(Player player) {
		Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return false;
		} else if (bPlayer.canMetalbend() && ElementalAbility.isMetalBlock(block)) {
			return true;
		}
		
		if (EarthAbility.isEarthbendable(player, block) || ElementalAbility.isTransparentToEarthbending(player, block)) {
			if (!ElementalAbility.isTransparentToEarthbending(player, block)) {
				MaterialData type = block.getState().getData();
				if (GeneralMethods.isSolid(block.getRelative(BlockFace.DOWN))) {
					if (type.getItemType() == Material.RED_SANDSTONE) {
						byte data = (byte) 0x1;
						block.setType(Material.SAND);
						block.setData(data);
					} else {
						block.setType(Material.SAND);
					}
					if (!sandBlocks.containsKey(block)) {
						sandIdEntities.put(block, type);
						sandBlocks.put(block, System.currentTimeMillis());
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
						if (!sandBlocks.containsKey(affectedBlock)) {
							sandIdEntities.putIfAbsent(affectedBlock, type);
							sandBlocks.put(affectedBlock, System.currentTimeMillis());
						}
					}
				}
			}
			return true;
		}

		return EarthAbility.isEarthbendable(player, block) || EarthAbility.isTransparentToEarthbending(player, block);
	}

	public static boolean isPassiveSand(Block block) {
		return sandBlocks.containsKey(block);
	}

	@SuppressWarnings("deprecation")
	public static void revertSand(Block block) {
		MaterialData materialdata = sandIdEntities.get(block);
		sandIdEntities.remove(block);
		sandBlocks.remove(block);
		
		if (block.getType() == Material.SAND) {
			block.setType(materialdata.getItemType());
			block.setData(materialdata.getData());
		}
	}

	public static void sandSpeed() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			
			if (bPlayer != null) {
				if (bPlayer.canSandbend() && bPlayer.hasElement(Element.EARTH) 
						&& !bPlayer.canBendPassive(Element.AIR) && !bPlayer.canBendPassive(Element.CHI)) {
					if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.SAND
							|| player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.SANDSTONE 
							|| player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.RED_SANDSTONE) {
						player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, sandSpeed - 1));
					}
				}
			}
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
		for (Block block : sandBlocks.keySet()) {
			if (System.currentTimeMillis() >= sandBlocks.get(block) + duration) {
				revertSand(block);
			}
		}
	}

	public static void revertAllSand() {
		for (Block block : sandBlocks.keySet()) {
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
}

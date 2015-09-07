package com.projectkorra.projectkorra.earthbending;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
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

	public static ConcurrentHashMap<Block, Long> sandblocks = new ConcurrentHashMap<Block, Long>();
	public static ConcurrentHashMap<Block, MaterialData> sandidentities = new ConcurrentHashMap<Block, MaterialData>();

	private static final long duration = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.Passive.Duration");
	private static int sandspeed = ProjectKorra.plugin.getConfig().getInt("Properties.Earth.Passive.SandRunPower");

	public static boolean softenLanding(Player player) {
		Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
		if (EarthMethods.canMetalbend(player) && EarthMethods.isMetalBlock(block)) {
			return true;
		}
		if (EarthMethods.isEarthbendable(player, block) || EarthMethods.isTransparentToEarthbending(player, block)) {
			if (!EarthMethods.isTransparentToEarthbending(player, block)) {
				MaterialData type = block.getState().getData();
				if (GeneralMethods.isSolid(block.getRelative(BlockFace.DOWN))) {
					if (type.getItemType() == Material.RED_SANDSTONE) {
						byte data = (byte) 0x1;
						block.setType(Material.SAND);
						block.setData(data);
					} else {
						block.setType(Material.SAND);
					}
					if (!sandblocks.containsKey(block)) {
						sandidentities.put(block, type);
						sandblocks.put(block, System.currentTimeMillis());
					}
				}
			}

			for (Block affectedBlock : GeneralMethods.getBlocksAroundPoint(block.getLocation(), 2)) {
				if (EarthMethods.isEarthbendable(player, affectedBlock)) {
					if (GeneralMethods.isSolid(affectedBlock.getRelative(BlockFace.DOWN))) {
						MaterialData type = affectedBlock.getState().getData();
						if (type.getItemType() == Material.RED_SANDSTONE) {
							byte data = (byte) 0x1;
							affectedBlock.setType(Material.SAND);
							affectedBlock.setData(data);
						} else {
							affectedBlock.setType(Material.SAND);
						}
						if (!sandblocks.containsKey(affectedBlock)) {
							sandidentities.putIfAbsent(affectedBlock, type);
							sandblocks.put(affectedBlock, System.currentTimeMillis());
						}
					}
				}
			}
			return true;
		}

		if (EarthMethods.isEarthbendable(player, block) || EarthMethods.isTransparentToEarthbending(player, block)) {
			return true;
		}
		return false;
	}

	public static boolean isPassiveSand(Block block) {
		return (sandblocks.containsKey(block));
	}

	public static void revertSand(Block block) {
		MaterialData materialdata = sandidentities.get(block);
		sandidentities.remove(block);
		sandblocks.remove(block);
		if (block.getType() == Material.SAND) {
			block.setType(materialdata.getItemType());
			block.setData(materialdata.getData());
		}
	}

	public static void sandSpeed() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p != null && GeneralMethods.getBendingPlayer(p.getName()) != null) {
				if (EarthMethods.canSandbend(p) && GeneralMethods.getBendingPlayer(p.getName()).hasElement(Element.Earth) && !GeneralMethods.canBendPassive(p.getName(), Element.Air) && !GeneralMethods.canBendPassive(p.getName(), Element.Chi)) {
					if (p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.SAND || p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.SANDSTONE || p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.RED_SANDSTONE) {
						p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, sandspeed - 1));
					}
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public static void handleMetalPassives() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (GeneralMethods.canBendPassive(player.getName(), Element.Earth) && EarthMethods.canMetalbend(player)) {
				if (player.isSneaking() && !GeneralMethods.getBendingPlayer(player.getName()).isOnCooldown("MetalPassive")) {
					Block block = player.getTargetBlock((HashSet<Material>) null, 5);
					if (block == null)
						continue;
					if (block.getType() == Material.IRON_DOOR_BLOCK && !GeneralMethods.isRegionProtectedFromBuild(player, null, block.getLocation())) {
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

						GeneralMethods.getBendingPlayer(player.getName()).addCooldown("MetalPassive", 200);

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
		for (Block block : sandblocks.keySet()) {
			if (System.currentTimeMillis() >= sandblocks.get(block) + duration) {
				revertSand(block);
			}
		}
	}

	public static void revertAllSand() {
		for (Block block : sandblocks.keySet()) {
			revertSand(block);
		}
	}

	public static void removeAll() {
		revertAllSand();
	}

	public static boolean canPhysicsChange(Block block) {
		if (LavaWall.affectedblocks.containsKey(block))
			return false;
		if (LavaWall.wallblocks.containsKey(block))
			return false;
		if (LavaWave.isBlockWave(block))
			return false;
		if (TempBlock.isTempBlock(block))
			return false;
		if (TempBlock.isTouchingTempBlock(block))
			return false;
		return true;
	}

	public static boolean canFlowFromTo(Block from, Block to) {
		// if (to.getType() == Material.TORCH)
		// return true;
		if (LavaWall.affectedblocks.containsKey(to) || LavaWall.affectedblocks.containsKey(from)) {
			// Methods.verbose("waterwallaffectedblocks");
			return false;
		}
		if (LavaWall.wallblocks.containsKey(to) || LavaWall.wallblocks.containsKey(from)) {
			// Methods.verbose("waterwallwall");
			return false;
		}
		if (LavaWave.isBlockWave(to) || LavaWave.isBlockWave(from)) {
			// Methods.verbose("wave");
			return false;
		}
		if (TempBlock.isTempBlock(to) || TempBlock.isTempBlock(from)) {
			// Methods.verbose("tempblock");
			return false;
		}
		//		if (Methods.isAdjacentToFrozenBlock(to)
		//				|| Methods.isAdjacentToFrozenBlock(from)) {
		//			// Methods.verbose("frozen");
		//			return false;
		//		}

		return true;
	}
}

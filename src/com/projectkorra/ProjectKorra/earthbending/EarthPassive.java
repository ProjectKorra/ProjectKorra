package com.projectkorra.ProjectKorra.earthbending;

import com.projectkorra.ProjectKorra.Element;
import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.TempBlock;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class EarthPassive {

    public static ConcurrentHashMap<Block, Long> sandblocks = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Block, Material> sandidentities = new ConcurrentHashMap<>();

    private static final long duration = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.Passive.Duration");
	private static int sandspeed = ProjectKorra.plugin.getConfig().getInt("Properties.Earth.Passive.SandRunPower");

	public static boolean softenLanding(Player player) {
		Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
		if (EarthMethods.canMetalbend(player) && EarthMethods.isMetalBlock(block)) {
			return true;
		}
		if (EarthMethods.isEarthbendable(player, block) || EarthMethods.isTransparentToEarthbending(player, block)) {
			if (!EarthMethods.isTransparentToEarthbending(player, block)) {
				Material type = block.getType();
				if (GeneralMethods.isSolid(block.getRelative(BlockFace.DOWN))) {
					if (type == Material.RED_SANDSTONE) {
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

            GeneralMethods.getBlocksAroundPoint(block.getLocation(), 2).stream()
                    .filter(affectedBlock -> EarthMethods.isEarthbendable(player, affectedBlock))
                    .filter(affectedBlock -> GeneralMethods.isSolid(affectedBlock.getRelative(BlockFace.DOWN)))
                    .forEach(affectedBlock -> {
                        Material type = affectedBlock.getType();
                        if (type == Material.RED_SANDSTONE) {
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
                    });
            return true;
		}

        return EarthMethods.isEarthbendable(player, block) || EarthMethods.isTransparentToEarthbending(player, block);
    }

    public static boolean isPassiveSand(Block block) {
		return (sandblocks.containsKey(block));
	}
	
	public static void revertSand(Block block) {
		Material type = sandidentities.get(block);
		sandidentities.remove(block);
		sandblocks.remove(block);
		if (block.getType() == Material.SAND) {
			if (block.getData() == (byte) 0x1) {
				block.setType(type);
			} else {
				block.setType(type);
			}
		}
	}
	
	public static void sandSpeed() {
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p != null && GeneralMethods.getBendingPlayer(p.getName()) != null)
                .filter(p -> EarthMethods.canSandbend(p) && GeneralMethods.getBendingPlayer(p.getName()).hasElement(Element.Earth)
                        && !GeneralMethods.canBendPassive(p.getName(), Element.Air)
                        && !GeneralMethods.canBendPassive(p.getName(), Element.Chi))
                .filter(p -> p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.SAND ||
                        p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.SANDSTONE ||
                        p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.RED_SANDSTONE)
                .forEach(p -> p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, sandspeed - 1)));
    }
	
	@SuppressWarnings("deprecation")
	public static void handleMetalPassives() {
		for (Player player: Bukkit.getOnlinePlayers()) {
			if (GeneralMethods.canBendPassive(player.getName(), Element.Earth) && EarthMethods.canMetalbend(player)) {
				if (player.isSneaking() && !GeneralMethods.getBendingPlayer(player.getName()).isOnCooldown("MetalPassive")) {
					Block block = player.getTargetBlock((HashSet<Material>) null, 5);
					if (block == null) continue;
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
        sandblocks.keySet().stream()
                .filter(block -> System.currentTimeMillis() >= sandblocks.get(block) + duration)
                .forEach(com.projectkorra.ProjectKorra.earthbending.EarthPassive::revertSand);
    }
	
	public static void revertAllSand() {
        sandblocks.keySet().forEach(EarthPassive::revertSand);
    }
	
	public static void removeAll() {
		revertAllSand();
	}
	
	public static boolean canPhysicsChange(Block block) {
        return !LavaWall.affectedblocks.containsKey(block)
                && !LavaWall.wallblocks.containsKey(block)
                && !LavaWave.isBlockWave(block)
                && !TempBlock.isTempBlock(block)
                && !TempBlock.isTouchingTempBlock(block);
    }

    public static boolean canFlowFromTo(Block from, Block to) {
		// if (to.getType() == Material.TORCH)
		// return true;
		if (LavaWall.affectedblocks.containsKey(to)
				|| LavaWall.affectedblocks.containsKey(from)) {
			// Methods.verbose("waterwallaffectedblocks");
			return false;
		}
		if (LavaWall.wallblocks.containsKey(to)
				|| LavaWall.wallblocks.containsKey(from)) {
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

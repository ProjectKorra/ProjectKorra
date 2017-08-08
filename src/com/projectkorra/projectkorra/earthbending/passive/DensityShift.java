package com.projectkorra.projectkorra.earthbending.passive;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.TempBlock;

public class DensityShift extends EarthAbility implements PassiveAbility {
	private static final Map<Block, Long> SAND_BLOCKS = new ConcurrentHashMap<>();
	private static final Map<Block, MaterialData> SAND_ID_ENTITIES = new ConcurrentHashMap<>();
	
	public DensityShift(Player player) {
		super(player);
	}

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

		if (EarthAbility.isEarth(block) || ElementalAbility.isTransparent(player, block)) {
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
				if (EarthAbility.isEarth(affectedBlock)) {
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

		return (TempBlock.isTempBlock(block) && EarthAbility.isEarthbendable(TempBlock.get(block).getBlock().getType(), true, true, false)) || EarthAbility.isEarthbendable(block.getType(), true, true, false) || EarthAbility.isTransparent(player, block);
	}

	public static boolean isPassiveSand(Block block) {
		return SAND_BLOCKS.containsKey(block);
	}

	public static void revertSand(Block block) {
		MaterialData materialdata = SAND_ID_ENTITIES.get(block);
		SAND_ID_ENTITIES.remove(block);
		SAND_BLOCKS.remove(block);

		if (block.getType() == Material.SAND) {
			block.setType(materialdata.getItemType());
			block.setData(materialdata.getData());
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
	
	public static Map<Block, Long> getSandBlocks() {
		return SAND_BLOCKS;
	}

	public static Map<Block, MaterialData> getSandIdEntities() {
		return SAND_ID_ENTITIES;
	}

	public static long getDuration() {
		return ConfigManager.getConfig().getLong("Abilities.Earth.Passive.Duration");
	}
	
	@Override
	public void progress() {}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public String getName() {
		return "DensityShift";
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	@Override
	public boolean isInstantiable() {
		return false;
	}

}

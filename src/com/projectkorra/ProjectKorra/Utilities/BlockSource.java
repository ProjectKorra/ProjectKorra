package com.projectkorra.ProjectKorra.Utilities;

import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.earthbending.EarthMethods;
import com.projectkorra.ProjectKorra.waterbending.WaterMethods;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;

/**
 * BlockSource is a class that handles water and earth bending sources. When a
 * Player left clicks or presses shift the update method is called which
 * attempts to update the player's sources.
 * 
 * In this class ClickType refers to the way in which the source was selected.
 * For example, Surge has two different ways to select a source, one involving
 * shift and another involving left clicks.
 */
public class BlockSource {
	public static enum BlockSourceType {
		WATER, ICE, PLANT, EARTH, METAL, LAVA
	}

	private static HashMap<Player, HashMap<BlockSourceType, HashMap<ClickType, BlockSourceInformation>>> playerSources = new HashMap<Player, HashMap<BlockSourceType, HashMap<ClickType, BlockSourceInformation>>>();
	private static FileConfiguration config = ProjectKorra.plugin.getConfig();
	// The player should never need to grab source blocks from farther than this.
	private static double MAX_RANGE = config.getDouble("Abilities.Water.WaterManipulation.Range");

	/**
	 * Updates all of the player's sources.
	 * 
	 * @param player the player performing the bending.
	 * @param clickType either ClickType.SHIFT_DOWN or ClickType.LEFT_CLICK
	 */
	public static void update(Player player, ClickType clickType) {
		String boundAbil = GeneralMethods.getBoundAbility(player);
		if (boundAbil == null) {
			return;
		}
		if (WaterMethods.isWaterAbility(boundAbil)) {
			Block waterBlock = WaterMethods.getWaterSourceBlock(player, MAX_RANGE, true);
			if (waterBlock != null) {
				putSource(player, waterBlock, BlockSourceType.WATER, clickType);
				if (WaterMethods.isPlant(waterBlock)) {
					putSource(player, waterBlock, BlockSourceType.PLANT, clickType);
				}
				if (WaterMethods.isIcebendable(waterBlock)) {
					putSource(player, waterBlock, BlockSourceType.ICE, clickType);
				}
			}
		} else if (EarthMethods.isEarthAbility(boundAbil)) {
			Block earthBlock = EarthMethods.getEarthSourceBlock(player, MAX_RANGE);
			if (earthBlock != null) {
				putSource(player, earthBlock, BlockSourceType.EARTH, clickType);
				if (EarthMethods.isMetal(earthBlock)) {
					putSource(player, earthBlock, BlockSourceType.METAL, clickType);
				}
			}

			// We need to handle lava differently, since getEarthSourceBlock doesn't account for
			// lava. We should only select the lava source if it is closer than the earth.
			Block lavaBlock = EarthMethods.getLavaSourceBlock(player, MAX_RANGE);
			double earthDist = earthBlock != null ? earthBlock.getLocation().distanceSquared(player.getLocation()) : Double.MAX_VALUE;
			double lavaDist = lavaBlock != null ? lavaBlock.getLocation().distanceSquared(player.getLocation()) : Double.MAX_VALUE;
			if (lavaBlock != null && lavaDist <= earthDist) {
				putSource(player, null, BlockSourceType.EARTH, clickType);
				putSource(player, lavaBlock, BlockSourceType.LAVA, clickType);
			}
		}
	}

	/**
	 * Helper method to create and update a specific source
	 * 
	 * @param player a player.
	 * @param block the block that is considered a source.
	 * @param sourceType the elemental type of the block.
	 * @param clickType the type of click, either SHIFT_DOWN or LEFT_CLICK.
	 */
	private static void putSource(Player player, Block block, BlockSourceType sourceType, ClickType clickType) {
		if (!playerSources.containsKey(player)) {
			playerSources.put(player, new HashMap<BlockSourceType, HashMap<ClickType, BlockSourceInformation>>());
		}
		if (!playerSources.get(player).containsKey(sourceType)) {
			playerSources.get(player).put(sourceType, new HashMap<ClickType, BlockSourceInformation>());
		}
		BlockSourceInformation info = new BlockSourceInformation(player, block, sourceType, clickType);
		playerSources.get(player).get(sourceType).put(clickType, info);
	}

	/**
	 * Access a block source information depending on a source and click type.
	 * 
	 * @param player the player that is trying to bend.
	 * @param clickType the action that was performed to access the source,
	 *            either ClickType.SHIFT_DOWN or ClickType.LEFT_CLICK.
	 * @return a valid bendable block, or null if none was found.
	 */
	public static BlockSourceInformation getBlockSourceInformation(Player player, BlockSourceType sourceType, ClickType clickType) {
		if (!playerSources.containsKey(player)) {
			return null;
		} else if (!playerSources.get(player).containsKey(sourceType)) {
			return null;
		} else if (!playerSources.get(player).get(sourceType).containsKey(clickType)) {
			return null;
		}
		return playerSources.get(player).get(sourceType).get(clickType);
	}

	/**
	 * Access a block source information depending on a range, source, and click
	 * type.
	 * 
	 * @param player the player that is trying to bend.
	 * @param range the maximum range to access the block.
	 * @param sourceType the elemental type of block to find.
	 * @param clickType the action that was performed to access the source,
	 *            either ClickType.SHIFT_DOWN or ClickType.LEFT_CLICK.
	 * @return a valid bendable block, or null if none was found.
	 */
	public static BlockSourceInformation getValidBlockSourceInformation(Player player, double range, BlockSourceType sourceType, ClickType clickType) {
		BlockSourceInformation blockInfo = getBlockSourceInformation(player, sourceType, clickType);
		return isStillAValidSource(blockInfo, range, clickType) ? blockInfo : null;
	}

	/**
	 * Access a specific type of source block depending on a range and click
	 * type.
	 * 
	 * @param player the player that is trying to bend.
	 * @param range the maximum range to access the block.
	 * @param sourceType the elemental type of block to find.
	 * @param clickType the action that was performed to access the source,
	 *            either ClickType.SHIFT_DOWN or ClickType.LEFT_CLICK.
	 * @return a valid bendable block, or null if none was found.
	 */
	public static Block getSourceBlock(Player player, double range, BlockSourceType sourceType, ClickType clickType) {
		BlockSourceInformation info = getValidBlockSourceInformation(player, range, sourceType, clickType);
		return info != null ? info.getBlock() : null;
	}

	/**
	 * Attempts to access a Water bendable block that was recently shifted or
	 * clicked on by the player.
	 * 
	 * @param player the player that is trying to bend.
	 * @param range the maximum range to access the block.
	 * @return a valid Water bendable block, or null if none was found.
	 */
	public static Block getWaterSourceBlock(Player player, double range) {
		return getWaterSourceBlock(player, range, ClickType.LEFT_CLICK);
	}

	/**
	 * Attempts to access a Water bendable block that was recently shifted or
	 * clicked on by the player.
	 * 
	 * @param player the player that is trying to bend.
	 * @param range the maximum range to access the block.
	 * @param clickType the action that was performed to access the source,
	 *            either ClickType.SHIFT_DOWN or ClickType.LEFT_CLICK.
	 * @return a valid Water bendable block, or null if none was found.
	 */
	public static Block getWaterSourceBlock(Player player, double range, ClickType clickType) {
		return getWaterSourceBlock(player, range, clickType, true, true, true);
	}

	/**
	 * Attempts to access a Water bendable block that was recently shifted or
	 * clicked on by the player.
	 * 
	 * @param player the player that is trying to bend.
	 * @param range the maximum range to access the block.
	 * @param allowWater true if water blocks are allowed.
	 * @param allowIce true if ice blocks are allowed.
	 * @param allowPlant true if plant blocks are allowed.
	 * @return a valid Water bendable block, or null if none was found.
	 */
	public static Block getWaterSourceBlock(Player player, double range, boolean allowWater, boolean allowIce, boolean allowPlant) {
		return getWaterSourceBlock(player, range, ClickType.LEFT_CLICK, allowWater, allowIce, allowPlant);
	}

	/**
	 * Attempts to access a Water bendable block that was recently shifted or
	 * clicked on by the player.
	 * 
	 * @param player the player that is trying to bend.
	 * @param range the maximum range to access the block.
	 * @param clickType the action that was performed to access the source,
	 *            either ClickType.SHIFT_DOWN or ClickType.LEFT_CLICK.
	 * @param allowWater true if water blocks are allowed.
	 * @param allowIce true if ice blocks are allowed.
	 * @param allowPlant true if plant blocks are allowed.
	 * @return a valid Water bendable block, or null if none was found.
	 */
	public static Block getWaterSourceBlock(Player player, double range, ClickType clickType, boolean allowWater, boolean allowIce, boolean allowPlant) {
		return getWaterSourceBlock(player, range, clickType, allowWater, allowIce, allowPlant, true);
	}

	/**
	 * Attempts to access a Water bendable block that was recently shifted or
	 * clicked on by the player.
	 * 
	 * @param player the player that is trying to bend.
	 * @param range the maximum range to access the block.
	 * @param clickType the action that was performed to access the source,
	 *            either ClickType.SHIFT_DOWN or ClickType.LEFT_CLICK.
	 * @param allowWater true if water blocks are allowed.
	 * @param allowIce true if ice blocks are allowed.
	 * @param allowPlant true if plant blocks are allowed.
	 * @param allowWaterBottles true if we should look for a close water block,
	 *            that may have been created by a WaterBottle.
	 * @return a valid Water bendable block, or null if none was found.
	 */
	public static Block getWaterSourceBlock(Player player, double range, ClickType clickType, boolean allowWater, boolean allowIce, boolean allowPlant, boolean allowWaterBottles) {
		Block sourceBlock = null;
		if (allowWaterBottles) {
			// Check the block in front of the player's eyes, it may have been created by a
			// WaterBottle.
			sourceBlock = WaterMethods.getWaterSourceBlock(player, range, allowPlant);
			if (sourceBlock == null || sourceBlock.getLocation().distance(player.getEyeLocation()) > 3) {
				sourceBlock = null;
			}
		}
		if (allowWater && sourceBlock == null) {
			sourceBlock = getSourceBlock(player, range, BlockSourceType.WATER, clickType);
		}
		if (allowIce && sourceBlock == null) {
			sourceBlock = getSourceBlock(player, range, BlockSourceType.ICE, clickType);
		}
		if (allowPlant && sourceBlock == null) {
			sourceBlock = getSourceBlock(player, range, BlockSourceType.PLANT, clickType);
		}
		return sourceBlock;
	}

	/**
	 * Attempts to access a Earth bendable block that was recently shifted or
	 * clicked on by the player.
	 * 
	 * @param player the player that is trying to bend.
	 * @param range the maximum range to access the block.
	 * @param clickType the action that was performed to access the source,
	 *            either ClickType.SHIFT_DOWN or ClickType.LEFT_CLICK.
	 * @return a valid Earth bendable block, or null if none was found.
	 */
	public static Block getEarthSourceBlock(Player player, double range, ClickType clickType) {
		return getEarthSourceBlock(player, range, clickType, true);
	}

	/**
	 * Attempts to access a Earth bendable block that was recently shifted or
	 * clicked on by the player.
	 * 
	 * @param player the player that is trying to bend.
	 * @param range the maximum range to access the block.
	 * @param clickType the action that was performed to access the source,
	 *            either ClickType.SHIFT_DOWN or ClickType.LEFT_CLICK.
	 * @param allowNearbySubstitute if a valid earth source could not be found
	 *            then this method will attempt to find a nearby valid earth
	 *            block.
	 * @return a valid Earth bendable block, or null if none was found.
	 */
	public static Block getEarthSourceBlock(Player player, double range, ClickType clickType, boolean allowNearbySubstitute) {
		Block sourceBlock = getSourceBlock(player, range, BlockSourceType.EARTH, clickType);
		if (sourceBlock == null && allowNearbySubstitute) {
			BlockSourceInformation blockInfo = getBlockSourceInformation(player, BlockSourceType.EARTH, clickType);

			if (blockInfo == null) {
				return null;
			}
			Block tempBlock = blockInfo.getBlock();
			if (tempBlock == null) {
				return null;
			}

			Location loc = tempBlock.getLocation();
			sourceBlock = EarthMethods.getNearbyEarthBlock(loc, 3, 3);
			if (sourceBlock == null || !sourceBlock.getLocation().getWorld().equals(player.getWorld()) || Math.abs(sourceBlock.getLocation().distance(player.getEyeLocation())) > range || !EarthMethods.isEarthbendable(player, sourceBlock)) {
				return null;
			}
		}
		return sourceBlock;
	}

	/**
	 * Attempts to access a Lava bendable block that was recently shifted or
	 * clicked on by the player.
	 * 
	 * @param player the player that is trying to bend.
	 * @param range the maximum range to access the block.
	 * @param clickType the action that was performed to access the source,
	 *            either ClickType.SHIFT_DOWN or ClickType.LEFT_CLICK.
	 * @return a valid Lava bendable block, or null if none was found.
	 */
	public static Block getLavaSourceBlock(Player player, double range, ClickType clickType) {
		return getSourceBlock(player, range, BlockSourceType.LAVA, clickType);
	}

	/**
	 * Attempts to access a Lava bendable block or an Earth block that was
	 * recently shifted or clicked on by the player.
	 * 
	 * @param player the player that is trying to bend.
	 * @param range the maximum range to access the block.
	 * @param clickType the action that was performed to access the source,
	 *            either ClickType.SHIFT_DOWN or ClickType.LEFT_CLICK.
	 * @return a valid Earth or Lava bendable block, or null if none was found.
	 */
	public static Block getEarthOrLavaSourceBlock(Player player, double range, ClickType clickType) {
		/*
		 * When Lava is selected as a source it automatically overrides the
		 * previous Earth based source. Only one of these types can exist, so if
		 * Lava exists then we know Earth is null.
		 */
		Block earthBlock = getEarthSourceBlock(player, range, clickType);
		BlockSourceInformation lavaBlockInfo = getValidBlockSourceInformation(player, range, BlockSourceType.LAVA, clickType);
		if (earthBlock != null) {
			return earthBlock;
		} else if (lavaBlockInfo != null) {
			return lavaBlockInfo.getBlock();
		}
		return null;
	}

	/**
	 * Determines if a BlockSourceInformation is valid, depending on the players
	 * range from the source, and if the source has not been modified since the
	 * time that it was first created.
	 * 
	 * @param info the source information.
	 * @param range the maximum bending range.
	 * @return true if it is valid.
	 */
	private static boolean isStillAValidSource(BlockSourceInformation info, double range, ClickType clickType) {
		if (info == null || info.getBlock() == null) {
			return false;
		} else if (info.getClickType() != clickType) {
			return false;
		} else if (!info.getPlayer().getWorld().equals(info.getBlock().getWorld())) {
			return false;
		} else if (Math.abs(info.getPlayer().getLocation().distance(info.getBlock().getLocation())) > range) {
			return false;
		} else if (info.getSourceType() == BlockSourceType.WATER && !WaterMethods.isWaterbendable(info.getBlock(), info.getPlayer())) {
			return false;
		} else if (info.getSourceType() == BlockSourceType.ICE && !WaterMethods.isIcebendable(info.getBlock())) {
			return false;
		} else if (info.getSourceType() == BlockSourceType.PLANT && (!WaterMethods.isPlant(info.getBlock()) || !WaterMethods.isWaterbendable(info.getBlock(), info.getPlayer()))) {
			return false;
		} else if (info.getSourceType() == BlockSourceType.EARTH && !EarthMethods.isEarthbendable(info.getPlayer(), info.getBlock())) {
			return false;
		} else if (info.getSourceType() == BlockSourceType.METAL && (!EarthMethods.isMetal(info.getBlock()) || !EarthMethods.isEarthbendable(info.getPlayer(), info.getBlock()))) {
			return false;
		} else if (info.getSourceType() == BlockSourceType.LAVA && (!EarthMethods.isLava(info.getBlock()) || !EarthMethods.isLavabendable(info.getBlock(), info.getPlayer()))) {
			return false;
		}
		return true;
	}
}

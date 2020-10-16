package com.projectkorra.projectkorra.util;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;

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
	/**
	 * An enum representation of the source types available for bending
	 * abilities.
	 *
	 * @author kingbirdy
	 */
	public static enum BlockSourceType {
		WATER, ICE, PLANT, EARTH, METAL, LAVA, SNOW
	}

	private static HashMap<Player, HashMap<BlockSourceType, HashMap<ClickType, BlockSourceInformation>>> playerSources = new HashMap<Player, HashMap<BlockSourceType, HashMap<ClickType, BlockSourceInformation>>>();
	private static FileConfiguration config = ConfigManager.defaultConfig.get();
	// The player should never need to grab source blocks from farther than this.
	private static double MAX_RANGE = config.getDouble("Abilities.Water.WaterManipulation.SelectRange");

	/**
	 * Updates all of the player's sources.
	 *
	 * @param player the player performing the bending.
	 * @param clickType either {@link ClickType}.SHIFT_DOWN or
	 *            ClickType.LEFT_CLICK
	 */
	public static void update(final Player player, final ClickType clickType) {
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return;
		}

		final CoreAbility coreAbil = bPlayer.getBoundAbility();
		if (coreAbil == null) {
			return;
		}

		if (coreAbil instanceof WaterAbility) {
			final Block waterBlock = WaterAbility.getWaterSourceBlock(player, MAX_RANGE, true);
			if (waterBlock != null) {
				putSource(player, waterBlock, BlockSourceType.WATER, clickType);
				if (ElementalAbility.isPlant(waterBlock)) {
					putSource(player, waterBlock, BlockSourceType.PLANT, clickType);
				}
				if (ElementalAbility.isIce(waterBlock)) {
					putSource(player, waterBlock, BlockSourceType.ICE, clickType);
				}
				if (WaterAbility.isSnow(waterBlock)) {
					putSource(player, waterBlock, BlockSourceType.SNOW, clickType);
				}
			}
		} else if (coreAbil instanceof EarthAbility) {
			final Block earthBlock = EarthAbility.getEarthSourceBlock(player, null, MAX_RANGE);
			if (earthBlock != null) {
				putSource(player, earthBlock, BlockSourceType.EARTH, clickType);
				if (ElementalAbility.isMetal(earthBlock)) {
					putSource(player, earthBlock, BlockSourceType.METAL, clickType);
				}
			}

			// We need to handle lava differently, since getEarthSourceBlock doesn't account for lava.
			// We should only select the lava source if it is closer than the earth.
			final Block lavaBlock = EarthAbility.getLavaSourceBlock(player, MAX_RANGE);
			final double earthDist = earthBlock != null ? earthBlock.getLocation().distanceSquared(player.getLocation()) : Double.MAX_VALUE;
			final double lavaDist = lavaBlock != null ? lavaBlock.getLocation().distanceSquared(player.getLocation()) : Double.MAX_VALUE;
			if (lavaBlock != null && lavaDist <= earthDist) {
				putSource(player, null, BlockSourceType.EARTH, clickType);
				putSource(player, lavaBlock, BlockSourceType.LAVA, clickType);
			}
		}
	}

	/**
	 * Helper method to create and update a specific source.
	 *
	 * @param player a player.
	 * @param block the block that is considered a source.
	 * @param sourceType the elemental type of the block.
	 * @param clickType the type of click, either SHIFT_DOWN or LEFT_CLICK.
	 */
	private static void putSource(final Player player, final Block block, final BlockSourceType sourceType, final ClickType clickType) {
		if (!playerSources.containsKey(player)) {
			playerSources.put(player, new HashMap<BlockSourceType, HashMap<ClickType, BlockSourceInformation>>());
		}
		if (!playerSources.get(player).containsKey(sourceType)) {
			playerSources.get(player).put(sourceType, new HashMap<ClickType, BlockSourceInformation>());
		}
		final BlockSourceInformation info = new BlockSourceInformation(player, block, sourceType, clickType);
		playerSources.get(player).get(sourceType).put(clickType, info);
	}

	/**
	 * Access a block's source information, depending on a
	 * {@link BlockSourceType} and {@link ClickType}.
	 *
	 * @param player the player that is trying to bend.
	 * @param clickType the action that was performed to access the source,
	 *            either ClickType.SHIFT_DOWN or ClickType.LEFT_CLICK.
	 * @return a valid bendable block, or null if none was found.
	 */
	public static BlockSourceInformation getBlockSourceInformation(final Player player, final BlockSourceType sourceType, final ClickType clickType) {
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
	 * Access a block source information depending on a range,
	 * {@link BlockSourceType}, and {@link ClickType}.
	 *
	 * @param player the player that is trying to bend.
	 * @param range the maximum range to access the block.
	 * @param sourceType the elemental type of block to find.
	 * @param clickType the action that was performed to access the source,
	 *            either ClickType.SHIFT_DOWN or ClickType.LEFT_CLICK.
	 * @return a valid bendable block, or null if none was found.
	 */
	public static BlockSourceInformation getValidBlockSourceInformation(final Player player, final double range, final BlockSourceType sourceType, final ClickType clickType) {
		final BlockSourceInformation blockInfo = getBlockSourceInformation(player, sourceType, clickType);
		return isStillAValidSource(blockInfo, range, clickType) ? blockInfo : null;
	}

	/**
	 * Access a specific type of source block depending on a range and
	 * {@link ClickType}.
	 *
	 * @param player the player that is trying to bend.
	 * @param range the maximum range to access the block.
	 * @param sourceType the elemental type of block to find.
	 * @param clickType the action that was performed to access the source,
	 *            either ClickType.SHIFT_DOWN or ClickType.LEFT_CLICK.
	 * @return a valid bendable block, or null if none was found.
	 */
	public static Block getSourceBlock(final Player player, final double range, final BlockSourceType sourceType, final ClickType clickType) {
		final BlockSourceInformation info = getValidBlockSourceInformation(player, range, sourceType, clickType);
		if (info != null) {
			if (TempBlock.isTempBlock(info.getBlock()) && !WaterAbility.isBendableWaterTempBlock(info.getBlock()) && !EarthAbility.isBendableEarthTempBlock(info.getBlock())) {
				return null;
			}
			return info.getBlock();
		}
		return null;
	}

	/**
	 * Attempts to access a Water bendable block that was recently shifted or
	 * clicked on by the player.
	 *
	 * @param player the player that is trying to bend.
	 * @param range the maximum range to access the block.
	 * @return a valid Water bendable block, or null if none was found.
	 */
	public static Block getWaterSourceBlock(final Player player, final double range) {
		return getWaterSourceBlock(player, range, ClickType.LEFT_CLICK);
	}

	/**
	 * Attempts to access a Water bendable block that was recently shifted or
	 * clicked on by the player.
	 *
	 * @param player the player that is trying to bend.
	 * @param range the maximum range to access the block.
	 * @param clickType the action that was performed to access the source,
	 *            either {@link ClickType}.SHIFT_DOWN or ClickType.LEFT_CLICK.
	 * @return a valid Water bendable block, or null if none was found.
	 */
	public static Block getWaterSourceBlock(final Player player, final double range, final ClickType clickType) {
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
	public static Block getWaterSourceBlock(final Player player, final double range, final boolean allowWater, final boolean allowIce, final boolean allowPlant) {
		return getWaterSourceBlock(player, range, ClickType.LEFT_CLICK, allowWater, allowIce, allowPlant);
	}

	/**
	 * Attempts to access a Water bendable block that was recently shifted or
	 * clicked on by the player.
	 *
	 * @param player the player that is trying to bend.
	 * @param range the maximum range to access the block.
	 * @param clickType the action that was performed to access the source,
	 *            either {@link ClickType}.SHIFT_DOWN or ClickType.LEFT_CLICK.
	 * @param allowWater true if water blocks are allowed.
	 * @param allowIce true if ice blocks are allowed.
	 * @param allowPlant true if plant blocks are allowed.
	 * @return a valid Water bendable block, or null if none was found.
	 */
	public static Block getWaterSourceBlock(final Player player, final double range, final ClickType clickType, final boolean allowWater, final boolean allowIce, final boolean allowPlant) {
		return getWaterSourceBlock(player, range, clickType, allowWater, allowIce, allowPlant, true, true);
	}

	/**
	 * Attempts to access a Water bendable block that was recently shifted or
	 * clicked on by the player.
	 *
	 * @param player the player that is trying to bend.
	 * @param range the maximum range to access the block.
	 * @param clickType the action that was performed to access the source,
	 *            either {@link ClickType}.SHIFT_DOWN or ClickType.LEFT_CLICK.
	 * @param allowWater true if water blocks are allowed.
	 * @param allowIce true if ice blocks are allowed.
	 * @param allowPlant true if plant blocks are allowed.
	 * @param allowWaterBottles true if we should look for a close water block,
	 *            that may have been created by a WaterBottle.
	 * @return a valid Water bendable block, or null if none was found.
	 */
	public static Block getWaterSourceBlock(final Player player, final double range, final ClickType clickType, final boolean allowWater, final boolean allowIce, final boolean allowPlant, final boolean allowSnow, final boolean allowWaterBottles) {
		Block sourceBlock = null;
		if (allowWaterBottles) {
			// Check the block in front of the player's eyes, it may have been created by a WaterBottle.
			sourceBlock = WaterAbility.getWaterSourceBlock(player, range, allowPlant);
			if (sourceBlock == null || (sourceBlock.getWorld().equals(player.getWorld()) && sourceBlock.getLocation().distance(player.getEyeLocation()) > 3)) {
				sourceBlock = null;
			}
		}
		final boolean dynamic = ConfigManager.getConfig().getBoolean("Properties.Water.DynamicSourcing");
		if (dynamic && sourceBlock == null) {
			if (allowWater && sourceBlock == null) {
				sourceBlock = getSourceBlock(player, range, BlockSourceType.WATER, clickType);
			}
			if (allowIce && sourceBlock == null) {
				sourceBlock = getSourceBlock(player, range, BlockSourceType.ICE, clickType);
			}
			if (allowPlant && sourceBlock == null) {
				sourceBlock = getSourceBlock(player, range, BlockSourceType.PLANT, clickType);
			}
			if (allowSnow && sourceBlock == null) {
				sourceBlock = getSourceBlock(player, range, BlockSourceType.SNOW, clickType);
			}
		} else {
			sourceBlock = WaterAbility.getWaterSourceBlock(player, range, allowPlant);
		}
		if (sourceBlock != null && !ElementalAbility.isAir(sourceBlock.getType()) && (ElementalAbility.isWater(sourceBlock) || ElementalAbility.isPlant(sourceBlock) || WaterAbility.isSnow(sourceBlock) || ElementalAbility.isIce(sourceBlock))) {
			if (TempBlock.isTempBlock(sourceBlock) && !WaterAbility.isBendableWaterTempBlock(sourceBlock)) {
				return null;
			}
			return sourceBlock;
		}
		return null;
	}

	/**
	 * Attempts to access a Earth bendable block that was recently shifted or
	 * clicked on by the player.
	 *
	 * @param player the player that is trying to bend.
	 * @param range the maximum range to access the block.
	 * @param clickType the action that was performed to access the source,
	 *            either {@link ClickType}.SHIFT_DOWN or ClickType.LEFT_CLICK.
	 * @return a valid Earth bendable block, or null if none was found.
	 */
	public static Block getEarthSourceBlock(final Player player, final double range, final ClickType clickType) {
		return getEarthSourceBlock(player, range, clickType, true);
	}

	/**
	 * Attempts to access a Earth bendable block that was recently shifted or
	 * clicked on by the player.
	 *
	 * @param player the player that is trying to bend.
	 * @param range the maximum range to access the block.
	 * @param clickType the action that was performed to access the source,
	 *            either {@link ClickType}.SHIFT_DOWN or ClickType.LEFT_CLICK.
	 * @param allowNearbySubstitute if a valid earth source could not be found
	 *            then this method will attempt to find a nearby valid earth
	 *            block.
	 * @return a valid Earth bendable block, or null if none was found.
	 */
	public static Block getEarthSourceBlock(final Player player, final double range, final ClickType clickType, final boolean allowNearbySubstitute) {
		Block sourceBlock = getSourceBlock(player, range, BlockSourceType.EARTH, clickType);
		final boolean dynamic = ConfigManager.getConfig().getBoolean("Properties.Earth.DynamicSourcing");
		if (dynamic && sourceBlock == null && allowNearbySubstitute) {
			final BlockSourceInformation blockInfo = getBlockSourceInformation(player, BlockSourceType.EARTH, clickType);

			if (blockInfo == null) {
				return null;
			}
			final Block tempBlock = blockInfo.getBlock();
			if (tempBlock == null) {
				return null;
			}

			final Location loc = tempBlock.getLocation();
			sourceBlock = EarthAbility.getNearbyEarthBlock(loc, 3, 1);
			if (sourceBlock == null || !sourceBlock.getLocation().getWorld().equals(player.getWorld()) || Math.abs(sourceBlock.getLocation().distance(player.getEyeLocation())) > range || !EarthAbility.isEarthbendable(player, sourceBlock)) {
				return null;
			}
		} else {
			sourceBlock = getSourceBlock(player, range, BlockSourceType.EARTH, clickType);
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
	 *            either {@link ClickType}.SHIFT_DOWN or ClickType.LEFT_CLICK.
	 * @return a valid Lava bendable block, or null if none was found.
	 */
	public static Block getLavaSourceBlock(final Player player, final double range, final ClickType clickType) {
		return getSourceBlock(player, range, BlockSourceType.LAVA, clickType);
	}

	/**
	 * Attempts to access a Lava bendable block or an Earth block that was
	 * recently shifted or clicked on by the player.
	 *
	 * @param player the player that is trying to bend.
	 * @param range the maximum range to access the block.
	 * @param clickType the action that was performed to access the source,
	 *            either {@link ClickType}.SHIFT_DOWN or ClickType.LEFT_CLICK.
	 * @return a valid Earth or Lava bendable block, or null if none was found.
	 */
	public static Block getEarthOrLavaSourceBlock(final Player player, final double range, final ClickType clickType) {
		/*
		 * When Lava is selected as a source it automatically overrides the
		 * previous Earth based source. Only one of these types can exist, so if
		 * Lava exists then we know Earth is null.
		 */
		final Block earthBlock = getEarthSourceBlock(player, range, clickType);
		final BlockSourceInformation lavaBlockInfo = getValidBlockSourceInformation(player, range, BlockSourceType.LAVA, clickType);
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
	private static boolean isStillAValidSource(final BlockSourceInformation info, final double range, final ClickType clickType) {
		if (info == null || info.getBlock() == null) {
			return false;
		} else if (info.getClickType() != clickType) {
			return false;
		} else if (!info.getPlayer().getWorld().equals(info.getBlock().getWorld())) {
			return false;
		} else if (Math.abs(info.getPlayer().getLocation().distance(info.getBlock().getLocation())) > range) {
			return false;
		} else if (info.getSourceType() == BlockSourceType.WATER && !WaterAbility.isWaterbendable(info.getPlayer(), null, info.getBlock())) {
			return false;
		} else if (info.getSourceType() == BlockSourceType.ICE && !WaterAbility.isIcebendable(info.getPlayer(), info.getBlock().getType(), false)) {
			return false;
		} else if (info.getSourceType() == BlockSourceType.PLANT && (!ElementalAbility.isPlant(info.getBlock()) || !WaterAbility.isWaterbendable(info.getPlayer(), null, info.getBlock()))) {
			return false;
		} else if (info.getSourceType() == BlockSourceType.EARTH && !EarthAbility.isEarthbendable(info.getPlayer(), info.getBlock())) {
			return false;
		} else if (info.getSourceType() == BlockSourceType.METAL && (!ElementalAbility.isMetal(info.getBlock()) || !EarthAbility.isEarthbendable(info.getPlayer(), info.getBlock()))) {
			return false;
		} else if (info.getSourceType() == BlockSourceType.LAVA && (!ElementalAbility.isLava(info.getBlock()) || !EarthAbility.isLavabendable(info.getPlayer(), info.getBlock()))) {
			return false;
		}
		return true;
	}
}

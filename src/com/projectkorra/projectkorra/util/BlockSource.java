package com.projectkorra.projectkorra.util;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.earthbending.EarthMethods;
import com.projectkorra.projectkorra.waterbending.WaterMethods;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
	 * An enum representation of the source types available for bending abilities.
	 * 
	 * @author kingbirdy
	 */
	public static enum BlockSourceType {
		WATER, ICE, PLANT, EARTH, METAL, LAVA, SAND
	}

	public static List<Block> randomBlocks = new ArrayList<Block>();
	private static HashMap<Player, HashMap<BlockSourceType, HashMap<ClickType, BlockSourceInformation>>> playerSources = new HashMap<Player, HashMap<BlockSourceType, HashMap<ClickType, BlockSourceInformation>>>();
	// The player should never need to grab source blocks from farther than this.

	/**
	 * Updates all of the player's sources.
	 * 
	 * @param player the player performing the bending.
	 * @param clickType either {@link ClickType}.SHIFT_DOWN or ClickType.LEFT_CLICK
	 */
	public static void update(Player player, int selectRange, ClickType clickType) {
		String boundAbil = GeneralMethods.getBoundAbility(player);
		if (boundAbil == null) {
			return;
		}
		if (WaterMethods.isWaterAbility(boundAbil)) {
			Block waterBlock = WaterMethods.getWaterSourceBlock(player, selectRange, true, true, true);
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
			Block earthBlock = EarthMethods.getEarthSourceBlock(player, selectRange, true, true, true);
			if (earthBlock != null) {
				putSource(player, earthBlock, BlockSourceType.EARTH, clickType);
				if (EarthMethods.isSand(earthBlock)) {
					putSource(player, earthBlock, BlockSourceType.SAND, clickType);
				}
				if (EarthMethods.isMetal(earthBlock)) {
					putSource(player, earthBlock, BlockSourceType.METAL, clickType);
				}
			}

			// We need to handle lava differently, since getEarthSourceBlock doesn't account for
			// lava. We should only select the lava source if it is closer than the earth.
			Block lavaBlock = EarthMethods.getLavaSourceBlock(player, selectRange);
			double earthDist = earthBlock != null ? earthBlock.getLocation().distanceSquared(player.getLocation()) : Double.MAX_VALUE;
			double lavaDist = lavaBlock != null ? lavaBlock.getLocation().distanceSquared(player.getLocation()) : Double.MAX_VALUE;
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
	 * Access a block's source information, depending on a {@link BlockSourceType} and {@link ClickType}.
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
	 * Access a block source information depending on a range, {@link BlockSourceType}, and {@link ClickType}.
	 * 
	 * @param player the player that is trying to bend.
	 * @param range the maximum range to access the block.
	 * @param sourceType the elemental type of block to find.
	 * @param clickType the action that was performed to access the source,
	 *            either ClickType.SHIFT_DOWN or ClickType.LEFT_CLICK.
	 * @return a valid bendable block, or null if none was found.
	 */
	public static BlockSourceInformation getValidBlockSourceInformation(Player player, int range, BlockSourceType sourceType, ClickType clickType) {
		BlockSourceInformation blockInfo = getBlockSourceInformation(player, sourceType, clickType);
		return isStillAValidSource(blockInfo, range, clickType) ? blockInfo : null;
	}

	/**
	 * Access a specific type of source block depending on a range and {@link ClickType}.
	 * 
	 * @param player the player that is trying to bend.
	 * @param range the maximum range to access the block.
	 * @param sourceType the elemental type of block to find.
	 * @param clickType the action that was performed to access the source,
	 *            either ClickType.SHIFT_DOWN or ClickType.LEFT_CLICK.
	 * @return a valid bendable block, or null if none was found.
	 */
	public static Block getDynamicEarthSourceBlock(Player player, int autoRange, int selectRange, BlockSourceType sourceType, ClickType clickType, boolean auto, boolean dynamic, boolean earth, boolean sand, boolean metal) {
		update(player, selectRange, clickType);
		BlockSourceInformation info = getValidBlockSourceInformation(player, selectRange, sourceType, clickType);
		if (info != null) {
			Block tempBlock = info.getBlock();
			if (EarthMethods.isEarthbendable(tempBlock.getType()) && earth) {
				return tempBlock;
			}
			if (EarthMethods.isSand(tempBlock) && sand) {
				return tempBlock;
			}
			if (EarthMethods.isMetal(tempBlock) && metal) {
				return tempBlock;
			}
		}
		if (info == null && dynamic) {
			return null;
		}
		return EarthMethods.getEarthSourceBlock(player, selectRange, earth, sand, metal);
	}
	
	/**
	 * Access a specific type of source block depending on a range and {@link ClickType}.
	 * 
	 * @param player the player that is trying to bend.
	 * @param range the maximum range to access the block.
	 * @param sourceType the elemental type of block to find.
	 * @param clickType the action that was performed to access the source,
	 *            either ClickType.SHIFT_DOWN or ClickType.LEFT_CLICK.
	 * @return a valid bendable block, or null if none was found.
	 */
	
	public static Block getDynamicWaterSourceBlock(Player player, int autoRange, int selectRange, BlockSourceType sourceType, ClickType clickType, boolean auto, boolean dynamic, boolean water, boolean ice, boolean plant) {
		update(player, selectRange, clickType);
		BlockSourceInformation info = getValidBlockSourceInformation(player, selectRange, sourceType, clickType);
		if (info != null && dynamic) {
			if (WaterMethods.isWater(info.getBlock()) && water) {
				return info.getBlock();
			} else if (WaterMethods.isIcebendable(info.getBlock()) && ice) {
				return info.getBlock();
			} else if (WaterMethods.isPlant(info.getBlock()) && plant) {
				return info.getBlock();
			}
		}
		if (info == null && dynamic) {
			return null;
		}
		return WaterMethods.getWaterSourceBlock(player, selectRange, water, ice, plant);
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
	public static Block getWaterSourceBlock(Player player, int autoRange, int selectRange, ClickType clickType, boolean auto, boolean dynamic, boolean bottles, boolean water, boolean ice, boolean plant) {
		Block sourceBlock = null;
		if(dynamic)
			sourceBlock = BlockSource.getDynamicWaterSourceBlock(player, autoRange, selectRange, BlockSourceType.WATER, clickType, auto, dynamic, water, ice, plant);
		else
			sourceBlock = WaterMethods.getWaterSourceBlock(player, selectRange, water, ice, plant);
		if (sourceBlock == null) {
			if(bottles) {
			// Check the block in front of the player's eyes, it may have been created by a
			// WaterBottle.
			sourceBlock = WaterMethods.getWaterSourceBlock(player, selectRange, water, ice, plant);
			}
			if (auto && (sourceBlock == null || sourceBlock.getLocation().distance(player.getEyeLocation()) > autoRange)) {
				sourceBlock = WaterMethods.getRandomWaterBlock(player, player.getLocation(), autoRange, water, ice, plant);
			}
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
	 *            either {@link ClickType}.SHIFT_DOWN or ClickType.LEFT_CLICK.
	 * @param allowNearbySubstitute if a valid earth source could not be found
	 *            then this method will attempt to find a nearby valid earth
	 *            block.
	 * @return a valid Earth bendable block, or null if none was found.
	 */
	public static Block getEarthSourceBlock(Player player, int autoRange, int selectRange, ClickType clickType, boolean auto, boolean dynamic, boolean earth, boolean sand, boolean metal) {
		Block sourceBlock = null;
		if(dynamic)
			sourceBlock = getDynamicEarthSourceBlock(player, autoRange, selectRange, BlockSourceType.EARTH, clickType, auto, dynamic, earth, sand, metal);
		else
			sourceBlock = EarthMethods.getEarthSourceBlock(player, selectRange, earth, sand, metal);
		if (sourceBlock == null) {
			BlockSourceInformation blockInfo = getBlockSourceInformation(player, BlockSourceType.EARTH, clickType);
			if (dynamic) {
				if (blockInfo == null) {
					return null;
				}
				Block tempBlock = blockInfo.getBlock();
				if (tempBlock == null) {
					return null;
				}

				Location loc = tempBlock.getLocation();
				sourceBlock = EarthMethods.getNearbyEarthBlock(loc, autoRange, 2, earth, sand, metal);
			}
			if (auto && (sourceBlock == null || !sourceBlock.getLocation().getWorld().equals(player.getWorld()) || Math.abs(sourceBlock.getLocation().distance(player.getEyeLocation())) > selectRange)) {
				return EarthMethods.getRandomEarthBlock(player, player.getLocation(), autoRange, earth, sand, metal);
			}
		}
		return sourceBlock;
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
	public static Block getEarthOrLavaSourceBlock(Player player, int autoRange, int selectRange, ClickType clickType, boolean auto, boolean dynamic, boolean earth, boolean sand, boolean metal) {
		/*
		 * When Lava is selected as a source it automatically overrides the
		 * previous Earth based source. Only one of these types can exist, so if
		 * Lava exists then we know Earth is null.
		 */
		Block earthBlock = getEarthSourceBlock(player, autoRange, selectRange, clickType, auto, dynamic, earth, sand, metal);
		BlockSourceInformation lavaBlockInfo = getValidBlockSourceInformation(player, selectRange, BlockSourceType.LAVA, clickType);
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
	private static boolean isStillAValidSource(BlockSourceInformation info, int range, ClickType clickType) {
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
		} else if (info.getSourceType() == BlockSourceType.SAND && !EarthMethods.isSand(info.getBlock())) {
			return false;
		} else if (info.getSourceType() == BlockSourceType.METAL && (!EarthMethods.isMetal(info.getBlock()) || !EarthMethods.isEarthbendable(info.getPlayer(), info.getBlock()))) {
			return false;
		} else if (info.getSourceType() == BlockSourceType.LAVA && (!EarthMethods.isLava(info.getBlock()) || !EarthMethods.isLavabendable(info.getBlock(), info.getPlayer()))) {
			return false;
		}
		return true;
	}
	
	public static boolean isAuto(Block block) {
		if (randomBlocks.contains(block)) {
			return true;
		}
		return false;
	}
}

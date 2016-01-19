package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.PhaseChangeFreeze;
import com.projectkorra.projectkorra.waterbending.SurgeWall;
import com.projectkorra.projectkorra.waterbending.SurgeWave;
import com.projectkorra.projectkorra.waterbending.WaterArms;
import com.projectkorra.projectkorra.waterbending.WaterSpout;
import com.projectkorra.rpg.RPGMethods;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public abstract class WaterAbility extends ElementalAbility {

	public WaterAbility(Player player) {
		super(player);
	}
	
	public boolean canAutoSource() {
		return getConfig().getBoolean("Abilities." + getElement() + "." + getName() + ".CanAutoSource");
	}
	
	public boolean canDynamicSource() {
		return getConfig().getBoolean("Abilities." + getElement() + "." + getName() + ".CanDynamicSource");
	}
	
	@Override
	public Element getElement() {
		return Element.WATER;
	}

	public Block getIceSourceBlock(double range) {
		return getIceSourceBlock(player, range);
	}
	
	public double getNightFactor() {
		if (getLocation() != null) {
			return getNightFactor(getLocation().getWorld());
		}
		return player != null ? getNightFactor(player.getLocation().getWorld()) : 1;
	}
	
	public double getNightFactor(double value) {
		return player != null ? getNightFactor(value, player.getWorld()) : value;
	}

	public Block getPlantSourceBlock(double range) {
		return getPlantSourceBlock(range, false);
	}
	
	public Block getPlantSourceBlock(double range, boolean onlyLeaves) {
		return getPlantSourceBlock(player, range, onlyLeaves);
	}
	
	@Override
	public boolean isExplosiveAbility() {
		return false;
	}
	
	@Override
	public boolean isIgniteAbility() {
		return false;
	}

	public boolean isWaterbendable(Block block) {
		return isWaterbendable(block, player);
	}

	public static boolean canBendPackedIce() {
		return getConfig().getBoolean("Properties.Water.CanBendPackedIce");
	}
	
	public static Block getIceSourceBlock(Player player, double range) {
		Location location = player.getEyeLocation();
		Vector vector = location.getDirection().clone().normalize();
		for (double i = 0; i <= range; i++) {
			Block block = location.clone().add(vector.clone().multiply(i)).getBlock();
			if (GeneralMethods.isRegionProtectedFromBuild(player, "IceBlast", location)) {
				continue;
			}
			if (isIcebendable(block)) {
				if (TempBlock.isTempBlock(block)) {
					continue;
				}
				return block;
			}
		}
		return null;
	}

	public static double getNightFactor(double value, World world) {
		if (isNight(world)) {
			if (GeneralMethods.hasRPG()) {
				if (isLunarEclipse(world)) {
					return RPGMethods.getFactor("LunarEclipse");
				} else if (isFullMoon(world)) {
					return RPGMethods.getFactor("FullMoon");
				} else {
					return value;
				}
			} else {
				if (isFullMoon(world)) {
					return getConfig().getDouble("Properties.Water.FullMoonFactor") * value;
				} else {
					return getConfig().getDouble("Properties.Water.NightFactor") * value;
				}
			}
		} else {
			return value;
		}
	}
		
	public static double getNightFactor(World world) {
		return getNightFactor(1, world);
	}

	public static Block getPlantSourceBlock(Player player, double range, boolean onlyLeaves) {
		Location location = player.getEyeLocation();
		Vector vector = location.getDirection().clone().normalize();
		
		for (double i = 0; i <= range; i++) {
			Block block = location.clone().add(vector.clone().multiply(i)).getBlock();
			if (GeneralMethods.isRegionProtectedFromBuild(player, "PlantDisc", location)) {
				continue;
			} else if (isPlantbendable(block, onlyLeaves)) {
				if (TempBlock.isTempBlock(block)) {
					continue;
				}
				return block;
			}
		}
		return null;
	}
	
	/**
	 * Finds a valid Water source for a Player. To use dynamic source selection,
	 * use BlockSource.getWaterSourceBlock() instead of this method. Dynamic
	 * source selection saves the user's previous source for future use.
	 * {@link BlockSource#getWaterSourceBlock(Player, double)}
	 * 
	 * @param player the player that is attempting to Waterbend.
	 * @param range the maximum block selection range.
	 * @param plantbending true if the player can bend plants.
	 * @return a valid Water source block, or null if one could not be found.
	 */
	@SuppressWarnings("deprecation")
	public static Block getWaterSourceBlock(Player player, double range, boolean plantbending) {
		Location location = player.getEyeLocation();
		Vector vector = location.getDirection().clone().normalize();
		
		for (double i = 0; i <= range; i++) {
			Block block = location.clone().add(vector.clone().multiply(i)).getBlock();
			if (GeneralMethods.isRegionProtectedFromBuild(player, "WaterManipulation", location)) {
				continue;
			} else if (isWaterbendable(block, player) && (!isPlant(block) || plantbending)) {
				if (TempBlock.isTempBlock(block)) {
					TempBlock tb = TempBlock.get(block);
					byte full = 0x0;
					if (tb.getState().getRawData() != full 
							&& (tb.getState().getType() != Material.WATER || tb.getState().getType() != Material.STATIONARY_WATER)) {
						continue;
					}
				}
				return block;
			}
		}
		return null;
	}

	public static boolean isAdjacentToFrozenBlock(Block block) {
		BlockFace[] faces = { BlockFace.DOWN, BlockFace.UP, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH };
		boolean adjacent = false;
		for (BlockFace face : faces) {
			if (PhaseChangeFreeze.getFrozenBlocks().containsKey((block.getRelative(face)))) {
				adjacent = true;
			}
		}
		return adjacent;
	}

	public static boolean isIcebendable(Block block) {
		return block != null ? isIcebendable(block.getType()) : false;
	}
	
	public static boolean isIcebendable(Material material) {
		if (material == Material.ICE || material == Material.SNOW) {
			return true;
		} else if (material == Material.PACKED_ICE && canBendPackedIce()) {
			return true;
		}
		return false;
	}
	
	public static boolean isPlantbendable(Block block) {
		return isPlantbendable(block, false);
	}
	
	public static boolean isPlantbendable(Block block, boolean leavesOnly) {
		if (block.getType() == Material.LEAVES) {
			return true;
		} else if (block.getType() == Material.LEAVES_2) {
			return true;
		} else if (isPlant(block) && !leavesOnly) {
			return true;
		}
		return false;
	}
	
	public static boolean isSnow(Block block) {
		return block != null ? isSnow(block.getType()) : false;
	}

	public static boolean isSnow(Material material) {
		return material == Material.SNOW || material == Material.SNOW_BLOCK;
	}

	@SuppressWarnings("deprecation")
	public static boolean isWaterbendable(Block block, Player player) {
		byte full = 0x0;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		
		if (TempBlock.isTempBlock(block)) {
			return false;
		} else if (isWater(block) && block.getData() == full) {
			return true;
		} else if (block.getType() == Material.ICE || block.getType() == Material.SNOW) {
			return true;
		} else if (block.getType() == Material.PACKED_ICE && canBendPackedIce()) {
			return true;
		} else if (bPlayer != null && bPlayer.canPlantbend() && isPlant(block)) {
			return true;
		}
		return false;
	}

	public static void playFocusWaterEffect(Block block) {
		block.getWorld().playEffect(block.getLocation(), Effect.SMOKE, 4, 20);
	}

	public static void playIcebendingSound(Location loc) {
		if (getConfig().getBoolean("Properties.Water.PlaySound")) {
			loc.getWorld().playSound(loc, Sound.FIRE_IGNITE, 2, 10);
		}
	}

	public static void playPlantbendingSound(Location loc) {
		if (getConfig().getBoolean("Properties.Water.PlaySound")) {
			loc.getWorld().playSound(loc, Sound.STEP_GRASS, 1, 10);
		}
	}

	public static void playWaterbendingSound(Location loc) {
		if (getConfig().getBoolean("Properties.Water.PlaySound")) {
			loc.getWorld().playSound(loc, Sound.WATER, 1, 10);
		}
	}

	/**
	 * Removes all water spouts in a location within a certain radius.
	 * 
	 * @param loc The location to use
	 * @param radius The radius around the location to remove spouts in
	 * @param source The player causing the removal
	 */
	public static void removeWaterSpouts(Location loc, double radius, Player source) {
		WaterSpout.removeSpouts(loc, radius, source);
	}

	/**
	 * Removes all water spouts in a location with a radius of 1.5.
	 * 
	 * @param loc The location to use
	 * @param source The player causing the removal
	 */
	public static void removeWaterSpouts(Location loc, Player source) {
		removeWaterSpouts(loc, 1.5, source);
	}
	
	public static void stopBending() {
		PhaseChangeFreeze.removeAllCleanup();
		WaterSpout.removeAllCleanup();
		SurgeWall.removeAllCleanup();
		SurgeWave.removeAllCleanup();
		WaterArms.removeAllCleanup();
	}
}

package com.projectkorra.projectkorra.ability;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.firebending.HeatControl;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.ParticleEffect.ParticleData;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.SurgeWall;
import com.projectkorra.projectkorra.waterbending.SurgeWave;
import com.projectkorra.projectkorra.waterbending.WaterSpout;
import com.projectkorra.projectkorra.waterbending.ice.PhaseChange;
import com.projectkorra.projectkorra.waterbending.multiabilities.WaterArms;
import com.projectkorra.rpg.RPGMethods;

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

	@Override
	public void handleCollision(Collision collision) {
		super.handleCollision(collision);
		if (collision.isRemovingFirst()) {
			ParticleData particleData = (ParticleEffect.ParticleData) new ParticleEffect.BlockData(Material.WATER, (byte) 0);
			ParticleEffect.BLOCK_CRACK.display(particleData, 1F, 1F, 1F, 0.1F, 10, collision.getLocationFirst(), 50);
		}
	}
	
	public static boolean isBendableWaterTempBlock(Block block) { // Will need to be done for earth as well.
		return isBendableWaterTempBlock(TempBlock.get(block));
	}
	
	public static boolean isBendableWaterTempBlock(TempBlock tempBlock) {
		return PhaseChange.getFrozenBlocksAsTempBlock().contains(tempBlock) || HeatControl.getMeltedBlocks().contains(tempBlock);
	}

	public boolean isIcebendable(Block block) {
		return isIcebendable(block.getType());
	}

	public boolean isIcebendable(Material material) {
		return isIcebendable(player, material);
	}

	public boolean isIcebendable(Player player, Material material) {
		return isIcebendable(player, material, false);
	}

	public boolean isPlantbendable(Block block) {
		return isPlantbendable(block.getType());
	}

	public boolean isPlantbendable(Material material) {
		return isPlantbendable(player, material);
	}

	public boolean isPlantbendable(Player player, Material material) {
		return isPlantbendable(player, material, false);
	}

	public boolean isWaterbendable(Block block) {
		return isWaterbendable(player, block);
	}

	public boolean isWaterbendable(Player player, Block block) {
		return isWaterbendable(player, null, block);
	}
	
	public boolean allowBreakPlants() {
		return true;
	}

	public static boolean isWaterbendable(Material material) {
		return isWater(material) || isIce(material) || isPlant(material) || isSnow(material);
	}

	public static Block getIceSourceBlock(Player player, double range) {
		Location location = player.getEyeLocation();
		Vector vector = location.getDirection().clone().normalize();
		for (double i = 0; i <= range; i++) {
			Block block = location.clone().add(vector.clone().multiply(i)).getBlock();
			if (GeneralMethods.isRegionProtectedFromBuild(player, "IceBlast", location)) {
				continue;
			}
			if (isIcebendable(player, block.getType(), false)) {
				if (TempBlock.isTempBlock(block) && !isBendableWaterTempBlock(block)) {
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
					return RPGMethods.getFactor("LunarEclipse") * value;
				} else if (isFullMoon(world)) {
					return RPGMethods.getFactor("FullMoon") * value;
				} else {
					return getConfig().getDouble("Properties.Water.NightFactor") * value;
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
			} else if (isPlantbendable(player, block.getType(), onlyLeaves)) {
				if (TempBlock.isTempBlock(block) && !isBendableWaterTempBlock(block)) {
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

		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		Block testBlock = player.getTargetBlock(getTransparentMaterialSet(), range > 3 ? 3 : (int) range);
		if (bPlayer == null) {
			return null;
		} else if (isWaterbendable(player, null, testBlock) && (!isPlant(testBlock) || plantbending)) {
			return testBlock;
		}

		for (double i = 0; i <= range; i++) {
			Block block = location.clone().add(vector.clone().multiply(i)).getBlock();
			if ((!isTransparent(player, block) && !isIce(block) && !isPlant(block) && !isSnow(block)) || GeneralMethods.isRegionProtectedFromBuild(player, "WaterManipulation", location)) {
				continue;
			} else if (isWaterbendable(player, null, block) && (!isPlant(block) || plantbending)) {
				if (TempBlock.isTempBlock(block) && !isBendableWaterTempBlock(block)) {
					continue;
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
			if (PhaseChange.getFrozenBlocksAsBlock().contains((block.getRelative(face)))) {
				adjacent = true;
			}
		}
		return adjacent;
	}

	public static boolean isIcebendable(Player player, Material material, boolean onlyIce) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		return bPlayer == null ? null : isIce(material) && bPlayer.canIcebend() && (!onlyIce || material == Material.ICE);
	}

	public static boolean isPlantbendable(Player player, Material material, boolean onlyLeaves) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (onlyLeaves) {
			return bPlayer == null ? null : isPlant(material) && bPlayer.canPlantbend() && isLeaves(material);
		} else {
			return bPlayer == null ? null : isPlant(material) && bPlayer.canPlantbend();
		}
	}

	public static boolean isLeaves(Block block) {
		return block != null ? isLeaves(block.getType()) : false;
	}

	public static boolean isLeaves(Material material) {
		return material == Material.LEAVES || material == Material.LEAVES_2;
	}

	public static boolean isSnow(Block block) {
		return block != null ? isSnow(block.getType()) : false;
	}

	public static boolean isSnow(Material material) {
		return material == Material.SNOW || material == Material.SNOW_BLOCK;
	}

	@SuppressWarnings("deprecation")
	public static boolean isWaterbendable(Player player, String abilityName, Block block) {
		byte full = 0x0;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null || !isWaterbendable(block.getType()) || GeneralMethods.isRegionProtectedFromBuild(player, abilityName, block.getLocation())) {
			return false;
		}
		if (TempBlock.isTempBlock(block) && !isBendableWaterTempBlock(block)) {
			return false;
		} else if (isWater(block) && block.getData() == full) {
			return true;
		} else if (isIce(block) && !bPlayer.canIcebend()) {
			return false;
		} else if (isPlant(block) && !bPlayer.canPlantbend()) {
			return false;
		}
		return true;
	}

	public static void playFocusWaterEffect(Block block) {
		block.getWorld().playEffect(block.getLocation(), Effect.SMOKE, 4, 20);
	}

	public static void playIcebendingSound(Location loc) {
		if (getConfig().getBoolean("Properties.Water.PlaySound")) {
			loc.getWorld().playSound(loc, Sound.valueOf(getConfig().getString("Properties.Water.IceSound")), 2, 10);
		}
	}

	public static void playPlantbendingSound(Location loc) {
		if (getConfig().getBoolean("Properties.Water.PlaySound")) {
			loc.getWorld().playSound(loc, Sound.valueOf(getConfig().getString("Properties.Water.PlantSound")), 1, 10);
		}
	}

	public static void playWaterbendingSound(Location loc) {
		if (getConfig().getBoolean("Properties.Water.PlaySound")) {
			loc.getWorld().playSound(loc, Sound.valueOf(getConfig().getString("Properties.Water.WaterSound")), 1, 10);
		}
	}

	/**
	 * This method was used for the old collision detection system. Please see
	 * {@link Collision} for the new system.
	 * <p>
	 * Removes all water spouts in a location within a certain radius.
	 * 
	 * @param loc The location to use
	 * @param radius The radius around the location to remove spouts in
	 * @param source The player causing the removal
	 */
	@Deprecated
	public static void removeWaterSpouts(Location loc, double radius, Player source) {
		WaterSpout.removeSpouts(loc, radius, source);
	}

	/**
	 * This method was used for the old collision detection system. Please see
	 * {@link Collision} for the new system.
	 * <p>
	 * Removes all water spouts in a location with a radius of 1.5.
	 * 
	 * @param loc The location to use
	 * @param source The player causing the removal
	 */
	@Deprecated
	public static void removeWaterSpouts(Location loc, Player source) {
		removeWaterSpouts(loc, 1.5, source);
	}

	public static void stopBending() {
		SurgeWall.removeAllCleanup();
		SurgeWave.removeAllCleanup();
		WaterArms.removeAllCleanup();
	}
}

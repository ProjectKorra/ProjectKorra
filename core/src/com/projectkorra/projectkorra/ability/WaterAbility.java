package com.projectkorra.projectkorra.ability;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.projectkorra.projectkorra.region.RegionProtection;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.firebending.HeatControl;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.SurgeWall;
import com.projectkorra.projectkorra.waterbending.SurgeWave;
import com.projectkorra.projectkorra.waterbending.Torrent;
import com.projectkorra.projectkorra.waterbending.WaterSpout;
import com.projectkorra.projectkorra.waterbending.ice.PhaseChange;
import com.projectkorra.projectkorra.waterbending.multiabilities.WaterArms;

public abstract class WaterAbility extends ElementalAbility {
	public static final Map<Material, Material> WATER_TRANSFORMABLE_BLOCKS = getWaterTransformableBlocks();

	public WaterAbility(final Player player) {
		super(player);
	}

	public boolean canAutoSource() {
		return getConfig().getBoolean("Abilities." + this.getElement() + "." + this.getName() + ".CanAutoSource");
	}

	public boolean canDynamicSource() {
		return getConfig().getBoolean("Abilities." + this.getElement() + "." + this.getName() + ".CanDynamicSource");
	}

	@Override
	public Element getElement() {
		return Element.WATER;
	}

	public Block getIceSourceBlock(final double range) {
		return getIceSourceBlock(this.player, range);
	}

	public Block getPlantSourceBlock(final double range) {
		return this.getPlantSourceBlock(range, false);
	}

	public Block getPlantSourceBlock(final double range, final boolean onlyLeaves) {
		return getPlantSourceBlock(this.player, range, onlyLeaves);
	}

	@Override
	public boolean isExplosiveAbility() {
		return false;
	}

	@Override
	public boolean isIgniteAbility() {
		return false;
	}

	public Block getSourceBlock() {
		return null;
	}

	@Override
	public void handleCollision(final Collision collision) {
		super.handleCollision(collision);
		if (collision.isRemovingFirst()) {
			ParticleEffect.BLOCK_CRACK.display(collision.getLocationFirst(), 10, 1, 1, 1, 0.1, collision.getLocationFirst().getBlock().getBlockData());
		}
	}

	public double getNightFactor(final double value) {
		return this.player != null ? value * getNightFactor(player.getWorld()) : 1;
	}

	public static boolean isBendableWaterTempBlock(final Block block) { // TODO: Will need to be done for earth as well.
		return isBendableWaterTempBlock(TempBlock.get(block));
	}

	public static boolean isBendableWaterTempBlock(final TempBlock tempBlock) {
		return PhaseChange.getFrozenBlocksMap().containsKey(tempBlock) || HeatControl.getMeltedBlocks().contains(tempBlock)
				|| SurgeWall.SOURCE_BLOCKS.contains(tempBlock) || Torrent.getFrozenBlocks().containsKey(tempBlock);
	}

	public boolean isIcebendable(final Block block) {
		return this.isIcebendable(block.getType());
	}

	public boolean isIcebendable(final Material material) {
		return this.isIcebendable(this.player, material);
	}

	public boolean isIcebendable(final Player player, final Material material) {
		return isIcebendable(player, material, false);
	}

	public boolean isPlantbendable(final Block block) {
		return this.isPlantbendable(block.getType());
	}

	public boolean isPlantbendable(final Material material) {
		return this.isPlantbendable(this.player, material);
	}

	public boolean isPlantbendable(final Player player, final Material material) {
		return isPlantbendable(player, material, false);
	}

	public boolean isWaterbendable(final Block block) {
		return this.isWaterbendable(this.player, block);
	}

	public boolean isWaterbendable(final Player player, final Block block) {
		return isWaterbendable(player, null, block);
	}

	public boolean allowBreakPlants() {
		return true;
	}

	public static boolean isWaterbendable(final Material material) {
		return isWater(material) || isIce(material) || isPlant(material) || isSnow(material) || isCauldron(material) || isMud(material) || isSponge(material);
	}

	public static Block getIceSourceBlock(final Player player, final double range) {
		final Location location = player.getEyeLocation();
		final Vector vector = location.getDirection().clone().normalize();
		for (double i = 0; i <= range; i++) {
			final Block block = location.clone().add(vector.clone().multiply(i)).getBlock();
			if (RegionProtection.isRegionProtected(player, location,"IceBlast")) {
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

	public static double getNightFactor() {
		return getConfig().getDouble("Properties.Water.NightFactor");
	}

	public static double getNightFactor(final double value, final World world) {
		if (isNight(world)) {
			return value * getNightFactor();
		}

		return value;
	}

	public static double getNightFactor(final World world) {
		return getNightFactor(1, world);
	}

	public static Block getPlantSourceBlock(final Player player, final double range, final boolean onlyLeaves) {
		final Location location = player.getEyeLocation();
		final Vector vector = location.getDirection().clone().normalize();

		for (double i = 0; i <= range; i++) {
			final Block block = location.clone().add(vector.clone().multiply(i)).getBlock();
			if (RegionProtection.isRegionProtected(player, location, "PlantDisc")) {
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
	public static Block getWaterSourceBlock(final Player player, final double range, final boolean plantbending) {
		final Location location = player.getEyeLocation();
		final Vector vector = location.getDirection().clone().normalize();

		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		final Set<Material> trans = getTransparentMaterialSet();

		if (plantbending) {
			final Set<Material> remove = new HashSet<>();
			for (final Material m : trans) {
				if (isPlant(m)) {
					remove.add(m);
				}
			}
			trans.removeAll(remove);
		}

		final Block testBlock = player.getTargetBlock(trans, Math.max(1, Math.min(3, (int)range)));
		if (bPlayer == null) {
			return null;
		} else if (isWaterbendable(player, null, testBlock) && (!isPlant(testBlock) || plantbending)) {
			return testBlock;
		}

		for (double i = 0; i <= range; i++) {
			final Block block = location.clone().add(vector.clone().multiply(i)).getBlock();
			if ((!isTransparent(player, block) && !isIce(block) && !isPlant(block) && !isSnow(block) && !isCauldron(block) && !isMud(block) && !isSponge(block)) || RegionProtection.isRegionProtected(player, location, "WaterManipulation")) {
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

	public static boolean isAdjacentToFrozenBlock(final Block block) {
		final BlockFace[] faces = { BlockFace.DOWN, BlockFace.UP, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH };
		boolean adjacent = false;
		for (final BlockFace face : faces) {
			if (PhaseChange.getFrozenBlocksAsBlock().contains((block.getRelative(face)))) {
				adjacent = true;
			}
		}
		return adjacent;
	}

	public static boolean isIcebendable(final Player player, final Material material, final boolean onlyIce) {
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		return bPlayer == null ? null : isIce(material) && bPlayer.canIcebend() && (!onlyIce || material == Material.ICE);
	}

	public static boolean isPlantbendable(final Player player, final Material material, final boolean onlyLeaves) {
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (onlyLeaves) {
			return bPlayer == null ? null : isPlant(material) && bPlayer.canPlantbend() && isLeaves(material);
		} else {
			return bPlayer == null ? null : isPlant(material) && bPlayer.canPlantbend();
		}
	}

	public static boolean isLeaves(final Block block) {
		return block != null ? isLeaves(block.getType()) : false;
	}

	public static boolean isLeaves(final Material material) {
		return Tag.LEAVES.isTagged(material);
	}

	public static boolean isSnow(final Block block) {
		return block != null ? isSnow(block.getType()) : false;
	}

	public static boolean isSnow(final Material material) {
		return material == Material.SNOW || material == Material.SNOW_BLOCK;
	}
	
	public static boolean isCauldron(final Block block) {
		return isCauldron(block.getType()) ? isCauldron(block.getType()) : GeneralMethods.getMCVersion() < 1170 && block.getType() == Material.CAULDRON && ((Levelled) block.getBlockData()).getLevel() >= 1;
	}
	
	public static boolean isCauldron(final Material material) {
		return GeneralMethods.getMCVersion() >= 1170 && (material == Material.getMaterial("WATER_CAULDRON") || material == Material.getMaterial("POWDER_SNOW_CAULDRON"));
	}

	public static boolean isSponge(final Block block) {
		return block != null ? isSponge(block.getType()) : false;
	}

	public static boolean isSponge(final Material material) {
		return material == Material.WET_SPONGE;
	}

	/**
	 * Checks if a source block is a transformable source.
	 *
	 * @param block
	 * @return True if the block is a non-transparent source, False otherwise.
	 */
	public static boolean isTransformableBlock(final Block block) {
		return isTransformableBlock(block.getType());
	}

	public static boolean isTransformableBlock(final Material material) {
		return WATER_TRANSFORMABLE_BLOCKS.containsKey(material);
	}

	public static boolean isWaterbendable(final Player player, final String abilityName, final Block block) {
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null || !isWaterbendable(block.getType())) {
			return false;
		}
		if (TempBlock.isTempBlock(block) && !isBendableWaterTempBlock(block)) {
			return false;
		} else if (isWater(block) && block.getBlockData() instanceof Levelled && ((Levelled) block.getBlockData()).getLevel() == 0) {
			return true;
		} else if (isIce(block) && !bPlayer.canIcebend()) {
			return false;
		} else if (isPlant(block) && !bPlayer.canPlantbend()) {
			return false;
		}
		return true;
	}

	public static Map<Material, Material> getWaterTransformableBlocks() {
		// If we want to revisit this configurably in the future, we can reuse this code.

		/*
		List<String> transformableBlocks = getConfig().getStringList("Properties.Water.TransformableBlocks");
		Map<Material, Material> transformables = new HashMap<>();

		for (String block : transformableBlocks) {
			String[] split = block.split(">");
			if (split.length != 2) {
				ProjectKorra.log.warning("Invalid TransformableBlock: " + block);
				continue;
			}
			Material from = Material.getMaterial(split[0]);
			Material to = Material.getMaterial(split[1]);
			if (from == null || to == null) {
				ProjectKorra.log.warning("Invalid TransformableBlock: " + block);
				continue;
			}
			transformables.put(from, to);
		}
		 */
		Map<Material, Material> transformables = new HashMap<>();
		if (GeneralMethods.getMCVersion() >= 1170) {
			transformables.put(Material.getMaterial("MUD"), Material.DIRT);
			transformables.put(Material.getMaterial("PACKED_MUD"), Material.DIRT);
		}
		if (GeneralMethods.getMCVersion() >= 1190) {
			transformables.put(Material.getMaterial("MUDDY_MANGROVE_ROOTS"), Material.getMaterial("MANGROVE_ROOTS"));
		}
		transformables.put(Material.WET_SPONGE, Material.SPONGE);
		return transformables;
	}

	/*
	public static void setupWaterTransformableBlocks() {
		if (!WATER_TRANSFORMABLE_BLOCKS.isEmpty()) {
			WATER_TRANSFORMABLE_BLOCKS.clear();
		}
		WATER_TRANSFORMABLE_BLOCKS.putAll(getWaterTransformableBlocks());
	}
	 */

	public static void playFocusWaterEffect(final Block block) {
		ParticleEffect.SMOKE_NORMAL.display(block.getLocation().add(0.5, 0.5, 0.5), 4);
	}

	public static void playIcebendingSound(final Location loc) {
		if (getConfig().getBoolean("Properties.Water.PlaySound")) {
			final float volume = (float) getConfig().getDouble("Properties.Water.IceSound.Volume");
			final float pitch = (float) getConfig().getDouble("Properties.Water.IceSound.Pitch");

			Sound sound = Sound.ITEM_FLINTANDSTEEL_USE;

			try {
				sound = Sound.valueOf(getConfig().getString("Properties.Water.IceSound.Sound"));
			} catch (final IllegalArgumentException exception) {
				ProjectKorra.log.warning("Your current value for 'Properties.Water.IceSound.Sound' is not valid.");
			} finally {
				loc.getWorld().playSound(loc, sound, volume, pitch);
			}
		}
	}

	public static void playMudbendingSound(final Location loc) {
		if (getConfig().getBoolean("Properties.Water.PlaySound")) {
			final float volume = (float) getConfig().getDouble("Properties.Water.MudSound.Volume");
			final float pitch = (float) getConfig().getDouble("Properties.Water.MudSound.Pitch");

			Sound sound = Sound.BLOCK_WET_GRASS_STEP;
			if (GeneralMethods.getMCVersion() >= 1190) {
				sound = Sound.valueOf("BLOCK_MUD_STEP");
			}

			try {
				sound = Sound.valueOf(getConfig().getString("Properties.Water.MudSound.Sound"));
			} catch (final IllegalArgumentException exception) {
				ProjectKorra.log.warning("Your current value for 'Properties.Water.MudSound.Sound' is not valid.");
			} finally {
				loc.getWorld().playSound(loc, sound, volume, pitch);
			}
		}
	}

	public static void playPlantbendingSound(final Location loc) {
		if (getConfig().getBoolean("Properties.Water.PlaySound")) {
			final float volume = (float) getConfig().getDouble("Properties.Water.PlantSound.Volume");
			final float pitch = (float) getConfig().getDouble("Properties.Water.PlantSound.Pitch");

			Sound sound = Sound.BLOCK_GRASS_STEP;

			try {
				sound = Sound.valueOf(getConfig().getString("Properties.Water.PlantSound.Sound"));
			} catch (final IllegalArgumentException exception) {
				ProjectKorra.log.warning("Your current value for 'Properties.Water.PlantSound.Sound' is not valid.");
			} finally {
				loc.getWorld().playSound(loc, sound, volume, pitch);
			}
		}
	}

	public static void playWaterbendingSound(final Location loc) {
		if (getConfig().getBoolean("Properties.Water.PlaySound")) {
			final float volume = (float) getConfig().getDouble("Properties.Water.WaterSound.Volume");
			final float pitch = (float) getConfig().getDouble("Properties.Water.WaterSound.Pitch");

			Sound sound = Sound.BLOCK_WATER_AMBIENT;

			try {
				sound = Sound.valueOf(getConfig().getString("Properties.Water.WaterSound.Sound"));
			} catch (final IllegalArgumentException exception) {
				ProjectKorra.log.warning("Your current value for 'Properties.Water.WaterSound.Sound' is not valid.");
			} finally {
				loc.getWorld().playSound(loc, sound, volume, pitch);
			}
		}
	}

	public static boolean updateSourceBlock(final Block sourceBlock) {
		if (isCauldron(sourceBlock)) {
			GeneralMethods.setCauldronData(sourceBlock, ((Levelled) sourceBlock.getBlockData()).getLevel() - 1);
			return true;
		} else if (isTransformableBlock(sourceBlock)) {
			if (isMud(sourceBlock)) {
				playMudbendingSound(sourceBlock.getLocation());
			} else if (isSponge(sourceBlock)) {
				sourceBlock.getWorld().playSound(sourceBlock.getLocation(), Sound.BLOCK_SLIME_BLOCK_BREAK, 1, 1);
			}
			sourceBlock.setType(WATER_TRANSFORMABLE_BLOCKS.get(sourceBlock.getType()));
			return true;
		}
		return false;
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
	public static void removeWaterSpouts(final Location loc, final double radius, final Player source) {
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
	public static void removeWaterSpouts(final Location loc, final Player source) {
		removeWaterSpouts(loc, 1.5, source);
	}

	/**
	 * Apply modifiers to this value. Applies the night factor to it
	 * @param value The value to modify
	 * @return The modified value
	 */
	@Override
	@Deprecated
	public double applyModifiers(double value) {
		return GeneralMethods.applyModifiers(value, getNightFactor(1.0));
	}

	/**
	 * Apply modifiers to this value. Applies the night factor to it
	 * @param value The value to modify
	 * @return The modified value
	 */
	@Deprecated
	public long applyModifiers(long value) {
		return GeneralMethods.applyModifiers(value, getNightFactor(1.0));
	}

	/**
	 * Apply modifiers to this value inversely (makes it smaller). Applies the night factor to it
	 * @param value The value to modify
	 * @return The modified value
	 */
	@Deprecated
	public double applyInverseModifiers(double value) {
		return GeneralMethods.applyInverseModifiers(value, getNightFactor(1.0));
	}

	/**
	 * Apply modifiers to this value inversely (makes it smaller). Applies the night factor to it
	 * @param value The value to modify
	 * @return The modified value
	 */
	@Deprecated
	public long applyInverseModifiers(long value) {
		return GeneralMethods.applyInverseModifiers(value, getNightFactor(1.0));
	}

	public static void stopBending() {
		SurgeWall.removeAllCleanup();
		SurgeWave.removeAllCleanup();
		WaterArms.removeAllCleanup();
	}
}

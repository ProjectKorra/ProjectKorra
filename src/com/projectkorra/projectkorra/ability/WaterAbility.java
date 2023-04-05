package com.projectkorra.projectkorra.ability;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Snow;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.region.RegionProtection;
import com.projectkorra.projectkorra.waterbending.plant.PlantRegrowth;
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
		return PhaseChange.getFrozenBlocksMap().containsKey(tempBlock) || HeatControl.getMeltedBlocks().contains(tempBlock) || SurgeWall.SOURCE_BLOCKS.contains(tempBlock) || Torrent.getFrozenBlocks().containsKey(tempBlock);
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
		return isWater(material) || isIce(material) || isPlant(material) || isSnow(material) || isCauldron(material);
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
	 * @param allowPlant true if plants are allowed.
	 * @return a valid Water source block, or null if one could not be found.
	 */
	public static Block getWaterSourceBlock(final Player player, final double range, boolean allowPlant) {
		return getWaterSourceBlock(player, range, true, true, allowPlant, true);
	}

	public static Block getWaterSourceBlock(final Player player, final double range, boolean allowPlant, boolean allowSnow) {
		return getWaterSourceBlock(player, range, true, true, allowPlant, allowSnow);
	}

	public static Block getWaterSourceBlock(final Player player, final double range, boolean allowPlant, boolean allowSnow, boolean allowIce) {
		return getWaterSourceBlock(player, range, true, allowIce, allowPlant, allowSnow);
	}

	public static Block getWaterSourceBlock(final Player player, final double range, boolean allowWater, boolean allowIce, boolean allowPlant, boolean allowSnow) {
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return null;
		} else {
			allowWater = bPlayer.hasElement(Element.WATER) && allowWater;
			allowIce = bPlayer.canIcebend() && allowIce;
			allowPlant = bPlayer.canPlantbend() && allowPlant;
			allowSnow = bPlayer.hasElement(Element.WATER) && allowSnow;
		}

		final Set<Material> trans = getTransparentMaterialSet();
		if (allowPlant) {
			final Set<Material> remove = new HashSet<>();
			for (final Material m : trans) {
				if (isPlant(m)) {
					remove.add(m);
				}
			}
			trans.removeAll(remove);
		}

		final Location location = player.getEyeLocation();
		final Vector vector = location.getDirection().clone().normalize();
		for (double i = range >= 3 ? 3 : range; i <= range; i = i > 3 ? i + 1 : (i > 0 ? i - 1 : 4)) {
			final Block block = location.clone().add(vector.clone().multiply(i)).getBlock();
			if (((isWater(block) && allowWater)
					|| (isCauldron(block) && allowWater)
					|| (isIce(block) && allowIce)
					|| (isPlant(block) && allowPlant)
					|| (isSnow(block) && allowSnow))
					&& !RegionProtection.isRegionProtected(player, location, "WaterManipulation")
					&& !(TempBlock.isTempBlock(block) && !isBendableWaterTempBlock(block)))
				return block;
		}
		return null;
	}

	public static boolean reduceWaterbendingSource(Player player, Block block) {
		return reduceWaterbendingSource(player, block, true);
	}

	public static boolean reduceWaterbendingSource(Player player, Block block, boolean allowIce) {
		return reduceWaterbendingSource(player, block, true, allowIce);
	}

	public static boolean reduceWaterbendingSource(Player player, Block block, boolean allowPlant, boolean allowIce) {
		return reduceWaterbendingSource(player, block, true, allowPlant, allowIce);
	}

	public static boolean reduceWaterbendingSource(Player player, Block block, boolean allowSnow, boolean allowPlant, boolean allowIce) {
		return reduceWaterbendingSource(player, block, true, allowSnow, allowPlant, allowIce);
	}

	public static boolean reduceWaterbendingSource(Player player, Block block, boolean allowWater, boolean allowSnow, boolean allowPlant, boolean allowIce) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null)
			return false;
		allowWater = bPlayer.hasElement(Element.WATER) && allowWater;
		allowSnow = bPlayer.hasElement(Element.WATER) && allowSnow;
		allowPlant = bPlayer.canPlantbend() && allowPlant;
		allowIce = bPlayer.canIcebend() && allowIce;
		if (isWaterbendable(player, null, block) && bPlayer.hasElement(Element.WATER)) {
			BlockData data = block.getBlockData();
			if (allowWater && data instanceof Levelled) { // Cauldrons, just water
				Levelled lvl = (Levelled) data;
				if (isCauldron(block)) { // Cauldrons
					GeneralMethods.setCauldronData(block, lvl.getLevel() - 1);
					return true;
				}
				if (GeneralMethods.isAdjacentToThreeOrMoreSources(block) || lvl.getLevel() >= 8)
					return true;
				if (lvl.getLevel() == 7) { // lowest water lvl
					GeneralMethods.removeBlock(block);
					return true;
				}
				lvl.setLevel(Math.min(lvl.getMaximumLevel(), lvl.getLevel() + 1));
				block.setBlockData(lvl);
				return true;
			} else if (allowSnow && isSnow(block)) { // layered snow
				Snow snow = (Snow) (data instanceof Snow ? data : Material.SNOW.createBlockData());
				int layers = (data instanceof Snow ? snow.getLayers() : snow.getMaximumLayers()) - 1;
				if (layers >= snow.getMinimumLayers()) {
					snow.setLayers(layers);
					block.setBlockData(snow);
				} else
					GeneralMethods.removeBlock(block);
				return true;
			} else if (allowWater && !GeneralMethods.isAdjacentToThreeOrMoreSources(block) && data instanceof Waterlogged && ((Waterlogged) data).isWaterlogged()) { // anything waterlogged
				Waterlogged drain = (Waterlogged) data;
				drain.setWaterlogged(false);
				block.setBlockData(drain);
				return true;
			} else if ((allowSnow && isSnow(block)) || (allowIce && isIce(block)) || (allowPlant && isPlant(block))) {
				if (PhaseChange.getFrozenBlocksAsBlock().contains(block)) {
					PhaseChange.thaw(block);
					return true;
				}
				if (TempBlock.isTempBlock(block)) {
					final TempBlock tb = TempBlock.get(block);
					if (isBendableWaterTempBlock(tb)) {
						tb.revertBlock();
						return true;
					}
				} else {
					new PlantRegrowth(player, block);
					GeneralMethods.removeBlock(block);
					return true;
				}
			}
		}
		return false;
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

	public static boolean isWaterbendable(final Player player, final String abilityName, final Block block) {
		if (TempBlock.isTempBlock(block) && !isBendableWaterTempBlock(block)) {
			return false;
		} else {
			final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			if (bPlayer == null) {
				return false;
			} else if ((isWater(block) || isSnow(block) || isCauldron(block)) && bPlayer.hasElement(Element.WATER)) {
				return true;
			} else if (isIce(block) && bPlayer.canIcebend()) {
				return true;
			} else if (isPlant(block) && bPlayer.canPlantbend()) {
				return true;
			}
		}
		return false;
	}

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
	public double applyModifiers(double value) {
		return GeneralMethods.applyModifiers(value, getNightFactor(1.0));
	}

	/**
	 * Apply modifiers to this value. Applies the night factor to it
	 * @param value The value to modify
	 * @return The modified value
	 */
	public long applyModifiers(long value) {
		return GeneralMethods.applyModifiers(value, getNightFactor(1.0));
	}

	/**
	 * Apply modifiers to this value inversely (makes it smaller). Applies the night factor to it
	 * @param value The value to modify
	 * @return The modified value
	 */
	public double applyInverseModifiers(double value) {
		return GeneralMethods.applyInverseModifiers(value, getNightFactor(1.0));
	}

	/**
	 * Apply modifiers to this value inversely (makes it smaller). Applies the night factor to it
	 * @param value The value to modify
	 * @return The modified value
	 */
	public long applyInverseModifiers(long value) {
		return GeneralMethods.applyInverseModifiers(value, getNightFactor(1.0));
	}

	public static void stopBending() {
		SurgeWall.removeAllCleanup();
		SurgeWave.removeAllCleanup();
		WaterArms.removeAllCleanup();
	}
}

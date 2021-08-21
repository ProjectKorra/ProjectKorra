package com.projectkorra.projectkorra.ability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.GeneralMethods;

/**
 * ElementalAbility is used to hold methods that should be accessible by every
 * Air, Water, Earth, Fire, Chi, or AvatarAbility. This class is mainly used to
 * keep CoreAbility from becoming too cluttered.
 */
public abstract class ElementalAbility extends CoreAbility {
	private static final PotionEffectType[] POSITIVE_EFFECTS = { PotionEffectType.ABSORPTION, PotionEffectType.DAMAGE_RESISTANCE, PotionEffectType.FAST_DIGGING, PotionEffectType.FIRE_RESISTANCE, PotionEffectType.HEAL, PotionEffectType.HEALTH_BOOST, PotionEffectType.INCREASE_DAMAGE, PotionEffectType.JUMP, PotionEffectType.NIGHT_VISION, PotionEffectType.REGENERATION, PotionEffectType.SATURATION, PotionEffectType.SPEED, PotionEffectType.WATER_BREATHING };
	private static final PotionEffectType[] NEUTRAL_EFFECTS = { PotionEffectType.INVISIBILITY };
	private static final PotionEffectType[] NEGATIVE_EFFECTS = { PotionEffectType.POISON, PotionEffectType.BLINDNESS, PotionEffectType.CONFUSION, PotionEffectType.HARM, PotionEffectType.HUNGER, PotionEffectType.SLOW, PotionEffectType.SLOW_DIGGING, PotionEffectType.WEAKNESS, PotionEffectType.WITHER };
	private static final Set<Material> TRANSPARENT = new HashSet<>();

	static {
		TRANSPARENT.clear();
		for (final Material mat : Material.values()) {
			if (GeneralMethods.isTransparent(mat)) {
				TRANSPARENT.add(mat);
			}
		}
	}

	public ElementalAbility(final Player player) {
		super(player);
	}

	public boolean isTransparent(final Block block) {
		return isTransparent(this.player, this.getName(), block);
	}

	public static List<String> addTagMaterials(List<String> materials) {
		ListIterator<String> iterator = new ArrayList<String>(materials).listIterator();
		while (iterator.hasNext()) {
			String tag = iterator.next();
			if (tag.startsWith("#")) {
				materials.addAll(GeneralMethods.tagToMaterialList(tag));
			}
		}

		return materials;
	}

	public static List<String> getEarthbendableBlocks() {
		List<String> materials = getConfig().getStringList("Properties.Earth.EarthBlocks");
		return addTagMaterials(materials);
	}

	public static Material[] getTransparentMaterials() {
		return TRANSPARENT.toArray(new Material[TRANSPARENT.size()]);
	}

	public static HashSet<Material> getTransparentMaterialSet() {
		return new HashSet<>(TRANSPARENT);
	}

	public static boolean isAir(final Material material) {
		return material == Material.AIR || material == Material.CAVE_AIR || material == Material.VOID_AIR;
	}

	public static boolean isDay(final World world) {
		final long time = world.getTime();
		if (world.getEnvironment() == Environment.NETHER || world.getEnvironment() == Environment.THE_END) {
			return true;
		}

		return time >= 23500 || time <= 12500;
	}

	public static boolean isEarth(final Block block) {
		return block != null && isEarth(block.getType());
	}

	public static boolean isEarth(final Material material) {
		return ElementalAbility.getEarthbendableBlocks().contains(material.toString());
	}
	
	public static boolean isFire(final Block block) {
		return block != null && isFire(block.getType());
	}
	
	public static boolean isFire(final Material material) {
		return material == Material.SOUL_FIRE || material == Material.FIRE;
	}

	public static boolean isFullMoon(final World world) {
		return (world.getFullTime() / 24000) % 8 == 0;
	}

	public static boolean isIce(final Block block) {
		return block != null && isIce(block.getType());
	}

	public static boolean isIce(final Material material) {
		List<String> materials = getConfig().getStringList("Properties.Water.IceBlocks");
		return addTagMaterials(materials).contains(material.toString());
	}

	public static boolean isLava(final Block block) {
		return block != null && isLava(block.getType());
	}

	public static boolean isLava(final Material material) {
		return material == Material.LAVA;
	}

	public static boolean isSnow(final Block block) {
		return block != null && isSnow(block.getType());
	}

	public static boolean isSnow(final Material material) {
		List<String> materials = getConfig().getStringList("Properties.Water.SnowBlocks");
		return addTagMaterials(materials).contains(material.toString());
	}

	public static boolean isMeltable(final Block block) {
		return isIce(block) || isSnow(block);
	}

	public static boolean isMetal(final Block block) {
		return block != null && isMetal(block.getType());
	}

	public static boolean isMetal(final Material material) {
		List<String> materials = getConfig().getStringList("Properties.Earth.MetalBlocks");
		return addTagMaterials(materials).contains(material.toString());
	}

	public static boolean isMetalBlock(final Block block) {
		return isMetal(block);
	}

	public static boolean isNegativeEffect(final PotionEffectType effect) {
		for (final PotionEffectType effect2 : NEGATIVE_EFFECTS) {
			if (effect2.equals(effect)) {
				return true;
			}
		}

		return false;
	}

	public static boolean isNeutralEffect(final PotionEffectType effect) {
		for (final PotionEffectType effect2 : NEUTRAL_EFFECTS) {
			if (effect2.equals(effect)) {
				return true;
			}
		}

		return false;
	}

	public static boolean isNight(final World world) {
		final long time = world.getTime();
		if (world.getEnvironment() == Environment.NETHER || world.getEnvironment() == Environment.THE_END) {
			return false;
		}

		return time >= 12950 && time <= 23050;
	}

	public static boolean isPlant(final Block block) {
		return block != null && isPlant(block.getType());
	}

	public static boolean isPlant(final Material material) {
		List<String> materials = getConfig().getStringList("Properties.Water.PlantBlocks");
		return addTagMaterials(materials).contains(material.toString());
	}

	public static boolean isPositiveEffect(final PotionEffectType effect) {
		for (final PotionEffectType effect2 : POSITIVE_EFFECTS) {
			if (effect2.equals(effect)) {
				return true;
			}
		}

		return false;
	}

	public static boolean isSand(final Block block) {
		return block != null && isSand(block.getType());
	}

	public static boolean isSand(final Material material) {
		List<String> materials = getConfig().getStringList("Properties.Earth.SandBlocks");
		return addTagMaterials(materials).contains(material.toString());
	}

	public static boolean isTransparent(final Player player, final Block block) {
		return isTransparent(player, null, block);
	}

	public static boolean isTransparent(final Player player, final String abilityName, final Block block) {
		return Arrays.asList(getTransparentMaterials()).contains(block.getType()) && !GeneralMethods.isRegionProtectedFromBuild(player, abilityName, block.getLocation());
	}

	public static boolean isWater(final Block block) {
		if (block == null) {
			return false;
		} else if (block.getState() instanceof Container) {
			return false; 
		} else {
			return isWater(block.getBlockData());
		}
	}

	public static boolean isWater(final BlockData data) {
		return (data instanceof Waterlogged) ? ((Waterlogged) data).isWaterlogged() : isWater(data.getMaterial());
	}

	public static boolean isWater(final Material material) {
		return material == Material.WATER || material == Material.SEAGRASS || material == Material.TALL_SEAGRASS || material == Material.KELP_PLANT || material == Material.KELP || material == Material.BUBBLE_COLUMN;
	}
}

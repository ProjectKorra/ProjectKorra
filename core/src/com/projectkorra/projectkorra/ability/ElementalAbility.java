package com.projectkorra.projectkorra.ability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import com.projectkorra.projectkorra.region.RegionProtection;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
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

	private static final Set<String> EARTH_BLOCKS = new HashSet<String>();
	private static final Set<String> ICE_BLOCKS = new HashSet<String>();
	private static final Set<String> METAL_BLOCKS = new HashSet<String>();
	private static final Set<String> PLANT_BLOCKS = new HashSet<String>();
	private static final Set<String> SAND_BLOCKS = new HashSet<String>();
	private static final Set<String> SNOW_BLOCKS = new HashSet<String>();

	// Once 1.16.5 no longer becomes LTS, this becomes obsolete and
	// we can remove the version check and reference these materials directly instead of doing standard lookups.
	protected static final Material LIGHT = Material.getMaterial("LIGHT");
	protected static final Set<Material> MUD_BLOCKS = getMudBlocks();
	private static Set<Material> getMudBlocks() {
		Set<Material> mudBlocks = new HashSet<>();
		mudBlocks.add(Material.getMaterial("MUD"));
		mudBlocks.add(Material.getMaterial("PACKED_MUD"));
		mudBlocks.add(Material.getMaterial("MUDDY_MANGROVE_ROOTS"));
		return mudBlocks;
	}

	static {
		TRANSPARENT.clear();
		for (final Material mat : Material.values()) {
			if (GeneralMethods.isTransparent(mat)) {
				TRANSPARENT.add(mat);
			}
		}
		clearBendableMaterials();
		setupBendableMaterials();
	}

	public ElementalAbility(final Player player) {
		super(player);
	}

	public boolean isTransparent(final Block block) {
		return isTransparent(this.player, this.getName(), block);
	}

	public static void clearBendableMaterials() {
		EARTH_BLOCKS.clear();
		ICE_BLOCKS.clear();
		METAL_BLOCKS.clear();
		PLANT_BLOCKS.clear();
		SAND_BLOCKS.clear();
		SNOW_BLOCKS.clear();
	}

	public static List<String> getEarthbendableBlocks() {
		return new ArrayList<String>(EARTH_BLOCKS);
	}

	public static void addTags(Set<String> outputSet, List<String> configList) {
		ListIterator<String> iterator = new ArrayList<String>(configList).listIterator();
		iterator.forEachRemaining(next -> {
			if (next.startsWith("#")) {
				NamespacedKey key = NamespacedKey.fromString(next.replaceFirst("#", ""));
				for (Material material : Bukkit.getTag(Tag.REGISTRY_BLOCKS, key, Material.class).getValues()) {
					outputSet.add(material.toString());
				}
			} else {
				outputSet.add(next.toUpperCase());
			}
		});
	}

	public static Material[] getTransparentMaterials() {
		return TRANSPARENT.toArray(new Material[TRANSPARENT.size()]);
	}

	public static HashSet<Material> getTransparentMaterialSet() {
		return new HashSet<>(TRANSPARENT);
	}

	public static boolean isAir(final Material material) {
		return material == Material.AIR || material == Material.CAVE_AIR || material == Material.VOID_AIR ||
				(GeneralMethods.getMCVersion() >= 1170 && material == LIGHT);
	}

	public static boolean isDay(final World world) {
		final long time = world.getTime();
		if (world.getEnvironment() == Environment.NETHER || world.getEnvironment() == Environment.THE_END) {
			return true;
		}

		return time >= 23750 || time <= 12250;
	}

	public static boolean isDawn(final World world) {
		final long time = world.getTime();
		if (world.getEnvironment() == Environment.NETHER || world.getEnvironment() == Environment.THE_END) {
			return false;
		}

		return time > 23250 && time < 23750;
	}

	public static boolean isDusk(final World world) {
		final long time = world.getTime();
		if (world.getEnvironment() == Environment.NETHER || world.getEnvironment() == Environment.THE_END) {
			return false;
		}

		return time > 12250 && time < 12750;
	}

	public static boolean isNight(final World world) {
		final long time = world.getTime();
		if (world.getEnvironment() == Environment.NETHER || world.getEnvironment() == Environment.THE_END) {
			return false;
		}

		return time >= 12750 && time <= 23250;
	}

	public static boolean isEarth(final Block block) {
		return block != null && isEarth(block.getType());
	}

	public static boolean isEarth(final Material material) {
		return EARTH_BLOCKS.contains(material.toString());
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
		return ICE_BLOCKS.contains(material.toString());
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
		return SNOW_BLOCKS.contains(material.toString());
	}

	public static boolean isMeltable(final Block block) {
		return isIce(block) || isSnow(block);
	}

	public static boolean isMetal(final Block block) {
		return block != null && isMetal(block.getType());
	}

	public static boolean isMetal(final Material material) {
		return METAL_BLOCKS.contains(material.toString());
	}

	public static boolean isMetalBlock(final Block block) {
		return isMetal(block);
	}

	public static boolean isMud(final Block block) {
		return block != null ? isMud(block.getType()) : false;
	}

	public static boolean isMud(final Material material) {
		return GeneralMethods.getMCVersion() >= 1190 && MUD_BLOCKS.contains(material);
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



	public static boolean isPlant(final Block block) {
		return block != null && isPlant(block.getType());
	}

	public static boolean isPlant(final Material material) {
		return PLANT_BLOCKS.contains(material.toString());
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
		return SAND_BLOCKS.contains(material.toString());
	}

	public static boolean isTransparent(final Player player, final Block block) {
		return isTransparent(player, null, block);
	}

	public static boolean isTransparent(final Player player, final String abilityName, final Block block) {
		return Arrays.asList(getTransparentMaterials()).contains(block.getType()) && !RegionProtection.isRegionProtected(player, block.getLocation(), CoreAbility.getAbility(abilityName));
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

	public double applyModifiers(double value) {
	    return value;
	}

	public static void setupBendableMaterials() {
		addTags(EARTH_BLOCKS, getConfig().getStringList( "Properties.Earth.EarthBlocks"));
		addTags(ICE_BLOCKS, getConfig().getStringList("Properties.Water.IceBlocks"));
		addTags(METAL_BLOCKS, getConfig().getStringList("Properties.Earth.MetalBlocks"));
		addTags(PLANT_BLOCKS, getConfig().getStringList("Properties.Water.PlantBlocks"));
		addTags(SAND_BLOCKS, getConfig().getStringList("Properties.Earth.SandBlocks"));
		addTags(SNOW_BLOCKS, getConfig().getStringList("Properties.Water.SnowBlocks"));
	}
}

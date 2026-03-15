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
import org.bukkit.block.data.type.Light;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.GeneralMethods;

/**
 * ElementalAbility is used to hold methods that should be accessible by every
 * Air, Water, Earth, Fire, Chi, or AvatarAbility. This class is mainly used to
 * keep CoreAbility from becoming too cluttered.
 */
public abstract class ElementalAbility extends CoreAbility {
	private static final PotionEffectType[] POSITIVE_EFFECTS = { PotionEffectType.ABSORPTION, PotionEffectType.RESISTANCE, PotionEffectType.HASTE, PotionEffectType.FIRE_RESISTANCE, PotionEffectType.INSTANT_HEALTH, PotionEffectType.HEALTH_BOOST, PotionEffectType.STRENGTH, PotionEffectType.JUMP_BOOST, PotionEffectType.NIGHT_VISION, PotionEffectType.REGENERATION, PotionEffectType.SATURATION, PotionEffectType.SPEED, PotionEffectType.WATER_BREATHING, PotionEffectType.BREATH_OF_THE_NAUTILUS, PotionEffectType.DOLPHINS_GRACE, PotionEffectType.LUCK, PotionEffectType.CONDUIT_POWER, PotionEffectType.HERO_OF_THE_VILLAGE };
	private static final PotionEffectType[] NEUTRAL_EFFECTS = { PotionEffectType.INVISIBILITY, PotionEffectType.GLOWING, PotionEffectType.LEVITATION, PotionEffectType.SLOW_FALLING, PotionEffectType.INFESTED, PotionEffectType.OOZING, PotionEffectType.WEAVING, PotionEffectType.WIND_CHARGED, PotionEffectType.TRIAL_OMEN, PotionEffectType.RAID_OMEN, PotionEffectType.BAD_OMEN };
	private static final PotionEffectType[] NEGATIVE_EFFECTS = { PotionEffectType.POISON, PotionEffectType.BLINDNESS, PotionEffectType.NAUSEA, PotionEffectType.INSTANT_DAMAGE, PotionEffectType.HUNGER, PotionEffectType.SLOWNESS, PotionEffectType.MINING_FATIGUE, PotionEffectType.WEAKNESS, PotionEffectType.WITHER, PotionEffectType.UNLUCK, PotionEffectType.DARKNESS };
	private static final Set<Material> TRANSPARENT = new HashSet<>();

	private static final Set<String> EARTH_BLOCKS = new HashSet<>();
	private static final Set<String> ICE_BLOCKS = new HashSet<>();
	private static final Set<String> METAL_BLOCKS = new HashSet<>();
	private static final Set<String> PLANT_BLOCKS = new HashSet<>();
	private static final Set<String> SAND_BLOCKS = new HashSet<>();
	private static final Set<String> SNOW_BLOCKS = new HashSet<>();

	protected static final Set<Material> MUD_BLOCKS = getMudBlocks();
	private static Set<Material> getMudBlocks() {
		Set<Material> mudBlocks = new HashSet<>();
		mudBlocks.add(Material.MUD);
		mudBlocks.add(Material.PACKED_MUD);
		mudBlocks.add(Material.MUDDY_MANGROVE_ROOTS);
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
		return new ArrayList<>(EARTH_BLOCKS);
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

	public static boolean isAir(final Block block) {
		return block.getType().isAir() || (block.getBlockData() instanceof Light light && !light.isWaterlogged());
	}

	public static boolean isAir(final Material material) {
		return material.isAir() || material == Material.LIGHT;
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
		return block != null && isMud(block.getType());
	}

	public static boolean isMud(final Material material) {
		return MUD_BLOCKS.contains(material);
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

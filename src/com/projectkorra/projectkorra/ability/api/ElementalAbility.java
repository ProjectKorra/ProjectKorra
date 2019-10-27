package com.projectkorra.projectkorra.ability.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.projectkorra.projectkorra.ability.Ability;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;
import com.projectkorra.projectkorra.configuration.configs.properties.EarthPropertiesConfig;
import com.projectkorra.projectkorra.configuration.configs.properties.WaterPropertiesConfig;

/**
 * ElementalAbility is used to hold methods that should be accessible by every
 * Air, Water, Earth, Fire, Chi, or AvatarAbility. This class is mainly used to
 * keep CoreAbility from becoming too cluttered.
 */
public abstract class ElementalAbility<C extends AbilityConfig> extends Ability<C> {
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

	public ElementalAbility(final C config, final Player player) {
		super(config, player);
	}

	public boolean isTransparent(final Block block) {
		return isTransparent(this.player, this.getName(), block);
	}

	public List<Material> getEarthbendableBlocks() {
		return Arrays.asList(ConfigManager.getConfig(EarthPropertiesConfig.class).EarthBlocks);
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

		if (time >= 23500 || time <= 12500) {
			return true;
		}

		return false;
	}

	public static boolean isEarth(final Block block) {
		return block != null ? isEarth(block.getType()) : false;
	}

	public static boolean isEarth(final Material material) {
		return Stream.of(ConfigManager.getConfig(EarthPropertiesConfig.class).EarthBlocks).anyMatch(material::equals);
	}

	public static boolean isFullMoon(final World world) {
		final double days = Math.ceil(world.getFullTime() / 24000) + 1;
		final double phase = days % 8;

		return phase == 0;
	}

	public static boolean isIce(final Block block) {
		return block != null ? isIce(block.getType()) : false;
	}

	public static boolean isIce(final Material material) {
		return Stream.of(ConfigManager.getConfig(WaterPropertiesConfig.class).IceBlocks).anyMatch(material::equals);
	}

	public static boolean isLava(final Block block) {
		return block != null ? isLava(block.getType()) : false;
	}

	public static boolean isLava(final Material material) {
		return material == Material.LAVA;
	}

	public static boolean isSnow(final Block block) {
		return block != null ? isSnow(block.getType()) : false;
	}

	public static boolean isSnow(final Material material) {
		return Stream.of(ConfigManager.getConfig(WaterPropertiesConfig.class).SnowBlocks).anyMatch(material::equals);
	}

	public static boolean isMeltable(final Block block) {
		if (isIce(block) || isSnow(block)) {
			return true;
		}

		return false;
	}

	public static boolean isMetal(final Block block) {
		return block != null ? isMetal(block.getType()) : false;
	}

	public static boolean isMetal(final Material material) {
		return Stream.of(ConfigManager.getConfig(EarthPropertiesConfig.class).MetalBlocks).anyMatch(material::equals);
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
		if (world.getEnvironment() == Environment.NETHER || world.getEnvironment() == Environment.THE_END) {
			return false;
		}

		final long time = world.getTime();

		if (time >= 12950 && time <= 23050) {
			return true;
		}

		return false;
	}

	public static boolean isPlant(final Block block) {
		return block != null ? isPlant(block.getType()) : false;
	}

	public static boolean isPlant(final Material material) {
		return Stream.of(ConfigManager.getConfig(WaterPropertiesConfig.class).PlantBlocks).anyMatch(material::equals);
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
		return block != null ? isSand(block.getType()) : false;
	}

	public static boolean isSand(final Material material) {
		return Stream.of(ConfigManager.getConfig(EarthPropertiesConfig.class).SandBlocks).anyMatch(material::equals);
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
		} else if (isWater(block.getType())) {
			return true;
		} else {
			return isWater(block.getBlockData());
		}
	}

	public static boolean isWater(final BlockData data) {
		return (data instanceof Waterlogged) ? ((Waterlogged) data).isWaterlogged() : isWater(data.getMaterial());
	}

	public static boolean isWater(final Material material) {
		return Stream.of(ConfigManager.getConfig(WaterPropertiesConfig.class).IceBlocks).anyMatch(material::equals);
	}

}

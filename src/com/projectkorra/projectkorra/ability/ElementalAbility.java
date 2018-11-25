package com.projectkorra.projectkorra.ability;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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
		for (Material mat : Material.values()) {
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

	public List<String> getEarthbendableBlocks() {
		return getConfig().getStringList("Properties.Earth.EarthBlocks");
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
		return getConfig().getStringList("Properties.Earth.EarthBlocks").contains(material.toString());
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
		return getConfig().getStringList("Properties.Water.IceBlocks").contains(material.toString());
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
		return getConfig().getStringList("Properties.Water.SnowBlocks").contains(material.toString());
	}

	public static boolean isMeltable(final Block block) {
		if (block.getType() == Material.ICE || block.getType() == Material.SNOW) {
			return true;
		}

		return false;
	}

	public static boolean isMetal(final Block block) {
		return block != null ? isMetal(block.getType()) : false;
	}

	public static boolean isMetal(final Material material) {
		return getConfig().getStringList("Properties.Earth.MetalBlocks").contains(material.toString());
	}

	public static boolean isMetalBlock(final Block block) {
		if (block.getType() == Material.GOLD_BLOCK || block.getType() == Material.IRON_BLOCK || block.getType() == Material.IRON_ORE || block.getType() == Material.GOLD_ORE || block.getType() == Material.QUARTZ_BLOCK || block.getType() == Material.NETHER_QUARTZ_ORE) {
			return true;
		}

		return false;
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
		return getConfig().getStringList("Properties.Water.PlantBlocks").contains(material.toString());
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
		return getConfig().getStringList("Properties.Earth.SandBlocks").contains(material.toString());
	}

	public static boolean isTransparent(final Player player, final Block block) {
		return isTransparent(player, null, block);
	}

	public static boolean isTransparent(final Player player, final String abilityName, final Block block) {
		return Arrays.asList(getTransparentMaterials()).contains(block.getType()) && !GeneralMethods.isRegionProtectedFromBuild(player, abilityName, block.getLocation());
	}

	public static boolean isUndead(final Entity entity) {
		if (entity == null) {
			return false;
		} else if (entity.getType() == EntityType.ZOMBIE || entity.getType() == EntityType.BLAZE || entity.getType() == EntityType.GIANT || entity.getType() == EntityType.IRON_GOLEM || entity.getType() == EntityType.MAGMA_CUBE || entity.getType() == EntityType.PIG_ZOMBIE || entity.getType() == EntityType.SKELETON || entity.getType() == EntityType.SLIME || entity.getType() == EntityType.SNOWMAN || entity.getType() == EntityType.ZOMBIE) {
			return true;
		}

		return false;
	}

	public static boolean isWater(final Block block) {
		if (block == null) {
			return false;
		} else {
			return isWater(block.getBlockData());
		}
	}
	
	public static boolean isWater(final BlockData data) {
		return (data instanceof Waterlogged) ? ((Waterlogged) data).isWaterlogged() : isWater(data.getMaterial());
	}

	public static boolean isWater(final Material material) {
		return material == Material.WATER || material == Material.SEAGRASS || material == Material.TALL_SEAGRASS || material == Material.KELP_PLANT;
	}

}

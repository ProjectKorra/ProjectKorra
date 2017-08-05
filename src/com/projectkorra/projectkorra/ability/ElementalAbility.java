package com.projectkorra.projectkorra.ability;

import java.util.Arrays;
import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.rpg.event.EventManager;

/**
 * ElementalAbility is used to hold methods that should be accessible by every
 * Air, Water, Earth, Fire, Chi, or AvatarAbility. This class is mainly used to
 * keep CoreAbility from becoming too cluttered.
 */
public abstract class ElementalAbility extends CoreAbility {
	private static final PotionEffectType[] POSITIVE_EFFECTS = { PotionEffectType.ABSORPTION, PotionEffectType.DAMAGE_RESISTANCE, PotionEffectType.FAST_DIGGING, PotionEffectType.FIRE_RESISTANCE, PotionEffectType.HEAL, PotionEffectType.HEALTH_BOOST, PotionEffectType.INCREASE_DAMAGE, PotionEffectType.JUMP, PotionEffectType.NIGHT_VISION, PotionEffectType.REGENERATION, PotionEffectType.SATURATION, PotionEffectType.SPEED, PotionEffectType.WATER_BREATHING };
	private static final PotionEffectType[] NEUTRAL_EFFECTS = { PotionEffectType.INVISIBILITY };
	private static final PotionEffectType[] NEGATIVE_EFFECTS = { PotionEffectType.POISON, PotionEffectType.BLINDNESS, PotionEffectType.CONFUSION, PotionEffectType.HARM, PotionEffectType.HUNGER, PotionEffectType.SLOW, PotionEffectType.SLOW_DIGGING, PotionEffectType.WEAKNESS, PotionEffectType.WITHER };

	public ElementalAbility(Player player) {
		super(player);
	}

	public boolean isTransparent(Block block) {
		return isTransparent(player, getName(), block);
	}

	public static Material[] getTransparentMaterials() {
		return GeneralMethods.NON_OPAQUE;
	}

	public static HashSet<Material> getTransparentMaterialSet() {
		HashSet<Material> set = new HashSet<Material>();
		for (Material material : getTransparentMaterials()) {
			set.add(material);
		}
		 
		return set;
	}

	public static boolean isDay(World world) {
		long time = world.getTime();
		if (world.getEnvironment() == Environment.NETHER || world.getEnvironment() == Environment.THE_END) {
			return true;
		}
		
		if (time >= 23500 || time <= 12500) {
			return true;
		}
		
		return false;
	}

	public static boolean isEarth(Block block) {
		return block != null ? isEarth(block.getType()) : false;
	}

	public static boolean isEarth(Material material) {
		return getConfig().getStringList("Properties.Earth.EarthBlocks").contains(material.toString());
	}

	public static boolean isFullMoon(World world) {
		if (GeneralMethods.hasRPG()) {
			return EventManager.marker.get(world).equalsIgnoreCase("FullMoon");
		} else {
			long days = world.getFullTime() / 24000;
			long phase = days % 8;
			
			if (phase == 0) {
				return true;
			}
			
			return false;
		}
	}

	public static boolean isIce(Block block) {
		return block != null ? isIce(block.getType()) : false;
	}

	public static boolean isIce(Material material) {
		return getConfig().getStringList("Properties.Water.IceBlocks").contains(material.toString());
	}

	public static boolean isLava(Block block) {
		return block != null ? isLava(block.getType()) : false;
	}

	public static boolean isLava(Material material) {
		return material == Material.LAVA || material == Material.STATIONARY_LAVA;
	}

	public static boolean isSnow(Block block) {
		return block != null ? isSnow(block.getType()) : false;
	}

	public static boolean isSnow(Material material) {
		return getConfig().getStringList("Properties.Water.SnowBlocks").contains(material.toString());
	}

	public static boolean isLunarEclipse(World world) {
		if (world == null || !GeneralMethods.hasRPG()) {
			return false;
		}
		
		return EventManager.marker.get(world).equalsIgnoreCase("LunarEclipse");
	}

	public static boolean isSolarEclipse(World world) {
		if (world == null || !GeneralMethods.hasRPG() || !EventManager.marker.containsKey(world)) {
			return false;
		}
		
		return EventManager.marker.get(world).equalsIgnoreCase("SolarEclipse");
	}

	public static boolean isMeltable(Block block) {
		if (block.getType() == Material.ICE || block.getType() == Material.SNOW) {
			return true;
		}
		
		return false;
	}

	public static boolean isMetal(Block block) {
		return block != null ? isMetal(block.getType()) : false;
	}

	public static boolean isMetal(Material material) {
		return getConfig().getStringList("Properties.Earth.MetalBlocks").contains(material.toString());
	}

	public static boolean isMetalBlock(Block block) {
		if (block.getType() == Material.GOLD_BLOCK || block.getType() == Material.IRON_BLOCK || block.getType() == Material.IRON_ORE || block.getType() == Material.GOLD_ORE || block.getType() == Material.QUARTZ_BLOCK || block.getType() == Material.QUARTZ_ORE) {
			return true;
		}
		
		return false;
	}

	public static boolean isNegativeEffect(PotionEffectType effect) {
		for (PotionEffectType effect2 : NEGATIVE_EFFECTS) {
			if (effect2.equals(effect)) {
				return true;
			}
		}
		
		return false;
	}

	public static boolean isNeutralEffect(PotionEffectType effect) {
		for (PotionEffectType effect2 : NEUTRAL_EFFECTS) {
			if (effect2.equals(effect)) {
				return true;
			}
		}
		
		return false;
	}

	public static boolean isNight(World world) {
		if (world.getEnvironment() == Environment.NETHER || world.getEnvironment() == Environment.THE_END) {
			return false;
		}

		long time = world.getTime();
		
		if (time >= 12950 && time <= 23050) {
			return true;
		}
		
		return false;
	}

	public static boolean isPlant(Block block) {
		return block != null ? isPlant(block.getType()) : false;
	}

	public static boolean isPlant(Material material) {
		return getConfig().getStringList("Properties.Water.PlantBlocks").contains(material.toString());
	}

	public static boolean isPositiveEffect(PotionEffectType effect) {
		for (PotionEffectType effect2 : POSITIVE_EFFECTS) {
			if (effect2.equals(effect)) {
				return true;
			}
		}
		
		return false;
	}

	public static boolean isSand(Block block) {
		return block != null ? isSand(block.getType()) : false;
	}

	public static boolean isSand(Material material) {
		return getConfig().getStringList("Properties.Earth.SandBlocks").contains(material.toString());
	}

	public static boolean isSozinsComet(World world) {
		if (world == null || !GeneralMethods.hasRPG()) {
			return false;
		}
		
		return EventManager.marker.get(world).equalsIgnoreCase("SozinsComet");
	}

	public static boolean isTransparent(Player player, Block block) {
		return isTransparent(player, null, block);
	}

	public static boolean isTransparent(Player player, String abilityName, Block block) {
		return Arrays.asList(getTransparentMaterials()).contains(block.getType()) && !GeneralMethods.isRegionProtectedFromBuild(player, abilityName, block.getLocation());
	}

	public static boolean isUndead(Entity entity) {
		if (entity == null) {
			return false;
		} else if (entity.getType() == EntityType.ZOMBIE || entity.getType() == EntityType.BLAZE || entity.getType() == EntityType.GIANT || entity.getType() == EntityType.IRON_GOLEM || entity.getType() == EntityType.MAGMA_CUBE || entity.getType() == EntityType.PIG_ZOMBIE || entity.getType() == EntityType.SKELETON || entity.getType() == EntityType.SLIME || entity.getType() == EntityType.SNOWMAN || entity.getType() == EntityType.ZOMBIE) {
			return true;
		}
		
		return false;
	}

	public static boolean isWater(Block block) {
		return block != null ? isWater(block.getType()) : null;
	}

	public static boolean isWater(Material material) {
		return material == Material.WATER || material == Material.STATIONARY_WATER;
	}

}

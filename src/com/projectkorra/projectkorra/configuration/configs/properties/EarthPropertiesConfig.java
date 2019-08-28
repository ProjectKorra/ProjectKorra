package com.projectkorra.projectkorra.configuration.configs.properties;

import org.bukkit.Material;
import org.bukkit.Sound;

import com.projectkorra.projectkorra.configuration.Config;

public class EarthPropertiesConfig implements Config {

	public final String Description = "";
	
	public final Material[] EarthBlocks = {
		Material.GRASS_BLOCK,
		Material.COBBLESTONE,
		Material.COBBLESTONE_SLAB,
		Material.COBBLESTONE_STAIRS,
		Material.COBBLESTONE_WALL,
		Material.GRASS_PATH,
		Material.DIRT,
		Material.PODZOL,
		Material.COARSE_DIRT,
		Material.GRAVEL,
		Material.OBSIDIAN,
		Material.STONE,
		Material.ANDESITE,
		Material.POLISHED_ANDESITE,
		Material.GRANITE,
		Material.POLISHED_GRANITE,
		Material.DIORITE,
		Material.POLISHED_DIORITE,
		Material.STONE_BRICKS,
		Material.STONE_BRICK_SLAB,
		Material.STONE_BRICK_STAIRS,
		Material.MOSSY_STONE_BRICKS,
		Material.CRACKED_STONE_BRICKS,
		Material.CHISELED_STONE_BRICKS,
		Material.MOSSY_COBBLESTONE,
		Material.MOSSY_COBBLESTONE_WALL,
		Material.TERRACOTTA,
		Material.BLACK_GLAZED_TERRACOTTA,
		Material.BLACK_TERRACOTTA,
		Material.BLUE_GLAZED_TERRACOTTA,
		Material.BLUE_TERRACOTTA,
		Material.BROWN_GLAZED_TERRACOTTA,
		Material.BROWN_TERRACOTTA,
		Material.CYAN_GLAZED_TERRACOTTA,
		Material.CYAN_TERRACOTTA,
		Material.GRAY_GLAZED_TERRACOTTA,
		Material.GRAY_TERRACOTTA,
		Material.GREEN_GLAZED_TERRACOTTA,
		Material.GREEN_TERRACOTTA,
		Material.LIGHT_BLUE_GLAZED_TERRACOTTA,
		Material.LIGHT_BLUE_TERRACOTTA,
		Material.LIGHT_GRAY_GLAZED_TERRACOTTA,
		Material.LIGHT_GRAY_TERRACOTTA,
		Material.LIME_GLAZED_TERRACOTTA,
		Material.LIME_TERRACOTTA,
		Material.MAGENTA_GLAZED_TERRACOTTA,
		Material.MAGENTA_TERRACOTTA,
		Material.ORANGE_GLAZED_TERRACOTTA,
		Material.ORANGE_TERRACOTTA,
		Material.PINK_GLAZED_TERRACOTTA,
		Material.PINK_TERRACOTTA,
		Material.PURPLE_GLAZED_TERRACOTTA,
		Material.PURPLE_TERRACOTTA,
		Material.RED_GLAZED_TERRACOTTA,
		Material.RED_TERRACOTTA,
		Material.WHITE_GLAZED_TERRACOTTA,
		Material.WHITE_TERRACOTTA,
		Material.YELLOW_GLAZED_TERRACOTTA,
		Material.YELLOW_TERRACOTTA,
		Material.BLACK_CONCRETE,
		Material.BLUE_CONCRETE,
		Material.BROWN_CONCRETE,
		Material.CYAN_CONCRETE,
		Material.GRAY_CONCRETE,
		Material.GREEN_CONCRETE,
		Material.LIGHT_BLUE_CONCRETE,
		Material.LIGHT_GRAY_CONCRETE,
		Material.LIME_CONCRETE,
		Material.MAGENTA_CONCRETE,
		Material.ORANGE_CONCRETE,
		Material.PINK_CONCRETE,
		Material.PURPLE_CONCRETE,
		Material.RED_CONCRETE,
		Material.WHITE_CONCRETE,
		Material.YELLOW_CONCRETE,
		Material.SANDSTONE,
		Material.SANDSTONE_SLAB,
		Material.SANDSTONE_STAIRS,
		Material.CHISELED_SANDSTONE,
		Material.RED_SANDSTONE,
		Material.RED_SANDSTONE_SLAB,
		Material.RED_SANDSTONE_STAIRS,
		Material.CHISELED_RED_SANDSTONE,
		Material.BRICKS,
		Material.BRICK_SLAB,
		Material.BRICK_STAIRS,
		Material.CLAY,
		Material.END_STONE,
		Material.END_STONE_BRICKS,
		Material.NETHER_BRICKS,
		Material.NETHER_BRICK_FENCE,
		Material.NETHER_BRICK_SLAB,
		Material.NETHER_BRICK_STAIRS,
		Material.RED_NETHER_BRICKS,
		Material.NETHERRACK,
		Material.DIAMOND_ORE,
		Material.EMERALD_ORE,
		Material.LAPIS_ORE,
		Material.COAL_ORE,
		Material.COAL_BLOCK,
		Material.REDSTONE_ORE,
		Material.QUARTZ_BLOCK,
		Material.QUARTZ_PILLAR,
		Material.QUARTZ_SLAB,
		Material.QUARTZ_STAIRS,
		Material.NETHER_QUARTZ_ORE
	};
	
	public final boolean RevertEarthbending = true;
	public final long RevertCheckTime = 0;
	public final boolean SafeRevert = true;
	
	public final boolean DynamicSourcing = true;
	public final double MaxSelectRange = 0;
	
	public final boolean PlaySound = true;
	public final Sound SoundType = Sound.ENTITY_GHAST_SHOOT;
	public final float SoundVolume = 0;
	public final float SoundPitch = 0;
	
	public final Material[] MetalBlocks = {
		Material.IRON_BARS,
		Material.IRON_BLOCK,
		Material.ANVIL,
		Material.CHIPPED_ANVIL,
		Material.DAMAGED_ANVIL,
		Material.IRON_ORE,
		Material.CAULDRON,
		Material.HOPPER,
		Material.RAIL,
		Material.ACTIVATOR_RAIL,
		Material.POWERED_RAIL,
		Material.DETECTOR_RAIL,
		Material.IRON_DOOR,
		Material.IRON_TRAPDOOR,
		Material.DAYLIGHT_DETECTOR,
		Material.GOLD_BLOCK,
		Material.GOLD_ORE
	};
	public final double MetalPowerFactor = 0;
	
	public final Sound MetalSoundType = Sound.ENTITY_IRON_GOLEM_HURT;
	public final float MetalSoundVolume = 0;
	public final float MetalSoundPitch = 0;
	
	public final Material[] SandBlocks = {
		Material.SAND,
		Material.RED_SAND
	};
	
	public final Sound SandSoundType = Sound.BLOCK_SAND_BREAK;
	public final float SandSoundVolume = 0;
	public final float SandSoundPitch = 0;
	
	public final Sound LavaSoundType = Sound.BLOCK_LAVA_AMBIENT;
	public final float LavaSoundVolume = 0;
	public final float LavaSoundPitch = 0;
	
	@Override
	public String getName() {
		return "Earth";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Properties" };
	}

}

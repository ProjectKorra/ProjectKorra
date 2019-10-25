package com.projectkorra.projectkorra.configuration.configs.properties;

import org.bukkit.Material;
import org.bukkit.Sound;

import com.projectkorra.projectkorra.configuration.Config;

public class WaterPropertiesConfig implements Config {

	public final String Description = "Water is the element of change. Waterbenders use their plethora of skills to redirect and manipulate opponents attacks against them. Similar to Earthbenders, they employ both defensive and offensive strategies to accomplish this.";
	
	public final Material[] WaterBlocks = {
		Material.WATER,
		Material.SEAGRASS,
		Material.TALL_SEAGRASS,
		Material.KELP_PLANT
	};
	
	public final double NightFactor = 1.25;
	
	public final String DayMessage = "You feel your power diminish as the moon sets";
	public final String NightMessage = "You feel your waterbending become more powerful as the moon rises";
	
	public final boolean DynamicSourcing = true;
	
	public final double MaxSelectRange = 0;
	
	public final long PlantRegrowTime = 0;
	
	public final boolean PlaySound = true;
	public final Sound SoundType = Sound.BLOCK_WATER_AMBIENT;
	public final float SoundVolume = 0;
	public final float SoundPitch = 0;
	
	public final Material[] IceBlocks = {
		Material.ICE,
		Material.BLUE_ICE,
		Material.FROSTED_ICE,
		Material.PACKED_ICE
	};
	
	public final boolean FreezePlayerHead = true;
	public final boolean FreezePlayerFeet = true;
	
	public final Sound IceSoundType = Sound.ITEM_FLINTANDSTEEL_USE;
	public final float IceSoundVolume = 0;
	public final float IceSoundPitch = 0;
	
	public final Material[] SnowBlocks = {
		Material.SNOW_BLOCK,
		Material.SNOW
	};
	
	public final Material[] PlantBlocks = {
		Material.GRASS,
		Material.TALL_GRASS,
		Material.FERN,
		Material.LARGE_FERN,
		Material.ORANGE_TULIP,
		Material.PINK_TULIP,
		Material.RED_TULIP,
		Material.WHITE_TULIP,
		Material.LILAC,
		Material.ROSE_BUSH,
		Material.PEONY,
		Material.POPPY,
		Material.DANDELION,
		Material.OXEYE_DAISY,
		Material.AZURE_BLUET,
		Material.SUNFLOWER,
		Material.LILY_PAD,
		Material.BLUE_ORCHID,
		Material.ALLIUM,
		Material.SUGAR_CANE,
		Material.VINE,
		Material.BROWN_MUSHROOM,
		Material.BROWN_MUSHROOM_BLOCK,
		Material.RED_MUSHROOM,
		Material.RED_MUSHROOM_BLOCK,
		Material.ACACIA_LEAVES,
		Material.ACACIA_SAPLING,
		Material.BIRCH_LEAVES,
		Material.BIRCH_SAPLING,
		Material.DARK_OAK_LEAVES,
		Material.DARK_OAK_SAPLING,
		Material.OAK_LEAVES,
		Material.OAK_SAPLING,
		Material.SPRUCE_LEAVES,
		Material.SPRUCE_SAPLING,
		Material.JUNGLE_LEAVES,
		Material.JUNGLE_SAPLING,
		Material.CHORUS_PLANT,
		Material.CHORUS_FRUIT,
		Material.CACTUS
		
	};
	
	public final Sound PlantSoundType = Sound.BLOCK_GRASS_STEP;
	public final float PlantSoundVolume = 0;
	public final float PlantSoundPitch = 0;
	
	@Override
	public String getName() {
		return "Water";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Properties" };
	}

}

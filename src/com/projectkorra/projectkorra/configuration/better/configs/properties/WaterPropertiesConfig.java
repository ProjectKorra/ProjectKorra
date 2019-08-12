package com.projectkorra.projectkorra.configuration.better.configs.properties;

import org.bukkit.Material;
import org.bukkit.Sound;

import com.projectkorra.projectkorra.configuration.better.Config;

public class WaterPropertiesConfig implements Config {

	public final Material[] WaterBlocks = {};
	
	public final double NightFactor = 0;
	
	public final boolean PlaySound = true;
	public final Sound SoundType = Sound.BLOCK_WATER_AMBIENT;
	public final float SoundVolume = 0;
	public final float SoundPitch = 0;
	
	public final Material[] IceBlocks = {};
	
	public final Sound IceSoundType = Sound.ITEM_FLINTANDSTEEL_USE;
	public final float IceSoundVolume = 0;
	public final float IceSoundPitch = 0;
	
	public final Material[] SnowBlocks = {};
	
	public final Material[] PlantBlocks = {};
	
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

package com.projectkorra.projectkorra.configuration.better.configs.properties;

import org.bukkit.Material;
import org.bukkit.Sound;

import com.projectkorra.projectkorra.configuration.better.Config;

public class EarthPropertiesConfig implements Config {

	public final Material[] EarthBlocks = {};
	public final boolean RevertEarthbending = true;
	
	public final boolean PlaySound = true;
	public final Sound SoundType = Sound.ENTITY_GHAST_SHOOT;
	public final float SoundVolume = 0;
	public final float SoundPitch = 0;
	
	public final Material[] MetalBlocks = {};
	public final double MetalPowerFactor = 0;
	
	public final Sound MetalSoundType = Sound.ENTITY_IRON_GOLEM_HURT;
	public final float MetalSoundVolume = 0;
	public final float MetalSoundPitch = 0;
	
	public final Material[] SandBlocks = {};
	
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

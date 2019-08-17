package com.projectkorra.projectkorra.configuration.configs.properties;

import org.bukkit.Sound;

import com.projectkorra.projectkorra.configuration.Config;
import com.projectkorra.projectkorra.util.ParticleEffect;

public class FirePropertiesConfig implements Config {

	public final String Description = "";
	
	public final boolean Griefing = true;
	public final long RevertTicks = 0;
	public final double DayFactor = 0;
	
	public final String DayMessage = "";
	public final String NightMessage = "";
	
	public final ParticleEffect Particles = ParticleEffect.FLAME;
	
	public final boolean PlaySound = true;
	public final Sound SoundType = Sound.BLOCK_FIRE_AMBIENT;
	public final float SoundVolume = 0;
	public final float SoundPitch = 0;
	
	public final Sound CombustionSoundType = Sound.ENTITY_FIREWORK_ROCKET_BLAST;
	public final float CombustionSoundVolume = 0;
	public final float CombustionSoundPitch = 0;
	
	public final Sound LightningSoundType = Sound.ENTITY_CREEPER_HURT;
	public final float LightningSoundVolume = 0;
	public final float LightningSoundPitch = 0;
	
	@Override
	public String getName() {
		return "Fire";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Properties" };
	}

}

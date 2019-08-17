package com.projectkorra.projectkorra.configuration.configs.properties;

import org.bukkit.Sound;

import com.projectkorra.projectkorra.configuration.Config;
import com.projectkorra.projectkorra.util.ParticleEffect;

public class AirPropertiesConfig implements Config {

	public final String Description = "";
	
	public final ParticleEffect Particles = ParticleEffect.SPELL;
	
	public final boolean PlaySound = true;
	public final Sound SoundType = Sound.ENTITY_CREEPER_HURT;
	public final float SoundVolume = 0;
	public final float SoundPitch = 0;
	
	@Override
	public String getName() {
		return "Air";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Properties" };
	}

}

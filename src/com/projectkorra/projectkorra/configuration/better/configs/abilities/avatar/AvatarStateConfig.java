package com.projectkorra.projectkorra.configuration.better.configs.abilities.avatar;

import org.bukkit.Sound;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class AvatarStateConfig extends AbilityConfig {

	public final double PowerMultiplier = 0;
	
	public final long Duration = 0;
	public final long Cooldown = 0;
	
	public final boolean RegenerationEnabled = true;
	public final int RegenerationPower = 0;
	
	public final boolean SpeedEnabled = true;
	public final int SpeedPower = 0;
	
	public final boolean ResistanceEnabled = true;
	public final int ResistancePower = 0;
	
	public final boolean FireResistanceEnabled = true;
	
	public final boolean PlaySound = true;
	public final Sound SoundType = Sound.BLOCK_ANVIL_LAND;
	public final float SoundVolume = 0;
	public final float SoundPitch = 0;
	
	public AvatarStateConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "AvatarState";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Avatar" };
	}

}
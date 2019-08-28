package com.projectkorra.projectkorra.configuration.configs.abilities.avatar;

import org.bukkit.Sound;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class AvatarStateConfig extends AbilityConfig {

	public final double PowerMultiplier = 2.0;
	
	public final long Duration = 20000;
	public final long Cooldown = 300000;
	
	public final boolean RegenerationEnabled = true;
	public final int RegenerationPower = 2;
	
	public final boolean SpeedEnabled = true;
	public final int SpeedPower = 3;
	
	public final boolean ResistanceEnabled = true;
	public final int ResistancePower = 3;
	
	public final boolean FireResistanceEnabled = true;
	
	public final boolean PlaySound = true;
	public final Sound SoundType = Sound.BLOCK_ANVIL_LAND;
	public final float SoundVolume = 1F;
	public final float SoundPitch = 1.5F;
	
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
package com.projectkorra.projectkorra.configuration.better.configs.abilities.avatar;

import org.bukkit.Sound;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class AvatarStateConfig extends AbilityConfig {

	/**
	 * this.regenEnabled = getConfig().getBoolean("Abilities.Avatar.AvatarState.PotionEffects.Regeneration.Enabled");
		this.speedEnabled = getConfig().getBoolean("Abilities.Avatar.AvatarState.PotionEffects.Speed.Enabled");
		this.resistanceEnabled = getConfig().getBoolean("Abilities.Avatar.AvatarState.PotionEffects.DamageResistance.Enabled");
		this.fireResistanceEnabled = getConfig().getBoolean("Abilities.Avatar.AvatarState.PotionEffects.FireResistance.Enabled");
		this.regenPower = getConfig().getInt("Abilities.Avatar.AvatarState.PotionEffects.Regeneration.Power") - 1;
		this.speedPower = getConfig().getInt("Abilities.Avatar.AvatarState.PotionEffects.Speed.Power") - 1;
		this.resistancePower = getConfig().getInt("Abilities.Avatar.AvatarState.PotionEffects.DamageResistance.Power") - 1;
		this.fireResistancePower = getConfig().getInt("Abilities.Avatar.AvatarState.PotionEffects.FireResistance.Power") - 1;
		this.duration = getConfig().getLong("Abilities.Avatar.AvatarState.Duration");
		this.cooldown = getConfig().getLong("Abilities.Avatar.AvatarState.Cooldown");
		this.factor = getConfig().getDouble("Abilities.Avatar.AvatarState.PowerMultiplier");
	 */
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
	
	public AvatarStateConfig(boolean enabled, String description, String instructions) {
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
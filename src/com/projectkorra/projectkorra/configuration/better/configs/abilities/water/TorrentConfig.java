package com.projectkorra.projectkorra.configuration.better.configs.abilities.water;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class TorrentConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final int MaxLayer = 0;
	public final double Knockback = 0;
	public final double Angle = 0;
	public final double Radius = 0;
	public final double Knockup = 0;
	public final long Interval = 0;
	public final double InitialDamage = 0;
	public final double SuccessiveDamage = 0;
	public final int MaxHits = 0;
	public final double DeflectDamage = 0;
	public final double Range = 0;
	public final double SelectRange = 0;
	
	public final boolean Revert = true;
	public final long RevertTime = 0;
	
	public final double AvatarState_Knockback = 0;
	public final double AvatarState_InitialDamage = 0;
	public final double AvatarState_SuccessiveDamage = 0;
	public final int AvatarState_MaxHits = 0;
	
	public final TorrentWaveConfig WaveConfig = new TorrentWaveConfig();
	
	public static class TorrentWaveConfig {
		
		public final long Cooldown = 0;
		public final long Interval = 0;
		public final double Height = 0;
		public final double Radius = 0;
		public final double Knockback = 0;
		public final double GrowSpeed = 0;
		
	}
	
	public TorrentConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "Torrent";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Water" };
	}

}
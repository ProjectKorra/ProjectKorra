package com.projectkorra.projectkorra.configuration.configs.abilities.water;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class TorrentConfig extends AbilityConfig {

	public final long Cooldown = 3500;
	public final int MaxLayer = 3;
	public final double Knockback = .25;
	public final double Angle = 10;
	public final double Radius = 3;
	public final double Knockup = .5;
	public final long Interval = 100;
	public final double InitialDamage = 3;
	public final double SuccessiveDamage = 1;
	public final int MaxHits = 4;
	public final double DeflectDamage = 3;
	public final double Range = 25;
	public final double SelectRange = 15;
	
	public final boolean Revert = true;
	public final long RevertTime = 5000;
	
	public final double AvatarState_Knockback = 3;
	public final double AvatarState_InitialDamage = 4;
	public final double AvatarState_SuccessiveDamage = 4;
	public final int AvatarState_MaxHits = 10;
	
	public final TorrentWaveConfig WaveConfig = new TorrentWaveConfig();
	
	public static class TorrentWaveConfig {
		
		public final long Cooldown = 7500;
		public final long Interval = 100;
		public final double Height = 1;
		public final double Radius = 25;
		public final double Knockback = 2.0;
		public final double GrowSpeed = 25;
		
	}
	
	public TorrentConfig() {
		super(true, "Torrent is one of the strongest moves in a waterbender's arsenal. It has the potential to do immense damage and to be comboed with other abilities to perform a deal a large damage burst. Torrent is fundamental for waterbender's.", "\n" + "(Torrent) Left click at a water source and hold sneak to form the torrent. Then, left click and the torrent will shoot out, moving in the direction you're looking. If the torrent hits an entity, it can drag them and deal damage. Additionally, if you left click before the torrent hits a surface or entity it will freeze on impact." + "\n" + "(Wave) Left click a water source and hold sneak to form a torrent around you. Then, release sneak to send a wave of water expanding outwards every direction that will push entities back.");
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
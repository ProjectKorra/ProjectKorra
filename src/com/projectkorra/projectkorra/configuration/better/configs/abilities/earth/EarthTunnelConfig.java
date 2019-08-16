package com.projectkorra.projectkorra.configuration.better.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class EarthTunnelConfig extends AbilityConfig {

	public final long Cooldown = 0;
	public final long Interval = 0;
	public final double MaxRadius = 0;
	public final double Range = 0;
	public final double InitialRadius = 0;
	public final int BlocksPerInterval = 0;
	public final boolean Revert = true;
	public final long RevertCheckTime = 0;
	public final boolean DropLootIfNotRevert = true;
	public final boolean IgnoreOres = true;
	
	public final double AvatarState_MaxRadius = 0;
	
	public EarthTunnelConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "EarthTunnel";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Earth" };
	}

}
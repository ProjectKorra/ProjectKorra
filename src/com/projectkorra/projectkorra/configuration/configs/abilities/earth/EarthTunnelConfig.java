package com.projectkorra.projectkorra.configuration.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class EarthTunnelConfig extends AbilityConfig {

	public final long Cooldown = 3000;
	public final long Interval = 100;
	public final double MaxRadius = 4;
	public final double Range = 15;
	public final double InitialRadius = 1;
	public final int BlocksPerInterval = 3;
	public final boolean Revert = true;
	public final long RevertCheckTime = 30000;
	public final boolean DropLootIfNotRevert = true;
	public final boolean IgnoreOres = true;
	
	public final double AvatarState_MaxRadius = 8;
	
	public EarthTunnelConfig() {
		super(true, "Earth Tunnel is a completely utility ability for earthbenders. It allows you to dig a hole that lowers players down while you continue the ability, create fast escape routes or just great for making your own cave systems.", "Hold sneak while looking at an earthbendable block to tunnel the blocks away. If you release sneak or look at a block that isn't earthbendable, the ability will cancel.");
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
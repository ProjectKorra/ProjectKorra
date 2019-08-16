package com.projectkorra.projectkorra.configuration.better.configs.abilities.earth;

import org.bukkit.Material;

import com.projectkorra.projectkorra.configuration.better.configs.abilities.AbilityConfig;

public class LavaFlowConfig extends AbilityConfig {

	public final long ShiftCooldown = 0;
	public final long ClickLavaCooldown = 0;
	public final long ClickLandCooldown = 0;
	public final Material RevertMaterial = Material.STONE;
	public final double ShiftPlatformRadius = 0;
	public final double ShiftRadius = 0;
	public final double ShiftFlowSpeed = 0;
	public final double ShiftRemoveSpeed = 0;
	public final long ShiftCleanupDelay = 0;
	public final double ParticleDensity = 0;
	public final double ClickRange = 0;
	public final double ClickRadius = 0;
	public final long ClickLavaStartDelay = 0;
	public final long ClickLandStartDelay = 0;
	public final long ClickLavaCleanupDelay = 0;
	public final long ClickLandCleanupDelay = 0;
	public final double ClickLavaCreateSpeed = 0;
	public final double ClickLandCreateSpeed = 0;
	public final int UpwardFlow = 0;
	public final int DownwardFlow = 0;
	public final boolean AllowNaturalFlow = true;
	
	public final long AvatarState_ShiftCooldown = 0;
	public final long AvatarState_ClickLavaCooldown = 0;
	public final long AvatarState_ClickLandCooldown = 0;
	public final double AvatarState_ShiftPlatformRadius = 0;
	public final double AvatarState_ShiftRadius = 0;
	public final double AvatarState_ClickRadius = 0;
	
	public LavaFlowConfig() {
		super(true, "", "");
	}

	@Override
	public String getName() {
		return "LavaFlow";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Earth" };
	}

}
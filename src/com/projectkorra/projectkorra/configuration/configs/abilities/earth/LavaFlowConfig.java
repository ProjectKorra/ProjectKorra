package com.projectkorra.projectkorra.configuration.configs.abilities.earth;

import org.bukkit.Material;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class LavaFlowConfig extends AbilityConfig {

	public final long ShiftCooldown = 10000;
	public final long ClickLavaCooldown = 10000;
	public final long ClickLandCooldown = 1500;
	public final Material RevertMaterial = Material.MAGMA_BLOCK;
	public final double ShiftPlatformRadius = 3;
	public final double ShiftRadius = 15;
	public final double ShiftFlowSpeed = .2;
	public final double ShiftRemoveSpeed = .5;
	public final long ShiftCleanupDelay = 7000;
	public final double ParticleDensity = 2;
	public final double ClickRange = 20;
	public final double ClickRadius = 5;
	public final long ClickLavaStartDelay = 750;
	public final long ClickLandStartDelay = 400;
	public final long ClickLavaCleanupDelay = 3500;
	public final long ClickLandCleanupDelay = 14000;
	public final double ClickLavaCreateSpeed = .045;
	public final double ClickLandCreateSpeed = .5;
	public final int UpwardFlow = 2;
	public final int DownwardFlow = 2;
	public final boolean AllowNaturalFlow = true;
	
	public final long AvatarState_ShiftCooldown = 2500;
	public final long AvatarState_ClickLavaCooldown = 1000;
	public final long AvatarState_ClickLandCooldown = 0;
	public final double AvatarState_ShiftPlatformRadius = 10;
	public final double AvatarState_ShiftRadius = 25;
	public final double AvatarState_ClickRadius = 20;
	
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
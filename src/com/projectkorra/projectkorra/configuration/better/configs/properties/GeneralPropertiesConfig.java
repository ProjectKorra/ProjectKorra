package com.projectkorra.projectkorra.configuration.better.configs.properties;

import com.projectkorra.projectkorra.configuration.better.Config;

public class GeneralPropertiesConfig implements Config {

	public final boolean UpdateChecker = true;
	
	public final double RegionProtection_CacheBlockTime = 0;
	public final boolean RegionProtection_RespectResidence = true;
	public final String RegionProtection_ResidenceFlag = "";
	public final boolean RegionProtection_AllowHarmlessAbilities = true;
	public final boolean RegionProtection_RespectWorldGuard = true;
	public final boolean RegionProtection_RespectFactions = true;
	public final boolean RegionProtection_RespectTowny = true;
	public final boolean RegionProtection_RespectGriefPrevention = true;
	public final boolean RegionProtection_RespectLWC = true;
	public final boolean RegionProtection_RespectKingdoms = true;
	public final boolean RegionProtection_RespectRedProtect = true;
	
	public final boolean Statistics = true;
	
	public final boolean DatabaseCooldowns = true;
	
	public final boolean BendingPreview = true;
	
	public final long GlobalCooldown = 0;
	
	public final boolean TogglePassivesWithAllBending = true;
	
	public final int MaxPresets = 0;
	
	public final boolean ImportEnabled = false;
	
	public final boolean BendingAffectFallingSand_Normal = true;
	public final double BendingAffectFallingSand_Normal_StrengthMultiplier = 0;
	
	public final boolean BendingAffectFallingSand_TNT = true;
	public final double BendingAffectFallingSand_TNT_StrengthMultiplier = 0;
	
	public final boolean DeathMessages = true;
	
	public final boolean ApplyHorizontalCollisionBarrierBlockDamage = true;
	
	public final String[] DisabledWorlds = {};
	
	@Override
	public String getName() {
		return "General";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Properties" };
	}

}
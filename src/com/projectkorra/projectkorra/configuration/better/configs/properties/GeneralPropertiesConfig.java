package com.projectkorra.projectkorra.configuration.better.configs.properties;

import com.projectkorra.projectkorra.configuration.better.Config;

public class GeneralPropertiesConfig implements Config {

	public final boolean UpdateChecker = true;
	
	public final RegionProtectionConfig RegionProtection = new RegionProtectionConfig();
	
	public final boolean Statistics = true;
	
	public final boolean DatabaseCooldowns = true;
	
	public final MySQLConfig MySQL = new MySQLConfig();
	
	public final boolean BendingPreview = true;
	
	public final long GlobalCooldown = 0;
	
	public final long ChooseCooldown = 0;
	
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
	
	public static final class RegionProtectionConfig {
		
		public final double CacheBlockTime = 0;
		
		public final boolean RespectResidence = true;
		public final String ResidenceFlag = "";
		
		public final boolean AllowHarmlessAbilities = true;
		
		public final boolean RespectWorldGuard = true;
		
		public final boolean RespectFactions = true;
		
		public final boolean RespectTowny = true;
		
		public final boolean RespectGriefPrevention = true;
		
		public final boolean RespectLWC = true;
		
		public final boolean RespectKingdoms = true;
		
		public final boolean RespectRedProtect = true;
		
	}
	
	public static final class MySQLConfig {
		
		public final boolean Enabled = false;
		
		public final String Host = "";
		public final int Port = 3306;
		public final String Username = "";
		public final String Password = "";
		public final String Database = "";
		
	}
	
	@Override
	public String getName() {
		return "General";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Properties" };
	}

}
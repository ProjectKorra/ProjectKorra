package com.projectkorra.projectkorra.airbending.passive;

import com.projectkorra.projectkorra.configuration.ConfigManager;

public class AirPassive {
	
	public static double getExhaustionFactor() {
		return getFactor();
	}

	public static double getFactor() {
		return ConfigManager.airConfig.get().getDouble("Abilities.Air.Passive.Factor");
	}

	public static int getJumpPower() {
		return ConfigManager.airConfig.get().getInt("Abilities.Air.Passive.AirAgility.JumpPower");
	}

	public static int getSpeedPower() {
		return ConfigManager.airConfig.get().getInt("Abilities.Air.Passive.AirAgility.SpeedPower");
	}
}

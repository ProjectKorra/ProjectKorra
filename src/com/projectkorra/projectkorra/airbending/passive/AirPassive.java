package com.projectkorra.projectkorra.airbending.passive;

import com.projectkorra.projectkorra.configuration.ConfigManager;

public class AirPassive {

	public static double getExhaustionFactor() {
		return getFactor();
	}

	public static double getFactor() {
		return ConfigManager.getConfig().getDouble("Abilities.Air.Passive.Factor");
	}

	public static int getJumpPower() {
		return ConfigManager.getConfig().getInt("Abilities.Air.Passive.AirAgility.JumpPower");
	}

	public static int getSpeedPower() {
		return ConfigManager.getConfig().getInt("Abilities.Air.Passive.AirAgility.SpeedPower");
	}
}

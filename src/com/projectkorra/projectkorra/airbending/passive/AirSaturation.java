package com.projectkorra.projectkorra.airbending.passive;

import com.projectkorra.projectkorra.ability.api.AirAbility;
import com.projectkorra.projectkorra.airbending.util.AirPassiveAbilityInfo;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.abilities.air.AirSaturationConfig;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class AirSaturation extends AirAbility<AirSaturationInfo, AirSaturationConfig> {
	public AirSaturation(final AirSaturationConfig config, final Player player) {
		super(config, player);
	}

	public static double getExhaustionFactor() {
		return ConfigManager.getConfig(AirSaturationConfig.class).ExhaustionFactor;
	}

	@Override
	public void progress() {}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public String getName() {
		return "AirSaturation";
	}

	@Override
	public Location getLocation() {
		return this.player.getLocation();
	}

	@Override
	public Class<AirSaturationConfig> getConfigType() {
		return AirSaturationConfig.class;
	}
}

class AirSaturationInfo extends AirPassiveAbilityInfo {

	@Override
	public String getName() {
		return "AirSaturation";
	}

	@Override
	public boolean isInstantiable() {
		return false;
	}

	@Override
	public boolean isProgressable() {
		return false;
	}
}

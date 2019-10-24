package com.projectkorra.projectkorra.chiblocking.passive;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.api.ChiAbility;
import com.projectkorra.projectkorra.ability.api.PassiveAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.abilities.chi.ChiSaturationConfig;

public class ChiSaturation extends ChiAbility<ChiSaturationConfig> implements PassiveAbility {
	public ChiSaturation(final ChiSaturationConfig config, final Player player) {
		super(config, player);
	}

	public static double getExhaustionFactor() {
		return ConfigManager.getConfig(ChiSaturationConfig.class).ExhaustionFactor;
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
		return "ChiSaturation";
	}

	@Override
	public Location getLocation() {
		return this.player.getLocation();
	}

	@Override
	public boolean isInstantiable() {
		return false;
	}

	@Override
	public boolean isProgressable() {
		return false;
	}
	
	@Override
	public Class<ChiSaturationConfig> getConfigType() {
		return ChiSaturationConfig.class;
	}
}

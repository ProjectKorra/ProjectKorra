package com.projectkorra.projectkorra.chiblocking.passive;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.abilities.chi.AcrobaticsConfig;

public class Acrobatics extends ChiAbility<AcrobaticsConfig> implements PassiveAbility {
	public Acrobatics(final AcrobaticsConfig config, final Player player) {
		super(config, player);
	}

	public static double getFallReductionFactor() {
		return ConfigManager.getConfig(AcrobaticsConfig.class).FallReductionFactor;
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
		return "Acrobatics";
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
	public Class<AcrobaticsConfig> getConfigType() {
		return AcrobaticsConfig.class;
	}
}

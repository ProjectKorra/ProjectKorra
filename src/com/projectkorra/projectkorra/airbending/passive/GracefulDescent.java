package com.projectkorra.projectkorra.airbending.passive;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.api.AirAbility;
import com.projectkorra.projectkorra.ability.api.PassiveAbility;
import com.projectkorra.projectkorra.configuration.configs.abilities.air.GracefulDescentConfig;

public class GracefulDescent extends AirAbility<GracefulDescentConfig> implements PassiveAbility {

	public GracefulDescent(final GracefulDescentConfig config, final Player player) {
		super(config, player);
	}

	@Override
	public void progress() {

	}

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
		return "GracefulDescent";
	}

	@Override
	public Location getLocation() {
		return null;
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
	public Class<GracefulDescentConfig> getConfigType() {
		return GracefulDescentConfig.class;
	}
}

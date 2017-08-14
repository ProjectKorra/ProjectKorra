package com.projectkorra.projectkorra.chiblocking.passive;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;

public class Acrobatics extends ChiAbility implements PassiveAbility {
	public Acrobatics(Player player) {
		super(player);
	}

	public static double getFallReductionFactor() {
		return ConfigManager.getConfig().getDouble("Abilities.Chi.Passive.Acrobatics.FallReductionFactor");
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
		return player.getLocation();
	}

	@Override
	public boolean isInstantiable() {
		return false;
	}
}

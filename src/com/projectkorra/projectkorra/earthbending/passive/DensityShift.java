package com.projectkorra.projectkorra.earthbending.passive;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;

public class DensityShift extends EarthAbility implements PassiveAbility {

	public DensityShift(Player player) {
		super(player);
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
		return "DensityShift";
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public boolean isInstantiable() {
		return false;
	}

}

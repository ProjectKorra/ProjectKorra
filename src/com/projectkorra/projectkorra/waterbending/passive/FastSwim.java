package com.projectkorra.projectkorra.waterbending.passive;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.EarthArmor;
import com.projectkorra.projectkorra.waterbending.WaterSpout;
import com.projectkorra.projectkorra.waterbending.multiabilities.WaterArms;

public class FastSwim extends WaterAbility implements PassiveAbility {

	private double swimSpeed;

	public FastSwim(Player player) {
		super(player);

		this.swimSpeed = ConfigManager.getConfig().getDouble("Abilities.Water.Passive.FastSwim.SpeedFactor");
	}

	@Override
	public void progress() {
		if (CoreAbility.hasAbility(player, WaterSpout.class) || CoreAbility.hasAbility(player, EarthArmor.class)) {
			return;
		} else if (CoreAbility.hasAbility(player, WaterArms.class)) {
			return;
		} else if (bPlayer.getBoundAbility() == null || (bPlayer.getBoundAbility() != null && !bPlayer.getBoundAbility().isSneakAbility())) {
			if (player.isSneaking() && WaterAbility.isWater(player.getLocation().getBlock())) {
				player.setVelocity(player.getEyeLocation().getDirection().clone().normalize().multiply(swimSpeed));
			}
		}
	}

	@Override
	public boolean isSneakAbility() {
		return true;
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
		return "FastSwim";
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public boolean isInstantiable() {
		return true;
	}

}

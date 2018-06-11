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
	private long cooldown;
	private double swimSpeed;
	private long duration;

	public FastSwim(Player player) {
		super(player);
		if (bPlayer.isOnCooldown(this)) {
			return;
		}

		this.cooldown = ConfigManager.getConfig().getLong("Abilities.Water.Passive.FastSwim.Cooldown");
		this.swimSpeed = ConfigManager.getConfig().getDouble("Abilities.Water.Passive.FastSwim.SpeedFactor");
		this.duration = ConfigManager.getConfig().getLong("Abilities.Water.Passive.FastSwim.Duration");
	}

	@Override
	public void progress() {
		if (!bPlayer.canUsePassive(this) || !bPlayer.canBendPassive(this) || 
				CoreAbility.hasAbility(player, WaterSpout.class) || CoreAbility.hasAbility(player, EarthArmor.class) || CoreAbility.hasAbility(player, WaterArms.class)) {
			return;
		}
		
		if (bPlayer.getBoundAbility() == null || (bPlayer.getBoundAbility() != null && !bPlayer.getBoundAbility().isSneakAbility())) {
			if (player.isSneaking() && WaterAbility.isWater(player.getLocation().getBlock()) && !bPlayer.isOnCooldown(this)) {
				if(duration != 0 && System.currentTimeMillis() > getStartTime() + duration) {
					bPlayer.addCooldown(this);
					return;
				}
				player.setVelocity(player.getEyeLocation().getDirection().clone().normalize().multiply(swimSpeed));	
			} else if (!player.isSneaking()) {
				bPlayer.addCooldown(this);
			}
		}
	}
	
	public static double getSwimSpeed() {
		return ConfigManager.getConfig().getDouble("Abilities.Water.Passive.FastSwim.SpeedFactor");
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
		return cooldown;
	}

	@Override
	public String getName() {
		return "FastSwim";
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	@Override
	public boolean isInstantiable() {
		return true;
	}
	
	@Override
	public boolean isProgressable() {
		return true;
	}
}

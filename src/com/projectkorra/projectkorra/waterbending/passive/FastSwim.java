package com.projectkorra.projectkorra.waterbending.passive;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.configuration.better.ConfigManager;
import com.projectkorra.projectkorra.configuration.better.configs.abilities.water.FastSwimConfig;
import com.projectkorra.projectkorra.earthbending.EarthArmor;
import com.projectkorra.projectkorra.waterbending.WaterSpout;
import com.projectkorra.projectkorra.waterbending.multiabilities.WaterArms;

public class FastSwim extends WaterAbility<FastSwimConfig> implements PassiveAbility {

	private long cooldown;
	private double swimSpeed;
	private long duration;

	public FastSwim(final FastSwimConfig config, final Player player) {
		super(config, player);
		if (this.bPlayer.isOnCooldown(this)) {
			return;
		}

		if (player.isSneaking()) { // the sneak event calls before they actually start sneaking
			return;
		}

		this.cooldown = config.Cooldown;
		this.swimSpeed = config.SpeedFactor;
		this.duration = config.Duration;

		this.start();
	}

	@Override
	public void progress() {
		if (!this.bPlayer.canUsePassive(this) || !this.bPlayer.canBendPassive(this) || CoreAbility.hasAbility(this.player, WaterSpout.class) || CoreAbility.hasAbility(this.player, EarthArmor.class) || CoreAbility.hasAbility(this.player, WaterArms.class)) {
			this.remove();
			return;
		}

		if (this.duration > 0 && System.currentTimeMillis() > this.getStartTime() + this.duration) {
			this.bPlayer.addCooldown(this);
			this.remove();
			return;
		}

		if (this.bPlayer.getBoundAbility() == null || (this.bPlayer.getBoundAbility() != null && !this.bPlayer.getBoundAbility().isSneakAbility())) {
			if (this.player.isSneaking()) {
				if (isWater(this.player.getLocation().getBlock()) && !this.bPlayer.isOnCooldown(this)) {
					this.player.setVelocity(this.player.getEyeLocation().getDirection().clone().normalize().multiply(this.swimSpeed));
				}
			} else {
				this.bPlayer.addCooldown(this);
				this.remove();
			}
		}
	}

	public static double getSwimSpeed() {
		return ConfigManager.getConfig(FastSwimConfig.class).SpeedFactor;
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
		return this.cooldown;
	}

	@Override
	public String getName() {
		return "FastSwim";
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
		return true;
	}
}

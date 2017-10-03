package com.projectkorra.projectkorra.chiblocking.passive;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.airbending.passive.AirAgility;
import com.projectkorra.projectkorra.chiblocking.AcrobatStance;
import com.projectkorra.projectkorra.configuration.ConfigManager;

public class ChiAgility extends ChiAbility implements PassiveAbility {

	// Configurable variables
	private int jumpPower;
	private int speedPower;

	// Instance related variables
	private boolean jumpActivate;
	private boolean speedActivate;

	public ChiAgility(Player player) {
		super(player);
		setFields();
	}

	public void setFields() {
		this.jumpPower = ConfigManager.getConfig().getInt("Abilities.Chi.Passive.ChiAgility.JumpPower");
		this.speedPower = ConfigManager.getConfig().getInt("Abilities.Chi.Passive.ChiAgility.SpeedPower");
	}

	@Override
	public void progress() {
		if (!player.isSprinting()) {
			return;
		}

		if (CoreAbility.hasAbility(player, AirAgility.class)) {
			AirAgility airAgility = CoreAbility.getAbility(player, AirAgility.class);
			if (airAgility.getJumpPower() > jumpPower) {
				jumpPower = airAgility.getJumpPower();
			}
			if (airAgility.getSpeedPower() > speedPower) {
				speedPower = airAgility.getSpeedPower();
			}
		}
		
		if (hasAbility(player, AcrobatStance.class)) {
			AcrobatStance stance = getAbility(player, AcrobatStance.class);
			jumpPower = Math.max(jumpPower, stance.getJump());
			speedPower = Math.max(speedPower, stance.getSpeed());
		}
		// Jump Buff
		jumpActivate = true;
		if (player.hasPotionEffect(PotionEffectType.JUMP)) {
			PotionEffect potion = player.getPotionEffect(PotionEffectType.JUMP);
			if (potion.getAmplifier() > jumpPower - 1) {
				jumpActivate = false;
			} else {
				player.removePotionEffect(PotionEffectType.JUMP);
			}
		}
		if (jumpActivate) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20, jumpPower - 1, true, false), false);
		}

		// Speed Buff
		speedActivate = true;
		if (player.hasPotionEffect(PotionEffectType.SPEED)) {
			PotionEffect potion = player.getPotionEffect(PotionEffectType.SPEED);
			if (potion.getAmplifier() > speedPower - 1) {
				speedActivate = false;
			} else {
				player.removePotionEffect(PotionEffectType.SPEED);
			}
		}
		if (speedActivate) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, speedPower - 1, true, false), false);
		}
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
		return "ChiAgility";
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

	public int getJumpPower() {
		return jumpPower;
	}

	public int getSpeedPower() {
		return speedPower;
	}

}

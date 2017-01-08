package com.projectkorra.projectkorra.airbending.passive;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;

public class AirAgilityPassive extends AirAbility implements PassiveAbility {
	
	// Configurable variables
	private int jumpPower;
	private int speedPower;
	
	// Instance related variables
	private boolean jumpActivate;
	private boolean speedActivate;

	public AirAgilityPassive(Player player) {
		super(player);
		setFields();
	}
	
	public void setFields() {
		this.jumpPower = ConfigManager.airConfig.get().getInt("Abilities.Air.Passive.AirAgility.JumpPower");
		this.speedPower = ConfigManager.airConfig.get().getInt("Abilities.Air.Passive.AirAgility.SpeedPower");
	}

	@Override
	public void progress() {
		if (!player.isSprinting()) {
			return;
		}
		
		// Jump Buff
		jumpActivate = true;
		if (player.hasPotionEffect(PotionEffectType.JUMP)) {
			for (PotionEffect potion : player.getActivePotionEffects()) {
				if (potion.getType() == PotionEffectType.JUMP) {
					if (potion.getAmplifier() > jumpPower - 1) {
						jumpActivate = false;
					}
				}
			}
		}
		if (jumpActivate) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20, jumpPower - 1, true, false), false);
		}

		// Speed Buff
		speedActivate = true;
		if (player.hasPotionEffect(PotionEffectType.SPEED)) {
			for (PotionEffect potion : player.getActivePotionEffects()) {
				if (potion.getType() == PotionEffectType.SPEED) {
					if (potion.getAmplifier() > speedPower - 1) {
						speedActivate = false;
					}
				}
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
		return "AirAgility";
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

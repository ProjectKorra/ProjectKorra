package com.projectkorra.projectkorra.chiblocking.passives;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.chiblocking.AcrobatStance;
import com.projectkorra.projectkorra.configuration.ConfigManager;

public class ChiAgilityPassive extends ChiAbility implements PassiveAbility {

	// Configurable variables
	private int jumpPower;
	private int speedPower;
	
	// Instance related variables
	private boolean jumpActivate;
	private boolean speedActivate;

	public ChiAgilityPassive(Player player) {
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
		
		// Jump Buff
		int jMax = jumpPower;
		if (hasAbility(player, AcrobatStance.class)) {
			AcrobatStance stance = getAbility(player, AcrobatStance.class);
			jMax = Math.max(jMax, stance.getJump());
		}
		jumpActivate = true;
		if (player.hasPotionEffect(PotionEffectType.JUMP)) {
			for (PotionEffect potion : player.getActivePotionEffects()) {
				if (potion.getType() == PotionEffectType.JUMP) {
					if (potion.getAmplifier() > jMax - 1) {
						jumpActivate = false;
					}
				}
			}
		}
		if (jumpActivate) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20, jMax - 1, true, false), false);
		}

		// Speed Buff
		int sMax = speedPower;
		if (hasAbility(player, AcrobatStance.class)) {
			AcrobatStance stance = getAbility(player, AcrobatStance.class);
			sMax = Math.max(sMax, stance.getJump());
		}
		speedActivate = true;
		if (player.hasPotionEffect(PotionEffectType.SPEED)) {
			for (PotionEffect potion : player.getActivePotionEffects()) {
				if (potion.getType() == PotionEffectType.SPEED) {
					if (potion.getAmplifier() > sMax - 1) {
						speedActivate = false;
					}
				}
			}
		}
		if (speedActivate) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, sMax - 1, true, false), false);
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
	public boolean isPlaceholder() {
		return false;
	}

}

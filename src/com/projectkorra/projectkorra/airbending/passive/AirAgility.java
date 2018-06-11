package com.projectkorra.projectkorra.airbending.passive;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.chiblocking.passive.ChiAgility;
import com.projectkorra.projectkorra.configuration.ConfigManager;

public class AirAgility extends AirAbility implements PassiveAbility {

	// Configurable variables
	private int jumpPower;
	private int speedPower;

	// Instance related variables
	private boolean jumpActivate;
	private boolean speedActivate;

	public AirAgility(Player player) {
		super(player);
		setFields();
	}

	public void setFields() {
		this.jumpPower = ConfigManager.getConfig().getInt("Abilities.Air.Passive.AirAgility.JumpPower");
		this.speedPower = ConfigManager.getConfig().getInt("Abilities.Air.Passive.AirAgility.SpeedPower");
	}

	@Override
	public void progress() {
		if (!player.isSprinting() || !bPlayer.canUsePassive(this) || !bPlayer.canBendPassive(this)) {
			return;
		}

		if (CoreAbility.hasAbility(player, ChiAgility.class) && bPlayer.canBendPassive(CoreAbility.getAbility(ChiAbility.class))) {
			ChiAgility chiAgility = CoreAbility.getAbility(player, ChiAgility.class);
			if (chiAgility.getJumpPower() > jumpPower) {
				jumpPower = chiAgility.getJumpPower();
			}
			if (chiAgility.getSpeedPower() > speedPower) {
				speedPower = chiAgility.getSpeedPower();
			}
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
		return "AirAgility";
	}

	@Override
	public Location getLocation() {
		return player != null ? player.getLocation() : null;
	}

	@Override
	public boolean isInstantiable() {
		return true;
	}
	
	@Override
	public boolean isProgressable() {
		return true;
	}

	public int getJumpPower() {
		return jumpPower;
	}

	public int getSpeedPower() {
		return speedPower;
	}

}

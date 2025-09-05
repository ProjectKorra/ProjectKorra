package com.projectkorra.projectkorra.airbending.passive;

import com.projectkorra.projectkorra.GeneralMethods;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.configuration.ConfigManager;

public class AirAgility extends AirAbility implements PassiveAbility {

	// Configurable variables.
	@Attribute("Jump")
	private int jumpPower;
	@Attribute(Attribute.SPEED)
	private int speedPower;

	public AirAgility(final Player player) {
		super(player);
		this.setFields();
	}

	public void setFields() {
		this.jumpPower = ConfigManager.getConfig().getInt("Abilities.Air.Passive.AirAgility.JumpPower") - 1;
		this.speedPower = ConfigManager.getConfig().getInt("Abilities.Air.Passive.AirAgility.SpeedPower") - 1;
	}

	@Override
	public void progress() {
		if (!this.player.isSprinting() || !this.bPlayer.canUsePassive(this) || !this.bPlayer.canBendPassive(this)) {
			return;
		}

		// Check if the player is wearing any armor
		int armorPoints = GeneralMethods.getArmorPoints(player);

		boolean shouldApplyNerf = armorPoints > 6;

		// Set jump and speed values based on armor status
		int adjustedJumpPower = shouldApplyNerf ? 0 : this.jumpPower;
		int adjustedSpeedPower = shouldApplyNerf ? 0 : this.speedPower;

		// Jump Buff.
		if (!this.player.hasPotionEffect(PotionEffectType.JUMP) || this.player.getPotionEffect(PotionEffectType.JUMP).getAmplifier() < this.jumpPower || (this.player.getPotionEffect(PotionEffectType.JUMP).getAmplifier() == this.jumpPower && this.player.getPotionEffect(PotionEffectType.JUMP).getDuration() == 1)) {
			this.player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 10, adjustedJumpPower, true, false), true);
		}
		// Speed Buff.
		if (!this.player.hasPotionEffect(PotionEffectType.SPEED) || this.player.getPotionEffect(PotionEffectType.SPEED).getAmplifier() < this.speedPower || (this.player.getPotionEffect(PotionEffectType.SPEED).getAmplifier() == this.speedPower && this.player.getPotionEffect(PotionEffectType.SPEED).getDuration() == 1)) {
			this.player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10, adjustedSpeedPower, true, false), true);
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
		return this.player != null ? this.player.getLocation() : null;
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
		return this.jumpPower;
	}

	public int getSpeedPower() {
		return this.speedPower;
	}

}

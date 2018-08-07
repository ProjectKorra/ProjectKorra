package com.projectkorra.projectkorra.airbending.passive;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.chiblocking.passive.ChiAgility;
import com.projectkorra.projectkorra.configuration.ConfigManager;

public class AirAgility extends AirAbility implements PassiveAbility {

	// Configurable variables.
	private int jumpPower;
	private int speedPower;

	// Instance related variables.
	private boolean jumpActivate;
	private boolean speedActivate;

	public AirAgility(final Player player) {
		super(player);
		this.setFields();
	}

	public void setFields() {
		this.jumpPower = ConfigManager.getConfig().getInt("Abilities.Air.Passive.AirAgility.JumpPower");
		this.speedPower = ConfigManager.getConfig().getInt("Abilities.Air.Passive.AirAgility.SpeedPower");
	}

	@Override
	public void progress() {
		if (!this.player.isSprinting() || !this.bPlayer.canUsePassive(this) || !this.bPlayer.canBendPassive(this)) {
			return;
		}

		if (CoreAbility.hasAbility(this.player, ChiAgility.class) && this.bPlayer.canBendPassive(CoreAbility.getAbility(ChiAbility.class))) {
			final ChiAgility chiAgility = CoreAbility.getAbility(this.player, ChiAgility.class);
			if (chiAgility.getJumpPower() > this.jumpPower) {
				this.jumpPower = chiAgility.getJumpPower();
			}
			if (chiAgility.getSpeedPower() > this.speedPower) {
				this.speedPower = chiAgility.getSpeedPower();
			}
		}

		// Jump Buff.
		this.jumpActivate = true;
		if (this.player.hasPotionEffect(PotionEffectType.JUMP)) {
			final PotionEffect potion = this.player.getPotionEffect(PotionEffectType.JUMP);
			if (potion.getAmplifier() > this.jumpPower - 1) {
				this.jumpActivate = false;
			} else {
				this.player.removePotionEffect(PotionEffectType.JUMP);
			}
		}
		if (this.jumpActivate) {
			this.player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20, this.jumpPower - 1, true, false), false);
		}

		// Speed Buff.
		this.speedActivate = true;
		if (this.player.hasPotionEffect(PotionEffectType.SPEED)) {
			final PotionEffect potion = this.player.getPotionEffect(PotionEffectType.SPEED);
			if (potion.getAmplifier() > this.speedPower - 1) {
				this.speedActivate = false;
			} else {
				this.player.removePotionEffect(PotionEffectType.SPEED);
			}
		}
		if (this.speedActivate) {
			this.player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, this.speedPower - 1, true, false), false);
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

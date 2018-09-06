package com.projectkorra.projectkorra.chiblocking.passive;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.airbending.passive.AirAgility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.chiblocking.AcrobatStance;
import com.projectkorra.projectkorra.configuration.ConfigManager;

public class ChiAgility extends ChiAbility implements PassiveAbility {

	// Configurable variables.
	@Attribute("Jump")
	private int jumpPower;
	@Attribute(Attribute.SPEED)
	private int speedPower;

	// Instance related variables.
	private boolean jumpActivate;
	private boolean speedActivate;

	public ChiAgility(final Player player) {
		super(player);
		this.setFields();
	}

	public void setFields() {
		this.jumpPower = ConfigManager.getConfig().getInt("Abilities.Chi.Passive.ChiAgility.JumpPower");
		this.speedPower = ConfigManager.getConfig().getInt("Abilities.Chi.Passive.ChiAgility.SpeedPower");
	}

	@Override
	public void progress() {
		if (!this.player.isSprinting() || !this.bPlayer.canUsePassive(this) || !this.bPlayer.canBendPassive(this)) {
			return;
		}

		if (CoreAbility.hasAbility(this.player, AirAgility.class) && this.bPlayer.canBendPassive(CoreAbility.getAbility(AirAbility.class))) {
			final AirAgility airAgility = CoreAbility.getAbility(this.player, AirAgility.class);
			if (airAgility.getJumpPower() > this.jumpPower) {
				this.jumpPower = airAgility.getJumpPower();
			}
			if (airAgility.getSpeedPower() > this.speedPower) {
				this.speedPower = airAgility.getSpeedPower();
			}
		}

		if (hasAbility(this.player, AcrobatStance.class)) {
			final AcrobatStance stance = getAbility(this.player, AcrobatStance.class);
			this.jumpPower = Math.max(this.jumpPower, stance.getJump());
			this.speedPower = Math.max(this.speedPower, stance.getSpeed());
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
		return "ChiAgility";
	}

	@Override
	public Location getLocation() {
		return null;
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

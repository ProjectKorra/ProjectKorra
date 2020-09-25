package com.projectkorra.projectkorra.chiblocking;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.attribute.Attribute;

public class AcrobatStance extends ChiAbility {

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute(Attribute.SPEED)
	private int speed;
	@Attribute("Jump")
	private int jump;
	@Attribute("ChiBlockBoost")
	private double chiBlockBoost;
	@Attribute("ParalyzeDodgeBoost")
	private double paralyzeDodgeBoost;

	public AcrobatStance(final Player player) {
		super(player);
		if (!this.bPlayer.canBend(this)) {
			return;
		}
		this.cooldown = getConfig().getLong("Abilities.Chi.AcrobatStance.Cooldown");
		this.duration = getConfig().getLong("Abilities.Chi.AcrobatStance.Duration");
		this.speed = getConfig().getInt("Abilities.Chi.AcrobatStance.Speed") - 1;
		this.jump = getConfig().getInt("Abilities.Chi.AcrobatStance.Jump") - 1;
		this.chiBlockBoost = getConfig().getDouble("Abilities.Chi.AcrobatStance.ChiBlockBoost");
		this.paralyzeDodgeBoost = getConfig().getDouble("Abilities.Chi.AcrobatStance.ParalyzeChanceDecrease");

		final ChiAbility stance = this.bPlayer.getStance();
		if (stance != null) {
			stance.remove();
			if (stance instanceof AcrobatStance) {
				this.bPlayer.setStance(null);
				return;
			}
		}
		this.start();
		this.bPlayer.setStance(this);
		player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_HURT, 0.5F, 2F);
	}

	@Override
	public void progress() {
		if (!this.bPlayer.canBendIgnoreBinds(this) || !this.bPlayer.hasElement(Element.CHI)) {
			this.remove();
			return;
		} else if (this.duration != 0 && System.currentTimeMillis() > this.getStartTime() + this.duration) {
			this.remove();
			return;
		}

		if (!this.player.hasPotionEffect(PotionEffectType.SPEED) || this.player.getPotionEffect(PotionEffectType.SPEED).getAmplifier() < this.speed || (this.player.getPotionEffect(PotionEffectType.SPEED).getAmplifier() == this.speed && this.player.getPotionEffect(PotionEffectType.SPEED).getDuration() == 1)) {
			this.player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10, this.speed, true, false), true);
		}
		if (!this.player.hasPotionEffect(PotionEffectType.JUMP) || this.player.getPotionEffect(PotionEffectType.JUMP).getAmplifier() < this.jump || (this.player.getPotionEffect(PotionEffectType.JUMP).getAmplifier() == this.jump && this.player.getPotionEffect(PotionEffectType.JUMP).getDuration() == 1)) {
			this.player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 10, this.jump, true, false), true);
		}
	}

	@Override
	public void remove() {
		super.remove();
		this.bPlayer.addCooldown(this);
		this.bPlayer.setStance(null);
		this.player.playSound(this.player.getLocation(), Sound.ENTITY_ENDER_DRAGON_SHOOT, 0.5F, 2F);
		this.player.removePotionEffect(PotionEffectType.SPEED);
		this.player.removePotionEffect(PotionEffectType.JUMP);
	}

	@Override
	public String getName() {
		return "AcrobatStance";
	}

	@Override
	public Location getLocation() {
		return this.player != null ? this.player.getLocation() : null;
	}

	@Override
	public long getCooldown() {
		return this.cooldown;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	public int getSpeed() {
		return this.speed;
	}

	public void setSpeed(final int speed) {
		this.speed = speed;
	}

	public int getJump() {
		return this.jump;
	}

	public void setJump(final int jump) {
		this.jump = jump;
	}

	public long getDuration() {
		return this.duration;
	}

	public void setDuration(final long duration) {
		this.duration = duration;
	}

	public double getChiBlockBoost() {
		return this.chiBlockBoost;
	}

	public void setChiBlockBoost(final double chiBlockBoost) {
		this.chiBlockBoost = chiBlockBoost;
	}

	public double getParalyzeDodgeBoost() {
		return this.paralyzeDodgeBoost;
	}

	public void setParalyzeDodgeBoost(final double paralyzeDodgeBoost) {
		this.paralyzeDodgeBoost = paralyzeDodgeBoost;
	}

}

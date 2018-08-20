package com.projectkorra.projectkorra.chiblocking;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.attribute.Attribute;

public class AcrobatStance extends ChiAbility {

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
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
		this.speed = getConfig().getInt("Abilities.Chi.AcrobatStance.Speed") + 1;
		this.jump = getConfig().getInt("Abilities.Chi.AcrobatStance.Jump") + 1;
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
		GeneralMethods.displayMovePreview(player);
		player.playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_HURT, 0.5F, 2F);
	}

	@Override
	public void progress() {
		if (!this.bPlayer.canBendIgnoreBinds(this) || !this.bPlayer.hasElement(Element.CHI)) {
			this.remove();
			return;
		}

		if (!this.player.hasPotionEffect(PotionEffectType.SPEED)) {
			this.player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, this.speed, true));
		}
		if (!this.player.hasPotionEffect(PotionEffectType.JUMP)) {
			this.player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 60, this.jump, true));
		}
	}

	@Override
	public void remove() {
		super.remove();
		this.bPlayer.addCooldown(this);
		this.bPlayer.setStance(null);
		GeneralMethods.displayMovePreview(this.player);
		this.player.playSound(this.player.getLocation(), Sound.ENTITY_ENDERDRAGON_SHOOT, 0.5F, 2F);
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

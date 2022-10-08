package com.projectkorra.projectkorra.chiblocking;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.attribute.Attribute;

public class WarriorStance extends ChiAbility {

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute("Strength")
	private int strength;
	@Attribute("Resistance")
	private int resistance;

	public WarriorStance(final Player player) {
		super(player);
		if (!this.bPlayer.canBend(this)) {
			return;
		}
		this.cooldown = getConfig().getLong("Abilities.Chi.WarriorStance.Cooldown");
		this.duration = getConfig().getLong("Abilities.Chi.WarriorStance.Duration");
		this.strength = getConfig().getInt("Abilities.Chi.WarriorStance.Strength") - 1;
		this.resistance = getConfig().getInt("Abilities.Chi.WarriorStance.Resistance"); //intended to be negative

		final ChiAbility stance = this.bPlayer.getStance();
		if (stance != null) {
			stance.remove();
			if (stance instanceof WarriorStance) {
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

		if (!this.player.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE) || this.player.getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE).getAmplifier() > this.resistance || (this.player.getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE).getAmplifier() == this.resistance && this.player.getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE).getDuration() == 1)) { //special case for negative resistance
			this.player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 10, this.resistance, true, false), true);
		}
		if (!this.player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE) || this.player.getPotionEffect(PotionEffectType.INCREASE_DAMAGE).getAmplifier() < this.strength || (this.player.getPotionEffect(PotionEffectType.INCREASE_DAMAGE).getAmplifier() == this.strength && this.player.getPotionEffect(PotionEffectType.INCREASE_DAMAGE).getDuration() == 1)) {
			this.player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 10, this.strength, true, false), true);
		}
	}

	@Override
	public void remove() {
		super.remove();
		this.bPlayer.addCooldown(this);
		this.bPlayer.setStance(null);
		if (this.player != null) {
			this.player.playSound(this.player.getLocation(), Sound.ENTITY_ENDER_DRAGON_SHOOT, 0.5F, 2F);
			this.player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
			this.player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
		}
	}

	@Override
	public String getName() {
		return "WarriorStance";
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

	public int getStrength() {
		return this.strength;
	}

	public void setStrength(final int strength) {
		this.strength = strength;
	}

	public int getResistance() {
		return this.resistance;
	}

	public void setResistance(final int resistance) {
		this.resistance = resistance;
	}

	public long getDuration() {
		return this.duration;
	}

	public void setDuration(final long duration) {
		this.duration = duration;
	}

}

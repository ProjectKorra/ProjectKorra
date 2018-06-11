package com.projectkorra.projectkorra.chiblocking;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.ChiAbility;

public class WarriorStance extends ChiAbility {

	private long cooldown;
	private int strength;
	private int resistance;

	public WarriorStance(Player player) {
		super(player);
		if (!bPlayer.canBend(this)) {
			return;
		}
		this.cooldown = getConfig().getLong("Abilities.Chi.WarriorStance.Cooldown");
		this.strength = getConfig().getInt("Abilities.Chi.WarriorStance.Strength") - 1;
		this.resistance = getConfig().getInt("Abilities.Chi.WarriorStance.Resistance");

		ChiAbility stance = bPlayer.getStance();
		if (stance != null) {
			stance.remove();
			if (stance instanceof WarriorStance) {
				bPlayer.setStance(null);
				return;
			}
		}
		start();
		bPlayer.setStance(this);
		GeneralMethods.displayMovePreview(player);
		player.playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_HURT, 0.5F, 2F);
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBinds(this) || !bPlayer.hasElement(Element.CHI)) {
			remove();
			return;
		}

		if (!player.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 60, resistance, true));
		}
		if (!player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 60, strength, true));
		}
	}

	@Override
	public void remove() {
		super.remove();
		bPlayer.addCooldown(this);
		bPlayer.setStance(null);
		if (player != null) {
			GeneralMethods.displayMovePreview(player);
			player.playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_SHOOT, 0.5F, 2F);
			player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
			player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
		}
	}

	@Override
	public String getName() {
		return "WarriorStance";
	}

	@Override
	public Location getLocation() {
		return player != null ? player.getLocation() : null;
	}

	@Override
	public long getCooldown() {
		return cooldown;
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
		return strength;
	}

	public void setStrength(int strength) {
		this.strength = strength;
	}

	public int getResistance() {
		return resistance;
	}

	public void setResistance(int resistance) {
		this.resistance = resistance;
	}

}

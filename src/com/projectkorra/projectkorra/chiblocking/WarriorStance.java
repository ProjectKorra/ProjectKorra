package com.projectkorra.projectkorra.chiblocking;

import com.projectkorra.projectkorra.ability.ChiAbility;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class WarriorStance extends ChiAbility {

	private int strength;
	private int resistance;
	
	public WarriorStance(Player player) {
		super(player);
		if (!bPlayer.canBend(this)) {
			return;
		}
		this.strength = getConfig().getInt("Abilities.Chi.WarriorStance.Strength");
		this.resistance = getConfig().getInt("Abilities.Chi.WarriorStance.Resistance");
		
		ChiAbility stance = bPlayer.getStance();
		if (stance != null) {
			stance.remove();
			bPlayer.setStance(null);
			return;
		} else {
			bPlayer.setStance(this);
			start();
		}
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			remove();
			return;
		}
		
		if (!player.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 2, resistance));
		}
		if (!player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 2, strength));
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
		return 0;
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

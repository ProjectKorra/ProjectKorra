package com.projectkorra.projectkorra.chiblocking;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.StockAbility;
import com.projectkorra.projectkorra.waterbending.Bloodbending;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ConcurrentHashMap;

public class WarriorStance {

	public int strength = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.WarriorStance.Strength") - 1;
	public int resistance = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.WarriorStance.Resistance");

	private Player player;
	public static ConcurrentHashMap<Player, WarriorStance> instances = new ConcurrentHashMap<Player, WarriorStance>();

	public WarriorStance(Player player) {
		this.player = player;
		if (instances.containsKey(player)) {
			instances.remove(player);
			return;
		}

		if (AcrobatStance.isInAcrobatStance(player)) {
			AcrobatStance.remove(player);
		}

		instances.put(player, this);
	}

	private void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		if (!GeneralMethods.canBend(player.getName(), StockAbility.WarriorStance.toString())) {
			remove();
			return;
		}

		if (Paralyze.isParalyzed(player) || Bloodbending.isBloodbended(player)) {
			remove();
			return;
		}
		if (!player.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 60, resistance));
		}

		if (!player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 60, strength));
		}
	}

	public static void progressAll() {
		for (Player player : instances.keySet()) {
			instances.get(player).progress();
		}
	}

	private void remove() {
		instances.remove(player);
	}

	public static boolean isInWarriorStance(Player player) {
		if (instances.containsKey(player))
			return true;
		return false;
	}

	public static void remove(Player player) {
		instances.remove(player);
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

	public Player getPlayer() {
		return player;
	}
}

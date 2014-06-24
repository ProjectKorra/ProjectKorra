package com.projectkorra.ProjectKorra.Ability;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.ProjectKorra.Flight;
import com.projectkorra.ProjectKorra.Methods;

public class AvatarState {

	public static ConcurrentHashMap<Player, AvatarState> instances = new ConcurrentHashMap<Player, AvatarState>();

	private static final double factor = 5;

	Player player;

	// boolean canfly = false;

	public AvatarState(Player player) {
		this.player = player;
		if (instances.containsKey(player)) {
			instances.remove(player);
		} else {
			new Flight(player);
			instances.put(player, this);
		}
	}

	public static void manageAvatarStates() {
		for (Player player : instances.keySet()) {
			progress(player);
		}
	}

	public static boolean progress(Player player) {
		return instances.get(player).progress();
	}

	private boolean progress() {
		if (!Methods.canBend(player.getName(), StockAbilities.AvatarState.name())) {
			instances.remove(player);
			return false;
		}
		addPotionEffects();
		return true;
	}

	private void addPotionEffects() {
		int duration = 70;
		player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,
				duration, 3));
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,
				duration, 2));
		player.addPotionEffect(new PotionEffect(
				PotionEffectType.DAMAGE_RESISTANCE, duration, 2));
		player.addPotionEffect(new PotionEffect(
				PotionEffectType.FIRE_RESISTANCE, duration, 2));
	}

	public static boolean isAvatarState(Player player) {
		if (instances.containsKey(player))
			return true;
		return false;
	}

	public static double getValue(double value) {
		return factor * value;
	}

	public static int getValue(int value) {
		return (int) factor * value;
	}

	public static ArrayList<Player> getPlayers() {
		ArrayList<Player> players = new ArrayList<Player>();
		for (Player player : instances.keySet()) {
			players.add(player);
		}
		return players;
	}
}
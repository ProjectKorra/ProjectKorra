package com.projectkorra.ProjectKorra.Ability;

import com.projectkorra.ProjectKorra.Flight;
import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AvatarState {

	public static ConcurrentHashMap<Player, AvatarState> instances = new ConcurrentHashMap<>();
	//public static Map<String, Long> cooldowns = new HashMap<String, Long>();
	public static Map<String, Long> startTimes = new HashMap<>();

	public static FileConfiguration config = ProjectKorra.plugin.getConfig();
	private static final long cooldown = ProjectKorra.plugin.getConfig().getLong("Abilities.AvatarState.Cooldown");
	private static boolean regenEnabled = config.getBoolean("Abilities.AvatarState.PotionEffects.Regeneration.Enabled");
	private static int regenPower = config.getInt("Abilities.AvatarState.PotionEffects.Regeneration.Power") - 1;
	private static boolean speedEnabled = config.getBoolean("Abilities.AvatarState.PotionEffects.Speed.Enabled");
	private static int speedPower = config.getInt("Abilities.AvatarState.PotionEffects.Speed.Power") - 1;
	private static boolean resistanceEnabled = config.getBoolean("Abilities.AvatarState.PotionEffects.DamageResistance.Enabled");
	private static int resistancePower = config.getInt("Abilities.AvatarState.PotionEffects.DamageResistance.Power") - 1;
	private static boolean fireResistanceEnabled = config.getBoolean("Abilities.AvatarState.PotionEffects.FireResistance.Enabled");
	private static int fireResistancePower = config.getInt("Abilities.AvatarState.PotionEffects.FireResistance.Power") - 1;
	private static long duration = config.getLong("Abilities.AvatarState.Duration");

	public static final double factor = config.getDouble("Abilities.AvatarState.PowerMultiplier");

	Player player;

	// boolean canfly = false;

	public AvatarState(Player player) {
		this.player = player;
		if (instances.containsKey(player)) {
			instances.remove(player);
			return;
		}
		//if (cooldowns.containsKey(player.getName())) {
			//if (cooldowns.get(player.getName()) + cooldown >= System.currentTimeMillis()) {
				//return;
			//} else {
				//cooldowns.remove(player.getName());
			//}
		//}
		if(GeneralMethods.getBendingPlayer(player.getName()).isOnCooldown("AvatarState")) {
			return;
		}
		new Flight(player);
		GeneralMethods.playAvatarSound(player.getLocation());
		instances.put(player, this);
		GeneralMethods.getBendingPlayer(player.getName()).addCooldown("AvatarState", cooldown);
		if (duration != 0) {
			startTimes.put(player.getName(), System.currentTimeMillis());
		}
	}

	public static void manageAvatarStates() {
		instances.keySet().forEach(AvatarState::progress);
	}

	public static boolean progress(Player player) {
		return instances.get(player).progress();
	}

	private boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			instances.remove(player);
		}
		if (!GeneralMethods.canBend(player.getName(), StockAbilities.AvatarState.name())) {
			instances.remove(player);
			if(player != null){
				if(GeneralMethods.getBendingPlayer(player.getName()).isOnCooldown("AvatarState")) {
					GeneralMethods.getBendingPlayer(player.getName()).removeCooldown("AvatarState");
				}
			return false;
			}
		}
		
		if (startTimes.containsKey(player.getName())) {
			if (startTimes.get(player.getName()) + duration < System.currentTimeMillis()) {
				startTimes.remove(player.getName());
				instances.remove(player);
			}
		}
		
		addPotionEffects();
		return true;
	}

	private void addPotionEffects() {
		int duration = 70;
		if (regenEnabled) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,
					duration, regenPower));
		}
		if (speedEnabled) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,
					duration, speedPower));
		}
		if (resistanceEnabled) {
			player.addPotionEffect(new PotionEffect(
					PotionEffectType.DAMAGE_RESISTANCE, duration, resistancePower));
		}
		if (fireResistanceEnabled) {
			player.addPotionEffect(new PotionEffect(
					PotionEffectType.FIRE_RESISTANCE, duration, fireResistancePower));
		}
	}

	public static boolean isAvatarState(Player player) {
		return instances.containsKey(player);
	}

	public static double getValue(double value) {
		return factor * value;
	}

	public static int getValue(int value) {
		return (int) factor * value;
	}

	public static List<Player> getPlayers() {
		return instances.keySet().stream().collect(Collectors.toList());
	}
}
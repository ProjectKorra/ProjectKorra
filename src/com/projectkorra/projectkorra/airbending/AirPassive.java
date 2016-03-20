package com.projectkorra.projectkorra.airbending;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.chiblocking.ChiPassive;
import com.projectkorra.projectkorra.configuration.ConfigManager;

public class AirPassive {

	private static final ConcurrentHashMap<Player, Float> FOOD = new ConcurrentHashMap<>();

	public static float getExhaustion(Player player, float level) {
		if (!FOOD.keySet().contains(player)) {
			FOOD.put(player, level);
			return level;
		} else {
			float oldlevel = FOOD.get(player);
			if (level < oldlevel) {
				level = 0;
			} else {
				double factor = getFactor();
				level = (float) ((level - oldlevel) * factor + oldlevel);
			}
			FOOD.replace(player, level);
			return level;
		}
	}

	public static void handlePassive() {
		int speedPower = 0;
		int jumpPower = 0;

		for (Player player : Bukkit.getOnlinePlayers()) {
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			if (bPlayer == null) {
				continue;
			}

			if (bPlayer.canBendPassive(Element.AIR)) {
				if (bPlayer.canBendPassive(Element.CHI)) {
					if (ChiPassive.getJumpPower() > getJumpPower()) {
						jumpPower = ChiPassive.getJumpPower();
					} else {
						jumpPower = getJumpPower();
					}

					if (ChiPassive.getSpeedPower() > getSpeedPower()) {
						speedPower = ChiPassive.getSpeedPower();
					} else {
						speedPower = getSpeedPower();
					}
				}
				player.setExhaustion(getExhaustion(player, player.getExhaustion()));
				
				if (player.isSprinting()) {
					if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
						player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, speedPower));
					}
					if (!player.hasPotionEffect(PotionEffectType.JUMP)) {
						player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 60, jumpPower));
					}
				}
			}
		}
	}

	public static double getFactor() {
		return ConfigManager.getConfig().getDouble("Abilities.Air.Passive.Factor");
	}

	public static int getJumpPower() {
		return ConfigManager.getConfig().getInt("Abilities.Air.Passive.Jump");
	}

	public static int getSpeedPower() {
		return ConfigManager.getConfig().getInt("Abilities.Air.Passive.Speed");
	}
}

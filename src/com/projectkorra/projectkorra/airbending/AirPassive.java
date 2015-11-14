package com.projectkorra.projectkorra.airbending;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.configuration.ConfigLoadable;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ConcurrentHashMap;

public class AirPassive implements ConfigLoadable {

	private static ConcurrentHashMap<Player, Float> food = new ConcurrentHashMap<Player, Float>();
	private static float factor = (float) config.get().getDouble("Abilities.Air.Passive.Factor");

	private static int speedPower = config.get().getInt("Abilities.Air.Passive.Speed");
	private static int jumpPower = config.get().getInt("Abilities.Air.Passive.Jump");

	public static float getExhaustion(Player player, float level) {
		if (!food.keySet().contains(player)) {
			food.put(player, level);
			return level;
		} else {
			float oldlevel = food.get(player);
			if (level < oldlevel) {
				level = 0;
			} else {
				factor = (float) config.get().getDouble("Abilities.Air.Passive.Factor");
				level = (level - oldlevel) * factor + oldlevel;
			}
			food.replace(player, level);
			return level;
		}
	}

	public static void handlePassive(Server server) {
		for (World world : server.getWorlds()) {
			for (Player player : world.getPlayers()) {
				if (!player.isOnline())
					return;
				if (GeneralMethods.canBendPassive(player.getName(), Element.Air)) {
					player.setExhaustion(getExhaustion(player, player.getExhaustion())); // Handles
																							// Food
																							// Passive
					if (player.isSprinting()) {
						if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
							speedPower = config.get().getInt("Abilities.Air.Passive.Speed");
							player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, speedPower - 1)); // Handles
																													// Speed
																													// Passive
						}
						if (!player.hasPotionEffect(PotionEffectType.JUMP)) {
							jumpPower = config.get().getInt("Abilities.Air.Passive.Jump");
							player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 60, jumpPower - 1)); // Handles
																												// jump
																												// passive.
						}
					}
				}
			}
		}
	}

	@Override
	public void reloadVariables() {
		factor = (float) config.get().getDouble("Abilities.Air.Passive.Factor");

		speedPower = config.get().getInt("Abilities.Air.Passive.Speed");
		jumpPower = config.get().getInt("Abilities.Air.Passive.Jump");
	}

}

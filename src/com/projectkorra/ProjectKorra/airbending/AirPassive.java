package com.projectkorra.ProjectKorra.airbending;

import com.projectkorra.ProjectKorra.Element;
import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ConcurrentHashMap;

public class AirPassive {

	private static ConcurrentHashMap<Player, Float> food = new ConcurrentHashMap<>();
	private static float factor = (float) ProjectKorra.plugin.getConfig().getDouble("Abilities.Air.Passive.Factor");
	
	private static int speedPower = ProjectKorra.plugin.getConfig().getInt("Abilities.Air.Passive.Speed");
	private static int jumpPower = ProjectKorra.plugin.getConfig().getInt("Abilities.Air.Passive.Jump");
	
	public static float getExhaustion(Player player, float level) {
		if (!food.keySet().contains(player)) {
			food.put(player, level);
			return level;
		} else {
			float oldlevel = food.get(player);
			if (level < oldlevel) {
				level = 0;
			} else {
				level = (level - oldlevel) * factor + oldlevel;
			}
			food.replace(player, level);
			return level;
		}
	}
	
	public static void handlePassive(Server server) {
		for (World world: server.getWorlds()) {
			for (Player player: world.getPlayers()) {
				if (!player.isOnline()) return;
				if (GeneralMethods.canBendPassive(player.getName(), Element.Air)) {
					player.setExhaustion(getExhaustion(player, player.getExhaustion())); // Handles Food Passive
					if (player.isSprinting()) {
						if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
							player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, speedPower - 1)); // Handles Speed Passive
						}
						if (!player.hasPotionEffect(PotionEffectType.JUMP)) {
							player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 60, jumpPower - 1)); // Handles jump passive.
						}
					}
				}
			}
		}
	}

}

package com.projectkorra.projectkorra.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.PassiveManager;
import com.projectkorra.projectkorra.airbending.passive.AirSaturation;
import com.projectkorra.projectkorra.chiblocking.passive.ChiSaturation;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.configuration.ConfigManager;

public class PassiveHandler {
	private static final Map<Player, Float> FOOD = new ConcurrentHashMap<>();

	public static float getExhaustion(Player player, float level, double factor) {
		if (!FOOD.keySet().contains(player)) {
			FOOD.put(player, level);
			return level;
		} else {
			float oldlevel = FOOD.get(player);
			if (level < oldlevel) {
				level = 0;
			} else {
				level = (float) ((level - oldlevel) * factor + oldlevel);
			}
			
			FOOD.put(player, level);
			return level;
		}
	}

	public static void checkExhaustionPassives(Player player) {
		if (!CoreAbility.getAbility(AirSaturation.class).isEnabled() && !CoreAbility.getAbility(ChiSaturation.class).isEnabled()) {
			return;
		}
		
		double air = AirSaturation.getExhaustionFactor();
		double chi = ChiSaturation.getExhaustionFactor();

		if (ConfigManager.defaultConfig.get().getStringList("Properties.DisabledWorlds").contains(player.getWorld().getName())) {
			return;
		}

		if (Commands.isToggledForAll && ConfigManager.defaultConfig.get().getBoolean("Properties.TogglePassivesWithAllBending")) {
			return;
		}

		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer == null) {
			return;
		}

		if (!PassiveManager.hasPassive(player, CoreAbility.getAbility(AirSaturation.class))) {
			air = 0;
		}
		
		if (!PassiveManager.hasPassive(player, CoreAbility.getAbility(ChiSaturation.class))) {
			chi = 0;
		}

		double max = Math.max(air, chi);
		if (max == 0) {
			return;
		} else {
			player.setExhaustion(getExhaustion(player, player.getExhaustion(), max));
		}
	}
}

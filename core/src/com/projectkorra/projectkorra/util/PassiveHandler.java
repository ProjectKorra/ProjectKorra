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

	public static float getExhaustion(final Player player, float level, final double factor) {
		final Float oldLevel = FOOD.get(player);
        if (oldLevel != null) {
			level = level < oldLevel ? 0 : (float) ((level - oldLevel) * factor + oldLevel);
        }
        FOOD.put(player, level);
        return level;
    }

	public static void checkExhaustionPassives(final Player player) {
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null || !bPlayer.canBendInWorld()) {
			return;
		} else if (Commands.isToggledForAll && ConfigManager.defaultConfig.get().getBoolean("Properties.TogglePassivesWithAllBending")) {
			return;
		}

		CoreAbility airPassive = CoreAbility.getAbility(AirSaturation.class);
		CoreAbility chiPassive = CoreAbility.getAbility(ChiSaturation.class);
		if ((airPassive == null || !airPassive.isEnabled()) && (chiPassive == null || !chiPassive.isEnabled())) {
			return;
		}

		double air = PassiveManager.hasPassive(player, airPassive) ? AirSaturation.getExhaustionFactor() : 0;
		double chi = PassiveManager.hasPassive(player, chiPassive) ? ChiSaturation.getExhaustionFactor() : 0;
		final double factor = Math.max(air, chi); // Should this not be Math.min instead? That way you get the best exhaustion decrease based on your element
		if (factor != 0) {
			player.setExhaustion(getExhaustion(player, player.getExhaustion(), factor));
        }
	}
}

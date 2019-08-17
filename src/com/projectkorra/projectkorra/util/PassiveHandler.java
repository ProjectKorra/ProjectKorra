package com.projectkorra.projectkorra.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.PassiveManager;
import com.projectkorra.projectkorra.airbending.passive.AirSaturation;
import com.projectkorra.projectkorra.chiblocking.passive.ChiSaturation;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.properties.GeneralPropertiesConfig;

public class PassiveHandler {
	private static final Map<Player, Float> FOOD = new ConcurrentHashMap<>();

	public static float getExhaustion(final Player player, float level, final double factor) {
		if (!FOOD.keySet().contains(player)) {
			FOOD.put(player, level);
			return level;
		} else {
			final float oldlevel = FOOD.get(player);
			if (level < oldlevel) {
				level = 0;
			} else {
				level = (float) ((level - oldlevel) * factor + oldlevel);
			}

			FOOD.put(player, level);
			return level;
		}
	}

	public static void checkExhaustionPassives(final Player player) {
		CoreAbility airsat = CoreAbility.getAbility(AirSaturation.class);
		CoreAbility chisat = CoreAbility.getAbility(ChiSaturation.class);
		
		if ((airsat == null || !airsat.isEnabled()) && (chisat == null || !chisat.isEnabled())) {
			return;
		}

		double air = AirSaturation.getExhaustionFactor();
		double chi = ChiSaturation.getExhaustionFactor();

		if (Stream.of(ConfigManager.getConfig(GeneralPropertiesConfig.class).DisabledWorlds).anyMatch(player.getWorld().getName()::equalsIgnoreCase)) {
			return;
		}

		if (Commands.isToggledForAll && ConfigManager.getConfig(GeneralPropertiesConfig.class).TogglePassivesWithAllBending) {
			return;
		}

		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer == null) {
			return;
		}

		if (!PassiveManager.hasPassive(player, airsat)) {
			air = 0;
		}

		if (!PassiveManager.hasPassive(player, chisat)) {
			chi = 0;
		}

		final double max = Math.max(air, chi);
		if (max == 0) {
			return;
		} else {
			player.setExhaustion(getExhaustion(player, player.getExhaustion(), max));
		}
	}
}

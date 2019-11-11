package com.projectkorra.projectkorra.util;

import com.projectkorra.projectkorra.ability.AbilityHandler;
import com.projectkorra.projectkorra.ability.PassiveAbilityManager;
import com.projectkorra.projectkorra.airbending.passive.AirSaturation;
import com.projectkorra.projectkorra.chiblocking.passive.ChiSaturation;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.properties.GeneralPropertiesConfig;
import com.projectkorra.projectkorra.module.Module;
import com.projectkorra.projectkorra.module.ModuleManager;
import com.projectkorra.projectkorra.player.BendingPlayerManager;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class PassiveHandler extends Module {

	private final BendingPlayerManager bendingPlayerManager;
	private final PassiveAbilityManager passiveAbilityManager;

	private final Map<UUID, Float> food = new HashMap<>();

	private PassiveHandler() {
		super("Passive");

		this.bendingPlayerManager = ModuleManager.getModule(BendingPlayerManager.class);
		this.passiveAbilityManager = ModuleManager.getModule(PassiveAbilityManager.class);
	}

	public float getExhaustion(final Player player, float level, final double factor) {
		if (!this.food.keySet().contains(player.getUniqueId())) {
			this.food.put(player.getUniqueId(), level);
			return level;
		} else {
			final float oldlevel = this.food.get(player.getUniqueId());
			if (level < oldlevel) {
				level = 0;
			} else {
				level = (float) ((level - oldlevel) * factor + oldlevel);
			}

			this.food.put(player.getUniqueId(), level);
			return level;
		}
	}

	public void checkExhaustionPassives(final Player player) {
		AbilityHandler airsat = this.passiveAbilityManager.getHandler(AirSaturation.AirSaturationHandler.class);
		AbilityHandler chisat = this.passiveAbilityManager.getHandler(ChiSaturation.ChiSaturationHandler.class);

		if (airsat == null && chisat == null) {
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

		if (!this.passiveAbilityManager.canUsePassive(player, airsat)) {
			air = 0;
		}

		if (!this.passiveAbilityManager.canUsePassive(player, chisat)) {
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

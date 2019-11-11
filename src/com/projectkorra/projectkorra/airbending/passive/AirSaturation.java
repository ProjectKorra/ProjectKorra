package com.projectkorra.projectkorra.airbending.passive;

import com.projectkorra.projectkorra.ability.AbilityHandler;
import com.projectkorra.projectkorra.ability.api.PassiveAbility;
import com.projectkorra.projectkorra.ability.legacy.AirAbility;
import com.projectkorra.projectkorra.airbending.util.AirPassiveAbilityInfo;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.abilities.air.AirSaturationConfig;
import com.projectkorra.projectkorra.element.Element;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class AirSaturation extends AirAbility<AirSaturationConfig> {
	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public String getName() {
		return null;
	}

	public AirSaturation(final Player player) {
		super(player);
	}

	public static double getExhaustionFactor() {
		return ConfigManager.getConfig(AirSaturationConfig.class).ExhaustionFactor;
	}

	@Override
	public void progress() {}

	@Override
	public Location getLocation() {
		return this.player.getLocation();
	}

	public static class AirSaturationHandler extends AbilityHandler<AirSaturation, AirSaturationConfig> implements PassiveAbility {

		public AirSaturationHandler(Class<AirSaturation> abilityClass, Class<AirSaturationConfig> configClass) {
			super(abilityClass, configClass);
		}

		@Override
		public String getName() {
			return "AirSaturation";
		}

		@Override
		public boolean isSneakAbility() {
			return false;
		}

		@Override
		public boolean isHarmlessAbility() {
			return false;
		}

		@Override
		public boolean isIgniteAbility() {
			return false;
		}

		@Override
		public boolean isExplosiveAbility() {
			return false;
		}

		@Override
		public long getCooldown() {
			return 0;
		}

		@Override
		public Element getElement() {
			return null;
		}

		@Override
		public String getDescription() {
			return null;
		}

		@Override
		public String getInstructions() {
			return null;
		}

		@Override
		public boolean isInstantiable() {
			return false;
		}

		@Override
		public boolean isProgressable() {
			return false;
		}
	}
}

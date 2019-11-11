package com.projectkorra.projectkorra.chiblocking.passive;

import com.projectkorra.projectkorra.ability.AbilityHandler;
import com.projectkorra.projectkorra.element.Element;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.legacy.ChiAbility;
import com.projectkorra.projectkorra.ability.api.PassiveAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.abilities.chi.ChiSaturationConfig;

public class ChiSaturation extends ChiAbility<ChiSaturation.ChiSaturationHandler> implements PassiveAbility {
	private ChiSaturation(final ChiSaturationHandler abilityHandler, final Player player) {
		super(abilityHandler, player);
	}

	public static double getExhaustionFactor() {
		return ConfigManager.getConfig(ChiSaturationConfig.class).ExhaustionFactor;
	}

	@Override
	public void progress() {}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public String getName() {
		return "ChiSaturation";
	}

	@Override
	public Location getLocation() {
		return this.player.getLocation();
	}

	@Override
	public boolean isInstantiable() {
		return false;
	}

	@Override
	public boolean isProgressable() {
		return false;
	}

	public static class ChiSaturationHandler extends AbilityHandler<ChiSaturation, ChiSaturationConfig> implements PassiveAbility {

		public ChiSaturationHandler(Class<ChiSaturation> abilityClass, Class<ChiSaturationConfig> configClass) {
			super(abilityClass, configClass);
		}

		@Override
		public String getName() {
			return "ChiSaturation";
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

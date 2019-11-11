package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;
import com.projectkorra.projectkorra.element.Element;
import com.projectkorra.projectkorra.player.BendingPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class AbilityHandler<T extends Ability, U extends AbilityConfig> implements Listener {

	private final Class<T> abilityClass;
	private final Class<U> configClass;

	public AbilityHandler(Class<T> abilityClass, Class<U> configClass) {
		this.abilityClass = abilityClass;
		this.configClass = configClass;
	}

	public T newInstance(Player player) {
		try {
			Constructor<T> constructor = abilityClass.getDeclaredConstructor(Player.class);

			return constructor.newInstance(player);
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new AbilityException(e);
		}
	}

	public Class<T> getAbility() {
		return this.abilityClass;
	}

	public U getConfig() {
		return ConfigManager.getConfig(this.configClass);
	}

	/**
	 * The name of the ability is used for commands such as <b>/bending
	 * display</b> and <b>/bending help</b>. The name is also used for
	 * determining the tag for cooldowns
	 * {@link BendingPlayer#addCooldown(Ability)}, therefore if two abilities
	 * have the same name they will also share cooldowns. If two classes share
	 * the same name (SurgeWall/SurgeWave) but need to have independent
	 * cooldowns, then {@link BendingPlayer#addCooldown(String, long)} should be
	 * called explicitly.
	 *
	 * @return Returns the name of the ability
	 */
	public abstract String getName();

	public abstract boolean isSneakAbility();

	public abstract boolean isHarmlessAbility();

	public abstract boolean isIgniteAbility();

	public abstract boolean isExplosiveAbility();

	public abstract long getCooldown();

	public abstract Element getElement();

	public abstract String getDescription();

	public abstract String getInstructions();

	/**
	 * @return true if this is a hidden ability.
	 */
	public boolean isHidden() {
		return false;
	}
}

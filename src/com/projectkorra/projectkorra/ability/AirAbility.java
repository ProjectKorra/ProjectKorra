package com.projectkorra.projectkorra.ability;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.airbending.AirSpout;
import com.projectkorra.projectkorra.airbending.Suffocate;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;
import com.projectkorra.projectkorra.configuration.configs.properties.AirPropertiesConfig;
import com.projectkorra.projectkorra.util.ParticleEffect;

public abstract class AirAbility<C extends AbilityConfig> extends ElementalAbility<C> {
	
	public AirAbility(final C config, final Player player) {
		super(config, player);
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
	public Element getElement() {
		return Element.AIR;
	}

	@Override
	public void handleCollision(final Collision collision) {
		super.handleCollision(collision);
		if (collision.isRemovingFirst()) {
			ParticleEffect.BLOCK_CRACK.display(collision.getLocationFirst(), 10, 1, 1, 1, 0.1, Material.WHITE_WOOL.createBlockData());
		}
	}

	/**
	 * Breaks a breathbendng hold on an entity or one a player is inflicting on
	 * an entity.
	 *
	 * @param entity The entity to be acted upon
	 */
	public static void breakBreathbendingHold(final Entity entity) {
		if (Suffocate.isBreathbent(entity)) {
			Suffocate.breakSuffocate(entity);
			return;
		}

		if (entity instanceof Player) {
			final Player player = (Player) entity;
			if (Suffocate.isChannelingSphere(player)) {
				Suffocate.remove(player);
			}
		}
	}

	/**
	 * Gets the Air Particles from the config.
	 *
	 * @return Config specified ParticleEffect
	 */
	public static ParticleEffect getAirbendingParticles() {
		return ConfigManager.getConfig(AirPropertiesConfig.class).Particles;
	}

	/**
	 * This method was used for the old collision detection system. Please see
	 * {@link Collision} for the new system.
	 * <p>
	 * Checks whether a location is within an AirShield.
	 *
	 * @param loc The location to check
	 * @return true If the location is inside an AirShield.
	 */
	@Deprecated
	public static boolean isWithinAirShield(final Location loc) {
		final List<String> list = new ArrayList<String>();
		list.add("AirShield");
		return GeneralMethods.blockAbilities(null, list, loc, 0);
	}

	/**
	 * Plays an integer amount of air particles in a location.
	 *
	 * @param loc The location to use
	 * @param amount The amount of particles
	 */
	public static void playAirbendingParticles(final Location loc, final int amount) {
		playAirbendingParticles(loc, amount, Math.random(), Math.random(), Math.random());
	}

	/**
	 * Plays an integer amount of air particles in a location with a given
	 * xOffset, yOffset, and zOffset.
	 *
	 * @param loc The location to use
	 * @param amount The amount of particles
	 * @param xOffset The xOffset to use
	 * @param yOffset The yOffset to use
	 * @param zOffset The zOffset to use
	 */
	public static void playAirbendingParticles(final Location loc, final int amount, final double xOffset, final double yOffset, final double zOffset) {
		getAirbendingParticles().display(loc, amount, xOffset, yOffset, zOffset);
	}

	/**
	 * Plays the Airbending Sound at a location if enabled in the config.
	 *
	 * @param loc The location to play the sound at
	 */
	public static void playAirbendingSound(final Location loc) {
		AirPropertiesConfig air = ConfigManager.getConfig(AirPropertiesConfig.class);
		
		if (air.PlaySound) {
			loc.getWorld().playSound(loc, air.SoundType, air.SoundVolume, air.SoundPitch);
		}
	}

	/**
	 * This method was used for the old collision detection system. Please see
	 * {@link Collision} for the new system.
	 * <p>
	 * Removes all air spouts in a location within a certain radius.
	 *
	 * @param loc The location to use
	 * @param radius The radius around the location to remove spouts in
	 * @param source The player causing the removal
	 */
	@Deprecated
	public static void removeAirSpouts(final Location loc, final double radius, final Player source) {
		AirSpout.removeSpouts(loc, radius, source);
	}

	/**
	 * This method was used for the old collision detection system. Please see
	 * {@link Collision} for the new system.
	 * <p>
	 * Removes all air spouts in a location with a radius of 1.5.
	 *
	 * @param loc The location to use
	 * @param source The player causing the removal
	 */
	@Deprecated
	public static void removeAirSpouts(final Location loc, final Player source) {
		removeAirSpouts(loc, 1.5, source);
	}

}

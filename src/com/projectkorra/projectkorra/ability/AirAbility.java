package com.projectkorra.projectkorra.ability;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.airbending.AirSpout;
import com.projectkorra.projectkorra.airbending.Suffocate;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.ParticleEffect.ParticleData;

public abstract class AirAbility extends ElementalAbility {

	public AirAbility(final Player player) {
		super(player);
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
			final ParticleData particleData = new ParticleEffect.BlockData(Material.WOOL, (byte) 0);
			ParticleEffect.BLOCK_CRACK.display(particleData, 1F, 1F, 1F, 0.1F, 10, collision.getLocationFirst(), 50);
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
		final String particle = getConfig().getString("Properties.Air.Particles");
		if (particle == null) {
			return ParticleEffect.CLOUD;
		} else if (particle.equalsIgnoreCase("spell")) {
			return ParticleEffect.SPELL;
		} else if (particle.equalsIgnoreCase("blacksmoke")) {
			return ParticleEffect.SMOKE;
		} else if (particle.equalsIgnoreCase("smoke")) {
			return ParticleEffect.CLOUD;
		} else if (particle.equalsIgnoreCase("smallsmoke")) {
			return ParticleEffect.SNOW_SHOVEL;
		} else {
			return ParticleEffect.CLOUD;
		}
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
		playAirbendingParticles(loc, amount, (float) Math.random(), (float) Math.random(), (float) Math.random());
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
	public static void playAirbendingParticles(final Location loc, final int amount, final float xOffset, final float yOffset, final float zOffset) {
		getAirbendingParticles().display(loc, xOffset, yOffset, zOffset, 0, amount);
	}

	/**
	 * Plays the Airbending Sound at a location if enabled in the config.
	 *
	 * @param loc The location to play the sound at
	 */
	public static void playAirbendingSound(final Location loc) {
		if (getConfig().getBoolean("Properties.Air.PlaySound")) {
			final float volume = (float) getConfig().getDouble("Properties.Air.Sound.Volume");
			final float pitch = (float) getConfig().getDouble("Properties.Air.Sound.Pitch");

			Sound sound = Sound.ENTITY_CREEPER_HURT;

			try {
				sound = Sound.valueOf(getConfig().getString("Properties.Air.Sound.Sound"));
			}
			catch (final IllegalArgumentException exception) {
				ProjectKorra.log.warning("Your current value for 'Properties.Air.Sound.Sound' is not valid.");
			}
			finally {
				loc.getWorld().playSound(loc, sound, volume, pitch);
			}
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

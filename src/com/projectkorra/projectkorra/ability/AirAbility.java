package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.airbending.AirSpout;
import com.projectkorra.projectkorra.airbending.Suffocate;
import com.projectkorra.projectkorra.util.ParticleEffect;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class AirAbility extends ElementalAbility {
	
	public AirAbility(Player player) {
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

	/**
	 * Breaks a breathbendng hold on an entity or one a player is inflicting on an entity.
	 * 
	 * @param entity The entity to be acted upon
	 */
	public static void breakBreathbendingHold(Entity entity) {
		if (Suffocate.isBreathbent(entity)) {
			Suffocate.breakSuffocate(entity);
			return;
		}

		if (entity instanceof Player) {
			Player player = (Player) entity;
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
		String particle = getConfig().getString("Properties.Air.Particles");
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
	 * Checks whether a location is within an AirShield.
	 * 
	 * @param loc The location to check
	 * @return true If the location is inside an AirShield.
	 */
	public static boolean isWithinAirShield(Location loc) {
		List<String> list = new ArrayList<String>();
		list.add("AirShield");
		return GeneralMethods.blockAbilities(null, list, loc, 0);
	}

	/**
	 * Plays an integer amount of air particles in a location.
	 * 
	 * @param loc The location to use
	 * @param amount The amount of particles
	 */
	public static void playAirbendingParticles(Location loc, int amount) {
		playAirbendingParticles(loc, amount, (float) Math.random(), (float) Math.random(), (float) Math.random());
	}

	/**
	 * Plays an integer amount of air particles in a location with a given xOffset, yOffset, and
	 * zOffset.
	 * 
	 * @param loc The location to use
	 * @param amount The amount of particles
	 * @param xOffset The xOffset to use
	 * @param yOffset The yOffset to use
	 * @param zOffset The zOffset to use
	 */
	public static void playAirbendingParticles(Location loc, int amount, float xOffset, float yOffset, float zOffset) {
		getAirbendingParticles().display(loc, xOffset, yOffset, zOffset, 0, amount);
	}

	/**
	 * Plays the Airbending Sound at a location if enabled in the config.
	 * 
	 * @param loc The location to play the sound at
	 */
	public static void playAirbendingSound(Location loc) {
		if (getConfig().getBoolean("Properties.Air.PlaySound")) {
			loc.getWorld().playSound(loc, Sound.CREEPER_HISS, 1, 5);
		}
	}

	/**
	 * Removes all air spouts in a location within a certain radius.
	 * 
	 * @param loc The location to use
	 * @param radius The radius around the location to remove spouts in
	 * @param source The player causing the removal
	 */
	public static void removeAirSpouts(Location loc, double radius, Player source) {
		AirSpout.removeSpouts(loc, radius, source);
	}

	/**
	 * Removes all air spouts in a location with a radius of 1.5.
	 * 
	 * @param loc The location to use
	 * @param source The player causing the removal
	 */
	public static void removeAirSpouts(Location loc, Player source) {
		removeAirSpouts(loc, 1.5, source);
	}

}

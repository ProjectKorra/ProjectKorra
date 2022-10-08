package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;

public abstract class LightningAbility extends FireAbility implements SubAbility {

	public LightningAbility(final Player player) {
		super(player);
	}

	public static void playLightningbendingHitSound(final Location loc) {
		if (getConfig().getBoolean("Properties.Fire.PlaySound")) {
			final float volume = (float) getConfig().getDouble("Properties.Fire.LightningHit.Volume");
			final float pitch = (float) getConfig().getDouble("Properties.Fire.LightningHit.Pitch");

			Sound sound = Sound.ENTITY_LIGHTNING_BOLT_THUNDER;
			try {
				sound = Sound.valueOf(getConfig().getString("Properties.Fire.LightningHit.Sound"));
			} catch (final IllegalArgumentException exception) {
				ProjectKorra.log.warning("Your current value for 'Properties.Fire.LightningHit.Sound' is not valid.");
			} finally {
				loc.getWorld().playSound(loc, sound, volume, pitch);
			}
		}
	}

	public static void playLightningbendingChargingSound(final Location loc) {
		if (getConfig().getBoolean("Properties.Fire.PlaySound")) {
			final float volume = (float) getConfig().getDouble("Properties.Fire.LightningCharge.Volume");
			final float pitch = (float) getConfig().getDouble("Properties.Fire.LightningCharge.Pitch");

			Sound sound = Sound.BLOCK_BEEHIVE_WORK;
			try {
				sound = Sound.valueOf(getConfig().getString("Properties.Fire.LightningCharge.Sound"));
			} catch (final IllegalArgumentException exception) {
				ProjectKorra.log.warning("Your current value for 'Properties.Fire.LightningCharge.Sound' is not valid.");
			} finally {
				loc.getWorld().playSound(loc, sound, volume, pitch);
			}
		}
	}

	public static void playLightningbendingSound(final Location loc) {
		if (getConfig().getBoolean("Properties.Fire.PlaySound")) {
			final float volume = (float) getConfig().getDouble("Properties.Fire.LightningSound.Volume");
			final float pitch = (float) getConfig().getDouble("Properties.Fire.LightningSound.Pitch");

			Sound sound = Sound.ENTITY_CREEPER_HURT;
			try {
				sound = Sound.valueOf(getConfig().getString("Properties.Fire.LightningSound.Sound"));
			} catch (final IllegalArgumentException exception) {
				ProjectKorra.log.warning("Your current value for 'Properties.Fire.LightningSound.Sound' is not valid.");
			} finally {
				loc.getWorld().playSound(loc, sound, volume, pitch);
			}
		}
	}

	/**
	 * Plays an integer amount of lightning particles in a location with a given
	 * xOffset, yOffset, and zOffset.
	 *
	 * @param ability The ability this particle is spawned for
	 * @param loc     The location to use
	 * @param amount  The amount of particles
	 * @param xOffset The xOffset to use
	 * @param yOffset The yOffset to use
	 * @param zOffset The zOffset to use
	 */
	public static void playLightningbendingParticles(final CoreAbility ability, final Location loc, final int amount, final double xOffset, final double yOffset, final double zOffset) {
		GeneralMethods.displayColoredParticle("#01E1FF", loc, amount, xOffset, yOffset, zOffset);
	}

	/**
	 * Plays an integer amount of lightning particles in a location.
	 *
	 * @param ability The ability this particle is spawned for
	 * @param loc     The location to use
	 * @param amount  The amount of particles
	 */
	public static void playLightningbendingParticles(final CoreAbility ability, final Location loc, final int amount) {
		playLightningbendingParticles(ability, loc, amount, Math.random(), Math.random(), Math.random());
	}

	@Override
	public Class<? extends Ability> getParentAbility() {
		return FireAbility.class;
	}

	@Override
	public Element getElement() {
		return Element.LIGHTNING;
	}

	@Override
	public boolean isIgniteAbility() {
		return false;
	}

	//Overriding these methods to make sure Lightning abilities don't get buffed by blue fire
	@Override
	public double applyModifiersDamage(double value) {
		return GeneralMethods.applyModifiers(value, getDayFactor(1.0));
	}

	@Override
	public double applyModifiersRange(double value) {
		return GeneralMethods.applyModifiers(value, getDayFactor(1.0));
	}

	@Override
	public long applyModifiersCooldown(long value) {
		return (long) GeneralMethods.applyInverseModifiers(value, getDayFactor(1.0));
	}

}

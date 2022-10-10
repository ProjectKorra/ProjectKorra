package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.util.ParticleEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;

public abstract class SandAbility extends EarthAbility implements SubAbility {

	public SandAbility(final Player player) {
		super(player);
	}

	/**
	 * Plays an integer amount of sand particles in a location with a given
	 * xOffset, yOffset, zOffset, speed and color.
	 *
	 * @param ability The ability this particle is spawned for
	 * @param loc The location to use
	 * @param amount The amount of particles
	 * @param xOffset The xOffset to use
	 * @param yOffset The yOffset to use
	 * @param zOffset The zOffset to use
	 * @param speed The particle animation speed
	 * @param red Use red sand for the particle
	 */
	public static void displaySandParticle(final CoreAbility ability, final Location loc, final int amount, final double xOffset, final double yOffset, final double zOffset, final double speed, final boolean red) {
		if (amount <= 0) {
			return;
		}

		final Material sand = red ? Material.RED_SAND : Material.SAND;
		final Material stone = red ? Material.RED_SANDSTONE : Material.SANDSTONE;

		ParticleEffect.BLOCK_CRACK.display(loc, amount, xOffset, yOffset, zOffset, speed, sand.createBlockData());
		ParticleEffect.BLOCK_CRACK.display(loc, amount, xOffset, yOffset, zOffset, speed, stone.createBlockData());
	}

	public static void playSandbendingSound(final Location loc) {
		if (getConfig().getBoolean("Properties.Earth.PlaySound")) {
			final float volume = (float) getConfig().getDouble("Properties.Earth.SandSound.Volume");
			final float pitch = (float) getConfig().getDouble("Properties.Earth.SandSound.Pitch");

			Sound sound = Sound.BLOCK_SAND_BREAK;

			try {
				sound = Sound.valueOf(getConfig().getString("Properties.Earth.SandSound.Sound"));
			} catch (final IllegalArgumentException exception) {
				ProjectKorra.log.warning("Your current value for 'Properties.Earth.SandSound.Sound' is not valid.");
			} finally {
				loc.getWorld().playSound(loc, sound, volume, pitch);
			}
		}
	}

	@Override
	public Class<? extends Ability> getParentAbility() {
		return EarthAbility.class;
	}

	@Override
	public Element getElement() {
		return Element.SAND;
	}

}

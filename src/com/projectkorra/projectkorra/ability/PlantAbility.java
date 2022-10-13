package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.functional.Functional;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;

public abstract class PlantAbility extends WaterAbility implements SubAbility {

	public PlantAbility(final Player player) {
		super(player);
	}
	
	@Override
	public Class<? extends Ability> getParentAbility() {
		return WaterAbility.class;
	}

	@Override
	public Element getElement() {
		return Element.PLANT;
	}

	public static Functional.Particle plantParticles = (ability, location, amount, xOffset, yOffset, zOffset, extra, data) -> {
		location.getWorld().spawnParticle(Particle.BLOCK_CRACK, location.clone().add(0.5, 0, 0.5), amount, xOffset, yOffset, zOffset, data);
	};
	
	// Because Plantbending deserves particles too!
	public static void playPlantbendingParticles(CoreAbility ability, final Location loc, final int amount, final double xOffset, final double yOffset, final double zOffset) {
		plantParticles.play(ability, loc, amount, xOffset, yOffset, zOffset, 0, Material.OAK_LEAVES.createBlockData());
	}

	public static void playPlantbendingSound(final Location loc) {
		if (getConfig().getBoolean("Properties.Water.PlaySound")) {
			final float volume = (float) getConfig().getDouble("Properties.Water.PlantSound.Volume");
			final float pitch = (float) getConfig().getDouble("Properties.Water.PlantSound.Pitch");

			Sound sound = Sound.BLOCK_GRASS_STEP;

			try {
				sound = Sound.valueOf(getConfig().getString("Properties.Water.PlantSound.Sound"));
			} catch (final IllegalArgumentException exception) {
				ProjectKorra.log.warning("Your current value for 'Properties.Water.PlantSound.Sound' is not valid.");
			} finally {
				loc.getWorld().playSound(loc, sound, volume, pitch);
			}
		}
	}
}

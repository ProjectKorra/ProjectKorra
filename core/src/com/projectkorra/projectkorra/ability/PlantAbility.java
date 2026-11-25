package com.projectkorra.projectkorra.ability;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
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

	// Because Plantbending deserves particles too!
	public void playPlantbendingParticles(final Location loc, final int amount, final double xOffset, final double yOffset, final double zOffset) {
		loc.getWorld().spawnParticle(Particle.BLOCK, loc.clone().add(0.5, 0, 0.5), amount, xOffset, yOffset, zOffset, 0, Material.OAK_LEAVES.createBlockData(), true);
	}

}

package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.ability.functional.Functional;
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

    public static Functional.Particle plantParticles = args -> {
        Location loc = (Location) args[0];
        int amount = (int) args[1];
        double xOffset = (double) args[2];
        double yOffset = (double) args[3];
        double zOffset = (double) args[4];
        loc.getWorld().spawnParticle(Particle.BLOCK_CRACK, loc.clone().add(0.5, 0, 0.5), amount, xOffset, yOffset, zOffset, Material.OAK_LEAVES.createBlockData());
    };

    // Because Plantbending deserves particles too!
    public void playPlantbendingParticles(final Location loc, final int amount, final double xOffset, final double yOffset, final double zOffset) {
        plantParticles.play(loc, amount, xOffset, yOffset, zOffset);
    }

}

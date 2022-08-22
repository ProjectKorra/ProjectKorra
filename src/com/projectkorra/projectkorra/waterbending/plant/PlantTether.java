package com.projectkorra.projectkorra.waterbending.plant;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.PlantAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PlantTether extends PlantAbility {
    private Block source;
    private Location originLoc;
    @Attribute(Attribute.SELECT_RANGE)
    private double selectRange;
    @Attribute(Attribute.COOLDOWN)
    private long cooldown;
    private boolean targeted;

    public PlantTether(Player player) {
        super(player);
        // get source that the player is looking at

        this.setFields();

        if (CoreAbility.hasAbility(player, PlantTether.class)) {
            // get if looking source vs player and do things for each particular thing.

            this.targeted = true;
        } else {
            // start active instance
            this.source = BlockSource.getWaterSourceBlock(player, this.selectRange, ClickType.SHIFT_DOWN, false, false, this.bPlayer.canPlantbend());
            if (this.source != null && !GeneralMethods.isRegionProtectedFromBuild(this, this.source.getLocation())) {
                this.originLoc = this.source.getLocation();
                //start a timer
                this.start();
            }
        }
    }

    private void setFields() {
        this.selectRange = this.applyModifiers(getConfig().getDouble("Abilities.Water.Plantbending.PlantTether.SelectRange"));
        this.cooldown = getConfig().getLong("Abilities.Water.Plantbending.PlantTether.Cooldown");
    }

    @Override
    public void progress() {
        // If !started and it times oout, remove ability and start cooldown
        if (this.targeted) {
            // start the real move, either player or entity
        } else {
            this.playPlantbendingParticles(this.originLoc, 2, 0.5, 0.5, 0.5);
        }

    }

    @Override
    public boolean isSneakAbility() {
        return true;
    }

    @Override
    public boolean isHarmlessAbility() {
        return false;
    }

    @Override
    public long getCooldown() {
        return this.cooldown;
    }

    @Override
    public String getName() {
        return "PlantTether";
    }

    @Override
    public Location getLocation() {
        return this.originLoc;
    }
}

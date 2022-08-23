package com.projectkorra.projectkorra.waterbending.plant;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.PlantAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;


public class PlantTether extends PlantAbility {
    private Block source;
    private Location originLoc;
    @Attribute(Attribute.SELECT_RANGE)
    private double selectRange;
    @Attribute(Attribute.COOLDOWN)
    private long cooldown;
    private double timeOut;
    private Entity target;

    private enum State {INITIAL, ENTITYPULL, SEARCHING}
    private State currentState;

    public PlantTether(Player player) {
        super(player);
        // get source that the player is looking at
        if (this.bPlayer.isOnCooldown("PlantTether")) {
            return;
        }

        this.setFields();
        this.currentState = State.INITIAL;
        this.source = BlockSource.getWaterSourceBlock(this.player, this.selectRange, ClickType.SHIFT_DOWN, false, false, this.bPlayer.canPlantbend(), false, false);
        if (this.source != null) {
            this.originLoc = this.source.getLocation();
            this.start();
        }
    }

    private void setFields() {
        this.selectRange = this.applyModifiers(getConfig().getDouble("Abilities.Water.PlantTether.SelectRange"));
        this.cooldown = getConfig().getLong("Abilities.Water.PlantTether.Cooldown");
    }

    @Override
    public void progress() {
        switch (this.currentState) {
            case SEARCHING:
                Block newSource = BlockSource.getWaterSourceBlock(this.player, this.selectRange, ClickType.SHIFT_DOWN, false, false, this.bPlayer.canPlantbend(), false, false);
                Entity entity = GeneralMethods.getTargetedEntity(this.player, this.selectRange);
                if (entity != null) {
                    this.currentState = State.ENTITYPULL;
                    this.target = entity;
                    new PlantRegrowth(this.player, this.source);
                    this.source.setType(Material.AIR);;
                } else if (newSource != null) {
                    if (this.source.getLocation().equals(newSource.getLocation())) {
                        this.currentState = State.ENTITYPULL;
                        this.target = this.player;
                    } else {
                        this.source = newSource;
                        this.originLoc = this.source.getLocation();
                        this.currentState = State.INITIAL;
                        this.timeOut = 0;
                    }
                } else {
                    this.currentState = State.INITIAL;
                }
                break;
            case ENTITYPULL:
                this.playPlantbendingParticles(this.target.getLocation().clone().add(-0.5, 0, -0.5), 4, 0.2, 0.5, 0.2);

                if ((this.timeOut > 15000) || (this.target.getLocation().distance(this.originLoc) < 0.5)) {
                    this.bPlayer.addCooldown("PlantTether", this.getCooldown());
                    this.remove();
                }
                break;
            case INITIAL:
                if (this.timeOut > 1000) {
                    this.remove();
                }
                break;
            default:
                break;
        }


        this.playPlantbendingParticles(this.originLoc, 3, 0.2, 0.5, 0.2);
        this.timeOut += 1;
    }


    public void searchForEntity() {
        this.currentState = State.SEARCHING;
    }

    public boolean isInitial() {
        return (this.currentState == State.INITIAL);
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

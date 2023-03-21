package com.projectkorra.projectkorra.airbending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import org.bukkit.Location;

public class SideSwipe extends AirAbility {

    @Attribute(Attribute.SPEED)
    private double velocity;
    @Attribute(Attribute.COOLDOWN)
    private long cooldown;
    private boolean revertCooldownOnLand;

    public SideSwipe(AirSwipe swipe) {
        super(swipe.getPlayer());
        if (this.player == null) // ability is disabled
            return;
        boolean releaseSneakAllowed = getConfig().getBoolean("Abilities.Air.AirSwipe.SideSwipe.OnReleaseSneak", true);
        boolean sneakClickAllowed = getConfig().getBoolean("Abilities.Air.AirSwipe.SideSwipe.OnSneakClick", false);
        boolean allowedUsage = (sneakClickAllowed && player.isSneaking())
                || (releaseSneakAllowed && (!player.isSneaking() && swipe.isCharging()));
        if (!allowedUsage
                || !swipe.isTilted()
                || swipe.getSwipeDirection() == null
                || isOnGround()
                || this.bPlayer.isOnCooldown("SideSwipe")
                || hasAbility(player, this.getClass())
        )
            return;
        this.revertCooldownOnLand = getConfig().getBoolean("Abilities.Air.AirSwipe.SideSwipe.RevertCooldownAtLanding", true);
        this.velocity = getConfig().getDouble("Abilities.Air.AirSwipe.SideSwipe.Velocity", 0.8);
        this.cooldown = getConfig().getLong("Abilities.Air.AirSwipe.SideSwipe.Cooldown", 7000);
        start();
        if (isStarted()) {
            GeneralMethods.setVelocity(swipe, player, swipe.getSwipeDirection().multiply(-1 * velocity));
            this.bPlayer.addCooldown("SideSwipe", this.cooldown);
            if (!revertCooldownOnLand)
                remove();
        }
    }

    /**
     * Checks, if the player is on land and removes cooldown then.
     */
    @Override
    public void progress() {
        if (!bPlayer.isOnCooldown("SideSwipe") || isOnGround()) {
            bPlayer.removeCooldown("SideSwipe");
            this.remove();
        }
    }

    private boolean isOnGround() {
        return player.getLocation().add(0, -0.01, 0).getBlock().getType().isSolid();
    }

    @Override
    public boolean isEnabled() {
        return getConfig().getBoolean("Abilities.Air.AirSwipe.TiltedSwipe.Enabled", false)
                && getConfig().getBoolean("Abilities.Air.AirSwipe.SideSwipe.Enabled", false);
    }

    @Override
    public boolean isSneakAbility() {
        return false;
    }

    @Override
    public boolean isHarmlessAbility() {
        return true;
    }

    @Override
    public long getCooldown() {
        return cooldown;
    }

    @Override
    public String getName() {
        return "AirSwipe";
    }

    @Override
    public String getInstructions() {
        return getAbility(AirSwipe.class).getInstructions();
    }

    public void setCooldown(final long cooldown) {
        this.cooldown = cooldown;
    }

    public double getVelocity() {
        return velocity;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public boolean isRevertCooldownOnLand() {
        return revertCooldownOnLand;
    }

    public void setRevertCooldownOnLand(boolean revertCooldownOnLand) {
        this.revertCooldownOnLand = revertCooldownOnLand;
    }

    @Override
    public Location getLocation() {
        return player.getLocation();
    }
}

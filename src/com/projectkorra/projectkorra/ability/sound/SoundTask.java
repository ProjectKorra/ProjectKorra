package com.projectkorra.projectkorra.ability.sound;

import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

public class SoundTask extends BukkitRunnable {

    private Location location;
    private AbilitySound sound;

    public SoundTask(final AbilitySound sound, final Location location) {
        setLocation(location);
        setSound(sound);
    }

    public Location getLocation() {
        return this.location;
    }

    public AbilitySound getSound() {
        return this.sound;
    }

    public void setLocation(final Location location) {
        this.location = location;
    }

    public void setSound(final AbilitySound sound) {
        this.sound = sound;
    }

    @Override
    public void run() {
        sound.play(getLocation());
    }
}

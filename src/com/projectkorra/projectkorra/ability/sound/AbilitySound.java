package com.projectkorra.projectkorra.ability.sound;

import org.bukkit.Location;
import org.bukkit.Sound;

public class AbilitySound {

    public Sound sound;
    public double volume;
    public double pitch;

    public AbilitySound() {}

    public Sound getSound() {
        return this.sound;
    }

    public double getVolume() {
        return this.volume;
    }

    public double getPitch() {
        return this.pitch;
    }

    public void play(final Location location) {}
}

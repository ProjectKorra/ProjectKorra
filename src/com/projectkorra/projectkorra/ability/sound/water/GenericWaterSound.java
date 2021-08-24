package com.projectkorra.projectkorra.ability.sound.water;

import com.projectkorra.projectkorra.ability.sound.AbilitySound;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.Configuration;

import java.util.EnumSet;

public class GenericWaterSound extends AbilitySound {

    public static GenericWaterSound getSound;

    public GenericWaterSound() {
        // Get Water sound properties from the config
        Configuration config = ConfigManager.getConfig();
        this.volume = config.getDouble("Properties.Water.WaterSound.Volume");
        if (Double.compare(volume, 0.1) < 0) this.volume = 0.1; // min.
        if (Double.compare(volume, 1.0) > 0) this.volume = 1.0; // max., no sense in allowing higher values for this
        this.pitch = config.getDouble("Properties.Water.WaterSound.Pitch");
        if (Double.compare(pitch, 0.5) < 0) this.pitch = 0.5; // min.
        if (Double.compare(pitch, 2.0) > 0) this.pitch = 2.0; // max.
        String n = config.getString("Properties.Water.WaterSound.Sound");
        // Check Sound enum for existence of configured sound
        this.sound = EnumSet.allOf(Sound.class)
                .stream()
                .filter(e -> e.toString().equals(n))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(String.format("'%s' is not a valid Sound argument.", n)));
        // returns null if thrown
        if (sound == null) {
            this.sound = Sound.BLOCK_WATER_AMBIENT;
        }
        // This caches the WaterSound and it's properties so we aren't recreating it every time a Water sound is played
        getSound = this;
    }

    public void play(final Location loc) {
        // override me for more control
        loc.getWorld().playSound(loc, getSound(), (float) getVolume(), (float) getPitch());
    }
}

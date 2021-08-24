package com.projectkorra.projectkorra.ability.sound.water;

import com.projectkorra.projectkorra.ability.sound.AbilitySound;
import com.projectkorra.projectkorra.ability.sound.SoundTask;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import org.bukkit.Location;

public class PlantSoundTask extends SoundTask {

    public PlantSoundTask(final AbilitySound sound, final Location location) {
        super(sound, location);
        if (!ConfigManager.getConfig().getBoolean("Properties.Water.PlaySound")) return;
        run();
    }

    @Override
    public void run() {
        getSound().play(getLocation());
    }
}

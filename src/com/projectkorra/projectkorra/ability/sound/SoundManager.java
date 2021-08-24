package com.projectkorra.projectkorra.ability.sound;

import com.projectkorra.projectkorra.ability.sound.water.GenericIceSound;
import com.projectkorra.projectkorra.ability.sound.water.GenericPlantSound;
import com.projectkorra.projectkorra.ability.sound.water.GenericWaterSound;

public class SoundManager {

    static {
        loadSounds();
    }

    public SoundManager() {

    }

    public static void loadSounds() {
        new GenericIceSound();
        new GenericPlantSound();
        new GenericWaterSound();
    }
}

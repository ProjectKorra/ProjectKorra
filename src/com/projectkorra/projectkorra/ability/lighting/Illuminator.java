package com.projectkorra.projectkorra.ability.lighting;

import org.bukkit.Location;

public interface Illuminator {

    void emitLightAt(Location loc, int brightness, long ticks);

    void emitLight(Light light);

    void killLight(Light light);
}

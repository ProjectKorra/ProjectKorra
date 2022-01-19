package com.projectkorra.projectkorra.ability.lighting;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class Light {

    private final LightPos lightPos;
    private final int brightness;
    private final long duration;
    private final long genesis;

    public Light(LightPos lightPos, int brightness, long ticks) {
        this.lightPos = lightPos;
        this.brightness = brightness;
        this.duration = ticks;
        this.genesis = System.currentTimeMillis();
    }

    public LightPos getLightPos() {
        return lightPos;
    }

    public int getBrightness() {
        return brightness;
    }

    public long getDuration() {
        return duration;
    }

    public long getGenesis() {
        return genesis;
    }

    public Location getLocation() {
        return lightPos.getLocation();
    }

    public BlockData getPriorData() {
        return lightPos.getPriorData();
    }

    public Block getBlock() {
        return lightPos.getBlock();
    }
}

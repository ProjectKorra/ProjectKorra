package com.projectkorra.projectkorra.ability.lighting;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;

public class Illuminator_Bukkit implements Illuminator {

    @Override
    public void emitLight(final Location location, int brightness, long ticks) {
        LightPos lightPos = new LightPos(location);
        Light light = new Light(lightPos, Math.min(brightness, 15), ticks);
        emitLight(light);
    }

    @Override
    public void emitLight(Light light) {
        if (!light.getLightPos().canLight()) return;

        BlockData lightData = Material.valueOf("LIGHT").createBlockData();
        ((Levelled) lightData).setLevel(light.getBrightness());

        for (final Player player : Bukkit.getOnlinePlayers()) {
            player.sendBlockChange(light.getLocation(), lightData);
        }

        LightManager.getActiveLights().put(light.getBlock(), light);

        Bukkit.getScheduler().scheduleSyncDelayedTask(LightManager.getPlugin(), () -> {
            killLight(light);
        }, light.getDuration());
    }

    @Override
    public void killLight(Light light) {
        Light newerLight = LightManager.getActiveLights().get(light.getBlock());

        if (newerLight != null) {
            if (LightManager.getActiveLights().containsKey(light.getBlock())) {
                Light newLight = LightManager.getActiveLights().get(light.getBlock());
                if (System.currentTimeMillis() - newLight.getGenesis() < light.getDuration()) return;
            }
            if (newerLight.getBrightness() >= 4) {
                emitLight(light.getLocation().add(0, 1.0, 0), Math.max(newerLight.getBrightness() - 3, 1), 5L);
                LightManager.getActiveLights().remove(light.getBlock());
                return;
            }
        }

        LightManager.getActiveLights().remove(light.getBlock());

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendBlockChange(light.getLocation(), light.getPriorData());
        }
    }
}

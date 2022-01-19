package com.projectkorra.projectkorra.ability.lighting;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Illuminator_Bukkit implements Illuminator {

    @Override
    public void emitLightAt(final Location loc, int brightness, long ticks) {
        Light light = new Light(loc.getBlock(), Math.min(brightness, 15), ticks); // Create a new Light.
        emitLight(light);
    }

    @Override
    public void emitLight(Light light) {
        if (!light.canLight()) return; // Check for any nearby blocks, check PK TempBlocks, and make sure it's AIR.
        if (light.isEmitting()) {
            return;
        } else {
            // Fade in?
        }
        LightManager.cache.add(light.block()); // Cache the light's Block.
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (player.getLocation().distance(light.block().getLocation()) < 32) { // Only send to players within this.
                player.sendBlockChange(light.block().getLocation(), LightManager.lightData.get(light.brightness()));
            }
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(LightManager.getPlugin(), () -> {
            killLight(light);
        }, light.ticks()); // Kill the light after the specified delay. Light fading includes additional ticks.
    }

    @Override
    public void killLight(Light light) {
        LightManager.cache.remove(light.block()); // Remove the light's block from the cache.
        // Fade out.
        if (light.brightness() > 3) {
            emitLightAt(light.block().getLocation(), light.brightness() - 3, 4L);
            return;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Not checking distance here yet because I want lights to be entirely reverted for everyone.
            player.sendBlockChange(light.block().getLocation(), light.priorData());
        }
    }
}

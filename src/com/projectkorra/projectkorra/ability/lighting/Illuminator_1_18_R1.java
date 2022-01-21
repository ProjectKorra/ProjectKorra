package com.projectkorra.projectkorra.ability.lighting;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.PacketPlayOutBlockChange;
import net.minecraft.world.level.block.state.IBlockData;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_18_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class Illuminator_1_18_R1 implements Illuminator {

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
                sendBlockChange(player, light.block().getLocation(), LightManager.lightData.get(light.brightness()));
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
            sendBlockChange(player, light.block().getLocation(), light.priorData());
        }
    }

    private void sendBlockChange(Player player, Location loc, BlockData data) {
        // Testing out the same method straight from CraftPlayer. Might try something else next.
        if (((CraftPlayer) player).getHandle().b != null) {
            BlockPosition blockPosition = new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            IBlockData iBlockData = ((CraftBlockData) data).getState();
            PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange(blockPosition, iBlockData);
            ((CraftPlayer) player).getHandle().b.a(packet);
        }
    }
}

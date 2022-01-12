package com.projectkorra.projectkorra.ability.lighting;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;

public class FauxLight {
    final public static ConcurrentHashMap<Block, FauxLight> cachedLights = new ConcurrentHashMap<>();
    private static boolean warned;

    final private Block block;
    final private int brightness;
    final private long duration;
    final private long startTime;

    final private Location location;
    final private BlockData priorData;

    private BlockData lightData;

    public FauxLight(final Block block, int brightness, long duration) {
        this.block = block;
        this.brightness = Math.min(brightness, 13);
        this.duration = Math.max(duration, 1);
        this.startTime = System.currentTimeMillis();

        this.location = block.getLocation();
        this.priorData = block.getBlockData();

        if (Material.matchMaterial("LIGHT") == null) {
            if (!warned) { // Warn the admins if their MC version does not contain the LIGHT material
                String warning = Bukkit.getVersion() + " does not contain LIGHT. Lighting will not work.";
                ProjectKorra.plugin.getLogger().info(warning);
                warned = true;
            }
            return;
        }

        this.lightData = Material.valueOf("LIGHT").createBlockData();

        cachedLights.put(this.block, this);

        final boolean isTempBlock = TempBlock.isTempBlock(block);
        final Material type = this.block.getType();
        if (isTempBlock || (type != Material.WATER && type != Material.AIR)) return;

        // For lighting underwater. TODO: Can we set the water level (Levelled) for waterlogged light?
        if (type == Material.WATER) { ((Waterlogged) this.lightData).setWaterlogged(true); }
        ((Levelled) lightData).setLevel(brightness);

        for (final Player player : Bukkit.getOnlinePlayers()) {
            Bukkit.getScheduler().runTaskAsynchronously(ProjectKorra.plugin, () -> {
                player.sendBlockChange(block.getLocation(), lightData); // Sends the block change packet async.
            });
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(ProjectKorra.plugin, this::killLight, duration);
    }

    public Block getBlock() {
        return this.block;
    }

    public int getBrightness() {
        return this.brightness;
    }

    public long getDuration() {
        return this.duration;
    }

    public long getStartTime() {
        return this.startTime;
    }

    private void killLight() {
        final FauxLight newLight = FauxLight.cachedLights.get(block);

        if (newLight != null) {
            if (System.currentTimeMillis() - newLight.getStartTime() < duration) {
                return;
            }
            if (getBrightness() > 3) {
                FauxLight.cachedLights.remove(newLight.getBlock());
                new FauxLight(newLight.getBlock(), Math.max(getBrightness() - 2, 1), 5L);
                return;
            }
        }

        FauxLight.cachedLights.remove(getBlock());
        for (final Player player : Bukkit.getOnlinePlayers()) {
            Bukkit.getScheduler().runTaskAsynchronously(ProjectKorra.plugin, () -> {
                player.sendBlockChange(location, priorData);
            });
        }
    }
}

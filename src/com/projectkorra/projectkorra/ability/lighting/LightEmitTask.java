package com.projectkorra.projectkorra.ability.lighting;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class LightEmitTask implements Runnable {

    private static boolean warned;

    final public static ConcurrentHashMap<Block, LightEmitTask> cachedTasks = new ConcurrentHashMap<>();

    final private Block block;
    final private int brightness;
    final private long delay;
    final private long startTime;

    public LightEmitTask(final Block block, int brightness, long delay) {
        this.block = block;
        this.brightness = Math.min(brightness, 15);
        this.delay = Math.max(delay, 25);
        this.startTime = System.currentTimeMillis();

        if (Material.matchMaterial("LIGHT") == null) {
            if (!warned) { // Warn the admins if their MC version does not contain the LIGHT material
                String warning = Bukkit.getVersion() + " does not contain LIGHT. This addon is incompatible.";
                ProjectKorra.plugin.getLogger().log(Level.INFO, warning);
                warned = true;
            }
            return;
        }

        cachedTasks.put(block, this);
        Bukkit.getScheduler().runTaskAsynchronously(ProjectKorra.plugin, this);
    }

    public Block getBlock() {
        return this.block;
    }

    public long getDelay() {
        return this.delay;
    }

    public long getStartTime() {
        return this.startTime;
    }

    @Override
    public void run() {
        final Material type = this.block.getType(); // The Material of the block in question
        final boolean isTempBlock = TempBlock.isTempBlock(block);

        // Stop if it's a temp block or anything other than water or air.
        if (isTempBlock || (type != Material.WATER && type != Material.AIR)) return;

        // Create the fake light block data to send to clients.
        BlockData lightData = Material.valueOf("LIGHT").createBlockData();
        if (type == Material.WATER) { ((Waterlogged) lightData).setWaterlogged(true); } // For lighting underwater.
        ((Levelled) lightData).setLevel(brightness); // Set the brightness level, 0-15.

        // Iterate online players and send them the light block change. Clients will handle the rendering.
        for (final Player player : Bukkit.getOnlinePlayers()) {
            player.sendBlockChange(block.getLocation(), lightData); // Sends the light.
            new LightKillTask(this, player); // Initiate the light removal.
        }
    }
}
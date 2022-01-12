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

    final public static ConcurrentHashMap<Block, LightEmitTask> cachedTasks = new ConcurrentHashMap<>();
    private static boolean warned;

    final private Block block;
    final private int brightness;
    final private long delay;
    final private long startTime;
    private BlockData lightData;

    public LightEmitTask(final Block block, int brightness, long delay) {
        this.block = block;
        this.brightness = Math.min(brightness, 13);
        this.delay = Math.max(delay, 1);
        this.startTime = System.currentTimeMillis();

        if (Material.matchMaterial("LIGHT") == null) {
            if (!warned) { // Warn the admins if their MC version does not contain the LIGHT material
                String warning = Bukkit.getVersion() + " does not contain LIGHT. Lighting will not work.";
                ProjectKorra.plugin.getLogger().log(Level.INFO, warning);
                warned = true;
            }
            return; // Return and do nothing if the server does not have LIGHT.
        } else {
            // Create the fake light block data to send to clients.
            this.lightData = Material.valueOf("LIGHT").createBlockData();
        }

        cachedTasks.put(this.block, this); // Cache this task for time checking in LightKillTask.
        Bukkit.getScheduler().scheduleSyncDelayedTask(ProjectKorra.plugin, this); // Runs this task.
    }

    public Block getBlock() {
        return this.block;
    }

    public int getBrightness() {
        return this.brightness;
    }

    public long getDelay() {
        return this.delay;
    }

    public long getStartTime() {
        return this.startTime;
    }

    @Override
    public void run() {
        final Material type = this.block.getType();
        final boolean isTempBlock = TempBlock.isTempBlock(block);
        // Return if it's a TempBlock or anything other than water or air.
        if (isTempBlock || (type != Material.WATER && type != Material.AIR)) return;
        // For lighting underwater. TODO: needs more testing.
        if (type == Material.WATER) { ((Waterlogged) this.lightData).setWaterlogged(true); }
        ((Levelled) lightData).setLevel(brightness); // Set the brightness level, 0-15.
        // Iterate online players and send them the light block change packet (via sendBlockChange.)
        for (final Player player : Bukkit.getOnlinePlayers()) {
            Bukkit.getScheduler().runTaskAsynchronously(ProjectKorra.plugin, () -> {
                player.sendBlockChange(block.getLocation(), lightData); // Sends the block change packet async.
            });
            new LightKillTask(this, player); // Starts the kill task (loop.)
        }
    }
}

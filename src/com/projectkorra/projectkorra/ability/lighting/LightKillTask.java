package com.projectkorra.projectkorra.ability.lighting;

import com.projectkorra.projectkorra.ProjectKorra;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

public class LightKillTask implements Runnable {

    final private LightEmitTask emitTask;
    final private Player player;
    final private Block block;
    final private long delay;
    final private Location blockLoc;
    final private BlockData priorData;

    public LightKillTask(final LightEmitTask emitTask, final Player player) {
        this.emitTask = emitTask;
        this.player = player;
        this.block = emitTask.getBlock();
        this.delay = emitTask.getDelay();
        this.blockLoc = block.getLocation();
        this.priorData = block.getBlockData();

        Bukkit.getScheduler().scheduleSyncDelayedTask(ProjectKorra.plugin, this, this.delay);
    }

    @Override
    public void run() {
        // Get the most recent scheduled emit task (with its start time) for this tasks block.
        final LightEmitTask newTask = LightEmitTask.cachedTasks.get(this.block);
        // This safely keeps the light on until tasks aren't being scheduled there anymore. (anti-flicker)
        if (newTask != null) {
            if (System.currentTimeMillis() - newTask.getStartTime() < this.delay) {
                new LightKillTask(newTask, this.player); // Recursively starts a new kill task.
                return;
            }
        }
        // Revert the block (update the client) to its original data (air, or water.)
        Bukkit.getScheduler().runTaskAsynchronously(ProjectKorra.plugin, () -> {
            player.sendBlockChange(blockLoc, priorData);
            LightEmitTask.cachedTasks.remove(emitTask.getBlock()); // Remove cached emit task for the block.
        });
    }
}

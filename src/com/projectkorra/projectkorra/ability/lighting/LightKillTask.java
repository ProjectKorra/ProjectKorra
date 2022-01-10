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
        final LightEmitTask newTask = LightEmitTask.cachedTasks.get(this.block);

        // Recursively call the kill task if the light hasn't lived longer than the delay.
        if (newTask != null) {
            if (System.currentTimeMillis() - newTask.getStartTime() < this.delay) {
                new LightKillTask(newTask, this.player);
                return;
            }
        }

        Bukkit.getScheduler().runTaskLaterAsynchronously(ProjectKorra.plugin, () -> {
            LightEmitTask.cachedTasks.remove(emitTask.getBlock());
            player.sendBlockChange(blockLoc, priorData);
        },1L);
    }
}

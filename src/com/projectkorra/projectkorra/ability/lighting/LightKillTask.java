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

        Bukkit.getScheduler().runTaskLaterAsynchronously(ProjectKorra.plugin, this, this.delay);
    }

    @Override
    public void run() {
        // TODO: The problem is somewhere here.
        // Abilities are flickering on charge, and a couple others, like WallOfFire, flash like crazy.

        final LightEmitTask newTask = LightEmitTask.cachedTasks.get(this.block);

        if (newTask != null) {
            if (System.currentTimeMillis() - newTask.getStartTime() < this.delay) {
                new LightKillTask(newTask, this.player);
                return;
            }
        }

        player.sendBlockChange(this.blockLoc, this.priorData);
        LightEmitTask.cachedTasks.remove(this.emitTask.getBlock());
    }
}
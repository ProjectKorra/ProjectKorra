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
    final private Location blockLoc;
    final private BlockData priorData;

    public LightKillTask(final LightEmitTask emitTask, final Player player) {
        this.emitTask = emitTask;
        this.player = player;

        Block block = emitTask.getBlock();
        long delay = emitTask.getDelay();

        this.blockLoc = block.getLocation();
        this.priorData = block.getBlockData();

        if (LightEmitTask.cachedTasks.get(block) != null) {
            if (System.currentTimeMillis() - LightEmitTask.cachedTasks.get(block).getStartTime() < delay) {
                new LightKillTask(this.emitTask, this.player);
                return;
            }
        }

        Bukkit.getScheduler().runTaskLaterAsynchronously(ProjectKorra.plugin, this, delay);
    }

    @Override
    public void run() {
        player.sendBlockChange(this.blockLoc, this.priorData);
        LightEmitTask.cachedTasks.remove(this.emitTask.getBlock());
    }
}
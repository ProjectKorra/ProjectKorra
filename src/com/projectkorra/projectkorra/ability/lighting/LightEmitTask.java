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
    private BlockData lightData;

    public LightEmitTask(final Block block, int brightness) {
        this.block = block;
        this.brightness = Math.min(brightness, 15);
        this.delay = 30;
        this.startTime = System.currentTimeMillis();

        if (Material.matchMaterial("LIGHT") == null) {
            if (!warned) { // Warn the admins if their MC version does not contain the LIGHT material
                String warning = Bukkit.getVersion() + " does not contain LIGHT. Lighting will not work.";
                ProjectKorra.plugin.getLogger().log(Level.INFO, warning);
                warned = true;
            }
            return;
        } else {
            // Create the fake light block data to send to clients.
            this.lightData = Material.valueOf("LIGHT").createBlockData();
        }

        cachedTasks.put(this.block, this);

        Bukkit.getScheduler().scheduleSyncDelayedTask(ProjectKorra.plugin, this);
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
        final Material type = this.block.getType();
        final boolean isTempBlock = TempBlock.isTempBlock(block);

        // Stop if it's a temp block or anything other than water or air.
        if (isTempBlock || (type != Material.WATER && type != Material.AIR)) return;

        if (type == Material.WATER) { ((Waterlogged) this.lightData).setWaterlogged(true); } // For lighting underwater.
        ((Levelled) lightData).setLevel(brightness); // Set the brightness level, 0-15.

        // Iterate online players and send them the light block change. Clients will handle the rendering.
        for (final Player player : Bukkit.getOnlinePlayers()) {
            Bukkit.getScheduler().runTaskAsynchronously(ProjectKorra.plugin, () -> {
                player.sendBlockChange(block.getLocation(), lightData); // Sends the block change packet async.
            });
            new LightKillTask(this, player); // Initiate the light removal.
        }
    }
}

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

import java.util.logging.Level;

public class LightEmitTask implements Runnable {

    private static boolean warned;

    final private Block block;
    final private int brightness;
    final private long delay;

    public LightEmitTask(final Block block, int brightness, long delay) {
        this.block = block;
        this.brightness = Math.min(brightness, 15);
        this.delay = Math.max(delay, 15);
        if (Material.matchMaterial("LIGHT") == null) {
            if (!warned) { // Warn the admins if their MC version does not contain the LIGHT material
                ProjectKorra.plugin.getLogger().log(Level.INFO,Bukkit.getVersion() + " does not contain LIGHT. This addon is incompatible.");
                warned = true;
            }
        }
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
        ((Levelled) lightData).setLevel(brightness); // Set the brightness level, 0-15
        // Iterate online players and send them the light block change. Clients will handle the rendering.
        final Location blockLoc = this.block.getLocation();
        final BlockData priorData = this.block.getBlockData();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendBlockChange(blockLoc, lightData); // Sends the light.
            // After the specified delay, revert the block to its original state without triggering any updates.
            Bukkit.getScheduler().scheduleSyncDelayedTask(ProjectKorra.plugin, () -> {
                player.sendBlockChange(blockLoc, priorData);
            }, Math.max(delay, 15)); // delay shouldn't be less than 15ms, things get weird with rapid light changes.
        }
    }
}
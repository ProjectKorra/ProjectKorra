package com.projectkorra.projectkorra.ability.lighting;

import com.projectkorra.projectkorra.util.TempBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class LightPos {

    private final Block block;
    private final Location location;
    private final BlockData priorData;

    public LightPos(final Location location) {
        this.block = location.getBlock();
        this.location = block.getLocation();
        this.priorData = block.getBlockData();
    }

    public LightPos(final Block block) {
        this.block = block;
        this.location = block.getLocation();
        this.priorData = block.getBlockData();
    }

    public Block getBlock() {
        return block;
    }

    public Location getLocation() {
        return location;
    }

    public BlockData getPriorData() {
        return priorData;
    }

    public boolean canLight() {
        return !TempBlock.isTempBlock(getBlock()) && (getBlock().getType() == Material.AIR);
    }
}

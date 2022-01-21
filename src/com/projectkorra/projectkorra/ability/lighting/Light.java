package com.projectkorra.projectkorra.ability.lighting;

import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;

public record Light(Block block, int brightness, long ticks) {

    public boolean canLight() {
        boolean hasNearbyBlock = false;
        for (BlockFace face : LightManager.blockFaces) {
            Block block = block();
            for (int i = 0; i < 6; i++) {
                block = block.getRelative(face);
                if (!block.isEmpty()) {
                    hasNearbyBlock = true;
                    break;
                }
            }
        }
        return hasNearbyBlock && !TempBlock.isTempBlock(block()) && block().getType() == Material.AIR;
    }

    public boolean isEmitting() {
        return LightManager.cache.contains(block());
    }

    public BlockData priorData() {
        return block.getBlockData();
    }
}

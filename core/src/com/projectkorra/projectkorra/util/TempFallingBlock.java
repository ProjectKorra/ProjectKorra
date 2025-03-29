package com.projectkorra.projectkorra.util;

import com.projectkorra.projectkorra.ability.CoreAbility;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class TempFallingBlock {
    public static ConcurrentHashMap<FallingBlock, TempFallingBlock> instances = new ConcurrentHashMap<>();

    private final FallingBlock fallingblock;
    private final CoreAbility ability;
    private final long creation;
    private final boolean expire;
    private Consumer<TempFallingBlock> onPlace;

    public TempFallingBlock(Location location, BlockData data, Vector velocity, CoreAbility ability) {
        this(location, data, velocity, ability, false);
    }

    public TempFallingBlock(Location location, BlockData data, Vector velocity, CoreAbility ability, boolean expire) {
        this.fallingblock = location.getWorld().spawnFallingBlock(location, data.clone());
        this.fallingblock.setVelocity(velocity);
        this.fallingblock.setDropItem(false);
        this.ability = ability;
        this.creation = System.currentTimeMillis();
        this.expire = expire;
        instances.put(fallingblock, this);
    }

    public static void manage() {
        long time = System.currentTimeMillis();

        for (TempFallingBlock tfb : instances.values()) {
            if (tfb.canExpire() && time > tfb.getCreationTime() + 5000) {
                tfb.remove();
            } else if (time > tfb.getCreationTime() + 120000) { // Add a hard timeout for any abilities that misuse this.
                tfb.remove();
            }
        }
    }

    public static TempFallingBlock get(FallingBlock fallingBlock) {
        return fallingBlock == null ? null : instances.get(fallingBlock);
    }

    public static boolean isTempFallingBlock(FallingBlock fallingBlock) {
        return instances.containsKey(fallingBlock);
    }

    public static void removeFallingBlock(FallingBlock fallingBlock) {
        if (instances.containsKey(fallingBlock)) {
            fallingBlock.remove();
            instances.remove(fallingBlock);
        }
    }

    public static void removeAllFallingBlocks() {
        for (FallingBlock fallingblock : instances.keySet()) {
            fallingblock.remove();
        }
        instances.clear();
    }

    public static List<TempFallingBlock> getFromAbility(CoreAbility ability) {
        List<TempFallingBlock> tempFallingBlocks = new ArrayList<>();
        for (TempFallingBlock tempFallingBlock : instances.values()) {
            if (tempFallingBlock.getAbility().equals(ability)) {
                tempFallingBlocks.add(tempFallingBlock);
            }
        }
        return tempFallingBlocks;
    }

    public void remove() {
        fallingblock.remove();
        instances.remove(fallingblock);
    }

    public FallingBlock getFallingBlock() {
        return fallingblock;
    }

    public CoreAbility getAbility() {
        return ability;
    }

    public Material getMaterial() {
        return fallingblock.getBlockData().getMaterial();
    }

    public BlockData getMaterialData() {
        return fallingblock.getBlockData();
    }

    public BlockData getData() {
        return fallingblock.getBlockData();
    }

    public Location getLocation() {
        return fallingblock.getLocation();
    }

    public long getCreationTime() {
        return creation;
    }

    public boolean canExpire() {
        return expire;
    }

    public void tryPlace() {
        if (onPlace != null) {
            onPlace.accept(this);
        }
    }

    public Consumer<TempFallingBlock> getOnPlace() {
        return onPlace;
    }

    public void setOnPlace(Consumer<TempFallingBlock> onPlace) {
        this.onPlace = onPlace;
    }
}

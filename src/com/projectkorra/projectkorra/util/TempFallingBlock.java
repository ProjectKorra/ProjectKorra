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

    private FallingBlock fallingblock;
    private CoreAbility ability;
    private long creation;
    private boolean expire;
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

    public static TempFallingBlock get(FallingBlock fallingblock) {
        if (isTempFallingBlock(fallingblock)) {
            return instances.get(fallingblock);
        }
        return null;
    }

    public static boolean isTempFallingBlock(FallingBlock fallingblock) {
        return instances.containsKey(fallingblock);
    }

    public static void removeFallingBlock(FallingBlock fallingblock) {
        if (isTempFallingBlock(fallingblock)) {
            fallingblock.remove();
            instances.remove(fallingblock);
        }
    }

    public static void removeAllFallingBlocks() {
        for (FallingBlock fallingblock : instances.keySet()) {
            fallingblock.remove();
            instances.remove(fallingblock);
        }
    }

    public static List<TempFallingBlock> getFromAbility(CoreAbility ability) {
        List<TempFallingBlock> tfbs = new ArrayList<TempFallingBlock>();
        for (TempFallingBlock tfb : instances.values()) {
            if (tfb.getAbility().equals(ability)) {
                tfbs.add(tfb);
            }
        }
        return tfbs;
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
        return fallingblock.getMaterial();
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

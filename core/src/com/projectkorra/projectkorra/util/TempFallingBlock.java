package com.projectkorra.projectkorra.util;

import com.projectkorra.projectkorra.ability.CoreAbility;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * TempFallingBlock is a utility class that allows for the creation and management of temporary falling blocks in Minecraft.
 * It provides methods to create, manage, and remove falling blocks, as well as to check if a falling block is a TempFallingBlock.
 */
public class TempFallingBlock {
    public static ConcurrentHashMap<FallingBlock, TempFallingBlock> instances = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<CoreAbility, Set<TempFallingBlock>> instancesByAbility = new ConcurrentHashMap<>();

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
        if (!instancesByAbility.containsKey(ability)) {
            instancesByAbility.put(ability, new HashSet<>());
        }
        instancesByAbility.get(ability).add(this);

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

    /**
     * Check if the falling block is a TempFallingBlock.
     * @param fallingblock The falling block to check.
     * @return True if the falling block is a TempFallingBlock, false otherwise.
     */
    public static boolean isTempFallingBlock(FallingBlock fallingblock) {
        return instances.containsKey(fallingblock);
    }

    /**
     * Remove a TempFallingBlock from the world.
     * @param fallingblock The falling block to remove.
     */
    public static void removeFallingBlock(FallingBlock fallingblock) {
        if (isTempFallingBlock(fallingblock)) {
            TempFallingBlock tempFallingBlock = instances.get(fallingblock);
            ThreadUtil.ensureEntity(fallingblock, fallingblock::remove);
            instances.remove(fallingblock);
            instancesByAbility.get(tempFallingBlock.ability).remove(tempFallingBlock);
            if (instancesByAbility.get(tempFallingBlock.ability).isEmpty()) {
                instancesByAbility.remove(tempFallingBlock.ability);
            }
        }
    }

    /**
     * Remove all TempFallingBlocks from the world.
     */
    public static void removeAllFallingBlocks() {
        for (FallingBlock fallingblock : instances.keySet()) {
            ThreadUtil.ensureEntity(fallingblock, fallingblock::remove);
        }
        instances.clear();
        instancesByAbility.clear();
    }

    /**
     * Get all TempFallingBlocks associated with a specific ability.
     * @param ability The ability to get TempFallingBlocks for.
     * @return A set of TempFallingBlocks associated with the ability.
     */
    public static Set<TempFallingBlock> getFromAbility(CoreAbility ability) {
        return instancesByAbility.getOrDefault(ability, new HashSet<>());
    }

    /**
     * Remove this TempFallingBlock from the world.
     */
    public void remove() {
        ThreadUtil.ensureEntity(fallingblock, fallingblock::remove);
        instances.remove(fallingblock);
    }

    /**
     * Get the falling block associated with this TempFallingBlock.
     * @return The falling block.
     */
    public FallingBlock getFallingBlock() {
        return fallingblock;
    }

    /**
     * Get the ability associated with this TempFallingBlock.
     * @return The ability.
     */
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

    /**
     * This method is called automatically when the falling block tries to be
     * placed in the world. It will call the onPlace consumer if it is set.
     */
    public void tryPlace() {
        if (onPlace != null) {
            onPlace.accept(this);
        }
    }

    /**
     * Get the onPlace callback for this TempFallingBlock. This will be called
     * when the falling block tries to be placed in the world.
     * @return The onPlace consumer, or null.
     */
    public Consumer<TempFallingBlock> getOnPlace() {
        return onPlace;
    }

    /**
     * Set the onPlace callback for this TempFallingBlock. This will be called
     * when the falling block tries to be placed in the world.
     * @param onPlace The consumer to set.
     */
    public void setOnPlace(Consumer<TempFallingBlock> onPlace) {
        this.onPlace = onPlace;
    }
}

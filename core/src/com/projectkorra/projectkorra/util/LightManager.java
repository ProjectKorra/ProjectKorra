package com.projectkorra.projectkorra.util;

import com.projectkorra.projectkorra.GeneralMethods;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class LightManager {

    private static final LightManager INSTANCE = new LightManager();

    // If this version is pre-LIGHT, this class basically does nothing.
    private static final boolean MODERN = GeneralMethods.getMCVersion() >= 1170;

    // Map of all LightData, keyed by *Block* Locations.
    // LightManager#addLight will automatically get the provided locations block location for you.
    private final Map<Location, LightData> lightMap = new ConcurrentHashMap<>();

    // Default LIGHT BlockData
    private final BlockData defaultLightData;
    private final BlockData defaultWaterloggedLightData;

    // Scheduler with threads equal to the number of available processors, this handles reverting expired lights.
    private ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
    private final Runnable reverterTask;

    public LightManager() {
        this.defaultLightData = Bukkit.createBlockData(Material.valueOf("LIGHT"));
        this.defaultWaterloggedLightData = createDefaultWaterloggedLightData();
        this.reverterTask = this::revertExpiredLights;
        startLightReverter();
    }

    /**
     * Returns the singleton instance of the LightManager class.
     *
     * @return  the singleton instance of the LightManager class
     */
    public static LightManager get() {
        return INSTANCE;
    }

    /**
     * Creates a default waterlogged light data by cloning the default light data and setting the waterlogged property if it implements the Waterlogged interface.
     *
     * @return  the default waterlogged light data
     */
    private BlockData createDefaultWaterloggedLightData() {
        BlockData data = this.defaultLightData.clone();
        if (data instanceof Waterlogged) {
            ((Waterlogged) data).setWaterlogged(true);
        }
        return data;
    }

    /**
     * Adds a light at the specified location with the given brightness, delay, UUID, and ephemeral flag.
     * Subsequent calls to a location with an active light replaces the expiration time with the new delay.
     * This can keep a light on for a location indefinitely with no flickering.
     *
     * @param location   the location where the light should be added
     * @param brightness  the brightness of the light, 1-15
     * @param delay       the delay, or expiry, in milliseconds before the light fades out
     * @param uuid        the UUID of the player associated with the light
     * @param ephemeral   a flag indicating whether the light is visible only to the player referenced before
     */
    public void addLight(Location location, int brightness, long delay, @Nullable UUID uuid, @Nullable Boolean ephemeral) {
        if (!MODERN) return;

        location = location.getBlock().getLocation();
        long expiryTime = System.currentTimeMillis() + delay;

        if (location.getBlock().getLightLevel() >= brightness || (!location.getBlock().isEmpty() && !location.getBlock().getType().equals(Material.WATER))) return;

        LightData newLightData = new LightData(location, brightness, uuid, ephemeral, expiryTime);
        lightMap.put(location, newLightData);

        sendLightChange(location, brightness, uuid, ephemeral);
    }

    /**
     * Starts the light reverter task by scheduling it to run at a fixed rate of 50 milliseconds, or 1 tick.
     * If the scheduler is already shut down, a new ScheduledThreadPoolExecutor is created with the number of available processors.
     */
    private void startLightReverter() {
        if (scheduler.isShutdown()) {
            scheduler = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
        }

        scheduler.scheduleAtFixedRate(reverterTask, 0, 50, TimeUnit.MILLISECONDS);
    }

    /**
     * Reverts all active lights immediately with no fade-out, then restarts the revert scheduler.
     * This does not normally need to be used as it's already called when ProjectKorra is reloaded.
     */
    public void removeAllLights() {
        if (!MODERN) return;

        lightMap.values().forEach(this::revertLight);
        lightMap.clear();
        scheduler.shutdown();

        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        startLightReverter();
    }

    /**
     * Retrieves the current time and iterates over all light data in the light map. If the current time is greater
     * than or equal to the expiry time of a light data, it fades the light out and removes the light data from the map.
     */
    private void revertExpiredLights() {
        if (!MODERN) return;

        long currentTime = System.currentTimeMillis();
        lightMap.values().removeIf(lightData -> {
            if (currentTime >= lightData.expiryTime) {
                fadeLight(lightData);
                return true;
            }
            return false;
        });
    }

    /**
     * Fades out a light by decrementing its brightness by 1. The light will stop fading when its brightness
     * reaches 0. The fade process is scheduled to run every 50 milliseconds, or 1 tick.
     *
     * @param lightData the LightData object containing the light's brightness, location, UUID, and ephemeral flag
     */
    private void fadeLight(LightData lightData) {
        AtomicInteger currentBrightness = new AtomicInteger(lightData.brightness);
        AtomicReference<ScheduledFuture<?>> futureRef = new AtomicReference<>();

        Runnable task = () -> {
            int brightness = currentBrightness.decrementAndGet();
            if (brightness > 0) {
                sendLightChange(lightData.location, brightness, lightData.uuid, lightData.ephemeral);
            } else {
                revertLight(lightData);
                futureRef.get().cancel(false);
            }
        };

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(task, 0, 50, TimeUnit.MILLISECONDS);
        futureRef.set(future);
    }

    /**
     * Sends a block change to the specified location. Brightness of 0 indicates that the light should be reverted.
     *
     * @param  location   the location where the light change is to be sent
     * @param  brightness the brightness level of the light
     * @param  uuid       the UUID of the player associated with the light
     * @param  ephemeral  a flag indicating whether the light is visible only to the player referenced before
     */
    private void sendLightChange(Location location, int brightness, UUID uuid, Boolean ephemeral) {
        BlockData lightData = brightness > 0 ? getLightData(location) : getCurrentBlockData(location);
        lightData = modifyLightLevel(lightData, brightness);

        int viewDistance = Bukkit.getServer().getViewDistance();

        if (ephemeral != null && ephemeral) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.getWorld().equals(location.getWorld()) && player.getLocation().distance(location) <= viewDistance * 16) {
                player.sendBlockChange(location, lightData);
            }
        } else {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getWorld().equals(location.getWorld()) && player.getLocation().distance(location) <= viewDistance * 16) {
                    player.sendBlockChange(location, lightData);
                }
            }
        }
    }

    /**
     * Returns the BlockData as light for the given Location, based on whether the block is water or air.
     *
     * @param  location  the Location to get the BlockData for
     * @return            the BlockData for the given Location
     */
    private BlockData getLightData(Location location) {
        BlockData lightData = location.getBlock().getType() == Material.WATER ? defaultWaterloggedLightData : defaultLightData;
        return lightData.clone();
    }

    /**
     * Returns the BlockData for the given Location, based on the current state of the block.
     * Used to revert lights that have expired.
     *
     * @param  location  the Location to get the BlockData for
     * @return            the BlockData for the given Location
     */
    private BlockData getCurrentBlockData(Location location) {
        return location.getBlock().getBlockData();
    }

    /**
     * Modifies the light level of the given BlockData. If the BlockData implements the Levelled interface,
     * the light level is set to the specified level. Otherwise, the original BlockData is returned unchanged.
     *
     * @param  blockData  the BlockData to modify
     * @param  level      the new light level to set
     * @return            the modified BlockData with the new light level, or the original BlockData if it does not implement Levelled
     */
    private BlockData modifyLightLevel(BlockData blockData, int level) {
        BlockData lightData = blockData.clone();
        if (lightData instanceof Levelled) {
            ((Levelled) lightData).setLevel(level);
        }
        return lightData;
    }

    /**
     * Helper method to revert a light at the specified location by calling sendLightChange with a brightness of 0.
     *
     * @param  lightData  the LightData object containing the location, brightness, UUID, and ephemeral flag of the light to be reverted
     */
    private void revertLight(LightData lightData) {
        sendLightChange(lightData.location, 0, lightData.uuid, lightData.ephemeral);
    }

    private static class LightData {
        final Location location;
        final int brightness;
        final UUID uuid;
        final Boolean ephemeral;
        long expiryTime;

        LightData(Location location, int brightness, UUID uuid, Boolean ephemeral, long expiryTime) {
            this.location = location;
            this.brightness = brightness;
            this.uuid = uuid;
            this.ephemeral = ephemeral;
            this.expiryTime = expiryTime;
        }
    }
}
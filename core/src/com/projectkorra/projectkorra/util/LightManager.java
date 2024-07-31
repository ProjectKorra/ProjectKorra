package com.projectkorra.projectkorra.util;

import com.projectkorra.projectkorra.GeneralMethods;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class LightManager {

    private static final int NUM_LOCKS = 16;
    private final Object[] locks = new Object[NUM_LOCKS];

    private static final LightManager INSTANCE = new LightManager();

    // If this version is pre-LIGHT, this class basically does nothing.
    private static final boolean MODERN = GeneralMethods.getMCVersion() >= 1170;

    // A map containing all active lights
    private final ConcurrentHashMap<Location, ConcurrentSkipListSet<LightData>> lightMap = new ConcurrentHashMap<>();

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
        for (int i = 0; i < NUM_LOCKS; i++) {
            locks[i] = new Object();
        }
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

    private Object getLockForLocation(Location location) {
        return locks[(location.hashCode() & 0x7FFFFFFF) % NUM_LOCKS];
    }

    /**
     * Adds a light at the specified location with the given brightness and delay. Visible for the specified players.
     * Subsequent calls to a location with an active light replaces the expiration time with the new delay.
     * This can keep a light on for a location indefinitely.
     *
     * @param location   the location where the light should be added
     * @param brightness  the brightness of the light, 1-15
     * @param delay       the delay, or expiry, in milliseconds before the light fades out
     * @param observers   the list of players who can see the light
     */
    public void addLight(Location location, int brightness, long delay, Collection<? extends Player> observers) {
        if (!MODERN) return;

        location = location.getBlock().getLocation();
        long expiryTime = System.currentTimeMillis() + delay;

        if (location.getBlock().getLightLevel() >= brightness ||
                (!location.getBlock().isEmpty() && !location.getBlock().getType().equals(Material.WATER))) return;

        LightData newLightData = new LightData(location, brightness, observers, expiryTime);

        Object lock = getLockForLocation(location);
        synchronized (lock) {
            ConcurrentSkipListSet<LightData> existingSet = lightMap.computeIfAbsent(location, loc -> new ConcurrentSkipListSet<>());
            existingSet.removeIf(lightData -> lightData.observers.equals(observers));
            existingSet.add(newLightData);
        }

        sendLightChange(location, brightness, observers);
    }

    /**
     * Adds a light at the specified location with the given brightness and delay. Visible for everyone.
     * Subsequent calls to a location with an active light replaces the expiration time with the new delay.
     * This can keep a light on for a location indefinitely.
     *
     * @param location   the location where the light should be added, will be visible for everyone
     * @param brightness  the brightness of the light, 1-15
     * @param delay       the delay, or expiry, in milliseconds before the light fades out
     */
    public void addLight(Location location, int brightness, long delay) {
        addLight(location, brightness, delay, Bukkit.getOnlinePlayers());
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

        lightMap.values().forEach(set -> set.forEach(this::revertLight));
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
        List<LightData> lightsToRevert = new ArrayList<>();

        lightMap.forEach((location, lightDataSet) -> {
            Iterator<LightData> iterator = lightDataSet.iterator();
            while (iterator.hasNext()) {
                LightData lightData = iterator.next();
                if (currentTime >= lightData.expiryTime) {
                    lightsToRevert.add(lightData);
                    iterator.remove();
                }
            }
            if (lightDataSet.isEmpty()) {
                lightMap.remove(location);
            }
        });

        for (LightData lightData : lightsToRevert) {
            fadeLight(lightData);
        }
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
                sendLightChange(lightData.location, brightness, lightData.observers);
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
     * @param observers   the list of players who can see the light
     */
    private void sendLightChange(Location location, int brightness, Collection<? extends Player> observers) {
        BlockData lightData = brightness > 0 ? getLightData(location) : getCurrentBlockData(location);
        lightData = modifyLightLevel(lightData, brightness);

        int viewDistance = Bukkit.getServer().getViewDistance();

        for (Player player : observers) {
            if (player == null || player.isDead() || !player.isOnline()) continue;
            if (player.getWorld().equals(location.getWorld()) && player.getLocation().distance(location) <= viewDistance * 16) {
                player.sendBlockChange(location, lightData);
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
        sendLightChange(lightData.location, 0, lightData.observers);
    }

    public static class LightData implements Comparable<LightData> {
        private final Location location;
        private final int brightness;
        private final Collection<? extends Player> observers;
        private final long expiryTime;

        public LightData(Location location, int brightness, Collection<? extends Player> observers, long expiryTime) {
            this.location = location;
            this.brightness = brightness;
            this.observers = observers;
            this.expiryTime = expiryTime;
        }

        public Location getLocation() { return location; }
        public int getBrightness() { return brightness; }
        public Collection<? extends Player> getObservers() { return observers; }
        public long getExpiryTime() { return expiryTime; }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            LightData that = (LightData) obj;
            return brightness == that.brightness &&
                    expiryTime == that.expiryTime &&
                    location.equals(that.location) &&
                    observers.equals(that.observers);
        }

        @Override
        public int hashCode() {
            return Objects.hash(location, brightness, observers, expiryTime);
        }

        @Override
        public String toString() {
            return "LightData{" +
                    "location=" + location +
                    ", brightness=" + brightness +
                    ", observers=" + observers +
                    ", expiryTime=" + expiryTime +
                    '}';
        }

        @Override
        public int compareTo(LightData other) {
            return Long.compare(this.expiryTime, other.expiryTime);
        }
    }
}
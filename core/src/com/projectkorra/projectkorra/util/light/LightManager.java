package com.projectkorra.projectkorra.util.light;

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

    private static final LightManager INSTANCE = new LightManager();

    private static final boolean MODERN = GeneralMethods.getMCVersion() >= 1170;

    private final Map<Location, LightData> lightMap = new ConcurrentHashMap<>();
    private ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
    private final BlockData defaultLightData;
    private final BlockData defaultWaterloggedLightData;
    private final Runnable reverterTask;

    public LightManager() {
        this.defaultLightData = Bukkit.createBlockData(Material.valueOf("LIGHT"));
        this.defaultWaterloggedLightData = createDefaultWaterloggedLightData();
        this.reverterTask = this::revertExpiredLights;
        startLightReverter();
    }

    public static LightManager get() {
        return INSTANCE;
    }

    private BlockData createDefaultWaterloggedLightData() {
        BlockData data = this.defaultLightData.clone();
        if (data instanceof Waterlogged) {
            ((Waterlogged) data).setWaterlogged(true);
        }
        return data;
    }

    public void addLight(Location location, int brightness, long delay, UUID uuid, Boolean ephemeral) {
        if (!MODERN) return;

        location = location.getBlock().getLocation();
        long expiryTime = System.currentTimeMillis() + delay;

        if (location.getBlock().getLightLevel() >= brightness || (!location.getBlock().isEmpty() && !location.getBlock().getType().equals(Material.WATER))) return;

        LightData newLightData = new LightData(location, brightness, uuid, ephemeral, expiryTime);
        lightMap.put(location, newLightData);

        sendLightChange(location, brightness, uuid, ephemeral);
    }

    private void startLightReverter() {
        if (scheduler.isShutdown()) {
            scheduler = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
        }

        scheduler.scheduleAtFixedRate(reverterTask, 0, 50, TimeUnit.MILLISECONDS);
    }

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

    private void sendLightChange(Location location, int brightness, UUID uuid, Boolean ephemeral) {
        BlockData lightData = brightness > 0 ? getLightData(location) : getCurrentBlockData(location);
        lightData = modifyLightLevel(lightData, brightness);

        if (ephemeral != null && ephemeral) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.sendBlockChange(location, lightData);
            }
        } else {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendBlockChange(location, lightData);
            }
        }
    }

    private BlockData getLightData(Location location) {
        BlockData lightData = location.getBlock().getType() == Material.WATER ? defaultWaterloggedLightData : defaultLightData;
        return lightData.clone();
    }

    private BlockData getCurrentBlockData(Location location) {
        return location.getBlock().getBlockData();
    }

    private BlockData modifyLightLevel(BlockData blockData, int level) {
        BlockData lightData = blockData.clone();
        if (lightData instanceof Levelled) {
            ((Levelled) lightData).setLevel(level);
        }
        return lightData;
    }

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
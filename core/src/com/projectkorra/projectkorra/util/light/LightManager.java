package com.projectkorra.projectkorra.util.light;

import com.projectkorra.projectkorra.ProjectKorra;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class LightManager {

    private static final LightManager INSTANCE = new LightManager();

    private final DelayQueue<LightData> lightQueue = new DelayQueue<>();
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
        location = location.getBlock().getLocation();
        long expiryTime = System.currentTimeMillis() + delay;

        if (location.getBlock().getLightLevel() >= brightness || (!location.getBlock().isEmpty() && !location.getBlock().getType().equals(Material.WATER))) return;

        LightData newLightData = new LightData(location, brightness, uuid, ephemeral, expiryTime);

        Location finalLocation = location;
        lightQueue.removeIf(lightData -> lightData.location.equals(finalLocation));
        lightQueue.add(newLightData);

        sendLightChange(location, brightness, uuid, ephemeral);
    }

    public void removeAllLights() {
        lightQueue.forEach(this::revertLight);
        lightQueue.clear();
        scheduler.shutdownNow();
        scheduler = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
    }

    private void startLightReverter() {
        scheduler.scheduleAtFixedRate(reverterTask, 0, 50, TimeUnit.MILLISECONDS);
    }

    private void revertExpiredLights() {
        List<LightData> expiredLights = new ArrayList<>();
        LightData expiredLight;
        while ((expiredLight = lightQueue.poll()) != null) {
            expiredLights.add(expiredLight);
        }
        expiredLights.forEach(this::fadeLight);
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

        BlockData finalLightData = lightData;
        Bukkit.getScheduler().runTaskAsynchronously(ProjectKorra.plugin, () -> {
            if (ephemeral != null && ephemeral) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    player.sendBlockChange(location, finalLightData);
                }
            } else {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendBlockChange(location, finalLightData);
                }
            }
        });
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

    private static class LightData implements Delayed {
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

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(expiryTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(@NotNull Delayed o) {
            return Long.compare(this.expiryTime, ((LightData) o).expiryTime);
        }
    }
}

package com.projectkorra.projectkorra.util;

import com.projectkorra.projectkorra.ProjectKorra;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.concurrent.CompletableFuture;

/**
 * PaperLib is a utility class that provides asynchronous methods for chunk
 * loading and teleportation, depending on the server environment (Spigot,
 * Paper, or Folia).
 * <br><br>
 * Instead of using Paper's official (but outdated) PaperLib, this class is
 * an updated version that works with the latest versions of Paper as well as Folia.
 * However, a lot of the methods in PaperLib that aren't required by ProjectKorra were
 * not ported over.
 */
public class PaperLib {

    private static Environment ENVIRONMENT;

    static {
        if (ProjectKorra.isFolia()) {
            ENVIRONMENT = new Folia();
        } else if (ProjectKorra.isPaper()) {
            ENVIRONMENT = new Paper();
        } else {
            ENVIRONMENT = new Spigot();
        }
    }

    /**
     * Asynchronously gets the chunk at the specified location.
     *
     * @param location The location to get the chunk at.
     * @return A CompletableFuture that will complete with the chunk at the
     *         specified location.
     */
    public static CompletableFuture<Chunk> getChunkAtAsync(Location location) {
        return ENVIRONMENT.getChunkAtAsync(location);
    }

    /**
     * Asynchronously gets the chunk at the specified block.
     *
     * @param block The block to get the chunk at.
     * @return A CompletableFuture that will complete with the chunk at the
     *         specified block.
     */
    public static CompletableFuture<Chunk> getChunkAtAsync(Block block) {
        return ENVIRONMENT.getChunkAtAsync(block);
    }

    /**
     * Asynchronously teleports the specified entity to the specified location.
     *
     * @param entity   The entity to teleport.
     * @param location The location to teleport the entity to.
     * @return A CompletableFuture that will complete with true if the teleport
     *         was successful, false otherwise.
     */
    public static CompletableFuture<Boolean> teleportAsync(Entity entity, Location location) {
        return ENVIRONMENT.teleportAsync(entity, location);
    }

    /**
     * Asynchronously teleports the specified entity to the specified location
     * with the specified teleport cause.
     *
     * @param entity The entity to teleport.
     * @param location The location to teleport the entity to.
     * @param cause The cause of the teleportation.
     * @return A CompletableFuture that will complete with true if the teleport
     *         was successful, false otherwise.
     */
    public static CompletableFuture<Boolean> teleportAsync(Entity entity, Location location, PlayerTeleportEvent.TeleportCause cause) {
        return ENVIRONMENT.teleportAsync(entity, location, cause);
    }

    private interface Environment {
        default CompletableFuture<Chunk> getChunkAtAsync(Location location) {
            return getChunkAtAsync(location.getBlock());
        }

        CompletableFuture<Chunk> getChunkAtAsync(Block block);

        default CompletableFuture<Boolean> teleportAsync(Entity entity, Location location) {
            return teleportAsync(entity, location, PlayerTeleportEvent.TeleportCause.PLUGIN);
        }

        CompletableFuture<Boolean> teleportAsync(Entity entity, Location location, PlayerTeleportEvent.TeleportCause cause);
    }

    static class Spigot implements Environment {

        @Override
        public CompletableFuture<Chunk> getChunkAtAsync(Block block) {
            return CompletableFuture.completedFuture(block.getChunk());
        }

        @Override
        public CompletableFuture<Boolean> teleportAsync(Entity entity, Location location, PlayerTeleportEvent.TeleportCause cause) {
            entity.teleport(location, cause);
            return CompletableFuture.completedFuture(true);
        }
    }

    static class Paper implements Environment {

        @Override
        public CompletableFuture<Chunk> getChunkAtAsync(Block block) {
            return block.getWorld().getChunkAtAsync(block);
        }

        @Override
        public CompletableFuture<Boolean> teleportAsync(Entity entity, Location location, PlayerTeleportEvent.TeleportCause cause) {
            return entity.teleportAsync(location, cause);
        }
    }

    static class Folia implements Environment {

        @Override
        public CompletableFuture<Chunk> getChunkAtAsync(Block block) {
            CompletableFuture<Chunk> future = new CompletableFuture<>();
            ThreadUtil.ensureLocation(block.getLocation(), () -> {
                Chunk chunk = block.getWorld().getChunkAt(block);
                future.complete(chunk);
            });
            return future;
        }

        @Override
        public CompletableFuture<Boolean> teleportAsync(Entity entity, Location location, PlayerTeleportEvent.TeleportCause cause) {
            return entity.teleportAsync(location, cause);
        }
    }
}

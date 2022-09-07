package com.projectkorra.projectkorra.region;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.hooks.RegionProtectionHook;
import com.projectkorra.projectkorra.util.BlockCacheElement;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RegionProtection {

    public RegionProtection() {
        if (enabled("WorldGuard")) new WorldGuard();
        if (enabled("Factions")) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("Factions");
            String website = plugin.getDescription().getWebsite();
            if (website != null && website.toLowerCase().contains("factionsuuid")) {
                new FactionsUUID();
            } else {
                new SaberFactions();
            }
        }
        if (enabled("LWC")) new LWC();
        if (enabled("Towny")) new Towny();
        if (enabled("Kingdoms")) new Kingdoms();
        if (enabled("RedProtect")) new RedProtect();
        if (enabled("GriefDefender")) new GriefDefender();
        if (enabled("GriefPrevention")) new GriefPrevention();
        if (enabled("Residence")) new Residence();
        if (enabled("Lands")) new Lands();
    }

    /**
     * Registered region protections
     */
    private static Map<JavaPlugin, RegionProtectionHook> PROTECTIONS = new LinkedHashMap<>(); //LinkedHashMap keeps the hashmap order of insertion

    /**
     * Cached region protection
     */
    private static final Map<String, Map<Block, BlockCacheElement>> BLOCK_CACHE = new ConcurrentHashMap<>();

    /**
     * Register a new type of region protection to respect with bending.
     * @param plugin The plugin the region protection belongs to
     * @param hook The region protection hook
     */
    public static void registerRegionProtection(@NotNull JavaPlugin plugin, @NotNull RegionProtectionHook hook) {
        PROTECTIONS.put(plugin, hook);
    }

    /**
     * Removes region protection for the unloaded plugin.
     * To be called by PK's Listener when a plugin unloads
     * @param plugin The plugin
     */
    public static void unloadPlugin(JavaPlugin plugin) {
        PROTECTIONS.remove(plugin);
    }

    /**
     * Get a list of currently active custom region protections
     * @return Enabled region protections
     */
    public static Collection<RegionProtectionHook> getActiveProtections() {
        return PROTECTIONS.values();
    }

    /**
     * Checks if a location is protected by region protection plugins. Abilities that damage terrain
     * will not damage the terrain (or progress) if this method returns true
     * @param player The player being checked
     * @param location The location to check
     * @param ability The ability to check
     * @return True if the region is protected by other plugins
     */
    public static boolean isRegionProtected(@NotNull Player player, @Nullable Location location, @Nullable CoreAbility ability) {
        if (!BLOCK_CACHE.containsKey(player.getName())) {
            BLOCK_CACHE.put(player.getName(), new ConcurrentHashMap<>());
        }

        final Map<Block, BlockCacheElement> blockMap = BLOCK_CACHE.get(player.getName());
        Block block = player.getLocation().getBlock();
        if (location != null) block = location.getBlock();
        if (blockMap.containsKey(block)) {
            final BlockCacheElement elem = blockMap.get(block);

            // both abilities must be equal to each other to use the cache
            if ((ability == null && elem.getAbility() == null) || (elem.getAbility() != null && elem.getAbility().equals(ability))) {
                return elem.isAllowed();
            }
        }

        final boolean value = isRegionProtectedCached(player, location, ability);
        blockMap.put(block, new BlockCacheElement(player, block, ability, value, System.currentTimeMillis()));
        return value;
    }

    /**
     * Checks if a location is protected by region protection plugins. Abilities that damage terrain
     * will not damage the terrain (or progress) if this method returns true
     * @param player The player being checked
     * @param location The location to check
     * @param ability The ability to check
     * @return True if the region is protected by other plugins
     */
    public static boolean isRegionProtected(@NotNull Player player, @Nullable Location location, @Nullable String ability) {
        return isRegionProtected(player, location, CoreAbility.getAbility(ability));
    }

    /**
     * Checks if a location is protected by region protection plugins. Abilities that damage terrain
     * will not damage the terrain (or progress) if this method returns true
     * @param player The player being checked
     * @param location The location to check
     * @return True if the region is protected by other plugins
     */
    public static boolean isRegionProtected(@NotNull Player player, @Nullable Location location) {
        return isRegionProtected(player, location, (CoreAbility) null);
    }

    /**
     * Checks if a location is protected by region protection plugins. Abilities that damage terrain
     * will not damage the terrain (or progress) if this method returns true
     * @param ability The ability being checked
     * @param location The location to check
     * @return True if the region is protected by other plugins
     */
    public static boolean isRegionProtected(@NotNull CoreAbility ability, @Nullable Location location) {
        return isRegionProtected(ability.getPlayer(), location, ability);
    }


    /**
     * Checks if a location is protected by region protection plugins. Abilities that damage terrain
     * will not damage the terrain (or progress) if this method returns true
     * @param player The player being checked
     * @param ability The ability to check
     * @return True if the region is protected by other plugins
     */
    public static boolean isRegionProtected(@NotNull Player player, @Nullable CoreAbility ability) {
        return isRegionProtected(player, null, ability);
    }

    protected static boolean isRegionProtectedCached(Player player, Location location, CoreAbility ability) {
        if (location != null && checkAll(player, location, ability)) return true;

        return checkAll(player, player.getLocation(), ability);
    }

    private static boolean checkAll(Player player, Location location, CoreAbility ability) {
        for (RegionProtectionHook protection : RegionProtection.getActiveProtections()) {
            try {
                if (protection.isRegionProtected(player, location, ability)) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Internal use only!
     * @param period The time, in milliseconds, to clean the cache
     */
    public static void startCleanCacheTask(double period) {
        Bukkit.getScheduler().runTaskTimer(ProjectKorra.plugin, () -> {
            for (final Map<Block, BlockCacheElement> map : BLOCK_CACHE.values()) {
                for (final Block key : map.keySet()) {
                    final BlockCacheElement value = map.get(key);

                    if (System.currentTimeMillis() - value.getTime() > period) {
                        map.remove(key);
                    }
                }
            }
        }, 0, (long) (period / 50));
    }

    private static boolean enabled(String plugin) {
        return Bukkit.getPluginManager().isPluginEnabled(plugin);
    }
}

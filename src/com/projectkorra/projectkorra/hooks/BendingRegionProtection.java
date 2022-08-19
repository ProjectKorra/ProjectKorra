package com.projectkorra.projectkorra.hooks;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class BendingRegionProtection {

    private static Map<JavaPlugin, BendingRegionProtection> PROTECTIONS = new HashMap<>();

    private boolean registered;

    public abstract JavaPlugin getPlugin();

    public abstract boolean isRegionProtected(Player player, Block block, String ability, boolean harmless, boolean igniteAbility, boolean explodeAbility);

    /**
     * Register this region protection
     */
    public void register() {
        if (!registered) {
            registered = true;

            PROTECTIONS.put(getPlugin(), this);
        }
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
    public static Collection<BendingRegionProtection> getActiveProtections() {
        return PROTECTIONS.values();
    }
}

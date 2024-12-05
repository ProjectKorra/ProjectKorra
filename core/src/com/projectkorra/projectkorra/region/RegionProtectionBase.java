package com.projectkorra.projectkorra.region;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.hooks.RegionProtectionHook;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class RegionProtectionBase implements RegionProtectionHook {

    private String plugin;
    private String path;
    private JavaPlugin cachedPlugin;

    public RegionProtectionBase(String plugin) {
        this(plugin, "Respect" + plugin);
    }

    public RegionProtectionBase(String plugin, String path) {
        this.plugin = plugin;
        this.path = path;

        if (Bukkit.getPluginManager().isPluginEnabled(plugin) && ConfigManager.defaultConfig.get().getBoolean("Properties.RegionProtection." + path)) {
            this.cachedPlugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin(plugin);
            RegionProtection.registerRegionProtection(cachedPlugin, this);
        }
    }

    @Override
    public final boolean isRegionProtected(@NotNull Player player, @NotNull Location location, @Nullable CoreAbility ability) {
        if (ConfigManager.defaultConfig.get().getBoolean("Properties.RegionProtection." + path)) {
            boolean isIgnite = false;
            boolean isExplosive = false;

            if (ability != null) {
                isIgnite = ability.isIgniteAbility();
                isExplosive = ability.isExplosiveAbility();
            }

            return isRegionProtectedReal(player, location, ability, isIgnite, isExplosive);
        }
        return false;
    }

    public abstract boolean isRegionProtectedReal(Player player, Location location, CoreAbility ability, boolean igniteAbility, boolean explosiveAbility);
}
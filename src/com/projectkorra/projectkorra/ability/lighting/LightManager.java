package com.projectkorra.projectkorra.ability.lighting;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LightManager {

    private static ConcurrentHashMap<Block, Light> cache = new ConcurrentHashMap<>();

    private static JavaPlugin owner;
    private static LightManager instance;

    private Illuminator illuminator;
    private String version;

    public LightManager(final JavaPlugin plugin) {
        owner = plugin;
        instance = this;

        if (setupIlluminator()) {
            if (this.version.equals("v_1_17_R1")) illuminator = new Illuminator_1_17_R1();
            plugin.getLogger().info("Using packets on " + this.version + " for ability lighting.");
        } else {
            if (Material.matchMaterial("LIGHT") != null) illuminator = new Illuminator_Bukkit();
            plugin.getLogger().info("Using the Bukkit API as a fallback for ability lighting.");
        }
    }

    public static ConcurrentHashMap<Block, Light> getActiveLights() {
        return cache;
    }

    public static JavaPlugin getPlugin() {
        return owner;
    }

    public Illuminator getIlluminator() {
        return this.illuminator;
    }

    public static LightManager getInstance() {
        return instance;
    }

    private boolean setupIlluminator() {
        String className = Bukkit.getServer().getClass().getPackage().getName();
        this.version = className.substring(className.lastIndexOf('.') + 1);
        return isCompatible(this.version);
    }

    private boolean isCompatible(String v) {
        switch (v) {
            case "v_1_17_R1":
                return true;
            default:
                owner.getLogger().info("Bukkit " + v + " may not be compatible with ability lighting.");
                return false;
        }
    }
}

package com.projectkorra.projectkorra.ability.lighting;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class LightManager {

    public static Set<Block> cache = new HashSet<>();
    public static HashMap<Integer, BlockData> lightData = new HashMap<>();

    public static final BlockFace[] blockFaces = {
            BlockFace.DOWN,
            BlockFace.UP,
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST
    };

    private static JavaPlugin owner;
    private static LightManager instance;

    private Illuminator illuminator;
    private String bukkitVersion;

    public LightManager(final JavaPlugin plugin) {
        owner = plugin;
        instance = this;
        if (checkVersion()) {
            // Uses manually constructed packets. Can also implement other ways, versions, or lightengines, here.
            if (bukkitVersion.equals("v_1_17_R1")) illuminator = new Illuminator_1_17_R1();
            plugin.getLogger().info("Using packets on " + bukkitVersion + " for ability lighting.");
        } else {
            // Use player::sendBlockChange() as a fallback. Not the greatest, but it works.
            if (Material.matchMaterial("LIGHT") != null) {
                illuminator = new Illuminator_Bukkit();
                plugin.getLogger().info("Using the Bukkit API as a fallback for ability lighting.");
            } else {
                return; // This will not work at all without at least having LIGHT available.
            }
        }
        // Store a reference map of every applicable light level and it's respective blockdata.
        for (int i = 0; i < 16; i++) {
            BlockData blockData = Material.valueOf("LIGHT").createBlockData(); // Use Enum LIGHT later.
            ((Levelled) blockData).setLevel(i);
            lightData.put(i, blockData);
        }
    }

    public static JavaPlugin getPlugin() {
        return owner; // Gets the owning plugin, ProjectKorra.
    }

    public static LightManager getInstance() {
        return instance; // Gets the current instance of this manager.
    }

    public Illuminator getIlluminator() {
        return this.illuminator; // This is the class used to emit new lights.
    }

    private boolean checkVersion() {
        String className = Bukkit.getServer().getClass().getPackage().getName();
        this.bukkitVersion = className.substring(className.lastIndexOf('.') + 1);
        return isCompatible(this.bukkitVersion);
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

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
    private Material lightMaterial;

    public LightManager(final JavaPlugin plugin) {
        owner = plugin;
        instance = this;
        if (Material.matchMaterial("LIGHT") != null) lightMaterial = Material.valueOf("LIGHT");
        if (!checkVersion()) return;
        // Store a reference map of every applicable light level and it's respective blockdata.
        for (int i = 0; i < 16; i++) {
            BlockData blockData = lightMaterial.createBlockData(); // Use Enum LIGHT later.
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
        return isCompatible(className.substring(className.lastIndexOf('.') + 1));
    }

    private boolean isCompatible(String v) {
        switch (v) {
            case "v1_18_R1":
                illuminator = new Illuminator_1_18_R1();
                return true;
            default:
                if (lightMaterial == null) return false;
                illuminator = new Illuminator_Bukkit();
                return true;
        }
    }
}

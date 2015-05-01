package com.projectkorra.ProjectKorra.airbending;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

import com.projectkorra.ProjectKorra.Element;
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.waterbending.WaterManipulation;

public class AirBubble {
    
    public static ConcurrentHashMap<Integer, AirBubble> instances = new ConcurrentHashMap<Integer, AirBubble>();
    
    private static double DEFAULT_AIR_RADIUS = ProjectKorra.plugin.getConfig().getDouble("Abilities.Air.AirBubble.Radius");
    private static double DEFAULT_WATER_RADIUS = ProjectKorra.plugin.getConfig().getDouble("Abilities.Water.WaterBubble.Radius");
    
    private Player player;
    private double radius;
    private double defaultAirRadius = DEFAULT_AIR_RADIUS;
    private double defaultWaterRadius = DEFAULT_WATER_RADIUS;
    private ConcurrentHashMap<Block, BlockState> waterorigins;
    
    public AirBubble(Player player) {
        this.player = player;
        waterorigins = new ConcurrentHashMap<Block, BlockState>();
        instances.put(player.getEntityId(), this);
    }
    
    private void pushWater() {
        if (Methods.isBender(player.getName(), Element.Air)) {
            radius = defaultAirRadius;
        } else {
            radius = defaultWaterRadius;
        }
        if (Methods.isBender(player.getName(), Element.Water)
                && Methods.isNight(player.getWorld())) {
            radius = Methods.waterbendingNightAugment(defaultWaterRadius,
                    player.getWorld());
        }
        if (defaultAirRadius > radius
                && Methods.isBender(player.getName(), Element.Air))
            radius = defaultAirRadius;
        Location location = player.getLocation();
        
        for (Block block : waterorigins.keySet()) {
            if (block.getWorld() != location.getWorld()) {
                if (block.getType() == Material.AIR || Methods.isWater(block))
                    waterorigins.get(block).update(true);
                waterorigins.remove(block);
            } else if (block.getLocation().distance(location) > radius) {
                if (block.getType() == Material.AIR || Methods.isWater(block))
                    waterorigins.get(block).update(true);
                waterorigins.remove(block);
            }
        }
        
        for (Block block : Methods.getBlocksAroundPoint(location, radius)) {
            if (waterorigins.containsKey(block))
                continue;
            if (!Methods.isWater(block))
                continue;
            if (Methods.isRegionProtectedFromBuild(player, "AirBubble",
                    block.getLocation()))
                continue;
            if (block.getType() == Material.STATIONARY_WATER
                    || block.getType() == Material.WATER) {
                if (WaterManipulation.canBubbleWater(block)) {
                    waterorigins.put(block, block.getState());
                    block.setType(Material.AIR);
                }
            }
            
        }
        
    }
    
    public boolean progress() {
        if (player.isDead() || !player.isOnline()) {
            removeBubble();
            return false;
        }
        
        if (!player.isSneaking()) {
            removeBubble();
            return false;
        }
        if (Methods.getBoundAbility(player) != null) {
            if (Methods.getBoundAbility(player).equalsIgnoreCase("AirBubble") && Methods.canBend(player.getName(), "AirBubble")) {
                pushWater();
                return true;
            }
            if (Methods.getBoundAbility(player).equalsIgnoreCase("WaterBubble") && Methods.canBend(player.getName(), "WaterBubble")) {
                pushWater();
                return true;
            }
        }
        
        removeBubble();
        return false;
    }
    
    public static void handleBubbles(Server server) {
        
        for (Player player : server.getOnlinePlayers()) {
            if (Methods.getBoundAbility(player) != null) {
                if (Methods.getBoundAbility(player).equalsIgnoreCase("AirBubble") || Methods.getBoundAbility(player).equalsIgnoreCase("WaterBubble")) {
                    if (!instances.containsKey(player.getEntityId()) && player.isSneaking()) {
                        new AirBubble(player);
                    }
                }
            }
        }
        
        for (int ID : instances.keySet()) {
            progress(ID);
        }
    }
    
    private void removeBubble() {
        for (Block block : waterorigins.keySet()) {
            // byte data = waterorigins.get(block);
            // byte data = 0x0;
            // block = block.getLocation().getBlock();
            // if (block.getType() == Material.AIR) {
            // block.setType(Material.WATER);
            // block.setData(data);
            // }
            if (block.getType() == Material.AIR || block.isLiquid())
                waterorigins.get(block).update(true);
        }
        instances.remove(player.getEntityId());
    }
    
    public static boolean progress(int ID) {
        return instances.get(ID).progress();
    }
    
    public boolean blockInBubble(Block block) {
        if (block.getWorld() != player.getWorld()) {
            return false;
        }
        if (block.getLocation().distance(player.getLocation()) <= radius) {
            return true;
        }
        return false;
    }
    
    public static boolean canFlowTo(Block block) {
        for (int ID : instances.keySet()) {
            if (instances.get(ID).blockInBubble(block)) {
                return false;
            }
        }
        return true;
    }
    
    public static void removeAll() {
        for (int id : instances.keySet()) {
            instances.get(id).removeBubble();
        }
    }
    
    public static String getDescription() {
        return "To use, the bender must merely have the ability selected."
                + " All water around the user in a small bubble will vanish,"
                + " replacing itself once the user either gets too far away or selects a different ability.";
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public double getRadius() {
        return radius;
    }
    
    public void setRadius(double radius) {
        this.radius = radius;
    }
    
    public double getDefaultAirRadius() {
        return defaultAirRadius;
    }
    
    public void setDefaultAirRadius(double defaultAirRadius) {
        this.defaultAirRadius = defaultAirRadius;
    }
    
    public double getDefaultWaterRadius() {
        return defaultWaterRadius;
    }
    
    public void setDefaultWaterRadius(double defaultWaterRadius) {
        this.defaultWaterRadius = defaultWaterRadius;
    }
    
}
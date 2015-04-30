package com.projectkorra.ProjectKorra.earthbending;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;

public class EarthTunnel {
    
    public static ConcurrentHashMap<Player, EarthTunnel> instances = new ConcurrentHashMap<Player, EarthTunnel>();
    
    private static final double MAX_RADIUS = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.EarthTunnel.MaxRadius");
    private static final double RANGE = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.EarthTunnel.Range");
    private static final double RADIUS = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.EarthTunnel.Radius");
    
    private static boolean revert = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Earth.EarthTunnel.Revert");
    private static final long INTERVAL = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.EarthTunnel.Interval");
    
    private Player player;
    private Block block;
    private Location origin, location;
    private Vector direction;
    private double depth, radius, angle;
    private double maxradius = MAX_RADIUS;
    private double range = RANGE;
    private double radiusinc = RADIUS;
    private long interval = INTERVAL;
    private long time;
    
    public EarthTunnel(Player player) {
        this.player = player;
        location = player.getEyeLocation().clone();
        origin = player.getTargetBlock((HashSet<Material>) null, (int) range).getLocation();
        block = origin.getBlock();
        direction = location.getDirection().clone().normalize();
        depth = origin.distance(location) - 1;
        if (depth < 0)
            depth = 0;
        angle = 0;
        radius = radiusinc;
        time = System.currentTimeMillis();
        
        instances.put(player, this);
    }
    
    private boolean progress() {
        if (player.isDead() || !player.isOnline()) {
            instances.remove(player);
            return false;
        }
        if (System.currentTimeMillis() - time >= interval) {
            time = System.currentTimeMillis();
            if (Math.abs(Math.toDegrees(player.getEyeLocation().getDirection()
                    .angle(direction))) > 20
                    || !player.isSneaking()) {
                instances.remove(player);
                return false;
            } else {
                while (!Methods.isEarthbendable(player, block)) {
                    if (!Methods.isTransparentToEarthbending(player, block)) {
                        instances.remove(player);
                        return false;
                    }
                    if (angle >= 360) {
                        angle = 0;
                        if (radius >= maxradius) {
                            radius = radiusinc;
                            if (depth >= range) {
                                instances.remove(player);
                                return false;
                            } else {
                                depth += .5;
                            }
                        } else {
                            radius += radiusinc;
                        }
                    } else {
                        angle += 20;
                    }
                    Vector vec = Methods.getOrthogonalVector(direction, angle,
                            radius);
                    block = location.clone()
                            .add(direction.clone().normalize().multiply(depth))
                            .add(vec).getBlock();
                }
                
                if (revert) {
                    Methods.addTempAirBlock(block);
                } else {
                    block.breakNaturally();
                }
                
                return true;
            }
        } else {
            return false;
        }
    }
    
    public static void progressAll() {
        for (Player player : instances.keySet()) {
            instances.get(player).progress();
        }
    }
    
    public static String getDescription() {
        return "Earth Tunnel is a completely utility ability for earthbenders. "
                + "To use, simply sneak (default: shift) in the direction you want to tunnel. "
                + "You will slowly begin tunneling in the direction you're facing for as long as you "
                + "sneak or if the tunnel has been dug long enough. This ability will be interupted "
                + "if it hits a block that cannot be earthbent.";
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public double getMaxradius() {
        return maxradius;
    }
    
    public void setMaxradius(double maxradius) {
        this.maxradius = maxradius;
    }
    
    public double getRange() {
        return range;
    }
    
    public void setRange(double range) {
        this.range = range;
    }
    
    public double getRadiusinc() {
        return radiusinc;
    }
    
    public void setRadiusinc(double radiusinc) {
        this.radiusinc = radiusinc;
    }
    
    public long getInterval() {
        return interval;
    }
    
    public void setInterval(long interval) {
        this.interval = interval;
    }
    
}
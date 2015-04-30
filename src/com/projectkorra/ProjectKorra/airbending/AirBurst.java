package com.projectkorra.ProjectKorra.airbending;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AvatarState;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class AirBurst {
    
    private static ConcurrentHashMap<Player, AirBurst> instances = new ConcurrentHashMap<Player, AirBurst>();
    private static double PARTICLES_PERCENTAGE = 50;
    
    static FileConfiguration config = ProjectKorra.plugin.getConfig();
    
    private static double threshold = config.getDouble("Abilities.Air.AirBurst.FallThreshold");
    private static double pushfactor = config.getDouble("Abilities.Air.AirBurst.PushFactor");
    private static double damage = config.getDouble("Abilities.Air.AirBurst.Damage");
    private static double deltheta = 10;
    private static double delphi = 10;
    
    private Player player;
    private long starttime;
    private long chargetime = config.getLong("Abilities.Air.AirBurst.ChargeTime");
    private boolean charged = false;
    public ArrayList<AirBlast> blasts = new ArrayList<AirBlast>();
    private ArrayList<Entity> affectedentities = new ArrayList<Entity>();
    
    public AirBurst(Player player) {
        BendingPlayer bPlayer = Methods.getBendingPlayer(player.getName());
        if (bPlayer.isOnCooldown("AirBurst"))
            return;
        if (instances.containsKey(player))
            return;
        starttime = System.currentTimeMillis();
        if (AvatarState.isAvatarState(player))
            chargetime = 0;
        this.player = player;
        instances.put(player, this);
    }
    
    public AirBurst() {
        
    }
    
    public static void coneBurst(Player player) {
        if (instances.containsKey(player))
            instances.get(player).coneBurst();
    }
    
    private void coneBurst() {
        if (charged) {
            Location location = player.getEyeLocation();
            Vector vector = location.getDirection();
            double angle = Math.toRadians(30);
            double x, y, z;
            double r = 1;
            for (double theta = 0; theta <= 180; theta += deltheta) {
                double dphi = delphi / Math.sin(Math.toRadians(theta));
                for (double phi = 0; phi < 360; phi += dphi) {
                    double rphi = Math.toRadians(phi);
                    double rtheta = Math.toRadians(theta);
                    x = r * Math.cos(rphi) * Math.sin(rtheta);
                    y = r * Math.sin(rphi) * Math.sin(rtheta);
                    z = r * Math.cos(rtheta);
                    Vector direction = new Vector(x, z, y);
                    if (direction.angle(vector) <= angle) {
                        AirBlast blast = new AirBlast(location, direction.normalize(), player,
                                pushfactor, this);
                        blast.setDamage(damage);
                    }
                }
            }
        }
        instances.remove(player);
    }
    
    private void sphereBurst() {
        if (charged) {
            Location location = player.getEyeLocation();
            double x, y, z;
            double r = 1;
            for (double theta = 0; theta <= 180; theta += deltheta) {
                double dphi = delphi / Math.sin(Math.toRadians(theta));
                for (double phi = 0; phi < 360; phi += dphi) {
                    double rphi = Math.toRadians(phi);
                    double rtheta = Math.toRadians(theta);
                    x = r * Math.cos(rphi) * Math.sin(rtheta);
                    y = r * Math.sin(rphi) * Math.sin(rtheta);
                    z = r * Math.cos(rtheta);
                    Vector direction = new Vector(x, z, y);
                    AirBlast blast = new AirBlast(location, direction.normalize(), player,
                            pushfactor, this);
                    blast.setDamage(damage);
                    blast.setShowParticles(false);
                    blasts.add(blast);
                }
            }
        }
        // Methods.verbose("--" + AirBlast.instances.size() + "--");
        instances.remove(player);
        handleSmoothParticles();
    }
    
    public static void fallBurst(Player player) {
        if (!Methods.canBend(player.getName(), "AirBurst")) {
            return;
        }
        if (player.getFallDistance() < threshold) {
            return;
        }
        if (Methods.getBoundAbility(player) == null) {
            return;
        }
        if (instances.containsKey(player)) {
            return;
        }
        if (!Methods.getBoundAbility(player).equalsIgnoreCase("AirBurst")) {
            return;
        }
        
        Location location = player.getLocation();
        double x, y, z;
        double r = 1;
        for (double theta = 75; theta < 105; theta += deltheta) {
            double dphi = delphi / Math.sin(Math.toRadians(theta));
            for (double phi = 0; phi < 360; phi += dphi) {
                double rphi = Math.toRadians(phi);
                double rtheta = Math.toRadians(theta);
                x = r * Math.cos(rphi) * Math.sin(rtheta);
                y = r * Math.sin(rphi) * Math.sin(rtheta);
                z = r * Math.cos(rtheta);
                Vector direction = new Vector(x, z, y);
                AirBlast blast = new AirBlast(location, direction.normalize(), player,
                        pushfactor, new AirBurst());
                blast.setDamage(damage);
            }
        }
    }
    
    void addAffectedEntity(Entity entity) {
        affectedentities.add(entity);
    }
    
    boolean isAffectedEntity(Entity entity) {
        return affectedentities.contains(entity);
    }
    
    private void progress() {
        if (!Methods.canBend(player.getName(), "AirBurst")) {
            instances.remove(player);
            return;
        }
        if (Methods.getBoundAbility(player) == null) {
            instances.remove(player);
            return;
        }
        
        if (!Methods.getBoundAbility(player).equalsIgnoreCase("AirBurst")) {
            instances.remove(player);
            return;
        }
        
        if (System.currentTimeMillis() > starttime + chargetime && !charged) {
            charged = true;
        }
        
        if (!player.isSneaking()) {
            if (charged) {
                sphereBurst();
            } else {
                instances.remove(player);
            }
        } else if (charged) {
            Location location = player.getEyeLocation();
            // location = location.add(location.getDirection().normalize());
            Methods.playAirbendingParticles(location, 10);
            // location.getWorld().playEffect(
            // location,
            // Effect.SMOKE,
            // Methods.getIntCardinalDirection(player.getEyeLocation()
            // .getDirection()), 3);
        }
    }
    
    public void handleSmoothParticles() {
        for (int i = 0; i < blasts.size(); i++) {
            final AirBlast blast = blasts.get(i);
            int toggleTime = 0;
            if (i % 4 != 0)
                toggleTime = (int) (i % (100 / PARTICLES_PERCENTAGE)) + 3;
            new BukkitRunnable() {
                public void run() {
                    blast.setShowParticles(true);
                }
            }.runTaskLater(ProjectKorra.plugin, toggleTime);
        }
    }
    
    public static void progressAll() {
        for (Player player : instances.keySet())
            instances.get(player).progress();
    }
    
    public static void removeAll() {
        instances.clear();
        
    }
}
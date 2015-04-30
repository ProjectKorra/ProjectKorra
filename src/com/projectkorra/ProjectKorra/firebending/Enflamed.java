package com.projectkorra.ProjectKorra.firebending;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class Enflamed {
    
    private static ConcurrentHashMap<Entity, Player> instances = new ConcurrentHashMap<Entity, Player>();
    private static ConcurrentHashMap<Entity, Long> times = new ConcurrentHashMap<Entity, Long>();
    
    private static final int damage = 1;
    private static final int max = 90;
    private static final long buffer = 30;
    
    public Enflamed(Entity entity, Player source) {
        if (entity.getEntityId() == source.getEntityId())
            return;
        if (instances.containsKey(entity)) {
            instances.replace(entity, source);
        } else {
            instances.put(entity, source);
        }
    }
    
    public static boolean isEnflamed(Entity entity) {
        // return false;
        if (instances.containsKey(entity)) {
            if (times.containsKey(entity)) {
                long time = times.get(entity);
                if (System.currentTimeMillis() < time + buffer) {
                    return false;
                }
            }
            times.put(entity, System.currentTimeMillis());
            return true;
        } else {
            return false;
        }
    }
    
    public static void dealFlameDamage(Entity entity) {
        if (instances.containsKey(entity) && entity instanceof LivingEntity) {
            if (entity instanceof Player) {
                if (!Extinguish.canBurn((Player) entity)) {
                    return;
                }
            }
            LivingEntity Lentity = (LivingEntity) entity;
            Player source = instances.get(entity);
            Lentity.damage(damage, source);
            if (entity.getFireTicks() > max)
                entity.setFireTicks(max);
        }
    }
    
    public static void handleFlames() {
        for (Entity entity : instances.keySet()) {
            if (entity.getFireTicks() <= 0) {
                instances.remove(entity);
            }
        }
    }
    
}
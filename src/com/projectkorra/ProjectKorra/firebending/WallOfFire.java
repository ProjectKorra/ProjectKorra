package com.projectkorra.ProjectKorra.firebending;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.Utilities.ParticleEffect;

public class WallOfFire {
    
    private Player player;
    
    private static double maxangle = 50;
    
    public static FileConfiguration config = ProjectKorra.plugin.getConfig();
    private static int RANGE = config.getInt("Abilities.Fire.WallOfFire.Range");
    private int HEIGHT = config.getInt("Abilities.Fire.WallOfFire.Height");
    private int WIDTH = config.getInt("Abilities.Fire.WallOfFire.Width");
    private long DURATION = config.getLong("Abilities.Fire.WallOfFire.Duration");
    private int DAMAGE = config.getInt("Abilities.Fire.WallOfFire.Damage");
    private static long interval = 250;
    private static long COOLDOWN = config.getLong("Abilities.Fire.WallOfFire.Cooldown");
    public static ConcurrentHashMap<Player, WallOfFire> instances = new ConcurrentHashMap<Player, WallOfFire>();
    private static long DAMAGE_INTERVAL = config.getLong("Abilities.Fire.WallOfFire.Interval");
    
    private Location origin;
    private long time, starttime;
    private boolean active = true;
    private int damagetick = 0, intervaltick = 0;
    private int range = RANGE;
    private int height = HEIGHT;
    private int width = WIDTH;
    private long duration = DURATION;
    private int damage = DAMAGE;
    private long cooldown = COOLDOWN;
    private long damageinterval = DAMAGE_INTERVAL;
    private List<Block> blocks = new ArrayList<Block>();
    
    public WallOfFire(Player player) {
        if (instances.containsKey(player) && !AvatarState.isAvatarState(player)) {
            return;
        }
        
        BendingPlayer bPlayer = Methods.getBendingPlayer(player.getName());
        
        if (bPlayer.isOnCooldown("WallOfFire"))
            return;
        
        this.player = player;
        
        origin = Methods.getTargetedLocation(player, range);
        
        World world = player.getWorld();
        
        if (Methods.isDay(player.getWorld())) {
            width = (int) Methods.getFirebendingDayAugment((double) width, world);
            height = (int) Methods.getFirebendingDayAugment((double) height, world);
            duration = (long) Methods.getFirebendingDayAugment((double) duration,
                    world);
            damage = (int) Methods.getFirebendingDayAugment((double) damage, world);
        }
        
        time = System.currentTimeMillis();
        starttime = time;
        
        Block block = origin.getBlock();
        
        if (block.isLiquid() || Methods.isSolid(block)) {
            return;
        }
        
        Vector direction = player.getEyeLocation().getDirection();
        Vector compare = direction.clone();
        compare.setY(0);
        
        if (Math.abs(direction.angle(compare)) > Math.toRadians(maxangle)) {
            return;
        }
        
        initializeBlocks();
        
        instances.put(player, this);
        bPlayer.addCooldown("WallOfFire", cooldown);
    }
    
    private void progress() {
        time = System.currentTimeMillis();
        
        if (time - starttime > cooldown) {
            instances.remove(player);
            return;
        }
        
        if (!active)
            return;
        
        if (time - starttime > duration) {
            active = false;
            return;
        }
        
        if (time - starttime > intervaltick * interval) {
            intervaltick++;
            display();
        }
        
        if (time - starttime > damagetick * damageinterval) {
            damagetick++;
            damage();
        }
        
    }
    
    private void initializeBlocks() {
        Vector direction = player.getEyeLocation().getDirection();
        direction = direction.normalize();
        
        Vector ortholr = Methods.getOrthogonalVector(direction, 0, 1);
        ortholr = ortholr.normalize();
        
        Vector orthoud = Methods.getOrthogonalVector(direction, 90, 1);
        orthoud = orthoud.normalize();
        
        double w = (double) width;
        double h = (double) height;
        
        for (double i = -w; i <= w; i++) {
            for (double j = -h; j <= h; j++) {
                Location location = origin.clone().add(
                        orthoud.clone().multiply(j));
                location = location.add(ortholr.clone().multiply(i));
                if (Methods.isRegionProtectedFromBuild(player,
                        "WallOfFire", location))
                    continue;
                Block block = location.getBlock();
                if (!blocks.contains(block))
                    blocks.add(block);
            }
        }
        
    }
    
    private void display() {
        for (Block block : blocks) {
            ParticleEffect.FLAME.display(block.getLocation(), 0.6F, 0.6F, 0.6F, 0, 6);
            ParticleEffect.SMOKE.display(block.getLocation(), 0.6F, 0.6F, 0.6F, 0, 6);
            
            if (Methods.rand.nextInt(7) == 0) {
                Methods.playFirebendingSound(block.getLocation());
            }
        }
    }
    
    private void damage() {
        double radius = height;
        if (radius < width)
            radius = width;
        radius = radius + 1;
        List<Entity> entities = Methods.getEntitiesAroundPoint(origin, radius);
        if (entities.contains(player))
            entities.remove(player);
        for (Entity entity : entities) {
            if (Methods.isRegionProtectedFromBuild(player, "WallOfFire",
                    entity.getLocation()))
                continue;
            for (Block block : blocks) {
                if (entity.getLocation().distance(block.getLocation()) <= 1.5) {
                    affect(entity);
                    break;
                }
            }
        }
    }
    
    private void affect(Entity entity) {
        entity.setFireTicks(50);
        Methods.setVelocity(entity, new Vector(0, 0, 0));
        if (entity instanceof LivingEntity) {
            Methods.damageEntity(player, entity, damage);
            new Enflamed(entity, player);
            Methods.breakBreathbendingHold(entity);
        }
    }
    
    public static void manage() {
        for (Player player : instances.keySet()) {
            instances.get(player).progress();
        }
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public int getRange() {
        return range;
    }
    
    public void setRange(int range) {
        this.range = range;
    }
    
    public int getHeight() {
        return height;
    }
    
    public void setHeight(int height) {
        this.height = height;
    }
    
    public int getWidth() {
        return width;
    }
    
    public void setWidth(int width) {
        this.width = width;
    }
    
    public long getDuration() {
        return duration;
    }
    
    public void setDuration(long duration) {
        this.duration = duration;
    }
    
    public int getDamage() {
        return damage;
    }
    
    public void setDamage(int damage) {
        this.damage = damage;
    }
    
    public long getCooldown() {
        return cooldown;
    }
    
    public void setCooldown(long cooldown) {
        this.cooldown = cooldown;
        if (player != null)
            Methods.getBendingPlayer(player.getName()).addCooldown("WallOfFire", cooldown);
    }
    
    public long getDamageinterval() {
        return damageinterval;
    }
    
    public void setDamageinterval(long damageinterval) {
        this.damageinterval = damageinterval;
    }
}

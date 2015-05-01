package com.projectkorra.ProjectKorra.firebending;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.Utilities.ParticleEffect;
import com.projectkorra.ProjectKorra.earthbending.EarthBlast;
import com.projectkorra.ProjectKorra.waterbending.Plantbending;
import com.projectkorra.ProjectKorra.waterbending.WaterManipulation;

public class FireBlast {
    
    Random rand = new Random();
    
    public static ConcurrentHashMap<Integer, FireBlast> instances = new ConcurrentHashMap<Integer, FireBlast>();
    private static double SPEED = ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.FireBlast.Speed");
    private static double PUSH_FACTOR = ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.FireBlast.Push");
    private static double RANGE = ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.FireBlast.Range");
    static boolean dissipate = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Fire.FireBlast.Dissipate");
    private static int DAMAGE = ProjectKorra.plugin.getConfig().getInt("Abilities.Fire.FireBlast.Damage");
    
    long cooldown = ProjectKorra.plugin.getConfig().getLong("Abilities.Fire.FireBlast.Cooldown");
    
    public static double AFFECTING_RADIUS = 2;
    // public static long interval = 2000;
    public static byte full = 0x0;
    private static int ID = Integer.MIN_VALUE;
    private static boolean canPowerFurnace = true;
    static final int maxticks = 10000;
    
    public Location location;
    private List<Block> safe = new ArrayList<Block>();
    private Location origin;
    private Vector direction;
    private Player player;
    private int id;
    private double speedfactor;
    private int ticks = 0;
    private double range = RANGE;
    private double damage = DAMAGE;
    private double speed = SPEED;
    private double pushfactor = PUSH_FACTOR;
    private double affectingradius = AFFECTING_RADIUS;
    private boolean showParticles = true;
    
    public FireBlast(Player player) {
        BendingPlayer bPlayer = Methods.getBendingPlayer(player.getName());
        
        if (bPlayer.isOnCooldown("FireBlast"))
            return;
        
        if (player.getEyeLocation().getBlock().isLiquid() || Fireball.isCharging(player)) {
            return;
        }
        range = Methods.getFirebendingDayAugment(range, player.getWorld());
        this.player = player;
        location = player.getEyeLocation();
        origin = player.getEyeLocation();
        direction = player.getEyeLocation().getDirection().normalize();
        location = location.add(direction.clone());
        id = ID;
        instances.put(id, this);
        bPlayer.addCooldown("FireBlast", cooldown);
        if (ID == Integer.MAX_VALUE)
            ID = Integer.MIN_VALUE;
        ID++;
        // time = System.currentTimeMillis();
        // timers.put(player, System.currentTimeMillis());
    }
    
    public FireBlast(Location location, Vector direction, Player player, int damage, List<Block> safeblocks) {
        if (location.getBlock().isLiquid()) {
            return;
        }
        safe = safeblocks;
        range = Methods.getFirebendingDayAugment(range, player.getWorld());
        // timers.put(player, System.currentTimeMillis());
        this.player = player;
        this.location = location.clone();
        origin = location.clone();
        this.direction = direction.clone().normalize();
        this.damage *= 1.5;
        id = ID;
        instances.put(id, this);
        if (ID == Integer.MAX_VALUE)
            ID = Integer.MIN_VALUE;
        ID++;
    }
    
    public boolean progress() {
        if (player.isDead() || !player.isOnline()) {
            instances.remove(id);
            return false;
        }
        
        if (Methods.isRegionProtectedFromBuild(player, "Blaze", location)) {
            instances.remove(id);
            return false;
        }
        
        speedfactor = speed * (ProjectKorra.time_step / 1000.);
        
        ticks++;
        
        if (ticks > maxticks) {
            instances.remove(id);
            return false;
        }
        
        Block block = location.getBlock();
        
        if (Methods.isSolid(block) || block.isLiquid()) {
            if (block.getType() == Material.FURNACE && canPowerFurnace) {
                Furnace furnace = (Furnace) block.getState();
                furnace.setBurnTime((short) 800);
                furnace.setCookTime((short) 800);
                furnace.update();
            } else if (FireStream.isIgnitable(player, block.getRelative(BlockFace.UP))) {
                ignite(location);
            }
            instances.remove(id);
            return false;
        }
        
        if (location.distance(origin) > range) {
            instances.remove(id);
            return false;
        }
        
        Methods.removeSpouts(location, player);
        
        double radius = affectingradius;
        Player source = player;
        if (EarthBlast.annihilateBlasts(location, radius, source)
                || WaterManipulation.annihilateBlasts(location, radius, source)
                || FireBlast.annihilateBlasts(location, radius, source)) {
            instances.remove(id);
            return false;
        }
        
        for (Entity entity : Methods.getEntitiesAroundPoint(location, affectingradius)) {
            // Block bblock = location.getBlock();
            // Block block1 = entity.getLocation().getBlock();
            // if (bblock.equals(block1))
            affect(entity);
            if (entity instanceof LivingEntity) {
                // Block block2 = ((LivingEntity) entity).getEyeLocation()
                // .getBlock();
                // if (bblock.equals(block1))
                // break;
                // if (bblock.equals(block2)) {
                // affect(entity);
                break;
                // }
            }
        }
        
        advanceLocation();
        
        return true;
    }
    
    private void advanceLocation() {
        if (showParticles) {
            // ParticleEffect.RED_DUST.display((float) 16, (float) 111, (float)
            // 227, 0.01F, 0, location, 256D);
            ParticleEffect.FLAME.display(location, 0.6F, 0.6F, 0.6F, 0, 20);
            ParticleEffect.SMOKE.display(location, 0.6F, 0.6F, 0.6F, 0, 20);
        }
        location = location.add(direction.clone().multiply(speedfactor));
        if (rand.nextInt(4) == 0) {
            Methods.playFirebendingSound(location);
        }
    }
    
    private void ignite(Location location) {
        for (Block block : Methods.getBlocksAroundPoint(location, affectingradius)) {
            if (FireStream.isIgnitable(player, block) && !safe.contains(block)) {
                if (Methods.isPlant(block))
                    new Plantbending(block);
                block.setType(Material.FIRE);
                if (dissipate) {
                    FireStream.ignitedblocks.put(block, player);
                    FireStream.ignitedtimes.put(block, System.currentTimeMillis());
                }
            }
        }
    }
    
    public void setDamage(double dmg) {
        this.damage = dmg;
    }
    
    public void setRange(double range) {
        this.range = range;
    }
    
    public void setShowParticles(boolean show) {
        this.showParticles = show;
    }
    
    public static boolean progress(int ID) {
        if (instances.containsKey(ID))
            return instances.get(ID).progress();
        return false;
    }
    
    public static void progressAll() {
        for (int id : instances.keySet()) {
            progress(id);
        }
    }
    
    private void affect(Entity entity) {
        if (entity.getEntityId() != player.getEntityId()) {
            if (AvatarState.isAvatarState(player)) {
                Methods.setVelocity(entity, direction.clone().multiply(AvatarState.getValue(pushfactor)));
            } else {
                Methods.setVelocity(entity, direction.clone().multiply(pushfactor));
            }
            if (entity instanceof LivingEntity) {
                entity.setFireTicks(50);
                Methods.damageEntity(player, entity, (int) Methods.getFirebendingDayAugment((double) damage, entity.getWorld()));
                Methods.breakBreathbendingHold(entity);
                new Enflamed(entity, player);
                instances.remove(id);
            }
        }
    }
    
    public static void removeFireBlastsAroundPoint(Location location, double radius) {
        for (int id : instances.keySet()) {
            Location fireblastlocation = instances.get(id).location;
            if (location.getWorld() == fireblastlocation.getWorld()) {
                if (location.distance(fireblastlocation) <= radius)
                    instances.remove(id);
            }
        }
        Fireball.removeFireballsAroundPoint(location, radius);
    }
    
    public static ArrayList<FireBlast> getAroundPoint(Location location, double radius) {
        ArrayList<FireBlast> list = new ArrayList<FireBlast>();
        for (int id : instances.keySet()) {
            Location fireblastlocation = instances.get(id).location;
            if (location.getWorld() == fireblastlocation.getWorld()) {
                if (location.distance(fireblastlocation) <= radius) {
                    list.add(instances.get(id));
                }
            }
        }
        return list;
    }
    
    public static boolean annihilateBlasts(Location location, double radius, Player source) {
        boolean broke = false;
        for (int id : instances.keySet()) {
            FireBlast blast = instances.get(id);
            Location fireblastlocation = blast.location;
            if (location.getWorld() == fireblastlocation.getWorld() && !blast.player.equals(source)) {
                if (location.distance(fireblastlocation) <= radius) {
                    instances.remove(id);
                    broke = true;
                }
            }
        }
        if (Fireball.annihilateBlasts(location, radius, source))
            broke = true;
        return broke;
    }
    
    public static void removeAll() {
        for (int id : instances.keySet()) {
            instances.remove(id);
        }
    }
    
    public static String getDescription() {
        return "FireBlast is the most fundamental bending technique of a firebender. "
                + "To use, simply left-click in a direction. A blast of fire will be created at your fingertips. "
                + "If this blast contacts an enemy, it will dissipate and engulf them in flames, "
                + "doing additional damage and knocking them back slightly. "
                + "If the blast hits terrain, it will ignite the nearby area. "
                + "Additionally, if you hold sneak, you will charge up the fireblast. "
                + "If you release it when it's charged, it will instead launch a powerful "
                + "fireball that explodes on contact.";
    }
    
    public long getCooldown() {
        return cooldown;
    }
    
    public void setCooldown(long cooldown) {
        this.cooldown = cooldown;
        if (player != null)
            Methods.getBendingPlayer(player.getName()).addCooldown("FireBlast", cooldown);
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public double getSpeed() {
        return speed;
    }
    
    public void setSpeed(double speed) {
        this.speed = speed;
    }
    
    public double getPushfactor() {
        return pushfactor;
    }
    
    public void setPushfactor(double pushfactor) {
        this.pushfactor = pushfactor;
    }
    
    public double getAffectingradius() {
        return affectingradius;
    }
    
    public void setAffectingradius(double affectingradius) {
        this.affectingradius = affectingradius;
    }
    
    public double getRange() {
        return range;
    }
    
    public double getDamage() {
        return damage;
    }
    
}
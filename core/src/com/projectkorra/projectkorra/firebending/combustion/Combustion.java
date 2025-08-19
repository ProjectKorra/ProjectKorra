package com.projectkorra.projectkorra.firebending.combustion;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CombustionAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.SubAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.attribute.markers.DayNightFactor;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.util.TempFallingBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Combustion extends CombustionAbility implements SubAbility {

    /* Visual */
    private float sonicBoomSpeed;
    private int sonicBoomParticleCount;
    private int sonicBoomFrequency;
    private double sonicBoomRadius;

    /* PVP */
    @Attribute(Attribute.SPEED)
    @DayNightFactor
    private int speed;
    @Attribute("Weight")
    private int weight;
    @Attribute(Attribute.COOLDOWN)
    @DayNightFactor(invert = true)
    private long cooldown;
    @Attribute(Attribute.DAMAGE)
    @DayNightFactor
    private double damage;
    @Attribute("WaterDamage")
    private double waterDamage;
    @Attribute(Attribute.RADIUS)
    @DayNightFactor
    private double explosionRadius;
    @Attribute(Attribute.RANGE)
    @DayNightFactor
    private double range;
    @Attribute(Attribute.CHARGE_DURATION)
    @DayNightFactor
    private long chargeDuration;
    @Attribute("BurnTime")
    @DayNightFactor
    private int burnTime;
    private boolean punishPlayer;
    long explosionRevertTime;

    /* Non-config variables */
    int chargeI = 0;
    private boolean forceRelease;
    private boolean needCharge, canCharge;
    private boolean thrown;
    private double increment;
    private double increasing;
    public static HashMap<Player, Boolean> damaged = new HashMap<Player, Boolean>();
    public static HashMap<Player, Boolean> clicked = new HashMap<Player, Boolean>();
    Location location;
    int advancementSteps = 0;
    private static List<Material> immuneBlocks = new ArrayList<>();

    public Combustion(Player player) {
        super(player);
        if (bPlayer.canBend(this) && !hasAbility(player, Combustion.class)) {
            setFields();
            setCollisions();
            start();
        }
    }

    private void setFields() {
        cooldown = ConfigManager.getConfig().getLong("Abilities.Fire.Combustion.Cooldown");
        chargeDuration = ConfigManager.getConfig().getLong("Abilities.Fire.Combustion.ChargeDuration");
        burnTime = ConfigManager.getConfig().getInt("Abilities.Fire.Combustion.BurnTime");
        explosionRevertTime = ConfigManager.getConfig().getLong("Abilities.Fire.Combustion.ExplosionRevertTime");
        explosionRadius = ConfigManager.getConfig().getDouble("Abilities.Fire.Combustion.ExplosionRadius");
        speed = ConfigManager.getConfig().getInt("Abilities.Fire.Combustion.Speed");
        weight = ConfigManager.getConfig().getInt("Abilities.Fire.Combustion.Weight");
        damage = ConfigManager.getConfig().getDouble("Abilities.Fire.Combustion.Damage");
        waterDamage = ConfigManager.getConfig().getDouble("Abilities.Fire.Combustion.WaterDamage");
        range = ConfigManager.getConfig().getDouble("Abilities.Fire.Combustion.Range");
        forceRelease = ConfigManager.getConfig().getBoolean("Abilities.Fire.Combustion.ForceRelease");
        punishPlayer = ConfigManager.getConfig().getBoolean("Abilities.Fire.Combustion.PunishPlayer");

        sonicBoomSpeed = (float) ConfigManager.getConfig().getDouble("Abilities.Fire.Combustion.SonicBoom.Speed") * 0.2f;
        sonicBoomParticleCount = ConfigManager.getConfig().getInt("Abilities.Fire.Combustion.SonicBoom.ParticleCount");
        sonicBoomFrequency = ConfigManager.getConfig().getInt("Abilities.Fire.Combustion.SonicBoom.Frequency");
        sonicBoomRadius = ConfigManager.getConfig().getDouble("Abilities.Fire.Combustion.SonicBoom.Radius");

        increment = 2 / ((float) chargeDuration / 50);
        increasing = 0;
        fillImmuneBlocks();
        if (damaged.get(player) != null) {
            damaged.remove(player);
        }
        if (clicked.get(player) != null) {
            clicked.remove(player);
        }

        canCharge = true;
        needCharge = true;
        thrown = false;
    }

    private void fillImmuneBlocks() {
        immuneBlocks.add(Material.AIR);
        immuneBlocks.add(Material.LAVA);
        immuneBlocks.add(Material.WATER);
        immuneBlocks.add(Material.OBSIDIAN);
        immuneBlocks.add(Material.CRYING_OBSIDIAN);
        immuneBlocks.add(Material.STRUCTURE_BLOCK);
        immuneBlocks.add(Material.BARRIER);
        immuneBlocks.add(Material.REPEATING_COMMAND_BLOCK);
        immuneBlocks.add(Material.CHAIN_COMMAND_BLOCK);
        immuneBlocks.add(Material.COMMAND_BLOCK);
        immuneBlocks.add(Material.BEDROCK);
    }

    private void setCollisions() {
        ProjectKorra.collisionManager.addCollision(new Collision(CoreAbility.getAbility("Combustion"), CoreAbility.getAbility("AirShield"), false, true));
        ProjectKorra.collisionManager.addCollision(new Collision(CoreAbility.getAbility("Combustion"), CoreAbility.getAbility("FireShield"), true, true));
    }

    private void chargeAnimation() {
        chargeI++;
        location = player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(0.3)).add(0, 0.1, 0);
        long maxDifference = (this.getStartTime() + chargeDuration) - System.currentTimeMillis();
        long difference = Math.abs(System.currentTimeMillis() - this.getStartTime());

        if (increasing < 2) {
            increasing += increment;
        }
        if (this.getStartTime() + chargeDuration > System.currentTimeMillis()) {
            GeneralMethods.displayColoredParticle(getHexColor(difference, maxDifference), location);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, (float) increasing, (float) increasing + new Random().nextFloat(-0.2f, 0));
        } else {
            location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, location, 1, 0, 0, 0, 0, null, true);
        }
    }

    private static String getHexColor(long min, long max) {
        if (max <= 0) return "#ffffff";
        float ratio = Math.max(0f, Math.min(1f, (float) min / max));
        int value = (int) (255 * ratio);
        return String.format("#%02X%02X%02X", value, value, value);
    }

    private void releaseCombustion() {
        canCharge = false;
        thrown = true;
        advance();

        if (advancementSteps % sonicBoomFrequency == 0) {
            sonicBoom(location, Particle.CLOUD, sonicBoomRadius, sonicBoomSpeed, sonicBoomParticleCount);
            location.getWorld().playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 4, 0);
        }
    }

    private void advance() {
        for (int i = 0; i <= speed; i++) {
            advancementSteps++;
            Vector dir = location.getDirection();
            dir.setX((location.getDirection().getX() * weight) + player.getEyeLocation().getDirection().getX() / (weight + 1));
            dir.setY((location.getDirection().getY() * weight) + player.getEyeLocation().getDirection().getY() / (weight + 1));
            dir.setZ((location.getDirection().getZ() * weight) + player.getEyeLocation().getDirection().getZ() / (weight + 1));
            location.setDirection(dir);
            location.add(location.getDirection());
            if (location.distance(player.getEyeLocation()) > range) {
                remove();
                bPlayer.addCooldown(this);
                return;
            }
            if (location.getBlock().getType() == Material.AIR) {
                new TempBlock(location.getBlock(), Material.LIGHT.createBlockData(), 250);
            }

            location.getWorld().spawnParticle(Particle.FLAME, location, 3, 0, 0, 0, 0.01, null, true);

            for (Location points : spawnCircle(location, location.getDirection(), 0.25, 15)) {
                points.getWorld().spawnParticle(Particle.SMOKE_NORMAL, points, 1, 0, 0, 0, 0, null, true);
            }
            for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 1.5)) {
                if (entity instanceof LivingEntity && entity != player) {
                    explode(location, false, true, entity);
                    remove();
                    bPlayer.addCooldown(this);
                    return;
                }
            }
            for (Block block : GeneralMethods.getBlocksAroundPoint(location, 1)) {
                if (block.getType() != Material.WATER) {
                    if (!block.isPassable()) {
                        explode(location, false, false, null);
                        remove();
                        bPlayer.addCooldown(this);
                        return;
                    }
                } else {
                    explode(location, true, false, null);
                    remove();
                    bPlayer.addCooldown(this);
                    return;
                }
            }
        }

    }

    private void explode(Location explosion, boolean isWater, boolean isEntity, @Nullable Entity entity) {
        if (!isWater) {
            for (Block block : GeneralMethods.getBlocksAroundPoint(explosion, explosionRadius)) {
                if (block.getType().getBlastResistance() < 10 && block.getType() != Material.AIR) {
                    if (!block.isPassable()) {
                        new TempFallingBlock(block.getLocation(), block.getBlockData(), new Vector(new Random().nextDouble(-0.7, 0.7), new Random().nextDouble(0.3, 0.7), new Random().nextDouble(-0.7, 0.7)), this);
                    }
                    new TempBlock(block, Material.AIR.createBlockData(), explosionRevertTime, this);
                    explosion.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, block.getLocation(), 1, 1.5, 1.5, 1.5, 0, null, true);
                }
            }
            explosion.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, explosion.add(0, 1, 0), 1, 0, 0, 0, 0, null, true);
            explosion.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, explosion, (int) explosionRadius * 2, explosionRadius, explosionRadius, explosionRadius, 0, null, true);
            explosion.getWorld().playSound(explosion, Sound.ENTITY_GENERIC_EXPLODE, 3, 0);
        }

        for (Entity entities : GeneralMethods.getEntitiesAroundPoint(explosion, explosionRadius)) {
            if (entities instanceof LivingEntity) {
                if (entities.getLocation().getBlock().getType() == Material.WATER) {
                    DamageHandler.damageEntity(entities, waterDamage, this);
                } else {
                    DamageHandler.damageEntity(entities, damage, this);

                }
            }
        }

        if (isWater) {
            explosion.getWorld().spawnParticle(Particle.FALLING_WATER, explosion, (int) explosionRadius * 200, explosionRadius, explosionRadius * 1.5, explosionRadius, 0, null, true);
            explosion.getWorld().spawnParticle(Particle.FALLING_DRIPSTONE_WATER, explosion, (int) explosionRadius * 200, explosionRadius, explosionRadius * 1.5, explosionRadius, 0, null, true);
            if (GeneralMethods.getMCVersion() > 1210) {
                explosion.getWorld().spawnParticle(Particle.valueOf("GUST_EMITTER_LARGE"), explosion.add(0, 1.5, 0), 1, 0, 0, 0, 0, null, true);
            } else {
                explosion.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, explosion.add(0, 1.5, 0), 1, 0, 0, 0, 0, null, true);
            }

            for (int i = 0; i <= explosionRadius * 50; i++) {
                double randomX = explosion.getX() + new Random().nextDouble(explosionRadius * -1, explosionRadius);
                double randomY = explosion.getY() + new Random().nextDouble(-1, 1);
                double randomZ = explosion.getZ() + new Random().nextDouble(explosionRadius * -1, explosionRadius);
                explosion.getWorld().spawnParticle(Particle.CLOUD, randomX, randomY, randomZ, 0, 0, 1.6 + new Random().nextDouble(0, 1), 0, 0.1, null, true);
            }
            explosion.getWorld().playSound(explosion, Sound.ENTITY_FISHING_BOBBER_SPLASH, 3, 0.7F);
            explosion.getWorld().playSound(explosion, Sound.BLOCK_WATER_AMBIENT, 3, 0.3F);
            for (Block block : GeneralMethods.getBlocksAroundPoint(explosion, explosionRadius)) {
                if (block.getType() == Material.WATER) {
                    block.getWorld().spawnParticle(Particle.BUBBLE_COLUMN_UP, block.getLocation(), 15, 0.5, 0.5, 0.5, 0.2, null, true);
                }
            }
        }
        if (isEntity) {
            assert entity != null;
            entity.setFireTicks(burnTime / 50);
        }
    }

    private void sonicBoom(Location spawn, Particle particle, double radius, float speed, int particleCount) {
        for (Location circlePoint : spawnCircle(spawn, spawn.getDirection(), radius, particleCount)) {
            Vector target = circlePoint.toVector().subtract(spawn.toVector());
            spawn.getWorld().spawnParticle(particle, spawn, 0, target.getX(), target.getY(), target.getZ(), speed, null, true);
        }
    }

    public List<Location> spawnCircle(Location location, Vector direction, double radius, int pointAmount) {
        List<Location> locations = new ArrayList<>();

        // Normalize and convert direction to yaw & pitch
        direction = direction.clone().normalize();
        float yaw = (float) Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ()));
        float pitch = (float) Math.toDegrees(-Math.asin(direction.getY()));

        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);

        for (int i = 0; i < pointAmount; i++) {
            double angle = 2 * Math.PI * i / pointAmount;
            double x = Math.cos(angle) * radius;
            double y = Math.sin(angle) * radius;

            Vector point = new Vector(x, y, 0);
            // Rotate the 2D circle to match direction
            point = rotateAroundX(point, pitchRad);
            point = rotateAroundY(point, -yawRad);

            locations.add(location.clone().add(point));
        }

        return locations;
    }
    private Vector rotateAroundX(Vector v, double angle) {
        double y = v.getY() * Math.cos(angle) - v.getZ() * Math.sin(angle);
        double z = v.getY() * Math.sin(angle) + v.getZ() * Math.cos(angle);
        return new Vector(v.getX(), y, z);
    }
    private Vector rotateAroundY(Vector v, double angle) {
        double x = v.getX() * Math.cos(angle) + v.getZ() * Math.sin(angle);
        double z = -v.getX() * Math.sin(angle) + v.getZ() * Math.cos(angle);
        return new Vector(x, v.getY(), z);
    }

    @Override
    public void progress() {
        needCharge = (this.getStartTime() + chargeDuration) > System.currentTimeMillis();
        if (!bPlayer.canBendIgnoreCooldowns(this) || !player.isOnline() || player.isDead()) {
            remove();
            return;
        }
        if (!thrown && punishPlayer) {
            if (damaged.get(player) != null) {
                explode(player.getLocation(), false, true, player);
                remove();
                bPlayer.addCooldown(this);
                damaged.remove(player);
                return;
            }
        }
        if (thrown) {
            releaseCombustion();
            setCollisions();
            if (clicked.get(player) != null) {

                explode(location, false, false, null);
                remove();
                bPlayer.addCooldown(this);
                damaged.remove(player);
                return;
            }
        } else {
            if (clicked.get(player) != null) {
                clicked.remove(player);
            }
        }

        if (needCharge && !player.isSneaking() && !thrown) {
            remove();
            return;
        }
        if (canCharge && player.isSneaking() && !thrown) {
            chargeAnimation();
        }
        if (!needCharge) {
            if (forceRelease && !thrown) {
                thrown = true;
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 2, 1);
                sonicBoom(location, Particle.SMALL_FLAME, 0.5, 0.2f, 60);
            }

            if (!player.isSneaking() && !thrown) {
                thrown = true;
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 2, 1);
                sonicBoom(location, Particle.SMALL_FLAME, 0.5, 0.2f, 60);
            }

            if (player.isSneaking() && canCharge && !thrown) {
                chargeAnimation();
            }
        }
    }

    @Override
    public boolean isSneakAbility() {
        return true;
    }

    @Override
    public boolean isHarmlessAbility() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return ConfigManager.getConfig().getBoolean("Abilities.Fire.Combustion.Enabled");
    }

    @Override
    public String getDescription() {
        return ConfigManager.languageConfig.get().getString("Abilities.Fire.Combustion.Description");
    }

    @Override
    public String getInstructions() {
        return ConfigManager.languageConfig.get().getString("Abilities.Fire.Combustion.Instructions");
    }

    @Override
    public long getCooldown() {
        return cooldown;
    }

    @Override
    public String getName() {
        return "Combustion";
    }

    @Override
    public Location getLocation() {
        return location;
    }

    public static void explode(Player player) {
        clicked.put(player, true);
    }

}
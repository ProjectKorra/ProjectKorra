package com.projectkorra.projectkorra.airbending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.region.RegionProtection;
import com.projectkorra.projectkorra.util.TempBlock;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BrushableBlock;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.type.Candle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

/**
 * @author Manu585
 */
public class AirBreath extends AirAbility {

    private static final String CONFIG_ROOT_PATH = "Abilities.Air.AirBreath.";

    private static final double MOUTH_Y_OFFSET = 0.2;
    private static final double PARTICLE_STEP = 0.5;
    private static final double BASE_CONE_ANGLE_DEGREES = 25.0;
    private static final double CONE_ANGLE_GROWTH_PER_BLOCK = 1.5;
    private static final double KNOCKBACK_FALLOFF_RANGE_FACTOR = 0.8;
    private static final double KNOCKBACK_VERTICAL_LIFT = 0.04;
    private static final double EXIT_BURST_FACTOR = 0.4;
    private static final long TEMP_BLOCK_DURATION = 10000L;

    @Attribute(Attribute.SELF_PUSH) // Maximum velocity the player can reach from self-push recoil
    private double selfPushFactor;
    @Attribute(Attribute.SELF_PUSH) // Per tick recoil acceleration applied when breath hits a surface
    private double selfPushStrength;
    @Attribute(Attribute.KNOCKBACK)
    private double knockback;
    @Attribute(Attribute.RADIUS)
    private double radius;
    @Attribute(Attribute.RANGE)
    private double range;
    @Attribute(Attribute.DURATION)
    private long duration;
    @Attribute(Attribute.COOLDOWN)
    private long cooldown;
    @Attribute(Attribute.CHARGE_DURATION)
    private long growTime;
    private boolean canExcavateSuspiciousBlocks;

    // The currently running lava-freeze wave, or null when no wave is active
    private BukkitTask lavaSplashTask = null;

    // Entities that were inside the breath cone last tick
    private final Set<Entity> previouslyInCone = new HashSet<>();

    public AirBreath(Player player) {
        super(player);

        if (getMouthLocation().getBlock().isLiquid()) return;
        if (!bPlayer.canBend(this)) return;
        if (CoreAbility.getAbility(player, AirBreath.class) != null) return;

        this.selfPushFactor = getConfig().getDouble(CONFIG_ROOT_PATH + "SelfPushFactor");
        this.selfPushStrength = getConfig().getDouble(CONFIG_ROOT_PATH + "SelfPushStrength");
        this.knockback = getConfig().getDouble(CONFIG_ROOT_PATH + "Knockback");
        this.radius = getConfig().getDouble(CONFIG_ROOT_PATH + "Radius");
        this.range = getConfig().getDouble(CONFIG_ROOT_PATH + "Range");
        this.duration = getConfig().getLong(CONFIG_ROOT_PATH + "Duration");
        this.cooldown = getConfig().getLong(CONFIG_ROOT_PATH + "Cooldown");
        this.growTime = getConfig().getLong(CONFIG_ROOT_PATH + "GrowTime");
        this.canExcavateSuspiciousBlocks = getConfig().getBoolean(CONFIG_ROOT_PATH + "CanExcavateSuspiciousBlocks");

        start();
    }

    @Override
    public void progress() {
        if (!player.isOnline() || player.isDead()) {
            remove();
            return;
        }

        if (!bPlayer.canBend(this) || !player.isSneaking() || hasExpired()) {
            removeWithCooldown();
            return;
        }

        Location mouthLocation = getMouthLocation();

        if (mouthLocation.getBlock().isLiquid()) {
            removeWithCooldown();
            return;
        }

        if (isInvalidEffectLocation(mouthLocation)) {
            removeWithCooldown();
            return;
        }

        playBreathAnimation();
        handleKnockback();
        handleProjectileRedirect();
        handleSelfPush();
    }

    @Override
    public void remove() {
        if (lavaSplashTask != null) {
            lavaSplashTask.cancel();
            lavaSplashTask = null;
        }
        super.remove();
    }

    /**
     * Marches along the player's look lookDirection spawning cone-shaped air particles.
     * Particles are placed randomly within a circle that widens with distance, forming a cone.
     * Stops at the first solid block.
     */
    private void playBreathAnimation() {
        BreathContext breathContext = createBreathContext();

        if (ThreadLocalRandom.current().nextDouble() < 0.4) {
            playAirbendingSound(breathContext.mouthLocation());
        }

        ConeBasis coneBasis = createConeBasis(breathContext.lookDirection());

        double hitRange = breathContext.reach();
        boolean hitWater = false;
        boolean hitLava = false;

        for (double dist = PARTICLE_STEP; dist <= breathContext.reach(); dist += PARTICLE_STEP) {
            Location center = breathContext.mouthLocation().clone().add(breathContext.lookDirection().clone().multiply(dist));
            Block block = center.getBlock();

            if (block.isLiquid()) {
                hitRange = dist;
                hitWater = block.getType() == Material.WATER;
                hitLava = !hitWater;
                break;
            }

            // Candles extinguishing check
            if (isLitCandle(block)) {
                extinguishCandleInCone(center, dist);
                hitRange = dist;
                break;
            }

            // Suspicious sand / gravel, excavate it
            if (isSuspiciousBlock(block) && canExcavateSuspiciousBlocks) {
                excavateSuspiciousBlocksInCone(center, dist);
                hitRange = dist;
                break;
            }

            // Not further up due to candle and suspicious blocks checks
            if (!block.isPassable()) {
                hitRange = dist;
                break;
            }

            // Extinguish fire blocks inside the cone cross-section at this distance
            if (isFireBlock(block)) {
                extinguishFireInCone(center, dist);
            }

            spawnBreathParticles(center, breathContext.lookDirection(), coneBasis, dist, breathContext.reach());
        }

        Location hitLocation = breathContext.mouthLocation().clone().add(breathContext.lookDirection().clone().multiply(hitRange));
        if (hitWater) {
            handleWaterSplash(hitLocation);
        } else if (hitLava) {
            handleLavaSplash(hitLocation, hitRange);
        }
    }

    private void spawnBreathParticles(Location center, Vector lookDirection, ConeBasis coneBasis, double distance, double reach) {
        double coneRadius = getConeRadius(distance);
        ThreadLocalRandom random = ThreadLocalRandom.current();

        // Scatter random particles within the cone cross-section at this distance.
        // Using polar coordinates with a slight radius bias toward the edge so the cone outline reads
        // densityFactor scales from 0.25 near the mouth to 1.0 at full reach so the
        // area close to the player stays open and visibility is not blocked.
        double densityFactor = 0.25 + 0.75 * (distance / reach);
        int count = random.nextInt(2, 4);

        for (int i = 0; i < count; i++) {
            if (random.nextDouble() > densityFactor) continue;

            double angle = random.nextDouble(Math.PI * 2);
            // sqrt distribution biases toward the outer edge while still filling the interior
            double r = coneRadius * Math.sqrt(random.nextDouble());
            // small forward jitter breaks up the uniform slice look
            double forwardJitter = random.nextDouble(-0.15, 0.15);

            Vector offset = coneBasis.perpendicular1().clone().multiply(Math.cos(angle) * r)
                    .add(coneBasis.perpendicular2().clone().multiply(Math.sin(angle) * r))
                    .add(lookDirection.clone().multiply(forwardJitter));

            playAirbendingParticles(center.clone().add(offset), 1, 0.02, 0.02, 0.02);
        }

        // Occasional central particle keeps the core of the breath visible.
        if (random.nextDouble() < 0.35 * densityFactor) {
            playAirbendingParticles(center, 1, 0.04, 0.04, 0.04);
        }
    }

    private ConeBasis createConeBasis(Vector lookDirection) {
        // Build two vectors perpendicular to dir to form the cone's cross-section plane
        Vector up = new Vector(0, 1, 0);
        Vector perpendicular1 = lookDirection.clone().crossProduct(up);

        // Fallback if the look direction is nearly vertical and cross product degenerates.
        if (perpendicular1.lengthSquared() < 1.0E-6) {
            perpendicular1 = lookDirection.clone().crossProduct(new Vector(1, 0, 0));
        }

        perpendicular1.normalize();
        Vector perpendicular2 = lookDirection.clone().crossProduct(perpendicular1).normalize();
        return new ConeBasis(perpendicular1, perpendicular2);
    }

    private void handleWaterSplash(Location location) {
        if (isInvalidEffectLocation(location)) return;

        if (ThreadLocalRandom.current().nextDouble() < 0.3) {
            location.getWorld().spawnParticle(Particle.SPLASH, location.clone().add(0, .2, 0), 20, 0.35, 0.35, 0.35, 0.15);
            location.getWorld().playSound(location, Sound.ENTITY_GENERIC_SPLASH, 0.4f, 1.3f);
        }

        Block block = location.getBlock();
        if (block.isLiquid() && TempBlock.isTempBlock(block)) {
            TempBlock.removeBlock(block);
        }
    }

    private void handleLavaSplash(Location location, double hitDistance) {
        if (isInvalidEffectLocation(location)) return;

        location.getWorld().spawnParticle(Particle.LAVA, location.clone().add(0, .2, 0), 8, 0.3, 0.3, 0.3, 0);
        location.getWorld().spawnParticle(Particle.ASH, location, 12, 0.3, 0.5, 0.3, 0.04);

        if (ThreadLocalRandom.current().nextDouble() < 0.6) {
            location.getWorld().playSound(location, Sound.BLOCK_LAVA_EXTINGUISH, 1.0f, 1.0f);
        }

        // Always harden the exact hit block immediately, regardless of any running wave.
        hardenLavaBlock(location.getBlock());

        // Only start a new outward ripple if no wave is already expanding.
        if (lavaSplashTask != null && !lavaSplashTask.isCancelled()) return;

        final double splashRadius = Math.max(1.0, radius * (hitDistance / range));
        final Location frozen = location.clone();
        final double[] waveRadius = { 0 };

        // A single repeating task expands the freeze wave outward every 3 ticks.
        // Makes the Lava hardening feel more natural.
        lavaSplashTask = new BukkitRunnable() {
            @Override
            public void run() {
                double previousRadius = waveRadius[0];
                waveRadius[0] += 0.5;

                if (waveRadius[0] > splashRadius) {
                    lavaSplashTask = null;
                    cancel();
                    return;
                }

                forEachBlockInRadius(
                        frozen,
                        waveRadius[0],
                        block -> block.getLocation().add(0.5, 0.5, 0.5).distance(frozen) > previousRadius,
                        AirBreath.this::hardenLavaBlock);
            }
        }.runTaskTimer(ProjectKorra.plugin, 0L, 2L);
    }

    private void hardenLavaBlock(Block block) {
        if (block.getType() == Material.LAVA) {
            if (block.getBlockData() instanceof Levelled levelled && levelled.getLevel() == 0) {
                new TempBlock(block, Material.OBSIDIAN.createBlockData(), TEMP_BLOCK_DURATION, this);
            } else {
                new TempBlock(block, Material.COBBLESTONE.createBlockData(), TEMP_BLOCK_DURATION, this);
            }
        }
    }

    /**
     * Extinguishes all fire blocks within the cone's cross-section radius at the given distance.
     */
    private void extinguishFireInCone(Location center, double distance) {
        if (isInvalidEffectLocation(center)) return;

        forEachBlockInRadius(center, getConeRadius(distance), block -> {
            if (isFireBlock(block)) {
                new TempBlock(block, Material.AIR.createBlockData(), TEMP_BLOCK_DURATION, this);
            }
        });

        center.getWorld().playSound(center, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.0f);
        center.getWorld().spawnParticle(Particle.SMOKE, center, 4, 0.15, 0.15, 0.15, 0.01);
    }

    private void extinguishCandleInCone(Location center, double distance) {
        if (isInvalidEffectLocation(center)) return;

        forEachBlockInRadius(center, getConeRadius(distance), block -> {
            if (!isLitCandle(block)) return;

            // Copy the existing block data and switch lit to false so the candle keeps its
            // state and reverts fully when the TempBlock expires.
            BlockData unlitData = block.getBlockData();
            if (unlitData instanceof Candle candle) {
                candle.setLit(false);
            }
            new TempBlock(block, unlitData, TEMP_BLOCK_DURATION, this);
        });

        center.getWorld().playSound(center, Sound.BLOCK_CANDLE_EXTINGUISH, 1.0f, 1.0f);
        center.getWorld().spawnParticle(Particle.SMOKE, center, 2, 0.1, 0.1, 0.1, 0.01);
    }

    private boolean isLitCandle(Block block) {
        return block.getBlockData() instanceof Candle candle && candle.isLit();
    }

    /**
     * Blows suspicious sand / gravel blocks apart within the cone cross-section, dropping their
     * hidden item exactly as vanilla brushing does and replacing the block with plain sand / gravel.
     * If the loot table hasn't been resolved yet (block never brushed before), it is resolved now.
     */
    private void excavateSuspiciousBlocksInCone(Location center, double distance) {
        if (isInvalidEffectLocation(center)) return;

        forEachBlockInRadius(center, getConeRadius(distance), block -> {
            if (!isSuspiciousBlock(block)) return;
            if (RegionProtection.isRegionProtected(this, block.getLocation())) return;

            Location dropLocation = block.getLocation().add(0.5, 0.7, 0.5);

            // Resolve and drop the stored item, matching vanilla loot behavior.
            if (block.getState() instanceof BrushableBlock brushable) {
                ItemStack item = brushable.getItem();
                if (item != null && item.getType() != Material.AIR) {
                    // Item was already resolved by prior brushing. drop it directly
                    block.getWorld().dropItemNaturally(dropLocation, item);
                } else if (brushable.getLootTable() != null) {
                    // Block never brushed: resolve using the block's own seed so the
                    // result is deterministic, matching vanilla behavior exactly.
                    LootContext lootContext = new LootContext.Builder(dropLocation).build();
                    for (ItemStack drop : brushable.getLootTable().populateLoot(new Random(brushable.getSeed()), lootContext)) {
                        block.getWorld().dropItemNaturally(dropLocation, drop);
                    }
                }
            }

            Sound brushSound = block.getType() == Material.SUSPICIOUS_SAND
                    ? Sound.ITEM_BRUSH_BRUSHING_SAND_COMPLETE
                    : Sound.ITEM_BRUSH_BRUSHING_GRAVEL_COMPLETE;

            block.getWorld().spawnParticle(
                    Particle.BLOCK,
                    block.getLocation().add(0.5, 0.5, 0.5),
                    12,
                    0.3,
                    0.3,
                    0.3,
                    block.getBlockData());

            block.getWorld().playSound(block.getLocation(), brushSound, 1.0f, 1.0f);
            block.setType(block.getType() == Material.SUSPICIOUS_SAND ? Material.SAND : Material.GRAVEL);
        });
    }

    /**
     * Continuously accelerates entities inside the cone, then gives them a final burst
     * the moment they leave, carrying them well past the breath range like a wind gust.
     */
    private void handleKnockback() {
        BreathContext breathContext = createBreathContext();
        Set<Entity> entitiesInCone = new HashSet<>();

        for (Entity entity : GeneralMethods.getEntitiesAroundPoint(breathContext.mouthLocation(), breathContext.reach())) {
            if (entity.equals(player)) continue;
            if (Commands.invincible.contains(entity.getName())) continue;
            if (RegionProtection.isRegionProtected(this, entity.getLocation())) continue;
            if (GeneralMethods.isHeavyEntity(entity) && !bPlayer.isAvatarState()) continue;
            if (isInsideBreathCone(breathContext.mouthLocation(), breathContext.lookDirection(), entity.getLocation())) continue;

            double distance = entity.getLocation().distance(breathContext.mouthLocation());
            double strength = getDistanceScaledKnockback(distance);
            if (strength <= 0) continue;

            entitiesInCone.add(entity);

            // Add to existing velocity each tick, slight Y lift so entities arc upward
            Vector push = breathContext.lookDirection().clone().multiply(strength);
            push.setY(push.getY() + KNOCKBACK_VERTICAL_LIFT);

            Vector newVelocity = entity.getVelocity().add(push);
            if (newVelocity.length() > knockback) {
                newVelocity = newVelocity.normalize().multiply(knockback);
            }
            GeneralMethods.setVelocity(this, entity, newVelocity);
        }

        // Entities that just left the cone get a final burst so they fly past the range boundary (makes it seem more like a wind burst)
        for (Entity entity : previouslyInCone) {
            if (entitiesInCone.contains(entity)) continue;

            Vector burst = breathContext.lookDirection().clone().multiply(knockback * EXIT_BURST_FACTOR);
            GeneralMethods.setVelocity(this, entity, entity.getVelocity().add(burst));
        }

        previouslyInCone.clear();
        previouslyInCone.addAll(entitiesInCone);
    }

    /**
     * Propels the player via recoil when their breath hits a solid surface or a heavy
     * entity (iron golem, sniffer, warden, ravager). Heavy entities act as walls
     * unless the player is in avatar state.
     */
    private void handleSelfPush() {
        BreathContext breathContext = createBreathContext();

        RayTraceResult blockResult = player.getWorld().rayTraceBlocks(
                breathContext.mouthLocation(),
                breathContext.lookDirection(),
                breathContext.reach(),
                FluidCollisionMode.NEVER,
                true);

        double hitDistance = blockResult != null && blockResult.getHitBlock() != null
                ? blockResult.getHitPosition().distance(breathContext.mouthLocation().toVector())
                : breathContext.reach() + 1;

        // Heavy entities act as a wall for recoil, but only when not in avatar state
        if (!bPlayer.isAvatarState()) {
            RayTraceResult entityRayTraceResult = player.getWorld().rayTraceEntities(
                    breathContext.mouthLocation(),
                    breathContext.lookDirection(),
                    breathContext.reach(),
                    0.5,
                    entity -> !entity.equals(player) && GeneralMethods.isHeavyEntity(entity));

            if (entityRayTraceResult != null && entityRayTraceResult.getHitEntity() != null) {
                double entityDistance = entityRayTraceResult.getHitPosition().distance(breathContext.mouthLocation().toVector());
                if (entityDistance < hitDistance) {
                    hitDistance = entityDistance;
                }
            }
        }

        if (hitDistance > breathContext.reach()) return;

        double strength = selfPushStrength * (1.0 + (range - hitDistance) / range);
        Vector recoil = breathContext.lookDirection.clone().multiply(-strength);
        Vector playerVelocity = player.getVelocity().add(recoil);

        if (playerVelocity.length() > selfPushFactor) {
            playerVelocity = playerVelocity.normalize().multiply(selfPushFactor);
        }

        GeneralMethods.setVelocity(this, player, playerVelocity);
    }

    /**
     * Deflects projectiles inside the breath cone.
     * The breath force is added to each projectile's current velocity so faster projectiles are
     * bent less than slow ones, which feels physically natural.
     * Projectiles fired by the bender themselves are ignored.
     */
    private void handleProjectileRedirect() {
        BreathContext breathContext = createBreathContext();

        for (Entity entity : GeneralMethods.getEntitiesAroundPoint(breathContext.mouthLocation(), breathContext.reach())) {
            if (!(entity instanceof Projectile projectile)) continue;
            if (player.equals(projectile.getShooter())) continue;
            if (RegionProtection.isRegionProtected(this, entity.getLocation())) continue;
            if (isInsideBreathCone(breathContext.mouthLocation(), breathContext.lookDirection(), entity.getLocation())) continue;

            double distance = entity.getLocation().distance(breathContext.mouthLocation());
            double strength = getDistanceScaledKnockback(distance);
            if (strength <= 0) continue;

            Vector newVelocity = entity.getVelocity().add(breathContext.lookDirection().clone().multiply(strength));
            entity.setVelocity(newVelocity);

            // Fireballs are self-propelled via an internal lookDirection vector
            // updating velocity alone isn't enough to actually steer them
            if (projectile instanceof Fireball fireball) {
                fireball.setDirection(newVelocity.clone().normalize());
            }

            // Transfer ownership to the player so kill credit and advancements are correctly attributed
            projectile.setShooter(player);
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
    public long getCooldown() {
        return cooldown;
    }

    @Override
    public String getName() {
        return "AirBreath";
    }

    @Override
    public Location getLocation() {
        return player != null ? player.getEyeLocation().subtract(0, .2, 0) : null;
    }

    private BreathContext createBreathContext() {
        Location mouthLocation = getMouthLocation();
        return new BreathContext(mouthLocation, mouthLocation.getDirection().normalize(), currentReach());
    }

    private Location getMouthLocation() {
        return player.getEyeLocation().subtract(0, MOUTH_Y_OFFSET, 0);
    }

    private double getConeHalfAngle(double distance) {
        return Math.toRadians(BASE_CONE_ANGLE_DEGREES + distance * CONE_ANGLE_GROWTH_PER_BLOCK);
    }

    private double getConeRadius(double distance) {
        return radius * (distance / range);
    }

    private double getDistanceScaledKnockback(double distance) {
        return knockback * (1.0 - distance / (range * KNOCKBACK_FALLOFF_RANGE_FACTOR));
    }

    private boolean isInvalidEffectLocation(Location location) {
        return location.getWorld() == null || RegionProtection.isRegionProtected(this, location);
    }

    private boolean isInsideBreathCone(Location origin, Vector direction, Location target) {
        Vector toTarget = target.toVector().subtract(origin.toVector());
        double distance = toTarget.length();

        if (distance < 0.1) {
            return true;
        }

        return !(toTarget.normalize().angle(direction) <= getConeHalfAngle(distance));
    }

    private boolean isFireBlock(Block block) {
        Material type = block.getType();
        return type == Material.FIRE || type == Material.SOUL_FIRE;
    }

    private boolean isSuspiciousBlock(Block block) {
        Material type = block.getType();
        return type == Material.SUSPICIOUS_SAND || type == Material.SUSPICIOUS_GRAVEL;
    }

    private void forEachBlockInRadius(Location center, double radius, Consumer<Block> consumer) {
        forEachBlockInRadius(center, radius, block -> true, consumer);
    }

    private void forEachBlockInRadius(Location center, double radius, Predicate<Block> filter, Consumer<Block> consumer) {
        int r = (int) Math.ceil(radius);

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    Block block = center.clone().add(dx, dy, dz).getBlock();

                    if (block.getLocation().add(0.5, 0.5, 0.5).distance(center) > radius) {
                        continue;
                    }

                    if (!filter.test(block)) {
                        continue;
                    }

                    consumer.accept(block);
                }
            }
        }
    }

    private void removeWithCooldown() {
        bPlayer.addCooldown(this);
        remove();
    }

    private boolean hasExpired() {
        return System.currentTimeMillis() - getStartTime() > duration;
    }

    // How far the breath currently reaches, grows linearly from 0 to range over given growtime
    private double currentReach() {
        double elapsed = System.currentTimeMillis() - getStartTime();
        return Math.min(range, range * elapsed / growTime);
    }

    public double getSelfPushFactor() {
        return selfPushFactor;
    }

    public double getSelfPushStrength() {
        return selfPushStrength;
    }

    public double getKnockback() {
        return knockback;
    }

    public double getRadius() {
        return radius;
    }

    public double getRange() {
        return range;
    }

    public long getDuration() {
        return duration;
    }

    public long getGrowTime() {
        return growTime;
    }

    public BukkitTask getLavaSplashTask() {
        return lavaSplashTask;
    }

    public Set<Entity> getPreviouslyInCone() {
        return previouslyInCone;
    }

    public static String getConfigRootPath() {
        return CONFIG_ROOT_PATH;
    }

    // DATA HOLDING RECORDS FOR BETTER READABILITY
    private record BreathContext(Location mouthLocation, Vector lookDirection, double reach) {}

    private record ConeBasis(Vector perpendicular1, Vector perpendicular2) {}

}

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

public class AirBreath extends AirAbility {

    private static final String CONFIG_ROOT_PATH = "Abilities.Air.AirBreath.";

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

    // The currently running lava-freeze wave, or null when no wave is active
    private BukkitTask lavaSplashTask = null;

    // Entities that were inside the breath cone last tick
    private Set<Entity> previouslyInCone = new HashSet<>();

    public AirBreath(Player player) {
        super(player);

        if (player.getEyeLocation().getBlock().isLiquid()) return;
        if (bPlayer.isOnCooldown(this)) return;
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

        start();
    }

    @Override
    public void progress() {
        if (!player.isOnline() || player.isDead()) {
            remove();
            return;
        }

        if (!bPlayer.canBend(this)) {
            bPlayer.addCooldown(this);
            remove();
            return;
        }

        if (!player.isSneaking()) {
            bPlayer.addCooldown(this);
            remove();
            return;
        }

        if (System.currentTimeMillis() - getStartTime() > duration) {
            bPlayer.addCooldown(this);
            remove();
            return;
        }

        if (player.getEyeLocation().getBlock().isLiquid()) {
            remove();
            return;
        }

        if (RegionProtection.isRegionProtected(this, player.getEyeLocation())) {
            remove();
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

    // How far the breath currently reaches, grows linearly from 0 to range over given growtime
    private double currentReach() {
        double elapsed = System.currentTimeMillis() - getStartTime();
        return Math.min(range, range * elapsed / growTime);
    }

    /**
     * Marches along the player's look lookDirection spawning cone-shaped air particles.
     * Particles are placed randomly within a circle that widens with distance, forming a cone.
     * Stops at the first solid block.
     */
    private void playBreathAnimation() {
        Location mouthLocation = player.getEyeLocation().subtract(0, .2, 0);
        Vector lookDirection = mouthLocation.getDirection().normalize();

        if (ThreadLocalRandom.current().nextDouble() < .4) {
            playAirbendingSound(mouthLocation);
        }

        // Build two vectors perpendicular to dir to form the cone's cross-section plane
        Vector up = new Vector(0, 1, 0);

        Vector perpendicular1 = lookDirection.clone().crossProduct(up).normalize();
        Vector perpendicular2 = lookDirection.clone().crossProduct(perpendicular1).normalize();

        final double step = 0.5;
        final double reach = currentReach();

        double hitRange = reach;
        boolean hitWater = false;
        boolean hitLava = false;

        for (double dist = step; dist <= reach; dist += step) {
            Location center = mouthLocation.clone().add(lookDirection.clone().multiply(dist));
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
            if (block.getType() == Material.SUSPICIOUS_SAND || block.getType() == Material.SUSPICIOUS_GRAVEL) {
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
            if (block.getType() == Material.FIRE || block.getType() == Material.SOUL_FIRE) {
                extinguishFireInCone(center, dist);
            }

            double coneRadius = radius * (dist / range);
            ThreadLocalRandom random = ThreadLocalRandom.current();

            // Scatter random particles within the cone cross-section at this distance.
            // Using polar coordinates with a slight radius bias toward the edge so the cone outline reads
            // densityFactor scales from 0.25 near the mouth to 1.0 at full reach so the
            // area close to the player stays open and visibility is not blocked.
            double densityFactor = 0.25 + 0.75 * (dist / reach);
            int count = random.nextInt(2, 4);
            for (int i = 0; i < count; i++) {
                if (random.nextDouble() > densityFactor) continue;

                double angle = random.nextDouble(Math.PI * 2);
                // sqrt distribution biases toward the outer edge while still filling the interior
                double r = coneRadius * Math.sqrt(random.nextDouble());
                // small forward jitter breaks up the uniform slice look
                double forwardJitter = random.nextDouble(-0.15, 0.15);

                Vector offset = perpendicular1.clone().multiply(Math.cos(angle) * r)
                        .add(perpendicular2.clone().multiply(Math.sin(angle) * r))
                        .add(lookDirection.clone().multiply(forwardJitter));

                playAirbendingParticles(center.clone().add(offset), 1, 0.02, 0.02, 0.02);
            }

            // Occasional central particle keeps the core of the breath visible.
            if (random.nextDouble() < 0.35 * densityFactor) {
                playAirbendingParticles(center, 1, 0.04, 0.04, 0.04);
            }
        }

        Location hitLoc = mouthLocation.clone().add(lookDirection.clone().multiply(hitRange));
        if (hitWater) {
            handleWaterSplash(hitLoc);
        } else if (hitLava) {
            handleLavaSplash(hitLoc, hitRange);
        }
    }

    private void handleWaterSplash(Location location) {
        if (location.getWorld() == null) return;
        if (RegionProtection.isRegionProtected(this, location)) return;

        if (ThreadLocalRandom.current().nextDouble() < .3) {
            location.getWorld().spawnParticle(Particle.SPLASH, location.add(0, .2, 0), 20, 0.35, 0.35, 0.35, 0.15);
            location.getWorld().playSound(location, Sound.ENTITY_GENERIC_SPLASH, 0.4f, 1.3f);
        }

        Block block = location.getBlock();
        if (block.isLiquid() && TempBlock.isTempBlock(block)) {
            TempBlock.removeBlock(block);
        }
    }

    private void handleLavaSplash(Location location, double hitDistance) {
        if (location.getWorld() == null) return;
        if (RegionProtection.isRegionProtected(this, location)) return;

        location.getWorld().spawnParticle(Particle.LAVA, location.add(0, .2, 0), 8, 0.3, 0.3, 0.3, 0);
        location.getWorld().spawnParticle(Particle.ASH, location, 12, 0.3, 0.5, 0.3, 0.04);

        if (ThreadLocalRandom.current().nextDouble() < .6) {
            location.getWorld().playSound(location, Sound.BLOCK_LAVA_EXTINGUISH, 1.0f, 1.0f);
        }

        // Always harden the exact hit block immediately, regardless of any running wave.
        hardenLavaBlock(location.getBlock());

        // Only start a new outward ripple if no wave is already expanding.
        if (lavaSplashTask != null && !lavaSplashTask.isCancelled()) return;

        final double splashRadius = Math.max(1.0, radius * (hitDistance / range));
        final Location frozen = location.clone();
        final double[] waveRadius = {0};

        // A single repeating task expands the freeze wave outward every 3 ticks.
        // Makes the Lava hardening feel more natural.
        lavaSplashTask = new BukkitRunnable() {
            @Override
            public void run() {
                double prevRadius = waveRadius[0];
                waveRadius[0] += 0.5;

                if (waveRadius[0] > splashRadius) {
                    lavaSplashTask = null;
                    cancel();
                    return;
                }

                int r = (int) Math.ceil(waveRadius[0]);
                for (int dx = -r; dx <= r; dx++) {
                    for (int dy = -r; dy <= r; dy++) {
                        for (int dz = -r; dz <= r; dz++) {
                            Block block = frozen.clone().add(dx, dy, dz).getBlock();
                            double dist = block.getLocation().add(0.5, 0.5, 0.5).distance(frozen);
                            // Process only the new ring added this step.
                            if (dist > waveRadius[0] || dist <= prevRadius) continue;
                            hardenLavaBlock(block);
                        }
                    }
                }
            }
        }.runTaskTimer(ProjectKorra.plugin, 0L, 2L);
    }

    private void hardenLavaBlock(Block block) {
        if (block.getType() == Material.LAVA) {
            if (block.getBlockData() instanceof Levelled lev && lev.getLevel() == 0) {
                new TempBlock(block, Material.OBSIDIAN.createBlockData(), 10000L, this);
            } else {
                new TempBlock(block, Material.COBBLESTONE.createBlockData(), 10000L, this);
            }
        }
    }

    /**
     * Extinguishes all fire blocks within the cone's cross-section radius at the given distance.
     */
    private void extinguishFireInCone(Location center, double distance) {
        if (center.getWorld() == null) return;
        if (RegionProtection.isRegionProtected(this, center)) return;

        double coneRadius = radius * (distance / range);
        int r = (int) Math.ceil(coneRadius);

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    Block nearby = center.clone().add(dx, dy, dz).getBlock();

                    if (nearby.getType() != Material.FIRE && nearby.getType() != Material.SOUL_FIRE) continue;
                    if (nearby.getLocation().add(0.5, 0.5, 0.5).distance(center) > coneRadius) continue;

                    new TempBlock(nearby, Material.AIR.createBlockData(), 10000L, this);
                }
            }
        }
        center.getWorld().playSound(center, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.0f);
        center.getWorld().spawnParticle(Particle.SMOKE, center, 4, 0.15, 0.15, 0.15, 0.01);
    }

    private void extinguishCandleInCone(Location center, double distance) {
        if (center.getWorld() == null) return;
        if (RegionProtection.isRegionProtected(this, center)) return;

        double coneRadius = radius * (distance / range);
        int r = (int) Math.ceil(coneRadius);

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    Block nearby = center.clone().add(dx, dy, dz).getBlock();

                    if (!isLitCandle(nearby)) continue;
                    if (nearby.getLocation().add(0.5, 0.5, 0.5).distance(center) > coneRadius) continue;

                    // Copy the existing block data and switch lit to false so the candle keeps its
                    // state and reverts fully when the TempBlock expires.
                    BlockData unlitData = nearby.getBlockData();
                    if (unlitData instanceof Candle candle) {
                        candle.setLit(false);
                    }
                    new TempBlock(nearby, unlitData, 10000L, this);
                }
            }
        }
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
        if (center.getWorld() == null) return;
        if (RegionProtection.isRegionProtected(this, center)) return;

        double coneRadius = radius * (distance / range);
        int r = (int) Math.ceil(coneRadius);

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    Block nearby = center.clone().add(dx, dy, dz).getBlock();

                    if (nearby.getType() != Material.SUSPICIOUS_SAND && nearby.getType() != Material.SUSPICIOUS_GRAVEL) continue;
                    if (nearby.getLocation().add(0.5, 0.5, 0.5).distance(center) > coneRadius) continue;
                    if (RegionProtection.isRegionProtected(this, nearby.getLocation())) continue;

                    Location dropLoc = nearby.getLocation().add(0.5, 0.7, 0.5);

                    // Resolve and drop the stored item, matching vanilla loot behavior.
                    if (nearby.getState() instanceof BrushableBlock brushable) {
                        ItemStack item = brushable.getItem();
                        if (item != null && item.getType() != Material.AIR) {
                            // Item was already resolved by prior brushing. drop it directly
                            nearby.getWorld().dropItemNaturally(dropLoc, item);
                        } else if (brushable.getLootTable() != null) {
                            // Block never brushed: resolve using the block's own seed so the
                            // result is deterministic, matching vanilla behavior exactly.
                            LootContext context = new LootContext.Builder(dropLoc).build();
                            for (ItemStack drop : brushable.getLootTable().populateLoot(new Random(brushable.getSeed()), context)) {
                                nearby.getWorld().dropItemNaturally(dropLoc, drop);
                            }
                        }
                    }

                    Sound brushSound = nearby.getType() == Material.SUSPICIOUS_SAND
                            ? Sound.ITEM_BRUSH_BRUSHING_SAND_COMPLETE
                            : Sound.ITEM_BRUSH_BRUSHING_GRAVEL_COMPLETE;
                    nearby.getWorld().spawnParticle(Particle.BLOCK, nearby.getLocation().add(0.5, 0.5, 0.5), 12, 0.3, 0.3, 0.3, nearby.getBlockData());
                    nearby.getWorld().playSound(nearby.getLocation(), brushSound, 1.0f, 1.0f);
                    nearby.setType(nearby.getType() == Material.SUSPICIOUS_SAND ? Material.SAND : Material.GRAVEL);
                }
            }
        }
    }

    /**
     * Continuously accelerates entities inside the cone, then gives them a final burst
     * the moment they leave, carrying them well past the breath range like a wind gust.
     */
    private void handleKnockback() {
        Location mouthLocation = player.getEyeLocation().subtract(0, .2, 0);
        Vector lookDirection = mouthLocation.getDirection().normalize();
        double reach = currentReach();

        Set<Entity> entitiesInCone = new HashSet<>();

        for (Entity entity : GeneralMethods.getEntitiesAroundPoint(mouthLocation, reach)) {
            if (entity.equals(player)) continue;
            if (Commands.invincible.contains(entity.getName())) continue;
            if (RegionProtection.isRegionProtected(this, entity.getLocation())) continue;
            if (GeneralMethods.isHeavyEntity(entity) && !bPlayer.isAvatarState()) continue;

            Vector toEntity = entity.getLocation().toVector().subtract(mouthLocation.toVector());
            double dist = toEntity.length();
            if (dist < 0.1) continue;

            double coneHalfAngle = Math.toRadians(25.0 + dist * 1.5);
            if (toEntity.clone().normalize().angle(lookDirection) > coneHalfAngle) continue;

            double strength = knockback * (1.0 - dist / (range * 0.8));
            if (strength <= 0) continue;

            entitiesInCone.add(entity);

            // Add to existing velocity each tick, slight Y lift so entities arc upward
            Vector push = lookDirection.clone().multiply(strength);
            push.setY(push.getY() + 0.04);
            Vector newVelocity = entity.getVelocity().add(push);
            if (newVelocity.length() > knockback) {
                newVelocity = newVelocity.normalize().multiply(knockback);
            }
            GeneralMethods.setVelocity(this, entity, newVelocity);
        }

        // Entities that just left the cone get a final burst so they fly past the range boundary (makes it seem more like a wind burst)
        for (Entity entity : previouslyInCone) {
            if (entitiesInCone.contains(entity)) continue;
            Vector burst = lookDirection.clone().multiply(knockback * 0.4);
            GeneralMethods.setVelocity(this, entity, entity.getVelocity().add(burst));
        }

        previouslyInCone = entitiesInCone;
    }

    /**
     * Propels the player via recoil when their breath hits a solid surface or a heavy
     * entity (iron golem, sniffer, warden, ravager). Heavy entities act as walls
     * unless the player is in avatar state.
     */
    private void handleSelfPush() {
        Location mouthLocation = player.getEyeLocation().subtract(0, .2, 0);
        Vector lookDirection = mouthLocation.getDirection().normalize();
        double reach = currentReach();

        RayTraceResult blockResult = player.getWorld().rayTraceBlocks(mouthLocation, lookDirection, reach, FluidCollisionMode.NEVER, true);
        double hitDistance = blockResult != null && blockResult.getHitBlock() != null
                ? blockResult.getHitPosition().distance(mouthLocation.toVector())
                : reach + 1;

        // Heavy entities act as a wall for recoil, but only when not in avatar state
        if (!bPlayer.isAvatarState()) {
            RayTraceResult entityRayTraceResult = player.getWorld().rayTraceEntities(mouthLocation, lookDirection, reach, 0.5, entity -> !entity.equals(player) && GeneralMethods.isHeavyEntity(entity));
            if (entityRayTraceResult != null && entityRayTraceResult.getHitEntity() != null) {
                double entityDist = entityRayTraceResult.getHitPosition().distance(mouthLocation.toVector());
                if (entityDist < hitDistance) {
                    hitDistance = entityDist;
                }
            }
        }

        if (hitDistance > reach) return;

        double strength = selfPushStrength * (1.0 + (range - hitDistance) / range);
        Vector recoil = lookDirection.clone().multiply(-strength);
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
        Location mouthLocation = player.getEyeLocation().subtract(0, .2, 0);
        Vector lookDirection = mouthLocation.getDirection().normalize();
        double reach = currentReach();

        for (Entity entity : GeneralMethods.getEntitiesAroundPoint(mouthLocation, reach)) {
            if (!(entity instanceof Projectile projectile)) continue;
            if (player.equals(projectile.getShooter())) continue;
            if (RegionProtection.isRegionProtected(this, entity.getLocation())) continue;

            Vector toProjectile = entity.getLocation().toVector().subtract(mouthLocation.toVector());
            double dist = toProjectile.length();
            if (dist < 0.1) continue;

            double coneHalfAngle = Math.toRadians(25.0 + dist * 1.5);
            if (toProjectile.clone().normalize().angle(lookDirection) > coneHalfAngle) continue;

            double strength = knockback * (1.0 - dist / (range * 0.8));
            if (strength <= 0) continue;

            Vector newVelocity = entity.getVelocity().add(lookDirection.clone().multiply(strength));
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

    private BreathContext createBreathContext() {
        Location mouth = getMouthLocation();
        return new BreathContext(mouth, mouth.getDirection().normalize(), currentReach());
    }

    private Location getMouthLocation() {
        return player.getEyeLocation().subtract(0, 0.2, 0);
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

    private record BreathContext(Location mouthLocation, Vector lookDirection, double reach) {}

}

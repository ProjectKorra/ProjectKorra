package com.projectkorra.projectkorra.waterbending.ice;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.ability.SubAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.attribute.markers.DayNightFactor;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class FrostBreath extends IceAbility implements SubAbility {

    /* General config variables */
    @Attribute(Attribute.COOLDOWN)
    @DayNightFactor(invert = true)
    private long cooldown;
    @Attribute(Attribute.DURATION)
    @DayNightFactor
    private long breathDuration;
    @Attribute(Attribute.RANGE)
    @DayNightFactor
    private double range;

    /* Frost effect config */
    private double frostDamage;
    private boolean frostEffect;

    /* Ice tempblocks config */
    private boolean iceBlocks;
    @Attribute("IceDuration")
    @DayNightFactor
    private long iceDuration;
    private boolean iceDamage;
    long timeRequired;

    /* Visuals config */
    private int particleCount;
    @Attribute(Attribute.WIDTH)
    @DayNightFactor
    private double particleExpansion;

    /* Snow tempblocks config */
    private boolean snow;
    private boolean bendableSnow;
    private long snowDuration;

    /* Non-config variables */
    Location abilLoc;
    List<String> biomeList = new ArrayList<>();
    HashMap<Entity, Long> breathTime = new HashMap<Entity, Long>();

    public FrostBreath(Player player) {
        super(player);

        if (bPlayer.canBend(this) && !hasAbility(player, FrostBreath.class)) {
            setFields();
            if(!biomeList.contains(player.getEyeLocation().getBlock().getBiome().toString().toUpperCase())){
                setCollisions();
                start();
            }
        }
    }

    private void setFields() {
        /* Unspecified general config values */
        breathDuration = ConfigManager.getConfig().getLong("Abilities.Water.FrostBreath.BreathDuration");
        cooldown = ConfigManager.getConfig().getLong("Abilities.Water.FrostBreath.Cooldown");
        range = ConfigManager.getConfig().getDouble("Abilities.Water.FrostBreath.Range");

        /* Particle config values */
        particleCount = ConfigManager.getConfig().getInt("Abilities.Water.FrostBreath.Particle.ParticleCount");
        particleExpansion = ConfigManager.getConfig().getDouble("Abilities.Water.FrostBreath.Particle.ParticleExpansion");

        /* Frost effect config values */
        frostDamage = ConfigManager.getConfig().getDouble("Abilities.Water.FrostBreath.FrostEffect.Damage");
        frostEffect = ConfigManager.getConfig().getBoolean("Abilities.Water.FrostBreath.FrostEffect.Enabled");

        /* Snow config */
        snow = ConfigManager.getConfig().getBoolean("Abilities.Water.FrostBreath.Snow.Enabled");
        bendableSnow = ConfigManager.getConfig().getBoolean("Abilities.Water.FrostBreath.Snow.Bendable");
        snowDuration = ConfigManager.getConfig().getLong("Abilities.Water.FrostBreath.Snow.Duration");

        /* Ice Tempblocks Config */
        iceBlocks = ConfigManager.getConfig().getBoolean("Abilities.Water.FrostBreath.Ice.Enabled");
        iceDuration = ConfigManager.getConfig().getLong("Abilities.Water.FrostBreath.Ice.BlockDuration");
        iceDamage = ConfigManager.getConfig().getBoolean("Abilities.Water.FrostBreath.Ice.Damage");
        timeRequired = ConfigManager.getConfig().getLong("Abilities.Water.FrostBreath.Ice.BreathTimeRequiredToFreeze");


        for (String s : ConfigManager.getConfig().getStringList("Abilities.Water.FrostBreath.DisallowedBiomes")) {
            biomeList.add(s);
        }
    }

    private void setCollisions() {
        ProjectKorra.collisionManager.addCollision(new Collision(CoreAbility.getAbility("FrostBreath"), CoreAbility.getAbility("AirShield"), true, false));
        ProjectKorra.collisionManager.addCollision(new Collision(CoreAbility.getAbility("FrostBreath"), CoreAbility.getAbility("FireShield"), true, true));
    }

    private void breathAnimation() {
        Location origin, target;
        origin = player.getEyeLocation().add(0, -0.2, 0).add(player.getEyeLocation().getDirection().multiply(0.5));

        origin.setX(origin.getX() + new Random().nextDouble(-0.03, 0.03));
        origin.setY(origin.getY() + new Random().nextDouble(-0.03, 0.03));
        origin.setZ(origin.getZ() + new Random().nextDouble(-0.03, 0.03));

        target = player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(range));
        if (GeneralMethods.getMCVersion() > 1203) {
            origin.getWorld().playSound(origin, Sound.valueOf("ENTITY_BREEZE_IDLE_GROUND"), 0.33f, 1);
        } else {
            origin.getWorld().playSound(origin, Sound.ENTITY_HORSE_BREATHE, 0.33f, 1);
        }
        double vX = target.toVector().subtract(origin.toVector()).getX();
        double vY = target.toVector().subtract(origin.toVector()).getY();
        double vZ = target.toVector().subtract(origin.toVector()).getZ();

        for (int i = 0; i <= particleCount; i++) {
            player.getWorld().spawnParticle(Particle.BUBBLE_POP, origin, 0, vX + new Random().nextDouble(particleExpansion * -1, Math.abs(particleExpansion)), vY + new Random().nextDouble(particleExpansion * -1, Math.abs(particleExpansion)), vZ + new Random().nextDouble(particleExpansion * -1, Math.abs(particleExpansion)), range / 120, null, true);
            player.getWorld().spawnParticle(Particle.SNOWFLAKE, origin, 0, vX + new Random().nextDouble(particleExpansion * -1, Math.abs(particleExpansion)), vY + new Random().nextDouble(particleExpansion * -1, Math.abs(particleExpansion)), vZ + new Random().nextDouble(particleExpansion * -1, Math.abs(particleExpansion)), range / 120, null, true);
        }
    }

    private void formHitbox() {
        Location ability, origin;
        ability = player.getEyeLocation().add(0, -0.1, 0);
        origin = ability.clone();

        while (ability.distance(origin) < range) {
            this.abilLoc = ability.clone();
            ability.add(ability.getDirection());

            if (!ability.getBlock().isPassable() && ability.getBlock().getType() != Material.ICE) {
                break;
            }

            for (Entity entity : GeneralMethods.getEntitiesAroundPoint(ability, 1.25 + (0.1 * ability.distance(origin)))) {
                if (entity instanceof LivingEntity && entity != player) {
                    if (frostEffect) {
                        if (entity.getFreezeTicks() < 130) {
                            entity.setFreezeTicks(entity.getFreezeTicks() + 4);
                        }
                        if (frostDamage > 0) {
                            DamageHandler.damageEntity(entity, frostDamage, this);
                        }
                    }
                    entity.setFireTicks(0);

                    if (iceBlocks) {
                        breathTime.putIfAbsent(entity, System.currentTimeMillis());
                        if (breathTime.get(entity) + timeRequired <= System.currentTimeMillis()) {
                            setIce((LivingEntity) entity);
                        }

                    }
                }
            }
        }
    }

    private void setIce(LivingEntity entity) {
        Location loc = entity.getLocation().add(0, 0.46875, 0);
        List<Block> toFreeze = new ArrayList<Block>();

        toFreeze.add(entity.getEyeLocation().add(0, 1, 0).getBlock());

        for (Block block : GeneralMethods.getBlocksAroundPoint(loc, 1.7)) {
            toFreeze.add(block);
        }

        for (Block block : toFreeze) {
            if (block.isPassable()) {
                TempBlock ice = new TempBlock(block, Material.ICE.createBlockData(), iceDuration, this);
                ice.setCanSuffocate(iceDamage);
                ice.setBendableSource(false);
            }
        }

    }

    private void formSnow() {
        Location origin, target;
        origin = player.getEyeLocation().add(0, -0.3, 0).add(player.getEyeLocation().getDirection().multiply(0.3));
        target = origin.clone();
        while (target.distance(origin) < range) {
            target.add(player.getEyeLocation().getDirection());
            List<Block> snowList = new ArrayList<Block>();

            if (!target.getBlock().isPassable()) {
                break;
            }

            for (Block block : GeneralMethods.getBlocksAroundPoint(target.clone().add(0, -1, 0), 1 + ((target.distance(origin) / range) * particleExpansion))) {
                if (!block.isPassable()) {
                    Block upperBlock = block.getLocation().add(0, 1, 0).getBlock();
                    if (upperBlock.isPassable() && upperBlock.getType() != Material.WATER && block.getType() != Material.ICE && block.getType() != Material.SNOW) {
                        snowList.add(block.getLocation().add(0, 1, 0).getBlock());
                    }
                } else if (block.getType() == Material.WATER) {
                    TempBlock ice = new TempBlock(block, Material.ICE.createBlockData(), snowDuration);
                    ice.setBendableSource(true);
                    ice.setCanSuffocate(false);
                }
            }


            for (Block block : snowList) {
                TempBlock snow = new TempBlock(block, Material.SNOW.createBlockData(), snowDuration);
                snow.setBendableSource(bendableSnow);
            }
        }

    }


    @Override
    public void progress() {
        if (!player.isSneaking() || !player.isOnline() || !bPlayer.canBend(this) || breathDuration + getStartTime() < System.currentTimeMillis()) {
            remove();
            bPlayer.addCooldown(this);
            return;
        }

        breathAnimation();
        formHitbox();
        setCollisions();
        if (snow) {
            formSnow();
        }
    }

    @Override
    public boolean isSneakAbility() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return ConfigManager.getConfig().getBoolean("Abilities.Water.FrostBreath.Enabled");
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
        return "FrostBreath";
    }

    @Override
    public Location getLocation() {
        if (this.abilLoc != null) {
            return this.abilLoc;
        } else {
            return null;
        }
    }

    @Override
    public String getDescription() {
        return ConfigManager.languageConfig.get().getString("Abilities.Water.FrostBreath.Description");
    }

    @Override
    public String getInstructions() {
        return ConfigManager.languageConfig.get().getString("Abilities.Water.FrostBreath.Instructions");
    }

}
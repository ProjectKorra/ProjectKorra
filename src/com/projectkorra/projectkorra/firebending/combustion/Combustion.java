package com.projectkorra.projectkorra.firebending.combustion;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.CombustionAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

public class Combustion extends CombustionAbility {

	private static final int MAX_TICKS = 10000;

	private boolean breakBlocks;
	private int ticks;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute("ExplosivePower")
	private float explosivePower;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.RADIUS)
	private double radius;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.RANGE)
	private double range;
	private double speedFactor;
	private Location location;
	private Location origin;
	private Vector direction;

	public Combustion(final Player player) {
		super(player);

		if (hasAbility(player, Combustion.class) || !this.bPlayer.canBend(this)) {
			return;
		}

		this.ticks = 0;
		this.breakBlocks = getConfig().getBoolean("Abilities.Fire.Combustion.BreakBlocks");
		this.explosivePower = (float) getConfig().getDouble("Abilities.Fire.Combustion.ExplosivePower");
		this.cooldown = getConfig().getLong("Abilities.Fire.Combustion.Cooldown");
		this.damage = getConfig().getDouble("Abilities.Fire.Combustion.Damage");
		this.radius = getConfig().getDouble("Abilities.Fire.Combustion.Radius");
		this.speed = getConfig().getDouble("Abilities.Fire.Combustion.Speed");
		this.range = getConfig().getDouble("Abilities.Fire.Combustion.Range");
		this.origin = player.getEyeLocation();
		this.direction = player.getEyeLocation().getDirection().normalize();
		this.location = this.origin.clone();

		if (this.bPlayer.isAvatarState()) {
			this.range = AvatarState.getValue(this.range);
			this.damage = AvatarState.getValue(this.damage);
		} else if (isDay(player.getWorld())) {
			this.range = this.getDayFactor(this.range);
			this.damage = this.getDayFactor(this.damage);
		}

		if (GeneralMethods.isRegionProtectedFromBuild(this, GeneralMethods.getTargetedLocation(player, this.range))) {
			return;
		}

		this.start();
		this.bPlayer.addCooldown(this);
	}

	public static void explode(final Player player) {
		final Combustion combustion = getAbility(player, Combustion.class);
		if (combustion != null) {
			combustion.createExplosion(combustion.location, combustion.explosivePower, combustion.breakBlocks);
			ParticleEffect.EXPLOSION_NORMAL.display(combustion.location, 3, Math.random(), Math.random(), Math.random(), 0);
		}
	}

	/**
	 * This method was used for the old collision detection system. Please see
	 * {@link Collision} for the new system.
	 */
	@Deprecated
	public static boolean removeAroundPoint(final Location loc, final double radius) {
		for (final Combustion combustion : getAbilities(Combustion.class)) {
			if (combustion.location.getWorld().equals(loc.getWorld())) {
				if (combustion.location.distanceSquared(loc) <= radius * radius) {
					explode(combustion.getPlayer());
					combustion.remove();
					return true;
				}
			}
		}
		return false;
	}

	private void advanceLocation() {
		ParticleEffect.FIREWORKS_SPARK.display(this.location, 2, .001, .001, .001, 0);
		ParticleEffect.CRIT.display(this.location, 3, Math.random() * 2, Math.random() * 2, Math.random() * 2, 0);
		playCombustionSound(this.location);
		this.location = this.location.add(this.direction.clone().multiply(this.speedFactor));
	}

	private void createExplosion(final Location block, final float power, final boolean canBreakBlocks) {
		if (canFireGrief()) {
			block.getWorld().createExplosion(block.getX(), block.getY(), block.getZ(), power, true, canBreakBlocks);
		}
		for (final Entity entity : block.getWorld().getEntities()) {
			if (entity instanceof LivingEntity) {
				if (entity.getLocation().distanceSquared(block) < this.radius * this.radius) { // They are close enough to the explosion.
					DamageHandler.damageEntity(entity, this.damage, this);
					AirAbility.breakBreathbendingHold(entity);
				}
			}
		}
		this.remove();
	}

	@Override
	public void progress() {
		if (!this.bPlayer.canBendIgnoreCooldowns(this)) {
			this.remove();
			return;
		} else if (GeneralMethods.isRegionProtectedFromBuild(this, this.location)) {
			this.remove();
			return;
		}

		this.speedFactor = this.speed * (ProjectKorra.time_step / 1000.0);
		this.ticks++;
		if (this.ticks > MAX_TICKS) {
			this.remove();
			return;
		} else if (this.location.distanceSquared(this.origin) > this.range * this.range) {
			this.remove();
			return;
		}

		final Block block = this.location.getBlock();
		if (block != null) {
			if (!ElementalAbility.isAir(block.getType()) && !isWater(block)) {
				this.createExplosion(block.getLocation(), this.explosivePower, this.breakBlocks);
			}
		}

		for (final Entity entity : this.location.getWorld().getEntities()) {
			if (entity instanceof LivingEntity) {
				if (entity.getLocation().distanceSquared(this.location) <= 4 && !entity.equals(this.player)) {
					this.createExplosion(this.location, this.explosivePower, this.breakBlocks);
				}
			}
		}
		this.advanceLocation();
	}

	@Override
	public String getName() {
		return "Combustion";
	}

	@Override
	public Location getLocation() {
		if (this.location != null) {
			return this.location;
		}
		return this.origin;
	}

	@Override
	public long getCooldown() {
		return this.cooldown;
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
	public double getCollisionRadius() {
		return this.getRadius();
	}

	public boolean isBreakBlocks() {
		return this.breakBlocks;
	}

	public void setBreakBlocks(final boolean breakBlocks) {
		this.breakBlocks = breakBlocks;
	}

	public int getTicks() {
		return this.ticks;
	}

	public void setTicks(final int ticks) {
		this.ticks = ticks;
	}

	public float getExplosivePower() {
		return this.explosivePower;
	}

	public void setExplosivePower(final float explosivePower) {
		this.explosivePower = explosivePower;
	}

	public double getDamage() {
		return this.damage;
	}

	public void setDamage(final double damage) {
		this.damage = damage;
	}

	public double getRadius() {
		return this.radius;
	}

	public void setRadius(final double radius) {
		this.radius = radius;
	}

	public double getSpeed() {
		return this.speed;
	}

	public void setSpeed(final double speed) {
		this.speed = speed;
	}

	public double getRange() {
		return this.range;
	}

	public void setRange(final double range) {
		this.range = range;
	}

	public double getSpeedFactor() {
		return this.speedFactor;
	}

	public void setSpeedFactor(final double speedFactor) {
		this.speedFactor = speedFactor;
	}

	public Location getOrigin() {
		return this.origin;
	}

	public void setOrigin(final Location origin) {
		this.origin = origin;
	}

	public Vector getDirection() {
		return this.direction;
	}

	public void setDirection(final Vector direction) {
		this.direction = direction;
	}

	public static long getMaxTicks() {
		return MAX_TICKS;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

}

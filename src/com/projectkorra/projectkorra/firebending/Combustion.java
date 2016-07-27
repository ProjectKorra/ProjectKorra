package com.projectkorra.projectkorra.firebending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.CombustionAbility;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Combustion extends CombustionAbility {

	private static final int MAX_TICKS = 10000;
	
	private boolean breakBlocks;
	private int ticks;
	private long cooldown;
	private float power;
	private double damage;
	private double radius;
	private double speed;
	private double range;
	private double speedFactor;
	private Location location;
	private Location origin;
	private Vector direction;

	public Combustion(Player player) {
		super(player);
		
		if (hasAbility(player, Combustion.class) || !bPlayer.canBend(this)) {
			return;
		}
		
		this.ticks = 0;
		this.breakBlocks = getConfig().getBoolean("Abilities.Fire.Combustion.BreakBlocks");
		this.power = (float) getConfig().getDouble("Abilities.Fire.Combustion.Power");
		this.cooldown = getConfig().getLong("Abilities.Fire.Combustion.Cooldown");
		this.damage = getConfig().getDouble("Abilities.Fire.Combustion.Damage");
		this.radius = getConfig().getDouble("Abilities.Fire.Combustion.Radius");
		this.speed = getConfig().getDouble("Abilities.Fire.Combustion.Speed");
		this.range = getConfig().getDouble("Abilities.Fire.Combustion.Range");
		this.origin = player.getEyeLocation();
		this.direction = player.getEyeLocation().getDirection().normalize();
		this.location = origin.clone();
		
		if (bPlayer.isAvatarState()) {
			range = AvatarState.getValue(range);
			damage = AvatarState.getValue(damage);
		} else if (isDay(player.getWorld())) {
			range = getDayFactor(range);
			damage = getDayFactor(damage);
		}

		if (GeneralMethods.isRegionProtectedFromBuild(this, GeneralMethods.getTargetedLocation(player, range))) {
			return;
		}

		start();
		bPlayer.addCooldown(this);
	}

	public static void explode(Player player) {
		Combustion combustion = getAbility(player, Combustion.class);
		if (combustion != null) {
			combustion.createExplosion(combustion.location, combustion.power, combustion.breakBlocks);
			ParticleEffect.EXPLODE.display(combustion.location, (float) Math.random(), (float) Math.random(),
					(float) Math.random(), 0, 3);
		}
	}

	public static boolean removeAroundPoint(Location loc, double radius) {
		for (Combustion combustion : getAbilities(Combustion.class)) {
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
		ParticleEffect.FIREWORKS_SPARK.display(location, (float) Math.random() / 2, (float) Math.random() / 2, (float) Math.random() / 2, 0, 5);
		ParticleEffect.FLAME.display(location, (float) Math.random() / 2, (float) Math.random() / 2, (float) Math.random() / 2, 0, 2);
		playCombustionSound(location);
		location = location.add(direction.clone().multiply(speedFactor));
	}

	private void createExplosion(Location block, float power, boolean canBreakBlocks) {
			if(canFireGrief()) {
				block.getWorld().createExplosion(block.getX(), block.getY(), block.getZ(), power, true, canBreakBlocks);
			}
			for (Entity entity : block.getWorld().getEntities()) {
				if (entity instanceof LivingEntity) {
					if (entity.getLocation().distanceSquared(block) < radius * radius) { // They are close enough to the explosion.
						DamageHandler.damageEntity((LivingEntity) entity, damage, this);
						AirAbility.breakBreathbendingHold(entity);
				}
			}
		}
		remove();
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			remove();
			return;
		} else if (GeneralMethods.isRegionProtectedFromBuild(this, location)) {
			remove();
			return;
		}

		speedFactor = speed * (ProjectKorra.time_step / 1000.0);
		ticks++;
		if (ticks > MAX_TICKS) {
			remove();
			return;
		} else if (location.distanceSquared(origin) > range * range) {
			remove();
			return;
		}

		Block block = location.getBlock();
		if (block != null) {
			if (block.getType() != Material.AIR && !isWater(block)) {
				createExplosion(block.getLocation(), power, breakBlocks);
			}
		}

		for (Entity entity : location.getWorld().getEntities()) {
			if (entity instanceof LivingEntity) {
				if (entity.getLocation().distanceSquared(location) <= 4 && !entity.equals(player)) {
					createExplosion(location, power, breakBlocks);
				}
			}
		}
		advanceLocation();
	}

	@Override
	public String getName() {
		return "Combustion";
	}

	@Override
	public Location getLocation() {
		if (location != null) {
			return location;
		}
		return origin;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}
	
	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public boolean isBreakBlocks() {
		return breakBlocks;
	}

	public void setBreakBlocks(boolean breakBlocks) {
		this.breakBlocks = breakBlocks;
	}

	public int getTicks() {
		return ticks;
	}

	public void setTicks(int ticks) {
		this.ticks = ticks;
	}

	public float getPower() {
		return power;
	}

	public void setPower(float power) {
		this.power = power;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getSpeedFactor() {
		return speedFactor;
	}

	public void setSpeedFactor(double speedFactor) {
		this.speedFactor = speedFactor;
	}

	public Location getOrigin() {
		return origin;
	}

	public void setOrigin(Location origin) {
		this.origin = origin;
	}

	public Vector getDirection() {
		return direction;
	}

	public void setDirection(Vector direction) {
		this.direction = direction;
	}

	public static long getMaxTicks() {
		return MAX_TICKS;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	
}

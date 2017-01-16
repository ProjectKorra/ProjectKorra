package com.projectkorra.projectkorra.firebending;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.firebending.util.FireDamageTimer;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

public class WallOfFire extends FireAbility {

	private boolean active;
	private int damageTick;
	private int intervalTick;
	private int range;
	private int height;
	private int width;
	private int damage;
	private long cooldown;
	private long damageInterval;
	private long duration;
	private long time;
	private long interval;
	private double fireTicks;
	private double maxAngle;
	private Random random;
	private Location origin;
	private List<Block> blocks;

	public WallOfFire(Player player) {
		super(player);

		this.active = true;
		this.maxAngle = getConfig().getDouble("Abilities.Fire.WallOfFire.MaxAngle");
		this.interval = getConfig().getLong("Abilities.Fire.WallOfFire.Interval");
		this.range = getConfig().getInt("Abilities.Fire.WallOfFire.Range");
		this.height = getConfig().getInt("Abilities.Fire.WallOfFire.Height");
		this.width = getConfig().getInt("Abilities.Fire.WallOfFire.Width");
		this.damage = getConfig().getInt("Abilities.Fire.WallOfFire.Damage");
		this.cooldown = getConfig().getLong("Abilities.Fire.WallOfFire.Cooldown");
		this.damageInterval = getConfig().getLong("Abilities.Fire.WallOfFire.DamageInterval");
		this.duration = getConfig().getLong("Abilities.Fire.WallOfFire.Duration");
		this.fireTicks = getConfig().getDouble("Abilities.Fire.WallOfFire.FireTicks");
		this.random = new Random();
		this.blocks = new ArrayList<>();

		if (hasAbility(player, WallOfFire.class) && !bPlayer.isAvatarState()) {
			return;
		} else if (bPlayer.isOnCooldown(this)) {
			return;
		}

		origin = GeneralMethods.getTargetedLocation(player, range);

		if (isDay(player.getWorld())) {
			width = (int) getDayFactor(width);
			height = (int) getDayFactor(height);
			duration = (long) getDayFactor(duration);
			damage = (int) getDayFactor(damage);
		}

		time = System.currentTimeMillis();
		Block block = origin.getBlock();
		if (block.isLiquid() || GeneralMethods.isSolid(block)) {
			return;
		}

		Vector direction = player.getEyeLocation().getDirection();
		Vector compare = direction.clone();
		compare.setY(0);
		if (Math.abs(direction.angle(compare)) > Math.toRadians(maxAngle)) {
			return;
		}

		initializeBlocks();
		start();
		bPlayer.addCooldown(this);
	}

	private void affect(Entity entity) {
		GeneralMethods.setVelocity(entity, new Vector(0, 0, 0));
		if (entity instanceof LivingEntity) {
			Block block = ((LivingEntity) entity).getEyeLocation().getBlock();
			if (TempBlock.isTempBlock(block) && isIce(block)) {
				return;
			}
			DamageHandler.damageEntity(entity, damage, this);
			AirAbility.breakBreathbendingHold(entity);
		}
		entity.setFireTicks((int) (fireTicks * 20));
		new FireDamageTimer(entity, player);
	}

	private void damage() {
		double radius = height;
		if (radius < width) {
			radius = width;
		}

		radius = radius + 1;
		List<Entity> entities = GeneralMethods.getEntitiesAroundPoint(origin, radius);
		if (entities.contains(player)) {
			entities.remove(player);
		}
		for (Entity entity : entities) {
			if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation())) {
				continue;
			}
			for (Block block : blocks) {
				if (entity.getLocation().distanceSquared(block.getLocation()) <= 1.5 * 1.5) {
					affect(entity);
					break;
				}
			}
		}
	}

	private void display() {
		for (Block block : blocks) {
			if (!isTransparent(block)) {
				continue;
			}
			ParticleEffect.FLAME.display(block.getLocation(), 0.6F, 0.6F, 0.6F, 0, 3);
			ParticleEffect.SMOKE.display(block.getLocation(), 0.6F, 0.6F, 0.6F, 0, 1);

			if (random.nextInt(7) == 0) {
				playFirebendingSound(block.getLocation());
			}
		}
	}

	private void initializeBlocks() {
		Vector direction = player.getEyeLocation().getDirection();
		direction = direction.normalize();

		Vector ortholr = GeneralMethods.getOrthogonalVector(direction, 0, 1);
		ortholr = ortholr.normalize();

		Vector orthoud = GeneralMethods.getOrthogonalVector(direction, 90, 1);
		orthoud = orthoud.normalize();

		double w = width;
		double h = height;

		for (double i = -w; i <= w; i++) {
			for (double j = -h; j <= h; j++) {
				Location location = origin.clone().add(orthoud.clone().multiply(j));
				location = location.add(ortholr.clone().multiply(i));
				if (GeneralMethods.isRegionProtectedFromBuild(this, location)) {
					continue;
				}
				Block block = location.getBlock();
				if (!blocks.contains(block)) {
					blocks.add(block);
				}
			}
		}
	}

	@Override
	public void progress() {
		time = System.currentTimeMillis();

		if (time - getStartTime() > cooldown) {
			remove();
			return;
		} else if (!active) {
			return;
		} else if (time - getStartTime() > duration) {
			active = false;
			return;
		}

		if (time - getStartTime() > intervalTick * interval) {
			intervalTick++;
			display();
		}

		if (time - getStartTime() > damageTick * damageInterval) {
			damageTick++;
			damage();
		}
	}

	@Override
	public String getName() {
		return "WallOfFire";
	}

	@Override
	public Location getLocation() {
		return origin;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public List<Location> getLocations() {
		ArrayList<Location> locations = new ArrayList<>();
		for (Block block : blocks) {
			locations.add(block.getLocation());
		}
		return locations;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public int getDamageTick() {
		return damageTick;
	}

	public void setDamageTick(int damageTick) {
		this.damageTick = damageTick;
	}

	public int getIntervalTick() {
		return intervalTick;
	}

	public void setIntervalTick(int intervalTick) {
		this.intervalTick = intervalTick;
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

	public int getDamage() {
		return damage;
	}

	public void setDamage(int damage) {
		this.damage = damage;
	}

	public long getDamageInterval() {
		return damageInterval;
	}

	public void setDamageInterval(long damageInterval) {
		this.damageInterval = damageInterval;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public double getFireTicks() {
		return fireTicks;
	}

	public void setFireTicks(double fireTicks) {
		this.fireTicks = fireTicks;
	}

	public double getMaxAngle() {
		return maxAngle;
	}

	public void setMaxAngle(double maxAngle) {
		this.maxAngle = maxAngle;
	}

	public Location getOrigin() {
		return origin;
	}

	public void setOrigin(Location origin) {
		this.origin = origin;
	}

	public List<Block> getBlocks() {
		return blocks;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

}

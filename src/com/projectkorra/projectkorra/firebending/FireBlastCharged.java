package com.projectkorra.projectkorra.firebending;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.BlueFireAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

public class FireBlastCharged extends FireAbility {

	private static final Map<Entity, FireBlastCharged> EXPLOSIONS = new ConcurrentHashMap<>();

	private boolean charged;
	private boolean launched;
	private boolean canDamageBlocks;
	private boolean dissipate;
	private long time;
	@Attribute(Attribute.CHARGE_DURATION)
	private long chargeTime;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	private long interval;
	@Attribute(Attribute.DAMAGE)
	private double maxDamage;
	@Attribute(Attribute.RANGE)
	private double range;
	private double collisionRadius;
	@Attribute(Attribute.DAMAGE + Attribute.RANGE)
	private double damageRadius;
	@Attribute("Explosion" + Attribute.RANGE)
	private double explosionRadius;
	private double innerRadius;
	@Attribute(Attribute.FIRE_TICK)
	private double fireTicks;
	private TNTPrimed explosion;
	private Location origin;
	private Location location;
	private Vector direction;

	public FireBlastCharged(final Player player) {
		super(player);

		if (!this.bPlayer.canBend(this) || hasAbility(player, FireBlastCharged.class)) {
			return;
		}

		this.charged = false;
		this.launched = false;
		this.canDamageBlocks = getConfig().getBoolean("Abilities.Fire.FireBlast.Charged.DamageBlocks");
		this.dissipate = getConfig().getBoolean("Abilities.Fire.FireBlast.Dissipate");
		this.chargeTime = getConfig().getLong("Abilities.Fire.FireBlast.Charged.ChargeTime");
		this.cooldown = getConfig().getLong("Abilities.Fire.FireBlast.Charged.Cooldown");
		this.time = System.currentTimeMillis();
		this.interval = 25;
		this.collisionRadius = getConfig().getDouble("Abilities.Fire.FireBlast.Charged.CollisionRadius");
		this.maxDamage = getConfig().getDouble("Abilities.Fire.FireBlast.Charged.Damage");
		this.range = getConfig().getDouble("Abilities.Fire.FireBlast.Charged.Range");
		this.damageRadius = getConfig().getDouble("Abilities.Fire.FireBlast.Charged.DamageRadius");
		this.explosionRadius = getConfig().getDouble("Abilities.Fire.FireBlast.Charged.ExplosionRadius");
		this.fireTicks = getConfig().getDouble("Abilities.Fire.FireBlast.Charged.FireTicks");
		this.innerRadius = this.damageRadius / 2;


		this.applyModifiers();

		if (!player.getEyeLocation().getBlock().isLiquid()) {
			this.start();
		}
	}

	private void applyModifiers() {
		long chargeTimeMod = 0;
		double damageMod = 0;
		double rangeMod = 0;

		if (isDay(player.getWorld())) {
			chargeTimeMod = (long) (this.chargeTime / getDayFactor() - this.chargeTime);
			damageMod = this.getDayFactor(this.maxDamage) - this.maxDamage;
			rangeMod = this.getDayFactor(this.range) - this.range;
		}

		chargeTimeMod = (long) (bPlayer.canUseSubElement(SubElement.BLUE_FIRE) ? (chargeTime / BlueFireAbility.getCooldownFactor() - chargeTime) + chargeTimeMod : chargeTimeMod);
		damageMod = (bPlayer.canUseSubElement(SubElement.BLUE_FIRE) ? (BlueFireAbility.getDamageFactor() * maxDamage - maxDamage) + damageMod : damageMod);
		rangeMod =  (bPlayer.canUseSubElement(SubElement.BLUE_FIRE) ? (BlueFireAbility.getRangeFactor() * range - range) + rangeMod : rangeMod);

		if (this.bPlayer.isAvatarState()) {
			this.chargeTime = getConfig().getLong("Abilities.Avatar.AvatarState.Fire.FireBlast.Charged.ChargeTime");
			this.maxDamage = getConfig().getDouble("Abilities.Avatar.AvatarState.Fire.FireBlast.Charged.Damage");
		}

		this.chargeTime += chargeTimeMod;
		this.maxDamage += damageMod;
		this.range += rangeMod;
	}

	public static boolean annihilateBlasts(final Location location, final double radius, final Player source) {
		boolean broke = false;
		for (final FireBlastCharged chargedBlast : getAbilities(FireBlastCharged.class)) {
			if (!chargedBlast.launched) {
				continue;
			}

			final Location fireBlastLocation = chargedBlast.location;
			if (location.getWorld().equals(fireBlastLocation.getWorld()) && !source.equals(chargedBlast.player)) {
				if (location.distanceSquared(fireBlastLocation) <= radius * radius) {
					chargedBlast.explode();
					broke = true;
				}
			}
		}
		return broke;
	}

	public static FireBlastCharged getFireball(final Entity entity) {
		return entity != null ? EXPLOSIONS.get(entity) : null;
	}

	public static boolean isCharging(final Player player) {
		for (final FireBlastCharged chargedBlast : getAbilities(player, FireBlastCharged.class)) {
			if (!chargedBlast.launched) {
				return true;
			}
		}
		return false;
	}

	public static void removeFireballsAroundPoint(final Location location, final double radius) {
		for (final FireBlastCharged fireball : getAbilities(FireBlastCharged.class)) {
			if (!fireball.launched) {
				continue;
			}
			final Location fireblastlocation = fireball.location;
			if (location.getWorld().equals(fireblastlocation.getWorld())) {
				if (location.distanceSquared(fireblastlocation) <= radius * radius) {
					fireball.remove();
				}
			}
		}
	}

	public void dealDamage(final Entity entity) {
		if (this.explosion == null) {
			return;
		}

		double distance = 0;
		if (entity.getWorld().equals(this.explosion.getWorld())) {
			distance = entity.getLocation().distance(this.explosion.getLocation());
		}
		if (distance > this.damageRadius) {
			return;
		} else if (distance < this.innerRadius) {
			DamageHandler.damageEntity(entity, this.maxDamage, this);
			return;
		}

		final double slope = -(this.maxDamage * .5) / (this.damageRadius - this.innerRadius);
		final double damage = slope * (distance - this.innerRadius) + this.maxDamage;
		DamageHandler.damageEntity(entity, damage, this);
		AirAbility.breakBreathbendingHold(entity);
	}

	public void explode() {
		boolean explode = true;
		for (final Block block : GeneralMethods.getBlocksAroundPoint(this.location, 3)) {
			if (GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
				explode = false;
				break;
			}
		}

		if (explode) {
			if (this.canDamageBlocks && this.explosionRadius > 0 && canFireGrief()) {
				this.explosion = this.player.getWorld().spawn(this.location, TNTPrimed.class);
				this.explosion.setFuseTicks(0);
				double yield = this.explosionRadius;

				if (!this.bPlayer.isAvatarState()) {
					yield = getDayFactor(yield, this.player.getWorld());
				} else {
					yield = AvatarState.getValue(yield);
				}

				this.explosion.setYield((float) yield);
				EXPLOSIONS.put(this.explosion, this);
			} else {
				final List<Entity> entities = GeneralMethods.getEntitiesAroundPoint(this.location, this.damageRadius);
				for (final Entity entity : entities) {
					if (entity instanceof LivingEntity) {
						final double slope = -(this.maxDamage * .5) / (this.damageRadius - this.innerRadius);
						double damage = 0;
						if (entity.getWorld().equals(this.location.getWorld())) {
							damage = slope * (entity.getLocation().distance(this.location) - this.innerRadius) + this.maxDamage;
						}
						DamageHandler.damageEntity(entity, damage, this);
					}
				}
				this.location.getWorld().playSound(this.location, Sound.ENTITY_GENERIC_EXPLODE, 5, 1);
				ParticleEffect.EXPLOSION_HUGE.display(this.location, 1, 0, 0, 0);
			}
		}

		this.ignite(this.location);
		this.remove();
	}

	private void executeFireball() {
		for (final Block block : GeneralMethods.getBlocksAroundPoint(this.location, this.collisionRadius)) {
			playFirebendingParticles(block.getLocation(), 5, 0.5, 0.5, 0.5);
			if ((new Random()).nextInt(4) == 0) {
				playFirebendingSound(this.location);
			}

		}

		boolean exploded = false;
		for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(this.location, this.collisionRadius)) {
			if (entity.getEntityId() == this.player.getEntityId() || GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation())) {
				continue;
			}
			entity.setFireTicks((int) (this.fireTicks * 20));
			if (entity instanceof LivingEntity) {
				if (!exploded) {
					this.explode();
					exploded = true;
				}
				this.dealDamage(entity);
			}
		}
	}

	private void ignite(final Location location) {
		for (final Block block : GeneralMethods.getBlocksAroundPoint(location, this.collisionRadius)) {
			if (isIgnitable(block)) {
				createTempFire(block.getLocation());
			}
		}
	}

	@Override
	public void progress() {
		if (!this.bPlayer.canBendIgnoreBinds(this) && !this.launched) {
			this.remove();
			return;
		} else if (!this.bPlayer.canBendIgnoreCooldowns(CoreAbility.getAbility("FireBlast")) && !this.launched) {
			this.remove();
			return;
		} else if (!this.player.isSneaking() && !this.charged) {
			this.remove();
			return;
		}

		if (System.currentTimeMillis() > this.getStartTime() + this.chargeTime) {
			this.charged = true;
		}
		if (!this.player.isSneaking() && !this.launched) {
			this.launched = true;
			this.location = this.player.getEyeLocation();
			this.origin = this.location.clone();
			this.direction = this.location.getDirection().normalize().multiply(this.collisionRadius);
		}

		if (System.currentTimeMillis() > this.time + this.interval) {
			if (this.launched) {
				if (GeneralMethods.isRegionProtectedFromBuild(this, this.location)) {
					this.remove();
					return;
				}
			}

			this.time = System.currentTimeMillis();

			if (!this.launched && !this.charged) {
				return;
			} else if (!this.launched) {
				playFirebendingParticles(this.player.getEyeLocation().clone().add(this.player.getEyeLocation().getDirection().clone()), 3, .001, .001, .001);
				return;
			}

			if (GeneralMethods.checkDiagonalWall(this.location, this.direction)) {
				this.explode();
				return;
			}
			this.location = this.location.clone().add(this.direction);
			if (this.location.distanceSquared(this.origin) > this.range * this.range) {
				this.remove();
				return;
			}

			if (GeneralMethods.isSolid(this.location.getBlock())) {
				this.explode();
				return;
			} else if (this.location.getBlock().isLiquid()) {
				this.remove();
				return;
			}
			this.executeFireball();
		}
	}

	@Override
	public void remove() {
		super.remove();
		if (this.charged) {
			this.bPlayer.addCooldown(this);
		}
	}

	@Override
	public String getName() {
		return "FireBlast";
	}

	@Override
	public Location getLocation() {
		return this.location != null ? this.location : this.origin;
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
	public boolean isCollidable() {
		return this.launched;
	}

	@Override
	public double getCollisionRadius() {
		return this.collisionRadius;
	}

	public boolean isCharged() {
		return this.charged;
	}

	public void setCharged(final boolean charged) {
		this.charged = charged;
	}

	public boolean isLaunched() {
		return this.launched;
	}

	public void setLaunched(final boolean launched) {
		this.launched = launched;
	}

	public boolean isCanDamageBlocks() {
		return this.canDamageBlocks;
	}

	public void setCanDamageBlocks(final boolean canDamageBlocks) {
		this.canDamageBlocks = canDamageBlocks;
	}

	public boolean isDissipate() {
		return this.dissipate;
	}

	public void setDissipate(final boolean dissipate) {
		this.dissipate = dissipate;
	}

	public long getTime() {
		return this.time;
	}

	public void setTime(final long time) {
		this.time = time;
	}

	public long getChargeTime() {
		return this.chargeTime;
	}

	public void setChargeTime(final long chargeTime) {
		this.chargeTime = chargeTime;
	}

	public long getInterval() {
		return this.interval;
	}

	public void setInterval(final long interval) {
		this.interval = interval;
	}

	public double getMaxDamage() {
		return this.maxDamage;
	}

	public void setMaxDamage(final double maxDamage) {
		this.maxDamage = maxDamage;
	}

	public double getRange() {
		return this.range;
	}

	public void setRange(final double range) {
		this.range = range;
	}

	public void setCollisionRadius(final double collisionRadius) {
		this.collisionRadius = collisionRadius;
	}

	public double getDamageRadius() {
		return this.damageRadius;
	}

	public void setDamageRadius(final double damageRadius) {
		this.damageRadius = damageRadius;
	}

	public double getExplosionRadius() {
		return this.explosionRadius;
	}

	public void setExplosionRadius(final double explosionRadius) {
		this.explosionRadius = explosionRadius;
	}

	public double getInnerRadius() {
		return this.innerRadius;
	}

	public void setInnerRadius(final double innerRadius) {
		this.innerRadius = innerRadius;
	}

	public double getFireTicks() {
		return this.fireTicks;
	}

	public void setFireTicks(final double fireTicks) {
		this.fireTicks = fireTicks;
	}

	public TNTPrimed getExplosion() {
		return this.explosion;
	}

	public void setExplosion(final TNTPrimed explosion) {
		this.explosion = explosion;
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

	public static Map<Entity, FireBlastCharged> getExplosions() {
		return EXPLOSIONS;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

}

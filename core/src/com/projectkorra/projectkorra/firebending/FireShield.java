package com.projectkorra.projectkorra.firebending;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.projectkorra.projectkorra.attribute.markers.DayNightFactor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.firebending.util.FireDamageTimer;

public class FireShield extends FireAbility {

	private boolean shield;
	@Attribute("IgniteEntities")
	private boolean ignite;
	@Attribute("Disc" + Attribute.DURATION) @DayNightFactor
	private long discDuration;
	@Attribute("Shield" + Attribute.DURATION) @DayNightFactor
	private long shieldDuration;
	@Attribute("Disc" + Attribute.COOLDOWN) @DayNightFactor(invert = true)
	private long discCooldown;
	@Attribute("Shield" + Attribute.COOLDOWN) @DayNightFactor(invert = true)
	private long shieldCooldown;
	@Attribute("Shield" + Attribute.RADIUS) @DayNightFactor
	private double shieldRadius;
	@Attribute("Disc" + Attribute.RADIUS) @DayNightFactor
	private double discRadius;
	@Attribute("Disc" + Attribute.FIRE_TICK) @DayNightFactor
	private double discFireTicks;
	@Attribute("Shield" + Attribute.FIRE_TICK) @DayNightFactor
	private double shieldFireTicks;
	private Location location;
	private Random random;
	private int increment = 20;

	public FireShield(final Player player) {
		this(player, false);
	}

	public FireShield(final Player player, final boolean shield) {
		super(player);

		this.shield = shield;
		this.ignite = true;
		this.discCooldown = getConfig().getLong("Abilities.Fire.FireShield.Disc.Cooldown");
		this.discDuration = getConfig().getLong("Abilities.Fire.FireShield.Disc.Duration");
		this.discRadius = getConfig().getDouble("Abilities.Fire.FireShield.Disc.Radius");
		this.discFireTicks = getConfig().getDouble("Abilities.Fire.FireShield.Disc.FireTicks");
		this.shieldCooldown = getConfig().getLong("Abilities.Fire.FireShield.Shield.Cooldown");
		this.shieldDuration = getConfig().getLong("Abilities.Fire.FireShield.Shield.Duration");
		this.shieldRadius = getConfig().getDouble("Abilities.Fire.FireShield.Shield.Radius");
		this.shieldFireTicks = getConfig().getDouble("Abilities.Fire.FireShield.Shield.FireTicks");
		this.random = new Random();

		if (hasAbility(player, FireShield.class) || this.bPlayer.isOnCooldown("FireShield")) {
			return;
		} else if (!player.getEyeLocation().getBlock().isLiquid()) {
			this.start();
			if (!shield) {
				this.bPlayer.addCooldown(this);
			}
		}
	}

	/**
	 * This method was used for the old collision detection system. Please see
	 * {@link Collision} for the new system.
	 */
	@Deprecated
	public static boolean isWithinShield(final Location loc) {
		for (final FireShield fshield : getAbilities(FireShield.class)) {
			final Location playerLoc = fshield.player.getLocation();

			if (fshield.shield) {
				if (!playerLoc.getWorld().equals(loc.getWorld())) {
					return false;
				} else if (playerLoc.distanceSquared(loc) <= fshield.shieldRadius * fshield.shieldRadius) {
					return true;
				}
			} else {
				final Location tempLoc = playerLoc.clone().add(playerLoc.multiply(fshield.discRadius));
				if (!tempLoc.getWorld().equals(loc.getWorld())) {
					return false;
				} else if (tempLoc.getWorld().equals(loc.getWorld()) && tempLoc.distance(loc) <= fshield.discRadius * fshield.discRadius) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void progress() {
		if (!this.bPlayer.canBendIgnoreCooldowns(this)) {
			this.bPlayer.addCooldown(this);
			this.remove();
			return;
		} else if ((!this.player.isSneaking() && this.shield) || (System.currentTimeMillis() > this.getStartTime() + this.shieldDuration && this.shield && this.shieldDuration > 0)) {
			this.bPlayer.addCooldown(this);
			this.remove();
			return;
		} else if (System.currentTimeMillis() > this.getStartTime() + this.discDuration && !this.shield) {
			this.bPlayer.addCooldown(this);
			this.remove();
			return;
		}

		if (this.shield) {
			this.location = this.player.getEyeLocation().clone();

			for (double theta = 0; theta < 180; theta += this.increment) {
				for (double phi = 0; phi < 360; phi += this.increment) {
					final double rphi = Math.toRadians(phi);
					final double rtheta = Math.toRadians(theta);

					final Location display = this.location.clone().add(this.shieldRadius / 1.5 * Math.cos(rphi) * Math.sin(rtheta), this.shieldRadius / 1.5 * Math.cos(rtheta), this.shieldRadius / 1.5 * Math.sin(rphi) * Math.sin(rtheta));
					emitFirebendingLight(display);

					if (this.random.nextInt(4) == 0) {
						playFirebendingParticles(display, 1, 0.1, 0.1, 0.1);
					}
					if (this.random.nextInt(7) == 0) {
						playFirebendingSound(display);
					}
				}
			}

			this.increment += 20;
			if (this.increment >= 70) {
				this.increment = 20;
			}

			for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(this.location, this.shieldRadius)) {
				if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation())) {
					continue;
				} else if (entity instanceof LivingEntity) {
					if (this.player.getEntityId() != entity.getEntityId() && this.ignite) {
						entity.setFireTicks((int) (this.shieldFireTicks * 20));
						new FireDamageTimer(entity, this.player, this);
					}
				} else if (entity instanceof Projectile) {
					entity.remove();
				}
			}
			for (final Block block : GeneralMethods.getBlocksAroundPoint(this.location, this.shieldRadius)) {
				dryWetBlocks(block, this, ThreadLocalRandom.current().nextInt(5) == 0);
			}
		} else {
			this.location = this.player.getEyeLocation().clone();
			final Vector direction = this.location.getDirection();
			this.location.add(direction.multiply(this.shieldRadius));

			playFirebendingParticles(this.location, 3, 0.2, 0.2, 0.2);
			emitFirebendingLight(this.location);

			for (double theta = 0; theta < 360; theta += 20) {
				final Vector vector = GeneralMethods.getOrthogonalVector(direction, theta, this.discRadius / 1.5);
				final Location display = this.location.add(vector);

				playFirebendingParticles(display, 2, 0.3, 0.2, 0.3);
				emitFirebendingLight(display);

				if (this.random.nextInt(4) == 0) {
					playFirebendingSound(display);
				}
				this.location.subtract(vector);
			}

			for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(this.location, this.discRadius + 1)) {
				if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation())) {
					continue;
				} else if (entity instanceof LivingEntity) {
					if (this.player.getEntityId() != entity.getEntityId() && this.ignite) {
						entity.setFireTicks((int) (this.discFireTicks * 20));
						new FireDamageTimer(entity, this.player, this);
					}
				} else if (entity instanceof Projectile) {
					entity.remove();
				}
			}
			for (final Block block : GeneralMethods.getBlocksAroundPoint(this.location, this.discRadius)) {
				dryWetBlocks(block, this, ThreadLocalRandom.current().nextInt(5) == 0);
			}
		}
	}

	@Override
	public String getName() {
		return "FireShield";
	}

	@Override
	public Location getLocation() {
		return this.location;
	}

	@Override
	public long getCooldown() {
		return this.shield ? this.shieldCooldown : this.discCooldown;
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
		return this.shield ? this.shieldRadius : this.discRadius;
	}

	public boolean isShield() {
		return this.shield;
	}

	public void setShield(final boolean shield) {
		this.shield = shield;
	}

	public boolean isIgnite() {
		return this.ignite;
	}

	public void setIgnite(final boolean ignite) {
		this.ignite = ignite;
	}

	public long getDuration() {
		return this.shield ? this.shieldDuration : this.discDuration;
	}

	public void setDiscDuration(final long duration) {
		this.discDuration = duration;
	}

	public void setShieldDuration(final long duration) {
		this.shieldDuration = duration;
	}

	public double getShieldRadius() {
		return this.shieldRadius;
	}

	public void setShieldRadius(final double shieldRadius) {
		this.shieldRadius = shieldRadius;
	}

	public double getDiscRadius() {
		return this.discRadius;
	}

	public void setDiscRadius(final double discRadius) {
		this.discRadius = discRadius;
	}

	public double getFireTicks(final boolean shield) {
		return shield ? this.shieldFireTicks : this.discFireTicks;
	}

	public void setFireTicks(final double fireTicks, final boolean shield) {
		if (shield) {
			this.shieldFireTicks = fireTicks;
		} else {
			this.discFireTicks = fireTicks;
		}
	}

	public void setDiscCooldown(final long cooldown) {
		this.discCooldown = cooldown;
	}

	public void setShieldCooldown(final long cooldown) {
		this.shieldCooldown = cooldown;
	}

}

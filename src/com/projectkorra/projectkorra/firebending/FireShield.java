package com.projectkorra.projectkorra.firebending;

import java.util.Random;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.firebending.util.FireDamageTimer;
import com.projectkorra.projectkorra.util.ParticleEffect;

public class FireShield extends FireAbility {

	private boolean shield;
	private boolean ignite;
	private long discDuration;
	private long shieldDuration;
	private long interval;
	private long discCooldown;
	private long shieldCooldown;
	private double radius;
	private double discRadius;
	private double discFireTicks;
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
		this.radius = getConfig().getDouble("Abilities.Fire.FireShield.Shield.Radius");
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
				} else if (playerLoc.distanceSquared(loc) <= fshield.radius * fshield.radius) {
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
			this.remove();
			return;
		} else if ((!this.player.isSneaking() && this.shield) || (System.currentTimeMillis() > this.getStartTime() + this.shieldDuration && this.shield && this.shieldDuration > 0)) {
			this.remove();
			return;
		} else if (System.currentTimeMillis() > this.getStartTime() + this.discDuration && !this.shield) {
			this.remove();
			return;
		}

		if (this.shield) {
			this.location = this.player.getEyeLocation().clone();

			for (double theta = 0; theta < 180; theta += this.increment) {
				for (double phi = 0; phi < 360; phi += this.increment) {
					final double rphi = Math.toRadians(phi);
					final double rtheta = Math.toRadians(theta);

					final Location display = this.location.clone().add(this.radius / 1.5 * Math.cos(rphi) * Math.sin(rtheta), this.radius / 1.5 * Math.cos(rtheta), this.radius / 1.5 * Math.sin(rphi) * Math.sin(rtheta));
					if (this.random.nextInt(6) == 0) {
						ParticleEffect.SMOKE.display(display, 0, 0, 0, 0, 1);
					}
					if (this.random.nextInt(4) == 0) {
						ParticleEffect.FLAME.display(display, 0.1f, 0.1f, 0.1f, 0.013f, 1);
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

			for (final Block testblock : GeneralMethods.getBlocksAroundPoint(this.player.getLocation(), this.radius)) {
				if (testblock.getType() == Material.FIRE) {
					testblock.setType(Material.AIR);
					testblock.getWorld().playEffect(testblock.getLocation(), Effect.EXTINGUISH, 0);
				}
			}

			for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(this.location, this.radius)) {
				if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation())) {
					continue;
				} else if (entity instanceof LivingEntity) {
					if (this.player.getEntityId() != entity.getEntityId() && this.ignite) {
						entity.setFireTicks((int) (this.shieldFireTicks * 20));
						new FireDamageTimer(entity, this.player);
					}
				} else if (entity instanceof Projectile) {
					entity.remove();
				}
			}
		} else {
			this.location = this.player.getEyeLocation().clone();
			final Vector direction = this.location.getDirection();
			this.location = this.location.clone().add(direction.multiply(this.radius));
			ParticleEffect.FLAME.display(this.location, 0.2f, 0.2f, 0.2f, 0.023f, 3);

			for (double theta = 0; theta < 360; theta += 20) {
				final Vector vector = GeneralMethods.getOrthogonalVector(direction, theta, this.discRadius / 1.5);
				final Location display = this.location.clone().add(vector);
				if (this.random.nextInt(6) == 0) {
					ParticleEffect.SMOKE.display(display, 0, 0, 0, 0, 1);
				}
				ParticleEffect.FLAME.display(display, 0.3f, 0.2f, 0.3f, 0.023f, 2);
				if (this.random.nextInt(4) == 0) {
					playFirebendingSound(display);
				}
			}

			for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(this.location, this.discRadius)) {
				if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation())) {
					continue;
				} else if (entity instanceof LivingEntity) {
					if (this.player.getEntityId() != entity.getEntityId() && this.ignite) {
						entity.setFireTicks((int) (this.discFireTicks * 20));
						new FireDamageTimer(entity, this.player);
					}
				} else if (entity instanceof Projectile) {
					entity.remove();
				}
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
		return this.shield ? this.radius : this.discRadius;
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

	public long getInterval() {
		return this.interval;
	}

	public void setInterval(final long interval) {
		this.interval = interval;
	}

	public double getRadius() {
		return this.radius;
	}

	public void setRadius(final double radius) {
		this.radius = radius;
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

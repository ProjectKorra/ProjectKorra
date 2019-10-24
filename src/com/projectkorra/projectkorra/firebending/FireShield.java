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
import com.projectkorra.projectkorra.ability.api.FireAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.configuration.configs.abilities.fire.FireShieldConfig;
import com.projectkorra.projectkorra.firebending.util.FireDamageTimer;
import com.projectkorra.projectkorra.util.ParticleEffect;

public class FireShield extends FireAbility<FireShieldConfig> {

	private boolean shield;
	@Attribute("IgniteEntities")
	private boolean ignite;
	@Attribute("Disc" + Attribute.DURATION)
	private long discDuration;
	@Attribute("Shield" + Attribute.DURATION)
	private long shieldDuration;
	@Attribute("Disc" + Attribute.COOLDOWN)
	private long discCooldown;
	@Attribute("Shield" + Attribute.COOLDOWN)
	private long shieldCooldown;
	@Attribute("Shield" + Attribute.RADIUS)
	private double shieldRadius;
	@Attribute("Disc" + Attribute.RADIUS)
	private double discRadius;
	@Attribute("Disc" + Attribute.FIRE_TICK)
	private double discFireTicks;
	@Attribute("Shield" + Attribute.FIRE_TICK)
	private double shieldFireTicks;
	private Location location;
	private Random random;
	private int increment = 20;

	public FireShield(final FireShieldConfig config, final Player player) {
		this(config, player, false);
	}

	public FireShield(final FireShieldConfig config, final Player player, final boolean shield) {
		super(config, player);

		this.shield = shield;
		this.ignite = true;
		this.discCooldown = config.DiscConfig.Cooldown;
		this.discDuration = config.DiscConfig.Duration;
		this.discRadius = config.DiscConfig.Radius;
		this.discFireTicks = config.DiscConfig.FireTicks;
		this.shieldCooldown = config.ShieldConfig.Cooldown;
		this.shieldDuration = config.ShieldConfig.Duration;
		this.shieldRadius = config.ShieldConfig.Radius;
		this.shieldFireTicks = config.ShieldConfig.FireTicks;
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
					if (this.random.nextInt(6) == 0) {
						ParticleEffect.SMOKE_NORMAL.display(display, 1, 0, 0, 0);
					}
					if (this.random.nextInt(4) == 0) {
						ParticleEffect.FLAME.display(display, 1, 0.1, 0.1, 0.1, 0.013);
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

			for (final Block testblock : GeneralMethods.getBlocksAroundPoint(this.player.getLocation(), this.shieldRadius)) {
				if (testblock.getType() == Material.FIRE) {
					testblock.setType(Material.AIR);
					testblock.getWorld().playEffect(testblock.getLocation(), Effect.EXTINGUISH, 0);
				}
			}

			for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(this.location, this.shieldRadius)) {
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
			this.location.add(direction.multiply(this.shieldRadius));
			ParticleEffect.FLAME.display(this.location, 3, 0.2, 0.2, 0.2, 0.00023);

			for (double theta = 0; theta < 360; theta += 20) {
				final Vector vector = GeneralMethods.getOrthogonalVector(direction, theta, this.discRadius / 1.5);
				final Location display = this.location.add(vector);
				if (this.random.nextInt(6) == 0) {
					ParticleEffect.SMOKE_NORMAL.display(display, 1, 0, 0, 0);
				}
				ParticleEffect.FLAME.display(display, 2, 0.3, 0.2, 0.3, 0.023);
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
	
	@Override
	public Class<FireShieldConfig> getConfigType() {
		return FireShieldConfig.class;
	}

}

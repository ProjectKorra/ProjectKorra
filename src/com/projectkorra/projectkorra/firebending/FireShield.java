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
	private double fireTicks;
	private Location location;
	private Random random;
	private int increment = 20;

	public FireShield(Player player) {
		this(player, false);
	}

	public FireShield(Player player, boolean shield) {
		super(player);

		this.shield = shield;
		this.ignite = true;
		this.discCooldown = getConfig().getLong("Abilities.Fire.FireShield.Disc.Cooldown");
		this.discDuration = getConfig().getLong("Abilities.Fire.FireShield.Disc.Duration");
		this.discRadius = getConfig().getDouble("Abilities.Fire.FireShield.Disc.Radius");
		this.shieldCooldown = getConfig().getLong("Abilities.Fire.FireShield.Shield.Cooldown");
		this.shieldDuration = getConfig().getLong("Abilities.Fire.FireShield.Shield.Duration");
		this.radius = getConfig().getDouble("Abilities.Fire.FireShield.Shield.Radius");
		this.fireTicks = getConfig().getDouble("Abilities.Fire.FireShield.FireTicks");
		this.random = new Random();

		if (hasAbility(player, FireShield.class) || bPlayer.isOnCooldown("FireShield")) {
			return;
		} else if (!player.getEyeLocation().getBlock().isLiquid()) {
			start();
			if (!shield) {
				bPlayer.addCooldown(this);
			}
		}
	}

	/**
	 * This method was used for the old collision detection system. Please see
	 * {@link Collision} for the new system.
	 */
	@Deprecated
	public static boolean isWithinShield(Location loc) {
		for (FireShield fshield : getAbilities(FireShield.class)) {
			Location playerLoc = fshield.player.getLocation();

			if (fshield.shield) {
				if (!playerLoc.getWorld().equals(loc.getWorld())) {
					return false;
				} else if (playerLoc.distanceSquared(loc) <= fshield.radius * fshield.radius) {
					return true;
				}
			} else {
				Location tempLoc = playerLoc.clone().add(playerLoc.multiply(fshield.discRadius));
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
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			remove();
			return;
		} else if ((!player.isSneaking() && shield) || (shieldDuration != 0 && System.currentTimeMillis() > getStartTime() + shieldDuration && shield)) {
			
			remove();
			return;
		} else if (System.currentTimeMillis() > getStartTime() + discDuration && !shield) {
			remove();
			return;
		}

		if (shield) {
			location = player.getEyeLocation().clone();

			for (double theta = 0; theta < 180; theta += increment) {
				for (double phi = 0; phi < 360; phi += increment) {
					double rphi = Math.toRadians(phi);
					double rtheta = Math.toRadians(theta);

					Location display = location.clone().add(radius/1.5 * Math.cos(rphi) * Math.sin(rtheta), radius/1.5 * Math.cos(rtheta), radius/1.5 * Math.sin(rphi) * Math.sin(rtheta));
					if (random.nextInt(6) == 0) {
						ParticleEffect.SMOKE.display(display, 0, 0, 0, 0, 1);
					}
					if (random.nextInt(4) == 0) {
						ParticleEffect.FLAME.display(display, 0.1f, 0.1f, 0.1f, 0.013f, 1);
					}
					if (random.nextInt(7) == 0) {
						playFirebendingSound(display);
					}
				}
			}
			
			increment += 20;
			if (increment >= 70) {
				increment = 20;
			}

			for (Block testblock : GeneralMethods.getBlocksAroundPoint(player.getLocation(), radius)) {
				if (testblock.getType() == Material.FIRE) {
					testblock.setType(Material.AIR);
					testblock.getWorld().playEffect(testblock.getLocation(), Effect.EXTINGUISH, 0);
				}
			}

			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, radius)) {
				if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation())) {
					continue;
				} else if (entity instanceof LivingEntity) { 
					if (player.getEntityId() != entity.getEntityId() && ignite) {
						entity.setFireTicks((int) (fireTicks * 20));
						new FireDamageTimer(entity, player);
					}
				} else if (entity instanceof Projectile) {
					entity.remove();
				}
			}
		} else {
			location = player.getEyeLocation().clone();
			Vector direction = location.getDirection();
			location = location.clone().add(direction.multiply(radius));
			ParticleEffect.FLAME.display(location, 0.2f, 0.2f, 0.2f, 0.023f, 3);

			for (double theta = 0; theta < 360; theta += 20) {
				Vector vector = GeneralMethods.getOrthogonalVector(direction, theta, discRadius/1.5);
				Location display = location.clone().add(vector);
				if (random.nextInt(6) == 0) {
					ParticleEffect.SMOKE.display(display, 0, 0, 0, 0, 1);
				}
				ParticleEffect.FLAME.display(display, 0.3f, 0.2f, 0.3f, 0.023f, 2);
				if (random.nextInt(4) == 0) {
					playFirebendingSound(display);
				}
			}

			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, discRadius)) {
				if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation())) {
					continue;
				} else if (entity instanceof LivingEntity) { 
					if (player.getEntityId() != entity.getEntityId() && ignite) {
						entity.setFireTicks((int) (fireTicks * 20));
						new FireDamageTimer(entity, player);
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
		return location;
	}

	@Override
	public long getCooldown() {
		return shield ? shieldCooldown : discCooldown;
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
		return shield ? radius : discRadius;
	}

	public boolean isShield() {
		return shield;
	}

	public void setShield(boolean shield) {
		this.shield = shield;
	}

	public boolean isIgnite() {
		return ignite;
	}

	public void setIgnite(boolean ignite) {
		this.ignite = ignite;
	}

	public long getDuration() {
		return shield ? shieldDuration : discDuration;
	}

	public void setDiscDuration(long duration) {
		this.discDuration = duration;
	}

	public void setShieldDuration(long duration) {
		this.shieldDuration = duration;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getDiscRadius() {
		return discRadius;
	}

	public void setDiscRadius(double discRadius) {
		this.discRadius = discRadius;
	}

	public double getFireTicks() {
		return fireTicks;
	}

	public void setFireTicks(double fireTicks) {
		this.fireTicks = fireTicks;
	}

	public void setDiscCooldown(long cooldown) {
		this.discCooldown = cooldown;
	}
	
	public void setShieldCooldown(long cooldown) {
		this.shieldCooldown = cooldown;
	}

}

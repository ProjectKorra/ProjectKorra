package com.projectkorra.projectkorra.airbending;

import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.region.RegionProtection;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.command.Commands;

public class AirShield extends AirAbility {

	@Attribute(Attribute.AVATAR_STATE_TOGGLE)
	private boolean isToggledByAvatarState;
	@Attribute("Max" + Attribute.RADIUS)
	private double maxRadius;
	@Attribute("Initial" + Attribute.RADIUS)
	private double initialRadius;
	private double radius;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.KNOCKBACK)
	private double pushFactor;
	private int streams;
	private int particles;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;
	private Random random;
	private HashMap<Integer, Integer> angles;
	private boolean dynamicCooldown;

	public AirShield(final Player player) {
		super(player);

		this.maxRadius = getConfig().getDouble("Abilities.Air.AirShield.MaxRadius");
		this.initialRadius = getConfig().getDouble("Abilities.Air.AirShield.InitialRadius");
		this.isToggledByAvatarState = ConfigManager.avatarStateConfig.get().getBoolean("Abilities.Air.AirShield.IsToggle");
		this.radius = this.initialRadius;
		this.cooldown = getConfig().getLong("Abilities.Air.AirShield.Cooldown");
		this.duration = getConfig().getLong("Abilities.Air.AirShield.Duration");
		this.speed = getConfig().getDouble("Abilities.Air.AirShield.Speed");
		this.pushFactor = getConfig().getDouble("Abilities.Air.AirShield.Push");
		this.streams = getConfig().getInt("Abilities.Air.AirShield.Streams");
		this.particles = getConfig().getInt("Abilities.Air.AirShield.Particles");
		this.dynamicCooldown = getConfig().getBoolean("Abilities.Air.AirShield.DynamicCooldown"); //any unused duration from shield is removed from the cooldown
		if (this.duration == 0) {
			this.dynamicCooldown = false;
		}
		this.random = new Random();
		this.angles = new HashMap<>();

		if (this.bPlayer.isAvatarState() && hasAbility(player, AirShield.class) && this.isToggledByAvatarState) {
			getAbility(player, AirShield.class).remove();
			return;
		}

		if (!this.bPlayer.canBend(this)) {
			return;
		}

		int angle = 0;
		final int di = Math.max(1, (int) (this.maxRadius * 2 / this.streams));
		for (int i = -(int) this.maxRadius + di; i < (int) this.maxRadius; i += di) {
			this.angles.put(i, angle);
			angle += 90;
			if (angle == 360) {
				angle = 0;
			}
		}

		this.start();
	}

	/**
	 * This method was used for the old collision detection system. Please see
	 * {@link Collision} for the new system.
	 */
	@Deprecated
	public static boolean isWithinShield(final Location loc) {
		for (final AirShield ashield : getAbilities(AirShield.class)) {
			if (!ashield.player.getWorld().equals(loc.getWorld())) {
				return false;
			} else if (ashield.player.getLocation().distanceSquared(loc) <= ashield.radius * ashield.radius) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void progress() {
		// AvatarState can use AirShield even when AirShield is not in the bound slot.
		if (this.player.getEyeLocation().getBlock().isLiquid()) {
			this.remove();
			return;
		} else if (!this.bPlayer.isAvatarState() || !this.isToggledByAvatarState) {
			if (!this.player.isSneaking() || !this.bPlayer.canBend(this)) {
				if (this.dynamicCooldown) {
					Long reducedCooldown = this.cooldown - (this.duration - (System.currentTimeMillis() - this.getStartTime()));
					if (reducedCooldown < 0L) {
						reducedCooldown = 0L;
					}
					this.bPlayer.addCooldown(this, reducedCooldown);
				} else {
					this.bPlayer.addCooldown(this);
				}
				this.remove();
				return;
			} else if (this.duration != 0) {
				if (this.getStartTime() + this.duration <= System.currentTimeMillis()) {
					this.bPlayer.addCooldown(this);
					this.remove();
					return;
				}
			}

		} else if (!this.bPlayer.canBendIgnoreBinds(this)) {
			this.remove();
			return;
		}
		this.rotateShield();
	}

	private void rotateShield() {
		final Location origin = this.player.getLocation();
		for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(origin, this.radius)) {
			if (RegionProtection.isRegionProtected(this.player, entity.getLocation(), "AirShield")) {
				continue;
			}
			if (origin.distanceSquared(entity.getLocation()) > 4) {
				double x, z, vx, vz, mag;
				double angle = 50;
				angle = Math.toRadians(angle);

				x = entity.getLocation().getX() - origin.getX();
				z = entity.getLocation().getZ() - origin.getZ();

				mag = Math.sqrt(x * x + z * z);

				vx = (x * Math.cos(angle) - z * Math.sin(angle)) / mag;
				vz = (x * Math.sin(angle) + z * Math.cos(angle)) / mag;

				final Vector velocity = entity.getVelocity().clone();

				velocity.setX(vx);
				velocity.setZ(vz);

				if (entity instanceof Player) {
					if (Commands.invincible.contains(((Player) entity).getName())) {
						continue;
					}
				}

				velocity.multiply(this.pushFactor);
				GeneralMethods.setVelocity(this, entity, velocity);
				entity.setFallDistance(0);
			}
		}

		for (final Block testblock : GeneralMethods.getBlocksAroundPoint(this.player.getLocation(), this.radius)) {
			if (FireAbility.isFire(testblock.getType())) {
				testblock.setType(Material.AIR);
				testblock.getWorld().playEffect(testblock.getLocation(), Effect.EXTINGUISH, 0);
			}
		}

		final Set<Integer> keys = this.angles.keySet();
		for (final int i : keys) {
			double x, y, z;
			final double factor = this.radius / this.maxRadius;
			double angle = this.angles.get(i);
			angle = Math.toRadians(angle);
			y = origin.getY() + factor * i;
			final double f = Math.sqrt(1 - factor * factor * (i / this.radius) * (i / this.radius));

			x = origin.getX() + this.radius * Math.cos(angle) * f;
			z = origin.getZ() + this.radius * Math.sin(angle) * f;

			final Location effect = new Location(origin.getWorld(), x, y, z);
			if (!GeneralMethods.isRegionProtectedFromBuild(this, effect)) {
				playAirbendingParticles(effect, this.particles);
				if (this.random.nextInt(4) == 0) {
					playAirbendingSound(effect);
				}
			}

			this.angles.put(i, this.angles.get(i) + (int) (this.speed));
		}

		if (this.radius < this.maxRadius) {
			this.radius += .3;
		}
		if (this.radius > this.maxRadius) {
			this.radius = this.maxRadius;
		}
	}

	@Override
	public String getName() {
		return "AirShield";
	}

	@Override
	public Location getLocation() {
		return this.player != null ? this.player.getLocation() : null;
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

	public boolean isToggledByAvatarState() {
		return this.isToggledByAvatarState;
	}

	public void setToggledByAvatarState(final boolean isToggledByAvatarState) {
		this.isToggledByAvatarState = isToggledByAvatarState;
	}

	public double getMaxRadius() {
		return this.maxRadius;
	}

	public void setMaxRadius(final double maxRadius) {
		this.maxRadius = maxRadius;
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

	public int getStreams() {
		return this.streams;
	}

	public void setStreams(final int streams) {
		this.streams = streams;
	}

	public int getParticles() {
		return this.particles;
	}

	public void setParticles(final int particles) {
		this.particles = particles;
	}

	public HashMap<Integer, Integer> getAngles() {
		return this.angles;
	}
}

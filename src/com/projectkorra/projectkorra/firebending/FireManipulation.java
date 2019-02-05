package com.projectkorra.projectkorra.firebending;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

public class FireManipulation extends FireAbility {

	public static enum FireManipulationType {
		SHIFT, CLICK;
	}

	// Configurable variables.
	private long streamCooldown;
	private double streamRange;
	private double streamDamage;
	private double streamSpeed;
	private int streamParticles;
	private boolean streamSneaking = true;
	private long streamRemoveTime = 0;
	private Vector streamSneakDirection;

	private long shieldCooldown;
	private double shieldRange;
	private double shieldDamage;
	private int shieldParticles;
	private long maxDuration;

	// Instance related variables.
	private FireManipulationType fireManipulationType;

	private boolean firing;
	private boolean charging;
	private Map<Location, Long> points;
	private Location shotPoint;
	private Location origin;
	private Location focalPoint;

	public FireManipulation(final Player player, final FireManipulationType fireManipulationType) {
		super(player);
		if (!this.bPlayer.canBend(this)) {
			return;
		}

		this.fireManipulationType = fireManipulationType;
		this.setFields();
		this.start();
	}

	public void setFields() {
		if (this.fireManipulationType == FireManipulationType.SHIFT) {
			this.streamCooldown = getConfig().getLong("Abilities.Fire.FireManipulation.Stream.Cooldown");
			this.streamRange = getConfig().getDouble("Abilities.Fire.FireManipulation.Stream.Range");
			this.streamDamage = getConfig().getDouble("Abilities.Fire.FireManipulation.Stream.Damage");
			this.streamSpeed = getConfig().getDouble("Abilities.Fire.FireManipulation.Stream.Speed");
			this.streamParticles = getConfig().getInt("Abilities.Fire.FireManipulation.Stream.Particles");

			this.shieldCooldown = getConfig().getLong("Abilities.Fire.FireManipulation.Shield.Cooldown");
			this.shieldRange = getConfig().getDouble("Abilities.Fire.FireManipulation.Shield.Range");
			this.shieldDamage = getConfig().getDouble("Abilities.Fire.FireManipulation.Shield.Damage");
			this.shieldParticles = getConfig().getInt("Abilities.Fire.FireManipulation.Shield.Particles");
			this.maxDuration = getConfig().getLong("Abilities.Fire.FireManipulation.Shield.MaxDuration");
			this.points = new ConcurrentHashMap<>();
		} else if (this.fireManipulationType == FireManipulationType.CLICK) {

		}
	}

	public void click() {
		if (System.currentTimeMillis() - this.getStartTime() > 1500) {
			if (!this.firing && !this.charging) {
				this.charging = true;
				this.focalPoint = GeneralMethods.getTargetedLocation(this.player, this.shieldRange * 2);
				this.origin = this.player.getLocation().clone();
			}
		}
	}

	@Override
	public void progress() {
		if (!this.bPlayer.canBend(this)) {
			this.remove();
			return;
		}

		if (!this.firing && !this.charging) {
			if (!this.player.isSneaking()) {
				this.bPlayer.addCooldown(this, this.shieldCooldown);
				this.remove();
				return;
			} else if (System.currentTimeMillis() - this.getStartTime() > this.maxDuration) {
				this.bPlayer.addCooldown(this, this.shieldCooldown);
				this.remove();
				return;
			}
			final Location targetLocation = GeneralMethods.getTargetedLocation(this.player, this.shieldRange);
			this.points.put(targetLocation, System.currentTimeMillis());
			for (final Location point : this.points.keySet()) {
				if (System.currentTimeMillis() - this.points.get(point) > 1500) {
					this.points.remove(point);
					return;
				}
				ParticleEffect.FLAME.display(point, 12, 0.25, 0.25, 0.25);
				ParticleEffect.SMOKE.display(point, 6, 0.25, 0.25, 0.25);
				for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(point, 1.2D)) {
					if (entity instanceof LivingEntity && entity.getUniqueId() != this.player.getUniqueId()) {
						DamageHandler.damageEntity(entity, this.shieldDamage, this);
					}
				}
				if (new Random().nextInt(this.points.keySet().size()) == 0) {
					playFirebendingSound(point);
				}
			}
		} else if (!this.firing && this.charging) {
			if (!this.player.isSneaking()) {
				this.remove();
				return;
			}
			boolean readyToFire = true;
			for (final Location point : this.points.keySet()) {
				if (point.distance(this.focalPoint) > 1) {
					readyToFire = false;
				}
			}
			if (readyToFire) {
				this.shotPoint = this.focalPoint.clone();
				this.firing = true;
				return;
			}
			for (final Location point : this.points.keySet()) {
				final Vector direction = this.focalPoint.toVector().subtract(point.toVector());
				point.add(direction.clone().multiply(this.streamSpeed / 5));
				ParticleEffect.FLAME.display(point, this.shieldParticles, 0.25, 0.25, 0.25);
				ParticleEffect.SMOKE.display(point, this.shieldParticles/2, 0.25, 0.25, 0.25);
			}
		} else {
			Vector direction = this.player.getLocation().getDirection().clone();
			if (this.streamSneaking && !this.player.isSneaking()) {
				this.streamSneaking = false;
				this.streamRemoveTime = System.currentTimeMillis();
				this.streamSneakDirection = direction;
			}
			if (!this.streamSneaking) {
				direction = this.streamSneakDirection;
				if (System.currentTimeMillis() - this.streamRemoveTime > 1000) {
					this.bPlayer.addCooldown(this, this.streamCooldown);
					this.remove();
					return;
				}
			}
			this.shotPoint.add(direction.multiply(this.streamSpeed));
			if (this.shotPoint.distance(this.origin) > this.streamRange) {
				this.bPlayer.addCooldown(this, this.streamCooldown);
				this.remove();
				return;
			}
			if (GeneralMethods.isSolid(this.shotPoint.getBlock())) {
				this.bPlayer.addCooldown(this);
				this.remove();
				return;
			}

			ParticleEffect.FLAME.display(this.shotPoint, this.streamParticles, 0.5, 0.5, 0.5, 0.01);
			ParticleEffect.SMOKE.display(this.shotPoint, this.streamParticles/2, 0.5, 0.5, 0.5, 0.01);
			for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(this.shotPoint, 2)) {
				if (entity instanceof LivingEntity && entity.getUniqueId() != this.player.getUniqueId()) {
					DamageHandler.damageEntity(entity, this.streamDamage, this);
				}
			}
			if (new Random().nextInt(5) == 0) {
				playFirebendingSound(this.shotPoint);
			}
		}
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
	public long getCooldown() {
		return 0;
	}

	@Override
	public String getName() {
		return "FireManipulation";
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public List<Location> getLocations() {
		final List<Location> locations = new ArrayList<>();
		locations.addAll(this.points.keySet());
		return locations;
	}

	public FireManipulationType getFireManipulationType() {
		return this.fireManipulationType;
	}

	@Override
	public double getCollisionRadius() {
		return 0.4;
	}
}

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

	// Configurable variables
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

	// Instance related variables
	private FireManipulationType fireManipulationType;

	private boolean firing;
	private boolean charging;
	private Map<Location, Long> points;
	private Location shotPoint;
	private Location origin;
	private Location focalPoint;

	public FireManipulation(Player player, FireManipulationType fireManipulationType) {
		super(player);
		if (!bPlayer.canBend(this)) {
			return;
		}

		this.fireManipulationType = fireManipulationType;
		setFields();
		start();
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
		if (System.currentTimeMillis() - getStartTime() > 1500) {
			if (!firing && !charging) {
				charging = true;
				focalPoint = GeneralMethods.getTargetedLocation(player, shieldRange * 2);
				origin = player.getLocation().clone();
			}
		}
	}

	@Override
	public void progress() {
		if (!bPlayer.canBend(this)) {
			remove();
			return;
		}

		if (!firing && !charging) {
			if (!player.isSneaking()) {
				remove();
				return;
			} else if (System.currentTimeMillis() - getStartTime() > maxDuration) {
				bPlayer.addCooldown(this, shieldCooldown);
				remove();
				return;
			}
			Location targetLocation = GeneralMethods.getTargetedLocation(player, shieldRange);
			points.put(targetLocation, System.currentTimeMillis());
			for (Location point : points.keySet()) {
				if (System.currentTimeMillis() - points.get(point) > 1500) {
					points.remove(point);
					return;
				}
				ParticleEffect.FLAME.display(point, 0.25F, 0.25F, 0.25F, 0, 12);
				ParticleEffect.SMOKE.display(point, 0.25F, 0.25F, 0.25F, 0, 6);
				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(point, 1.2D)) {
					if (entity instanceof LivingEntity && entity.getUniqueId() != player.getUniqueId()) {
						DamageHandler.damageEntity(entity, shieldDamage, this);
					}
				}
				if (new Random().nextInt(points.keySet().size()) == 0) {
					playFirebendingSound(point);
				}
			}
		} else if (!firing && charging) {
			if (!player.isSneaking()) {
				remove();
				return;
			}
			boolean readyToFire = true;
			for (Location point : points.keySet()) {
				if (point.distance(focalPoint) > 1) {
					readyToFire = false;
				}
			}
			if (readyToFire) {
				shotPoint = focalPoint.clone();
				firing = true;
				return;
			}
			for (Location point : points.keySet()) {
				Vector direction = focalPoint.toVector().subtract(point.toVector());
				point.add(direction.clone().multiply(streamSpeed / 5));
				ParticleEffect.FLAME.display(point, 0.25F, 0.25F, 0.25F, 0, shieldParticles);
				ParticleEffect.SMOKE.display(point, 0.25F, 0.25F, 0.25F, 0, shieldParticles / 2);
			}
		} else {
			Vector direction = player.getLocation().getDirection().clone();
			if (streamSneaking && !player.isSneaking()) {
				streamSneaking = false;
				streamRemoveTime = System.currentTimeMillis();
				streamSneakDirection = direction;
			}
			if (!streamSneaking) {
				direction = streamSneakDirection;
				if (System.currentTimeMillis() - streamRemoveTime > 1000) {
					remove();
					return;
				}
			}
			shotPoint.add(direction.multiply(streamSpeed));
			if (shotPoint.distance(origin) > streamRange) {
				bPlayer.addCooldown(this, streamCooldown);
				remove();
				return;
			}
			ParticleEffect.FLAME.display(shotPoint, 0.5F, 0.5F, 0.5F, 0.01F, streamParticles);
			ParticleEffect.SMOKE.display(shotPoint, 0.5F, 0.5F, 0.5F, 0.01F, streamParticles / 2);
			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(shotPoint, 2)) {
				if (entity instanceof LivingEntity && entity.getUniqueId() != player.getUniqueId()) {
					DamageHandler.damageEntity(entity, streamDamage, this);
				}
			}
			if (new Random().nextInt(5) == 0) {
				playFirebendingSound(shotPoint);
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
		List<Location> locations = new ArrayList<>();
		locations.addAll(points.keySet());
		return locations;
	}

	public FireManipulationType getFireManipulationType() {
		return fireManipulationType;
	}
	
	@Override
	public double getCollisionRadius() {
		return 0.4;
	}
}
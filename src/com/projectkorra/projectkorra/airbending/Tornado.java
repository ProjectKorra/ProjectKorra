package com.projectkorra.projectkorra.airbending;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.util.VelocityBuilder;

public class Tornado extends AirAbility {

	@Attribute(Attribute.COOLDOWN)
	private final long cooldown;
	@Attribute(Attribute.DURATION)
	private final long duration;
	private int numberOfStreams;
	private int particleCount;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.HEIGHT)
	private double maxHeight;
	@Attribute(Attribute.KNOCKBACK)
	private double playerPushFactor;
	@Attribute(Attribute.RADIUS)
	private double radius;
	@Attribute(Attribute.RANGE)
	private double range;
	private double npcPushFactor;
	private double currentHeight;
	private double currentRadius;
	private Location origin;
	private final Random random;
	private final Map<Integer, Double> angles;
	private VelocityBuilder velocity;

	public Tornado(final Player player) {
		super(player);

		this.cooldown = getConfig().getLong("Abilities.Air.Tornado.Cooldown");
		this.duration = getConfig().getLong("Abilities.Air.Tornado.Duration");
		this.range = getConfig().getDouble("Abilities.Air.Tornado.Range");
		this.origin = player.getTargetBlock((HashSet<Material>) null, (int) this.range).getLocation();
		this.origin.setY(this.origin.getY() - 1.0 / 10.0 * this.currentHeight);
		this.maxHeight = getConfig().getDouble("Abilities.Air.Tornado.Height");
		this.playerPushFactor = getConfig().getDouble("Abilities.Air.Tornado.PlayerPushFactor");
		this.radius = getConfig().getDouble("Abilities.Air.Tornado.Radius");
		this.npcPushFactor = getConfig().getDouble("Abilities.Air.Tornado.NpcPushFactor");
		this.speed = getConfig().getDouble("Abilities.Air.Tornado.Speed");
		this.numberOfStreams = getConfig().getInt("Abilities.Air.Tornado.StreamCount");
		this.currentHeight = 2;
		this.currentRadius = 0;
		this.random = new Random();
		this.angles = new ConcurrentHashMap<>();
		this.velocity = new VelocityBuilder();

		double angle = 0;
		for (int i = 0; i <= this.maxHeight; i += (int) this.maxHeight / this.numberOfStreams) {
			this.angles.put(i, angle);
			angle += 360 / this.numberOfStreams;
			if (angle >= 360) {
				angle -= 360;
			}
		}

		this.flightHandler.createInstance(player, this.getName());
		player.setAllowFlight(true);
		this.start();
	}

	@Override
	public void progress() {
		if (this.player.getEyeLocation().getBlock().isLiquid() || !this.player.isSneaking() || !this.bPlayer.canBend(this)) {
			this.bPlayer.addCooldown(this);
			this.remove();
			return;
		} else if (GeneralMethods.isRegionProtectedFromBuild(this, this.origin)) {
			this.remove();
			return;
		} else if (this.duration != 0) {
			if (this.getStartTime() + this.duration <= System.currentTimeMillis()) {
				this.bPlayer.addCooldown(this);
				this.remove();
				return;
			}
		}
		this.rotateTornado();
	}

	@Override
	public void remove() {
		super.remove();
		this.flightHandler.removeInstance(this.player, this.getName());
	}

	private void rotateTornado() {
		this.origin = GeneralMethods.getTargetedLocation(player, range, getTransparentMaterials()).subtract(0, 1, 0);
		final double timefactor = this.currentHeight / this.maxHeight;
		this.currentRadius = timefactor * this.radius;

		if (!ElementalAbility.isAir(this.origin.getBlock().getType()) && this.origin.getBlock().getType() != Material.BARRIER) {
			this.origin.setY(this.origin.getY() - 1. / 10. * this.currentHeight);

			for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(this.origin, this.currentHeight)) {
				if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation())) {
					continue;
				}
				final double y = entity.getLocation().getY();
				double factor;
				if (y > this.origin.getY() && y < this.origin.getY() + this.currentHeight) {
					factor = (y - this.origin.getY()) / this.currentHeight;
					final Location testloc = new Location(this.origin.getWorld(), this.origin.getX(), y, this.origin.getZ());
					if (testloc.getWorld().equals(entity.getWorld()) && testloc.distance(entity.getLocation()) < this.currentRadius * factor) {
						double x, z, vx, vz, mag;
						double angle = 100;
						double vy = 0.7 * this.npcPushFactor;
						angle = Math.toRadians(angle);

						x = entity.getLocation().getX() - this.origin.getX();
						z = entity.getLocation().getZ() - this.origin.getZ();

						mag = Math.sqrt(x * x + z * z);

						if (mag == 0.0) {
							vx = 0.0;
							vz = 0.0;
						} else {
							vx = (x * Math.cos(angle) - z * Math.sin(angle)) / mag;
							vz = (x * Math.sin(angle) + z * Math.cos(angle)) / mag;
						}

						if (entity instanceof Player) {
							vy = 0.05 * this.playerPushFactor;
						}

						if (entity.getEntityId() == this.player.getEntityId()) {
							final Vector direction = this.player.getEyeLocation().getDirection().clone().normalize();
							vx = direction.getX();
							vz = direction.getZ();
							final Location playerloc = this.player.getLocation();
							final double py = playerloc.getY();
							final double oy = this.origin.getY();
							final double dy = py - oy;

							if (dy >= this.currentHeight * .95) {
								vy = 0;
							} else if (dy >= this.currentHeight * .85) {
								vy = 6.0 * (.95 - dy / this.currentHeight);
							} else {
								vy = .6;
							}
						}

						if (entity instanceof Player) {
							if (Commands.invincible.contains(((Player) entity).getName())) {
								continue;
							}
						}

						Vector dir = new Vector(vx, vy, vz);
						this.velocity.direction(dir).knockback(dir.length() * timefactor).apply(entity, this);
						entity.setFallDistance(0);

						breakBreathbendingHold(entity);
					}
				}
			}
			
			double x, z;
			for (double j = 0; j < currentHeight; j += 0.4) {
				for (final int i : this.angles.keySet()) {
					if (Math.random() < 0.4) {
						continue;
					}
					
					double angle = this.angles.get(i);
					angle = Math.toRadians(angle);
	
					x = this.origin.getX() + (j / this.maxHeight) * this.radius * Math.cos(angle + j);
					z = this.origin.getZ() + (j / this.maxHeight) * this.radius * Math.sin(angle + j);
	
					final Location effect = new Location(this.origin.getWorld(), x, origin.getY() + j, z);
					if (!GeneralMethods.isRegionProtectedFromBuild(this, effect)) {
						playAirbendingParticles(effect, 1, 0.14, 0.14, 0.14);
						if (this.random.nextInt(20) == 0) {
							playAirbendingSound(effect);
						}
					}
				}
			}
		}
		
		for (int i : this.angles.keySet()) {
			this.angles.put(i, this.angles.get(i) + this.speed * 360 / this.numberOfStreams / 10);
		}
		
		this.currentHeight = this.currentHeight > this.maxHeight ? this.maxHeight : this.currentHeight + 1;
	}

	@Override
	public String getName() {
		return "Tornado";
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

	public Location getOrigin() {
		return this.origin;
	}

	public void setOrigin(final Location origin) {
		this.origin = origin;
	}

	public int getNumberOfStreams() {
		return this.numberOfStreams;
	}

	public void setNumberOfStreams(final int numberOfStreams) {
		this.numberOfStreams = numberOfStreams;
	}

	public int getParticleCount() {
		return this.particleCount;
	}

	public void setParticleCount(final int particleCount) {
		this.particleCount = particleCount;
	}

	public double getSpeed() {
		return this.speed;
	}

	public void setSpeed(final double speed) {
		this.speed = speed;
	}

	public double getMaxHeight() {
		return this.maxHeight;
	}

	public void setMaxHeight(final double maxHeight) {
		this.maxHeight = maxHeight;
	}

	public double getPlayerPushFactor() {
		return this.playerPushFactor;
	}

	public void setPlayerPushFactor(final double playerPushFactor) {
		this.playerPushFactor = playerPushFactor;
	}

	public double getRadius() {
		return this.radius;
	}

	public void setRadius(final double radius) {
		this.radius = radius;
	}

	public double getRange() {
		return this.range;
	}

	public void setRange(final double range) {
		this.range = range;
	}

	public double getNpcPushFactor() {
		return this.npcPushFactor;
	}

	public void setNpcPushFactor(final double npcPushFactor) {
		this.npcPushFactor = npcPushFactor;
	}

	public double getCurrentHeight() {
		return this.currentHeight;
	}

	public void setCurrentHeight(final double currentHeight) {
		this.currentHeight = currentHeight;
	}

	public double getCurrentRadius() {
		return this.currentRadius;
	}

	public void setCurrentRadius(final double currentRadius) {
		this.currentRadius = currentRadius;
	}

	public Map<Integer, Double> getAngles() {
		return this.angles;
	}
}

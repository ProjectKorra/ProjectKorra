package com.projectkorra.projectkorra.airbending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.util.Flight;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class Tornado extends AirAbility {

	private int numberOfStreams;
	private int particleCount;
	private double speed;
	private double maxHeight;
	private double playerPushFactor;
	private double radius;
	private double range;
	private double npcPushFactor;
	private double currentHeight;
	private double currentRadius;
	private boolean couldFly;
	private Flight flight;
	private Location origin;
	private Random random;
	private Map<Integer, Integer> angles;

	public Tornado(Player player) {
		super(player);
		
		this.range = getConfig().getDouble("Abilities.Air.Tornado.Range");
		this.origin = player.getTargetBlock((HashSet<Material>) null, (int) range).getLocation();
		this.origin.setY(origin.getY() - 1.0 / 10.0 * currentHeight);
		this.maxHeight = getConfig().getDouble("Abilities.Air.Tornado.Height");
		this.playerPushFactor = getConfig().getDouble("Abilities.Air.Tornado.PlayerPushFactor");
		this.radius = getConfig().getDouble("Abilities.Air.Tornado.Radius");
		this.npcPushFactor = getConfig().getDouble("Abilities.Air.Tornado.NpcPushFactor");
		this.speed = getConfig().getDouble("Abilities.Air.Tornado.Speed");
		this.numberOfStreams = (int) (.3 * (double) maxHeight);
		this.currentHeight = 2;
		this.currentRadius = currentHeight / maxHeight * radius;
		this.random = new Random();
		this.angles = new ConcurrentHashMap<>();

		int angle = 0;
		for (int i = 0; i <= maxHeight; i += (int) maxHeight / numberOfStreams) {
			angles.put(i, angle);
			angle += 90;
			if (angle == 360) {
				angle = 0;
			}
		}

		this.flight = new Flight(player);
		this.couldFly = player.getAllowFlight();
		player.setAllowFlight(true);
		start();
	}

	@Override
	public void progress() {
		if (player.getEyeLocation().getBlock().isLiquid() || !player.isSneaking() || !bPlayer.canBendIgnoreCooldowns(this)) {
			remove();
			return;
		} else if (GeneralMethods.isRegionProtectedFromBuild(this, origin)) {
			remove();
			return;
		}
		rotateTornado();
	}
	
	@Override
	public void remove() {
		super.remove();
		flight.remove();
		player.setAllowFlight(couldFly);
	}

	private void rotateTornado() {
		origin = player.getTargetBlock((HashSet<Material>) null, (int) range).getLocation();
		double timefactor = currentHeight / maxHeight;
		currentRadius = timefactor * radius;

		if (origin.getBlock().getType() != Material.AIR && origin.getBlock().getType() != Material.BARRIER) {
			origin.setY(origin.getY() - 1. / 10. * currentHeight);

			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(origin, currentHeight)) {
				if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation())) {
					continue;
				}
				double y = entity.getLocation().getY();
				double factor;
				if (y > origin.getY() && y < origin.getY() + currentHeight) {
					factor = (y - origin.getY()) / currentHeight;
					Location testloc = new Location(origin.getWorld(), origin.getX(), y, origin.getZ());
					if (testloc.distance(entity.getLocation()) < currentRadius * factor) {
						double x, z, vx, vz, mag;
						double angle = 100;
						double vy = 0.7 * npcPushFactor;
						angle = Math.toRadians(angle);

						x = entity.getLocation().getX() - origin.getX();
						z = entity.getLocation().getZ() - origin.getZ();

						mag = Math.sqrt(x * x + z * z);

						vx = (x * Math.cos(angle) - z * Math.sin(angle)) / mag;
						vz = (x * Math.sin(angle) + z * Math.cos(angle)) / mag;

						if (entity instanceof Player) {
							vy = 0.05 * playerPushFactor;
						}

						if (entity.getEntityId() == player.getEntityId()) {
							Vector direction = player.getEyeLocation().getDirection().clone().normalize();
							vx = direction.getX();
							vz = direction.getZ();
							Location playerloc = player.getLocation();
							double py = playerloc.getY();
							double oy = origin.getY();
							double dy = py - oy;

							if (dy >= currentHeight * .95) {
								vy = 0;
							} else if (dy >= currentHeight * .85) {
								vy = 6.0 * (.95 - dy / currentHeight);
							} else {
								vy = .6;
							}
						}

						if (entity instanceof Player) {
							if (Commands.invincible.contains(((Player) entity).getName())) {
								continue;
							}
						}

						Vector velocity = entity.getVelocity();
						velocity.setX(vx);
						velocity.setZ(vz);
						velocity.setY(vy);
						velocity.multiply(timefactor);
						GeneralMethods.setVelocity(entity, velocity);
						entity.setFallDistance(0);

						breakBreathbendingHold(entity);

						if (entity instanceof Player) {
							new Flight((Player) entity);
						}
					}
				}
			}

			for (int i : angles.keySet()) {
				double x, y, z, factor;
				double angle = (double) angles.get(i);
				angle = Math.toRadians(angle);

				y = origin.getY() + timefactor * (double) i;
				factor = (double) i / currentHeight;

				x = origin.getX() + timefactor * factor * currentRadius * Math.cos(angle);
				z = origin.getZ() + timefactor * factor * currentRadius * Math.sin(angle);

				Location effect = new Location(origin.getWorld(), x, y, z);
				if (!GeneralMethods.isRegionProtectedFromBuild(this, effect)) {
					playAirbendingParticles(effect, particleCount);
					if (random.nextInt(20) == 0) {
						playAirbendingSound(effect);
					}
				}
				angles.put(i, angles.get(i) + 25 * (int) speed);
			}
		}
		currentHeight = currentHeight > maxHeight ? maxHeight : currentHeight + 1;
	}

	@Override
	public String getName() {
		return "Tornado";
	}

	@Override
	public Location getLocation() {
		return player != null ? player.getLocation() : null;
	}

	@Override
	public long getCooldown() {
		return 0;
	}
	
	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public Location getOrigin() {
		return origin;
	}

	public void setOrigin(Location origin) {
		this.origin = origin;
	}

	public int getNumberOfStreams() {
		return numberOfStreams;
	}

	public void setNumberOfStreams(int numberOfStreams) {
		this.numberOfStreams = numberOfStreams;
	}

	public int getParticleCount() {
		return particleCount;
	}

	public void setParticleCount(int particleCount) {
		this.particleCount = particleCount;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getMaxHeight() {
		return maxHeight;
	}

	public void setMaxHeight(double maxHeight) {
		this.maxHeight = maxHeight;
	}

	public double getPlayerPushFactor() {
		return playerPushFactor;
	}

	public void setPlayerPushFactor(double playerPushFactor) {
		this.playerPushFactor = playerPushFactor;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getNpcPushFactor() {
		return npcPushFactor;
	}

	public void setNpcPushFactor(double npcPushFactor) {
		this.npcPushFactor = npcPushFactor;
	}

	public double getCurrentHeight() {
		return currentHeight;
	}

	public void setCurrentHeight(double currentHeight) {
		this.currentHeight = currentHeight;
	}

	public double getCurrentRadius() {
		return currentRadius;
	}

	public void setCurrentRadius(double currentRadius) {
		this.currentRadius = currentRadius;
	}

	public Map<Integer, Integer> getAngles() {
		return angles;
	}
}

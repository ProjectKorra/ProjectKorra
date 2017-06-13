package com.projectkorra.projectkorra.airbending;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.util.Flight;
import com.projectkorra.projectkorra.waterbending.WaterSpout;

public class AirSuction extends AirAbility {

	private static final int MAX_TICKS = 10000;
	private static final Map<Player, Location> ORIGINS = new ConcurrentHashMap<>();

	private boolean hasOtherOrigin;
	private int ticks;
	private int particleCount;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute(Attribute.RADIUS)
	private double radius;
	@Attribute(Attribute.POWER)
	private double pushFactor;
	private Random random;
	private Location location;
	private Location origin;
	private Vector direction;

	public AirSuction(Player player) {
		super(player);

		if (bPlayer.isOnCooldown(this)) {
			return;
		} else if (player.getEyeLocation().getBlock().isLiquid()) {
			return;
		} else if (hasAbility(player, AirSpout.class) || hasAbility(player, WaterSpout.class)) {
			return;
		}

		this.hasOtherOrigin = false;
		this.ticks = 0;
		this.particleCount = getConfig().getInt("Abilities.Air.AirSuction.Particles");
		this.speed = getConfig().getDouble("Abilities.Air.AirSuction.Speed");
		this.range = getConfig().getDouble("Abilities.Air.AirSuction.Range");
		this.radius = getConfig().getDouble("Abilities.Air.AirSuction.Radius");
		this.pushFactor = getConfig().getDouble("Abilities.Air.AirSuction.Push");
		this.cooldown = getConfig().getLong("Abilities.Air.AirSuction.Cooldown");
		this.random = new Random();

		if (ORIGINS.containsKey(player)) {
			origin = ORIGINS.get(player);
			hasOtherOrigin = true;
			ORIGINS.remove(player);
		} else {
			origin = player.getEyeLocation();
		}

		location = GeneralMethods.getTargetedLocation(player, range, GeneralMethods.NON_OPAQUE);
		direction = GeneralMethods.getDirection(location, origin).normalize();
		Entity entity = GeneralMethods.getTargetedEntity(player, range);

		if (entity != null) {
			direction = GeneralMethods.getDirection(entity.getLocation(), origin).normalize();
			location = getLocation(origin, direction.clone().multiply(-1));
		}

		bPlayer.addCooldown(this);
		if (bPlayer.isAvatarState()) {
			this.pushFactor = getConfig().getDouble("Abilities.Avatar.AvatarState.Air.AirSuction.Push");
		}
		start();
	}

	private static void playOriginEffect(Player player) {
		if (!ORIGINS.containsKey(player)) {
			return;
		}

		Location origin = ORIGINS.get(player);
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null || player.isDead() || !player.isOnline()) {
			return;
		} else if (!origin.getWorld().equals(player.getWorld())) {
			ORIGINS.remove(player);
			return;
		} else if (!bPlayer.canBendIgnoreCooldowns(getAbility("AirSuction"))) {
			ORIGINS.remove(player);
			return;
		} else if (origin.distanceSquared(player.getEyeLocation()) > getSelectRange() * getSelectRange()) {
			ORIGINS.remove(player);
			return;
		}

		playAirbendingParticles(origin, getSelectParticles());
	}

	public static void progressOrigins() {
		for (Player player : ORIGINS.keySet()) {
			playOriginEffect(player);
		}
	}

	public static void setOrigin(Player player) {
		Location location = GeneralMethods.getTargetedLocation(player, getSelectRange(), GeneralMethods.NON_OPAQUE);
		if (location.getBlock().isLiquid() || GeneralMethods.isSolid(location.getBlock())) {
			return;
		} else if (GeneralMethods.isRegionProtectedFromBuild(player, "AirSuction", location)) {
			return;
		} else {
			ORIGINS.put(player, location);
		}
	}

	private void advanceLocation() {
		playAirbendingParticles(location, particleCount, 0.275F, 0.275F, 0.275F);
		if (random.nextInt(4) == 0) {
			playAirbendingSound(location);
		}
		double speedFactor = speed * (ProjectKorra.time_step / 1000.);
		location = location.add(direction.clone().multiply(speedFactor));
	}

	private Location getLocation(Location origin, Vector direction) {
		Location location = origin.clone();
		for (double i = 1; i <= range; i++) {
			location = origin.clone().add(direction.clone().multiply(i));
			if (!isTransparent(location.getBlock()) || GeneralMethods.isRegionProtectedFromBuild(this, location)) {
				return origin.clone().add(direction.clone().multiply(i - 1));
			}
		}
		return location;
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		} else if (GeneralMethods.isRegionProtectedFromBuild(player, "AirSuction", location)) {
			remove();
			return;
		}

		ticks++;
		if (ticks > MAX_TICKS) {
			remove();
			return;
		} else if ((location.distanceSquared(origin) > range * range) || (location.distanceSquared(origin) <= 1)) {
			remove();
			return;
		}

		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, radius)) {
			if (entity.getEntityId() != player.getEntityId() || hasOtherOrigin) {
				Vector velocity = entity.getVelocity();
				double max = speed;
				double factor = pushFactor;

				Vector push = direction.clone();
				if (Math.abs(push.getY()) > max && entity.getEntityId() != player.getEntityId()) {
					if (push.getY() < 0) {
						push.setY(-max);
					} else {
						push.setY(max);
					}
				}
				if (location.getWorld().equals(origin.getWorld())) {
					factor *= 1 - location.distance(origin) / (2 * range);
				}

				double comp = velocity.dot(push.clone().normalize());
				if (comp > factor) {
					velocity.multiply(.5);
					velocity.add(push.clone().normalize().multiply(velocity.clone().dot(push.clone().normalize())));
				} else if (comp + factor * .5 > factor) {
					velocity.add(push.clone().multiply(factor - comp));
				} else {
					velocity.add(push.clone().multiply(factor * .5));
				}

				if (entity instanceof Player) {
					if (Commands.invincible.contains(((Player) entity).getName())) {
						continue;
					}
				}

				GeneralMethods.setVelocity(entity, velocity);
				new HorizontalVelocityTracker(entity, player, 200l, this);
				entity.setFallDistance(0);
				if (entity.getEntityId() != player.getEntityId() && entity instanceof Player) {
					new Flight((Player) entity, player);
				}

				if (entity.getFireTicks() > 0) {
					entity.getWorld().playEffect(entity.getLocation(), Effect.EXTINGUISH, 0);
				}
				entity.setFireTicks(0);
				breakBreathbendingHold(entity);
			}
		}

		advanceLocation();
	}

	/**
	 * This method was used for the old collision detection system. Please see
	 * {@link Collision} for the new system.
	 */
	@Deprecated
	public static boolean removeAirSuctionsAroundPoint(Location location, double radius) {
		boolean removed = false;
		for (AirSuction airSuction : getAbilities(AirSuction.class)) {
			Location airSuctionlocation = airSuction.location;
			if (location.getWorld() == airSuctionlocation.getWorld()) {
				if (location.distanceSquared(airSuctionlocation) <= radius * radius) {
					airSuction.remove();
				}
				removed = true;
			}
		}
		return removed;
	}

	@Override
	public String getName() {
		return "AirSuction";
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public long getCooldown() {
		return cooldown;
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
		return getRadius();
	}

	public Location getOrigin() {
		return origin;
	}

	public void setOrigin(Location origin) {
		this.origin = origin;
	}

	public Vector getDirection() {
		return direction;
	}

	public void setDirection(Vector direction) {
		this.direction = direction;
	}

	public boolean isHasOtherOrigin() {
		return hasOtherOrigin;
	}

	public void setHasOtherOrigin(boolean hasOtherOrigin) {
		this.hasOtherOrigin = hasOtherOrigin;
	}

	public int getTicks() {
		return ticks;
	}

	public void setTicks(int ticks) {
		this.ticks = ticks;
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

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getPushFactor() {
		return pushFactor;
	}

	public void setPushFactor(double pushFactor) {
		this.pushFactor = pushFactor;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public static Map<Player, Location> getOrigins() {
		return ORIGINS;
	}

	public static int getSelectParticles() {
		return getConfig().getInt("Abilities.Air.AirSuction.SelectParticles");
	}

	public static double getSelectRange() {
		return getConfig().getDouble("Abilities.Air.AirSuction.SelectRange");
	}

}

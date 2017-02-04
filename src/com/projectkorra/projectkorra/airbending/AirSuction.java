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
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.util.Attribute;
import com.projectkorra.projectkorra.util.Attribute.Attributable;
import com.projectkorra.projectkorra.util.Flight;
import com.projectkorra.projectkorra.waterbending.WaterSpout;

public class AirSuction extends AirAbility implements Attributable{

	private static final int MAX_TICKS = 10000;
	private static final Map<Player, Location> ORIGINS = new ConcurrentHashMap<>();

	private boolean hasOtherOrigin;
	private int ticks;
	private int particleCount;
	private long cooldown;
	private double speed;
	private double range;
	private double radius;
	private double pushFactor;
	private Random random;
	private Location location;
	private Location origin;
	private Vector direction;
	private static Attribute<Integer> particleCountA;
	private static Attribute<Integer> selectParticlesA;
	private static Attribute<Long> cooldownA;
	private static Attribute<Double> speedA;
	private static Attribute<Double> rangeA;
	private static Attribute<Double> radiusA;
	private static Attribute<Double> pushFactorA;
	private static Attribute<Double> selectRangeA;

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
		this.particleCount = particleCountA.getModified(bPlayer);
		this.speed = speedA.getModified(bPlayer);
		this.range = rangeA.getModified(bPlayer);
		this.radius = radiusA.getModified(bPlayer);
		this.pushFactor = pushFactorA.getModified(bPlayer);
		this.cooldown = cooldownA.getModified(bPlayer);
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
			this.pushFactor = getConfig().getDouble("Abilities.Avatar.AvatarState.AirSuction.Push");
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
		} else if (origin.distanceSquared(player.getEyeLocation()) > getSelectRange(bPlayer) * getSelectRange(bPlayer)) {
			ORIGINS.remove(player);
			return;
		}

		playAirbendingParticles(origin, getSelectParticles(bPlayer));
	}

	public static void progressOrigins() {
		for (Player player : ORIGINS.keySet()) {
			playOriginEffect(player);
		}
	}

	public static void setOrigin(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return;
		}
		Location location = GeneralMethods.getTargetedLocation(player, getSelectRange(bPlayer), GeneralMethods.NON_OPAQUE);
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

	public static int getSelectParticles(BendingPlayer bPlayer) {
		return selectParticlesA.getModified(bPlayer);
	}

	public static double getSelectRange(BendingPlayer bPlayer) {
		return selectRangeA.getModified(bPlayer);
	}

	@Override
	public void registerAttributes() {
		particleCountA = new Attribute<Integer>(this, "particleCount", getConfig().getInt("Abilities.Air.AirSuction.Particles"));
		selectParticlesA = new Attribute<Integer>(this, "selectParticles", getConfig().getInt("Abilities.Air.AirSuction.SelectParticles"));
		cooldownA = new Attribute<Long>(this, "cooldown", getConfig().getLong("Abilities.Air.AirSuction.Cooldown"));
		speedA = new Attribute<Double>(this, "speed", getConfig().getDouble("Abilities.Air.AirSuction.Speed"));
		rangeA = new Attribute<Double>(this, "range", getConfig().getDouble("Abilities.Air.AirSuction.Range"));
		radiusA = new Attribute<Double>(this, "radius", getConfig().getDouble("Abilities.Air.AirSuction.Radius"));
		pushFactorA = new Attribute<Double>(this, "pushFactor", getConfig().getDouble("Abilities.Air.AirSuction.Push"));
		selectRangeA = new Attribute<Double>(this, "selectRange", getConfig().getDouble("Abilities.Air.AirSuction.SelectRange"));
	}

}

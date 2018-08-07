package com.projectkorra.projectkorra.airbending;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.material.Door;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.waterbending.WaterSpout;

public class AirSuction extends AirAbility {

	private static final int MAX_TICKS = 10000;
	private static final Map<Player, Location> ORIGINS = new ConcurrentHashMap<>();
	private static Material doorTypes[] = { Material.WOODEN_DOOR, Material.SPRUCE_DOOR, Material.BIRCH_DOOR, Material.JUNGLE_DOOR, Material.ACACIA_DOOR, Material.DARK_OAK_DOOR, Material.TRAP_DOOR };
	private final List<Block> affectedDoors = new ArrayList<>();

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

	public AirSuction(final Player player) {
		super(player);

		if (this.bPlayer.isOnCooldown(this)) {
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
			this.origin = ORIGINS.get(player);
			this.hasOtherOrigin = true;
			ORIGINS.remove(player);
		} else {
			this.origin = player.getEyeLocation();
		}

		this.location = GeneralMethods.getTargetedLocation(player, this.range, getTransparentMaterials());
		this.direction = GeneralMethods.getDirection(this.location, this.origin).normalize();
		final Entity entity = GeneralMethods.getTargetedEntity(player, this.range);

		if (entity != null) {
			this.direction = GeneralMethods.getDirection(entity.getLocation(), this.origin).normalize();
			this.location = this.getLocation(this.origin, this.direction.clone().multiply(-1));
		}

		this.bPlayer.addCooldown(this);
		if (this.bPlayer.isAvatarState()) {
			this.pushFactor = getConfig().getDouble("Abilities.Avatar.AvatarState.Air.AirSuction.Push");
		}
		this.start();
	}

	private static void playOriginEffect(final Player player) {
		if (!ORIGINS.containsKey(player)) {
			return;
		}

		final Location origin = ORIGINS.get(player);
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
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
		for (final Player player : ORIGINS.keySet()) {
			playOriginEffect(player);
		}
	}

	public static void setOrigin(final Player player) {
		final Material[] ignore = new Material[getTransparentMaterials().length + doorTypes.length];
		for (int i = 0; i < ignore.length; i++) {
			if (i < getTransparentMaterials().length) {
				ignore[i] = getTransparentMaterials()[i];
			} else {
				ignore[i] = doorTypes[i - getTransparentMaterials().length];
			}
		}
		final Location location = GeneralMethods.getTargetedLocation(player, getSelectRange(), ignore);
		if (location.getBlock().isLiquid() || GeneralMethods.isSolid(location.getBlock())) {
			return;
		} else if (GeneralMethods.isRegionProtectedFromBuild(player, "AirSuction", location)) {
			return;
		} else {
			ORIGINS.put(player, location);
		}
	}

	private void advanceLocation() {
		playAirbendingParticles(this.location, this.particleCount, 0.275F, 0.275F, 0.275F);
		if (this.random.nextInt(4) == 0) {
			playAirbendingSound(this.location);
		}
		final double speedFactor = this.speed * (ProjectKorra.time_step / 1000.);
		this.location = this.location.add(this.direction.clone().multiply(speedFactor));

		if (Arrays.asList(doorTypes).contains(this.location.getBlock().getType()) && !this.affectedDoors.contains(this.location.getBlock())) {
			this.handleDoorMechanics(this.location.getBlock());
		}
	}

	private void handleDoorMechanics(final Block block) {
		boolean tDoor = false;
		final boolean open = (block.getData() & 0x4) == 0x4;

		if (block.getType() != Material.TRAP_DOOR) {
			final Door door = (Door) block.getState().getData();
			final BlockFace face = door.getFacing();
			final Vector toPlayer = GeneralMethods.getDirection(block.getLocation(), this.player.getLocation().getBlock().getLocation());
			final double[] dims = { toPlayer.getX(), toPlayer.getY(), toPlayer.getZ() };

			for (int i = 0; i < 3; i++) {
				if (i == 1) {
					continue;
				}
				final BlockFace bf = GeneralMethods.getBlockFaceFromValue(i, dims[i]);

				if (bf == face) {
					if (!open) {
						return;
					}
				} else if (bf.getOppositeFace() == face) {
					if (open) {
						return;
					}
				}
			}
		} else {
			tDoor = true;

			if (this.origin.getY() < block.getY()) {
				if (open) {
					return;
				}
			} else {
				if (!open) {
					return;
				}
			}
		}

		block.setData((byte) ((block.getData() & 0x4) == 0x4 ? (block.getData() & ~0x4) : (block.getData() | 0x4)));
		final String sound = "BLOCK_WOODEN_" + (tDoor ? "TRAP" : "") + "DOOR_" + (!open ? "OPEN" : "CLOSE");
		block.getWorld().playSound(block.getLocation(), sound, 0.5f, 0);
		this.affectedDoors.add(block);
	}

	private Location getLocation(final Location origin, final Vector direction) {
		Location location = origin.clone();
		for (double i = 1; i <= this.range; i++) {
			location = origin.clone().add(direction.clone().multiply(i));
			if ((!this.isTransparent(location.getBlock()) && !Arrays.asList(doorTypes).contains(location.getBlock().getType())) || GeneralMethods.isRegionProtectedFromBuild(this, location)) {
				return origin.clone().add(direction.clone().multiply(i - 1));
			}
		}
		return location;
	}

	@Override
	public void progress() {
		if (this.player.isDead() || !this.player.isOnline()) {
			this.remove();
			return;
		} else if (GeneralMethods.isRegionProtectedFromBuild(this.player, "AirSuction", this.location)) {
			this.remove();
			return;
		}

		this.ticks++;
		if (this.ticks > MAX_TICKS) {
			this.remove();
			return;
		} else if ((this.location.distanceSquared(this.origin) > this.range * this.range) || (this.location.distanceSquared(this.origin) <= 1)) {
			this.remove();
			return;
		}

		for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(this.location, this.radius)) {
			if (entity.getEntityId() != this.player.getEntityId() || this.hasOtherOrigin) {
				final Vector velocity = entity.getVelocity();
				final double max = this.speed;
				double factor = this.pushFactor;

				final Vector push = this.direction.clone();
				if (Math.abs(push.getY()) > max && entity.getEntityId() != this.player.getEntityId()) {
					if (push.getY() < 0) {
						push.setY(-max);
					} else {
						push.setY(max);
					}
				}
				if (this.location.getWorld().equals(this.origin.getWorld())) {
					factor *= 1 - this.location.distance(this.origin) / (2 * this.range);
				}

				final double comp = velocity.dot(push.clone().normalize());
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
				new HorizontalVelocityTracker(entity, this.player, 200l, this);
				entity.setFallDistance(0);
				if (entity.getEntityId() != this.player.getEntityId() && entity instanceof Player) {
					ProjectKorra.flightHandler.createInstance((Player) entity, this.player, 5000L, this.getName());
				}

				if (entity.getFireTicks() > 0) {
					entity.getWorld().playEffect(entity.getLocation(), Effect.EXTINGUISH, 0);
				}
				entity.setFireTicks(0);
				breakBreathbendingHold(entity);
			}
		}

		this.advanceLocation();
	}

	/**
	 * This method was used for the old collision detection system. Please see
	 * {@link Collision} for the new system.
	 */
	@Deprecated
	public static boolean removeAirSuctionsAroundPoint(final Location location, final double radius) {
		boolean removed = false;
		for (final AirSuction airSuction : getAbilities(AirSuction.class)) {
			final Location airSuctionlocation = airSuction.location;
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
		return this.location;
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

	public Vector getDirection() {
		return this.direction;
	}

	public void setDirection(final Vector direction) {
		this.direction = direction;
	}

	public boolean isHasOtherOrigin() {
		return this.hasOtherOrigin;
	}

	public void setHasOtherOrigin(final boolean hasOtherOrigin) {
		this.hasOtherOrigin = hasOtherOrigin;
	}

	public int getTicks() {
		return this.ticks;
	}

	public void setTicks(final int ticks) {
		this.ticks = ticks;
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

	public double getRange() {
		return this.range;
	}

	public void setRange(final double range) {
		this.range = range;
	}

	public double getRadius() {
		return this.radius;
	}

	public void setRadius(final double radius) {
		this.radius = radius;
	}

	public double getPushFactor() {
		return this.pushFactor;
	}

	public void setPushFactor(final double pushFactor) {
		this.pushFactor = pushFactor;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

	public void setCooldown(final long cooldown) {
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

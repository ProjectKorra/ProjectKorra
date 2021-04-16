package com.projectkorra.projectkorra.airbending;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.waterbending.WaterSpout;

public class AirSuction extends AirAbility {

	private final List<Block> affectedDoors = new ArrayList<>();

	private boolean progressing;
	private int particleCount;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute(Attribute.RADIUS)
	private double radius;
	@Attribute(Attribute.KNOCKBACK)
	private double pushFactor;
	@Attribute(Attribute.KNOCKBACK + "Others")
	private double pushFactorForOthers;
	private Random random;
	private Location location;
	private Location origin;
	private Vector direction;
	private boolean canAffectSelf;

	public AirSuction(final Player player) {
		super(player);

		if (this.bPlayer.isOnCooldown(this)) {
			return;
		} else if (player.getEyeLocation().getBlock().isLiquid()) {
			return;
		} else if (hasAbility(player, AirSpout.class) || hasAbility(player, WaterSpout.class)) {
			return;
		}

		if (hasAbility(player, AirSuction.class)) {
			final AirSuction suc = getAbility(player, AirSuction.class);
			if (!suc.isProgressing()) {
				final Location loc = this.getTargetLocation();

				if (!GeneralMethods.isRegionProtectedFromBuild(player, this.getName(), loc)) {
					suc.setOrigin(loc);
				}
			}
			return;
		}

		this.progressing = false;
		this.particleCount = getConfig().getInt("Abilities.Air.AirSuction.Particles");
		this.speed = getConfig().getDouble("Abilities.Air.AirSuction.Speed");
		this.range = getConfig().getDouble("Abilities.Air.AirSuction.Range");
		this.radius = getConfig().getDouble("Abilities.Air.AirSuction.Radius");
		this.pushFactor = getConfig().getDouble("Abilities.Air.AirSuction.Push.Self");
		this.pushFactorForOthers = getConfig().getDouble("Abilities.Air.AirSuction.Push.Others");
		this.cooldown = getConfig().getLong("Abilities.Air.AirSuction.Cooldown");
		this.random = new Random();
		this.origin = this.getTargetLocation();
		this.canAffectSelf = true;

		if (GeneralMethods.isRegionProtectedFromBuild(player, this.getName(), this.origin)) {
			return;
		}

		this.location = null;

		if (this.bPlayer.isAvatarState()) {
			this.pushFactor = getConfig().getDouble("Abilities.Avatar.AvatarState.Air.AirSuction.Push");
		}

		this.start();
	}

	private void advanceLocation() {
		playAirbendingParticles(this.location, this.particleCount, 0.275F, 0.275F, 0.275F);
		if (this.random.nextInt(4) == 0) {
			playAirbendingSound(this.location);
		}
		final double speedFactor = this.speed * (ProjectKorra.time_step / 1000.);
		this.location = this.location.add(this.direction.clone().multiply(speedFactor));

		if ((Arrays.asList(AirBlast.DOORS).contains(this.location.getBlock().getType()) || Arrays.asList(AirBlast.TDOORS).contains(this.location.getBlock().getType())) && !this.affectedDoors.contains(this.location.getBlock())) {
			this.handleDoorMechanics(this.location.getBlock());
		}
	}

	private void handleDoorMechanics(final Block block) {
		boolean tDoor = false;
		boolean open = false;

		if (Arrays.asList(AirBlast.DOORS).contains(block.getType())) {
			final Door door = (Door) block.getBlockData();
			final BlockFace face = door.getFacing();
			final Vector toPlayer = GeneralMethods.getDirection(block.getLocation(), this.player.getLocation().getBlock().getLocation());
			final double[] dims = { toPlayer.getX(), toPlayer.getY(), toPlayer.getZ() };

			for (int i = 0; i < 3; i++) {
				if (i == 1) {
					continue;
				}
				final BlockFace bf = GeneralMethods.getBlockFaceFromValue(i, dims[i]);

				if (bf == face) {
					if (!door.isOpen()) {
						return;
					}
				} else if (bf.getOppositeFace() == face) {
					if (door.isOpen()) {
						return;
					}
				}
			}

			door.setOpen(!door.isOpen());
			block.setBlockData(door);
			open = door.isOpen();
		} else {
			tDoor = true;
			final TrapDoor trap = (TrapDoor) block.getBlockData();

			if (this.origin.getY() < block.getY()) {
				if (trap.isOpen()) {
					return;
				}
			} else {
				if (!trap.isOpen()) {
					return;
				}
			}

			trap.setOpen(!trap.isOpen());
			block.setBlockData(trap);
			open = trap.isOpen();
		}

		final String sound = "block_wooden_" + (tDoor ? "trap" : "") + "door_" + (!open ? "open" : "close");
		block.getWorld().playSound(block.getLocation(), sound, 0.5f, 0);
		this.affectedDoors.add(block);
	}

	private Location getTargetLocation() {
		final Material[] ignore = new Material[getTransparentMaterials().length + AirBlast.DOORS.length + AirBlast.TDOORS.length];

		for (int i = 0; i < ignore.length; i++) {
			if (i < getTransparentMaterials().length) {
				ignore[i] = getTransparentMaterials()[i];
			} else if (i < getTransparentMaterials().length + AirBlast.DOORS.length) {
				ignore[i] = AirBlast.DOORS[i - getTransparentMaterials().length];
			} else {
				ignore[i] = AirBlast.TDOORS[i - getTransparentMaterials().length - AirBlast.DOORS.length];
			}
		}

		return GeneralMethods.getTargetedLocation(this.player, getSelectRange(), ignore);
	}

	@Override
	public void progress() {
		if (this.player.isDead() || !this.player.isOnline()) {
			this.remove();
			return;
		}

		if (this.progressing) {
			if (GeneralMethods.isRegionProtectedFromBuild(this.player, "AirSuction", this.location)) {
				this.remove();
				return;
			} else if (!this.location.getWorld().equals(this.origin.getWorld()) || this.location.distanceSquared(this.origin) > this.range * this.range || this.location.distanceSquared(this.origin) <= 1) {
				this.remove();
				return;
			}

			for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(this.location, this.radius)) {
				if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation()) || ((entity instanceof Player) && Commands.invincible.contains(((Player) entity).getName()))) {
					continue;
				}
				if ((entity.getEntityId() == this.player.getEntityId()) && !this.canAffectSelf) {
					continue;
				}
				
				double knockback = this.pushFactor;
				
				if (entity.getEntityId() != player.getEntityId()) {
					knockback = this.pushFactorForOthers;
				}
				
				final double max = this.speed;
				final Vector push = this.direction.clone();

				if (Math.abs(push.getY()) > max) {
					if (push.getY() < 0) {
						push.setY(-max);
					} else {
						push.setY(max);
					}
				}

				if (this.location.getWorld().equals(this.origin.getWorld())) {
					knockback *= 1 - this.location.distance(this.origin) / (2 * this.range);
				}
				
				push.normalize().multiply(knockback);
				
				if (Math.abs(entity.getVelocity().dot(push)) > knockback) {
					push.normalize().add(entity.getVelocity()).multiply(knockback);
				}
				GeneralMethods.setVelocity(this, entity, push.normalize().multiply(knockback));
				
				new HorizontalVelocityTracker(entity, this.player, 200l, this);
				entity.setFallDistance(0);

				if (entity.getFireTicks() > 0) {
					entity.getWorld().playEffect(entity.getLocation(), Effect.EXTINGUISH, 0);
				}
				entity.setFireTicks(0);
				breakBreathbendingHold(entity);
			}

			this.advanceLocation();
		} else {
			if (bPlayer == null || player.isDead() || !player.isOnline()) {
				return;
			} else if (!origin.getWorld().equals(player.getWorld())) {
				remove();
				return;
			} else if (!bPlayer.canBendIgnoreCooldowns(this)) {
				remove();
				return;
			} else if (origin.distanceSquared(player.getLocation()) > this.range * this.range) {
				remove();
				return;
			}

			playAirbendingParticles(this.origin, 5, 0.5, 0.5, 0.5);
		}
	}

	public void shoot() {
		Location target;
		final Entity entity = GeneralMethods.getTargetedEntity(this.player, this.range);

		if (entity != null) {
			target = entity.getLocation();
		} else {
			target = this.getTargetLocation();
		}

		this.location = target.clone();
		this.direction = GeneralMethods.getDirection(this.location, this.origin).normalize();
		this.progressing = true;
		this.bPlayer.addCooldown(this);
	}

	public static void shoot(final Player player) {
		AirSuction suc = null;

		if (CoreAbility.hasAbility(player, AirSuction.class)) {
			suc = CoreAbility.getAbility(player, AirSuction.class);
			if (suc.isProgressing()) {
				return;
			}
		} else {
			suc = new AirSuction(player);
			suc.setOrigin(player.getEyeLocation().clone());
			suc.setCanEffectSelf(false);
		}

		if (suc.getOrigin() != null) {
			suc.shoot();
		}
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

	public boolean isProgressing() {
		return this.progressing;
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

	public void setCanEffectSelf(final boolean affect) {
		this.canAffectSelf = affect;
	}

	public static int getSelectParticles() {
		return getConfig().getInt("Abilities.Air.AirSuction.SelectParticles");
	}

	public static double getSelectRange() {
		return getConfig().getDouble("Abilities.Air.AirSuction.SelectRange");
	}

}

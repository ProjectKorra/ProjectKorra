package com.projectkorra.projectkorra.earthbending;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

public class EarthSmash extends EarthAbility {

	public static enum State {
		START, LIFTING, LIFTED, GRABBED, SHOT, FLYING, REMOVED
	}

	@Attribute("AllowGrab")
	private boolean allowGrab;
	@Attribute("AllowFlight")
	private boolean allowFlight;
	private int animationCounter;
	private int progressCounter;
	private int requiredBendableBlocks;
	private int maxBlocksToPassThrough;
	private long delay;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.CHARGE_DURATION)
	private long chargeTime;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute("Flight" + Attribute.DURATION)
	private long flightDuration;
	private long flightStartTime;
	private long shootAnimationInterval;
	private long flightAnimationInterval;
	private long liftAnimationInterval;
	@Attribute(Attribute.SELECT_RANGE)
	private double selectRange;
	@Attribute("GrabRange")
	private double grabRange;
	@Attribute("ShootRange")
	private double shootRange;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.KNOCKBACK)
	private double knockback;
	@Attribute(Attribute.KNOCKUP)
	private double knockup;
	@Attribute(Attribute.SPEED)
	private double flightSpeed;
	private double grabbedDistance;
	private double grabDetectionRadius;
	private double hitRadius;
	private double flightDetectionRadius;
	private State state;
	private Block origin;
	private Location location;
	private Location destination;
	private ArrayList<Entity> affectedEntities;
	private ArrayList<BlockRepresenter> currentBlocks;
	private ArrayList<TempBlock> affectedBlocks;

	public EarthSmash(final Player player, final ClickType type) {
		super(player);

		this.state = State.START;
		this.requiredBendableBlocks = getConfig().getInt("Abilities.Earth.EarthSmash.RequiredBendableBlocks");
		this.maxBlocksToPassThrough = getConfig().getInt("Abilities.Earth.EarthSmash.MaxBlocksToPassThrough");
		this.setFields();
		this.affectedEntities = new ArrayList<>();
		this.currentBlocks = new ArrayList<>();
		this.affectedBlocks = new ArrayList<>();

		if (type == ClickType.SHIFT_DOWN || type == ClickType.SHIFT_UP && !player.isSneaking()) {
			final EarthSmash flySmash = flyingInSmashCheck(player);
			if (flySmash != null) {
				flySmash.state = State.FLYING;
				flySmash.player = player;
				flySmash.setFields();
				flySmash.flightStartTime = System.currentTimeMillis();
				return;
			}

			EarthSmash grabbedSmash = this.aimingAtSmashCheck(player, State.LIFTED);
			if (grabbedSmash == null) {
				if (this.bPlayer.isOnCooldown(this)) {
					return;
				}
				grabbedSmash = this.aimingAtSmashCheck(player, State.SHOT);
			}

			if (grabbedSmash != null) {
				grabbedSmash.state = State.GRABBED;
				grabbedSmash.grabbedDistance = 0;
				if (grabbedSmash.location.getWorld().equals(player.getWorld())) {
					grabbedSmash.grabbedDistance = grabbedSmash.location.distance(player.getEyeLocation());
				}
				grabbedSmash.player = player;
				grabbedSmash.setFields();
				return;
			}

			this.start();
		} else if (type == ClickType.LEFT_CLICK && player.isSneaking()) {
			for (final EarthSmash smash : getAbilities(EarthSmash.class)) {
				if (smash.state == State.GRABBED && smash.player == player) {
					smash.state = State.SHOT;
					smash.destination = player.getEyeLocation().clone().add(player.getEyeLocation().getDirection().normalize().multiply(smash.shootRange));
					smash.location.getWorld().playEffect(smash.location, Effect.GHAST_SHOOT, 0, 10);
				}
			}
			return;
		} else if (type == ClickType.RIGHT_CLICK && player.isSneaking()) {
			final EarthSmash grabbedSmash = this.aimingAtSmashCheck(player, State.GRABBED);
			if (grabbedSmash != null) {
				player.teleport(grabbedSmash.location.clone().add(0, 2, 0));
				grabbedSmash.state = State.FLYING;
				grabbedSmash.player = player;
				grabbedSmash.setFields();
				grabbedSmash.flightStartTime = System.currentTimeMillis();
			}
			return;
		}
	}

	public void setFields() {
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(this.player);
		this.shootAnimationInterval = getConfig().getLong("Abilities.Earth.EarthSmash.Shoot.AnimationInterval");
		this.flightAnimationInterval = getConfig().getLong("Abilities.Earth.EarthSmash.Flight.AnimationInterval");
		this.liftAnimationInterval = getConfig().getLong("Abilities.Earth.EarthSmash.LiftAnimationInterval");
		this.grabDetectionRadius = getConfig().getDouble("Abilities.Earth.EarthSmash.Grab.DetectionRadius");
		this.flightDetectionRadius = getConfig().getDouble("Abilities.Earth.EarthSmash.Flight.DetectionRadius");
		this.hitRadius = getConfig().getDouble("Abilities.Earth.EarthSmash.Shoot.CollisionRadius");
		this.allowGrab = getConfig().getBoolean("Abilities.Earth.EarthSmash.Grab.Enabled");
		this.allowFlight = getConfig().getBoolean("Abilities.Earth.EarthSmash.Flight.Enabled");
		this.selectRange = getConfig().getDouble("Abilities.Earth.EarthSmash.SelectRange");
		this.grabRange = getConfig().getDouble("Abilities.Earth.EarthSmash.Grab.Range");
		this.shootRange = getConfig().getDouble("Abilities.Earth.EarthSmash.Shoot.Range");
		this.damage = getConfig().getDouble("Abilities.Earth.EarthSmash.Damage");
		this.knockback = getConfig().getDouble("Abilities.Earth.EarthSmash.Knockback");
		this.knockup = getConfig().getDouble("Abilities.Earth.EarthSmash.Knockup");
		this.flightSpeed = getConfig().getDouble("Abilities.Earth.EarthSmash.Flight.Speed");
		this.chargeTime = getConfig().getLong("Abilities.Earth.EarthSmash.ChargeTime");
		this.cooldown = getConfig().getLong("Abilities.Earth.EarthSmash.Cooldown");
		this.flightDuration = getConfig().getLong("Abilities.Earth.EarthSmash.Flight.Duration");
		this.duration = getConfig().getLong("Abilities.Earth.EarthSmash.Duration");

		if (bPlayer.isAvatarState()) {
			this.selectRange = getConfig().getDouble("Abilities.Avatar.AvatarState.Earth.EarthSmash.SelectRange");
			this.grabRange = getConfig().getDouble("Abilities.Avatar.AvatarState.Earth.EarthSmash.GrabRange");
			this.chargeTime = getConfig().getLong("Abilities.Avatar.AvatarState.Earth.EarthSmash.ChargeTime");
			this.cooldown = getConfig().getLong("Abilities.Avatar.AvatarState.Earth.EarthSmash.Cooldown");
			this.damage = getConfig().getDouble("Abilities.Avatar.AvatarState.Earth.EarthSmash.Damage");
			this.knockback = getConfig().getDouble("Abilities.Avatar.AvatarState.Earth.EarthSmash.Knockback");
			this.flightSpeed = getConfig().getDouble("Abilities.Avatar.AvatarState.Earth.EarthSmash.FlightSpeed");
			this.flightDuration = getConfig().getLong("Abilities.Avatar.AvatarState.Earth.EarthSmash.FlightTimer");
			this.shootRange = getConfig().getDouble("Abilities.Avatar.AvatarState.Earth.EarthSmash.ShootRange");
		}
	}

	@Override
	public void progress() {
		this.progressCounter++;
		if (this.state == State.LIFTED && this.duration > 0 && System.currentTimeMillis() - this.getStartTime() > this.duration) {
			this.remove();
			return;
		}

		if (this.state == State.START) {
			if (!this.bPlayer.canBend(this)) {
				this.remove();
				return;
			}
		} else if (this.state == State.START || this.state == State.FLYING || this.state == State.GRABBED) {
			if (!this.bPlayer.canBendIgnoreCooldowns(this)) {
				this.remove();
				return;
			}
		}

		if (this.state == State.START && this.progressCounter > 1) {
			if (!this.player.isSneaking()) {
				if (System.currentTimeMillis() - this.getStartTime() >= this.chargeTime) {
					this.origin = this.getEarthSourceBlock(this.selectRange);
					if (this.origin == null) {
						this.remove();
						return;
					} else if (TempBlock.isTempBlock(this.origin) && !isBendableEarthTempBlock(this.origin)) {
						this.remove();
						return;
					}
					this.bPlayer.addCooldown(this);
					this.location = this.origin.getLocation();
					this.state = State.LIFTING;
				} else {
					this.remove();
					return;
				}
			} else if (System.currentTimeMillis() - this.getStartTime() > this.chargeTime) {
				final Location tempLoc = this.player.getEyeLocation().add(this.player.getEyeLocation().getDirection().normalize().multiply(1.2));
				tempLoc.add(0, 0.3, 0);
				ParticleEffect.SMOKE_NORMAL.display(tempLoc, 4, 0.3, 0.1, 0.3, 0);
			}
		} else if (this.state == State.LIFTING) {
			if (System.currentTimeMillis() - this.delay >= this.liftAnimationInterval) {
				this.delay = System.currentTimeMillis();
				this.animateLift();
			}
		} else if (this.state == State.GRABBED) {
			if (this.player.isSneaking()) {
				this.revert();
				final Location oldLoc = this.location.clone();
				this.location = this.player.getEyeLocation().add(this.player.getEyeLocation().getDirection().normalize().multiply(this.grabbedDistance));

				// Check to make sure the new location is available to move to.
				for (final Block block : this.getBlocks()) {
					if (!ElementalAbility.isAir(block.getType()) && !this.isTransparent(block)) {
						this.location = oldLoc;
						break;
					}
				}
				this.draw();
				return;
			} else {
				this.state = State.LIFTED;
				return;
			}
		} else if (this.state == State.SHOT) {
			if (System.currentTimeMillis() - this.delay >= this.shootAnimationInterval) {
				this.delay = System.currentTimeMillis();
				if (GeneralMethods.isRegionProtectedFromBuild(this, this.location)) {
					this.remove();
					return;
				}

				this.revert();
				this.location.add(GeneralMethods.getDirection(this.location, this.destination).normalize().multiply(1));
				if (this.location.distanceSquared(this.destination) < 4) {
					this.remove();
					return;
				}

				// If an earthsmash runs into too many blocks we should remove it.
				int badBlocksFound = 0;
				for (final Block block : this.getBlocks()) {
					if (!ElementalAbility.isAir(block.getType()) && (!this.isTransparent(block) || block.getType() == Material.WATER)) {
						badBlocksFound++;
					}
				}

				if (badBlocksFound > this.maxBlocksToPassThrough) {
					this.remove();
					return;
				}
				this.shootingCollisionDetection();
				this.draw();
				this.smashToSmashCollisionDetection();
			}
			return;
		} else if (this.state == State.FLYING) {
			if (!this.player.isSneaking()) {
				this.remove();
				return;
			} else if (System.currentTimeMillis() - this.delay >= this.flightAnimationInterval) {
				this.delay = System.currentTimeMillis();
				if (GeneralMethods.isRegionProtectedFromBuild(this, this.location)) {
					this.remove();
					return;
				}
				this.revert();
				this.destination = this.player.getEyeLocation().clone().add(this.player.getEyeLocation().getDirection().normalize().multiply(this.shootRange));
				final Vector direction = GeneralMethods.getDirection(this.location, this.destination).normalize();

				final List<Entity> entities = GeneralMethods.getEntitiesAroundPoint(this.location.clone().add(0, 2, 0), this.flightDetectionRadius);
				if (entities.size() == 0) {
					this.remove();
					return;
				}
				for (final Entity entity : entities) {
					if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation()) || ((entity instanceof Player) && Commands.invincible.contains(((Player) entity).getName()))) {
						continue;
					}
					GeneralMethods.setVelocity(this, entity, direction.clone().multiply(this.flightSpeed));
				}

				// These values tend to work well when dealing with a person aiming upward or downward.
				if (direction.getY() < -0.35) {
					this.location = this.player.getLocation().clone().add(0, -3.2, 0);
				} else if (direction.getY() > 0.35) {
					this.location = this.player.getLocation().clone().add(0, -1.7, 0);
				} else {
					this.location = this.player.getLocation().clone().add(0, -2.2, 0);
				}
				this.draw();
			}
			if (System.currentTimeMillis() - this.flightStartTime > this.flightDuration) {
				this.remove();
				return;
			}
		}
	}

	/**
	 * Begins animating the EarthSmash from the ground. The lift animation
	 * consists of 3 steps, and each one has to design the shape in the ground
	 * that removes the Earthbendable material. We also need to make sure that
	 * there is a clear path for the EarthSmash to rise, and that there is
	 * enough Earthbendable material for it to be created.
	 */
	public void animateLift() {
		if (this.animationCounter < 4) {
			this.revert();
			this.location.add(0, 1, 0);
			// Remove the blocks underneath the rising smash.
			if (this.animationCounter == 0) {
				// Check all of the blocks and make sure that they can be removed AND make sure there is enough dirt.
				int totalBendableBlocks = 0;
				for (int x = -1; x <= 1; x++) {
					for (int y = -2; y <= -1; y++) {
						for (int z = -1; z <= 1; z++) {
							final Block block = this.location.clone().add(x, y, z).getBlock();
							if (GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
								this.remove();
								return;
							}
							if (this.isEarthbendable(block)) {
								totalBendableBlocks++;
							}
						}
					}
				}
				if (totalBendableBlocks < this.requiredBendableBlocks) {
					this.remove();
					return;
				}
				// Make sure there is a clear path upward otherwise remove.
				for (int y = 0; y <= 3; y++) {
					final Block tempBlock = this.location.clone().add(0, y, 0).getBlock();
					if (!this.isTransparent(tempBlock) && !ElementalAbility.isAir(tempBlock.getType())) {
						this.remove();
						return;
					}
				}
				// Design what this EarthSmash looks like by using BlockRepresenters.
				final Location tempLoc = this.location.clone().add(0, -2, 0);
				for (int x = -1; x <= 1; x++) {
					for (int y = -1; y <= 1; y++) {
						for (int z = -1; z <= 1; z++) {
							if ((Math.abs(x) + Math.abs(y) + Math.abs(z)) % 2 == 0) {
								final Block block = tempLoc.clone().add(x, y, z).getBlock();
								this.currentBlocks.add(new BlockRepresenter(x, y, z, this.selectMaterialForRepresenter(block.getType()), block.getBlockData()));
							}
						}
					}
				}

				// Remove the design of the second level of removed dirt.
				for (int x = -1; x <= 1; x++) {
					for (int z = -1; z <= 1; z++) {
						if ((Math.abs(x) + Math.abs(z)) % 2 == 1) {
							final Block block = this.location.clone().add(x, -2, z).getBlock();
							if (this.isEarthbendable(block)) {
								addTempAirBlock(block);
							}
						}

						// Remove the first level of dirt.
						final Block block = this.location.clone().add(x, -1, z).getBlock();
						if (this.isEarthbendable(block)) {
							addTempAirBlock(block);
						}
					}
				}

				/*
				 * We needed to calculate all of the blocks based on the
				 * location being 1 above the initial bending block, however we
				 * want to animate it starting from the original bending block.
				 * We must readjust the location back to what it originally was.
				 */
				this.location.add(0, -1, 0);

			}
			// Move any entities that are above the rock.
			final List<Entity> entities = GeneralMethods.getEntitiesAroundPoint(this.location, 2.5);
			for (final Entity entity : entities) {
				final org.bukkit.util.Vector velocity = entity.getVelocity();
				GeneralMethods.setVelocity(this, entity, velocity.add(new Vector(0, 0.36, 0)));
			}
			this.location.getWorld().playEffect(this.location, Effect.GHAST_SHOOT, 0, 7);
			this.draw();
		} else {
			this.state = State.LIFTED;
		}
		this.animationCounter++;
	}

	/**
	 * Redraws the blocks for this instance of EarthSmash.
	 */
	public void draw() {
		if (this.currentBlocks.size() == 0) {
			this.remove();
			return;
		}
		for (final BlockRepresenter blockRep : this.currentBlocks) {
			final Block block = this.location.clone().add(blockRep.getX(), blockRep.getY(), blockRep.getZ()).getBlock();
			if (block.getType().equals(Material.SAND) || block.getType().equals(Material.GRAVEL)) { // Check if block can be affected by gravity.

			}
			if (this.player != null && this.isTransparent(block)) {
				this.affectedBlocks.add(new TempBlock(block, blockRep.getType()));
				getPreventEarthbendingBlocks().add(block);
			}
		}
	}

	public void revert() {
		this.checkRemainingBlocks();
		for (int i = 0; i < this.affectedBlocks.size(); i++) {
			final TempBlock tblock = this.affectedBlocks.get(i);
			getPreventEarthbendingBlocks().remove(tblock.getBlock());
			tblock.revertBlock();
			this.affectedBlocks.remove(i);
			i--;
		}
	}

	/**
	 * Checks to see which of the blocks are still attached to the EarthSmash,
	 * remember that blocks can be broken or used in other abilities so we need
	 * to double check and remove any that are not still attached.
	 *
	 * Also when we remove the blocks from instances, movedearth, or tempair we
	 * should do it on a delay because tempair takes a couple seconds before the
	 * block shows up in that map.
	 */
	public void checkRemainingBlocks() {
		for (int i = 0; i < this.currentBlocks.size(); i++) {
			final BlockRepresenter brep = this.currentBlocks.get(i);
			final Block block = this.location.clone().add(brep.getX(), brep.getY(), brep.getZ()).getBlock();
			// Check for grass because sometimes the dirt turns into grass.
			if (block.getType() != brep.getType() && (block.getType() != Material.GRASS) && (block.getType() != Material.COBBLESTONE)) {
				this.currentBlocks.remove(i);
				i--;
			}
		}
	}

	@Override
	public void remove() {
		super.remove();
		this.state = State.REMOVED;
		this.revert();
	}

	/**
	 * Gets the blocks surrounding the EarthSmash's loc. This method ignores the
	 * blocks that should be Air, and only returns the ones that are dirt.
	 */
	public List<Block> getBlocks() {
		final List<Block> blocks = new ArrayList<Block>();
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				for (int z = -1; z <= 1; z++) {
					if ((Math.abs(x) + Math.abs(y) + Math.abs(z)) % 2 == 0) { // Give it the cool shape.
						if (this.location != null) {
							blocks.add(this.location.getWorld().getBlockAt(this.location.clone().add(x, y, z)));
						}
					}
				}
			}
		}
		return blocks;
	}

	/**
	 * Gets the blocks surrounding the EarthSmash's loc. This method returns all
	 * the blocks surrounding the loc, including dirt and air.
	 */
	public List<Block> getBlocksIncludingInner() {
		final List<Block> blocks = new ArrayList<Block>();
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				for (int z = -1; z <= 1; z++) {
					if (this.location != null) {
						blocks.add(this.location.getWorld().getBlockAt(this.location.clone().add(x, y, z)));
					}
				}
			}
		}
		return blocks;
	}

	/**
	 * Switches the Sand Material and Gravel to SandStone and stone
	 * respectively, since gravel and sand cannot be bent due to gravity.
	 */
	public static Material selectMaterial(final Material mat) {
		if (mat == Material.SAND) {
			return Material.SANDSTONE;
		} else if (mat == Material.GRAVEL) {
			return Material.STONE;
		} else {
			return mat;
		}
	}

	public Material selectMaterialForRepresenter(final Material mat) {
		final Material tempMat = selectMaterial(mat);
		final Random rand = new Random();
		if (!isEarthbendable(tempMat, true, true, true) && !this.isMetalbendable(tempMat)) {
			if (this.currentBlocks.size() < 1) {
				return Material.DIRT;
			} else {
				return this.currentBlocks.get(rand.nextInt(this.currentBlocks.size())).getType();
			}
		}
		return tempMat;
	}

	/**
	 * Determines if a player is trying to grab an EarthSmash. A player is
	 * trying to grab an EarthSmash if they are staring at it and holding shift.
	 */
	private EarthSmash aimingAtSmashCheck(final Player player, final State reqState) {
		if (!this.allowGrab) {
			return null;
		}

		final List<Block> blocks = GeneralMethods.getBlocksAroundPoint(GeneralMethods.getTargetedLocation(player, this.grabRange, getTransparentMaterials()), 1);
		for (final EarthSmash smash : getAbilities(EarthSmash.class)) {
			if (reqState == null || smash.state == reqState) {
				for (final Block block : blocks) {
					if (block == null || smash.getLocation() == null) {
						continue;
					}
					if (block.getLocation().getWorld() == smash.location.getWorld() && block.getLocation().distanceSquared(smash.location) <= Math.pow(this.grabDetectionRadius, 2)) {
						return smash;
					}
				}
			}
		}
		return null;
	}

	/**
	 * This method handles any collision between an EarthSmash and the
	 * surrounding entities, the method only applies to earthsmashes that have
	 * already been shot.
	 */
	public void shootingCollisionDetection() {
		final List<Entity> entities = GeneralMethods.getEntitiesAroundPoint(this.location, this.hitRadius);
		for (final Entity entity : entities) {
			if (entity instanceof LivingEntity && entity != this.player && !this.affectedEntities.contains(entity)) {
				if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation()) || ((entity instanceof Player) && Commands.invincible.contains(((Player) entity).getName()))) {
					continue;
				}
				this.affectedEntities.add(entity);
				final double damage = this.currentBlocks.size() / 13.0 * this.damage;
				DamageHandler.damageEntity(entity, damage, this);
				final Vector travelVec = GeneralMethods.getDirection(this.location, entity.getLocation());
				GeneralMethods.setVelocity(this, entity, travelVec.setY(this.knockup).normalize().multiply(this.knockback));
			}
		}
	}

	/**
	 * EarthSmash to EarthSmash collision can only happen when one of the
	 * Smashes have been shot by a player. If we find out that one of them have
	 * collided then we want to return since a smash can only remove 1 at a
	 * time.
	 */
	public void smashToSmashCollisionDetection() {
		for (final EarthSmash smash : getAbilities(EarthSmash.class)) {
			if (smash.location != null && smash != this && smash.location.getWorld() == this.location.getWorld() && smash.location.distanceSquared(this.location) < Math.pow(this.flightDetectionRadius, 2)) {
				smash.remove();
				this.remove();
				return;
			}
		}
	}

	/**
	 * Determines whether or not a player is trying to fly ontop of an
	 * EarthSmash. A player is considered "flying" if they are standing ontop of
	 * the earthsmash and holding shift.
	 */
	private static EarthSmash flyingInSmashCheck(final Player player) {
		for (final EarthSmash smash : getAbilities(EarthSmash.class)) {
			if (!smash.allowFlight) {
				continue;
			}
			// Check to see if the player is standing on top of the smash.
			if (smash.state == State.LIFTED) {
				if (smash.location.getWorld().equals(player.getWorld()) && smash.location.clone().add(0, 2, 0).distanceSquared(player.getLocation()) <= Math.pow(smash.flightDetectionRadius, 2)) {
					return smash;
				}
			}
		}
		return null;
	}

	/**
	 * A BlockRepresenter is used to keep track of each of the individual types
	 * of blocks that are attached to an EarthSmash. Without the representer
	 * then an EarthSmash can only be made up of 1 material at a time. For
	 * example, an ESmash that is entirely dirt, coalore, or sandstone. Using
	 * the representer will allow all the materials to be mixed together.
	 */
	public class BlockRepresenter {
		private int x, y, z;
		private Material type;
		private BlockData data;

		public BlockRepresenter(final int x, final int y, final int z, final Material type, final BlockData data) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.type = type;
			this.data = data;
		}

		public int getX() {
			return this.x;
		}

		public int getY() {
			return this.y;
		}

		public int getZ() {
			return this.z;
		}

		public Material getType() {
			return this.type;
		}

		public BlockData getData() {
			return this.data;
		}

		public void setX(final int x) {
			this.x = x;
		}

		public void setY(final int y) {
			this.y = y;
		}

		public void setZ(final int z) {
			this.z = z;
		}

		public void setType(final Material type) {
			this.type = type;
		}

		public void setData(final BlockData data) {
			this.data = data;
		}

		@Override
		public String toString() {
			return this.x + ", " + this.y + ", " + this.z + ", " + this.type.toString();
		}
	}

	public class Pair<F, S> {
		private F first; // first member of pair.
		private S second; // second member of pair.

		public Pair(final F first, final S second) {
			this.first = first;
			this.second = second;
		}

		public void setFirst(final F first) {
			this.first = first;
		}

		public void setSecond(final S second) {
			this.second = second;
		}

		public F getFirst() {
			return this.first;
		}

		public S getSecond() {
			return this.second;
		}
	}

	@Override
	public String getName() {
		return "EarthSmash";
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
	public List<Location> getLocations() {
		final ArrayList<Location> locations = new ArrayList<>();
		for (final TempBlock tblock : this.affectedBlocks) {
			locations.add(tblock.getLocation());
		}
		return locations;
	}

	public boolean isAllowGrab() {
		return this.allowGrab;
	}

	public void setAllowGrab(final boolean allowGrab) {
		this.allowGrab = allowGrab;
	}

	public boolean isAllowFlight() {
		return this.allowFlight;
	}

	public void setAllowFlight(final boolean allowFlight) {
		this.allowFlight = allowFlight;
	}

	public int getAnimationCounter() {
		return this.animationCounter;
	}

	public void setAnimationCounter(final int animationCounter) {
		this.animationCounter = animationCounter;
	}

	public int getProgressCounter() {
		return this.progressCounter;
	}

	public void setProgressCounter(final int progressCounter) {
		this.progressCounter = progressCounter;
	}

	public int getRequiredBendableBlocks() {
		return this.requiredBendableBlocks;
	}

	public void setRequiredBendableBlocks(final int requiredBendableBlocks) {
		this.requiredBendableBlocks = requiredBendableBlocks;
	}

	public int getMaxBlocksToPassThrough() {
		return this.maxBlocksToPassThrough;
	}

	public void setMaxBlocksToPassThrough(final int maxBlocksToPassThrough) {
		this.maxBlocksToPassThrough = maxBlocksToPassThrough;
	}

	public long getDelay() {
		return this.delay;
	}

	public void setDelay(final long delay) {
		this.delay = delay;
	}

	public long getChargeTime() {
		return this.chargeTime;
	}

	public void setChargeTime(final long chargeTime) {
		this.chargeTime = chargeTime;
	}

	public long getDuration() {
		return this.duration;
	}

	public void setDuration(final long duration) {
		this.duration = duration;
	}

	public long getFlightDuration() {
		return this.flightDuration;
	}

	public void setFlightDuration(final long flightDuration) {
		this.flightDuration = flightDuration;
	}

	public long getFlightStartTime() {
		return this.flightStartTime;
	}

	public void setFlightStartTime(final long flightStartTime) {
		this.flightStartTime = flightStartTime;
	}

	public long getShootAnimationInterval() {
		return this.shootAnimationInterval;
	}

	public void setShootAnimationInterval(final long shootAnimationInterval) {
		this.shootAnimationInterval = shootAnimationInterval;
	}

	public long getFlightAnimationInterval() {
		return this.flightAnimationInterval;
	}

	public void setFlightAnimationInterval(final long flightAnimationInterval) {
		this.flightAnimationInterval = flightAnimationInterval;
	}

	public long getLiftAnimationInterval() {
		return this.liftAnimationInterval;
	}

	public void setLiftAnimationInterval(final long liftAnimationInterval) {
		this.liftAnimationInterval = liftAnimationInterval;
	}

	public double getGrabRange() {
		return this.grabRange;
	}

	public void setGrabRange(final double grabRange) {
		this.grabRange = grabRange;
	}

	public double getSelectRange() {
		return this.selectRange;
	}

	public void setSelectRange(final double selectRange) {
		this.selectRange = selectRange;
	}

	public double getShootRange() {
		return this.shootRange;
	}

	public void setShootRange(final double shootRange) {
		this.shootRange = shootRange;
	}

	public double getDamage() {
		return this.damage;
	}

	public void setDamage(final double damage) {
		this.damage = damage;
	}

	public double getKnockback() {
		return this.knockback;
	}

	public void setKnockback(final double knockback) {
		this.knockback = knockback;
	}

	public double getKnockup() {
		return this.knockup;
	}

	public void setKnockup(final double knockup) {
		this.knockup = knockup;
	}

	public double getFlightSpeed() {
		return this.flightSpeed;
	}

	public void setFlightSpeed(final double flightSpeed) {
		this.flightSpeed = flightSpeed;
	}

	public double getGrabbedDistance() {
		return this.grabbedDistance;
	}

	public void setGrabbedDistance(final double grabbedDistance) {
		this.grabbedDistance = grabbedDistance;
	}

	public double getGrabDetectionRadius() {
		return this.grabDetectionRadius;
	}

	public void setGrabDetectionRadius(final double grabDetectionRadius) {
		this.grabDetectionRadius = grabDetectionRadius;
	}

	public double getFlightDetectionRadius() {
		return this.flightDetectionRadius;
	}

	public void setFlightDetectionRadius(final double flightDetectionRadius) {
		this.flightDetectionRadius = flightDetectionRadius;
	}

	public State getState() {
		return this.state;
	}

	public void setState(final State state) {
		this.state = state;
	}

	public Block getOrigin() {
		return this.origin;
	}

	public void setOrigin(final Block origin) {
		this.origin = origin;
	}

	public Location getDestination() {
		return this.destination;
	}

	public void setDestination(final Location destination) {
		this.destination = destination;
	}

	public ArrayList<Entity> getAffectedEntities() {
		return this.affectedEntities;
	}

	public ArrayList<BlockRepresenter> getCurrentBlocks() {
		return this.currentBlocks;
	}

	public ArrayList<TempBlock> getAffectedBlocks() {
		return this.affectedBlocks;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

}

package com.projectkorra.projectkorra.airbending;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.region.RegionProtection;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Switch;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.earthbending.lava.LavaFlow;
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;

public class AirBlast extends AirAbility {

	private static final int MAX_TICKS = 10000;
	private static final Map<Player, Location> ORIGINS = new ConcurrentHashMap<>();
	@Deprecated(since = "1.13.0")
	public static final Material[] DOORS = Tag.WOODEN_DOORS.getValues().toArray(new Material[0]);
	@Deprecated(since = "1.13.0")
	public static final Material[] TDOORS = Tag.WOODEN_TRAPDOORS.getValues().toArray(new Material[0]);
	@Deprecated(since = "1.13.0")
	public static final Material[] BUTTONS = Tag.BUTTONS.getValues().toArray(new Material[0]);

	private boolean canFlickLevers;
	private boolean canOpenDoors;
	private boolean canPressButtons;
	private boolean canCoolLava;
	private boolean isFromOtherOrigin;
	private boolean showParticles;
	private int ticks;
	private int particles;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	private double speedFactor;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute(Attribute.SELF_PUSH)
	private double pushFactor;
	@Attribute(Attribute.KNOCKBACK)
	private double pushFactorForOthers;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.RADIUS)
	private double radius;
	private Location location;
	private Location origin;
	private Vector direction;
	private AirBurst source;
	private Random random;
	private ArrayList<Block> affectedBlocks;
	private ArrayList<Entity> affectedEntities;

	public AirBlast(final Player player) {
		super(player);
		if (this.bPlayer.isOnCooldown(this)) {
			return;
		} else if (player.getEyeLocation().getBlock().isLiquid()) {
			return;
		}

		this.setFields();

		this.origin = ORIGINS.remove(player);
		if (this.origin != null) {
			final Entity entity = GeneralMethods.getTargetedEntity(player, this.range);
			this.isFromOtherOrigin = true;
			if (entity != null) {
				this.direction = GeneralMethods.getDirection(this.origin, GeneralMethods.getTargetedLocation(player, this.range, false, false)).normalize();
			} else {
				this.direction = GeneralMethods.getDirection(this.origin, GeneralMethods.getTargetedLocation(player, this.range)).normalize();
			}
		} else {
			this.origin = player.getEyeLocation();
			this.direction = this.origin.getDirection();
		}
		if (!GeneralMethods.isFinite(this.direction)) {
			return;
		}
		this.location = this.origin.clone();
		this.bPlayer.addCooldown(this);
		this.start();
	}

	public AirBlast(final Player player, final Location location, final Vector direction, final double modifiedPushFactor, final AirBurst burst) {
		super(player);
		if (location.getBlock().isLiquid()) {
			return;
		}
		this.source = burst;
		this.origin = location.clone();
		this.direction = direction.clone();
		this.location = location.clone();

		this.setFields();

		this.affectedBlocks = new ArrayList<>();
		this.affectedEntities = new ArrayList<>();

		// prevent the airburst related airblasts from triggering doors/levers/buttons.
		this.canOpenDoors = false;
		this.canPressButtons = false;
		this.canFlickLevers = false;

		if (this.bPlayer.isAvatarState()) {
			this.pushFactor = getConfig().getDouble("Abilities.Avatar.AvatarState.Air.AirBlast.Push.Self");
			this.pushFactorForOthers = getConfig().getDouble("Abilities.Avatar.AvatarState.Air.AirBlast.Push.Entities");
		}

		this.pushFactor *= modifiedPushFactor;

		this.start();
	}

	private void setFields() {
		this.particles = getConfig().getInt("Abilities.Air.AirBlast.Particles");
		this.cooldown = getConfig().getLong("Abilities.Air.AirBlast.Cooldown");
		this.range = getConfig().getDouble("Abilities.Air.AirBlast.Range");
		this.speed = getConfig().getDouble("Abilities.Air.AirBlast.Speed");
		this.range = getConfig().getDouble("Abilities.Air.AirBlast.Range");
		this.radius = getConfig().getDouble("Abilities.Air.AirBlast.Radius");
		this.pushFactor = getConfig().getDouble("Abilities.Air.AirBlast.Push.Self");
		this.pushFactorForOthers = getConfig().getDouble("Abilities.Air.AirBlast.Push.Entities");
		this.canFlickLevers = getConfig().getBoolean("Abilities.Air.AirBlast.CanFlickLevers");
		this.canOpenDoors = getConfig().getBoolean("Abilities.Air.AirBlast.CanOpenDoors");
		this.canPressButtons = getConfig().getBoolean("Abilities.Air.AirBlast.CanPressButtons");
		this.canCoolLava = getConfig().getBoolean("Abilities.Air.AirBlast.CanCoolLava");

		this.isFromOtherOrigin = false;
		this.showParticles = true;
		this.random = new Random();
		this.affectedBlocks = new ArrayList<>();
		this.affectedEntities = new ArrayList<>();
	}

	private static void playOriginEffect(final Player player) {
		final Location origin = ORIGINS.get(player);
		if (origin == null) {
			return;
		}

		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null || player.isDead()) {
			return;
		} else if (!origin.getWorld().equals(player.getWorld())) {
			ORIGINS.remove(player);
			return;
		} else if (!bPlayer.canBendIgnoreCooldowns(getAbility("AirBlast"))) {
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
		final Location location = GeneralMethods.getTargetedLocation(player, getSelectRange(), getTransparentMaterials());
		if (location.getBlock().isLiquid() || GeneralMethods.isSolid(location.getBlock())) {
			return;
		} else if (RegionProtection.isRegionProtected(player, location, "AirBlast")) {
			return;
		}

		ORIGINS.put(player, location);
	}

	private void advanceLocation() {
		if (this.showParticles) {
			playAirbendingParticles(this.location, this.particles, 0.275F, 0.275F, 0.275F);
		}
		if (this.random.nextInt(4) == 0) {
			playAirbendingSound(this.location);
		}

		BlockIterator blocks = new BlockIterator(this.location.getWorld(), this.location.toVector(), this.direction, 0, (int) Math.ceil(this.direction.clone().multiply(speedFactor).length()));

		while (blocks.hasNext() && checkLocation(blocks.next()));
		
		this.location.add(this.direction.clone().multiply(speedFactor));
	}

	public boolean checkLocation(Block block) {
		Location blockLocation = block.getLocation();
		if (GeneralMethods.checkDiagonalWall(blockLocation, this.direction)) {
			this.remove();
			return false;
		}

		if ((!block.isPassable() || block.isLiquid()) && !this.affectedBlocks.contains(block)) {
			if (block.getType() == Material.LAVA && this.canCoolLava) {
				if (LavaFlow.isLavaFlowBlock(block)) {
					LavaFlow.removeBlock(block); // TODO: Make more generic for future lava generating moves.
				} else if (block.getBlockData() instanceof Levelled levelled && levelled.getLevel() == 0) {
					new TempBlock(block, Material.OBSIDIAN);
				} else {
					new TempBlock(block, Material.COBBLESTONE);
				}
			}
			this.remove();
			return false;
		}
		if (!processBlock(blockLocation)) {
			remove();
			return false;
		}
		
		return true;
	}

	private void affect(final Entity entity) {
		final boolean isUser = entity == this.player;
		double knockback = this.pushFactorForOthers;

		if (isUser) {
			if (isFromOtherOrigin) {
				knockback = this.pushFactor;
			} else {
				return;
			}
		}

		final double max = this.speed / this.speedFactor;

		final Vector push = this.direction.clone();
		if (Math.abs(push.getY()) > max && !isUser) {
			if (push.getY() < 0) {
				push.setY(-max);
			} else {
				push.setY(max);
			}
		}

		if (this.location.getWorld().equals(this.origin.getWorld())) {
			knockback *= 1 - this.location.distance(this.origin) / (2 * this.range);
		}
		
		if (GeneralMethods.isSolid(entity.getLocation().add(0, -0.5, 0).getBlock()) && source == null) {
			knockback *= 0.85;
		}
		
		push.normalize().multiply(knockback);

		Vector velocity = entity.getVelocity();
		if (Math.abs(velocity.dot(push)) > knockback && velocity.angle(push) > Math.PI / 3) {
			push.normalize().add(velocity).multiply(knockback);
		}
		GeneralMethods.setVelocity(this, entity, push);

        new HorizontalVelocityTracker(entity, this.player, 200L, this.source != null ? this.source : this);

		if (!isUser && this.damage > 0 && entity instanceof LivingEntity && !this.affectedEntities.contains(entity)) {
			DamageHandler.damageEntity(entity, this.damage, this.source != null ? this.source : this);
			this.affectedEntities.add(entity);
		}

		if (entity.getFireTicks() > 0) {
			entity.getWorld().playEffect(entity.getLocation(), Effect.EXTINGUISH, 0);
		}

		entity.setFireTicks(0);
		breakBreathbendingHold(entity);
	}

	@Override
	public void progress() {
		if (this.player.isDead()) {
			this.remove();
			return;
		} else if (GeneralMethods.isRegionProtectedFromBuild(this, this.location)) {
			this.remove();
			return;
		}

		this.speedFactor = this.speed * (ProjectKorra.time_step / 1000.0);
		this.ticks++;

		if (this.ticks > MAX_TICKS) {
			this.remove();
			return;
		}

		for (final Block testblock : GeneralMethods.getBlocksAroundPoint(this.location, this.radius)) {
			if (!processBlock(testblock.getLocation())) {
				remove();
				return;
			}
		}

		/*
		 * If a player presses shift and AirBlasts straight down then the
		 * AirBlast's location gets messed up and reading the distance returns
		 * Double.NaN. If we don't remove this instance then the AirBlast will
		 * never be removed.
		 */
		double dist = 0;
		if (this.location.getWorld().equals(this.origin.getWorld())) {
			dist = this.location.distance(this.origin);
		}
		if (Double.isNaN(dist) || dist > this.range) {
			this.remove();
			return;
		}

		for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(this.location, this.radius)) {
			if ((entity instanceof Player && Commands.invincible.contains(entity.getName())) || GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation())) {
				continue;
			}
			this.affect(entity);
		}

		this.advanceLocation();
	}

	/**
	 * Process all blocks that should be modified for the selected location
	 * @param location The location of the block
	 * @return False if the ability should be removed
	 */
	private boolean processBlock(Location location) {
		Block block = location.getBlock();
		Material type = block.getType();
		if (RegionProtection.isRegionProtected(this, location)) {
			return false;
		} else if (ElementalAbility.tryExtinguish(block)) {
			return false;
		} else if (this.affectedBlocks.contains(block)) {
			return false;
		}

		BlockData data = block.getBlockData();
		if (Tag.WOODEN_DOORS.isTagged(type) && data instanceof Door door) {
			final BlockFace face = door.getFacing();
			final Vector toPlayer = GeneralMethods.getDirection(block.getLocation(), this.player.getLocation().getBlock().getLocation());
			final double[] dims = { toPlayer.getX(), toPlayer.getY(), toPlayer.getZ() };

			for (int i = 0; i < 3; i++) {
				if (i == 1) {
					continue;
				}

				final BlockFace bf = GeneralMethods.getBlockFaceFromValue(i, dims[i]);
				if (bf == face && !door.isOpen()) {
					return false;
				} else if (bf.getOppositeFace() == face && door.isOpen()) {
					return false;
				}
			}

			door.setOpen(!door.isOpen());
			block.setBlockData(door);
			block.getWorld().playSound(block.getLocation(), door.isOpen() ? Sound.BLOCK_WOODEN_DOOR_OPEN : Sound.BLOCK_WOODEN_DOOR_CLOSE, 0.5f, 0);
			this.affectedBlocks.add(block);
		} else if (Tag.WOODEN_TRAPDOORS.isTagged(type) && data instanceof TrapDoor trapDoor) {
			if (this.origin.getY() < block.getY() && !trapDoor.isOpen()) {
				return false;
			} else if (trapDoor.isOpen()) {
				return false;
			}

			trapDoor.setOpen(!trapDoor.isOpen());
			block.setBlockData(trapDoor);
			block.getWorld().playSound(block.getLocation(), trapDoor.isOpen()
					? Sound.BLOCK_WOODEN_TRAPDOOR_OPEN
					: Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE, 0.5f, 0);
		} else if (Tag.BUTTONS.isTagged(type) && data instanceof Switch button) {
			if (!button.isPowered()) {
				button.setPowered(true);
				block.setBlockData(button);
				this.affectedBlocks.add(block);
				Bukkit.getScheduler().runTaskLater(ProjectKorra.plugin, () -> {
					// TODO: test if this will overwrite a block change made before button unpresses
					button.setPowered(false);
					block.setBlockData(button);
					AirBlast.this.affectedBlocks.remove(block);
					block.getWorld().playSound(block.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_OFF, 0.5f, 0);
				}, 15L);
			}

			block.getWorld().playSound(block.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 0.5f, 0);
		} else if (type == Material.LEVER && data instanceof Switch lever) {
			lever.setPowered(!lever.isPowered());
			block.setBlockData(lever);
			this.affectedBlocks.add(block);
			block.getWorld().playSound(block.getLocation(), Sound.BLOCK_LEVER_CLICK, 0.5f, 0);
		} else if (Tag.CANDLES.isTagged(type) || Tag.CAMPFIRES.isTagged(type) || type == Material.REDSTONE_TORCH || type == Material.REDSTONE_WALL_TORCH) {
			if (data instanceof Lightable lightable && lightable.isLit()) {
				lightable.setLit(false);
				block.setBlockData(lightable);
				block.getWorld().playEffect(block.getLocation(), Effect.EXTINGUISH, 0);
			}
		}
		return true;
	}

	/**
	 * This method was used for the old collision detection system. Please see
	 * {@link Collision} for the new system.
	 */
	@Deprecated
	public static boolean removeAirBlastsAroundPoint(final Location location, final double radius) {
		boolean removed = false;
		for (final AirBlast airBlast : getAbilities(AirBlast.class)) {
			final Location airBlastlocation = airBlast.location;
			if (location.getWorld() == airBlastlocation.getWorld()) {
				if (location.distanceSquared(airBlastlocation) <= radius * radius) {
					airBlast.remove();
				}
				removed = true;
			}
		}
		return removed;
	}

	@Override
	public String getName() {
		return "AirBlast";
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

	public int getTicks() {
		return this.ticks;
	}

	public void setTicks(final int ticks) {
		this.ticks = ticks;
	}

	public double getSpeedFactor() {
		return this.speedFactor;
	}

	public void setSpeedFactor(final double speedFactor) {
		this.speedFactor = speedFactor;
	}

	public double getRange() {
		return this.range;
	}

	public void setRange(final double range) {
		this.range = range;
	}

	public double getPushFactor() {
		return this.pushFactor;
	}

	public void setPushFactor(final double pushFactor) {
		this.pushFactor = pushFactor;
	}

	public double getPushFactorForOthers() {
		return this.pushFactorForOthers;
	}

	public void setPushFactorForOthers(final double pushFactorForOthers) {
		this.pushFactorForOthers = pushFactorForOthers;
	}

	public double getDamage() {
		return this.damage;
	}

	public void setDamage(final double damage) {
		this.damage = damage;
	}

	public double getSpeed() {
		return this.speed;
	}

	public void setSpeed(final double speed) {
		this.speed = speed;
	}

	public double getRadius() {
		return this.radius;
	}

	public void setRadius(final double radius) {
		this.radius = radius;
	}

	public boolean isCanFlickLevers() {
		return this.canFlickLevers;
	}

	public void setCanFlickLevers(final boolean canFlickLevers) {
		this.canFlickLevers = canFlickLevers;
	}

	public boolean isCanOpenDoors() {
		return this.canOpenDoors;
	}

	public void setCanOpenDoors(final boolean canOpenDoors) {
		this.canOpenDoors = canOpenDoors;
	}

	public boolean isCanPressButtons() {
		return this.canPressButtons;
	}

	public void setCanPressButtons(final boolean canPressButtons) {
		this.canPressButtons = canPressButtons;
	}

	public boolean isCanCoolLava() {
		return this.canCoolLava;
	}

	public void setCanCoolLava(final boolean canCoolLava) {
		this.canCoolLava = canCoolLava;
	}

	public boolean isFromOtherOrigin() {
		return this.isFromOtherOrigin;
	}

	public void setFromOtherOrigin(final boolean isFromOtherOrigin) {
		this.isFromOtherOrigin = isFromOtherOrigin;
	}

	public boolean isShowParticles() {
		return this.showParticles;
	}

	public void setShowParticles(final boolean showParticles) {
		this.showParticles = showParticles;
	}

	public AirBurst getSource() {
		return this.source;
	}

	public void setSource(final AirBurst source) {
		this.source = source;
	}

	/**
	 * @deprecated Use {@link #getAffectedBlocks()}
	 */
	@Deprecated(since = "1.13.0")
	public ArrayList<Block> getAffectedLevers() {
		return this.affectedBlocks;
	}

	public List<Block> getAffectedBlocks() {
		return this.affectedBlocks;
	}

	public ArrayList<Entity> getAffectedEntities() {
		return this.affectedEntities;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

	public int getParticles() {
		return this.particles;
	}

	public void setParticles(final int particles) {
		this.particles = particles;
	}

	public static int getSelectParticles() {
		return getConfig().getInt("Abilities.Air.AirBlast.SelectParticles");
	}

	public static double getSelectRange() {
		return getConfig().getInt("Abilities.Air.AirBlast.SelectRange");
	}

}

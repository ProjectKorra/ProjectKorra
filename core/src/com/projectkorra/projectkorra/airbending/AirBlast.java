package com.projectkorra.projectkorra.airbending;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.region.RegionProtection;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Switch;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.earthbending.lava.LavaFlow;
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;

public class AirBlast extends AirAbility {

	private static final Map<UUID, AirBlastData> playerBlastData = new ConcurrentHashMap<>();
	static class AirBlastData {
		private int chainCount;
		private long lastBlastTime;

		public AirBlastData() {
			this.chainCount = 0;
			this.lastBlastTime = 0;
		}
	}

	private static final int MAX_TICKS = 10000;
	private static final Map<Player, Location> ORIGINS = new ConcurrentHashMap<>();
	public static final Material[] DOORS = Tag.WOODEN_DOORS.getValues().toArray(new Material[0]);
	public static final Material[] TDOORS = Tag.WOODEN_TRAPDOORS.getValues().toArray(new Material[0]);
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
	private ArrayList<Block> affectedLevers;
	private ArrayList<Entity> affectedEntities;

	@Attribute("MaxChains")
	private int maxChains;
	@Attribute("LongCooldown")
	private long longCooldown;

	public AirBlast(final Player player) {
		super(player);
		if (this.bPlayer.isOnCooldown(this)) {
			return;
		} else if (player.getEyeLocation().getBlock().isLiquid()) {
			return;
		}

		this.setFields();

		// Retrieve or create player's AirBlast data
		AirBlastData data = playerBlastData.computeIfAbsent(player.getUniqueId(), p -> new AirBlastData());

		long currentTime = System.currentTimeMillis();

		// Reset chain count if the time since last blast exceeds the long cooldown
		if (currentTime - data.lastBlastTime > this.longCooldown) {
			data.chainCount = 0;
		}

		// Enforce max chains limit
		if (data.chainCount >= this.maxChains) {
			// this.bPlayer.addCooldown(this, this.longCooldown); // Apply long cooldown
			data.chainCount = 0; // Reset chain count for future use
		}

		if (ORIGINS.containsKey(player)) {
			final Entity entity = GeneralMethods.getTargetedEntity(player, this.range);
			this.isFromOtherOrigin = true;
			this.origin = ORIGINS.get(player);
			ORIGINS.remove(player);

			if (entity != null) {
				this.direction = GeneralMethods.getDirection(this.origin, GeneralMethods.getTargetedLocation(player, this.range, false, false)).normalize();
			} else {
				this.direction = GeneralMethods.getDirection(this.origin, GeneralMethods.getTargetedLocation(player, this.range)).normalize();
			}
		} else {
			this.origin = player.getEyeLocation();
			this.direction = player.getEyeLocation().getDirection().normalize();
		}
		if(!Double.isFinite(this.direction.getX()) || !Double.isFinite(this.direction.getY()) || !Double.isFinite(this.direction.getZ())) {
			return;
		}
		this.location = this.origin.clone();

		// Update chain count and last blast time
		data.chainCount++;
		data.lastBlastTime = currentTime;

		// Update the map
		playerBlastData.put(player.getUniqueId(), data);

		if (data.chainCount >= this.maxChains) {
			this.bPlayer.addCooldown(this, this.longCooldown); // Apply long cooldown
		} else {
			this.bPlayer.addCooldown(this);
		}

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

		this.affectedLevers = new ArrayList<>();
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
		this.affectedLevers = new ArrayList<>();
		this.affectedEntities = new ArrayList<>();

		this.maxChains = getConfig().getInt("Abilities.Air.AirBlast.MaxChains");
		this.longCooldown = getConfig().getLong("Abilities.Air.AirBlast.LongCooldown");
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

	public static void setOrigin(final BendingPlayer player) {
		if (player.isOnCooldown("AirBlast")) {
			return;
		}
		final Location location = GeneralMethods.getTargetedLocation(player.getPlayer(), getSelectRange(), getTransparentMaterials());
		if (location.getBlock().isLiquid() || GeneralMethods.isSolid(location.getBlock())) {
			return;
		} else if (RegionProtection.isRegionProtected(player.getPlayer(), location, "AirBlast")) {
			return;
		}

		ORIGINS.put(player.getPlayer(), location);

	}

	private void advanceLocation() {
		if (this.showParticles) {
			playAirbendingParticles(this.location, this.particles, 0.275F, 0.275F, 0.275F);
		}
		if (this.random.nextInt(4) == 0) {
			playAirbendingSound(this.location);
		}

		BlockIterator blocks = new BlockIterator(this.getLocation().getWorld(), this.location.toVector(), this.direction, 0, (int) Math.ceil(this.direction.clone().multiply(speedFactor).length()));

		while (blocks.hasNext() && checkLocation(blocks.next()));
		
		this.location.add(this.direction.clone().multiply(speedFactor));
	}

	public boolean checkLocation(Block block) {
		if (GeneralMethods.checkDiagonalWall(block.getLocation(), this.direction)) {
			this.remove();
			return false;
		}

		if ((!block.isPassable() || block.isLiquid()) && !this.affectedLevers.contains(block)) {
			if (block.getType() == Material.LAVA && this.canCoolLava) {
				if (LavaFlow.isLavaFlowBlock(block)) {
					LavaFlow.removeBlock(block); // TODO: Make more generic for future lava generating moves.
				} else if (block.getBlockData() instanceof Levelled && ((Levelled) block.getBlockData()).getLevel() == 0) {
					new TempBlock(block, Material.OBSIDIAN);
				} else {
					new TempBlock(block, Material.COBBLESTONE);
				}
			}
			this.remove();
			return false;
		}
		if (!processBlock(block.getLocation())) {
			remove();
			return false;
		}
		
		return true;
	}

	private void affect(final Entity entity) {
		if (entity instanceof Player) {
			if (Commands.invincible.contains(((Player) entity).getName())) {
				return;
			}
		}
			
		final boolean isUser = entity.getUniqueId() == this.player.getUniqueId();
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
		
		if (Math.abs(entity.getVelocity().dot(push)) > knockback && entity.getVelocity().angle(push) > Math.PI / 3) {
			push.normalize().add(entity.getVelocity()).multiply(knockback);
		}
		GeneralMethods.setVelocity(this, entity, push);
		
		if (this.source != null) {
			new HorizontalVelocityTracker(entity, this.player, 200l, this.source);
		} else {
			new HorizontalVelocityTracker(entity, this.player, 200l, this);
		}

		if (this.damage > 0 && entity instanceof LivingEntity && !entity.equals(this.player) && !this.affectedEntities.contains(entity)) {
			if (this.source != null) {
				DamageHandler.damageEntity(entity, this.damage, this.source);
			} else {
				DamageHandler.damageEntity(entity, this.damage, this);
			}
			
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
		if (this.player.isDead() || !this.player.isOnline()) {
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

		final Block block = this.location.getBlock();

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
			if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation()) || ((entity instanceof Player) && Commands.invincible.contains(((Player) entity).getName()))) {
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
		Block testblock = location.getBlock();
		if (GeneralMethods.isRegionProtectedFromBuild(this, location)) {
			return false;
		} else if (FireAbility.isFire(testblock.getType())) {
			if (TempBlock.isTempBlock(testblock)) {
				TempBlock.removeBlock(testblock);
			} else {
				testblock.setType(Material.AIR);
			}
			
			testblock.getWorld().playEffect(testblock.getLocation(), Effect.EXTINGUISH, 0);
			return false;
		} else if (this.affectedLevers.contains(testblock)) {
			return false;
		}

		if (Arrays.asList(DOORS).contains(testblock.getType())) {
			if (testblock.getBlockData() instanceof Door) {
				final Door door = (Door) testblock.getBlockData();
				final BlockFace face = door.getFacing();
				final Vector toPlayer = GeneralMethods.getDirection(testblock.getLocation(), this.player.getLocation().getBlock().getLocation());
				final double[] dims = { toPlayer.getX(), toPlayer.getY(), toPlayer.getZ() };

				for (int i = 0; i < 3; i++) {
					if (i == 1) {
						continue;
					}

					final BlockFace bf = GeneralMethods.getBlockFaceFromValue(i, dims[i]);

					if (bf == face) {
						if (!door.isOpen()) {
							return false;
						}
					} else if (bf.getOppositeFace() == face) {
						if (door.isOpen()) {
							return false;
						}
					}
				}

				door.setOpen(!door.isOpen());
				testblock.setBlockData(door);
				testblock.getWorld().playSound(testblock.getLocation(), Sound.valueOf("BLOCK_WOODEN_DOOR_" + (door.isOpen() ? "OPEN" : "CLOSE")), 0.5f, 0);
				this.affectedLevers.add(testblock);
			}
		} else if (Arrays.asList(TDOORS).contains(testblock.getType())) {
			if (testblock.getBlockData() instanceof TrapDoor) {
				final TrapDoor tDoor = (TrapDoor) testblock.getBlockData();

				if (this.origin.getY() < testblock.getY()) {
					if (!tDoor.isOpen()) {
						return false;
					}
				} else {
					if (tDoor.isOpen()) {
						return false;
					}
				}

				tDoor.setOpen(!tDoor.isOpen());
				testblock.setBlockData(tDoor);
				testblock.getWorld().playSound(testblock.getLocation(), Sound.valueOf("BLOCK_WOODEN_TRAPDOOR_" + (tDoor.isOpen() ? "OPEN" : "CLOSE")), 0.5f, 0);
			}
		} else if (Arrays.asList(BUTTONS).contains(testblock.getType())) {
			if (testblock.getBlockData() instanceof Switch) {
				final Switch button = (Switch) testblock.getBlockData();
				if (!button.isPowered()) {
					button.setPowered(true);
					testblock.setBlockData(button);
					this.affectedLevers.add(testblock);

					new BukkitRunnable() {

						@Override
						public void run() {
							button.setPowered(false);
							testblock.setBlockData(button);
							AirBlast.this.affectedLevers.remove(testblock);
							testblock.getWorld().playSound(testblock.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_OFF, 0.5f, 0);
						}

					}.runTaskLater(ProjectKorra.plugin, 15);
				}

				testblock.getWorld().playSound(testblock.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 0.5f, 0);
			}
		} else if (testblock.getType() == Material.LEVER) {
			if (testblock.getBlockData() instanceof Switch) {
				final Switch lever = (Switch) testblock.getBlockData();
				lever.setPowered(!lever.isPowered());
				testblock.setBlockData(lever);
				this.affectedLevers.add(testblock);
				testblock.getWorld().playSound(testblock.getLocation(), Sound.BLOCK_LEVER_CLICK, 0.5f, 0);
			}
		} else if (testblock.getType().toString().contains("CANDLE") || testblock.getType().toString().contains("CAMPFIRE") || testblock.getType() == Material.REDSTONE_WALL_TORCH) {
			if (testblock.getBlockData() instanceof Lightable) {
				final Lightable lightable = (Lightable) testblock.getBlockData();
				if (lightable.isLit()) {
					lightable.setLit(false);
					testblock.setBlockData(lightable);
					testblock.getWorld().playEffect(testblock.getLocation(), Effect.EXTINGUISH, 0);
				}
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

	public ArrayList<Block> getAffectedLevers() {
		return this.affectedLevers;
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

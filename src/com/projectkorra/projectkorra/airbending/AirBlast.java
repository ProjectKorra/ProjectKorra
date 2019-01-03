package com.projectkorra.projectkorra.airbending;

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
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Switch;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class AirBlast extends AirAbility {

	private static final int MAX_TICKS = 10000;
	private static final Map<Player, Location> ORIGINS = new ConcurrentHashMap<>();
	public static final Material[] DOORS = { Material.ACACIA_DOOR, Material.BIRCH_DOOR, Material.DARK_OAK_DOOR, Material.JUNGLE_DOOR, Material.OAK_DOOR, Material.SPRUCE_DOOR };
	public static final Material[] TDOORS = { Material.ACACIA_TRAPDOOR, Material.BIRCH_TRAPDOOR, Material.DARK_OAK_TRAPDOOR, Material.JUNGLE_TRAPDOOR, Material.OAK_TRAPDOOR, Material.SPRUCE_TRAPDOOR };
	public static final Material[] BUTTONS = { Material.ACACIA_BUTTON, Material.BIRCH_BUTTON, Material.DARK_OAK_BUTTON, Material.JUNGLE_BUTTON, Material.OAK_BUTTON, Material.SPRUCE_BUTTON, Material.STONE_BUTTON };

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
	@Attribute(Attribute.KNOCKBACK)
	private double pushFactor;
	@Attribute(Attribute.KNOCKBACK + "Others")
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

	public AirBlast(final Player player) {
		super(player);
		if (this.bPlayer.isOnCooldown(this)) {
			return;
		} else if (player.getEyeLocation().getBlock().isLiquid()) {
			return;
		}

		this.setFields();

		if (ORIGINS.containsKey(player)) {
			final Entity entity = GeneralMethods.getTargetedEntity(player, this.range);
			this.isFromOtherOrigin = true;
			this.origin = ORIGINS.get(player);
			ORIGINS.remove(player);

			if (entity != null) {
				this.direction = GeneralMethods.getDirection(this.origin, entity.getLocation()).normalize();
			} else {
				this.direction = GeneralMethods.getDirection(this.origin, GeneralMethods.getTargetedLocation(player, this.range)).normalize();
			}
		} else {
			this.origin = player.getEyeLocation();
			this.direction = player.getEyeLocation().getDirection().normalize();
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
		this.pushFactor = getConfig().getDouble("Abilities.Air.AirBlast.Push.Entities");
		this.pushFactorForOthers = getConfig().getDouble("Abilities.Air.AirBlast.Push.Self");
		this.canFlickLevers = getConfig().getBoolean("Abilities.Air.AirBlast.CanFlickLevers");
		this.canOpenDoors = getConfig().getBoolean("Abilities.Air.AirBlast.CanOpenDoors");
		this.canPressButtons = getConfig().getBoolean("Abilities.Air.AirBlast.CanPressButtons");
		this.canCoolLava = getConfig().getBoolean("Abilities.Air.AirBlast.CanCoolLava");

		this.isFromOtherOrigin = false;
		this.showParticles = true;
		this.random = new Random();
		this.affectedLevers = new ArrayList<>();
		this.affectedEntities = new ArrayList<>();
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

	public static void setOrigin(final Player player) {
		final Location location = GeneralMethods.getTargetedLocation(player, getSelectRange(), getTransparentMaterials());
		if (location.getBlock().isLiquid() || GeneralMethods.isSolid(location.getBlock())) {
			return;
		} else if (GeneralMethods.isRegionProtectedFromBuild(player, "AirBlast", location)) {
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
		if (GeneralMethods.checkDiagonalWall(this.location, this.direction)) {
			this.remove();
			return;
		}

		this.location = this.location.add(this.direction.clone().multiply(this.speedFactor));
	}

	private void affect(final Entity entity) {
		final boolean isUser = entity.getUniqueId() == this.player.getUniqueId();

		if (!isUser || this.isFromOtherOrigin) {
			this.pushFactor = this.pushFactorForOthers;
			final Vector velocity = entity.getVelocity();
			final double max = this.speed / this.speedFactor;
			double factor = this.pushFactor;

			final Vector push = this.direction.clone();
			if (Math.abs(push.getY()) > max && !isUser) {
				if (push.getY() < 0) {
					push.setY(-max);
				} else {
					push.setY(max);
				}
			}
			if (this.location.getWorld().equals(this.origin.getWorld())) {
				factor *= 1 - this.location.distance(this.origin) / (2 * this.range);
			}

			if (isUser && GeneralMethods.isSolid(this.player.getLocation().add(0, -.5, 0).getBlock())) {
				factor *= .5;
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
					return;
				}
			}

			if (Double.isNaN(velocity.length())) {
				return;
			}

			GeneralMethods.setVelocity(entity, velocity);
			if (this.source != null) {
				new HorizontalVelocityTracker(entity, this.player, 200l, this.source);
			} else {
				new HorizontalVelocityTracker(entity, this.player, 200l, this);
			}

			if (!isUser && entity instanceof Player) {
				flightHandler.createInstance((Player) entity, this.player, 1000L, this.getName());
			}
			if (entity.getFireTicks() > 0) {
				entity.getWorld().playEffect(entity.getLocation(), Effect.EXTINGUISH, 0);
			}

			entity.setFireTicks(0);
			breakBreathbendingHold(entity);

			if (this.source != null && (this.damage > 0 && entity instanceof LivingEntity && !entity.equals(this.player) && !this.affectedEntities.contains(entity))) {
				DamageHandler.damageEntity(entity, this.damage, this.source);
				this.affectedEntities.add(entity);
			} else if (this.source == null && (this.damage > 0 && entity instanceof LivingEntity && !entity.equals(this.player) && !this.affectedEntities.contains(entity))) {
				DamageHandler.damageEntity(entity, this.damage, this);
				this.affectedEntities.add(entity);
			}
		}
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

		Block block = this.location.getBlock();
		
		for (final Block testblock : GeneralMethods.getBlocksAroundPoint(this.location, this.radius)) {
			if (testblock.getType() == Material.FIRE) {
				testblock.setType(Material.AIR);
				testblock.getWorld().playEffect(testblock.getLocation(), Effect.EXTINGUISH, 0);
				continue;
			} else if (GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
				continue;
			} else if (affectedLevers.contains(testblock)) {
				continue;
			}
			
			if (Arrays.asList(DOORS).contains(testblock.getType())) {
				if (testblock.getBlockData() instanceof Door) {
					Door door = (Door) testblock.getBlockData();
					BlockFace face = door.getFacing();
					Vector toPlayer = GeneralMethods.getDirection(block.getLocation(), this.player.getLocation().getBlock().getLocation());
					double[] dims = { toPlayer.getX(), toPlayer.getY(), toPlayer.getZ() };

					for (int i = 0; i < 3; i++) {
						if (i == 1) {
							continue;
						}
						
						BlockFace bf = GeneralMethods.getBlockFaceFromValue(i, dims[i]);

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
					testblock.setBlockData(door);
					testblock.getWorld().playSound(testblock.getLocation(), Sound.valueOf("BLOCK_WOODEN_DOOR_" + (door.isOpen() ? "OPEN" : "CLOSE")), 0.5f, 0);
					affectedLevers.add(testblock);
				}
			} else if (Arrays.asList(TDOORS).contains(testblock.getType())) {
				if (testblock.getBlockData() instanceof TrapDoor) {
					TrapDoor tDoor = (TrapDoor) testblock.getBlockData();
					
					if (this.origin.getY() < block.getY()) {
						if (!tDoor.isOpen()) {
							return;
						}
					} else {
						if (tDoor.isOpen()) {
							return;
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
						affectedLevers.add(testblock);
						
						new BukkitRunnable() {

							@Override
							public void run() {
								button.setPowered(false);
								testblock.setBlockData(button);
								affectedLevers.remove(testblock);
								testblock.getWorld().playSound(testblock.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_OFF, 0.5f, 0);
							}
							
						}.runTaskLater(ProjectKorra.plugin, 15);
					}
					
					testblock.getWorld().playSound(testblock.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 0.5f, 0);
				}
			} else if (testblock.getType() == Material.LEVER) {
				if (testblock.getBlockData() instanceof Switch) {
					Switch lever = (Switch) testblock.getBlockData();
					lever.setPowered(!lever.isPowered());
					testblock.setBlockData(lever);
					affectedLevers.add(testblock);
					testblock.getWorld().playSound(testblock.getLocation(), Sound.BLOCK_LEVER_CLICK, 0.5f, 0);
				}
			}
		}
		
		if ((GeneralMethods.isSolid(block) || block.isLiquid()) && !this.affectedLevers.contains(block) && this.canCoolLava) {
			if (block.getType() == Material.LAVA) {
				if (LavaFlow.isLavaFlowBlock(block)) {
					LavaFlow.removeBlock(block); // TODO: Make more generic for future lava generating moves.
				} else if (block.getBlockData() instanceof Levelled && ((Levelled) block.getBlockData()).getLevel() == 0) {
					new TempBlock(block, Material.OBSIDIAN);
				} else {
					new TempBlock(block, Material.COBBLESTONE);
				}
			}
			this.remove();
			return;
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
			if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation()) || ((entity instanceof Player) && Commands.invincible.contains(((Player) entity).getName()))){
				continue;
			}
			this.affect(entity);
		}

		this.advanceLocation();
		return;
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

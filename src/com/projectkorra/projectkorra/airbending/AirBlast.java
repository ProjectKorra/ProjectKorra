package com.projectkorra.projectkorra.airbending;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.material.Button;
import org.bukkit.material.Lever;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.Flight;

public class AirBlast extends AirAbility {

	private static final int MAX_TICKS = 10000;
	private static final Map<Player, Location> ORIGINS = new ConcurrentHashMap<>();

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
	@Attribute(Attribute.POWER)
	private double pushFactor;
	@Attribute(Attribute.POWER)
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

	public AirBlast(Player player) {
		super(player);
		if (bPlayer.isOnCooldown(this)) {
			return;
		} else if (player.getEyeLocation().getBlock().isLiquid()) {
			return;
		}

		setFields();

		if (ORIGINS.containsKey(player)) {
			Entity entity = GeneralMethods.getTargetedEntity(player, range);
			this.isFromOtherOrigin = true;
			this.origin = ORIGINS.get(player);
			ORIGINS.remove(player);

			if (entity != null) {
				this.direction = GeneralMethods.getDirection(origin, entity.getLocation()).normalize();
			} else {
				this.direction = GeneralMethods.getDirection(origin, GeneralMethods.getTargetedLocation(player, range)).normalize();
			}
		} else {
			origin = player.getEyeLocation();
			direction = player.getEyeLocation().getDirection().normalize();
		}

		this.location = origin.clone();
		bPlayer.addCooldown(this);
		start();
	}

	public AirBlast(Player player, Location location, Vector direction, double modifiedPushFactor, AirBurst burst) {
		super(player);
		if (location.getBlock().isLiquid()) {
			return;
		}
		this.source = burst;
		this.origin = location.clone();
		this.direction = direction.clone();
		this.location = location.clone();

		setFields();
		
		this.affectedLevers = new ArrayList<>();
		this.affectedEntities = new ArrayList<>();
		//prevent the airburst related airblasts from triggering doors/levers/buttons
		this.canOpenDoors = false;
		this.canPressButtons = false;
		this.canFlickLevers = false;

		if (bPlayer.isAvatarState()) {
			this.pushFactor = getConfig().getDouble("Abilities.Avatar.AvatarState.Air.AirBlast.Push.Self");
			this.pushFactorForOthers = getConfig().getDouble("Abilities.Avatar.AvatarState.Air.AirBlast.Push.Entities");
		}
		
		this.pushFactor *= modifiedPushFactor;

		start();
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
		for (Player player : ORIGINS.keySet()) {
			playOriginEffect(player);
		}
	}

	public static void setOrigin(Player player) {
		Location location = GeneralMethods.getTargetedLocation(player, getSelectRange(), GeneralMethods.NON_OPAQUE);
		if (location.getBlock().isLiquid() || GeneralMethods.isSolid(location.getBlock())) {
			return;
		} else if (GeneralMethods.isRegionProtectedFromBuild(player, "AirBlast", location)) {
			return;
		}

		ORIGINS.put(player, location);

	}

	private void advanceLocation() {
		if (showParticles) {
			playAirbendingParticles(location, particles, 0.275F, 0.275F, 0.275F);
		}
		if (random.nextInt(4) == 0) {
			playAirbendingSound(location);
		}
		location = location.add(direction.clone().multiply(speedFactor));
	}

	private void affect(Entity entity) {
		boolean isUser = entity.getUniqueId() == player.getUniqueId();

		if (!isUser || isFromOtherOrigin) {
			pushFactor = pushFactorForOthers;
			Vector velocity = entity.getVelocity();
			double max = speed / speedFactor;
			double factor = pushFactor;

			Vector push = direction.clone();
			if (Math.abs(push.getY()) > max && !isUser) {
				if (push.getY() < 0) {
					push.setY(-max);
				} else {
					push.setY(max);
				}
			}
			if (location.getWorld().equals(origin.getWorld())) {
				factor *= 1 - location.distance(origin) / (2 * range);
			}

			if (isUser && GeneralMethods.isSolid(player.getLocation().add(0, -.5, 0).getBlock())) {
				factor *= .5;
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
					return;
				}
			}

			if (Double.isNaN(velocity.length())) {
				return;
			}

			GeneralMethods.setVelocity(entity, velocity);
			if (source != null) {
				new HorizontalVelocityTracker(entity, player, 200l, CoreAbility.getAbility("AirBurst"));
			} else {
				new HorizontalVelocityTracker(entity, player, 200l, this);
			}

			if (!isUser && entity instanceof Player) {
				new Flight((Player) entity, player);
			}
			if (entity.getFireTicks() > 0) {
				entity.getWorld().playEffect(entity.getLocation(), Effect.EXTINGUISH, 0);
			}

			entity.setFireTicks(0);
			breakBreathbendingHold(entity);

			if (source != null && (this.damage > 0 && entity instanceof LivingEntity && !entity.equals(player) && !affectedEntities.contains(entity))) {
				DamageHandler.damageEntity((LivingEntity) entity, damage, CoreAbility.getAbility("AirBurst"));
				affectedEntities.add(entity);
			} else if (source == null && (damage > 0 && entity instanceof LivingEntity && !entity.equals(player) && !affectedEntities.contains(entity))) {
				DamageHandler.damageEntity((LivingEntity) entity, damage, this);
				affectedEntities.add(entity);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		} else if (GeneralMethods.isRegionProtectedFromBuild(this, location)) {
			remove();
			return;
		}

		speedFactor = speed * (ProjectKorra.time_step / 1000.0);
		ticks++;

		if (ticks > MAX_TICKS) {
			remove();
			return;
		}

		Block block = location.getBlock();
		for (Block testblock : GeneralMethods.getBlocksAroundPoint(location, radius)) {
			if (testblock.getType() == Material.FIRE) {
				testblock.setType(Material.AIR);
				testblock.getWorld().playEffect(testblock.getLocation(), Effect.EXTINGUISH, 0);
			}
			if (GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
				continue;
			}

			Material doorTypes[] = { Material.WOODEN_DOOR, Material.SPRUCE_DOOR, Material.BIRCH_DOOR, Material.JUNGLE_DOOR, Material.ACACIA_DOOR, Material.DARK_OAK_DOOR };
			if (Arrays.asList(doorTypes).contains(block.getType()) && canOpenDoors) {
				if (block.getData() >= 8) {
					block = block.getRelative(BlockFace.DOWN);
				}

				if (block.getData() < 4) {
					block.setData((byte) (block.getData() + 4));
					block.getWorld().playSound(block.getLocation(), Sound.BLOCK_WOODEN_DOOR_CLOSE, 10, 1);
				} else {
					block.setData((byte) (block.getData() - 4));
					block.getWorld().playSound(block.getLocation(), Sound.BLOCK_WOODEN_DOOR_OPEN, 10, 1);
				}
			}
			if ((block.getType() == Material.LEVER) && !affectedLevers.contains(block) && canFlickLevers) {
				Lever lever = new Lever(Material.LEVER, block.getData());
				lever.setPowered(!lever.isPowered());
				block.setData(lever.getData());

				Block supportBlock = block.getRelative(lever.getAttachedFace());
				if (supportBlock != null && supportBlock.getType() != Material.AIR) {
					BlockState initialSupportState = supportBlock.getState();
					BlockState supportState = supportBlock.getState();
					supportState.setType(Material.AIR);
					supportState.update(true, false);
					initialSupportState.update(true);
				}
				affectedLevers.add(block);
			} else if ((block.getType() == Material.STONE_BUTTON) && !affectedLevers.contains(block) && canPressButtons) {
				final Button button = new Button(Material.STONE_BUTTON, block.getData());
				button.setPowered(!button.isPowered());
				block.setData(button.getData());

				Block supportBlock = block.getRelative(button.getAttachedFace());
				if (supportBlock != null && supportBlock.getType() != Material.AIR) {
					BlockState initialSupportState = supportBlock.getState();
					BlockState supportState = supportBlock.getState();
					supportState.setType(Material.AIR);
					supportState.update(true, false);
					initialSupportState.update(true);
				}

				final Block btBlock = block;
				new BukkitRunnable() {
					public void run() {
						button.setPowered(!button.isPowered());
						btBlock.setData(button.getData());

						Block supportBlock = btBlock.getRelative(button.getAttachedFace());
						if (supportBlock != null && supportBlock.getType() != Material.AIR) {
							BlockState initialSupportState = supportBlock.getState();
							BlockState supportState = supportBlock.getState();
							supportState.setType(Material.AIR);
							supportState.update(true, false);
							initialSupportState.update(true);
						}
					}
				}.runTaskLater(ProjectKorra.plugin, 10);

				affectedLevers.add(block);
			} else if ((block.getType() == Material.WOOD_BUTTON) && !affectedLevers.contains(block) && canPressButtons) {
				final Button button = new Button(Material.WOOD_BUTTON, block.getData());
				button.setPowered(!button.isPowered());
				block.setData(button.getData());

				Block supportBlock = block.getRelative(button.getAttachedFace());
				if (supportBlock != null && supportBlock.getType() != Material.AIR) {
					BlockState initialSupportState = supportBlock.getState();
					BlockState supportState = supportBlock.getState();
					supportState.setType(Material.AIR);
					supportState.update(true, false);
					initialSupportState.update(true);
				}

				final Block btBlock = block;

				new BukkitRunnable() {
					public void run() {
						button.setPowered(!button.isPowered());
						btBlock.setData(button.getData());

						Block supportBlock = btBlock.getRelative(button.getAttachedFace());
						if (supportBlock != null && supportBlock.getType() != Material.AIR) {
							BlockState initialSupportState = supportBlock.getState();
							BlockState supportState = supportBlock.getState();
							supportState.setType(Material.AIR);
							supportState.update(true, false);
							initialSupportState.update(true);
						}
					}
				}.runTaskLater(ProjectKorra.plugin, 15);

				affectedLevers.add(block);
			}
		}
		if ((GeneralMethods.isSolid(block) || block.isLiquid()) && !affectedLevers.contains(block) && canCoolLava) {
			if (block.getType() == Material.LAVA || block.getType() == Material.STATIONARY_LAVA) {
				if (block.getData() == 0x0) {
					block.setType(Material.OBSIDIAN);
				} else {
					block.setType(Material.COBBLESTONE);
				}
			}
			remove();
			return;
		}

		/*
		 * If a player presses shift and AirBlasts straight down then the
		 * AirBlast's location gets messed up and reading the distance returns
		 * Double.NaN. If we don't remove this instance then the AirBlast will
		 * never be removed.
		 */
		double dist = 0;
		if (location.getWorld().equals(origin.getWorld())) {
			dist = location.distance(origin);
		}
		if (Double.isNaN(dist) || dist > range) {
			remove();
			return;
		}

		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, radius)) {
			affect(entity);
		}

		advanceLocation();
		return;
	}

	/**
	 * This method was used for the old collision detection system. Please see
	 * {@link Collision} for the new system.
	 */
	@Deprecated
	public static boolean removeAirBlastsAroundPoint(Location location, double radius) {
		boolean removed = false;
		for (AirBlast airBlast : getAbilities(AirBlast.class)) {
			Location airBlastlocation = airBlast.location;
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

	public int getTicks() {
		return ticks;
	}

	public void setTicks(int ticks) {
		this.ticks = ticks;
	}

	public double getSpeedFactor() {
		return speedFactor;
	}

	public void setSpeedFactor(double speedFactor) {
		this.speedFactor = speedFactor;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getPushFactor() {
		return pushFactor;
	}

	public void setPushFactor(double pushFactor) {
		this.pushFactor = pushFactor;
	}

	public double getPushFactorForOthers() {
		return pushFactorForOthers;
	}

	public void setPushFactorForOthers(double pushFactorForOthers) {
		this.pushFactorForOthers = pushFactorForOthers;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public boolean isCanFlickLevers() {
		return canFlickLevers;
	}

	public void setCanFlickLevers(boolean canFlickLevers) {
		this.canFlickLevers = canFlickLevers;
	}

	public boolean isCanOpenDoors() {
		return canOpenDoors;
	}

	public void setCanOpenDoors(boolean canOpenDoors) {
		this.canOpenDoors = canOpenDoors;
	}

	public boolean isCanPressButtons() {
		return canPressButtons;
	}

	public void setCanPressButtons(boolean canPressButtons) {
		this.canPressButtons = canPressButtons;
	}

	public boolean isCanCoolLava() {
		return canCoolLava;
	}

	public void setCanCoolLava(boolean canCoolLava) {
		this.canCoolLava = canCoolLava;
	}

	public boolean isFromOtherOrigin() {
		return isFromOtherOrigin;
	}

	public void setFromOtherOrigin(boolean isFromOtherOrigin) {
		this.isFromOtherOrigin = isFromOtherOrigin;
	}

	public boolean isShowParticles() {
		return showParticles;
	}

	public void setShowParticles(boolean showParticles) {
		this.showParticles = showParticles;
	}

	public AirBurst getSource() {
		return source;
	}

	public void setSource(AirBurst source) {
		this.source = source;
	}

	public ArrayList<Block> getAffectedLevers() {
		return affectedLevers;
	}

	public ArrayList<Entity> getAffectedEntities() {
		return affectedEntities;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public int getParticles() {
		return particles;
	}

	public void setParticles(int particles) {
		this.particles = particles;
	}

	public static int getSelectParticles() {
		return getConfig().getInt("Abilities.Air.AirBlast.SelectParticles");
	}

	public static double getSelectRange() {
		return getConfig().getInt("Abilities.Air.AirBlast.SelectRange");
	}

}

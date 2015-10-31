package com.projectkorra.projectkorra.airbending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AvatarState;
import com.projectkorra.projectkorra.ability.StockAbility;
import com.projectkorra.projectkorra.ability.api.CoreAbility;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.util.Flight;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class AirBlast extends CoreAbility {

	private static ConcurrentHashMap<Player, Location> origins = new ConcurrentHashMap<Player, Location>();

	public static double speed = config.get().getDouble("Abilities.Air.AirBlast.Speed");
	public static double defaultrange = config.get().getDouble("Abilities.Air.AirBlast.Range");
	public static double affectingradius = config.get().getDouble("Abilities.Air.AirBlast.Radius");
	public static double defaultpushfactor = config.get().getDouble("Abilities.Air.AirBlast.Push.Entities");
	public static double otherpushfactor = config.get().getDouble("Abilities.Air.AirBlast.Push.Self");
	
	public static boolean flickLevers = config.get().getBoolean("Abilities.Air.AirBlast.CanFlickLevers");
	public static boolean openDoors = config.get().getBoolean("Abilities.Air.AirBlast.CanOpenDoors");
	public static boolean pressButtons = config.get().getBoolean("Abilities.Air.AirBlast.CanPressButtons");
	public static boolean coolLava = config.get().getBoolean("Abilities.Air.AirBlast.CanCoolLava");
	
	private static double originselectrange = 10;
	private static final int maxticks = 10000;
	/* Package visible variables */
	static double maxspeed = 1. / defaultpushfactor;
	/* End Package visible variables */

	// public static long interval = 2000;
	public static byte full = 0x0;

	Location location;
	private Location origin;
	private Vector direction;
	private Player player;
	private double speedfactor;
	private double range = defaultrange;
	private double pushfactor = defaultpushfactor;
	private double damage = 0;

	private boolean otherorigin = false;
	private boolean showParticles = true;
	private int ticks = 0;

	private ArrayList<Block> affectedlevers = new ArrayList<Block>();
	private ArrayList<Entity> affectedentities = new ArrayList<Entity>();

	@SuppressWarnings("unused")
	private AirBurst source = null;

	public AirBlast(Location location, Vector direction, Player player, double factorpush, AirBurst burst) {
		if (location.getBlock().isLiquid()) {
			return;
		}
		//reloadVariables();
		source = burst;

		this.player = player;
		origin = location.clone();
		this.direction = direction.clone();
		this.location = location.clone();
		pushfactor *= factorpush;
		//instances.put(uuid, this);
		putInstance(player, this);
	}

	public AirBlast(Player player) {
		/* Initial Checks */
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
		if (bPlayer.isOnCooldown("AirBlast"))
			return;
		if (player.getEyeLocation().getBlock().isLiquid()) {
			return;
		}
		/* End Initial Checks */
		this.player = player;
		if (origins.containsKey(player)) {
			otherorigin = true;
			origin = origins.get(player);
			origins.remove(player);
			Entity entity = GeneralMethods.getTargetedEntity(player, range, new ArrayList<Entity>());
			if (entity != null) {
				direction = GeneralMethods.getDirection(origin, entity.getLocation()).normalize();
			} else {
				direction = GeneralMethods.getDirection(origin, GeneralMethods.getTargetedLocation(player, range)).normalize();
			}
		} else {
			origin = player.getEyeLocation();
			direction = player.getEyeLocation().getDirection().normalize();
		}
		location = origin.clone();
		putInstance(player, this);
		//instances.put(uuid, this);
		bPlayer.addCooldown("AirBlast", GeneralMethods.getGlobalCooldown());

		// time = System.currentTimeMillis();
		// timers.put(player, System.currentTimeMillis());
	}

	private static void playOriginEffect(Player player) {
		if (!origins.containsKey(player))
			return;
		Location origin = origins.get(player);
		if (!origin.getWorld().equals(player.getWorld())) {
			origins.remove(player);
			return;
		}

		if (GeneralMethods.getBoundAbility(player) == null) {
			origins.remove(player);
			return;
		}

		if (!GeneralMethods.getBoundAbility(player).equalsIgnoreCase("AirBlast") || !GeneralMethods.canBend(player.getName(), "AirBlast")) {
			origins.remove(player);
			return;
		}

		if (origin.distance(player.getEyeLocation()) > originselectrange) {
			origins.remove(player);
			return;
		}

		AirMethods.playAirbendingParticles(origin, 6);
		//		origin.getWorld().playEffect(origin, Effect.SMOKE, 4,
		//				(int) originselectrange);
	}

	public static void progressAll() {
		CoreAbility.progressAll(StockAbility.AirBlast);
		for (Player player : origins.keySet()) {
			playOriginEffect(player);
		}
	}

	public static void setOrigin(Player player) {
		Location location = GeneralMethods.getTargetedLocation(player, originselectrange, GeneralMethods.nonOpaque);
		if (location.getBlock().isLiquid() || GeneralMethods.isSolid(location.getBlock()))
			return;

		if (GeneralMethods.isRegionProtectedFromBuild(player, "AirBlast", location))
			return;

		if (origins.containsKey(player)) {
			origins.replace(player, location);
		} else {
			origins.put(player, location);
		}
	}

	private void advanceLocation() {
		if (showParticles)
			AirMethods.playAirbendingParticles(location, 6);
		if (GeneralMethods.rand.nextInt(4) == 0) {
			AirMethods.playAirbendingSound(location);
		}
		location = location.add(direction.clone().multiply(speedfactor));
	}

	private void affect(Entity entity) {
		boolean isUser = entity.getUniqueId() == player.getUniqueId();

		if (!isUser || otherorigin) {
			pushfactor = otherpushfactor;
			Vector velocity = entity.getVelocity();
			// double mag = Math.abs(velocity.getY());
			double max = maxspeed;
			double factor = pushfactor;
			if (AvatarState.isAvatarState(player)) {
				max = AvatarState.getValue(maxspeed);
				factor = AvatarState.getValue(factor);
			}

			Vector push = direction.clone();
			if (Math.abs(push.getY()) > max && !isUser) {
				if (push.getY() < 0)
					push.setY(-max);
				else
					push.setY(max);
			}

			factor *= 1 - location.distance(origin) / (2 * range);

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
				if (Commands.invincible.contains(((Player) entity).getName()))
					return;
			}

			if (Double.isNaN(velocity.length()))
				return;

			GeneralMethods.setVelocity(entity, velocity);
			new HorizontalVelocityTracker(entity, player, 200l);
			entity.setFallDistance(0);
			if (!isUser && entity instanceof Player) {
				new Flight((Player) entity, player);
			}
			if (entity.getFireTicks() > 0)
				entity.getWorld().playEffect(entity.getLocation(), Effect.EXTINGUISH, 0);
			entity.setFireTicks(0);
			AirMethods.breakBreathbendingHold(entity);

			if (damage > 0 && entity instanceof LivingEntity && !entity.equals(player) && !affectedentities.contains(entity)) {
				GeneralMethods.damageEntity(player, entity, damage, "AirBlast");
				affectedentities.add(entity);
			}
		}
	}

	public Player getPlayer() {
		return player;
	}

	public double getPushfactor() {
		return pushfactor;
	}

	public double getRange() {
		return range;
	}

	public boolean getShowParticles() {
		return this.showParticles;
	}

	@Override
	public StockAbility getStockAbility() {
		return StockAbility.AirBlast;
	}

	@SuppressWarnings("deprecation")
	public boolean progress() {
		//ProjectKorra.log.info("FireBlast id: " + getID());
		if (player.isDead() || !player.isOnline()) {
			remove();
			return false;
		}

		if (GeneralMethods.isRegionProtectedFromBuild(player, "AirBlast", location)) {
			remove();
			return false;
		}

		speedfactor = speed * (ProjectKorra.time_step / 1000.);

		ticks++;

		if (ticks > maxticks) {
			remove();
			return false;
		}
		Block block = location.getBlock();
		for (Block testblock : GeneralMethods.getBlocksAroundPoint(location, affectingradius)) {
			if (testblock.getType() == Material.FIRE) {
				testblock.setType(Material.AIR);
				testblock.getWorld().playEffect(testblock.getLocation(), Effect.EXTINGUISH, 0);
			}

			if (GeneralMethods.isRegionProtectedFromBuild(getPlayer(), "AirBlast", block.getLocation())) continue;
			
			Material doorTypes[] = { Material.WOODEN_DOOR, Material.SPRUCE_DOOR, Material.BIRCH_DOOR, Material.JUNGLE_DOOR, Material.ACACIA_DOOR, Material.DARK_OAK_DOOR };
			if (Arrays.asList(doorTypes).contains(block.getType()) && openDoors) {
				if (block.getData() >= 8) {
					block = block.getRelative(BlockFace.DOWN);
				}

				if (block.getData() < 4) {
					block.setData((byte) (block.getData() + 4));
					block.getWorld().playSound(block.getLocation(), Sound.DOOR_CLOSE, 10, 1);
				} else {
					block.setData((byte) (block.getData() - 4));
					block.getWorld().playSound(block.getLocation(), Sound.DOOR_OPEN, 10, 1);
				}
			}
			if ((block.getType() == Material.LEVER) && !affectedlevers.contains(block) && flickLevers) {
				// BlockState state = block.getState();
				// Lever lever = (Lever) (state.getData());
				// lever.setPowered(!lever.isPowered());
				// state.setData(lever);
				// state.update(true, true);
				//
				// Block relative = block.getRelative(((Attachable) block
				// .getState().getData()).getFacing(), -1);
				// relative.getState().update(true, true);
				//
				// for (Block block2 : Methods.getBlocksAroundPoint(
				// relative.getLocation(), 2))
				// block2.getState().update(true, true);

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

				affectedlevers.add(block);

			} else if ((block.getType() == Material.STONE_BUTTON) && !affectedlevers.contains(block) && pressButtons) {

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

				affectedlevers.add(block);
			} else if ((block.getType() == Material.WOOD_BUTTON) && !affectedlevers.contains(block) && pressButtons) {

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

				affectedlevers.add(block);
			}
		}
		if ((GeneralMethods.isSolid(block) || block.isLiquid()) && !affectedlevers.contains(block) && coolLava) {
			if (block.getType() == Material.LAVA || block.getType() == Material.STATIONARY_LAVA) {
				if (block.getData() == full) {
					block.setType(Material.OBSIDIAN);
				} else {
					block.setType(Material.COBBLESTONE);
				}
			}
			remove();
			return false;
		}

		/*
		 * If a player presses shift and AirBlasts straight down then the
		 * AirBlast's location gets messed up and reading the distance returns
		 * Double.NaN. If we don't remove this instance then the AirBlast will
		 * never be removed.
		 */
		double dist = location.distance(origin);
		if (Double.isNaN(dist) || dist > range) {
			remove();
			return false;
		}

		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, affectingradius)) {
			affect(entity);
		}

		advanceLocation();
		return true;
	}
	
	public static boolean removeAirBlastsAroundPoint(Location location, double radius) {
		boolean removed = false;
		for (Integer id : getInstances(StockAbility.AirBlast).keySet()) {
			AirBlast airBlast = ((AirBlast)getAbility(id));
		Location airBlastlocation = airBlast.location;
		if (location.getWorld() == airBlastlocation.getWorld()) {
			if (location.distance(airBlastlocation) <= radius)
				airBlast.remove();
			removed = true;
			}
		}
		return removed;
	}

	@Override
	public void reloadVariables() {
		speed = config.get().getDouble("Abilities.Air.AirBlast.Speed");
		defaultrange = config.get().getDouble("Abilities.Air.AirBlast.Range");
		affectingradius = config.get().getDouble("Abilities.Air.AirBlast.Radius");
		defaultpushfactor = config.get().getDouble("Abilities.Air.AirBlast.Push");
		
		flickLevers = config.get().getBoolean("Abilities.Air.AirBlast.CanFlickLevers");
		openDoors = config.get().getBoolean("Abilities.Air.AirBlast.CanOpenDoors");
		pressButtons = config.get().getBoolean("Abilities.Air.AirBlast.CanPressButtons");
		coolLava = config.get().getBoolean("Abilities.Air.AirBlast.CanCoolLava");
		maxspeed = 1. / defaultpushfactor;
		range = defaultrange;
		pushfactor = defaultpushfactor;
	}

	public void setDamage(double dmg) {
		this.damage = dmg;
	}

	public void setPushfactor(double pushfactor) {
		this.pushfactor = pushfactor;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public void setShowParticles(boolean show) {
		this.showParticles = show;
	}

	@Override
	public InstanceType getInstanceType() {
		return InstanceType.MULTIPLE;
	}

}

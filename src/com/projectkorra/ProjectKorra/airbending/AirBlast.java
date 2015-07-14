package com.projectkorra.ProjectKorra.airbending;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
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

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.Commands;
import com.projectkorra.ProjectKorra.Flight;
import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.BaseAbility;
import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.Ability.StockAbilities;
import com.projectkorra.ProjectKorra.Objects.HorizontalVelocityTracker;

public class AirBlast extends BaseAbility {

	private static ConcurrentHashMap<Player, Location> origins = new ConcurrentHashMap<Player, Location>();

	static final int maxticks = 10000;

	public static double speed = config.getDouble("Abilities.Air.AirBlast.Speed");
	public static double defaultrange = config.getDouble("Abilities.Air.AirBlast.Range");
	public static double affectingradius = config.getDouble("Abilities.Air.AirBlast.Radius");
	public static double defaultpushfactor = config.getDouble("Abilities.Air.AirBlast.Push");
	private static double originselectrange = 10;
	static double maxspeed = 1. / defaultpushfactor;
	// public static long interval = 2000;
	public static byte full = 0x0;

	public Location location;
	private Location origin;
	private Vector direction;
	private Player player;
	private UUID uuid;
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
		reloadVariables();
		source = burst;

		this.player = player;
		origin = location.clone();
		this.direction = direction.clone();
		this.location = location.clone();
		pushfactor *= factorpush;
		//instances.put(uuid, this);
		putInstance(StockAbilities.AirBlast, uuid, this);
	}
	
	public AirBlast(Player player) {
		/* Initial Checks */
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
		if (bPlayer.isOnCooldown("AirBlast")) return;
		if (player.getEyeLocation().getBlock().isLiquid()) {
			return;
		}
		/* End Initial Checks */
		reloadVariables();
		this.player = player;
		this.uuid = player.getUniqueId();
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
		putInstance(StockAbilities.AirBlast, uuid, this);
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

		AirMethods.playAirbendingParticles(origin, 10);
		//		origin.getWorld().playEffect(origin, Effect.SMOKE, 4,
		//				(int) originselectrange);
	}
	
	public static void progressAll() {
		BaseAbility.progressAll(StockAbilities.AirBlast);
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
			AirMethods.playAirbendingParticles(location, 10);
		if (GeneralMethods.rand.nextInt(4) == 0) {
			AirMethods.playAirbendingSound(location);
		}
		location = location.add(direction.clone().multiply(speedfactor));
	}

	private void affect(Entity entity) {
		boolean isUser = entity.getEntityId() == player.getEntityId();

		if (!isUser || otherorigin) {
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
				if (Commands.invincible.contains(((Player) entity).getName())) return;
			}

			if(Double.isNaN(velocity.length()))
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
				GeneralMethods.damageEntity(player, entity, damage);
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

	@SuppressWarnings("deprecation")
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}

		if (GeneralMethods.isRegionProtectedFromBuild(player, "AirBlast", location)) {
			remove();
			return;
		}

		speedfactor = speed * (ProjectKorra.time_step / 1000.);

		ticks++;

		if (ticks > maxticks) {
			remove();
			return;
		}
		Block block = location.getBlock();
		for (Block testblock : GeneralMethods.getBlocksAroundPoint(location, affectingradius)) {
			if (testblock.getType() == Material.FIRE) {
				testblock.setType(Material.AIR);
				testblock.getWorld().playEffect(testblock.getLocation(), Effect.EXTINGUISH, 0);
			}

			Material doorTypes[] = {Material.WOODEN_DOOR, Material.SPRUCE_DOOR, 
					Material.BIRCH_DOOR, Material.JUNGLE_DOOR, Material.ACACIA_DOOR, Material.DARK_OAK_DOOR};
			if (Arrays.asList(doorTypes).contains(block.getType())) {
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
			if ((block.getType() == Material.LEVER)
					&& !affectedlevers.contains(block)) {
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

			} else if ((block.getType() == Material.STONE_BUTTON) 
					&& !affectedlevers.contains(block)) {

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
			} else if ((block.getType() == Material.WOOD_BUTTON) 
					&& !affectedlevers.contains(block)) {

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
		if ((GeneralMethods.isSolid(block) || block.isLiquid()) && !affectedlevers.contains(block)) {
			if (block.getType() == Material.LAVA || block.getType() == Material.STATIONARY_LAVA) {
				if (block.getData() == full) {
					block.setType(Material.OBSIDIAN);
				} else {
					block.setType(Material.COBBLESTONE);
				}
			}
			remove();
			return;
		}

		/*
		 *	If a player presses shift and AirBlasts straight down then
		 *	the AirBlast's location gets messed up and reading the distance
		 *	returns Double.NaN. If we don't remove this instance then
		 *	the AirBlast will never be removed. 
		 */
		double dist = location.distance(origin);
		if (Double.isNaN(dist) || dist > range) {
			remove();
			return;
		}

		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, affectingradius)) {
			affect(entity);
		}

		advanceLocation();
	}

	@Override
	public void reloadVariables() {
		speed = config.getDouble("Abilities.Air.AirBlast.Speed");
		defaultrange = config.getDouble("Abilities.Air.AirBlast.Range");
		affectingradius = config.getDouble("Abilities.Air.AirBlast.Radius");
		defaultpushfactor = config.getDouble("Abilities.Air.AirBlast.Push");
		maxspeed = 1. / defaultpushfactor;
		range = defaultrange;
		pushfactor = defaultpushfactor;
	}

	@Override
	public void remove() {
		removeInstance(StockAbilities.AirBlast, uuid);
		//instances.remove(uuid);
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
}
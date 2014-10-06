package com.projectkorra.ProjectKorra.waterbending;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.TempBlock;
import com.projectkorra.ProjectKorra.Ability.AvatarState;

public class WaterWave {
	public static enum AbilityType {
		CLICK, SHIFT, RELEASE
	}

	public static enum AnimateState {
		RISE, TOWARDPLAYER, CIRCLE, SHRINK
	}

	public static ArrayList<WaterWave> instances = new ArrayList<WaterWave>();

	public static boolean ICE_ONLY = false;
	public static boolean ENABLED = ProjectKorra.plugin.getConfig().getBoolean(
			"Abilities.Water.WaterSpout.Wave.Enabled");
	public static double RANGE = ProjectKorra.plugin.getConfig().getDouble(
			"Abilities.Water.WaterSpout.Wave.Range");
	public static double MAX_SPEED = ProjectKorra.plugin.getConfig().getDouble(
			"Abilities.Water.WaterSpout.Wave.Speed");
	public static long CHARGE_TIME = ProjectKorra.plugin.getConfig().getLong(
			"Abilities.Water.WaterSpout.Wave.ChargeTime");
	public static long FLIGHT_TIME = ProjectKorra.plugin.getConfig().getLong(
			"Abilities.Water.WaterSpout.Wave.FlightTime");

	private Player player;
	private long time;
	private AbilityType type;
	private Location origin, currentLoc;
	private Vector direction;
	private double radius = 3.8;
	private boolean charging = false;
	private AnimateState anim;
	private ConcurrentHashMap<Block, TempBlock> affectedBlocks = new ConcurrentHashMap<Block, TempBlock>();

	public WaterWave(Player player, AbilityType type) {
		if(!ENABLED)
			return;
		
		this.player = player;
		this.time = System.currentTimeMillis();
		this.type = type;
		instances.add(this);

		if (type == AbilityType.CLICK)
			this.progress();
	}

	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		if (type != AbilityType.RELEASE) {
			if (!Methods.canBend(player.getName(), "WaterSpout") 
					|| !player.hasPermission("bending.ability.WaterSpout.Wave")) {
				remove();
				return;
			}
			String ability = Methods.getBoundAbility(player);
			if (ability == null || !ability.equalsIgnoreCase("WaterSpout")) {
				remove();
				return;
			}
		}

		if (type == AbilityType.CLICK) {
			if (origin == null) {
				removeType(player, AbilityType.CLICK);
				instances.add(this);

				Block block = Methods.getWaterSourceBlock(player, RANGE,
						Methods.canPlantbend(player));
				if (block == null) {
					remove();
					return;
				}
				Block blockAbove = block.getRelative(BlockFace.UP);
				if (blockAbove.getType() != Material.AIR
						&& !Methods.isWaterbendable(blockAbove, player)) {
					remove();
					return;
				}
				origin = block.getLocation();

				if (!Methods.isWaterbendable(block, player)
						|| Methods.isRegionProtectedFromBuild(player,
								"WaterSpout", origin)) {
					remove();
					return;
				}
				if (ICE_ONLY
						&& !(block.getType() == Material.ICE
								|| block.getType() == Material.SNOW || block
								.getType() == Material.PACKED_ICE)) {
					remove();
					return;
				}
			}
			if (player.getLocation().distance(origin) > RANGE) {
				remove();
				return;
			} else if (player.isSneaking()) {
				new WaterWave(player, AbilityType.SHIFT);
				return;
			}
			Methods.playFocusWaterEffect(origin.getBlock());
		} else if (type == AbilityType.SHIFT) {
			if (direction == null) {
				direction = player.getEyeLocation().getDirection();
			}
			if (!charging) {
				if (!containsType(player, AbilityType.CLICK)) {
					removeType(player, AbilityType.CLICK);
					remove();
					return;
				}
				charging = true;
				anim = AnimateState.RISE;

				WaterWave clickSpear = getType(player, AbilityType.CLICK)
						.get(0);
				origin = clickSpear.origin.clone();
				currentLoc = origin.clone();
				if (Methods.isPlant(origin.getBlock()))
					new Plantbending(origin.getBlock());
				// else
				// Methods.addTempAirBlock(origin.getBlock());

			}

			removeType(player, AbilityType.CLICK);
			if (!player.isSneaking()) {
				if (System.currentTimeMillis() - time > CHARGE_TIME) {
					WaterWave wwave = new WaterWave(player, AbilityType.RELEASE);
					wwave.anim = AnimateState.SHRINK;
					wwave.direction = direction;
				}
				remove();
				return;
			}

			double animSpeed = 1.2;
			if (anim == AnimateState.RISE) {
				revertBlocks();
				currentLoc.add(0, animSpeed, 0);
				Block block = currentLoc.getBlock();
				if (!(Methods.isWaterbendable(block, player) || block.getType() == Material.AIR)
						|| Methods.isRegionProtectedFromBuild(player,
								"WaterSpout", block.getLocation())) {
					remove();
					return;
				}
				createBlock(block, Material.STATIONARY_WATER);
				if (currentLoc.distance(origin) > 2)
					anim = AnimateState.TOWARDPLAYER;
			} else if (anim == AnimateState.TOWARDPLAYER) {
				revertBlocks();
				Location eyeLoc = player.getTargetBlock(null, 2).getLocation();
				eyeLoc.setY(player.getEyeLocation().getY());
				Vector vec = Methods.getDirection(currentLoc, eyeLoc);
				currentLoc.add(vec.normalize().multiply(animSpeed));

				Block block = currentLoc.getBlock();
				if (!(Methods.isWaterbendable(block, player) || block.getType() == Material.AIR)
						|| Methods.isRegionProtectedFromBuild(player,
								"WaterSpout", block.getLocation())) {
					remove();
					return;
				}

				createBlock(block, Material.STATIONARY_WATER);
				if (currentLoc.distance(eyeLoc) < 1.3) {
					anim = AnimateState.CIRCLE;
					Vector tempDir = player.getLocation().getDirection();
					tempDir.setY(0);
					direction = tempDir.normalize();
					revertBlocks();
				}
			} else if (anim == AnimateState.CIRCLE) {
				drawCircle(120, 5);
			}
		} else if (type == AbilityType.RELEASE) {
			if (anim == AnimateState.SHRINK) {
				radius -= 0.20;
				drawCircle(360, 15);
				if (radius < 1) {
					revertBlocks();
					time = System.currentTimeMillis();
					anim = null;
				}
			} else {
				if ((System.currentTimeMillis() - time > FLIGHT_TIME && !AvatarState
						.isAvatarState(player)) || player.isSneaking()) {
					remove();
					return;
				}
				double currentSpeed = MAX_SPEED
						- (MAX_SPEED
								* (double) (System.currentTimeMillis() - time) / (double) FLIGHT_TIME);
				double nightSpeed = Methods.waterbendingNightAugment(
						currentSpeed * 0.9, player.getWorld());
				currentSpeed = nightSpeed > currentSpeed ? nightSpeed
						: currentSpeed;
				if (AvatarState.isAvatarState(player))
					currentSpeed = Methods.waterbendingNightAugment(MAX_SPEED,
							player.getWorld());

				player.setVelocity(player.getEyeLocation().getDirection()
						.normalize().multiply(currentSpeed));
				for (Block block : Methods.getBlocksAroundPoint(player
						.getLocation().add(0, -1, 0), 1.5))
					if (block.getType() == Material.AIR
							&& !Methods.isRegionProtectedFromBuild(player,
									"WaterSpout", block.getLocation()))
						createBlock(block, Material.STATIONARY_WATER, (byte) 0);
				revertBlocksDelay(20L);
			}
		}
	}

	public void drawCircle(double theta, double increment) {
		double rotateSpeed = 45;
		revertBlocks();
		direction = Methods.rotateXZ(direction, rotateSpeed);
		for (double i = 0; i < theta; i += increment) {
			Vector dir = Methods.rotateXZ(direction, i - theta / 2).normalize()
					.multiply(radius);
			dir.setY(0);
			Block block = player.getEyeLocation().add(dir).getBlock();
			currentLoc = block.getLocation();
			if (block.getType() == Material.AIR
					&& !Methods.isRegionProtectedFromBuild(player,
							"WaterSpout", block.getLocation()))
				createBlock(block, Material.STATIONARY_WATER, (byte) 8);
		}
	}

	public void remove() {
		instances.remove(this);
		revertBlocks();
	}

	public void createBlock(Block block, Material mat) {
		createBlock(block, mat, (byte) 0);
	}

	public void createBlock(Block block, Material mat, byte data) {
		affectedBlocks.put(block, new TempBlock(block, mat, data));
	}

	public void revertBlocks() {
		Enumeration<Block> keys = affectedBlocks.keys();
		while (keys.hasMoreElements()) {
			Block block = keys.nextElement();
			affectedBlocks.get(block).revertBlock();
			affectedBlocks.remove(block);
		}
	}

	public void revertBlocksDelay(long delay) {
		Enumeration<Block> keys = affectedBlocks.keys();
		while (keys.hasMoreElements()) {
			final Block block = keys.nextElement();
			final TempBlock tblock = affectedBlocks.get(block);
			affectedBlocks.remove(block);
			new BukkitRunnable() {
				public void run() {
					tblock.revertBlock();
				}
			}.runTaskLater(ProjectKorra.plugin, delay);
		}
	}

	public static void progressAll() {
		// Bukkit.broadcastMessage("Instances:" + instances.size());
		for (int i = 0; i < instances.size(); i++)
			instances.get(i).progress();
	}

	public static void removeAll() {
		for (int i = 0; i < instances.size(); i++) {
			instances.get(i).remove();
			i--;
		}
	}

	public static boolean containsType(Player player, AbilityType type) {
		for (int i = 0; i < instances.size(); i++) {
			WaterWave spear = instances.get(i);
			if (spear.player.equals(player) && spear.type.equals(type))
				return true;
		}
		return false;
	}

	public static void removeType(Player player, AbilityType type) {
		for (int i = 0; i < instances.size(); i++) {
			WaterWave spear = instances.get(i);
			if (spear.player.equals(player) && spear.type.equals(type)) {
				instances.remove(i);
				i--;
			}
		}
	}

	public static ArrayList<WaterWave> getType(Player player, AbilityType type) {
		ArrayList<WaterWave> list = new ArrayList<WaterWave>();
		for (WaterWave spear : instances) {
			if (spear.player.equals(player) && spear.type.equals(type))
				list.add(spear);
		}
		return list;
	}

	public static boolean wasBrokenFor(Player player, Block block) {
		if (containsType(player, AbilityType.CLICK)) {
			WaterWave wwave = getType(player, AbilityType.CLICK).get(0);
			if (wwave.origin == null)
				return false;
			if (wwave.origin.getBlock().equals(block))
				return true;
		}
		return false;
	}
}

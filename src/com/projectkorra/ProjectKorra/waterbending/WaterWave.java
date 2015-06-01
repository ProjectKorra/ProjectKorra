package com.projectkorra.ProjectKorra.waterbending;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.TempBlock;
import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.Utilities.BlockSource;
import com.projectkorra.ProjectKorra.Utilities.ClickType;

public class WaterWave {
	public static enum AbilityType {
		CLICK, SHIFT, RELEASE
	}

	public static enum AnimateState {
		RISE, TOWARDPLAYER, CIRCLE, SHRINK
	}

	public static ArrayList<WaterWave> instances = new ArrayList<WaterWave>();
	public static ConcurrentHashMap<Block, TempBlock> frozenBlocks = new ConcurrentHashMap<Block, TempBlock>();

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
	public static double WAVE_RADIUS = 1.5;
	public static double ICE_WAVE_DAMAGE = ProjectKorra.plugin.getConfig().getDouble(
			"Abilities.Water.WaterCombo.IceWave.Damage");

	private Player player;
	private long time;
	private AbilityType type;
	private Location origin, currentLoc;
	private Vector direction;
	private double radius = 3.8;
	private boolean charging = false;
	private boolean iceWave = false;
	private int progressCounter = 0;
	private AnimateState anim;
	private double range = RANGE;
	private double speed = MAX_SPEED;
	private double chargeTime = CHARGE_TIME;
	private double flightTime = FLIGHT_TIME;
	private double waveRadius = WAVE_RADIUS;
	private double damage = ICE_WAVE_DAMAGE;
	private ConcurrentHashMap<Block, TempBlock> affectedBlocks = new ConcurrentHashMap<Block, TempBlock>();
	private ArrayList<Entity> affectedEntities = new ArrayList<Entity>();
	private ArrayList<BukkitRunnable> tasks = new ArrayList<BukkitRunnable>();

	public WaterWave(Player player, AbilityType type) {
		if (!ENABLED)
			return;

		this.player = player;
		this.time = System.currentTimeMillis();
		this.type = type;
		instances.add(this);

		if (type == AbilityType.CLICK)
			this.progress();
	}

	public void progress() {
		progressCounter++;
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		if (type != AbilityType.RELEASE) {
			if (!GeneralMethods.canBend(player.getName(), "WaterSpout")
					|| !player.hasPermission("bending.ability.WaterSpout.Wave")) {
				remove();
				return;
			}
			String ability = GeneralMethods.getBoundAbility(player);
			if (ability == null || !ability.equalsIgnoreCase("WaterSpout")) {
				remove();
				return;
			}
		}

		if (type == AbilityType.CLICK) {
			if (origin == null) {
				removeType(player, AbilityType.CLICK);
				instances.add(this);

				Block block = BlockSource.getWaterSourceBlock(player, range, ClickType.LEFT_CLICK, 
						true, true, WaterMethods.canPlantbend(player));
				if (block == null) {
					remove();
					return;
				}
				Block blockAbove = block.getRelative(BlockFace.UP);
				if (blockAbove.getType() != Material.AIR
						&& !WaterMethods.isWaterbendable(blockAbove, player)) {
					remove();
					return;
				}
				origin = block.getLocation();

				if (!WaterMethods.isWaterbendable(block, player)
						|| GeneralMethods.isRegionProtectedFromBuild(player,
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
			if (player.getLocation().distance(origin) > range) {
				remove();
				return;
			} else if (player.isSneaking()) {
				new WaterWave(player, AbilityType.SHIFT);
				return;
			}
			WaterMethods.playFocusWaterEffect(origin.getBlock());
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
				if (WaterMethods.isPlant(origin.getBlock()))
					new Plantbending(origin.getBlock());

			}

			removeType(player, AbilityType.CLICK);
			if (!player.isSneaking()) {
				if (System.currentTimeMillis() - time > chargeTime) {
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
				if (!(WaterMethods.isWaterbendable(block, player) || block.getType() == Material.AIR)
						|| GeneralMethods.isRegionProtectedFromBuild(player,
								"WaterSpout", block.getLocation())) {
					remove();
					return;
				}
				createBlock(block, Material.STATIONARY_WATER);
				if (currentLoc.distance(origin) > 2)
					anim = AnimateState.TOWARDPLAYER;
			} else if (anim == AnimateState.TOWARDPLAYER) {
				revertBlocks();
				Location eyeLoc = player.getTargetBlock((HashSet<Material>) null, 2).getLocation();
				eyeLoc.setY(player.getEyeLocation().getY());
				Vector vec = GeneralMethods.getDirection(currentLoc, eyeLoc);
				currentLoc.add(vec.normalize().multiply(animSpeed));

				Block block = currentLoc.getBlock();
				if (!(WaterMethods.isWaterbendable(block, player) || block.getType() == Material.AIR)
						|| GeneralMethods.isRegionProtectedFromBuild(player,
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
				if ((System.currentTimeMillis() - time > flightTime && !AvatarState
						.isAvatarState(player)) || player.isSneaking()) {
					remove();
					return;
				}
				player.setFallDistance(0f);
				double currentSpeed = speed
						- (speed
								* (double) (System.currentTimeMillis() - time) / (double) flightTime);
				double nightSpeed = WaterMethods.waterbendingNightAugment(
						currentSpeed * 0.9, player.getWorld());
				currentSpeed = nightSpeed > currentSpeed ? nightSpeed
						: currentSpeed;
				if (AvatarState.isAvatarState(player))
					currentSpeed = WaterMethods.waterbendingNightAugment(speed,
							player.getWorld());

				player.setVelocity(player.getEyeLocation().getDirection()
						.normalize().multiply(currentSpeed));
				for (Block block : GeneralMethods.getBlocksAroundPoint(player
						.getLocation().add(0, -1, 0), waveRadius))
					if (block.getType() == Material.AIR
							&& !GeneralMethods.isRegionProtectedFromBuild(player,
									"WaterSpout", block.getLocation())) {
						if (iceWave)
							createBlockDelay(block, Material.ICE, (byte) 0, 2L);
						else
							createBlock(block, Material.STATIONARY_WATER,
									(byte) 0);
					}
				revertBlocksDelay(20L);

				if (iceWave && progressCounter % 3 == 0) {
					for (Entity entity : GeneralMethods.getEntitiesAroundPoint(player
							.getLocation().add(0, -1, 0), waveRadius * 1.5)) {
						if (entity != this.player
								&& entity instanceof LivingEntity
								&& !affectedEntities.contains(entity)) {
							affectedEntities.add(entity);
							final double aug = WaterMethods
									.getWaterbendingNightAugment(player
											.getWorld());
							GeneralMethods.damageEntity(player, entity, aug
									* damage);
							final Player fplayer = this.player;
							final Entity fent = entity;
							new BukkitRunnable() {
								public void run() {
									createIceSphere(fplayer, fent, aug * 2.5);
								}
							}.runTaskLater(ProjectKorra.plugin, 6);
						}
					}
				}
			}
		}
	}

	public void drawCircle(double theta, double increment) {
		double rotateSpeed = 45;
		revertBlocks();
		direction = GeneralMethods.rotateXZ(direction, rotateSpeed);
		for (double i = 0; i < theta; i += increment) {
			Vector dir = GeneralMethods.rotateXZ(direction, i - theta / 2).normalize()
					.multiply(radius);
			dir.setY(0);
			Block block = player.getEyeLocation().add(dir).getBlock();
			currentLoc = block.getLocation();
			if (block.getType() == Material.AIR
					&& !GeneralMethods.isRegionProtectedFromBuild(player,
							"WaterSpout", block.getLocation()))
				createBlock(block, Material.STATIONARY_WATER, (byte) 8);
		}
	}

	public void remove() {
		instances.remove(this);
		revertBlocks();
		for (BukkitRunnable task : tasks)
			task.cancel();
	}

	public void createBlockDelay(final Block block, final Material mat,
			final byte data, long delay) {
		BukkitRunnable br = new BukkitRunnable() {
			@Override
			public void run() {
				createBlock(block, mat, data);
			}
		};
		br.runTaskLater(ProjectKorra.plugin, delay);
		tasks.add(br);
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
					if (!frozenBlocks.containsKey(block))
						tblock.revertBlock();
				}
			}.runTaskLater(ProjectKorra.plugin, delay);
		}
	}

	public void createIceSphere(Player player, Entity entity, double radius) {
		for (double x = -radius; x <= radius; x += 0.5)
			for (double y = -radius; y <= radius; y += 0.5)
				for (double z = -radius; z <= radius; z += 0.5) {
					Block block = entity.getLocation().getBlock().getLocation()
							.add(x, y, z).getBlock();
					if (block.getLocation().distance(
							entity.getLocation().getBlock().getLocation()) > radius)
						continue;

					if (block.getType() == Material.AIR
							|| block.getType() == Material.ICE
							|| WaterMethods.isWaterbendable(block, player)) {

						if (!frozenBlocks.containsKey(block)) {
							TempBlock tblock = new TempBlock(block,
									Material.ICE, (byte) 1);
							frozenBlocks.put(block, tblock);
						}
					}
				}
	}

	public static void progressAll() {
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
			WaterWave wave = instances.get(i);
			if (wave.player.equals(player) && wave.type.equals(type))
				return true;
		}
		return false;
	}

	public static void removeType(Player player, AbilityType type) {
		for (int i = 0; i < instances.size(); i++) {
			WaterWave wave = instances.get(i);
			if (wave.player.equals(player) && wave.type.equals(type)) {
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

	public static boolean canThaw(Block block) {
		return frozenBlocks.containsKey(block);
	}

	public static void thaw(Block block) {
		if (frozenBlocks.containsKey(block)) {
			frozenBlocks.get(block).revertBlock();
			frozenBlocks.remove(block);
		}
	}

	public Player getPlayer() {
		return player;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getChargeTime() {
		return chargeTime;
	}

	public void setChargeTime(double chargeTime) {
		this.chargeTime = chargeTime;
	}

	public double getFlightTime() {
		return flightTime;
	}

	public void setFlightTime(double flightTime) {
		this.flightTime = flightTime;
	}

	public double getWaveRadius() {
		return waveRadius;
	}

	public void setWaveRadius(double waveRadius) {
		this.waveRadius = waveRadius;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}
	

	public void setIceWave(boolean b) {
		this.iceWave = b;
	}
	
	public boolean isIceWave() {
		return this.iceWave;
	}
}

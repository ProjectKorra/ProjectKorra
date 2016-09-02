package com.projectkorra.projectkorra.waterbending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.firebending.FireBlast;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class SurgeWall extends WaterAbility {

	private static final byte FULL = 0x0;
	private static final String RANGE_CONFIG = "Abilities.Water.Surge.Wall.Range";
	private static final ConcurrentHashMap<Block, Block> AFFECTED_BLOCKS = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<Block, Player> WALL_BLOCKS = new ConcurrentHashMap<>();	

	private boolean progressing;
	private boolean settingUp;
	private boolean forming;
	private boolean frozen;
	private long time;
	private long interval;
	private long cooldown;
	private double radius;
	private double range;
	private Block sourceBlock;
	private Location location;
	private Location firstDestination;
	private Location targetDestination;
	private Vector firstDirection;
	private Vector targetDirection;

	@SuppressWarnings("deprecation")
	public SurgeWall(Player player) {
		super(player);
		
		this.interval = getConfig().getLong("Abilities.Water.Surge.Wall.Interval");
		this.cooldown = getConfig().getLong("Abilities.Water.Surge.Wall.Cooldown");
		this.range = getConfig().getDouble(RANGE_CONFIG);
		this.radius = getConfig().getDouble("Abilities.Water.Surge.Wall.Radius");

		SurgeWave wave = getAbility(player, SurgeWave.class);
		if (wave != null && !wave.isProgressing()) {
			wave.moveWater();
			return;
		}

		if (bPlayer.isAvatarState()) {
			radius = AvatarState.getValue(radius);
		}
		
		SurgeWall wall = getAbility(player, SurgeWall.class);
		if (wall != null) {
			if (wall.progressing) {
				wall.freezeThaw();
				return;
			} else if (prepare()) {
				wall.remove();
				start();
				time = System.currentTimeMillis();
			}
		} else if (prepare()) {
			start();
			time = System.currentTimeMillis();
		}

		if (bPlayer.isOnCooldown("SurgeWall")) {
			return;
		} else if (wall == null && WaterReturn.hasWaterBottle(player)) {
			Location eyeLoc = player.getEyeLocation();
			Block block = eyeLoc.add(eyeLoc.getDirection().normalize()).getBlock();
			
			if (isTransparent(player, block) && isTransparent(player, eyeLoc.getBlock())) {
				block.setType(Material.WATER);
				block.setData(FULL);
				
				wave = new SurgeWave(player);
				wave.setCanHitSelf(false);
				wave.moveWater();
				
				if (!wave.isProgressing()) {
					block.setType(Material.AIR);
					wave.remove();
				} else {
					WaterReturn.emptyWaterBottle(player);
				}
			}
		}
	}

	private void freezeThaw() {
		if (!bPlayer.canIcebend()) {
			return;
		} else if (frozen) {
			thaw();
		} else {
			freeze();
		}
	}

	private void freeze() {
		frozen = true;
		for (Block block : WALL_BLOCKS.keySet()) {
			if (WALL_BLOCKS.get(block) == player) {
				new TempBlock(block, Material.ICE, (byte) 0);
				playIcebendingSound(block.getLocation());
			}
		}
	}

	private void thaw() {
		frozen = false;
		for (Block block : WALL_BLOCKS.keySet()) {
			if (WALL_BLOCKS.get(block) == player) {
				new TempBlock(block, Material.STATIONARY_WATER, (byte) 8);
			}
		}
	}

	public boolean prepare() {
		cancelPrevious();
		Block block = BlockSource.getWaterSourceBlock(player, range, ClickType.LEFT_CLICK, true, true, bPlayer.canPlantbend());
		
		if (block != null && !GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
			sourceBlock = block;
			focusBlock();
			return true;
		}
		return false;
	}

	private void cancelPrevious() {
		SurgeWall oldWave = getAbility(player, SurgeWall.class);
		if (oldWave != null) {
			if (oldWave.progressing) {
				oldWave.removeWater(oldWave.sourceBlock);
			} else {
				oldWave.remove();
			}
		}
	}

	private void focusBlock() {
		location = sourceBlock.getLocation();
	}

	@SuppressWarnings("deprecation")
	public void moveWater() {
		if (sourceBlock != null) {
			targetDestination = player.getTargetBlock(getTransparentMaterialSet(), (int) range).getLocation();

			if (targetDestination.distanceSquared(location) <= 1) {
				progressing = false;
				targetDestination = null;
			} else {
				bPlayer.addCooldown("SurgeWall", cooldown);
				progressing = true;
				settingUp = true;
				firstDestination = getToEyeLevel();
				firstDirection = getDirection(sourceBlock.getLocation(), firstDestination);
				targetDirection = getDirection(firstDestination, targetDestination);
				
				if (isPlant(sourceBlock) || isSnow(sourceBlock)) {
					new PlantRegrowth(player, sourceBlock);
				}
				if (!GeneralMethods.isAdjacentToThreeOrMoreSources(sourceBlock)) {
					sourceBlock.setType(Material.AIR);
				}
				addWater(sourceBlock);
			}

		}
	}

	private Location getToEyeLevel() {
		Location loc = sourceBlock.getLocation().clone();
		loc.setY(targetDestination.getY());
		return loc;
	}

	private Vector getDirection(Location location, Location destination) {
		double x1, y1, z1;
		double x0, y0, z0;

		x1 = destination.getX();
		y1 = destination.getY();
		z1 = destination.getZ();

		x0 = location.getX();
		y0 = location.getY();
		z0 = location.getZ();

		return new Vector(x1 - x0, y1 - y0, z1 - z0);
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			remove();
			return;
		}

		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();
			boolean matchesName = bPlayer.getBoundAbilityName().equalsIgnoreCase(getName());
			
			if (!progressing && !matchesName) {
				remove();
				return;
			} else if (progressing && (!player.isSneaking() || !matchesName)) {
				remove();
				return;
			} else if (!progressing) {
				sourceBlock.getWorld().playEffect(location, Effect.SMOKE, 4, (int) range);
				return;
			}

			if (forming) {
				if ((new Random()).nextInt(7) == 0) {
					playWaterbendingSound(location);
				}
				
				ArrayList<Block> blocks = new ArrayList<Block>();
				Location targetLoc = GeneralMethods.getTargetedLocation(player, (int) range, 8, 9, 79);
				location = targetLoc.clone();
				Vector eyeDir = player.getEyeLocation().getDirection();
				Vector vector;
				Block block;
				
				for (double i = 0; i <= getNightFactor(radius); i += 0.5) {
					for (double angle = 0; angle < 360; angle += 10) {
						vector = GeneralMethods.getOrthogonalVector(eyeDir.clone(), angle, i);
						block = targetLoc.clone().add(vector).getBlock();
						
						if (GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
							continue;
						} else if (WALL_BLOCKS.containsKey(block)) {
							blocks.add(block);
						} else if (!blocks.contains(block) 
								&& (block.getType() == Material.AIR 
									|| block.getType() == Material.FIRE 
									|| isWaterbendable(block)) && isTransparent(block)) {
							WALL_BLOCKS.put(block, player);
							addWallBlock(block);
							blocks.add(block);
							FireBlast.removeFireBlastsAroundPoint(block.getLocation(), 2);
						}
					}
				}

				for (Block blocki : WALL_BLOCKS.keySet()) {
					if (WALL_BLOCKS.get(blocki) == player && !blocks.contains(blocki)) {
						finalRemoveWater(blocki);
					}
				}
				return;
			}

			if (sourceBlock.getLocation().distanceSquared(firstDestination) < 0.5 * 0.5 && settingUp) {
				settingUp = false;
			}

			Vector direction;
			if (settingUp) {
				direction = firstDirection;
			} else {
				direction = targetDirection;
			}

			location = location.clone().add(direction);

			Block block = location.getBlock();
			if (block.getLocation().equals(sourceBlock.getLocation())) {
				location = location.clone().add(direction);
				block = location.getBlock();
			}
			
			if (block.getType() != Material.AIR) {
				remove();
				return;
			} else if (!progressing) {
				remove();
				return;
			}

			addWater(block);
			removeWater(sourceBlock);
			sourceBlock = block;

			if (location.distanceSquared(targetDestination) < 1) {
				removeWater(sourceBlock);;
				forming = true;
			}
		}
	}

	private void addWallBlock(Block block) {
		if (frozen) {
			new TempBlock(block, Material.ICE, (byte) 0);
		} else {
			new TempBlock(block, Material.STATIONARY_WATER, (byte) 8);
		}
	}

	@Override
	public void remove() {
		super.remove();
		returnWater();
		finalRemoveWater(sourceBlock);
		
		for (Block block : WALL_BLOCKS.keySet()) {
			if (WALL_BLOCKS.get(block) == player) {
				finalRemoveWater(block);
			}
		}
		
	}

	private void removeWater(Block block) {
		if (block != null) {
			if (AFFECTED_BLOCKS.containsKey(block)) {
				if (!GeneralMethods.isAdjacentToThreeOrMoreSources(block)) {
					TempBlock.revertBlock(block, Material.AIR);
				}
				AFFECTED_BLOCKS.remove(block);
			}
		}
	}

	private static void finalRemoveWater(Block block) {
		if (block != null) {
			if (AFFECTED_BLOCKS.containsKey(block)) {
				TempBlock.revertBlock(block, Material.AIR);
				AFFECTED_BLOCKS.remove(block);
			}
			if (WALL_BLOCKS.containsKey(block)) {
				TempBlock.revertBlock(block, Material.AIR);
				WALL_BLOCKS.remove(block);
			}
		}
	}

	private void addWater(Block block) {
		if (GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
			return;
		} else if (!TempBlock.isTempBlock(block)) {
			new TempBlock(block, Material.STATIONARY_WATER, (byte) 8);
			AFFECTED_BLOCKS.put(block, block);
		}
	}

	@SuppressWarnings("deprecation")
	public static void form(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return;
		}
		
		int range = getConfig().getInt(RANGE_CONFIG);
		SurgeWall wall = getAbility(player, SurgeWall.class);
		SurgeWave wave = getAbility(player, SurgeWave.class);
		
		if (wall == null) {
			if (wave == null 
					&& BlockSource.getWaterSourceBlock(player, range, ClickType.LEFT_CLICK, true, true, bPlayer.canPlantbend()) == null
					&& WaterReturn.hasWaterBottle(player)) {
				if (bPlayer.isOnCooldown("SurgeWall")) {
					return;
				}

				Location eyeLoc = player.getEyeLocation();
				Block block = eyeLoc.add(eyeLoc.getDirection().normalize()).getBlock();
				if (isTransparent(player, block) && isTransparent(player, eyeLoc.getBlock())) {
					block.setType(Material.WATER);
					block.setData(FULL);
					
					wall = new SurgeWall(player);
					wall.moveWater();
					if (!wall.progressing) {
						block.setType(Material.AIR);
						wall.remove();
					} else {
						WaterReturn.emptyWaterBottle(player);
					}
					return;
				}
			}

			wave = new SurgeWave(player);
			return;
		} else {
			if (isWaterbendable(player, null, player.getTargetBlock((HashSet<Material>) null, range))) {
				wave = new SurgeWave(player);
				return;
			}
		}

		if (wall != null) {
			wall.moveWater();
		}
	}

	public static void removeAllCleanup() {
		for (Block block : AFFECTED_BLOCKS.keySet()) {
			TempBlock.revertBlock(block, Material.AIR);
			AFFECTED_BLOCKS.remove(block);
			WALL_BLOCKS.remove(block);
		}
		for (Block block : WALL_BLOCKS.keySet()) {
			TempBlock.revertBlock(block, Material.AIR);
			AFFECTED_BLOCKS.remove(block);
			WALL_BLOCKS.remove(block);
		}
	}


	public static void thaw(Block block) {
		finalRemoveWater(block);
	}
	
	public static boolean wasBrokenFor(Player player, Block block) {
		SurgeWall wall = getAbility(player, SurgeWall.class);
		if (wall != null) {
			if (wall.sourceBlock == null) {
				return false;
			} else if (wall.sourceBlock.equals(block)) {
				return true;
			}
		}
		return false;
	}

	private void returnWater() {
		if (location != null) {
			if (frozen) {
				location.getBlock().setType(Material.WATER);
			}
			new WaterReturn(player, location.getBlock());
		}
	}

	@Override
	public String getName() {
		return "Surge";
	}

	@Override
	public Location getLocation() {
		if (location != null) {
			return location;
		} else if (sourceBlock != null) {
			return sourceBlock.getLocation();
		}
		return player != null ? player.getLocation() : null;
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

	public boolean isProgressing() {
		return progressing;
	}

	public void setProgressing(boolean progressing) {
		this.progressing = progressing;
	}

	public boolean isSettingUp() {
		return settingUp;
	}

	public void setSettingUp(boolean settingUp) {
		this.settingUp = settingUp;
	}

	public boolean isForming() {
		return forming;
	}

	public void setForming(boolean forming) {
		this.forming = forming;
	}

	public boolean isFrozen() {
		return frozen;
	}

	public void setFrozen(boolean frozen) {
		this.frozen = frozen;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
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

	public Block getSourceBlock() {
		return sourceBlock;
	}

	public void setSourceBlock(Block sourceBlock) {
		this.sourceBlock = sourceBlock;
	}

	public Location getFirstDestination() {
		return firstDestination;
	}

	public void setFirstDestination(Location firstDestination) {
		this.firstDestination = firstDestination;
	}

	public Location getTargetDestination() {
		return targetDestination;
	}

	public void setTargetDestination(Location targetDestination) {
		this.targetDestination = targetDestination;
	}

	public Vector getFirstDirection() {
		return firstDirection;
	}

	public void setFirstDirection(Vector firstDirection) {
		this.firstDirection = firstDirection;
	}

	public Vector getTargetDirection() {
		return targetDirection;
	}

	public void setTargetDirection(Vector targetDirection) {
		this.targetDirection = targetDirection;
	}

	public static ConcurrentHashMap<Block, Block> getAffectedBlocks() {
		return AFFECTED_BLOCKS;
	}

	public static ConcurrentHashMap<Block, Player> getWallBlocks() {
		return WALL_BLOCKS;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	
}

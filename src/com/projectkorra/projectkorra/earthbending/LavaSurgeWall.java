package com.projectkorra.projectkorra.earthbending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.LavaAbility;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.firebending.FireBlast;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.BlockSource.BlockSourceType;
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
import java.util.concurrent.ConcurrentHashMap;

public class LavaSurgeWall extends LavaAbility {
	
	private static final ConcurrentHashMap<Block, Block> AFFECTED_BLOCKS = new ConcurrentHashMap<Block, Block>();
	private static final ConcurrentHashMap<Block, Player> WALL_BLOCKS = new ConcurrentHashMap<Block, Player>();
	private static final int SURGE_WAVE_RANGE = 20; // TODO: remove this
	
	private boolean progressing;
	private boolean settingUp;
	private boolean forming;
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
	
	public LavaSurgeWall(Player player) {
		super(player);
		
		this.interval = 30;
		this.radius = getConfig().getDouble("Abilities.Water.Surge.Wall.Radius");
		this.range = getConfig().getDouble("Abilities.Water.Surge.Wall.Range");
		this.cooldown = GeneralMethods.getGlobalCooldown();
		
		LavaSurgeWave wave = getAbility(player, LavaSurgeWave.class);
		if (wave != null && wave.isProgressing()) {
			LavaSurgeWave.launch(player);
			return;
		}

		if (bPlayer.isAvatarState()) {
			radius = AvatarState.getValue(radius);
			range = AvatarState.getValue(range);
		}

		if (!bPlayer.canBend(this)) {
			return;
		}
	}

	public boolean prepare() {
		cancelPrevious();
		Block block = BlockSource.getSourceBlock(player, range, BlockSourceType.LAVA, ClickType.LEFT_CLICK);
		if (block != null) {
			sourceBlock = block;
			focusBlock();
			return true;
		}
		return false;
	}

	private void cancelPrevious() {
		LavaSurgeWall lavaWall = getAbility(player, LavaSurgeWall.class);
		if (lavaWall != null) {
			if (lavaWall.progressing) {
				lavaWall.removeLava(lavaWall.sourceBlock);
			} else {
				lavaWall.cancel();
			}
		}
	}

	public void cancel() {
		remove();
	}

	private void focusBlock() {
		location = sourceBlock.getLocation();
	}

	public void moveLava() {
		if (sourceBlock != null) {
			targetDestination = getTargetEarthBlock((int) range).getLocation();
			
			if (targetDestination.distanceSquared(location) <= 1) {
				progressing = false;
				targetDestination = null;
			} else {
				progressing = true;
				settingUp = true;
				firstDestination = getToEyeLevel();
				firstDirection = getDirection(sourceBlock.getLocation(), firstDestination);
				targetDirection = getDirection(firstDestination, targetDestination);

				if (!GeneralMethods.isAdjacentToThreeOrMoreSources(sourceBlock)) {
					sourceBlock.setType(Material.AIR);
				}
				addLava(sourceBlock);
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
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			if (!forming) {
				breakBlock();
			}
			remove();
			return;
		}
		
		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();
			if (progressing && !player.isSneaking()) {
				remove();
				return;
			} 
			
			if (!progressing) {
				sourceBlock.getWorld().playEffect(location, Effect.SMOKE, 4, (int) range);
				return;
			}
			
			if (forming) {
				ArrayList<Block> blocks = new ArrayList<Block>();
				Location loc = GeneralMethods.getTargetedLocation(player, (int) range, 8, 9, 79);
				location = loc.clone();
				Vector dir = player.getEyeLocation().getDirection();
				Vector vec;
				Block block;
				
				for (double i = 0; i <= radius; i += 0.5) {
					for (double angle = 0; angle < 360; angle += 10) {
						vec = GeneralMethods.getOrthogonalVector(dir.clone(), angle, i);
						block = loc.clone().add(vec).getBlock();
						
						if (GeneralMethods.isRegionProtectedFromBuild(player, "LavaSurge", block.getLocation())) {
							continue;
						}
						if (WALL_BLOCKS.containsKey(block)) {
							blocks.add(block);
						} else if (!blocks.contains(block) 
								&& (block.getType() == Material.AIR || block.getType() == Material.FIRE || isLavabendable(block))) {
							WALL_BLOCKS.put(block, player);
							addWallBlock(block);
							blocks.add(block);
							FireBlast.removeFireBlastsAroundPoint(block.getLocation(), 2);
						}
					}
				}
				
				for (Block blocki : WALL_BLOCKS.keySet()) {
					if (WALL_BLOCKS.get(blocki) == player && !blocks.contains(blocki)) {
						finalRemoveLava(blocki);
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
				breakBlock();
				return;
			} else if (!progressing) {
				breakBlock();
				return;
			}
			
			addLava(block);
			removeLava(sourceBlock);
			sourceBlock = block;
			if (location.distanceSquared(targetDestination) < 1) {
				removeLava(sourceBlock);
				forming = true;
			}
			return;
		}
	}

	private void addWallBlock(Block block) {
		new TempBlock(block, Material.STATIONARY_LAVA, (byte) 8);
	}

	private void breakBlock() {
		finalRemoveLava(sourceBlock);
		for (Block block : WALL_BLOCKS.keySet()) {
			if (WALL_BLOCKS.get(block) == player) {
				finalRemoveLava(block);
			}
		}
		remove();
	}

	private void removeLava(Block block) {
		if (block != null) {
			if (AFFECTED_BLOCKS.containsKey(block)) {
				if (!GeneralMethods.isAdjacentToThreeOrMoreSources(block)) {
					TempBlock.revertBlock(block, Material.AIR);
				}
				AFFECTED_BLOCKS.remove(block);
			}
		}
	}

	private static void finalRemoveLava(Block block) {
		if (AFFECTED_BLOCKS.containsKey(block)) {
			TempBlock.revertBlock(block, Material.AIR);
			AFFECTED_BLOCKS.remove(block);
		}
		if (WALL_BLOCKS.containsKey(block)) {
			TempBlock.revertBlock(block, Material.AIR);
			WALL_BLOCKS.remove(block);
		}
	}

	private void addLava(Block block) {
		if (GeneralMethods.isRegionProtectedFromBuild(player, "LavaSurge", block.getLocation()))
			return;
		if (!TempBlock.isTempBlock(block)) {
			new TempBlock(block, Material.STATIONARY_LAVA, (byte) 8);
			AFFECTED_BLOCKS.put(block, block);
		}
	}

	public static void moveLava(Player player) {
		LavaSurgeWall wall = getAbility(player, LavaSurgeWall.class);
		if (wall != null) {
			wall.moveLava();
		}
	}

	@SuppressWarnings("deprecation")
	public static void form(Player player) {
		if (!hasAbility(player, LavaSurgeWall.class)) {
			new LavaSurgeWave(player);
			return;
		} else if (isLavabendable(player, player.getTargetBlock((HashSet<Byte>) null, SURGE_WAVE_RANGE))) {
			new LavaSurgeWave(player);
			return;
		}
		moveLava(player);
	}

	public static void cleanup() {
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

	public static boolean wasBrokenFor(Player player, Block block) {
		LavaSurgeWall wall = getAbility(player, LavaSurgeWall.class);
		if (wall != null) {
			if (wall.sourceBlock == null) {
				return false;
			} else if (wall.sourceBlock.equals(block)) {
				return true;
			}
		}
		return false;
	}
	
	public static ConcurrentHashMap<Block, Block> getAffectedBlocks() {
		return AFFECTED_BLOCKS;
	}

	public static ConcurrentHashMap<Block, Player> getWallBlocks() {
		return WALL_BLOCKS;
	}

	@Override
	public String getName() {
		return null;
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

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	
}

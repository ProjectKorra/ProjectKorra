package com.projectkorra.projectkorra.earthbending.lava;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.ElementalAbility;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LavaSurgeWall extends LavaAbility {

	private static final Map<Block, Block> AFFECTED_BLOCKS = new ConcurrentHashMap<Block, Block>();
	private static final Map<Block, Player> WALL_BLOCKS = new ConcurrentHashMap<Block, Player>();
	private static final int SURGE_WAVE_RANGE = 20;

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

	public LavaSurgeWall(final Player player) {
		super(player);

		this.interval = 30;
		this.radius = getConfig().getDouble("Abilities.Water.Surge.Wall.Radius");
		this.range = getConfig().getDouble("Abilities.Water.Surge.Wall.Range");
		this.cooldown = GeneralMethods.getGlobalCooldown();

		final LavaSurgeWave wave = getAbility(player, LavaSurgeWave.class);
		if (wave != null && wave.isProgressing()) {
			LavaSurgeWave.launch(player);
			return;
		}

		if (this.bPlayer.isAvatarState()) {
			this.radius = AvatarState.getValue(this.radius);
			this.range = AvatarState.getValue(this.range);
		}

		if (!this.bPlayer.canBend(this)) {
			return;
		}
	}

	public boolean prepare() {
		this.cancelPrevious();
		final Block block = BlockSource.getSourceBlock(this.player, this.range, BlockSourceType.LAVA, ClickType.LEFT_CLICK);
		if (block != null) {
			this.sourceBlock = block;
			this.focusBlock();
			return true;
		}
		return false;
	}

	private void cancelPrevious() {
		final LavaSurgeWall lavaWall = getAbility(this.player, LavaSurgeWall.class);
		if (lavaWall != null) {
			if (lavaWall.progressing) {
				lavaWall.removeLava(lavaWall.sourceBlock);
			} else {
				lavaWall.cancel();
			}
		}
	}

	public void cancel() {
		this.remove();
	}

	private void focusBlock() {
		this.location = this.sourceBlock.getLocation();
	}

	public void moveLava() {
		if (this.sourceBlock != null) {
			this.targetDestination = this.getTargetEarthBlock((int) this.range).getLocation();

			if (this.targetDestination.distanceSquared(this.location) <= 1) {
				this.progressing = false;
				this.targetDestination = null;
			} else {
				this.progressing = true;
				this.settingUp = true;
				this.firstDestination = this.getToEyeLevel();
				this.firstDirection = this.getDirection(this.sourceBlock.getLocation(), this.firstDestination);
				this.targetDirection = this.getDirection(this.firstDestination, this.targetDestination);

				if (!GeneralMethods.isAdjacentToThreeOrMoreSources(this.sourceBlock)) {
					this.sourceBlock.setType(Material.AIR);
				}
				this.addLava(this.sourceBlock);
			}
		}
	}

	private Location getToEyeLevel() {
		final Location loc = this.sourceBlock.getLocation().clone();
		loc.setY(this.targetDestination.getY());
		return loc;
	}

	private Vector getDirection(final Location location, final Location destination) {
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
		if (!this.bPlayer.canBendIgnoreCooldowns(this)) {
			if (!this.forming) {
				this.breakBlock();
			}
			this.remove();
			return;
		}

		if (System.currentTimeMillis() - this.time >= this.interval) {
			this.time = System.currentTimeMillis();
			if (this.progressing && !this.player.isSneaking()) {
				this.remove();
				return;
			}

			if (!this.progressing) {
				this.sourceBlock.getWorld().playEffect(this.location, Effect.SMOKE, 4, (int) this.range);
				return;
			}

			if (this.forming) {
				final ArrayList<Block> blocks = new ArrayList<Block>();
				final Location loc = GeneralMethods.getTargetedLocation(this.player, (int) this.range, Material.WATER, Material.ICE);
				this.location = loc.clone();
				final Vector dir = this.player.getEyeLocation().getDirection();
				Vector vec;
				Block block;

				for (double i = 0; i <= this.radius; i += 0.5) {
					for (double angle = 0; angle < 360; angle += 10) {
						vec = GeneralMethods.getOrthogonalVector(dir.clone(), angle, i);
						block = loc.clone().add(vec).getBlock();

						if (GeneralMethods.isRegionProtectedFromBuild(this.player, "LavaSurge", block.getLocation())) {
							continue;
						}
						if (WALL_BLOCKS.containsKey(block)) {
							blocks.add(block);
						} else if (!blocks.contains(block) && (ElementalAbility.isAir(block.getType()) || block.getType() == Material.FIRE || this.isLavabendable(block))) {
							WALL_BLOCKS.put(block, this.player);
							this.addWallBlock(block);
							blocks.add(block);
							FireBlast.removeFireBlastsAroundPoint(block.getLocation(), 2);
						}
					}
				}

				for (final Block blocki : WALL_BLOCKS.keySet()) {
					if (WALL_BLOCKS.get(blocki) == this.player && !blocks.contains(blocki)) {
						finalRemoveLava(blocki);
					}
				}

				return;
			}

			if (this.sourceBlock.getLocation().distanceSquared(this.firstDestination) < 0.5 * 0.5 && this.settingUp) {
				this.settingUp = false;
			}

			Vector direction;
			if (this.settingUp) {
				direction = this.firstDirection;
			} else {
				direction = this.targetDirection;
			}

			this.location = this.location.clone().add(direction);
			Block block = this.location.getBlock();
			if (block.getLocation().equals(this.sourceBlock.getLocation())) {
				this.location = this.location.clone().add(direction);
				block = this.location.getBlock();
			}

			if (!ElementalAbility.isAir(block.getType())) {
				this.breakBlock();
				return;
			} else if (!this.progressing) {
				this.breakBlock();
				return;
			}

			this.addLava(block);
			this.removeLava(this.sourceBlock);
			this.sourceBlock = block;
			if (this.location.distanceSquared(this.targetDestination) < 1) {
				this.removeLava(this.sourceBlock);
				this.forming = true;
			}
			return;
		}
	}

	private void addWallBlock(final Block block) {
		new TempBlock(block, Material.LAVA, GeneralMethods.getLavaData(0));
	}

	private void breakBlock() {
		finalRemoveLava(this.sourceBlock);
		for (final Block block : WALL_BLOCKS.keySet()) {
			if (WALL_BLOCKS.get(block) == this.player) {
				finalRemoveLava(block);
			}
		}
		this.remove();
	}

	private void removeLava(final Block block) {
		if (block != null) {
			if (AFFECTED_BLOCKS.containsKey(block)) {
				if (!GeneralMethods.isAdjacentToThreeOrMoreSources(block)) {
					TempBlock.revertBlock(block, Material.AIR);
				}
				AFFECTED_BLOCKS.remove(block);
			}
		}
	}

	private static void finalRemoveLava(final Block block) {
		if (AFFECTED_BLOCKS.containsKey(block)) {
			TempBlock.revertBlock(block, Material.AIR);
			AFFECTED_BLOCKS.remove(block);
		}
		if (WALL_BLOCKS.containsKey(block)) {
			TempBlock.revertBlock(block, Material.AIR);
			WALL_BLOCKS.remove(block);
		}
	}

	private void addLava(final Block block) {
		if (GeneralMethods.isRegionProtectedFromBuild(this.player, "LavaSurge", block.getLocation())) {
			return;
		}
		if (!TempBlock.isTempBlock(block)) {
			new TempBlock(block, Material.LAVA, GeneralMethods.getLavaData(0));
			AFFECTED_BLOCKS.put(block, block);
		}
	}

	public static void moveLava(final Player player) {
		final LavaSurgeWall wall = getAbility(player, LavaSurgeWall.class);
		if (wall != null) {
			wall.moveLava();
		}
	}

	public static void form(final Player player) {
		if (!hasAbility(player, LavaSurgeWall.class)) {
			new LavaSurgeWave(player);
			return;
		} else if (isLavabendable(player, player.getTargetBlock((HashSet<Material>) null, SURGE_WAVE_RANGE))) {
			new LavaSurgeWave(player);
			return;
		}
		moveLava(player);
	}

	public static void cleanup() {
		for (final Block block : AFFECTED_BLOCKS.keySet()) {
			TempBlock.revertBlock(block, Material.AIR);
			AFFECTED_BLOCKS.remove(block);
			WALL_BLOCKS.remove(block);
		}
		for (final Block block : WALL_BLOCKS.keySet()) {
			TempBlock.revertBlock(block, Material.AIR);
			AFFECTED_BLOCKS.remove(block);
			WALL_BLOCKS.remove(block);
		}
	}

	public static boolean wasBrokenFor(final Player player, final Block block) {
		final LavaSurgeWall wall = getAbility(player, LavaSurgeWall.class);
		if (wall != null) {
			if (wall.sourceBlock == null) {
				return false;
			} else if (wall.sourceBlock.equals(block)) {
				return true;
			}
		}
		return false;
	}

	public static Map<Block, Block> getAffectedBlocks() {
		return AFFECTED_BLOCKS;
	}

	public static Map<Block, Player> getWallBlocks() {
		return WALL_BLOCKS;
	}

	@Override
	public String getName() {
		return "LavaSurgeWall";
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
	public boolean isHiddenAbility() {
		return true;
	}

	public boolean isProgressing() {
		return this.progressing;
	}

	public void setProgressing(final boolean progressing) {
		this.progressing = progressing;
	}

	public boolean isSettingUp() {
		return this.settingUp;
	}

	public void setSettingUp(final boolean settingUp) {
		this.settingUp = settingUp;
	}

	public boolean isForming() {
		return this.forming;
	}

	public void setForming(final boolean forming) {
		this.forming = forming;
	}

	public long getTime() {
		return this.time;
	}

	public void setTime(final long time) {
		this.time = time;
	}

	public long getInterval() {
		return this.interval;
	}

	public void setInterval(final long interval) {
		this.interval = interval;
	}

	public double getRadius() {
		return this.radius;
	}

	public void setRadius(final double radius) {
		this.radius = radius;
	}

	public double getRange() {
		return this.range;
	}

	public void setRange(final double range) {
		this.range = range;
	}

	public Block getSourceBlock() {
		return this.sourceBlock;
	}

	public void setSourceBlock(final Block sourceBlock) {
		this.sourceBlock = sourceBlock;
	}

	public Location getFirstDestination() {
		return this.firstDestination;
	}

	public void setFirstDestination(final Location firstDestination) {
		this.firstDestination = firstDestination;
	}

	public Location getTargetDestination() {
		return this.targetDestination;
	}

	public void setTargetDestination(final Location targetDestination) {
		this.targetDestination = targetDestination;
	}

	public Vector getFirstDirection() {
		return this.firstDirection;
	}

	public void setFirstDirection(final Vector firstDirection) {
		this.firstDirection = firstDirection;
	}

	public Vector getTargetDirection() {
		return this.targetDirection;
	}

	public void setTargetDirection(final Vector targetDirection) {
		this.targetDirection = targetDirection;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

}

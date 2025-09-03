package com.projectkorra.projectkorra.waterbending;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import com.projectkorra.projectkorra.attribute.markers.DayNightFactor;
import com.projectkorra.projectkorra.region.RegionProtection;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.firebending.FireBlast;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.plant.PlantRegrowth;
import com.projectkorra.projectkorra.waterbending.util.WaterReturn;

public class SurgeWall extends WaterAbility {

	private static final Map<Block, Block> AFFECTED_BLOCKS = new ConcurrentHashMap<>();
	private static final Map<Block, Player> WALL_BLOCKS = new ConcurrentHashMap<>();
	public static final List<TempBlock> SOURCE_BLOCKS = new ArrayList<>();

	private boolean progressing;
	private boolean settingUp;
	private boolean forming;
	private boolean frozen;
	private boolean solidifyLava;
	private long time;
	private long interval;
	@Attribute(Attribute.COOLDOWN) @DayNightFactor(invert = true)
	private long cooldown;
	@Attribute(Attribute.DURATION) @DayNightFactor
	private long duration;
	private long obsidianDuration;
	@Attribute("Wall" + Attribute.RADIUS) @DayNightFactor
	private double radius;
	@Attribute("Wall" + Attribute.RANGE) @DayNightFactor
	private double range;
	@Attribute(Attribute.SELECT_RANGE)
	private double selectRange;
	private Block sourceBlock;
	private Location location;
	private Location firstDestination;
	private Location targetDestination;
	private ArrayList<Location> locations;
	private Vector firstDirection;
	private Vector targetDirection;
	private Map<Block, TempBlock> tempBlocks = new HashMap<>();

	public SurgeWall(final Player player) {
		super(player);

		this.interval = getConfig().getLong("Abilities.Water.Surge.Wall.Interval");
		this.cooldown = getConfig().getLong("Abilities.Water.Surge.Wall.Cooldown");
		this.duration = getConfig().getLong("Abilities.Water.Surge.Wall.Duration");
		this.range = getConfig().getDouble("Abilities.Water.Surge.Wall.Range");
		this.radius = getConfig().getDouble("Abilities.Water.Surge.Wall.Radius");
		this.selectRange = getConfig().getDouble("Abilities.Water.Surge.Wall.SelectRange");
		this.solidifyLava = getConfig().getBoolean("Abilities.Water.Surge.Wall.SolidifyLava.Enabled");
		this.obsidianDuration = getConfig().getLong("Abilities.Water.Surge.Wall.SolidifyLava.Duration");
		this.locations = new ArrayList<>();

		SurgeWave wave = getAbility(player, SurgeWave.class);
		if (wave != null && !wave.isProgressing() && !this.bPlayer.isOnCooldown("SurgeWave")) {
			wave.moveWater();
			return;
		}

		final SurgeWall wall = getAbility(player, SurgeWall.class);
		if (wall != null) {
			if (wall.progressing) {
				wall.freezeThaw();
				return;
			} else if (this.prepare()) {
				wall.remove();
				this.start();
				this.time = System.currentTimeMillis();
			}
		} else if (!this.bPlayer.isOnCooldown("SurgeWall") && this.prepare()) {
			this.start();
			this.time = System.currentTimeMillis();
			return;
		}

		if (this.bPlayer.isOnCooldown("SurgeWave") || player.isSneaking()) {
			return;
		} else if (wall == null && WaterReturn.hasWaterBottle(player)) {
			final Location eyeLoc = player.getEyeLocation();
			final Block block = eyeLoc.add(eyeLoc.getDirection().normalize()).getBlock();

			if (isTransparent(player, block) && isTransparent(player, eyeLoc.getBlock())) {
				final TempBlock tempBlock = new TempBlock(block, Material.WATER);
				SOURCE_BLOCKS.add(tempBlock);

				wave = new SurgeWave(player);
				wave.setCanHitSelf(false);
				wave.moveWater();

				if (!wave.isProgressing()) {
					wave.remove();
				} else {
					WaterReturn.emptyWaterBottle(player);
				}

				SOURCE_BLOCKS.remove(tempBlock);
				tempBlock.revertBlock();
			}
		}
	}

	private void freezeThaw() {
		if (!this.bPlayer.canIcebend()) {
			return;
		} else if (this.frozen) {
			this.thaw();
		} else {
			this.freeze();
		}
	}

	private void freeze() {
		this.frozen = true;

		tempBlocks.values().forEach(TempBlock::revertBlock);
		tempBlocks.clear();

		for (final Block block : WALL_BLOCKS.keySet()) {
			if (WALL_BLOCKS.get(block) == this.player) {
				tempBlocks.put(block, new TempBlock(block, Material.ICE.createBlockData(), this).setCanSuffocate(false));
				playIcebendingSound(block.getLocation());
			}
		}
	}

	private void thaw() {
		this.frozen = false;

		tempBlocks.values().forEach(TempBlock::revertBlock);
		tempBlocks.clear();

		for (final Block block : WALL_BLOCKS.keySet()) {
			if (WALL_BLOCKS.get(block) == this.player) {
				tempBlocks.put(block, new TempBlock(block, Material.WATER));
			}
		}
	}

	public boolean prepare() {
		this.cancelPrevious();
		final Block block = BlockSource.getWaterSourceBlock(this.player, this.selectRange, ClickType.LEFT_CLICK, true, true, this.bPlayer.canPlantbend());

		if (block != null && !RegionProtection.isRegionProtected(this, block.getLocation())) {
			this.sourceBlock = block;
			this.focusBlock();
			return true;
		}
		return false;
	}

	private void cancelPrevious() {
		final SurgeWall oldWave = getAbility(this.player, SurgeWall.class);
		if (oldWave != null) {
			if (oldWave.progressing) {
				oldWave.removeWater(oldWave.sourceBlock);
			} else {
				oldWave.remove();
			}
		}
	}

	private void focusBlock() {
		this.location = this.sourceBlock.getLocation();
	}

	public void moveWater() {
		if (this.sourceBlock != null) {
			this.targetDestination = this.player.getTargetBlock(getTransparentMaterialSet(), (int) this.range).getLocation();

			if (this.targetDestination.distanceSquared(this.location) <= 1) {
				this.progressing = false;
				this.targetDestination = null;
			} else {
				this.bPlayer.addCooldown("SurgeWall", this.cooldown);
				this.progressing = true;
				this.settingUp = true;
				this.firstDestination = this.getToEyeLevel();
				this.firstDirection = this.getDirection(this.sourceBlock.getLocation(), this.firstDestination);
				this.targetDirection = this.getDirection(this.firstDestination, this.targetDestination);

				if (isPlant(this.sourceBlock) || isSnow(this.sourceBlock)) {
					new PlantRegrowth(this.player, this.sourceBlock);
					this.sourceBlock.setType(Material.AIR, false);
				} else if (isCauldron(this.sourceBlock) || isTransformableBlock(this.sourceBlock)) {
					updateSourceBlock(this.sourceBlock);
				}
				this.addWater(this.sourceBlock);
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
		if (!this.bPlayer.canBendIgnoreBindsCooldowns(this)) {
			this.remove();
			return;
		} else if (this.duration != 0 && System.currentTimeMillis() > this.getStartTime() + this.duration) {
			this.bPlayer.addCooldown(this);
			this.remove();
			return;
		} else if (!isWaterbendable(this.sourceBlock) && !this.settingUp && !this.forming && !this.progressing) {
			remove();
			return;
		}

		this.locations.clear();

		if (System.currentTimeMillis() - this.time >= this.interval) {
			this.time = System.currentTimeMillis();
			final boolean matchesName = this.bPlayer.getBoundAbilityName().equals(this.getName());

			if (!this.progressing && !matchesName) {
				this.remove();
				return;
			} else if (this.progressing && (!this.player.isSneaking() || !matchesName)) {
				this.remove();
				return;
			} else if (!this.progressing) {
				ParticleEffect.SMOKE_NORMAL.display(this.sourceBlock.getLocation().add(0.5, 0.5, 0.5), 1);
				return;
			}

			if (this.forming) {
				if ((new Random()).nextInt(7) == 0) {
					playWaterbendingSound(this.location);
				}

				final ArrayList<Block> blocks = new ArrayList<Block>();
				final Location targetLoc = GeneralMethods.getTargetedLocation(this.player, (int) this.range, false, false, Material.WATER, Material.ICE);
				this.location = targetLoc.clone();
				final Vector eyeDir = this.player.getEyeLocation().getDirection();
				Vector vector;
				Block block;
				for (double i = 0; i <= this.radius; i += 0.5) {
					for (double angle = 0; angle < 360; angle += 10) {
						vector = GeneralMethods.getOrthogonalVector(eyeDir.clone(), angle, i);
						block = targetLoc.clone().add(vector).getBlock();

						if (RegionProtection.isRegionProtected(this, block.getLocation())) {
							continue;
						} else if (WALL_BLOCKS.containsKey(block)) {
							blocks.add(block);
						} else if (!blocks.contains(block) && (ElementalAbility.isAir(block.getType()) || FireAbility.isFire(block.getType()) || this.isWaterbendable(block)) && this.isTransparent(block)) {
							if (!isWater(block) || frozen) {
								WALL_BLOCKS.put(block, this.player);
								this.addWallBlock(block);
							} else if (isWater(block) && !frozen) {
								ParticleEffect.WATER_BUBBLE.display(block.getLocation().clone().add(.5, .5, .5), 1, ThreadLocalRandom.current().nextDouble(0, 0.5), ThreadLocalRandom.current().nextDouble(0, 0.5), ThreadLocalRandom.current().nextDouble(0, 0.5), 0);
							}
							blocks.add(block);
							this.locations.add(block.getLocation());
							FireBlast.removeFireBlastsAroundPoint(block.getLocation(), 2);
						}
					}
				}

				for (final Block blocki : WALL_BLOCKS.keySet()) {
					if (WALL_BLOCKS.get(blocki) == this.player && !blocks.contains(blocki)) {
						this.finalRemoveWater(blocki);
					}

					if (solidifyLava) {
						for (BlockFace relative : BlockFace.values()) {
							Block blockRelative = blocki.getRelative(relative);
							if (blockRelative.getType() == Material.LAVA) {
								Levelled levelled = (Levelled)blockRelative.getBlockData();
								TempBlock tempBlock;

								if (levelled.getLevel() == 0)
									tempBlock = new TempBlock(blockRelative, Material.OBSIDIAN);
								else
									tempBlock = new TempBlock(blockRelative, Material.COBBLESTONE);

								tempBlock.setRevertTime(obsidianDuration);
								tempBlock.getBlock().getWorld().playSound(tempBlock.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 0.2F, 1);
							}
						}
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
				this.remove();
				return;
			} else if (!this.progressing) {
				this.remove();
				return;
			}

			this.addWater(block);
			this.removeWater(this.sourceBlock);
			this.sourceBlock = block;

			if (this.location.distanceSquared(this.targetDestination) < 1) {
				this.removeWater(this.sourceBlock);
				this.forming = true;
			}
		}
	}

	private void addWallBlock(final Block block) {
		if (this.frozen) {
			tempBlocks.put(block, new TempBlock(block, Material.ICE.createBlockData(), this));
		} else {
			tempBlocks.put(block, new TempBlock(block, Material.WATER.createBlockData(), this));
		}
	}

	@Override
	public void remove() {
		super.remove();
		this.returnWater();
		this.finalRemoveWater(this.sourceBlock);

		for (final Block block : WALL_BLOCKS.keySet()) {
			if (WALL_BLOCKS.get(block) == this.player) {
				this.finalRemoveWater(block);
			}
		}

	}

	private void removeWater(final Block block) {
		if (block != null) {
			if (AFFECTED_BLOCKS.containsKey(block)) {
				if (!GeneralMethods.isAdjacentToThreeOrMoreSources(block)) {
					TempBlock tb = tempBlocks.get(block);
					if (tb != null) {
						tb.revertBlock();
					}
				}
				AFFECTED_BLOCKS.remove(block);
			}
		}
	}

	private void finalRemoveWater(final Block block) {
		if (block != null) {
			if (AFFECTED_BLOCKS.containsKey(block)) {

				TempBlock tb = tempBlocks.get(block);
				if (tb != null) {
					tb.revertBlock();
				}

				AFFECTED_BLOCKS.remove(block);
			}
			if (WALL_BLOCKS.containsKey(block)) {
				TempBlock tb = tempBlocks.get(block);
				if (tb != null) {
					tb.revertBlock();
				}
				WALL_BLOCKS.remove(block);
			}
		}
	}

	private void addWater(final Block block) {
		if (RegionProtection.isRegionProtected(this, block.getLocation())) {
			return;
		} else if (!TempBlock.isTempBlock(block)) {
			tempBlocks.put(block, new TempBlock(block, Material.WATER));
			AFFECTED_BLOCKS.put(block, block);
		}
	}

	@Override
	public boolean allowBreakPlants() {
		return false;
	}

	//TODO Remove this method and use instance based methods instead of static ones
	public static void form(final Player player) {
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return;
		}

		final double range = WaterAbility.getNightFactor(player.getWorld()) * getConfig().getDouble("Abilities.Water.Surge.Wall.Range");
		SurgeWall wall = getAbility(player, SurgeWall.class);
		SurgeWave wave = getAbility(player, SurgeWave.class);

		if (wave != null) {
			if (wave.isProgressing() && !wave.isFreezing()) {
				// Freeze the wave.
				new SurgeWave(player);
			} else if (wave.isActivateFreeze()) {
				wave.remove();
				return;
			}
		}

		if (wall == null) {
			final Block source = BlockSource.getWaterSourceBlock(player, range, ClickType.SHIFT_DOWN, true, true, bPlayer.canPlantbend());

			if (wave == null && source == null && WaterReturn.hasWaterBottle(player)) {
				if (bPlayer.isOnCooldown("SurgeWall")) {
					return;
				}

				final Location eyeLoc = player.getEyeLocation();
				final Block block = eyeLoc.add(eyeLoc.getDirection().normalize()).getBlock();

				if (isTransparent(player, block) && isTransparent(player, eyeLoc.getBlock())) {
					final TempBlock tempBlock = new TempBlock(block, Material.WATER);
					SOURCE_BLOCKS.add(tempBlock);

					wall = new SurgeWall(player);
					wall.moveWater();

					if (!wall.progressing) {
						SOURCE_BLOCKS.remove(tempBlock);
						tempBlock.revertBlock();
						wall.remove();
					} else {
						WaterReturn.emptyWaterBottle(player);
					}

					SOURCE_BLOCKS.remove(tempBlock);
					tempBlock.revertBlock();
					return;
				}
			}

			// If SurgeWall isn't being created, then try to source SurgeWave.
			if (!bPlayer.isOnCooldown("SurgeWave")) {
				wave = new SurgeWave(player);
			}
			return;
		} else {
			if (isWaterbendable(player, null, player.getTargetBlock((HashSet<Material>) null, Math.min(1, (int)range)))) {
				wave = new SurgeWave(player);
				return;
			}
		}

		if (wall != null) {
			wall.moveWater();
		}
	}

	public static void removeAllCleanup() {
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
		final SurgeWall wall = getAbility(player, SurgeWall.class);
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
		if (this.location != null) {
			if (this.frozen) {
				this.thaw();
			}
			new WaterReturn(this.player, this.location.getBlock());
		}
	}

	@Override
	public String getName() {
		return "Surge";
	}

	@Override
	public Location getLocation() {
		if (this.location != null) {
			return this.location;
		} else if (this.sourceBlock != null) {
			return this.sourceBlock.getLocation();
		}
		return this.player != null ? this.player.getLocation() : null;
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
	public List<Location> getLocations() {
		return this.locations;
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

	public boolean isFrozen() {
		return this.frozen;
	}

	public void setFrozen(final boolean frozen) {
		this.frozen = frozen;
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

	@Override
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

	public static Map<Block, Block> getAffectedBlocks() {
		return AFFECTED_BLOCKS;
	}

	public static Map<Block, Player> getWallBlocks() {
		return WALL_BLOCKS;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

}

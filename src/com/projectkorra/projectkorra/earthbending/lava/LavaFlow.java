package com.projectkorra.projectkorra.earthbending.lava;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.LavaAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.configuration.configs.abilities.earth.LavaFlowConfig;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.Information;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

@SuppressWarnings("deprecation")
public class LavaFlow extends LavaAbility<LavaFlowConfig> {

	public static enum AbilityType {
		SHIFT, CLICK
	}

	private static final Map<Block, TempBlock> TEMP_LAVA_BLOCKS = new ConcurrentHashMap<>();
	private static final Map<Block, TempBlock> TEMP_LAND_BLOCKS = new ConcurrentHashMap<>();
	private static final Map<Block, TempBlock> TEMP_AIR_BLOCKS = new ConcurrentHashMap<>();

	private boolean removing;
	private boolean makeLava;
	private boolean clickIsFinished;
	private boolean shiftIsFinished;
	private boolean allowNaturalFlow;
	private int shiftCounter;
	private int upwardFlow;
	private int downwardFlow;
	private long time;
	private long clickLavaDelay;
	private long clickLandDelay;
	@Attribute("Click" + Attribute.COOLDOWN)
	private long clickLavaCooldown;
	private long clickLandCooldown;
	@Attribute("Shift" + Attribute.COOLDOWN)
	private long shiftCooldown;
	private long clickLavaCleanupDelay;
	private long clickLandCleanupDelay;
	private double particleDensity;
	private double particleOffset;
	private double currentRadius;
	private double shiftPlatformRadius;
	@Attribute("Shift" + Attribute.RADIUS)
	private double shiftMaxRadius;
	@Attribute("Shift" + Attribute.SPEED)
	private double shiftFlowSpeed;
	private double shiftRemoveSpeed;
	private double shiftRemoveDelay;
	@Attribute(Attribute.RANGE)
	private double clickRange;
	@Attribute("Click" + Attribute.RADIUS)
	private double clickLavaRadius;
	private double clickLandRadius;
	@Attribute("ClickLava" + Attribute.SPEED)
	private double lavaCreateSpeed;
	@Attribute("ClickLand" + Attribute.SPEED)
	private double landCreateSpeed;
	private AbilityType type;
	private Location origin;
	private ArrayList<TempBlock> affectedBlocks;
	private ArrayList<BukkitRunnable> tasks;
	private Material revertMaterial;
	private World world;

	/**
	 * Creates a new LavaFlow ability and initializes all of the variables and
	 * cooldowns. The ability is not guaranteed to continue, it may be the case
	 * that the player doesn't have the correct permissions to bend this
	 * ability.
	 *
	 * @param player the player that bended the ability
	 * @param type either shift or sneak
	 */
	public LavaFlow(final LavaFlowConfig config, final Player player, final AbilityType type) {
		super(config, player);
		if (!this.bPlayer.canLavabend()) {
			return;
		}

		this.world = player.getWorld();
		this.time = System.currentTimeMillis();
		this.type = type;
		this.shiftCounter = 0;
		this.currentRadius = 0;
		this.particleOffset = 3;
		this.removing = false;
		this.makeLava = true;
		this.clickIsFinished = false;
		this.affectedBlocks = new ArrayList<TempBlock>();
		this.tasks = new ArrayList<BukkitRunnable>();
		this.revertMaterial = config.RevertMaterial;

		this.shiftCooldown = config.ShiftCooldown;
		this.shiftPlatformRadius = config.ShiftPlatformRadius;
		this.shiftMaxRadius = config.ShiftRadius;
		this.shiftFlowSpeed = config.ShiftFlowSpeed;
		this.shiftRemoveSpeed = config.ShiftRemoveSpeed;
		this.shiftRemoveDelay = config.ShiftCleanupDelay;
		this.particleDensity = config.ParticleDensity;
		this.clickRange = config.ClickRange;
		this.clickLavaRadius = config.ClickRadius;
		this.clickLandRadius = config.ClickRadius;
		this.clickLavaDelay = config.ClickLavaStartDelay;
		this.clickLandDelay = config.ClickLandStartDelay;
		this.clickLavaCooldown = config.ClickLavaCooldown;
		this.clickLandCooldown = config.ClickLandCooldown;
		this.clickLavaCleanupDelay = config.ClickLavaCleanupDelay;
		this.clickLandCleanupDelay = config.ClickLandCleanupDelay;
		this.lavaCreateSpeed = config.ClickLavaCreateSpeed;
		this.landCreateSpeed = config.ClickLandCreateSpeed;
		this.upwardFlow = config.UpwardFlow;
		this.downwardFlow = config.DownwardFlow;
		this.allowNaturalFlow = config.AllowNaturalFlow;

		if (this.bPlayer.isAvatarState()) {
			this.shiftCooldown = config.AvatarState_ShiftCooldown;
			this.clickLavaCooldown = config.AvatarState_ClickLavaCooldown;
			this.clickLandCooldown = config.AvatarState_ClickLandCooldown;
			this.shiftPlatformRadius = config.AvatarState_ShiftPlatformRadius;
			this.clickLavaRadius = config.AvatarState_ClickRadius;
			this.shiftMaxRadius = config.AvatarState_ShiftRadius;
		}

		if (type == AbilityType.SHIFT) {
			// Update the shift counter for all the player's LavaFlows.
			final ArrayList<LavaFlow> shiftFlows = LavaFlow.getLavaFlow(player, LavaFlow.AbilityType.SHIFT);
			if (shiftFlows.size() > 0 && !player.isSneaking()) {
				for (final LavaFlow lavaFlow : shiftFlows) {
					lavaFlow.shiftCounter++;
				}
			}

			if (this.bPlayer.isOnCooldown("LavaFlow")) {
				this.removeSlowly();
				return;
			}
			this.start();
		} else if (type == AbilityType.CLICK) {
			final Block sourceBlock = BlockSource.getEarthOrLavaSourceBlock(player, this.clickRange, ClickType.LEFT_CLICK);
			if (sourceBlock == null) {
				this.removeSlowly();
				return;
			}

			final long cooldown = this.makeLava ? this.clickLavaCooldown : this.clickLandCooldown;
			this.origin = sourceBlock.getLocation();
			this.makeLava = !isLava(sourceBlock);
			if (this.bPlayer.isOnCooldown("LavaFlow")) {
				this.removeSlowly();
				return;
			} else {
				this.bPlayer.addCooldown("LavaFlow", cooldown);
			}
			this.start();
		}
	}

	/**
	 * Progresses LavaFlow by 1 tick. This is the heart of the ability, it
	 * determines whether or not the LavaFlow type is Click/Sneaking, and it
	 * will remove the ability if any issues arise.
	 */
	@Override
	public void progress() {
		if (this.shiftCounter > 0 && this.type == AbilityType.SHIFT) {
			this.removeSlowly();
			return;
		} else if (this.removing) {
			return;
		} else if (this.player.isDead() || !this.player.isOnline() || this.player.getWorld() != this.world) {
			this.removeSlowly();
			return;
		}

		final Random random = new Random();

		if (this.type == AbilityType.SHIFT) {
			if (System.currentTimeMillis() - this.time > this.shiftRemoveDelay) {
				this.removeSlowly();
				return;
			}
			if (!this.player.isSneaking() && !this.removing) {
				if (this.affectedBlocks.size() > 0) {
					this.removeOnDelay();
					this.removing = true;
					this.bPlayer.addCooldown("LavaFlow", this.shiftCooldown);
				} else {
					this.removeSlowly();
				}
				return;
			}

			if (!this.bPlayer.canBendIgnoreCooldowns(this)) {
				this.removeSlowly();
				return;
			} else if (this.origin == null) {
				this.origin = this.player.getLocation().clone().add(0, -1, 0);
				if (!this.isEarthbendable(this.origin.getBlock()) && this.origin.getBlock().getType() != Material.GLOWSTONE) {
					this.removeSlowly();
					return;
				}
			}

			for (double x = -this.currentRadius; x <= this.currentRadius + this.particleOffset; x++) {
				for (double z = -this.currentRadius; z < this.currentRadius + this.particleOffset; z++) {
					final Location loc = this.origin.clone().add(x, 0, z);
					final Block block = GeneralMethods.getTopBlock(loc, this.upwardFlow, this.downwardFlow);
					if (block == null) {
						continue;
					}

					final double dSquared = distanceSquaredXZ(block.getLocation(), this.origin);
					if (dSquared > Math.pow(this.shiftPlatformRadius, 2)) {
						if (dSquared < Math.pow(this.currentRadius, 2) && !GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
							if (dSquared < this.shiftPlatformRadius * 4 || this.getAdjacentLavaBlocks(block.getLocation()).size() > 0) {
								if (!isLava(block)) {
									if (isPlant(block) || isSnow(block)) {
										final Block lower = block.getRelative(BlockFace.DOWN);
										if (isPlant(lower) || isSnow(lower)) {
											final Block lower2 = lower.getRelative(BlockFace.DOWN);
											if (!isEarth(lower2) && !isSand(lower2) && !isMetal(lower2)) {
												continue;
											}
											this.createLava(lower2);
										} else {
											if (!isEarth(lower) && !isSand(lower) && !isMetal(lower)) {
												continue;
											}
											this.createLava(lower);
										}
									} else {
										if (!isEarth(block) && !isSand(block) && !isMetal(block)) {
											continue;
										}
										this.createLava(block);
									}
								}

							}
						} else if (Math.random() < this.particleDensity && dSquared < Math.pow(this.currentRadius + this.particleDensity, 2) && this.currentRadius + this.particleDensity < this.shiftMaxRadius && random.nextInt(3) == 0) {
							ParticleEffect.LAVA.display(loc, 1, Math.random(), Math.random(), Math.random());
						}
					}
				}

				if (!this.shiftIsFinished) {
					if (random.nextInt(10) == 0) {
						ParticleEffect.LAVA.display(this.player.getLocation(), 1, Math.random(), Math.random(), Math.random());
					}
				}

				this.currentRadius += this.shiftFlowSpeed;
				if (this.currentRadius > this.shiftMaxRadius) {
					this.currentRadius = this.shiftMaxRadius;
					this.shiftIsFinished = true;
				}
			}
		} else if (this.type == AbilityType.CLICK) {
			/*
			 * The variable makeLava refers to whether or not the ability is
			 * trying to remove land in place of lava or if makeLava = false
			 * then lava is being replaced with land.
			 *
			 * Notice we have separate variables between both versions, because
			 * most of the time making lava will have longer delays and longer
			 * cooldowns.
			 */
			final long curTime = System.currentTimeMillis() - this.time;
			final double delay = this.makeLava ? this.clickLavaDelay : this.clickLandDelay;

			if (this.makeLava && curTime > this.clickLavaCleanupDelay) {
				this.makeLava = false;
				this.removeSlowly();
				return;
			} else if (!this.makeLava && curTime > this.clickLandCleanupDelay) {
				this.removeSlowly();
				return;
			} else if (!this.makeLava && curTime < delay) {
				return;
			} else if (this.makeLava && curTime < delay) {
				for (double x = -this.clickLavaRadius; x <= this.clickLavaRadius; x++) {
					for (double z = -this.clickLavaRadius; z <= this.clickLavaRadius; z++) {
						final Location loc = this.origin.clone().add(x, 0, z);
						final Block tempBlock = GeneralMethods.getTopBlock(loc, this.upwardFlow, this.downwardFlow);
						if (!isWater(tempBlock)) {
							if (tempBlock != null && !isLava(tempBlock) && Math.random() < this.particleDensity && tempBlock.getLocation().distanceSquared(this.origin) <= Math.pow(this.clickLavaRadius, 2)) {
								if (random.nextInt(5) == 0) {
									ParticleEffect.LAVA.display(loc, 1, Math.random(), Math.random(), Math.random());
								}
							}
						}
					}
				}
				return;
			}

			/*
			 * Start to create all of the lava, if all of the lava has been
			 * created then we are finished with this instance of LavaFlow, but
			 * we need to keep it running so that we can revert the blocks.
			 */
			if (!this.clickIsFinished) {
				this.clickIsFinished = true;
				final double radius = this.makeLava ? this.clickLavaRadius : this.clickLandRadius;

				for (double x = -radius; x <= radius; x++) {
					for (double z = -radius; z <= radius; z++) {
						final Location loc = this.origin.clone().add(x, 0, z);
						final Block tempBlock = GeneralMethods.getTopBlock(loc, this.upwardFlow, this.downwardFlow);

						final double dSquared = distanceSquaredXZ(tempBlock.getLocation(), this.origin);
						if (dSquared < Math.pow(radius, 2) && !GeneralMethods.isRegionProtectedFromBuild(this, loc)) {
							if (this.makeLava && !isLava(tempBlock)) {
								this.clickIsFinished = false;
								if (Math.random() < this.lavaCreateSpeed) {
									if (!isLava(tempBlock) || isSnow(tempBlock)) {
										if (isPlant(tempBlock) || isSnow(tempBlock)) {
											final Block lower = tempBlock.getRelative(BlockFace.DOWN);
											if (isPlant(lower) || isSnow(lower)) {
												final Block lower2 = lower.getRelative(BlockFace.DOWN);
												if (!isEarth(lower2) && !isSand(lower2) && !isMetal(lower2)) {
													continue;
												}
												this.createLava(lower2);
											} else {
												if (!isEarth(lower) && !isSand(lower) && !isMetal(lower)) {
													continue;
												}
												this.createLava(lower);
											}
										} else {
											if (!isEarth(tempBlock) && !isSand(tempBlock) && !isMetal(tempBlock)) {
												continue;
											}
											this.createLava(tempBlock);
										}
									}
								} else {
									if (random.nextInt(4) == 0) {
										final Block block = loc.getBlock();
										final Block above = block.getRelative(BlockFace.UP);

										if ((isEarth(block) || isSand(block) || isMetal(block)) && !isWater(above)) {
											ParticleEffect.LAVA.display(loc, 1, Math.random(), Math.random(), Math.random(), 0);
										}
									}
								}
							} else if (!this.makeLava && isLava(tempBlock)) {
								this.clickIsFinished = false;
								if (Math.random() < this.landCreateSpeed) {
									this.removeLava(tempBlock);
								}
							}
						}
					}
				}
				return;
			}
		}
	}

	/**
	 * Creates a LavaBlock and appends the TempBlock to our arraylist called
	 * TEMP_LAVA_BLOCKS.
	 *
	 * If ALLOW_NATURAL_FLOW is turned on then this method will remove the block
	 * from TempBlock.instances, which will allow the lava to flow naturally.
	 *
	 * @param block the block that will be turned to lava
	 */
	public void createLava(final Block block) {
		if (isEarth(block) || isSand(block) || isMetal(block)) {
			if (EarthAbility.getMovedEarth().containsKey(block)) {
				final Information info = EarthAbility.getMovedEarth().get(block);
				if (!info.getBlock().equals(block)) {
					return;
				}
			}
			if (isPlant(block.getRelative(BlockFace.UP)) || isSnow(block.getRelative(BlockFace.UP))) {
				final Block above = block.getRelative(BlockFace.UP);
				final Block above2 = above.getRelative(BlockFace.UP);
				if (isPlant(above) || isSnow(above)) {
					final TempBlock tb = new TempBlock(above, Material.AIR);
					TEMP_AIR_BLOCKS.put(above, tb);
					this.affectedBlocks.add(tb);
					if (isPlant(above2) && above2.getType().equals(Material.TALL_GRASS)) {
						final TempBlock tb2 = new TempBlock(above2, Material.AIR);
						TEMP_AIR_BLOCKS.put(above2, tb2);
						this.affectedBlocks.add(tb);
					}
				} else {
					return;
				}
			}
			TempBlock tblock = null;
			if (this.allowNaturalFlow) {
				block.setType(Material.LAVA);
				block.setBlockData(GeneralMethods.getLavaData(0));
			} else {
				tblock = new TempBlock(block, Material.LAVA, GeneralMethods.getLavaData(0));
			}

			if (tblock != null) {
				TEMP_LAVA_BLOCKS.put(block, tblock);
				this.affectedBlocks.add(tblock);
			}
		}
	}

	/**
	 * Removes a lava block if it is inside of our ArrayList of TempBlocks.
	 *
	 * @param testBlock the block to attempt to remove
	 */
	public void removeLava(final Block testBlock) {
		if (TEMP_LAVA_BLOCKS.containsKey(testBlock)) {
			final TempBlock tb = TEMP_LAVA_BLOCKS.get(testBlock);
			tb.revertBlock();
			TEMP_LAVA_BLOCKS.remove(testBlock);
			this.affectedBlocks.remove(tb);
			return;
		}

		final TempBlock tblock = new TempBlock(testBlock, this.revertMaterial);
		this.affectedBlocks.add(tblock);
		TEMP_LAND_BLOCKS.put(testBlock, tblock);
	}

	public static boolean isLavaFlowBlock(final Block block) {
		return isLavaFlowBlock(TEMP_AIR_BLOCKS, block) || isLavaFlowBlock(TEMP_LAND_BLOCKS, block) || isLavaFlowBlock(TEMP_LAVA_BLOCKS, block);
	}

	private static boolean isLavaFlowBlock(final Map<Block, TempBlock> map, final Block block) {
		return map.containsKey(block);
	}

	public static boolean removeBlock(final Block block) {
		return removeBlock(TEMP_AIR_BLOCKS, block) || removeBlock(TEMP_LAND_BLOCKS, block) || removeBlock(TEMP_LAVA_BLOCKS, block);
	}

	private static boolean removeBlock(final Map<Block, TempBlock> map, final Block block) {
		if (map.containsKey(block)) {
			final TempBlock tb = map.get(block);
			map.remove(block);

			for (final LavaFlow lavaflow : CoreAbility.getAbilities(LavaFlow.class)) {
				lavaflow.getAffectedBlocks().remove(tb);
			}

			tb.revertBlock();

			return true;
		}

		return false;
	}

	/**
	 * Causes this instance of LavaFlow to remove() after a specified amount of
	 * time. This is useful for causing the Shift version of the ability to
	 * automatically clean up over time.
	 */
	public void removeOnDelay() {
		final BukkitRunnable br = new BukkitRunnable() {
			@Override
			public void run() {
				LavaFlow.this.removeSlowly();
			}
		};
		br.runTaskLater(ProjectKorra.plugin, (long) (this.shiftRemoveDelay / 1000.0 * 20.0));
		this.tasks.add(br);
	}

	/**
	 * Removes this instance of LavaFlow, cleans up any blocks that are
	 * remaining in TEMP_LAVA_BLOCKS, and cancels any remaining tasks.
	 *
	 * This version of remove will create tasks that remove each lava block with
	 * an animation.
	 */
	public void removeSlowly() {
		super.remove();
		for (int i = this.affectedBlocks.size() - 1; i > -1; i--) {
			final TempBlock tblock = this.affectedBlocks.get(i);
			final boolean isTempAir = TEMP_AIR_BLOCKS.values().contains(tblock);

			new BukkitRunnable() {

				@Override
				public void run() {
					tblock.revertBlock();

					if (TEMP_LAVA_BLOCKS.values().contains(tblock)) {
						LavaFlow.this.affectedBlocks.remove(tblock);
						TEMP_LAVA_BLOCKS.remove(tblock.getBlock());
					}
					if (TEMP_LAND_BLOCKS.values().contains(tblock)) {
						LavaFlow.this.affectedBlocks.remove(tblock);
						TEMP_LAND_BLOCKS.remove(tblock.getBlock());
					}
					if (TEMP_AIR_BLOCKS.values().contains(tblock)) {
						LavaFlow.this.affectedBlocks.remove(tblock);
						TEMP_AIR_BLOCKS.remove(tblock.getBlock());
					}

					if (isTempAir && tblock.getState().getType() == Material.TALL_GRASS) {
						tblock.getBlock().getRelative(BlockFace.UP).setType(Material.TALL_GRASS);
					}
				}
			}.runTaskLater(ProjectKorra.plugin, (long) (i / this.shiftRemoveSpeed));
		}

		for (final BukkitRunnable task : this.tasks) {
			task.cancel();
		}
	}

	/**
	 * Removes this ability instance instantly. This method does not cause any
	 * block animation, it just removes everything.
	 */
	@Override
	public void remove() {
		super.remove();
		for (int i = this.affectedBlocks.size() - 1; i > -1; i--) {
			final TempBlock tblock = this.affectedBlocks.get(i);
			tblock.revertBlock();
			if (TEMP_LAVA_BLOCKS.values().contains(tblock)) {
				this.affectedBlocks.remove(tblock);
				TEMP_LAVA_BLOCKS.remove(tblock.getBlock());
			}
			if (TEMP_LAND_BLOCKS.values().contains(tblock)) {
				this.affectedBlocks.remove(tblock);
				TEMP_LAND_BLOCKS.remove(tblock.getBlock());
			}
		}

		for (final BukkitRunnable task : this.tasks) {
			task.cancel();
		}
	}

	/**
	 * Returns a list of all the Lava blocks that are adjacent to the block at
	 * loc.
	 *
	 * @param loc the middle location of the adjacent blocks
	 * @return a list of the adjacent blocks
	 */
	public ArrayList<Block> getAdjacentLavaBlocks(final Location loc) {
		final ArrayList<Block> list = getAdjacentBlocks(loc);
		for (int i = 0; i < list.size(); i++) {
			final Block block = list.get(i);
			if (!isLava(block)) {
				list.remove(i);
				i--;
			}
		}
		return list;
	}

	/**
	 * Returns an ArrayList of all the surrounding blocks for loc, but it
	 * excludes the block that is contained at Loc.
	 *
	 * @param loc the middle block location
	 * @return a list of adjacent blocks
	 */
	public static ArrayList<Block> getAdjacentBlocks(final Location loc) {
		final ArrayList<Block> list = new ArrayList<Block>();
		final Block block = loc.getBlock();

		for (int x = -1; x <= 1; x++) {
			for (int y = -2; y <= 1; y++) {
				for (int z = -1; z <= 1; z++) {
					if (!(x == 0 && y == 0 && z == 0)) {
						list.add(block.getLocation().add(x, y, z).getBlock());
					}
				}
			}
		}
		return list;
	}

	/**
	 * Gets the distance between 2 locations but ignores their Y values. This
	 * was useful in allowing the flow of lava to look more natural and not be
	 * substantially shortened by the Y distance if it is flowing upward or
	 * downward.
	 *
	 * @param l1 the first location
	 * @param l2 the second location
	 * @return the distance squared between l1 and l2
	 */
	public static double distanceSquaredXZ(final Location l1, final Location l2) {
		final Location temp1 = l1.clone();
		final Location temp2 = l2.clone();
		temp1.setY(0);
		temp2.setY(0);
		return temp1.distanceSquared(temp2);
	}

	/**
	 * Returns all of the LavaFlows created by a specific player but filters the
	 * abilities based on shift or click.
	 *
	 * @param player the player that created the ability instance
	 * @param type the specific type of ability we are looking for
	 * @return a list of all the LavaFlow instances
	 */
	public static ArrayList<LavaFlow> getLavaFlow(final Player player, final AbilityType type) {
		final ArrayList<LavaFlow> list = new ArrayList<LavaFlow>();
		for (final LavaFlow lf : getAbilities(LavaFlow.class)) {
			if (lf.player != null && lf.player == player && lf.type != null && lf.type == type) {
				list.add(lf);
			}
		}
		return list;
	}

	public static Material getRevertMaterial() {
		Material m = Material.STONE;
		final LavaFlow lf = (LavaFlow) CoreAbility.getAbility("LavaFlow");
		m = lf.revertMaterial;
		return m;
	}

	public static Map<Block, TempBlock> getTempLandBlocks() {
		return TEMP_LAND_BLOCKS;
	}

	public static Map<Block, TempBlock> getTempLavaBlocks() {
		return TEMP_LAVA_BLOCKS;
	}

	@Override
	public String getName() {
		return "LavaFlow";
	}

	@Override
	public Location getLocation() {
		if (this.origin != null) {
			return this.origin;
		} else if (this.player != null) {
			return this.player.getLocation();
		}
		return null;
	}

	@Override
	public long getCooldown() {
		return this.type == AbilityType.CLICK ? this.clickLandCooldown : this.shiftCooldown;
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
		final ArrayList<Location> locations = new ArrayList<>();
		for (final TempBlock tblock : this.affectedBlocks) {
			locations.add(tblock.getLocation());
		}
		return locations;
	}

	public boolean isRemoving() {
		return this.removing;
	}

	public void setRemoving(final boolean removing) {
		this.removing = removing;
	}

	public boolean isMakeLava() {
		return this.makeLava;
	}

	public void setMakeLava(final boolean makeLava) {
		this.makeLava = makeLava;
	}

	public boolean isClickIsFinished() {
		return this.clickIsFinished;
	}

	public void setClickIsFinished(final boolean clickIsFinished) {
		this.clickIsFinished = clickIsFinished;
	}

	public boolean isShiftIsFinished() {
		return this.shiftIsFinished;
	}

	public void setShiftIsFinished(final boolean shiftIsFinished) {
		this.shiftIsFinished = shiftIsFinished;
	}

	public boolean isAllowNaturalFlow() {
		return this.allowNaturalFlow;
	}

	public void setAllowNaturalFlow(final boolean allowNaturalFlow) {
		this.allowNaturalFlow = allowNaturalFlow;
	}

	public int getShiftCounter() {
		return this.shiftCounter;
	}

	public void setShiftCounter(final int shiftCounter) {
		this.shiftCounter = shiftCounter;
	}

	public int getUpwardFlow() {
		return this.upwardFlow;
	}

	public void setUpwardFlow(final int upwardFlow) {
		this.upwardFlow = upwardFlow;
	}

	public int getDownwardFlow() {
		return this.downwardFlow;
	}

	public void setDownwardFlow(final int downwardFlow) {
		this.downwardFlow = downwardFlow;
	}

	public long getTime() {
		return this.time;
	}

	public void setTime(final long time) {
		this.time = time;
	}

	public long getClickLavaDelay() {
		return this.clickLavaDelay;
	}

	public void setClickLavaDelay(final long clickLavaDelay) {
		this.clickLavaDelay = clickLavaDelay;
	}

	public long getClickLandDelay() {
		return this.clickLandDelay;
	}

	public void setClickLandDelay(final long clickLandDelay) {
		this.clickLandDelay = clickLandDelay;
	}

	public long getClickLavaCooldown() {
		return this.clickLavaCooldown;
	}

	public void setClickLavaCooldown(final long clickLavaCooldown) {
		this.clickLavaCooldown = clickLavaCooldown;
	}

	public long getClickLandCooldown() {
		return this.clickLandCooldown;
	}

	public void setClickLandCooldown(final long clickLandCooldown) {
		this.clickLandCooldown = clickLandCooldown;
	}

	public long getShiftCooldown() {
		return this.shiftCooldown;
	}

	public void setShiftCooldown(final long shiftCooldown) {
		this.shiftCooldown = shiftCooldown;
	}

	public long getClickLavaCleanupDelay() {
		return this.clickLavaCleanupDelay;
	}

	public void setClickLavaCleanupDelay(final long clickLavaCleanupDelay) {
		this.clickLavaCleanupDelay = clickLavaCleanupDelay;
	}

	public long getClickLandCleanupDelay() {
		return this.clickLandCleanupDelay;
	}

	public void setClickLandCleanupDelay(final long clickLandCleanupDelay) {
		this.clickLandCleanupDelay = clickLandCleanupDelay;
	}

	public double getParticleDensity() {
		return this.particleDensity;
	}

	public void setParticleDensity(final double particleDensity) {
		this.particleDensity = particleDensity;
	}

	public double getParticleOffset() {
		return this.particleOffset;
	}

	public void setParticleOffset(final double particleOffset) {
		this.particleOffset = particleOffset;
	}

	public double getCurrentRadius() {
		return this.currentRadius;
	}

	public void setCurrentRadius(final double currentRadius) {
		this.currentRadius = currentRadius;
	}

	public double getShiftPlatformRadius() {
		return this.shiftPlatformRadius;
	}

	public void setShiftPlatformRadius(final double shiftPlatformRadius) {
		this.shiftPlatformRadius = shiftPlatformRadius;
	}

	public double getShiftMaxRadius() {
		return this.shiftMaxRadius;
	}

	public void setShiftMaxRadius(final double shiftMaxRadius) {
		this.shiftMaxRadius = shiftMaxRadius;
	}

	public double getShiftFlowSpeed() {
		return this.shiftFlowSpeed;
	}

	public void setShiftFlowSpeed(final double shiftFlowSpeed) {
		this.shiftFlowSpeed = shiftFlowSpeed;
	}

	public double getShiftRemoveSpeed() {
		return this.shiftRemoveSpeed;
	}

	public void setShiftRemoveSpeed(final double shiftRemoveSpeed) {
		this.shiftRemoveSpeed = shiftRemoveSpeed;
	}

	public double getShiftRemoveDelay() {
		return this.shiftRemoveDelay;
	}

	public void setShiftRemoveDelay(final double shiftRemoveDelay) {
		this.shiftRemoveDelay = shiftRemoveDelay;
	}

	public double getClickRange() {
		return this.clickRange;
	}

	public void setClickRange(final double clickRange) {
		this.clickRange = clickRange;
	}

	public double getClickLavaRadius() {
		return this.clickLavaRadius;
	}

	public void setClickLavaRadius(final double clickLavaRadius) {
		this.clickLavaRadius = clickLavaRadius;
	}

	public double getClickLandRadius() {
		return this.clickLandRadius;
	}

	public void setClickLandRadius(final double clickLandRadius) {
		this.clickLandRadius = clickLandRadius;
	}

	public double getLavaCreateSpeed() {
		return this.lavaCreateSpeed;
	}

	public void setLavaCreateSpeed(final double lavaCreateSpeed) {
		this.lavaCreateSpeed = lavaCreateSpeed;
	}

	public double getLandCreateSpeed() {
		return this.landCreateSpeed;
	}

	public void setLandCreateSpeed(final double landCreateSpeed) {
		this.landCreateSpeed = landCreateSpeed;
	}

	public AbilityType getType() {
		return this.type;
	}

	public void setType(final AbilityType type) {
		this.type = type;
	}

	public Location getOrigin() {
		return this.origin;
	}

	public void setOrigin(final Location origin) {
		this.origin = origin;
	}

	public ArrayList<TempBlock> getAffectedBlocks() {
		return this.affectedBlocks;
	}

	public ArrayList<BukkitRunnable> getTasks() {
		return this.tasks;
	}
	
	@Override
	public Class<LavaFlowConfig> getConfigType() {
		return LavaFlowConfig.class;
	}

}

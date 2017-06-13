package com.projectkorra.projectkorra.earthbending.lava;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.World;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.LavaAbility;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

public class LavaFlow extends LavaAbility {

	public static enum AbilityType {
		SHIFT, CLICK
	}

	private static final Map<Block, TempBlock> TEMP_LAVA_BLOCKS = new ConcurrentHashMap<>();
	public static final Map<TempBlock, Block> TEMP_LAVA_BLOCKS_BY_TEMPBLOCK = new ConcurrentHashMap<>();
	private static final Map<Block, TempBlock> TEMP_LAND_BLOCKS = new ConcurrentHashMap<>();
	private static final Map<TempBlock, Block> TEMP_LAND_BLOCKS_BY_TEMPBLOCK = new ConcurrentHashMap<>();
	private static final Map<Block, TempBlock> TEMP_AIR_BLOCKS = new ConcurrentHashMap<>();
	private static final Map<TempBlock, Block> TEMP_AIR_BLOCKS_BY_TEMPBLOCK = new ConcurrentHashMap<>();

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
	private long clickLavaCooldown;
	private long clickLandCooldown;
	private long shiftCooldown;
	private long clickLavaCleanupDelay;
	private long clickLandCleanupDelay;
	private double particleDensity;
	private double particleOffset;
	private double currentRadius;
	private double shiftPlatformRadius;
	private double shiftMaxRadius;
	private double shiftFlowSpeed;
	private double shiftRemoveSpeed;
	private double shiftRemoveDelay;
	private double clickRange;
	private double clickLavaRadius;
	private double clickLandRadius;
	private double lavaCreateSpeed;
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
	public LavaFlow(Player player, AbilityType type) {
		super(player);
		if (!bPlayer.canLavabend()) {
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
		this.revertMaterial = Material.getMaterial(getConfig().getString("Abilities.Earth.LavaFlow.RevertMaterial"));

		this.shiftCooldown = getConfig().getLong("Abilities.Earth.LavaFlow.ShiftCooldown");
		this.shiftPlatformRadius = getConfig().getDouble("Abilities.Earth.LavaFlow.ShiftPlatformRadius");
		this.shiftMaxRadius = getConfig().getDouble("Abilities.Earth.LavaFlow.ShiftRadius");
		this.shiftFlowSpeed = getConfig().getDouble("Abilities.Earth.LavaFlow.ShiftFlowSpeed");
		this.shiftRemoveSpeed = getConfig().getDouble("Abilities.Earth.LavaFlow.ShiftRemoveSpeed");
		this.shiftRemoveDelay = getConfig().getLong("Abilities.Earth.LavaFlow.ShiftCleanupDelay");
		this.particleDensity = getConfig().getDouble("Abilities.Earth.LavaFlow.ParticleDensity");
		this.clickRange = getConfig().getDouble("Abilities.Earth.LavaFlow.ClickRange");
		this.clickLavaRadius = getConfig().getDouble("Abilities.Earth.LavaFlow.ClickRadius");
		this.clickLandRadius = getConfig().getDouble("Abilities.Earth.LavaFlow.ClickRadius");
		this.clickLavaDelay = getConfig().getLong("Abilities.Earth.LavaFlow.ClickLavaStartDelay");
		this.clickLandDelay = getConfig().getLong("Abilities.Earth.LavaFlow.ClickLandStartDelay");
		this.clickLavaCooldown = getConfig().getLong("Abilities.Earth.LavaFlow.ClickLavaCooldown");
		this.clickLandCooldown = getConfig().getLong("Abilities.Earth.LavaFlow.ClickLandCooldown");
		this.clickLavaCleanupDelay = getConfig().getLong("Abilities.Earth.LavaFlow.ClickLavaCleanupDelay");
		this.clickLandCleanupDelay = getConfig().getLong("Abilities.Earth.LavaFlow.ClickLandCleanupDelay");
		this.lavaCreateSpeed = getConfig().getDouble("Abilities.Earth.LavaFlow.ClickLavaCreateSpeed");
		this.landCreateSpeed = getConfig().getDouble("Abilities.Earth.LavaFlow.ClickLandCreateSpeed");
		this.upwardFlow = getConfig().getInt("Abilities.Earth.LavaFlow.UpwardFlow");
		this.downwardFlow = getConfig().getInt("Abilities.Earth.LavaFlow.DownwardFlow");
		this.allowNaturalFlow = getConfig().getBoolean("Abilities.Earth.LavaFlow.AllowNaturalFlow");

		if (bPlayer.isAvatarState()) {
			shiftCooldown = getConfig().getLong("Abilities.Avatar.AvatarState.Earth.LavaFlow.ShiftCooldown");
			clickLavaCooldown = getConfig().getLong("Abilities.Avatar.AvatarState.Earth.LavaFlow.ClickLavaCooldown");
			clickLandCooldown = getConfig().getLong("Abilities.Avatar.AvatarState.Earth.LavaFlow.ClickLandCooldown");
			shiftPlatformRadius = getConfig().getDouble("Abilities.Avatar.AvatarState.Earth.LavaFlow.ShiftPlatformRadius");
			clickLavaRadius = getConfig().getDouble("Abilities.Avatar.AvatarState.Earth.LavaFlow.ClickRadius");
			shiftMaxRadius = getConfig().getDouble("Abilities.Avatar.AvatarState.Earth.LavaFlow.ShiftRadius");
		}

		if (type == AbilityType.SHIFT) {
			// Update the shift counter for all the player's LavaFlows
			ArrayList<LavaFlow> shiftFlows = LavaFlow.getLavaFlow(player, LavaFlow.AbilityType.SHIFT);
			if (shiftFlows.size() > 0 && !player.isSneaking()) {
				for (LavaFlow lavaFlow : shiftFlows) {
					lavaFlow.shiftCounter++;
				}
			}

			if (bPlayer.isOnCooldown("LavaFlow")) {
				removeSlowly();
				return;
			}
			start();
		} else if (type == AbilityType.CLICK) {
			Block sourceBlock = BlockSource.getEarthOrLavaSourceBlock(player, clickRange, ClickType.LEFT_CLICK);
			if (sourceBlock == null) {
				removeSlowly();
				return;
			}

			long cooldown = makeLava ? clickLavaCooldown : clickLandCooldown;
			origin = sourceBlock.getLocation();
			makeLava = !isLava(sourceBlock);
			if (bPlayer.isOnCooldown("LavaFlow")) {
				removeSlowly();
				return;
			} else {
				bPlayer.addCooldown("LavaFlow", cooldown);
			}
			start();
		}
	}

	/**
	 * Progresses LavaFlow by 1 tick. This is the heart of the ability, it
	 * determines whether or not the LavaFlow type is Click/Sneaking, and it
	 * will remove the ability if any issues arise.
	 */
	@Override
	public void progress() {
		if (shiftCounter > 0 && type == AbilityType.SHIFT) {
			removeSlowly();
			return;
		} else if (removing) {
			return;
		} else if (player.isDead() || !player.isOnline() || player.getWorld() != this.world) {
			removeSlowly();
			return;
		}

		Random random = new Random();

		if (type == AbilityType.SHIFT) {
			if (System.currentTimeMillis() - time > shiftRemoveDelay) {
				removeSlowly();
				return;
			}
			if (!player.isSneaking() && !removing) {
				if (affectedBlocks.size() > 0) {
					removeOnDelay();
					removing = true;
					bPlayer.addCooldown("LavaFlow", shiftCooldown);
				} else {
					removeSlowly();
				}
				return;
			}

			if (!bPlayer.canBendIgnoreCooldowns(this)) {
				removeSlowly();
				return;
			} else if (origin == null) {
				origin = player.getLocation().clone().add(0, -1, 0);
				if (!isEarthbendable(origin.getBlock()) && origin.getBlock().getType() != Material.GLOWSTONE) {
					removeSlowly();
					return;
				}
			}

			for (double x = -currentRadius; x <= currentRadius + particleOffset; x++) {
				for (double z = -currentRadius; z < currentRadius + particleOffset; z++) {
					Location loc = origin.clone().add(x, 0, z);
					Block block = GeneralMethods.getTopBlock(loc, upwardFlow, downwardFlow);
					if (block == null) {
						continue;
					}

					double dSquared = distanceSquaredXZ(block.getLocation(), origin);
					if (dSquared > Math.pow(shiftPlatformRadius, 2)) {
						if (dSquared < Math.pow(currentRadius, 2) && !GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
							if (dSquared < shiftPlatformRadius * 4 || getAdjacentLavaBlocks(block.getLocation()).size() > 0) {
								if (!isLava(block)) {
									if (isPlant(block) || isSnow(block)) {
										Block lower = block.getRelative(BlockFace.DOWN);
										if (isPlant(lower) || isSnow(lower)) {
											Block lower2 = lower.getRelative(BlockFace.DOWN);
											if (!isEarth(lower2) && !isSand(lower2) && !isMetal(lower2)) {
												continue;
											}
											createLava(lower2);
										} else {
											if (!isEarth(lower) && !isSand(lower) && !isMetal(lower)) {
												continue;
											}
											createLava(lower);
										}
									} else {
										if (!isEarth(block) && !isSand(block) && !isMetal(block)) {
											continue;
										}
										createLava(block);
									}
								}

							}
						} else if (Math.random() < particleDensity && dSquared < Math.pow(currentRadius + particleDensity, 2) && currentRadius + particleDensity < shiftMaxRadius && random.nextInt(3) == 0) {
							ParticleEffect.LAVA.display(loc, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0, 1);
						}
					}
				}

				if (!shiftIsFinished) {
					if (random.nextInt(10) == 0) {
						ParticleEffect.LAVA.display(player.getLocation(), (float) Math.random(), (float) Math.random(), (float) Math.random(), 0, 1);
					}
				}

				currentRadius += shiftFlowSpeed;
				if (currentRadius > shiftMaxRadius) {
					currentRadius = shiftMaxRadius;
					shiftIsFinished = true;
				}
			}
		} else if (type == AbilityType.CLICK) {
			/*
			 * The variable makeLava refers to whether or not the ability is
			 * trying to remove land in place of lava or if makeLava = false
			 * then lava is being replaced with land.
			 * 
			 * Notice we have separate variables between both versions, because
			 * most of the time making lava will have longer delays and longer
			 * cooldowns.
			 */
			long curTime = System.currentTimeMillis() - time;
			double delay = makeLava ? clickLavaDelay : clickLandDelay;

			if (makeLava && curTime > clickLavaCleanupDelay) {
				removeSlowly();
				return;
			} else if (!makeLava && curTime > clickLandCleanupDelay) {
				removeSlowly();
				return;
			} else if (!makeLava && curTime < delay) {
				return;
			} else if (makeLava && curTime < delay) {
				for (double x = -clickLavaRadius; x <= clickLavaRadius; x++) {
					for (double z = -clickLavaRadius; z <= clickLavaRadius; z++) {
						Location loc = origin.clone().add(x, 0, z);
						Block tempBlock = GeneralMethods.getTopBlock(loc, upwardFlow, downwardFlow);
						if (!isWater(tempBlock)) {
							if (tempBlock != null && !isLava(tempBlock) && Math.random() < particleDensity && tempBlock.getLocation().distanceSquared(origin) <= Math.pow(clickLavaRadius, 2)) {
								if (random.nextInt(3) == 0) {
									ParticleEffect.LAVA.display(loc, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0, 1);
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
			if (!clickIsFinished) {
				clickIsFinished = true;
				double radius = makeLava ? clickLavaRadius : clickLandRadius;

				for (double x = -radius; x <= radius; x++) {
					for (double z = -radius; z <= radius; z++) {
						Location loc = origin.clone().add(x, 0, z);
						Block tempBlock_t = GeneralMethods.getTopBlock(loc, upwardFlow, downwardFlow);
						Block tempBlock_b = GeneralMethods.getBottomBlock(loc, upwardFlow, downwardFlow);
						Block tempBlock = tempBlock_t;
						if (tempBlock_t.getLocation().distance(player.getLocation()) > tempBlock_b.getLocation().distance(player.getLocation()))
							tempBlock = tempBlock_b;
						if (tempBlock == null) {
							continue;
						}

						double dSquared = distanceSquaredXZ(tempBlock.getLocation(), origin);
						if (dSquared < Math.pow(radius, 2) && !GeneralMethods.isRegionProtectedFromBuild(this, loc)) {
							if (makeLava && !isLava(tempBlock)) {
								clickIsFinished = false;
								if (Math.random() < lavaCreateSpeed) {
									if (!isLava(tempBlock) || isSnow(tempBlock)) {
										if (isPlant(tempBlock) || isSnow(tempBlock)) {
											Block lower = tempBlock.getRelative(BlockFace.DOWN);
											if (isPlant(lower) || isSnow(lower)) {
												Block lower2 = lower.getRelative(BlockFace.DOWN);
												if (!isEarth(lower2) && !isSand(lower2) && !isMetal(lower2)) {
													continue;
												}
												createLava(lower2);
											} else {
												if (!isEarth(lower) && !isSand(lower) && !isMetal(lower)) {
													continue;
												}
												createLava(lower);
											}
										} else {
											if (!isEarth(tempBlock) && !isSand(tempBlock) && !isMetal(tempBlock)) {
												continue;
											}
											createLava(tempBlock);
										}
									}
								} else {
									if (random.nextInt(4) == 0) {
										Block block = loc.getBlock();
										Block above = block.getRelative(BlockFace.UP);
										Block above2 = above.getRelative(BlockFace.UP);
										if (!isWater(block) && !isWater(above) && !isWater(above2)) {
											ParticleEffect.LAVA.display(loc, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0, 1);
										}
									}
								}
							} else if (!makeLava && isLava(tempBlock)) {
								clickIsFinished = false;
								if (Math.random() < landCreateSpeed) {
									removeLava(tempBlock);
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
	public void createLava(Block block) {
		if (isEarth(block) || isSand(block) || isMetal(block)) {
			if (isPlant(block.getRelative(BlockFace.UP)) || isSnow(block.getRelative(BlockFace.UP))) {
				Block above = block.getRelative(BlockFace.UP);
				Block above2 = above.getRelative(BlockFace.UP);
				if (isPlant(above) || isSnow(above)) {
					TempBlock tb = new TempBlock(above, Material.AIR, (byte) 0);
					TEMP_AIR_BLOCKS.put(above, tb);
					TEMP_AIR_BLOCKS_BY_TEMPBLOCK.put(tb, above);
					affectedBlocks.add(tb);
					if (isPlant(above2) && above2.getType().equals(Material.DOUBLE_PLANT)) {
						TempBlock tb2 = new TempBlock(above2, Material.AIR, (byte) 0);
						TEMP_AIR_BLOCKS.put(above2, tb2);
						TEMP_AIR_BLOCKS_BY_TEMPBLOCK.put(tb2, above2);
						affectedBlocks.add(tb);
					}
				} else
					return;
			}
			TempBlock tblock;
			if(allowNaturalFlow) {
				tblock = new TempBlock(block, Material.LAVA, (byte) 0);
			}
			else {
				tblock = new TempBlock(block, Material.STATIONARY_LAVA, (byte) 0);
			}
			
			if(tblock!=null) {
				TEMP_LAVA_BLOCKS.put(block, tblock);
				TEMP_LAVA_BLOCKS_BY_TEMPBLOCK.put(tblock, block);
				affectedBlocks.add(tblock);

				if (allowNaturalFlow) {
					TempBlock.removeBlock(block);
				}
			}
		}
	}

	/**
	 * Removes a lava block if it is inside of our ArrayList of TempBlocks.
	 * 
	 * @param testBlock the block to attempt to remove
	 */
	@SuppressWarnings("deprecation")
	public void removeLava(Block testBlock) {
		if (TEMP_LAVA_BLOCKS.containsKey(testBlock)) {
			TempBlock tb = TEMP_LAVA_BLOCKS.get(testBlock);
			tb.revertBlock();
			TEMP_LAVA_BLOCKS.remove(testBlock);
			TEMP_LAVA_BLOCKS_BY_TEMPBLOCK.remove(tb);
			affectedBlocks.remove(tb);
			return;
		}

		TempBlock tblock = new TempBlock(testBlock, revertMaterial, testBlock.getData());
		affectedBlocks.add(tblock);
		TEMP_LAND_BLOCKS.put(testBlock, tblock);
		TEMP_LAND_BLOCKS_BY_TEMPBLOCK.put(tblock, testBlock);
	}

	/**
	 * Causes this instance of LavaFlow to remove() after a specified amount of
	 * time. This is useful for causing the Shift version of the ability to
	 * automatically clean up over time.
	 */
	public void removeOnDelay() {
		BukkitRunnable br = new BukkitRunnable() {
			@Override
			public void run() {
				removeSlowly();
			}
		};
		br.runTaskLater(ProjectKorra.plugin, (long) (shiftRemoveDelay / 1000.0 * 20.0));
		tasks.add(br);
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
		for (int i = affectedBlocks.size() - 1; i > -1; i--) {
			final TempBlock tblock = affectedBlocks.get(i);
			final boolean isTempAir = TEMP_AIR_BLOCKS.values().contains(tblock);
			new BukkitRunnable() {
				@SuppressWarnings("deprecation")
				@Override
				public void run() {
					tblock.revertBlock();
					if (isTempAir && tblock.getState().getType() == Material.DOUBLE_PLANT) {
						tblock.getBlock().getRelative(BlockFace.UP).setType(Material.DOUBLE_PLANT);
						tblock.getBlock().getRelative(BlockFace.UP).setData((byte) (tblock.getState().getRawData() + 8));
					}

				}
			}.runTaskLater(ProjectKorra.plugin, (long) (i / shiftRemoveSpeed));

			if (TEMP_LAVA_BLOCKS.values().contains(tblock)) {
				affectedBlocks.remove(tblock);
				TEMP_LAVA_BLOCKS.remove(TEMP_LAVA_BLOCKS_BY_TEMPBLOCK.get(tblock));
				TEMP_LAVA_BLOCKS_BY_TEMPBLOCK.remove(tblock);
			}
			if (TEMP_LAND_BLOCKS.values().contains(tblock)) {
				affectedBlocks.remove(tblock);
				TEMP_LAND_BLOCKS.remove(TEMP_LAND_BLOCKS_BY_TEMPBLOCK.get(tblock));
				TEMP_LAVA_BLOCKS_BY_TEMPBLOCK.remove(tblock);
			}
			if (TEMP_AIR_BLOCKS.values().contains(tblock)) {
				affectedBlocks.remove(tblock);
				TEMP_AIR_BLOCKS.remove(TEMP_AIR_BLOCKS_BY_TEMPBLOCK.get(tblock));
				TEMP_AIR_BLOCKS_BY_TEMPBLOCK.remove(tblock);
			}
		}

		for (BukkitRunnable task : tasks) {
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
		for (int i = affectedBlocks.size() - 1; i > -1; i--) {
			final TempBlock tblock = affectedBlocks.get(i);
			tblock.revertBlock();
			if (TEMP_LAVA_BLOCKS.values().contains(tblock)) {
				affectedBlocks.remove(tblock);
				TEMP_LAVA_BLOCKS.remove(TEMP_LAVA_BLOCKS_BY_TEMPBLOCK.get(tblock));
				TEMP_LAVA_BLOCKS_BY_TEMPBLOCK.remove(tblock);
			}
			if (TEMP_LAND_BLOCKS.values().contains(tblock)) {
				affectedBlocks.remove(tblock);
				TEMP_LAND_BLOCKS.remove(TEMP_LAND_BLOCKS_BY_TEMPBLOCK.get(tblock));
				TEMP_LAND_BLOCKS_BY_TEMPBLOCK.remove(tblock);
			}
		}

		for (BukkitRunnable task : tasks) {
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
	public ArrayList<Block> getAdjacentLavaBlocks(Location loc) {
		ArrayList<Block> list = getAdjacentBlocks(loc);
		for (int i = 0; i < list.size(); i++) {
			Block block = list.get(i);
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
	public static ArrayList<Block> getAdjacentBlocks(Location loc) {
		ArrayList<Block> list = new ArrayList<Block>();
		Block block = loc.getBlock();

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
	public static double distanceSquaredXZ(Location l1, Location l2) {
		Location temp1 = l1.clone();
		Location temp2 = l2.clone();
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
	public static ArrayList<LavaFlow> getLavaFlow(Player player, AbilityType type) {
		ArrayList<LavaFlow> list = new ArrayList<LavaFlow>();
		for (LavaFlow lf : getAbilities(LavaFlow.class)) {
			if (lf.player != null && lf.player == player && lf.type != null && lf.type == type) {
				list.add(lf);
			}
		}
		return list;
	}

	public static Material getRevertMaterial() {
		Material m = Material.STONE;
		LavaFlow lf = (LavaFlow) CoreAbility.getAbility("LavaFlow");
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
		if (origin != null) {
			return origin;
		} else if (player != null) {
			return player.getLocation();
		}
		return null;
	}

	@Override
	public long getCooldown() {
		return type == AbilityType.CLICK ? clickLandCooldown : shiftCooldown;
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
		ArrayList<Location> locations = new ArrayList<>();
		for (TempBlock tblock : affectedBlocks) {
			locations.add(tblock.getLocation());
		}
		return locations;
	}

	public boolean isRemoving() {
		return removing;
	}

	public void setRemoving(boolean removing) {
		this.removing = removing;
	}

	public boolean isMakeLava() {
		return makeLava;
	}

	public void setMakeLava(boolean makeLava) {
		this.makeLava = makeLava;
	}

	public boolean isClickIsFinished() {
		return clickIsFinished;
	}

	public void setClickIsFinished(boolean clickIsFinished) {
		this.clickIsFinished = clickIsFinished;
	}

	public boolean isShiftIsFinished() {
		return shiftIsFinished;
	}

	public void setShiftIsFinished(boolean shiftIsFinished) {
		this.shiftIsFinished = shiftIsFinished;
	}

	public boolean isAllowNaturalFlow() {
		return allowNaturalFlow;
	}

	public void setAllowNaturalFlow(boolean allowNaturalFlow) {
		this.allowNaturalFlow = allowNaturalFlow;
	}

	public int getShiftCounter() {
		return shiftCounter;
	}

	public void setShiftCounter(int shiftCounter) {
		this.shiftCounter = shiftCounter;
	}

	public int getUpwardFlow() {
		return upwardFlow;
	}

	public void setUpwardFlow(int upwardFlow) {
		this.upwardFlow = upwardFlow;
	}

	public int getDownwardFlow() {
		return downwardFlow;
	}

	public void setDownwardFlow(int downwardFlow) {
		this.downwardFlow = downwardFlow;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getClickLavaDelay() {
		return clickLavaDelay;
	}

	public void setClickLavaDelay(long clickLavaDelay) {
		this.clickLavaDelay = clickLavaDelay;
	}

	public long getClickLandDelay() {
		return clickLandDelay;
	}

	public void setClickLandDelay(long clickLandDelay) {
		this.clickLandDelay = clickLandDelay;
	}

	public long getClickLavaCooldown() {
		return clickLavaCooldown;
	}

	public void setClickLavaCooldown(long clickLavaCooldown) {
		this.clickLavaCooldown = clickLavaCooldown;
	}

	public long getClickLandCooldown() {
		return clickLandCooldown;
	}

	public void setClickLandCooldown(long clickLandCooldown) {
		this.clickLandCooldown = clickLandCooldown;
	}

	public long getShiftCooldown() {
		return shiftCooldown;
	}

	public void setShiftCooldown(long shiftCooldown) {
		this.shiftCooldown = shiftCooldown;
	}

	public long getClickLavaCleanupDelay() {
		return clickLavaCleanupDelay;
	}

	public void setClickLavaCleanupDelay(long clickLavaCleanupDelay) {
		this.clickLavaCleanupDelay = clickLavaCleanupDelay;
	}

	public long getClickLandCleanupDelay() {
		return clickLandCleanupDelay;
	}

	public void setClickLandCleanupDelay(long clickLandCleanupDelay) {
		this.clickLandCleanupDelay = clickLandCleanupDelay;
	}

	public double getParticleDensity() {
		return particleDensity;
	}

	public void setParticleDensity(double particleDensity) {
		this.particleDensity = particleDensity;
	}

	public double getParticleOffset() {
		return particleOffset;
	}

	public void setParticleOffset(double particleOffset) {
		this.particleOffset = particleOffset;
	}

	public double getCurrentRadius() {
		return currentRadius;
	}

	public void setCurrentRadius(double currentRadius) {
		this.currentRadius = currentRadius;
	}

	public double getShiftPlatformRadius() {
		return shiftPlatformRadius;
	}

	public void setShiftPlatformRadius(double shiftPlatformRadius) {
		this.shiftPlatformRadius = shiftPlatformRadius;
	}

	public double getShiftMaxRadius() {
		return shiftMaxRadius;
	}

	public void setShiftMaxRadius(double shiftMaxRadius) {
		this.shiftMaxRadius = shiftMaxRadius;
	}

	public double getShiftFlowSpeed() {
		return shiftFlowSpeed;
	}

	public void setShiftFlowSpeed(double shiftFlowSpeed) {
		this.shiftFlowSpeed = shiftFlowSpeed;
	}

	public double getShiftRemoveSpeed() {
		return shiftRemoveSpeed;
	}

	public void setShiftRemoveSpeed(double shiftRemoveSpeed) {
		this.shiftRemoveSpeed = shiftRemoveSpeed;
	}

	public double getShiftRemoveDelay() {
		return shiftRemoveDelay;
	}

	public void setShiftRemoveDelay(double shiftRemoveDelay) {
		this.shiftRemoveDelay = shiftRemoveDelay;
	}

	public double getClickRange() {
		return clickRange;
	}

	public void setClickRange(double clickRange) {
		this.clickRange = clickRange;
	}

	public double getClickLavaRadius() {
		return clickLavaRadius;
	}

	public void setClickLavaRadius(double clickLavaRadius) {
		this.clickLavaRadius = clickLavaRadius;
	}

	public double getClickLandRadius() {
		return clickLandRadius;
	}

	public void setClickLandRadius(double clickLandRadius) {
		this.clickLandRadius = clickLandRadius;
	}

	public double getLavaCreateSpeed() {
		return lavaCreateSpeed;
	}

	public void setLavaCreateSpeed(double lavaCreateSpeed) {
		this.lavaCreateSpeed = lavaCreateSpeed;
	}

	public double getLandCreateSpeed() {
		return landCreateSpeed;
	}

	public void setLandCreateSpeed(double landCreateSpeed) {
		this.landCreateSpeed = landCreateSpeed;
	}

	public AbilityType getType() {
		return type;
	}

	public void setType(AbilityType type) {
		this.type = type;
	}

	public Location getOrigin() {
		return origin;
	}

	public void setOrigin(Location origin) {
		this.origin = origin;
	}

	public ArrayList<TempBlock> getAffectedBlocks() {
		return affectedBlocks;
	}

	public ArrayList<BukkitRunnable> getTasks() {
		return tasks;
	}

}

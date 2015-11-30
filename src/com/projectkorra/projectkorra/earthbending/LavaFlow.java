package com.projectkorra.projectkorra.earthbending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AvatarState;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class LavaFlow {
	public static enum AbilityType {
		SHIFT, CLICK
	}

	/** The material that lava will be converted into **/
	public static final Material REVERT_MATERIAL = Material.STONE;
	/** A list of every instance of this ability **/
	public static final ArrayList<LavaFlow> instances = new ArrayList<LavaFlow>();
	/** The temporary lava blocks that have been created by every LavaFlow **/
	public static final ArrayList<TempBlock> TEMP_LAVA_BLOCKS = new ArrayList<TempBlock>();
	/** The temporary land blocks that have been created by every LavaFlow **/
	public static final ArrayList<TempBlock> TEMP_LAND_BLOCKS = new ArrayList<TempBlock>();

	public static final long SHIFT_COOLDOWN = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.LavaFlow.ShiftCooldown");
	public static final long CLICK_LAVA_DELAY = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.LavaFlow.ClickLavaStartDelay");
	public static final long CLICK_LAND_DELAY = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.LavaFlow.ClickLandStartDelay");
	public static final long CLICK_LAVA_COOLDOWN = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.LavaFlow.ClickLavaCooldown");
	public static final long CLICK_LAND_COOLDOWN = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.LavaFlow.ClickLandCooldown");
	public static final long CLICK_LAVA_CLEANUP_DELAY = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.LavaFlow.ClickLavaCleanupDelay");
	public static final long CLICK_LAND_CLEANUP_DELAY = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.LavaFlow.ClickLandCleanupDelay");
	public static final long SHIFT_REMOVE_DELAY = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.LavaFlow.ShiftCleanupDelay");

	public static final double CLICK_RANGE = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.LavaFlow.ClickRange");
	public static final double CLICK_LAVA_RADIUS = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.LavaFlow.ClickRadius");
	public static final double CLICK_LAND_RADIUS = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.LavaFlow.ClickRadius");
	public static final double SHIFT_PLATFORM_RADIUS = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.LavaFlow.ShiftPlatformRadius");
	public static final double SHIFT_MAX_RADIUS = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.LavaFlow.ShiftRadius");
	public static final double SHIFT_REMOVE_SPEED = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.LavaFlow.ShiftRemoveSpeed");
	public static final double LAVA_CREATE_SPEED = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.LavaFlow.ClickLavaCreateSpeed");
	public static final double LAND_CREATE_SPEED = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.LavaFlow.ClickLandCreateSpeed");
	public static final double SHIFT_FLOW_SPEED = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.LavaFlow.ShiftFlowSpeed");

	public static final int UPWARD_FLOW = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.LavaFlow.UpwardFlow");
	public static final int DOWNWARD_FLOW = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.LavaFlow.DownwardFlow");
	public static final boolean ALLOW_NATURAL_FLOW = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Earth.LavaFlow.AllowNaturalFlow");
	public static final double PARTICLE_DENSITY = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.LavaFlow.ParticleDensity");
	private static final double PARTICLE_OFFSET = 3;

	private Player player;
	private BendingPlayer bplayer;
	private long time;
	private int shiftCounter;
	private boolean removing;
	private boolean makeLava;
	private boolean clickIsFinished;
	private double currentRadius;

	private double shiftPlatformRadius, shiftMaxRadius;
	private double shiftFlowSpeed, shiftRemoveSpeed, shiftRemoveDelay;
	private double particleDensity;
	private double clickRange, clickLavaRadius, clickLandRadius;
	private long clickLavaDelay, clickLandDelay, clickLavaCooldown, clickLandCooldown,
			shiftCooldown;
	private long clickLavaCleanupDelay, clickLandCleanupDelay;
	private double lavaCreateSpeed, landCreateSpeed;
	private int upwardFlow, downwardFlow;
	private boolean shiftIsFinished;
	private boolean allowNaturalFlow;
	private AbilityType type;
	private Location origin;

	/** All of the blocks that have been modified by this ability **/
	private ArrayList<TempBlock> affectedBlocks;
	private ArrayList<BukkitRunnable> tasks;

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
		if (!EarthMethods.canLavabend(player))
			return;

		time = System.currentTimeMillis();
		this.player = player;
		this.type = type;
		bplayer = GeneralMethods.getBendingPlayer(player.getName());

		shiftCounter = 0;
		currentRadius = 0;
		removing = false;
		makeLava = true;
		clickIsFinished = false;
		affectedBlocks = new ArrayList<TempBlock>();
		tasks = new ArrayList<BukkitRunnable>();

		shiftCooldown = SHIFT_COOLDOWN;
		shiftPlatformRadius = SHIFT_PLATFORM_RADIUS;
		shiftMaxRadius = SHIFT_MAX_RADIUS;
		shiftFlowSpeed = SHIFT_FLOW_SPEED;
		shiftRemoveSpeed = SHIFT_REMOVE_SPEED;
		shiftRemoveDelay = SHIFT_REMOVE_DELAY;
		particleDensity = PARTICLE_DENSITY;
		clickRange = CLICK_RANGE;
		clickLavaRadius = CLICK_LAVA_RADIUS;
		clickLandRadius = CLICK_LAND_RADIUS;
		clickLavaDelay = CLICK_LAVA_DELAY;
		clickLandDelay = CLICK_LAND_DELAY;
		clickLavaCooldown = CLICK_LAVA_COOLDOWN;
		clickLandCooldown = CLICK_LAND_COOLDOWN;
		clickLavaCleanupDelay = CLICK_LAVA_CLEANUP_DELAY;
		clickLandCleanupDelay = CLICK_LAND_CLEANUP_DELAY;
		lavaCreateSpeed = LAVA_CREATE_SPEED;
		landCreateSpeed = LAND_CREATE_SPEED;
		upwardFlow = UPWARD_FLOW;
		downwardFlow = DOWNWARD_FLOW;
		allowNaturalFlow = ALLOW_NATURAL_FLOW;

		if (AvatarState.isAvatarState(player)) {
			shiftCooldown = 0;
			clickLavaCooldown = 0;
			clickLandCooldown = 0;
			shiftPlatformRadius = AvatarState.getValue(shiftPlatformRadius);
			shiftMaxRadius = AvatarState.getValue(shiftMaxRadius);
			shiftFlowSpeed = AvatarState.getValue(shiftFlowSpeed);
			shiftRemoveDelay = AvatarState.getValue(shiftRemoveDelay);
			clickRange = AvatarState.getValue(clickRange);
			clickLavaRadius = AvatarState.getValue(clickLavaRadius);
			clickLandRadius = AvatarState.getValue(clickLandRadius);
			clickLavaCleanupDelay = (long) AvatarState.getValue(clickLavaCleanupDelay);
			clickLandCleanupDelay = (long) AvatarState.getValue(clickLandCleanupDelay);
			lavaCreateSpeed = AvatarState.getValue(lavaCreateSpeed);
			landCreateSpeed = AvatarState.getValue(landCreateSpeed);
			upwardFlow = AvatarState.getValue(upwardFlow);
			downwardFlow = AvatarState.getValue(downwardFlow);
		}

		if (type == AbilityType.SHIFT) {
			// Update the shift counter for all the player's LavaFlows
			ArrayList<LavaFlow> shiftFlows = LavaFlow.getLavaFlow(player, LavaFlow.AbilityType.SHIFT);
			if (shiftFlows.size() > 0 && !player.isSneaking()) {
				for (LavaFlow lf : shiftFlows) {
					lf.shiftCounter++;
				}
			}

			if (bplayer.isOnCooldown("lavaflowshift")) {
				remove();
				return;
			}
			instances.add(this);
		} else if (type == AbilityType.CLICK) {
			Block sourceBlock = BlockSource.getEarthOrLavaSourceBlock(player, clickRange, ClickType.LEFT_CLICK, false);
			if (sourceBlock == null) {
				remove();
				return;
			}
			origin = sourceBlock.getLocation();
			makeLava = !isLava(sourceBlock);
			long cooldown = makeLava ? clickLavaCooldown : clickLandCooldown;

			if (makeLava) {
				if (bplayer.isOnCooldown("lavaflowmakelava")) {
					remove();
					return;
				} else
					bplayer.addCooldown("lavaflowmakelava", cooldown);
			}

			if (!makeLava) {
				if (bplayer.isOnCooldown("lavaflowmakeland")) {
					remove();
					return;
				} else
					bplayer.addCooldown("lavaflowmakeland", cooldown);
			}
			instances.add(this);
		}
	}

	/**
	 * Progresses LavaFlow by 1 tick. This is the heart of the ability, it
	 * determines whether or not the LavaFlow type is Click/Sneaking, and it
	 * will remove the ability if any issues arise.
	 */
	public void progress() {
		if (shiftCounter > 0 && type == AbilityType.SHIFT) {
			remove();
			return;
		} else if (removing)
			return;
		else if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}

		if (type == AbilityType.SHIFT) {
			if (System.currentTimeMillis() - time > shiftRemoveDelay) {
				remove();
				return;
			}
			if (!player.isSneaking() && !removing) {
				if (affectedBlocks.size() > 0) {
					removeOnDelay();
					removing = true;
					bplayer.addCooldown("lavaflowshift", shiftCooldown);
				} else
					remove();
				return;
			}

			String ability = GeneralMethods.getBoundAbility(player);
			if (ability == null) {
				remove();
				return;
			} else if (!ability.equalsIgnoreCase("LavaFlow") || !GeneralMethods.canBend(player.getName(), "LavaFlow")) {
				remove();
				return;
			} else if (origin == null) {
				origin = player.getLocation().clone().add(0, -1, 0);
				if (!EarthMethods.isEarthbendable(player, origin.getBlock()) && origin.getBlock().getType() != Material.GLOWSTONE) {
					remove();
					return;
				}
			}

			for (double x = -currentRadius; x <= currentRadius + PARTICLE_OFFSET; x++) {
				for (double z = -currentRadius; z < currentRadius + PARTICLE_OFFSET; z++) {
					Location loc = origin.clone().add(x, 0, z);
					Block block = GeneralMethods.getTopBlock(loc, upwardFlow, downwardFlow);
					if (block == null)
						continue;

					double dSquared = distanceSquaredXZ(block.getLocation(), origin);
					if (!isLava(block) && dSquared > Math.pow(shiftPlatformRadius, 2)) {
						if (dSquared < Math.pow(currentRadius, 2) && !GeneralMethods.isRegionProtectedFromBuild(player, "LavaFlow", block.getLocation())) {
							if (dSquared < shiftPlatformRadius * 4 || getAdjacentLavaBlocks(block.getLocation()).size() > 0)
								createLava(block);
						} else if (Math.random() < particleDensity && dSquared < Math.pow(currentRadius + particleDensity, 2) && currentRadius + particleDensity < shiftMaxRadius) {
							if (GeneralMethods.rand.nextInt(3) == 0) {
								ParticleEffect.LAVA.display(loc, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0, 1);
							}
						}
					}
				}
				
				if(!shiftIsFinished) {
					if (GeneralMethods.rand.nextInt(10) == 0) {
						ParticleEffect.LAVA.display(player.getLocation(), (float) Math.random(), (float) Math.random(), (float) Math.random(), 0, 1);
					}
				}
				
				currentRadius += shiftFlowSpeed;
				if (currentRadius > shiftMaxRadius) {
					currentRadius = shiftMaxRadius;
					shiftIsFinished = true;
				}
			}
		}

		/*
		 * The variable makeLava refers to whether or not the ability is trying
		 * to remove land in place of lava or if makeLava = false then lava is
		 * being replaced with land.
		 * 
		 * Notice we have separate variables between both versions, because most
		 * of the time making lava will have longer delays and longer cooldowns.
		 */
		else if (type == AbilityType.CLICK) {
			long curTime = System.currentTimeMillis() - time;
			double delay = makeLava ? clickLavaDelay : clickLandDelay;
			if (makeLava && curTime > clickLavaCleanupDelay) {
				remove();
				return;
			} else if (!makeLava && curTime > clickLandCleanupDelay) {
				remove();
				return;
			} else if (!makeLava && curTime < delay)
				return;
			else if (makeLava && curTime < delay) {
				for (double x = -clickLavaRadius; x <= clickLavaRadius; x++)
					for (double z = -clickLavaRadius; z <= clickLavaRadius; z++) {
						Location loc = origin.clone().add(x, 0, z);
						Block tempBlock = GeneralMethods.getTopBlock(loc, upwardFlow, downwardFlow);
						if (tempBlock != null && !isLava(tempBlock) && Math.random() < PARTICLE_DENSITY && tempBlock.getLocation().distanceSquared(origin) <= Math.pow(clickLavaRadius, 2)) {
							if (GeneralMethods.rand.nextInt(3) == 0) {
								ParticleEffect.LAVA.display(loc, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0, 1);
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
				for (double x = -radius; x <= radius; x++)
					for (double z = -radius; z <= radius; z++) {
						Location loc = origin.clone().add(x, 0, z);
						Block tempBlock = GeneralMethods.getTopBlock(loc, upwardFlow, downwardFlow);
						if (tempBlock == null)
							continue;

						double dSquared = distanceSquaredXZ(tempBlock.getLocation(), origin);
						if (dSquared < Math.pow(radius, 2) && !GeneralMethods.isRegionProtectedFromBuild(player, "LavaFlow", loc)) {
							if (makeLava && !isLava(tempBlock)) {
								clickIsFinished = false;
								if (Math.random() < lavaCreateSpeed)
									createLava(tempBlock);
								else {
									if (GeneralMethods.rand.nextInt(4) == 0) {
										ParticleEffect.LAVA.display(loc, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0, 1);
									}
								}
							} else if (!makeLava && isLava(tempBlock)) {
								clickIsFinished = false;
								if (Math.random() < landCreateSpeed)
									removeLava(tempBlock);
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
		if (isEarthbendableMaterial(block.getType(), player)) {
			TempBlock tblock = new TempBlock(block, Material.STATIONARY_LAVA, (byte) 0);
			TEMP_LAVA_BLOCKS.add(tblock);
			affectedBlocks.add(tblock);
			if (allowNaturalFlow)
				TempBlock.instances.remove(block);
		}
	}

	/**
	 * Removes a lava block if it is inside of our ArrayList of TempBlocks.
	 * 
	 * @param testBlock the block to attempt to remove
	 */
	@SuppressWarnings("deprecation")
	public void removeLava(Block testBlock) {
		for (int i = 0; i < TEMP_LAVA_BLOCKS.size(); i++) {
			TempBlock tblock = TEMP_LAVA_BLOCKS.get(i);
			Block block = tblock.getBlock();
			if (block.equals(testBlock)) {
				tblock.revertBlock();
				TEMP_LAVA_BLOCKS.remove(i);
				affectedBlocks.remove(tblock);
				return;
			}
		}

		TempBlock tblock = new TempBlock(testBlock, REVERT_MATERIAL, testBlock.getData());
		affectedBlocks.add(tblock);
		TEMP_LAND_BLOCKS.add(tblock);
	}

	/**
	 * Causes this instance of LavaFlow to remove() after a specified amount of
	 * time. This is useful for causing the Shift version of the ability to
	 * automatically clean up over time.
	 */
	public void removeOnDelay() {
		BukkitRunnable br = new BukkitRunnable() {
			public void run() {
				remove();
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
	public void remove() {
		instances.remove(this);
		for (int i = affectedBlocks.size() - 1; i > -1; i--) {
			final TempBlock tblock = affectedBlocks.get(i);
			new BukkitRunnable() {
				public void run() {
					tblock.revertBlock();
				}
			}.runTaskLater(ProjectKorra.plugin, (long) (i / shiftRemoveSpeed));

			if (TEMP_LAVA_BLOCKS.contains(tblock)) {
				affectedBlocks.remove(tblock);
				TEMP_LAVA_BLOCKS.remove(tblock);
			}
			if (TEMP_LAND_BLOCKS.contains(tblock)) {
				affectedBlocks.remove(tblock);
				TEMP_LAND_BLOCKS.remove(tblock);
			}
		}

		for (BukkitRunnable task : tasks)
			task.cancel();
	}

	/**
	 * Removes this ability instance instantly. This method does not cause any
	 * block animation, it just removes everything.
	 */
	public void removeInstantly() {
		instances.remove(this);
		for (int i = affectedBlocks.size() - 1; i > -1; i--) {
			final TempBlock tblock = affectedBlocks.get(i);
			tblock.revertBlock();
			if (TEMP_LAVA_BLOCKS.contains(tblock)) {
				affectedBlocks.remove(tblock);
				TEMP_LAVA_BLOCKS.remove(tblock);
			}
			if (TEMP_LAND_BLOCKS.contains(tblock)) {
				affectedBlocks.remove(tblock);
				TEMP_LAND_BLOCKS.remove(tblock);
			}
		}

		for (BukkitRunnable task : tasks)
			task.cancel();
	}

	/**
	 * Progresses every instance of LavaFlow by 1 tick
	 */
	public static void progressAll() {
		for (int i = instances.size() - 1; i >= 0; i--)
			instances.get(i).progress();
	}

	/**
	 * Removes every instancce of LavaFlow without animating the block removing
	 * process.
	 */
	public static void removeAll() {
		for (int i = instances.size() - 1; i >= 0; i--) {
			instances.get(i).removeInstantly();
		}
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
		for (int x = -1; x <= 1; x++)
			for (int y = -1; y <= 1; y++)
				for (int z = -1; z <= 1; z++)
					if (!(x == 0 && y == 0 && z == 0))
						list.add(block.getLocation().add(x, y, z).getBlock());
		return list;
	}

	/**
	 * Returns a list of all the Lava blocks that are adjacent to the block at
	 * loc.
	 * 
	 * @param loc the middle location of the adjacent blocks
	 * @return a list of the adjacent blocks
	 */
	public static ArrayList<Block> getAdjacentLavaBlocks(Location loc) {
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
	 * A version of Methods.isEarthbendable that avoids using the
	 * isRegionProtected call since it isn't necessary in the case of just
	 * checking a specific material.
	 * 
	 * @param mat The material to check
	 * @param player The player that is earthbending
	 * @return true if the material is earthbendable
	 */
	public static boolean isEarthbendableMaterial(Material mat, Player player) {
		for (String s : ProjectKorra.plugin.getConfig().getStringList("Properties.Earth.EarthbendableBlocks"))
			if (mat == Material.getMaterial(s))
				return true;
		if (ProjectKorra.plugin.getConfig().getStringList("Properties.Earth.MetalBlocks").contains(mat.toString()) && EarthMethods.canMetalbend(player)) {
			return true;
		}
		return false;
	}

	public static boolean isLava(Block block) {
		return block.getType() == Material.LAVA || block.getType() == Material.STATIONARY_LAVA;
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
	 * Returns all the LavaFlows created by a specific player.
	 * 
	 * @param player the player that created the ability instance
	 * @return a list of all the LavaFlows created by player
	 */
	public static ArrayList<LavaFlow> getLavaFlow(Player player) {
		ArrayList<LavaFlow> list = new ArrayList<LavaFlow>();
		for (LavaFlow lf : instances)
			if (lf.player != null && lf.player == player)
				list.add(lf);
		return list;
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
		for (LavaFlow lf : instances)
			if (lf.player != null && lf.player == player && lf.type != null && lf.type == type)
				list.add(lf);
		return list;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
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

	public double getParticleDensity() {
		return particleDensity;
	}

	public void setParticleDensity(double particleDensity) {
		this.particleDensity = particleDensity;
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
		if (player != null)
			bplayer.addCooldown("lavaflowmakelava", clickLavaCooldown);
	}

	public long getClickLandCooldown() {
		return clickLandCooldown;
	}

	public void setClickLandCooldown(long clickLandCooldown) {
		this.clickLandCooldown = clickLandCooldown;
		if (player != null)
			bplayer.addCooldown("lavaflowmakeland", clickLandCooldown);
	}

	public long getShiftCooldown() {
		return shiftCooldown;
	}

	public void setShiftCooldown(long shiftCooldown) {
		this.shiftCooldown = shiftCooldown;
		if (player != null)
			bplayer.addCooldown("lavaflowshift", shiftCooldown);
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

	public boolean isAllowNaturalFlow() {
		return allowNaturalFlow;
	}

	public void setAllowNaturalFlow(boolean allowNaturalFlow) {
		this.allowNaturalFlow = allowNaturalFlow;
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

	public void setAffectedBlocks(ArrayList<TempBlock> affectedBlocks) {
		this.affectedBlocks = affectedBlocks;
	}

	public ArrayList<BukkitRunnable> getTasks() {
		return tasks;
	}

	public void setTasks(ArrayList<BukkitRunnable> tasks) {
		this.tasks = tasks;
	}

	public int getShiftCounter() {
		return shiftCounter;
	}

	public void setShiftCounter(int shiftCounter) {
		this.shiftCounter = shiftCounter;
	}
}

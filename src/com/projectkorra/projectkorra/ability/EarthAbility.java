package com.projectkorra.projectkorra.ability;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.RaiseEarth;
import com.projectkorra.projectkorra.earthbending.lava.LavaFlow;
import com.projectkorra.projectkorra.earthbending.passive.DensityShift;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.Information;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

public abstract class EarthAbility extends ElementalAbility {

	private static final HashSet<Block> PREVENT_EARTHBENDING = new HashSet<Block>();
	private static final Map<Block, Information> MOVED_EARTH = new ConcurrentHashMap<Block, Information>();
	private static final Map<Integer, Information> TEMP_AIR_LOCATIONS = new ConcurrentHashMap<Integer, Information>();
	private static final ArrayList<Block> PREVENT_PHYSICS = new ArrayList<Block>();

	public EarthAbility(final Player player) {
		super(player);
	}

	public int getEarthbendableBlocksLength(final Block block, Vector direction, final int maxlength) {
		final Location location = block.getLocation();
		direction = direction.normalize();
		for (int i = 0; i <= maxlength; i++) {
			final double j = i;
			if (!this.isEarthbendable(location.clone().add(direction.clone().multiply(j)).getBlock())) {
				return i;
			}
		}
		return maxlength;
	}

	public Block getEarthSourceBlock(final double range) {
		return getEarthSourceBlock(this.player, this.getName(), range);
	}

	@Override
	public Element getElement() {
		return Element.EARTH;
	}

	public Block getLavaSourceBlock(final double range) {
		return getLavaSourceBlock(this.player, this.getName(), range);
	}

	public Block getTargetEarthBlock(final int range) {
		return getTargetEarthBlock(this.player, range);
	}

	@Override
	public boolean isExplosiveAbility() {
		return false;
	}

	@Override
	public boolean isIgniteAbility() {
		return false;
	}

	@Override
	public void handleCollision(final Collision collision) {
		super.handleCollision(collision);
		if (collision.isRemovingFirst()) {
			ParticleEffect.BLOCK_CRACK.display(collision.getLocationFirst(), 10, 1, 1, 1, 0.1, Material.DIRT.createBlockData());
		}
	}
	
	public static boolean isBendableEarthTempBlock(final Block block) {
		return isBendableEarthTempBlock(TempBlock.get(block));
	}
	
	public static boolean isBendableEarthTempBlock(final TempBlock tempBlock) {
		return DensityShift.getSandBlocks().contains(tempBlock);
	}

	public static boolean isEarthbendable(final Material material, final boolean metal, final boolean sand, final boolean lava) {
		return isEarth(material) || (metal && isMetal(material)) || (sand && isSand(material)) || (lava && isLava(material));
	}

	public boolean isEarthbendable(final Block block) {
		return isEarthbendable(this.player, this.getName(), block);
	}

	public static boolean isEarthbendable(final Player player, final Block block) {
		return isEarthbendable(player, null, block);
	}

	public boolean isLavabendable(final Block block) {
		return isLavabendable(this.player, block);
	}

	public boolean isMetalbendable(final Block block) {
		return this.isMetalbendable(block.getType());
	}

	public boolean isMetalbendable(final Material material) {
		return isMetalbendable(this.player, material);
	}

	public boolean isSandbendable(final Block block) {
		return this.isSandbendable(block.getType());
	}

	public boolean isSandbendable(final Material material) {
		return isSandbendable(this.player, material);
	}

	public boolean moveEarth(final Block block, final Vector direction, final int chainlength) {
		return this.moveEarth(block, direction, chainlength, true);
	}

	public boolean moveEarth(Block block, final Vector direction, final int chainlength, final boolean throwplayer) {
		if ((!TempBlock.isTempBlock(block) || isBendableEarthTempBlock(block)) && this.isEarthbendable(block) && !GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
			boolean up = false;
			boolean down = false;
			final Vector norm = direction.clone().normalize();
			if (norm.dot(new Vector(0, 1, 0)) == 1) {
				up = true;
			} else if (norm.dot(new Vector(0, -1, 0)) == 1) {
				down = true;
			}

			final Vector negnorm = norm.clone().multiply(-1);
			final Location location = block.getLocation();
			final ArrayList<Block> blocks = new ArrayList<Block>();

			for (double j = -2; j <= chainlength; j++) {
				final Block checkblock = location.clone().add(negnorm.clone().multiply(j)).getBlock();
				if (!PREVENT_PHYSICS.contains(checkblock)) {
					blocks.add(checkblock);
					PREVENT_PHYSICS.add(checkblock);
				}
			}

			Block affectedblock = location.clone().add(norm).getBlock();
			if (DensityShift.isPassiveSand(block)) {
				DensityShift.revertSand(block);
			}
			if (TempBlock.isTempBlock(affectedblock)) {
				TempBlock.get(affectedblock).revertBlock();
			}
			if (LavaFlow.isLavaFlowBlock(block)) {
				LavaFlow.removeBlock(block);
			}

			if (affectedblock == null) {
				return false;
			} else if (this.isTransparent(affectedblock)) {
				if (throwplayer) {
					for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(affectedblock.getLocation(), 1.75)) {
						if (entity instanceof LivingEntity) {
							final LivingEntity lentity = (LivingEntity) entity;
							if (lentity.getEyeLocation().getBlockX() == affectedblock.getX() && lentity.getEyeLocation().getBlockZ() == affectedblock.getZ()) {
								if (!(entity instanceof FallingBlock)) {
									GeneralMethods.setVelocity(this, entity, norm.clone().multiply(.75));
									
								}
							}
						} else {
							if (entity.getLocation().getBlockX() == affectedblock.getX() && entity.getLocation().getBlockZ() == affectedblock.getZ()) {
								if (!(entity instanceof FallingBlock)) {
									GeneralMethods.setVelocity(this, entity, norm.clone().multiply(.75));
								}
							}
						}
					}
				}
				if (up) {
					final Block topblock = affectedblock.getRelative(BlockFace.UP);
					if (!isAir(topblock.getType())) {
						GeneralMethods.breakBlock(affectedblock);
					} else if (!affectedblock.isLiquid() && !isAir(affectedblock.getType())) {
						moveEarthBlock(affectedblock, topblock);
					}
				} else {
					GeneralMethods.breakBlock(affectedblock);
				}

				moveEarthBlock(block, affectedblock);
				playEarthbendingSound(block.getLocation());

				for (double i = 1; i < chainlength; i++) {
					affectedblock = location.clone().add(negnorm.getX() * i, negnorm.getY() * i, negnorm.getZ() * i).getBlock();
					if (!this.isEarthbendable(affectedblock)) {
						if (down) {
							if (this.isTransparent(affectedblock) && !affectedblock.isLiquid() && !isAir(affectedblock.getType())) {
								moveEarthBlock(affectedblock, block);
							}
						}
						break;
					}
					if (DensityShift.isPassiveSand(affectedblock)) {
						DensityShift.revertSand(affectedblock);
					}
					if (block == null) {
						for (final Block checkblock : blocks) {
							PREVENT_PHYSICS.remove(checkblock);
						}
						return false;
					}
					moveEarthBlock(affectedblock, block);
					block = affectedblock;
				}

				final int i = chainlength;
				affectedblock = location.clone().add(negnorm.getX() * i, negnorm.getY() * i, negnorm.getZ() * i).getBlock();
				if (!this.isEarthbendable(affectedblock)) {
					if (down) {
						if (this.isTransparent(affectedblock) && !affectedblock.isLiquid() && !isAir(affectedblock.getType())) {
							moveEarthBlock(affectedblock, block);
						}
					}
				}
			} else {
				for (final Block checkblock : blocks) {
					PREVENT_PHYSICS.remove(checkblock);
				}
				return false;
			}
			for (final Block checkblock : blocks) {
				PREVENT_PHYSICS.remove(checkblock);
			}
			return true;
		}
		return false;
	}

	public void moveEarth(final Location location, final Vector direction, final int chainlength) {
		this.moveEarth(location, direction, chainlength, true);
	}

	public void moveEarth(final Location location, final Vector direction, final int chainlength, final boolean throwplayer) {
		this.moveEarth(location.getBlock(), direction, chainlength, throwplayer);
	}

	/**
	 * Creates a temporary air block.
	 *
	 * @param block The block to use as a base
	 */
	public static void addTempAirBlock(final Block block) {
		Information info;

		if (MOVED_EARTH.containsKey(block)) {
			info = MOVED_EARTH.get(block);
			MOVED_EARTH.remove(block);

		} else {
			info = new Information();

			info.setBlock(block);
			info.setState(block.getState());
		}
		block.setType(Material.AIR, false);
		info.setTime(System.currentTimeMillis());
		TEMP_AIR_LOCATIONS.put(info.getID(), info);
	}

	public static void displaySandParticle(final Location loc, final int amount, final double xOffset, final double yOffset, final double zOffset, final double speed, final boolean red) {
		if (amount <= 0) {
			return;
		}

		final Material sand = red ? Material.RED_SAND : Material.SAND;
		final Material stone = red ? Material.RED_SANDSTONE : Material.SANDSTONE;

		ParticleEffect.BLOCK_CRACK.display(loc, amount, xOffset, yOffset, zOffset, speed, sand.createBlockData());
		ParticleEffect.BLOCK_CRACK.display(loc, amount, xOffset, yOffset, zOffset, speed, stone.createBlockData());
	}

	/**
	 * Finds a valid Earth source for a Player. To use dynamic source selection,
	 * use BlockSource.getEarthSourceBlock() instead of this method. Dynamic
	 * source selection saves the user's previous source for future use.
	 * {@link BlockSource#getEarthSourceBlock(Player, double, com.projectkorra.projectkorra.util.ClickType)}
	 *
	 * @param range the maximum block selection range.
	 * @return a valid Earth source block, or null if one could not be found.
	 */
	public static Block getEarthSourceBlock(final Player player, final String abilityName, final double range) {
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		final Block testBlock = player.getTargetBlock(getTransparentMaterialSet(), (int) range);
		if (bPlayer == null) {
			return null;
		} else if (isEarthbendable(testBlock.getType(), true, true, true)) {
			return testBlock;
		} else if (!isTransparent(player, testBlock)) {
			return null;
		}

		final Location location = player.getEyeLocation();
		final Vector vector = location.getDirection().clone().normalize();

		for (double i = 0; i <= range; i++) {
			final Block block = location.clone().add(vector.clone().multiply(i)).getBlock();
			if (GeneralMethods.isRegionProtectedFromBuild(player, abilityName, location)) {
				continue;
			} else if (isEarthbendable(player, block)) {
				return block;
			}
		}
		return null;
	}

	public static Block getLavaSourceBlock(final Player player, final double range) {
		return getLavaSourceBlock(player, null, range);
	}

	/**
	 * Finds a valid Lava source for a Player. To use dynamic source selection,
	 * use BlockSource.getLavaSourceBlock() instead of this method. Dynamic
	 * source selection saves the user's previous source for future use.
	 * {@link BlockSource#getLavaSourceBlock(Player, double, com.projectkorra.projectkorra.util.ClickType)}
	 *
	 * @param range the maximum block selection range.
	 * @return a valid Lava source block, or null if one could not be found.
	 */
	public static Block getLavaSourceBlock(final Player player, final String abilityName, final double range) {
		final Location location = player.getEyeLocation();
		final Vector vector = location.getDirection().clone().normalize();

		for (double i = 0; i <= range; i++) {
			final Block block = location.clone().add(vector.clone().multiply(i)).getBlock();
			if (GeneralMethods.isRegionProtectedFromBuild(player, abilityName, location)) {
				continue;
			}
			if (isLavabendable(player, block)) {
				return block;
			}
		}
		return null;
	}

	public static double getMetalAugment(final double value) {
		return value * getConfig().getDouble("Properties.Earth.MetalPowerFactor");
	}

	public static Map<Block, Information> getMovedEarth() {
		return MOVED_EARTH;
	}

	/**
	 * Attempts to find the closest earth block near a given location.
	 *
	 * @param loc the initial location to search from.
	 * @param radius the maximum radius to search for the earth block.
	 * @param maxVertical the maximum block height difference between the
	 *            starting location and the earth bendable block.
	 * @return an earth bendable block, or null.
	 */
	public static Block getNearbyEarthBlock(final Location loc, final double radius, final int maxVertical) {
		if (loc == null) {
			return null;
		}

		final int rotation = 30;
		for (int i = 0; i < radius; i++) {
			Vector tracer = new Vector(i, 1, 0);
			for (int deg = 0; deg < 360; deg += rotation) {
				final Location searchLoc = loc.clone().add(tracer);
				final Block block = GeneralMethods.getTopBlock(searchLoc, maxVertical);

				if (block != null && isEarthbendable(block.getType(), true, true, true)) {
					return block;
				}
				tracer = GeneralMethods.rotateXZ(tracer, rotation);
			}
		}
		return null;
	}

	public static HashSet<Block> getPreventEarthbendingBlocks() {
		return PREVENT_EARTHBENDING;
	}

	public static ArrayList<Block> getPreventPhysicsBlocks() {
		return PREVENT_PHYSICS;
	}

	public static ChatColor getSubChatColor() {
		return ChatColor.valueOf(ConfigManager.getConfig().getString("Properties.Chat.Colors.EarthSub"));
	}

	public static Block getTargetEarthBlock(final Player player, final int range) {
		return player.getTargetBlock(getTransparentMaterialSet(), range);
	}

	public static Map<Integer, Information> getTempAirLocations() {
		return TEMP_AIR_LOCATIONS;
	}

	public static boolean isEarthbendable(final Player player, final String abilityName, final Block block) {
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null || !isEarthbendable(block.getType(), true, true, true) || PREVENT_EARTHBENDING.contains(block) || GeneralMethods.isRegionProtectedFromBuild(player, abilityName, block.getLocation())) {
			return false;
		} else if (isMetal(block) && !bPlayer.canMetalbend()) {
			return false;
		} else if (isSand(block) && !bPlayer.canSandbend()) {
			return false;
		} else if (isLava(block) && !bPlayer.canLavabend()) {
			return false;
		}
		return true;
	}

	public static boolean isEarthRevertOn() {
		return getConfig().getBoolean("Properties.Earth.RevertEarthbending");
	}

	public static boolean isLavabendable(final Player player, final Block block) {
		if (isLava(block) && (block.getBlockData() instanceof Levelled && ((Levelled) block.getBlockData()).getLevel() == 0)) {
			return true;
		}
		return false;
	}

	public static boolean isMetalbendable(final Player player, final Material material) {
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		return bPlayer == null ? null : isMetal(material) && bPlayer.canMetalbend();
	}

	public static boolean isSandbendable(final Player player, final Material material) {
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		return bPlayer == null ? null : isSand(material) && bPlayer.canSandbend();
	}

	public static void moveEarthBlock(final Block source, final Block target) {
		Information info;

		if (MOVED_EARTH.containsKey(source)) {
			info = MOVED_EARTH.get(source);
			MOVED_EARTH.remove(source);
		} else {
			info = new Information();
			info.setBlock(source);
			info.setTime(System.currentTimeMillis());
			info.setState(source.getState());
		}
		info.setTime(System.currentTimeMillis());
		MOVED_EARTH.put(target, info);

		if (info.getState().getType() == Material.SAND) {
			target.setType(Material.SANDSTONE, false);
		} else if (info.getState().getType() == Material.RED_SAND) {
			target.setType(Material.RED_SANDSTONE, false);
		} else if (info.getState().getType() == Material.GRAVEL) {
			target.setType(Material.STONE, false);
		} else if (info.getState().getType().name().endsWith("CONCRETE_POWDER")) {
			target.setType(Material.getMaterial(info.getState().getType().name().replace("_POWDER", "")), false);
		} else {
			target.setBlockData(info.getState().getBlockData(), false);
		}

		source.setType(Material.AIR, false);
	}

	public static void playEarthbendingSound(final Location loc) {
		if (getConfig().getBoolean("Properties.Earth.PlaySound")) {
			final float volume = (float) getConfig().getDouble("Properties.Earth.EarthSound.Volume");
			final float pitch = (float) getConfig().getDouble("Properties.Earth.EarthSound.Pitch");

			Sound sound = Sound.ENTITY_GHAST_SHOOT;

			try {
				sound = Sound.valueOf(getConfig().getString("Properties.Earth.EarthSound.Sound"));
			} catch (final IllegalArgumentException exception) {
				ProjectKorra.log.warning("Your current value for 'Properties.Earth.EarthSound.Sound' is not valid.");
			} finally {
				loc.getWorld().playSound(loc, sound, volume, pitch);
			}
		}
	}

	public static void playMetalbendingSound(final Location loc) {
		if (getConfig().getBoolean("Properties.Earth.PlaySound")) {
			final float volume = (float) getConfig().getDouble("Properties.Earth.MetalSound.Volume");
			final float pitch = (float) getConfig().getDouble("Properties.Earth.MetalSound.Pitch");

			Sound sound = Sound.ENTITY_IRON_GOLEM_HURT;

			try {
				sound = Sound.valueOf(getConfig().getString("Properties.Earth.MetalSound.Sound"));
			} catch (final IllegalArgumentException exception) {
				ProjectKorra.log.warning("Your current value for 'Properties.Earth.MetalSound.Sound' is not valid.");
			} finally {
				loc.getWorld().playSound(loc, sound, volume, pitch);
			}
		}
	}

	public static void playSandbendingSound(final Location loc) {
		if (getConfig().getBoolean("Properties.Earth.PlaySound")) {
			final float volume = (float) getConfig().getDouble("Properties.Earth.SandSound.Volume");
			final float pitch = (float) getConfig().getDouble("Properties.Earth.SandSound.Pitch");

			Sound sound = Sound.BLOCK_SAND_BREAK;

			try {
				sound = Sound.valueOf(getConfig().getString("Properties.Earth.SandSound.Sound"));
			} catch (final IllegalArgumentException exception) {
				ProjectKorra.log.warning("Your current value for 'Properties.Earth.SandSound.Sound' is not valid.");
			} finally {
				loc.getWorld().playSound(loc, sound, volume, pitch);
			}
		}
	}

	public static void playLavabendingSound(final Location loc) {
		if (getConfig().getBoolean("Properties.Earth.PlaySound")) {
			final float volume = (float) getConfig().getDouble("Properties.Earth.LavaSound.Volume");
			final float pitch = (float) getConfig().getDouble("Properties.Earth.LavaSound.Pitch");

			Sound sound = Sound.BLOCK_LAVA_AMBIENT;

			try {
				sound = Sound.valueOf(getConfig().getString("Properties.Earth.LavaSound.Sound"));
			} catch (final IllegalArgumentException exception) {
				ProjectKorra.log.warning("Your current value for 'Properties.Earth.LavaSound.Sound' is not valid.");
			} finally {
				loc.getWorld().playSound(loc, sound, volume, pitch);
			}
		}
	}

	public static void removeAllEarthbendedBlocks() {
		for (final Block block : MOVED_EARTH.keySet()) {
			revertBlock(block);
		}
		for (final Integer i : TEMP_AIR_LOCATIONS.keySet()) {
			revertAirBlock(i, true);
		}
	}

	public static void removeRevertIndex(final Block block) {
		if (MOVED_EARTH.containsKey(block)) {
			final Information info = MOVED_EARTH.get(block);
			if (block.getType() == Material.SANDSTONE && info.getType() == Material.SAND) {
				block.setType(Material.SAND, false);
			}
			if (RaiseEarth.blockInAllAffectedBlocks(block)) {
				EarthAbility.revertBlock(block);
			}

			MOVED_EARTH.remove(block);
		}
	}

	public static void revertAirBlock(final int i) {
		revertAirBlock(i, false);
	}

	public static void revertAirBlock(final int i, final boolean force) {
		if (!TEMP_AIR_LOCATIONS.containsKey(i)) {
			return;
		}

		final Information info = TEMP_AIR_LOCATIONS.get(i);
		final Block block = info.getState().getBlock();

		if (!ElementalAbility.isAir(block.getType()) && !block.isLiquid()) {
			if (force || !MOVED_EARTH.containsKey(block)) {
				TEMP_AIR_LOCATIONS.remove(i);
			} else {
				info.setTime(info.getTime() + 10000);
			}
			return;
		} else {
			info.getState().update(true, false);
			TEMP_AIR_LOCATIONS.remove(i);
		}
	}

	public static boolean revertBlock(final Block block) {
		if (!isEarthRevertOn()) {
			MOVED_EARTH.remove(block);
			return false;
		}
		if (MOVED_EARTH.containsKey(block)) {
			final Information info = MOVED_EARTH.get(block);
			final Block sourceblock = info.getState().getBlock();

			if (ElementalAbility.isAir(info.getState().getType())) {
				MOVED_EARTH.remove(block);
				return true;
			}

			if (block.equals(sourceblock)) {
				info.getState().update(true, false);
				if (RaiseEarth.blockInAllAffectedBlocks(sourceblock)) {
					RaiseEarth.revertAffectedBlock(sourceblock);
				}
				if (RaiseEarth.blockInAllAffectedBlocks(block)) {
					RaiseEarth.revertAffectedBlock(block);
				}
				MOVED_EARTH.remove(block);
				return true;
			}

			if (MOVED_EARTH.containsKey(sourceblock)) {
				addTempAirBlock(block);
				MOVED_EARTH.remove(block);
				return true;
			}

			if (ElementalAbility.isAir(sourceblock.getType()) || sourceblock.isLiquid()) {
				info.getState().update(true, false);
			} else {

			}

			if (GeneralMethods.isAdjacentToThreeOrMoreSources(block, false)) {
				final BlockData data = Material.WATER.createBlockData();
				if (data instanceof Levelled) {
					((Levelled) data).setLevel(7);
				}
				block.setBlockData(data, false);
			} else {
				block.setType(Material.AIR, false);
			}

			if (RaiseEarth.blockInAllAffectedBlocks(sourceblock)) {
				RaiseEarth.revertAffectedBlock(sourceblock);
			}
			if (RaiseEarth.blockInAllAffectedBlocks(block)) {
				RaiseEarth.revertAffectedBlock(block);
			}
			MOVED_EARTH.remove(block);
		}
		return true;
	}

	public static void stopBending() {
		DensityShift.removeAll();

		if (isEarthRevertOn()) {
			removeAllEarthbendedBlocks();
		}
	}
}

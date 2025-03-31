package com.projectkorra.projectkorra.ability;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.projectkorra.projectkorra.region.RegionProtection;
import com.projectkorra.projectkorra.util.ClickType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
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

	private static final HashSet<Block> PREVENT_EARTHBENDING = new HashSet<>();
	private static final Map<Block, Information> MOVED_EARTH = new ConcurrentHashMap<>();
	private static final Map<Integer, Information> TEMP_AIR_LOCATIONS = new ConcurrentHashMap<>();
	private static final ArrayList<Block> PREVENT_PHYSICS = new ArrayList<>();

	protected int noiseReduction = 0;

	public EarthAbility(final Player player) {
		super(player);
	}

	public int getEarthbendableBlocksLength(final Block block, Vector direction, final int maxlength) {
		final Location location = block.getLocation();
		direction = direction.clone().normalize();
		for (int i = 0; i <= maxlength; i++) {
            if (!this.isEarthbendable(location.clone().add(direction.clone().multiply(i)).getBlock())) {
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

	public static boolean isEarthbendable(final Material material, final boolean metal, final boolean sand, final boolean lava, final boolean mud) {
		return isEarth(material) || (metal && isMetal(material)) || (sand && isSand(material)) || (lava && isLava(material)) || (mud && isMud(material));
	}

	public static boolean isEarthbendable(final Material material, final boolean metal, final boolean sand, final boolean lava) {
		return isEarthbendable(material, metal, sand, lava, true);
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

	public boolean moveEarth(final Block block, final Vector direction, final int chainLength) {
		return this.moveEarth(block, direction, chainLength, true);
	}

	public boolean moveEarth(Block block, Vector direction, final int chainLength, final boolean throwPlayer) {
		final Location location = block.getLocation();
		if ((!TempBlock.isTempBlock(block) || isBendableEarthTempBlock(block)) && isEarthbendable(block) && !GeneralMethods.isRegionProtectedFromBuild(this, location)) {
			direction = direction.clone().normalize();
			boolean up = direction.dot(new Vector(0, 1, 0)) == 1;
			boolean down = direction.dot(new Vector(0, -1, 0)) == 1;

			final Vector inverse = direction.clone().multiply(-1);
			final ArrayList<Block> blocks = new ArrayList<>();

			for (double i = -2; i <= chainLength; i++) {
				final Block checkblock = location.clone().add(inverse.clone().multiply(i)).getBlock();
				if (!PREVENT_PHYSICS.contains(checkblock)) {
					blocks.add(checkblock);
					PREVENT_PHYSICS.add(checkblock);
				}
			}

			Block affectedblock = location.clone().add(direction).getBlock();
			if (DensityShift.isPassiveSand(block)) {
				DensityShift.revertSand(block);
			}
			if (TempBlock.isTempBlock(affectedblock)) {
				TempBlock.get(affectedblock).revertBlock();
			}
			if (LavaFlow.isLavaFlowBlock(block)) {
				LavaFlow.removeBlock(block);
			}

            if (isTransparent(affectedblock)) {
                if (throwPlayer) {
                    for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(affectedblock.getLocation(), 1.75)) {
						if (entity instanceof FallingBlock) {
							continue;
						}
						Location entityLocation = (entity instanceof LivingEntity livingEntity) ? livingEntity.getEyeLocation() : entity.getLocation();
						if (entityLocation.getBlockX() == affectedblock.getX() && entityLocation.getBlockZ() == affectedblock.getZ()) {
							GeneralMethods.setVelocity(this, entity, direction.clone().multiply(.75));
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

                // Play sound every other tick, but also alternate on the tick that it should occur on for each different ability
                if ((CoreAbility.getCurrentTick() + this.getId()) % (2 + this.noiseReduction) == 0) {
					playEarthbendingSound(block.getLocation());
				}

                for (int i = 1; i < chainLength; i++) {
                    affectedblock = location.clone().add(inverse.getX() * i, inverse.getY() * i, inverse.getZ() * i).getBlock();
                    if (!isEarthbendable(affectedblock)) {
                        if (down && isTransparent(affectedblock) && !affectedblock.isLiquid() && !isAir(affectedblock.getType())) {
							moveEarthBlock(affectedblock, block);
                        }
                        break;
                    }
                    if (DensityShift.isPassiveSand(affectedblock)) {
                        DensityShift.revertSand(affectedblock);
                    }
                    moveEarthBlock(affectedblock, block);
                    block = affectedblock;
                }

                affectedblock = location.clone().add(inverse.getX() * chainLength, inverse.getY() * chainLength, inverse.getZ() * chainLength).getBlock();
                if (down && !isEarthbendable(affectedblock) && isTransparent(affectedblock) && !affectedblock.isLiquid() && !isAir(affectedblock.getType())) {
					moveEarthBlock(affectedblock, block);
                }
            } else {
				PREVENT_PHYSICS.removeAll(blocks);
                return false;
            }
			PREVENT_PHYSICS.removeAll(blocks);
			return true;
		}
		return false;
	}

	public void moveEarth(final Location location, final Vector direction, final int chainLength) {
		this.moveEarth(location, direction, chainLength, true);
	}

	public void moveEarth(final Location location, final Vector direction, final int chainLength, final boolean throwPlayer) {
		this.moveEarth(location.getBlock(), direction, chainLength, throwPlayer);
	}

	/**
	 * Creates a temporary air block.
	 *
	 * @param block The block to use as a base
	 */
	public static void addTempAirBlock(final Block block) {
		Information info = MOVED_EARTH.remove(block);
		if (info == null) {
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
	 * {@link BlockSource#getEarthSourceBlock(Player, double, ClickType)}
	 *
	 * @param range the maximum block selection range.
	 * @return a valid Earth source block, or null if one could not be found.
	 */
	public static Block getEarthSourceBlock(final Player player, final String abilityName, final double range) {
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return null;
		}

		final Block testBlock = player.getTargetBlock(getTransparentMaterialSet(), Math.max((int) range, 1));
		if (isEarthbendable(testBlock.getType(), true, true, true)) {
			return testBlock;
		} else if (!isTransparent(player, testBlock)) {
			return null;
		}

		final Location location = player.getEyeLocation();
		final Vector vector = location.getDirection();

		for (double i = 0; i <= range; i++) {
			final Block block = location.clone().add(vector.clone().multiply(i)).getBlock();
			if (!RegionProtection.isRegionProtected(player, location, CoreAbility.getAbility(abilityName)) && isEarthbendable(player, block)) {
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
	 * {@link BlockSource#getLavaSourceBlock(Player, double, ClickType)}
	 *
	 * @param range the maximum block selection range.
	 * @return a valid Lava source block, or null if one could not be found.
	 */
	public static Block getLavaSourceBlock(final Player player, final String abilityName, final double range) {
		final Location location = player.getEyeLocation();
		final Vector vector = location.getDirection();

		for (double i = 0; i <= range; i++) {
			final Block block = location.clone().add(vector.clone().multiply(i)).getBlock();
			if (!RegionProtection.isRegionProtected(player, location, CoreAbility.getAbility(abilityName)) && isLavabendable(player, block)) {
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
	 * @param location the initial location to search from.
	 * @param radius the maximum radius to search for the earth block.
	 * @param maxVertical the maximum block height difference between the
	 *            starting location and the earth bendable block.
	 * @return an earth bendable block, or null.
	 */
	public static Block getNearbyEarthBlock(final Location location, final double radius, final int maxVertical) {
		if (location == null) {
			return null;
		}

		final int rotation = 30;
		for (int i = 0; i < radius; i++) {
			Vector tracer = new Vector(i, 1, 0);
			for (int deg = 0; deg < 360; deg += rotation) {
				final Location searchLoc = location.clone().add(tracer);
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
		return player.getTargetBlock(getTransparentMaterialSet(), Math.max(range, 1));
	}

	public static Map<Integer, Information> getTempAirLocations() {
		return TEMP_AIR_LOCATIONS;
	}

	public static boolean isEarthbendable(final Player player, final String abilityName, final Block block) {
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		return bPlayer != null
				&& isEarthbendable(block.getType(), bPlayer.canMetalbend(), bPlayer.canSandbend(), bPlayer.canLavabend())
				&& !RegionProtection.isRegionProtected(player, block.getLocation(), CoreAbility.getAbility(abilityName));
	}

	public static boolean isEarthRevertOn() {
		return getConfig().getBoolean("Properties.Earth.RevertEarthbending");
	}

	public static boolean isLavabendable(final Player player, final Block block) {
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
        return bPlayer != null && isLava(block) && (block.getBlockData() instanceof Levelled levelled && levelled.getLevel() == 0) && bPlayer.canLavabend();
    }

	public static boolean isMetalbendable(final Player player, final Material material) {
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		return bPlayer != null && isMetal(material) && bPlayer.canMetalbend();
	}

	public static boolean isSandbendable(final Player player, final Material material) {
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		return bPlayer != null && isSand(material) && bPlayer.canSandbend();
	}

	public static void moveEarthBlock(final Block source, final Block target) {
		Information info = MOVED_EARTH.remove(source);
		if (info == null) {
			info = new Information();
			info.setBlock(source);
			info.setState(source.getState());
		}
		info.setTime(System.currentTimeMillis());
		MOVED_EARTH.put(target, info);

		BlockState state = info.getState();
		Material type = state.getType();
		switch(type) {
			case SAND -> target.setType(Material.SANDSTONE, false);
			case RED_SAND -> target.setType(Material.RED_SANDSTONE, false);
			case GRAVEL -> target.setType(Material.STONE, false);
			default -> {
				if (Tag.CONCRETE_POWDER.isTagged(type)) {
					Material concrete = Material.getMaterial(type.name().replace("_POWDER", ""));
					target.setBlockData(concrete != null ? concrete.createBlockData() : state.getBlockData(), false);
				} else {
					target.setBlockData(state.getBlockData(), false);
				}
			}
		}

		source.setType(Material.AIR, false);
	}

	public static void playEarthbendingSound(final Location loc) {
        if (!getConfig().getBoolean("Properties.Earth.PlaySound")) {
            return;
        }

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

	public static void playMetalbendingSound(final Location loc) {
        if (!getConfig().getBoolean("Properties.Earth.PlaySound")) {
            return;
        }

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

	public static void playMudbendingSound(final Location loc) {
        if (!getConfig().getBoolean("Properties.Earth.PlaySound")) {
            return;
        }

        final float volume = (float) getConfig().getDouble("Properties.Earth.MudSound.Volume");
        final float pitch = (float) getConfig().getDouble("Properties.Earth.MudSound.Pitch");
        Sound sound = Sound.BLOCK_MUD_PLACE;

        try {
            sound = Sound.valueOf(getConfig().getString("Properties.Earth.MudSound.Sound"));
        } catch (final IllegalArgumentException exception) {
            ProjectKorra.log.warning("Your current value for 'Properties.Earth.MudSound.Sound' is not valid.");
        } finally {
            loc.getWorld().playSound(loc, sound, volume, pitch);
        }
    }

	public static void playSandbendingSound(final Location loc) {
        if (!getConfig().getBoolean("Properties.Earth.PlaySound")) {
            return;
        }

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

	public static void playLavabendingSound(final Location loc) {
        if (!getConfig().getBoolean("Properties.Earth.PlaySound")) {
            return;
        }

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

	public static void removeAllEarthbendedBlocks() {
		for (final Block block : MOVED_EARTH.keySet()) {
			revertBlock(block);
		}
		for (final Integer i : TEMP_AIR_LOCATIONS.keySet()) {
			revertAirBlock(i, true);
		}
	}

	public static void removeRevertIndex(final Block block) {
		final Information info = MOVED_EARTH.remove(block);
		if (info != null) {
			if (block.getType() == Material.SANDSTONE && info.getType() == Material.SAND) {
				block.setType(Material.SAND, false);
			}
			if (RaiseEarth.blockInAllAffectedBlocks(block)) {
				EarthAbility.revertBlock(block);
			}
		}
	}

	public static void revertAirBlock(final int i) {
		revertAirBlock(i, false);
	}

	public static void revertAirBlock(final int id, final boolean force) {
		final Information info = TEMP_AIR_LOCATIONS.get(id);
		if (info == null) {
			return;
		}

		final BlockState state = info.getState();
		final Block block = state.getBlock();
		if (!ElementalAbility.isAir(block.getType()) && !block.isLiquid()) {
			if (force || !MOVED_EARTH.containsKey(block)) {
				TEMP_AIR_LOCATIONS.remove(id);
			} else {
				info.setTime(info.getTime() + 10000);
			}
		} else {
			state.update(true, false);
			TEMP_AIR_LOCATIONS.remove(id);
		}
	}

	public static boolean revertBlock(final Block block) {
		if (!isEarthRevertOn()) {
			MOVED_EARTH.remove(block);
			return false;
		}

		final Information info = MOVED_EARTH.remove(block);
		if (info == null) {
			return true;
		}

		final BlockState state = info.getState();
		final Block sourceblock = state.getBlock();
		if (ElementalAbility.isAir(state.getType())) {
			return true;
		}

		if (block.equals(sourceblock)) {
			state.update(true, false);
			if (RaiseEarth.blockInAllAffectedBlocks(sourceblock)) {
				RaiseEarth.revertAffectedBlock(sourceblock);
			}
			if (RaiseEarth.blockInAllAffectedBlocks(block)) {
				RaiseEarth.revertAffectedBlock(block);
			}
			return true;
		} else if (MOVED_EARTH.containsKey(sourceblock)) {
			addTempAirBlock(block);
			return true;
		}

		if (ElementalAbility.isAir(sourceblock.getType()) || sourceblock.isLiquid()) {
			state.update(true, false);
		}

		if (GeneralMethods.isAdjacentToThreeOrMoreSources(block, false)) {
			final Levelled data = (Levelled) Material.WATER.createBlockData();
			data.setLevel(7);
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
		return true;
	}

	/**
	 * Gets the noise reduction amount for this ability. This is used to reduce the noise
	 * produced by {@link #moveEarth}. Higher values mean less noise
	 * @return the amount of noise reduction
	 */
	public int getNoiseReduction() {
		return noiseReduction;
	}

	/**
	 * Sets the noise reduction amount for this ability. This is used to reduce the noise
	 * produced by {@link #moveEarth}. Higher values mean less noise
	 * @param reduceEarthNoise the amount of noise reduction
	 */
	public void setNoiseReduction(int reduceEarthNoise) {
		this.noiseReduction = Math.max(0, reduceEarthNoise); //Don't let it be less than 0
	}

	public double applyMetalPowerFactor(double value, Block source) {
		return !isMetalbendable(source) ? value : value * getConfig().getDouble("Properties.Earth.MetalPowerFactor", 1.5D);
	}

	public static void stopBending() {
		DensityShift.removeAll();

		if (isEarthRevertOn()) {
			removeAllEarthbendedBlocks();
		}
	}
}

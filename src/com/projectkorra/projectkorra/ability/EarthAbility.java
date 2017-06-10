package com.projectkorra.projectkorra.ability;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.RaiseEarth;
import com.projectkorra.projectkorra.earthbending.lava.LavaFlow;
import com.projectkorra.projectkorra.earthbending.passive.EarthPassive;
import com.projectkorra.projectkorra.earthbending.sand.SandSpout;
import com.projectkorra.projectkorra.firebending.Illumination;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.Information;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.ParticleEffect.ParticleData;
import com.projectkorra.projectkorra.util.TempBlock;

public abstract class EarthAbility extends ElementalAbility {

	private static final HashSet<Block> PREVENT_EARTHBENDING = new HashSet<Block>();
	private static final Map<Block, Information> MOVED_EARTH = new ConcurrentHashMap<Block, Information>();
	private static final Map<Integer, Information> TEMP_AIR_LOCATIONS = new ConcurrentHashMap<Integer, Information>();
	private static final ArrayList<Block> PREVENT_PHYSICS = new ArrayList<Block>();

	public EarthAbility(Player player) {
		super(player);
	}

	public int getEarthbendableBlocksLength(Block block, Vector direction, int maxlength) {
		Location location = block.getLocation();
		direction = direction.normalize();
		for (int i = 0; i <= maxlength; i++) {
			double j = i;
			if (!isEarthbendable(location.clone().add(direction.clone().multiply(j)).getBlock())) {
				return i;
			}
		}
		return maxlength;
	}

	public Block getEarthSourceBlock(double range) {
		return getEarthSourceBlock(player, getName(), range);
	}

	@Override
	public Element getElement() {
		return Element.EARTH;
	}

	public Block getLavaSourceBlock(double range) {
		return getLavaSourceBlock(player, getName(), range);
	}

	public Block getTargetEarthBlock(int range) {
		return getTargetEarthBlock(player, range);
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
	public void handleCollision(Collision collision) {
		super.handleCollision(collision);
		if (collision.isRemovingFirst()) {
			ParticleData particleData = (ParticleEffect.ParticleData) new ParticleEffect.BlockData(Material.DIRT, (byte) 0);
			ParticleEffect.BLOCK_CRACK.display(particleData, 1F, 1F, 1F, 0.1F, 10, collision.getLocationFirst(), 50);
		}
	}

	public static boolean isEarthbendable(Material material) {
		return isEarth(material) || isMetal(material) || isSand(material) || isLava(material);
	}

	public boolean isEarthbendable(Block block) {
		return isEarthbendable(player, getName(), block);
	}

	public static boolean isEarthbendable(Player player, Block block) {
		return isEarthbendable(player, null, block);
	}

	public boolean isLavabendable(Block block) {
		return isLavabendable(player, block);
	}

	public boolean isMetalbendable(Block block) {
		return isMetalbendable(block.getType());
	}

	public boolean isMetalbendable(Material material) {
		return isMetalbendable(player, material);
	}

	public boolean isSandbendable(Block block) {
		return isSandbendable(block.getType());
	}

	public boolean isSandbendable(Material material) {
		return isSandbendable(player, material);
	}

	public void moveEarth(Block block, Vector direction, int chainlength) {
		moveEarth(block, direction, chainlength, true);
	}

	public boolean moveEarth(Block block, Vector direction, int chainlength, boolean throwplayer) {
		if (isEarthbendable(block) && !GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
			boolean up = false;
			boolean down = false;
			Vector norm = direction.clone().normalize();
			if (norm.dot(new Vector(0, 1, 0)) == 1) {
				up = true;
			} else if (norm.dot(new Vector(0, -1, 0)) == 1) {
				down = true;
			}

			Vector negnorm = norm.clone().multiply(-1);
			Location location = block.getLocation();
			ArrayList<Block> blocks = new ArrayList<Block>();

			for (double j = -2; j <= chainlength; j++) {
				Block checkblock = location.clone().add(negnorm.clone().multiply(j)).getBlock();
				if (!PREVENT_PHYSICS.contains(checkblock)) {
					blocks.add(checkblock);
					PREVENT_PHYSICS.add(checkblock);
				}
			}

			Block affectedblock = location.clone().add(norm).getBlock();
			if (EarthPassive.isPassiveSand(block)) {
				EarthPassive.revertSand(block);
			}
			if (Illumination.isIlluminationTorch(affectedblock) && TempBlock.isTempBlock(affectedblock)) {
				TempBlock.get(affectedblock).revertBlock();
			}

			if (affectedblock == null) {
				return false;
			} else if (isTransparent(affectedblock)) {
				if (throwplayer) {
					for (Entity entity : GeneralMethods.getEntitiesAroundPoint(affectedblock.getLocation(), 1.75)) {
						if (entity instanceof LivingEntity) {
							LivingEntity lentity = (LivingEntity) entity;
							if (lentity.getEyeLocation().getBlockX() == affectedblock.getX() && lentity.getEyeLocation().getBlockZ() == affectedblock.getZ()) {
								if (!(entity instanceof FallingBlock)) {
									entity.setVelocity(norm.clone().multiply(.75));
								}
							}
						} else {
							if (entity.getLocation().getBlockX() == affectedblock.getX() && entity.getLocation().getBlockZ() == affectedblock.getZ()) {
								if (!(entity instanceof FallingBlock)) {
									entity.setVelocity(norm.clone().multiply(.75));
								}
							}
						}
					}
				}
				if (up) {
					Block topblock = affectedblock.getRelative(BlockFace.UP);
					if (topblock.getType() != Material.AIR) {
						GeneralMethods.breakBlock(affectedblock);
					} else if (!affectedblock.isLiquid() && affectedblock.getType() != Material.AIR) {
						moveEarthBlock(affectedblock, topblock);
					}
				} else {
					GeneralMethods.breakBlock(affectedblock);
				}

				moveEarthBlock(block, affectedblock);
				playEarthbendingSound(block.getLocation());

				for (double i = 1; i < chainlength; i++) {
					affectedblock = location.clone().add(negnorm.getX() * i, negnorm.getY() * i, negnorm.getZ() * i).getBlock();
					if (!isEarthbendable(affectedblock)) {
						if (down) {
							if (isTransparent(affectedblock) && !affectedblock.isLiquid() && affectedblock.getType() != Material.AIR) {
								moveEarthBlock(affectedblock, block);
							}
						}
						break;
					}
					if (EarthPassive.isPassiveSand(affectedblock)) {
						EarthPassive.revertSand(affectedblock);
					}
					if (block == null) {
						for (Block checkblock : blocks) {
							PREVENT_PHYSICS.remove(checkblock);
						}
						return false;
					}
					moveEarthBlock(affectedblock, block);
					block = affectedblock;
				}

				int i = chainlength;
				affectedblock = location.clone().add(negnorm.getX() * i, negnorm.getY() * i, negnorm.getZ() * i).getBlock();
				if (!isEarthbendable(affectedblock)) {
					if (down) {
						if (isTransparent(affectedblock) && !affectedblock.isLiquid()) {
							moveEarthBlock(affectedblock, block);
						}
					}
				}
			} else {
				for (Block checkblock : blocks) {
					PREVENT_PHYSICS.remove(checkblock);
				}
				return false;
			}
			for (Block checkblock : blocks) {
				PREVENT_PHYSICS.remove(checkblock);
			}
			return true;
		}
		return false;
	}

	public void moveEarth(Location location, Vector direction, int chainlength) {
		moveEarth(location, direction, chainlength, true);
	}

	public void moveEarth(Location location, Vector direction, int chainlength, boolean throwplayer) {
		moveEarth(location.getBlock(), direction, chainlength, throwplayer);
	}

	/**
	 * Creates a temporary air block.
	 * 
	 * @param block The block to use as a base
	 */
	@SuppressWarnings("deprecation")
	public static void addTempAirBlock(Block block) {
		Information info;

		if (MOVED_EARTH.containsKey(block)) {
			info = MOVED_EARTH.get(block);
			MOVED_EARTH.remove(block);

		} else {
			info = new Information();

			info.setBlock(block);
			info.setState(block.getState());
			info.setData(block.getData());
		}
		block.setType(Material.AIR);
		info.setTime(System.currentTimeMillis());
		TEMP_AIR_LOCATIONS.put(info.getID(), info);
	}

	public static void displaySandParticle(Location loc, float xOffset, float yOffset, float zOffset, float amount, float speed, boolean red) {
		if (amount <= 0)
			return;

		for (int x = 0; x < amount; x++) {
			if (!red) {
				ParticleEffect.ITEM_CRACK.display(new ParticleEffect.ItemData(Material.SAND, (byte) 0), new Vector(((Math.random() - 0.5) * xOffset), ((Math.random() - 0.5) * yOffset), ((Math.random() - 0.5) * zOffset)), speed, loc, 255.0);
				ParticleEffect.ITEM_CRACK.display(new ParticleEffect.ItemData(Material.SANDSTONE, (byte) 0), new Vector(((Math.random() - 0.5) * xOffset), ((Math.random() - 0.5) * yOffset), ((Math.random() - 0.5) * zOffset)), speed, loc, 255.0);
			} else if (red) {
				ParticleEffect.ITEM_CRACK.display(new ParticleEffect.ItemData(Material.SAND, (byte) 1), new Vector(((Math.random() - 0.5) * xOffset), ((Math.random() - 0.5) * yOffset), ((Math.random() - 0.5) * zOffset)), speed, loc, 255.0);
				ParticleEffect.ITEM_CRACK.display(new ParticleEffect.ItemData(Material.RED_SANDSTONE, (byte) 0), new Vector(((Math.random() - 0.5) * xOffset), ((Math.random() - 0.5) * yOffset), ((Math.random() - 0.5) * zOffset)), speed, loc, 255.0);
			}

		}
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
	@SuppressWarnings("deprecation")
	public static Block getEarthSourceBlock(Player player, String abilityName, double range) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		Block testBlock = player.getTargetBlock(getTransparentMaterialSet(), (int) range);
		if (bPlayer == null) {
			return null;
		} else if (isEarthbendable(testBlock.getType())) {
			return testBlock;
		} else if (!isTransparent(player, testBlock)) {
			return null;
		}

		Location location = player.getEyeLocation();
		Vector vector = location.getDirection().clone().normalize();

		for (double i = 0; i <= range; i++) {
			Block block = location.clone().add(vector.clone().multiply(i)).getBlock();
			if (GeneralMethods.isRegionProtectedFromBuild(player, abilityName, location)) {
				continue;
			} else if (isEarthbendable(player, block)) {
				return block;
			}
		}
		return null;
	}

	public static Block getLavaSourceBlock(Player player, double range) {
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
	@SuppressWarnings("deprecation")
	public static Block getLavaSourceBlock(Player player, String abilityName, double range) {
		Location location = player.getEyeLocation();
		Vector vector = location.getDirection().clone().normalize();

		for (double i = 0; i <= range; i++) {
			Block block = location.clone().add(vector.clone().multiply(i)).getBlock();
			if (GeneralMethods.isRegionProtectedFromBuild(player, abilityName, location)) {
				continue;
			}
			if (isLavabendable(player, block)) {
				if (TempBlock.isTempBlock(block)) {
					TempBlock tb = TempBlock.get(block);
					byte full = 0x0;
					if (tb.getState().getRawData() != full && !isLava(tb.getState().getType())) {
						continue;
					}
				}
				return block;
			}
		}
		return null;
	}

	public static double getMetalAugment(double value) {
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
	public static Block getNearbyEarthBlock(Location loc, double radius, int maxVertical) {
		if (loc == null) {
			return null;
		}

		int rotation = 30;
		for (int i = 0; i < radius; i++) {
			Vector tracer = new Vector(i, 1, 0);
			for (int deg = 0; deg < 360; deg += rotation) {
				Location searchLoc = loc.clone().add(tracer);
				Block block = GeneralMethods.getTopBlock(searchLoc, maxVertical);

				if (block != null && isEarthbendable(block.getType())) {
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

	@SuppressWarnings("deprecation")
	public static Block getTargetEarthBlock(Player player, int range) {
		return player.getTargetBlock(getTransparentMaterialSet(), range);
	}

	public static Map<Integer, Information> getTempAirLocations() {
		return TEMP_AIR_LOCATIONS;
	}

	public static boolean isEarthbendable(Player player, String abilityName, Block block) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null || !isEarthbendable(block.getType()) || PREVENT_EARTHBENDING.contains(block) || GeneralMethods.isRegionProtectedFromBuild(player, abilityName, block.getLocation())) {
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

	@SuppressWarnings("deprecation")
	public static boolean isLavabendable(Player player, Block block) {
		byte full = 0x0;
		if (TempBlock.isTempBlock(block)) {
			TempBlock tblock = TempBlock.instances.get(block);
			if (tblock == null || !LavaFlow.getTempLavaBlocks().values().contains(tblock)) {
				return false;
			}
		}
		if (isLava(block) && block.getData() == full) {
			return true;
		}
		return false;
	}

	public static boolean isMetalbendable(Player player, Material material) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		return bPlayer == null ? null : isMetal(material) && bPlayer.canMetalbend();
	}

	public static boolean isSandbendable(Player player, Material material) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		return bPlayer == null ? null : isSand(material) && bPlayer.canSandbend();
	}

	@SuppressWarnings("deprecation")
	public static void moveEarthBlock(Block source, Block target) {
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
			if (info.getState().getRawData() == (byte) 0x1) {
				target.setType(Material.RED_SANDSTONE);
			} else {
				target.setType(Material.SANDSTONE);
			}
		} else if (info.getState().getType() == Material.GRAVEL) {
			target.setType(Material.STONE);
		} else {
			target.setType(info.getState().getType());
			target.setData(info.getState().getRawData());
		}
		
		source.setType(Material.AIR);
	}

	public static void playEarthbendingSound(Location loc) {
		if (getConfig().getBoolean("Properties.Earth.PlaySound")) {
			loc.getWorld().playSound(loc, Sound.valueOf(getConfig().getString("Properties.Earth.EarthSound")), 0, 10);
		}
	}

	public static void playMetalbendingSound(Location loc) {
		if (getConfig().getBoolean("Properties.Earth.PlaySound")) {
			loc.getWorld().playSound(loc, Sound.valueOf(getConfig().getString("Properties.Earth.MetalSound")), 1, 10);
		}
	}

	public static void playSandBendingSound(Location loc) {
		if (getConfig().getBoolean("Properties.Earth.PlaySound")) {
			loc.getWorld().playSound(loc, Sound.valueOf(getConfig().getString("Properties.Earth.SandSound")), 1.5f, 5);
		}
	}

	public static void removeAllEarthbendedBlocks() {
		for (Block block : MOVED_EARTH.keySet()) {
			revertBlock(block);
		}
		for (Integer i : TEMP_AIR_LOCATIONS.keySet()) {
			revertAirBlock(i, true);
		}
	}

	public static void removeRevertIndex(Block block) {
		if (MOVED_EARTH.containsKey(block)) {
			Information info = MOVED_EARTH.get(block);
			if (block.getType() == Material.SANDSTONE && info.getType() == Material.SAND) {
				block.setType(Material.SAND);
			}
			if (RaiseEarth.blockInAllAffectedBlocks(block)) {
				EarthAbility.revertBlock(block);
			}

			MOVED_EARTH.remove(block);
		}
	}

	public static void revertAirBlock(int i) {
		revertAirBlock(i, false);
	}

	public static void revertAirBlock(int i, boolean force) {
		if (!TEMP_AIR_LOCATIONS.containsKey(i)) {
			return;
		}

		Information info = TEMP_AIR_LOCATIONS.get(i);
		Block block = info.getState().getBlock();

		if (block.getType() != Material.AIR && !block.isLiquid()) {
			if (force || !MOVED_EARTH.containsKey(block)) {
				TEMP_AIR_LOCATIONS.remove(i);
			} else {
				info.setTime(info.getTime() + 10000);
			}
			return;
		} else {
			info.getState().update(true);
			TEMP_AIR_LOCATIONS.remove(i);
		}
	}

	@SuppressWarnings("deprecation")
	public static boolean revertBlock(Block block) {
		byte full = 0x0;
		if (!isEarthRevertOn()) {
			MOVED_EARTH.remove(block);
			return false;
		}
		if (MOVED_EARTH.containsKey(block)) {
			Information info = MOVED_EARTH.get(block);
			Block sourceblock = info.getState().getBlock();

			if (info.getState().getType() == Material.AIR) {
				MOVED_EARTH.remove(block);
				return true;
			}

			if (block.equals(sourceblock)) {
				info.getState().update(true);
				if (RaiseEarth.blockInAllAffectedBlocks(sourceblock)) {
					EarthAbility.revertBlock(sourceblock);
				}
				if (RaiseEarth.blockInAllAffectedBlocks(block)) {
					EarthAbility.revertBlock(block);
				}
				MOVED_EARTH.remove(block);
				return true;
			}

			if (MOVED_EARTH.containsKey(sourceblock)) {
				addTempAirBlock(block);
				MOVED_EARTH.remove(block);
				return true;
			}

			if (sourceblock.getType() == Material.AIR || sourceblock.isLiquid()) {
				info.getState().update(true);
			} else {
				//GeneralMethods.dropItems(block,
				//		GeneralMethods.getDrops(block, info.getState().getType(), info.getState().getRawData(), DIAMOND_PICKAXE));
			}

			if (GeneralMethods.isAdjacentToThreeOrMoreSources(block)) {
				block.setType(Material.WATER);
				block.setData(full);
			} else {
				block.setType(Material.AIR);
			}

			if (RaiseEarth.blockInAllAffectedBlocks(sourceblock)) {
				EarthAbility.revertBlock(sourceblock);
			}
			if (RaiseEarth.blockInAllAffectedBlocks(block)) {
				EarthAbility.revertBlock(block);
			}
			MOVED_EARTH.remove(block);
		}
		return true;
	}

	public static void stopBending() {
		EarthPassive.removeAll();

		if (isEarthRevertOn()) {
			removeAllEarthbendedBlocks();
		}
	}

	public static void removeSandSpouts(Location loc, double radius, Player source) {
		SandSpout.removeSpouts(loc, radius, source);
	}

	public static void removeSandSpouts(Location loc, Player source) {
		removeSandSpouts(loc, 1.5, source);
	}
}

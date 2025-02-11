package com.projectkorra.projectkorra;

import com.google.common.io.Files;
import com.google.common.reflect.ClassPath;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.ability.util.CollisionInitializer;
import com.projectkorra.projectkorra.ability.util.CollisionManager;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.ability.util.PassiveManager;
import com.projectkorra.projectkorra.airbending.AirBlast;
import com.projectkorra.projectkorra.airbending.AirShield;
import com.projectkorra.projectkorra.airbending.AirSpout;
import com.projectkorra.projectkorra.airbending.AirSuction;
import com.projectkorra.projectkorra.airbending.AirSwipe;
import com.projectkorra.projectkorra.airbending.util.AirbendingManager;
import com.projectkorra.projectkorra.board.BendingBoardManager;
import com.projectkorra.projectkorra.chiblocking.util.ChiblockingManager;
import com.projectkorra.projectkorra.command.PKCommand;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.EarthBlast;
import com.projectkorra.projectkorra.earthbending.EarthTunnel;
import com.projectkorra.projectkorra.earthbending.passive.EarthPassive;
import com.projectkorra.projectkorra.earthbending.util.EarthbendingManager;
import com.projectkorra.projectkorra.event.AbilityVelocityAffectEntityEvent;
import com.projectkorra.projectkorra.event.BendingReloadEvent;
import com.projectkorra.projectkorra.firebending.FireBlast;
import com.projectkorra.projectkorra.firebending.FireShield;
import com.projectkorra.projectkorra.firebending.combustion.Combustion;
import com.projectkorra.projectkorra.firebending.util.FirebendingManager;
import com.projectkorra.projectkorra.object.Preset;
import com.projectkorra.projectkorra.region.RegionProtection;
import com.projectkorra.projectkorra.storage.DBConnection;
import com.projectkorra.projectkorra.util.ChatUtil;
import com.projectkorra.projectkorra.util.ColoredParticle;
import com.projectkorra.projectkorra.util.LightManager;
import com.projectkorra.projectkorra.util.MovementHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.RevertChecker;
import com.projectkorra.projectkorra.util.TempArmor;
import com.projectkorra.projectkorra.util.TempArmorStand;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.util.TempFallingBlock;
import com.projectkorra.projectkorra.waterbending.WaterManipulation;
import com.projectkorra.projectkorra.waterbending.WaterSpout;
import com.projectkorra.projectkorra.waterbending.blood.Bloodbending;
import com.projectkorra.projectkorra.waterbending.util.WaterbendingManager;
import io.lumine.mythic.lib.UtilityMethods;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class GeneralMethods {

	private static ProjectKorra plugin;

	public GeneralMethods(final ProjectKorra plugin) {
		GeneralMethods.plugin = plugin;
	}

	/**
	 * Checks to see if an AbilityExists. Uses method
	 * {@link CoreAbility#getAbility(String)} to check if it exists.
	 *
	 * @param string Ability Name
	 * @return true if ability exists
	 */
	public static boolean abilityExists(final String string) {
		return CoreAbility.getAbility(string) != null;
	}

	/**
	 * Deprecated. Use {@link BendingPlayer#bindAbility(String)} instead
	 * @param sender The player
	 * @param name The ability name
	 */
	@Deprecated
	public static void bindAbility(Player sender, String name) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(sender);
		if (bPlayer != null) bPlayer.bindAbility(name);
	}

	/**
	 * Deprecated. Use {@link BendingPlayer#bindAbility(String, int)} instead
	 * @param sender The player
	 * @param name The ability name
	 * @param slot The slot
	 */
	@Deprecated
	public static void bindAbility(Player sender, String name, int slot) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(sender);
		if (bPlayer != null) bPlayer.bindAbility(name, slot);
	}

	/**
	 * This method was used for the old collision detection system. Please see
	 * {@link Collision} for the new system.
	 * <p>
	 * Cycles through a list of ability names to check if any instances of the
	 * abilities exist at a specific location. If an instance of the ability is
	 * found then it will be removed, with the exception FireShield, and
	 * AirShield.
	 */
	@Deprecated
	public static boolean blockAbilities(final Player player, final List<String> abilitiesToBlock, final Location loc, final double radius) {
		boolean hasBlocked = false;
		for (final String ability : abilitiesToBlock) {
			if (ability.equalsIgnoreCase("FireBlast")) {
				hasBlocked = FireBlast.annihilateBlasts(loc, radius, player) || hasBlocked;
			} else if (ability.equalsIgnoreCase("EarthBlast")) {
				hasBlocked = EarthBlast.annihilateBlasts(loc, radius, player) || hasBlocked;
			} else if (ability.equalsIgnoreCase("WaterManipulation")) {
				hasBlocked = WaterManipulation.annihilateBlasts(loc, radius, player) || hasBlocked;
			} else if (ability.equalsIgnoreCase("AirSwipe")) {
				hasBlocked = AirSwipe.removeSwipesAroundPoint(loc, radius) || hasBlocked;
			} else if (ability.equalsIgnoreCase("AirBlast")) {
				hasBlocked = AirBlast.removeAirBlastsAroundPoint(loc, radius) || hasBlocked;
			} else if (ability.equalsIgnoreCase("AirSuction")) {
				hasBlocked = AirSuction.removeAirSuctionsAroundPoint(loc, radius) || hasBlocked;
			} else if (ability.equalsIgnoreCase("Combustion")) {
				hasBlocked = Combustion.removeAroundPoint(loc, radius) || hasBlocked;
			} else if (ability.equalsIgnoreCase("FireShield")) {
				hasBlocked = FireShield.isWithinShield(loc) || hasBlocked;
			} else if (ability.equalsIgnoreCase("AirShield")) {
				hasBlocked = AirShield.isWithinShield(loc) || hasBlocked;
			} else if (ability.equalsIgnoreCase("WaterSpout")) {
				hasBlocked = WaterSpout.removeSpouts(loc, radius, player) || hasBlocked;
			} else if (ability.equalsIgnoreCase("AirSpout")) {
				hasBlocked = AirSpout.removeSpouts(loc, radius, player) || hasBlocked;
			} else if (ability.equalsIgnoreCase("Twister")) {
				// hasBlocked = AirCombo.removeAroundPoint(player, "Twister", loc, radius) || hasBlocked;
			} else if (ability.equalsIgnoreCase("AirStream")) {
				// hasBlocked = AirCombo.removeAroundPoint(player, "AirStream", loc, radius) || hasBlocked;
			} else if (ability.equalsIgnoreCase("AirSweep")) {
				// hasBlocked = AirCombo.removeAroundPoint(player, "AirSweep", loc, radius) || hasBlocked;
			} else if (ability.equalsIgnoreCase("FireKick")) {
				// hasBlocked = FireCombo.removeAroundPoint(player, "FireKick", loc, radius) || hasBlocked;
			} else if (ability.equalsIgnoreCase("FireSpin")) {
				// hasBlocked = FireCombo.removeAroundPoint(player, "FireSpin", loc, radius) || hasBlocked;
			} else if (ability.equalsIgnoreCase("FireWheel")) {
				// hasBlocked = FireCombo.removeAroundPoint(player, "FireWheel", loc, radius) || hasBlocked;
			}
		}
		return hasBlocked;
	}

	/**
	 * Breaks a block and sets it to {@link Material#AIR AIR}.
	 *
	 * @param block The block to break
	 */
	public static void breakBlock(final Block block) {
		block.breakNaturally(new ItemStack(Material.AIR));
	}

	public static boolean canView(final Player player, final String ability) {
		return player.hasPermission("bending.ability." + ability);
	}

	/**
	 * Checks if there is a diagonal wall in front of the location in the given
	 * direction, in any 2 dimensions and all 3
	 *
	 * @param location spot to check
	 * @param direction which way to check
	 * @return true if diagonal wall is found
	 */
	public static boolean checkDiagonalWall(final Location location, final Vector direction) {
		final boolean[] xyzsolid = { false, false, false };
		for (int i = 0; i < 3; i++) {
			double value;
			if (i == 0) {
				value = direction.getX();
			} else if (i == 1) {
				value = direction.getY();
			} else {
				value = direction.getZ();
			}
			final BlockFace face = GeneralMethods.getBlockFaceFromValue(i, value);
			if (face == null) {
				continue;
			}
			xyzsolid[i] = location.getBlock().getRelative(face).getType().isSolid();
		}
		final boolean a = xyzsolid[0] && xyzsolid[2];
		final boolean b = xyzsolid[0] && xyzsolid[1];
		final boolean c = xyzsolid[1] && xyzsolid[2];
		return (a || b || c || (a && b));
	}

	public static int compareArmor(Material first, Material second) {
		return getArmorTier(first) - getArmorTier(second);
	}

	@Deprecated
	public static void displayColoredParticle(final Location loc, ParticleEffect type, final String hexVal, final float xOffset, final float yOffset, final float zOffset) {
		int r = 0;
		int g = 0;
		int b = 0;
		if (hexVal.length() <= 6) {
			r = Integer.valueOf(hexVal.substring(0, 2), 16);
			g = Integer.valueOf(hexVal.substring(2, 4), 16);
			b = Integer.valueOf(hexVal.substring(4, 6), 16);
		} else if (hexVal.length() <= 7 && hexVal.charAt(0) == '#') {
			r = Integer.valueOf(hexVal.substring(1, 3), 16);
			g = Integer.valueOf(hexVal.substring(3, 5), 16);
			b = Integer.valueOf(hexVal.substring(5, 7), 16);
		}
		float red = r / 255.0F;
		final float green = g / 255.0F;
		final float blue = b / 255.0F;
		if (red <= 0) {
			red = 1 / 255.0F;
		}
		loc.setX(loc.getX() + (Math.random() * 2 - 1) * xOffset);
		loc.setY(loc.getY() + (Math.random() * 2 - 1) * yOffset);
		loc.setZ(loc.getZ() + (Math.random() * 2 - 1) * zOffset);

		if (type != ParticleEffect.RED_DUST && type != ParticleEffect.REDSTONE && type != ParticleEffect.SPELL_MOB && type != ParticleEffect.MOB_SPELL && type != ParticleEffect.SPELL_MOB_AMBIENT && type != ParticleEffect.MOB_SPELL_AMBIENT) {
			type = ParticleEffect.RED_DUST;
		}
		type.display(loc, 0, red, green, blue);
	}

	@Deprecated
	public static void displayColoredParticle(final Location loc, final String hexVal) {
		displayColoredParticle(loc, ParticleEffect.RED_DUST, hexVal, 0, 0, 0);
	}

	@Deprecated
	public static void displayColoredParticle(final Location loc, final String hexVal, final float xOffset, final float yOffset, final float zOffset) {
		displayColoredParticle(loc, ParticleEffect.RED_DUST, hexVal, xOffset, yOffset, zOffset);
	}

	public static void displayColoredParticle(String hexVal, final Location loc, final int amount, final double offsetX, final double offsetY, final double offsetZ) {
		int r = 0;
		int g = 0;
		int b = 0;

		if (hexVal.startsWith("#")) {
			hexVal = hexVal.substring(1);
		}

		if (hexVal.length() <= 6) {
			r = Integer.valueOf(hexVal.substring(0, 2), 16).intValue();
			g = Integer.valueOf(hexVal.substring(2, 4), 16).intValue();
			b = Integer.valueOf(hexVal.substring(4, 6), 16).intValue();
		}

		new ColoredParticle(Color.fromRGB(r, g, b), 1F).display(loc, amount, offsetX, offsetY, offsetZ);
	}

	public static void displayColoredParticle(final String hexVal, final Location loc) {
		displayColoredParticle(hexVal, loc, 1, 0, 0, 0);
	}

	/**
	 * Drops a {@code Collection<ItemStack>} of items on a specified block.
	 *
	 * @param block The block to drop items on.
	 * @param items The items to drop.
	 */
	public static void dropItems(final Block block, final Collection<ItemStack> items) {
		for (final ItemStack item : items) {
			if(item.getType() != Material.AIR) {
				block.getWorld().dropItem(block.getLocation(), item);
			}
		}
	}


	@Deprecated
	public static void displayMovePreview(final Player player) {
		ChatUtil.displayMovePreview(player);
	}

	/**
	 * Deprecated. Use {@link ChatUtil#displayMovePreview(Player, int)} instead
	 * @param player The player
	 * @param slot The slot
	 */
	@Deprecated
	public static void displayMovePreview(final Player player, final int slot) {
		ChatUtil.displayMovePreview(player, slot);
	}

	/**
	 * Gets the number of absorption hearts of a specified {@link Player}.
	 * @param player the {@link Player} to get the absorption hearts of.
	 * @deprecated Use Player#getAbsorptionAmount instead.
	 */
	@Deprecated
	public static float getAbsorbationHealth(final Player player) {
		return (float) player.getAbsorptionAmount();
	}

	/**
	 * Sets the number of absorption hearts of a specified {@link Player}.
	 * @param player the {@link Player} to set the absorption hearts of.
	 * @param hearts a float representing the number of hearts to set.
	 * @deprecated Use Player#setAbsorbationHealth instead.
	 */
	@Deprecated
	public static void setAbsorbationHealth(final Player player, final float hearts) {
		player.setAbsorptionAmount(hearts);
	}

	public static int getArmorTier(Material mat) {
		switch (mat) {
			case NETHERITE_HELMET:
			case NETHERITE_CHESTPLATE:
			case NETHERITE_LEGGINGS:
			case NETHERITE_BOOTS:
				return 7;
			case DIAMOND_HELMET:
			case DIAMOND_CHESTPLATE:
			case DIAMOND_LEGGINGS:
			case DIAMOND_BOOTS:
				return 6;
			case TURTLE_HELMET:
				return 5;
			case IRON_HELMET:
			case IRON_CHESTPLATE:
			case IRON_LEGGINGS:
			case IRON_BOOTS:
				return 4;
			case CHAINMAIL_HELMET:
			case CHAINMAIL_CHESTPLATE:
			case CHAINMAIL_LEGGINGS:
			case CHAINMAIL_BOOTS:
				return 3;
			case GOLDEN_HELMET:
			case GOLDEN_CHESTPLATE:
			case GOLDEN_LEGGINGS:
			case GOLDEN_BOOTS:
				return 2;
			case LEATHER_HELMET:
			case LEATHER_CHESTPLATE:
			case LEATHER_LEGGINGS:
			case LEATHER_BOOTS:
				return 1;
			default:
				return 0;
		}
	}

	public static int getArmorIndex(Material mat) {
		switch (mat) {
			case NETHERITE_HELMET:
			case DIAMOND_HELMET:
			case TURTLE_HELMET:
			case IRON_HELMET:
			case CHAINMAIL_HELMET:
			case GOLDEN_HELMET:
			case LEATHER_HELMET:
				return 3;
			case NETHERITE_CHESTPLATE:
			case DIAMOND_CHESTPLATE:
			case IRON_CHESTPLATE:
			case CHAINMAIL_CHESTPLATE:
			case GOLDEN_CHESTPLATE:
			case LEATHER_CHESTPLATE:
				return 2;
			case NETHERITE_LEGGINGS:
			case DIAMOND_LEGGINGS:
			case IRON_LEGGINGS:
			case CHAINMAIL_LEGGINGS:
			case GOLDEN_LEGGINGS:
			case LEATHER_LEGGINGS:
				return 1;
			case NETHERITE_BOOTS:
			case DIAMOND_BOOTS:
			case IRON_BOOTS:
			case CHAINMAIL_BOOTS:
			case GOLDEN_BOOTS:
			case LEATHER_BOOTS:
				return 0;
			default:
				return -1;
		}
	}

	/**
	 * This gets the BlockFace in the specified dimension of a certain value
	 *
	 * @param xyz 0 for x, 1 for y, 2 for z
	 * @param value vector value for which direction to check
	 * @return BlockFace for block in specified dimension and value
	 */
	public static BlockFace getBlockFaceFromValue(final int xyz, final double value) {
		switch (xyz) {
			case 0:
				if (value > 0) {
					return BlockFace.EAST;
				} else if (value < 0) {
					return BlockFace.WEST;
				} else {
					return BlockFace.SELF;
				}
			case 1:
				if (value > 0) {
					return BlockFace.UP;
				} else if (value < 0) {
					return BlockFace.DOWN;
				} else {
					return BlockFace.SELF;
				}
			case 2:
				if (value > 0) {
					return BlockFace.SOUTH;
				} else if (value < 0) {
					return BlockFace.NORTH;
				} else {
					return BlockFace.SELF;
				}
			default:
				return null;
		}
	}

	public static List<Block> getBlocksAlongLine(final Location ploc, final Location tloc, final World w) {
		final List<Block> blocks = new ArrayList<Block>();

		// Next we will name each coordinate
		final int x1 = ploc.getBlockX();
		final int y1 = ploc.getBlockY();
		final int z1 = ploc.getBlockZ();

		final int x2 = tloc.getBlockX();
		final int y2 = tloc.getBlockY();
		final int z2 = tloc.getBlockZ();

		// Then we create the following integers
		int xMin, yMin, zMin;
		int xMax, yMax, zMax;
		int x, y, z;

		// Now we need to make sure xMin is always lower then xMax
		if (x1 > x2) { // If x1 is a higher number then x2
			xMin = x2;
			xMax = x1;
		} else {
			xMin = x1;
			xMax = x2;
		}
		// Same with Y
		if (y1 > y2) {
			yMin = y2;
			yMax = y1;
		} else {
			yMin = y1;
			yMax = y2;
		}

		// And Z
		if (z1 > z2) {
			zMin = z2;
			zMax = z1;
		} else {
			zMin = z1;
			zMax = z2;
		}

		// Now it's time for the loop
		for (x = xMin; x <= xMax; x++) {
			for (y = yMin; y <= yMax; y++) {
				for (z = zMin; z <= zMax; z++) {
					final Block b = new Location(w, x, y, z).getBlock();
					blocks.add(b);
				}
			}
		}

		// And last but not least, we return with the list
		return blocks;
	}

	/**
	 * Gets a {@code List<Blocks>} within the specified radius around the
	 * specified location.
	 *
	 * @param location The base location
	 * @param radius The block radius from location to include within the list
	 *            of blocks
	 * @return The list of Blocks
	 */
	public static List<Block> getBlocksAroundPoint(final Location location, final double radius) {
		final List<Block> blocks = new ArrayList<Block>();

		final int xorg = location.getBlockX();
		final int yorg = location.getBlockY();
		final int zorg = location.getBlockZ();

		final int r = (int) radius * 4;

		for (int x = xorg - r; x <= xorg + r; x++) {
			for (int y = yorg - r; y <= yorg + r; y++) {
				for (int z = zorg - r; z <= zorg + r; z++) {
					final Block block = location.getWorld().getBlockAt(x, y, z);
					if (block.getLocation().distanceSquared(location) <= radius * radius) {
						blocks.add(block);
					}
				}
			}
		}
		return blocks;
	}

	public static BlockFace getCardinalDirection(final Vector vector) {
		final BlockFace[] faces = { BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST };
		Vector n, ne, e, se, s, sw, w, nw;
		w = new Vector(-1, 0, 0);
		n = new Vector(0, 0, -1);
		s = n.clone().multiply(-1);
		e = w.clone().multiply(-1);
		ne = n.clone().add(e.clone()).normalize();
		se = s.clone().add(e.clone()).normalize();
		nw = n.clone().add(w.clone()).normalize();
		sw = s.clone().add(w.clone()).normalize();

		final Vector[] vectors = { n, ne, e, se, s, sw, w, nw };

		double comp = 0;
		int besti = 0;
		for (int i = 0; i < vectors.length; i++) {
			final double dot = vector.dot(vectors[i]);
			if (dot > comp) {
				comp = dot;
				besti = i;
			}
		}
		return faces[besti];
	}

	public static List<Location> getCircle(final Location loc, final int radius, final int height, final boolean hollow, final boolean sphere, final int plusY) {
		final List<Location> circleblocks = new ArrayList<Location>();
		final int cx = loc.getBlockX();
		final int cy = loc.getBlockY();
		final int cz = loc.getBlockZ();

		for (int x = cx - radius; x <= cx + radius; x++) {
			for (int z = cz - radius; z <= cz + radius; z++) {
				for (int y = (sphere ? cy - radius : cy); y < (sphere ? cy + radius : cy + height); y++) {
					final double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0);

					if (dist < radius * radius && !(hollow && dist < (radius - 1) * (radius - 1))) {
						final Location l = new Location(loc.getWorld(), x, y + plusY, z);
						circleblocks.add(l);
					}
				}
			}
		}
		return circleblocks;
	}

	/**
	 * Gets the closest entity within the specified radius around a point
	 * @param center point to check around
	 * @param radius distance from center to check within
	 * @return null if not found
	 */
	public static Entity getClosestEntity(Location center, double radius) {
		Entity found = null;
		Double distance = null;

		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(center, radius)) {
			double check = center.distanceSquared(entity.getLocation());

			if (distance == null || check < distance) {
				found = entity;
				distance = check;
			}
		}

		return found;
	}

	/**
	 * Gets the closest LivingEntity within the specified radius around a point
	 * @param center point to check around
	 * @param radius distance from center to check within
	 * @return null if not found
	 */
	public static LivingEntity getClosestLivingEntity(Location center, double radius) {
		LivingEntity le = null;
		Double distance = null;

		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(center, radius)) {
			double check = center.distanceSquared(entity.getLocation());

			if (entity instanceof LivingEntity && (distance == null || check < distance)) {
				le = (LivingEntity) entity;
				distance = check;
			}
		}

		return le;
	}

	public static String getCurrentDate() {
		final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		final Date date = new Date();
		return dateFormat.format(date);
	}

	public static Vector getDirection(final Location location, final Location destination) {
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

	public static double getDistanceFromLine(final Vector line, final Location pointonline, final Location point) {
		final Vector AP = new Vector();
		double Ax, Ay, Az;
		Ax = pointonline.getX();
		Ay = pointonline.getY();
		Az = pointonline.getZ();

		double Px, Py, Pz;
		Px = point.getX();
		Py = point.getY();
		Pz = point.getZ();

		AP.setX(Px - Ax);
		AP.setY(Py - Ay);
		AP.setZ(Pz - Az);

		return (AP.crossProduct(line).length()) / (line.length());
	}

	/**
	 * Gets a {@code Collection<ItemStack>} of item drops from a single block.
	 *
	 * @param block The single block
	 * @param type The Material type to change the block into
	 * @param data The block data to change the block into
	 * @return The item drops fromt the specified block
	 */
	public static Collection<ItemStack> getDrops(final Block block, final Material type, final BlockData data) {
		final BlockState tempstate = block.getState();
		block.setType(type);
		block.setBlockData(data);
		final Collection<ItemStack> item = block.getDrops();
		tempstate.update(true);
		return item;
	}

	/**
	 * Gets a {@code List<Entity>} of entities around a specified radius from
	 * the specified area
	 *
	 * @param location The base location
	 * @param radius The radius of blocks to look for entities from the location
	 * @param acceptable A function that determines if an entity is acceptable or not to be a part of this list
	 * @return A list of entities around a point
	 */
	public static List<Entity> getEntitiesAroundPoint(final Location location, final double radius, Predicate<Entity> acceptable) {
		return new ArrayList<>(location.getWorld().getNearbyEntities(location, radius, radius, radius, acceptable));
	}

	/**
	 * Gets a {@code List<Entity>} of entities around a specified radius from
	 * the specified area. Excludes dead entities, marker armorstands and spectators
	 *
	 * @param location The base location
	 * @param radius The radius of blocks to look for entities from the location
	 * @return A list of entities around a point
	 */
	public static List<Entity> getEntitiesAroundPoint(final Location location, final double radius) {
		return getEntitiesAroundPoint(location, radius, getEntityFilter());
	}

	/**
	 * Get the filter used to filter out dead entities, immune entities, marker armorstands and spectators
	 * @return The filter
	 */
	public static Predicate<Entity> getEntityFilter() {
		return entity -> !(!entity.isValid() || entity.hasMetadata ("BendingImmunity")
				|| (entity instanceof Player && ((Player) entity).getGameMode().equals(GameMode.SPECTATOR))
				|| (entity instanceof ArmorStand && ((ArmorStand) entity).isMarker()));
	}

	public static long getGlobalCooldown() {
		return ConfigManager.defaultConfig.get().getLong("Properties.GlobalCooldown");
	}

	/**
	 *
	 * @param one One location being tested
	 * @param two Another location being tested
	 * @return The horizontal distance between two locations
	 */
	public static double getHorizontalDistance(final Location one, final Location two) {
		final double x = one.getX() - two.getX();
		final double z = one.getZ() - two.getZ();
		return Math.sqrt((x * x) + (z * z));
	}

	public static int getIntCardinalDirection(final Vector vector) {
		final BlockFace face = getCardinalDirection(vector);

		switch (face) {
			case SOUTH:
				return 7;
			case SOUTH_WEST:
				return 6;
			case WEST:
				return 3;
			case NORTH_WEST:
				return 0;
			case NORTH:
				return 1;
			case NORTH_EAST:
				return 2;
			case EAST:
				return 5;
			case SOUTH_EAST:
				return 8;
			default:
				return 4;
		}
	}

	public static Plugin getItems() {
		if (hasItems()) {
			return Bukkit.getServer().getPluginManager().getPlugin("ProjectKorraItems");
		}
		return null;
	}

	/**
	 * Returns the last ability used by a player. Also checks if a combo was
	 * used.
	 *
	 * @param player The player to check
	 * @return name of last ability used
	 */
	public static String getLastUsedAbility(final Player player, final boolean checkCombos) {
		final List<AbilityInformation> lastUsedAbility = ComboManager.getRecentlyUsedAbilities(player, 1);
		if (!lastUsedAbility.isEmpty()) {
			if (ComboManager.checkForValidCombo(player) != null && checkCombos) {
				return ComboManager.checkForValidCombo(player).getName();
			} else {
				return lastUsedAbility.get(0).getAbilityName();
			}
		}
		return null;
	}

	/**
	 * Gets a location with a specified distance away from the left side of a
	 * location.
	 *
	 * @param location The origin location
	 * @param distance The distance to the left
	 * @return the location of the distance to the left
	 */
	public static Location getLeftSide(final Location location, final double distance) {
		final float angle = (float) Math.toRadians(location.getYaw());
		return location.clone().add(new Vector(Math.cos(angle), 0, Math.sin(angle)).normalize().multiply(distance));
	}

	public static int getMaxPresets(final Player player) {
		final int max = ConfigManager.getConfig().getInt("Properties.MaxPresets");
		if (player.isOp()) {
			return max;
		}
		for (int i = max; i > 0; i--) {
			if (player.hasPermission("bending.command.preset.create." + i)) {
				return i;
			}
		}
		return 0;
	}

	public static Vector getOrthogonalVector(final Vector axis, final double degrees, final double length) {
		Vector ortho = new Vector(axis.getY(), -axis.getX(), 0);
		ortho = ortho.normalize();
		ortho = ortho.multiply(length);

		return rotateVectorAroundVector(axis, ortho, degrees);
	}

	public static Collection<Player> getPlayersAroundPoint(final Location location, final double distance) {
		final Collection<Player> players = new HashSet<Player>();
		for (final Player player : Bukkit.getOnlinePlayers()) {
			if (player.getLocation().getWorld().equals(location.getWorld())) {
				if (player.getLocation().distanceSquared(location) <= distance * distance) {
					players.add(player);
				}
			}
		}
		return players;
	}

	public static Location getPointOnLine(final Location origin, final Location target, final double distance) {
		return origin.clone().add(getDirection(origin, target).normalize().multiply(distance));
	}

	/**
	 * Returns a location with a specified distance away from the right side of
	 * a location.
	 *
	 * @param location The origin location
	 * @param distance The distance to the right
	 * @return the location of the distance to the right
	 */
	public static Location getRightSide(final Location location, final double distance) {
		final float angle = (float) Math.toRadians(location.getYaw());
		return location.clone().subtract(new Vector(Math.cos(angle), 0, Math.sin(angle)).normalize().multiply(distance));
	}

	public static Location getMainHandLocation(final Player player) {
		double y = 1.2 - (player.isSneaking() ? 0.4 : 0);
		if (player.getMainHand() == MainHand.LEFT) {
			return GeneralMethods.getLeftSide(player.getLocation(), .55).add(0, y, 0)
					.add(player.getLocation().getDirection().multiply(0.8));
		} else {
			return GeneralMethods.getRightSide(player.getLocation(), .55).add(0, y, 0)
					.add(player.getLocation().getDirection().multiply(0.8));
		}
	}

	public static Location getOffHandLocation(final Player player) {
		double y = 1.2 - (player.isSneaking() ? 0.4 : 0);
		if (player.getMainHand() == MainHand.RIGHT) {
			return GeneralMethods.getLeftSide(player.getLocation(), .55).add(0, y, 0)
					.add(player.getLocation().getDirection().multiply(0.8));
		} else {
			return GeneralMethods.getRightSide(player.getLocation(), .55).add(0, y, 0)
					.add(player.getLocation().getDirection().multiply(0.8));
		}
	}

	public static Plugin getProbending() {
		if (hasProbending()) {
			return Bukkit.getServer().getPluginManager().getPlugin("Probending");
		}
		return null;
	}

	public static Plugin getRPG() {
		if (hasRPG()) {
			return Bukkit.getServer().getPluginManager().getPlugin("ProjectKorraRPG");
		}
		return null;
	}

	public static Plugin getSpirits() {
		if (hasSpirits()) {
			return Bukkit.getServer().getPluginManager().getPlugin("ProjectKorraSpirits");
		}
		return null;
	}

	public static BlockData getLavaData(final int level) {
		return Material.LAVA.createBlockData(d -> ((Levelled) d).setLevel((level < 0 || level > ((Levelled) d).getMaximumLevel()) ? 0 : level));
	}

	public static BlockData getWaterData(final int level) {
		return Material.WATER.createBlockData(d -> ((Levelled) d).setLevel((level < 0 || level > ((Levelled) d).getMaximumLevel()) ? 0 : level));
	}
	
	public static BlockData getCauldronData(final Material material, final int level) {
		if (!material.name().contains("CAULDRON")) {
			return null;
		}
		return material.createBlockData(d -> ((Levelled) d).setLevel((level > 3 || level > ((Levelled) d).getMaximumLevel()) ? 3 : level < 1 ? 1 : level));
	}
	
	public static void setCauldronData(final Block block, final int level) {
		if (block.getBlockData() instanceof Levelled) {
			Levelled levelled = (Levelled) block.getBlockData();
			if (level >= 1 && level < 3) {
				levelled.setLevel(level);
				block.setBlockData(levelled);
			} else if (level < 1) {
				block.setType(Material.CAULDRON);
			}
		}
		return;
	}

	public static Entity getTargetedEntity(final Player player, final double range, final List<Entity> avoid) {
		double longestr = range + 1;
		Entity target = null;
		final Location origin = player.getEyeLocation();
		final Vector direction = player.getEyeLocation().getDirection().normalize();
		for (final Entity entity : getEntitiesAroundPoint(origin, range)) {
			if (entity instanceof Player) {
				if (((Player) entity).isDead() || ((Player) entity).getGameMode().equals(GameMode.SPECTATOR)) {
					continue;
				}
			}
			if (avoid.contains(entity)) {
				continue;
			}
			if (entity.getWorld().equals(origin.getWorld())) {
				if (entity.getLocation().distanceSquared(origin) < longestr * longestr && getDistanceFromLine(direction, origin, entity.getLocation()) < 2 && (entity instanceof LivingEntity) && entity.getEntityId() != player.getEntityId() && entity.getLocation().distanceSquared(origin.clone().add(direction)) < entity.getLocation().distanceSquared(origin.clone().add(direction.clone().multiply(-1)))) {
					target = entity;
					longestr = entity.getLocation().distance(origin);
				}
			}
		}
		if (target != null) {
			if (GeneralMethods.isObstructed(origin, target.getLocation())) {
				target = null;
			}
		}
		return target;
	}

	public static Entity getTargetedEntity(final Player player, final double range) {
		return getTargetedEntity(player, range, new ArrayList<Entity>());
	}

	public static Location getTargetedLocation(final Player player, final double range, final boolean ignoreTempBlocks, final boolean checkDiagonals, final Material... nonOpaque2) {
		final Location origin = player.getEyeLocation();
		final Vector direction = origin.getDirection();

		final HashSet<Material> trans = new HashSet<Material>();
		trans.add(Material.AIR);
		trans.add(Material.CAVE_AIR);
		trans.add(Material.VOID_AIR);

		if (nonOpaque2 != null) {
			Collections.addAll(trans, nonOpaque2);
		}

		final Location location = origin.clone();
		final Vector vec = direction.normalize().multiply(0.2);

		for (double i = 0; i < range; i += 0.2) {
			location.add(vec);

			if (checkDiagonals && checkDiagonalWall(location, vec)) {
				location.subtract(vec);
				break;
			}

			final Block block = location.getBlock();

			if (trans.contains(block.getType())) {
				continue;
			} else if (ignoreTempBlocks && (TempBlock.isTempBlock(block) && !WaterAbility.isBendableWaterTempBlock(block) && !EarthAbility.isBendableEarthTempBlock(block))) {
				continue;
			} else {
				location.subtract(vec);
				break;
			}
		}

		return location;
	}

	public static Location getTargetedLocation(final Player player, final double range, final boolean ignoreTempBlocks, final Material... nonOpaque2) {
		return getTargetedLocation(player, range, ignoreTempBlocks, true, nonOpaque2);
	}

	public static Location getTargetedLocation(final Player player, final double range, final Material... nonOpaque2) {
		return getTargetedLocation(player, range, false, nonOpaque2);
	}

	public static Location getTargetedLocation(final Player player, final int range) {
		return getTargetedLocation(player, range, false);
	}

	public static Block getTopBlock(final Location loc, final int range) {
		return getTopBlock(loc, range, range);
	}

	/**
	 * Returns the top block based around loc. PositiveY is the maximum amount
	 * of distance it will check upward. Similarly, negativeY is for downward.
	 */
	public static Block getTopBlock(final Location loc, final int positiveY, final int negativeY) {
		Block blockHolder = loc.getBlock();
		int y = 0;
		// Only one of these while statements will go
		while (!ElementalAbility.isAir(blockHolder.getType()) && Math.abs(y) < Math.abs(positiveY)) {
			y++;
			final Block tempBlock = loc.clone().add(0, y, 0).getBlock();
			if (ElementalAbility.isAir(tempBlock.getType())) {
				return blockHolder;
			}
			blockHolder = tempBlock;
		}

		while (ElementalAbility.isAir(blockHolder.getType()) && Math.abs(y) < Math.abs(negativeY)) {
			y--;
			blockHolder = loc.clone().add(0, y, 0).getBlock();
			if (!ElementalAbility.isAir(blockHolder.getType())) {
				return blockHolder;
			}
		}
		return blockHolder;
	}

	public static Block getBottomBlock(final Location loc, final int positiveY, final int negativeY) {
		Block blockHolder = loc.getBlock();
		int y = 0;
		// Only one of these while statements will go
		while (!ElementalAbility.isAir(blockHolder.getType()) && Math.abs(y) < Math.abs(negativeY)) {
			y--;
			final Block tempblock = loc.clone().add(0, y, 0).getBlock();
			if (ElementalAbility.isAir(tempblock.getType())) {
				return blockHolder;
			}

			blockHolder = tempblock;
		}

		while (!ElementalAbility.isAir(blockHolder.getType()) && Math.abs(y) < Math.abs(positiveY)) {
			y++;
			blockHolder = loc.clone().add(0, y, 0).getBlock();
			if (ElementalAbility.isAir(blockHolder.getType())) {
				return blockHolder;
			}
		}

		return blockHolder;
	}

	public static ArrayList<Element> getElementsWithNoWeaponBending() {
		final ArrayList<Element> elements = new ArrayList<Element>();

		if (!plugin.getConfig().getBoolean("Properties.Air.CanBendWithWeapons")) {
			elements.add(Element.AIR);
		}
		if (!plugin.getConfig().getBoolean("Properties.Water.CanBendWithWeapons")) {
			elements.add(Element.WATER);
		}
		if (!plugin.getConfig().getBoolean("Properties.Earth.CanBendWithWeapons")) {
			elements.add(Element.EARTH);
		}
		if (!plugin.getConfig().getBoolean("Properties.Fire.CanBendWithWeapons")) {
			elements.add(Element.FIRE);
		}
		if (!plugin.getConfig().getBoolean("Properties.Chi.CanBendWithWeapons")) {
			elements.add(Element.CHI);
		}

		return elements;
	}

	public static boolean hasItems() {
		return Bukkit.getServer().getPluginManager().getPlugin("ProjectKorraItems") != null;
	}

	public static boolean hasProbending() {
		return Bukkit.getServer().getPluginManager().getPlugin("Probending") != null;
	}

	public static boolean hasRPG() {
		return Bukkit.getServer().getPluginManager().getPlugin("ProjectKorraRPG") != null;
	}

	public static boolean hasSpirits() {
		return Bukkit.getServer().getPluginManager().getPlugin("ProjectKorraSpirits") != null;
	}

	public static boolean isArmor(Material mat) {
		switch (mat) {
			case NETHERITE_HELMET:
			case NETHERITE_CHESTPLATE:
			case NETHERITE_LEGGINGS:
			case NETHERITE_BOOTS:
			case DIAMOND_HELMET:
			case DIAMOND_CHESTPLATE:
			case DIAMOND_LEGGINGS:
			case DIAMOND_BOOTS:
			case TURTLE_HELMET:
			case IRON_HELMET:
			case IRON_CHESTPLATE:
			case IRON_LEGGINGS:
			case IRON_BOOTS:
			case CHAINMAIL_HELMET:
			case CHAINMAIL_CHESTPLATE:
			case CHAINMAIL_LEGGINGS:
			case CHAINMAIL_BOOTS:
			case GOLDEN_HELMET:
			case GOLDEN_CHESTPLATE:
			case GOLDEN_LEGGINGS:
			case GOLDEN_BOOTS:
			case LEATHER_HELMET:
			case LEATHER_CHESTPLATE:
			case LEATHER_LEGGINGS:
			case LEATHER_BOOTS:
				return true;
			default:
				return false;
		}
	}

	public static boolean isAdjacentToThreeOrMoreSources(final Block block) {
		return isAdjacentToThreeOrMoreSources(block, false);
	}

	public static boolean isAdjacentToThreeOrMoreSources(final Block block, final boolean lava) {
		if (block == null || (TempBlock.isTempBlock(block) && (!lava && !WaterAbility.isBendableWaterTempBlock(block)))) {
			return false;
		}
		int sources = 0;
		final BlockFace[] faces = { BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH };
		for (final BlockFace face : faces) {
			final Block blocki = block.getRelative(face);
			if (lava) {
				if (!(blocki.getType() == Material.LAVA && EarthPassive.canPhysicsChange(blocki))) {
					continue;
				}
			} else {
				if (!((ElementalAbility.isWater(blocki) || ElementalAbility.isIce(blocki)) && WaterManipulation.canPhysicsChange(blocki))) {
					continue;
				}
			}

			//At this point it should either be water or lava
			if (blocki.getBlockData() instanceof Levelled) {
				final Levelled level = (Levelled) blocki.getBlockData();
				if (level.getLevel() == 0) {
					sources++;
				}
			} else { //ice
				sources++;
			}
		}
		return sources >= 2;
	}

	public static boolean isInteractable(final Block block) {
		return isInteractable(block.getType());
	}

	public static boolean isInteractable(final Material material) {
		if (GeneralMethods.getMCVersion() >= 1170 && material == Material.getMaterial("LIGHT")) return false;

		return material.isInteractable();
	}

	public static boolean isObstructed(final Location location1, final Location location2) {
		final Vector loc1 = location1.toVector();
		final Vector loc2 = location2.toVector();

		final Vector direction = loc2.subtract(loc1);
		direction.normalize();

		Location loc;

		double max = 0;
		if (location1.getWorld().equals(location2.getWorld())) {
			max = location1.distance(location2);
		}

		for (double i = 0; i <= max; i++) {
			loc = location1.clone().add(direction.clone().multiply(i));
			final Material type = loc.getBlock().getType();
			if (type != Material.AIR && !(ElementalAbility.getTransparentMaterialSet().contains(type) || ElementalAbility.isWater(loc.getBlock()))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Deprecated. Use {@link RegionProtection#isRegionProtected(Player, Location, CoreAbility)} instead
	 */
	@Deprecated
	public static boolean isRegionProtectedFromBuild(final Player player, final String ability, final Location loc) {
		return RegionProtection.isRegionProtected(player, loc, CoreAbility.getAbility(ability));
	}

	/**
	 * Deprecated. Use {@link RegionProtection#isRegionProtected(Player, Location, CoreAbility)} instead
	 */
	@Deprecated
	public static boolean isRegionProtectedFromBuild(final Ability ability, final Location loc) {
		return RegionProtection.isRegionProtected(ability.getPlayer(), loc, (CoreAbility) ability);
	}

	/**
	 * Deprecated. Use {@link RegionProtection#isRegionProtected(Player, Location, CoreAbility)} instead
	 */
	@Deprecated
	public static boolean isRegionProtectedFromBuild(final Player player, final Location loc) {
		return RegionProtection.isRegionProtected(player, loc);
	}

	public static boolean isSameArmor(Material a, Material b) {
		int ai = getArmorIndex(a), bi = getArmorIndex(b);

		if (ai == -1 || bi == -1) {
			return false;
		}

		return ai == bi;
	}

	public static boolean isSolid(final Block block) {
		return isSolid(block.getType());
	}

	public static boolean isSolid(final Material material) {
		return material.isSolid();
	}

	public static boolean isTransparent(final Block block) {
		return isTransparent(block.getType());
	}

	public static boolean isTransparent(final Material material) {
		return !material.isOccluding() && !material.isSolid();
	}

	public static boolean isFakeEvent(final EntityDamageEvent event) {
		if (Bukkit.getPluginManager().isPluginEnabled("MythicLib")) {
			return UtilityMethods.isFakeEvent(event);
		}
		return false;
	}

	/** Checks if an entity is Undead **/
	public static boolean isUndead(final Entity entity) {
		if (entity == null) {
			return false;
		}

		switch (entity.getType()) {
			case SKELETON:
			case STRAY:
			case WITHER_SKELETON:
			case WITHER:
			case ZOMBIE:
			case HUSK:
			case ZOMBIE_VILLAGER:
			case ZOMBIFIED_PIGLIN:
			case ZOGLIN:
			case DROWNED:
			case ZOMBIE_HORSE:
			case SKELETON_HORSE:
			case PHANTOM:
				return true;
			default:
				return false;
		}
	}

	public static boolean isWeapon(final Material mat) {

		switch(mat) {
			case BOW:
			case CROSSBOW:
			case DIAMOND_AXE:
			case DIAMOND_HOE:
			case DIAMOND_PICKAXE:
			case DIAMOND_SHOVEL:
			case DIAMOND_SWORD:
			case GOLDEN_AXE:
			case GOLDEN_HOE:
			case GOLDEN_PICKAXE:
			case GOLDEN_SHOVEL:
			case GOLDEN_SWORD:
			case IRON_AXE:
			case IRON_HOE:
			case IRON_PICKAXE:
			case IRON_SHOVEL:
			case IRON_SWORD:
			case NETHERITE_AXE:
			case NETHERITE_HOE:
			case NETHERITE_PICKAXE:
			case NETHERITE_SHOVEL:
			case NETHERITE_SWORD:
			case STONE_AXE:
			case STONE_HOE:
			case STONE_PICKAXE:
			case STONE_SHOVEL:
			case STONE_SWORD:
			case TRIDENT:
			case WOODEN_AXE:
			case WOODEN_HOE:
			case WOODEN_PICKAXE:
			case WOODEN_SHOVEL:
			case WOODEN_SWORD:
				return true;
			default:
				return false;
		}
	}

	public static void reloadPlugin(final CommandSender sender) {
		ProjectKorra.log.info("Reloading ProjectKorra and configuration");
		final BendingReloadEvent event = new BendingReloadEvent(sender);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			sender.sendMessage(ChatColor.RED + "Reload event cancelled");
			return;
		}
		if (DBConnection.isOpen()) {
			DBConnection.sql.close();
		}
		GeneralMethods.stopBending();

		// Reverts all active lights, then restarts the light revert scheduler
		LightManager.get().restart();

		ConfigManager.defaultConfig.reload();
		ConfigManager.languageConfig.reload();
		ConfigManager.presetConfig.reload();
		ConfigManager.avatarStateConfig.reload();
		Arrays.stream(Element.getElements()).forEach(e -> {e.setColor(null); e.setSubColor(null);}); //Load colors from config again
		Arrays.stream(Element.getSubElements()).forEach(e -> {e.setColor(null); e.setSubColor(null);}); //Same for subs
		ElementalAbility.clearBendableMaterials(); // Clear and re-cache the material lists on reload.
		ElementalAbility.setupBendableMaterials();
		// WaterAbility.setupWaterTransformableBlocks();
		EarthTunnel.clearBendableMaterials();

		Bukkit.getScheduler().cancelTasks(ProjectKorra.plugin);
		ProjectKorra.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(ProjectKorra.plugin, new BendingManager(), 0, 1);
		ProjectKorra.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(ProjectKorra.plugin, new AirbendingManager(ProjectKorra.plugin), 0, 1);
		ProjectKorra.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(ProjectKorra.plugin, new WaterbendingManager(ProjectKorra.plugin), 0, 1);
		ProjectKorra.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(ProjectKorra.plugin, new EarthbendingManager(ProjectKorra.plugin), 0, 1);
		ProjectKorra.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(ProjectKorra.plugin, new FirebendingManager(ProjectKorra.plugin), 0, 1);
		ProjectKorra.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(ProjectKorra.plugin, new ChiblockingManager(ProjectKorra.plugin), 0, 1);
		ProjectKorra.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(ProjectKorra.plugin, new BendingManager.TempElementsRunnable(), 20, 20);
		ProjectKorra.plugin.revertChecker = ProjectKorra.plugin.getServer().getScheduler().runTaskTimerAsynchronously(ProjectKorra.plugin, new RevertChecker(ProjectKorra.plugin), 0, 200);

		EarthTunnel.setupBendableMaterials();
		Bloodbending.loadBloodlessFromConfig();
		Preset.loadExternalPresets();
		new MultiAbilityManager();
		new ComboManager();
		PKCommand.reloadCommands();
		// Stop the previous collision detection task before creating new manager.
		ProjectKorra.collisionManager.stopCollisionDetection();
		ProjectKorra.collisionManager = new CollisionManager();
		ProjectKorra.collisionInitializer = new CollisionInitializer(ProjectKorra.collisionManager);
		HandlerList.unregisterAll(ProjectKorra.plugin); //Unregister all listeners registered by addons AND ProjectKorra
		Bukkit.getPluginManager().registerEvents(new PKListener(ProjectKorra.plugin), ProjectKorra.plugin); //Re-register our listener
		CoreAbility.registerAbilities(); //Register all abilities again
		reloadAddonPlugins();  //Register all addons and addon listeners again
		ProjectKorra.collisionInitializer.initializeDefaultCollisions(); // must be called after abilities have been registered.
		ProjectKorra.collisionManager.startCollisionDetection();

		DBConnection.init();

		if (!DBConnection.isOpen()) {
			ProjectKorra.log.severe("Unable to enable ProjectKorra due to the database not being open");
			stopPlugin();
		}
		BendingPlayer.getOfflinePlayers().clear();
		BendingPlayer.getPlayers().clear();
		OfflineBendingPlayer.TEMP_ELEMENTS.clear();
		BendingPlayer.DISABLED_WORLDS = new HashSet<>(ConfigManager.defaultConfig.get().getStringList("Properties.DisabledWorlds"));
		BendingBoardManager.reload();
		for (final Player player : Bukkit.getOnlinePlayers()) {
			Preset.unloadPreset(player);
			OfflineBendingPlayer.loadAsync(player.getUniqueId(), false);
			PassiveManager.registerPassives(player);
		}

		plugin.updater.checkUpdate();
		ProjectKorra.log.info("Reload complete");
	}

	public static void reloadAddonPlugins() {
		for (int i = CoreAbility.getAddonPlugins().size() - 1; i > -1; i--) {
			final String entry = CoreAbility.getAddonPlugins().get(i);
			final String[] split = entry.split("::");
			if (Bukkit.getServer().getPluginManager().isPluginEnabled(split[0])) {
				CoreAbility.registerPluginAbilities((JavaPlugin) Bukkit.getServer().getPluginManager().getPlugin(split[0]), split[1]);
			} else {
				CoreAbility.getAddonPlugins().remove(i);
			}
		}
	}

	public static void removeBlock(final Block block) {
		if (isAdjacentToThreeOrMoreSources(block, false)) {
			block.setType(Material.WATER);
			if (block.getBlockData() instanceof Levelled) {
				((Levelled) block.getBlockData()).setLevel(1);
			}
		} else {
			block.setType(Material.AIR);
		}
	}

	/**
	 * Deprecated. Use {@link BendingPlayer#removeUnusableAbilities()} instead.
	 * @param player The name of the player
	 */
	@Deprecated
	public static void removeUnusableAbilities(final String player) {
		BendingPlayer bendingPlayer = BendingPlayer.getBendingPlayer(player);
		bendingPlayer.removeUnusableAbilities();
	}

	/**
	 * Deprecated. Use {@link BendingPlayer#removeUnusableAbilities()} instead.
	 * @param player The player
	 */
	@Deprecated
	public static void removeUnusableAbilities(final Player player) {
		BendingPlayer bendingPlayer = BendingPlayer.getBendingPlayer(player);
		bendingPlayer.removeUnusableAbilities();
	}

	public static Vector rotateVectorAroundVector(final Vector axis, final Vector rotator, final double degrees) {
		final double angle = Math.toRadians(degrees);
		Vector rotation = axis.clone();
		final Vector rotate = rotator.clone();
		rotation = rotation.normalize();

		final Vector thirdaxis = rotation.crossProduct(rotate).normalize().multiply(rotate.length());

		return rotate.multiply(Math.cos(angle)).add(thirdaxis.multiply(Math.sin(angle)));
	}

	/**
	 * Rotates a vector around the Y plane.
	 */
	public static Vector rotateXZ(final Vector vec, final double theta) {
		final Vector vec2 = vec.clone();
		final double x = vec2.getX();
		final double z = vec2.getZ();
		vec2.setX(x * Math.cos(Math.toRadians(theta)) - z * Math.sin(Math.toRadians(theta)));
		vec2.setZ(x * Math.sin(Math.toRadians(theta)) + z * Math.cos(Math.toRadians(theta)));
		return vec2;
	}

	public static boolean runDebug() {
		final File debugFile = new File(plugin.getDataFolder(), "debug.txt");
		if (debugFile.exists()) {
			String format = "yyyy-MM-dd-HH-mm-ss";
			Instant instant = Instant.ofEpochMilli(debugFile.lastModified());
			LocalDateTime datetime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
			String formatted = datetime.format(DateTimeFormatter.ofPattern(format));
			try {
				Files.move(debugFile, new File(debugFile.getParentFile(), "debug_" + formatted + ".txt"));
			} catch (IOException e) {
				e.printStackTrace();
				debugFile.delete();
			}
		}

		List<String> f = new ArrayList<>();

		f.add("ProjectKorra Debug: Paste this on http://pastie.org and put it in your bug report thread.");
		f.add("====================");
		f.add("");
		f.add("Date Created: " + getCurrentDate());
		f.add("Java Version: " + System.getProperty("java.version"));
		f.add("Bukkit Version: " + Bukkit.getServer().getVersion());
		f.add("");
		f.add("ProjectKorra (Core) Information");
		f.add("====================");
		f.add("Version: " + plugin.getDescription().getVersion());
		f.add("Author: " + plugin.getDescription().getAuthors());
		final List<String> officialSidePlugins = new ArrayList<>();
		if (hasRPG()) {
			officialSidePlugins.add("- ProjectKorra RPG v" + getRPG().getDescription().getVersion());
		}
		if (hasItems()) {
			officialSidePlugins.add("- ProjectKorra Items v" + getItems().getDescription().getVersion());
		}
		if (hasSpirits()) {
			officialSidePlugins.add("- ProjectKorra Spirits v" + getSpirits().getDescription().getVersion());
		}
		if (hasProbending()) {
			officialSidePlugins.add("- Probending v" + getProbending().getDescription().getVersion());
		}
		if (!officialSidePlugins.isEmpty()) {
			f.add("");
			f.add("ProjectKorra (Side Plugin) Information");
			f.add("====================");
			f.addAll(officialSidePlugins);
		}

		f.add("");
		f.add("Supported Plugins");
		f.add("====================");

		for (JavaPlugin plugin : RegionProtection.getActiveProtections().keySet()) {
			if (plugin.isEnabled()) {
				f.add("- " + plugin.getName() + " v" + plugin.getDescription().getVersion());
			}
		}

		Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
		if (papi != null && papi.isEnabled()) {
			f.add("- " + papi.getName() + " v" + papi.getDescription().getVersion());
		}

		f.add("");
		f.add("Plugins Hooking Into ProjectKorra (Core)");
		f.add("====================");

		final String[] pkPlugins = new String[] { "projectkorrarpg", "projectkorraitems", "projectkorraspirits", "probending" };
		for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
			if (plugin.getDescription().getDepend().contains("ProjectKorra") && !Arrays.asList(pkPlugins).contains(plugin.getName().toLowerCase())) {
				f.add("- " + plugin.getDescription().getName() + " v" + plugin.getDescription().getVersion());
			}
		}

		f.add("");
		f.add("Ability Information");
		f.add("====================");
		final ArrayList<String> stockAbils = new ArrayList<String>();
		final ArrayList<String> unofficialAbils = new ArrayList<String>();
		for (final CoreAbility ability : CoreAbility.getAbilities()) {
			if (ability.getClass().getPackage().getName().startsWith("com.projectkorra")) {
				stockAbils.add(ability.getName());
			} else {
				if (ability instanceof AddonAbility) {
					unofficialAbils.add(ChatColor.stripColor(ability.getName() + " v" + ((AddonAbility) ability).getVersion() + " (" + ((AddonAbility) ability).getAuthor() + ")"));
				} else {
					unofficialAbils.add(ChatColor.stripColor(ability.getName() + " (" + ability.getClass().getName() + ")"));
				}
			}
		}
		if (!stockAbils.isEmpty()) {
			Collections.sort(stockAbils);
			for (final String ability : stockAbils) {
				f.add("- " + ability + " (STOCK)");
			}
		}
		if (!unofficialAbils.isEmpty()) {
			Collections.sort(unofficialAbils);
			for (final String ability : unofficialAbils) {
				f.add("- " + ability);
			}
		}

		f.add("");
		f.add("Collection Sizes");
		f.add("====================");
		final ClassLoader loader = ProjectKorra.class.getClassLoader();
		try {
			for (final ClassPath.ClassInfo info : ClassPath.from(loader).getTopLevelClasses()) {
				if (info.getName().startsWith("com.projectkorra.") && !info.getName().contains("hooks")
						&& !info.getName().startsWith("com.projectkorra.projectkorra.region")
						&& !info.getName().startsWith("com.projectkorra.projectkorra.ProjectKorra")) {
					try {
						final Class<?> clazz = info.load();
						for (final Field field : clazz.getDeclaredFields()) {
							final String simpleName = clazz.getSimpleName();
							field.setAccessible(true);
							try {
								final Object obj = field.get(null);
								if (obj instanceof Collection) {
									Collection<?> coll = ((Collection<?>) obj);
									if (coll.size() > 0)
										f.add(simpleName + ": " + field.getName() + " size=" + coll.size());
								} else if (obj instanceof Map) {
									Map<?, ?> map = (Map<?, ?>) obj;
									if (map.size() > 0)
										f.add(simpleName + ": " + field.getName() + " size=" + map.size());
								}
							} catch (final Exception ignored) {}
						}
					}  catch (Exception ignored) {}
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}

		f.add("");
		f.add("CoreAbility Debugger");
		f.add("====================");
		f.addAll(Arrays.asList(CoreAbility.getDebugString().split("\\n")));

		try {
			final File dataFolder = plugin.getDataFolder();
			if (!dataFolder.exists()) {
				dataFolder.mkdir();
			}

			final File saveTo = new File(plugin.getDataFolder(), "debug.txt");
			if (saveTo.exists()) {
				saveTo.delete();
			}
			saveTo.createNewFile();

			final FileWriter fw = new FileWriter(saveTo, true);
			final PrintWriter pw = new PrintWriter(fw);
			for (String line : f) {
				pw.println(line);
			}
			pw.flush();
			pw.close();

			return true;
		} catch (final IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Deprecated. Use {@link OfflineBendingPlayer#saveAbility(String, int)} instead.
	 * @param bPlayer The bending player
	 * @param slot The slot to save the ability to
	 * @param ability The ability to save
	 */
	@Deprecated
	public static void saveAbility(final BendingPlayer bPlayer, final int slot, final String ability) {
		bPlayer.saveAbility(ability, slot);
	}

	/**
	 * Deprecated. Please use {@link BendingPlayer#saveElements()} instead
	 * @param bPlayer The bending player
	 */
	@Deprecated
	public static void saveElements(final BendingPlayer bPlayer) {
		bPlayer.saveElements();
	}

	/**
	 * Deprecated. Please use {@link BendingPlayer#saveSubElements()} instead
	 * @param bPlayer The bending player
	 */
	@Deprecated
	public static void saveSubElements(final BendingPlayer bPlayer) {
		bPlayer.saveSubElements();
	}

	/**
	 * <strong>Literally</strong> does nothing anymore. Instead, just set the permaremove state
	 * inside the bPlayer and it will update
	 * @param bPlayer Stop, this is useless
	 */
	@Deprecated
	public static void savePermaRemoved(final BendingPlayer bPlayer) {}

	public static FallingBlock spawnFallingBlock(final Location loc, final Material type) {
		return spawnFallingBlock(loc, type, type.createBlockData());
	}

	public static FallingBlock spawnFallingBlock(final Location loc, final Material type, final BlockData data) {
		return loc.getWorld().spawnFallingBlock(loc, data);
	}

	public static boolean playerHeadIsInBlock(final Player player, final Block block) {
		return playerHeadIsInBlock(player, block, false);
	}

	public static boolean playerHeadIsInBlock(final Player player, final Block block, final boolean exact) {
		double checkDistance;
		if (exact) {
			checkDistance = 0.5;
		} else {
			checkDistance = 0.75;
		}
		return (player.getEyeLocation().getBlockY() == block.getLocation().getBlockY() && (Math.abs(player.getEyeLocation().getX() - block.getLocation().add(0.5, 0.0, 0.5).getX()) < checkDistance) && (Math.abs(player.getEyeLocation().getZ() - block.getLocation().add(0.5, 0.0, 0.5).getZ()) < checkDistance));
	}

	public static boolean playerFeetIsInBlock(final Player player, final Block block) {
		return playerFeetIsInBlock(player, block, false);
	}

	public static boolean playerFeetIsInBlock(final Player player, final Block block, final boolean exact) {
		double checkDistance;
		if (exact) {
			checkDistance = 0.5;
		} else {
			checkDistance = 0.75;
		}
		return (player.getLocation().getBlockY() == block.getLocation().getBlockY() && (Math.abs(player.getLocation().getX() - block.getLocation().add(0.5, 0.0, 0.5).getX()) < checkDistance) && (Math.abs(player.getLocation().getZ() - block.getLocation().add(0.5, 0.0, 0.5).getZ()) < checkDistance));
	}

	/**
	 * Deprecated. Use {@link com.projectkorra.projectkorra.util.ChatUtil#sendBrandingMessage(CommandSender, String)}
	 */
	@Deprecated
	public static void sendBrandingMessage(final CommandSender sender, final String message) {
		ChatUtil.sendBrandingMessage(sender, message);
	}

	/**
	 * Apply multiple modifiers to a value, using mod multipliers.
	 * E.g. applyModifiers(10, 1.5, 1.5) = 20 and not 10 * 1.5 * 1.5 (22.5)
	 * @param value The value to modify
	 * @param modifiers The modifiers to apply
	 */
	public static double applyModifiers(double value, double... modifiers) {
		double totalModifier = 0;

		for(double mod : modifiers) {
			totalModifier += mod - 1;
		}
		return value * (1 + totalModifier);
	}

	/**
	 * Apply multiple modifiers to a value, using negative mod multipliers.
	 * This should be used instead for values that are better the smaller they are
	 * E.g. applyModifiers(10, 1.5) = 0.667
	 * @param value The value to modify
	 * @param modifiers The modifiers to apply
	 */
	public static double applyInverseModifiers(double value, double... modifiers) {
		double totalMod = 1;
		for (double mod : modifiers) {
			totalMod *= mod;
		}
		return (value / (totalMod == 0 ? 0.0001 : totalMod));
	}

	/**
	 * Apply multiple modifiers to a value, using mod multipliers.
	 * E.g. applyModifiers(10, 1.5, 1.5) = 20 and not 10 * 1.5 * 1.5 (22.5)
	 * @param value The value to modify
	 * @param modifiers The modifiers to apply
	 */
	public static long applyModifiers(long value, double... modifiers) {
		return (long) applyModifiers((double)value, modifiers);
	}

	/**
	 * Apply multiple modifiers to a value, using negative mod multipliers.
	 * This should be used instead for values that are better the smaller they are
	 * E.g. applyModifiers(10, 1.5) = 0.667
	 * @param value The value to modify
	 * @param modifiers The modifiers to apply
	 */
	public static long applyInverseModifiers(long value, double... modifiers) {
		return (long) applyInverseModifiers((double)value, modifiers);
	}

	public static void stopBending() {
		CoreAbility.removeAll();
		EarthAbility.stopBending();
		WaterAbility.stopBending();
		FireAbility.stopBending();

		TempBlock.removeAll();
		TempArmor.revertAll();
		TempArmorStand.removeAll();
		MovementHandler.resetAll();
		MultiAbilityManager.removeAll();
		TempFallingBlock.removeAllFallingBlocks();
	}

	public static void stopPlugin() {
		plugin.getServer().getPluginManager().disablePlugin(plugin);
	}

	public static boolean locationEqualsIgnoreDirection(final Location loc1, final Location loc2) {
		return loc1.getWorld().equals(loc2.getWorld()) && loc1.getX() == loc2.getX() && loc1.getY() == loc2.getY() && loc1.getZ() == loc2.getZ();
	}

	public static boolean isLightEmitting(final Material material) {
		switch (material.name()) {
			case "GLOWSTONE":
			case "TORCH":
			case "SEA_LANTERN":
			case "BEACON":
			case "REDSTONE_LAMP":
			case "REDSTONE_TORCH":
			case "MAGMA_BLOCK":
			case "LAVA":
			case "JACK_O_LANTERN":
			case "CRYING_OBSIDIAN":
			case "SHROOMLIGHT":
			case "CAMPFIRE":
			case "SOUL_CAMPFIRE":
			case "SOUL_TORCH":
			case "LANTERN":
			case "SOUL_LANTERN":
			case "CONDUIT":
			case "RESPAWN_ANCHOR":
			case "BROWN_MUSHROOM":
			case "BREWING_STAND":
			case "ENDER_CHEST":
			case "END_PORTAL_FRAME":
			case "END_ROD":
			case "LIGHT":
			case "AMETHYST_CLUSTER":
			case "CAVE_VINES":
			case "GLOW_LICHEN":
			case "OCHRE_FROGLIGHT":
			case "PEARLESCENT_FROGLIGHT":
			case "VERDANT_FROGLIGHT":
			case "SCULK_CATALYST":
				return true;
			default:
				return false;
		}
	}

	@Deprecated
	public static void setVelocity(Entity entity, Vector vector) {
		setVelocity(null,entity,vector);
	}
	
	public static void setVelocity(Ability ability, Entity entity, Vector vector) {
		final AbilityVelocityAffectEntityEvent event = new AbilityVelocityAffectEntityEvent(ability, entity, vector);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) 
			return;
		
		Vector velocity = event.getVelocity();
		if(velocity == null || Double.isNaN(velocity.length()))
		    return;
		
		if (entity instanceof TNTPrimed) {
			if (ConfigManager.defaultConfig.get().getBoolean("Properties.BendingAffectFallingSand.TNT")) {
				velocity.multiply(ConfigManager.defaultConfig.get().getDouble("Properties.BendingAffectFallingSand.TNTStrengthMultiplier"));
			}
		} else if (entity instanceof FallingBlock) {
			if (ConfigManager.defaultConfig.get().getBoolean("Properties.BendingAffectFallingSand.Normal")) {
				velocity.multiply(ConfigManager.defaultConfig.get().getDouble("Properties.BendingAffectFallingSand.NormalStrengthMultiplier"));
			}
		}

		// Attempt to stop velocity from going over the packet cap.
		if (velocity.getX() > 4) {
			velocity.setX(4);
		} else if (velocity.getX() < -4) {
			velocity.setX(-4);
		}

		if (velocity.getY() > 4) {
			velocity.setY(4);
		} else if (velocity.getY() < -4) {
			velocity.setY(-4);
		}

		if (velocity.getZ() > 4) {
			velocity.setZ(4);
		} else if (velocity.getZ() < -4) {
			velocity.setZ(-4);
		}
		event.getAffected().setVelocity(velocity);
	}

	public static int getMCVersion() {
		String version = Bukkit.getBukkitVersion().split("-", 2)[0];
		if (!version.matches("\\d+\\.\\d+(\\.\\d+)?")) {
			ProjectKorra.log.warning("Version not valid! Cannot parse version \"" + version + "\"");
			return 1164; //1.16.4
		}

		String[] split = version.split("\\.", 3);

		int major = Integer.parseInt(split[0]);
		int minor = 0;
		int fix = 0;

		if (split.length > 1) {
			minor = Integer.parseInt(split[1]);

			if (split.length > 2) {
				fix = Integer.parseInt(split[2]);
			}
		}
		return major * 1000 + minor * 10 + fix; //1.16.4 -> 1164; 1.18 -> 1180
	}
}

package com.projectkorra.ProjectKorra;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Ability.AbilityModule;
import com.projectkorra.ProjectKorra.Ability.AbilityModuleManager;
import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.earthbending.EarthColumn;
import com.projectkorra.ProjectKorra.earthbending.EarthPassive;
import com.projectkorra.ProjectKorra.waterbending.FreezeMelt;
import com.projectkorra.ProjectKorra.waterbending.WaterManipulation;

public class Methods {

	static ProjectKorra plugin;

	public Methods(ProjectKorra plugin) {
		Methods.plugin = plugin;
	}

	public static ConcurrentHashMap<Block, Information> movedearth = new ConcurrentHashMap<Block, Information>();
	public static ConcurrentHashMap<Integer, Information> tempair = new ConcurrentHashMap<Integer, Information>();
	public static ArrayList<Block> tempnophysics = new ArrayList<Block>();

	public static boolean isBender(String player, Element element) {
		BendingPlayer bPlayer = getBendingPlayer(player);
		if (bPlayer.hasElement(element)) return true;
		return false;
	}

	public static BendingPlayer getBendingPlayer(String player) {
		return BendingPlayer.players.get(player);
	}

	public static void createBendingPlayer(UUID uuid, String player) {
		/*
		 * This will run when the player logs in.
		 */
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM pk_players WHERE uuid = '" + uuid.toString() + "'");
		try {
			if (!rs2.next()) { // Data doesn't exist, we want a completely new player.
				new BendingPlayer(uuid, player, new ArrayList<Element>(), new HashMap<Integer, String>(), false);
				DBConnection.sql.modifyQuery("INSERT INTO pk_players (uuid, player) VALUES ('" + uuid.toString() + "', '" + player + "')");
				ProjectKorra.log.info("Created new BendingPlayer for " + player);
			} else {
				// The player has at least played before.
				String player2 = rs2.getString("player");
				if (!player.equalsIgnoreCase(player2)) DBConnection.sql.modifyQuery("UPDATE pk_players SET player = '" + player2 + "' WHERE uuid = '" + uuid.toString() + "'"); // They have changed names.
				String element = rs2.getString("element");
				String permaremoved = rs2.getString("permaremoved");
				String slot1 = rs2.getString("slot1");
				String slot2 = rs2.getString("slot2");
				String slot3 = rs2.getString("slot3");
				String slot4 = rs2.getString("slot4");
				String slot5 = rs2.getString("slot5");
				String slot6 = rs2.getString("slot6");
				String slot7 = rs2.getString("slot7");
				String slot8 = rs2.getString("slot8");
				String slot9 = rs2.getString("slot9");
				boolean p = false;
				ArrayList<Element> elements = new ArrayList<Element>();
				if (element != null) { // Player has an element.
					if (element.contains("a")) elements.add(Element.Air);
					if (element.contains("w")) elements.add(Element.Water);
					if (element.contains("e")) elements.add(Element.Earth);
					if (element.contains("f")) elements.add(Element.Fire);
					if (element.contains("c")) elements.add(Element.Chi);
				}

				HashMap<Integer, String> abilities = new HashMap<Integer, String>();
				if (slot1 != null) abilities.put(1, slot1);
				if (slot2 != null) abilities.put(2, slot2);
				if (slot3 != null) abilities.put(3, slot3);
				if (slot4 != null) abilities.put(4, slot4);
				if (slot5 != null) abilities.put(5, slot5);
				if (slot6 != null) abilities.put(6, slot6);
				if (slot7 != null) abilities.put(7, slot7);
				if (slot8 != null) abilities.put(8, slot8);
				if (slot9 != null) abilities.put(9, slot9);

				if (permaremoved == null) p = false;
				if (permaremoved.equals("true")) p = true;
				if (permaremoved.equals("false")) p = false;
				new BendingPlayer(uuid, player2, elements, abilities, p);
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public static void saveBendingPlayer(String player) {
		BendingPlayer bPlayer = BendingPlayer.players.get(player);
		if (bPlayer == null) return;
		String uuid = bPlayer.uuid.toString();

		StringBuilder elements = new StringBuilder();
		if (bPlayer.hasElement(Element.Air)) elements.append("a");
		if (bPlayer.hasElement(Element.Water)) elements.append("w");
		if (bPlayer.hasElement(Element.Earth)) elements.append("e");
		if (bPlayer.hasElement(Element.Fire)) elements.append("f");
		if (bPlayer.hasElement(Element.Chi)) elements.append("c");

		HashMap<Integer, String> abilities = bPlayer.abilities;

		if (abilities.get(1) != null) DBConnection.sql.modifyQuery("UPDATE pk_players SET slot1 = '" + abilities.get(1) + "' WHERE uuid = '" + uuid + "'");
		if (abilities.get(2) != null) DBConnection.sql.modifyQuery("UPDATE pk_players SET slot2 = '" + abilities.get(2) + "' WHERE uuid = '" + uuid + "'");
		if (abilities.get(3) != null) DBConnection.sql.modifyQuery("UPDATE pk_players SET slot3 = '" + abilities.get(3) + "' WHERE uuid = '" + uuid + "'");
		if (abilities.get(4) != null) DBConnection.sql.modifyQuery("UPDATE pk_players SET slot4 = '" + abilities.get(4) + "' WHERE uuid = '" + uuid + "'");
		if (abilities.get(5) != null) DBConnection.sql.modifyQuery("UPDATE pk_players SET slot5 = '" + abilities.get(5) + "' WHERE uuid = '" + uuid + "'");
		if (abilities.get(6) != null) DBConnection.sql.modifyQuery("UPDATE pk_players SET slot6 = '" + abilities.get(6) + "' WHERE uuid = '" + uuid + "'");
		if (abilities.get(7) != null) DBConnection.sql.modifyQuery("UPDATE pk_players SET slot7 = '" + abilities.get(7) + "' WHERE uuid = '" + uuid + "'");
		if (abilities.get(8) != null) DBConnection.sql.modifyQuery("UPDATE pk_players SET slot8 = '" + abilities.get(8) + "' WHERE uuid = '" + uuid + "'");
		if (abilities.get(9) != null) DBConnection.sql.modifyQuery("UPDATE pk_players SET slot9 = '" + abilities.get(9) + "' WHERE uuid = '" + uuid + "'");


		DBConnection.sql.modifyQuery("UPDATE pk_players SET element = '" + elements + "' WHERE uuid = '" + uuid + "'");
		boolean permaRemoved = bPlayer.permaRemoved;

		if (permaRemoved) {
			DBConnection.sql.modifyQuery("UPDATE pk_players SET permaremoved = 'true' WHERE uuid = '" + uuid + "'");
		} else {
			DBConnection.sql.modifyQuery("UPDATE pk_players SET permaremoved = 'false' WHERE uuid = '" + uuid + "'");
		}
	}

	public static void stopBending() {
		List<AbilityModule> abilities = AbilityModuleManager.ability;
		for (AbilityModule ab: abilities) {
			ab.stop();
		}
		EarthPassive.removeAll();
	}

	public static boolean isSolid(Block block) {
		if (Arrays.asList(nonOpaque).contains(block.getTypeId())) return false;
		return true;
	}

	public static String getAbility(String string) {
		for (String st: AbilityModuleManager.abilities) {
			if (st.equalsIgnoreCase(string)) return st;
		}
		return null;
	}

	public static void bindAbility(Player player, String ability) {
		int slot = player.getInventory().getHeldItemSlot() + 1;
		BendingPlayer bPlayer = getBendingPlayer(player.getName());
		bPlayer.abilities.put(slot, ability);
	}

	public static void bindAbility(Player player, String ability, int slot) {
		BendingPlayer bPlayer = getBendingPlayer(player.getName());
		bPlayer.abilities.put(slot, ability);
	}
	public static boolean abilityExists(String string) {
		if (getAbility(string) == null) return false;
		return true;
	}
	public static List<Entity> getEntitiesAroundPoint(Location location,
			double radius) {

		List<Entity> entities = location.getWorld().getEntities();
		List<Entity> list = location.getWorld().getEntities();

		for (Entity entity : entities) {
			if (entity.getWorld() != location.getWorld()) {
				list.remove(entity);
			} else if (entity.getLocation().distance(location) > radius) {
				list.remove(entity);
			}
		}

		return list;

	}

	public static Entity getTargetedEntity(Player player, double range,
			List<Entity> avoid) {
		double longestr = range + 1;
		Entity target = null;
		Location origin = player.getEyeLocation();
		Vector direction = player.getEyeLocation().getDirection().normalize();
		for (Entity entity : origin.getWorld().getEntities()) {
			if (avoid.contains(entity))
				continue;
			if (entity.getLocation().distance(origin) < longestr
					&& getDistanceFromLine(direction, origin,
							entity.getLocation()) < 2
							&& (entity instanceof LivingEntity)
							&& entity.getEntityId() != player.getEntityId()
							&& entity.getLocation().distance(
									origin.clone().add(direction)) < entity
									.getLocation().distance(
											origin.clone().add(
													direction.clone().multiply(-1)))) {
				target = entity;
				longestr = entity.getLocation().distance(origin);
			}
		}
		return target;
	}

	public static boolean isAbilityInstalled(String name, String author) {
		String ability = getAbility(name);
		if (ability == null) return false;
		if (AbilityModuleManager.authors.get(name).equalsIgnoreCase(author)) return true;
		return false;
	}

	public static double getDistanceFromLine(Vector line, Location pointonline,
			Location point) {

		Vector AP = new Vector();
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

	public static boolean canBend(String player, String ability) {
		BendingPlayer bPlayer = getBendingPlayer(player);
		Player p = Bukkit.getPlayer(player);
		if (!bPlayer.isToggled) return false;
		if (p == null) return false;
		if (!p.hasPermission("bending.ability." + ability)) return false;
		if (isAirAbility(ability) && !isBender(player, Element.Air)) return false;
		if (isWaterAbility(ability) && !isBender(player, Element.Water)) return false;
		if (isEarthAbility(ability) && !isBender(player, Element.Earth)) return false;
		if (isFireAbility(ability) && !isBender(player, Element.Fire)) return false;
		if (isChiAbility(ability) && !isBender(player, Element.Chi)) return false;
		return true;
	}

	public static boolean canBendPassive(String player, Element element) {
		BendingPlayer bPlayer = getBendingPlayer(player);
		if (!bPlayer.isToggled) return false;
		if (!bPlayer.hasElement(element)) return false;
		return true;
	}

	public static boolean isAirAbility(String ability) {
		return AbilityModuleManager.airbendingabilities.contains(ability);
	}

	public static boolean isWaterAbility(String ability) {
		return AbilityModuleManager.waterbendingabilities.contains(ability);
	}

	public static boolean isEarthAbility(String ability) {
		return AbilityModuleManager.earthbendingabilities.contains(ability);
	}

	public static boolean isFireAbility(String ability) {
		return AbilityModuleManager.firebendingabilities.contains(ability);
	}

	public static boolean isChiAbility(String ability) {
		return AbilityModuleManager.chiabilities.contains(ability);
	}

	public static ChatColor getAbilityColor(String ability) {
		if (AbilityModuleManager.chiabilities.contains(ability)) return ChatColor.GOLD;
		if (AbilityModuleManager.airbendingabilities.contains(ability)) return ChatColor.GRAY;
		if (AbilityModuleManager.waterbendingabilities.contains(ability)) return ChatColor.AQUA;
		if (AbilityModuleManager.earthbendingabilities.contains(ability)) return ChatColor.GREEN;
		if (AbilityModuleManager.firebendingabilities.contains(ability)) return ChatColor.RED;
		else return null;
	}

	public static boolean isWater(Block block) {
		if (block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER) return true;
		return false;
	}

	public static String getBoundAbility(Player player) {
		BendingPlayer bPlayer = getBendingPlayer(player.getName());

		int slot = player.getInventory().getHeldItemSlot() + 1;
		return bPlayer.abilities.get(slot);
	}

	public static boolean isWaterbendable(Block block, Player player) {
		byte full = 0x0;
		if (TempBlock.isTempBlock(block)) return false;
		if ((block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER) && block.getData() == full) return true;
		if (block.getType() == Material.ICE || block.getType() == Material.SNOW || block.getType() == Material.PACKED_ICE) return true;
		if (canPlantbend(player) && isPlant(block)) return true;
		return false;
	}

	public static boolean canPlantbend(Player player) {
		return player.hasPermission("bending.ability.plantbending");
	}

	public static boolean isPlant(Block block) {
		if (Arrays.asList(plantIds).contains(block.getTypeId())) return true;
		return false;
	}

	public static boolean isAdjacentToThreeOrMoreSources(Block block) {
		if (TempBlock.isTempBlock(block))
			return false;
		int sources = 0;
		byte full = 0x0;
		BlockFace[] faces = { BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH,
				BlockFace.SOUTH };
		for (BlockFace face : faces) {
			Block blocki = block.getRelative(face);
			if ((blocki.getType() == Material.WATER || blocki.getType() == Material.STATIONARY_WATER)
					&& blocki.getData() == full
					&& WaterManipulation.canPhysicsChange(blocki))
				sources++;
			if (FreezeMelt.frozenblocks.containsKey(blocki)) {
				if (FreezeMelt.frozenblocks.get(blocki) == full)
					sources++;
			} else if (blocki.getType() == Material.ICE) {
				sources++;
			}
		}
		if (sources >= 2)
			return true;
		return false;
	}

	public static List<Block> getBlocksAroundPoint(Location location, double radius) {
		List<Block> blocks = new ArrayList<Block>();

		int xorg = location.getBlockX();
		int yorg = location.getBlockY();
		int zorg = location.getBlockZ();

		int r = (int) radius * 4;

		for (int x = xorg - r; x <= xorg + r; x++) {
			for (int y = yorg - r; y <= yorg + r; y++) {
				for (int z = zorg - r; z <= zorg + r; z++) {
					Block block = location.getWorld().getBlockAt(x, y, z);
					// if
					// (block.getLocation().distance(originblock.getLocation())
					// <= radius) {
					if (block.getLocation().distance(location) <= radius) {
						blocks.add(block);
					}
				}
			}
		}

		return blocks;
	}

	public static boolean isEarthbendable(Player player, Block block) {
		Material material = block.getType();

		for (String s: plugin.getConfig().getStringList("Properties.Earth.EarthbendableBlocks")) {
			if (material == Material.getMaterial(s)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isChiBlocked(String player) {
		long currTime = System.currentTimeMillis();
		long duration = ProjectKorra.plugin.getConfig().getLong("Abilities.Chi.Passive.BlockChi.Duration");
		if (BendingPlayer.blockedChi.contains(player)) {
			if (BendingPlayer.blockedChi.get(player) + duration >= currTime) {
				BendingPlayer.blockedChi.remove(player);
				return false;
			}
		}
		return true;
	}

	public static Vector rotateVectorAroundVector(Vector axis, Vector rotator,
			double degrees) {
		double angle = Math.toRadians(degrees);
		Vector rotation = axis.clone();
		Vector rotate = rotator.clone();
		rotation = rotation.normalize();

		Vector thirdaxis = rotation.crossProduct(rotate).normalize()
				.multiply(rotate.length());

		return rotate.multiply(Math.cos(angle)).add(
				thirdaxis.multiply(Math.sin(angle)));

		// return new Vector(x, z, y);
	}

	public static Vector getOrthogonalVector(Vector axis, double degrees,
			double length) {

		Vector ortho = new Vector(axis.getY(), -axis.getX(), 0);
		ortho = ortho.normalize();
		ortho = ortho.multiply(length);

		return rotateVectorAroundVector(axis, ortho, degrees);

	}

	public static boolean isWeapon(Material mat) {
		if (mat == null) return false;
		if (mat == Material.WOOD_AXE || mat == Material.WOOD_PICKAXE
				|| mat == Material.WOOD_SPADE || mat == Material.WOOD_SWORD

				|| mat == Material.STONE_AXE || mat == Material.STONE_PICKAXE
				|| mat == Material.STONE_SPADE || mat == Material.STONE_SWORD

				|| mat == Material.IRON_AXE || mat == Material.IRON_PICKAXE
				|| mat == Material.IRON_SWORD || mat == Material.IRON_SPADE

				|| mat == Material.DIAMOND_AXE || mat == Material.DIAMOND_PICKAXE
				|| mat == Material.DIAMOND_SWORD || mat == Material.DIAMOND_SPADE)
			return true;
		return false;
	}

	public static boolean isTransparentToEarthbending(Player player, Block block) {
		if (Arrays.asList(transparentToEarthbending).contains(block.getTypeId())) return true;
		return false;
	}

	public static double firebendingDayAugment(double value, World world) {
		if (isDay(world)) {
			return plugin.getConfig().getDouble("Properties.Fire.DayFactor") * value;
		}
		return value;
	}

	public static double getFirebendingDayAugment(World world) {
		if (isDay(world)) return plugin.getConfig().getDouble("Properties.Fire.DayFactor");
		return 1;
	}

	public static Block getEarthSourceBlock(Player player, double range) {
		Block testblock = player.getTargetBlock(getTransparentEarthbending(),
				(int) range);
		if (isEarthbendable(player, testblock))
			return testblock;
		Location location = player.getEyeLocation();
		Vector vector = location.getDirection().clone().normalize();
		for (double i = 0; i <= range; i++) {
			Block block = location.clone().add(vector.clone().multiply(i))
					.getBlock();
			//			if (isRegionProtectedFromBuild(player, Abilities.RaiseEarth,
			//					location))
			//				continue;
			if (isEarthbendable(player, block)) {
				return block;
			}
		}
		return null;
	}

	public static int getEarthbendableBlocksLength(Player player, Block block,
			Vector direction, int maxlength) {
		Location location = block.getLocation();
		direction = direction.normalize();
		double j;
		for (int i = 0; i <= maxlength; i++) {
			j = (double) i;
			if (!isEarthbendable(player,
					location.clone().add(direction.clone().multiply(j))
					.getBlock())) {
				return i;
			}
		}
		return maxlength;
	}

	public static boolean isMeltable(Block block) {
		if (block.getType() == Material.ICE || block.getType() == Material.SNOW || block.getType() == Material.PACKED_ICE) {
			return true;
		}
		return false;
	}

	public static Block getWaterSourceBlock(Player player, double range,
			boolean plantbending) {
		Location location = player.getEyeLocation();
		Vector vector = location.getDirection().clone().normalize();
		for (double i = 0; i <= range; i++) {
			Block block = location.clone().add(vector.clone().multiply(i))
					.getBlock();
			//			if (isRegionProtectedFromBuild(player, Abilities.WaterManipulation,
			//					location))
			//				continue;
			if (isWaterbendable(block, player)
					&& (!isPlant(block) || plantbending)) {
				if (TempBlock.isTempBlock(block)) {
					TempBlock tb = TempBlock.get(block);
					byte full = 0x0;
					if (tb.state.getRawData() != full
							&& (tb.state.getType() != Material.WATER || tb.state
							.getType() != Material.STATIONARY_WATER)) {
						continue;
					}
				}
				return block;
			}
		}
		return null;
	}

	public static boolean isObstructed(Location location1, Location location2) {
		Vector loc1 = location1.toVector();
		Vector loc2 = location2.toVector();

		Vector direction = loc2.subtract(loc1);
		direction.normalize();

		Location loc;

		double max = location1.distance(location2);

		for (double i = 0; i <= max; i++) {
			loc = location1.clone().add(direction.clone().multiply(i));
			Material type = loc.getBlock().getType();
			if (type != Material.AIR
					&& !Arrays.asList(transparentToEarthbending).contains(
							type.getId()))
				return true;
		}

		return false;
	}

	public static boolean isAdjacentToFrozenBlock(Block block) {
		BlockFace[] faces = { BlockFace.DOWN, BlockFace.UP, BlockFace.NORTH,
				BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH };
		boolean adjacent = false;
		if (Methods.isAbilityInstalled("PhaseChange", "orion304")) {
			for (BlockFace face : faces) {
				if (FreezeMelt.frozenblocks.containsKey((block.getRelative(face))))
					adjacent = true;
			}
		}
		return adjacent;
	}



	public static void playFocusWaterEffect(Block block) {
		block.getWorld().playEffect(block.getLocation(), Effect.SMOKE, 4, 20);
	}

	public static void removeRevertIndex(Block block) {
		if (movedearth.containsKey(block)) {
			Information info = movedearth.get(block);
			if (block.getType() == Material.SANDSTONE
					&& info.getType() == Material.SAND)
				block.setType(Material.SAND);
			if (EarthColumn.blockInAllAffectedBlocks(block))
				EarthColumn.revertBlock(block);

			EarthColumn.resetBlock(block);

			movedearth.remove(block);
		}
	}

	public static double waterbendingNightAugment(double value, World world) {
		if (isNight(world)) {
			return plugin.getConfig().getDouble("Properties.Water.NightFactor") * value;
		}
		return value;
	}

	public static double getWaterbendingNightAugment(World world) {
		if (isNight(world)) return plugin.getConfig().getDouble("Properties.Water.NightFactor");
		return 1;
	}

	public static boolean isNight(World world) {
		if (world.getEnvironment() == Environment.NETHER || world.getEnvironment() == Environment.THE_END) {
			return false;
		}

		long time = world.getTime();
		if (time >= 12950 && time <= 23050) {
			return true;
		}
		return false;
	}

	public static Location getPointOnLine(Location origin, Location target,
			double distance) {
		return origin.clone().add(
				getDirection(origin, target).normalize().multiply(distance));

	}

	public static void breakBlock(Block block) {
		block.breakNaturally(new ItemStack(Material.AIR));
	}

	public static boolean canBeBloodbent(Player player) {
		if (AvatarState.isAvatarState(player))
			return false;
		if ((isChiBlocked(player.getName())))
			return true;
		if (canBend(player.getName(), "Bloodbending") && Methods.getBendingPlayer(player.getName()).isToggled)
			return false;
		return true;
	}

	public static void moveEarth(Player player, Location location,
			Vector direction, int chainlength) {
		moveEarth(player, location, direction, chainlength, true);
	}

	public static void moveEarth(Player player, Location location,
			Vector direction, int chainlength, boolean throwplayer) {
		Block block = location.getBlock();
		moveEarth(player, block, direction, chainlength, throwplayer);
	}

	public static void moveEarth(Player player, Block block, Vector direction,
			int chainlength) {
		moveEarth(player, block, direction, chainlength, true);
	}

	public static boolean moveEarth(Player player, Block block,
			Vector direction, int chainlength, boolean throwplayer) {
		if (isEarthbendable(player, block)) {
			//				&& !isRegionProtectedFromBuild(player, Abilities.RaiseEarth,
			//						block.getLocation())) {

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
				Block checkblock = location.clone()
						.add(negnorm.clone().multiply(j)).getBlock();
				if (!tempnophysics.contains(checkblock)) {
					blocks.add(checkblock);
					tempnophysics.add(checkblock);
				}
			}

			Block affectedblock = location.clone().add(norm).getBlock();
			if (EarthPassive.isPassiveSand(block)) {
				EarthPassive.revertSand(block);
			}
			// if (block.getType() == Material.SAND) {
			// block.setType(Material.SANDSTONE);
			// }

			if (affectedblock == null)
				return false;
			if (isTransparentToEarthbending(player, affectedblock)) {
				if (throwplayer) {
					for (Entity entity : getEntitiesAroundPoint(
							affectedblock.getLocation(), 1.75)) {
						if (entity instanceof LivingEntity) {
							LivingEntity lentity = (LivingEntity) entity;
							if (lentity.getEyeLocation().getBlockX() == affectedblock
									.getX()
									&& lentity.getEyeLocation().getBlockZ() == affectedblock
									.getZ())
								if (!(entity instanceof FallingBlock))
									entity.setVelocity(norm.clone().multiply(
											.75));
						} else {
							if (entity.getLocation().getBlockX() == affectedblock
									.getX()
									&& entity.getLocation().getBlockZ() == affectedblock
									.getZ())
								if (!(entity instanceof FallingBlock))
									entity.setVelocity(norm.clone().multiply(
											.75));
						}
					}

				}

				if (up) {
					Block topblock = affectedblock.getRelative(BlockFace.UP);
					if (topblock.getType() != Material.AIR) {
						breakBlock(affectedblock);
					} else if (!affectedblock.isLiquid()
							&& affectedblock.getType() != Material.AIR) {
						// affectedblock.setType(Material.GLASS);
						moveEarthBlock(affectedblock, topblock);
					}
				} else {
					breakBlock(affectedblock);
				}

				// affectedblock.setType(block.getType());
				// affectedblock.setData(block.getData());
				//
				// addTempEarthBlock(block, affectedblock);
				moveEarthBlock(block, affectedblock);
				block.getWorld().playEffect(block.getLocation(),
						Effect.GHAST_SHOOT, 0, 4);

				for (double i = 1; i < chainlength; i++) {
					affectedblock = location
							.clone()
							.add(negnorm.getX() * i, negnorm.getY() * i,
									negnorm.getZ() * i).getBlock();
					if (!isEarthbendable(player, affectedblock)) {
						// verbose(affectedblock.getType());
						if (down) {
							if (isTransparentToEarthbending(player,
									affectedblock)
									&& !affectedblock.isLiquid()
									&& affectedblock.getType() != Material.AIR) {
								moveEarthBlock(affectedblock, block);
							}
						}
						// if (!Tools.adjacentToThreeOrMoreSources(block)
						// && Tools.isWater(block)) {
						// block.setType(Material.AIR);
						// } else {
						// byte full = 0x0;
						// block.setType(Material.WATER);
						// block.setData(full);
						// }
						break;
					}
					if (EarthPassive.isPassiveSand(affectedblock)) {
						EarthPassive.revertSand(affectedblock);
					}
					// if (affectedblock.getType() == Material.SAND) {
					// affectedblock.setType(Material.SANDSTONE);
					// }
					if (block == null) {
						for (Block checkblock : blocks) {
							tempnophysics.remove(checkblock);
						}
						return false;
					}
					// block.setType(affectedblock.getType());
					// block.setData(affectedblock.getData());
					// addTempEarthBlock(affectedblock, block);
					moveEarthBlock(affectedblock, block);
					block = affectedblock;
				}

				int i = chainlength;
				affectedblock = location
						.clone()
						.add(negnorm.getX() * i, negnorm.getY() * i,
								negnorm.getZ() * i).getBlock();
				if (!isEarthbendable(player, affectedblock)) {
					if (down) {
						if (isTransparentToEarthbending(player, affectedblock)
								&& !affectedblock.isLiquid()) {
							moveEarthBlock(affectedblock, block);
						}
					}
				}

			} else {
				for (Block checkblock : blocks) {
					tempnophysics.remove(checkblock);
				}
				return false;
			}
			for (Block checkblock : blocks) {
				tempnophysics.remove(checkblock);
			}
			return true;
		}
		return false;
	}


	public static void moveEarthBlock(Block source, Block target) {
		byte full = 0x0;
		Information info;
		if (movedearth.containsKey(source)) {
			// verbose("Moving something already moved.");
			info = movedearth.get(source);
			info.setTime(System.currentTimeMillis());
			movedearth.remove(source);
			movedearth.put(target, info);
		} else {
			// verbose("Moving something for the first time.");
			info = new Information();
			info.setBlock(source);
			// info.setType(source.getType());
			// info.setData(source.getData());
			info.setTime(System.currentTimeMillis());
			info.setState(source.getState());
			movedearth.put(target, info);
		}

		if (isAdjacentToThreeOrMoreSources(source)) {
			source.setType(Material.WATER);
			source.setData(full);
		} else {
			source.setType(Material.AIR);
		}
		if (info.getState().getType() == Material.SAND) {
			target.setType(Material.SANDSTONE);
		} else {
			target.setType(info.getState().getType());
			target.setData(info.getState().getRawData());
		}
	}

	public static void addTempAirBlock(Block block) {
		if (movedearth.containsKey(block)) {
			Information info = movedearth.get(block);
			block.setType(Material.AIR);
			info.setTime(System.currentTimeMillis());
			movedearth.remove(block);
			tempair.put(info.getID(), info);
		} else {
			Information info = new Information();
			info.setBlock(block);
			// info.setType(block.getType());
			// info.setData(block.getData());
			info.setState(block.getState());
			info.setTime(System.currentTimeMillis());
			block.setType(Material.AIR);
			tempair.put(info.getID(), info);
		}

	}

	public static Vector getDirection(Location location, Location destination) {
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

	public static HashSet<Byte> getTransparentEarthbending() {
		HashSet<Byte> set = new HashSet<Byte>();
		for (int i : transparentToEarthbending) {
			set.add((byte) i);
		}
		return set;
	}

	public static void damageEntity(Player player, Entity entity, double damage) {
		if (entity instanceof LivingEntity) {
			((LivingEntity) entity).damage(damage, player);
			((LivingEntity) entity)
			.setLastDamageCause(new EntityDamageByEntityEvent(player,
					entity, DamageCause.CUSTOM, damage));
		}
	}

	public static boolean isDay(World world) {
		long time = world.getTime();
		if (time >= 23500 || time <= 12500) {
			return true;
		}
		return false;
	}

	public static int getIntCardinalDirection(Vector vector) {
		BlockFace face = getCardinalDirection(vector);

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
		}

		return 4;

	}

	public static Collection<ItemStack> getDrops(Block block, Material type,
			byte data, ItemStack breakitem) {
		BlockState tempstate = block.getState();
		block.setType(type);
		block.setData(data);
		Collection<ItemStack> item = block.getDrops();
		tempstate.update(true);
		return item;
	}

	public static void dropItems(Block block, Collection<ItemStack> items) {
		for (ItemStack item : items)
			block.getWorld().dropItem(block.getLocation(), item);
	}

	public static Location getTargetedLocation(Player player, int range) {
		return getTargetedLocation(player, range, 0);
	}

	public static Location getTargetedLocation(Player player,
			double originselectrange, Integer... nonOpaque2) {
		Location origin = player.getEyeLocation();
		Vector direction = origin.getDirection();

		HashSet<Byte> trans = new HashSet<Byte>();
		trans.add((byte) 0);

		if (nonOpaque2 == null) {
			trans = null;
		} else {
			for (int i : nonOpaque2) {
				trans.add((byte) i);
			}
		}

		Block block = player.getTargetBlock(trans, (int) originselectrange + 1);
		double distance = block.getLocation().distance(origin) - 1.5;
		Location location = origin.add(direction.multiply(distance));

		return location;
	}

	public static BlockFace getCardinalDirection(Vector vector) {
		BlockFace[] faces = { BlockFace.NORTH, BlockFace.NORTH_EAST,
				BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH,
				BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST };
		Vector n, ne, e, se, s, sw, w, nw;
		w = new Vector(-1, 0, 0);
		n = new Vector(0, 0, -1);
		s = n.clone().multiply(-1);
		e = w.clone().multiply(-1);
		ne = n.clone().add(e.clone()).normalize();
		se = s.clone().add(e.clone()).normalize();
		nw = n.clone().add(w.clone()).normalize();
		sw = s.clone().add(w.clone()).normalize();

		Vector[] vectors = { n, ne, e, se, s, sw, w, nw };

		double comp = 0;
		int besti = 0;
		for (int i = 0; i < vectors.length; i++) {
			double dot = vector.dot(vectors[i]);
			if (dot > comp) {
				comp = dot;
				besti = i;
			}
		}

		return faces[besti];

	}

	private static Integer[] plantIds = { 6, 18, 31, 32, 37, 38, 39, 40, 59, 81, 83, 86, 99, 100, 103, 104, 105, 106, 111, 161, 175};
	public static Integer[] transparentToEarthbending = {0, 6, 8, 9, 10, 11, 30, 31, 32, 37, 38, 39, 40, 50, 51, 59, 78, 83, 106};
	public static Integer[] nonOpaque = {0, 6, 8, 9, 10, 11, 27, 28, 30, 31, 32, 37, 38, 39, 40, 50, 51, 55, 59, 66, 68, 69, 70, 72,
		75, 76, 77, 78, 83, 90, 93, 94, 104, 105, 106, 111, 115, 119, 127, 131, 132};

}

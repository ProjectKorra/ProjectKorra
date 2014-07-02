package com.projectkorra.ProjectKorra;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;

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
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.Vector;

import com.massivecraft.factions.listeners.FactionsListenerMain;
import com.massivecraft.mcore.ps.PS;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.PlayerCache;
import com.palmergames.bukkit.towny.object.PlayerCache.TownBlockStatus;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import com.palmergames.bukkit.towny.war.flagwar.TownyWar;
import com.palmergames.bukkit.towny.war.flagwar.TownyWarConfig;
import com.projectkorra.ProjectKorra.Ability.AbilityModule;
import com.projectkorra.ProjectKorra.Ability.AbilityModuleManager;
import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.airbending.AirBlast;
import com.projectkorra.ProjectKorra.airbending.AirBubble;
import com.projectkorra.ProjectKorra.airbending.AirBurst;
import com.projectkorra.ProjectKorra.airbending.AirScooter;
import com.projectkorra.ProjectKorra.airbending.AirShield;
import com.projectkorra.ProjectKorra.airbending.AirSpout;
import com.projectkorra.ProjectKorra.airbending.AirSuction;
import com.projectkorra.ProjectKorra.airbending.AirSwipe;
import com.projectkorra.ProjectKorra.airbending.Tornado;
import com.projectkorra.ProjectKorra.chiblocking.ChiPassive;
import com.projectkorra.ProjectKorra.chiblocking.RapidPunch;
import com.projectkorra.ProjectKorra.earthbending.Catapult;
import com.projectkorra.ProjectKorra.earthbending.CompactColumn;
import com.projectkorra.ProjectKorra.earthbending.EarthArmor;
import com.projectkorra.ProjectKorra.earthbending.EarthBlast;
import com.projectkorra.ProjectKorra.earthbending.EarthColumn;
import com.projectkorra.ProjectKorra.earthbending.EarthPassive;
import com.projectkorra.ProjectKorra.earthbending.EarthTunnel;
import com.projectkorra.ProjectKorra.earthbending.Shockwave;
import com.projectkorra.ProjectKorra.earthbending.Tremorsense;
import com.projectkorra.ProjectKorra.firebending.Cook;
import com.projectkorra.ProjectKorra.firebending.FireBlast;
import com.projectkorra.ProjectKorra.firebending.FireBurst;
import com.projectkorra.ProjectKorra.firebending.FireJet;
import com.projectkorra.ProjectKorra.firebending.FireShield;
import com.projectkorra.ProjectKorra.firebending.FireStream;
import com.projectkorra.ProjectKorra.firebending.Fireball;
import com.projectkorra.ProjectKorra.firebending.Illumination;
import com.projectkorra.ProjectKorra.firebending.Lightning;
import com.projectkorra.ProjectKorra.firebending.WallOfFire;
import com.projectkorra.ProjectKorra.waterbending.Bloodbending;
import com.projectkorra.ProjectKorra.waterbending.FreezeMelt;
import com.projectkorra.ProjectKorra.waterbending.IceSpike;
import com.projectkorra.ProjectKorra.waterbending.IceSpike2;
import com.projectkorra.ProjectKorra.waterbending.OctopusForm;
import com.projectkorra.ProjectKorra.waterbending.Plantbending;
import com.projectkorra.ProjectKorra.waterbending.WaterManipulation;
import com.projectkorra.ProjectKorra.waterbending.WaterReturn;
import com.projectkorra.ProjectKorra.waterbending.WaterSpout;
import com.projectkorra.ProjectKorra.waterbending.WaterWall;
import com.projectkorra.ProjectKorra.waterbending.Wave;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;

public class Methods {

	static ProjectKorra plugin;

	public Methods(ProjectKorra plugin) {
		Methods.plugin = plugin;
	}

	private static final ItemStack pickaxe = new ItemStack(
			Material.DIAMOND_PICKAXE);
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
		AirBlast.removeAll();
		AirBubble.removeAll();
		AirShield.instances.clear();
		AirSuction.instances.clear();
		AirScooter.removeAll();
		AirSpout.removeAll();
		AirSwipe.instances.clear();
		Tornado.instances.clear();
		AirBurst.removeAll();

		Catapult.removeAll();
		CompactColumn.removeAll();
		EarthBlast.removeAll();
		EarthColumn.removeAll();
		EarthPassive.removeAll();
		EarthArmor.removeAll();
		EarthTunnel.instances.clear();
		Shockwave.removeAll();
		Tremorsense.removeAll();

		FreezeMelt.removeAll();
		IceSpike.removeAll();
		IceSpike2.removeAll();
		WaterManipulation.removeAll();
		WaterSpout.removeAll();
		WaterWall.removeAll();
		Wave.removeAll();
		Plantbending.regrowAll();
		OctopusForm.removeAll();
		Bloodbending.instances.clear();

		FireStream.removeAll();
		Fireball.removeAll();
		WallOfFire.instances.clear();
		Lightning.instances.clear();
		FireShield.removeAll();
		FireBlast.removeAll();
		FireBurst.removeAll();
		FireJet.instances.clear();
		Cook.removeAll();
		Illumination.removeAll();

		RapidPunch.instance.clear();

		Flight.removeAll();
		WaterReturn.removeAll();
		TempBlock.removeAll();
		removeAllEarthbendedBlocks();

		EarthPassive.removeAll();
	}

	public static void removeAllEarthbendedBlocks() {
		for (Block block : movedearth.keySet()) {
			revertBlock(block);
		}

		for (Integer i : tempair.keySet()) {
			revertAirBlock(i, true);
		}
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
		if (isAirAbility(ability)) {
			player.sendMessage(getAirColor() + "Succesfully bound " + ability + " to slot " + slot);
		}
		else if (isWaterAbility(ability)) {
			player.sendMessage(getWaterColor() + "Succesfully bound " + ability + " to slot " + slot);
		}
		else if (isEarthAbility(ability)) {
			player.sendMessage(getEarthColor() + "Succesfully bound " + ability + " to slot " + slot);
		}
		else if (isFireAbility(ability)) {
			player.sendMessage(getFireColor() + "Succesfully bound " + ability + " to slot " + slot);
		}
		else if (isChiAbility(ability)) {
			player.sendMessage(getChiColor() + "Succesfully bound " + ability + " to slot " + slot);
		} else {
			player.sendMessage(getAvatarColor() + "Successfully bound " + ability + " to slot " + slot);
		}
	}

	public static void bindAbility(Player player, String ability, int slot) {
		BendingPlayer bPlayer = getBendingPlayer(player.getName());
		bPlayer.abilities.put(slot, ability);
		if (isAirAbility(ability)) {
			player.sendMessage(getAirColor() + "Succesfully bound " + ability + " to slot " + slot);
		}
		else if (isWaterAbility(ability)) {
			player.sendMessage(getWaterColor() + "Succesfully bound " + ability + " to slot " + slot);
		}
		else if (isEarthAbility(ability)) {
			player.sendMessage(getEarthColor() + "Succesfully bound " + ability + " to slot " + slot);
		}
		else if (isFireAbility(ability)) {
			player.sendMessage(getFireColor() + "Succesfully bound " + ability + " to slot " + slot);
		}
		else if (isChiAbility(ability)) {
			player.sendMessage(getChiColor() + "Succesfully bound " + ability + " to slot " + slot);
		} else {
			player.sendMessage(getAvatarColor() + "Successfully bound " + ability + " to slot " + slot);
		}
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
		if (isRegionProtectedFromBuild(p, ability, p.getLocation())) return false;
		return true;
	}

	public static void removeUnusableAbilities(String player) {
		BendingPlayer bPlayer = getBendingPlayer(player);
		HashMap<Integer, String> slots = bPlayer.getAbilities();
		HashMap<Integer, String> finalabilities = new HashMap<Integer, String>();
		try {
			for (int i: slots.keySet()) {
				if (canBend(player, slots.get(i))) {
					finalabilities.put(i, slots.get(i));
				}
			}
			bPlayer.abilities = finalabilities;
		} catch (Exception ex) {

		}

	}
	
	public static boolean hasPermission(Player player, String ability) {
		if (player.hasPermission("bending.ability." + ability)) return true;
		return false;
	}

	public static boolean canBendPassive(String player, Element element) {
		BendingPlayer bPlayer = getBendingPlayer(player);
		Player p = Bukkit.getPlayer(player);
		if (!bPlayer.isToggled) return false;
		if (!bPlayer.hasElement(element)) return false;
		if (isRegionProtectedFromBuild(p, null, p.getLocation())) return false;
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

	public static ChatColor getAirColor() {
		return ChatColor.valueOf(plugin.getConfig().getString("Properties.Chat.Colors.Air"));
	}

	public static ChatColor getWaterColor() {
		return ChatColor.valueOf(plugin.getConfig().getString("Properties.Chat.Colors.Water"));
	}

	public static ChatColor getEarthColor() {
		return ChatColor.valueOf(plugin.getConfig().getString("Properties.Chat.Colors.Earth"));
	}

	public static ChatColor getFireColor() {
		return ChatColor.valueOf(plugin.getConfig().getString("Properties.Chat.Colors.Fire"));
	}

	public static ChatColor getChiColor() {
		return ChatColor.valueOf(plugin.getConfig().getString("Properties.Chat.Colors.Chi"));
	}

	public static ChatColor getAvatarColor() {
		return ChatColor.valueOf(plugin.getConfig().getString("Properties.Chat.Colors.Avatar"));
	}

	public static ChatColor getAbilityColor(String ability) {
		if (AbilityModuleManager.chiabilities.contains(ability)) return getChiColor();
		if (AbilityModuleManager.airbendingabilities.contains(ability)) return getAirColor();
		if (AbilityModuleManager.waterbendingabilities.contains(ability)) return getWaterColor();
		if (AbilityModuleManager.earthbendingabilities.contains(ability)) return getEarthColor();
		if (AbilityModuleManager.firebendingabilities.contains(ability)) return getFireColor();
		else return getAvatarColor();
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
		return isEarthbendable(player, "RaiseEarth", block);
	}

	public static boolean isEarthbendable(Player player, String ability,
			Block block) {
		if (isRegionProtectedFromBuild(player, ability,
				block.getLocation()))
			return false;
		Material material = block.getType();

		// if ((material == Material.STONE) || (material == Material.CLAY)
		// || (material == Material.COAL_ORE)
		// || (material == Material.DIAMOND_ORE)
		// || (material == Material.DIRT)
		// || (material == Material.GOLD_ORE)
		// || (material == Material.GRASS)
		// || (material == Material.GRAVEL)
		// || (material == Material.IRON_ORE)
		// || (material == Material.LAPIS_ORE)
		// || (material == Material.NETHERRACK)
		// || (material == Material.REDSTONE_ORE)
		// || (material == Material.SAND)
		// || (material == Material.SANDSTONE)) {
		// return true;
		// }
		for (String s : ProjectKorra.plugin.getConfig().getStringList("Properties.Earth.EarthbendableBlocks")) {

			if (material == Material.getMaterial(s)) {

				return true;

			}

		}
		return false;
	}

	public static boolean isChiBlocked(String player) {
		return Methods.getBendingPlayer(player).isChiBlocked();
		//		long currTime = System.currentTimeMillis();
		//		long duration = ProjectKorra.plugin.getConfig().getLong("Abilities.Chi.Passive.BlockChi.Duration");
		//		if (BendingPlayer.blockedChi.contains(player)) {
		//			if (BendingPlayer.blockedChi.get(player) + ChiPassive.duration >= System.currentTimeMillis()) {
		//				return true;
		//			} else {
		//				BendingPlayer.blockedChi.remove(player);
		//				return false;
		//			}
		//		} else {
		//			Bukkit.getServer().broadcastMessage("test");
		//			return false;
		//		}
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
		return isTransparentToEarthbending(player, "RaiseEarth", block);
	}

	public static boolean isTransparentToEarthbending(Player player,
			String ability, Block block) {
		if (isRegionProtectedFromBuild(player, ability,
				block.getLocation()))
			return false;
		if (Arrays.asList(transparentToEarthbending).contains(block.getTypeId()))
			return true;
		return false;
	}

	public static void removeSpouts(Location location, double radius,
			Player sourceplayer) {
		WaterSpout.removeSpouts(location, radius, sourceplayer);
		AirSpout.removeSpouts(location, radius, sourceplayer);
	}

	public static void removeSpouts(Location location, Player sourceplayer) {
		removeSpouts(location, 1.5, sourceplayer);
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
			if (isRegionProtectedFromBuild(player, "RaiseEarth",
					location))
				continue;
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
			if (isRegionProtectedFromBuild(player, "WaterManipulation",
					location))
				continue;
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
		for (BlockFace face : faces) {
			if (FreezeMelt.frozenblocks.containsKey((block.getRelative(face))))
				adjacent = true;
		}

		return adjacent;
	}

	public static void removeBlock(Block block) {
		if (isAdjacentToThreeOrMoreSources(block)) {
			block.setType(Material.WATER);
			block.setData((byte) 0x0);
		} else {
			block.setType(Material.AIR);
		}
	}

	public static void revertAirBlock(int i) {
		revertAirBlock(i, false);
	}

	public static void revertAirBlock(int i, boolean force) {
		if (!tempair.containsKey(i))
			return;
		Information info = tempair.get(i);
		Block block = info.getState().getBlock();
		if (block.getType() != Material.AIR && !block.isLiquid()) {
			if (force || !movedearth.containsKey(block)) {
				dropItems(
						block,
						getDrops(block, info.getState().getType(), info
								.getState().getRawData(), pickaxe));
				// ItemStack item = new ItemStack(info.getType());
				// item.setData(new MaterialData(info.getType(),
				// info.getData()));
				// block.getWorld().dropItem(block.getLocation(), item);
				tempair.remove(i);
			} else {
				info.setTime(info.getTime() + 10000);
			}
			return;
		} else {
			// block.setType(info.getType());
			// block.setData(info.getData());
			info.getState().update(true);
			tempair.remove(i);
		}
	}

	public static boolean revertBlock(Block block) {
		byte full = 0x0;
		if (movedearth.containsKey(block)) {
			Information info = movedearth.get(block);
			Block sourceblock = info.getState().getBlock();

			if (info.getState().getType() == Material.AIR) {
				movedearth.remove(block);
				return true;
			}

			if (block.equals(sourceblock)) {
				// verbose("Equals!");
				// if (block.getType() == Material.SANDSTONE
				// && info.getState().getType() == Material.SAND)
				// block.setType(Material.SAND);
				info.getState().update(true);
				if (EarthColumn.blockInAllAffectedBlocks(sourceblock))
					EarthColumn.revertBlock(sourceblock);
				if (EarthColumn.blockInAllAffectedBlocks(block))
					EarthColumn.revertBlock(block);
				EarthColumn.resetBlock(sourceblock);
				EarthColumn.resetBlock(block);
				movedearth.remove(block);
				return true;
			}

			if (movedearth.containsKey(sourceblock)) {
				addTempAirBlock(block);
				movedearth.remove(block);
				return true;
				// verbose("Block: " + block);
				// verbose("Sourceblock: " + sourceblock);
				// verbose("StartBlock: " + startblock);
				// if (startblock != null) {
				// if (startblock.equals(sourceblock)) {
				// sourceblock.setType(info.getType());
				// sourceblock.setData(info.getData());
				// if (adjacentToThreeOrMoreSources(block)) {
				// block.setType(Material.WATER);
				// block.setData(full);
				// } else {
				// block.setType(Material.AIR);
				// }
				// movedearth.get(startblock).setInteger(10);
				// if (EarthColumn
				// .blockInAllAffectedBlocks(sourceblock))
				// EarthColumn.revertBlock(sourceblock);
				// if (EarthColumn.blockInAllAffectedBlocks(block))
				// EarthColumn.revertBlock(block);
				// EarthColumn.resetBlock(sourceblock);
				// EarthColumn.resetBlock(block);
				// movedearth.remove(block);
				// return true;
				// }
				//
				// } else {
				// startblock = block;
				// }
				// revertBlock(sourceblock, startblock, true);
			}

			if (sourceblock.getType() == Material.AIR || sourceblock.isLiquid()) {
				// sourceblock.setType(info.getType());
				// sourceblock.setData(info.getData());
				info.getState().update(true);
			} else {
				// if (info.getType() != Material.AIR) {
				// ItemStack item = new ItemStack(info.getType());
				// item.setData(new MaterialData(info.getType(), info
				// .getData()));
				// block.getWorld().dropItem(block.getLocation(), item);
				dropItems(
						block,
						getDrops(block, info.getState().getType(), info
								.getState().getRawData(), pickaxe));
				// }
			}

			// if (info.getInteger() != 10) {
			if (isAdjacentToThreeOrMoreSources(block)) {
				block.setType(Material.WATER);
				block.setData(full);
			} else {
				block.setType(Material.AIR);
			}
			// }

			if (EarthColumn.blockInAllAffectedBlocks(sourceblock))
				EarthColumn.revertBlock(sourceblock);
			if (EarthColumn.blockInAllAffectedBlocks(block))
				EarthColumn.revertBlock(block);
			EarthColumn.resetBlock(sourceblock);
			EarthColumn.resetBlock(block);
			movedearth.remove(block);
		}
		return true;
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
		if (isEarthbendable(player, block)
				&& !isRegionProtectedFromBuild(player, "RaiseEarth",
						block.getLocation())) {

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

	public static boolean isHarmlessAbility(String ability) {
		return Arrays.asList(AbilityModuleManager.harmlessabilities).contains(ability);
	}

	public static boolean isRegionProtectedFromBuild(Player player,
			String ability, Location loc) {

		boolean allowharmless = plugin.getConfig().getBoolean("Properties.RegionProtection.AllowHarmlessAbilities");
		boolean respectWorldGuard = plugin.getConfig().getBoolean("Properties.RegionProtection.RespectWorldGuard");
		boolean respectPreciousStones = plugin.getConfig().getBoolean("Properties.RegionProtection.RespectPreciousStones");
		boolean respectFactions = plugin.getConfig().getBoolean("Properties.RegionProtection.RespectFactions");
		boolean respectTowny = plugin.getConfig().getBoolean("Properties.RegionProtection.RespectTowny");
		boolean respectGriefPrevention = plugin.getConfig().getBoolean("Properties.RegionProtection.RespectGriefPrevention");

		Set<String> ignite = AbilityModuleManager.igniteabilities;
		Set<String> explode = AbilityModuleManager.explodeabilities;
		//		List<Abilities> ignite = new ArrayList<Abilities>();
		//		ignite.add(Abilities.Blaze);
		//		List<Abilities> explode = new ArrayList<Abilities>();
		//		explode.add(Abilities.FireBlast);
		//		explode.add(Abilities.Lightning);

		if (ability == null && allowharmless)
			return false;
		if (isHarmlessAbility(ability) && allowharmless)
			return false;

		// if (ignite.contains(ability)) {
		// BlockIgniteEvent event = new BlockIgniteEvent(location.getBlock(),
		// IgniteCause.FLINT_AND_STEEL, player);
		// Bending.plugin.getServer().getPluginManager().callEvent(event);
		// if (event.isCancelled())
		// return false;
		// event.setCancelled(true);
		// }

		PluginManager pm = Bukkit.getPluginManager();

		Plugin wgp = pm.getPlugin("WorldGuard");
		Plugin psp = pm.getPlugin("PreciousStone");
		Plugin fcp = pm.getPlugin("Factions");
		Plugin twnp = pm.getPlugin("Towny");
		Plugin gpp = pm.getPlugin("GriefPrevention");
		Plugin mcore = pm.getPlugin("mcore");

		for (Location location : new Location[] { loc, player.getLocation() }) {

			if (wgp != null && respectWorldGuard) {
				WorldGuardPlugin wg = (WorldGuardPlugin) Bukkit
						.getPluginManager().getPlugin("WorldGuard");
				if (!player.isOnline())
					return true;

				if (ignite.contains(ability)) {
					if (!wg.hasPermission(player, "worldguard.override.lighter")) {
						if (wg.getGlobalStateManager().get(location.getWorld()).blockLighter)
							return true;
						if (!wg.getGlobalRegionManager().hasBypass(player,
								location.getWorld())
								&& !wg.getGlobalRegionManager()
								.get(location.getWorld())
								.getApplicableRegions(location)
								.allows(DefaultFlag.LIGHTER,
										wg.wrapPlayer(player)))
							return true;
					}

				}
				if (explode.contains(ability)) {
					if (wg.getGlobalStateManager().get(location.getWorld()).blockTNTExplosions)
						return true;
					if (!wg.getGlobalRegionManager().get(location.getWorld())
							.getApplicableRegions(location)
							.allows(DefaultFlag.TNT))
						return true;
				}

				if ((!(wg.getGlobalRegionManager().canBuild(player, location)) || !(wg
						.getGlobalRegionManager()
						.canConstruct(player, location)))) {
					return true;
				}
			}

			if (psp != null && respectPreciousStones) {
				PreciousStones ps = (PreciousStones) psp;

				if (ignite.contains(ability)) {
					if (ps.getForceFieldManager().hasSourceField(location,
							FieldFlag.PREVENT_FIRE))
						return true;
				}
				if (explode.contains(ability)) {
					if (ps.getForceFieldManager().hasSourceField(location,
							FieldFlag.PREVENT_EXPLOSIONS))
						return true;
				}

				if (ps.getForceFieldManager().hasSourceField(location,
						FieldFlag.PREVENT_PLACE))
					return true;
			}

			if (fcp != null && mcore != null && respectFactions) {
				if (ignite.contains(ability)) {

				}

				if (explode.contains(ability)) {

				}

				if (!FactionsListenerMain.canPlayerBuildAt(player,
						PS.valueOf(loc.getBlock()), false)) {
					return true;
				}

				// if (!FactionsBlockListener.playerCanBuildDestroyBlock(player,
				// location, "build", true)) {
				// return true;
				// }
			}

			if (twnp != null && respectTowny) {
				Towny twn = (Towny) twnp;

				WorldCoord worldCoord;

				try {
					TownyWorld world = TownyUniverse.getDataSource().getWorld(
							location.getWorld().getName());
					worldCoord = new WorldCoord(world.getName(),
							Coord.parseCoord(location));

					boolean bBuild = PlayerCacheUtil.getCachePermission(player,
							location, 3, (byte) 0,
							TownyPermission.ActionType.BUILD);

					if (ignite.contains(ability)) {

					}

					if (explode.contains(ability)) {

					}

					if (!bBuild) {
						PlayerCache cache = twn.getCache(player);
						TownBlockStatus status = cache.getStatus();

						if (((status == TownBlockStatus.ENEMY) && TownyWarConfig
								.isAllowingAttacks())) {

							try {
								TownyWar.callAttackCellEvent(twn, player,
										location.getBlock(), worldCoord);
							} catch (Exception e) {
								TownyMessaging.sendErrorMsg(player,
										e.getMessage());
							}

							return true;

						} else if (status == TownBlockStatus.WARZONE) {
						} else {
							return true;
						}

						if ((cache.hasBlockErrMsg()))
							TownyMessaging.sendErrorMsg(player,
									cache.getBlockErrMsg());
					}

				} catch (Exception e1) {
					TownyMessaging.sendErrorMsg(player, TownySettings
							.getLangString("msg_err_not_configured"));
				}

			}

			if (gpp != null && respectGriefPrevention) {
				String reason = GriefPrevention.instance.allowBuild(player,
						location);

				if (ignite.contains(ability)) {

				}

				if (explode.contains(ability)) {

				}

				if (reason != null)
					return true;
			}
		}

		return false;
	}

	private static Integer[] plantIds = { 6, 18, 31, 32, 37, 38, 39, 40, 59, 81, 83, 86, 99, 100, 103, 104, 105, 106, 111, 161, 175};
	public static Integer[] transparentToEarthbending = {0, 6, 8, 9, 10, 11, 30, 31, 32, 37, 38, 39, 40, 50, 51, 59, 78, 83, 106};
	public static Integer[] nonOpaque = {0, 6, 8, 9, 10, 11, 27, 28, 30, 31, 32, 37, 38, 39, 40, 50, 51, 55, 59, 66, 68, 69, 70, 72,
		75, 76, 77, 78, 83, 90, 93, 94, 104, 105, 106, 111, 115, 119, 127, 131, 132};

}

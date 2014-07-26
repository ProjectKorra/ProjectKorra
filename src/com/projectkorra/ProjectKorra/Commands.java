package com.projectkorra.ProjectKorra;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.projectkorra.ProjectKorra.Ability.AbilityModuleManager;
import com.projectkorra.ProjectKorra.Ability.StockAbilities;

public class Commands {

	ProjectKorra plugin;

	public Commands(ProjectKorra plugin) {
		this.plugin = plugin;
		init();
	}
	String[] airaliases = {"air", "a", "airbending", "airbender"};
	String[] wateraliases = {"water", "w", "waterbending", "waterbender"};
	String[] earthaliases = {"earth", "e", "earthbending", "earthbender"};
	String[] firealiases = {"fire", "f", "firebending", "firebender"};
	String[] chialiases = {"chi", "c", "chiblocking", "chiblocker"};

	String[] helpaliases = {"help", "h"};
	String[] versionaliases = {"version", "v"};
	String[] permaremovealiases = {"permaremove", "premove", "permremove", "pr"};
	String[] choosealiases = {"choose", "ch"};
	String[] removealiases = {"remove", "rm"};
	String[] togglealiases = {"toggle", "t"};
	String[] displayaliases = {"display", "d"};
	String[] bindaliases = {"bind", "b"};
	String[] clearaliases = {"clear", "cl", "c"};
	String[] reloadaliases = {"reload", "r"};
	String[] addaliases = {"add", "a"};
	String[] whoaliases = {"who", "w"};
	String[] importaliases = {"import", "i"};

	public static boolean debug = ProjectKorra.plugin.getConfig().getBoolean("debug");

	public static boolean isToggledForAll = false;

	private static BukkitTask importTask;
	private void init() {
		PluginCommand projectkorra = plugin.getCommand("projectkorra");
		CommandExecutor exe;

		exe = new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
				if (args.length == 0) {
					s.sendMessage(ChatColor.RED + "/bending help [Ability/Command] " + ChatColor.YELLOW + "Display help.");
					s.sendMessage(ChatColor.RED + "/bending choose [Element] " + ChatColor.YELLOW + "Choose an element.");
					s.sendMessage(ChatColor.RED + "/bending bind [Ability] # " + ChatColor.YELLOW + "Bind an ability.");
					return true;
				}
				if (Arrays.asList(reloadaliases).contains(args[0].toLowerCase())) {
					if (args.length != 1) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: /bending reload");
						return true;
					}

					if (!s.hasPermission("bending.command.reload")) {
						s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
						return true;
					}

					Methods.reloadPlugin();
					s.sendMessage(ChatColor.AQUA + "Bending config reloaded.");
					return true;
				}
				if (Arrays.asList(clearaliases).contains(args[0].toLowerCase())) {
					if (args.length > 2) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: /bending clear <#>");
						return true;
					}
					if (!s.hasPermission("bending.command.clear")) {
						s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
						return true;
					}

					if (!(s instanceof Player)) {
						s.sendMessage(ChatColor.RED + "This command is only usable by players.");
						return true;
					}
					BendingPlayer bPlayer = Methods.getBendingPlayer(s.getName());
					if (args.length == 1) {
						bPlayer.abilities.clear();
						s.sendMessage("Your bound abilities have been cleared.");
						return true;
					}

					if (args.length == 2) {
						int slot = Integer.parseInt(args[1]);
						if (slot < 1 || slot > 9) {
							s.sendMessage(ChatColor.RED + "The slot must be an integer between 0 and 9.");
							return true;
						}
						if (bPlayer.abilities.get(slot) != null) {
							bPlayer.abilities.remove(slot);
						}
						s.sendMessage("You have cleared slot #" + slot);
						return true;
					}
				}
				if (Arrays.asList(bindaliases).contains(args[0].toLowerCase())) {
					if (args.length > 3 || args.length == 1) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: /bending bind [Ability] <#>");
						return true;
					}

					if (!s.hasPermission("bending.command.bind")) {
						s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
						return true;
					}

					if (!(s instanceof Player)) {
						s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
						return true;
					}

					if (args.length == 2) {
						// We bind the ability to the slot they have selected..
						// bending bind [Ability]
						String abil = args[1];
						if (!Methods.abilityExists(abil)) {
							s.sendMessage(ChatColor.RED + "That is not an ability.");
							return true;
						}

						String ability = Methods.getAbility(abil);

						if (!Methods.hasPermission((Player) s, ability)) {
							s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
							return true;
						}
						if (Methods.isAirAbility(ability) && !Methods.isBender(s.getName(), Element.Air)) {
							s.sendMessage(Methods.getAirColor() + "You must be an Airbender to bind this ability.");
							return true;
						}
						if (Methods.isWaterAbility(ability) && !Methods.isBender(s.getName(), Element.Water)) {
							s.sendMessage(Methods.getWaterColor() + "You must be a Waterbender to bind this ability.");
							return true;
						}
						if (Methods.isEarthAbility(ability) && !Methods.isBender(s.getName(), Element.Earth)) {
							s.sendMessage(Methods.getEarthColor() + "You must be an Earthbender to bind this ability.");
							return true;
						}
						if (Methods.isFireAbility(ability) && !Methods.isBender(s.getName(), Element.Fire)) {
							s.sendMessage(Methods.getFireColor() + "You must be a Firebender to bind this ability.");
							return true;
						}
						if (Methods.isChiAbility(ability) && !Methods.isBender(s.getName(), Element.Chi)) {
							s.sendMessage(Methods.getChiColor() + "You must be a ChiBlocker to bind this ability.");
							return true;
						}

						Methods.bindAbility((Player) s, ability);
						//						s.sendMessage("Ability Bound to slot");
						return true;
					}

					if (args.length == 3) {
						// bending bind ability [Slot]
						String abil = args[1];
						if (!Methods.abilityExists(abil)) {
							s.sendMessage(ChatColor.RED + "That ability doesn't exist.");
							return true;
						}
						String ability = Methods.getAbility(abil);
						int slot = 0;
						try {
							slot = Integer.parseInt(args[2]);
						} catch (NumberFormatException e) {
							s.sendMessage(ChatColor.RED + "Slot must be an integer between 1 and 9.");
							return true;
						}
						if (slot < 1 || slot > 9) {
							s.sendMessage(ChatColor.RED + "Slot must be an integer between 1 and 9.");
							return true;
						}

						if (!Methods.hasPermission((Player) s, ability)) {
							s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
							return true;
						}

						if (Methods.isAirAbility(ability) && !Methods.isBender(s.getName(), Element.Air)) {
							s.sendMessage(Methods.getAirColor() + "You must be an Airbender to bind this ability.");
							return true;
						}
						if (Methods.isWaterAbility(ability) && !Methods.isBender(s.getName(), Element.Water)) {
							s.sendMessage(Methods.getWaterColor() + "You must be a Waterbender to bind this ability.");
							return true;
						}
						if (Methods.isEarthAbility(ability) && !Methods.isBender(s.getName(), Element.Earth)) {
							s.sendMessage(Methods.getEarthColor() + "You must be an Earthbender to bind this ability.");
							return true;
						}
						if (Methods.isFireAbility(ability) && !Methods.isBender(s.getName(), Element.Fire)) {
							s.sendMessage(Methods.getFireColor() + "You must be a Firebender to bind this ability.");
							return true;
						}
						if (Methods.isChiAbility(ability) && !Methods.isBender(s.getName(), Element.Chi)) {
							s.sendMessage(Methods.getChiColor() + "You must be a ChiBlocker to bind this ability.");
							return true;
						}
						Methods.bindAbility((Player) s, ability, slot);
						//						s.sendMessage("Ability Bound");
						return true;
					}
				}
				if (Arrays.asList(importaliases).contains(args[0].toLowerCase())) {
					if (!s.hasPermission("bending.command.import")) {
						s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
						return true;
					}
					if (!Methods.isImportEnabled()) {
						s.sendMessage(ChatColor.RED + "Importing has been disabled in the config");
						return true;
					}

					s.sendMessage(ChatColor.GREEN + "Preparing data for import.");					
					File bendingPlayersFile = new File(".", "converted.yml");
					FileConfiguration bendingPlayers = YamlConfiguration.loadConfiguration(bendingPlayersFile);

					final LinkedList<BendingPlayer> bPlayers = new LinkedList<BendingPlayer>();
					for (String string: bendingPlayers.getConfigurationSection("").getKeys(false)) {
						if (string.equalsIgnoreCase("version")) continue;
						String playername = string;
						UUID uuid = Bukkit.getOfflinePlayer(playername).getUniqueId();
						ArrayList<Element> element = new ArrayList<Element>();
						List<Integer> oe = bendingPlayers.getIntegerList(string + ".BendingTypes");
						HashMap<Integer, String> abilities = new HashMap<Integer, String>();
						List<Integer> oa = bendingPlayers.getIntegerList(string + ".SlotAbilities"); 
						boolean permaremoved = bendingPlayers.getBoolean(string + ".Permaremoved");

						int slot = 1;
						for (int i : oa) {
							if (StockAbilities.getAbility(i) != null) {
								abilities.put(slot, StockAbilities.getAbility(i).toString());
								slot++;
							} else {
								abilities.put(slot, null);
								slot++;
							}
						}

						for (int i : oe) {
							if (Element.getType(i) != null) {
								element.add(Element.getType(i));
							}
						}

						BendingPlayer bPlayer = new BendingPlayer(uuid, playername, element, abilities, permaremoved);
						bPlayers.add(bPlayer);
					}

					final int total = bPlayers.size();
					final CommandSender sender = s;
					s.sendMessage(ChatColor.GREEN + "Import of data started. Do NOT stop / reload your server.");
					if (debug) {
						s.sendMessage(ChatColor.RED + "Console will print out all of the players that are imported if debug mode is enabled as they import.");
					}
					importTask = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
						public void run() {
							int i = 0;
							if (i >= 10) {
								sender.sendMessage(ChatColor.GREEN + "10 / " + total + " players converted thus far!");
								return;
							}

							while (i < 10) {
								if (bPlayers.isEmpty()) {
									sender.sendMessage(ChatColor.GREEN + "All data has been queued up, please allow up to 5 minutes for the data to complete, then reboot your server.");
									Bukkit.getServer().getScheduler().cancelTask(importTask.getTaskId());
									plugin.getConfig().set("Properties.ImportEnabled", false);
									plugin.saveConfig();
									for (Player player: Bukkit.getOnlinePlayers()) {
										Methods.createBendingPlayer(player.getUniqueId(), player.getName());
									}
									return;
								}
								StringBuilder elements = new StringBuilder();
								BendingPlayer bPlayer = bPlayers.pop();
								if (bPlayer.hasElement(Element.Air)) elements.append("a");
								if (bPlayer.hasElement(Element.Water)) elements.append("w");
								if (bPlayer.hasElement(Element.Earth)) elements.append("e");
								if (bPlayer.hasElement(Element.Fire)) elements.append("f");
								if (bPlayer.hasElement(Element.Chi)) elements.append("c");

								HashMap<Integer, String> abilities = bPlayer.abilities;

								ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM pk_players WHERE uuid = '" + bPlayer.uuid.toString() + "'");

								try {
									if (rs2.next()) { // SQL Data already exists for player.
										DBConnection.sql.modifyQuery("UPDATE pk_players SET player = '" + bPlayer.player + "' WHERE uuid = '" + bPlayer.uuid.toString());
										DBConnection.sql.modifyQuery("UPDATE pk_players SET element = '" + elements + "' WHERE uuid = '" + bPlayer.uuid.toString());
										DBConnection.sql.modifyQuery("UPDATE pk_players SET permaremoved = '" + bPlayer.isPermaRemoved() + "' WHERE uuid = '" + bPlayer.uuid.toString());
										for (int slot = 1; slot < 10; slot++) {
											DBConnection.sql.modifyQuery("UPDATE pk_players SET slot" + slot + " = '" + abilities.get(slot) + "' WHERE player = '" + bPlayer.getPlayerName() + "'");
										}
									} else {
										DBConnection.sql.modifyQuery("INSERT INTO pk_players (uuid, player, element, permaremoved) VALUES ('" + bPlayer.uuid.toString() + "', '" + bPlayer.player + "', '" + elements + "', '" + bPlayer.isPermaRemoved() +"')");
										for (int slot = 1; slot < 10; slot++) {
											DBConnection.sql.modifyQuery("UPDATE pk_players SET slot" + slot + " = '" + abilities.get(slot) + "' WHERE player = '" + bPlayer.getPlayerName() + "'");
										}
									}
								} catch (SQLException ex) {
									ex.printStackTrace();
								}
								i++;
								if (debug) {
									System.out.println("[ProjectKorra] Successfully imported " + bPlayer.player + ". " + bPlayers.size() + " players left to import.");
								}
							}
						}
					}, 0, 40);
					return true;

				}
				if (Arrays.asList(displayaliases).contains(args[0].toLowerCase())) {
					if (args.length > 2) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: /bending display <Element>");
						return true;
					}

					if (!s.hasPermission("bending.command.display")) {
						s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
						return true;
					}

					if (args.length == 2) {
						//bending display [Element]
						if (Arrays.asList(airaliases).contains(args[1].toLowerCase())) {
							if (AbilityModuleManager.airbendingabilities.isEmpty()) {
								s.sendMessage(Methods.getAirColor() + "There are no airbending abilities available.");
								return true;
							}
							for (String st: AbilityModuleManager.airbendingabilities) {
								if (Methods.hasPermission((Player) s, st)) {
									s.sendMessage(Methods.getAirColor() + st);
								}
							}
							return true;
						}
						if (Arrays.asList(wateraliases).contains(args[1].toLowerCase())) {
							if (AbilityModuleManager.waterbendingabilities.isEmpty()) {
								s.sendMessage(Methods.getWaterColor() + "There are no waterbending abilities available.");
								return true;
							}
							for (String st: AbilityModuleManager.waterbendingabilities) {
								if (Methods.hasPermission((Player) s, st)) {
									s.sendMessage(Methods.getWaterColor() + st);
								}
							}
							return true;
						}
						if (Arrays.asList(earthaliases).contains(args[1].toLowerCase())) {
							if (AbilityModuleManager.earthbendingabilities.isEmpty()) {
								s.sendMessage(Methods.getEarthColor() + "There are no earthbending abilities available.");
								return true;
							}
							for (String st: AbilityModuleManager.earthbendingabilities) {
								if (Methods.hasPermission((Player) s, st)) {
									if (Methods.isMetalbendingAbility(st)) {
										s.sendMessage(Methods.getMetalbendingColor() + st);
									} else {
										s.sendMessage(Methods.getEarthColor() + st);
									}
								}
							}
							return true;
						}
						if (Arrays.asList(firealiases).contains(args[1].toLowerCase())) {
							if (AbilityModuleManager.firebendingabilities.isEmpty()) {
								s.sendMessage(Methods.getFireColor() + "There are no firebending abilities available.");
								return true;
							}
							for (String st: AbilityModuleManager.firebendingabilities) {
								if (Methods.hasPermission((Player) s, st)) {
									s.sendMessage(Methods.getFireColor() + st);
								}
							}
							return true;
						}
						if (Arrays.asList(chialiases).contains(args[1].toLowerCase())) {
							if (AbilityModuleManager.chiabilities.isEmpty()) {
								s.sendMessage(Methods.getChiColor() + "There are no chiblocking abilities available.");
								return true;
							}

							for (String st: AbilityModuleManager.chiabilities) {
								if (Methods.hasPermission((Player) s, st)) {
									s.sendMessage(Methods.getChiColor()  + st);
								}
							}
							return true;
						} else {
							s.sendMessage(ChatColor.RED + "Not a valid Element." + ChatColor.WHITE + " Elements: " + 
									Methods.getAirColor() + "Air" + 
									Methods.getEarthColor() + "Earth" + 
									Methods.getFireColor() + "Fire" +
									Methods.getWaterColor() + "Water" +
									Methods.getChiColor() + "Chi");

						}
					}
					if (args.length == 1) {
						//bending display
						if (!(s instanceof Player)) {
							s.sendMessage(ChatColor.RED + "This command is only usable by players.");
							return true;
						}
						BendingPlayer bPlayer = Methods.getBendingPlayer(s.getName());
						HashMap<Integer, String> abilities = bPlayer.abilities;

						if (abilities.isEmpty()) {
							s.sendMessage("You don't have any bound abilities.");
							s.sendMessage("If you would like to see a list of available abilities, please use the /bending display [Element] command. Use /bending help for more information.");
							return true;
						}

						for (int i = 1; i <= 9; i++) {
							String ability = abilities.get(i);
							if (ability != null) s.sendMessage(i + " - " + Methods.getAbilityColor(ability) + ability);
						}

						return true;
					}
				}
				if (Arrays.asList(togglealiases).contains(args[0].toLowerCase())) {
					if (args.length > 2) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: /bending toggle <all>");
						return true;
					}
					if (args.length == 1) {
						if (!s.hasPermission("bending.command.toggle")) {
							s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
							return true;
						}

						if (!(s instanceof Player)) {
							s.sendMessage(ChatColor.RED + "This command is only usable by players.");
							return true;
						}

						BendingPlayer bPlayer = Methods.getBendingPlayer(s.getName());

						if (isToggledForAll) {
							s.sendMessage(ChatColor.DARK_RED + "Bending has been toggled off for all players. You may not use this command until after it is turned back on.");
							return true;
						}
						if (bPlayer.isToggled) {
							s.sendMessage(ChatColor.RED + "Your bending has been toggled off. You will not be able to use most abilities until you toggle it back.");
							bPlayer.isToggled = false;
							return true;
						} else {
							s.sendMessage(ChatColor.GREEN + "You have turned your Bending back on.");
							bPlayer.isToggled = true;
							return true;
						}
					} else if (args.length == 2 && args[1].equalsIgnoreCase("all")) {
						if (!s.hasPermission("bending.command.toggle.all")) {
							s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
							return true;
						}
						
						if (isToggledForAll) { // Bending is toggled off for all players.
							isToggledForAll = false;
							for (Player player: Bukkit.getOnlinePlayers()) {
								s.sendMessage(ChatColor.GREEN + "Bending has been toggled back on for all players.");
								return true;
							}
						} else {
							isToggledForAll = true;
							for (Player player: Bukkit.getOnlinePlayers()) {
								s.sendMessage(ChatColor.RED + "Bending has been toggled off for all players.");
								return true;
							}
						}
					} else {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: /bending toggle <all>");
					}
				}
				if (Arrays.asList(whoaliases).contains(args[0].toLowerCase())) {
					if (args.length > 2) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: /bending who <Player>");
						return true;
					}
					if (!s.hasPermission("bending.command.who")) {
						s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
						return true;
					}

					if (args.length == 2) {
						Player p = Bukkit.getPlayer(args[1]);
						if (p == null) {
							s.sendMessage(ChatColor.RED + "That player is not online.");
							return true;
						}

						String un = p.getName();
						s.sendMessage(un + " - ");
						if (Methods.isBender(un, Element.Air)) {
							s.sendMessage(Methods.getAirColor() + "- Airbender");
						}
						if (Methods.isBender(un, Element.Water)) {
							s.sendMessage(Methods.getWaterColor() + "- Waterbender");
						}
						if (Methods.isBender(un, Element.Earth)) {
							if (Methods.canMetalbend(p)) {
								s.sendMessage(Methods.getEarthColor() + "- Earthbender " + Methods.getMetalbendingColor() + "(Can Metalbend)");
							} else {
								s.sendMessage(Methods.getEarthColor() + "- Earthbender");
							}
						}
						if (Methods.isBender(un, Element.Fire)) {
							s.sendMessage(Methods.getFireColor() + "- Firebender");
						}
						if (Methods.isBender(un, Element.Chi)) {
							s.sendMessage(Methods.getChiColor() + "- ChiBlocker");
						}
						BendingPlayer bPlayer = Methods.getBendingPlayer(un);
						if (bPlayer != null)  {
							s.sendMessage("Abilities: ");
							for (int i = 1; i <= 9; i++) {
								String ability = bPlayer.getAbilities().get(i);
								if (ability != null) s.sendMessage(i + " - " + Methods.getAbilityColor(ability) + ability);
							}
						}
						return true;
					}
					if (args.length == 1) {
						List<String> players = new ArrayList<String>();
						for (Player player: Bukkit.getOnlinePlayers()) {
							String un = player.getName();

							BendingPlayer bp = Methods.getBendingPlayer(un);
							if (bp.elements.size() > 1) {
								players.add(Methods.getAvatarColor() + un);
								continue;
							}
							if (bp.elements.size() == 0) {
								players.add(un);
								continue;
							}
							if (Methods.isBender(un, Element.Air)) {
								players.add(Methods.getAirColor() + un);
								continue;
							}
							if (Methods.isBender(un, Element.Water)){
								players.add(Methods.getWaterColor() + un);
								continue;
							}
							if (Methods.isBender(un, Element.Earth)) {
								players.add(Methods.getEarthColor() + un);
								continue;
							}
							if (Methods.isBender(un, Element.Chi)) {
								players.add(Methods.getChiColor() + un);
								continue;
							}
							if (Methods.isBender(un, Element.Fire)) {
								players.add(Methods.getFireColor() + un);
								continue;
							}
						}
						for (String st: players) {
							s.sendMessage(st);
						}
						return true;
					}
				}
				if (Arrays.asList(versionaliases).contains(args[0].toLowerCase())) {
					if (args.length != 1) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: /bending version");
						return true;
					}

					if (!s.hasPermission("bending.command.version")) {
						s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
						return true;
					}
					s.sendMessage(ChatColor.GREEN + "This server is running " + ChatColor.RED + "ProjectKorra v" + plugin.getDescription().getVersion());
					return true;
				}
				if (Arrays.asList(removealiases).contains(args[0].toLowerCase())) {
					//bending remove [Player]
					if (args.length != 2) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: /bending remove [Player]");
						return true;
					}

					if (!s.hasPermission("bending.admin.remove")) {
						s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
						return true;
					}

					Player player = Bukkit.getPlayer(args[1]);

					if (player == null) {
						s.sendMessage(ChatColor.RED + "That player is not online.");
						return true;
					}

					BendingPlayer bPlayer = Methods.getBendingPlayer(player.getName());
					Methods.removeUnusableAbilities(player.getName());
					bPlayer.elements.clear();
					s.sendMessage(ChatColor.GREEN + "You have removed the bending of " + ChatColor.DARK_AQUA + player.getName());
					player.sendMessage(ChatColor.GREEN + "Your bending has been removed by " + ChatColor.DARK_AQUA + s.getName());
					return true;

				}
				if (Arrays.asList(permaremovealiases).contains(args[0].toLowerCase())) {
					//bending permaremove [Player]
					if (args.length != 2) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: " + ChatColor.DARK_AQUA + "/bending permaremove [Player]");
						return true;
					}

					if (!s.hasPermission("bending.admin.permaremove")) {
						s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
						return true;
					}

					Player player = Bukkit.getPlayer(args[1]);

					if (player == null) {
						s.sendMessage(ChatColor.RED + "That player is not online.");
						return true;
					}

					BendingPlayer bPlayer = Methods.getBendingPlayer(player.getName());
					bPlayer.elements.clear();
					Methods.removeUnusableAbilities(player.getName());
					bPlayer.permaRemoved = true;
					player.sendMessage(ChatColor.RED + "Your bending has been permanently removed.");
					s.sendMessage(ChatColor.RED + "You have permanently removed the bending of: " + ChatColor.DARK_AQUA + player.getName());
					return true;
				}
				if (Arrays.asList(addaliases).contains(args[0].toLowerCase())) {
					//bending add [Player] [Element]
					if (args.length > 3) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: " + ChatColor.DARK_AQUA + "/bending add [Player] [Element]");
						s.sendMessage(ChatColor.GOLD + "Applicable Elements: " + ChatColor.DARK_AQUA + "Air, Water, Earth, Fire, Chi");
						return true;
					}
					if (args.length == 3) {
						if (!s.hasPermission("bending.command.add.others")) {
							s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
							return true;
						}

						Player player = Bukkit.getPlayer(args[1]);
						if (player == null) {
							s.sendMessage(ChatColor.RED + "That player is not online.");
							return true;
						}

						BendingPlayer bPlayer = Methods.getBendingPlayer(player.getName());
						if (Arrays.asList(airaliases).contains(args[2].toLowerCase())) {
							bPlayer.addElement(Element.Air);
							player.sendMessage(Methods.getAirColor() + "You are also an airbender.");
							s.sendMessage(ChatColor.DARK_AQUA + player.getName() + Methods.getAirColor() + " is also an airbender.");
							return true;
						}

						if (Arrays.asList(wateraliases).contains(args[2].toLowerCase())) {
							bPlayer.addElement(Element.Water);
							player.sendMessage(Methods.getWaterColor() + "You are also a waterbender.");
							s.sendMessage(ChatColor.DARK_AQUA + player.getName() + Methods.getWaterColor() + " is also a waterbender.");
							return true;
						}

						if (Arrays.asList(earthaliases).contains(args[2].toLowerCase())) {
							bPlayer.addElement(Element.Earth);
							player.sendMessage(Methods.getEarthColor() + "You are also an Earthbender.");
							s.sendMessage(ChatColor.DARK_AQUA + player.getName() + Methods.getEarthColor() + " is also an Earthbender.");
							return true;
						}

						if (Arrays.asList(firealiases).contains(args[2].toLowerCase())) {
							bPlayer.addElement(Element.Fire);
							player.sendMessage(Methods.getFireColor() + "You are also a Firebender.");
							s.sendMessage(ChatColor.DARK_AQUA + player.getName() + Methods.getFireColor() + " is also a Firebender");
							return true;
						}
						if (Arrays.asList(chialiases).contains(args[2].toLowerCase())) {
							bPlayer.addElement(Element.Chi);
							player.sendMessage(Methods.getChiColor() + "You are also a ChiBlocker.");
							s.sendMessage(ChatColor.DARK_AQUA + player.getName() + Methods.getChiColor() + " is also a ChiBlocker");
							return true;
						}

						s.sendMessage(ChatColor.RED + "You must specify an element.");
						return true;
					}
					if (args.length == 2) {
						// Target = Self
						if (!s.hasPermission("bending.command.add")) {
							s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
							return true;
						}

						if (!(s instanceof Player)) {
							s.sendMessage(ChatColor.RED + "This command is only usable by Players.");
							return true;
						}

						BendingPlayer bPlayer = Methods.getBendingPlayer(s.getName());

						if (Arrays.asList(airaliases).contains(args[1].toLowerCase())) {
							bPlayer.addElement(Element.Air);
							s.sendMessage(Methods.getAirColor() + "You are also an airbender.");
							return true;
						}

						if (Arrays.asList(wateraliases).contains(args[1].toLowerCase())) {
							bPlayer.addElement(Element.Water);
							s.sendMessage(Methods.getWaterColor() + "You are also a waterbender.");
							return true;
						}

						if (Arrays.asList(earthaliases).contains(args[1].toLowerCase())) {
							bPlayer.addElement(Element.Earth);
							s.sendMessage(Methods.getEarthColor() + "You are also an Earthbender.");
							return true;
						}

						if (Arrays.asList(firealiases).contains(args[1].toLowerCase())) {
							bPlayer.addElement(Element.Fire);
							s.sendMessage(Methods.getFireColor() + "You are also a Firebender.");
							return true;
						}
						if (Arrays.asList(chialiases).contains(args[1].toLowerCase())) {
							bPlayer.addElement(Element.Chi);
							s.sendMessage(Methods.getChiColor() + "You are also a ChiBlocker.");
							return true;
						}
						s.sendMessage(ChatColor.RED + "You must specify an element.");
					}
				}
				if (Arrays.asList(choosealiases).contains(args[0].toLowerCase())) {
					// /bending choose [Player] [Element]
					if (args.length > 3) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: " + ChatColor.DARK_AQUA + "/bending choose [Player] [Element]");
						s.sendMessage(ChatColor.GOLD + "Applicable Elements: " + ChatColor.DARK_AQUA + "Air, Water, Earth, Fire, and Chi");
						return true;
					}

					if (args.length == 2) {
						if (!s.hasPermission("bending.command.choose")) {
							s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
							return true;
						}

						if (!(s instanceof Player)) {
							s.sendMessage(ChatColor.RED + "This command is only usable by players.");
							return true;
						}

						BendingPlayer bPlayer = Methods.getBendingPlayer(s.getName());

						if (bPlayer.isPermaRemoved()) {
							s.sendMessage(ChatColor.RED + "Your bending was permanently removed.");
							return true;
						}

						if (!bPlayer.getElements().isEmpty()) {
							if (!s.hasPermission("bending.command.rechoose")) {
								s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
								return true;
							}
						}
						if (Arrays.asList(airaliases).contains(args[1].toLowerCase())) {
							bPlayer.setElement(Element.Air);
							s.sendMessage(Methods.getAirColor() + "You are now an Airbender.");
							Methods.removeUnusableAbilities(s.getName());
							return true;
						}
						if (Arrays.asList(wateraliases).contains(args[1].toLowerCase())) {
							bPlayer.setElement(Element.Water);
							s.sendMessage(Methods.getWaterColor() + "You are now a waterbender.");
							Methods.removeUnusableAbilities(s.getName());
							return true;
						}
						if (Arrays.asList(earthaliases).contains(args[1].toLowerCase())) {
							bPlayer.setElement(Element.Earth);
							s.sendMessage(Methods.getEarthColor() + "You are now an Earthbender.");
							Methods.removeUnusableAbilities(s.getName());
							return true;
						}
						if (Arrays.asList(firealiases).contains(args[1].toLowerCase())) {
							bPlayer.setElement(Element.Fire);
							s.sendMessage(Methods.getFireColor() + "You are now a Firebender.");
							Methods.removeUnusableAbilities(s.getName());
							return true;
						}
						if (Arrays.asList(chialiases).contains(args[1].toLowerCase())) {
							bPlayer.setElement(Element.Chi);
							s.sendMessage(Methods.getChiColor() + "You are now a ChiBlocker.");
							Methods.removeUnusableAbilities(s.getName());
							return true;
						}
						s.sendMessage(ChatColor.GOLD + "Proper Usage: " + ChatColor.DARK_AQUA + "/bending choose [Element]");
						s.sendMessage(ChatColor.GOLD + "Applicable Elements: " + ChatColor.DARK_AQUA + "Air, Water, Earth, Fire, Chi");
						return true;
					}
					if (args.length == 3) {
						if (!s.hasPermission("bending.admin.choose")) {
							s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
							return true;
						}
						Player target = Bukkit.getPlayer(args[1]);
						if (target == null) {
							s.sendMessage(ChatColor.RED + "That player is not online.");
							return true;
						}
						BendingPlayer bTarget = Methods.getBendingPlayer(target.getName());

						if (bTarget.isPermaRemoved()) {
							s.sendMessage(ChatColor.RED + "That player's bending was permanently removed.");
							return true;
						}
						Element e = null;
						if (Arrays.asList(airaliases).contains(args[2])) e = Element.Air;
						if (Arrays.asList(wateraliases).contains(args[2])) e = Element.Water;
						if (Arrays.asList(earthaliases).contains(args[2])) e = Element.Earth;
						if (Arrays.asList(firealiases).contains(args[2])) e = Element.Fire;
						if (Arrays.asList(chialiases).contains(args[2])) e = Element.Chi;

						if (e == null) {
							s.sendMessage(ChatColor.RED + "You must specify an element.");
							return true;
						} else {
							bTarget.setElement(e);
							Methods.removeUnusableAbilities(target.getName());
							target.sendMessage(ChatColor.RED + "Your bending has been changed to " + ChatColor.DARK_AQUA + e.toString() + ChatColor.RED + " by " + ChatColor.DARK_AQUA + s.getName());
							return true;
						}
					}
				}
				if (Arrays.asList(helpaliases).contains(args[0].toLowerCase())) {
					if (args.length != 2) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: /bending help Command/Ability");
						s.sendMessage(ChatColor.YELLOW + "/bending add <Player> [Element]");
						s.sendMessage(ChatColor.YELLOW + "/bending bind [Ability] <Slot>");
						s.sendMessage(ChatColor.YELLOW + "/bending clear <slot>");
						s.sendMessage(ChatColor.YELLOW + "/bending choose <Player> [Element]");
						s.sendMessage(ChatColor.YELLOW + "/bending display <Element>");
						s.sendMessage(ChatColor.YELLOW + "/bending import");
						s.sendMessage(ChatColor.YELLOW + "/bending permaremove <Player>");
						s.sendMessage(ChatColor.YELLOW + "/bending remove [Player]");
						s.sendMessage(ChatColor.YELLOW + "/bending toggle");
						s.sendMessage(ChatColor.YELLOW + "/bending version");
						s.sendMessage(ChatColor.YELLOW + "/bending who");
						return true;
					}
					if (!s.hasPermission("bending.command.help")) {
						s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
						return true;
					}
					if (Arrays.asList(importaliases).contains(args[1].toLowerCase())) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: " + ChatColor.DARK_AQUA + "/bending import");
						s.sendMessage(ChatColor.YELLOW + "This command will import your old bendingPlayers.yml from the Bending plugin."
								+ " It will generate a convert.yml file to convert the data to be used with this plugin."
								+ " You can delete the file once the complete message is displayed"
								+ " This command should only be used ONCE.");
					}
					if (Arrays.asList(displayaliases).contains(args[1].toLowerCase())) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: " + ChatColor.DARK_AQUA + "/bending display <Element>");
						s.sendMessage(ChatColor.YELLOW + "This command will show you all of the elements you have bound if you do not specify an element."
								+ " If you do specify an element (Air, Water, Earth, Fire, or Chi), it will show you all of the available "
								+ " abilities of that element installed on the server.");
					}
					if (Arrays.asList(choosealiases).contains(args[1].toLowerCase())) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: " + ChatColor.DARK_AQUA + "/bending choose <Player> [Element]");
						s.sendMessage(ChatColor.GOLD + "Applicable Elements: " + ChatColor.DARK_AQUA + "Air, Water, Earth, Fire, Chi");
						s.sendMessage(ChatColor.YELLOW + "This command will allow the user to choose a player either for himself or <Player> if specified. "
								+ " This command can only be used once per player unless they have permission to rechoose their element.");
						return true;
					}
					if (args[1].equalsIgnoreCase("add")) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: " + ChatColor.DARK_AQUA + "/bending add <Player> [Element]");
						s.sendMessage(ChatColor.GOLD + "Applicable Elements: " + ChatColor.DARK_AQUA + "Air, Water, Earth, Fire, Chi");
						s.sendMessage(ChatColor.YELLOW + "This command will allow the user to add an element to the targeted <Player>, or themselves if the target"
								+ " is not specified. This command is typically reserved for server administrators.");
						return true;
					}
					if (Arrays.asList(permaremovealiases).contains(args[1].toLowerCase())) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: " + ChatColor.DARK_AQUA + "/bending permaremove <Player>");
						s.sendMessage(ChatColor.YELLOW + "This command will permanently remove the Bending of the targeted <Player>. Once removed, a player"
								+ " may only receive Bending again if this command is run on them again. This command is typically reserved for"
								+ " administrators.");
						return true;
					}
					if (Arrays.asList(versionaliases).contains(args[1].toLowerCase())) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: " + ChatColor.DARK_AQUA + "/bending version");
						s.sendMessage(ChatColor.YELLOW + "This command will print out the version of ProjectKorra this server is running.");
						return true;
					}

					if (Arrays.asList(removealiases).contains(args[1].toLowerCase())) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: " + ChatColor.DARK_AQUA + "/bending remove [Player]");
						s.sendMessage(ChatColor.YELLOW + "This command will remove the element of the targeted [Player]. The player will be able to re-pick "
								+ " their element after this command is run on them, assuming their Bending was not permaremoved.");
						return true;
					}

					if (Arrays.asList(togglealiases).contains(args[1].toLowerCase())) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: " + ChatColor.DARK_AQUA + "/bending toggle <all>");
						s.sendMessage(ChatColor.YELLOW + "This command will toggle a player's own Bending on or off. If toggled off, all abilities should stop"
								+ " working until it is toggled back on. Logging off will automatically toggle your Bending back on. If you run the command /bending toggle all, Bending will be turned off for all players and cannot be turned back on until the command is run again.");
						return true;
					}
					if (args[1].equalsIgnoreCase("who")) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: " + ChatColor.DARK_AQUA + "/bending who <Player>");
						s.sendMessage(ChatColor.YELLOW + "This command will tell you what element all players that are online are (If you don't specify a player)"
								+ " or give you information about the player that you specify.");
						return true;
					}

					if (Arrays.asList(clearaliases).contains(args[1].toLowerCase())) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: " + ChatColor.DARK_AQUA + "/bending clear <slot>");
						s.sendMessage(ChatColor.YELLOW + "This command will clear the bound ability from the slot you specify (if you specify one."
								+ " If you choose not to specify a slot, all of your abilities will be cleared.");
					}
					if (Arrays.asList(reloadaliases).contains(args[1].toLowerCase())) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: " + ChatColor.DARK_AQUA + "/bending reload");
						s.sendMessage(ChatColor.YELLOW + "This command will reload the Bending config file.");
						return true;
					}
					if (Arrays.asList(bindaliases).contains(args[1].toLowerCase())) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: " + ChatColor.DARK_AQUA + "/bending bind [Ability] <Slot>");
						s.sendMessage(ChatColor.YELLOW + "This command will bind an ability to the slot you specify (if you specify one), or the slot currently"
								+ " selected in your hotbar (If you do not specify a Slot #).");
					}

					if (Methods.abilityExists(args[1])) {
						String ability = Methods.getAbility(args[1]);
						if (Methods.isAirAbility(ability)) {
							s.sendMessage(Methods.getAirColor() + ability + " - ");
							s.sendMessage(Methods.getAirColor() + AbilityModuleManager.descriptions.get(ability));
						}
						else if (Methods.isWaterAbility(ability)) {
							s.sendMessage(Methods.getWaterColor() + ability + " - ");
							s.sendMessage(Methods.getWaterColor() + AbilityModuleManager.descriptions.get(ability));
						}
						else if (Methods.isEarthAbility(ability)) {
							if (Methods.isMetalbendingAbility(ability)) {
								s.sendMessage(Methods.getMetalbendingColor() + ability + " - ");
								s.sendMessage(Methods.getMetalbendingColor() + AbilityModuleManager.descriptions.get(ability));
							} else {
								s.sendMessage(Methods.getEarthColor() + ability + " - ");
								s.sendMessage(Methods.getEarthColor() + AbilityModuleManager.descriptions.get(ability));
							}
						}
						else if (Methods.isFireAbility(ability)) {
							s.sendMessage(Methods.getFireColor() + ability + " - ");
							s.sendMessage(Methods.getFireColor() + AbilityModuleManager.descriptions.get(ability));
						}
						else if (Methods.isChiAbility(ability)) {
							s.sendMessage(Methods.getChiColor() + ability + " - ");
							s.sendMessage(Methods.getChiColor() + AbilityModuleManager.descriptions.get(ability));
						}
						else {
							s.sendMessage(Methods.getAvatarColor() + ability + " - ");
							s.sendMessage(Methods.getAvatarColor() + AbilityModuleManager.descriptions.get(ability));
						}
					}
				}
				return true;
			}
		}; projectkorra.setExecutor(exe);
	}

}

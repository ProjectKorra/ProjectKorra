package com.projectkorra.ProjectKorra;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import com.projectkorra.ProjectKorra.Ability.AbilityModuleManager;
import com.projectkorra.ProjectKorra.Ability.StockAbilities;
import com.projectkorra.ProjectKorra.Objects.Preset;
import com.projectkorra.ProjectKorra.Utilities.GrapplingHookAPI;
import com.projectkorra.rpg.RPGMethods;

public class Commands {

	ProjectKorra plugin;

	public Commands(ProjectKorra plugin) {
		this.plugin = plugin;
		init();
	}

	/*
	 * Element Aliases
	 */
	String[] airaliases = {"air", "a", "airbending", "airbender"};
	String[] wateraliases = {"water", "w", "waterbending", "waterbender"};
	String[] earthaliases = {"earth", "e", "earthbending", "earthbender"};
	String[] firealiases = {"fire", "f", "firebending", "firebender"};
	String[] chialiases = {"chi", "c", "chiblocking", "chiblocker"};

	/*
	 * Command Aliases
	 */
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
	String[] givealiases = {"give", "g", "spawn"};
	String[] invinciblealiases = {"invincible", "inv"};
	String[] presetaliases = {"preset", "presets", "pre", "set", "p"};
	String[] avataraliases = {"avatar", "ava"};

	/*
	 * Item Aliases
	 */

	public static Set<String> invincible = new HashSet<String>();

	String[] grapplinghookaliases = {"grapplinghook", "grapplehook", "hook", "ghook"};

	public static boolean debug = ProjectKorra.plugin.getConfig().getBoolean("debug");

	public static boolean isToggledForAll = false;

	private static BukkitTask importTask;
	private void init() {
		PluginCommand projectkorra = plugin.getCommand("projectkorra");
		CommandExecutor exe;

		exe = new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
                FileConfiguration config = plugin.getConfig();
				for(int i = 0; i < args.length; i++){
					args[i] = args[i].toLowerCase();
				}
				if (args.length == 0) {
					s.sendMessage(ChatColor.RED + "/bending help [Ability/Command] " + ChatColor.YELLOW + "Display help.");
					s.sendMessage(ChatColor.RED + "/bending choose [Element] " + ChatColor.YELLOW + "Choose an element.");
					s.sendMessage(ChatColor.RED + "/bending bind [Ability] # " + ChatColor.YELLOW + "Bind an ability.");
					return true;
				}
				if (Arrays.asList(avataraliases).contains(args[0].toLowerCase())) {
					if (!Methods.hasRPG()) {
						s.sendMessage(ChatColor.RED + "This command cannot be used unless you have ProjectKorra (RPG) installed.");
						return true;
					}
					
					if (!s.hasPermission("bending.command.avatar")) {
						s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
						return true;
					}
					
					if (args.length != 2) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: /bending avatar [Player]");
						return true;
					}
					
					Player player = Bukkit.getPlayer(args[1]);
					if (player == null) {
						s.sendMessage(ChatColor.RED + "That player is not online.");
						return true;
					}
					
					UUID uuid = player.getUniqueId();
					
					if (RPGMethods.hasBeenAvatar(uuid)) {
						s.sendMessage(ChatColor.RED + "This player has already been the Avatar.");
						return true;
					}
					
					RPGMethods.setAvatar(uuid);
					s.sendMessage(ChatColor.DARK_AQUA + player.getName() + ChatColor.GREEN + " is now the Avatar.");
					player.sendMessage("You are now the Avatar.");
					return true;
					
				}
				if (args[0].equalsIgnoreCase("debug")) {
					if (args.length != 1) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: /bending debug");
						return true;
					}
					
					if (!s.hasPermission("bending.admin.debug")) {
						s.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
						return true;
					}
					
					Methods.runDebug();
					s.sendMessage(ChatColor.GREEN + "Debug File Created as debug.txt in the ProjectKorra plugin folder.");
					s.sendMessage(ChatColor.GREEN + "Put contents on pastie.org and create a bug report  on the ProjectKorra forum if you need to.");
					return true;
				}
				if (Arrays.asList(presetaliases).contains(args[0].toLowerCase())) {
					if (!(s instanceof Player)) {
						s.sendMessage(ChatColor.RED + "This command is only usable by players.");
						return true;
					}

					Player player = (Player) s;

					String[] deletealiases = {"delete", "d", "del"};
					String[] createaliases = {"create", "c", "save"};
					String[] listaliases = {"list", "l"};
					if (args.length == 2 && Arrays.asList(listaliases).contains(args[1].toLowerCase())) {
						if (!s.hasPermission("bending.command.preset.list")) {
							s.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
							return true;
						}

						List<Preset> listnames = Preset.presets.get(player.getUniqueId());
						List<String> ln2 = new ArrayList<String>();

						if (listnames == null || listnames.isEmpty()) {
							s.sendMessage(ChatColor.RED + "You don't have any presets.");
							return true;
						}

						for (Preset preset: listnames) {
							ln2.add(preset.getName());
						}

						s.sendMessage(ChatColor.GREEN + "Your Presets: " + ChatColor.DARK_AQUA + ln2.toString());
						return true;						

					}else if (args.length != 3) { // bending preset bind|create|delete {name}
						s.sendMessage(ChatColor.GOLD + "Proper Usage: /bending preset create|bind|list|delete [name]");
						return true;
					}

					String name = args[2];

					if (Arrays.asList(deletealiases).contains(args[1].toLowerCase())) {
						if (!s.hasPermission("bending.command.preset.delete")) {
							s.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
							return true;
						}
						if (!Preset.presetExists(player, name)) {
							s.sendMessage(ChatColor.RED + "You don't have a preset with that name.");
							return true;
						}

						Preset preset = Preset.getPreset(player, name);
						preset.delete();
						s.sendMessage(ChatColor.GREEN + "You have deleted your preset named: " + name);
					}

					if (Arrays.asList(bindaliases).contains(args[1].toLowerCase())) {
						if (!s.hasPermission("bending.command.preset.bind")) {
							s.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
							return true;
						}

						if (!Preset.presetExists(player, name)) {
							s.sendMessage(ChatColor.RED + "You don't have a preset with that name.");
							return true;
						}

						Preset.bindPreset(player, name);
						s.sendMessage(ChatColor.GREEN + "Your bound slots have been set to match the " + name + " preset.");
						return true;
					}

					if (Arrays.asList(createaliases).contains(args[1].toLowerCase())) {
						if (!s.hasPermission("bending.command.preset.create")) {
							s.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
							return true;
						}

						int limit = Methods.getMaxPresets(player);

						if (Preset.presets.get(player) != null && Preset.presets.get(player).size() >= limit) {
							s.sendMessage(ChatColor.RED + "You have reached your max number of Presets.");
							return true;
						}

						if (Preset.presetExists(player, name)) {
							s.sendMessage(ChatColor.RED + "A preset with that name already exists.");
							return true;
						}

						BendingPlayer bPlayer = Methods.getBendingPlayer(player.getName());

						if (bPlayer == null) return true;

						HashMap<Integer, String> abilities = bPlayer.getAbilities();
						Preset preset = new Preset(player.getUniqueId(), name, abilities);
						preset.save();
						s.sendMessage(ChatColor.GREEN + "Created preset with the name: " + name);
						return true;
					}

				}
				if (Arrays.asList(invinciblealiases).contains(args[0].toLowerCase())) {
					if (!s.hasPermission("bending.command.invincible")) {
						s.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
						return true;
					}

					if (!(s instanceof Player)) {
						s.sendMessage(ChatColor.RED + "This command is only usable by players.");
						return true;
					}

					if (args.length != 1) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: /bending invincible");
						return true;
					}

					if (!invincible.contains(s.getName())) {
						/*
						 * Player is not invincible.
						 */
						invincible.add(s.getName());
						s.sendMessage(ChatColor.GREEN + "You are now invincible to all bending damage and effects. Use this command again to disable this.");
						return true;
					} else {
						invincible.remove(s.getName());
						s.sendMessage(ChatColor.RED + "You are no longer invincible to all bending damage and effects.");
					}
				}
				if (Arrays.asList(givealiases).contains(args[0].toLowerCase())) {
					if (!s.hasPermission("bending.command.give")) {
						s.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
						return true;
					}

					if (args.length < 3) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: /bending give [Player] [Item] <Properties>");
						return true;
					}

					Player player = Bukkit.getPlayer(args[1]);

					if (player == null) {
						s.sendMessage(ChatColor.RED + "That player is not online.");
						return true;
					}

					if (Arrays.asList(grapplinghookaliases).contains(args[2].toLowerCase())) {
						/*
						 * They are spawning in a grappling hook.
						 * bending give [Player] grapplinghook [# of Uses]
						 */

						if (args.length != 4) {
							s.sendMessage(ChatColor.GOLD + "Proper Usage: /bending give [Player] GrapplingHook <#OfUses>");
							return true;
						}
						int uses;
						try {
							uses = Integer.parseInt(args[3]);
						} catch (NumberFormatException e) {
							s.sendMessage(ChatColor.RED + "You must specify a number of uses you want the grappling hook to have.");
							s.sendMessage(ChatColor.GOLD + "Example: /bending give " + s.getName() + " grapplinghook 25");
							return true;
						}

						ItemStack hook = GrapplingHookAPI.createHook(uses);
						player.getInventory().addItem(hook);
						s.sendMessage(ChatColor.GREEN + "A grappling hook with " + uses + " uses has been added to your inventory.");
						return true;
					} else {
						s.sendMessage(ChatColor.RED + "That is not a valid Bending item.");
						s.sendMessage(ChatColor.GOLD + "Acceptable Items: GrapplingHook");
						return true;
					}
				}
				if (Arrays.asList(reloadaliases).contains(args[0].toLowerCase())) {
					if (args.length != 1) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: /bending reload");
						return true;
					}

					if (!s.hasPermission("bending.command.reload")) {
						s.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
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
						s.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
						return true;
					}

					if (!(s instanceof Player)) {
						s.sendMessage(ChatColor.RED + "This command is only usable by players.");
						return true;
					}
					BendingPlayer bPlayer = Methods.getBendingPlayer(s.getName());
					if (args.length == 1) {
						bPlayer.getAbilities().clear();
						for (int i = 1; i <= 9; i++) {
							Methods.saveAbility(bPlayer, i, null);
						}
						s.sendMessage("Your bound abilities have been cleared.");
						return true;
					}

					if (args.length == 2) {
						try {
							int slot = Integer.parseInt(args[1]);
							if (slot < 1 || slot > 9) {
								s.sendMessage(ChatColor.RED + "The slot must be an integer between 0 and 9.");
								return true;
							}
							if (bPlayer.getAbilities().get(slot) != null) {
								bPlayer.getAbilities().remove(slot);
								Methods.saveAbility(bPlayer, slot, null);
							}
							s.sendMessage("You have cleared slot #" + slot);
							return true;
						} catch (NumberFormatException e) {
							s.sendMessage(ChatColor.RED + "The slot must be an integer between 0 and 9.");
						}
					}
				}
				if (Arrays.asList(bindaliases).contains(args[0].toLowerCase())) {
					if (args.length > 3 || args.length == 1) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: /bending bind [Ability] <#>");
						return true;
					}

					if (!s.hasPermission("bending.command.bind")) {
						s.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
						return true;
					}

					if (!(s instanceof Player)) {
						s.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
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
							s.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
							return true;
						}
						if (Methods.isAirAbility(ability) && !Methods.isBender(s.getName(), Element.Air)) {
							s.sendMessage(Methods.getAirColor() + config.getString("messages.must-be-airbender"));
							return true;
						}
						if (Methods.isWaterAbility(ability) && !Methods.isBender(s.getName(), Element.Water)) {
							s.sendMessage(Methods.getWaterColor() + config.getString("messages.must-be-waterbender"));
							return true;
						}
						if (Methods.isEarthAbility(ability) && !Methods.isBender(s.getName(), Element.Earth)) {
							s.sendMessage(Methods.getEarthColor() + config.getString("messages.must-be-earthbender"));
							return true;
						}
						if (Methods.isFireAbility(ability) && !Methods.isBender(s.getName(), Element.Fire)) {
							s.sendMessage(Methods.getFireColor() + config.getString("messages.must-be-firebender"));
							return true;
						}
						if (Methods.isChiAbility(ability) && !Methods.isBender(s.getName(), Element.Chi)) {
							s.sendMessage(Methods.getChiColor() + config.getString("messages.must-be-chiblocker"));
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
							s.sendMessage(ChatColor.RED + config.getString("messages.invalid-ability"));
							return true;
						}
						String ability = Methods.getAbility(abil);
						int slot = 0;
						try {
							slot = Integer.parseInt(args[2]);
						} catch (NumberFormatException e) {
							s.sendMessage(ChatColor.RED + config.getString("messages.invalid-slot"));
							return true;
						}
						if (slot < 1 || slot > 9) {
							s.sendMessage(ChatColor.RED + config.getString("messages.invalid-slot"));
							return true;
						}

						if (!Methods.hasPermission((Player) s, ability)) {
							s.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
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
						s.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
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

								HashMap<Integer, String> abilities = bPlayer.getAbilities();

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
						s.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
						return true;
					}

					if (args.length == 2) {
						if (!(s instanceof Player)) {
							s.sendMessage(ChatColor.RED + "This command is only usable by players.");
							return true;
						}
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
									if (Methods.isSubAbility(Element.Earth, st)) {
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
									ChatColor.WHITE + " | " +
									Methods.getWaterColor() + "Water" +
									ChatColor.WHITE + " | " +
									Methods.getEarthColor() + "Earth" + 
									ChatColor.WHITE + " | " +
									Methods.getFireColor() + "Fire" +
									ChatColor.WHITE + " | " +
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
						HashMap<Integer, String> abilities = bPlayer.getAbilities();

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
							s.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
							return true;
						}

						if (!(s instanceof Player)) {
							s.sendMessage(ChatColor.RED + "This command is only usable by players.");
							return true;
						}

						BendingPlayer bPlayer = Methods.getBendingPlayer(s.getName());

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
							s.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
							return true;
						}

						if (isToggledForAll) { // Bending is toggled off for all players.
							isToggledForAll = false;
							for (Player player: Bukkit.getOnlinePlayers()) {
								player.sendMessage(ChatColor.GREEN + "Bending has been toggled back on for all players.");
								return true;
							}
						} else {
							isToggledForAll = true;
							for (Player player: Bukkit.getOnlinePlayers()) {
								player.sendMessage(ChatColor.RED + "Bending has been toggled off for all players.");
								return true;
							}
						}
					} else {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: /bending toggle <all>");
						return true;
					}
				}
				if (Arrays.asList(whoaliases).contains(args[0].toLowerCase())) {
					if (args.length > 2) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: /bending who <Player>");
						return true;
					}
					if (!s.hasPermission("bending.command.who")) {
						s.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
						return true;
					}

					if (args.length == 2) {

						Player p = Bukkit.getPlayer(args[1]);
						if (p == null) {
							s.sendMessage(ChatColor.GREEN + "You are running a lookup of an offline player, this may take a second.");
							ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM pk_players WHERE player = '" + args[1] + "'");
							try {
								UUID uuid = UUID.fromString(rs2.getString("uuid"));
								if (rs2.next()) {
									String element = rs2.getString("element");
									s.sendMessage(args[1] + " - ");
									if (element.contains("a")) {
										s.sendMessage(Methods.getAirColor() + "- Airbender");
									}
									if (element.contains("w")) {
										s.sendMessage(Methods.getWaterColor() + "- Waterbender");
									}
									if (element.contains("e")) {
										s.sendMessage(Methods.getEarthColor() + "- Earthbender");
									}
									if (element.contains("f")) {
										s.sendMessage(Methods.getFireColor() + "- Firebender");
									}
									if (element.contains("c")) {
										s.sendMessage(Methods.getChiColor() + "- Chiblocker");
									}
									if (Methods.hasRPG()) {
										if (RPGMethods.isCurrentAvatar(uuid)) {
											s.sendMessage(Methods.getAvatarColor() + "Current Avatar");
										} else if (RPGMethods.hasBeenAvatar(uuid)) {
											s.sendMessage(Methods.getAvatarColor() + "Former Avatar");
										} else {
											
										}
									}
								} else {
									s.sendMessage(ChatColor.RED + "We could not find any player in your database with that username. Are you sure it is typed correctly?");
								}
							} catch (SQLException e) {
								e.printStackTrace();
							}
							return true;
						}

						String un = p.getName();
						s.sendMessage(un + " - ");
						if (Methods.isBender(un, Element.Air)) {
							s.sendMessage(Methods.getAirColor() + "- Airbender");
						}
						if (Methods.isBender(un, Element.Water)) {
							s.sendMessage(Methods.getWaterColor() + "- Waterbender");
							if (Methods.canPlantbend(p)) {
								s.sendMessage(Methods.getWaterColor() + "    Can Plantbend");
							}
							if (Methods.canBloodbend(p)) {
								s.sendMessage(Methods.getWaterColor() + "    Can Bloodbend");
							}
						}
						if (Methods.isBender(un, Element.Earth)) {
							s.sendMessage(Methods.getEarthColor() + "- Earthbender");
							if (Methods.canMetalbend(p)) {
								s.sendMessage(Methods.getMetalbendingColor() + "    Can Metalbend");
							}
							if (Methods.canLavabend(p)) {
								s.sendMessage(Methods.getMetalbendingColor() + "    Can Lavabend");
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
								if (ability == null || ability.equalsIgnoreCase("null")) {
									continue;
								} else {
									s.sendMessage(i + " - " + Methods.getAbilityColor(ability) + ability);
								}
							}
						}
						
						if (Methods.hasRPG()) {
							if (RPGMethods.isCurrentAvatar(p.getUniqueId())) {
								s.sendMessage(Methods.getAvatarColor() + "Current Avatar");
							} else if (RPGMethods.hasBeenAvatar(p.getUniqueId())) {
								s.sendMessage(Methods.getAvatarColor() + "Former Avatar");
							}
						}

						if (p.getName().equalsIgnoreCase("MistPhizzle")) {
							s.sendMessage(ChatColor.YELLOW + "ProjectKorra Founder"); // MistPhizzle
						}
						
						if (p.getName().equalsIgnoreCase("runefist") // codiaz
								|| p.getName().equalsIgnoreCase("Jacklin213") // Jacklin213
								|| p.getName().equalsIgnoreCase("kingbirdy") // kingbirdy
								|| p.getName().equalsIgnoreCase("CherryCoolade") // Coolade
								|| p.getName().equalsIgnoreCase("sampepere") // AlexTheCoder
								|| p.getName().equalsIgnoreCase("Carbogen")) // Carbogen
						{ 
							s.sendMessage(ChatColor.YELLOW + "ProjectKorra Developer");
						}
						if (p.getName().equalsIgnoreCase("vidcom") // vidcom
								|| p.getName().equalsIgnoreCase("Zolteex") // Zolteex
								|| p.getName().equalsIgnoreCase("Rawry2001") // Majorite
								|| p.getName().equalsIgnoreCase("OmniCypher") // OmniCypher
								|| p.getName().equalsIgnoreCase("Gahshunk292") // Gahshunk
								|| p.getName().equalsIgnoreCase("ashe36")) { // ashe36
							s.sendMessage(ChatColor.YELLOW + "ProjectKorra Concept Designer");
						}
						
						if (p.getName().equalsIgnoreCase("kiammcquaid")
								|| p.getName().equalsIgnoreCase("UnlitedOwns")) {
							s.sendMessage(ChatColor.YELLOW + "ProjectKorra GFX Artist");
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
						s.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
						return true;
					}
					s.sendMessage(ChatColor.GREEN + "Core Version: " + ChatColor.RED + plugin.getDescription().getVersion());
					if (Methods.hasRPG()) {
						s.sendMessage(ChatColor.GREEN + "RPG Version: " + ChatColor.RED + Methods.getRPG().getDescription().getVersion());
					}
					s.sendMessage(ChatColor.GREEN + "Founded by: " + ChatColor.RED + "MistPhizzle");
					s.sendMessage(ChatColor.GREEN + "Learn More: " + ChatColor.RED + "http://projectkorra.com");
					return true;
				}
				if (Arrays.asList(removealiases).contains(args[0].toLowerCase())) {
					//bending remove [Player]
					if (args.length != 2) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: /bending remove [Player]");
						return true;
					}

					if (!s.hasPermission("bending.admin.remove")) {
						s.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
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
					Methods.saveElements(bPlayer);
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
						s.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
						return true;
					}

					Player player = Bukkit.getPlayer(args[1]);

					if (player == null) {
						s.sendMessage(ChatColor.RED + "That player is not online.");
						return true;
					}

					BendingPlayer bPlayer = Methods.getBendingPlayer(player.getName());

					if (bPlayer.isPermaRemoved()) {
						bPlayer.permaRemoved = false;
						Methods.savePermaRemoved(bPlayer);
						s.sendMessage(ChatColor.RED + "You have restored the bending of: " + ChatColor.DARK_AQUA + player.getName());
						return true;
					}

					bPlayer.elements.clear();
					Methods.removeUnusableAbilities(player.getName());
					Methods.saveElements(bPlayer);
					bPlayer.permaRemoved = true;
					Methods.savePermaRemoved(bPlayer);
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
							s.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
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
							Methods.saveElements(bPlayer);
							player.sendMessage(Methods.getAirColor() + "You are also an Airbender.");
							s.sendMessage(ChatColor.DARK_AQUA + player.getName() + Methods.getAirColor() + " is also an Airbender.");
							return true;
						}

						if (Arrays.asList(wateraliases).contains(args[2].toLowerCase())) {
							bPlayer.addElement(Element.Water);
							Methods.saveElements(bPlayer);
							player.sendMessage(Methods.getWaterColor() + "You are also a waterbender.");
							s.sendMessage(ChatColor.DARK_AQUA + player.getName() + Methods.getWaterColor() + " is also a Waterbender.");
							return true;
						}

						if (Arrays.asList(earthaliases).contains(args[2].toLowerCase())) {
							bPlayer.addElement(Element.Earth);
							Methods.saveElements(bPlayer);
							player.sendMessage(Methods.getEarthColor() + "You are also an Earthbender.");
							s.sendMessage(ChatColor.DARK_AQUA + player.getName() + Methods.getEarthColor() + " is also an Earthbender.");
							return true;
						}

						if (Arrays.asList(firealiases).contains(args[2].toLowerCase())) {
							bPlayer.addElement(Element.Fire);
							Methods.saveElements(bPlayer);
							player.sendMessage(Methods.getFireColor() + "You are also a Firebender.");
							s.sendMessage(ChatColor.DARK_AQUA + player.getName() + Methods.getFireColor() + " is also a Firebender");
							return true;
						}
						if (Arrays.asList(chialiases).contains(args[2].toLowerCase())) {
							bPlayer.addElement(Element.Chi);
							Methods.saveElements(bPlayer);
							player.sendMessage(Methods.getChiColor() + "You are also a ChiBlocker.");
							s.sendMessage(ChatColor.DARK_AQUA + player.getName() + Methods.getChiColor() + " is also a ChiBlocker");
							return true;
						}

						s.sendMessage(ChatColor.RED + config.getString("messages.must-specify-element"));
						return true;
					}
					if (args.length == 2) {
						// Target = Self
						if (!s.hasPermission("bending.command.add")) {
							s.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
							return true;
						}

						if (!(s instanceof Player)) {
							s.sendMessage(ChatColor.RED + "This command is only usable by Players.");
							return true;
						}

						BendingPlayer bPlayer = Methods.getBendingPlayer(s.getName());

						if (Arrays.asList(airaliases).contains(args[1].toLowerCase())) {
							bPlayer.addElement(Element.Air);
							Methods.saveElements(bPlayer);
							s.sendMessage(Methods.getAirColor() + "You are also an airbender.");
							return true;
						}

						if (Arrays.asList(wateraliases).contains(args[1].toLowerCase())) {
							bPlayer.addElement(Element.Water);
							Methods.saveElements(bPlayer);
							s.sendMessage(Methods.getWaterColor() + "You are also a waterbender.");
							return true;
						}

						if (Arrays.asList(earthaliases).contains(args[1].toLowerCase())) {
							bPlayer.addElement(Element.Earth);
							Methods.saveElements(bPlayer);
							s.sendMessage(Methods.getEarthColor() + "You are also an Earthbender.");
							return true;
						}

						if (Arrays.asList(firealiases).contains(args[1].toLowerCase())) {
							bPlayer.addElement(Element.Fire);
							Methods.saveElements(bPlayer);
							s.sendMessage(Methods.getFireColor() + "You are also a Firebender.");
							return true;
						}
						if (Arrays.asList(chialiases).contains(args[1].toLowerCase())) {
							bPlayer.addElement(Element.Chi);
							Methods.saveElements(bPlayer);
							s.sendMessage(Methods.getChiColor() + "You are also a ChiBlocker.");
							return true;
						}
						s.sendMessage(ChatColor.RED + config.getString("messages.must-specify-element"));
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
							s.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
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
								s.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
								return true;
							}
						}
						if (Arrays.asList(airaliases).contains(args[1].toLowerCase())) {
							bPlayer.setElement(Element.Air);
							s.sendMessage(Methods.getAirColor() + "You are now an Airbender.");
							Methods.removeUnusableAbilities(s.getName());
							Methods.saveElements(bPlayer);
							return true;
						}
						if (Arrays.asList(wateraliases).contains(args[1].toLowerCase())) {
							bPlayer.setElement(Element.Water);
							s.sendMessage(Methods.getWaterColor() + "You are now a Waterbender.");
							Methods.removeUnusableAbilities(s.getName());
							Methods.saveElements(bPlayer);
							return true;
						}
						if (Arrays.asList(earthaliases).contains(args[1].toLowerCase())) {
							bPlayer.setElement(Element.Earth);
							s.sendMessage(Methods.getEarthColor() + "You are now an Earthbender.");
							Methods.removeUnusableAbilities(s.getName());
							Methods.saveElements(bPlayer);
							return true;
						}
						if (Arrays.asList(firealiases).contains(args[1].toLowerCase())) {
							bPlayer.setElement(Element.Fire);
							s.sendMessage(Methods.getFireColor() + "You are now a Firebender.");
							Methods.removeUnusableAbilities(s.getName());
							Methods.saveElements(bPlayer);
							return true;
						}
						if (Arrays.asList(chialiases).contains(args[1].toLowerCase())) {
							bPlayer.setElement(Element.Chi);
							s.sendMessage(Methods.getChiColor() + "You are now a ChiBlocker.");
							Methods.removeUnusableAbilities(s.getName());
							Methods.saveElements(bPlayer);
							return true;
						}
						s.sendMessage(ChatColor.GOLD + "Proper Usage: " + ChatColor.DARK_AQUA + "/bending choose [Element]");
						s.sendMessage(ChatColor.GOLD + "Applicable Elements: " + ChatColor.DARK_AQUA + "Air, Water, Earth, Fire, Chi");
						return true;
					}
					if (args.length == 3) {
						if (!s.hasPermission("bending.admin.choose")) {
							s.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
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
							s.sendMessage(ChatColor.RED + config.getString("messages.must-specify-element"));
							return true;
						} else {
							bTarget.setElement(e);
							Methods.removeUnusableAbilities(target.getName());
							Methods.saveElements(bTarget);
							s.sendMessage(ChatColor.RED + "You have changed " + ChatColor.DARK_AQUA + target.getName() + "'s " + ChatColor.RED + "element to " + ChatColor.DARK_AQUA + e.toString() + ChatColor.RED + ".");
							target.sendMessage(ChatColor.RED + "Your bending has been changed to " + ChatColor.DARK_AQUA + e.toString() + ChatColor.RED + " by " + ChatColor.DARK_AQUA + s.getName());
							return true;
						}
					}
				}
				if (Arrays.asList(helpaliases).contains(args[0].toLowerCase())) {
					if (!s.hasPermission("bending.command.help")) {
						s.sendMessage(ChatColor.RED + config.getString("messages.no-permission"));
						return true;
					}
					if (args.length != 2) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: /bending help Command/Ability");
						if (s.hasPermission("bending.command.add")) {
							s.sendMessage(ChatColor.YELLOW + "/bending add <Player> [Element]");
						}
						if (s.hasPermission("bending.command.bind"))
							s.sendMessage(ChatColor.YELLOW + "/bending bind [Ability] <Slot>");
						if (s.hasPermission("bending.command.clear"))
							s.sendMessage(ChatColor.YELLOW + "/bending clear <slot>");
						if (s.hasPermission("bending.command.choose"))
							s.sendMessage(ChatColor.YELLOW + "/bending choose <Player> [Element]");
						if (s.hasPermission("bending.command.display"))
							s.sendMessage(ChatColor.YELLOW + "/bending display <Element>");
						if (s.hasPermission("bending.command.import"))
							s.sendMessage(ChatColor.YELLOW + "/bending import");
						if (s.hasPermission("bending.admin.permaremove"))
							s.sendMessage(ChatColor.YELLOW + "/bending permaremove <Player>");
						if (s.hasPermission("bending.admin.remove"))
							s.sendMessage(ChatColor.YELLOW + "/bending remove [Player]");
						if (s.hasPermission("bending.command.toggle"))
							s.sendMessage(ChatColor.YELLOW + "/bending toggle");
						if (s.hasPermission("bending.command.version"))
							s.sendMessage(ChatColor.YELLOW + "/bending version");
						if (s.hasPermission("bending.command.who"))
							s.sendMessage(ChatColor.YELLOW + "/bending who");
						if (s.hasPermission("bending.command.give"))
							s.sendMessage(ChatColor.YELLOW + "/bending give [Player] [Item] <Properties>");
						if (s.hasPermission("bending.command.invincible"))
							s.sendMessage(ChatColor.YELLOW + "/bending invincible");
						return true;
					}
					if (Arrays.asList(airaliases).contains(args[1].toLowerCase())) {
						s.sendMessage(Methods.getAirColor() + config.getString("Properties.Air.Description"));
//						s.sendMessage(ChatColor.YELLOW + "Learn More: " + ChatColor.DARK_AQUA + "http://tinyurl.com/qffg9m3");
						s.sendMessage(ChatColor.YELLOW + config.getString("messages.learn-more") + ChatColor.DARK_AQUA + config.getString("Properties.Air.Reference"));
					}
					if (Arrays.asList(wateraliases).contains(args[1].toLowerCase())) {
						s.sendMessage(Methods.getWaterColor() + config.getString("Properties.Water.Description"));
//						s.sendMessage(ChatColor.YELLOW + "Learn More: " + ChatColor.DARK_AQUA + "http://tinyurl.com/lod3plv");
                        s.sendMessage(ChatColor.YELLOW + config.getString("messages.learn-more") + ChatColor.DARK_AQUA + config.getString("Properties.Water.Reference"));
					}
					if (Arrays.asList(earthaliases).contains(args[1].toLowerCase())) {
						s.sendMessage(Methods.getEarthColor() + config.getString("Properties.Earth.Description"));
//						s.sendMessage(ChatColor.YELLOW + "Learn More: " + ChatColor.DARK_AQUA + "http://tinyurl.com/qaudl42");
                        s.sendMessage(ChatColor.YELLOW + config.getString("messages.learn-more") + ChatColor.DARK_AQUA + config.getString("Properties.Earth.Reference"));
					}
					if (Arrays.asList(firealiases).contains(args[1].toLowerCase())) {
						s.sendMessage(Methods.getFireColor() + config.getString("Properties.Fire.Description"));

                        //TODO Transfer combo info to config
						s.sendMessage(ChatColor.YELLOW + "Firebenders can chain their abilities into combos, type "
								+ Methods.getFireColor() + "/b help FireCombo" + ChatColor.YELLOW + " for more information.");
//						s.sendMessage(ChatColor.YELLOW + "Learn More: " + ChatColor.DARK_AQUA + "http://tinyurl.com/k4fkjhb");
                        s.sendMessage(ChatColor.YELLOW + config.getString("messages.learn-more") + ChatColor.DARK_AQUA + config.getString("Properties.Fire.Reference"));
					}
					if (Arrays.asList(chialiases).contains(args[1].toLowerCase())) {
						s.sendMessage(Methods.getChiColor() + config.getString("Properties.Chi.Description") );
//						s.sendMessage(ChatColor.YELLOW + "Learn More: " + ChatColor.DARK_AQUA + "http://tinyurl.com/mkp9n6y");
                        s.sendMessage(ChatColor.YELLOW + config.getString("messages.learn-more") + ChatColor.DARK_AQUA + config.getString("Properties.Chi.Reference"));
					}
					if (Arrays.asList(invinciblealiases).contains(args[1].toLowerCase())) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: " + ChatColor.DARK_AQUA + "/bending invincible");
						s.sendMessage(ChatColor.YELLOW + "This command will make you impervious to all Bending damage. Once you "
								+ "use this command, you will stay invincible until you either log off, or use this command again.");
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
					if (Arrays.asList(givealiases).contains(args[1].toLowerCase())) {
						s.sendMessage(ChatColor.GOLD + "Proper Usage: " + ChatColor.DARK_AQUA + "/bending give [Player] [Item] <Properties>");
						s.sendMessage(ChatColor.YELLOW + "This command will give you an item that was created for the Bending plugin so you do not have to craft it."
								+ " Each item may have its own properties involved, so the amount of arguments may change. However, the Player and Item will be "
								+ " required each time you use this command.");
						s.sendMessage(ChatColor.DARK_AQUA + "Items: GrapplingHook");
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
					if (args[1].equalsIgnoreCase("FireCombo")) {
						s.sendMessage(ChatColor.GOLD + "Fire Combos:");
						s.sendMessage(Methods.getFireColor() + "FireKick" + ChatColor.WHITE + ": A short ranged arc of fire launches from the player's feet dealing moderate damage to enemies.");
						s.sendMessage(ChatColor.GOLD + "FireBlast > FireBlast > (Hold Shift) > FireBlast. ");
						s.sendMessage(Methods.getFireColor() + "FireSpin" + ChatColor.WHITE + ": A circular array of fire that causes damage and massive knockback to nearby enemies.");
						s.sendMessage(ChatColor.GOLD + "FireBlast > FireBlast > FireShield > (Tap Shift). ");
						s.sendMessage(Methods.getFireColor() + "FireWheel" + ChatColor.WHITE + ": A high-speed wheel of fire that travels along the ground for long distances dealing high damage.");
						s.sendMessage(ChatColor.GOLD + "FireShield (Hold Shift) > Right Click a block in front of you twice > Switch to Blaze > Release Shift. ");
						s.sendMessage(Methods.getFireColor() + "JetBlast" + ChatColor.WHITE + ": Create an explosive blast that propels your FireJet at higher speeds.");
						s.sendMessage(ChatColor.GOLD + "FireJet (Tap Shift) > FireJet (Tap Shift) > FireShield (Tap Shift) > FireJet. ");
						s.sendMessage(Methods.getFireColor() + "JetBlaze" + ChatColor.WHITE + ": Damages and burns all enemies in the proximity of your FireJet.");
						s.sendMessage(ChatColor.GOLD + "FireJet (Tap Shift) > FireJet (Tap Shift) > Blaze (Tap Shift) > FireJet. ");
					}
					if (args[1].equalsIgnoreCase("AirCombo")) {
						s.sendMessage(ChatColor.GOLD + "AirCombo:");
						s.sendMessage(Methods.getAirColor() + "Twister" + ChatColor.WHITE + ": Create a cyclone of air that travels along the ground grabbing nearby entities.");
						s.sendMessage(ChatColor.GOLD + "AirShield (Tap Shift) > Tornado (Hold Shift) > AirBlast (Left Click)");
						s.sendMessage(Methods.getAirColor() + "AirStream" + ChatColor.WHITE + ": Control a large stream of air that grabs onto enemies allowing you to direct them temporarily.");
						s.sendMessage(ChatColor.GOLD + "AirShield (Hold Shift) > AirSuction (Left Click) > AirBlast (Left Click)");
						s.sendMessage(Methods.getAirColor() + "AirSweep" + ChatColor.WHITE + ": Sweep the air in front of you hitting multiple enemies, causing moderate damage and a large knockback. The radius and direction of AirSweep is controlled by moving your mouse in a sweeping motion. For example, if you want to AirSweep upward, then move your mouse upward right after you left click AirBurst");
						s.sendMessage(ChatColor.GOLD + "AirSwipe (Left Click) > AirSwipe (Left Click) > AirBurst (Hold Shift) > AirBurst (Left Click)");
					}
					if (args[1].equalsIgnoreCase("WaterCombo")) {
						s.sendMessage(ChatColor.GOLD + "WaterCombos:");
						s.sendMessage(Methods.getAirColor() + "IceWave" + ChatColor.WHITE + ": PhaseChange your WaterWave into an IceWave that freezes and damages enemies.");
						s.sendMessage(ChatColor.GOLD + "Create a WaterSpout Wave > PhaseChange (Left Click)");
						s.sendMessage(Methods.getAirColor() + "IceBullet" + ChatColor.WHITE + ": Using a large cavern of ice, you can punch ice shards at your opponent causing moderate damage. To rapid fire, you must alternate between Left clicking and right clicking with IceBlast.");
						s.sendMessage(ChatColor.GOLD + "WaterBubble (Tap Shift) > IceBlast (Hold Shift) > IceBlast (Left Click) > Wait for ice to Form > Then alternate between Left and Right click with IceBlast");
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

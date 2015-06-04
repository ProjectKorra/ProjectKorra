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
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.projectkorra.ProjectKorra.Ability.AbilityModuleManager;
import com.projectkorra.ProjectKorra.Ability.StockAbilities;
import com.projectkorra.ProjectKorra.Ability.Combo.ComboAbilityModule;
import com.projectkorra.ProjectKorra.Ability.Combo.ComboModuleManager;
import com.projectkorra.ProjectKorra.CustomEvents.PlayerChangeElementEvent;
import com.projectkorra.ProjectKorra.CustomEvents.PlayerChangeElementEvent.Result;
import com.projectkorra.ProjectKorra.Objects.Preset;
import com.projectkorra.ProjectKorra.Utilities.GrapplingHookAPI;
import com.projectkorra.ProjectKorra.airbending.AirMethods;
import com.projectkorra.ProjectKorra.chiblocking.ChiMethods;
import com.projectkorra.ProjectKorra.earthbending.EarthMethods;
import com.projectkorra.ProjectKorra.firebending.FireMethods;
import com.projectkorra.ProjectKorra.waterbending.WaterMethods;
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
	 * Subelement Aliases
	 */

	//Air
	String[] flightaliases = {"flight", "fl"};
	String[] spiritualprojectionaliases = {"spiritualprojection", "sp", "spiritual"};

	//Water
	String[] bloodaliases = {"bloodbending", "bb"};
	String[] healingaliases = {"healing", "heal"};
	String[] icealiases = {"icebending", "ice", "ib"};
	String[] plantaliases = {"plantbending", "plant"};

	//Earth
	String[] metalbendingaliases = {"metalbending", "mb", "metal"};
	String[] lavabendingaliases = {"lavabending", "lb", "lava"};
	String[] sandbendingaliases = {"sandbending", "sb", "sand"};

	//Firebending
	String[] combustionaliases = {"combustionbending", "combustion", "cb"};
	String[] lightningaliases = {"lightningbending", "lightning"};


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
	String[] checkaliases = {"check", "chk"};

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
			@SuppressWarnings("deprecation")
			@Override
			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
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
					if (!GeneralMethods.hasRPG()) {
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
						s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
						return true;
					}

					GeneralMethods.runDebug();
					s.sendMessage(ChatColor.GREEN + "Debug File Created as debug.txt in the ProjectKorra plugin folder.");
					s.sendMessage(ChatColor.GREEN + "Put contents on pastie.org and create a bug report  on the ProjectKorra forum if you need to.");
					return true;
				}
				if (Arrays.asList(presetaliases).contains(args[0].toLowerCase())) {
					if (!(s instanceof Player)) {
						s.sendMessage(ChatColor.RED + "This command is only usable by players.");
						return true;
					}
					
					if (MultiAbilityManager.hasMultiAbilityBound((Player) s)) {
						s.sendMessage(ChatColor.RED + "You can't edit your binds right now!");
						return true;
					}

					Player player = (Player) s;

					String[] deletealiases = {"delete", "d", "del"};
					String[] createaliases = {"create", "c", "save"};
					String[] listaliases = {"list", "l"};
					if (args.length == 2 && Arrays.asList(listaliases).contains(args[1].toLowerCase())) {
						if (!s.hasPermission("bending.command.preset.list")) {
							s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
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
							s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
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
							s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
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
							s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
							return true;
						}

						int limit = GeneralMethods.getMaxPresets(player);

						if (Preset.presets.get(player) != null && Preset.presets.get(player).size() >= limit) {
							s.sendMessage(ChatColor.RED + "You have reached your max number of Presets.");
							return true;
						}

						if (Preset.presetExists(player, name)) {
							s.sendMessage(ChatColor.RED + "A preset with that name already exists.");
							return true;
						}

						BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());

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
						s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
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
						s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
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
						s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
						return true;
					}

					GeneralMethods.reloadPlugin();
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
					
					if (MultiAbilityManager.hasMultiAbilityBound((Player) s)) {
						s.sendMessage(ChatColor.RED + "You can't edit your binds right now!");
						return true;
					}
					
					BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(s.getName());
					if (args.length == 1) {
						bPlayer.getAbilities().clear();
						for (int i = 1; i <= 9; i++) {
							GeneralMethods.saveAbility(bPlayer, i, null);
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
								GeneralMethods.saveAbility(bPlayer, slot, null);
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
						if (!GeneralMethods.abilityExists(abil)) {
							s.sendMessage(ChatColor.RED + "That is not an ability.");
							return true;
						}

						String ability = GeneralMethods.getAbility(abil);

						if (!GeneralMethods.canBind(((Player) s).getName(), ability)) {
							s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
							return true;

						}
						if (AirMethods.isAirAbility(ability) && !GeneralMethods.isBender(s.getName(), Element.Air)) {
							s.sendMessage(AirMethods.getAirColor() + "You must be an Airbender to bind this ability.");
							return true;
						}
						if (WaterMethods.isWaterAbility(ability) && !GeneralMethods.isBender(s.getName(), Element.Water)) {
							s.sendMessage(WaterMethods.getWaterColor() + "You must be a Waterbender to bind this ability.");
							return true;
						}
						if (EarthMethods.isEarthAbility(ability) && !GeneralMethods.isBender(s.getName(), Element.Earth)) {
							s.sendMessage(EarthMethods.getEarthColor() + "You must be an Earthbender to bind this ability.");
							return true;
						}
						if (FireMethods.isFireAbility(ability) && !GeneralMethods.isBender(s.getName(), Element.Fire)) {
							s.sendMessage(FireMethods.getFireColor() + "You must be a Firebender to bind this ability.");
							return true;
						}
						if (ChiMethods.isChiAbility(ability) && !GeneralMethods.isBender(s.getName(), Element.Chi)) {
							s.sendMessage(ChiMethods.getChiColor() + "You must be a ChiBlocker to bind this ability.");
							return true;
						}

						GeneralMethods.bindAbility((Player) s, ability);
						//						s.sendMessage("Ability Bound to slot");
						return true;
					}

					if (args.length == 3) {
						// bending bind ability [Slot]
						String abil = args[1];
						if (!GeneralMethods.abilityExists(abil)) {
							s.sendMessage(ChatColor.RED + "That ability doesn't exist.");
							return true;
						}
						String ability = GeneralMethods.getAbility(abil);
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

						if (!GeneralMethods.canBind(((Player) s).getName(), ability)) {
							s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
							return true;
						}

						if (AirMethods.isAirAbility(ability) && !GeneralMethods.isBender(s.getName(), Element.Air)) {
							s.sendMessage(AirMethods.getAirColor() + "You must be an Airbender to bind this ability.");
							return true;
						}
						if (WaterMethods.isWaterAbility(ability) && !GeneralMethods.isBender(s.getName(), Element.Water)) {
							s.sendMessage(WaterMethods.getWaterColor() + "You must be a Waterbender to bind this ability.");
							return true;
						}
						if (EarthMethods.isEarthAbility(ability) && !GeneralMethods.isBender(s.getName(), Element.Earth)) {
							s.sendMessage(EarthMethods.getEarthColor() + "You must be an Earthbender to bind this ability.");
							return true;
						}
						if (FireMethods.isFireAbility(ability) && !GeneralMethods.isBender(s.getName(), Element.Fire)) {
							s.sendMessage(FireMethods.getFireColor() + "You must be a Firebender to bind this ability.");
							return true;
						}
						if (ChiMethods.isChiAbility(ability) && !GeneralMethods.isBender(s.getName(), Element.Chi)) {
							s.sendMessage(ChiMethods.getChiColor() + "You must be a ChiBlocker to bind this ability.");
							return true;
						}
						GeneralMethods.bindAbility((Player) s, ability, slot);
						//						s.sendMessage("Ability Bound");
						return true;
					}
				}
				if (Arrays.asList(importaliases).contains(args[0].toLowerCase())) {
					if (!s.hasPermission("bending.command.import")) {
						s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
						return true;
					}
					if (!GeneralMethods.isImportEnabled()) {
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
										GeneralMethods.createBendingPlayer(player.getUniqueId(), player.getName());
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
									GeneralMethods.logError(ex);
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
						if (!(s instanceof Player)) {
							s.sendMessage(ChatColor.RED + "This command is only usable by players.");
							return true;
						}
						//bending display [Element]
						if (Arrays.asList(airaliases).contains(args[1].toLowerCase())) {
							if (AbilityModuleManager.airbendingabilities.isEmpty()) {
								s.sendMessage(AirMethods.getAirColor() + "There are no airbending abilities available.");
								return true;
							}
							for (String st: AbilityModuleManager.airbendingabilities) {
								if (GeneralMethods.isSubAbility(st)) continue;
								if (GeneralMethods.canView((Player) s, st)) {
									s.sendMessage(AirMethods.getAirColor() + st);
								}
							}
							if (!AbilityModuleManager.flightabilities.isEmpty()) {
								s.sendMessage(GeneralMethods.getSubBendingColor(Element.Air) + "Use /bending display Flight for all Flight subelement abilities.");
							}
							if (!AbilityModuleManager.spiritualprojectionabilities.isEmpty()) {
								s.sendMessage(GeneralMethods.getSubBendingColor(Element.Air) + "Use /bending display SpiritualProjection for all Spiritual Projection subelement abilities.");
							}
							return true;
						}

						if (Arrays.asList(flightaliases).contains(args[1].toLowerCase())) {
							if (AbilityModuleManager.flightabilities.isEmpty()) {
								s.sendMessage(GeneralMethods.getSubBendingColor(Element.Air) + "There are no flight abilities installed on this server.");
								return true;
							}

							for (String st: AbilityModuleManager.flightabilities) {
								if (GeneralMethods.canView((Player) s, st)) {
									s.sendMessage(GeneralMethods.getSubBendingColor(Element.Air) + st);
								}
							}
							return true;
						}

						if (Arrays.asList(spiritualprojectionaliases).contains(args[1].toLowerCase())) {
							if (AbilityModuleManager.spiritualprojectionabilities.isEmpty()) {
								s.sendMessage(GeneralMethods.getSubBendingColor(Element.Air) + "There are no Spiritual Projection abilities installed on this server.");
								return true;
							}

							for (String st: AbilityModuleManager.spiritualprojectionabilities) {
								if (GeneralMethods.canView((Player) s, st)) {
									s.sendMessage(GeneralMethods.getSubBendingColor(Element.Air) + st);
								}
							}
							return true;
						}


						if (Arrays.asList(wateraliases).contains(args[1].toLowerCase())) {
							if (AbilityModuleManager.waterbendingabilities.isEmpty()) {
								s.sendMessage(WaterMethods.getWaterColor() + "There are no waterbending abilities available.");
								return true;
							}
							for (String st: AbilityModuleManager.waterbendingabilities) {
								if (GeneralMethods.isSubAbility(st)) continue;
								if (GeneralMethods.canView((Player) s, st)) {
									s.sendMessage(WaterMethods.getWaterColor() + st);
								}
							}
							if (!AbilityModuleManager.bloodabilities.isEmpty()) {
								s.sendMessage(GeneralMethods.getSubBendingColor(Element.Water) + "Use /bending display Bloodbending for bloodbending sub abilities.");
							}
							if (!AbilityModuleManager.healingabilities.isEmpty()) {
								s.sendMessage(GeneralMethods.getSubBendingColor(Element.Water) + "Use /bending display Healing for Healing sub abilities.");
							}
							if (!AbilityModuleManager.iceabilities.isEmpty()) {
								s.sendMessage(GeneralMethods.getSubBendingColor(Element.Water) + "Use /bending display Icebending for Ice sub abilities.");
							}
							if (!AbilityModuleManager.plantabilities.isEmpty()) {
								s.sendMessage(GeneralMethods.getSubBendingColor(Element.Water) + "Use /bending display Plant for Plantbending sub abilities.");
							}
							return true;
						}

						if (Arrays.asList(bloodaliases).contains(args[1].toLowerCase())) {
							if (AbilityModuleManager.bloodabilities.isEmpty()) {
								s.sendMessage(GeneralMethods.getSubBendingColor(Element.Water) + "There are no Bloodbending abilities installed on this server.");
								return true;
							}

							for (String st: AbilityModuleManager.bloodabilities) {
								if (GeneralMethods.canView((Player) s, st)) {
									s.sendMessage(GeneralMethods.getSubBendingColor(Element.Water) + st);
								}
							}
							return true;
						}

						if (Arrays.asList(healingaliases).contains(args[1].toLowerCase())) {
							if (AbilityModuleManager.healingabilities.isEmpty()) {
								s.sendMessage(GeneralMethods.getSubBendingColor(Element.Water) + "There are no Healing abilities installed on this server.");
								return true;
							}

							for (String st: AbilityModuleManager.healingabilities) {
								if (GeneralMethods.canView((Player) s, st)) {
									s.sendMessage(GeneralMethods.getSubBendingColor(Element.Water) + st);
								}
							}
							return true;
						}
						
						if (Arrays.asList(icealiases).contains(args[1].toLowerCase())) {
							if (AbilityModuleManager.iceabilities.isEmpty()) {
								s.sendMessage(GeneralMethods.getSubBendingColor(Element.Water) + "There are no Icebending abilities installed on this server.");
								return true;
							}
							
							for (String st: AbilityModuleManager.iceabilities) {
								if (GeneralMethods.canView((Player) s, st)) {
									s.sendMessage(GeneralMethods.getSubBendingColor(Element.Water) + st);
								}
							}
							
							return true;
						}
						

						if (Arrays.asList(plantaliases).contains(args[1].toLowerCase())) {
							if (AbilityModuleManager.plantabilities.isEmpty()) {
								s.sendMessage(GeneralMethods.getSubBendingColor(Element.Water) + "There are no Plantbending abilities installed on this server.");
								return true;
							}

							for (String st: AbilityModuleManager.plantabilities) {
								if (GeneralMethods.canView((Player) s, st)) {
									s.sendMessage(GeneralMethods.getSubBendingColor(Element.Water) + st);
								}
							}
							return true;
						}
						if (Arrays.asList(earthaliases).contains(args[1].toLowerCase())) {
							if (AbilityModuleManager.earthbendingabilities.isEmpty()) {
								s.sendMessage(EarthMethods.getEarthColor() + "There are no earthbending abilities available.");
								return true;
							}
							for (String st: AbilityModuleManager.earthbendingabilities) {
								if (GeneralMethods.isSubAbility(st)) continue;
								if (GeneralMethods.canView((Player) s, st)) {
									s.sendMessage(EarthMethods.getEarthColor() + st);
								}
							}
							if (!AbilityModuleManager.lavaabilities.isEmpty()) {
								s.sendMessage(GeneralMethods.getSubBendingColor(Element.Earth) + "Use /bending display LavaBending for Lava sub abilities.");
							}
							if (!AbilityModuleManager.metalabilities.isEmpty()) {
								s.sendMessage(GeneralMethods.getSubBendingColor(Element.Earth) + "Use /bending display Metalbending for Metal sub abilities.");
							}

							if (!AbilityModuleManager.sandabilities.isEmpty()) {
								s.sendMessage(GeneralMethods.getSubBendingColor(Element.Earth) + "Use /bending display Sand for Sandbending sub abilities.");
							}
							return true;
						}

						if (Arrays.asList(lavabendingaliases).contains(args[1].toLowerCase())) {
							if (AbilityModuleManager.lavaabilities.isEmpty()) {
								s.sendMessage(GeneralMethods.getSubBendingColor(Element.Earth) + "There are no lavabending abilities installed on this server.");
								return true;
							}

							for (String st: AbilityModuleManager.lavaabilities) {
								if (GeneralMethods.canView((Player) s, st)) {
									s.sendMessage(GeneralMethods.getSubBendingColor(Element.Earth) + st);
								}
							}
							return true;
						}

						if (Arrays.asList(metalbendingaliases).contains(args[1].toLowerCase())) {
							if (AbilityModuleManager.metalabilities.isEmpty()) {
								s.sendMessage(GeneralMethods.getSubBendingColor(Element.Earth) + "There are no metal abilities installed on this server.");
								return true;
							}

							for (String st: AbilityModuleManager.metalabilities) {
								if (GeneralMethods.canView((Player) s, st)) {
									s.sendMessage(GeneralMethods.getSubBendingColor(Element.Earth) + st);
								}
							}
							return true;
						}

						if (Arrays.asList(sandbendingaliases).contains(args[1].toLowerCase())) {
							if (AbilityModuleManager.sandabilities.isEmpty()) {
								s.sendMessage(GeneralMethods.getSubBendingColor(Element.Earth) + "There are no sandbending abilities installed on this server.");
								return true;
							}

							for (String st: AbilityModuleManager.sandabilities) {
								if (GeneralMethods.canView((Player) s, st)) {
									s.sendMessage(GeneralMethods.getSubBendingColor(Element.Earth) + st);
								}
							}
							return true;
						}

						if (Arrays.asList(firealiases).contains(args[1].toLowerCase())) {
							if (AbilityModuleManager.firebendingabilities.isEmpty()) {
								s.sendMessage(FireMethods.getFireColor() + "There are no firebending abilities available.");
								return true;
							}
							for (String st: AbilityModuleManager.firebendingabilities) {
								if (GeneralMethods.isSubAbility(st)) continue;
								if (GeneralMethods.canView((Player) s, st)) {
									s.sendMessage(FireMethods.getFireColor() + st);
								}
							}
							if (!AbilityModuleManager.combustionabilities.isEmpty()) {
								s.sendMessage(GeneralMethods.getSubBendingColor(Element.Fire) + "Use /bending display Combustion for Combustion sub abilities.");
							}
							if (!AbilityModuleManager.lightningabilities.isEmpty()) {
								s.sendMessage(GeneralMethods.getSubBendingColor(Element.Fire) + "Use /bending display Lightning for Lightning sub abilities.");
							}
							return true;
						}
						
						if (Arrays.asList(lightningaliases).contains(args[1].toLowerCase())) {
							if (AbilityModuleManager.lightningabilities.isEmpty()) {
								s.sendMessage(GeneralMethods.getSubBendingColor(Element.Fire) + "There are no lightning abilities installed on this server.");
								return true;
							}
							
							for (String st: AbilityModuleManager.lightningabilities) {
								if (GeneralMethods.canView((Player) s, st)) {
									s.sendMessage(GeneralMethods.getSubBendingColor(Element.Fire) + st);
								}
							}
						}
						
						if (Arrays.asList(combustionaliases).contains(args[1].toLowerCase())) {
							if (AbilityModuleManager.combustionabilities.isEmpty()) {
								s.sendMessage(GeneralMethods.getSubBendingColor(Element.Fire) + "There are no combustion abilities installed on this server.");
								return true;
							}
							
							for (String st: AbilityModuleManager.combustionabilities) {
								if (GeneralMethods.canView((Player) s, st)) {
									s.sendMessage(GeneralMethods.getSubBendingColor(Element.Fire) + st);
								}
							}
						}
						
						if (Arrays.asList(chialiases).contains(args[1].toLowerCase())) {
							if (AbilityModuleManager.chiabilities.isEmpty()) {
								s.sendMessage(ChiMethods.getChiColor() + "There are no chiblocking abilities available.");
								return true;
							}

							for (String st: AbilityModuleManager.chiabilities) {
								if (GeneralMethods.canView((Player) s, st)) {
									s.sendMessage(ChiMethods.getChiColor()  + st);
								}
							}
							return true;
						} else {
							s.sendMessage(ChatColor.RED + "Not a valid Element." + ChatColor.WHITE + " Elements: " + 
									AirMethods.getAirColor() + "Air" + 
									ChatColor.WHITE + " | " +
									WaterMethods.getWaterColor() + "Water" +
									ChatColor.WHITE + " | " +
									EarthMethods.getEarthColor() + "Earth" + 
									ChatColor.WHITE + " | " +
									FireMethods.getFireColor() + "Fire" +
									ChatColor.WHITE + " | " +
									ChiMethods.getChiColor() + "Chi");

						}
					}
					if (args.length == 1) {
						//bending display
						if (!(s instanceof Player)) {
							s.sendMessage(ChatColor.RED + "This command is only usable by players.");
							return true;
						}
						BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(s.getName());
						HashMap<Integer, String> abilities = bPlayer.getAbilities();

						if (abilities.isEmpty()) {
							s.sendMessage("You don't have any bound abilities.");
							s.sendMessage("If you would like to see a list of available abilities, please use the /bending display [Element] command. Use /bending help for more information.");
							return true;
						}

						for (int i = 1; i <= 9; i++) {
							String ability = abilities.get(i);
							if (ability != null) s.sendMessage(i + " - " + GeneralMethods.getAbilityColor(ability) + ability);
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

						BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(s.getName());

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
						s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
						return true;
					}

					if (args.length == 2) {

						Player p = Bukkit.getPlayer(args[1]);
						if (p == null) {
							s.sendMessage(ChatColor.GREEN + "You are running a lookup of an offline player, this may take a second.");

							final String player = args[1];
							final CommandSender sender = s;
							new BukkitRunnable() {
								@Override
								public void run() {
									ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM pk_players WHERE player = '" + player + "'");
									try {
										final List<String> messages = new ArrayList<String>();

										if (rs2.next()) {
											UUID uuid = UUID.fromString(rs2.getString("uuid"));
											String element = rs2.getString("element");

											messages.add(player + " - ");
											if (element.contains("a")) messages.add(AirMethods.getAirColor() + "- Airbender");
											if (element.contains("w")) messages.add(WaterMethods.getWaterColor() + "- Waterbender");
											if (element.contains("e")) messages.add(EarthMethods.getEarthColor() + "- Earthbender");
											if (element.contains("f")) messages.add(FireMethods.getFireColor() + "- Firebender");
											if (element.contains("c")) messages.add(ChiMethods.getChiColor() + "- Chiblocker");

											if (GeneralMethods.hasRPG()) {
												if (RPGMethods.isCurrentAvatar(uuid)) {
													messages.add(GeneralMethods.getAvatarColor() + "Current Avatar");
												} else if (RPGMethods.hasBeenAvatar(uuid)) {
													messages.add(GeneralMethods.getAvatarColor() + "Former Avatar");
												} else {

												}
											}
										} else {
											messages.add(ChatColor.RED + "We could not find any player in your database with that username. Are you sure it is typed correctly?");
										}

										new BukkitRunnable() {
											@Override
											public void run() {
												for (String message : messages) {
													sender.sendMessage(message);
												}
											}
										}.runTask(ProjectKorra.plugin);
									} catch (SQLException e) {
										e.printStackTrace();
										GeneralMethods.logError(e);
									}
								}
							}.runTaskAsynchronously(ProjectKorra.plugin);
							return true;
						}

						String un = p.getName();
						s.sendMessage(un + " - ");
						if (GeneralMethods.isBender(un, Element.Air)) {
							s.sendMessage(AirMethods.getAirColor() + "- Airbender");
							if(AirMethods.canAirFlight(p)) {
								s.sendMessage(GeneralMethods.getSubBendingColor(Element.Air) + "    Can Fly");
							}
							if(AirMethods.canUseSpiritualProjection(p)) {
								s.sendMessage(GeneralMethods.getSubBendingColor(Element.Air) + "    Can use Spiritual Projection");
							}
						}
						if (GeneralMethods.isBender(un, Element.Water)) {
							s.sendMessage(WaterMethods.getWaterColor() + "- Waterbender");
							if (WaterMethods.canPlantbend(p)) {
								s.sendMessage(GeneralMethods.getSubBendingColor(Element.Water) + "    Can Plantbend");
							}
							if (WaterMethods.canBloodbend(p)) {
								if(WaterMethods.canBloodbendAtAnytime(p))
									s.sendMessage(GeneralMethods.getSubBendingColor(Element.Water) + "    Can Bloodbend anytime, on any day");
								else
									s.sendMessage(GeneralMethods.getSubBendingColor(Element.Water) + "    Can Bloodbend");
							}
							if (WaterMethods.canIcebend(p)) {
								s.sendMessage(GeneralMethods.getSubBendingColor(Element.Water) + "    Can Icebend");
							}
							if (WaterMethods.canWaterHeal(p)) {
								s.sendMessage(GeneralMethods.getSubBendingColor(Element.Water) + "    Can Heal");
							}
						}
						if (GeneralMethods.isBender(un, Element.Earth)) {
							s.sendMessage(EarthMethods.getEarthColor() + "- Earthbender");
							if (EarthMethods.canMetalbend(p)) {
								s.sendMessage(GeneralMethods.getSubBendingColor(Element.Earth) + "    Can Metalbend");
							}
							if (EarthMethods.canLavabend(p)) {
								s.sendMessage(GeneralMethods.getSubBendingColor(Element.Earth) + "    Can Lavabend");
							}
							if (EarthMethods.canSandbend(p)) {
								s.sendMessage(GeneralMethods.getSubBendingColor(Element.Earth) + "    Can Sandbend");
							}
						}
						if (GeneralMethods.isBender(un, Element.Fire)) {
							s.sendMessage(FireMethods.getFireColor() + "- Firebender");
							if(FireMethods.canCombustionbend(p)) {
								s.sendMessage(GeneralMethods.getSubBendingColor(Element.Fire) + "    Can Combustionbend");
							}
							if(FireMethods.canLightningbend(p)) {
								s.sendMessage(GeneralMethods.getSubBendingColor(Element.Fire) + "    Can Bend Lightning");
							}
						}
						if (GeneralMethods.isBender(un, Element.Chi)) {
							s.sendMessage(ChiMethods.getChiColor() + "- ChiBlocker");
						}
						BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(un);
						UUID uuid2 = bPlayer.uuid;
						if (bPlayer != null)  {
							s.sendMessage("Abilities: ");
							for (int i = 1; i <= 9; i++) {
								String ability = bPlayer.getAbilities().get(i);
								if (ability == null || ability.equalsIgnoreCase("null")) {
									continue;
								} else {
									s.sendMessage(i + " - " + GeneralMethods.getAbilityColor(ability) + ability);
								}
							}
						}

						if (GeneralMethods.hasRPG()) {
							if (RPGMethods.isCurrentAvatar(p.getUniqueId())) {
								s.sendMessage(GeneralMethods.getAvatarColor() + "Current Avatar");
							} else if (RPGMethods.hasBeenAvatar(p.getUniqueId())) {
								s.sendMessage(GeneralMethods.getAvatarColor() + "Former Avatar");
							}
						}

						if (uuid2.toString().equals("8621211e-283b-46f5-87bc-95a66d68880e")) {
							s.sendMessage(ChatColor.YELLOW + "ProjectKorra Founder"); // MistPhizzle
						}

						if (uuid2.toString().equals("9636d66a-bff8-48e4-993e-68f0e7891c3b") // codiaz
								|| uuid2.toString().equals("833a7132-a9ec-4f0a-ad9c-c3d6b8a1c7eb") // Jacklin213
								|| uuid2.toString().equals("96f40c81-dd5d-46b6-9afe-365114d4a082") // Coolade
								|| uuid2.toString().equals("81adae76-d647-4b41-bfb0-8166516fa189") // AlexTheCoder
								|| uuid2.toString().equals("c364ffe2-de9e-4117-9735-6d14bde038f6") // Carbogen
								|| uuid2.toString().equals("a197291a-cd78-43bb-aa38-52b7c82bc68c")) // OmniCypher
							s.sendMessage(ChatColor.YELLOW + "ProjectKorra Developer");

						if (uuid2.toString().equals("929b14fc-aaf1-4f0f-84c2-f20c55493f53")) { // vidcom
							s.sendMessage(ChatColor.YELLOW + "ProjectKorra Concept Designer");
							s.sendMessage(ChatColor.YELLOW + "ProjectKorra Community Moderator");
						}

						if (uuid2.toString().equals("9c18ff57-04b3-4841-9726-9d64373d0d65")) { // coastyo
							s.sendMessage(ChatColor.YELLOW + "ProjectKorra Concept Designer");
							s.sendMessage(ChatColor.YELLOW + "ProjectKorra Graphic Artist");
						}

						if (uuid2.toString().equals("b2d82a84-ce22-4518-a8fc-1b28aeda0c0b") // Shunky
								|| uuid2.toString().equals("15d1a5a7-76ef-49c3-b193-039b27c47e30")) { // Kiam
							s.sendMessage(ChatColor.YELLOW + "ProjectKorra Concept Designer");
						}

						if (uuid2.toString().equals("0fd77ff6-07fb-4a7d-ba87-ae6f802ed1f9")) { // Hit_Manx
							s.sendMessage(ChatColor.YELLOW + "ProjectKorra Concept Designer");
							s.sendMessage(ChatColor.YELLOW + "ProjectKorra Wiki Contributor");
						}
						return true;
					}
					if (args.length == 1) {
						List<String> players = new ArrayList<String>();
						for (Player player: Bukkit.getOnlinePlayers()) {
							String un = player.getName();

							BendingPlayer bp = GeneralMethods.getBendingPlayer(un);
							if (bp.elements.size() > 1) {
								players.add(GeneralMethods.getAvatarColor() + un);
								continue;
							}
							if (bp.elements.size() == 0) {
								players.add(un);
								continue;
							}
							if (GeneralMethods.isBender(un, Element.Air)) {
								players.add(AirMethods.getAirColor() + un);
								continue;
							}
							if (GeneralMethods.isBender(un, Element.Water)){
								players.add(WaterMethods.getWaterColor() + un);
								continue;
							}
							if (GeneralMethods.isBender(un, Element.Earth)) {
								players.add(EarthMethods.getEarthColor() + un);
								continue;
							}
							if (GeneralMethods.isBender(un, Element.Chi)) {
								players.add(ChiMethods.getChiColor() + un);
								continue;
							}
							if (GeneralMethods.isBender(un, Element.Fire)) {
								players.add(FireMethods.getFireColor() + un);
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
					s.sendMessage(ChatColor.GREEN + "Core Version: " + ChatColor.RED + plugin.getDescription().getVersion());
					if (GeneralMethods.hasRPG()) {
						s.sendMessage(ChatColor.GREEN + "RPG Version: " + ChatColor.RED + GeneralMethods.getRPG().getDescription().getVersion());
					}
					if (GeneralMethods.hasItems()) {
						s.sendMessage(ChatColor.GREEN + "Items Version: " + ChatColor.RED + GeneralMethods.getItems().getDescription().getVersion());
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
						s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
						return true;
					}

					Player player = Bukkit.getPlayer(args[1]);

					if (player == null) {
						s.sendMessage(ChatColor.RED + "That player is not online.");
						return true;
					}

					BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
					GeneralMethods.removeUnusableAbilities(player.getName());
					bPlayer.elements.clear();
					GeneralMethods.saveElements(bPlayer);
					s.sendMessage(ChatColor.GREEN + "You have removed the bending of " + ChatColor.DARK_AQUA + player.getName());
					player.sendMessage(ChatColor.GREEN + "Your bending has been removed by " + ChatColor.DARK_AQUA + s.getName());
					Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(s, player, null, Result.REMOVE));
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

					BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());

					if (bPlayer.isPermaRemoved()) {
						bPlayer.permaRemoved = false;
						GeneralMethods.savePermaRemoved(bPlayer);
						s.sendMessage(ChatColor.RED + "You have restored the bending of: " + ChatColor.DARK_AQUA + player.getName());
						return true;
					}

					bPlayer.elements.clear();
					GeneralMethods.removeUnusableAbilities(player.getName());
					GeneralMethods.saveElements(bPlayer);
					bPlayer.permaRemoved = true;
					GeneralMethods.savePermaRemoved(bPlayer);
					player.sendMessage(ChatColor.RED + "Your bending has been permanently removed.");
					s.sendMessage(ChatColor.RED + "You have permanently removed the bending of: " + ChatColor.DARK_AQUA + player.getName());
					Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(s, player, null, Result.PERMAREMOVE));
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

						BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
						if (Arrays.asList(airaliases).contains(args[2].toLowerCase())) {
							bPlayer.addElement(Element.Air);
							GeneralMethods.saveElements(bPlayer);
							player.sendMessage(AirMethods.getAirColor() + "You are also an Airbender.");
							s.sendMessage(ChatColor.DARK_AQUA + player.getName() + AirMethods.getAirColor() + " is also an Airbender.");
							Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(s, player, Element.Air, Result.ADD));
							return true;
						}

						if (Arrays.asList(wateraliases).contains(args[2].toLowerCase())) {
							bPlayer.addElement(Element.Water);
							GeneralMethods.saveElements(bPlayer);
							player.sendMessage(WaterMethods.getWaterColor() + "You are also a waterbender.");
							s.sendMessage(ChatColor.DARK_AQUA + player.getName() + WaterMethods.getWaterColor() + " is also a Waterbender.");
							Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(s, player, Element.Water, Result.ADD));
							return true;
						}

						if (Arrays.asList(earthaliases).contains(args[2].toLowerCase())) {
							bPlayer.addElement(Element.Earth);
							GeneralMethods.saveElements(bPlayer);
							player.sendMessage(EarthMethods.getEarthColor() + "You are also an Earthbender.");
							s.sendMessage(ChatColor.DARK_AQUA + player.getName() + EarthMethods.getEarthColor() + " is also an Earthbender.");
							Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(s, player, Element.Earth, Result.ADD));

							return true;
						}

						if (Arrays.asList(firealiases).contains(args[2].toLowerCase())) {
							bPlayer.addElement(Element.Fire);
							GeneralMethods.saveElements(bPlayer);
							player.sendMessage(FireMethods.getFireColor() + "You are also a Firebender.");
							s.sendMessage(ChatColor.DARK_AQUA + player.getName() + FireMethods.getFireColor() + " is also a Firebender");
							Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(s, player, Element.Fire, Result.ADD));
							return true;
						}
						if (Arrays.asList(chialiases).contains(args[2].toLowerCase())) {
							bPlayer.addElement(Element.Chi);
							GeneralMethods.saveElements(bPlayer);
							player.sendMessage(ChiMethods.getChiColor() + "You are also a ChiBlocker.");
							s.sendMessage(ChatColor.DARK_AQUA + player.getName() + ChiMethods.getChiColor() + " is also a ChiBlocker");
							Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(s, player, Element.Chi, Result.ADD));
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

						BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(s.getName());

						if (Arrays.asList(airaliases).contains(args[1].toLowerCase())) {
							if (!s.hasPermission("bending.command.add.air")) {
								s.sendMessage(ChatColor.RED + "You don't have permsision to add " + AirMethods.getAirColor() + "Airbending");
								return true;
							}
							bPlayer.addElement(Element.Air);
							GeneralMethods.saveElements(bPlayer);
							s.sendMessage(AirMethods.getAirColor() + "You are also an Airbender.");
							Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(s, (Player) s, Element.Air, Result.ADD));
							return true;
						}

						if (Arrays.asList(wateraliases).contains(args[1].toLowerCase())) {
							if (!s.hasPermission("bending.command.add.water")) {
								s.sendMessage(ChatColor.RED + "You don't have permission to add " + WaterMethods.getWaterColor() + "Waterbending");
								return true;
							}
							bPlayer.addElement(Element.Water);
							GeneralMethods.saveElements(bPlayer);
							s.sendMessage(WaterMethods.getWaterColor() + "You are also a Waterbender.");
							Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(s, (Player) s, Element.Water, Result.ADD));
							return true;
						}

						if (Arrays.asList(earthaliases).contains(args[1].toLowerCase())) {
							if (!s.hasPermission("bending.command.add.earth")) {
								s.sendMessage(ChatColor.RED + "You dont' have permission to add " + EarthMethods.getEarthColor() + "Earthbending");
								return true;
							}
							bPlayer.addElement(Element.Earth);
							GeneralMethods.saveElements(bPlayer);
							s.sendMessage(EarthMethods.getEarthColor() + "You are also an Earthbender.");
							Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(s, (Player) s, Element.Earth, Result.ADD));
							return true;
						}

						if (Arrays.asList(firealiases).contains(args[1].toLowerCase())) {
							if (!s.hasPermission("bending.command.add.fire")) {
								s.sendMessage(ChatColor.RED + "You don't have permission to add " + FireMethods.getFireColor() + "Firebending");
								return true;
							}
							bPlayer.addElement(Element.Fire);
							GeneralMethods.saveElements(bPlayer);
							s.sendMessage(FireMethods.getFireColor() + "You are also a Firebender.");
							Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(s, (Player) s, Element.Fire, Result.ADD));
							return true;
						}
						if (Arrays.asList(chialiases).contains(args[1].toLowerCase())) {
							if (!s.hasPermission("bending.command.add.chi")) {
								s.sendMessage(ChatColor.RED + "You don't have permission to add " + ChiMethods.getChiColor() + "Chiblocking");
								return true;
							}
							bPlayer.addElement(Element.Chi);
							GeneralMethods.saveElements(bPlayer);
							s.sendMessage(ChiMethods.getChiColor() + "You are also a ChiBlocker.");
							Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(s, (Player) s, Element.Chi, Result.ADD));
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

						BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(s.getName());

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
							if (!s.hasPermission("bending.command.choose.air")) {
								s.sendMessage(ChatColor.RED + "You don't have permission to choose " + AirMethods.getAirColor() + "Airbending");
								return true;
							}
							bPlayer.setElement(Element.Air);
							s.sendMessage(AirMethods.getAirColor() + "You are now an Airbender.");
							GeneralMethods.removeUnusableAbilities(s.getName());
							GeneralMethods.saveElements(bPlayer);
							Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(s, (Player) s, Element.Air, Result.CHOOSE));
							return true;
						}
						if (Arrays.asList(wateraliases).contains(args[1].toLowerCase())) {
							if (!s.hasPermission("bending.command.choose.water")) {
								s.sendMessage(ChatColor.RED + "You don't have permission to choose " + WaterMethods.getWaterColor() + "Waterbending");
								return true;
							}
							bPlayer.setElement(Element.Water);
							s.sendMessage(WaterMethods.getWaterColor() + "You are now a Waterbender.");
							GeneralMethods.removeUnusableAbilities(s.getName());
							GeneralMethods.saveElements(bPlayer);
							Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(s, (Player) s, Element.Water, Result.CHOOSE));
							return true;
						}
						if (Arrays.asList(earthaliases).contains(args[1].toLowerCase())) {
							if (!s.hasPermission("bending.command.choose.earth")) {
								s.sendMessage(ChatColor.RED + "You don't have permission to choose " + EarthMethods.getEarthColor() + "Earthbending");
								return true;
							}
							bPlayer.setElement(Element.Earth);
							s.sendMessage(EarthMethods.getEarthColor() + "You are now an Earthbender.");
							GeneralMethods.removeUnusableAbilities(s.getName());
							GeneralMethods.saveElements(bPlayer);
							Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(s, (Player) s, Element.Earth, Result.CHOOSE));
							return true;
						}
						if (Arrays.asList(firealiases).contains(args[1].toLowerCase())) {
							if (!s.hasPermission("bending.command.choose.fire")) {
								s.sendMessage(ChatColor.RED + "You don't have permission to choose " + FireMethods.getFireColor() + "Firebending");
								return true;
							}
							bPlayer.setElement(Element.Fire);
							s.sendMessage(FireMethods.getFireColor() + "You are now a Firebender.");
							GeneralMethods.removeUnusableAbilities(s.getName());
							GeneralMethods.saveElements(bPlayer);
							Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(s, (Player) s, Element.Fire, Result.CHOOSE));
							return true;
						}
						if (Arrays.asList(chialiases).contains(args[1].toLowerCase())) {
							if (!s.hasPermission("bending.command.choose.chi")) {
								s.sendMessage(ChatColor.RED + "You don't have permission to choose " + ChiMethods.getChiColor() + "Chiblocking");
								return true;
							}
							bPlayer.setElement(Element.Chi);
							s.sendMessage(ChiMethods.getChiColor() + "You are now a ChiBlocker.");
							GeneralMethods.removeUnusableAbilities(s.getName());
							GeneralMethods.saveElements(bPlayer);
							Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(s, (Player) s, Element.Chi, Result.CHOOSE));
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
						BendingPlayer bTarget = GeneralMethods.getBendingPlayer(target.getName());

//						if (bTarget.isPermaRemoved()) {
//							s.sendMessage(ChatColor.RED + "That player's bending was permanently removed.");
//							return true;
//						}
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
							GeneralMethods.removeUnusableAbilities(target.getName());
							GeneralMethods.saveElements(bTarget);
							s.sendMessage(ChatColor.RED + "You have changed " + ChatColor.DARK_AQUA + target.getName() + "'s " + ChatColor.RED + "element to " + ChatColor.DARK_AQUA + e.toString() + ChatColor.RED + ".");
							target.sendMessage(ChatColor.RED + "Your bending has been changed to " + ChatColor.DARK_AQUA + e.toString() + ChatColor.RED + " by " + ChatColor.DARK_AQUA + s.getName());
							Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(s, target, e, Result.CHOOSE));
							return true;
						}
					}
				}
				if (Arrays.asList(helpaliases).contains(args[0].toLowerCase())) {
					if (!s.hasPermission("bending.command.help")) {
						s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
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
						s.sendMessage(AirMethods.getAirColor() + "Air is the element of freedom. Airbenders are natural pacifists and "
								+ "great explorers. There is nothing stopping them from scaling the tallest of mountains and walls easily. They specialize in redirection, "
								+ "from blasting things away with gusts of winds, to forming a shield around them to prevent damage. Easy to get across flat terrains, "
								+ "such as oceans, there is practically no terrain off limits to Airbenders. They lack much raw damage output, but make up for it with "
								+ "with their ridiculous amounts of utility and speed.");
						s.sendMessage(ChatColor.YELLOW + "Learn More: " + ChatColor.DARK_AQUA + "http://tinyurl.com/qffg9m3");
					}
					if (Arrays.asList(wateraliases).contains(args[1].toLowerCase())) {
						s.sendMessage(WaterMethods.getWaterColor() + "Water is the element of change. Waterbending focuses on using your "
								+ "opponents own force against them. Using redirection and various dodging tactics, you can be made "
								+ "practically untouchable by an opponent. Waterbending provides agility, along with strong offensive "
								+ "skills while in or near water.");
						s.sendMessage(ChatColor.YELLOW + "Learn More: " + ChatColor.DARK_AQUA + "http://tinyurl.com/lod3plv");
					}
					if (Arrays.asList(earthaliases).contains(args[1].toLowerCase())) {
						s.sendMessage(EarthMethods.getEarthColor() + "Earth is the element of substance. Earthbenders share many of the " 
								+ "same fundamental techniques as Waterbenders, but their domain is quite different and more readily "
								+ "accessible. Earthbenders dominate the ground and subterranean, having abilities to pull columns "
								+ "of rock straight up from the earth or drill their way through the mountain. They can also launch "
								+ "themselves through the air using pillars of rock, and will not hurt themselves assuming they land "
								+ "on something they can bend. The more skilled Earthbenders can even bend metal.");
						s.sendMessage(ChatColor.YELLOW + "Learn More: " + ChatColor.DARK_AQUA + "http://tinyurl.com/qaudl42");
					}
					if (Arrays.asList(firealiases).contains(args[1].toLowerCase())) {
						s.sendMessage(FireMethods.getFireColor() + "Fire is the element of power. Firebenders focus on destruction and "
								+ "incineration. Their abilities are pretty straight forward: set things on fire. They do have a bit "
								+ "of utility however, being able to make themselves un-ignitable, extinguish large areas, cook food "
								+ "in their hands, extinguish large areas, small bursts of flight, and then comes the abilities to shoot "
								+ "fire from your hands.");
						s.sendMessage(ChatColor.YELLOW + "Firebenders can chain their abilities into combos, type " 
								+ FireMethods.getFireColor() + "/b help FireCombo" + ChatColor.YELLOW + " for more information.");
						s.sendMessage(ChatColor.YELLOW + "Learn More: " + ChatColor.DARK_AQUA + "http://tinyurl.com/k4fkjhb");
					}
					if (Arrays.asList(chialiases).contains(args[1].toLowerCase())) {
						s.sendMessage(ChiMethods.getChiColor() + "Chiblockers focus on bare handed combat, utilizing their agility and "
								+ "speed to stop any bender right in their path. Although they lack the ability to bend any of the "
								+ "other elements, they are great in combat, and a serious threat to any bender. Chiblocking was "
								+ "first shown to be used by Ty Lee in Avatar: The Last Airbender, then later by members of the "
								+ "Equalists in The Legend of Korra.");
						s.sendMessage(ChatColor.YELLOW + "Learn More: " + ChatColor.DARK_AQUA + "http://tinyurl.com/mkp9n6y");
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
						s.sendMessage(FireMethods.getFireColor() + "FireKick" + ChatColor.WHITE + ": A short ranged arc of fire launches from the player's feet dealing moderate damage to enemies.");
						s.sendMessage(ChatColor.GOLD + "FireBlast > FireBlast > (Hold Shift) > FireBlast. ");
						s.sendMessage(FireMethods.getFireColor() + "FireSpin" + ChatColor.WHITE + ": A circular array of fire that causes damage and massive knockback to nearby enemies.");
						s.sendMessage(ChatColor.GOLD + "FireBlast > FireBlast > FireShield > (Tap Shift). ");
						s.sendMessage(FireMethods.getFireColor() + "FireWheel" + ChatColor.WHITE + ": A high-speed wheel of fire that travels along the ground for long distances dealing high damage.");
						s.sendMessage(ChatColor.GOLD + "FireShield (Hold Shift) > Right Click a block in front of you twice > Switch to Blaze > Release Shift. ");
						s.sendMessage(FireMethods.getFireColor() + "JetBlast" + ChatColor.WHITE + ": Create an explosive blast that propels your FireJet at higher speeds.");
						s.sendMessage(ChatColor.GOLD + "FireJet (Tap Shift) > FireJet (Tap Shift) > FireShield (Tap Shift) > FireJet. ");
						s.sendMessage(FireMethods.getFireColor() + "JetBlaze" + ChatColor.WHITE + ": Damages and burns all enemies in the proximity of your FireJet.");
						s.sendMessage(ChatColor.GOLD + "FireJet (Tap Shift) > FireJet (Tap Shift) > Blaze (Tap Shift) > FireJet. ");
						for(ComboAbilityModule cam : ComboModuleManager.combo)
						{
							if(cam.getElement().equals(Element.Fire.toString()))
							{
								ChatColor color = GeneralMethods.getAvatarColor();
								if(cam.getSubElement() == null)
									color = FireMethods.getFireColor();
								else color = GeneralMethods.getSubBendingColor(Element.Fire);
								s.sendMessage(color + cam.getName() + ChatColor.WHITE + ": " + cam.getDescription());
								s.sendMessage(ChatColor.GOLD + cam.getInstructions());
							}
						}
					}
					if (args[1].equalsIgnoreCase("AirCombo")) {
						s.sendMessage(ChatColor.GOLD + "AirCombo:");
						s.sendMessage(AirMethods.getAirColor() + "Twister" + ChatColor.WHITE + ": Create a cyclone of air that travels along the ground grabbing nearby entities.");
						s.sendMessage(ChatColor.GOLD + "AirShield (Tap Shift) > Tornado (Hold Shift) > AirBlast (Left Click)");
						s.sendMessage(AirMethods.getAirColor() + "AirStream" + ChatColor.WHITE + ": Control a large stream of air that grabs onto enemies allowing you to direct them temporarily.");
						s.sendMessage(ChatColor.GOLD + "AirShield (Hold Shift) > AirSuction (Left Click) > AirBlast (Left Click)");
						s.sendMessage(AirMethods.getAirColor() + "AirSweep" + ChatColor.WHITE + ": Sweep the air in front of you hitting multiple enemies, causing moderate damage and a large knockback. The radius and direction of AirSweep is controlled by moving your mouse in a sweeping motion. For example, if you want to AirSweep upward, then move your mouse upward right after you left click AirBurst");
						s.sendMessage(ChatColor.GOLD + "AirSwipe (Left Click) > AirSwipe (Left Click) > AirBurst (Hold Shift) > AirBurst (Left Click)");
						for(ComboAbilityModule cam : ComboModuleManager.combo)
						{
							if(cam.getElement().equals(Element.Air.toString()))
							{
								ChatColor color = GeneralMethods.getAvatarColor();
								if(cam.getSubElement() == null)
									color = AirMethods.getAirColor();
								else color = GeneralMethods.getSubBendingColor(Element.valueOf(cam.getElement()));
								s.sendMessage(color + cam.getName() + ChatColor.WHITE + ": " + cam.getDescription());
								s.sendMessage(ChatColor.GOLD + cam.getInstructions());
							}
						}
					}
					if (args[1].equalsIgnoreCase("WaterCombo")) {
						s.sendMessage(ChatColor.GOLD + "WaterCombos:");
						s.sendMessage(WaterMethods.getWaterColor() + "IceWave" + ChatColor.WHITE + ": PhaseChange your WaterWave into an IceWave that freezes and damages enemies.");
						s.sendMessage(ChatColor.GOLD + "Create a WaterSpout Wave > PhaseChange (Left Click)");
						s.sendMessage(WaterMethods.getWaterColor() + "IceBullet" + ChatColor.WHITE + ": Using a large cavern of ice, you can punch ice shards at your opponent causing moderate damage. To rapid fire, you must alternate between Left clicking and right clicking with IceBlast.");
						s.sendMessage(ChatColor.GOLD + "WaterBubble (Tap Shift) > IceBlast (Hold Shift) > IceBlast (Left Click) > Wait for ice to Form > Then alternate between Left and Right click with IceBlast");
						for(ComboAbilityModule cam : ComboModuleManager.combo)
						{
							if(cam.getElement().equals(Element.Water.toString()))
							{
								ChatColor color = GeneralMethods.getAvatarColor();
								if(cam.getSubElement() == null)
									color = WaterMethods.getWaterColor();
								else color = GeneralMethods.getSubBendingColor(Element.valueOf(cam.getElement()));
								s.sendMessage(color + cam.getName() + ChatColor.WHITE + ": " + cam.getDescription());
								s.sendMessage(ChatColor.GOLD + cam.getInstructions());
							}
						}
					}
					if (args[1].equalsIgnoreCase("EarthCombo")) {
						s.sendMessage(ChatColor.GOLD + "EarthCombos:");
						for(ComboAbilityModule cam : ComboModuleManager.combo)
						{
							if(cam.getElement().equals(Element.Earth.toString()))
							{
								ChatColor color = GeneralMethods.getAvatarColor();
								if(cam.getSubElement() == null)
									color = EarthMethods.getEarthColor();
								else color = GeneralMethods.getSubBendingColor(Element.valueOf(cam.getElement()));
								s.sendMessage(color + cam.getName() + ChatColor.WHITE + ": " + cam.getDescription());
								s.sendMessage(ChatColor.GOLD + cam.getInstructions());
							}
						}
					}
					if (args[1].equalsIgnoreCase("ChiCombo"))
					{
						s.sendMessage(ChatColor.GOLD + "ChiCombos:");
						s.sendMessage(ChiMethods.getChiColor() + "Immobilize" + ChatColor.WHITE + ": Deliver a series of strikes to an enemy to temporarely immobilize them.");
						s.sendMessage(ChatColor.GOLD + "QuickStrike > SwiftKick > QuickStrike > QuickStrike");
						for(ComboAbilityModule cam : ComboModuleManager.combo)
						{
							if(cam.getElement().equals(Element.Chi.toString()))
							{
								ChatColor color = GeneralMethods.getAvatarColor();
								if(cam.getSubElement() == null)
									color = ChiMethods.getChiColor();
								else color = GeneralMethods.getSubBendingColor(Element.valueOf(cam.getElement()));
								s.sendMessage(color + cam.getName() + ChatColor.WHITE + ": " + cam.getDescription());
								s.sendMessage(ChatColor.GOLD + cam.getInstructions());
							}
						}
					}
					if (GeneralMethods.abilityExists(args[1])) {
						String ability = GeneralMethods.getAbility(args[1]);
						if (AirMethods.isAirAbility(ability)) {
							s.sendMessage(AirMethods.getAirColor() + ability + " - ");
							s.sendMessage(AirMethods.getAirColor() + AbilityModuleManager.descriptions.get(ability));
						}
						else if (WaterMethods.isWaterAbility(ability)) {
							s.sendMessage(WaterMethods.getWaterColor() + ability + " - ");
							s.sendMessage(WaterMethods.getWaterColor() + AbilityModuleManager.descriptions.get(ability));
						}
						else if (EarthMethods.isEarthAbility(ability)) {
							if (EarthMethods.isMetalbendingAbility(ability)) {
								s.sendMessage(EarthMethods.getMetalbendingColor() + ability + " - ");
								s.sendMessage(EarthMethods.getMetalbendingColor() + AbilityModuleManager.descriptions.get(ability));
							} else {
								s.sendMessage(EarthMethods.getEarthColor() + ability + " - ");
								s.sendMessage(EarthMethods.getEarthColor() + AbilityModuleManager.descriptions.get(ability));
							}
						}
						else if (FireMethods.isFireAbility(ability)) {
							s.sendMessage(FireMethods.getFireColor() + ability + " - ");
							s.sendMessage(FireMethods.getFireColor() + AbilityModuleManager.descriptions.get(ability));
						}
						else if (ChiMethods.isChiAbility(ability)) {
							s.sendMessage(ChiMethods.getChiColor() + ability + " - ");
							s.sendMessage(ChiMethods.getChiColor() + AbilityModuleManager.descriptions.get(ability));
						}
						else {
							s.sendMessage(GeneralMethods.getAvatarColor() + ability + " - ");
							s.sendMessage(GeneralMethods.getAvatarColor() + AbilityModuleManager.descriptions.get(ability));
						}
					}
				}//available 
				if (Arrays.asList(checkaliases).contains(args[0].toLowerCase())) {
					if (!s.hasPermission("bending.command.avatar")) {
						s.sendMessage(ChatColor.RED + "You don't have permission to do that.");
						return true;
					}
					
					if (s instanceof Player) {
						if (plugin.updater.updateAvalible()) {
							s.sendMessage(ChatColor.GREEN + "There is a new version of " + ChatColor.GOLD + "ProjectKorra" + ChatColor.GREEN + " available!");
							s.sendMessage(ChatColor.YELLOW + "Current version: " + ChatColor.RED + plugin.updater.getCurrentVersion());
							s.sendMessage(ChatColor.YELLOW + "Latest version: " + ChatColor.GOLD + plugin.updater.getCurrentVersion());
						} else {
							s.sendMessage(ChatColor.YELLOW + "You have the latest version of " + ChatColor.GOLD + "ProjectKorra");
						}	
					} else if (s instanceof ConsoleCommandSender) {
						plugin.updater.checkUpdate();
					}
				}
				return true;
			}
		}; projectkorra.setExecutor(exe);
	}

}
package com.projectkorra.ProjectKorra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import com.projectkorra.ProjectKorra.Ability.AbilityModuleManager;

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
	
	private void init() {
		PluginCommand projectkorra = plugin.getCommand("projectkorra");
		CommandExecutor exe;

		exe = new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
				if (args.length == 0) {
					s.sendMessage("§c/bending help [Ability/Command] §eDisplay help.");
					s.sendMessage("§c/bending choose [Element] §eChoose an element.");
					s.sendMessage("§c/bending bind [Ability] # §eBind an ability.");
					return true;
				}
				if (Arrays.asList(reloadaliases).contains(args[0].toLowerCase())) {
					if (args.length != 1) {
						s.sendMessage("§6Proper Usage: /bending reload");
						return true;
					}
					
					if (!s.hasPermission("bending.command.reload")) {
						s.sendMessage("§cYou don't have permission to do that.");
						return true;
					}
					
					plugin.reloadConfig();
					s.sendMessage("§bBending config reloaded.");
					return true;
				}
				if (Arrays.asList(clearaliases).contains(args[0].toLowerCase())) {
					if (args.length > 2) {
						s.sendMessage("§6Proper Usage: /bending clear <#>");
						return true;
					}
					if (!s.hasPermission("bending.command.clear")) {
						s.sendMessage("§cYou don't have permission to do that.");
						return true;
					}
					
					if (!(s instanceof Player)) {
						s.sendMessage("§cThis command is only usable by players.");
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
							s.sendMessage("§cThe slot must be an integer between 0 and 9.");
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
						s.sendMessage("§6Proper Usage: /bending bind [Ability] <#>");
						return true;
					}
					
					if (!s.hasPermission("bending.command.bind")) {
						s.sendMessage("§cYou don't have permission to do that.");
						return true;
					}
					
					if (!(s instanceof Player)) {
						s.sendMessage("§cYou don't have permission to do that.");
						return true;
					}
					
					if (args.length == 2) {
						// We bind the ability to the slot they have selected..
						// bending bind [Ability]
						String abil = args[1];
						if (!Methods.abilityExists(abil)) {
							s.sendMessage("§cThat is not an ability.");
							return true;
						}
						
						String ability = Methods.getAbility(abil);
						
						if (Methods.isAirAbility(ability) && !Methods.isBender(s.getName(), Element.Air)) {
							s.sendMessage("§7You must be an Airbender to bind this ability.");
							return true;
						}
						if (Methods.isWaterAbility(ability) && !Methods.isBender(s.getName(), Element.Water)) {
							s.sendMessage("§bYou must be a Waterbender to bind this ability.");
							return true;
						}
						if (Methods.isEarthAbility(ability) && !Methods.isBender(s.getName(), Element.Earth)) {
							s.sendMessage("§aYou must be an Earthbender to bind this ability.");
							return true;
						}
						if (Methods.isFireAbility(ability) && !Methods.isBender(s.getName(), Element.Fire)) {
							s.sendMessage("§cYou must be a Firebender to bind this ability.");
							return true;
						}
						if (Methods.isChiAbility(ability) && !Methods.isBender(s.getName(), Element.Chi)) {
							s.sendMessage("§6You must be a ChiBlocker to bind this ability.");
							return true;
						}
						
						Methods.bindAbility((Player) s, ability);
						s.sendMessage("Ability Bound.");
						return true;
					}
					
					if (args.length == 3) {
						// bending bind ability [Slot]
						String abil = args[1];
						if (!Methods.abilityExists(abil)) {
							s.sendMessage("§cThat ability doesn't exist.");
							return true;
						}
						String ability = Methods.getAbility(abil);
						int slot = Integer.parseInt(args[2]);
						if (slot < 1 || slot > 9) {
							s.sendMessage("§cSlot must be an integer between 1 and 9.");
							return true;
						}
						
						if (Methods.isAirAbility(ability) && !Methods.isBender(s.getName(), Element.Air)) {
							s.sendMessage("§7You must be an Airbender to bind this ability.");
							return true;
						}
						if (Methods.isWaterAbility(ability) && !Methods.isBender(s.getName(), Element.Water)) {
							s.sendMessage("§bYou must be a Waterbender to bind this ability.");
							return true;
						}
						if (Methods.isEarthAbility(ability) && !Methods.isBender(s.getName(), Element.Earth)) {
							s.sendMessage("§aYou must be an Earthbender to bind this ability.");
							return true;
						}
						if (Methods.isFireAbility(ability) && !Methods.isBender(s.getName(), Element.Fire)) {
							s.sendMessage("§cYou must be a Firebender to bind this ability.");
							return true;
						}
						if (Methods.isChiAbility(ability) && !Methods.isBender(s.getName(), Element.Air)) {
							s.sendMessage("§6You must be a ChiBlocker to bind this ability.");
							return true;
						}
						Methods.bindAbility((Player) s, ability, slot);
						s.sendMessage("Ability Bound");
						return true;
					}
				}
				if (Arrays.asList(displayaliases).contains(args[0].toLowerCase())) {
					if (args.length > 2) {
						s.sendMessage("§6Proper Usage: /bending display <Element>");
						return true;
					}
					
					if (!s.hasPermission("bending.command.display")) {
						s.sendMessage("§cYou don't have permission to do that.");
						return true;
					}
					
					if (args.length == 2) {
						//bending display [Element]
						if (Arrays.asList(airaliases).contains(args[1])) {
							if (AbilityModuleManager.airbendingabilities.isEmpty()) {
								s.sendMessage("§7There are no airbending abilities available.");
								return true;
							}
							for (String st: AbilityModuleManager.airbendingabilities) {
								s.sendMessage("§7" + st);
							}
							return true;
						}
						if (Arrays.asList(wateraliases).contains(args[1])) {
							if (AbilityModuleManager.waterbendingabilities.isEmpty()) {
								s.sendMessage("§bThere are no waterbending abilities available.");
								return true;
							}
							for (String st: AbilityModuleManager.waterbendingabilities) {
								s.sendMessage("§b" + st);
							}
							return true;
						}
						if (Arrays.asList(earthaliases).contains(args[1])) {
							if (AbilityModuleManager.earthbendingabilities.isEmpty()) {
								s.sendMessage("§aThere are no earthbending abilities available.");
								return true;
							}
							for (String st: AbilityModuleManager.earthbendingabilities) {
								s.sendMessage("§a" + st);
							}
							return true;
						}
						if (Arrays.asList(firealiases).contains(args[1])) {
							if (AbilityModuleManager.firebendingabilities.isEmpty()) {
								s.sendMessage("§cThere are no firebending abilities available.");
								return true;
							}
							for (String st: AbilityModuleManager.firebendingabilities) {
								s.sendMessage("§c" + st);
							}
							return true;
						}
						if (Arrays.asList(chialiases).contains(args[1])) {
							if (AbilityModuleManager.chiabilities.isEmpty()) {
								s.sendMessage("§6There are no chiblocking abilities available.");
								return true;
							}
							for (String st: AbilityModuleManager.chiabilities) {
								s.sendMessage("§6" + st);
							}
							return true;
						}
					}
					if (args.length == 1) {
						//bending display
						if (!(s instanceof Player)) {
							s.sendMessage("§cThis command is only usable by players.");
							return true;
						}
						BendingPlayer bPlayer = Methods.getBendingPlayer(s.getName());
						HashMap<Integer, String> abilities = bPlayer.abilities;
						
						String slot1 = abilities.get(1);
						String slot2 = abilities.get(2);
						String slot3 = abilities.get(3);
						String slot4 = abilities.get(4);
						String slot5 = abilities.get(5);
						String slot6 = abilities.get(6);
						String slot7 = abilities.get(7);
						String slot8 = abilities.get(8);
						String slot9 = abilities.get(9);
						
						if (slot1 != null) {
							if (Methods.isAirAbility(slot1)) s.sendMessage("1 - §7" + slot1);
							if (Methods.isWaterAbility(slot1)) s.sendMessage("1 - §b" + slot1);
							if (Methods.isEarthAbility(slot1)) s.sendMessage("1 - §a" + slot1);
							if (Methods.isFireAbility(slot1)) s.sendMessage("1 - §c" + slot1);
							if (Methods.isChiAbility(slot1)) s.sendMessage("1 - §6" + slot1);
						}
						
						if (slot2 != null) {
							if (Methods.isAirAbility(slot2)) s.sendMessage("2 - §7" + slot2);
							if (Methods.isWaterAbility(slot2)) s.sendMessage("2 - §b" + slot2);
							if (Methods.isEarthAbility(slot2)) s.sendMessage("2 - §a" + slot2);
							if (Methods.isFireAbility(slot2)) s.sendMessage("2 - §c" + slot2);
							if (Methods.isChiAbility(slot2)) s.sendMessage("2 - §6" + slot2);
						}
						
						if (slot3 != null) {
							if (Methods.isAirAbility(slot3)) s.sendMessage("3 - §7" + slot3);
							if (Methods.isWaterAbility(slot3)) s.sendMessage("3 - §b" + slot3);
							if (Methods.isEarthAbility(slot3)) s.sendMessage("3 - §a" + slot3);
							if (Methods.isFireAbility(slot3)) s.sendMessage("3 - §c" + slot3);
							if (Methods.isChiAbility(slot3)) s.sendMessage("3 - §6" + slot3);
						}
						
						if (slot4 != null) {
							if (Methods.isAirAbility(slot4)) s.sendMessage("4 - §7" + slot4);
							if (Methods.isWaterAbility(slot4)) s.sendMessage("4 - §b" + slot4);
							if (Methods.isEarthAbility(slot4)) s.sendMessage("4 - §a" + slot4);
							if (Methods.isFireAbility(slot4)) s.sendMessage("4 - §c" + slot4);
							if (Methods.isChiAbility(slot4)) s.sendMessage("4 - §6" + slot4);
						}
						
						if (slot5 != null) {
							if (Methods.isAirAbility(slot5)) s.sendMessage("5 - §7" + slot5);
							if (Methods.isWaterAbility(slot5)) s.sendMessage("5 - §b" + slot5);
							if (Methods.isEarthAbility(slot5)) s.sendMessage("5 - §a" + slot5);
							if (Methods.isFireAbility(slot5)) s.sendMessage("5 - §c" + slot5);
							if (Methods.isChiAbility(slot5)) s.sendMessage("5 - §6" + slot5);
						}
						
						if (slot6 != null) {
							if (Methods.isAirAbility(slot6)) s.sendMessage("6 - §7" + slot6);
							if (Methods.isWaterAbility(slot6)) s.sendMessage("6 - §b" + slot6);
							if (Methods.isEarthAbility(slot6)) s.sendMessage("6 - §a" + slot6);
							if (Methods.isFireAbility(slot6)) s.sendMessage("6 - §c" + slot6);
							if (Methods.isChiAbility(slot6)) s.sendMessage("6 - §6" + slot6);
						}
						
						if (slot7 != null) {
							if (Methods.isAirAbility(slot7)) s.sendMessage("7 - §7" + slot7);
							if (Methods.isWaterAbility(slot7)) s.sendMessage("7 - §b" + slot7);
							if (Methods.isEarthAbility(slot7)) s.sendMessage("7 - §a" + slot7);
							if (Methods.isFireAbility(slot7)) s.sendMessage("7 - §c" + slot7);
							if (Methods.isChiAbility(slot7)) s.sendMessage("7 - §6" + slot7);
						}
						
						if (slot8 != null) {
							if (Methods.isAirAbility(slot8)) s.sendMessage("8 - §7" + slot8);
							if (Methods.isWaterAbility(slot8)) s.sendMessage("8 - §b" + slot8);
							if (Methods.isEarthAbility(slot8)) s.sendMessage("8 - §a" + slot8);
							if (Methods.isFireAbility(slot8)) s.sendMessage("8 - §c" + slot8);
							if (Methods.isChiAbility(slot8)) s.sendMessage("8 - §6" + slot8);
						}
						
						if (slot9 != null) {
							if (Methods.isAirAbility(slot9)) s.sendMessage("9 - §7" + slot9);
							if (Methods.isWaterAbility(slot9)) s.sendMessage("9 - §b" + slot9);
							if (Methods.isEarthAbility(slot9)) s.sendMessage("9 - §a" + slot9);
							if (Methods.isFireAbility(slot9)) s.sendMessage("9 - §c" + slot9);
							if (Methods.isChiAbility(slot9)) s.sendMessage("9 - §6" + slot9);
						}
						if (abilities.isEmpty()) {
							s.sendMessage("You don't have any bound abilities.");
							return true;
						}
						return true;
					}
				}
				if (Arrays.asList(togglealiases).contains(args[0].toLowerCase())) {
					if (args.length != 1) {
						s.sendMessage("§6Proper Usage: /bending toggle");
						return true;
					}
					if (!s.hasPermission("bending.command.toggle")) {
						s.sendMessage("§cYou don't have permission to do that.");
						return true;
					}
					
					if (!(s instanceof Player)) {
						s.sendMessage("§cThis command is only usable by players.");
						return true;
					}
					
					BendingPlayer bPlayer = Methods.getBendingPlayer(s.getName());
					
					if (bPlayer.isToggled) {
						s.sendMessage("§cYour bending has been toggled off. You will not be able to use most abilities until you toggle it back.");
						bPlayer.isToggled = false;
						return true;
					} else {
						s.sendMessage("§aYou have turned your Bending back on.");
						bPlayer.isToggled = true;
						return true;
					}
				}
				if (args[0].equalsIgnoreCase("who")) {
					if (args.length > 2) {
						s.sendMessage("§6Proper Usage: /bending who <Player>");
						return true;
					}
					if (!s.hasPermission("bending.command.who")) {
						s.sendMessage("§cYou don't have permission to do that.");
						return true;
					}
					
					if (args.length == 2) {
						Player p = Bukkit.getPlayer(args[1]);
						if (p == null) {
							s.sendMessage("§cThat player is not online.");
							return true;
						}
						
						String un = p.getName();
						s.sendMessage(un + " - ");
						if (Methods.isBender(un, Element.Air)) {
							s.sendMessage("§7- Airbender");
						}
						if (Methods.isBender(un, Element.Water)) {
							s.sendMessage("§b- Waterbender");
						}
						if (Methods.isBender(un, Element.Earth)) {
							s.sendMessage("§a- Earthbender");
						}
						if (Methods.isBender(un, Element.Fire)) {
							s.sendMessage("§c- Firebender");
						}
						if (Methods.isBender(un, Element.Chi)) {
							s.sendMessage("§6- ChiBlocker");
						}
						return true;
					}
					if (args.length == 1) {
						List<String> players = new ArrayList<String>();
						for (Player player: Bukkit.getOnlinePlayers()) {
							String un = player.getName();
							
							BendingPlayer bp = Methods.getBendingPlayer(un);
							if (bp.elements.size() > 1) {
								players.add("§5" + un);
								continue;
							}
							if (bp.elements.size() == 0) {
								players.add(un);
								continue;
							}
							if (Methods.isBender(un, Element.Air)) {
								players.add("§7" + un);
								continue;
							}
							if (Methods.isBender(un, Element.Water)){
								players.add("§b" + un);
								continue;
							}
							if (Methods.isBender(un, Element.Earth)) {
								players.add("§a" + un);
								continue;
							}
							if (Methods.isBender(un, Element.Chi)) {
								players.add("§6" + un);
								continue;
							}
							if (Methods.isBender(un, Element.Fire)) {
								players.add("§c" + un);
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
						s.sendMessage("§6Proper Usage: /bending version");
						return true;
					}
					
					if (!s.hasPermission("bending.command.version")) {
						s.sendMessage("§cYou don't have permission to do that.");
						return true;
					}
					s.sendMessage("§aThis server is running §cProjectKorra v" + plugin.getDescription().getVersion());
					return true;
				}
				if (Arrays.asList(removealiases).contains(args[0].toLowerCase())) {
					//bending remove [Player]
					if (args.length != 2) {
						s.sendMessage("§6Proper Usage: /bending remove [Player]");
						return true;
					}
					
					if (!s.hasPermission("bending.admin.remove")) {
						s.sendMessage("§cYou don't have permission to do that.");
						return true;
					}
					
					Player player = Bukkit.getPlayer(args[1]);
					
					if (player == null) {
						s.sendMessage("§cThat player is not online.");
						return true;
					}
					
					BendingPlayer bPlayer = Methods.getBendingPlayer(player.getName());
					bPlayer.elements.clear();
					s.sendMessage("§aYou have removed the bending of §3" + player.getName());
					player.sendMessage("§aYour bending has been removed by §3" + s.getName());
					return true;
					
				}
				if (Arrays.asList(permaremovealiases).contains(args[0].toLowerCase())) {
					//bending permaremove [Player]
					if (args.length != 2) {
						s.sendMessage("§6Proper Usage: /§3/bending permaremove [Player]");
						return true;
					}
					
					if (!s.hasPermission("bending.admin.permaremove")) {
						s.sendMessage("§cYou don't have permission to do that.");
						return true;
					}
					
					Player player = Bukkit.getPlayer(args[1]);
					
					if (player == null) {
						s.sendMessage("§cThat player is not online.");
						return true;
					}
					
					BendingPlayer bPlayer = Methods.getBendingPlayer(player.getName());
					bPlayer.elements.clear();
					bPlayer.permaRemoved = true;
					player.sendMessage("§cYour bending has been permanently removed.");
					s.sendMessage("§cYou have permanently removed the bending of: §3" + player.getName());
					return true;
				}
				if (args[0].equalsIgnoreCase("add")) {
					//bending add [Player] [Element]
					if (args.length > 3) {
						s.sendMessage("§6Proper Usage: §3/bending add [Player] [Element]");
						s.sendMessage("§6Applicable Elements: §3Air, Water, Earth, Fire, Chi");
						return true;
					}
					if (args.length == 3) {
						if (!s.hasPermission("bending.command.add.others")) {
							s.sendMessage("§cYou don't have permission to do that.");
							return true;
						}
						
						Player player = Bukkit.getPlayer(args[1]);
						if (player == null) {
							s.sendMessage("§cThat player is not online.");
							return true;
						}
						
						BendingPlayer bPlayer = Methods.getBendingPlayer(player.getName());
						if (Arrays.asList(airaliases).contains(args[1].toLowerCase())) {
							bPlayer.addElement(Element.Air);
							player.sendMessage("§7You are also an airbender.");
							s.sendMessage("§3" + player.getName() + " §7is also an airbender.");
							return true;
						}
						
						if (Arrays.asList(wateraliases).contains(args[1].toLowerCase())) {
							bPlayer.addElement(Element.Water);
							player.sendMessage("§bYou are also a waterbender.");
							s.sendMessage("§3" + player.getName() + " §bis also a waterbender.");
							return true;
						}
						
						if (Arrays.asList(earthaliases).contains(args[1].toLowerCase())) {
							bPlayer.addElement(Element.Earth);
							player.sendMessage("§aYou are also an Earthbender.");
							s.sendMessage("§3" + player.getName() + " §ais also an Earthbender.");
							return true;
						}
						
						if (Arrays.asList(firealiases).contains(args[1].toLowerCase())) {
							bPlayer.addElement(Element.Fire);
							player.sendMessage("§aYou are also a Firebender.");
							s.sendMessage("§3" + player.getName() + " §cis also a Firebender");
							return true;
						}
						if (Arrays.asList(chialiases).contains(args[1].toLowerCase())) {
							bPlayer.addElement(Element.Chi);
							player.sendMessage("§6You are also a ChiBlocker.");
							s.sendMessage("§3" + player.getName() + " §6is also a ChiBlocker");
							return true;
						}
						
						s.sendMessage("§cYou must specify an element.");
						return true;
					}
					if (args.length == 2) {
						// Target = Self
						if (!s.hasPermission("bending.command.add")) {
							s.sendMessage("§cYou don't have permission to do that.");
							return true;
						}

						if (!(s instanceof Player)) {
							s.sendMessage("§cThis command is only usable by Players.");
							return true;
						}

						BendingPlayer bPlayer = Methods.getBendingPlayer(s.getName());

						if (Arrays.asList(airaliases).contains(args[1].toLowerCase())) {
							bPlayer.addElement(Element.Air);
							s.sendMessage("§7You are also an airbender.");
							return true;
						}
						
						if (Arrays.asList(wateraliases).contains(args[1].toLowerCase())) {
							bPlayer.addElement(Element.Water);
							s.sendMessage("§bYou are also a waterbender.");
							return true;
						}
						
						if (Arrays.asList(earthaliases).contains(args[1].toLowerCase())) {
							bPlayer.addElement(Element.Earth);
							s.sendMessage("§aYou are also an Earthbender.");
							return true;
						}
						
						if (Arrays.asList(firealiases).contains(args[1].toLowerCase())) {
							bPlayer.addElement(Element.Fire);
							s.sendMessage("§aYou are also a Firebender.");
							return true;
						}
						if (Arrays.asList(chialiases).contains(args[1].toLowerCase())) {
							bPlayer.addElement(Element.Chi);
							s.sendMessage("§6You are also a ChiBlocker.");
							return true;
						}
						s.sendMessage("§cYou must specify an element.");
					}
				}
				if (Arrays.asList(choosealiases).contains(args[0].toLowerCase())) {
					// /bending choose [Player] [Element]
					if (args.length > 3) {
						s.sendMessage("§6Proper Usage: §3/bending choose [Player] [Element]");
						s.sendMessage("§6Applicable Elements: §3Air, Water, Earth, Fire, and Chi");
						return true;
					}

					if (args.length == 2) {
						if (!s.hasPermission("bending.command.choose")) {
							s.sendMessage("§cYou don't have permission to do that.");
							return true;
						}

						if (!(s instanceof Player)) {
							s.sendMessage("§cThis command is only usable by players.");
							return true;
						}

						BendingPlayer bPlayer = Methods.getBendingPlayer(s.getName());

						if (bPlayer.isPermaRemoved()) {
							s.sendMessage("§cYour bending was permanently removed.");
							return true;
						}
						
						if (!bPlayer.getElements().isEmpty()) {
							if (!s.hasPermission("bending.command.rechoose")) {
								s.sendMessage("§cYou don't have permission to do that.");
								return true;
							}
						}
						if (Arrays.asList(airaliases).contains(args[1].toLowerCase())) {
							bPlayer.setElement(Element.Air);
							s.sendMessage("§7You are now an Airbender.");
							return true;
						}
						if (Arrays.asList(wateraliases).contains(args[1].toLowerCase())) {
							bPlayer.setElement(Element.Water);
							s.sendMessage("§bYou are now a waterbender.");
							return true;
						}
						if (Arrays.asList(earthaliases).contains(args[1].toLowerCase())) {
							bPlayer.setElement(Element.Earth);
							s.sendMessage("§aYou are now an Earthbender.");
							return true;
						}
						if (Arrays.asList(firealiases).contains(args[1].toLowerCase())) {
							bPlayer.setElement(Element.Fire);
							s.sendMessage("§cYou are now a Firebender.");
							return true;
						}
						if (Arrays.asList(chialiases).contains(args[1].toLowerCase())) {
							bPlayer.setElement(Element.Chi);
							s.sendMessage("§cYou are now a ChiBlocker.");
							return true;
						}
						s.sendMessage("§6Proper Usage: §3/bending choose [Element]");
						s.sendMessage("§6Applicable Elements: §3Air, Water, Earth, Fire");
						return true;
					}
					if (args.length == 3) {
						if (!s.hasPermission("bending.admin.choose")) {
							s.sendMessage("§cYou don't have permission to do that.");
							return true;
						}
						Player target = Bukkit.getPlayer(args[1]);
						if (target == null) {
							s.sendMessage("§cThat player is not online.");
							return true;
						}
						BendingPlayer bTarget = Methods.getBendingPlayer(target.getName());

						if (bTarget.isPermaRemoved()) {
							s.sendMessage("§cThat player's bending was permanently removed.");
							return true;
						}
						Element e = null;
						if (Arrays.asList(airaliases).contains(args[2])) e = Element.Air;
						if (Arrays.asList(wateraliases).contains(args[2])) e = Element.Water;
						if (Arrays.asList(earthaliases).contains(args[2])) e = Element.Earth;
						if (Arrays.asList(firealiases).contains(args[2])) e = Element.Fire;
						if (Arrays.asList(chialiases).contains(args[2])) e = Element.Chi;

						if (e == null) {
							s.sendMessage("§cYou must specify an element.");
							return true;
						} else {
							bTarget.setElement(e);
							target.sendMessage("§cYour bending has been changed to §3" + e.toString() + " §cby §3" + s.getName());
							return true;
						}
					}
				}
				if (Arrays.asList(helpaliases).contains(args[0].toLowerCase())) {
					if (args.length != 2) {
						s.sendMessage("§6Proper Usage: /bending help Command/Ability");
						return true;
					}
					if (!s.hasPermission("bending.command.help")) {
						s.sendMessage("§cYou don't have permission to do that.");
						return true;
					}
					if (Arrays.asList(displayaliases).contains(args[1].toLowerCase())) {
						s.sendMessage("§6Proper Usage: §3/bending display <Element>");
						s.sendMessage("§eThis command will show you all of the elements you have bound if you do not specify an element."
								+ " If you do specify an element (Air, Water, Earth, Fire, or Chi), it will show you all of the available "
								+ " abilities of that element installed on the server.");
					}
					if (Arrays.asList(choosealiases).contains(args[1].toLowerCase())) {
						s.sendMessage("§6Proper Usage: §3/bending choose <Player> [Element]");
						s.sendMessage("§6Applicable Elements: §3Air, Water, Earth, Fire, Chi");
						s.sendMessage("§eThis command will allow the user to choose a player either for himself or <Player> if specified. "
								+ " This command can only be used once per player unless they have permission to rechoose their element.");
						return true;
					}
					if (args[1].equalsIgnoreCase("add")) {
						s.sendMessage("§6Proper Usage: §3/bending add <Player> [Element]");
						s.sendMessage("§6Applicable Elements: §3Air, Water, Earth, Fire, Chi");
						s.sendMessage("§eThis command will allow the user to add an element to the targeted <Player>, or themselves if the target"
								+ " is not specified. This command is typically reserved for server administrators.");
						return true;
					}
					if (Arrays.asList(permaremovealiases).contains(args[1].toLowerCase())) {
						s.sendMessage("§6Proper Usage: §3/bending permaremove <Player>");
						s.sendMessage("§eThis command will permanently remove the Bending of the targeted <Player>. Once removed, a player"
								+ " may only receive Bending again if this command is run on them again. This command is typically reserved for"
								+ " administrators.");
						return true;
					}
					if (Arrays.asList(versionaliases).contains(args[1].toLowerCase())) {
						s.sendMessage("§6Proper Usage: §3/bending version");
						s.sendMessage("§eThis command will print out the version of ProjectKorra this server is running.");
						return true;
					}
					
					if (Arrays.asList(removealiases).contains(args[1].toLowerCase())) {
						s.sendMessage("§6Proper Usage: §3/bending remove [Player]");
						s.sendMessage("§eThis command will remove the element of the targeted [Player]. The player will be able to re-pick "
								+ " their element after this command is run on them, assuming their Bending was not permaremoved.");
						return true;
					}
					
					if (Arrays.asList(togglealiases).contains(args[1].toLowerCase())) {
						s.sendMessage("§6Proper Usage: §3/bending toggle");
						s.sendMessage("§eThis command will toggle a player's own Bending on or off. If toggled off, all abilities should stop"
								+ " working until it is toggled back on. Logging off will automatically toggle your Bending back on.");
						return true;
					}
					if (args[1].equalsIgnoreCase("who")) {
						s.sendMessage("§6Proper Usage: §3/bending who <Player>");
						s.sendMessage("§eThis command will tell you what element all players that are online are (If you don't specify a player)"
								+ " or give you information about the player that you specify.");
						return true;
					}
					
					if (Arrays.asList(clearaliases).contains(args[1].toLowerCase())) {
						s.sendMessage("§6Proper Usage: §3/bending clear <slot>");
						s.sendMessage("§eThis command will clear the bound ability from the slot you specify (if you specify one."
								+ " If you choose not to specify a slot, all of your abilities will be cleared.");
					}
					if (Arrays.asList(reloadaliases).contains(args[1].toLowerCase())) {
						s.sendMessage("§6Proper Usage: §3/bending reload");
						s.sendMessage("§eThis command will reload the Bending config file.");
						return true;
					}
					if (Arrays.asList(bindaliases).contains(args[1].toLowerCase())) {
						s.sendMessage("§6Proper Usage: §3/bending bind [Ability] <Slot>");
						s.sendMessage("§eThis command will bind an ability to the slot you specify (if you specify one), or the slot currently"
								+ " selected in your hotbar (If you do not specify a Slot #).");
					}
					
					if (Methods.abilityExists(args[1])) {
						String ability = Methods.getAbility(args[1]);
						if (Methods.isAirAbility(ability)) {
							s.sendMessage("§7" + ability + " - ");
							s.sendMessage("§7" + AbilityModuleManager.descriptions.get(ability));
						}
						if (Methods.isWaterAbility(ability)) {
							s.sendMessage("§b" + ability + " - ");
							s.sendMessage("§b" + AbilityModuleManager.descriptions.get(ability));
						}
						if (Methods.isEarthAbility(ability)) {
							s.sendMessage("§a" + ability + " - ");
							s.sendMessage("§a" + AbilityModuleManager.descriptions.get(ability));
						}
						if (Methods.isFireAbility(ability)) {
							s.sendMessage("§c" + ability + " - ");
							s.sendMessage("§c" + AbilityModuleManager.descriptions.get(ability));
						}
						if (Methods.isChiAbility(ability)) {
							s.sendMessage("§6" + ability + " - ");
							s.sendMessage("§6" + AbilityModuleManager.descriptions.get(ability));
						}
					}
				}
				return true;
			}
		}; projectkorra.setExecutor(exe);
	}

}

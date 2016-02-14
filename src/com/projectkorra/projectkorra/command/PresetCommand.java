package com.projectkorra.projectkorra.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.object.Preset;

/**
 * Executor for /bending preset. Extends {@link PKCommand}.
 */
public class PresetCommand extends PKCommand {

	private static final String[] createaliases = { "create", "c", "save" };
	private static final String[] deletealiases = { "delete", "d", "del" };
	private static final String[] listaliases = { "list", "l" };
	private static final String[] bindaliases = { "bind", "b" };

	public PresetCommand() {
		super("preset", "/bending preset create|bind|list|delete [name]", "This command manages Presets, which are saved bindings. Use /bending preset list to view your existing presets, use /bending [create|delete] [name] to manage your presets, and use /bending bind [name] to bind an existing preset.", new String[] { "preset", "presets", "pre", "set", "p" });
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute(CommandSender sender, List<String> args) {
		if (!isPlayer(sender) || !correctLength(sender, args.size(), 1, 3)) {
			return;
		} else if (MultiAbilityManager.hasMultiAbilityBound((Player) sender)) {
			sender.sendMessage(ChatColor.RED + "You can't edit your binds right now!");
			return;
		}

		Player player = (Player) sender;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer == null) {
			GeneralMethods.createBendingPlayer(((Player) player).getUniqueId(), player.getName());
			bPlayer = BendingPlayer.getBendingPlayer(player);
		}

		//bending preset list
		if (args.size() == 1) {
			if (Arrays.asList(listaliases).contains(args.get(0)) && hasPermission(sender, "list")) {
				List<Preset> presets = Preset.presets.get(player.getUniqueId());
				List<String> presetNames = new ArrayList<String>();

				if (presets == null || presets.isEmpty()) {
					sender.sendMessage(ChatColor.RED + "You don't have any presets.");
					return;
				}

				for (Preset preset : presets) {
					presetNames.add(preset.getName());
				}

				sender.sendMessage(ChatColor.GREEN + "Your Presets: " + ChatColor.DARK_AQUA + presetNames.toString());
				return;
			} else {
				help(sender, false);
				return;
			}
		}

		String name = args.get(1);
		if (Arrays.asList(deletealiases).contains(args.get(0)) && hasPermission(sender, "delete")) { //bending preset delete name
			if (!Preset.presetExists(player, name)) {
				sender.sendMessage(ChatColor.RED + "You don't have a preset with that name.");
				return;
			}

			Preset preset = Preset.getPreset(player, name);
			preset.delete();
			sender.sendMessage(ChatColor.GREEN + "You have deleted your preset named: " + ChatColor.YELLOW + name);
			return;
		} else if (Arrays.asList(bindaliases).contains(args.get(0)) && hasPermission(sender, "bind")) { //bending preset bind name
			if (args.size() < 3) {
				boolean boundAll = false;
				if (Preset.presetExists(player, name)) {
					Preset preset = Preset.getPreset(player, name);
					boundAll = Preset.bindPreset(player, preset);
				} else if (Preset.externalPresetExists(name) && hasPermission(sender, "bind.external")) {
					boundAll = Preset.bindExternalPreset(player, name);
				} else if (!Preset.externalPresetExists(name) && hasPermission(sender, "bind.external")) {
					sender.sendMessage(ChatColor.RED + "No external preset with that name exists.");
					return;
				} else if (bPlayer.isPermaRemoved()) {
					player.sendMessage(ChatColor.RED + "Your bending was permanently removed.");
					return;
				} else {
					sender.sendMessage(ChatColor.RED + "You don't have a preset with that name.");
					return;
				}

				sender.sendMessage(ChatColor.GREEN + "Your bound slots have been set to match the " + ChatColor.YELLOW + name + ChatColor.GREEN + " preset.");
				if (!boundAll) {
					sender.sendMessage(ChatColor.RED + "Some abilities were not bound because you cannot bend the required element.");
				}
			} else if (hasPermission(sender, "bind.external.assign") && Preset.externalPresetExists(name)) {
				if (!Preset.externalPresetExists(name)) {
					sender.sendMessage(ChatColor.RED + "No external preset with that name exists.");
					return;
				}

				Player player2 = Bukkit.getPlayer(args.get(2));
				if (player2 != null && player2.isOnline()) {
					BendingPlayer bPlayer2 = BendingPlayer.getBendingPlayer(player2);

					if (bPlayer2 == null) {
						GeneralMethods.createBendingPlayer(((Player) player2).getUniqueId(), player2.getName());
						bPlayer2 = BendingPlayer.getBendingPlayer(player2);
					}
					if (bPlayer2.isPermaRemoved()) {
						player.sendMessage(ChatColor.RED + "That players bending was permanently removed.");
						return;
					}
					boolean boundAll = Preset.bindExternalPreset(player2, name);

					sender.sendMessage(ChatColor.GREEN + "The bound slots of " + ChatColor.YELLOW + player2.getName() + ChatColor.GREEN + " have been set to match the " + ChatColor.YELLOW + name + ChatColor.GREEN + " preset.");
					player2.sendMessage(ChatColor.GREEN + "Your bound slots have been set to match the " + ChatColor.YELLOW + name + ChatColor.GREEN + " preset.");
					if (!boundAll) {
						player2.sendMessage(ChatColor.RED + "Some abilities were not bound, either the preset contains invalid abilities or you cannot bend the required elements.");
					}
					return;
				} else {
					sender.sendMessage(ChatColor.RED + "Player not found.");
				}
			} else if (hasPermission(sender, "bind.assign") && Preset.presetExists(player, name)) {
				if (!Preset.presetExists(player, name)) {
					sender.sendMessage(ChatColor.RED + "You don't have a preset with that name.");
					return;
				}

				Player player2 = Bukkit.getPlayer(args.get(2));
				if (player2 != null && player2.isOnline()) {
					BendingPlayer bPlayer2 = BendingPlayer.getBendingPlayer(player2);

					if (bPlayer2 == null) {
						GeneralMethods.createBendingPlayer(((Player) player2).getUniqueId(), player2.getName());
						bPlayer2 = BendingPlayer.getBendingPlayer(player2);
					}
					if (bPlayer2.isPermaRemoved()) {
						player.sendMessage(ChatColor.RED + "That players bending was permanently removed.");
						return;
					}
					Preset preset = Preset.getPreset(player, name);
					boolean boundAll = Preset.bindPreset(player2, preset);

					sender.sendMessage(ChatColor.GREEN + "The bound slots of " + ChatColor.YELLOW + player2.getName() + ChatColor.GREEN + " have been set to match your " + ChatColor.YELLOW + name + ChatColor.GREEN + " preset.");
					player2.sendMessage(ChatColor.GREEN + "Your bound slots have been set to match " + ChatColor.YELLOW + player.getName() + "'s " + name + ChatColor.GREEN + " preset.");
					if (!boundAll) {
						player2.sendMessage(ChatColor.RED + "Some abilities were not bound, either the preset contains invalid abilities or you cannot bend the required elements.");
					}
					return;
				} else {
					sender.sendMessage(ChatColor.RED + "Player not found.");
				}
			}
		} else if (Arrays.asList(createaliases).contains(args.get(0)) && hasPermission(sender, "create")) { //bending preset create name
			int limit = GeneralMethods.getMaxPresets(player);

			if (Preset.presets.get(player) != null && Preset.presets.get(player).size() >= limit) {
				sender.sendMessage(ChatColor.RED + "You have reached your max number of Presets.");
				return;
			} else if (Preset.presetExists(player, name)) {
				sender.sendMessage(ChatColor.RED + "A preset with that name already exists.");
				return;
			}

			if (bPlayer == null) {
				return;
			}
			HashMap<Integer, String> abilities = (HashMap<Integer, String>) bPlayer.getAbilities().clone();

			Preset preset = new Preset(player.getUniqueId(), name, abilities);
			preset.save(player);
			sender.sendMessage(ChatColor.GREEN + "Created preset with the name: " + ChatColor.YELLOW + name);
		} else {
			help(sender, false);
		}
	}
}
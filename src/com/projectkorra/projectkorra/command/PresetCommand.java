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
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.object.Preset;

/**
 * Executor for /bending preset. Extends {@link PKCommand}.
 */
public class PresetCommand extends PKCommand {

	private static final String[] createaliases = { "create", "c", "save" };
	private static final String[] deletealiases = { "delete", "d", "del" };
	private static final String[] listaliases = { "list", "l" };
	private static final String[] bindaliases = { "bind", "b" };

	private String noPresets;
	private String noPresetName;
	private String deletePreset;
	private String noPresetNameExternal;
	private String bendingRemoved;
	private String bound;
	private String failedToBindAll;
	private String bendingRemovedOther;
	private String boundOtherConfirm;
	private String succesfullyCopied;
	private String reachedMax;
	private String alreadyExists;
	private String createdNewPreset;
	private String cantEditBinds;

	public PresetCommand() {
		super("preset", "/bending preset <Bind/Create/Delete/List> [Preset]", ConfigManager.languageConfig.get().getString("Commands.Preset.Description"), new String[] { "preset", "presets", "pre", "set", "p" });

		this.noPresets = ConfigManager.languageConfig.get().getString("Commands.Preset.NoPresets");
		this.noPresetName = ConfigManager.languageConfig.get().getString("Commands.Preset.NoPresetName");
		this.deletePreset = ConfigManager.languageConfig.get().getString("Commands.Preset.Delete");
		this.noPresetNameExternal = ConfigManager.languageConfig.get().getString("Commands.Preset.External.NoPresetName");
		this.bendingRemoved = ConfigManager.languageConfig.get().getString("Commands.Preset.BendingPermanentlyRemoved");
		this.bound = ConfigManager.languageConfig.get().getString("Commands.Preset.SuccesfullyBound");
		this.failedToBindAll = ConfigManager.languageConfig.get().getString("Commands.Preset.FailedToBindAll");
		this.bendingRemovedOther = ConfigManager.languageConfig.get().getString("Commands.Preset.Other.BendingPermanentlyRemoved");
		this.boundOtherConfirm = ConfigManager.languageConfig.get().getString("Commands.Preset.Other.SuccesfullyBoundConfirm");
		this.succesfullyCopied = ConfigManager.languageConfig.get().getString("Commands.Preset.SuccesfullyCopied");
		this.reachedMax = ConfigManager.languageConfig.get().getString("Commands.Preset.MaxPresets");
		this.alreadyExists = ConfigManager.languageConfig.get().getString("Commands.Preset.AlreadyExists");
		this.createdNewPreset = ConfigManager.languageConfig.get().getString("Commands.Preset.Created");
		this.cantEditBinds = ConfigManager.languageConfig.get().getString("Commands.Preset.CantEditBinds");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute(CommandSender sender, List<String> args) {
		if (!isPlayer(sender) || !correctLength(sender, args.size(), 1, 3)) {
			return;
		} else if (MultiAbilityManager.hasMultiAbilityBound((Player) sender)) {
			GeneralMethods.sendBrandingMessage(sender, this.cantEditBinds);
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
					GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.noPresets);
					return;
				}

				for (Preset preset : presets) {
					presetNames.add(preset.getName());
				}

				sender.sendMessage(ChatColor.GREEN + "Presets: " + ChatColor.DARK_AQUA + presetNames.toString());
				return;
			} else {
				help(sender, false);
				return;
			}
		}

		String name = args.get(1);
		if (Arrays.asList(deletealiases).contains(args.get(0)) && hasPermission(sender, "delete")) { //bending preset delete name
			if (!Preset.presetExists(player, name)) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.noPresetName);
				return;
			}

			Preset preset = Preset.getPreset(player, name);
			preset.delete();
			sender.sendMessage(ChatColor.GREEN + this.deletePreset.replace("{name}", ChatColor.YELLOW + preset.getName() + ChatColor.GREEN));
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
					GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.noPresetNameExternal);
					return;
				} else if (bPlayer.isPermaRemoved()) {
					GeneralMethods.sendBrandingMessage(player, ChatColor.RED + this.bendingRemoved);
					return;
				} else {
					GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.noPresetName);
					return;
				}

				sender.sendMessage(ChatColor.GREEN + bound.replace("{name}", ChatColor.YELLOW + name + ChatColor.GREEN));
				if (!boundAll) {
					GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.failedToBindAll);
				}
			} else if (hasPermission(sender, "bind.external.assign") && Preset.externalPresetExists(name)) {
				if (!Preset.externalPresetExists(name)) {
					GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.noPresetNameExternal);
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
						GeneralMethods.sendBrandingMessage(player, ChatColor.RED + this.bendingRemovedOther);
						return;
					}
					boolean boundAll = Preset.bindExternalPreset(player2, name);

					GeneralMethods.sendBrandingMessage(sender, ChatColor.GREEN + this.boundOtherConfirm.replace("{target}", ChatColor.YELLOW + player2.getName() + ChatColor.GREEN).replace("{name}", ChatColor.YELLOW + name + ChatColor.GREEN + ChatColor.YELLOW));
					GeneralMethods.sendBrandingMessage(player2, ChatColor.GREEN + this.bound.replace("{name}", ChatColor.YELLOW + name + ChatColor.GREEN));
					if (!boundAll) {
						GeneralMethods.sendBrandingMessage(player2, ChatColor.RED + this.failedToBindAll);
					}
					return;
				} else {
					GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + ConfigManager.languageConfig.get().getString("Commands.Preset.PlayerNotFound"));
				}
			} else if (hasPermission(sender, "bind.assign") && Preset.presetExists(player, name)) {
				if (!Preset.presetExists(player, name)) {
					GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.noPresetName);
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
						GeneralMethods.sendBrandingMessage(player, ChatColor.RED + this.bendingRemovedOther);
						return;
					}
					Preset preset = Preset.getPreset(player, name);
					boolean boundAll = Preset.bindPreset(player2, preset);

					GeneralMethods.sendBrandingMessage(sender, ChatColor.GREEN + this.boundOtherConfirm.replace("{target}", ChatColor.YELLOW + player2.getName() + ChatColor.GREEN).replace("{name}", ChatColor.YELLOW + name + ChatColor.GREEN + ChatColor.YELLOW));
					GeneralMethods.sendBrandingMessage(player2, ChatColor.GREEN + this.succesfullyCopied.replace("{target}", ChatColor.YELLOW + player.getName() + ChatColor.GREEN));
					if (!boundAll) {
						GeneralMethods.sendBrandingMessage(player2, ChatColor.RED + this.failedToBindAll);
					}
					return;
				} else {
					GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + ConfigManager.languageConfig.get().getString("Commands.Preset.PlayerNotFound"));
				}
			}
		} else if (Arrays.asList(createaliases).contains(args.get(0)) && hasPermission(sender, "create")) { //bending preset create name
			int limit = GeneralMethods.getMaxPresets(player);

			if (Preset.presets.get(player) != null && Preset.presets.get(player).size() >= limit) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.reachedMax);
				return;
			} else if (Preset.presetExists(player, name)) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.alreadyExists);
				return;
			}

			if (bPlayer == null) {
				return;
			}
			HashMap<Integer, String> abilities = (HashMap<Integer, String>) bPlayer.getAbilities().clone();

			Preset preset = new Preset(player.getUniqueId(), name, abilities);
			preset.save(player);
			GeneralMethods.sendBrandingMessage(sender, ChatColor.GREEN + this.createdNewPreset.replace("{name}", ChatColor.YELLOW + name + ChatColor.GREEN));
		} else {
			help(sender, false);
		}
	}

	@Override
	protected List<String> getTabCompletion(CommandSender sender, List<String> args) {
		if (args.size() >= 3 || !sender.hasPermission("bending.command.preset") || !(sender instanceof Player))
			return new ArrayList<String>();
		List<String> l = new ArrayList<String>();
		if (args.size() == 0) {
			l.add("create");
			l.add("delete");
			l.add("list");
			l.add("bind");
			return l;
		} else if (args.size() == 2 && Arrays.asList(new String[] { "delete", "d", "del", "bind", "b" }).contains(args.get(0).toLowerCase())) {
			List<Preset> presets = Preset.presets.get(((Player) sender).getUniqueId());
			List<String> presetNames = new ArrayList<String>();
			if (presets != null && presets.size() != 0) {
				for (Preset preset : presets) {
					presetNames.add(preset.getName());
				}
			}
			if (sender.hasPermission("bending.command.preset.bind.external")) {
				if (Preset.externalPresets.keySet().size() > 0) {
					for (String externalPreset : Preset.externalPresets.keySet()) {
						presetNames.add(externalPreset);
					}
				}
			}
			if (presetNames.size() == 0)
				return new ArrayList<String>();
		}
		return l;
	}
}

package com.projectkorra.projectkorra.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.commands.PresetCommandConfig;
import com.projectkorra.projectkorra.configuration.configs.properties.CommandPropertiesConfig;
import com.projectkorra.projectkorra.object.Preset;

/**
 * Executor for /bending preset. Extends {@link PKCommand}.
 */
public class PresetCommand extends PKCommand<PresetCommandConfig> {

	private static final String[] createaliases = { "create", "c", "save" };
	private static final String[] deletealiases = { "delete", "d", "del" };
	private static final String[] listaliases = { "list", "l" };
	private static final String[] bindaliases = { "bind", "b" };

	private final String noPresets;
	private final String noPresetName;
	private final String deletePreset;
	private final String noPresetNameExternal;
	private final String bendingRemoved;
	private final String bound;
	private final String failedToBindAll;
	private final String bendingRemovedOther;
	private final String boundOtherConfirm;
	private final String succesfullyCopied;
	private final String reachedMax;
	private final String alreadyExists;
	private final String createdNewPreset;
	private final String cantEditBinds;

	public PresetCommand(final PresetCommandConfig config) {
		super(config, "preset", "/bending preset <Bind/Create/Delete/List> [Preset]", config.Description, new String[] { "preset", "presets", "pre", "set", "p" });

		this.noPresets = config.NoPresets;
		this.noPresetName = config.NoPresetName;
		this.deletePreset = config.Delete;
		this.noPresetNameExternal = config.NoPresetName_External;
		this.bendingRemoved = ConfigManager.getConfig(CommandPropertiesConfig.class).BendingPermanentlyRemoved;
		this.bound = config.SuccessfullyBound;
		this.failedToBindAll = config.FailedToBindAll;
		this.bendingRemovedOther = ConfigManager.getConfig(CommandPropertiesConfig.class).BendingPermanentlyRemoved_Other;
		this.boundOtherConfirm = config.SuccessfullyBound_Other;
		this.succesfullyCopied = config.SuccessfullyCopied;
		this.reachedMax = config.MaxPresets;
		this.alreadyExists = config.AlreadyExists;
		this.createdNewPreset = config.Created;
		this.cantEditBinds = config.CantEditBinds;
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.isPlayer(sender) || !this.correctLength(sender, args.size(), 1, 3)) {
			return;
		} else if (MultiAbilityManager.hasMultiAbilityBound((Player) sender)) {
			GeneralMethods.sendBrandingMessage(sender, this.cantEditBinds);
			return;
		}

		final Player player = (Player) sender;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer == null) {
			GeneralMethods.createBendingPlayer(player.getUniqueId(), player.getName());
			bPlayer = BendingPlayer.getBendingPlayer(player);
		}

		// bending preset list.
		if (args.size() == 1) {
			if (Arrays.asList(listaliases).contains(args.get(0)) && this.hasPermission(sender, "list")) {
				boolean firstMessage = true;

				final List<Preset> presets = Preset.presets.get(player.getUniqueId());
				final List<String> presetNames = new ArrayList<String>();

				if (presets == null || presets.isEmpty()) {
					GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.noPresets);
					return;
				}

				for (final Preset preset : presets) {
					presetNames.add(preset.getName());
				}

				for (final String s : this.getPage(presetNames, ChatColor.GOLD + "Presets: ", 1, false)) {
					if (firstMessage) {
						GeneralMethods.sendBrandingMessage(sender, s);
						firstMessage = false;
					} else {
						sender.sendMessage(ChatColor.YELLOW + s);
					}
				}

				return;
			} else {
				this.help(sender, false);
				return;
			}
		}

		final String name = args.get(1);
		if (Arrays.asList(deletealiases).contains(args.get(0)) && this.hasPermission(sender, "delete")) { // bending preset delete name.
			if (!Preset.presetExists(player, name)) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.noPresetName);
				return;
			}

			final Preset preset = Preset.getPreset(player, name);
			preset.delete();
			GeneralMethods.sendBrandingMessage(sender, ChatColor.GREEN + this.deletePreset.replace("{name}", ChatColor.YELLOW + preset.getName() + ChatColor.GREEN));
			return;
		} else if (Arrays.asList(bindaliases).contains(args.get(0)) && this.hasPermission(sender, "bind")) { // bending preset bind name.
			if (args.size() < 3) {
				boolean boundAll = false;
				if (Preset.presetExists(player, name)) {
					final Preset preset = Preset.getPreset(player, name);

					GeneralMethods.sendBrandingMessage(sender, ChatColor.GREEN + this.bound.replace("{name}", ChatColor.YELLOW + preset.getName() + ChatColor.GREEN));
					boundAll = Preset.bindPreset(player, preset);

					if (!boundAll) {
						GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.failedToBindAll);
					}
				} else if (Preset.externalPresetExists(name) && this.hasPermission(sender, "bind.external")) {
					boundAll = Preset.bindExternalPreset(player, name);
				} else if (!Preset.externalPresetExists(name) && this.hasPermission(sender, "bind.external")) {
					GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.noPresetNameExternal);
					return;
				} else if (bPlayer.isPermaRemoved()) {
					GeneralMethods.sendBrandingMessage(player, ChatColor.RED + this.bendingRemoved);
					return;
				} else {
					GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.noPresetName);
					return;
				}

			} else if (this.hasPermission(sender, "bind.external.assign") && Preset.externalPresetExists(name)) {
				if (!Preset.externalPresetExists(name)) {
					GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.noPresetNameExternal);
					return;
				}

				final Player player2 = Bukkit.getPlayer(args.get(2));
				if (player2 != null && player2.isOnline()) {
					BendingPlayer bPlayer2 = BendingPlayer.getBendingPlayer(player2);

					if (bPlayer2 == null) {
						GeneralMethods.createBendingPlayer(player2.getUniqueId(), player2.getName());
						bPlayer2 = BendingPlayer.getBendingPlayer(player2);
					}
					if (bPlayer2.isPermaRemoved()) {
						GeneralMethods.sendBrandingMessage(player, ChatColor.RED + this.bendingRemovedOther);
						return;
					}
					final boolean boundAll = Preset.bindExternalPreset(player2, name);

					GeneralMethods.sendBrandingMessage(sender, ChatColor.GREEN + this.boundOtherConfirm.replace("{target}", ChatColor.YELLOW + player2.getName() + ChatColor.GREEN).replace("{name}", ChatColor.YELLOW + name + ChatColor.GREEN + ChatColor.YELLOW));
					GeneralMethods.sendBrandingMessage(player2, ChatColor.GREEN + this.bound.replace("{name}", ChatColor.YELLOW + name + ChatColor.GREEN));
					if (!boundAll) {
						GeneralMethods.sendBrandingMessage(player2, ChatColor.RED + this.failedToBindAll);
					}
					return;
				} else {
					GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + config.PlayerNotFound);
				}
			} else if (this.hasPermission(sender, "bind.assign") && Preset.presetExists(player, name)) {
				if (!Preset.presetExists(player, name)) {
					GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.noPresetName);
					return;
				}

				final Player player2 = Bukkit.getPlayer(args.get(2));
				if (player2 != null && player2.isOnline()) {
					BendingPlayer bPlayer2 = BendingPlayer.getBendingPlayer(player2);

					if (bPlayer2 == null) {
						GeneralMethods.createBendingPlayer(player2.getUniqueId(), player2.getName());
						bPlayer2 = BendingPlayer.getBendingPlayer(player2);
					}
					if (bPlayer2.isPermaRemoved()) {
						GeneralMethods.sendBrandingMessage(player, ChatColor.RED + this.bendingRemovedOther);
						return;
					}
					final Preset preset = Preset.getPreset(player, name);
					final boolean boundAll = Preset.bindPreset(player2, preset);

					GeneralMethods.sendBrandingMessage(sender, ChatColor.GREEN + this.boundOtherConfirm.replace("{target}", ChatColor.YELLOW + player2.getName() + ChatColor.GREEN).replace("{name}", ChatColor.YELLOW + name + ChatColor.GREEN + ChatColor.YELLOW));
					GeneralMethods.sendBrandingMessage(player2, ChatColor.GREEN + this.succesfullyCopied.replace("{target}", ChatColor.YELLOW + player.getName() + ChatColor.GREEN));
					if (!boundAll) {
						GeneralMethods.sendBrandingMessage(player2, ChatColor.RED + this.failedToBindAll);
					}
					return;
				} else {
					GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + config.PlayerNotFound);
				}
			}
		} else if (Arrays.asList(createaliases).contains(args.get(0)) && this.hasPermission(sender, "create")) { // bending preset create name.
			final int limit = GeneralMethods.getMaxPresets(player);

			if (Preset.presets.get(player.getUniqueId()) != null && Preset.presets.get(player.getUniqueId()).size() >= limit) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.reachedMax);
				return;
			} else if (Preset.presetExists(player, name)) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.alreadyExists);
				return;
			}

			if (bPlayer == null) {
				return;
			}
			String[] abilities = new String[9];
			for (int slot = 0; slot < 9; slot++) {
				abilities[slot] = bPlayer.getAbilities()[slot];
			}

			final Preset preset = new Preset(player.getUniqueId(), name, abilities);
			preset.save(player);
			GeneralMethods.sendBrandingMessage(sender, ChatColor.GREEN + this.createdNewPreset.replace("{name}", ChatColor.YELLOW + name + ChatColor.GREEN));
		} else {
			this.help(sender, false);
		}
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (args.size() >= 3 || !sender.hasPermission("bending.command.preset") || !(sender instanceof Player)) {
			return new ArrayList<String>();
		}
		final List<String> l = new ArrayList<String>();
		if (args.size() == 0) {
			l.add("create");
			l.add("delete");
			l.add("list");
			l.add("bind");
			return l;
		} else if (args.size() == 2 && Arrays.asList(new String[] { "delete", "d", "del", "bind", "b" }).contains(args.get(0).toLowerCase())) {
			final List<Preset> presets = Preset.presets.get(((Player) sender).getUniqueId());
			final List<String> presetNames = new ArrayList<String>();
			if (presets != null && presets.size() != 0) {
				for (final Preset preset : presets) {
					presetNames.add(preset.getName());
				}
			}
			if (sender.hasPermission("bending.command.preset.bind.external")) {
				if (Preset.externalPresets.keySet().size() > 0) {
					for (final String externalPreset : Preset.externalPresets.keySet()) {
						presetNames.add(externalPreset);
					}
				}
			}
			if (presetNames.size() == 0) {
				return new ArrayList<String>();
			}
		}
		return l;
	}
}

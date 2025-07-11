package com.projectkorra.projectkorra.command;

import java.util.*;
import java.util.stream.Collectors;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.object.Preset;

import javax.annotation.Nullable;

/**
 * Executor for /bending preset. Extends {@link PKCommand}.
 */
public class PresetCommand extends PKCommand {
	public enum PresetAction {
		CREATE("create", "c", "save"),
		DELETE("delete", "d", "del"),
		UPDATE("update", "u", "up"),
		LIST("list", "l", "li"),
		BIND("bind", "b");

		private final Set<String> aliases;

		PresetAction(String... aliases) {
			this.aliases = Arrays.stream(aliases).map(String::toLowerCase).collect(Collectors.toSet());
		}

		public boolean matches(String input) {
			return aliases.contains(input.toLowerCase());
		}

		public static Optional<PresetAction> fromInput(String input) {
			return Arrays.stream(values()).filter(action -> action.matches(input)).findFirst();
		}
	}

	private static final String INVALID_NAME = ".*[.,;:*'\"?=`<>+\\-\\[\\]{}^@!#$/\\\\%&()].*";

	private final String cantEditBindsMessage;
	private final String playerNotFoundMessage;
	private final String noPresetNameDefined;
	private final String noAbilitiesBoundMessage;
	private final String noPresetsMessage;
	private final String failedToBindAllMessage;
	private final String successfullyBoundMessage;
	private final String targetSuccessfullyBoundMessage;
	private final String noSuchPresetMessage;
	private final String deletePresetMessage;
	private final String databaseErrorMessage;
	private final String invalidNameMessage;
	private final String reachedMaxMessage;
	private final String alreadyExistsMessage;
	private final String createdNewPresetMessage;
	private final String updatedPresetMessage;
	private final String updatedPresetNewNameMessage;

	public PresetCommand() {
		super("preset", "/bending preset <Bind/Create/Delete/Update/List>", ConfigManager.languageConfig.get().getString("Commands.Preset.Description"), new String[] { "preset", "presets", "pre", "set", "p" });

		FileConfiguration languageConfig = ConfigManager.languageConfig.get();

		this.cantEditBindsMessage = languageConfig.getString("Commands.Preset.CantEditBinds", "You can't edit your binds right now!");
		this.playerNotFoundMessage = languageConfig.getString("Commands.Preset.PlayerNotFound", "Player not found.");
		this.noPresetNameDefined = languageConfig.getString("Commands.Preset.DefineName", "Define the preset.");
		this.noAbilitiesBoundMessage = languageConfig.getString("Commands.Preset.NoAbilitiesBound", "You don't have any abilities bound!");
		this.noPresetsMessage = languageConfig.getString("Commands.Preset.NoPresets", "You do not have any presets.");
		this.failedToBindAllMessage = languageConfig.getString("Commands.Preset.FailedToBindAll", "Some abilities were not bound due to missing elements.");
		this.successfullyBoundMessage = languageConfig.getString("Commands.Preset.SuccesfullyBound", "Your binds have been set to match the '{name}' preset.");
		this.targetSuccessfullyBoundMessage = languageConfig.getString("Commands.Preset.Other.SuccesfullyBoundConfirm", "The binds of '{target}' have been set to match the '{name}' preset.");
		this.noSuchPresetMessage = languageConfig.getString("Commands.Preset.NoSuchPreset", "There is no such preset.");
		this.deletePresetMessage = languageConfig.getString("Commands.Preset.Delete", "You have deleted your '{name}' preset.");
		this.databaseErrorMessage = languageConfig.getString("Commands.Preset.DatabaseError", "An error occurred while processing the preset '{name}'.");
		this.invalidNameMessage = languageConfig.getString("Commands.Preset.InvalidName", "You must enter a valid name for your preset.");
		this.reachedMaxMessage = languageConfig.getString("Commands.Preset.MaxPresets", "You've reached your maximum number of presets!");
		this.alreadyExistsMessage = languageConfig.getString("Commands.Preset.AlreadyExists", "A preset with that name already exists.");
		this.createdNewPresetMessage = languageConfig.getString("Commands.Preset.Created", "Created a new preset named '{name}'.");
		this.updatedPresetMessage = languageConfig.getString("Commands.Preset.Update", "Successfully updated the '{name}' preset.");
		this.updatedPresetNewNameMessage = languageConfig.getString("Commands.Preset.Update.NewName", "Successfully updated the '{name}' preset. New name: '{new}'.");
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.correctLength(sender, args.size(), 1, 3)) {
			return;
		} else if (sender instanceof Player player && MultiAbilityManager.hasMultiAbilityBound(player)) {
			ChatUtil.sendBrandingMessage(sender, this.cantEditBindsMessage);
			return;
		}

		if (!hasPermission(sender)) return;

		// CREATE OPTIONAL FROM FIRST ARG [Bind/Create/Delete..]
		Optional<PresetAction> actionOption = PresetAction.fromInput(args.getFirst());

		if (actionOption.isEmpty()) {
			this.help(sender, false);
			return;
		}

		switch (actionOption.get()) {
			case LIST -> {
				if (!hasPermission(sender, "list") || !(sender instanceof Player player)) return;

				int page = args.size() >= 2 ? parseInt(args.get(1)) : 1;
				Player target = player;

				if (args.size() >= 3 && hasPermission(sender, "list.others")) {
					Player specified = Bukkit.getPlayerExact(args.get(2));
					if (specified == null || !specified.isOnline()) {
						ChatUtil.sendBrandingMessage(sender, playerNotFoundMessage);
						return;
					} else {
						target = specified;
					}
				}

				listPresets(page, target, player);
			}

			case DELETE -> {
				if (!hasPermission(sender, "delete") || !(sender instanceof Player player)) return;

				if (args.size() <= 1) {
					ChatUtil.sendBrandingMessage(player, ChatColor.RED + noPresetNameDefined);
					return;
				}

				deletePreset(player, args.get(1));
			}

			case CREATE -> {
				if (!hasPermission(sender, "create") || !(sender instanceof Player player)) return;

				if (BendingPlayer.getBendingPlayer(player).getAbilities().isEmpty()) {
					ChatUtil.sendBrandingMessage(player, ChatColor.RED + noAbilitiesBoundMessage);
					return;
				}

				if (args.size() <= 1) {
					ChatUtil.sendBrandingMessage(player, ChatColor.RED + noPresetNameDefined);
					return;
				}

				createPreset(player, args.get(1));
			}

			case UPDATE -> {
				if (!hasPermission(sender, "update") || !(sender instanceof Player player)) return;

				if (BendingPlayer.getBendingPlayer(player).getAbilities().isEmpty()) {
					ChatUtil.sendBrandingMessage(player, ChatColor.RED + noAbilitiesBoundMessage);
					return;
				}

				if (args.size() < 2) {
					ChatUtil.sendBrandingMessage(player, ChatColor.RED + noPresetNameDefined);
					return;
				}

				String oldName = args.get(1);
				updatePreset(player, oldName, args.size() > 2 ? args.get(2) : oldName);
			}

			case BIND -> {
				if (!hasPermission(sender, "bind")) return;

				String presetName = args.get(1);

				// Admin bind | /bending preset bind [Preset] [Player]
				if (args.size() >= 3) {
					if (!hasPermission(sender, "bind.assign")) return;

					Player target = Bukkit.getPlayerExact(args.get(2));
					if (target == null || !target.isOnline()) {
						ChatUtil.sendBrandingMessage(sender, ChatColor.RED + playerNotFoundMessage);
						return;
					}

					bindPreset(target, presetName, sender);
					return;
				}

				// Self bind | /bending preset bind [Preset]
				if (!(sender instanceof Player player)) return;
				bindPreset(player, presetName, player);
			}
		}
	}

	/**
	 * Lists all player Presets
	 *
	 * @param page {@link Preset} List page
	 * @param player Player to check for
	 * @param receiver Player to send the list to
	 */
	private void listPresets(int page, Player player, Player receiver) {
		List<Preset> allPresets = Preset.presets.get(player.getUniqueId());

		if (allPresets == null || allPresets.isEmpty()) {
			ChatUtil.sendBrandingMessage(receiver, ChatColor.RED + noPresetsMessage);
			return;
		}

		List<String> presetNames = allPresets.stream().map(Preset::getName).toList();
		List<String> pageLines = getPage(presetNames, ChatColor.GOLD + "Presets: ", page, false);

		if (pageLines.isEmpty()) return;

		ChatUtil.sendBrandingMessage(receiver, pageLines.getFirst());
		pageLines.stream().skip(1).forEach(line -> receiver.sendMessage(ChatColor.YELLOW + line));
	}

	/**
	 * Bind {@link Preset} for Player
	 *
	 * @param target Player to bind {@link Preset} for
	 * @param presetName {@link Preset} to bind
	 * @param executor Message receiver
	 */
	private void bindPreset(Player target, String presetName, CommandSender executor) {
		boolean isAdmin = !target.getName().equals(executor.getName());

		if (BendingPlayer.getBendingPlayer(target).isPermaRemoved()) {
			if (isAdmin) {
				ChatUtil.sendBrandingMessage(executor, failedToBindAllMessage);
				return;
			}

			ChatUtil.sendBrandingMessage(target, failedToBindAllMessage);
			return;
		}

		if (Preset.presetExists(target, presetName)) {
			this.bindPlayerPreset(target, presetName, executor, isAdmin);
			return;
		}


		if (Preset.externalPresetExists(presetName)) {
			this.bindExternalPreset(target, presetName, executor, isAdmin);
			return;
		}

		if (isAdmin) {
			ChatUtil.sendBrandingMessage(executor, ChatColor.RED + noSuchPresetMessage);
		} else {
			ChatUtil.sendBrandingMessage(target, ChatColor.RED + noSuchPresetMessage);
		}
	}

	private void bindPlayerPreset(Player target, String presetName, CommandSender executor, boolean admin) {
		boolean boundAll = Preset.bindPreset(target, Preset.getPreset(target, presetName));

		ChatUtil.sendBrandingMessage(target, ChatColor.GREEN + successfullyBoundMessage.replace("{name}", ChatColor.YELLOW + presetName + ChatColor.GREEN));

		if (admin) {
			ChatUtil.sendBrandingMessage(executor, ChatColor.GREEN + targetSuccessfullyBoundMessage
					.replace("{target}", ChatColor.YELLOW + target.getName() + ChatColor.GREEN)
					.replace("{name}", ChatColor.YELLOW + presetName + ChatColor.GREEN));
		}

		if (!boundAll) {
			ChatUtil.sendBrandingMessage(target, ChatColor.RED + failedToBindAllMessage);
			if (admin) {
				ChatUtil.sendBrandingMessage(executor, ChatColor.RED + failedToBindAllMessage);
			}
		}
	}

	private void bindExternalPreset(Player target, String presetName, CommandSender executor, boolean admin) {
		if (!admin && !hasPermission(target, "bind.external")) return;
		boolean boundAll = Preset.bindExternalPreset(target, presetName);

		ChatUtil.sendBrandingMessage(target, ChatColor.GREEN + successfullyBoundMessage.replace("{name}", ChatColor.YELLOW + presetName + ChatColor.GREEN));

		if (admin) {
			ChatUtil.sendBrandingMessage(executor, ChatColor.GREEN + targetSuccessfullyBoundMessage
					.replace("{target}", ChatColor.YELLOW + target.getName() + ChatColor.GREEN)
					.replace("{name}", ChatColor.YELLOW + presetName + ChatColor.GREEN));
		}

		if (!boundAll) {
			ChatUtil.sendBrandingMessage(target, ChatColor.RED + failedToBindAllMessage);
			if (admin) {
				ChatUtil.sendBrandingMessage(executor, ChatColor.RED + failedToBindAllMessage);
			}
		}
	}

	/**
	 * Delete {@link Preset} for Player
	 *
	 * @param player Player to delete {@link Preset} for
	 * @param presetName {@link Preset} to delete
	 */
	private void deletePreset(Player player, String presetName) {
		Preset preset = Preset.getPreset(player, presetName);
		if (preset == null || !Preset.presetExists(player, presetName)) {
			ChatUtil.sendBrandingMessage(player, ChatColor.RED + noSuchPresetMessage);
			return;
		}

        preset.delete().thenAccept(success -> {
			if (success) {
				ChatUtil.sendBrandingMessage(player, ChatColor.GREEN + deletePresetMessage.replace("{name}", ChatColor.YELLOW + preset.getName() + ChatColor.GREEN));
			} else {
				ChatUtil.sendBrandingMessage(player, ChatColor.RED + databaseErrorMessage.replace("{name}", ChatColor.YELLOW + preset.getName() + ChatColor.RED));
			}
		}).exceptionally(e -> {
			ProjectKorra.log.severe("Failed to delete preset for " + player.getName() + "!" + e.getMessage());
			return null;
		});
	}

	/**
	 * Create a new {@link Preset}
	 *
	 * @param player Creator of {@link Preset}
	 * @param presetName Name of {@link Preset}
	 */
	private void createPreset(Player player, String presetName) {
		// PRESET NAME VALID CHECK
		if (presetName == null || presetName.isBlank() || presetName.matches(INVALID_NAME)) {
			ChatUtil.sendBrandingMessage(player, ChatColor.RED + invalidNameMessage);
			return;
		}

		// MAX PRESET LIMIT REACHED CHECK
		if (Preset.presets.get(player.getUniqueId()) != null && Preset.presets.get(player.getUniqueId()).size() >= GeneralMethods.getMaxPresets(player)) {
			ChatUtil.sendBrandingMessage(player, ChatColor.RED + reachedMaxMessage);
			return;
		}

		// PRESET ALREADY EXISTS CHECK
		if (Preset.presetExists(player, presetName)) {
			ChatUtil.sendBrandingMessage(player, ChatColor.RED + alreadyExistsMessage);
			return;
		}

		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null || !bPlayer.isOnline()) {
			return;
		}

		// CREATE AND REGISTER PRESET
		Preset preset = new Preset(player.getUniqueId(), presetName, new HashMap<>(bPlayer.getAbilities()));

		// STORE PRESET IN DATABASE
		preset.save().thenAccept(b -> {
			if (b) {
				ChatUtil.sendBrandingMessage(player, ChatColor.GREEN + createdNewPresetMessage.replace("{name}", ChatColor.YELLOW + presetName + ChatColor.GREEN));
			} else {
				ChatUtil.sendBrandingMessage(player, ChatColor.RED + databaseErrorMessage.replace("{name}", ChatColor.YELLOW + presetName + ChatColor.RED));
			}
		}).exceptionally(e -> {
			ProjectKorra.log.severe("Failed to create preset!" + e.getMessage());
			return null;
		});
	}

	/**
	 * Updates a Players {@link Preset}. Can assign new name or keep old name
	 * to update binds for existing {@link Preset}
	 *
	 * @param player Player to update Preset for
	 * @param oldName Preset to edit
	 * @param newName New name for existing Preset
	 */
	private void updatePreset(Player player, String oldName, @Nullable String newName) {
		// PRESET EXISTS CHECK
		if (!Preset.presetExists(player, oldName)) {
			ChatUtil.sendBrandingMessage(player, ChatColor.RED + noSuchPresetMessage);
			return;
		}

		// BUILD FINAL NAME
		String finalName = (newName == null || newName.isBlank()) ? oldName : newName;

		// MAKE SURE THE NEW NAME ISN'T AN ALREADY EXISTING PRESET
		if (!finalName.equalsIgnoreCase(oldName) && Preset.presetExists(player, finalName)) {
			ChatUtil.sendBrandingMessage(player, ChatColor.RED + alreadyExistsMessage.replace("{name}", ChatColor.YELLOW + finalName + ChatColor.RED));
			return;
		}

		// GET PRESET TO UPDATE
		Preset oldPreset = Preset.getPreset(player, oldName);
		if (oldPreset == null) {
			ChatUtil.sendBrandingMessage(player, ChatColor.RED + noSuchPresetMessage);
			return;
		}

		if (newName == null || newName.isBlank() || newName.equals(oldName)) {
			oldPreset.update(new HashMap<>(BendingPlayer.getBendingPlayer(player).getAbilities())).thenAccept(success -> {
				if (success) {
					ChatUtil.sendBrandingMessage(player, ChatColor.GREEN + updatedPresetMessage.replace("{name}", ChatColor.YELLOW + oldName + ChatColor.GREEN));
				} else {
					ChatUtil.sendBrandingMessage(player, ChatColor.RED + databaseErrorMessage.replace("{name}", ChatColor.YELLOW + oldName + ChatColor.RED));
				}
			}).exceptionally(e -> {
				ProjectKorra.log.severe("Failed to update Preset!" + e.getMessage());
				return null;
			});
		} else {
			// DELETE PRESET TO UPDATE FROM MAP AND DB
			oldPreset.delete().thenAccept(success -> {
				if (!success) {
					ChatUtil.sendBrandingMessage(player, ChatColor.RED + databaseErrorMessage.replace("{name}", ChatColor.YELLOW + oldName + ChatColor.RED));
					return;
				}

				// CREATE NEW (UPDATED) PRESET AND PUT IN DB
				Preset newPreset = new Preset(player.getUniqueId(), finalName, new HashMap<>(BendingPlayer.getBendingPlayer(player).getAbilities()));
				newPreset.save().thenAccept(saved -> {
					if (saved) {
						ChatUtil.sendBrandingMessage(player, ChatColor.GREEN + updatedPresetNewNameMessage
								.replace("{name}", ChatColor.YELLOW + oldName + ChatColor.GREEN)
								.replace("{new}", ChatColor.YELLOW + newName + ChatColor.GREEN));
					} else {
						ChatUtil.sendBrandingMessage(player, ChatColor.RED + databaseErrorMessage.replace("{name}", ChatColor.YELLOW + finalName + ChatColor.RED));
					}
				}).exceptionally(e -> {
					ProjectKorra.log.severe("Failed to Update Preset (SAVE)!" + e.getMessage());
					return null;
				});
			}).exceptionally(e -> {
				ProjectKorra.log.severe("Failed to Update Preset (DELETE)!" + e.getMessage());
				return null;
			});
		}
	}

	private int parseInt(String string) {
		try {
			return Integer.parseInt(string);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (!hasPermission(sender) || !(sender instanceof Player player)) {
			return Collections.emptyList();
		}

		if (args.isEmpty()) {
			return Arrays.stream(PresetAction.values())
					.map(action -> action.name().toLowerCase())
					.toList();
		}

		Optional<PresetAction> actionOpt = PresetAction.fromInput(args.getFirst().toLowerCase());

		if (args.size() == 1) {
			if (actionOpt.isEmpty()) {
				return Arrays.stream(PresetAction.values())
						.map(action -> action.name().toLowerCase())
						.filter(s -> s.startsWith(args.getFirst().toLowerCase()))
						.toList();
			}

			PresetAction action = actionOpt.get();

			if (action == PresetAction.LIST) {
				return List.of("<Page>");
			}

			if (action == PresetAction.CREATE) {
				return List.of("<Name>");
			}

			if (action == PresetAction.UPDATE) {
				return getTabPresetSuggestions(player, false);
			}

			if (action == PresetAction.DELETE || action == PresetAction.BIND) {
				boolean includeExternal = (action == PresetAction.BIND && sender.hasPermission("bending.command." + this.getName() + ".bind.external")); // Not using PKCommand.hasPermission() here because it will print a no perms msg when tab completing
				return getTabPresetSuggestions(player, includeExternal);
			}
		}

		if (args.size() == 2) {
			if (actionOpt.isEmpty()) return Collections.emptyList();
			PresetAction action = actionOpt.get();

			if (action == PresetAction.BIND || action == PresetAction.LIST) {
				return getOnlinePlayerNames(sender);
			}

			if (action == PresetAction.UPDATE) {
				return List.of("<NewName>");
			}
		}

		return Collections.emptyList();
	}

	private List<String> getTabPresetSuggestions(Player player, boolean includeExternal) {
		List<String> presets = Preset.presets.getOrDefault(player.getUniqueId(), List.of())
				.stream()
				.map(Preset::getName)
				.distinct()
				.sorted()
				.collect(Collectors.toCollection(ArrayList::new));

		if (includeExternal) {
			presets.addAll(Preset.externalPresets.keySet());
		}

		return presets;
	}
}

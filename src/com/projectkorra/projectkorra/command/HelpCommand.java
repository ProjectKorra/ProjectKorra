package com.projectkorra.projectkorra.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.api.AddonAbility;
import com.projectkorra.projectkorra.ability.api.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.api.PassiveAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.commands.HelpCommandConfig;
import com.projectkorra.projectkorra.configuration.configs.properties.AirPropertiesConfig;
import com.projectkorra.projectkorra.configuration.configs.properties.AvatarPropertiesConfig;
import com.projectkorra.projectkorra.configuration.configs.properties.ChiPropertiesConfig;
import com.projectkorra.projectkorra.configuration.configs.properties.EarthPropertiesConfig;
import com.projectkorra.projectkorra.configuration.configs.properties.FirePropertiesConfig;
import com.projectkorra.projectkorra.configuration.configs.properties.WaterPropertiesConfig;

/**
 * Executor for /bending help. Extends {@link PKCommand}.
 */
@SuppressWarnings("rawtypes")
public class HelpCommand extends PKCommand<HelpCommandConfig> {

	private final String required;
	private final String optional;
	private final String properUsage;
	private final String learnMore;
	private final String air;
	private final String water;
	private final String earth;
	private final String fire;
	private final String chi;
	private final String avatar;
	private final String invalidTopic;
	private final String usage;
	private final String rpgUsage;
	private final String spiritsUsage;
	private final String itemsUsage;

	public HelpCommand(final HelpCommandConfig config) {
		super(config, "help", "/bending help <Page/Topic>", config.Description, new String[] { "help", "h" });

		this.required = config.Required;
		this.optional = config.Optional;
		this.properUsage = config.ProperUsage;
		this.learnMore = config.LearnMore;
		this.air = ConfigManager.getConfig(AirPropertiesConfig.class).Description;
		this.water = ConfigManager.getConfig(WaterPropertiesConfig.class).Description;
		this.earth = ConfigManager.getConfig(EarthPropertiesConfig.class).Description;
		this.fire = ConfigManager.getConfig(FirePropertiesConfig.class).Description;
		this.chi = ConfigManager.getConfig(ChiPropertiesConfig.class).Description;
		this.avatar = ConfigManager.getConfig(AvatarPropertiesConfig.class).Description;
		this.invalidTopic = config.InvalidTopic;
		this.usage = config.Usage;
		this.rpgUsage = config.RPGUsage;
		this.spiritsUsage = config.SpiritsUsage;
		this.itemsUsage = config.ItemsUsage;
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		boolean firstMessage = true;

		if (!this.hasPermission(sender) || !this.correctLength(sender, args.size(), 0, 1)) {
			return;
		} else if (args.size() == 0) {
			final List<String> strings = new ArrayList<String>();
			for (final PKCommand command : instances.values()) {
				if (!command.getName().equalsIgnoreCase("help") && sender.hasPermission("bending.command." + command.getName())) {
					strings.add(command.getProperUse());
				}
			}
			if (GeneralMethods.hasItems()) {
				strings.add(this.itemsUsage);
			}
			if (GeneralMethods.hasRPG()) {
				strings.add(this.rpgUsage);
			}
			if (GeneralMethods.hasSpirits()) {
				strings.add(this.spiritsUsage);
			}
			Collections.sort(strings);
			Collections.reverse(strings);
			strings.add(instances.get("help").getProperUse());
			Collections.reverse(strings);

			for (final String s : this.getPage(strings, ChatColor.GOLD + "Commands: <" + this.required + "> [" + this.optional + "]", 1, false)) {
				if (firstMessage) {
					GeneralMethods.sendBrandingMessage(sender, s);
					firstMessage = false;
				} else {
					sender.sendMessage(ChatColor.YELLOW + s);
				}
			}
			return;
		}

		final String arg = args.get(0).toLowerCase();

		if (this.isNumeric(arg)) {
			final List<String> strings = new ArrayList<String>();
			for (final PKCommand command : instances.values()) {
				strings.add(command.getProperUse());
			}
			if (GeneralMethods.hasItems()) {
				strings.add(this.itemsUsage);
			}
			if (GeneralMethods.hasRPG()) {
				strings.add(this.rpgUsage);
			}
			if (GeneralMethods.hasSpirits()) {
				strings.add(this.spiritsUsage);
			}
			for (final String s : this.getPage(strings, ChatColor.GOLD + "Commands: <" + this.required + "> [" + this.optional + "]", Integer.valueOf(arg), true)) {
				if (firstMessage) {
					GeneralMethods.sendBrandingMessage(sender, s);
					firstMessage = false;
				} else {
					sender.sendMessage(ChatColor.YELLOW + s);
				}
			}
		} else if (instances.keySet().contains(arg)) {// bending help command.
			instances.get(arg).help(sender, true);
		} else if (Arrays.asList(Commands.comboaliases).contains(arg)) { // bending help elementcombo.
			sender.sendMessage(ChatColor.GOLD + this.properUsage.replace("{command1}", ChatColor.RED + "/bending display " + arg + ChatColor.GOLD).replace("{command2}", ChatColor.RED + "/bending help <Combo Name>" + ChatColor.GOLD));
		} else if (Arrays.asList(Commands.passivealiases).contains(arg)) { // bending help elementpassive.
			sender.sendMessage(ChatColor.GOLD + this.properUsage.replace("{command1}", ChatColor.RED + "/bending display " + arg + ChatColor.GOLD).replace("{command2}", ChatColor.RED + "/bending help <Passive Name>" + ChatColor.RED));
		} else if (CoreAbility.getAbility(arg) != null && !(CoreAbility.getAbility(arg) instanceof ComboAbility) && CoreAbility.getAbility(arg).isEnabled() && !CoreAbility.getAbility(arg).isHiddenAbility() || CoreAbility.getAbility(arg) instanceof PassiveAbility) { // bending help ability.
			final CoreAbility ability = CoreAbility.getAbility(arg);
			final ChatColor color = ability.getElement().getColor();

			if (ability instanceof AddonAbility) {
				if (ability instanceof PassiveAbility) {
					sender.sendMessage(color + (ChatColor.BOLD + ability.getName()) + ChatColor.WHITE + " (Addon Passive)");
				} else {
					sender.sendMessage(color + (ChatColor.BOLD + ability.getName()) + ChatColor.WHITE + " (Addon)");
				}

				sender.sendMessage(color + ability.getDescription());

				if (!ability.getInstructions().isEmpty()) {
					sender.sendMessage(ChatColor.WHITE + this.usage + ability.getInstructions());
				}

				final AddonAbility abil = (AddonAbility) CoreAbility.getAbility(arg);
				sender.sendMessage(color + "- By: " + ChatColor.WHITE + abil.getAuthor());
				sender.sendMessage(color + "- Version: " + ChatColor.WHITE + abil.getVersion());
			} else {
				if (ability instanceof PassiveAbility) {
					sender.sendMessage(color + (ChatColor.BOLD + ability.getName()) + ChatColor.WHITE + " (Passive)");
				} else {
					sender.sendMessage(color + (ChatColor.BOLD + ability.getName()));
				}

				sender.sendMessage(color + ability.getDescription());

				if (!ability.getInstructions().isEmpty()) {
					sender.sendMessage(ChatColor.WHITE + this.usage + ability.getInstructions());
				}
			}
		} else if (Arrays.asList(Commands.airaliases).contains(arg)) {
			sender.sendMessage(Element.AIR.getColor() + this.air.replace("/b display Air", Element.AIR.getSubColor() + "/b display Air" + Element.AIR.getColor()));
			sender.sendMessage(ChatColor.YELLOW + this.learnMore + ChatColor.DARK_AQUA + "http://projectkorra.com/");
		} else if (Arrays.asList(Commands.wateraliases).contains(arg)) {
			sender.sendMessage(Element.WATER.getColor() + this.water.replace("/b display Water", Element.WATER.getSubColor() + "/b display Water" + Element.WATER.getColor()));
			sender.sendMessage(ChatColor.YELLOW + this.learnMore + ChatColor.DARK_AQUA + "http://projectkorra.com/");
		} else if (Arrays.asList(Commands.earthaliases).contains(arg)) {
			sender.sendMessage(Element.EARTH.getColor() + this.earth.replace("/b display Earth", Element.EARTH.getSubColor() + "/b display Earth" + Element.EARTH.getColor()));
			sender.sendMessage(ChatColor.YELLOW + this.learnMore + ChatColor.DARK_AQUA + "http://projectkorra.com/");
		} else if (Arrays.asList(Commands.firealiases).contains(arg)) {
			sender.sendMessage(Element.FIRE.getColor() + this.fire.replace("/b display Fire", Element.FIRE.getSubColor() + "/b display Fire" + Element.FIRE.getColor()));
			sender.sendMessage(ChatColor.YELLOW + this.learnMore + ChatColor.DARK_AQUA + "http://projectkorra.com/");
		} else if (Arrays.asList(Commands.chialiases).contains(arg)) {
			sender.sendMessage(Element.CHI.getColor() + this.chi.replace("/b display Chi", Element.CHI.getSubColor() + "/b display Chi" + Element.CHI.getColor()));
			sender.sendMessage(ChatColor.YELLOW + this.learnMore + ChatColor.DARK_AQUA + "http://projectkorra.com/");
		} else if (Arrays.asList(Commands.avataraliases).contains(arg)) {
			sender.sendMessage(Element.AVATAR.getColor() + this.avatar.replace("/b display Avatar", Element.AVATAR.getSubColor() + "/b display Avatar" + Element.AVATAR.getColor()));
			sender.sendMessage(ChatColor.YELLOW + this.learnMore + ChatColor.DARK_AQUA + "http://projectkorra.com/");
		} else {
			// combos - handled differently because they're stored in CamelCase in ComboManager.
			for (final String combo : ComboManager.getDescriptions().keySet()) {
				if (combo.equalsIgnoreCase(arg)) {
					final CoreAbility ability = CoreAbility.getAbility(combo);
					final ChatColor color = ability != null ? ability.getElement().getColor() : null;

					if (ability instanceof AddonAbility) {
						sender.sendMessage(color + (ChatColor.BOLD + ability.getName()) + ChatColor.WHITE + " (Addon Combo)");
						sender.sendMessage(color + ability.getDescription());

						if (!ability.getInstructions().isEmpty()) {
							sender.sendMessage(ChatColor.WHITE + this.usage + ability.getInstructions());
						}

						final AddonAbility abil = (AddonAbility) CoreAbility.getAbility(arg);
						sender.sendMessage(color + "- By: " + ChatColor.WHITE + abil.getAuthor());
						sender.sendMessage(color + "- Version: " + ChatColor.WHITE + abil.getVersion());
					} else {
						sender.sendMessage(color + (ChatColor.BOLD + ability.getName()) + ChatColor.WHITE + " (Combo)");
						sender.sendMessage(color + ComboManager.getDescriptions().get(combo));
						sender.sendMessage(ChatColor.WHITE + this.usage + ComboManager.getInstructions().get(combo));
					}

					return;
				}
			}

			sender.sendMessage(ChatColor.RED + this.invalidTopic);
		}
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (args.size() >= 1 || !sender.hasPermission("bending.command.help")) {
			return new ArrayList<String>();
		}

		final List<String> list = new ArrayList<String>();
		for (final Element e : Element.getAllElements()) {
			list.add(e.getName());
		}

		final List<String> abils = new ArrayList<String>();
		for (final CoreAbility coreAbil : CoreAbility.getAbilities()) {
			if (!(sender instanceof Player) && (!coreAbil.isHiddenAbility()) && coreAbil.isEnabled() && !abils.contains(coreAbil.getName())) {
				abils.add(coreAbil.getName());
			} else if (sender instanceof Player) {
				if ((!coreAbil.isHiddenAbility()) && coreAbil.isEnabled() && !abils.contains(coreAbil.getName())) {
					abils.add(coreAbil.getName());
				}
			}
		}

		Collections.sort(abils);
		list.addAll(abils);
		return list;
	}
}

package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.info.AbilityInfo;
import com.projectkorra.projectkorra.ability.api.AddonAbilityInfo;
import com.projectkorra.projectkorra.ability.api.ComboAbilityInfo;
import com.projectkorra.projectkorra.ability.api.PassiveAbilityInfo;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.commands.HelpCommandConfig;
import com.projectkorra.projectkorra.configuration.configs.properties.*;
import com.projectkorra.projectkorra.element.Element;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.*;

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
		AbilityInfo abilityInfo = this.abilityManager.getAbilityInfo(arg);

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
		} else if (abilityInfo != null && !(abilityInfo instanceof ComboAbilityInfo) && !abilityInfo.isHidden() || abilityInfo instanceof PassiveAbilityInfo) { // bending help ability.
			final ChatColor color = abilityInfo.getElement().getColor();

			if (abilityInfo instanceof AddonAbilityInfo) {
				if (abilityInfo instanceof PassiveAbilityInfo) {
					sender.sendMessage(color + (ChatColor.BOLD + abilityInfo.getName()) + ChatColor.WHITE + " (Addon Passive)");
				} else {
					sender.sendMessage(color + (ChatColor.BOLD + abilityInfo.getName()) + ChatColor.WHITE + " (Addon)");
				}

				sender.sendMessage(color + abilityInfo.getDescription());

				if (!abilityInfo.getInstructions().isEmpty()) {
					sender.sendMessage(ChatColor.WHITE + this.usage + abilityInfo.getInstructions());
				}

				final AddonAbilityInfo addonAbilityInfo = (AddonAbilityInfo) abilityInfo;
				sender.sendMessage(color + "- By: " + ChatColor.WHITE + addonAbilityInfo.getAuthor());
				sender.sendMessage(color + "- Version: " + ChatColor.WHITE + addonAbilityInfo.getVersion());
			} else {
				if (abilityInfo instanceof PassiveAbilityInfo) {
					sender.sendMessage(color + (ChatColor.BOLD + abilityInfo.getName()) + ChatColor.WHITE + " (Passive)");
				} else {
					sender.sendMessage(color + (ChatColor.BOLD + abilityInfo.getName()));
				}

				sender.sendMessage(color + abilityInfo.getDescription());

				if (!abilityInfo.getInstructions().isEmpty()) {
					sender.sendMessage(ChatColor.WHITE + this.usage + abilityInfo.getInstructions());
				}
			}
		} else if (Arrays.asList(Commands.airaliases).contains(arg)) {
			Element air = this.elementManager.getAir();

			sender.sendMessage(air.getColor() + this.air.replace("/b display Air", air.getSecondaryColor() + "/b display Air" + air.getColor()));
			sender.sendMessage(ChatColor.YELLOW + this.learnMore + ChatColor.DARK_AQUA + "http://projectkorra.com/");
		} else if (Arrays.asList(Commands.wateraliases).contains(arg)) {
			Element water = this.elementManager.getWater();

			sender.sendMessage(water.getColor() + this.water.replace("/b display Water", water.getSecondaryColor() + "/b display Water" + water.getColor()));
			sender.sendMessage(ChatColor.YELLOW + this.learnMore + ChatColor.DARK_AQUA + "http://projectkorra.com/");
		} else if (Arrays.asList(Commands.earthaliases).contains(arg)) {
			Element earth = this.elementManager.getEarth();

			sender.sendMessage(earth.getColor() + this.earth.replace("/b display Earth", earth.getSecondaryColor() + "/b display Earth" + earth.getColor()));
			sender.sendMessage(ChatColor.YELLOW + this.learnMore + ChatColor.DARK_AQUA + "http://projectkorra.com/");
		} else if (Arrays.asList(Commands.firealiases).contains(arg)) {
			Element fire = this.elementManager.getFire();

			sender.sendMessage(fire.getColor() + this.fire.replace("/b display Fire", fire.getSecondaryColor() + "/b display Fire" + fire.getColor()));
			sender.sendMessage(ChatColor.YELLOW + this.learnMore + ChatColor.DARK_AQUA + "http://projectkorra.com/");
		} else if (Arrays.asList(Commands.chialiases).contains(arg)) {
			Element chi = this.elementManager.getChi();

			sender.sendMessage(chi.getColor() + this.chi.replace("/b display Chi", chi.getSecondaryColor() + "/b display Chi" + chi.getColor()));
			sender.sendMessage(ChatColor.YELLOW + this.learnMore + ChatColor.DARK_AQUA + "http://projectkorra.com/");
		} else if (Arrays.asList(Commands.avataraliases).contains(arg)) {
			Element avatar = this.elementManager.getAvatar();

			sender.sendMessage(avatar.getColor() + this.avatar.replace("/b display Avatar", avatar.getSecondaryColor() + "/b display Avatar" + avatar.getColor()));
			sender.sendMessage(ChatColor.YELLOW + this.learnMore + ChatColor.DARK_AQUA + "http://projectkorra.com/");
		} else {
			ComboAbilityInfo comboAbilityInfo =  this.comboAbilityManager.getAbility(arg);

			if (comboAbilityInfo == null) {
				sender.sendMessage(ChatColor.RED + this.invalidTopic);
				return;
			}

			final ChatColor color = comboAbilityInfo.getElement().getColor();

			if (comboAbilityInfo instanceof AddonAbilityInfo) {
				sender.sendMessage(color + (ChatColor.BOLD + comboAbilityInfo.getName()) + ChatColor.WHITE + " (Addon Combo)");
				sender.sendMessage(color + comboAbilityInfo.getDescription());

				if (!comboAbilityInfo.getInstructions().isEmpty()) {
					sender.sendMessage(ChatColor.WHITE + this.usage + comboAbilityInfo.getInstructions());
				}

				final AddonAbilityInfo addonAbilityInfo = (AddonAbilityInfo) comboAbilityInfo;
				sender.sendMessage(color + "- By: " + ChatColor.WHITE + addonAbilityInfo.getAuthor());
				sender.sendMessage(color + "- Version: " + ChatColor.WHITE + addonAbilityInfo.getVersion());
			} else {
				sender.sendMessage(color + (ChatColor.BOLD + comboAbilityInfo.getName()) + ChatColor.WHITE + " (Combo)");
				sender.sendMessage(color + comboAbilityInfo.getDescription());
				sender.sendMessage(ChatColor.WHITE + this.usage + comboAbilityInfo.getInstructions());
			}
		}
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (args.size() >= 1 || !sender.hasPermission("bending.command.help")) {
			return new ArrayList<>();
		}

		final List<String> list = new ArrayList<>();
		for (final Element e : this.elementManager.getElements()) {
			list.add(e.getName());
		}

		final Set<String> abilitySet = new HashSet<>();

		for (AbilityInfo abilityInfo : this.abilityManager.getAbilityInfo()) {
			if (!abilityInfo.isHidden()) {
				abilitySet.add(abilityInfo.getName());
			}
		}

		List<String> abilityList = new ArrayList<>(abilitySet);
		Collections.sort(abilityList);
		list.addAll(abilityList);

		return list;
	}
}

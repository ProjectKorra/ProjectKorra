package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ChatUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Executor for /bending help. Extends {@link PKCommand}.
 */
public class HelpCommand extends PKCommand {

	private final String required;
	private final String optional;
	private final String properUsage;
	private final Map<Element, String> elementHelp = new HashMap<>();
	private final String invalidTopic;
	private final String usage;
	private final String slotFormat;
	private final String bindStart;
	private final String bindSeperator;
	private final String bindEnd;
	private final String hoverBind;
	private final boolean enableQuickBind;

	public HelpCommand() {
		super("help", "/bending help <Page/Topic>", ConfigManager.languageConfig.get().getString("Commands.Help.Description"), new String[] { "help", "h" });

		this.required = ConfigManager.languageConfig.get().getString("Commands.Help.Required");
		this.optional = ConfigManager.languageConfig.get().getString("Commands.Help.Optional");
		this.properUsage = ConfigManager.languageConfig.get().getString("Commands.Help.ProperUsage");
		this.invalidTopic = ConfigManager.languageConfig.get().getString("Commands.Help.InvalidTopic");
		this.usage = ConfigManager.languageConfig.get().getString("Commands.Help.Usage");
		this.slotFormat = ConfigManager.languageConfig.get().getString("Commands.Help.SlotFormat");
		this.bindStart = ConfigManager.languageConfig.get().getString("Commands.Help.BindStart");
		this.bindSeperator = ConfigManager.languageConfig.get().getString("Commands.Help.BindSeparator");
		this.bindEnd = ConfigManager.languageConfig.get().getString("Commands.Help.BindEnd");
		this.hoverBind = ConfigManager.languageConfig.get().getString("Commands.Help.HoverBind");
		this.enableQuickBind = ConfigManager.languageConfig.get().getBoolean("Commands.Help.EnableQuickBind");

		elementHelp.clear();

		for (Element e : Element.getAllElements()) {
			if (ConfigManager.languageConfig.get().contains("Commands.Help.Elements." + e.getName())) {
				elementHelp.put(e, ChatUtil.multiline(ChatUtil.color(ConfigManager.languageConfig.get().getString("Commands.Help.Elements." + e.getName()))));
			}
		}
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		boolean firstMessage = true;

		if (!this.hasPermission(sender) || !this.correctLength(sender, args.size(), 0, 1)) {
			return;
		} else if (args.isEmpty()) {
			final List<String> strings = new ArrayList<>();
			for (final PKCommand command : instances.values()) {
				if (!command.getName().equalsIgnoreCase("help") && sender.hasPermission("bending.command." + command.getName())) {
					strings.add(command.getProperUse());
				}
			}
			
			Collections.sort(strings);
			Collections.reverse(strings);
			strings.add(instances.get("help").getProperUse());
			Collections.reverse(strings);

			for (final String s : this.getPage(strings, ChatColor.GOLD + "Commands: <" + this.required + "> [" + this.optional + "]", 1, false)) {
				if (firstMessage) {
					ChatUtil.sendBrandingMessage(sender, s);
					firstMessage = false;
				} else {
					sender.sendMessage(ChatColor.YELLOW + s);
				}
			}
			return;
		}

		final String arg = args.get(0).toLowerCase();

		if (this.isNumeric(arg)) {
			final List<String> strings = new ArrayList<>();
			for (final PKCommand command : instances.values()) {
				strings.add(command.getProperUse());
			}
			
			for (final String s : this.getPage(strings, ChatColor.GOLD + "Commands: <" + this.required + "> [" + this.optional + "]", Integer.valueOf(arg), true)) {
				if (firstMessage) {
					ChatUtil.sendBrandingMessage(sender, s);
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
			final boolean isAddonAbility = ability instanceof AddonAbility;
			final boolean isPassiveAbility = ability instanceof PassiveAbility;
			
			
			sender.sendMessage(color + (ChatColor.BOLD + ability.getName()) + ChatColor.WHITE + (isAddonAbility ? (isPassiveAbility ? "(Addon Passive)" : " (Addon)") : (isPassiveAbility ? "(Passive)" : "")));
			sender.sendMessage(color + ability.getDescription());
			
			if (!ability.getInstructions().isEmpty()) {
				sender.sendMessage(ChatColor.WHITE + this.usage + ability.getInstructions());
			}
			
			if (!isPassiveAbility && sender instanceof Player && sender.hasPermission("bending.ability." + arg) && this.enableQuickBind) {
				final ComponentBuilder bindShortcut = new ComponentBuilder();
				bindShortcut.appendLegacy(ChatUtil.color(this.bindStart));
				bindShortcut.append("", ComponentBuilder.FormatRetention.NONE);

				for (int i = 1; i <= 9; i++) {
					if (i > 1) {
						bindShortcut.appendLegacy(ChatUtil.color(this.bindSeperator));
						bindShortcut.append("", ComponentBuilder.FormatRetention.NONE);
					}
					
					bindShortcut.appendLegacy(ChatUtil.color(this.slotFormat.replace("{slot}", String.valueOf(i)).replace("{element_color}", color.toString())));
					bindShortcut.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder().appendLegacy( ChatColor.WHITE + this.hoverBind.replace("{ability}", color + ability.getName() + ChatColor.WHITE).replace("{slot}", color + String.valueOf(i) + ChatColor.WHITE)).create())));
					bindShortcut.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bending bind " + ability.getName() + " " + i));
					bindShortcut.append("", ComponentBuilder.FormatRetention.NONE);
				}

				bindShortcut.appendLegacy(ChatUtil.color(this.bindEnd));
				sender.spigot().sendMessage(bindShortcut.create());
			}
			
			if (isAddonAbility) {
				final AddonAbility addonAbility = (AddonAbility) ability;
				sender.sendMessage(color + "- By: " + ChatColor.WHITE + addonAbility.getAuthor());
				sender.sendMessage(color + "- Version: " + ChatColor.WHITE + addonAbility.getVersion());
			}
		} else if (Arrays.asList(Commands.airaliases).contains(arg)) {
			sender.sendMessage(Element.AIR.getColor() + this.elementHelp.get(Element.AIR).replaceAll("(?i)/b display air", Element.AIR.getSubColor() + "/b display air" + Element.AIR.getColor()));
		} else if (Arrays.asList(Commands.wateraliases).contains(arg)) {
			sender.sendMessage(Element.WATER.getColor() + this.elementHelp.get(Element.WATER).replaceAll("(?i)/b display water", Element.WATER.getSubColor() + "/b display water" + Element.WATER.getColor()));
		} else if (Arrays.asList(Commands.earthaliases).contains(arg)) {
			sender.sendMessage(Element.EARTH.getColor() + this.elementHelp.get(Element.EARTH).replaceAll("(?i)/b display earth", Element.EARTH.getSubColor() + "/b display earth" + Element.EARTH.getColor()));
		} else if (Arrays.asList(Commands.firealiases).contains(arg)) {
			sender.sendMessage(Element.FIRE.getColor() + this.elementHelp.get(Element.FIRE).replaceAll("(?i)/b display fire", Element.FIRE.getSubColor() + "/b display fire" + Element.FIRE.getColor()));
		} else if (Arrays.asList(Commands.chialiases).contains(arg)) {
			sender.sendMessage(Element.CHI.getColor() + this.elementHelp.get(Element.CHI).replaceAll("(?i)/b display chi", Element.CHI.getSubColor() + "/b display chi" + Element.CHI.getColor()));
		} else if (Arrays.asList(Commands.avataraliases).contains(arg)) {
			sender.sendMessage(Element.AVATAR.getColor() + this.elementHelp.get(Element.AVATAR).replaceAll("(?i)/b display avatar", Element.AVATAR.getSubColor() + "/b display avatar" + Element.AVATAR.getColor()));
		} else if (Element.getElement(arg) != null && elementHelp.containsKey(Element.getElement(arg))) {
			Element element = Element.getElement(arg);
			sender.sendMessage(element.getColor() + this.elementHelp.get(element).replaceAll("(?i)/b display " + element.getName().toLowerCase(), element.getSubColor() + "/b display " + element.getName().toLowerCase() + element.getColor()));
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
		if (!args.isEmpty() || !sender.hasPermission("bending.command.help")) {
			return new ArrayList<>();
		}

		final List<String> list = new ArrayList<>();
		for (final Element e : Element.getAllElements()) {
			list.add(e.getName());
		}

		final List<String> abils = new ArrayList<>();
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

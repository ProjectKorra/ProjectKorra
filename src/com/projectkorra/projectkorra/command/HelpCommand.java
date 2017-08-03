package com.projectkorra.projectkorra.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.projectkorra.items.command.PKICommand;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.rpg.commands.RPGCommand;

/**
 * Executor for /bending help. Extends {@link PKCommand}.
 */
public class HelpCommand extends PKCommand {

	private String required;
	private String optional;
	private String properUsage;
	private String learnMore;
	private String air;
	private String water;
	private String earth;
	private String fire;
	private String chi;
	private String invalidTopic;
	private String usage;

	public HelpCommand() {
		super("help", "/bending help <Page/Topic>", ConfigManager.languageConfig.get().getString("Commands.Help.Description"), new String[] { "help", "h" });

		this.required = ConfigManager.languageConfig.get().getString("Commands.Help.Required");
		this.optional = ConfigManager.languageConfig.get().getString("Commands.Help.Optional");
		this.properUsage = ConfigManager.languageConfig.get().getString("Commands.Help.ProperUsage");
		this.learnMore = ConfigManager.languageConfig.get().getString("Commands.Help.Elements.LearnMore");
		this.air = ConfigManager.languageConfig.get().getString("Commands.Help.Elements.Air");
		this.water = ConfigManager.languageConfig.get().getString("Commands.Help.Elements.Water");
		this.earth = ConfigManager.languageConfig.get().getString("Commands.Help.Elements.Earth");
		this.fire = ConfigManager.languageConfig.get().getString("Commands.Help.Elements.Fire");
		this.chi = ConfigManager.languageConfig.get().getString("Commands.Help.Elements.Chi");
		this.invalidTopic = ConfigManager.languageConfig.get().getString("Commands.Help.InvalidTopic");
		this.usage = ConfigManager.languageConfig.get().getString("Commands.Help.Usage");
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		boolean firstMessage = true;
		
		if (!hasPermission(sender) || !correctLength(sender, args.size(), 0, 1))
			return;
		else if (args.size() == 0) {
			List<String> strings = new ArrayList<String>();
			for (PKCommand command : instances.values()) {
				if (!command.getName().equalsIgnoreCase("help") && sender.hasPermission("bending.command." + command.getName())) {
					strings.add(command.getProperUse());
				}
			}
			if (GeneralMethods.hasItems()) {
				for (PKICommand command : PKICommand.instances.values()) {
					if (sender.hasPermission("bendingitems.command." + command.getName()))
						strings.add(command.getProperUse());
				}
			}
			if (GeneralMethods.hasRPG()) {
				for (RPGCommand command : RPGCommand.instances.values()) {
					if (sender.hasPermission("bending.command.rpg." + command.getName()))
						strings.add(command.getProperUse());
				}
			}
			if (GeneralMethods.hasSpirits()) {
				//spirits commands being added (if needed)
			}
			Collections.sort(strings);
			Collections.reverse(strings);
			strings.add(instances.get("help").getProperUse());
			Collections.reverse(strings);
			
			for (String s : getPage(strings, ChatColor.GOLD + "Commands: <" + required + "> [" + optional + "]", 1, false)) {
				if (firstMessage) {
					GeneralMethods.sendBrandingMessage(sender, s);
					firstMessage = false;
				} else {
					sender.sendMessage(ChatColor.YELLOW + s);
				}
			}
			return;
		}

		String arg = args.get(0).toLowerCase();
		
		if (isNumeric(arg)) {
			List<String> strings = new ArrayList<String>();
			for (PKCommand command : instances.values()) {
				strings.add(command.getProperUse());
			}
			if (GeneralMethods.hasItems()) {
				for (PKICommand command : PKICommand.instances.values()) {
					if (sender.hasPermission("bendingitems.command." + command.getName()))
						strings.add(command.getProperUse());
				}
			}
			if (GeneralMethods.hasRPG()) {
				for (RPGCommand command : RPGCommand.instances.values()) {
					if (sender.hasPermission("bending.command.rpg." + command.getName()))
						strings.add(command.getProperUse());
				}
			}
			if (GeneralMethods.hasSpirits()) {
				//spirits commands being added (if needed)
			}
			for (String s : getPage(strings, ChatColor.GOLD + "Commands: <" + required + "> [" + optional + "]", Integer.valueOf(arg), true)) {
				if (firstMessage) {
					GeneralMethods.sendBrandingMessage(sender, s);
					firstMessage = false;
				} else {
					sender.sendMessage(ChatColor.YELLOW + s);
				}
			}
		} else if (instances.keySet().contains(arg)) {//bending help command
			instances.get(arg).help(sender, true);
		} else if (Arrays.asList(Commands.comboaliases).contains(arg)) { //bending help elementcombo
			sender.sendMessage(ChatColor.GOLD + properUsage.replace("{command1}", ChatColor.RED + "/bending display " + arg + ChatColor.GOLD).replace("{command2}", ChatColor.RED + "/bending help <Combo Name>" + ChatColor.GOLD));
		} else if (Arrays.asList(Commands.passivealiases).contains(arg)) { //bending help elementpassive
			sender.sendMessage(ChatColor.GOLD + properUsage.replace("{command1}", ChatColor.RED + "/bending display " + arg + ChatColor.GOLD).replace("{command2}", ChatColor.RED + "/bending help <Passive Name>" + ChatColor.RED));
		} else if (CoreAbility.getAbility(arg) != null && !(CoreAbility.getAbility(arg) instanceof ComboAbility) && CoreAbility.getAbility(arg).isEnabled() && !CoreAbility.getAbility(arg).isHiddenAbility() || CoreAbility.getAbility(arg) instanceof PassiveAbility) { //bending help ability
			CoreAbility ability = CoreAbility.getAbility(arg);
			ChatColor color = ability.getElement().getColor();
			
			if (ability instanceof AddonAbility) {
				if (ability instanceof PassiveAbility) {
					sender.sendMessage(color + (ChatColor.BOLD + ability.getName()) + ChatColor.WHITE + " (Addon Passive)");
				} else {
					sender.sendMessage(color + (ChatColor.BOLD + ability.getName()) + ChatColor.WHITE + " (Addon)");
				}
				
				sender.sendMessage(color + ability.getDescription());
				
				if (!ability.getInstructions().isEmpty()) {
					sender.sendMessage(ChatColor.GOLD + usage + ability.getInstructions());
				}
				
				AddonAbility abil = (AddonAbility) CoreAbility.getAbility(arg);
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
					sender.sendMessage(ChatColor.GOLD + usage + ability.getInstructions());
				}
			}
		} else if (Arrays.asList(Commands.airaliases).contains(arg)) {
			sender.sendMessage(Element.AIR.getColor() + air.replace("/b help AirCombos", Element.AIR.getSubColor() + "/b help AirCombos" + Element.AIR.getColor()));
			sender.sendMessage(ChatColor.YELLOW + learnMore + ChatColor.DARK_AQUA + "http://projectkorra.com/");
		} else if (Arrays.asList(Commands.wateraliases).contains(arg)) {
			sender.sendMessage(Element.WATER.getColor() + water.replace("/b help WaterCombos", Element.WATER.getSubColor() + "/b h WaterCombos" + Element.WATER.getColor()));
			sender.sendMessage(ChatColor.YELLOW + learnMore + ChatColor.DARK_AQUA + "http://projectkorra.com/");
		} else if (Arrays.asList(Commands.earthaliases).contains(arg)) {
			sender.sendMessage(Element.EARTH.getColor() + earth);
			sender.sendMessage(ChatColor.YELLOW + learnMore + ChatColor.DARK_AQUA + "http://projectkorra.com/");
		} else if (Arrays.asList(Commands.firealiases).contains(arg)) {
			sender.sendMessage(Element.FIRE.getColor() + fire.replace("/b h FireCombos", Element.FIRE.getSubColor() + "/b h FireCombos" + Element.FIRE.getColor()));
			sender.sendMessage(ChatColor.YELLOW + learnMore + ChatColor.DARK_AQUA + "http://projectkorra.com/");
		} else if (Arrays.asList(Commands.chialiases).contains(arg)) {
			sender.sendMessage(Element.CHI.getColor() + chi.replace("/b h ChiCombos", Element.CHI.getSubColor() + "/b h ChiCombos" + Element.CHI.getColor()));
			sender.sendMessage(ChatColor.YELLOW + learnMore + ChatColor.DARK_AQUA + "http://projectkorra.com/");
		} else {
			//combos - handled differently because they're stored in CamelCase in ComboManager
			for (String combo : ComboManager.getDescriptions().keySet()) {
				if (combo.equalsIgnoreCase(arg)) {				
					CoreAbility ability = CoreAbility.getAbility(combo);
					ChatColor color = ability != null ? ability.getElement().getColor() : null;
					
					if (ability instanceof AddonAbility) {
						sender.sendMessage(color + (ChatColor.BOLD + ability.getName()) + ChatColor.WHITE + " (Addon Combo)");
						sender.sendMessage(color + ability.getDescription());
						
						if (!ability.getInstructions().isEmpty()) {
							sender.sendMessage(ChatColor.GOLD + usage + ability.getInstructions());
						}
						
						AddonAbility abil = (AddonAbility) CoreAbility.getAbility(arg);
						sender.sendMessage(color + "- By: " + ChatColor.WHITE + abil.getAuthor());
						sender.sendMessage(color + "- Version: " + ChatColor.WHITE + abil.getVersion());
					} else {
						sender.sendMessage(color + (ChatColor.BOLD + ability.getName()) + ChatColor.WHITE + " (Combo)");
						sender.sendMessage(color + ComboManager.getDescriptions().get(combo));
						sender.sendMessage(ChatColor.GOLD + usage + ComboManager.getInstructions().get(combo));
					}

					return;
				}
			}
			
			sender.sendMessage(ChatColor.RED + invalidTopic);
		}
	}

	@Override
	protected List<String> getTabCompletion(CommandSender sender, List<String> args) {
		if (args.size() >= 1 || !sender.hasPermission("bending.command.help")) {
			return new ArrayList<String>();
		}
		
		List<String> list = new ArrayList<String>();
		for (Element e : Element.getAllElements()) {
			list.add(e.getName());
		}
		
		List<String> abils = new ArrayList<String>();
		for (CoreAbility coreAbil : CoreAbility.getAbilities()) {
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

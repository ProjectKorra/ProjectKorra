package com.projectkorra.projectkorra.command;

import com.projectkorra.items.command.PKICommand;
import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.rpg.commands.RPGCommand;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
				sender.sendMessage(ChatColor.YELLOW + s);
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
				sender.sendMessage(ChatColor.YELLOW + s);
			}
		} else if (instances.keySet().contains(arg.toLowerCase())) {//bending help command
			instances.get(arg).help(sender, true);
		} else if (Arrays.asList(Commands.comboaliases).contains(arg)) { //bending help elementcombo
			sender.sendMessage(ChatColor.GOLD + properUsage.replace("{command1}", ChatColor.RED + "/bending display " + arg + ChatColor.GOLD).replace("{command2}", ChatColor.RED + "/bending help <Combo Name>" + ChatColor.GOLD));
		} else if (CoreAbility.getAbility(arg) != null && !(CoreAbility.getAbility(arg) instanceof ComboAbility) && CoreAbility.getAbility(arg).isEnabled() && !CoreAbility.getAbility(arg).isHiddenAbility()) { //bending help ability
			CoreAbility ability = CoreAbility.getAbility(arg);
			ChatColor color = ability.getElement().getColor();
			sender.sendMessage(color + ability.getName() + " - ");
			sender.sendMessage(color + ability.getDescription());
		} else if (Arrays.asList(Commands.airaliases).contains(args.get(0))) {
			sender.sendMessage(Element.AIR.getColor() + air.replace("/b help AirCombos", Element.AIR.getSubColor() + "/b help AirCombos" + Element.AIR.getColor()));
			sender.sendMessage(ChatColor.YELLOW + learnMore + ChatColor.DARK_AQUA + "http://tinyurl.com/qffg9m3");
		} else if (Arrays.asList(Commands.wateraliases).contains(args.get(0))) {
			sender.sendMessage(Element.WATER.getColor() + water.replace("/b help WaterCombos", Element.WATER.getSubColor() + "/b h WaterCombos" + Element.WATER.getColor()));
			sender.sendMessage(ChatColor.YELLOW + learnMore + ChatColor.DARK_AQUA + "http://tinyurl.com/lod3plv");
		} else if (Arrays.asList(Commands.earthaliases).contains(args.get(0))) {
			sender.sendMessage(Element.EARTH.getColor() + earth);
			sender.sendMessage(ChatColor.YELLOW + learnMore + ChatColor.DARK_AQUA + "http://tinyurl.com/qaudl42");
		} else if (Arrays.asList(Commands.firealiases).contains(args.get(0))) {
			sender.sendMessage(Element.FIRE.getColor() + fire.replace("/b h FireCombos", Element.FIRE.getSubColor() + "/b h FireCombos" + Element.FIRE.getColor()));
			sender.sendMessage(ChatColor.YELLOW + learnMore + ChatColor.DARK_AQUA + "http://tinyurl.com/k4fkjhb");
		} else if (Arrays.asList(Commands.chialiases).contains(args.get(0))) {
			sender.sendMessage(Element.CHI.getColor() + chi.replace("/b h ChiCombos", Element.CHI.getSubColor() + "/b h ChiCombos" + Element.CHI.getColor()));
			sender.sendMessage(ChatColor.YELLOW + learnMore + ChatColor.DARK_AQUA + "http://tinyurl.com/mkp9n6y");
		} else {
			//combos - handled differently because they're stored in CamelCase in ComboManager
			for (String combo : ComboManager.getDescriptions().keySet()) {
				if (combo.equalsIgnoreCase(arg)) {
					CoreAbility coreAbility = CoreAbility.getAbility(combo);
					ChatColor color = coreAbility != null ? coreAbility.getElement().getColor() : null;
					sender.sendMessage(color + combo + " (Combo) - ");
					sender.sendMessage(color + ComboManager.getDescriptions().get(combo));
					sender.sendMessage(ChatColor.GOLD + usage + ComboManager.getInstructions().get(combo));
					return;
				}
			}
			sender.sendMessage(ChatColor.RED + invalidTopic);
		}
	}
	
	@Override
	protected List<String> getTabCompletion(CommandSender sender, List<String> args) {
		if (args.size() >= 1 || !sender.hasPermission("bending.command.help")) return new ArrayList<String>();
		List<String> list = new ArrayList<String>();
		for (Element e : Element.getAllElements()) {
			list.add(e.getName());
		}
		List<String> abils = new ArrayList<String>();
		for (CoreAbility coreAbil : CoreAbility.getAbilities()) {
			if (!(sender instanceof Player) && (!coreAbil.isHiddenAbility() || coreAbil instanceof ComboAbility) && coreAbil.isEnabled()) {
				abils.add(coreAbil.getName());
			} else if (sender instanceof Player) {
				BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(sender.getName());
				if (bPlayer.canBind(coreAbil) || (coreAbil instanceof ComboAbility)) {
					abils.add(coreAbil.getName());
				}
			}
		}

		Collections.sort(abils);
		list.addAll(abils);
		return list;
	}
}

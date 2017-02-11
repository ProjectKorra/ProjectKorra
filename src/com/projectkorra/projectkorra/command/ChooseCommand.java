package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent.Result;
import com.projectkorra.projectkorra.event.PlayerChangeSubElementEvent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Executor for /bending choose. Extends {@link PKCommand}.
 */
public class ChooseCommand extends PKCommand {

	private String invalidElement;
	private String playerNotFound;
	private String chosenCFW;
	private String chosenAE;
	private String chosenOtherCFW;
	private String chosenOtherAE;

	public ChooseCommand() {
		super("choose", "/bending choose <Element> [Player]", ConfigManager.languageConfig.get().getString("Commands.Choose.Description"), new String[] { "choose", "ch" });

		this.playerNotFound = ConfigManager.languageConfig.get().getString("Commands.Choose.PlayerNotFound");
		this.invalidElement = ConfigManager.languageConfig.get().getString("Commands.Choose.InvalidElement");
		this.chosenCFW = ConfigManager.languageConfig.get().getString("Commands.Choose.SuccessfullyChosenCFW");
		this.chosenAE = ConfigManager.languageConfig.get().getString("Commands.Choose.SuccessfullyChosenAE");
		this.chosenOtherCFW = ConfigManager.languageConfig.get().getString("Commands.Choose.Other.SuccessfullyChosenCFW");
		this.chosenOtherAE = ConfigManager.languageConfig.get().getString("Commands.Choose.Other.SuccessfullyChosenAE");
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if (!correctLength(sender, args.size(), 1, 2)) {
			return;
		} else if (args.size() == 1) {
			if (!hasPermission(sender) || !isPlayer(sender)) {
				return;
			}

			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(sender.getName());
			if (bPlayer == null) {
				GeneralMethods.createBendingPlayer(((Player) sender).getUniqueId(), sender.getName());
				bPlayer = BendingPlayer.getBendingPlayer(sender.getName());
			}
			if (bPlayer.isPermaRemoved()) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + ConfigManager.languageConfig.get().getString("Commands.Preset.BendingPermanentlyRemoved"));
				return;
			}

			if (!bPlayer.getElements().isEmpty() && !sender.hasPermission("bending.command.rechoose")) {
				GeneralMethods.sendBrandingMessage(sender, super.noPermissionMessage);
				return;
			}
			String element = args.get(0).toLowerCase();
			if (element.equalsIgnoreCase("a"))
				element = "air";
			else if (element.equalsIgnoreCase("e"))
				element = "earth";
			else if (element.equalsIgnoreCase("f"))
				element = "fire";
			else if (element.equalsIgnoreCase("w"))
				element = "water";
			else if (element.equalsIgnoreCase("c"))
				element = "chi";
			Element targetElement = Element.getElement(element);
			if (Arrays.asList(Element.getAllElements()).contains(targetElement)) {
				if (!hasPermission(sender, element)) {
					return;
				}
				add(sender, (Player) sender, targetElement);
				return;
			} else {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + invalidElement);
				return;
			}
		} else if (args.size() == 2) {
			if (!sender.hasPermission("bending.admin.choose")) {
				GeneralMethods.sendBrandingMessage(sender, super.noPermissionMessage);
				return;
			}
			Player target = ProjectKorra.plugin.getServer().getPlayer(args.get(1));
			if (target == null || !target.isOnline()) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + playerNotFound);
				return;
			}
			String element = args.get(0).toLowerCase();
			if (element.equalsIgnoreCase("a"))
				element = "air";
			else if (element.equalsIgnoreCase("e"))
				element = "earth";
			else if (element.equalsIgnoreCase("f"))
				element = "fire";
			else if (element.equalsIgnoreCase("w"))
				element = "water";
			else if (element.equalsIgnoreCase("c"))
				element = "chi";
			Element targetElement = Element.getElement(element);
			if (Arrays.asList(Element.getAllElements()).contains(targetElement) && targetElement != Element.AVATAR) {
				add(sender, target, targetElement);
				return;
			} else {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + invalidElement);
			}
		}
	}

	/**
	 * Adds the ability to bend the given element to the specified Player.
	 * 
	 * @param sender The CommandSender who issued the command
	 * @param target The Player to add the element to
	 * @param element The element to add to the Player
	 */
	private void add(CommandSender sender, Player target, Element element) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(target);

		if (bPlayer == null) {
			return;
		}
		if (element instanceof SubElement) {
			SubElement sub = (SubElement) element;
			bPlayer.addSubElement(sub);
			ChatColor color = sub != null ? sub.getColor() : ChatColor.WHITE;
			if (!(sender instanceof Player) || !((Player) sender).equals(target)) {
				GeneralMethods.sendBrandingMessage(sender, color + chosenOtherCFW.replace("{target}", ChatColor.DARK_AQUA + target.getName() + color).replace("{element}", sub.getName() + sub.getType().getBender()));
			} else {
				GeneralMethods.sendBrandingMessage(target, color + chosenCFW.replace("{element}", sub.getName() + sub.getType().getBender()));
			}
			GeneralMethods.saveSubElements(bPlayer);
			Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeSubElementEvent(sender, target, sub, com.projectkorra.projectkorra.event.PlayerChangeSubElementEvent.Result.CHOOSE));
		} else {
			bPlayer.setElement(element);
			bPlayer.getSubElements().clear();
			for (SubElement sub : Element.getAllSubElements()) {
				if (bPlayer.hasElement(sub.getParentElement()) && bPlayer.hasSubElementPermission(sub)) {
					bPlayer.addSubElement(sub);
				}
			}

			ChatColor color = element != null ? element.getColor() : ChatColor.WHITE;
			if (!(sender instanceof Player) || !((Player) sender).equals(target)) {
				if (element != Element.AIR && element != Element.EARTH)
					GeneralMethods.sendBrandingMessage(sender, color + chosenOtherCFW.replace("{target}", ChatColor.DARK_AQUA + target.getName() + color).replace("{element}", element.getName() + element.getType().getBender()));
				else
					GeneralMethods.sendBrandingMessage(sender, color + chosenOtherAE.replace("{target}", ChatColor.DARK_AQUA + target.getName() + color).replace("{element}", element.getName() + element.getType().getBender()));
			} else {
				if (element != Element.AIR && element != Element.EARTH)
					GeneralMethods.sendBrandingMessage(target, color + chosenCFW.replace("{element}", element.getName() + element.getType().getBender()));
				else
					GeneralMethods.sendBrandingMessage(target, color + chosenAE.replace("{element}", element.getName() + element.getType().getBender()));
			}
			GeneralMethods.saveElements(bPlayer);
			GeneralMethods.saveSubElements(bPlayer);
			Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(sender, target, element, Result.CHOOSE));
		}

		GeneralMethods.removeUnusableAbilities(target.getName());

	}

	public static boolean isVowel(char c) {
		return "AEIOUaeiou".indexOf(c) != -1;
	}

	@Override
	protected List<String> getTabCompletion(CommandSender sender, List<String> args) {
		if (args.size() >= 2 || !sender.hasPermission("bending.command.choose"))
			return new ArrayList<String>();

		List<String> l = new ArrayList<String>();
		if (args.size() == 0) {

			l.add("Air");
			l.add("Earth");
			l.add("Fire");
			l.add("Water");
			l.add("Chi");
			for (Element e : Element.getAddonElements()) {
				l.add(e.getName());
			}
		} else {
			for (Player p : Bukkit.getOnlinePlayers()) {
				l.add(p.getName());
			}
		}
		return l;
	}
}

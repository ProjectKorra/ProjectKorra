package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent.Result;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * Executor for /bending choose. Extends {@link PKCommand}.
 */
public class ChooseCommand extends PKCommand {

	private String invalidElement;
	private String playerNotFound;
	private String chosen;
	private String chosenOther;

	public ChooseCommand() {
		super("choose", "/bending choose <Element> [Player]", ConfigManager.languageConfig.get().getString("Commands.Choose.Description"), new String[] { "choose", "ch" });
		
		this.playerNotFound = ConfigManager.languageConfig.get().getString("Commands.Choose.PlayerNotFound");
		this.invalidElement = ConfigManager.languageConfig.get().getString("Commands.Choose.InvalidElement");
		this.chosen = ConfigManager.languageConfig.get().getString("Commands.Choose.SuccessfullyChosen");
		this.chosenOther = ConfigManager.languageConfig.get().getString("Commands.Choose.Other.SuccessfullyChosen");
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
				sender.sendMessage(ChatColor.RED + ConfigManager.languageConfig.get().getString("Commands.Preset.BendingPermanentlyRemoved"));
				return;
			}

			if (!bPlayer.getElements().isEmpty() && !sender.hasPermission("bending.command.rechoose")) {
				sender.sendMessage(super.noPermissionMessage);
				return;
			}
			String element = args.get(0).toLowerCase();
			Element target = Element.getElement(element);
			if (Arrays.asList(Element.getAllElements()).contains(target)) {
				if (!hasPermission(sender, element)) {
					return;
				}
				add(sender, (Player) sender, target);
				return;
			} else {
				sender.sendMessage(ChatColor.RED + invalidElement);
				return;
			}
		} else if (args.size() == 2) {
			if (!sender.hasPermission("bending.admin.choose")) {
				sender.sendMessage(super.noPermissionMessage);
				return;
			}
			Player target = ProjectKorra.plugin.getServer().getPlayer(args.get(1));
			if (target == null || !target.isOnline()) {
				sender.sendMessage(ChatColor.RED + playerNotFound);
				return;
			}
			String element = args.get(0).toLowerCase();
			Element targetElement = Element.getElement(element);
			if (Arrays.asList(Element.getAllElements()).contains(targetElement)) {
				add(sender, target, targetElement);
				return;
			} else {
				sender.sendMessage(ChatColor.RED + invalidElement);
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
		
		bPlayer.setElement(element);
		ChatColor color = element != null ? element.getColor() : null;
		if (!(sender instanceof Player) || !((Player) sender).equals(target)) {
			sender.sendMessage(color + chosenOther.replace("{target}", ChatColor.DARK_AQUA + target.getName() + color).replace("{element}", element.getName() + element.getType().getBender()));
		} else {
			target.sendMessage(color + chosen.replace("{element}", element.getName() + element.getType().getBender()));
		}
		
		
		
		GeneralMethods.removeUnusableAbilities(target.getName());
		GeneralMethods.saveElements(bPlayer);
		Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(sender, target, element, Result.CHOOSE));
	}
	
	public static boolean isVowel(char c) {
		return "AEIOUaeiou".indexOf(c) != -1;
	}
}

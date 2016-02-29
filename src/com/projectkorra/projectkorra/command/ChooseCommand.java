package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
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

	public ChooseCommand() {
		super("choose", "/bending choose <Element> [Player]", "This command will allow the user to choose a player either for himself or <Player> if specified. This command can only be used once per player unless they have permission to rechoose their element.", new String[] { "choose", "ch" });
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
				sender.sendMessage(ChatColor.RED + "Your bending was permanently removed.");
				return;
			}

			if (!bPlayer.getElements().isEmpty() && !sender.hasPermission("bending.command.rechoose")) {
				sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
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
				sender.sendMessage(ChatColor.RED + "That is not a valid element.");
				return;
			}
		} else if (args.size() == 2) {
			if (!sender.hasPermission("bending.admin.choose")) {
				sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
				return;
			}
			Player target = ProjectKorra.plugin.getServer().getPlayer(args.get(1));
			if (target == null || !target.isOnline()) {
				sender.sendMessage(ChatColor.RED + "That player is not online.");
				return;
			}
			String element = args.get(0).toLowerCase();
			Element targetElement = Element.getElement(element);
			if (Arrays.asList(Element.getAllElements()).contains(targetElement)) {
				add(sender, target, targetElement);
				return;
			} else {
				sender.sendMessage(ChatColor.RED + "That is not a valid element.");
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
			sender.sendMessage(ChatColor.DARK_AQUA + target.getName() + color + " is now a" + (isVowel(element.getName().charAt(0)) ? "n " : " ") + element.getName() + element.getType().getBender() + ".");
		} else {
			target.sendMessage(color + "You are now a" + (isVowel(element.getName().charAt(0)) ? "n " : " ") + element.getName() + element.getType().getBender() + ".");
		}
		
		
		
		GeneralMethods.removeUnusableAbilities(target.getName());
		GeneralMethods.saveElements(bPlayer);
		Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(sender, target, element, Result.CHOOSE));
	}
	
	public static boolean isVowel(char c) {
		return "AEIOUaeiou".indexOf(c) != -1;
	}
}

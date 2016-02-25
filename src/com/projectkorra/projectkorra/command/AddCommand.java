package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent.Result;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * Executor for /bending add. Extends {@link PKCommand}.
 */
public class AddCommand extends PKCommand {

	public AddCommand() {
		super("add", "/bending add <Element> [Player]", "This command will allow the user to add an element to the targeted <Player>, or themselves if the target is not specified. This command is typically reserved for server administrators.", new String[] { "add", "a" });
	}

	public void execute(CommandSender sender, List<String> args) {
		if (!correctLength(sender, args.size(), 1, 2)) {
			return;
		} else if (args.size() == 1) { //bending add element
			if (!hasPermission(sender) || !isPlayer(sender)) {
				return;
			}
			add(sender, (Player) sender, Element.fromString(args.get(0).toLowerCase()));
		} else if (args.size() == 2) { //bending add element combo
			if (!hasPermission(sender, "others")) {
				return;
			}
			Player player = Bukkit.getPlayer(args.get(1));
			if (player == null) {
				sender.sendMessage(ChatColor.RED + "That player is not online.");
				return;
			}
			add(sender, player, Element.fromString(args.get(0).toLowerCase()));
		}
	}

	/**
	 * Adds the ability to bend an element to a player.
	 * 
	 * @param sender The CommandSender who issued the add command
	 * @param target The player to add the element to
	 * @param element The element to add
	 */
	private void add(CommandSender sender, Player target, Element element) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(target);
		if (bPlayer == null) {
			GeneralMethods.createBendingPlayer(target.getUniqueId(), target.getName());
			bPlayer = BendingPlayer.getBendingPlayer(target);
		}
		if (bPlayer.isPermaRemoved()) {
			sender.sendMessage(ChatColor.RED + "That player's bending was permanently removed.");
			return;
		}
		if (Arrays.asList(Element.getAllElements()).contains(element)) {
			if (bPlayer.hasElement(element)) {
				if (!(sender instanceof Player) || !((Player) sender).equals(target)) {
					sender.sendMessage(ChatColor.DARK_AQUA + target.getName() + ChatColor.RED + " already has that element!");
				} else {
					sender.sendMessage(ChatColor.RED + "You already have that element!");
				}
				return;
			}
			bPlayer.addElement(element);
			ChatColor color = element.getColor();
			
			if (!(sender instanceof Player) || !((Player) sender).equals(target)) {
				sender.sendMessage(ChatColor.DARK_AQUA + target.getName() + color + " is also a" + (isVowel(element.getName().charAt(0)) ? "n " : " ") + element.getName() + (element.getType() != null ? element.getType().getBender() : "") + ".");
			} else {
				target.sendMessage(color + "You are also a" + (isVowel(element.getName().charAt(0)) ? "n " : " ") + element.getName() + (element.getType() != null ? element.getType().getBender() : "") + ".");
			}
			GeneralMethods.saveElements(bPlayer);
			Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(sender, target, element, Result.ADD));
			return;
		} else {
			sender.sendMessage(ChatColor.RED + "You must specify a valid element.");
		}
	}
	
	public static boolean isVowel(char c) {
		return "AEIOUaeiou".indexOf(c) != -1;
	}
}

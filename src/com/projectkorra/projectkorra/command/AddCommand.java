package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
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
 * Executor for /bending add. Extends {@link PKCommand}.
 */
public class AddCommand extends PKCommand {

	private String playerNotFound;
	private String invalidElement;
	private String addedOther;
	private String added;
	private String alreadyHasElementOther;
	private String alreadyHasElement;

	public AddCommand() {
		super("add", "/bending add <Element> [Player]", ConfigManager.languageConfig.get().getString("Commands.Add.Description"), new String[] { "add", "a" });
		
		this.playerNotFound = ConfigManager.languageConfig.get().getString("Commands.Add.PlayerNotFound");
		this.invalidElement = ConfigManager.languageConfig.get().getString("Commands.Add.InvalidElement");
		this.addedOther = ConfigManager.languageConfig.get().getString("Commands.Add.Other.SuccessfullyAdded");
		this.added = ConfigManager.languageConfig.get().getString("Commands.Add.SuccessfullyAdded");
		this.alreadyHasElementOther = ConfigManager.languageConfig.get().getString("Commands.Add.Other.AlreadyHasElement");
		this.alreadyHasElement = ConfigManager.languageConfig.get().getString("Commands.Add.AlreadyHasElement");
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
				sender.sendMessage(ChatColor.RED + playerNotFound);
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
			sender.sendMessage(ChatColor.RED + ConfigManager.languageConfig.get().getString("Commands.Preset.Other.BendingPermanentlyRemoved"));
			return;
		}
		if (Arrays.asList(Element.getAllElements()).contains(element)) {
			if (bPlayer.hasElement(element)) {
				if (!(sender instanceof Player) || !((Player) sender).equals(target)) {
					sender.sendMessage(ChatColor.RED + alreadyHasElementOther.replace("{target}", ChatColor.DARK_AQUA + target.getName() + ChatColor.RED));
				} else {
					sender.sendMessage(ChatColor.RED + alreadyHasElement);
				}
				return;
			}
			bPlayer.addElement(element);
			ChatColor color = element.getColor();
			
			if (!(sender instanceof Player) || !((Player) sender).equals(target)) {
				sender.sendMessage(color + addedOther.replace("{target}", ChatColor.DARK_AQUA + target.getName() + color).replace("{element}", element.getName() + element.getType().getBender()));
			} else {
				target.sendMessage(color + added.replace("{element}", element.getName() + element.getType().getBender()));
			}
			GeneralMethods.saveElements(bPlayer);
			Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(sender, target, element, Result.ADD));
			return;
		} else {
			sender.sendMessage(ChatColor.RED + invalidElement);
		}
	}
	
	public static boolean isVowel(char c) {
		return "AEIOUaeiou".indexOf(c) != -1;
	}
}

package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * Executor for /bending toggle. Extends {@link PKCommand}.
 */
public class ToggleCommand extends PKCommand {

	public ToggleCommand() {
		super("toggle", "/bending toggle <all | (element) <player>>", "This command will toggle a player's own Bending on or off. If toggled off, all abilities should stop working until it is toggled back on. Logging off will automatically toggle your Bending back on. If you run the command /bending toggle all, Bending will be turned off for all players and cannot be turned back on until the command is run again.", new String[] { "toggle", "t" });
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if (!correctLength(sender, args.size(), 0, 2)) {
			return;
		} else if (args.size() == 0) { //bending toggle
			if (!hasPermission(sender) || !isPlayer(sender)) {
				return;
			}
			if (Commands.isToggledForAll) {
				sender.sendMessage(ChatColor.RED + "Bending is currently toggled off for all players.");
				return;
			}
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(sender.getName());
			if (bPlayer == null) {
				GeneralMethods.createBendingPlayer(((Player) sender).getUniqueId(), sender.getName());
				bPlayer = BendingPlayer.getBendingPlayer(sender.getName());
			}
			if (bPlayer.isToggled()) {
				sender.sendMessage(ChatColor.RED + "Your bending has been toggled off. You will not be able to use most abilities until you toggle it back.");
				bPlayer.toggleBending();
			} else {
				sender.sendMessage(ChatColor.GREEN + "You have turned your Bending back on.");
				bPlayer.toggleBending();
			}
		} else if (args.size() == 1 && args.get(0).equalsIgnoreCase("all") && hasPermission(sender, "all")) { //bending toggle all
			if (Commands.isToggledForAll) { // Bending is toggled off for all players.
				Commands.isToggledForAll = false;
				for (Player player : Bukkit.getOnlinePlayers()) {
					player.sendMessage(ChatColor.GREEN + "Bending has been toggled back on for all players.");
				}
				if (!(sender instanceof Player))
					sender.sendMessage(ChatColor.GREEN + "Bending has been toggled back on for all players.");
			} else {
				Commands.isToggledForAll = true;
				for (Player player : Bukkit.getOnlinePlayers()) {
					player.sendMessage(ChatColor.RED + "Bending has been toggled off for all players.");
				}
				if (!(sender instanceof Player))
					sender.sendMessage(ChatColor.RED + "Bending has been toggled off for all players.");
			}
		} else if (sender instanceof Player && args.size() == 1 
				&& Element.getElement(getElement(args.get(0))) != null 
				&& BendingPlayer.getBendingPlayer(sender.getName()).hasElement(Element.getElement(getElement(args.get(0))))) {
			Element e = Element.getElement(getElement(args.get(0)));
			ChatColor color = e != null ? e.getColor() : null;
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(sender.getName());
			bPlayer.toggleElement(e);
			
			if (bPlayer.isElementToggled(e) == false) {
				if (e == Element.CHI) {
					sender.sendMessage(color + "You have toggled off your chiblocking");
				} else {
					sender.sendMessage(color + "You have toggled off your " + getElement(args.get(0)).toLowerCase() + "bending");
				}
			} else {
				if (e == Element.CHI) {
					sender.sendMessage(color + "You have toggled on your chiblocking");
				} else {
					sender.sendMessage(color + "You have toggled on your " + getElement(args.get(0)).toLowerCase() + "bending");
				}
			}
		} else if (sender instanceof Player && args.size() == 2 
				&& Element.getElement(getElement(args.get(0))) != null 
				&& BendingPlayer.getBendingPlayer(sender.getName()).hasElement(Element.getElement(getElement(args.get(0))))) {
			Player target = Bukkit.getPlayer(args.get(1));
			if (!hasAdminPermission(sender)) return;
			if (target == null) {
				sender.sendMessage(ChatColor.RED + "Target is not found.");
			}
			Element e = Element.getElement(getElement(args.get(0)));
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(target.getName());
			ChatColor color = e != null ? e.getColor() : null;
			
			if (bPlayer.isElementToggled(e) == true) {
				if (e == Element.CHI) {
					sender.sendMessage(color + "You have toggled off " + ChatColor.DARK_AQUA + target.getName() + "'s chiblocking");
					target.sendMessage(color + "Your chiblocking has been toggled off by " + ChatColor.DARK_AQUA + sender.getName());
				} else {
					sender.sendMessage(color + "You have toggled off " + ChatColor.DARK_AQUA + target.getName() + "'s " + getElement(args.get(0)).toLowerCase() + "bending");
					target.sendMessage(color + "Your " + getElement(args.get(0)).toLowerCase() + "bending has been toggled off by " + ChatColor.DARK_AQUA + sender.getName());
				}
			} else {
				if (e == Element.CHI) {
					sender.sendMessage(color + "You have toggled on " + ChatColor.DARK_AQUA + target.getName() + "'s chiblocking");
					target.sendMessage(color + "Your chiblocking has been toggled on by " + ChatColor.DARK_AQUA + sender.getName());
				} else {
					sender.sendMessage(color + "You have toggled on " + ChatColor.DARK_AQUA + target.getName() + "'s " + getElement(args.get(0)).toLowerCase() + "bending");
					target.sendMessage(color + "Your " + getElement(args.get(0)).toLowerCase() + "bending has been toggled on by " + ChatColor.DARK_AQUA + sender.getName());
				}
			}
			bPlayer.toggleElement(e);
		} else {
			help(sender, false);
		}
	}

	public String getElement(String string) {
		if (Arrays.asList(Commands.airaliases).contains(string)) return "air";
		if (Arrays.asList(Commands.chialiases).contains(string)) return "chi";
		if (Arrays.asList(Commands.earthaliases).contains(string)) return "earth";
		if (Arrays.asList(Commands.firealiases).contains(string)) return "fire";
		if (Arrays.asList(Commands.wateraliases).contains(string)) return "water";
		return null;
	}

	public boolean hasAdminPermission(CommandSender sender) {
		if (!sender.hasPermission("bending.admin.toggle")) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
			return false;
		}
		return true;
	}
}

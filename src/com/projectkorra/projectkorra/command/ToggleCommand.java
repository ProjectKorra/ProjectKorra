package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Executor for /bending toggle. Extends {@link PKCommand}.
 */
public class ToggleCommand extends PKCommand {

	public ToggleCommand() {
		super("toggle", "/bending toggle <all>", "This command will toggle a player's own Bending on or off. If toggled off, all abilities should stop working until it is toggled back on. Logging off will automatically toggle your Bending back on. If you run the command /bending toggle all, Bending will be turned off for all players and cannot be turned back on until the command is run again.", new String[] { "toggle", "t" });
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if (!correctLength(sender, args.size(), 0, 1)) {
			return;
		} else if (args.size() == 0) { //bending toggle
			if (!hasPermission(sender) || !isPlayer(sender)) {
				return;
			}
			if (Commands.isToggledForAll) {
				sender.sendMessage(ChatColor.RED + "Bending is currently toggled off for all players.");
				return;
			}
			BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(sender.getName());
			if (bPlayer == null) {
				GeneralMethods.createBendingPlayer(((Player) sender).getUniqueId(), sender.getName());
				bPlayer = GeneralMethods.getBendingPlayer(sender.getName());
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
		} else {
			help(sender, false);
		}
	}

}

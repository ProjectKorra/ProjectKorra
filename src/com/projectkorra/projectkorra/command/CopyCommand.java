package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

public class CopyCommand extends PKCommand {
	
	public CopyCommand() {
		super("copy", "/bending copy <Player> [Player]", "This command will allow the user to copy the binds of another player either for himself or assign them to <Player> if specified.", new String[] { "copy", "co" });
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if (!correctLength(sender, args.size(), 1, 2)) {
			return;
		} else if (args.size() == 1) {
			if (!hasPermission(sender) || !isPlayer(sender)) {
				return;
			}

			Player orig = Bukkit.getPlayer(args.get(0));

			if (orig == null || !orig.isOnline()) {
				sender.sendMessage(ChatColor.RED + "Player not found.");
				return;
			}


			boolean boundAll = assignAbilities(sender, orig, (Player) sender, true);
			sender.sendMessage(ChatColor.GREEN + "Your bound abilities have been made the same as " + ChatColor.YELLOW + orig.getName());
			if (!boundAll) {
				sender.sendMessage(ChatColor.RED + "Some abilities were not bound because you cannot bend the required element.");
			}
		} else if (args.size() == 2) {
			if (!hasPermission(sender, "assign")) {
				sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
				return;
			}

			Player orig = ProjectKorra.plugin.getServer().getPlayer(args.get(0));
			Player target = ProjectKorra.plugin.getServer().getPlayer(args.get(1));

			if ((orig == null || !orig.isOnline()) || (target == null || !target.isOnline())) {
				sender.sendMessage(ChatColor.RED + "That player is not online.");
				return;
			}

			boolean boundAll = assignAbilities(sender, orig, target, false);
			sender.sendMessage(ChatColor.GREEN + "The bound abilities of " + ChatColor.YELLOW + target.getName() + ChatColor.GREEN + " have been been made the same as " + ChatColor.YELLOW + orig.getName());
			target.sendMessage(ChatColor.GREEN + "Your bound abilities have been made the same as " + ChatColor.YELLOW + orig.getName());
			if (!boundAll) {
				sender.sendMessage(ChatColor.RED + "Some abilities were not bound because you cannot bend the required element.");
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private boolean assignAbilities(CommandSender sender, Player player, Player player2, boolean self) {

		BendingPlayer orig = BendingPlayer.getBendingPlayer(player);
		BendingPlayer target = BendingPlayer.getBendingPlayer(player2);

		if (orig == null) {
			GeneralMethods.createBendingPlayer(((Player) player).getUniqueId(), player.getName());
			orig = BendingPlayer.getBendingPlayer(player);
		}
		if (target == null) {
			GeneralMethods.createBendingPlayer(((Player) player2).getUniqueId(), player2.getName());
			target = BendingPlayer.getBendingPlayer(player2);
		}
		if (orig.isPermaRemoved()) {
			if (self) {
				player.sendMessage(ChatColor.RED + "Your bending was permanently removed.");
			} else {
				sender.sendMessage(ChatColor.RED + "That players bending was permanently removed.");
			}
			return false;
		}
		
		HashMap<Integer, String> abilities = (HashMap<Integer, String>) orig.getAbilities().clone();
		boolean boundAll = true;
		for (int i = 1; i <= 9; i++) {
			CoreAbility coreAbil = CoreAbility.getAbility(abilities.get(i));
			if (coreAbil != null && !target.canBind(coreAbil)) {
				abilities.remove(i);
				boundAll = false;
			}
		}
		target.setAbilities(abilities);
		return boundAll;
	}

}
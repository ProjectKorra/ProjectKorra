package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent.Result;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Executor for /bending permaremove. Extends {@link PKCommand}.
 */
public class PermaremoveCommand extends PKCommand {

	public PermaremoveCommand() {
		super("permaremove", "/bending permaremove [Player]", "This command will permanently remove the Bending of the targeted <Player>. Once removed, a player may only receive Bending again if this command is run on them again. This command is typically reserved for administrators.", new String[] { "permaremove", "premove", "permremove", "pr" });
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if (!hasPermission(sender) || !correctLength(sender, args.size(), 0, 1)) {
			return;
		}
		if (args.size() == 1) {
			permaremove(sender, args.get(0));
		} else if (args.size() == 0 && isPlayer(sender))
			permaremove(sender, sender.getName());
	}

	/**
	 * Permanently removes a player's bending, or restores it if it had already
	 * been permaremoved.
	 * 
	 * @param sender The CommandSender who issued the permaremove command
	 * @param target The Player who's bending should be permaremoved
	 */
	private void permaremove(CommandSender sender, String target) {
		Player player = Bukkit.getPlayer(target);
		if (player == null) {
			sender.sendMessage(ChatColor.RED + "That player is not online.");
			return;
		}

		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			GeneralMethods.createBendingPlayer(player.getUniqueId(), player.getName());
			bPlayer = BendingPlayer.getBendingPlayer(player.getName());
		}

		if (bPlayer.isPermaRemoved()) {
			bPlayer.setPermaRemoved(false);
			GeneralMethods.savePermaRemoved(bPlayer);
			player.sendMessage(ChatColor.GREEN + "Your bending has been restored.");
			if (!(sender instanceof Player) || sender.getName().equals(target))
				sender.sendMessage(ChatColor.GREEN + "You have restored the bending of: " + ChatColor.DARK_AQUA + player.getName());
		} else {
			bPlayer.getElements().clear();
			GeneralMethods.saveElements(bPlayer);
			bPlayer.setPermaRemoved(true);
			GeneralMethods.savePermaRemoved(bPlayer);
			GeneralMethods.removeUnusableAbilities(player.getName());
			player.sendMessage(ChatColor.RED + "Your bending has been permanently removed.");
			if (!(sender instanceof Player) || sender.getName().equals(target))
				sender.sendMessage(ChatColor.RED + "You have permenantly removed the bending of: " + ChatColor.DARK_AQUA + player.getName());
			Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(sender, player, null, Result.PERMAREMOVE));
		}
	}

	/**
	 * Checks if the sender has the permission 'bending.admin.permaremove'. If
	 * not, it tells them they don't have permission to use the command.
	 * 
	 * @return True if they have the permission, false otherwise
	 */
	@Override
	public boolean hasPermission(CommandSender sender) {
		if (!sender.hasPermission("bending.admin.permaremove")) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
			return false;
		}
		return true;
	}
}

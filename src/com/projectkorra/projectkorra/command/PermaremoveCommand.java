package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent.Result;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Executor for /bending permaremove. Extends {@link PKCommand}.
 */
public class PermaremoveCommand extends PKCommand {

	private String playerIsOffline;
	private String restored;
	private String restoredConfirm;
	private String removed;
	private String removedConfirm;

	public PermaremoveCommand() {
		super("permaremove", "/bending permaremove <Player>", ConfigManager.languageConfig.get().getString("Commands.PermaRemove.Description"), new String[] { "permaremove", "premove", "permremove", "pr" });

		this.playerIsOffline = ConfigManager.languageConfig.get().getString("Commands.PermaRemove.PlayerOffline");
		this.restored = ConfigManager.languageConfig.get().getString("Commands.PermaRemove.Restored");
		this.restoredConfirm = ConfigManager.languageConfig.get().getString("Commands.PermaRemove.RestoredConfirm");
		this.removed = ConfigManager.languageConfig.get().getString("Commands.PermaRemove.Removed");
		this.removedConfirm = ConfigManager.languageConfig.get().getString("Commands.PermaRemove.RemovedConfirm");
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
			GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.playerIsOffline);
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
			GeneralMethods.sendBrandingMessage(player, ChatColor.GREEN + this.restored);
			if (!(sender instanceof Player) || !sender.getName().equalsIgnoreCase(target))
				GeneralMethods.sendBrandingMessage(sender, ChatColor.GREEN + this.restoredConfirm.replace("{target}", ChatColor.DARK_AQUA + player.getName() + ChatColor.GREEN));
		} else {
			bPlayer.getElements().clear();
			GeneralMethods.saveElements(bPlayer);
			bPlayer.setPermaRemoved(true);
			GeneralMethods.savePermaRemoved(bPlayer);
			GeneralMethods.removeUnusableAbilities(player.getName());
			GeneralMethods.sendBrandingMessage(player, ChatColor.RED + this.removed);
			if (!(sender instanceof Player) || !sender.getName().equalsIgnoreCase(target))
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.removedConfirm.replace("{target}", ChatColor.DARK_AQUA + player.getName() + ChatColor.RED));
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
			GeneralMethods.sendBrandingMessage(sender, super.noPermissionMessage);
			return false;
		}
		return true;
	}

	@Override
	protected List<String> getTabCompletion(CommandSender sender, List<String> args) {
		if (args.size() >= 1 || !sender.hasPermission("bending.command.permaremove"))
			return new ArrayList<String>();
		List<String> players = new ArrayList<String>();
		for (Player p : Bukkit.getOnlinePlayers()) {
			players.add(p.getName());
		}
		return players;
	}
}

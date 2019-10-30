package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.configuration.configs.commands.PermaremoveCommandConfig;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent.Result;
import com.projectkorra.projectkorra.player.BendingPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Executor for /bending permaremove. Extends {@link PKCommand}.
 */
public class PermaremoveCommand extends PKCommand<PermaremoveCommandConfig> {

	private final String playerIsOffline;
	private final String restored;
	private final String restoredConfirm;
	private final String removed;
	private final String removedConfirm;

	public PermaremoveCommand(final PermaremoveCommandConfig config) {
		super(config, "permaremove", "/bending permaremove <Player>", config.Description, new String[] { "permaremove", "premove", "permremove", "pr" });

		this.playerIsOffline = config.PlayerOffline;
		this.restored = config.Restored;
		this.restoredConfirm = config.Restored_Other;
		this.removed = config.Removed;
		this.removedConfirm = config.Removed_Other;
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.hasPermission(sender) || !this.correctLength(sender, args.size(), 0, 1)) {
			return;
		}
		if (args.size() == 1) {
			Player player = Bukkit.getPlayer(args.get(0));

			if (player == null) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.playerIsOffline);
				return;
			}

			this.permaremove(sender, player);
		} else if (args.size() == 0 && this.isPlayer(sender)) {
			this.permaremove(sender, (Player) sender);
		}
	}

	/**
	 * Permanently removes a player's bending, or restores it if it had already
	 * been permaremoved.
	 *
	 * @param sender The CommandSender who issued the permaremove command
	 * @param player The Player who's bending should be permaremoved
	 */
	private void permaremove(final CommandSender sender, final Player player) {
		BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);

		if (bendingPlayer.isBendingPermanentlyRemoved()) {
			this.bendingPlayerManager.setBendingPermanentlyRemoved(player, false);
			GeneralMethods.sendBrandingMessage(player, ChatColor.GREEN + this.restored);
			if (!(sender instanceof Player) || !sender.getName().equalsIgnoreCase(player.getName())) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.GREEN + this.restoredConfirm.replace("{target}", ChatColor.DARK_AQUA + player.getName() + ChatColor.GREEN));
			}
		} else {
			this.elementManager.clearElements(player);
			this.bendingPlayerManager.setBendingPermanentlyRemoved(player, true);
			GeneralMethods.removeUnusableAbilities(player.getName());
			GeneralMethods.sendBrandingMessage(player, ChatColor.RED + this.removed);
			if (!(sender instanceof Player) || !sender.getName().equalsIgnoreCase(player.getName())) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.removedConfirm.replace("{target}", ChatColor.DARK_AQUA + player.getName() + ChatColor.RED));
			}
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
	public boolean hasPermission(final CommandSender sender) {
		if (!sender.hasPermission("bending.admin.permaremove")) {
			GeneralMethods.sendBrandingMessage(sender, super.noPermissionMessage);
			return false;
		}
		return true;
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (args.size() >= 1 || !sender.hasPermission("bending.command.permaremove")) {
			return new ArrayList<String>();
		}
		final List<String> players = new ArrayList<String>();
		for (final Player p : Bukkit.getOnlinePlayers()) {
			players.add(p.getName());
		}
		return players;
	}
}

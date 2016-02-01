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
 * Executor for /bending remove. Extends {@link PKCommand}.
 */
public class RemoveCommand extends PKCommand {

	public RemoveCommand() {
		super("remove", "/bending remove <Player> [Element]", "This command will remove the element of the targeted [Player]. The player will be able to re-pick their element after this command is run on them, assuming their Bending was not permaremoved.", new String[] { "remove", "rm" });
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if (!hasPermission(sender) || !correctLength(sender, args.size(), 1, 2)) {
			return;
		}

		Player player = Bukkit.getPlayer(args.get(0));
		if (player == null) {
			Element e = Element.getElement(getElement(args.get(0)));
			BendingPlayer senderBPlayer = BendingPlayer.getBendingPlayer(sender.getName());
			
			if (senderBPlayer != null && e != null && sender instanceof Player) {
				if (senderBPlayer.hasElement(e)) {
					senderBPlayer.getElements().remove(e);
					GeneralMethods.saveElements(senderBPlayer);
					GeneralMethods.removeUnusableAbilities(sender.getName());
					
					if (e == Element.CHI) {
						sender.sendMessage(Element.CHI.getColor() + "You have removed your chiblocking.");
						return;
					}
					sender.sendMessage(e.getColor() + "You have removed your " + e.toString().toLowerCase() + "bending.");
					Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(sender, player, e, Result.REMOVE));
					return;
				} else {
					sender.sendMessage(ChatColor.RED + "You do not have that element!");
					return;
				}
			}
			sender.sendMessage(ChatColor.RED + "That player is not online.");
			return;
		}

		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			GeneralMethods.createBendingPlayer(player.getUniqueId(), player.getName());
			bPlayer = BendingPlayer.getBendingPlayer(player);
		}
		if (args.size() == 2) {
			Element e = Element.getElement(getElement(args.get(1)));
			if (e != null) {
				if (!bPlayer.hasElement(e)) {
					sender.sendMessage(ChatColor.DARK_RED + "Targeted player does not have that element");
					return;
				}
				bPlayer.getElements().remove(e);
				GeneralMethods.saveElements(bPlayer);
				GeneralMethods.removeUnusableAbilities(player.getName());
				if (e == Element.CHI) {
					sender.sendMessage(Element.CHI.getColor() + "You have removed the chiblocking of " + ChatColor.DARK_AQUA + player.getName());
					player.sendMessage(Element.CHI.getColor() + "Your chiblocking has been removed by " + ChatColor.DARK_AQUA + sender.getName());
				} else {
					sender.sendMessage(e.getColor() + "You have removed the " + getElement(args.get(1)).toLowerCase() + "bending of " + ChatColor.DARK_AQUA + player.getName());
					player.sendMessage(e.getColor() + "Your " + getElement(args.get(1)).toLowerCase() + "bending has been removed by " + ChatColor.DARK_AQUA + sender.getName());
				}
				Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(sender, player, e, Result.REMOVE));
				return;
			}
		} else if (args.size() == 1) {
			bPlayer.getElements().clear();
			GeneralMethods.saveElements(bPlayer);
			GeneralMethods.removeUnusableAbilities(player.getName());
			sender.sendMessage(ChatColor.YELLOW + "You have removed the bending of " + ChatColor.DARK_AQUA + player.getName());
			player.sendMessage(ChatColor.YELLOW + "Your bending has been removed by " + ChatColor.DARK_AQUA + sender.getName());
			Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(sender, player, null, Result.REMOVE));
		}
	}

	/**
	 * Checks if the CommandSender has the permission 'bending.admin.remove'. If
	 * not, it tells them they don't have permission to use the command.
	 * 
	 * @return True if they have the permission, false otherwise
	 */
	@Override
	public boolean hasPermission(CommandSender sender) {
		if (sender.hasPermission("bending.admin." + getName())) {
			return true;
		}
		sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
		return false;
	}
	
	public String getElement(String string) {
		if (Arrays.asList(Commands.airaliases).contains(string)) return "air";
		if (Arrays.asList(Commands.chialiases).contains(string)) return "chi";
		if (Arrays.asList(Commands.earthaliases).contains(string)) return "earth";
		if (Arrays.asList(Commands.firealiases).contains(string)) return "fire";
		if (Arrays.asList(Commands.wateraliases).contains(string)) return "water";
		return null;
	}
}

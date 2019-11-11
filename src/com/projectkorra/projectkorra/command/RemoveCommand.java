package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.configuration.configs.commands.RemoveCommandConfig;
import com.projectkorra.projectkorra.element.Element;
import com.projectkorra.projectkorra.player.BendingPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Executor for /bending remove. Extends {@link PKCommand}.
 */
public class RemoveCommand extends PKCommand<RemoveCommandConfig> {

	private final String succesfullyRemovedElementSelf, wrongElementSelf, invalidElement, playerOffline, wrongElementTarget, succesfullyRemovedElementTarget, succesfullyRemovedElementTargetConfirm, succesfullyRemovedAllElementsTarget, succesfullyRemovedAllElementsTargetConfirm;

	public RemoveCommand(final RemoveCommandConfig config) {
		super(config, "remove", "/bending remove <Player> [Element]", config.Description, new String[] { "remove", "rm" });

		this.succesfullyRemovedElementSelf = config.RemovedElement;
		this.succesfullyRemovedAllElementsTarget = config.RemovedAllElements_ByOther;
		this.succesfullyRemovedAllElementsTargetConfirm = config.RemovedAllElements_Other;
		this.succesfullyRemovedElementTarget = config.RemovedElement_ByOther;
		this.succesfullyRemovedElementTargetConfirm = config.RemovedAllElements_Other;
		this.invalidElement = config.InvalidElement;
		this.wrongElementSelf = config.WrongElement;
		this.wrongElementTarget = config.WrongElement_Other;
		this.playerOffline = config.PlayerOffline;
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.hasPermission(sender) || !this.correctLength(sender, args.size(), 1, 2)) {
			return;
		}

		Player player = Bukkit.getPlayer(args.get(0));

		if (player == null) {
			if (args.size() != 1) {
				this.help(sender, false);
				return;
			}

			if (!(sender instanceof Player)) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.playerOffline);
				return;
			}

			player = (Player) sender;
			final Element e = this.elementManager.getElement(args.get(0));
			final BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);

			if (e == null) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.invalidElement);
				return;
			}

			if (!this.elementManager.removeElement(player, e)) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.wrongElementSelf);
				return;
			}

			GeneralMethods.removeUnusableAbilities(player.getName());
			GeneralMethods.sendBrandingMessage(sender, e.getColor() + this.succesfullyRemovedElementSelf.replace("{element}", e.getName() + e.getType().getBending()));
			return;
		}

		BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);

		if (args.size() == 2) {
			final Element e = this.elementManager.getElement(args.get(1));

			if (e == null) {
				return;
			}

			if (!this.elementManager.removeElement(player, e)) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.DARK_RED + this.wrongElementTarget.replace("{target}", player.getName()));
				return;
			}

			GeneralMethods.removeUnusableAbilities(player.getName());
			GeneralMethods.sendBrandingMessage(player, e.getColor() + this.succesfullyRemovedElementTarget.replace("{element}", e.getName() + e.getType().getBending()).replace("{sender}", ChatColor.DARK_AQUA + sender.getName() + e.getColor()));
			GeneralMethods.sendBrandingMessage(sender, e.getColor() + this.succesfullyRemovedElementTargetConfirm.replace("{element}", e.getName() + e.getType().getBending()).replace("{target}", ChatColor.DARK_AQUA + player.getName() + e.getColor()));
			return;
		}

		if (args.size() == 1) {
			this.elementManager.clearElements(player);
			GeneralMethods.removeUnusableAbilities(player.getName());

			if (!player.equals(sender)) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.YELLOW + this.succesfullyRemovedAllElementsTargetConfirm.replace("{target}", ChatColor.DARK_AQUA + player.getName() + ChatColor.YELLOW));
			}

			GeneralMethods.sendBrandingMessage(player, ChatColor.YELLOW + this.succesfullyRemovedAllElementsTarget.replace("{sender}", ChatColor.DARK_AQUA + sender.getName() + ChatColor.YELLOW));
		}
	}

	/**
	 * Checks if the CommandSender has the permission 'bending.admin.remove'. If
	 * not, it tells them they don't have permission to use the command.
	 *
	 * @return True if they have the permission, false otherwise
	 */
	@Override
	public boolean hasPermission(final CommandSender sender) {
		if (sender.hasPermission("bending.admin." + this.getName())) {
			return true;
		}
		GeneralMethods.sendBrandingMessage(sender, super.noPermissionMessage);
		return false;
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (args.size() >= 2 || !sender.hasPermission("bending.command.remove")) {
			return new ArrayList<>();
		}
		final List<String> l = new ArrayList<>();
		if (args.size() == 0) {
			for (final Player p : Bukkit.getOnlinePlayers()) {
				l.add(p.getName());
			}
		} else {
			for (Element e : this.elementManager.getAllElements()) {
				l.add(e.getName());
			}
		}
		return l;
	}
}

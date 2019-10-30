package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.configuration.configs.commands.ToggleCommandConfig;
import com.projectkorra.projectkorra.element.Element;
import com.projectkorra.projectkorra.element.SubElement;
import com.projectkorra.projectkorra.player.BendingPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Executor for /bending toggle. Extends {@link PKCommand}.
 */
public class ToggleCommand extends PKCommand<ToggleCommandConfig> {

	private final String toggledOffForAll, toggleOffSelf, toggleOnSelf, toggleOffAll, toggleOnAll, toggledOffSingleElement, toggledOnSingleElement, wrongElementOther, toggledOnOtherElementConfirm, toggledOffOtherElementConfirm, toggledOnOtherElement, toggledOffOtherElement, wrongElement, notFound;

	public ToggleCommand(final ToggleCommandConfig config) {
		super(config, "toggle", "/bending toggle <All/Element/Player> [Player]", config.Description, new String[] { "toggle", "t" });

		this.toggledOffForAll = config.ToggledOffForAll;
		this.toggleOffSelf = config.ToggledOff;
		this.toggleOnSelf = config.ToggledOn;
		this.toggleOffAll = config.ToggledOff_All;
		this.toggleOnAll = config.ToggledOn_All;
		this.toggledOffSingleElement = config.ToggledOffSingleElement;
		this.toggledOnSingleElement = config.ToggledOnSingleElement;
		this.wrongElementOther = config.WrongElement_Other;
		this.toggledOnOtherElementConfirm = config.ToggledOn_Other;
		this.toggledOffOtherElementConfirm = config.ToggledOff_Other;
		this.toggledOnOtherElement = config.ToggledOn_ByOther;
		this.toggledOffOtherElement = config.ToggledOff_ByOther;
		this.wrongElement = config.WrongElement;
		this.notFound = config.PlayerNotFound;
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.correctLength(sender, args.size(), 0, 2)) {
			return;
		} else if (args.size() == 0) { // bending toggle,
			if (!this.hasPermission(sender) || !this.isPlayer(sender)) {
				return;
			}
			if (Commands.isToggledForAll) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.toggledOffForAll);
				return;
			}

			Player player = (Player) sender;
			BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);

			if (bendingPlayer.isToggled()) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.toggleOffSelf);
				bendingPlayer.toggleBending();
			} else {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.GREEN + this.toggleOnSelf);
				bendingPlayer.toggleBending();
			}
		} else if (args.size() == 1) {
			Element element = this.elementManager.getElement(args.get(0));

			if (args.size() == 1 && args.get(0).equalsIgnoreCase("all") && this.hasPermission(sender, "all")) { // bending toggle all.
				if (Commands.isToggledForAll) { // Bending is toggled off for all players.
					Commands.isToggledForAll = false;
					for (final Player player : Bukkit.getOnlinePlayers()) {
						GeneralMethods.sendBrandingMessage(player, ChatColor.GREEN + this.toggleOnAll);
					}
					if (!(sender instanceof Player)) {
						GeneralMethods.sendBrandingMessage(sender, ChatColor.GREEN + this.toggleOnAll);
					}

				} else {
					Commands.isToggledForAll = true;
					for (final Player player : Bukkit.getOnlinePlayers()) {
						GeneralMethods.sendBrandingMessage(player, ChatColor.RED + this.toggleOffAll);
					}
					if (!(sender instanceof Player)) {
						GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.toggleOffAll);
					}
				}
			} else if (sender instanceof Player && args.size() == 1 && element != null && !(element instanceof SubElement)) {
				Player player = (Player) sender;
				BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);

				if (!bendingPlayer.hasElement(element)) {
					GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.wrongElement);
					return;
				}

				bendingPlayer.toggleElement(element);

				if (bendingPlayer.isElementToggled(element)) {
					GeneralMethods.sendBrandingMessage(sender, element.getColor() + this.toggledOnSingleElement.replace("{element}", element.getName() + (element.getType() != null ? element.getType().getBending() : "")));
				} else {
					GeneralMethods.sendBrandingMessage(sender, element.getColor() + this.toggledOffSingleElement.replace("{element}", element.getName() + (element.getType() != null ? element.getType().getBending() : "")));
				}
			} else {
				this.help(sender, false);
			}

		} else if (sender instanceof Player && args.size() == 2 && this.elementManager.getElement(args.get(0)) != null && !(this.elementManager.getElement(args.get(0)) instanceof SubElement)) {
			final Player target = Bukkit.getPlayer(args.get(1));
			if (!this.hasAdminPermission(sender)) {
				return;
			}
			if (target == null) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.notFound);
				return;
			}
			Element element = this.elementManager.getElement(args.get(0));
			BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(target);

			if (!bendingPlayer.hasElement(element)) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.wrongElementOther.replace("{target}", ChatColor.DARK_AQUA + target.getName() + ChatColor.RED));
				return;
			}

			if (bendingPlayer.isElementToggled(element)) {
				GeneralMethods.sendBrandingMessage(sender, element.getColor() + this.toggledOffOtherElementConfirm.replace("{target}", target.getName()).replace("{element}", element.getName() + (element.getType() != null ? element.getType().getBending() : "")));
				GeneralMethods.sendBrandingMessage(target, element.getColor() + this.toggledOffOtherElement.replace("{element}", element.getName() + (element.getType() != null ? element.getType().getBending() : "")).replace("{sender}", ChatColor.DARK_AQUA + sender.getName()));
			} else {
				GeneralMethods.sendBrandingMessage(sender, element.getColor() + this.toggledOnOtherElementConfirm.replace("{target}", target.getName()).replace("{element}", element.getName() + (element.getType() != null ? element.getType().getBending() : "")));
				GeneralMethods.sendBrandingMessage(target, element.getColor() + this.toggledOnOtherElement.replace("{element}", element.getName() + (element.getType() != null ? element.getType().getBending() : "")).replace("{sender}", ChatColor.DARK_AQUA + sender.getName()));
			}

			bendingPlayer.toggleElement(element);
		} else {
			this.help(sender, false);
		}
	}

	public boolean hasAdminPermission(final CommandSender sender) {
		if (!sender.hasPermission("bending.admin.toggle")) {
			GeneralMethods.sendBrandingMessage(sender, super.noPermissionMessage);
			return false;
		}
		return true;
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (args.size() >= 2 || !sender.hasPermission("bending.command.toggle.others")) {
			return new ArrayList<String>();
		}
		final List<String> l = new ArrayList<String>();
		if (args.size() == 0) {
			final List<String> elements = new ArrayList<String>();
			for (Element e : this.elementManager.getElements()) {
				elements.add(e.getName());
			}
			Collections.sort(elements);
			l.add("All");
			l.addAll(elements);
		} else {
			for (final Player p : Bukkit.getOnlinePlayers()) {
				l.add(p.getName());
			}
		}
		return l;
	}
}

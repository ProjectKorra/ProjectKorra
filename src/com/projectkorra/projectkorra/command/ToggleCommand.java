package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.util.PassiveManager;
import com.projectkorra.projectkorra.configuration.ConfigManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Executor for /bending toggle. Extends {@link PKCommand}.
 */
public class ToggleCommand extends PKCommand {

	private String toggledOffForAll, toggleOffSelf, toggleOnSelf, toggleOffAll, toggleOnAll,
			toggledOffSingleElement, toggledOnSingleElement, wrongElementOther,
			toggledOnOtherElementConfirm, toggledOffOtherElementConfirm, toggledOnOtherElement,
			toggledOffOtherElement, wrongElement, notFound;

	//config.addDefault("Commands.Toggle.Other.ToggledOnElementConfirm", "You've toggled on {target}'s {element}");
	//config.addDefault("Commands.Toggle.Other.ToggledOffElementConfirm", "You've toggled off {target}'s {element}");
	//config.addDefault("Commands.Toggle.Other.ToggledOnElementConfirm", "Your {element} has been toggled on by {sender}.");
	//config.addDefault("Commands.Toggle.Other.ToggledOffElementConfirm", "Your {element} has been toggled off by {sender}.");

	public ToggleCommand() {
		super("toggle", "/bending toggle <All/Element/Player> [Player]", ConfigManager.languageConfig.get().getString("Commands.Toggle.Description"), new String[] { "toggle", "t" });

		FileConfiguration c = ConfigManager.languageConfig.get();

		this.toggledOffForAll = c.getString("Commands.Toggle.All.ToggledOffForAll");
		this.toggleOffSelf = c.getString("Commands.Toggle.ToggledOff");
		this.toggleOnSelf = c.getString("Commands.Toggle.ToggledOn");
		this.toggleOffAll = c.getString("Commands.Toggle.All.ToggleOff");
		this.toggleOnAll = c.getString("Commands.Toggle.All.ToggleOn");
		this.toggledOffSingleElement = c.getString("Commands.Toggle.ToggleOffSingleElement");
		this.toggledOnSingleElement = c.getString("Commands.Toggle.ToggleOnSingleElement");
		this.wrongElementOther = c.getString("Commands.Toggle.Other.WrongElement");
		this.toggledOnOtherElementConfirm = c.getString("Commands.Toggle.Other.ToggledOnElementConfirm");
		this.toggledOffOtherElementConfirm = c.getString("Commands.Toggle.Other.ToggledOffElementConfirm");
		this.toggledOnOtherElement = c.getString("Commands.Toggle.Other.ToggledOnElementByOther");
		this.toggledOffOtherElement = c.getString("Commands.Toggle.Other.ToggledOffElementByOther");
		this.wrongElement = c.getString("Commands.Toggle.WrongElement");
		this.notFound = c.getString("Commands.Toggle.Other.PlayerNotFound");
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
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + toggledOffForAll);
				return;
			}
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(sender.getName());
			if (bPlayer == null) {
				GeneralMethods.createBendingPlayer(((Player) sender).getUniqueId(), sender.getName());
				bPlayer = BendingPlayer.getBendingPlayer(sender.getName());
			}
			if (bPlayer.isToggled()) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + toggleOffSelf);
				bPlayer.toggleBending();
			} else {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.GREEN + toggleOnSelf);
				bPlayer.toggleBending();
			}
		} else if (args.size() == 1) {
			if (args.size() == 1 && args.get(0).equalsIgnoreCase("all") && hasPermission(sender, "all")) { //bending toggle all
				if (Commands.isToggledForAll) { // Bending is toggled off for all players.
					Commands.isToggledForAll = false;
					for (Player player : Bukkit.getOnlinePlayers()) {
						GeneralMethods.sendBrandingMessage(player, ChatColor.GREEN + toggleOnAll);
						PassiveManager.registerPassives(player); // TODO: This is a temporary fix. Passives currently need to be re-registered in multiple places.
					}
					if (!(sender instanceof Player))
						GeneralMethods.sendBrandingMessage(sender, ChatColor.GREEN + toggleOnAll);
					
				} else {
					Commands.isToggledForAll = true;
					for (Player player : Bukkit.getOnlinePlayers()) {
						GeneralMethods.sendBrandingMessage(player, ChatColor.RED + toggleOffAll);
					}
					if (!(sender instanceof Player))
						GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + toggleOffAll);
				}
			} else if (sender instanceof Player && args.size() == 1 && Element.fromString(args.get(0)) != null && !(Element.fromString(args.get(0)) instanceof SubElement)) {
				if (!BendingPlayer.getBendingPlayer(sender.getName()).hasElement(Element.fromString(args.get(0)))) {
					GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + wrongElement);
					return;
				}
				Element e = Element.fromString(args.get(0));
				ChatColor color = e != null ? e.getColor() : null;
				BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(sender.getName());
				bPlayer.toggleElement(e);

				if (bPlayer.isElementToggled(e)) {
					GeneralMethods.sendBrandingMessage(sender, color + toggledOnSingleElement.replace("{element}", e.getName() + (e.getType() != null ? e.getType().getBending() : "")));
				} else {
					GeneralMethods.sendBrandingMessage(sender, color + toggledOffSingleElement.replace("{element}", e.getName() + (e.getType() != null ? e.getType().getBending() : "")));
				}
			} else {
				help(sender, false);
			}
			
		} else if (sender instanceof Player && args.size() == 2 && Element.fromString(args.get(0)) != null && !(Element.fromString(args.get(0)) instanceof SubElement)) {
			Player target = Bukkit.getPlayer(args.get(1));
			if (!hasAdminPermission(sender))
				return;
			if (target == null) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + notFound);
				return;
			}
			if (!BendingPlayer.getBendingPlayer(target.getName()).hasElement(Element.fromString(args.get(0)))) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + wrongElementOther.replace("{target}", ChatColor.DARK_AQUA + target.getName() + ChatColor.RED));
				return;
			}
			Element e = Element.fromString(args.get(0));
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(target.getName());
			ChatColor color = e != null ? e.getColor() : null;

			if (bPlayer.isElementToggled(e)) {
				GeneralMethods.sendBrandingMessage(sender, color + this.toggledOffOtherElementConfirm.replace("{target}", target.getName()).replace("{element}", e.getName() + (e.getType() != null ? e.getType().getBending() : "")));
				GeneralMethods.sendBrandingMessage(target, color + this.toggledOffOtherElement.replace("{element}", e.getName() + (e.getType() != null ? e.getType().getBending() : "")).replace("{sender}", ChatColor.DARK_AQUA + sender.getName()));
			} else {
				GeneralMethods.sendBrandingMessage(sender, color + this.toggledOnOtherElementConfirm.replace("{target}", target.getName()).replace("{element}", e.getName() + (e.getType() != null ? e.getType().getBending() : "")));
				GeneralMethods.sendBrandingMessage(target, color + this.toggledOnOtherElement.replace("{element}", e.getName() + (e.getType() != null ? e.getType().getBending() : "")).replace("{sender}", ChatColor.DARK_AQUA + sender.getName()));
			}
			bPlayer.toggleElement(e);
		} else {
			help(sender, false);
		}
	}

	public boolean hasAdminPermission(CommandSender sender) {
		if (!sender.hasPermission("bending.admin.toggle")) {
			GeneralMethods.sendBrandingMessage(sender, super.noPermissionMessage);
			return false;
		}
		return true;
	}

	@Override
	protected List<String> getTabCompletion(CommandSender sender, List<String> args) {
		if (args.size() >= 2 || !sender.hasPermission("bending.command.toggle.others"))
			return new ArrayList<String>();
		List<String> l = new ArrayList<String>();
		if (args.size() == 0) {
			List<String> elements = new ArrayList<String>();
			for (Element e : Element.getAllElements()) {
				elements.add(e.getName());
			}
			Collections.sort(elements);
			l.add("All");
			l.addAll(elements);
		} else {
			for (Player p : Bukkit.getOnlinePlayers()) {
				l.add(p.getName());
			}
		}
		return l;
	}
}

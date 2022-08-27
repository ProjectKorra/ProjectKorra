package com.projectkorra.projectkorra.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.configuration.ConfigManager;

/**
 * Executor for /bending toggle. Extends {@link PKCommand}.
 */
public class ToggleCommand extends PKCommand {

	private final String toggledOffForAll, toggleOffSelf, toggleOnSelf, toggleOffAll, toggleOnAll, toggledOffSingleElement, toggledOnSingleElement, toggledOnSingleElementPassive, toggledOffSingleElementPassive, wrongElementOther, toggledOnOtherElementConfirm, toggledOffOtherElementConfirm, toggledOnOtherElement, toggledOffOtherElement, wrongElement, notFound, toggleAllPassivesOffSelf, toggleAllPassivesOnSelf;

	public ToggleCommand() {
		super("toggle", "/bending toggle <All/Element> [Player]", ConfigManager.languageConfig.get().getString("Commands.Toggle.Description"), new String[] { "toggle", "t" });

		final FileConfiguration c = ConfigManager.languageConfig.get();

		this.toggledOffForAll = c.getString("Commands.Toggle.All.ToggledOffForAll");
		this.toggleOffSelf = c.getString("Commands.Toggle.ToggledOff");
		this.toggleOnSelf = c.getString("Commands.Toggle.ToggledOn");
		this.toggleOffAll = c.getString("Commands.Toggle.All.ToggleOff");
		this.toggleOnAll = c.getString("Commands.Toggle.All.ToggleOn");
		this.toggledOffSingleElement = c.getString("Commands.Toggle.ToggleOffSingleElement");
		this.toggledOnSingleElement = c.getString("Commands.Toggle.ToggleOnSingleElement");
		this.toggledOffSingleElementPassive = c.getString("Commands.Toggle.ToggleOffSingleElementPassive");
		this.toggledOnSingleElementPassive = c.getString("Commands.Toggle.ToggleOnSingleElementPassive");
		this.wrongElementOther = c.getString("Commands.Toggle.Other.WrongElement");
		this.toggledOnOtherElementConfirm = c.getString("Commands.Toggle.Other.ToggledOnElementConfirm");
		this.toggledOffOtherElementConfirm = c.getString("Commands.Toggle.Other.ToggledOffElementConfirm");
		this.toggledOnOtherElement = c.getString("Commands.Toggle.Other.ToggledOnElementByOther");
		this.toggledOffOtherElement = c.getString("Commands.Toggle.Other.ToggledOffElementByOther");
		this.wrongElement = c.getString("Commands.Toggle.WrongElement");
		this.notFound = c.getString("Commands.Toggle.Other.PlayerNotFound");
		this.toggleAllPassivesOffSelf = c.getString("Commands.Toggle.ToggledPassivesOff");
		this.toggleAllPassivesOnSelf =  c.getString("Commands.Toggle.ToggledPassivesOn");
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
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(sender.getName());

			if (bPlayer.isToggled()) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.toggleOffSelf);
				bPlayer.toggleBending();
			} else {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.GREEN + this.toggleOnSelf);
				bPlayer.toggleBending();
			}
		} else if (args.size() == 1) {
			String toggleableParam = args.get(0);
			if (toggleableParam.equalsIgnoreCase("all") && this.hasPermission(sender, "all")) { // bending toggle all.
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
			} else if (sender instanceof Player && args.size() == 1 && Element.fromString(toggleableParam) != null && !(Element.fromString(toggleableParam) instanceof SubElement)) {
				if (!BendingPlayer.getBendingPlayer(sender.getName()).hasElement(Element.fromString(toggleableParam))) {
					GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.wrongElement);
					return;
				}
				final Element e = Element.fromString(toggleableParam);
				final ChatColor color = e != null ? e.getColor() : null;
				final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(sender.getName());
				bPlayer.toggleElement(e);

				if (bPlayer.isElementToggled(e)) {
					GeneralMethods.sendBrandingMessage(sender, color + this.toggledOnSingleElement.replace("{element}", e.getName() + (e.getType() != null ? e.getType().getBending() : "")));
				} else {
					GeneralMethods.sendBrandingMessage(sender, color + this.toggledOffSingleElement.replace("{element}", e.getName() + (e.getType() != null ? e.getType().getBending() : "")));
				}
			}  else if (sender instanceof Player && args.size() == 1 && !toggleableParam.equalsIgnoreCase("Passives") && Element.fromString(toggleableParam.split("passives")[0]) != null && !(Element.fromString(toggleableParam.split("passives")[0]) instanceof SubElement)) {
				if (!BendingPlayer.getBendingPlayer(sender.getName()).hasElement(Element.fromString(toggleableParam.split("passives")[0]))) {
					GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.wrongElement);
					return;
				}
				final Element e = Element.fromString(toggleableParam.split("passives")[0]);
				final ChatColor color = e != null ? e.getColor() : null;
				final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(sender.getName());
				bPlayer.togglePassive(e);

				if (bPlayer.isPassiveToggled(e)) {
					GeneralMethods.sendBrandingMessage(sender, color + this.toggledOnSingleElementPassive.replace("{element}", e.getName() + (e.getType() != null ? e.getType().getBending() : "")));
				} else {
					GeneralMethods.sendBrandingMessage(sender, color + this.toggledOffSingleElementPassive.replace("{element}", e.getName() + (e.getType() != null ? e.getType().getBending() : "")));
				}
			} else if (sender instanceof Player && args.size() == 1 && toggleableParam.equalsIgnoreCase("Passives")) {
				BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(sender.getName());

				if (bPlayer.isToggledPassives()) {
					GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.toggleAllPassivesOffSelf);
					bPlayer.toggleAllPassives();
				} else {
					GeneralMethods.sendBrandingMessage(sender, ChatColor.GREEN + this.toggleAllPassivesOnSelf);
					bPlayer.toggleAllPassives();
				}
			} else {
				this.help(sender, false);
			}

		} else if (sender instanceof Player && args.size() == 2 && Element.fromString(args.get(0)) != null && !(Element.fromString(args.get(0)) instanceof SubElement)) {
			Element e = Element.fromString(args.get(0));
			final Player target = Bukkit.getPlayer(args.get(1));
			if (!this.hasAdminPermission(sender)) {
				return;
			}
			if (target == null) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.notFound);
				return;
			}
			if (!BendingPlayer.getBendingPlayer(target.getName()).hasElement(e)) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.wrongElementOther.replace("{target}", ChatColor.DARK_AQUA + target.getName() + ChatColor.RED));
				return;
			}
			final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(target.getName());
			final ChatColor color = e != null ? e.getColor() : null;

			if (bPlayer.isElementToggled(e)) {
				GeneralMethods.sendBrandingMessage(sender, color + this.toggledOffOtherElementConfirm.replace("{target}", target.getName()).replace("{element}", e.getName() + (e.getType() != null ? e.getType().getBending() : "")));
				GeneralMethods.sendBrandingMessage(target, color + this.toggledOffOtherElement.replace("{element}", e.getName() + (e.getType() != null ? e.getType().getBending() : "")).replace("{sender}", ChatColor.DARK_AQUA + sender.getName()));
			} else {
				GeneralMethods.sendBrandingMessage(sender, color + this.toggledOnOtherElementConfirm.replace("{target}", target.getName()).replace("{element}", e.getName() + (e.getType() != null ? e.getType().getBending() : "")));
				GeneralMethods.sendBrandingMessage(target, color + this.toggledOnOtherElement.replace("{element}", e.getName() + (e.getType() != null ? e.getType().getBending() : "")).replace("{sender}", ChatColor.DARK_AQUA + sender.getName()));
			}
			bPlayer.toggleElement(e);
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
			for (final Element e : Element.getAllElements()) {
				elements.add(e.getName());
				elements.add(e.getName() + "Passives");
			}
			Collections.sort(elements);
			l.add("All");
			l.add("Passives");
			l.addAll(elements);
		} else {
			for (final Player p : Bukkit.getOnlinePlayers()) {
				l.add(p.getName());
			}
		}
		return l;
	}
}

package com.projectkorra.projectkorra.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.util.ChatUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.configuration.ConfigManager;

/**
 * Executor for /bending toggle. Extends {@link PKCommand}.
 */
public class ToggleCommand extends PKCommand {

	private final String toggledOffForAll, toggleOffSelf, toggleOnSelf, toggleOffAll, toggleOnAll, toggledOffSingleElement, toggledOnSingleElement, toggledOnSingleElementPassive, toggledOffSingleElementPassive, wrongElementOther, toggledOnOtherElementConfirm, toggledOffOtherElementConfirm, toggledOnOtherElement, toggledOffOtherElement, wrongElement, notFound, toggleAllPassivesOffSelf, toggleAllPassivesOnSelf;

	private Set<Element> cachedPassiveElements = new HashSet<>();

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

		//1 tick later because commands are created before abilities are
		Bukkit.getScheduler().runTaskLater(ProjectKorra.plugin, () ->
				cachedPassiveElements = CoreAbility.getAbilities().stream().filter(ab -> ab instanceof PassiveAbility)
						.map(Ability::getElement).collect(Collectors.toSet())
		, 1L);
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
				ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.toggledOffForAll);
				return;
			}
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(sender.getName());

			if (bPlayer.isToggled()) {
				ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.toggleOffSelf);
				bPlayer.toggleBending();
			} else {
				ChatUtil.sendBrandingMessage(sender, ChatColor.GREEN + this.toggleOnSelf);
				bPlayer.toggleBending();
			}
		} else if (args.size() == 1) {
			String toggleableParam = args.get(0).toLowerCase();
			if (toggleableParam.equals("all") && this.hasPermission(sender, "all")) { // bending toggle all.
				if (Commands.isToggledForAll) { // Bending is toggled off for all players.
					Commands.isToggledForAll = false;
					for (final Player player : Bukkit.getOnlinePlayers()) {
						ChatUtil.sendBrandingMessage(player, ChatColor.GREEN + this.toggleOnAll);
					}
					if (!(sender instanceof Player)) {
						ChatUtil.sendBrandingMessage(sender, ChatColor.GREEN + this.toggleOnAll);
					}

				} else {
					Commands.isToggledForAll = true;
					for (final Player player : Bukkit.getOnlinePlayers()) {
						ChatUtil.sendBrandingMessage(player, ChatColor.RED + this.toggleOffAll);
					}
					if (!(sender instanceof Player)) {
						ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.toggleOffAll);
					}
				}
			} else if (sender instanceof Player && args.size() == 1 && Element.fromString(toggleableParam) != null && !(Element.fromString(toggleableParam) instanceof SubElement)) {
				final Element e = Element.fromString(toggleableParam);
				if (!BendingPlayer.getBendingPlayer(sender.getName()).hasElement(e)) {
					ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.wrongElement);
					return;
				}

				final ChatColor color = e.getColor();
				final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(sender.getName());
				bPlayer.toggleElement(e);

				if (bPlayer.isElementToggled(e)) {
					ChatUtil.sendBrandingMessage(sender, color + this.toggledOnSingleElement.replace("{element}", e.getName() + (e.getType() != null ? e.getType().getBending() : "")));
				} else {
					ChatUtil.sendBrandingMessage(sender, color + this.toggledOffSingleElement.replace("{element}", e.getName() + (e.getType() != null ? e.getType().getBending() : "")));
				}
			}  else if (sender instanceof Player && args.size() == 1 && !toggleableParam.equals("passives") && Element.fromString(toggleableParam.split("passives")[0]) != null) {
				final Element e = Element.fromString(toggleableParam.split("passives")[0]);
				if (!BendingPlayer.getBendingPlayer(sender.getName()).hasElement(e)) {
					ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.wrongElement);
					return;
				}
				final ChatColor color = e.getColor();
				final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(sender.getName());
				bPlayer.togglePassive(e);

				if (bPlayer.isPassiveToggled(e)) {
					ChatUtil.sendBrandingMessage(sender, color + this.toggledOnSingleElementPassive.replace("{element}", e.getName() + (e.getType() != null ? e.getType().getBending() : "")));
				} else {
					ChatUtil.sendBrandingMessage(sender, color + this.toggledOffSingleElementPassive.replace("{element}", e.getName() + (e.getType() != null ? e.getType().getBending() : "")));
				}
			} else if (sender instanceof Player && args.size() == 1 && toggleableParam.equals("passives")) {
				BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(sender.getName());

				if (bPlayer.isToggledPassives()) {
					ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.toggleAllPassivesOffSelf);
				} else {
					ChatUtil.sendBrandingMessage(sender, ChatColor.GREEN + this.toggleAllPassivesOnSelf);
				}
				bPlayer.toggleAllPassives();
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
				ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.notFound);
				return;
			}
			if (!BendingPlayer.getBendingPlayer(target.getName()).hasElement(e)) {
				ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.wrongElementOther.replace("{target}", ChatColor.DARK_AQUA + target.getName() + ChatColor.RED));
				return;
			}
			final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(target.getName());
			final ChatColor color = e.getColor();

			if (bPlayer.isElementToggled(e)) {
				ChatUtil.sendBrandingMessage(sender, color + this.toggledOffOtherElementConfirm.replace("{target}", target.getName()).replace("{element}", e.getName() + (e.getType() != null ? e.getType().getBending() : "")));
				ChatUtil.sendBrandingMessage(target, color + this.toggledOffOtherElement.replace("{element}", e.getName() + (e.getType() != null ? e.getType().getBending() : "")).replace("{sender}", ChatColor.DARK_AQUA + sender.getName()));
			} else {
				ChatUtil.sendBrandingMessage(sender, color + this.toggledOnOtherElementConfirm.replace("{target}", target.getName()).replace("{element}", e.getName() + (e.getType() != null ? e.getType().getBending() : "")));
				ChatUtil.sendBrandingMessage(target, color + this.toggledOnOtherElement.replace("{element}", e.getName() + (e.getType() != null ? e.getType().getBending() : "")).replace("{sender}", ChatColor.DARK_AQUA + sender.getName()));
			}
			bPlayer.toggleElement(e);
		} else {
			this.help(sender, false);
		}
	}

	public boolean hasAdminPermission(final CommandSender sender) {
		if (!sender.hasPermission("bending.admin.toggle")) {
			ChatUtil.sendBrandingMessage(sender, super.noPermissionMessage);
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
			}
			cachedPassiveElements.forEach(e -> elements.add(e.getName() + "Passives"));
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

package com.projectkorra.projectkorra.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.ComboAbility;
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
		super("toggle", "/bending toggle <All/Element/Ability> [Player]", ConfigManager.languageConfig.get().getString("Commands.Toggle.Description"), new String[] { "toggle", "t" });

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
		}

		// 1. /bending toggle (Self - only for players)
		if (args.size() == 0) {
			if (!this.isPlayer(sender)) {
				ChatUtil.sendBrandingMessage(sender, ChatColor.RED + "You must be a player to use this command without arguments.");
				return;
			}
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer((Player) sender);
			if (bPlayer.isToggled()) {
				ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.toggleOffSelf);
				bPlayer.toggleBending();
			} else {
				ChatUtil.sendBrandingMessage(sender, ChatColor.GREEN + this.toggleOnSelf);
				bPlayer.toggleBending();
			}
			return;
		}

		// 2. Argument Parsing
		String arg0 = args.get(0).toLowerCase();
		Player target = (args.size() == 2) ? Bukkit.getPlayer(args.get(1)) : (sender instanceof Player ? (Player) sender : null);

		// Handle "All" toggle (Permissions required)
		if ((arg0.equals("all") || arg0.equals("on") || arg0.equals("off")) && this.hasPermission(sender, "all")) {
			final boolean newState = arg0.equals("all") ? !Commands.isToggledForAll : arg0.equals("on");
			Commands.isToggledForAll = newState;
			final ChatColor color = newState ? ChatColor.GREEN : ChatColor.RED;
			final String msg = newState ? this.toggleOnAll : this.toggleOffAll;
			for (Player p : Bukkit.getOnlinePlayers()) ChatUtil.sendBrandingMessage(p, color + msg);
			if (!(sender instanceof Player)) ChatUtil.sendBrandingMessage(sender, color + msg);
			return;
		}

		// Handle Target Validation
		if (args.size() == 2 && target == null) {
			ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.notFound);
			return;
		}
		if (target == null) {
			this.help(sender, false);
			return;
		}

		// Admin Permission Check for others
		if (!target.equals(sender) && !this.hasAdminPermission(sender)) {
			return;
		}

		// 3. Process Abilities, Elements, and Passives
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(target);
		final CoreAbility coreAbil = CoreAbility.getAbility(arg0);
		final Element element = Element.fromString(arg0);

		// Ability Toggle
		if (coreAbil != null && coreAbil.isEnabled() && !(coreAbil instanceof ComboAbility) && !coreAbil.isHiddenAbility()) {
			bPlayer.toggleAbility(coreAbil);
			ChatColor color = coreAbil.getElement() != null ? coreAbil.getElement().getColor() : ChatColor.WHITE;
			String msg = bPlayer.isAbilityToggled(coreAbil) ? this.toggledOnSingleElement : this.toggledOffSingleElement;
			ChatUtil.sendBrandingMessage(sender, color + msg.replace("{element}", coreAbil.getName()));
		}
		// Element Toggle
		else if (element != null && !(element instanceof SubElement)) {
			if (!bPlayer.hasElement(element)) {
				ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.wrongElement);
				return;
			}
			bPlayer.toggleElement(element);
			ChatColor color = element.getColor();
			String msg = bPlayer.isElementToggled(element) ? this.toggledOnSingleElement : this.toggledOffSingleElement;
			ChatUtil.sendBrandingMessage(sender, color + msg.replace("{element}", element.getName() + (element.getType() != null ? element.getType().getBending() : "")));
		}
		// Passive Toggle
		else if (arg0.equals("passives")) {
			bPlayer.toggleAllPassives();
			ChatUtil.sendBrandingMessage(sender, bPlayer.isToggledPassives() ? ChatColor.GREEN + this.toggleAllPassivesOnSelf : ChatColor.RED + this.toggleAllPassivesOffSelf);
		}
		// Help fallback
		else {
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
		// 1. Basic permission and size check
		if (args.size() > 2 || !sender.hasPermission("bending.command.toggle")) {
			return new ArrayList<>();
		}

		final List<String> l = new ArrayList<>();

		// 2. Logic for the FIRST argument
		if (args.size() == 0) {
			final List<String> elements = new ArrayList<>();
			final List<String> abilities = new ArrayList<>();

			for (final Element e : Element.getAllElements()) {
				elements.add(e.getName());
			}

			CoreAbility.getAbilitiesByName().stream()
					.filter(ab -> ab.isEnabled() && !ab.isHiddenAbility() && !(ab instanceof ComboAbility))
					.map(CoreAbility::getName)
					.distinct()
					.forEach(abilities::add);

			if (this.cachedPassiveElements != null) {
				this.cachedPassiveElements.forEach(e -> elements.add(e.getName() + "Passives"));
			}

			Collections.sort(elements);
			Collections.sort(abilities);

			l.add("All");
			l.add("On");
			l.add("Off");
			l.add("Passives");
			l.addAll(elements);
			l.addAll(abilities);
		}
		// 3. Logic for the SECOND argument: ONLY add players
		else if (args.size() == 1) {
			if (!this.hasAdminPermission(sender)) {
				return new ArrayList<>();
			}
			// Ensure the list is empty before adding players
			l.clear();
			for (final Player p : Bukkit.getOnlinePlayers()) {
				l.add(p.getName());
			}
		}
		return l;
	}
}

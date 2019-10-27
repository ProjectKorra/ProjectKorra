package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.commands.AddCommandConfig;
import com.projectkorra.projectkorra.configuration.configs.properties.CommandPropertiesConfig;
import com.projectkorra.projectkorra.element.Element;
import com.projectkorra.projectkorra.element.SubElement;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent.Result;
import com.projectkorra.projectkorra.player.BendingPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Executor for /bending add. Extends {@link PKCommand}.
 */
public class AddCommand extends PKCommand<AddCommandConfig> {

	private final String playerNotFound;
	private final String invalidElement;
	private final String addedOther;
	private final String addedOtherVowel;
	private final String added;
	private final String addedVowel;
	private final String alreadyHasElementOther;
	private final String alreadyHasElement;
	private final String alreadyHasSubElementOther;
	private final String alreadyHasSubElement;
	private final String addedOtherAll;
	private final String addedAll;
	private final String alreadyHasAllElementsOther;
	private final String alreadyHasAllElements;

	public AddCommand(final AddCommandConfig config) {
		super(config, "add", "/bending add <Element/SubElement> [Player]", config.Description, new String[] { "add", "a" });

		this.playerNotFound = config.PlayerNotFound;
		this.invalidElement = config.InvalidElement;
		this.addedOther = config.SuccessfullyAdded_Other;
		this.addedOtherVowel = config.SuccessfullyAddedVowel_Other;
		this.added = config.SuccessfullyAdded;
		this.addedVowel = config.SuccessfullyAddedVowel;
		this.addedOtherAll = config.SuccessfullyAddedAll_Other;
		this.addedAll = config.SuccessfullyAddedAll;
		this.alreadyHasElementOther = config.AlreadyHasElement_Other;
		this.alreadyHasElement = config.AlreadyHasElement;
		this.alreadyHasSubElementOther = config.AlreadyHasSubElement_Other;
		this.alreadyHasSubElement = config.AlreadyHasSubElement;
		this.alreadyHasAllElementsOther = config.AlreadyHasAllElements_Other;
		this.alreadyHasAllElements = config.AlreadyHasAllElements;
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.correctLength(sender, args.size(), 1, 2)) {
			return;
		} else if (args.size() == 1) { // bending add element.
			if (!this.hasPermission(sender) || !this.isPlayer(sender)) {
				return;
			}
			this.add(sender, (Player) sender, args.get(0).toLowerCase());
		} else if (args.size() == 2) { // bending add element combo.
			if (!this.hasPermission(sender, "others")) {
				return;
			}
			final Player player = Bukkit.getPlayer(args.get(1));
			if (player == null) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.playerNotFound);
				return;
			}
			this.add(sender, player, args.get(0).toLowerCase());
		}
	}

	/**
	 * Adds the ability to bend an element to a player.
	 *
	 * @param sender      The CommandSender who issued the add command
	 * @param target      The player to add the element to
	 * @param elementName The element to add
	 */
	private void add(final CommandSender sender, final Player target, final String elementName) {

		BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(target);

		if (bendingPlayer.isBendingRemoved()) {
			GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + ConfigManager.getConfig(CommandPropertiesConfig.class).BendingPermanentlyRemoved_Other);
			return;
		}

		if (elementName.toLowerCase().equals("all")) {
			final StringBuilder elements = new StringBuilder();
			List<Element> added = new LinkedList<>();

			for (Element element : this.elementManager.getElements()) {
				if (bendingPlayer.hasElement(element) || element.equals(this.elementManager.getAvatar())) {
					continue;
				}

				this.elementManager.addElement(target, element);
				added.add(element);

				if (elements.length() > 1) {
					elements.append(ChatColor.YELLOW + ", ");
				}

				elements.append(element.getColor() + element.getName());

				Bukkit.getPluginManager().callEvent(new PlayerChangeElementEvent(sender, target, element, Result.ADD));
			}

			if (added.size() > 0) {
				if (!(sender instanceof Player) || !(sender).equals(target)) {
					GeneralMethods.sendBrandingMessage(sender, ChatColor.YELLOW + this.addedOtherAll.replace("{target}", ChatColor.DARK_AQUA + target.getName() + ChatColor.YELLOW) + elements);
					GeneralMethods.sendBrandingMessage(target, ChatColor.YELLOW + this.addedAll + elements);
				} else {
					GeneralMethods.sendBrandingMessage(target, ChatColor.YELLOW + this.addedAll + elements);
				}
			} else {
				if (!(sender instanceof Player) || !(sender).equals(target)) {
					GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.alreadyHasAllElementsOther.replace("{target}", ChatColor.DARK_AQUA + target.getName() + ChatColor.RED));
				} else {
					GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.alreadyHasAllElements);
				}
			}

		} else {

			// get the [sub]element.
			Element e = this.elementManager.getElement(elementName);

			if (e == null) {
				return;
			}

			List<Element> adding = new LinkedList<>();
			adding.add(e);

			if (e.equals(this.elementManager.getAvatar())) {
				adding.clear();
				adding.add(this.elementManager.getAir());
				adding.add(this.elementManager.getEarth());
				adding.add(this.elementManager.getFire());
				adding.add(this.elementManager.getWater());
			}

			List<Element> added = new LinkedList<>();

			for (Element elem : adding) {

				if (bendingPlayer.hasElement(elem)) {
					continue;
				}

				this.elementManager.addElement(target, elem);
				added.add(elem);

				Bukkit.getPluginManager().callEvent(new PlayerChangeElementEvent(sender, target, elem, Result.ADD));
			}

			if (added.isEmpty()) {
				if (!(sender instanceof Player) || !(sender).equals(target)) {
					if (adding.size() == 1 && adding.get(0) instanceof SubElement) {
						GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.alreadyHasSubElementOther.replace("{target}", ChatColor.DARK_AQUA + target.getName() + ChatColor.RED));
					} else {
						GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.alreadyHasElementOther.replace("{target}", ChatColor.DARK_AQUA + target.getName() + ChatColor.RED));
					}
				} else {
					if (adding.size() == 1 && adding.get(0) instanceof SubElement) {
						GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.alreadyHasSubElement);
					} else {
						GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.alreadyHasElement);
					}
				}

				return;
			}

			for (Element elem : added) {
				ChatColor color = elem.getColor();
				boolean vowel = GeneralMethods.isVowel(ChatColor.stripColor(elem.getName()).charAt(0));

				if (!(sender instanceof Player) || !(sender).equals(target)) {
					if (vowel) {
						GeneralMethods.sendBrandingMessage(sender, color + this.addedOtherVowel.replace("{target}", ChatColor.DARK_AQUA + target.getName() + color).replace("{element}", elem.getName() + elem.getType().getBender()));
					} else {
						GeneralMethods.sendBrandingMessage(sender, color + this.addedOther.replace("{target}", ChatColor.DARK_AQUA + target.getName() + color).replace("{element}", elem.getName() + elem.getType().getBender()));
					}
				} else {
					if (vowel) {
						GeneralMethods.sendBrandingMessage(target, color + this.addedVowel.replace("{element}", elem.getName() + elem.getType().getBender()));
					} else {
						GeneralMethods.sendBrandingMessage(target, color + this.added.replace("{element}", elem.getName() + elem.getType().getBender()));
					}
				}
			}
		}
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (args.size() >= 2 || !sender.hasPermission("bending.command.add")) {
			return new ArrayList<String>();
		}
		final List<String> l = new ArrayList<>();
		if (args.size() == 0) {
			for (Element element : this.elementManager.getElements()) {
				l.add(element.getName());
			}
		} else {
			for (final Player p : Bukkit.getOnlinePlayers()) {
				l.add(p.getName());
			}
		}
		return l;
	}
}

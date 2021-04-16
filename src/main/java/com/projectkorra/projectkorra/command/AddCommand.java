package com.projectkorra.projectkorra.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent.Result;
import com.projectkorra.projectkorra.event.PlayerChangeSubElementEvent;

/**
 * Executor for /bending add. Extends {@link PKCommand}.
 */
public class AddCommand extends PKCommand {

	private final String playerNotFound;
	private final String invalidElement;
	private final String addedOtherCFW;
	private final String addedOtherAE;
	private final String addedCFW;
	private final String addedAE;
	private final String alreadyHasElementOther;
	private final String alreadyHasElement;
	private final String alreadyHasSubElementOther;
	private final String alreadyHasSubElement;
	private final String addedOtherAll;
	private final String addedAll;
	private final String alreadyHasAllElementsOther;
	private final String alreadyHasAllElements;

	public AddCommand() {
		super("add", "/bending add <Element/SubElement> [Player]", ConfigManager.languageConfig.get().getString("Commands.Add.Description"), new String[] { "add", "a" });

		this.playerNotFound = ConfigManager.languageConfig.get().getString("Commands.Add.PlayerNotFound");
		this.invalidElement = ConfigManager.languageConfig.get().getString("Commands.Add.InvalidElement");
		this.addedOtherCFW = ConfigManager.languageConfig.get().getString("Commands.Add.Other.SuccessfullyAddedCFW");
		this.addedOtherAE = ConfigManager.languageConfig.get().getString("Commands.Add.Other.SuccessfullyAddedAE");
		this.addedCFW = ConfigManager.languageConfig.get().getString("Commands.Add.SuccessfullyAddedCFW");
		this.addedAE = ConfigManager.languageConfig.get().getString("Commands.Add.SuccessfullyAddedAE");
		this.addedOtherAll = ConfigManager.languageConfig.get().getString("Commands.Add.Other.SuccessfullyAddedAll");
		this.addedAll = ConfigManager.languageConfig.get().getString("Commands.Add.SuccessfullyAddedAll");
		this.alreadyHasElementOther = ConfigManager.languageConfig.get().getString("Commands.Add.Other.AlreadyHasElement");
		this.alreadyHasElement = ConfigManager.languageConfig.get().getString("Commands.Add.AlreadyHasElement");
		this.alreadyHasSubElementOther = ConfigManager.languageConfig.get().getString("Commands.Add.Other.AlreadyHasSubElement");
		this.alreadyHasSubElement = ConfigManager.languageConfig.get().getString("Commands.Add.AlreadyHasSubElement");
		this.alreadyHasAllElementsOther = ConfigManager.languageConfig.get().getString("Commands.Add.Other.AlreadyHasAllElements");
		this.alreadyHasAllElements = ConfigManager.languageConfig.get().getString("Commands.Add.AlreadyHasAllElements");
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
	 * @param sender The CommandSender who issued the add command
	 * @param target The player to add the element to
	 * @param element The element to add
	 */
	private void add(final CommandSender sender, final Player target, final String element) {

		// if they aren't a BendingPlayer, create them.
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(target);
		if (bPlayer == null) {
			GeneralMethods.createBendingPlayer(target.getUniqueId(), target.getName());
			bPlayer = BendingPlayer.getBendingPlayer(target);
		} else if (bPlayer.isPermaRemoved()) { // ignore permabanned users.
			GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + ConfigManager.languageConfig.get().getString("Commands.Preset.Other.BendingPermanentlyRemoved"));
			return;
		}

		if (element.toLowerCase().equals("all")) {
			final StringBuilder elements = new StringBuilder("");
			boolean elementFound = false;
			for (final Element e : Element.getAllElements()) {
				if (!bPlayer.hasElement(e) && e != Element.AVATAR) {
					elementFound = true;
					bPlayer.addElement(e);

					if (elements.length() > 1) {
						elements.append(ChatColor.YELLOW + ", ");
					}
					elements.append(e.toString());

					bPlayer.getSubElements().clear();
					for (final SubElement sub : Element.getAllSubElements()) {
						if (bPlayer.hasElement(sub.getParentElement()) && bPlayer.hasSubElementPermission(sub)) {
							bPlayer.addSubElement(sub);
						}
					}

					GeneralMethods.saveElements(bPlayer);
					GeneralMethods.saveSubElements(bPlayer);
					Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(sender, target, e, Result.ADD));
				}
			}
			if (elementFound) {
				if (!(sender instanceof Player) || !((Player) sender).equals(target)) {
					GeneralMethods.sendBrandingMessage(sender, ChatColor.YELLOW + this.addedOtherAll.replace("{target}", ChatColor.DARK_AQUA + target.getName() + ChatColor.YELLOW) + elements);
					GeneralMethods.sendBrandingMessage(target, ChatColor.YELLOW + this.addedAll + elements);
				} else {
					GeneralMethods.sendBrandingMessage(target, ChatColor.YELLOW + this.addedAll + elements);
				}
			} else {
				if (!(sender instanceof Player) || !((Player) sender).equals(target)) {
					GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.alreadyHasAllElementsOther.replace("{target}", ChatColor.DARK_AQUA + target.getName() + ChatColor.RED));
				} else {
					GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.alreadyHasAllElements);
				}
			}
			return;
		} else {

			// get the [sub]element.
			Element e = Element.fromString(element);
			if (e == null) {
				e = Element.fromString(element);
			}

			if (e == Element.AVATAR) {
				this.add(sender, target, Element.AIR.getName());
				this.add(sender, target, Element.EARTH.getName());
				this.add(sender, target, Element.FIRE.getName());
				this.add(sender, target, Element.WATER.getName());
				return;
			}

			// if it's an element:
			if (Arrays.asList(Element.getAllElements()).contains(e)) {
				if (bPlayer.hasElement(e)) { // if already had, determine who to send the error message to.
					if (!(sender instanceof Player) || !((Player) sender).equals(target)) {
						GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.alreadyHasElementOther.replace("{target}", ChatColor.DARK_AQUA + target.getName() + ChatColor.RED));
					} else {
						GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.alreadyHasElement);
					}
					return;
				}

				// add all allowed subelements.
				bPlayer.addElement(e);
				bPlayer.getSubElements().clear();
				for (final SubElement sub : Element.getAllSubElements()) {
					if (bPlayer.hasElement(sub.getParentElement()) && bPlayer.hasSubElementPermission(sub)) {
						bPlayer.addSubElement(sub);
					}
				}

				// send the message.
				final ChatColor color = e.getColor();
				if (!(sender instanceof Player) || !((Player) sender).equals(target)) {
					if (e != Element.AIR && e != Element.EARTH && e != Element.BLUE_FIRE) {
						GeneralMethods.sendBrandingMessage(sender, color + this.addedOtherCFW.replace("{target}", ChatColor.DARK_AQUA + target.getName() + color).replace("{element}", e.toString() + e.getType().getBender()));
						GeneralMethods.sendBrandingMessage(target, color + this.addedCFW.replace("{element}", e.toString() + e.getType().getBender()));
					} else {
						GeneralMethods.sendBrandingMessage(sender, color + this.addedOtherAE.replace("{target}", ChatColor.DARK_AQUA + target.getName() + color).replace("{element}", e.toString() + e.getType().getBender()));
						GeneralMethods.sendBrandingMessage(target, color + this.addedAE.replace("{element}", e.toString() + e.getType().getBender()));
					}
				} else {
					if (e != Element.AIR && e != Element.EARTH) {
						GeneralMethods.sendBrandingMessage(target, color + this.addedCFW.replace("{element}", e.toString() + e.getType().getBender()));
					} else {
						GeneralMethods.sendBrandingMessage(target, color + this.addedAE.replace("{element}", e.toString() + e.getType().getBender()));
					}

				}
				GeneralMethods.saveElements(bPlayer);
				GeneralMethods.saveSubElements(bPlayer);
				Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(sender, target, e, Result.ADD));
				return;

				// if it's a sub element:
			} else if (Arrays.asList(Element.getAllSubElements()).contains(e)) {
				final SubElement sub = (SubElement) e;
				if (bPlayer.hasSubElement(sub)) { // if already had, determine  who to send the error message to.
					if (!(sender instanceof Player) || !((Player) sender).equals(target)) {
						GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.alreadyHasSubElementOther.replace("{target}", ChatColor.DARK_AQUA + target.getName() + ChatColor.RED));
					} else {
						GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.alreadyHasSubElement);
					}
					return;
				}
				bPlayer.addSubElement(sub);
				final ChatColor color = e.getColor();

				if (!(sender instanceof Player) || !((Player) sender).equals(target)) {
					if (e != Element.AIR && e != Element.EARTH) {
						GeneralMethods.sendBrandingMessage(sender, color + this.addedOtherCFW.replace("{target}", ChatColor.DARK_AQUA + target.getName() + color).replace("{element}", sub.toString() + sub.getType().getBender()));
					} else {
						GeneralMethods.sendBrandingMessage(sender, color + this.addedOtherAE.replace("{target}", ChatColor.DARK_AQUA + target.getName() + color).replace("{element}", sub.toString() + sub.getType().getBender()));
					}

				} else {
					if (e != Element.AIR && e != Element.EARTH) {
						GeneralMethods.sendBrandingMessage(target, color + this.addedCFW.replace("{element}", sub.toString() + sub.getType().getBender()));
					} else {
						GeneralMethods.sendBrandingMessage(target, color + this.addedAE.replace("{element}", sub.toString() + sub.getType().getBender()));
					}
				}
				GeneralMethods.saveSubElements(bPlayer);
				Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeSubElementEvent(sender, target, sub, com.projectkorra.projectkorra.event.PlayerChangeSubElementEvent.Result.ADD));
				return;

			} else { // bad element.
				sender.sendMessage(ChatColor.RED + this.invalidElement);
			}

		}
	}

	public static boolean isVowel(final char c) {
		return "AEIOUaeiou".indexOf(c) != -1;
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (args.size() >= 2 || !sender.hasPermission("bending.command.add")) {
			return new ArrayList<String>();
		}
		final List<String> l = new ArrayList<String>();
		if (args.size() == 0) {

			l.add("Air");
			l.add("Earth");
			l.add("Fire");
			l.add("Water");
			l.add("Chi");
			for (final Element e : Element.getAddonElements()) {
				l.add(e.getName());
			}

			l.add("Blood");
			l.add("Combustion");
			l.add("Flight");
			l.add("Healing");
			l.add("Ice");
			l.add("Lava");
			l.add("Lightning");
			l.add("Metal");
			l.add("Plant");
			l.add("Sand");
			l.add("Spiritual");
			l.add("BlueFire");
			for (final SubElement e : Element.getAddonSubElements()) {
				l.add(e.getName());
			}
		} else {
			for (final Player p : Bukkit.getOnlinePlayers()) {
				l.add(p.getName());
			}
		}
		return l;
	}
}

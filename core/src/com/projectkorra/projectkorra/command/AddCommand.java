package com.projectkorra.projectkorra.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.projectkorra.projectkorra.OfflineBendingPlayer;
import com.projectkorra.projectkorra.util.ChatUtil;
import org.bukkit.Bukkit;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.SubElement;
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
		}

		if (args.size() == 1 && this.hasPermission(sender) && this.isPlayer(sender)) { // bending add element.
			this.add(sender, (Player) sender, args.get(0).toLowerCase());
		} else if (args.size() == 2 && this.hasPermission(sender, "others")) { // bending add element combo.
			this.getPlayer(args.get(1)).thenAccept(player -> {
				if (player == null || (!player.isOnline() && !player.hasPlayedBefore())) {
					ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.playerNotFound);
					return;
				}
				this.add(sender, player, args.get(0).toLowerCase());
			}).exceptionally(e -> {
				e.printStackTrace();
				return null;
			});
		}
	}

	/**
	 * Adds the ability to bend an element to a player.
	 *
	 * @param sender The CommandSender who issued the add command
	 * @param target The player to add the element to
	 * @param element The element to add
	 */
	private void add(final CommandSender sender, final OfflinePlayer target, final String element) {

		// if they aren't a BendingPlayer, create them.
		BendingPlayer.getOrLoadOfflineAsync(target).thenAccept(bPlayer -> {
			boolean online = bPlayer instanceof BendingPlayer;

			if (bPlayer.isPermaRemoved()) { // ignore permabanned users.
				ChatUtil.sendBrandingMessage(sender, ChatColor.RED + ConfigManager.languageConfig.get().getString("Commands.Preset.Other.BendingPermanentlyRemoved"));
				return;
			}

			if (element.equalsIgnoreCase("all")) {
				final StringBuilder elements = new StringBuilder();
				for (final Element e : Element.getAllElements()) {
                    if (bPlayer.hasElement(e) || e == Element.AVATAR || !this.hasPermission(sender, e.getName().toLowerCase())) {
                        continue;
                    }

                    PlayerChangeElementEvent event = new PlayerChangeElementEvent(sender, target, e, Result.ADD);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) continue; // if the event is cancelled, don't add the element.

                    bPlayer.addElement(e);

                    if (!elements.isEmpty()) {
                        elements.append(ChatColor.YELLOW).append(", ");
                    }
                    elements.append(e);
                }

				if (!elements.isEmpty()) {
					if (!sender.equals(target)) {
						ChatUtil.sendBrandingMessage(sender, ChatColor.YELLOW + this.addedOtherAll.replace("{target}", ChatColor.DARK_AQUA + target.getName() + ChatColor.YELLOW) + elements);
                    }
                    if (online) {
						ChatUtil.sendBrandingMessage((Player) target, ChatColor.YELLOW + this.addedAll + elements);
					}
                    addPermittedSubElements(sender, target, bPlayer);
					bPlayer.saveElements();
				} else {
					if (!sender.equals(target)) {
						ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.alreadyHasAllElementsOther.replace("{target}", ChatColor.DARK_AQUA + target.getName() + ChatColor.RED));
					} else {
						ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.alreadyHasAllElements);
					}
				}
				return;
			}

			// Singular Element/SubElement
			Element e = Element.fromString(element);
			if (e == Element.AVATAR) {
				this.add(sender, target, Element.AIR.getName());
				this.add(sender, target, Element.EARTH.getName());
				this.add(sender, target, Element.FIRE.getName());
				this.add(sender, target, Element.WATER.getName());
				return;
			}

			if (bPlayer.hasElement(e)) { // if already had, determine who to send the error message to.
				if (!sender.equals(target)) {
					String message = e instanceof SubElement ? this.alreadyHasSubElementOther : this.alreadyHasElementOther;
					ChatUtil.sendBrandingMessage(sender, ChatColor.RED + message.replace("{target}", ChatColor.DARK_AQUA + target.getName() + ChatColor.RED));
				} else {
					String message = e instanceof SubElement ? this.alreadyHasSubElement : this.alreadyHasElement;
					ChatUtil.sendBrandingMessage(sender, ChatColor.RED + message);
				}
				return;
			}

			// if it's an element:
			if (Arrays.asList(Element.getAllElements()).contains(e)) {
				if (!sender.hasPermission("bending.command.add." + Element.AVATAR.getName().toLowerCase())
						&& !this.hasPermission(sender, e.getName().toLowerCase())) {
					return;
				}

				PlayerChangeElementEvent event = new PlayerChangeElementEvent(sender, target, e, Result.ADD);
				Bukkit.getServer().getPluginManager().callEvent(event);
				if (event.isCancelled()) return; // if the event is cancelled, don't add the element.

				bPlayer.addElement(e);

				// send the message.
				final ChatColor color = e.getColor();
				final String bender = e + e.getType().getBender();
				if (!sender.equals(target)) {
					String message = ChatUtil.indefArticle(e.getName(), this.addedOtherAE, this.addedOtherCFW);
					String targetMessage = ChatUtil.indefArticle(e.getName(), this.addedAE, this.addedCFW);
					ChatUtil.sendBrandingMessage(sender, color + message.replace("{target}", ChatColor.DARK_AQUA + target.getName() + color).replace("{element}", bender));
					if (online) {
						ChatUtil.sendBrandingMessage((Player) target, color + targetMessage.replace("{element}", bender));
					}
				} else {
					ChatUtil.sendBrandingMessage(sender, color + ChatUtil.indefArticle(e.getName(), addedAE, addedCFW).replace("{element}", bender));
				}
				addPermittedSubElements(sender, target, bPlayer);
				bPlayer.saveElements();

				// if it's a sub element:
			} else if (e instanceof SubElement sub && Arrays.asList(Element.getAllSubElements()).contains(sub)) {
				if (!this.hasPermission(sender, sub.getName().toLowerCase())) {
					return;
				}

				PlayerChangeSubElementEvent event = new PlayerChangeSubElementEvent(sender, target, sub, com.projectkorra.projectkorra.event.PlayerChangeSubElementEvent.Result.ADD);
				Bukkit.getServer().getPluginManager().callEvent(event);
				if (event.isCancelled()) return; // if the event is cancelled, don't add the subelement.

				bPlayer.addSubElement(sub);

				final ChatColor color = e.getColor();
				final String bender = e + e.getType().getBender();
				if (!sender.equals(target)) {
					ChatUtil.sendBrandingMessage(sender, color + this.addedOtherCFW.replace("{target}", ChatColor.DARK_AQUA + target.getName() + color).replace("{element}", bender));
				} else {
					ChatUtil.sendBrandingMessage(sender, color + this.addedCFW.replace("{element}", bender));
				}
				bPlayer.saveSubElements();
			} else { // bad element.
				sender.sendMessage(ChatColor.RED + this.invalidElement);
			}
		}).exceptionally(e -> {
			e.printStackTrace();
			return null;
		});

	}

	private void addPermittedSubElements(CommandSender sender, OfflinePlayer target, OfflineBendingPlayer offlineBPlayer) {
		offlineBPlayer.getSubElements().clear();
		if (offlineBPlayer instanceof  BendingPlayer bPlayer) {
			for (SubElement subElement : Element.getAllSubElements()) {
				if (!bPlayer.hasElement(subElement.getParentElement()) || !bPlayer.hasSubElementPermission(subElement)) {
					continue;
				}

				PlayerChangeSubElementEvent subEvent = new PlayerChangeSubElementEvent(sender, target, subElement, PlayerChangeSubElementEvent.Result.ADD);
				Bukkit.getPluginManager().callEvent(subEvent);
				if (subEvent.isCancelled()) continue; // if the event is cancelled, don't add the subelement.

				bPlayer.addSubElement(subElement);
			}
		}
		offlineBPlayer.saveSubElements();
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (args.size() >= 2 || !sender.hasPermission("bending.command.add")) {
			return List.of();
		}

		if (args.size() == 0) {
			final List<String> completion = new ArrayList<>();

			// add tab completion for elements the player has permission to add
			for (Element element : Element.getAllElements()) {
				if (this.hasPermission(sender, element.getName().toLowerCase())) {
					completion.add(element.getName());
				}
			}

			// add tab completion for sub-elements the player has permission to add
			for (Element element : Element.getAllSubElements()) {
				if (this.hasPermission(sender, element.getName().toLowerCase())) {
					completion.add(element.getName());
				}
			}
			return completion;
		} else {
			return getOnlinePlayerNames(sender);
		}
	}
}

package com.projectkorra.projectkorra.command;

import java.util.ArrayList;
import java.util.List;

import com.projectkorra.projectkorra.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
 * Executor for /bending remove. Extends {@link PKCommand}.
 */
public class RemoveCommand extends PKCommand {

	private final String succesfullyRemovedElementSelf, wrongElementSelf, invalidElement, playerNotFound, wrongElementTarget,
			succesfullyRemovedElementTarget, succesfullyRemovedElementTargetConfirm, succesfullyRemovedAllElementsTarget,
			succesfullyRemovedAllElementsTargetConfirm, noElements, noElementsTarget, noElementsTargetTemps;

	public RemoveCommand() {
		super("remove", "/bending remove <Player> [Element]", ConfigManager.languageConfig.get().getString("Commands.Remove.Description"), new String[] { "remove", "rm" });

		this.succesfullyRemovedElementSelf = ConfigManager.languageConfig.get().getString("Commands.Remove.RemovedElement");
		this.succesfullyRemovedAllElementsTarget = ConfigManager.languageConfig.get().getString("Commands.Remove.Other.RemovedAllElements");
		this.succesfullyRemovedAllElementsTargetConfirm = ConfigManager.languageConfig.get().getString("Commands.Remove.Other.RemovedAllElementsConfirm");
		this.succesfullyRemovedElementTarget = ConfigManager.languageConfig.get().getString("Commands.Remove.Other.RemovedElement");
		this.succesfullyRemovedElementTargetConfirm = ConfigManager.languageConfig.get().getString("Commands.Remove.Other.RemovedElementConfirm");
		this.invalidElement = ConfigManager.languageConfig.get().getString("Commands.Remove.InvalidElement");
		this.wrongElementSelf = ConfigManager.languageConfig.get().getString("Commands.Remove.WrongElement");
		this.wrongElementTarget = ConfigManager.languageConfig.get().getString("Commands.Remove.Other.WrongElement");
		this.playerNotFound = ConfigManager.languageConfig.get().getString("Commands.Remove.PlayerNotFound");
		this.noElements = ConfigManager.languageConfig.get().getString("Commands.Remove.NoElements");
		this.noElementsTarget = ConfigManager.languageConfig.get().getString("Commands.Remove.Other.NoElements");
		this.noElementsTargetTemps = ConfigManager.languageConfig.get().getString("Commands.Remove.Other.NoElementsWithTemps");
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.hasPermission(sender) || !this.correctLength(sender, args.size(), 1, 2)) {
			return;
		}

		if (args.size() == 1) {
			final Element e = Element.fromString(args.get(0));
			if (e == null) { //The first argument must be a playername instead
				final OfflinePlayer player = Bukkit.getOfflinePlayer(args.get(0));
				if (!player.isOnline() && !player.hasPlayedBefore()) { //Player not found
					ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.playerNotFound);
					return;
				}

				BendingPlayer.getOrLoadOfflineAsync(player).thenAccept(bPlayer -> {
					boolean online = bPlayer instanceof BendingPlayer;

					//If they have no elements
					if (bPlayer.getElements().size() == 0) {
						//If they still have temp elements
						if (bPlayer.hasTempElements()) {
							ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.noElementsTargetTemps.replace("{target}", player.getName()));
						} else { //Tell them they have no elements
							if (player != sender) ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.noElementsTarget.replace("{target}", player.getName()));
							else ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.noElements);
						}
						return;
					}

					bPlayer.getElements().clear();
					bPlayer.getSubElements().clear();
					bPlayer.saveElements();
					bPlayer.saveSubElements();
					if (online) ((BendingPlayer)bPlayer).removeUnusableAbilities();
					if (!player.getName().equalsIgnoreCase(sender.getName())) {
						ChatUtil.sendBrandingMessage(sender, ChatColor.YELLOW + this.succesfullyRemovedAllElementsTargetConfirm.replace("{target}", ChatColor.DARK_AQUA + player.getName() + ChatColor.YELLOW));
					}

					if (online) {
						ChatUtil.sendBrandingMessage((Player) player, ChatColor.YELLOW + this.succesfullyRemovedAllElementsTarget.replace("{sender}", ChatColor.DARK_AQUA + sender.getName() + ChatColor.YELLOW));
						Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(sender, (Player) player, null, Result.REMOVE));
					}
				});
			} else { //The first argument is an element
				if (!(sender instanceof Player)) { //Make sure the sender is a player
					help(sender, false);
					return;
				}

				BendingPlayer senderBPlayer = BendingPlayer.getBendingPlayer((Player) sender);
				Player player = (Player) sender;

				//If it is a temp element, let the TempCommand handle it
				if (senderBPlayer.hasTempElement(e)) {
					TempCommand.TEMP_COMMAND.removeElement(e, senderBPlayer, sender, false);
					return;
				}

				if (e instanceof SubElement) { //If it's a subelement
					if (senderBPlayer.hasElement(e)) {
						senderBPlayer.getSubElements().remove(e);
						senderBPlayer.saveSubElements();
						senderBPlayer.removeUnusableAbilities();
						ChatUtil.sendBrandingMessage(sender, e.getColor() + this.succesfullyRemovedElementSelf.replace("{element}", e.toString() + e.getType().getBending()).replace("{sender}", ChatColor.DARK_AQUA + sender.getName() + e.getColor()));
						Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeSubElementEvent(sender, player, (SubElement) e, com.projectkorra.projectkorra.event.PlayerChangeSubElementEvent.Result.REMOVE));
					} else {
						ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.wrongElementSelf);
					}
					return;
				}

				//If it's an element
				if (senderBPlayer.hasElement(e)) {
					senderBPlayer.getElements().remove(e);
					for (final SubElement sub : Element.getSubElements(e)) {
						if (!(sub instanceof Element.MultiSubElement)) senderBPlayer.getSubElements().remove(sub);
						else {
							Element.MultiSubElement multiSubElement = (Element.MultiSubElement) sub;
							boolean keep = false;
							for (Element parent : multiSubElement.getParentElements()) {
								if (senderBPlayer.hasElement(parent)) {
									keep = true;
									break;
								}
							}
							if (!keep) senderBPlayer.getSubElements().remove(sub);
						}
					}
					senderBPlayer.saveElements();
					senderBPlayer.saveSubElements();
					senderBPlayer.removeUnusableAbilities();

					ChatUtil.sendBrandingMessage(sender, e.getColor() + this.succesfullyRemovedElementSelf.replace("{element}", e.toString() + e.getType().getBending()));
					Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(sender, (Player) sender, e, Result.REMOVE));
					return;
				} else {
					ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.wrongElementSelf);
				}
			}
			return;
		}

		//2 arguments
		final OfflinePlayer player = Bukkit.getOfflinePlayer(args.get(0));
		if (!player.isOnline() && !player.hasPlayedBefore()) { //Player not found
			ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.playerNotFound);
			return;
		}

		Element element = Element.fromString(args.get(1));
		if (element == null) {
			ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.invalidElement);
			return;
		}

		BendingPlayer.getOrLoadOfflineAsync(player).thenAccept(bPlayer -> {
			boolean online = bPlayer instanceof BendingPlayer;
			if (!bPlayer.hasElement(element)) {
				ChatUtil.sendBrandingMessage(sender, ChatColor.DARK_RED + this.wrongElementTarget.replace("{target}", player.getName()));
				return;
			}

			//If the element is a temp element that hasn't expired, remove it via the temp command (just saves repeating the same code)
			if (bPlayer.hasTempElement(element)) {
				TempCommand.TEMP_COMMAND.removeElement(element, bPlayer, sender, false);
				return;
			}

			if (element instanceof SubElement) {
				bPlayer.getSubElements().remove(element);
				bPlayer.saveSubElements();
			} else {
				bPlayer.getElements().remove(element);
				for (final SubElement sub : Element.getSubElements(element)) {
					if (!(sub instanceof Element.MultiSubElement)) bPlayer.getSubElements().remove(sub);
					else {
						Element.MultiSubElement multiSubElement = (Element.MultiSubElement) sub;
						boolean keep = false;
						for (Element parent : multiSubElement.getParentElements()) {
							if (bPlayer.hasElement(parent)) {
								keep = true;
								break;
							}
						}
						if (!keep) bPlayer.getSubElements().remove(sub);
					}
				}
				bPlayer.saveElements();
				bPlayer.saveSubElements();
			}
			if (player != sender) ChatUtil.sendBrandingMessage(sender, element.getColor() + this.succesfullyRemovedElementTargetConfirm.replace("{element}", element.toString() + element.getType().getBending()).replace("{target}", ChatColor.DARK_AQUA + player.getName() + element.getColor()));

			if (online) {
				((BendingPlayer)bPlayer).removeUnusableAbilities();
				ChatUtil.sendBrandingMessage((Player)player, element.getColor() + this.succesfullyRemovedElementTarget.replace("{element}", element.toString() + element.getType().getBending()).replace("{sender}", ChatColor.DARK_AQUA + sender.getName() + element.getColor()));
				Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(sender, (Player) player, element, Result.REMOVE));
			}
		});
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (args.size() >= 2 || !sender.hasPermission("bending.command.remove")) {
			return new ArrayList<>();
		}
		final List<String> l = new ArrayList<>();
		if (args.size() == 0) {
			return getOnlinePlayerNames(sender);
		} else {
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
		}
		return l;
	}
}

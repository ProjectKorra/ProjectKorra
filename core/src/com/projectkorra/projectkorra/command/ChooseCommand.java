package com.projectkorra.projectkorra.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.projectkorra.projectkorra.util.ChatUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
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
import com.projectkorra.projectkorra.util.TimeUtil;

/**
 * Executor for /bending choose. Extends {@link PKCommand}.
 */
public class ChooseCommand extends PKCommand {

	private final String invalidElement;
	private final String playerNotFound;
	private final String onCooldown;
	private final String chosenCFW;
	private final String chosenAE;
	private final String chosenOtherCFW;
	private final String chosenOtherAE;
	private final long cooldown;

	public ChooseCommand() {
		super("choose", "/bending choose <Element> [Player]", ConfigManager.languageConfig.get().getString("Commands.Choose.Description"), new String[] { "choose", "ch" });

		this.playerNotFound = ConfigManager.languageConfig.get().getString("Commands.Choose.PlayerNotFound");
		this.invalidElement = ConfigManager.languageConfig.get().getString("Commands.Choose.InvalidElement");
		this.onCooldown = ConfigManager.languageConfig.get().getString("Commands.Choose.OnCooldown");
		this.chosenCFW = ConfigManager.languageConfig.get().getString("Commands.Choose.SuccessfullyChosenCFW");
		this.chosenAE = ConfigManager.languageConfig.get().getString("Commands.Choose.SuccessfullyChosenAE");
		this.chosenOtherCFW = ConfigManager.languageConfig.get().getString("Commands.Choose.Other.SuccessfullyChosenCFW");
		this.chosenOtherAE = ConfigManager.languageConfig.get().getString("Commands.Choose.Other.SuccessfullyChosenAE");
		this.cooldown = ConfigManager.defaultConfig.get().getLong("Properties.ChooseCooldown");
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.correctLength(sender, args.size(), 1, 2)) {
			return;
		}
		if (args.size() == 1) {
			if (!this.hasPermission(sender) || !this.isPlayer(sender)) {
				return;
			}

			//Don't need to bother with offline players here because the sender is always online
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(sender.getName());
			if (bPlayer.isPermaRemoved()) {
				ChatUtil.sendBrandingMessage(sender, ChatColor.RED + ConfigManager.languageConfig.get().getString("Commands.Preset.BendingPermanentlyRemoved"));
				return;
			}
			if (!bPlayer.getElements().isEmpty() && !sender.hasPermission("bending.command.rechoose")) {
				ChatUtil.sendBrandingMessage(sender, super.noPermissionMessage);
				return;
			}
			String element = args.get(0).toLowerCase();
			if (element.equalsIgnoreCase("a")) {
				element = "air";
			} else if (element.equalsIgnoreCase("e")) {
				element = "earth";
			} else if (element.equalsIgnoreCase("f")) {
				element = "fire";
			} else if (element.equalsIgnoreCase("w")) {
				element = "water";
			} else if (element.equalsIgnoreCase("c")) {
				element = "chi";
			}
			final Element targetElement = Element.getElement(element);
			if (Arrays.asList(Element.getAllElements()).contains(targetElement)) {
				if (!this.hasPermission(sender, element)) {
					return;
				}
				if (bPlayer.isOnCooldown("ChooseElement")) {
					if (sender.hasPermission("bending.command.choose.ignorecooldown") || sender.hasPermission("bending.admin.choose")) {
						bPlayer.removeCooldown("ChooseElement");
					} else {
						ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.onCooldown.replace("%cooldown%", TimeUtil.formatTime(bPlayer.getCooldown("ChooseElement") - System.currentTimeMillis())));
						return;
					}
				}

				this.add(sender, (Player) sender, targetElement);

				if (sender.hasPermission("bending.command.choose.ignorecooldown") || sender.hasPermission("bending.admin.choose")) {
					return;
				}

				bPlayer.addCooldown("ChooseElement", this.cooldown, true);
			} else {
				ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.invalidElement);
			}
		} else if (args.size() == 2) {
			if (!sender.hasPermission("bending.admin.choose")) {
				ChatUtil.sendBrandingMessage(sender, super.noPermissionMessage);
				return;
			}

			this.getPlayer(args.get(1)).thenAccept((target) -> {
				if (!target.hasPlayedBefore() && !target.isOnline()) {
					ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.playerNotFound);
					return;
				}
				String element = args.get(0).toLowerCase();
				if (element.equalsIgnoreCase("a")) {
					element = "air";
				} else if (element.equalsIgnoreCase("e")) {
					element = "earth";
				} else if (element.equalsIgnoreCase("f")) {
					element = "fire";
				} else if (element.equalsIgnoreCase("w")) {
					element = "water";
				} else if (element.equalsIgnoreCase("c")) {
					element = "chi";
				}
				final Element targetElement = Element.getElement(element);
				if (Arrays.asList(Element.getAllElements()).contains(targetElement) && targetElement != Element.AVATAR) {
					this.add(sender, target, targetElement);

					if (target.isOnline()) {
						if (((Player)target).hasPermission("bending.command.choose.ignorecooldown") || ((Player)target).hasPermission("bending.admin.choose")) {
							return;
						}
					}

					BendingPlayer.getOrLoadOfflineAsync(target).thenAccept(bPlayer -> {
						bPlayer.addCooldown("ChooseElement", this.cooldown, true);
					});
				} else {
					ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.invalidElement);
				}
			}).exceptionally(e -> {
				e.printStackTrace();
				return null;
			});

		}
	}

	/**
	 * Adds the ability to bend the given element to the specified Player.
	 *
	 * @param sender The CommandSender who issued the command
	 * @param target The Player to add the element to
	 * @param element The element to add to the Player
	 */
	private void add(final CommandSender sender, final OfflinePlayer target, final Element element) {
		BendingPlayer.getOrLoadOfflineAsync(target).thenAccept(bPlayer -> {
			boolean online = bPlayer instanceof BendingPlayer;

			if (element instanceof SubElement) {
				final SubElement sub = (SubElement) element;
				bPlayer.addSubElement(sub);
				final ChatColor color = sub.getColor();
				if (!(sender instanceof Player) || !((Player) sender).equals(target)) {
					ChatUtil.sendBrandingMessage(sender, color + this.chosenOtherCFW.replace("{target}", ChatColor.DARK_AQUA + target.getName() + color).replace("{element}", sub.getName() + sub.getType().getBender()));
				} else {
					if (online) ChatUtil.sendBrandingMessage((Player) target, color + this.chosenCFW.replace("{element}", sub.getName() + sub.getType().getBender()));
				}
				bPlayer.saveSubElements();
				if (online) {
					((BendingPlayer) bPlayer).removeUnusableAbilities();
				}
				Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeSubElementEvent(sender, target, sub, PlayerChangeSubElementEvent.Result.CHOOSE));
			} else {
				if (element == Element.AVATAR) {

					PlayerChangeElementEvent event = new PlayerChangeElementEvent(sender, target, element, Result.CHOOSE);
					Bukkit.getServer().getPluginManager().callEvent(event);
					if (event.isCancelled()) return; //Do nothing if cancelled

					bPlayer.getElements().clear();
					for (Element e : new Element[] {Element.AIR, Element.EARTH, Element.FIRE, Element.WATER}) {
						bPlayer.addElement(e);

						if (online) {
							for (final SubElement sub : Element.getSubElements(e)) {
								if (((BendingPlayer) bPlayer).hasSubElementPermission(sub)) {
									PlayerChangeSubElementEvent subEvent = new PlayerChangeSubElementEvent(sender, target, sub, PlayerChangeSubElementEvent.Result.CHOOSE);
									Bukkit.getServer().getPluginManager().callEvent(subEvent);
									if (subEvent.isCancelled()) continue; //Do nothing if cancelled
									bPlayer.addSubElement(sub);
								}
							}
						}
					}
				} else {
					PlayerChangeElementEvent event = new PlayerChangeElementEvent(sender, target, element, Result.CHOOSE);
					Bukkit.getServer().getPluginManager().callEvent(event);
					if (event.isCancelled()) return; //Do nothing if cancelled

					bPlayer.setElement(element);
					bPlayer.getSubElements().clear();

					if (online) {
						for (final SubElement sub : Element.getSubElements(element)) {
							if (((BendingPlayer) bPlayer).hasSubElementPermission(sub)) {
								PlayerChangeSubElementEvent subEvent = new PlayerChangeSubElementEvent(sender, target, sub, PlayerChangeSubElementEvent.Result.CHOOSE);

								Bukkit.getServer().getPluginManager().callEvent(subEvent);
								if (subEvent.isCancelled()) continue; //Do nothing if cancelled

								bPlayer.addSubElement(sub);
							}
						}
					}
				}

				final ChatColor color = element.getColor();
				if (!(sender instanceof Player) || !((Player) sender).equals(target)) {
					if (element != Element.AIR && element != Element.EARTH) {
						ChatUtil.sendBrandingMessage(sender, color + this.chosenOtherCFW.replace("{target}", ChatColor.DARK_AQUA + target.getName() + color).replace("{element}", element.getName() + element.getType().getBender()));
					} else {
						ChatUtil.sendBrandingMessage(sender, color + this.chosenOtherAE.replace("{target}", ChatColor.DARK_AQUA + target.getName() + color).replace("{element}", element.getName() + element.getType().getBender()));
					}
				} else {
					if (element != Element.AIR && element != Element.EARTH) {
						if (online) ChatUtil.sendBrandingMessage((Player) target, color + this.chosenCFW.replace("{element}", element.getName() + element.getType().getBender()));
					} else {
						if (online) ChatUtil.sendBrandingMessage((Player) target, color + this.chosenAE.replace("{element}", element.getName() + element.getType().getBender()));
					}
				}
				bPlayer.saveElements();
				bPlayer.saveSubElements();
				if (online) {
					((BendingPlayer)bPlayer).removeUnusableAbilities();
				}

			}
		}).exceptionally(e -> {
			e.printStackTrace();
			return null;
		});
	}

	public static boolean isVowel(final char c) {
		return "AEIOUaeiou".indexOf(c) != -1;
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (args.size() >= 2 || !sender.hasPermission("bending.command.choose")) {
			return new ArrayList<>();
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
		} else {
			for (final Player p : Bukkit.getOnlinePlayers()) {
				l.add(p.getName());
			}
		}
		return l;
	}
}

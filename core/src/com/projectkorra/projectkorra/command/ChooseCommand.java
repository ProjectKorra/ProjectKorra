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

		if (args.size() == 1 && this.hasPermission(sender) && this.isPlayer(sender)) {
			//Don't need to bother with offline players here because the sender is always online
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(sender.getName());
			if (bPlayer.isPermaRemoved()) {
				ChatUtil.sendBrandingMessage(sender, ChatColor.RED + ConfigManager.languageConfig.get().getString("Commands.Preset.BendingPermanentlyRemoved"));
				return;
			} else if (!bPlayer.getElements().isEmpty() && !sender.hasPermission("bending.command.rechoose")) {
				ChatUtil.sendBrandingMessage(sender, super.noPermissionMessage);
				return;
			}

			final Element element = Element.fromString(args.get(0));
			if (element == null || !Arrays.asList(Element.getAllElements()).contains(element)) {
				ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.invalidElement);
				return;
			} else if (!this.hasPermission(sender, element.getName().toLowerCase())) {
				return;
			}

			boolean bypassCooldown = sender.hasPermission("bending.command.choose.ignorecooldown") || sender.hasPermission("bending.admin.choose");
			if (bPlayer.isOnCooldown("ChooseElement")) {
				if (bypassCooldown) {
					bPlayer.removeCooldown("ChooseElement");
				} else {
					ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.onCooldown.replace("%cooldown%", TimeUtil.formatTime(bPlayer.getCooldown("ChooseElement") - System.currentTimeMillis())));
					return;
				}
			}

			this.add(sender, (Player) sender, element);
			if (!bypassCooldown) {
				bPlayer.addCooldown("ChooseElement", this.cooldown, true);
			}
		} else if (args.size() == 2) {
			if (!sender.hasPermission("bending.admin.choose")) {
				ChatUtil.sendBrandingMessage(sender, super.noPermissionMessage);
				return;
			}

			this.getPlayer(args.get(1)).thenAccept(target -> {
				if (!target.hasPlayedBefore() && !target.isOnline()) {
					ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.playerNotFound);
					return;
				}

				final Element element = Element.fromString(args.get(0));
				// TODO: Remove element == Element.AVATAR maybe? Is it intentional that a player with permission can choose avatar for themselves but an admin cannot choose Avatar for someone else?
				if (element == null || !Arrays.asList(Element.getAllElements()).contains(element) || element == Element.AVATAR) {
					ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.invalidElement);
					return;
				}

				this.add(sender, target, element);
				if (target instanceof Player online && (online.hasPermission("bending.command.choose.ignorecooldown") || online.hasPermission("bending.admin.choose"))) {
					return;
				}

				BendingPlayer.getOrLoadOfflineAsync(target).thenAccept(bPlayer -> {
					bPlayer.addCooldown("ChooseElement", this.cooldown, true);
				});
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

			if (element == Element.AVATAR) {
				PlayerChangeElementEvent event = new PlayerChangeElementEvent(sender, target, element, Result.CHOOSE);
				Bukkit.getServer().getPluginManager().callEvent(event);
				if (event.isCancelled()) return; //Do nothing if cancelled

				bPlayer.getElements().clear();
				for (Element e : new Element[] {Element.AIR, Element.EARTH, Element.FIRE, Element.WATER}) {
					bPlayer.addElement(e);
				}
			} else {
				PlayerChangeElementEvent event = new PlayerChangeElementEvent(sender, target, element, Result.CHOOSE);
				Bukkit.getServer().getPluginManager().callEvent(event);
				if (event.isCancelled()) return; //Do nothing if cancelled

				bPlayer.setElement(element);
				bPlayer.getSubElements().clear();
			}

			if (online) {
				addPermittedSubElements(sender, target, element, (BendingPlayer) bPlayer);
			}

			final ChatColor color = element.getColor();
			final String bender = element.getName() + element.getType().getBender();
			if (!sender.equals(target)) {
				String message = ChatUtil.indefArticle(element.getName(), this.chosenOtherAE, this.chosenOtherCFW);
				ChatUtil.sendBrandingMessage(sender, color + message.replace("{target}", ChatColor.DARK_AQUA + target.getName() + color).replace("{element}", bender));
			} else {
				String message = ChatUtil.indefArticle(element.getName(), this.chosenAE, this.chosenCFW);
				ChatUtil.sendBrandingMessage(sender, color + message.replace("{element}", bender));
			}
			bPlayer.saveElements();
			bPlayer.saveSubElements();
			if (online) {
				((BendingPlayer) bPlayer).removeUnusableAbilities();
			}
		}).exceptionally(e -> {
			e.printStackTrace();
			return null;
		});
	}

	private void addPermittedSubElements(CommandSender sender, OfflinePlayer target, Element element, BendingPlayer bPlayer) {
		for (final SubElement sub : Element.getSubElements(element)) {
			if (bPlayer.hasSubElementPermission(sub)) {
				PlayerChangeSubElementEvent subEvent = new PlayerChangeSubElementEvent(sender, target, sub, PlayerChangeSubElementEvent.Result.CHOOSE);
				Bukkit.getPluginManager().callEvent(subEvent);
				if (!subEvent.isCancelled()) {
					bPlayer.addSubElement(sub);
				}
			}
		}
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (args.size() >= 2 || !sender.hasPermission("bending.command.choose")) {
			return List.of();
		}

		final List<String> completion = new ArrayList<>();
		if (args.size() == 0) {
			for (Element element : Element.getAllElements()) {
				completion.add(element.getName());
			}
		} else {
			for (final Player player : Bukkit.getOnlinePlayers()) {
				completion.add(player.getName());
			}
		}
		return completion;
	}
}

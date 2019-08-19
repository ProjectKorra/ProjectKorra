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
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.commands.ChooseCommandConfig;
import com.projectkorra.projectkorra.configuration.configs.properties.CommandPropertiesConfig;
import com.projectkorra.projectkorra.configuration.configs.properties.GeneralPropertiesConfig;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent.Result;
import com.projectkorra.projectkorra.util.TimeUtil;

/**
 * Executor for /bending choose. Extends {@link PKCommand}.
 */
public class ChooseCommand extends PKCommand<ChooseCommandConfig> {

	private final String invalidElement;
	private final String playerNotFound;
	private final String onCooldown;
	private final String chosen;
	private final String chosenVowel;
	private final String chosenOther;
	private final String chosenOtherVowel;
	private final long cooldown;

	public ChooseCommand(final ChooseCommandConfig config) {
		super(config, "choose", "/bending choose <Element> [Player]", config.Description, new String[] { "choose", "ch" });

		this.playerNotFound = config.PlayerNotFound;
		this.invalidElement = config.InvalidElement;
		this.onCooldown = config.OnCooldown;
		this.chosen = config.SuccessfullyChosen;
		this.chosenVowel = config.SuccessfullyChosenVowel;
		this.chosenOther = config.SuccessfullyChosen_Other;
		this.chosenOtherVowel = config.SuccessfullyChosenVowel_Other;
		this.cooldown = ConfigManager.getConfig(GeneralPropertiesConfig.class).ChooseCooldown;
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.correctLength(sender, args.size(), 1, 2)) {
			return;
		} else if (args.size() == 1) {
			if (!this.hasPermission(sender) || !this.isPlayer(sender)) {
				return;
			}

			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(sender.getName());
			if (bPlayer == null) {
				GeneralMethods.createBendingPlayer(((Player) sender).getUniqueId(), sender.getName());
				bPlayer = BendingPlayer.getBendingPlayer(sender.getName());
			}
			if (bPlayer.isPermaRemoved()) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + ConfigManager.getConfig(CommandPropertiesConfig.class).BendingPermanentlyRemoved);
				return;
			}
			if (!bPlayer.getElements().isEmpty() && !sender.hasPermission("bending.command.rechoose")) {
				GeneralMethods.sendBrandingMessage(sender, super.noPermissionMessage);
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
					if (sender.hasPermission("bending.choose.ignorecooldown") || sender.hasPermission("bending.admin.choose")) {
						bPlayer.removeCooldown("ChooseElement");
					} else {
						GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.onCooldown.replace("%cooldown%", TimeUtil.formatTime(bPlayer.getCooldown("ChooseElement") - System.currentTimeMillis())));
						return;
					}
				}

				this.add(sender, (Player) sender, targetElement);

				if (sender.hasPermission("bending.choose.ignorecooldown") || sender.hasPermission("bending.admin.choose")) {
					return;
				}

				bPlayer.addCooldown("ChooseElement", this.cooldown, true);
				return;
			} else {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.invalidElement);
				return;
			}
		} else if (args.size() == 2) {
			if (!sender.hasPermission("bending.admin.choose")) {
				GeneralMethods.sendBrandingMessage(sender, super.noPermissionMessage);
				return;
			}
			final Player target = ProjectKorra.plugin.getServer().getPlayer(args.get(1));
			if (target == null || !target.isOnline()) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.playerNotFound);
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

				if (target.hasPermission("bending.choose.ignorecooldown") || target.hasPermission("bending.admin.choose")) {
					return;
				}

				final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(target);
				bPlayer.addCooldown("ChooseElement", this.cooldown, true);

				return;
			} else {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.invalidElement);
			}
		}
	}

	/**
	 * Adds the ability to bend the given element to the specified Player.
	 *
	 * @param sender The CommandSender who issued the command
	 * @param target The Player to add the element to
	 * @param element The element to add to the Player
	 */
	private void add(final CommandSender sender, final Player target, final Element element) {
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(target);

		if (bPlayer == null) {
			return;
		}
		
		bPlayer.setElement(element);

		final ChatColor color = element != null ? element.getColor() : ChatColor.WHITE;
		boolean vowel = GeneralMethods.isVowel(ChatColor.stripColor(element.getName()).charAt(0));
		
		if (!(sender instanceof Player) || !((Player) sender).equals(target)) {
			if (vowel) {
				GeneralMethods.sendBrandingMessage(sender, color + this.chosenOtherVowel.replace("{target}", ChatColor.DARK_AQUA + target.getName() + color).replace("{element}", element.getName() + element.getType().getBender()));
			} else {
				GeneralMethods.sendBrandingMessage(sender, color + this.chosenOther.replace("{target}", ChatColor.DARK_AQUA + target.getName() + color).replace("{element}", element.getName() + element.getType().getBender()));
			}
		} else {
			if (vowel) {
				GeneralMethods.sendBrandingMessage(target, color + this.chosenVowel.replace("{element}", element.getName() + element.getType().getBender()));
			} else {
				GeneralMethods.sendBrandingMessage(target, color + this.chosen.replace("{element}", element.getName() + element.getType().getBender()));
			}
		}
		
		GeneralMethods.saveElement(bPlayer, element);
		Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(sender, target, element, Result.CHOOSE));
		GeneralMethods.removeUnusableAbilities(target.getName());
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (args.size() >= 2 || !sender.hasPermission("bending.command.choose")) {
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
		} else {
			for (final Player p : Bukkit.getOnlinePlayers()) {
				l.add(p.getName());
			}
		}
		return l;
	}
}

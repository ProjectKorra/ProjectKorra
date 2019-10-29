package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.commands.ChooseCommandConfig;
import com.projectkorra.projectkorra.configuration.configs.properties.CommandPropertiesConfig;
import com.projectkorra.projectkorra.configuration.configs.properties.GeneralPropertiesConfig;
import com.projectkorra.projectkorra.element.Element;
import com.projectkorra.projectkorra.element.SubElement;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent.Result;
import com.projectkorra.projectkorra.player.BendingPlayer;
import com.projectkorra.projectkorra.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

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

			Player player = (Player) sender;
			BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);

			if (bendingPlayer.isBendingPermanentlyRemoved()) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + ConfigManager.getConfig(CommandPropertiesConfig.class).BendingPermanentlyRemoved);
				return;
			}

			if (!bendingPlayer.getElements().isEmpty() && !sender.hasPermission("bending.command.rechoose")) {
				GeneralMethods.sendBrandingMessage(sender, super.noPermissionMessage);
				return;
			}

			String elementName = args.get(0).toLowerCase();

			if (elementName.equalsIgnoreCase("a")) {
				elementName = "air";
			} else if (elementName.equalsIgnoreCase("e")) {
				elementName = "earth";
			} else if (elementName.equalsIgnoreCase("f")) {
				elementName = "fire";
			} else if (elementName.equalsIgnoreCase("w")) {
				elementName = "water";
			} else if (elementName.equalsIgnoreCase("c")) {
				elementName = "chi";
			}

			final Element element = this.elementManager.getElement(elementName);

			if (element == null) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.invalidElement);
				return;
			}

			if (!this.hasPermission(sender, elementName)) {
				return;
			}
			if (bendingPlayer.isOnCooldown("ChooseElement")) {
				if (sender.hasPermission("bending.choose.ignorecooldown") || sender.hasPermission("bending.admin.choose")) {
					bendingPlayer.removeCooldown("ChooseElement");
				} else {
					GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.onCooldown.replace("%cooldown%", TimeUtil.formatTime(bendingPlayer.getCooldown("ChooseElement") - System.currentTimeMillis())));
					return;
				}
			}

			this.add(sender, (Player) sender, element);

			if (sender.hasPermission("bending.choose.ignorecooldown") || sender.hasPermission("bending.admin.choose")) {
				return;
			}

			bendingPlayer.addCooldown("ChooseElement", this.cooldown, true);
		} else if (args.size() == 2) {
			if (!sender.hasPermission("bending.admin.choose")) {
				GeneralMethods.sendBrandingMessage(sender, super.noPermissionMessage);
				return;
			}

			final Player player = ProjectKorra.plugin.getServer().getPlayer(args.get(1));

			if (player == null || !player.isOnline()) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.playerNotFound);
				return;
			}

			String elementName = args.get(0).toLowerCase();
			if (elementName.equalsIgnoreCase("a")) {
				elementName = "air";
			} else if (elementName.equalsIgnoreCase("e")) {
				elementName = "earth";
			} else if (elementName.equalsIgnoreCase("f")) {
				elementName = "fire";
			} else if (elementName.equalsIgnoreCase("w")) {
				elementName = "water";
			} else if (elementName.equalsIgnoreCase("c")) {
				elementName = "chi";
			}

			final Element element = this.elementManager.getElement(elementName);

			if (element == null || element.equals(this.elementManager.getAvatar())) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.invalidElement);
				return;
			}

			this.add(sender, player, element);

			if (player.hasPermission("bending.choose.ignorecooldown") || player.hasPermission("bending.admin.choose")) {
				return;
			}

			final BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);
			bendingPlayer.addCooldown("ChooseElement", this.cooldown, true);
		}
	}

	/**
	 * Adds the ability to bend the given element to the specified Player.
	 *
	 * @param sender The CommandSender who issued the command
	 * @param player The Player to add the element to
	 * @param element The element to add to the Player
	 */
	private void add(final CommandSender sender, final Player player, final Element element) {
		final BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);

		if (bendingPlayer == null) {
			return;
		}

		this.elementManager.setElement(player, element);

		final ChatColor color = element != null ? element.getColor() : ChatColor.WHITE;
		boolean vowel = GeneralMethods.isVowel(ChatColor.stripColor(element.getName()).charAt(0));
		
		if (!(sender instanceof Player) || !(sender).equals(player)) {
			if (vowel) {
				GeneralMethods.sendBrandingMessage(sender, color + this.chosenOtherVowel.replace("{target}", ChatColor.DARK_AQUA + player.getName() + color).replace("{element}", element.getName() + element.getType().getBender()));
			} else {
				GeneralMethods.sendBrandingMessage(sender, color + this.chosenOther.replace("{target}", ChatColor.DARK_AQUA + player.getName() + color).replace("{element}", element.getName() + element.getType().getBender()));
			}
		} else {
			if (vowel) {
				GeneralMethods.sendBrandingMessage(player, color + this.chosenVowel.replace("{element}", element.getName() + element.getType().getBender()));
			} else {
				GeneralMethods.sendBrandingMessage(player, color + this.chosen.replace("{element}", element.getName() + element.getType().getBender()));
			}
		}
		
		Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(sender, player, element, Result.CHOOSE));
		GeneralMethods.removeUnusableAbilities(player.getName());
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (args.size() >= 2 || !sender.hasPermission("bending.command.choose")) {
			return new ArrayList<>();
		}

		final List<String> l = new ArrayList<String>();
		if (args.size() == 0) {
			for (Element element : this.elementManager.getElements()) {
				if (!(element instanceof SubElement)) {
					l.add(element.getName());
				}
			}
		} else {
			for (final Player p : Bukkit.getOnlinePlayers()) {
				l.add(p.getName());
			}
		}
		return l;
	}
}

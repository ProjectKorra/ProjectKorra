package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.OfflineBendingPlayer;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ChatUtil;
import com.projectkorra.projectkorra.util.TimeUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Executor for /bending temp. Extends {@link PKCommand}.
 *
 */
public class TempCommand extends PKCommand {

	protected static TempCommand TEMP_COMMAND; //So the remove command can access some of the temp command methods

	private final String playerNotFound;
	private final String invalidElement;
	private final String invalidTime;
	private final String addedSuccess;
	private final String addedSuccessOther;
	private final String addedSuccessAvatar;
	private final String addedSuccessOtherAvatar;
	private final String extendSuccess;
	private final String extendSuccessOther;
	private final String reduceSuccess;
	private final String reduceSuccessOther;
	private final String reduceSuccessRemove;
	private final String reduceSuccessOtherRemove;
	private final String removeSuccess;
	private final String removeSuccessOther;
	private final String removeElementNotFound;
	private final String removeNoElements;
	private final String alreadyHasTempElement;
	private final String alreadyHasElement;
	private final String alreadyHasSubElement;
	private final String alreadyHasTempSubElement;

	private final String[] addAliases = {"add", "a"};
	private final String[] extendAliases = {"extend", "e"};
	private final String[] reduceAliases = {"reduce", "re"};
	private final String[] removeAliases = {"remove", "r"};

	public TempCommand() {
		super("temp", "/bending temp <Add/Extend/Reduce/Remove> <Player> [Element] [Time]",
				ConfigManager.languageConfig.get().getString("Commands.Temp.Description.Main"), new String[] { "temp", "te", "temporary", "tempadd", "addtemp"});

		this.playerNotFound = ChatUtil.color(ConfigManager.languageConfig.get().getString("Commands.Temp.PlayerNotFound"));
		this.invalidElement = ChatUtil.color(ConfigManager.languageConfig.get().getString("Commands.Temp.InvalidElement"));
		this.invalidTime = ChatUtil.color(ConfigManager.languageConfig.get().getString("Commands.Temp.InvalidTime"));
		this.alreadyHasElement = ChatUtil.color(ConfigManager.languageConfig.get().getString("Commands.Temp.AlreadyHasElement"));
		this.alreadyHasSubElement = ChatUtil.color(ConfigManager.languageConfig.get().getString("Commands.Temp.AlreadyHasSubElement"));
		this.alreadyHasTempElement = ChatUtil.color(ConfigManager.languageConfig.get().getString("Commands.Temp.AlreadyHasTempElement"));
		this.alreadyHasTempSubElement = ChatUtil.color(ConfigManager.languageConfig.get().getString("Commands.Temp.AlreadyHasTempSubElement"));
		this.addedSuccess = ChatUtil.color(ConfigManager.languageConfig.get().getString("Commands.Temp.Add.Success"));
		this.addedSuccessAvatar = ChatUtil.color(ConfigManager.languageConfig.get().getString("Commands.Temp.Add.SuccessAvatar"));
		this.addedSuccessOther = ChatUtil.color(ConfigManager.languageConfig.get().getString("Commands.Temp.Add.SuccessOther"));
		this.addedSuccessOtherAvatar = ChatUtil.color(ConfigManager.languageConfig.get().getString("Commands.Temp.Add.SuccessAvatarOther"));
		this.extendSuccess = ChatUtil.color(ConfigManager.languageConfig.get().getString("Commands.Temp.Extend.Success"));
		this.extendSuccessOther = ChatUtil.color(ConfigManager.languageConfig.get().getString("Commands.Temp.Extend.SuccessOther"));
		this.reduceSuccess = ChatUtil.color(ConfigManager.languageConfig.get().getString("Commands.Temp.Reduce.Success"));
		this.reduceSuccessOther = ChatUtil.color(ConfigManager.languageConfig.get().getString("Commands.Temp.Reduce.SuccessOther"));
		this.reduceSuccessRemove = ChatUtil.color(ConfigManager.languageConfig.get().getString("Commands.Temp.Reduce.SuccessRemove"));
		this.reduceSuccessOtherRemove = ChatUtil.color(ConfigManager.languageConfig.get().getString("Commands.Temp.Reduce.SuccessOtherRemove"));
		this.removeSuccess = ChatUtil.color(ConfigManager.languageConfig.get().getString("Commands.Temp.Remove.Success"));
		this.removeSuccessOther = ChatUtil.color(ConfigManager.languageConfig.get().getString("Commands.Temp.Remove.SuccessOther"));
		this.removeElementNotFound = ChatUtil.color(ConfigManager.languageConfig.get().getString("Commands.Temp.Remove.ElementNotFound"));
		this.removeNoElements = ChatUtil.color(ConfigManager.languageConfig.get().getString("Commands.Temp.Remove.NoElements"));

		TEMP_COMMAND = this;
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.correctLength(sender, args.size(), 1, 4)) {
			return;
		} else if (args.size() == 1 || args.size() == 2 || (args.size() == 3 && !Arrays.asList(removeAliases).contains(args.get(0).toLowerCase()))) {
			if (!this.hasPermission(sender)) {
				return;
			}

			if (Arrays.asList(addAliases).contains(args.get(0).toLowerCase())) {
				help(sender, "Add");
			} else if (Arrays.asList(extendAliases).contains(args.get(0).toLowerCase())) {
				help(sender, "Extend");
			} else if (Arrays.asList(reduceAliases).contains(args.get(0).toLowerCase())) {
				help(sender, "Reduce");
			} else if (Arrays.asList(removeAliases).contains(args.get(0).toLowerCase())) {
				help(sender, "Remove");
			} else {
				help(sender, true);
			}
			return;
		}

		getPlayer(args.get(1)).thenAccept(p -> _execute(p, sender, args));

	}

	public void _execute(OfflinePlayer player, final CommandSender sender, final List<String> args) {

		if (!player.hasPlayedBefore() && !player.isOnline()) {
			ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.playerNotFound);
			return;
		}

		if (Arrays.asList(removeAliases).contains(args.get(0).toLowerCase())) {
			if (!hasPermission(sender, "remove")) return;
			if (args.size() > 2) {
				Element element = Element.fromString(args.get(2));

				if (element == null && !args.get(2).equalsIgnoreCase("all")) {
					ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.invalidElement);
					return;
				} else if (args.get(2).equalsIgnoreCase("all")) {
					BendingPlayer.getOrLoadOfflineAsync(player).thenAccept(bPlayer -> {
						removeAll(bPlayer, sender);
					});
				} else {
					BendingPlayer.getOrLoadOfflineAsync(player).thenAccept(bPlayer -> {
						removeElement(element, bPlayer, sender, false);
					});
				}
			} else {
				BendingPlayer.getOrLoadOfflineAsync(player).thenAccept(bPlayer -> {
					removeAll(bPlayer, sender);
				});
			}
			return;
		}

		Element element = Element.fromString(args.get(2));

		if (element == null && !args.get(2).equalsIgnoreCase("all")) {
			ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.invalidElement);
			return;
		}

		final long time;

		try {
			time = TimeUtil.unformatTime(args.get(3));

			if (time <= 0) {
				ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.invalidTime);
				return;
			}
		} catch (NumberFormatException e) {
			ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.invalidTime);
			return;
		}

		if (Arrays.asList(addAliases).contains(args.get(0).toLowerCase())) {
			if (!hasPermission(sender, "add")) return;
			BendingPlayer.getOrLoadOfflineAsync(player).thenAccept(bPlayer -> Bukkit.getScheduler().runTaskLater(ProjectKorra.plugin, () -> {
				addElement(element, bPlayer, sender, time);
			}, 1L));
		} else if (Arrays.asList(extendAliases).contains(args.get(0).toLowerCase())) {
			if (!hasPermission(sender, "extend")) return;
			BendingPlayer.getOrLoadOfflineAsync(player).thenAccept(bPlayer -> Bukkit.getScheduler().runTaskLater(ProjectKorra.plugin, () -> {
				extendElement(element, bPlayer, sender, time);
			}, 1L));
		} else if (Arrays.asList(reduceAliases).contains(args.get(0).toLowerCase())) {
			if (!hasPermission(sender, "reduce")) return;
			BendingPlayer.getOrLoadOfflineAsync(player).thenAccept(bPlayer -> Bukkit.getScheduler().runTaskLater(ProjectKorra.plugin, () ->{
				reduceElement(element, bPlayer, sender, time, false);
			}, 1L));
		}
	}

	private void help(CommandSender sender, String type) {
		if (!hasPermission(sender, type.toLowerCase())) return;

		String cmd = "/bending temp " + type.toLowerCase() + " <player> <element> <time>";
		if (type.equalsIgnoreCase("remove")) cmd = "/bending temp remove <player> <element>";

		sender.sendMessage(ChatUtil.color(ChatColor.GOLD + ConfigManager.languageConfig.get().getString("Commands.ProperUsage")
				.replace("{command}", ChatColor.DARK_AQUA + cmd)));
		sender.sendMessage(ChatUtil.color(ChatColor.YELLOW + ConfigManager.languageConfig.get().getString("Commands.Temp.Description." + type)));
	}

	private void addElement(Element element, OfflineBendingPlayer bPlayer, CommandSender sender, long time) {
		boolean sub = element instanceof SubElement;

		if (!sub && bPlayer.getElements().contains(element)) {
			ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.alreadyHasElement.replace("{target}", bPlayer.getName()));
			return;
		} else if (sub && bPlayer.getSubElements().contains(element)) {
			ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.alreadyHasSubElement.replace("{target}", bPlayer.getName()));
			return;
		} else if (!sub && bPlayer.getTempElements().containsKey(element) && bPlayer.getTempElementTime(element) > 0) {
			ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.alreadyHasTempElement.replace("{target}", bPlayer.getName()));
			return;
		} else if (sub && bPlayer.getTempSubElements().containsKey(element) && bPlayer.getTempElementTime(element) > 0) {
			ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.alreadyHasTempSubElement.replace("{target}", bPlayer.getName()));
			return;
		}

		if (!bPlayer.addTempElement(element, sender, time)) return;

		String message = element == Element.AVATAR ? addedSuccessAvatar : addedSuccess;
		String messageOther = element == Element.AVATAR ? addedSuccessOtherAvatar : addedSuccessOther;

		long expiry = time + System.currentTimeMillis();
		String newExpiryString = TimeUtil.formatTime(time, true);


		ChatUtil.sendBrandingMessage(sender, ChatColor.YELLOW + messageOther
				.replace("{element}", element.getColor() + element.getName() + ChatColor.YELLOW)
				.replace("{bending}", element.getColor() + element.getType().getBending()+ ChatColor.YELLOW)
				.replace("{bender}", element.getColor() + element.getType().getBender()+ ChatColor.YELLOW)
				.replace("{bend}", element.getColor() + element.getType().getBend() + ChatColor.YELLOW)
				.replace("{target}", bPlayer.getName())
				.replace("{time}", newExpiryString));

		if (bPlayer.isOnline() && (!(sender instanceof Player) || !((Player)sender).getUniqueId().equals(bPlayer.getUUID()))) {
			ChatUtil.sendBrandingMessage(bPlayer.getPlayer().getPlayer(), ChatColor.YELLOW + message
					.replace("{element}", element.getColor() + element.getName() + ChatColor.YELLOW)
					.replace("{bending}", element.getColor() + element.getType().getBending() + ChatColor.YELLOW)
					.replace("{bender}", element.getColor() + element.getType().getBender() + ChatColor.YELLOW)
					.replace("{bend}", element.getColor() + element.getType().getBend() + ChatColor.YELLOW)
					.replace("{time}", newExpiryString));
		}
	}

	private void extendElement(Element element, OfflineBendingPlayer bPlayer, CommandSender sender, long time) {
		if (element == null) { //All elements
			for (Element e1 : Element.getAllElements()) {
				extendElement(e1, bPlayer, sender, time);
			}
			for (Element e1 : Element.getAllSubElements()) {
				extendElement(e1, bPlayer, sender, time);
			}
			return;
		}

		boolean add;
		long oldTime;

		if (element instanceof SubElement) {
			//If they don't have it, or they do but it has already expired
			add = !bPlayer.getTempSubElements().containsKey(element) || (bPlayer.getTempSubElements().get(element) != -1 && bPlayer.getTempSubElements().get(element) < System.currentTimeMillis());

			oldTime = bPlayer.getTempSubElementRelativeTime((SubElement) element);
		} else {
			//If they don't have it, or they do but it has already expired
			add = !bPlayer.getTempElements().containsKey(element) || bPlayer.getTempElements().get(element) < System.currentTimeMillis();

			oldTime = bPlayer.getTempElementRelativeTime(element);
		}

		long newExpiry = time + oldTime;
		String newExpiryString = TimeUtil.formatTime(newExpiry);

		String message = add ? (element == Element.AVATAR ? this.addedSuccessAvatar : this.addedSuccess) : this.extendSuccess;
		String messageOther = add ? (element == Element.AVATAR ? this.addedSuccessOtherAvatar : this.addedSuccessOther) : this.extendSuccessOther;

		if (!bPlayer.setTempElement(element, sender, newExpiry)) return;

		ChatUtil.sendBrandingMessage(sender, ChatColor.YELLOW + messageOther
				.replace("{element}", element.getColor() + element.getName())
				.replace("{bending}", element.getType().getBending())
				.replace("{bender}", element.getType().getBender())
				.replace("{bend}", element.getType().getBend())
				.replace("{target}", bPlayer.getName())
				.replace("{time}", newExpiryString));

		if (bPlayer.isOnline() && (!(sender instanceof Player) || !((Player)sender).getUniqueId().equals(bPlayer.getUUID()))) {
			ChatUtil.sendBrandingMessage(bPlayer.getPlayer().getPlayer(), ChatColor.YELLOW + message
					.replace("{element}", element.getColor() + element.getName())
					.replace("{element}", element.getColor() + element.getName())
					.replace("{bending}", element.getType().getBending())
					.replace("{bender}", element.getType().getBender())
					.replace("{bend}", element.getType().getBend())
					.replace("{time}", newExpiryString));
		}
	}

	private void reduceElement(Element element, OfflineBendingPlayer bPlayer, CommandSender sender, long time, boolean all) {
		if (element == null) { //All elements
			for (Element e1 : Element.getAllElements()) {
				reduceElement(e1, bPlayer, sender, time, true);
			}
			for (Element e1 : Element.getAllSubElements()) {
				reduceElement(e1, bPlayer, sender, time, true);
			}
			return;
		}

		if (element instanceof SubElement && bPlayer.getTempSubElements().containsKey(element) && bPlayer.getTempSubElements().get(element) == -1L) {
			return; //Skip elements tied to parent elements
		}

		boolean elementCheck = !bPlayer.getTempElements().containsKey(element) || bPlayer.getTempElements().get(element) < System.currentTimeMillis();
		boolean subElementCheck = !bPlayer.getTempSubElements().containsKey(element) || (bPlayer.getTempSubElements().get(element) < System.currentTimeMillis() && bPlayer.getTempSubElements().get(element) != -1L);

		//If they don't have it, or they do but it has already expired
		if (element instanceof SubElement ? subElementCheck : elementCheck) { //or it has already expired
			if (!all) { //Don't bother the player if we are doing it for all elements
				ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.removeElementNotFound
						.replace("{element}", element.getColor() + element.getName())
						.replace("{bending}", element.getType().getBending())
						.replace("{bender}", element.getType().getBender())
						.replace("{bend}", element.getType().getBend())
						.replace("{target}", bPlayer.getName()));
			}

			return;
		}

		boolean remove;
		long newExpiry;

		if (element instanceof SubElement) {
			remove = bPlayer.getTempSubElements().get(element) - time < System.currentTimeMillis() && bPlayer.getTempSubElements().get(element) != -1L;
			newExpiry = remove ? 0 : bPlayer.getTempSubElementRelativeTime((SubElement) element) - time;
		} else {
			remove = bPlayer.getTempElements().get(element) - time < System.currentTimeMillis();
			newExpiry = remove ? 0 : bPlayer.getTempElementRelativeTime(element) - time;
		}

		String newExpiraryString = TimeUtil.formatTime(newExpiry);

		String message = remove ? this.reduceSuccessRemove : this.reduceSuccess;
		String messageOther = remove ? this.reduceSuccessOtherRemove : this.reduceSuccessOther;

		if (!bPlayer.setTempElement(element, sender, newExpiry)) return;

		ChatUtil.sendBrandingMessage(sender, ChatColor.YELLOW + messageOther
				.replace("{element}", element.getColor() + element.getName())
				.replace("{bending}", element.getType().getBending())
				.replace("{bender}", element.getType().getBender())
				.replace("{bend}", element.getType().getBend())
				.replace("{target}", bPlayer.getName())
				.replace("{time}", newExpiraryString));

		if (bPlayer.isOnline() && (!(sender instanceof Player) || !((Player)sender).getUniqueId().equals(bPlayer.getUUID()))) {
			ChatUtil.sendBrandingMessage(bPlayer.getPlayer().getPlayer(), ChatColor.YELLOW + message
					.replace("{element}", element.getColor() + element.getName())
					.replace("{bending}", element.getType().getBending())
					.replace("{bender}", element.getType().getBender())
					.replace("{bend}", element.getType().getBend())
					.replace("{time}", newExpiraryString));
		}
	}

	public boolean removeElement(Element element, OfflineBendingPlayer bPlayer, CommandSender sender, boolean all) {
		//If they don't have it, or they do but it has already expired

		if (!bPlayer.hasTempElement(element)) { //or it has already expired
			if (!all) { //Don't bother the player if we are doing it for all elements
				ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.removeElementNotFound
						.replace("{element}", element.getColor() + element.getName())
						.replace("{bending}", element.getType().getBending())
						.replace("{bender}", element.getType().getBender())
						.replace("{bend}", element.getType().getBend())
						.replace("{target}", bPlayer.getName()));
			}
			return false;
		}

		if (!bPlayer.removeTempElement(element, sender)) return false;

		ChatUtil.sendBrandingMessage(sender, ChatColor.YELLOW + this.removeSuccessOther
				.replace("{element}", element.getColor() + element.getName())
				.replace("{bending}", element.getType().getBending())
				.replace("{bender}", element.getType().getBender())
				.replace("{bend}", element.getType().getBend())
				.replace("{target}", bPlayer.getName()));

		if (bPlayer.isOnline() && (!(sender instanceof Player) || !((Player)sender).getUniqueId().equals(bPlayer.getUUID()))) {
			ChatUtil.sendBrandingMessage(bPlayer.getPlayer().getPlayer(), ChatColor.YELLOW + this.removeSuccess
					.replace("{element}", element.getColor() + element.getName())
					.replace("{bending}", element.getType().getBending())
					.replace("{bender}", element.getType().getBender())
					.replace("{bend}", element.getType().getBend()));
		}

		return true;
	}

	private void removeAll(OfflineBendingPlayer bPlayer, CommandSender sender) {
		List<Element> elements = new ArrayList<>(bPlayer.getTempElements().keySet()); //Clone the keyset so we don't get concurrent modifications
		elements.addAll(bPlayer.getTempSubElements().keySet());
		boolean removed = false;

		for (Element e : elements) {
			if (removeElement(e, bPlayer, sender, true)) removed = true;
		}

		if (!removed) {
			ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.removeNoElements.replace("{target}", bPlayer.getName()));
		}
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (args.size() >= 4 || !sender.hasPermission("bending.command.temp")) {
			return new ArrayList<>();
		}
		final List<String> l = new ArrayList<String>();
		if (args.size() == 0) {
			for (String cmd : new String[]{"add", "extend", "reduce", "remove"}) {
				if (sender.hasPermission("bending.command.temp." + cmd)) l.add(cmd);
			}
		} else if (args.size() == 1) {
			return getOnlinePlayerNames(sender);
		} else if (args.size() == 2) {
			l.addAll(Arrays.stream(Element.getAllElements()).map(Element::getName).collect(Collectors.toList()));
			l.addAll(Arrays.stream(Element.getAllSubElements()).map(Element::getName).collect(Collectors.toList()));

			l.add("all");
		} else if (!Arrays.asList(removeAliases).contains(args.get(0).toLowerCase())){
			l.add("30m");
			l.add("30d");
			l.add("1h");
			l.add("1d");
			l.add("6h");
			l.add("24h");
			l.add("7d");
		}

		return l;
	}
}

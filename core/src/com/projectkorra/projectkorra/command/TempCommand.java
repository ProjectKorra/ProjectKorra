package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.OfflineBendingPlayer;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ChatUtil;
import com.projectkorra.projectkorra.util.TimeUtil;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Executor for /bending add. Extends {@link PKCommand}.
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

		OfflinePlayer player = Bukkit.getOfflinePlayer(args.get(1));

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
						if (removeElement(element, bPlayer, sender)) {
							if (bPlayer.isOnline())
								((BendingPlayer)bPlayer).recalculateTempElements(false);
							else
								bPlayer.saveTempElements();
						}
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

		if (element == null) {
			if (args.get(2).equalsIgnoreCase("all")) {

			}


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
			BendingPlayer.getOrLoadOfflineAsync(player).thenAccept(bPlayer -> {
				addElement(element, bPlayer, sender, time);
			});
		} else if (Arrays.asList(extendAliases).contains(args.get(0).toLowerCase())) {
			if (!hasPermission(sender, "extend")) return;
			BendingPlayer.getOrLoadOfflineAsync(player).thenAccept(bPlayer -> {
				extendElement(element, bPlayer, sender, time);
			});
		} else if (Arrays.asList(reduceAliases).contains(args.get(0).toLowerCase())) {
			if (!hasPermission(sender, "reduce")) return;
			BendingPlayer.getOrLoadOfflineAsync(player).thenAccept(bPlayer -> {
				reduceElement(element, bPlayer, sender, time);
			});
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
		} else if (!sub && bPlayer.getTempElements().containsKey(element)) {
			ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.alreadyHasTempElement.replace("{target}", bPlayer.getName()));
			return;
		} else if (sub && bPlayer.getTempElements().containsKey(element)) {
			ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.alreadyHasTempSubElement.replace("{target}", bPlayer.getName()));
			return;
		}

		long expiry = time + System.currentTimeMillis();
		String newExpiryString = TimeUtil.formatTime(time, true);

		String message = element == Element.AVATAR ? this.addedSuccessAvatar : this.addedSuccess;
		String messageOther = element == Element.AVATAR ? this.addedSuccessOtherAvatar : this.addedSuccessOther;

		ChatUtil.sendBrandingMessage(sender, ChatColor.YELLOW + messageOther
				.replace("{element}", element.getColor() + element.getName())
				.replace("{bending}", element.getType().getBending())
				.replace("{bender}", element.getType().getBender())
				.replace("{bend}", element.getType().getBend())
				.replace("{target}", bPlayer.getName())
				.replace("{time}", newExpiryString));

		if (bPlayer.isOnline() && (!(sender instanceof Player) || !((Player)sender).getUniqueId().equals(bPlayer.getUUID()))) {
			ChatUtil.sendBrandingMessage(sender, ChatColor.YELLOW + message
					.replace("{element}", element.getColor() + element.getName())
					.replace("{bending}", element.getType().getBending())
					.replace("{bender}", element.getType().getBender())
					.replace("{bend}", element.getType().getBend())
					.replace("{time}", newExpiryString));
		}

		if (element instanceof SubElement) {
			bPlayer.getTempSubElements().put((SubElement) element, expiry);
		} else {
			bPlayer.getTempElements().put(element, expiry);

			if (bPlayer.isOnline()) {
				for (final SubElement subElement : Element.getSubElements(element)) {
					if (((BendingPlayer)bPlayer).hasSubElementPermission(subElement)) {
						bPlayer.getTempSubElements().put(subElement, -1L); //Set the expiry to -1 to indicate that the time is linked to the parent element
					}
				}
			}
		}

		if (bPlayer.isOnline()) ((BendingPlayer)bPlayer).recalculateTempElements(false);
		else bPlayer.saveTempElements();
	}

	private void extendElement(Element element, OfflineBendingPlayer bPlayer, CommandSender sender, long time) {
		//If they don't have it, or they do but it has already expired
		boolean add = !bPlayer.getTempElements().containsKey(element) || bPlayer.getTempElements().get(element) < System.currentTimeMillis();

		long oldExpiry = bPlayer.getTempElements().getOrDefault(element, System.currentTimeMillis());
		long newExpiry = time + oldExpiry;
		String newExpiryString = TimeUtil.formatTime(newExpiry - System.currentTimeMillis());

		String message = add ? (element == Element.AVATAR ? this.addedSuccessAvatar : this.addedSuccess) : this.extendSuccess;
		String messageOther = add ? (element == Element.AVATAR ? this.addedSuccessOtherAvatar : this.addedSuccessOther) : this.extendSuccessOther;

		ChatUtil.sendBrandingMessage(sender, ChatColor.YELLOW + messageOther
				.replace("{element}", element.getColor() + element.getName())
				.replace("{bending}", element.getType().getBending())
				.replace("{bender}", element.getType().getBender())
				.replace("{bend}", element.getType().getBend())
				.replace("{target}", bPlayer.getName())
				.replace("{time}", newExpiryString));

		if (bPlayer.isOnline() && (!(sender instanceof Player) || !((Player)sender).getUniqueId().equals(bPlayer.getUUID()))) {
			ChatUtil.sendBrandingMessage(sender, ChatColor.YELLOW + message
					.replace("{element}", element.getColor() + element.getName())
					.replace("{element}", element.getColor() + element.getName())
					.replace("{bending}", element.getType().getBending())
					.replace("{bender}", element.getType().getBender())
					.replace("{bend}", element.getType().getBend())
					.replace("{time}", newExpiryString));
		}

		if (element instanceof SubElement) {
			bPlayer.getTempSubElements().put((SubElement) element, newExpiry);
		} else {
			bPlayer.getTempElements().put(element, newExpiry);

			if (bPlayer.isOnline()) {
				if (add) { //If we are just adding, add all subs to temp elements with the same expiry the player has permission for
					for (final SubElement sub : Element.getSubElements(element)) {
						if (((BendingPlayer) bPlayer).hasSubElementPermission(sub)) {
							bPlayer.getTempElements().put(sub, -1L); //Set the expiry to -1 to indicate that the time is linked to the parent element
						}
					}
				}
			}
		}

		if (bPlayer.isOnline()) ((BendingPlayer)bPlayer).recalculateTempElements(false);
		else bPlayer.saveTempElements();
	}

	private void reduceElement(Element element, OfflineBendingPlayer bPlayer, CommandSender sender, long time) {
		//If they don't have it, or they do but it has already expired
		if (!bPlayer.getTempElements().containsKey(element) || bPlayer.getTempElements().get(element) < System.currentTimeMillis()) { //or it has already expired
			ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.removeElementNotFound
					.replace("{element}", element.getColor() + element.getName())
					.replace("{bending}", element.getType().getBending())
					.replace("{bender}", element.getType().getBender())
					.replace("{bend}", element.getType().getBend())
					.replace("{target}", bPlayer.getName()));
			return;
		}

		boolean remove = bPlayer.getTempElements().get(element) - time < System.currentTimeMillis();

		long newExpiry = remove ? 0 : bPlayer.getTempElements().get(element) - time;
		String newExpiraryString = TimeUtil.formatTime(newExpiry - System.currentTimeMillis());

		String message = remove ? this.reduceSuccessRemove : this.reduceSuccess;
		String messageOther = remove ? this.reduceSuccessOtherRemove : this.reduceSuccessOther;

		ChatUtil.sendBrandingMessage(sender, ChatColor.YELLOW + messageOther
				.replace("{element}", element.getColor() + element.getName())
				.replace("{bending}", element.getType().getBending())
				.replace("{bender}", element.getType().getBender())
				.replace("{bend}", element.getType().getBend())
				.replace("{target}", bPlayer.getName())
				.replace("{time}", newExpiraryString));

		if (bPlayer.isOnline() && (!(sender instanceof Player) || !((Player)sender).getUniqueId().equals(bPlayer.getUUID()))) {
			ChatUtil.sendBrandingMessage(sender, ChatColor.YELLOW + message
					.replace("{element}", element.getColor() + element.getName())
					.replace("{bending}", element.getType().getBending())
					.replace("{bender}", element.getType().getBender())
					.replace("{bend}", element.getType().getBend())
					.replace("{time}", newExpiraryString));
		}

		if (element instanceof SubElement) {
			if (remove) bPlayer.getTempSubElements().remove(element);
			else bPlayer.getTempSubElements().put((SubElement) element, newExpiry);
		} else { //For parent elements
			if (remove) {
				bPlayer.getTempElements().remove(element);

				for (SubElement tempSub : bPlayer.getTempSubElements().keySet()) {
					long expiry = bPlayer.getTempSubElements().get(tempSub);
					if (tempSub.getParentElement().equals(element) && expiry == -1L) { //If the sub expiry is linked to the parent element
						bPlayer.getTempSubElements().remove(tempSub);
					}
				}
			} else { //If not removing, set the new expiry
				bPlayer.getTempElements().put(element, newExpiry);
			}
		}

		if (bPlayer.isOnline())
			((BendingPlayer)bPlayer).recalculateTempElements(false);
		else
			bPlayer.saveTempElements();
	}

	public boolean removeElement(Element element, OfflineBendingPlayer bPlayer, CommandSender sender) {
		//If they don't have it, or they do but it has already expired
		if (!bPlayer.getTempElements().containsKey(element) || bPlayer.getTempElements().get(element) < System.currentTimeMillis()) { //or it has already expired
			ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.removeElementNotFound
					.replace("{element}", element.getColor() + element.getName())
					.replace("{bending}", element.getType().getBending())
					.replace("{bender}", element.getType().getBender())
					.replace("{bend}", element.getType().getBend())
					.replace("{target}", bPlayer.getName()));
			return false;
		}

		if (element instanceof SubElement) {
			if (bPlayer.isOnline()) {
				bPlayer.getTempSubElements().remove(element);
			} else {										    	        	//Mark it to be removed when the player logs in next. Allows
				bPlayer.getTempSubElements().put((SubElement) element, 0L); 	//the player to see that it was removed when they were offline
			}
		} else { //For parent elements
			if (bPlayer.isOnline()) {
				bPlayer.getTempElements().remove(element);
			} else {										    	//Mark it to be removed when the player logs in next. Allows
				bPlayer.getTempElements().put(element, 0L); 		//the player to see that it was removed when they were offline
			}

			//Remove all subs that are tied to the parent element
			for (SubElement tempSub : bPlayer.getTempSubElements().keySet()) {
				long expiry = bPlayer.getTempSubElements().get(tempSub);

				if (tempSub.getParentElement().equals(element) && expiry == -1L) { //If the sub expiry is linked to the parent element
					bPlayer.getTempSubElements().remove(tempSub);
				}
			}
		}

		ChatUtil.sendBrandingMessage(sender, ChatColor.YELLOW + this.removeSuccessOther
				.replace("{element}", element.getColor() + element.getName())
				.replace("{bending}", element.getType().getBending())
				.replace("{bender}", element.getType().getBender())
				.replace("{bend}", element.getType().getBend())
				.replace("{target}", bPlayer.getName()));

		if (bPlayer.isOnline() && (!(sender instanceof Player) || !((Player)sender).getUniqueId().equals(bPlayer.getUUID()))) {
			ChatUtil.sendBrandingMessage(sender, ChatColor.YELLOW + this.removeSuccess
					.replace("{element}", element.getColor() + element.getName())
					.replace("{bending}", element.getType().getBending())
					.replace("{bender}", element.getType().getBender())
					.replace("{bend}", element.getType().getBend()));
		}

		return true;
	}

	private void removeAll(OfflineBendingPlayer bPlayer, CommandSender sender) {
		List<Element> elements = new ArrayList<>(bPlayer.getTempElements().keySet()); //Clone the keyset so we don't get concurrent modifications
		boolean removed = false;

		for (Element e : elements) {
			if (removeElement(e, bPlayer, sender)) removed = true;
		}

		if (removed)
			if (bPlayer.isOnline())
				((BendingPlayer)bPlayer).recalculateTempElements(false);
			else
				bPlayer.saveTempElements();
		else {
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

			if (Arrays.asList(removeAliases).contains(args.get(0).toLowerCase())) {
				l.add("all");
			}
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

package com.projectkorra.projectkorra.command;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.storage.DBConnection;
import com.projectkorra.projectkorra.util.Statistic;
import com.projectkorra.projectkorra.util.StatisticsMethods;

public class StatsCommand extends PKCommand {

	private static final String[] getaliases = { "get", "g" };
	private static final String[] leaderboardaliases = { "leaderboard", "lb", "l" };

	private String invalidLookup;
	private String invalidSearchType;
	private String invalidStatistic;
	private String invalidPlayer;

	public StatsCommand() {
		super("stats", "/bending stats <get/leaderboard> <ability/element/all> <statistic> [player/page]", ConfigManager.languageConfig.get().getString("Commands.Stats.Description"), new String[] { "statistics", "stats" });

		this.invalidLookup = ConfigManager.languageConfig.get().getString("Commands.Stats.InvalidLookup");
		this.invalidSearchType = ConfigManager.languageConfig.get().getString("Commands.Stats.InvalidSearchType");
		this.invalidStatistic = ConfigManager.languageConfig.get().getString("Commands.Stats.InvalidStatistic");
		this.invalidPlayer = ConfigManager.languageConfig.get().getString("Commands.Stats.InvalidPlayer");
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if (!correctLength(sender, args.size(), 3, 4)) {
			return;
		}
		CoreAbility ability = CoreAbility.getAbility(args.get(1));
		Element element = Element.getElement(args.get(1));
		Object object = null;
		if (ability != null) {
			object = ability;
		} else if (element != null) {
			object = element;
		}
		Statistic statistic = Statistic.getStatistic(args.get(2));

		boolean containsGet = contains(args.get(0), Arrays.asList(getaliases));
		boolean containsLeaderboard = contains(args.get(0), Arrays.asList(leaderboardaliases));
		if (!containsGet && !containsLeaderboard) {
			GeneralMethods.sendBrandingMessage(sender, invalidLookup);
			return;
		} else if (object == null && !args.get(1).equalsIgnoreCase("all")) {
			GeneralMethods.sendBrandingMessage(sender, invalidSearchType);
			return;
		} else if (statistic == null) {
			GeneralMethods.sendBrandingMessage(sender, invalidStatistic);
			return;
		}
		if (containsGet) {
			Player target = null;
			if (args.size() == 4) {
				target = ProjectKorra.plugin.getServer().getPlayer(args.get(3));
				if (target == null) {
					GeneralMethods.sendBrandingMessage(sender, invalidPlayer.replace("%player%", args.get(3)));
					return;
				}
			} else {
				if (isPlayer(sender)) {
					target = (Player) sender;
				} else {
					return;
				}
			}
			String message = getTarget(object, statistic, target);
			GeneralMethods.sendBrandingMessage(sender, ChatColor.translateAlternateColorCodes('&', message));
		} else if (containsLeaderboard) {
			int page = 1;
			try {
				page = Integer.parseInt(args.get(3));
			} catch (IndexOutOfBoundsException | NumberFormatException e) {
			}
			Object o = object;
			int p = page;
			new BukkitRunnable() {
				@Override
				public void run() {
					List<String> messages = getLeaderboard(sender, o, statistic, p);
					for (String message : messages) {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
					}
				}
			}.runTaskAsynchronously(ProjectKorra.plugin);
		}
	}

	public boolean contains(String s, List<String> l) {
		for (String string : l) {
			if (string.equalsIgnoreCase(s)) {
				return true;
			}
		}
		return false;
	}

	public String getTarget(Object object, Statistic statistic, Player target) {
		String statName = statistic.name().substring(0, 1).toUpperCase() + statistic.name().substring(1).toLowerCase();
		String message = "&8- &f%object% " + statName + " &e%player%: " + "&f%value%";
		long value = 0;
		if (object == null) {
			value = StatisticsMethods.getStatisticTotal(target.getUniqueId(), statistic);
		} else {
			value = StatisticsMethods.getStatistic(target.getUniqueId(), object, statistic);
		}
		if (object instanceof CoreAbility) {
			CoreAbility ability = (CoreAbility) object;
			message = message.replace("%object%", ability.getName()).replace("%player%", target.getName()).replace("%value%", String.valueOf(value));
		} else if (object instanceof Element) {
			Element element = (Element) object;
			message = message.replace("%object%", element.getName()).replace("%player%", target.getName()).replace("%value%", String.valueOf(value));
		} else {
			message = message.replace("%object%", "Total").replace("%player%", target.getName()).replace("%value%", String.valueOf(value));
		}
		return message;
	}

	public List<String> getLeaderboard(CommandSender sender, Object object, Statistic statistic, int page) {
		final List<String> messages = new ArrayList<>();
		List<UUID> uuids = pullUUIDs(statistic, object);
		int maxPage = (uuids.size() / 10) + 1;
		int p = page > maxPage ? maxPage : page;
		p = p < 1 ? 1 : p;
		String statName = statistic.name().substring(0, 1).toUpperCase() + statistic.name().substring(1).toLowerCase();
		String title = "%object% " + statName + " Leaderboard";
		if (object instanceof CoreAbility) {
			CoreAbility ability = (CoreAbility) object;
			title = title.replace("%object%", ability.getName());
		} else if (object instanceof Element) {
			Element element = (Element) object;
			title = title.replace("%object%", element.getName());
		} else {
			title = title.replace("%object%", "Total");
		}
		GeneralMethods.sendBrandingMessage(sender, ChatColor.translateAlternateColorCodes('&', "&8- &f" + title + " &8- [&7" + p + "/" + maxPage + "&8]"));
		int maxIndex = (10 * p) - 1;
		int minIndex = maxIndex - 9;
		try {
			uuids.get(minIndex);
		} catch (IndexOutOfBoundsException e) {
			messages.add("&7No statistics found.");
			return messages;
		}
		for (int index = minIndex; index < maxIndex; index++) {
			if (index < 0 || index >= uuids.size()) {
				break;
			}
			UUID uuid = uuids.get(index);
			long value = 0;
			if (object == null) {
				value = StatisticsMethods.getStatisticTotal(uuid, statistic);
			} else {
				value = StatisticsMethods.getStatistic(uuid, object, statistic);
			}
			OfflinePlayer oPlayer = ProjectKorra.plugin.getServer().getOfflinePlayer(uuid);
			messages.add("&7" + (index + 1) + ") &e" + oPlayer.getName() + " &f" + value);
		}
		return messages;
	}

	public List<UUID> pullUUIDs(Statistic statistic) {
		return pullUUIDs(statistic, null);
	}

	public List<UUID> pullUUIDs(Statistic statistic, Object object) {
		Set<UUID> uuids = new HashSet<>();
		try (ResultSet rs = DBConnection.sql.readQuery("SELECT uuid FROM pk_stats")) {
			while (rs.next()) {
				UUID uuid = UUID.fromString(rs.getString("uuid"));
				if (object == null) {
					if (StatisticsMethods.getStatisticTotal(uuid, statistic) > 0) {
						uuids.add(uuid);
					}
				} else {
					if (StatisticsMethods.getStatistic(uuid, object, statistic) > 0) {
						uuids.add(uuid);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		for (Player player : ProjectKorra.plugin.getServer().getOnlinePlayers()) {
			UUID uuid = player.getUniqueId();
			if (object == null) {
				if (StatisticsMethods.getStatisticTotal(uuid, statistic) > 0) {
					uuids.add(uuid);
				}
			} else {
				if (StatisticsMethods.getStatistic(uuid, object, statistic) > 0) {
					uuids.add(uuid);
				}
			}
		}
		List<UUID> list = new ArrayList<>(uuids);
		Collections.sort(list, new Comparator<UUID>() {
			@Override
			public int compare(UUID u1, UUID u2) {
				long value1 = 0;
				long value2 = 0;
				if (object == null) {
					value1 = StatisticsMethods.getStatisticTotal(u1, statistic);
					value2 = StatisticsMethods.getStatisticTotal(u2, statistic);
				} else {
					value1 = StatisticsMethods.getStatistic(u1, object, statistic);
					value2 = StatisticsMethods.getStatistic(u2, object, statistic);
				}
				return (int) (value2 - value1);
			}
		});
		return list;
	}

}

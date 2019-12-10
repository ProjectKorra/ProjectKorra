package com.projectkorra.projectkorra.command;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

	private final String invalidLookup;
	private final String invalidSearchType;
	private final String invalidStatistic;
	private final String invalidPlayer;

	public StatsCommand() {
		super("stats", "/bending stats <get/leaderboard> <ability/element/all> <statistic> [player/page]", ConfigManager.languageConfig.get().getString("Commands.Stats.Description"), new String[] { "statistics", "stats" });

		this.invalidLookup = ConfigManager.languageConfig.get().getString("Commands.Stats.InvalidLookup");
		this.invalidSearchType = ConfigManager.languageConfig.get().getString("Commands.Stats.InvalidSearchType");
		this.invalidStatistic = ConfigManager.languageConfig.get().getString("Commands.Stats.InvalidStatistic");
		this.invalidPlayer = ConfigManager.languageConfig.get().getString("Commands.Stats.InvalidPlayer");
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.correctLength(sender, args.size(), 3, 4)) {
			return;
		}
		final CoreAbility ability = CoreAbility.getAbility(args.get(1));
		final Element element = Element.getElement(args.get(1));
		Object object = null;
		if (ability != null) {
			object = ability;
		} else if (element != null) {
			object = element;
		}
		final Statistic statistic = Statistic.getStatistic(args.get(2));

		final boolean containsGet = this.contains(args.get(0), Arrays.asList(getaliases));
		final boolean containsLeaderboard = this.contains(args.get(0), Arrays.asList(leaderboardaliases));
		if (!containsGet && !containsLeaderboard) {
			GeneralMethods.sendBrandingMessage(sender, this.invalidLookup);
			return;
		} else if (object == null && !args.get(1).equalsIgnoreCase("all")) {
			GeneralMethods.sendBrandingMessage(sender, this.invalidSearchType);
			return;
		} else if (statistic == null) {
			GeneralMethods.sendBrandingMessage(sender, this.invalidStatistic);
			return;
		}
		if (containsGet) {
			Player target = null;
			if (args.size() == 4) {
				target = ProjectKorra.plugin.getServer().getPlayer(args.get(3));
				if (target == null) {
					GeneralMethods.sendBrandingMessage(sender, this.invalidPlayer.replace("%player%", args.get(3)));
					return;
				}
			} else {
				if (this.isPlayer(sender)) {
					target = (Player) sender;
				} else {
					return;
				}
			}
			final String message = this.getTarget(object, statistic, target);
			GeneralMethods.sendBrandingMessage(sender, ChatColor.translateAlternateColorCodes('&', message));
		} else if (containsLeaderboard) {
			int page = 1;
			try {
				page = Integer.parseInt(args.get(3));
			} catch (IndexOutOfBoundsException | NumberFormatException e) {}
			final Object o = object;
			final int p = page;
			new BukkitRunnable() {
				@Override
				public void run() {
					final List<String> messages = StatsCommand.this.getLeaderboard(sender, o, statistic, p);
					for (final String message : messages) {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
					}
				}
			}.runTaskAsynchronously(ProjectKorra.plugin);
		}
	}

	public boolean contains(final String s, final List<String> l) {
		for (final String string : l) {
			if (string.equalsIgnoreCase(s)) {
				return true;
			}
		}
		return false;
	}

	public String getTarget(final Object object, final Statistic statistic, final Player target) {
		String message = "&8- &f%object% " + statistic.getDisplayName() + " &e%player%: " + "&f%value%";
		long value = 0;
		if (object == null) {
			value = StatisticsMethods.getStatisticTotal(target.getUniqueId(), statistic);
		} else {
			value = StatisticsMethods.getStatistic(target.getUniqueId(), object, statistic);
		}
		if (object instanceof CoreAbility) {
			final CoreAbility ability = (CoreAbility) object;
			message = message.replace("%object%", ability.getName()).replace("%player%", target.getName()).replace("%value%", String.valueOf(value));
		} else if (object instanceof Element) {
			final Element element = (Element) object;
			message = message.replace("%object%", element.getName()).replace("%player%", target.getName()).replace("%value%", String.valueOf(value));
		} else {
			message = message.replace("%object%", "Total").replace("%player%", target.getName()).replace("%value%", String.valueOf(value));
		}
		return message;
	}

	public List<String> getLeaderboard(final CommandSender sender, final Object object, final Statistic statistic, final int page) {
		final List<String> messages = new ArrayList<>();
		final List<UUID> uuids = this.pullUUIDs(statistic, object);
		final int maxPage = (uuids.size() / 10) + 1;
		int p = page > maxPage ? maxPage : page;
		p = p < 1 ? 1 : p;
		String title = "%object% " + statistic.getDisplayName() + " Leaderboard";
		if (object instanceof CoreAbility) {
			final CoreAbility ability = (CoreAbility) object;
			title = title.replace("%object%", ability.getName());
		} else if (object instanceof Element) {
			final Element element = (Element) object;
			title = title.replace("%object%", element.getName());
		} else {
			title = title.replace("%object%", "Total");
		}
		GeneralMethods.sendBrandingMessage(sender, ChatColor.translateAlternateColorCodes('&', "&8- &f" + title + " &8- [&7" + p + "/" + maxPage + "&8]"));
		final int maxIndex = (10 * p) - 1;
		final int minIndex = maxIndex - 9;
		try {
			uuids.get(minIndex);
		} catch (final IndexOutOfBoundsException e) {
			messages.add("&7No statistics found.");
			return messages;
		}
		for (int index = minIndex; index < maxIndex; index++) {
			if (index < 0 || index >= uuids.size()) {
				break;
			}
			final UUID uuid = uuids.get(index);
			long value = 0;
			if (object == null) {
				value = StatisticsMethods.getStatisticTotal(uuid, statistic);
			} else {
				value = StatisticsMethods.getStatistic(uuid, object, statistic);
			}
			final OfflinePlayer oPlayer = ProjectKorra.plugin.getServer().getOfflinePlayer(uuid);
			messages.add("&7" + (index + 1) + ") &e" + oPlayer.getName() + " &f" + value);
		}
		return messages;
	}

	public List<UUID> pullUUIDs(final Statistic statistic) {
		return this.pullUUIDs(statistic, null);
	}

	public List<UUID> pullUUIDs(final Statistic statistic, final Object object) {
		final Set<UUID> uuids = new HashSet<>();
		try (ResultSet rs = DBConnection.sql.readQuery("SELECT uuid FROM pk_stats")) {
			while (rs.next()) {
				final UUID uuid = UUID.fromString(rs.getString("uuid"));
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
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		for (final Player player : ProjectKorra.plugin.getServer().getOnlinePlayers()) {
			final UUID uuid = player.getUniqueId();
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
		final List<UUID> list = new ArrayList<>(uuids);
		Collections.sort(list, (u1, u2) -> {
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
		});
		return list;
	}

}

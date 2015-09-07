package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.StockAbility;
import com.projectkorra.projectkorra.storage.DBConnection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Executor for /bending import. Extends {@link PKCommand}.
 */
public class ImportCommand extends PKCommand {

	boolean debugEnabled = ProjectKorra.plugin.getConfig().getBoolean("debug");
	BukkitTask importTask;

	public ImportCommand() {
		super("import", "/bending import", "This command will import your old bendingPlayers.yml from the Bending plugin. It will generate a convert.yml file to convert the data to be used with this plugin. You can delete the file once the complete message is displayed. This command should only be used ONCE.", new String[] { "import", "i" });
	}

	@Override
	public void execute(final CommandSender sender, List<String> args) {
		if (!hasPermission(sender) || !correctLength(sender, args.size(), 0, 0)) {
			return;
		} else if (!GeneralMethods.isImportEnabled()) {
			sender.sendMessage(ChatColor.RED + "Importing has been disabled in the config");
			return;
		}

		sender.sendMessage(ChatColor.GREEN + "Preparing data for import.");
		File bendingPlayersFile = new File(".", "converted.yml");
		FileConfiguration bendingPlayers = YamlConfiguration.loadConfiguration(bendingPlayersFile);

		final LinkedList<BendingPlayer> bPlayers = new LinkedList<BendingPlayer>();
		for (String string : bendingPlayers.getConfigurationSection("").getKeys(false)) {
			if (string.equalsIgnoreCase("version"))
				continue;
			String playername = string;
			UUID uuid = ProjectKorra.plugin.getServer().getOfflinePlayer(playername).getUniqueId();
			ArrayList<Element> element = new ArrayList<Element>();
			List<Integer> oe = bendingPlayers.getIntegerList(string + ".BendingTypes");
			HashMap<Integer, String> abilities = new HashMap<Integer, String>();
			List<Integer> oa = bendingPlayers.getIntegerList(string + ".SlotAbilities");
			boolean permaremoved = bendingPlayers.getBoolean(string + ".Permaremoved");

			int slot = 1;
			for (int i : oa) {
				if (StockAbility.getAbility(i) != null) {
					abilities.put(slot, StockAbility.getAbility(i).toString());
					slot++;
				} else {
					abilities.put(slot, null);
					slot++;
				}
			}

			for (int i : oe) {
				if (Element.getType(i) != null) {
					element.add(Element.getType(i));
				}
			}

			BendingPlayer bPlayer = new BendingPlayer(uuid, playername, element, abilities, permaremoved);
			bPlayers.add(bPlayer);
		}

		final int total = bPlayers.size();
		sender.sendMessage(ChatColor.GREEN + "Import of data started. Do NOT stop / reload your server.");
		if (debugEnabled) {
			sender.sendMessage(ChatColor.RED + "Console will print out all of the players that are imported if debug mode is enabled as they import.");
		}
		importTask = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(ProjectKorra.plugin, new Runnable() {
			public void run() {
				int i = 0;
				if (i >= 10) {
					sender.sendMessage(ChatColor.GREEN + "10 / " + total + " players converted thus far!");
					return;
				}

				while (i < 10) {
					if (bPlayers.isEmpty()) {
						sender.sendMessage(ChatColor.GREEN + "All data has been queued up, please allow up to 5 minutes for the data to complete, then reboot your server.");
						Bukkit.getServer().getScheduler().cancelTask(importTask.getTaskId());
						ProjectKorra.plugin.getConfig().set("Properties.ImportEnabled", false);
						ProjectKorra.plugin.saveConfig();
						for (Player player : Bukkit.getOnlinePlayers()) {
							GeneralMethods.createBendingPlayer(player.getUniqueId(), player.getName());
						}
						return;
					}
					StringBuilder elements = new StringBuilder();
					BendingPlayer bPlayer = bPlayers.pop();
					if (bPlayer.hasElement(Element.Air))
						elements.append("a");
					if (bPlayer.hasElement(Element.Water))
						elements.append("w");
					if (bPlayer.hasElement(Element.Earth))
						elements.append("e");
					if (bPlayer.hasElement(Element.Fire))
						elements.append("f");
					if (bPlayer.hasElement(Element.Chi))
						elements.append("c");

					HashMap<Integer, String> abilities = bPlayer.getAbilities();

					ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM pk_players WHERE uuid = '" + bPlayer.getUUIDString() + "'");

					try {
						if (rs2.next()) { // SQL Data already exists for player.
							DBConnection.sql.modifyQuery("UPDATE pk_players SET player = '" + bPlayer.getName() + "' WHERE uuid = '" + bPlayer.getUUIDString());
							DBConnection.sql.modifyQuery("UPDATE pk_players SET element = '" + elements + "' WHERE uuid = '" + bPlayer.getUUIDString());
							DBConnection.sql.modifyQuery("UPDATE pk_players SET permaremoved = '" + bPlayer.isPermaRemoved() + "' WHERE uuid = '" + bPlayer.getUUIDString());
							for (int slot = 1; slot < 10; slot++) {
								DBConnection.sql.modifyQuery("UPDATE pk_players SET slot" + slot + " = '" + abilities.get(slot) + "' WHERE player = '" + bPlayer.getName() + "'");
							}
						} else {
							DBConnection.sql.modifyQuery("INSERT INTO pk_players (uuid, player, element, permaremoved) VALUES ('" + bPlayer.getUUIDString() + "', '" + bPlayer.getName() + "', '" + elements + "', '" + bPlayer.isPermaRemoved() + "')");
							for (int slot = 1; slot < 10; slot++) {
								DBConnection.sql.modifyQuery("UPDATE pk_players SET slot" + slot + " = '" + abilities.get(slot) + "' WHERE player = '" + bPlayer.getName() + "'");
							}
						}
					}
					catch (SQLException ex) {
						ex.printStackTrace();
					}
					i++;
					if (debugEnabled) {
						System.out.println("[ProjectKorra] Successfully imported " + bPlayer.getName() + ". " + bPlayers.size() + " players left to import.");
					}
				}
			}
		}, 0, 40);
	}
}

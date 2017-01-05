package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.configuration.ConfigManager;
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
	private String disabled;
	private String preparingData;
	private String importStarted;
	private String debugWarning;
	private String queuedUp;

	public ImportCommand() {
		super("import", "/bending import", ConfigManager.languageConfig.get().getString("Commands.Import.Description"), new String[] { "import", "i" });
		
		this.disabled = ConfigManager.languageConfig.get().getString("Commands.Import.Description");
		this.preparingData = ConfigManager.languageConfig.get().getString("Commands.Import.PreparingData");
		this.importStarted = ConfigManager.languageConfig.get().getString("Commands.Import.ImportStarted");
		this.debugWarning = ConfigManager.languageConfig.get().getString("Commands.Import.DebugWarning");
		this.queuedUp = ConfigManager.languageConfig.get().getString("Commands.Import.DataQueuedUp");
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if (!hasPermission(sender) || !correctLength(sender, args.size(), 0, 0)) {
			return;
		} else if (!GeneralMethods.isImportEnabled()) {
			sender.sendMessage(ChatColor.RED + this.disabled);
			return;
		}

		sender.sendMessage(ChatColor.GREEN + this.preparingData);
		File bendingPlayersFile = new File(".", "converted.yml");
		FileConfiguration bendingPlayers = YamlConfiguration.loadConfiguration(bendingPlayersFile);

		final LinkedList<BendingPlayer> bPlayers = new LinkedList<BendingPlayer>();
		for (String string : bendingPlayers.getConfigurationSection("").getKeys(false)) {
			if (string.equalsIgnoreCase("version"))
				continue;
			String playername = string;
			@SuppressWarnings("deprecation")
			UUID uuid = ProjectKorra.plugin.getServer().getOfflinePlayer(playername).getUniqueId();
			ArrayList<Element> elements = new ArrayList<Element>();
			ArrayList<SubElement> subs = new ArrayList<SubElement>();
			List<Integer> bendingTypes = bendingPlayers.getIntegerList(string + ".BendingTypes");
			boolean permaremoved = bendingPlayers.getBoolean(string + ".Permaremoved");
			Element[] mainElements = Element.getMainElements();
			Element[] allElements = Element.getAllElements();
			
			for (int i : bendingTypes) {
				if (i < mainElements.length) {
					elements.add(mainElements[i]);
				}
			}
			
			for (Element e : allElements) {
				if (e instanceof SubElement) {
					SubElement s = (SubElement) e;
					subs.add(s);
				}
			}

			BendingPlayer bPlayer = new BendingPlayer(uuid, playername, elements, subs, new HashMap<Integer, String>(), permaremoved);
			bPlayers.add(bPlayer);
		}

		final CommandSender s = sender;
		final int total = bPlayers.size();
		sender.sendMessage(ChatColor.GREEN + this.importStarted);
		if (debugEnabled) {
			sender.sendMessage(ChatColor.RED + this.debugWarning);
		}
		importTask = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(ProjectKorra.plugin, new Runnable() {
			public void run() {
				int i = 0;
				if (i >= 10) {
					s.sendMessage(ChatColor.GREEN + "10 / " + total + "!");
					return;
				}

				while (i < 10) {
					if (bPlayers.isEmpty()) {
						s.sendMessage(ChatColor.GREEN + queuedUp);
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
					if (bPlayer.hasElement(Element.AIR))
						elements.append("a");
					if (bPlayer.hasElement(Element.WATER))
						elements.append("w");
					if (bPlayer.hasElement(Element.EARTH))
						elements.append("e");
					if (bPlayer.hasElement(Element.FIRE))
						elements.append("f");
					if (bPlayer.hasElement(Element.CHI))
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
						System.out.println("[ProjectKorra] Successfully imported " + bPlayer.getName() + ". " + bPlayers.size() + " players left to import."); // not configurable because it's internal
					}
				}
			}
		}, 0, 40);
	}
}

package com.projectkorra.projectkorra.object;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.storage.DBConnection;

public class Preset2 {
	private Player player;
	private String name;

	public Preset2(Player player, String name) {
		this.player = player;
		this.name = name;

		if (player != null && name != null) {
			if (ProjectKorra.plugin.getConfig().getString("Storage.engine").equalsIgnoreCase("mysql")) {
				DBConnection.sql.modifyQuery("INSERT INTO pk_presets (uuid, name) VALUES ('" + player.getUniqueId()
						+ "', '" + name + "') " + "ON DUPLICATE KEY UPDATE name=VALUES(name)");
			} else {
				DBConnection.sql.modifyQuery("INSERT OR REPLACE INTO pk_presets (uuid, name) VALUES ('"
						+ player.getUniqueId() + "', '" + name + "')");
			}
			for (int s = 0; 9 >= s; s++) {
				add(s);
			}
		}

	}

	public Player getPlayer() {
		return player;
	}

	public String getName() {
		return name;
	}

	public void load() {
		if (player != null && name != null) {
			ResultSet rs = DBConnection.sql
					.readQuery("SELECT * FROM pk_presets WHERE player = '" + player.getName() + "'");
			BendingPlayer bplayer = GeneralMethods.getBendingPlayer(player.getName());
			for (int n = 0; 9 >= n; n++) {
				HashMap<Integer, String> abilities = new HashMap<>();
				try {
					abilities.put(n, rs.getString("slot" + n));
				} catch (SQLException e) {
					e.printStackTrace();
				}

				bplayer.setAbilities(abilities);
			}
		}
		return;
	}

	public void add(int s) {
		DBConnection.sql.modifyQuery("UPDATE pk_presets SET slot" + s + " = '"
				+ GeneralMethods.getBendingPlayer(player).getAbilities().get(s) + "' WHERE uuid = '"
				+ player.getUniqueId() + "' AND name = '" + name + "'");
	}

	public void remove() {
		DBConnection.sql.modifyQuery(
				"DELETE FROM pk_presets WHERE uuid = '" + player.getUniqueId() + "' AND name = '" + name + "'");
	}
}

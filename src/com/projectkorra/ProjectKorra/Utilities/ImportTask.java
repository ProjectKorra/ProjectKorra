package com.projectkorra.ProjectKorra.Utilities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import com.projectkorra.ProjectKorra.DBConnection;

public class ImportTask implements Runnable {
	
	private String uuid;
	private String playername;
	private boolean permaremoved;
	private StringBuilder elements;
	private HashMap<Integer, String> abilities;
	
	public ImportTask(String uuid, String playername, boolean permaremoved, StringBuilder elements, HashMap<Integer, String> abilities) {
		this.uuid = uuid;
		this.playername = playername;
		this.permaremoved = permaremoved;
		this.elements = elements;
		this.abilities = abilities;
	}

	@Override
	public void run() {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM pk_players WHERE uuid = '" + uuid + "'");
		
		try {
			if (rs2.next()) { // SQL Data already exists for player.
				DBConnection.sql.modifyQuery("UPDATE pk_players SET player = '" + playername + "' WHERE uuid = '" + uuid + "'");
				DBConnection.sql.modifyQuery("UPDATE pk_players SET element = '" + elements + "' WHERE uuid = '" + uuid + "'");
				DBConnection.sql.modifyQuery("UPDATE pk_players SET permaremoved = '" + permaremoved + "' WHERE uuid = '" + uuid + "'");
				for (int slot = 1; slot < 10; slot++) {
					DBConnection.sql.modifyQuery("UPDATE pk_players SET slot" + slot + " = '" + abilities.get(slot) + "' WHERE uuid = '" + uuid + "'");
				}
			} else {
				DBConnection.sql.modifyQuery("INSERT INTO pk_players (uuid, player, element, permaremoved) VALUES ('" + uuid + "', '" + playername + "', '" + elements + "', '" + permaremoved +"')");
				for (int slot = 1; slot < 10; slot++) {
					DBConnection.sql.modifyQuery("UPDATE pk_players SET slot" + slot + " = '" + abilities.get(slot) + "' WHERE uuid = '" + uuid + "'");
				}
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

}

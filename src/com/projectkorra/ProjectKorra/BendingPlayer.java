package com.projectkorra.ProjectKorra;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BendingPlayer {

	public static ConcurrentHashMap<String, BendingPlayer> players = new ConcurrentHashMap<String, BendingPlayer>();
	//	public static ConcurrentHashMap<String, Long> blockedChi = new ConcurrentHashMap<String, Long>();

	UUID uuid;
	String player;
	ArrayList<Element> elements;
	private HashMap<Integer, String> abilities;
	ConcurrentHashMap<String, Long> cooldowns;
	boolean permaRemoved;
	boolean isToggled;
	private long slowTime = 0;
	private boolean tremorsense = true;
	boolean blockedChi;

	public BendingPlayer(UUID uuid, String player, ArrayList<Element> elements, HashMap<Integer, String> abilities, boolean permaRemoved) {
		this.uuid = uuid;
		this.player = player;
		this.elements = elements;
		this.setAbilities(abilities);
		cooldowns = new ConcurrentHashMap<String, Long>();
		this.permaRemoved = permaRemoved;
		isToggled = true;
		blockedChi = false;

		players.put(player, this);
	}

	public boolean isOnCooldown(String ability) {
		return this.cooldowns.containsKey(ability);
	}

	public void addCooldown(String ability, long cooldown) {
		this.cooldowns.put(ability, cooldown + System.currentTimeMillis());
	}

	public void removeCooldown(String ability) {
		this.cooldowns.remove(ability);
	}

	public UUID getUUID() {
		return this.uuid;
	}

	public String getPlayerName() {
		return this.player;
	}

	public List<Element> getElements() {
		return this.elements;
	}

	public HashMap<Integer, String> getAbilities() {
		return this.abilities;
	}

	public boolean isPermaRemoved() {
		return this.permaRemoved;
	}

	public void addElement(Element e) {
		this.elements.add(e);
	}

	public boolean hasElement(Element e) {
		return this.elements.contains(e);
	}

	public void setElement(Element e) {
		this.elements.clear();
		this.elements.add(e);
	} 

	public boolean canBeSlowed() {
		return (System.currentTimeMillis() > slowTime);
	}

	public void slow(long cooldown) {
		slowTime = System.currentTimeMillis() + cooldown;
	}

	public void toggleTremorsense() {
		tremorsense = !tremorsense;
	}

	public boolean isTremorsensing() {
		return tremorsense;
	}

	public void blockChi() {
		blockedChi = true;
	}

	public void unblockChi() {
		blockedChi = false;
	}

	public boolean isChiBlocked() {
		return blockedChi;
	}
	
	public static List<BendingPlayer> getBendingPlayers() {
		List<BendingPlayer> bPlayers = new ArrayList<BendingPlayer>(players.values());
		return bPlayers;
	}
	
	public static boolean dataExists(UUID uuid) {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM pk_players WHERE uuid = '" + uuid.toString() + "'");
		try {
			if (rs2.next()) return true;
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void delete() {
		DBConnection.sql.modifyQuery("DELETE FROM pk_players WHERE uuid = '" + uuid.toString() + "'");
		players.remove(player);
		ProjectKorra.log.info("Deleted data for " + this.player);
	}

	public void setAbilities(HashMap<Integer, String> abilities) {
		this.abilities = abilities;
		for (int i = 1; i <= 9; i++) {
			DBConnection.sql.modifyQuery("UPDATE pk_players SET slot" + i + " = '" + (abilities.get(i) == null ? null: abilities.get(i)) + "' WHERE uuid = '" + uuid + "'");
		}
	}
}

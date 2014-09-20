package com.projectkorra.ProjectKorra;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
	long lastTime = 0;

	public BendingPlayer(UUID uuid, String player, ArrayList<Element> elements, HashMap<Integer, String> abilities, boolean permaRemoved) {
		this.uuid = uuid;
		this.player = player;
		this.elements = elements;
		this.setAbilities(abilities);
		cooldowns = new ConcurrentHashMap<String, Long>();
		this.permaRemoved = permaRemoved;
		isToggled = true;
		blockedChi = false;
		lastTime = System.currentTimeMillis();

		players.put(player, this);
	}
	
	public BendingPlayer(Player player) {
		this.uuid = player.getUniqueId();
		this.player = player.getName();
		this.elements = new ArrayList<Element>();
		this.setAbilities(new HashMap<Integer, String>());
		cooldowns = new ConcurrentHashMap<String, Long>();
		this.permaRemoved = false;
		isToggled = true;
		blockedChi = false;
		lastTime = System.currentTimeMillis();
		
		players.put(player.getName(), this);
	}
	public BendingPlayer(UUID uuid) {
		String playername = Bukkit.getOfflinePlayer(uuid).getName();
		
		players.put(playername, this);
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

	public void setAbilities(HashMap<Integer, String> abilities) {
		this.abilities = abilities;
		for (int i = 1; i <= 9; i++) {
			DBConnection.sql.modifyQuery("UPDATE pk_players SET slot" + i + " = '" + (abilities.get(i) == null ? null: abilities.get(i)) + "' WHERE uuid = '" + uuid + "'");
		}
	}
	
	public static BendingPlayer getBendingPlayer(Player player) {
		if (players.containsKey(player.getName())) {
			return players.get(player.getName());
		}
		
		BendingPlayer bPlayer = getPlayer(player.getUniqueId());
		if (bPlayer != null) {
			players.put(player.getName(), bPlayer);
			return bPlayer;
		} else {
			return new BendingPlayer(player);
		}
	}
	
	public static BendingPlayer getPlayer(UUID uuid) {
		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM pk_players WHERE uuid = '" + uuid.toString() + "'");
		try {
			if (!rs2.next()) {
				return null;
			} else {
				String name = rs2.getString("player");
				String element = rs2.getString("element");
				String permaremoved = rs2.getString("permaremoved");
				boolean p = false;
				ArrayList<Element> elements = new ArrayList<Element>();
				if (element != null) { // Player has an element.
					if (element.contains("a")) elements.add(Element.Air);
					if (element.contains("w")) elements.add(Element.Water);
					if (element.contains("e")) elements.add(Element.Earth);
					if (element.contains("f")) elements.add(Element.Fire);
					if (element.contains("c")) elements.add(Element.Chi);
				}

				HashMap<Integer, String> abilities = new HashMap<Integer, String>();
				for (int i = 1; i <= 9; i++) {
					String slot = rs2.getString("slot" + i);
					if (slot != null) abilities.put(i, slot);
				}

				if (permaremoved == null) {
					p = false;
				}
				else if (permaremoved.equals("true")) {
					p = true;
				}
				else if (permaremoved.equals("false")) {
					p = false;
				}

				return new BendingPlayer(uuid, name, elements, abilities, p);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}

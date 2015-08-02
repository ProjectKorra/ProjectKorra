package com.projectkorra.ProjectKorra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;

import com.projectkorra.ProjectKorra.CustomEvents.PlayerCooldownChangeEvent;
import com.projectkorra.ProjectKorra.CustomEvents.PlayerCooldownChangeEvent.Result;

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
		this.permaRemoved = permaRemoved;
		cooldowns = new ConcurrentHashMap<String, Long>();
		isToggled = true;
		blockedChi = false;

		players.put(player, this);
		PKListener.login(this);
	}

	public void addCooldown(String ability, long cooldown) {
		PlayerCooldownChangeEvent event = new PlayerCooldownChangeEvent(Bukkit.getPlayer(uuid), ability, Result.ADDED);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (!event.isCancelled()) {
			this.cooldowns.put(ability, cooldown + System.currentTimeMillis());
		}
	}

	public void addElement(Element e) {
		this.elements.add(e);
	}

	public void blockChi() {
		blockedChi = true;
	}

	public boolean canBeSlowed() {
		return (System.currentTimeMillis() > slowTime);
	}

	public HashMap<Integer, String> getAbilities() {
		return this.abilities;
	}

	public List<Element> getElements() {
		return this.elements;
	}

	public String getPlayerName() {
		return this.player;
	}

	public UUID getUUID() {
		return this.uuid;
	}

	public boolean hasElement(Element e) {
		return this.elements.contains(e);
	}

	public boolean isChiBlocked() {
		return this.blockedChi;
	}

	public boolean isOnCooldown(String ability) {
		return this.cooldowns.containsKey(ability);
	}

	public boolean isPermaRemoved() {
		return this.permaRemoved;
	}

	public boolean isToggled() {
		return this.isToggled;
	}

	public boolean isTremorsensing() {
		return this.tremorsense;
	}

	public void removeCooldown(String ability) {
		PlayerCooldownChangeEvent event = new PlayerCooldownChangeEvent(Bukkit.getPlayer(uuid), ability, Result.REMOVED);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (!event.isCancelled()) {
			this.cooldowns.remove(ability);
		}
	}

	public void setAbilities(HashMap<Integer, String> abilities) {
		this.abilities = abilities;
		for (int i = 1; i <= 9; i++) {
			DBConnection.sql.modifyQuery("UPDATE pk_players SET slot" + i + " = '" + (abilities.get(i) == null ? null : abilities.get(i)) + "' WHERE uuid = '" + uuid + "'");
		}
	}

	public void setElement(Element e) {
		this.elements.clear();
		this.elements.add(e);
	}

	public void slow(long cooldown) {
		slowTime = System.currentTimeMillis() + cooldown;
	}

	public void toggleTremorsense() {
		tremorsense = !tremorsense;
	}

	public void unblockChi() {
		blockedChi = false;
	}
}

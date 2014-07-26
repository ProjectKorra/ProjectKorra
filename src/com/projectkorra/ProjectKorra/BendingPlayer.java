package com.projectkorra.ProjectKorra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BendingPlayer {

	public static ConcurrentHashMap<String, BendingPlayer> players = new ConcurrentHashMap<String, BendingPlayer>();
	public static ConcurrentHashMap<String, Long> blockedChi = new ConcurrentHashMap<String, Long>();

	UUID uuid;
	String player;
	ArrayList<Element> elements;
	HashMap<Integer, String> abilities;
	boolean isChiBlocked;
	boolean isEnergybent;
	boolean permaRemoved;
	boolean isToggled;
	private long slowTime = 0;
	private boolean tremorsense = true;

	public BendingPlayer(UUID uuid, String player, ArrayList<Element> elements, HashMap<Integer, String> abilities, boolean permaRemoved) {
		this.uuid = uuid;
		this.player = player;
		this.elements = elements;
		this.abilities = abilities;
		this.permaRemoved = permaRemoved;
		isToggled = true;
		isChiBlocked = false;
		isEnergybent = false;

		players.put(player, this);
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
		isChiBlocked = true;
	}

	public void unblockChi() {
		isChiBlocked = false;
	}

	public boolean isChiBlocked() {
		return isChiBlocked;
	}
	
	public void EnergybendBPlayer() {
		isEnergybent = true;
	}
	
	public void RestoreBendingBPlayer() {
		isEnergybent = false;
	}
	
	public boolean isEnergybent() {
		return isEnergybent;
	}
}

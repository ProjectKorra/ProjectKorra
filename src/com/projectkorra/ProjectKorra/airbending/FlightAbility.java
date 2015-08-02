package com.projectkorra.ProjectKorra.airbending;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Flight;

public class FlightAbility {

	public static ConcurrentHashMap<String, FlightAbility> instances = new ConcurrentHashMap<String, FlightAbility>();
	private static ConcurrentHashMap<String, Integer> hits = new ConcurrentHashMap<String, Integer>();
	private static ConcurrentHashMap<String, Boolean> hovering = new ConcurrentHashMap<String, Boolean>();
	private Player p;
	private Flight flight;

	public FlightAbility(Player player) {
		if (!AirMethods.canFly(player, true, false)) return;
		player.setAllowFlight(true);
		player.setVelocity(player.getEyeLocation().getDirection().normalize());
		instances.put(player.getName(), this);
		p = player;
	}

	private void progress() {
		if (!AirMethods.canFly(p, false, isHovering(p))) {
			remove(p);
			return;
		}

		if (flight == null) flight = new Flight(p);

		if (isHovering(p)) {
			Vector vec = p.getVelocity().clone();
			vec.setY(0);
			p.setVelocity(vec);
		} else {
			p.setVelocity(p.getEyeLocation().getDirection().normalize());
		}

	}

	public static void addHit(Player player) {
		if (instances.containsKey(player)) {
			if (hits.containsKey(player.getName())) {
				if (hits.get(player.getName()) >= 4) {
					hits.remove(player.getName());
					remove(player);
				}
			} else {
				hits.put(player.getName(), 1);
			}
		}
	}

	public static boolean isHovering(Player player) {
		return hovering.containsKey(player.getName());
	}

	public static void setHovering(Player player, boolean bool) {
		String playername = player.getName();

		if (bool) {
			if (!hovering.containsKey(playername)) {
				hovering.put(playername, true);
				player.setVelocity(new Vector(0, 0, 0));
			}
		} else {
			if (hovering.containsKey(playername)) {
				hovering.remove(playername);
			}
		}
	}

	public static void progressAll() {
		for (String names : instances.keySet()) {
			instances.get(names).progress();
		}
	}

	public void remove() {
		String name = p.getName();
		instances.remove(name);
		hits.remove(name);
		hovering.remove(name);
		if (flight != null) flight.revert();
	}

	public static void remove(Player player) {
		if (instances.containsKey(player.getName())) instances.get(player.getName()).remove();
	}

	public static void removeAll() {
		Iterator<String> it = instances.keySet().iterator();
		while (it.hasNext()) {
			instances.get(it.next()).remove();
		}
		instances.clear();
		hits.clear();
		hovering.clear();
	}

}
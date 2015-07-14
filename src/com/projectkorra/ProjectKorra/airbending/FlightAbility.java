package com.projectkorra.ProjectKorra.airbending;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Flight;
import com.projectkorra.ProjectKorra.Ability.Ability;
import com.projectkorra.ProjectKorra.Ability.StockAbilities;

public class FlightAbility extends Ability {
	
	private static ConcurrentHashMap<String, Integer> hits = new ConcurrentHashMap<String, Integer>();
	private static ConcurrentHashMap<String, Boolean> hovering = new ConcurrentHashMap<String, Boolean>();
	private Player player;
	private UUID uuid;
	private Flight flight;
	
	public FlightAbility(Player player) {		
		if (!AirMethods.canFly(player, true, false)) 
			return;
		player.setAllowFlight(true);
		player.setVelocity(player.getEyeLocation().getDirection().normalize());
		this.player = player;
		this.uuid = player.getUniqueId();
		//instances.put(player.getUniqueId(), this);
		putInstance(StockAbilities.Flight, uuid, this);
	}
	
	public static void addHit(Player player) {
		if (contains(player)) {
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
	
	public static boolean contains(Player player) {
		return getInstance(StockAbilities.Flight).containsKey(player.getUniqueId());
	}
	
	public static boolean isHovering(Player player) {
		return hovering.containsKey(player.getName());
	}

	public static void remove(Player player) {
		if (contains(player))
			((FlightAbility) getInstance(StockAbilities.Flight).get(player.getUniqueId())).remove();
	}
	
	public static void removeAll() {
		Ability.removeAll(StockAbilities.Flight);
		hits.clear();
		hovering.clear();
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
	
	@Override
	public void progress() {
		if (!AirMethods.canFly(player, false, isHovering(player))) {
			remove(player);
			return;
		}
		
		if (flight == null)
			flight = new Flight(player);
		
		if (isHovering(player)) {
			Vector vec = player.getVelocity().clone();
			vec.setY(0);
			player.setVelocity(vec);
		} else {
			player.setVelocity(player.getEyeLocation().getDirection().normalize());
		}
		
	}
	
	@Override
	public void reloadVariables() {}

	@Override
	public void remove() {
		String name = player.getName();
		//instances.remove(uuid);
		removeInstance(StockAbilities.Flight, uuid);
		hits.remove(name);
		hovering.remove(name);
		if (flight != null) flight.revert();
	}
	
}
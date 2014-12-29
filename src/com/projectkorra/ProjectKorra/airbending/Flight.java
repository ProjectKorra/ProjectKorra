package com.projectkorra.ProjectKorra.airbending;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Methods;

public class Flight {
	
	public static ConcurrentHashMap<String, Flight> instances = new ConcurrentHashMap<String, Flight>();
	private static ConcurrentHashMap<String, Integer> hits = new ConcurrentHashMap<String, Integer>();
	private static ConcurrentHashMap<String, Boolean> hovering = new ConcurrentHashMap<String, Boolean>();
	private Player p;
	
	
	public Flight(Player player) {		
		if(!Methods.canFly(player, true, false)) return;
		
		player.setAllowFlight(true);
		
		player.setVelocity(player.getEyeLocation().getDirection().normalize());
		
		instances.put(player.getName(), this);
		p = player;
	}
	
	private void progress() {
		if(!Methods.canFly(p, false, isHovering(p))) {
			remove(p);
			return;
		}
		
		p.setAllowFlight(true);
		
		p.setVelocity(p.getEyeLocation().getDirection().normalize());
	}
	
	public static void addHit(Player player) {
		if(instances.containsKey(player)) {
			if(hits.containsKey(player.getName())) {
				if(hits.get(player.getName()) >= 4) {
					hits.remove(player.getName());
					remove(player);
				}
			}else{
				hits.put(player.getName(), 1);
			}
		}
	}

	public static boolean isHovering(Player player) {
		String playername = player.getName();
		
		if(hovering.containsKey(playername) && hovering.get(playername)) return true;
		if(hovering.containsKey(playername) && hovering.get(playername)) return false; //Shouldn't happen
		return false;
	}
	
	public static void setHovering(Player player, boolean bool) {
		String playername = player.getName();
		
		if(bool) {
			if(!hovering.containsKey(playername)) {
				hovering.put(playername, true);
				player.setFlying(true);
				player.setVelocity(new Vector(0, 0 ,0));
			}
		}else{
			if(hovering.containsKey(playername)) {
				hovering.remove(playername);
				player.setFlying(false);
			}
		}
	}
	
	public static void progressAll() {
		for(String names : instances.keySet()) {
			instances.get(names).progress();
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void remove(Player player) {
		if(instances.containsKey(player.getName())) {
			instances.remove(player.getName());
			if(hits.containsKey(player.getName())) {
				hits.remove(player.getName());
			}
			if(hovering.containsKey(player.getName())) {
				hovering.remove(player.getName());
			}
			if((!(player.getGameMode().getValue() == 1))) {
				if(!(player.getGameMode().getValue() == 1)) {
					player.setAllowFlight(false);
				}
			}
			if((!(player.getGameMode().getValue() == 3))) {
				if(!(player.getGameMode().getValue() == 1)) {
					player.setAllowFlight(false);
				}
			}
		}
	}
	
	public static void removeAll() {
		instances.clear();
		hits.clear();
		hovering.clear();
	}
	
}
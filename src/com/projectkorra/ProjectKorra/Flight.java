package com.projectkorra.ProjectKorra;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.airbending.AirScooter;
import com.projectkorra.ProjectKorra.airbending.AirSpout;
import com.projectkorra.ProjectKorra.airbending.SpiritualProjection;
import com.projectkorra.ProjectKorra.airbending.Tornado;
import com.projectkorra.ProjectKorra.earthbending.Catapult;
import com.projectkorra.ProjectKorra.earthbending.SandSpout;
import com.projectkorra.ProjectKorra.firebending.FireJet;
import com.projectkorra.ProjectKorra.waterbending.Bloodbending;
import com.projectkorra.ProjectKorra.waterbending.WaterSpout;

public class Flight {

	private static ConcurrentHashMap<Player, Flight> instances = new ConcurrentHashMap<Player, Flight>();
	private static long duration = 5000;
	
	private Player player = null, source = null;

	private boolean couldFly = false, wasFlying = false;

	private long time;

	public Flight(Player player) {
		this(player, null);
	}

	public Flight(Player player, Player source) {
		if (instances.containsKey(player)) {
			Flight flight = instances.get(player);
			flight.refresh(source);
			instances.replace(player, flight);
			return;
		}
		couldFly = player.getAllowFlight();
		wasFlying = player.isFlying();
		this.player = player;
		this.source = source;
		time = System.currentTimeMillis();
		instances.put(player, this);
	}
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Flight )) {
			return false;
		}
		Flight flight = (Flight) object;
		return flight.player == this.player && flight.source == this.source && 
				flight.couldFly == this.couldFly && flight.wasFlying == this.wasFlying;
	}
	
	public static Player getLaunchedBy(Player player) {
		if (instances.containsKey(player)) {
			return instances.get(player).source;
		}

		return null;
	}
	
	public static void handle() {
		ArrayList<Player> players = new ArrayList<Player>();
		ArrayList<Player> newflyingplayers = new ArrayList<Player>();
		ArrayList<Player> avatarstateplayers = new ArrayList<Player>();
		ArrayList<Player> airscooterplayers = new ArrayList<Player>();
		ArrayList<Player> waterspoutplayers = new ArrayList<Player>();
		ArrayList<Player> airspoutplayers = new ArrayList<Player>();
		ArrayList<Player> sandspoutplayers = new ArrayList<Player>();
		ArrayList<Player> spiritualprojectionplayers = new ArrayList<Player>();

		players.addAll(Tornado.getPlayers());
//		players.addAll(Speed.getPlayers());
		players.addAll(FireJet.getPlayers());
		players.addAll(Catapult.getPlayers());
		avatarstateplayers = AvatarState.getPlayers();
		airscooterplayers = AirScooter.getPlayers();
		waterspoutplayers = WaterSpout.getPlayers();
		airspoutplayers = AirSpout.getPlayers();
		sandspoutplayers = SandSpout.getPlayers();
		spiritualprojectionplayers = SpiritualProjection.getPlayers();

		for (Player player : instances.keySet()) {
			Flight flight = instances.get(player);
			if (avatarstateplayers.contains(player)
					|| airscooterplayers.contains(player)
					|| waterspoutplayers.contains(player)
					|| airspoutplayers.contains(player) 
					|| sandspoutplayers.contains(player)
					|| spiritualprojectionplayers.contains(player)) {
				continue;
			}
			if (Bloodbending.isBloodbended(player)) {
				player.setAllowFlight(true);
				player.setFlying(false);
				continue;
			}

			if (players.contains(player)) {
				flight.refresh(null);
				player.setAllowFlight(true);
				if (player.getGameMode() != GameMode.CREATIVE)
					player.setFlying(false);
				newflyingplayers.add(player);
				continue;
			}

			if (flight.source == null) {
				flight.revert();
				flight.remove();
			} else {
				if (System.currentTimeMillis() > flight.time + duration) {
					flight.remove();
				}
			}
		}
	}
	public static void removeAll() {
		for (Player player : instances.keySet()) {
			Flight flight = instances.get(player);
			if (player == null || flight == null) {
				instances.remove(player);
				continue;
			}
			//if (flight.source != null)
			flight.revert();
			flight.remove();
		}
	}

	private void refresh(Player source) {
		this.source = source;
		time = System.currentTimeMillis();
		instances.replace(player, this);
	}

	public void remove() {
		if (player == null) {
			for (Player player : instances.keySet()) {
				if (instances.get(player).equals(this)) {
					instances.remove(player);
				}
			}
			return;
		}
		instances.remove(player);
	}

	public void revert() {
		if (player == null) {
			return;
		}
		player.setAllowFlight(couldFly);
		player.setFlying(wasFlying);
	}

}
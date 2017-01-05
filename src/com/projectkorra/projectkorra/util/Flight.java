package com.projectkorra.projectkorra.util;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.airbending.AirScooter;
import com.projectkorra.projectkorra.airbending.AirSpout;
import com.projectkorra.projectkorra.airbending.Tornado;
import com.projectkorra.projectkorra.earthbending.Catapult;
import com.projectkorra.projectkorra.earthbending.sand.SandSpout;
import com.projectkorra.projectkorra.firebending.FireJet;
import com.projectkorra.projectkorra.waterbending.WaterSpout;
import com.projectkorra.projectkorra.waterbending.blood.Bloodbending;

public class Flight {

	private static Map<Player, Flight> instances = new ConcurrentHashMap<Player, Flight>();
	private static long duration = 5000;

	private Player player;
	private Player source;
	private boolean couldFly = false;
	private boolean wasFlying = false;
	private long time;

	public Flight(Player player) {
		this(player, null);
	}

	public Flight(Player player, Player source) {
		if (instances.containsKey(player)) {
			Flight flight = instances.get(player);
			flight.refresh(source);
			return;
		}
		this.couldFly = player.getAllowFlight();
		this.wasFlying = player.isFlying();
		this.player = player;
		this.source = source;
		this.time = System.currentTimeMillis();
		instances.put(player, this);
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Flight)) {
			return false;
		}
		Flight flight = (Flight) object;
		return flight.player == this.player && flight.source == this.source && flight.couldFly == this.couldFly && flight.wasFlying == this.wasFlying;
	}

	public static Player getLaunchedBy(Player player) {
		if (instances.containsKey(player)) {
			return instances.get(player).source;
		}
		return null;
	}

	public static void handle() {
		ArrayList<Player> players = new ArrayList<>();
		ArrayList<Player> newFlyingPlayers = new ArrayList<Player>();
		Set<Player> airScooterPlayers = CoreAbility.getPlayers(AirScooter.class);
		Set<Player> waterSpoutPlayers = CoreAbility.getPlayers(WaterSpout.class);
		Set<Player> airSpoutPlayers = CoreAbility.getPlayers(AirSpout.class);
		Set<Player> sandSpoutPlayers = CoreAbility.getPlayers(SandSpout.class);

		players.addAll(CoreAbility.getPlayers(Tornado.class));
		players.addAll(CoreAbility.getPlayers(FireJet.class));
		players.addAll(CoreAbility.getPlayers(Catapult.class));

		for (Player player : instances.keySet()) {
			Flight flight = instances.get(player);
			if (System.currentTimeMillis() <= flight.time + duration) {
				if (airScooterPlayers.contains(player) || waterSpoutPlayers.contains(player) || airSpoutPlayers.contains(player) || sandSpoutPlayers.contains(player)) {
					continue;
				}
				if (Bloodbending.isBloodbent(player)) {
					player.setAllowFlight(true);
					player.setFlying(false);
					continue;
				}

				if (players.contains(player)) {
					flight.refresh(null);
					player.setAllowFlight(true);
					if (player.getGameMode() != GameMode.CREATIVE)
						player.setFlying(false);
					newFlyingPlayers.add(player);
					continue;
				}
				if (flight.source == null) {
					flight.revert();
					flight.remove();
				} else {
					if (System.currentTimeMillis() >= flight.time + duration) {
						flight.revert();
						flight.remove();
					}
				}
			} else {
				flight.revert();
				flight.remove();
			}
		}
	}

	public static void removeAll() {
		for (Player player : instances.keySet()) {
			Flight flight = instances.get(player);
			if (flight == null) {
				instances.remove(player);
				continue;
			}
			flight.revert();
			flight.remove();
		}
	}

	private void refresh(Player source) {
		this.source = source;
		time = System.currentTimeMillis();
		instances.put(player, this);
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

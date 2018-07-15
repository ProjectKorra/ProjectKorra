package com.projectkorra.projectkorra.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.projectkorra.ProjectKorra;

public class FlightHandler {

	/**
	 * A Map containing all Flight instances.
	 */
	private final Map<UUID, Flight> INSTANCES = new HashMap<>();
	/**
	 * A PriorityQueue containing all Flight instances which have a specified
	 * duration. This is used to reduce the number of iterations when cleaning
	 * up dead instances.
	 */
	private final PriorityQueue<FlightAbility> CLEANUP = new PriorityQueue<>(100, new Comparator<FlightAbility>() {
		@Override
		public int compare(FlightAbility f1, FlightAbility f2) {
			return (int) (f1.duration - f2.duration);
		}
	});

	public FlightHandler() {
		startCleanup();
	}

	/**
	 * Create a new Flight instance for the provided player with an unlimited
	 * duration. Call {@link FlightHandler#removeInstance(Player)} to remove
	 * this instance when necessary.
	 * 
	 * @param player The flying player
	 * @param identifier The ability using Flight
	 */
	public void createInstance(Player player, String identifier) {
		createInstance(player, Flight.PERMANENT, identifier);
	}

	/**
	 * Create a new Flight instance for the provided player with an unlimited
	 * duration. This method will set the source for this Flight instance to the
	 * second provided player argument. Call
	 * {@link FlightHandler#removeInstance(Player)} to remove this instance when
	 * necessary.
	 * 
	 * @param player The flying player
	 * @param source The source player
	 * @param identifier The ability using Flight
	 */
	public void createInstance(Player player, Player source, String identifier) {
		createInstance(player, source, Flight.PERMANENT, identifier);
	}

	/**
	 * Create a new Flight instance with the specified duration. This instance
	 * will automatically be removed with the set delay.
	 * 
	 * @param player The flying player
	 * @param duration Flight duration
	 * @param identifier The ability using Flight
	 */
	public void createInstance(Player player, long duration, String identifier) {
		createInstance(player, null, duration, identifier);
	}

	/**
	 * Create a new Flight instance with the specified duration. This method
	 * will set the source for this Flight instance to the second provided
	 * player argument. This instance will automatically be removed with the set
	 * delay.
	 * 
	 * @param player The flying player
	 * @param source The source player
	 * @param duration Flight duration
	 * @param identifier The ability using Flight
	 */
	public void createInstance(Player player, Player source, long duration, String identifier) {
		if (INSTANCES.containsKey(player.getUniqueId())) {
			Flight flight = INSTANCES.get(player.getUniqueId());
			FlightAbility ability = new FlightAbility(player, identifier, duration);
			if (duration != Flight.PERMANENT) {
				CLEANUP.add(ability);
			}
			flight.abilities.put(identifier, ability);
		}
		Flight flight = new Flight(player, source);
		FlightAbility ability = new FlightAbility(player, identifier, duration);
		if (duration != Flight.PERMANENT) {
			CLEANUP.add(ability);
		}
		flight.abilities.put(identifier, ability);
		INSTANCES.put(player.getUniqueId(), flight);
	}

	/**
	 * Remove a player's Flight status with the provided identifier. If this is
	 * the last ability using Flight, then their Flight instance shall be
	 * reverted to its initial state. This method does not need to be called for
	 * instances with a defined duration, however can be used in this case if
	 * necessary.
	 * 
	 * @param player The flying player
	 * @param identifier The ability using Flight
	 */
	public void removeInstance(Player player, String identifier) {
		if (INSTANCES.containsKey(player.getUniqueId())) {
			Flight flight = INSTANCES.get(player.getUniqueId());
			if (flight.abilities.containsKey(identifier)) {
				flight.abilities.remove(identifier);
			}
			if (flight.abilities.isEmpty()) {
				wipeInstance(player);
			}
		}
	}

	/**
	 * Completely wipe all Flight data for the player. Should only be used if it
	 * is guaranteed they have a Flight instance.
	 * 
	 * @param player
	 */
	private void wipeInstance(Player player) {
		Flight flight = INSTANCES.get(player.getUniqueId());
		player.setAllowFlight(flight.couldFly);
		player.setFlying(flight.wasFlying);
		flight.abilities.values().forEach(ability -> CLEANUP.remove(ability));
		INSTANCES.remove(player.getUniqueId());
	}

	/**
	 * Get the provided player's Flight instance.
	 * 
	 * @param player The flying player
	 * @return Flight instance
	 */
	public Flight getInstance(Player player) {
		if (INSTANCES.containsKey(player.getUniqueId())) {
			return INSTANCES.get(player.getUniqueId());
		}
		return null;
	}

	public void startCleanup() {
		new BukkitRunnable() {
			@Override
			public void run() {
				long currentTime = System.currentTimeMillis();
				while (!CLEANUP.isEmpty()) {
					FlightAbility ability = CLEANUP.peek();
					if (currentTime >= ability.startTime + ability.duration) {
						CLEANUP.poll();
						removeInstance(ability.player, ability.identifier);
					} else {
						break;
					}
				}
			}
		}.runTaskTimer(ProjectKorra.plugin, 0, 1);
	}

	public static class Flight {

		public static final int PERMANENT = -1;

		private Player player;
		private Player source;
		private boolean couldFly;
		private boolean wasFlying;
		private Map<String, FlightAbility> abilities;

		public Flight(Player player, Player source) {
			this.player = player;
			this.source = source;
			this.couldFly = player.getAllowFlight();
			this.wasFlying = player.isFlying();
			this.abilities = new HashMap<>();
		}

		public Player getPlayer() {
			return player;
		}

		public Player getSource() {
			return source;
		}

		@Override
		public String toString() {
			return "Flight{player=" + player.getName() + ",source=" + (source != null ? source.getName() : "null") + ",couldFly=" + couldFly + ",wasFlying=" + wasFlying + ",abilities=" + abilities + "}";
		}

	}

	public static class FlightAbility {

		private Player player;
		private String identifier;
		private long duration;
		private long startTime;

		public FlightAbility(Player player, String identifier, long duration) {
			this.player = player;
			this.identifier = identifier;
			this.duration = duration;
			this.startTime = System.currentTimeMillis();
		}

		@Override
		public String toString() {
			return "FlightAbility{player=" + player.getName() + ",identifier=" + identifier + ",duration=" + duration + ",startTime=" + startTime + "}";
		}
	}

}

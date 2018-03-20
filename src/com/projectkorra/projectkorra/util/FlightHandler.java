package com.projectkorra.projectkorra.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.projectkorra.ProjectKorra;

public class FlightHandler {

	/**
	 * A Map containing all Flight instances.
	 */
	private final Map<UUID, Flight> INSTANCES = new HashMap<>();
	private final Map<UUID, Set<String>> ABILITIES = new HashMap<>();
	/**
	 * A PriorityQueue containing all Flight instances which have a specified
	 * duration. This is used to reduce the number of iterations when cleaning
	 * up dead instances.
	 */
	private final PriorityQueue<Flight> CLEANUP = new PriorityQueue<>(100, new Comparator<Flight>() {
		@Override
		public int compare(Flight f1, Flight f2) {
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
	 * @return a new Flight instance
	 */
	public Flight createInstance(Player player, String identifier) {
		return createInstance(player, Flight.PERMANENT, identifier);
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
	 * @return a new Flight instance
	 */
	public Flight createInstance(Player player, Player source, String identifier) {
		return createInstance(player, source, Flight.PERMANENT, identifier);
	}

	/**
	 * Create a new Flight instance with the specified duration. This instance
	 * will automatically be removed with the set delay.
	 * 
	 * @param player The flying player
	 * @param duration Flight duration
	 * @param identifier The ability using Flight
	 * @return a new Flight instance
	 */
	public Flight createInstance(Player player, long duration, String identifier) {
		return createInstance(player, null, duration, identifier);
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
	 * @return a new Flight instance
	 */
	public Flight createInstance(Player player, Player source, long duration, String identifier) {
		if (!ABILITIES.containsKey(player.getUniqueId())) {
			ABILITIES.put(player.getUniqueId(), new HashSet<>());
		}
		ABILITIES.get(player.getUniqueId()).add(identifier);
		if (INSTANCES.containsKey(player.getUniqueId())) {
			return INSTANCES.get(player.getUniqueId());
		}
		Flight flight = new Flight(player, source, duration);
		if (flight.duration != Flight.PERMANENT) {
			CLEANUP.add(flight);
		}
		INSTANCES.put(player.getUniqueId(), flight);
		return flight;
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
		if (ABILITIES.containsKey(player.getUniqueId())) {
			ABILITIES.get(player.getUniqueId()).remove(identifier);
		}
		if (ABILITIES.get(player.getUniqueId()).isEmpty()) {
			ABILITIES.remove(player.getUniqueId());
			if (INSTANCES.containsKey(player.getUniqueId())) {
				wipeInstance(player);
				// Flight flight = INSTANCES.get(player.getUniqueId());
				// player.setAllowFlight(flight.couldFly);
				// player.setFlying(flight.wasFlying);
				// if (CLEANUP.contains(flight)) {
				// CLEANUP.remove(flight);
				// }
				// INSTANCES.remove(player.getUniqueId());
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
		ABILITIES.remove(player.getUniqueId());
		INSTANCES.remove(player.getUniqueId());
		CLEANUP.remove(flight);
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
					Flight flight = CLEANUP.peek();
					if (currentTime >= flight.startTime + flight.duration) {
						CLEANUP.poll();
						wipeInstance(flight.player);
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
		private long duration;
		private long startTime;

		public Flight(Player player, Player source, long duration) {
			this.player = player;
			this.source = source;
			this.couldFly = player.getAllowFlight();
			this.wasFlying = player.isFlying();
			this.duration = duration;
			this.startTime = System.currentTimeMillis();
		}

		public Player getPlayer() {
			return player;
		}

		public Player getSource() {
			return source;
		}

	}

}

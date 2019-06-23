package com.projectkorra.projectkorra.util;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.projectkorra.Manager;
import com.projectkorra.projectkorra.ProjectKorra;

public class FlightHandler extends Manager {

	/**
	 * A Map containing all Flight instances.
	 */
	private final Map<UUID, Flight> INSTANCES = new HashMap<>();
	/**
	 * A PriorityQueue containing all Flight instances which have a specified
	 * duration. This is used to reduce the number of iterations when cleaning
	 * up dead instances.
	 */
	private final PriorityQueue<FlightAbility> CLEANUP = new PriorityQueue<>(100, (f1, f2) -> (int) (f1.duration - f2.duration));

	private FlightHandler() {}

	@Override
	public void onActivate() {
		this.startCleanup();
	}

	/**
	 * Create a new Flight instance for the provided player with an unlimited
	 * duration. Call {@link FlightHandler#removeInstance(Player)} to remove
	 * this instance when necessary.
	 *
	 * @param player The flying player
	 * @param identifier The ability using Flight
	 */
	public void createInstance(final Player player, final String identifier) {
		this.createInstance(player, Flight.PERMANENT, identifier);
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
	public void createInstance(final Player player, final Player source, final String identifier) {
		this.createInstance(player, source, Flight.PERMANENT, identifier);
	}

	/**
	 * Create a new Flight instance with the specified duration. This instance
	 * will automatically be removed with the set delay.
	 *
	 * @param player The flying player
	 * @param duration Flight duration
	 * @param identifier The ability using Flight
	 */
	public void createInstance(final Player player, final long duration, final String identifier) {
		this.createInstance(player, null, duration, identifier);
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
	public void createInstance(final Player player, final Player source, final long duration, final String identifier) {
		if (this.INSTANCES.containsKey(player.getUniqueId())) {
			final Flight flight = this.INSTANCES.get(player.getUniqueId());
			final FlightAbility ability = new FlightAbility(player, identifier, duration);
			if (duration != Flight.PERMANENT) {
				this.CLEANUP.add(ability);
			}
			flight.abilities.put(identifier, ability);
		} else {
			final Flight flight = new Flight(player, source);
			final FlightAbility ability = new FlightAbility(player, identifier, duration);
			if (duration != Flight.PERMANENT) {
				this.CLEANUP.add(ability);
			}
			flight.abilities.put(identifier, ability);
			this.INSTANCES.put(player.getUniqueId(), flight);
		}
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
	public void removeInstance(final Player player, final String identifier) {
		if (this.INSTANCES.containsKey(player.getUniqueId())) {
			final Flight flight = this.INSTANCES.get(player.getUniqueId());
			if (flight.abilities.containsKey(identifier)) {
				flight.abilities.remove(identifier);
			}
			if (flight.abilities.isEmpty()) {
				this.wipeInstance(player);
			}
		}
	}

	/**
	 * Completely wipe all Flight data for the player. Should only be used if it
	 * is guaranteed they have a Flight instance.
	 *
	 * @param player
	 */
	private void wipeInstance(final Player player) {
		final Flight flight = this.INSTANCES.get(player.getUniqueId());
		player.setAllowFlight(flight.couldFly);
		player.setFlying(flight.wasFlying);
		flight.abilities.values().forEach(ability -> this.CLEANUP.remove(ability));
		this.INSTANCES.remove(player.getUniqueId());
	}

	/**
	 * Get the provided player's Flight instance.
	 *
	 * @param player The flying player
	 * @return Flight instance
	 */
	public Flight getInstance(final Player player) {
		if (this.INSTANCES.containsKey(player.getUniqueId())) {
			return this.INSTANCES.get(player.getUniqueId());
		}
		return null;
	}

	public void startCleanup() {
		new BukkitRunnable() {
			@Override
			public void run() {
				final long currentTime = System.currentTimeMillis();
				while (!FlightHandler.this.CLEANUP.isEmpty()) {
					final FlightAbility ability = FlightHandler.this.CLEANUP.peek();
					if (currentTime >= ability.startTime + ability.duration) {
						FlightHandler.this.CLEANUP.poll();
						FlightHandler.this.removeInstance(ability.player, ability.identifier);
					} else {
						break;
					}
				}
			}
		}.runTaskTimer(ProjectKorra.plugin, 0, 1);
	}

	public static class Flight {

		public static final int PERMANENT = -1;

		private final Player player;
		private final Player source;
		private final boolean couldFly;
		private final boolean wasFlying;
		private final Map<String, FlightAbility> abilities;

		public Flight(final Player player, final Player source) {
			this.player = player;
			this.source = source;
			this.couldFly = player.getAllowFlight();
			this.wasFlying = player.isFlying();
			this.abilities = new HashMap<>();
		}

		public Player getPlayer() {
			return this.player;
		}

		public Player getSource() {
			return this.source;
		}

		@Override
		public String toString() {
			return "Flight{player=" + this.player.getName() + ",source=" + (this.source != null ? this.source.getName() : "null") + ",couldFly=" + this.couldFly + ",wasFlying=" + this.wasFlying + ",abilities=" + this.abilities + "}";
		}
	}

	public static class FlightAbility {

		private final Player player;
		private final String identifier;
		private final long duration;
		private final long startTime;

		public FlightAbility(final Player player, final String identifier, final long duration) {
			this.player = player;
			this.identifier = identifier;
			this.duration = duration;
			this.startTime = System.currentTimeMillis();
		}

		@Override
		public String toString() {
			return "FlightAbility{player=" + this.player.getName() + ",identifier=" + this.identifier + ",duration=" + this.duration + ",startTime=" + this.startTime + "}";
		}
	}

}

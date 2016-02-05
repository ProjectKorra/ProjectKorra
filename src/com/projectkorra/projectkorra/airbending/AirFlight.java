package com.projectkorra.projectkorra.airbending;

import com.projectkorra.projectkorra.ability.FlightAbility;
import com.projectkorra.projectkorra.util.Flight;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.concurrent.ConcurrentHashMap;

public class AirFlight extends FlightAbility {
	
	private static final ConcurrentHashMap<String, Integer> HITS = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, Boolean> HOVERING = new ConcurrentHashMap<>();
	
	private boolean firstProgressIteration;
	private int maxHitsBeforeRemoval;
	private double speed;
	private Flight flight;

	public AirFlight(Player player) {
		super(player);		
		this.maxHitsBeforeRemoval = getConfig().getInt("Abilities.Air.Flight.MaxHits");
		this.speed = getConfig().getDouble("Abilities.Air.Flight.Speed");
		this.firstProgressIteration = true;
		start();
	}

	public static void addHit(Player player) {
		AirFlight airFlight = getAbility(player, AirFlight.class);
		if (airFlight != null) {
			if (HITS.containsKey(player.getName())) {
				if (HITS.get(player.getName()) >= airFlight.maxHitsBeforeRemoval) {
					HITS.remove(player.getName());
					remove(player);
				}
			} else {
				HITS.put(player.getName(), 1);
			}
		}
	}

	public static boolean isFlying(Player player) {
		return hasAbility(player, AirFlight.class);
	}

	public static boolean isHovering(Player player) {
		return HOVERING.containsKey(player.getName());
	}

	public static void remove(Player player) {
		if (isFlying(player)) {
			getAbility(player, AirFlight.class).remove();
		}
	}

	public static void cleanup() {
		HITS.clear();
		HOVERING.clear();
	}

	public static void setHovering(Player player, boolean bool) {
		String playername = player.getName();

		if (bool) {
			if (!HOVERING.containsKey(playername)) {
				HOVERING.put(playername, true);
				player.setVelocity(new Vector(0, 0, 0));
			}
		} else {
			if (HOVERING.containsKey(playername)) {
				HOVERING.remove(playername);
			}
		}
	}

	@Override
	public void progress() {
		boolean isHovering = isHovering(player);
		if (!bPlayer.canBend(this)) {
			remove();
			return;
		} else if (!player.isSneaking() && !isHovering && !firstProgressIteration) {
			remove();
			return;
		} else if (player.getLocation().subtract(0, 0.5, 0).getBlock().getType() != Material.AIR) {
			remove();
			return;
		}

		player.setAllowFlight(true);
		
		if (flight == null) {
			flight = new Flight(player);
			player.setFlying(true);
		}

		if (isHovering) {
			Vector vec = player.getVelocity().clone();
			vec.setY(0);
			player.setVelocity(vec);
		} else {
			player.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(speed));
		}
		firstProgressIteration = false;
	}

	@Override
	public void remove() {
		super.remove();
		HITS.remove(player.getName());
		HOVERING.remove(player.getName());
		if (flight != null) {
			flight.revert();
		}
		player.setFlying(false);
		player.setAllowFlight(false);
	}

	@Override
	public String getName() {
		return "Flight";
	}

	@Override
	public Location getLocation() {
		return player != null ? player.getLocation() : null;
	}

	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

}

package com.projectkorra.ProjectKorra.airbending;

import com.projectkorra.ProjectKorra.Ability.CoreAbility;
import com.projectkorra.ProjectKorra.Ability.StockAbility;
import com.projectkorra.ProjectKorra.Utilities.Flight;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.concurrent.ConcurrentHashMap;

public class FlightAbility extends CoreAbility {

	private static ConcurrentHashMap<String, Integer> hits = new ConcurrentHashMap<String, Integer>();
	private static ConcurrentHashMap<String, Boolean> hovering = new ConcurrentHashMap<String, Boolean>();
	private Player player;
	private Flight flight;

	public FlightAbility(Player player) {
		if (!AirMethods.canFly(player, true, false))
			return;
		player.setAllowFlight(true);
		player.setVelocity(player.getEyeLocation().getDirection().normalize());
		this.player = player;
		//instances.put(player.getUniqueId(), this);
		putInstance(player, this);
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
		return containsPlayer(player, FlightAbility.class);
	}

	public static boolean isHovering(Player player) {
		return hovering.containsKey(player.getName());
	}

	public static void remove(Player player) {
		if (contains(player))
			getAbilityFromPlayer(player, FlightAbility.class).remove();
	}

	public static void removeAll() {
		CoreAbility.removeAll(StockAbility.Flight);
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
	public StockAbility getStockAbility() {
		return StockAbility.Flight;
	}

	@Override
	public boolean progress() {
		if (!AirMethods.canFly(player, false, isHovering(player))) {
			remove(player);
			return false;
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
		return true;
	}

	@Override
	public void reloadVariables() {
	}

	@Override
	public void remove() {
		String name = player.getName();
		//instances.remove(uuid);
		super.remove();
		hits.remove(name);
		hovering.remove(name);
		if (flight != null)
			flight.revert();
	}

}

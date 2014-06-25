package com.projectkorra.ProjectKorra.firebending;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Flight;
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AvatarState;


public class FireJet {

	public static ConcurrentHashMap<Player, FireJet> instances = new ConcurrentHashMap<Player, FireJet>();
	public static ConcurrentHashMap<String, Long> cooldowns = new ConcurrentHashMap<String, Long>();
	private static final double defaultfactor = ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.FireJet.Speed");
	private static final long defaultduration = ProjectKorra.plugin.getConfig().getLong("Abilities.Fire.FireJet.Duration");
	// private static final long cooldown = ConfigManager.fireJetCooldown;

	// private static ConcurrentHashMap<Player, Long> timers = new
	// ConcurrentHashMap<Player, Long>();

	private Player player;
	// private boolean canfly;
	private long time;
	private long duration = defaultduration;
	private double factor = defaultfactor;

	public FireJet(Player player) {
		if (instances.containsKey(player)) {
			// player.setAllowFlight(canfly);
			instances.remove(player);
			return;
		}
		// if (timers.containsKey(player)) {
		// if (System.currentTimeMillis() < timers.get(player)
		// + (long) ((double) cooldown / Methods
		// .getFirebendingDayAugment(player.getWorld()))) {
		// return;
		// }
		// }
		if (cooldowns.containsKey(player.getName())) {
			if (cooldowns.get(player.getName()) +  ProjectKorra.plugin.getConfig().getLong("Abilities.Fire.FireJet.Cooldown") >= System.currentTimeMillis()) {
				return;
			} else {
				cooldowns.remove(player.getName());
			}
		}

		factor = Methods.firebendingDayAugment(defaultfactor, player.getWorld());
		Block block = player.getLocation().getBlock();
		if (FireStream.isIgnitable(player, block)
				|| block.getType() == Material.AIR
				|| AvatarState.isAvatarState(player)) {
			player.setVelocity(player.getEyeLocation().getDirection().clone()
					.normalize().multiply(factor));
			block.setType(Material.FIRE);
			this.player = player;
			// canfly = player.getAllowFlight();
			new Flight(player);
			player.setAllowFlight(true);
			time = System.currentTimeMillis();
			// timers.put(player, time);
			instances.put(player, this);
			cooldowns.put(player.getName(), System.currentTimeMillis());
		}

	}

	public static boolean checkTemporaryImmunity(Player player) {
		if (instances.containsKey(player)) {
			return true;
		}
		return false;
	}

	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			// player.setAllowFlight(canfly);
			instances.remove(player);
			return;
		}
		if ((Methods.isWater(player.getLocation().getBlock()) || System
				.currentTimeMillis() > time + duration)
				&& !AvatarState.isAvatarState(player)) {
			// player.setAllowFlight(canfly);
			instances.remove(player);
		} else {
			player.getWorld().playEffect(player.getLocation(),
					Effect.MOBSPAWNER_FLAMES, 1);
			double timefactor;
			if (AvatarState.isAvatarState(player)) {
				timefactor = 1;
			} else {
				timefactor = 1 - ((double) (System.currentTimeMillis() - time))
						/ (2.0 * duration);
			}
			Vector velocity = player.getEyeLocation().getDirection().clone()
					.normalize().multiply(factor * timefactor);
			// Vector velocity = player.getVelocity().clone();
			// velocity.add(player.getEyeLocation().getDirection().clone()
			// .normalize().multiply(factor * timefactor));
			player.setVelocity(velocity);
			player.setFallDistance(0);
		}
	}

	public static void progressAll() {
		for (Player player : instances.keySet()) {
			instances.get(player).progress();
		}
	}

	public static ArrayList<Player> getPlayers() {
		ArrayList<Player> players = new ArrayList<Player>();
		for (Player player : instances.keySet()) {
			players.add(player);
		}
		return players;
	}

}

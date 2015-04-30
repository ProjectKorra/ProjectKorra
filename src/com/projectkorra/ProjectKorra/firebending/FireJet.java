package com.projectkorra.ProjectKorra.firebending;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Flight;
import com.projectkorra.projectkorra.Methods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.Ability.AvatarState;
import com.projectkorra.projectkorra.Utilities.ParticleEffect;


public class FireJet {

	public static ConcurrentHashMap<Player, FireJet> instances = new ConcurrentHashMap<Player, FireJet>();

	private static final double defaultfactor = ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.FireJet.Speed");
	private static final long defaultduration = ProjectKorra.plugin.getConfig().getLong("Abilities.Fire.FireJet.Duration");
	private static boolean isToggle = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Fire.FireJet.IsAvatarStateToggle");

	private Player player;
	private long time;
	private long duration = defaultduration;
	private double factor = defaultfactor;

	public FireJet(Player player) {
		if (instances.containsKey(player)) {
			instances.remove(player);
			return;
		}
		BendingPlayer bPlayer = Methods.getBendingPlayer(player.getName());

		if (bPlayer.isOnCooldown("FireJet")) return;

		factor = Methods.getFirebendingDayAugment(defaultfactor, player.getWorld());
		Block block = player.getLocation().getBlock();
		if (FireStream.isIgnitable(player, block) || block.getType() == Material.AIR	|| AvatarState.isAvatarState(player)) {
			player.setVelocity(player.getEyeLocation().getDirection().clone().normalize().multiply(factor));
			block.setType(Material.FIRE);
			this.player = player;
			// canfly = player.getAllowFlight();
			new Flight(player);
			player.setAllowFlight(true);
			time = System.currentTimeMillis();
			// timers.put(player, time);
			instances.put(player, this);
			bPlayer.addCooldown("FireJet", ProjectKorra.plugin.getConfig().getLong("Abilities.Fire.FireJet.Cooldown"));
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
		if ((Methods.isWater(player.getLocation().getBlock()) || System.currentTimeMillis() > time + duration)
				&& (!AvatarState.isAvatarState(player) || !isToggle)) {
			// player.setAllowFlight(canfly);
			instances.remove(player);
		} else {
			if (Methods.rand.nextInt(2) == 0) {
				Methods.playFirebendingSound(player.getLocation());
			}
			ParticleEffect.FLAME.display(player.getLocation(), 0.6F, 0.6F, 0.6F, 0, 20);
			ParticleEffect.SMOKE.display(player.getLocation(), 0.6F, 0.6F, 0.6F, 0, 20);
			double timefactor;
			if (AvatarState.isAvatarState(player) && isToggle) {
				timefactor = 1;
			} else {
				timefactor = 1 - ((double) (System.currentTimeMillis() - time)) / (2.0 * duration);
			}
			Vector velocity = player.getEyeLocation().getDirection().clone().normalize().multiply(factor * timefactor);
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

	public Player getPlayer() {
		return player;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public double getFactor() {
		return factor;
	}

	public void setFactor(double factor) {
		this.factor = factor;
	}

}

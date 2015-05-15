package com.projectkorra.ProjectKorra.chiblocking;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.airbending.Suffocate;

public class RapidPunch {

	public static ConcurrentHashMap<Player, RapidPunch> instances = new ConcurrentHashMap<Player, RapidPunch>();
	public static List<Player> punching = new ArrayList<Player>();
	
	private int damage = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.RapidPunch.Damage");
	private int punches = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.RapidPunch.Punches");
	private int distance = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.RapidPunch.Distance");
	private long cooldown = ProjectKorra.plugin.getConfig().getLong("Abilities.Chi.RapidPunch.Cooldown");
	
	private int numpunches;
	// private long timers;
	private Entity target;
	private Player player;

	public RapidPunch(Player p) {// , Entity t) {
		player = p;
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(p.getName());
		if (instances.containsKey(p))
			return;
		if (bPlayer.isOnCooldown("RapidPunch")) return;

		Entity t = GeneralMethods.getTargetedEntity(p, distance, new ArrayList<Entity>());

		if (t == null)
			return;

		target = t;
		numpunches = 0;
		instances.put(p, this);
	}
	
	public static void startPunchAll() {
		for (Player player : instances.keySet()) {
			if (player != null) instances.get(player).startPunch(player);
		}
	}

	public void startPunch(Player p) {
		if (numpunches >= punches)
			instances.remove(p);
		if (target instanceof LivingEntity && target != null) {
			LivingEntity lt = (LivingEntity) target;
			GeneralMethods.damageEntity(p, target, damage);
			if (target instanceof Player) {
				if (ChiPassive.willChiBlock(p, (Player) target)) {
					ChiPassive.blockChi((Player) target);
				}
				if(Suffocate.isChannelingSphere((Player) target)) {
					Suffocate.remove((Player) target);
				}
			}
			lt.setNoDamageTicks(0);
		}
		GeneralMethods.getBendingPlayer(p.getName()).addCooldown("RapidPunch", cooldown);
		swing(p);
		numpunches++;
	}

	private void swing(Player p) {
	}

	public static String getDescription() {
		return "This ability allows the chiblocker to punch rapidly in a short period. To use, simply punch."
				+ " This has a short cooldown.";
	}

	public int getDamage() {
		return damage;
	}

	public void setDamage(int damage) {
		this.damage = damage;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public long getCooldown() {
		return cooldown;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
		if(player != null)
			GeneralMethods.getBendingPlayer(player.getName()).addCooldown("RapidPunch", cooldown);
	}

	public Player getPlayer() {
		return player;
	}

	public int getPunches() {
		return punches;
	}

	public void setPunches(int punches) {
		this.punches = punches;
	}

	public int getNumpunches() {
		return numpunches;
	}

	public void setNumpunches(int numpunches) {
		this.numpunches = numpunches;
	}
}
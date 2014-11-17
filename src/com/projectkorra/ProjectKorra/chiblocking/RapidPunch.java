package com.projectkorra.ProjectKorra.chiblocking;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.airbending.Suffocate;

public class RapidPunch {

	public static ConcurrentHashMap<Player, RapidPunch> instances = new ConcurrentHashMap<Player, RapidPunch>();
	public static List<Player> punching = new ArrayList<Player>();
	
	private static int damage = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.RapidPunch.Damage");
	private static int punches = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.RapidPunch.Punches");
	private int distance = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.RapidPunch.Distance");
	private long cooldown = ProjectKorra.plugin.getConfig().getLong("Abilities.Chi.RapidPunch.Cooldown");
	
	private int numpunches;
	// private long timers;
	private Entity target;

	public RapidPunch(Player p) {// , Entity t) {
		BendingPlayer bPlayer = Methods.getBendingPlayer(p.getName());
		if (instances.containsKey(p))
			return;
		if (bPlayer.isOnCooldown("RapidPunch")) return;

		Entity t = Methods.getTargetedEntity(p, distance, new ArrayList<Entity>());

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
			Methods.damageEntity(p, target, damage);
			if (target instanceof Player) {
				if (ChiPassive.willChiBlock((Player) target,p)) {
					ChiPassive.blockChi((Player) target);
				}
				if(Suffocate.isChannelingSphere((Player) target)) {
					Suffocate.remove((Player) target);
				}
			}
			lt.setNoDamageTicks(0);
		}
		Methods.getBendingPlayer(p.getName()).addCooldown("RapidPunch", cooldown);
		swing(p);
		numpunches++;
	}

	private void swing(Player p) {
	}

	public static String getDescription() {
		return "This ability allows the chiblocker to punch rapidly in a short period. To use, simply punch."
				+ " This has a short cooldown.";
	}

}

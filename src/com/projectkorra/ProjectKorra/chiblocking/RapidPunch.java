package com.projectkorra.ProjectKorra.chiblocking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.airbending.Suffocate;

public class RapidPunch {

	public static ConcurrentHashMap<Player, RapidPunch> instances = new ConcurrentHashMap<Player, RapidPunch>();
	public static List<Player> punching = new ArrayList<Player>();
	private static Map<String, Long> cooldowns = new HashMap<String, Long>();
	
	private static int damage = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.RapidPunch.Damage");
	private static int punches = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.RapidPunch.Punches");
	private int distance = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.RapidPunch.Distance");
	private long cooldown = ProjectKorra.plugin.getConfig().getLong("Abilities.Chi.RapidPunch.Cooldown");
	
	private int numpunches;
	// private long timers;
	private Entity target;

	public RapidPunch(Player p) {// , Entity t) {
		if (instances.containsKey(p))
			return;
		if (cooldowns.containsKey(p.getName())) {
			if (cooldowns.get(p.getName()) + cooldown >= System.currentTimeMillis()) {
				return;
			} else {
				cooldowns.remove(p.getName());
			}
		}

		Entity t = Methods.getTargetedEntity(p, distance, new ArrayList<Entity>());

		if (t == null)
			return;

		target = t;
		numpunches = 0;
		instances.put(p, this);
	}

	public void startPunch(Player p) {
		if (numpunches >= punches)
			instances.remove(p);
		if (target instanceof LivingEntity && target != null) {
			LivingEntity lt = (LivingEntity) target;
			Methods.damageEntity(p, target, damage);
			if (target instanceof Player)
				if (ChiPassive.willChiBlock((Player) target)) {
					ChiPassive.blockChi((Player) target);
				}
				if(Suffocate.isChannelingSphere((Player) target)) {
					Suffocate.remove((Player) target);
				}
			lt.setNoDamageTicks(0);
		}
		cooldowns.put(p.getName(), System.currentTimeMillis());
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
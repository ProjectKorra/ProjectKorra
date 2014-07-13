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

public class RapidPunch {

	private static int damage = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.RapidPunch.Damage");
	private int distance = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.RapidPunch.Distance");
	 private long cooldown = ProjectKorra.plugin.getConfig().getLong("Abilities.Chi.RapidPunch.Cooldown");
	private static int punches = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.RapidPunch.Punches");

	 private static Map<String, Long> cooldowns = new HashMap<String, Long>();
	public static ConcurrentHashMap<Player, RapidPunch> instance = new ConcurrentHashMap<Player, RapidPunch>();
	private int numpunches;
	// private long timers;
	private Entity target;
	public static List<Player> punching = new ArrayList<Player>();

	public RapidPunch(Player p) {// , Entity t) {
		if (instance.containsKey(p))
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
		instance.put(p, this);
	}

	public void startPunch(Player p) {
		if (numpunches >= punches)
			instance.remove(p);
		if (target instanceof LivingEntity && target != null) {
			LivingEntity lt = (LivingEntity) target;
			Methods.damageEntity(p, target, "RapidPunch", damage);
			if (target instanceof Player)
				if (ChiPassive.willChiBlock((Player) target)) {
					ChiPassive.blockChi((Player) target);
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
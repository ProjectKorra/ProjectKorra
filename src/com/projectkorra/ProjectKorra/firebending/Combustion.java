package com.projectkorra.ProjectKorra.firebending;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Effect;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.Utilities.ParticleEffect;

public class Combustion {
	
	public static long chargeTime = ProjectKorra.plugin.getConfig().getLong("Abilities.Fire.Combustion.ChargeTime");
	public static long cooldown = ProjectKorra.plugin.getConfig().getLong("Abilities.Fire.Combustion.Cooldown");
	
	public static List<Integer> fireballs = new ArrayList<Integer>();
	
	private Player player;
	private long starttime;
	private boolean charged = false;
	public static ConcurrentHashMap<Player, Combustion> instances = new ConcurrentHashMap<Player, Combustion>();
	public static HashMap<String, Long> cooldowns = new HashMap<String, Long>();
	
	public Combustion(Player player) {
		if (instances.containsKey(player)) {
			return;
		}
		if (cooldowns.containsKey(player.getName())) {
			if (cooldowns.get(player.getName()) + cooldown >= System.currentTimeMillis()) {
				return;
			} else {
				cooldowns.remove(player.getName());
			}
		}
		this.player = player;
		starttime = System.currentTimeMillis();
		instances.put(player, this);
	}
	
	private void progress() {
		
		if (!instances.containsKey(player)) {
			return;
		}
		
		if (player.isDead() || !player.isOnline()) {
			instances.remove(player);
			return;
		}
		
		if (!Methods.canBend(player.getName(), "Combustion")) {
			instances.remove(player);
			return;
		}
		
		if (Methods.getBoundAbility(player) == null || !Methods.getBoundAbility(player).equalsIgnoreCase("Combustion")) {
			instances.remove(player);
			return;
		}
		
		long warmup = chargeTime;
		if (AvatarState.isAvatarState(player)) {
			warmup = 0;
		}
		
		if (System.currentTimeMillis() > starttime + warmup) {
			charged = true;
		}
				
		if (charged) {
			if (player.isSneaking()) {
				player.getWorld().playEffect(player.getEyeLocation(), Effect.SMOKE, 4, 3);
			} else {
				launchFireball();
				cooldowns.put(player.getName(), System.currentTimeMillis());
				instances.remove(player);
			}
		} else {
			if (!player.isSneaking()) {
				instances.remove(player);
			}
		}
		
		for (Entity entity: player.getWorld().getEntities()) {
			if (fireballs.contains(entity.getEntityId())) {
				ParticleEffect.CLOUD.display(entity.getLocation(), 1.0F, 1.0F, 1.0F, 1.0F, 30);
			}
		}
	}
	
	public static void progressAll() {
		for (Player player: instances.keySet()) {
			instances.get(player).progress();
		}
	}
	
	private void launchFireball() {
		fireballs.add(player.launchProjectile(org.bukkit.entity.Fireball.class).getEntityId());
	}

}

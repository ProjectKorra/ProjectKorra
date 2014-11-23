package com.projectkorra.ProjectKorra.chiblocking;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.earthbending.MetalClips;
import com.projectkorra.ProjectKorra.waterbending.Bloodbending;

public class AcrobatStance {

	public static double chiBlockBost = ProjectKorra.plugin.getConfig().getDouble("Abilities.Chi.AcrobatStance.ChiBlockBoost");
	public static double paralyzeDodgeBoost = ProjectKorra.plugin.getConfig().getDouble("Abilities.Chi.AcrobatStance.ParalyzeChanceDecrease");
	public static ConcurrentHashMap<Player, AcrobatStance> instances = new ConcurrentHashMap<Player, AcrobatStance>();
	
	private Player player;
	
	public AcrobatStance(Player player) {
		this.player = player;
		if (instances.containsKey(player)) {
			instances.remove(player);
			return;
		}
		
		if (WarriorStance.isInWarriorStance(player)) {
			WarriorStance.remove(player);
		}
		
		instances.put(player, this);
	}
	
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		
		if (!Methods.canBend(player.getName(), "AcrobatStance")) {
			remove();
			return;
		}
		
		if (MetalClips.isControlled(player) || Paralyze.isParalyzed(player) || Bloodbending.isBloodbended(player)) {
			remove();
			return;
		}
		
		if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, ChiPassive.speedPower + 1));
		}
		
		if (!player.hasPotionEffect(PotionEffectType.JUMP)) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 60, ChiPassive.jumpPower + 1));
		}
	}
	
	public static void progressAll() {
		for (Player player: instances.keySet()) {
			instances.get(player).progress();
		}
	}
	
	private void remove() {
		instances.remove(player);
	}
	
	public static void remove(Player player) {
		instances.remove(player);
	}
	
	public static boolean isInAcrobatStance(Player player) {
		return instances.containsKey(player);
	}
}

package com.projectkorra.ProjectKorra.chiblocking;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.earthbending.MetalClips;
import com.projectkorra.ProjectKorra.waterbending.Bloodbending;

public class AcrobatStance {

	public static double CHI_BLOCK_BOOST = ProjectKorra.plugin.getConfig().getDouble("Abilities.Chi.AcrobatStance.ChiBlockBoost");
	public static double PARA_DODGE_BOOST = ProjectKorra.plugin.getConfig().getDouble("Abilities.Chi.AcrobatStance.ParalyzeChanceDecrease");
	public static ConcurrentHashMap<Player, AcrobatStance> instances = new ConcurrentHashMap<Player, AcrobatStance>();
	
	private Player player;
	public double chiBlockBost = CHI_BLOCK_BOOST;
	public double paralyzeDodgeBoost = PARA_DODGE_BOOST;
	public int speed = ChiPassive.speedPower + 1;
	public int jump = ChiPassive.jumpPower + 1;
	
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
		
		if (!GeneralMethods.canBend(player.getName(), "AcrobatStance")) {
			remove();
			return;
		}
		
		if (MetalClips.isControlled(player) || Paralyze.isParalyzed(player) || Bloodbending.isBloodbended(player)) {
			remove();
			return;
		}
		
		if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, speed));
		}
		
		if (!player.hasPotionEffect(PotionEffectType.JUMP)) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 60, jump));
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

	public double getChiBlockBost() {
		return chiBlockBost;
	}

	public void setChiBlockBost(double chiBlockBost) {
		this.chiBlockBost = chiBlockBost;
	}

	public double getParalyzeDodgeBoost() {
		return paralyzeDodgeBoost;
	}

	public void setParalyzeDodgeBoost(double paralyzeDodgeBoost) {
		this.paralyzeDodgeBoost = paralyzeDodgeBoost;
	}

	public Player getPlayer() {
		return player;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public int getJump() {
		return jump;
	}

	public void setJump(int jump) {
		this.jump = jump;
	}
}

package com.projectkorra.ProjectKorra.airbending.chiblocking;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.Element;
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;

public class ChiPassive {
	
	public static double FallReductionFactor = ProjectKorra.plugin.getConfig().getDouble("Abilities.Chi.Passive.FallReductionFactor");
	public static int jumpPower = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.Passive.Jump");
	public static int speedPower = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.Passive.Speed");
	
	public static int dodgeChance = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.Passive.ChiBlock.DodgeChance");
	public static int duration = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.Passive.ChiBlock.Duration");
	
	public static boolean willChiBlock(Player player) {
		Random rand = new Random();
		if (rand.nextInt(99) + 1 < dodgeChance) {
			return false;
		}
		if (Methods.isChiBlocked(player.getName())) return false;
		return true;
	}
	
	public static void blockChi(Player player) {
		BendingPlayer.blockedChi.put(player.getName(), System.currentTimeMillis());
	}
	
	public static void handlePassive() {
		for (Player player: Bukkit.getOnlinePlayers()) {
			if (Methods.canBendPassive(player.getName(), Element.Chi)) {
				if (player.isSprinting()) {
					if (!player.hasPotionEffect(PotionEffectType.JUMP)) {
						player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 60, jumpPower - 1));
					}
					if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
						player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, speedPower - 1));
					}
				}
			}
			if (BendingPlayer.blockedChi.contains(player.getName())) {
				if (BendingPlayer.blockedChi.get(player.getName()) + duration >= System.currentTimeMillis()) {
					BendingPlayer.blockedChi.remove(player.getName());
				}
			}
		}
	}
}

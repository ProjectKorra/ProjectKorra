package com.projectkorra.ProjectKorra.chiblocking;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.Element;
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;

public class ChiPassive {
	
	private static FileConfiguration config = ProjectKorra.plugin.getConfig();
	
	public static double FallReductionFactor = config.getDouble("Abilities.Chi.Passive.FallReductionFactor");
	public static int jumpPower = config.getInt("Abilities.Chi.Passive.Jump");
	public static int speedPower = config.getInt("Abilities.Chi.Passive.Speed");
	public static int dodgeChance = config.getInt("Abilities.Chi.Passive.BlockChi.DodgeChance");
	public static int duration = config.getInt("Abilities.Chi.Passive.BlockChi.Duration");
	
	static long ticks = (duration / 1000) * 20;
	
	public static boolean willChiBlock(Player player) {
		Random rand = new Random();
		if (rand.nextInt(99) + 1 < dodgeChance) {
			return false;
		}
		if (Methods.isChiBlocked(player.getName())) {
			return false;
		}
		return true;
	}
	
	public static void blockChi(final Player player) {
		final BendingPlayer bPlayer = Methods.getBendingPlayer(player.getName());
		if (bPlayer == null) return;
		bPlayer.blockChi();
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(ProjectKorra.plugin, new Runnable() {
			public void run() {
				bPlayer.unblockChi();
			}
		}, ticks);
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
		}
//		for (String s: BendingPlayer.blockedChi.keySet()) {
//			
//			if (Methods.getBendingPlayer(s) == null) continue;
//			Bukkit.getServer().broadcastMessage("Chi Blocked for " + s);
//			if (BendingPlayer.blockedChi.get(s) + duration < System.currentTimeMillis()) {
//				Methods.getBendingPlayer(s).unblockChi();
//				Bukkit.getServer().broadcastMessage(s + " Chi Unblocked");
//			}
//			if (!(BendingPlayer.blockedChi.get(s) + duration >= System.currentTimeMillis())) {
//				if (Methods.getBendingPlayer(s) == null) continue;
//				Methods.getBendingPlayer(s).unblockChi();
//			}
//			if (BendingPlayer.blockedChi.contains(player.getName())) {
//				if (BendingPlayer.blockedChi.get(player.getName()) + duration < System.currentTimeMillis()) {
//					BendingPlayer.blockedChi.remove(player.getName());
//				} else {
//				}
//			}
//		}
//		for (String s: BendingPlayer.blockedChi.keySet()) {
//			if (BendingPlayer.blockedChi.get(s) + duration >= System.currentTimeMillis()) {
//				Bukkit.getServer().broadcastMessage(s + "'s Chi is blocked.");
//			} else {
//				Bukkit.getServer().broadcastMessage(s + "'s Chi has been unblocked.");
//				BendingPlayer.blockedChi.remove(s);
//			}
//		}
	}
}

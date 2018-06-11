package com.projectkorra.projectkorra.chiblocking.passive;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.airbending.Suffocate;
import com.projectkorra.projectkorra.chiblocking.AcrobatStance;
import com.projectkorra.projectkorra.chiblocking.QuickStrike;
import com.projectkorra.projectkorra.chiblocking.SwiftKick;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ActionBar;

public class ChiPassive {
	public static boolean willChiBlock(Player attacker, Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return false;
		}

		ChiAbility stance = bPlayer.getStance();
		QuickStrike quickStrike = CoreAbility.getAbility(player, QuickStrike.class);
		SwiftKick swiftKick = CoreAbility.getAbility(player, SwiftKick.class);
		double newChance = getChance();

		if (stance != null && stance instanceof AcrobatStance) {
			newChance += ((AcrobatStance) stance).getChiBlockBoost();
		}

		if (quickStrike != null) {
			newChance += quickStrike.getBlockChance();
		} else if (swiftKick != null) {
			newChance += swiftKick.getBlockChance();
		}

		if (Math.random() > newChance / 100.0) {
			return false;
		} else if (bPlayer.isChiBlocked()) {
			return false;
		}
		
		return true;
	}

	public static void blockChi(final Player player) {
		if (Suffocate.isChannelingSphere(player)) {
			Suffocate.remove(player);
		}

		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return;
		}
		
		bPlayer.blockChi();
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_HURT, 2, 0);
		
		long start = System.currentTimeMillis();
		new BukkitRunnable() {
			@Override
			public void run() {
				ActionBar.sendActionBar(Element.CHI.getColor() + "* Chiblocked *", player);
				if (System.currentTimeMillis() >= start + getDuration()) {
					bPlayer.unblockChi();
					cancel();
				}
			}
		}.runTaskTimer(ProjectKorra.plugin, 0, 1);
	}
	
	public static double getChance() {
		return ConfigManager.getConfig().getDouble("Abilities.Chi.Passive.BlockChi.Chance");
	}

	public static int getDuration() {
		return ConfigManager.getConfig().getInt("Abilities.Chi.Passive.BlockChi.Duration");
	}

	public static long getTicks() {
		return (getDuration() / 1000) * 20;
	}
}

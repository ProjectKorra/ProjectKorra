package com.projectkorra.projectkorra.chiblocking.passive;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.airbending.Suffocate;
import com.projectkorra.projectkorra.chiblocking.AcrobatStance;
import com.projectkorra.projectkorra.chiblocking.QuickStrike;
import com.projectkorra.projectkorra.chiblocking.SwiftKick;
import com.projectkorra.projectkorra.configuration.ConfigManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ChiPassive {

	public static boolean willChiBlock(Player attacker, Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return false;
		}

		ChiAbility stance = bPlayer.getStance();
		QuickStrike quickStrike = CoreAbility.getAbility(player, QuickStrike.class);
		SwiftKick swiftKick = CoreAbility.getAbility(player, SwiftKick.class);
		double newChance = 0;

		if (stance != null && stance instanceof AcrobatStance) {
			newChance = getChance() + ((AcrobatStance) stance).getChiBlockBoost();
		}

		if (quickStrike != null) {
			newChance = getChance() + quickStrike.getBlockChance();
		} else if (swiftKick != null) {
			newChance = getChance() + swiftKick.getBlockChance();
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

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(ProjectKorra.plugin, new Runnable() {
			@Override
			public void run() {
				bPlayer.unblockChi();
			}
		}, getTicks());
	}

	public static double getExhaustionFactor() {
		return ConfigManager.getConfig().getDouble("Abilities.Chi.Passive.ChiSaturation.ExhaustionFactor");
	}

	public static double getFallReductionFactor() {
		return ConfigManager.getConfig().getDouble("Abilities.Chi.Passive.Acrobatics.FallReductionFactor");
	}

	public static int getJumpPower() {
		return ConfigManager.getConfig().getInt("Abilities.Chi.Passive.ChiAgility.JumpPower");
	}

	public static int getSpeedPower() {
		return ConfigManager.getConfig().getInt("Abilities.Chi.Passive.ChiAgility.SpeedPower");
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

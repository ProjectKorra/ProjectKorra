package com.projectkorra.projectkorra.chiblocking.passive;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.api.ChiAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.airbending.Suffocate;
import com.projectkorra.projectkorra.chiblocking.AcrobatStance;
import com.projectkorra.projectkorra.chiblocking.QuickStrike;
import com.projectkorra.projectkorra.chiblocking.SwiftKick;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.properties.ChiPropertiesConfig;
import com.projectkorra.projectkorra.util.ActionBar;

@SuppressWarnings("rawtypes")
public class ChiPassive {
	public static boolean willChiBlock(final Player attacker, final Player player) {
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return false;
		}

		final ChiAbility stance = bPlayer.getStance();
		final QuickStrike quickStrike = CoreAbility.getAbility(player, QuickStrike.class);
		final SwiftKick swiftKick = CoreAbility.getAbility(player, SwiftKick.class);
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
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_HURT, 2, 0);

		final long start = System.currentTimeMillis();
		new BukkitRunnable() {
			@Override
			public void run() {
				ActionBar.sendActionBar(Element.CHI.getColor() + "* Chiblocked *", player);
				if (System.currentTimeMillis() >= start + getDuration()) {
					bPlayer.unblockChi();
					this.cancel();
				}
			}
		}.runTaskTimer(ProjectKorra.plugin, 0, 1);
	}

	public static double getChance() {
		return ConfigManager.getConfig(ChiPropertiesConfig.class).BlockChiChance;
	}

	public static long getDuration() {
		return ConfigManager.getConfig(ChiPropertiesConfig.class).BlockChiDuration;
	}

	public static long getTicks() {
		return (getDuration() / 1000) * 20;
	}
}

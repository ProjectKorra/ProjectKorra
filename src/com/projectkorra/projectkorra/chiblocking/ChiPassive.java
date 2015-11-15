package com.projectkorra.projectkorra.chiblocking;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.airbending.Suffocate;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ChiPassive {

	private static FileConfiguration config = ProjectKorra.plugin.getConfig();

	public static double FallReductionFactor = config.getDouble("Abilities.Chi.Passive.FallReductionFactor");
	public static int jumpPower = config.getInt("Abilities.Chi.Passive.Jump");
	public static int speedPower = config.getInt("Abilities.Chi.Passive.Speed");
	public static double chance = config.getDouble("Abilities.Chi.Passive.BlockChi.Chance");
	public static int duration = config.getInt("Abilities.Chi.Passive.BlockChi.Duration");

	static long ticks = (duration / 1000) * 20;

	public static boolean willChiBlock(Player attacker, Player player) {
		if (AcrobatStance.isInAcrobatStance(attacker)) {
			chance = chance + AcrobatStance.CHI_BLOCK_BOOST;
		}
		if (GeneralMethods.getBoundAbility(player) == "QuickStrike") {
			chance = chance + QuickStrike.blockChance;
		}
		if (GeneralMethods.getBoundAbility(player) == "SwiftKick") {
			chance = chance + SwiftKick.blockChance;
		}
		if (Math.random() > chance/100) {
			return false;
		}
		if (ChiMethods.isChiBlocked(player.getName())) {
			return false;
		}
		return true;
	}

	public static void blockChi(final Player player) {
		if (Suffocate.isChannelingSphere(player)) {
			Suffocate.remove(player);
		}
		final BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
		if (bPlayer == null)
			return;
		bPlayer.blockChi();

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(ProjectKorra.plugin, new Runnable() {
			public void run() {
				bPlayer.unblockChi();
			}
		}, ticks);
	}

	public static void handlePassive() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (GeneralMethods.canBendPassive(player.getName(), Element.Chi) && !GeneralMethods.canBendPassive(player.getName(), Element.Air)) { // If they're an airbender and gets the boosts we want to give them that instead of the Chi.
				if (player.isSprinting()) {
					if (!player.hasPotionEffect(PotionEffectType.JUMP) && !AcrobatStance.isInAcrobatStance(player)) {
						player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 60, jumpPower - 1));
					}
					if (!player.hasPotionEffect(PotionEffectType.SPEED) && !AcrobatStance.isInAcrobatStance(player)) {
						player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, speedPower - 1));
					}
				}
			}
		}
	}
}

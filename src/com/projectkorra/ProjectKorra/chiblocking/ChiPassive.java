package com.projectkorra.ProjectKorra.chiblocking;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.Element;
import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.airbending.Suffocate;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class ChiPassive {

    private static FileConfiguration config = ProjectKorra.plugin.getConfig();

    public static double FallReductionFactor = config.getDouble("Abilities.Chi.Passive.FallReductionFactor");
    public static int jumpPower = config.getInt("Abilities.Chi.Passive.Jump");
    public static int speedPower = config.getInt("Abilities.Chi.Passive.Speed");
    public static double dodgeChance = config.getDouble("Abilities.Chi.Passive.BlockChi.DodgeChance");
    public static int duration = config.getInt("Abilities.Chi.Passive.BlockChi.Duration");

    static long ticks = (duration / 1000) * 20;

    public static boolean willChiBlock(Player attacker, Player player) {
        if (AcrobatStance.isInAcrobatStance(attacker)) {
            dodgeChance = dodgeChance - AcrobatStance.CHI_BLOCK_BOOST;
        }

        Random rand = new Random();
        if (rand.nextInt(99) + 1 < dodgeChance) {
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
        if (bPlayer == null) return;
        bPlayer.blockChi();

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(ProjectKorra.plugin, bPlayer::unblockChi, ticks);
    }

    public static void handlePassive() {
        // If they're an airbender and gets the boosts we want to give them that instead of the Chi.
        Bukkit.getOnlinePlayers().stream()
                .filter(player -> GeneralMethods.canBendPassive(player.getName(), Element.Chi)
                        && !GeneralMethods.canBendPassive(player.getName(), Element.Air))
                .filter(Player::isSprinting)
                .forEach(player -> {
                    if (!player.hasPotionEffect(PotionEffectType.JUMP) && !AcrobatStance.isInAcrobatStance(player)) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 60, jumpPower - 1));
                    }
                    if (!player.hasPotionEffect(PotionEffectType.SPEED) && !AcrobatStance.isInAcrobatStance(player)) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, speedPower - 1));
                    }
                });
    }
}

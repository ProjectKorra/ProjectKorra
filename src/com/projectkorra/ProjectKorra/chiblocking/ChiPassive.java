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
import com.projectkorra.ProjectKorra.airbending.Suffocate;

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
        if (Methods.isChiBlocked(player.getName())) {
            return false;
        }
        return true;
    }
    
    public static void blockChi(final Player player) {
        if (Suffocate.isChannelingSphere(player)) {
            Suffocate.remove(player);
        }
        final BendingPlayer bPlayer = Methods.getBendingPlayer(player.getName());
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
            if (Methods.canBendPassive(player.getName(), Element.Chi)) {
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

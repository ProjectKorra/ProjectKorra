package com.projectkorra.ProjectKorra.waterbending;

import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.TempBlock;
import com.projectkorra.ProjectKorra.airbending.AirMethods;
import com.projectkorra.ProjectKorra.chiblocking.Smokescreen;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

public class HealingWaters {

	private static final double range = ProjectKorra.plugin.getConfig().getDouble("Abilities.Water.HealingWaters.Radius");
	private static final long interval = ProjectKorra.plugin.getConfig().getLong("Abilities.Water.HealingWaters.Interval");
	private static final int power = ProjectKorra.plugin.getConfig().getInt("Abilities.Water.HealingWaters.Power");
	
	private static long time = 0;

	public static void heal(Server server) {
		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();
            server.getOnlinePlayers().stream()
                    .filter(player -> GeneralMethods.getBoundAbility(player) != null)
                    .filter(player -> GeneralMethods.getBoundAbility(player).equalsIgnoreCase("HealingWaters")
                            && GeneralMethods.canBend(player.getName(), "HealingWaters"))
                    .forEach(com.projectkorra.ProjectKorra.waterbending.HealingWaters::heal);
        }
	}

	private static void heal(Player player) {
		if (inWater(player)) {
			if (player.isSneaking()) {
                Entity entity = GeneralMethods.getTargetedEntity(player, range, new ArrayList<>());
                if (entity instanceof LivingEntity && inWater(entity)) {
					giveHPToEntity((LivingEntity) entity);
				}
			} else {
				giveHP(player);
			}
		}
	}

	private static void giveHPToEntity(LivingEntity le) {
		if (!le.isDead() && le.getHealth() < le.getMaxHealth()) {
			applyHealingToEntity(le);
		}
        le.getActivePotionEffects().stream()
                .filter(effect -> WaterMethods.isNegativeEffect(effect.getType()))
                .forEach(effect -> le.removePotionEffect(effect.getType()));
    }

	private static void giveHP(Player player) {
		if (!player.isDead() && player.getHealth() < 20) {
			applyHealing(player);
		}
		for(PotionEffect effect : player.getActivePotionEffects()) {
			if(WaterMethods.isNegativeEffect(effect.getType())) {
				if((effect.getType() == PotionEffectType.BLINDNESS) && Smokescreen.blinded.containsKey(player.getName())) {
					return;
				}
				player.removePotionEffect(effect.getType());
			}
		}
	}



	private static boolean inWater(Entity entity) {
		Block block = entity.getLocation().getBlock();
        return WaterMethods.isWater(block) && !TempBlock.isTempBlock(block);
    }

	private static void applyHealing(Player player) {
		if (!GeneralMethods.isRegionProtectedFromBuild(player, "HealingWaters", player.getLocation()))
			if(player.getHealth() < player.getMaxHealth()) {
				player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 70, power));
				AirMethods.breakBreathbendingHold(player);
			}
//			for(PotionEffect effect : player.getActivePotionEffects()) {
//				if(Methods.isNegativeEffect(effect.getType())) {
//					player.removePotionEffect(effect.getType());
//				}
//			}
	}

	private static void applyHealingToEntity(LivingEntity le) {
		if(le.getHealth() < le.getMaxHealth()) {
			le.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 70, 1));
			AirMethods.breakBreathbendingHold(le);
		}
//		for(PotionEffect effect : le.getActivePotionEffects()) {
//			if(Methods.isNegativeEffect(effect.getType())) {
//				le.removePotionEffect(effect.getType());
//			}
//		}
	}

	public static String getDescription() {
		return "To use, the bender must be at least partially submerged in water. "
				+ "If the user is not sneaking, this ability will automatically begin "
				+ "working provided the user has it selected. If the user is sneaking, "
				+ "he/she is channeling the healing to their target in front of them. "
				+ "In order for this channel to be successful, the user and the target must "
				+ "be at least partially submerged in water. This ability will heal the user or target, and it will also remove any negative potion effects the user or target has.";
	}
}
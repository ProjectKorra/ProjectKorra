package com.projectkorra.ProjectKorra.waterbending;

import java.util.ArrayList;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.Methods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.TempBlock;
import com.projectkorra.projectkorra.chiblocking.Smokescreen;

public class HealingWaters {

	private static final double range = ProjectKorra.plugin.getConfig().getDouble("Abilities.Water.HealingWaters.Radius");
	private static final long interval = ProjectKorra.plugin.getConfig().getLong("Abilities.Water.HealingWaters.Interval");
	private static final int power = ProjectKorra.plugin.getConfig().getInt("Abilities.Water.HealingWaters.Power");
	
	private static long time = 0;

	public static void heal(Server server) {
		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();
			for (Player player : server.getOnlinePlayers()) {
				if (Methods.getBoundAbility(player) != null) {
					if (Methods.getBoundAbility(player).equalsIgnoreCase("HealingWaters") && Methods.canBend(player.getName(),"HealingWaters")) {
						heal(player);
					}
				}
			}
		}
	}

	private static void heal(Player player) {
		if (inWater(player)) {
			if (player.isSneaking()) {
				Entity entity = Methods.getTargetedEntity(player, range, new ArrayList<Entity>());
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
		for(PotionEffect effect : le.getActivePotionEffects()) {
			if(Methods.isNegativeEffect(effect.getType())) {
				le.removePotionEffect(effect.getType());
			}
		}
	}

	private static void giveHP(Player player) {
		if (!player.isDead() && player.getHealth() < 20) {
			applyHealing(player);
		}
		for(PotionEffect effect : player.getActivePotionEffects()) {
			if(Methods.isNegativeEffect(effect.getType())) {
				if((effect.getType() == PotionEffectType.BLINDNESS) && Smokescreen.blinded.containsKey(player.getName())) {
					return;
				}
				player.removePotionEffect(effect.getType());
			}
		}
	}



	private static boolean inWater(Entity entity) {
		Block block = entity.getLocation().getBlock();
		if (Methods.isWater(block) && !TempBlock.isTempBlock(block))
			return true;
		return false;
	}

	private static void applyHealing(Player player) {
		if (!Methods.isRegionProtectedFromBuild(player, "HealingWaters", player.getLocation()))
			if(player.getHealth() < player.getMaxHealth()) {
				player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 70, power));
				Methods.breakBreathbendingHold(player);
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
			Methods.breakBreathbendingHold(le);
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
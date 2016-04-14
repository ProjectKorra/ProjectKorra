package com.projectkorra.projectkorra.waterbending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.HealingAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.chiblocking.Smokescreen;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class HealingWaters extends HealingAbility {

	private static long time = 0;
	
	public HealingWaters(Player player) {
		super(player);
	}
	
	public static void heal() {
		if (System.currentTimeMillis() - time >= getInterval()) {
			time = System.currentTimeMillis();
			for (Player player : Bukkit.getServer().getOnlinePlayers()) {
				BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
				if (bPlayer != null && bPlayer.canBend(getAbility("HealingWaters"))) {
					heal(player);
				}
			}
		}
	}
	
	private static void heal(Player player) {
		if (inWater(player)) {
			if ((getShiftRequired() == true && player.isSneaking()) || getShiftRequired() == false) {
				Entity target = GeneralMethods.getTargetedEntity(player, getRadius());
				if (target == null || !(target instanceof LivingEntity)) {
					giveHP(player);
				} else {
					giveHPToEntity((LivingEntity) target);
				}
			} else if (getShiftRequired() == true && !player.isSneaking()) {
				return;
			}
		}
	}
	
	private static void giveHPToEntity(LivingEntity le) {
		if (!le.isDead() && le.getHealth() < le.getMaxHealth()) {
			applyHealingToEntity(le);
		}
		for (PotionEffect effect : le.getActivePotionEffects()) {
			if (WaterAbility.isNegativeEffect(effect.getType())) {
				le.removePotionEffect(effect.getType());
			}
		}
	}

	private static void giveHP(Player player) {
		if (!player.isDead() && player.getHealth() < 20) {
			applyHealing(player);
		}
		
		for (PotionEffect effect : player.getActivePotionEffects()) {
			if (isNegativeEffect(effect.getType())) {
				if ((effect.getType() == PotionEffectType.BLINDNESS) && Smokescreen.getBlindedTimes().containsKey(player.getName())) {
					return;
				}
				player.removePotionEffect(effect.getType());
			}
		}
	}

	private static boolean inWater(Entity entity) {
		Block block = entity.getLocation().getBlock();
		return isWater(block) && !TempBlock.isTempBlock(block);
	}

	private static void applyHealing(Player player) {
		if (!GeneralMethods.isRegionProtectedFromBuild(player, "HealingWaters", player.getLocation())) {
			if (player.getHealth() < player.getMaxHealth()) {
				player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, getDuration(), getPower()));
				AirAbility.breakBreathbendingHold(player);
			}
		}
	}

	private static void applyHealingToEntity(LivingEntity le) {
		if (le.getHealth() < le.getMaxHealth()) {
			le.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, getDuration(), 1));
			AirAbility.breakBreathbendingHold(le);
		}
	}

	public static long getTime() {
		return time;
	}

	public static void setTime(long time) {
		HealingWaters.time = time;
	}

	public static boolean getShiftRequired() {
		return getConfig().getBoolean("Abilities.Water.HealingWaters.ShiftRequired");
	}
	
	public static double getRadius() {
		return getConfig().getDouble("Abilities.Water.HealingWaters.Radius");
	}

	public static long getInterval() {
		return getConfig().getLong("Abilities.Water.HealingWaters.Interval");
	}

	public static int getPower() {
		return getConfig().getInt("Abilities.Water.HealingWaters.Power");
	}
	
	public static int getDuration() {
		return getConfig().getInt("Abilities.Water.HealingWaters.Duration");
	}

	@Override
	public String getName() {
		return "HealingWaters";
	}

	@Override
	public void progress() {
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public long getCooldown() {
		return 0;
	}
	
	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}
	
}

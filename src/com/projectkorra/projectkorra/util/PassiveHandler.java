package com.projectkorra.projectkorra.util;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.airbending.AirPassive;
import com.projectkorra.projectkorra.chiblocking.AcrobatStance;
import com.projectkorra.projectkorra.chiblocking.ChiPassive;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.EarthArmor;
import com.projectkorra.projectkorra.earthbending.EarthPassive;
import com.projectkorra.projectkorra.waterbending.PlantArmor;

import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ConcurrentHashMap;

public class PassiveHandler implements Runnable {
	
	private static final ConcurrentHashMap<Player, Float> FOOD = new ConcurrentHashMap<>();
	
	public static float getExhaustion(Player player, float level, double factor) {
		if (!FOOD.keySet().contains(player)) {
			FOOD.put(player, level);
			return level;
		} else {
			float oldlevel = FOOD.get(player);
			if (level < oldlevel) {
				level = 0;
			} else {
				level = (float) ((level - oldlevel) * factor + oldlevel);
			}
			FOOD.replace(player, level);
			return level;
		}
	}
	
	public static void checkArmorPassives(Player player) {
		if (ConfigManager.defaultConfig.get().getStringList("Properties.DisabledWorlds").contains(player.getWorld().getName())) {
			return;
		}
		
		if (Commands.isToggledForAll && ConfigManager.defaultConfig.get().getBoolean("Properties.TogglePassivesWithAllBending")) {
			return;
		}
		
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		
		if (bPlayer == null) return;
		if (CoreAbility.hasAbility(player, EarthArmor.class)) {
			EarthArmor abil = CoreAbility.getAbility(player, EarthArmor.class);
			if (abil.isFormed()) {
				int strength = abil.getStrength();
				player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 3, strength - 1), false);
			}
		}
		if (CoreAbility.hasAbility(player, PlantArmor.class)) {
			PlantArmor abil = CoreAbility.getAbility(player, PlantArmor.class);
			if (abil.isFormed()) {
				int strength = abil.getResistance();
				player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 3, strength - 1), false);
			}
		}
	}
	
	public static void checkExhaustionPassives(Player player) {
		double air = AirPassive.getExhaustionFactor();
		double chi = ChiPassive.getExhaustionFactor();
		
		if (ConfigManager.defaultConfig.get().getStringList("Properties.DisabledWorlds").contains(player.getWorld().getName())) {
			return;
		}
		
		if (Commands.isToggledForAll && ConfigManager.defaultConfig.get().getBoolean("Properties.TogglePassivesWithAllBending")) {
			return;
		}
		
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		
		if (bPlayer == null) return;
		
		if (!bPlayer.hasElement(Element.AIR)) air = 0;
		if (!bPlayer.hasElement(Element.CHI)) chi = 0;
		
		double max = Math.max(air, chi);
		if (max == 0) return;
		else {
			player.setExhaustion(getExhaustion(player, player.getExhaustion(), max));
		}
	}

	public static void checkSpeedPassives(Player player) {
		if (!player.isSprinting()) return;
		int air = AirPassive.getSpeedPower();
		int chi = ChiPassive.getSpeedPower();
		int earth = EarthPassive.getSandRunSpeed();
		
		if (ConfigManager.defaultConfig.get().getStringList("Properties.DisabledWorlds").contains(player.getWorld().getName())) {
			return;
		}
		
		if (Commands.isToggledForAll && ConfigManager.defaultConfig.get().getBoolean("Properties.TogglePassivesWithAllBending")) {
			return;
		}
		
		boolean sandbender = true;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		
		if (bPlayer == null) return;
		if (!bPlayer.canBendPassive(Element.EARTH)) sandbender = false;
		if (!bPlayer.hasSubElement(SubElement.SAND)) sandbender = false;
		if (!bPlayer.canBendPassive(Element.AIR)) air = 0;
		if (!bPlayer.canBendPassive(Element.CHI)) chi = 0;
		
		int max = 0;
		
		if (sandbender && EarthAbility.isSand(player.getLocation().getBlock().getRelative(BlockFace.DOWN))) {
			if (CoreAbility.hasAbility(player, AcrobatStance.class)) {
				AcrobatStance abil = CoreAbility.getAbility(player, AcrobatStance.class);
				max = Math.max(air, chi);
				max = Math.max(max, abil.getSpeed());
				max = Math.max(max, earth);
			} else {
				max = Math.max(air, chi);
				max = Math.max(max, earth);
			}
		} else {
			if (CoreAbility.hasAbility(player, AcrobatStance.class)) {
				AcrobatStance abil = CoreAbility.getAbility(player, AcrobatStance.class);
				max = Math.max(air, chi);
				max = Math.max(max, abil.getSpeed());
			} else {
				max = Math.max(air, chi);
			}
		}
		
		if (max == 0) return;
		boolean b = true;
		if (player.hasPotionEffect(PotionEffectType.SPEED)) {
			for (PotionEffect potion : player.getActivePotionEffects()) {
				if (potion.getType() == PotionEffectType.SPEED) {
					if (potion.getAmplifier() > max - 1) b = false;
				}
			}
		}
		if (b) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 3, max-1, true, false), false);
		}
	}
	
	public static void checkJumpPassives(Player player) {
		if (!player.isSprinting()) return;
		int air = AirPassive.getJumpPower();
		int chi = ChiPassive.getJumpPower();
		
		if (ConfigManager.defaultConfig.get().getStringList("Properties.DisabledWorlds").contains(player.getWorld().getName())) {
			return;
		}
		
		if (Commands.isToggledForAll && ConfigManager.defaultConfig.get().getBoolean("Properties.TogglePassivesWithAllBending")) {
			return;
		}
		
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		
		if (bPlayer == null) return;
		if (!bPlayer.canBendPassive(Element.AIR)) air = 0;
		if (!bPlayer.canBendPassive(Element.CHI)) chi = 0;
		int max = 0;
		if (CoreAbility.hasAbility(player, AcrobatStance.class)) {
			AcrobatStance abil = CoreAbility.getAbility(player, AcrobatStance.class);
			max = Math.max(air, chi);
			max = Math.max(max, abil.getSpeed());
		} else {
			max = Math.max(air, chi);
		}
		
		if (max == 0) return;
		boolean b = true;
		if (player.hasPotionEffect(PotionEffectType.JUMP)) {
			for (PotionEffect potion : player.getActivePotionEffects()) {
				if (potion.getType() == PotionEffectType.JUMP) {
					if (potion.getAmplifier() > max - 1) b = false;
				}
			}
		}
		if (b) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 3, max-1, true, false), false);
		}
	}

	@Override
	public void run() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			checkSpeedPassives(player);
			checkJumpPassives(player);
		}
		
	}
}

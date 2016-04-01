package com.projectkorra.projectkorra.util;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.airbending.AirPassive;
import com.projectkorra.projectkorra.chiblocking.AcrobatStance;
import com.projectkorra.projectkorra.chiblocking.ChiPassive;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.EarthArmor;
import com.projectkorra.projectkorra.earthbending.EarthPassive;
import com.projectkorra.projectkorra.waterbending.PlantArmor;

public class PassiveHandler {
	
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
	
	public static void handleArmorPassives() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (ConfigManager.defaultConfig.get().getStringList("Properties.DisabledWorlds").contains(player.getWorld().getName())) {
				return;
			}
			
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			
			if (bPlayer == null) continue;
			if (CoreAbility.hasAbility(player, EarthArmor.class)) {
				EarthArmor abil = CoreAbility.getAbility(player, EarthArmor.class);
				int strength = abil.getStrength();
				player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 3, strength - 1), false);
			}
			if (CoreAbility.hasAbility(player, PlantArmor.class)) {
				PlantArmor abil = CoreAbility.getAbility(player, PlantArmor.class);
				int strength = abil.getResistance();
				player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 3, strength - 1), false);
			}
		}
	}
	
	public static void handleExhaustionPassives() {
		double air = AirPassive.getExhaustionFactor();
		double chi = ChiPassive.getExhaustionFactor();
		
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (ConfigManager.defaultConfig.get().getStringList("Properties.DisabledWorlds").contains(player.getWorld().getName())) {
				return;
			}
			
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			
			if (bPlayer == null) continue;
			
			if (!bPlayer.hasElement(Element.AIR)) air = 0;
			if (!bPlayer.hasElement(Element.CHI)) chi = 0;
			
			double max = Math.max(air, chi);
			if (max == 0) continue;
			else {
				player.setExhaustion(getExhaustion(player, player.getExhaustion(), max));
			}
		}
	}

	public static void handleSpeedPassives() {
		int air = AirPassive.getSpeedPower();
		int chi = ChiPassive.getSpeedPower();
		int earth = EarthPassive.getSandRunSpeed();
		
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (ConfigManager.defaultConfig.get().getStringList("Properties.DisabledWorlds").contains(player.getWorld().getName())) {
				return;
			}
			
			boolean sandbender = true;
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			
			if (bPlayer == null) continue;
			if (!bPlayer.hasElement(Element.EARTH)) sandbender = false;
			if (!bPlayer.hasSubElement(SubElement.SAND)) sandbender = false;
			if (!bPlayer.hasElement(Element.AIR)) air = 0;
			if (!bPlayer.hasElement(Element.CHI)) chi = 0;
			
			int max = 0;
			
			if (sandbender && (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.SAND
					|| player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.SANDSTONE 
					|| player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.RED_SANDSTONE)) {
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
			
			if (max == 0) continue;
			if (player.isSprinting()) {
				player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 3, max-1));
			}
		}
	}
	
	public static void handleJumpPassives() {
		int air = AirPassive.getJumpPower();
		int chi = ChiPassive.getJumpPower();
		
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (ConfigManager.defaultConfig.get().getStringList("Properties.DisabledWorlds").contains(player.getWorld().getName())) {
				return;
			}
			
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			
			if (bPlayer == null) continue;
			if (!bPlayer.hasElement(Element.AIR)) air = 0;
			if (!bPlayer.hasElement(Element.CHI)) chi = 0;
			int max = 0;
			if (CoreAbility.hasAbility(player, AcrobatStance.class)) {
				AcrobatStance abil = CoreAbility.getAbility(player, AcrobatStance.class);
				max = Math.max(air, chi);
				max = Math.max(max, abil.getSpeed());
			} else {
				max = Math.max(air, chi);
				if (max == 0) {
					continue;
				}
			}
			
			if (player.isSprinting()) {
				player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 3, max-1), false);
			}
		}
	}
}

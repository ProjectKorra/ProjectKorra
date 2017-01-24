package com.projectkorra.projectkorra.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.PassiveManager;
import com.projectkorra.projectkorra.airbending.passive.AirPassive;
import com.projectkorra.projectkorra.airbending.passive.AirSaturation;
import com.projectkorra.projectkorra.chiblocking.passive.ChiPassive;
import com.projectkorra.projectkorra.chiblocking.passive.ChiSaturation;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.configuration.ConfigManager;

public class PassiveHandler {

	private static final Map<Player, Float> FOOD = new ConcurrentHashMap<>();

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
			FOOD.put(player, level);
			return level;
		}
	}

	public static void checkExhaustionPassives(Player player) {
		if (!CoreAbility.getAbility(AirSaturation.class).isEnabled() && !CoreAbility.getAbility(ChiSaturation.class).isEnabled()) {
			return;
		}
		double air = AirPassive.getExhaustionFactor();
		double chi = ChiPassive.getExhaustionFactor();

		if (ConfigManager.defaultConfig.get().getStringList("Properties.DisabledWorlds").contains(player.getWorld().getName())) {
			return;
		}

		if (Commands.isToggledForAll && ConfigManager.defaultConfig.get().getBoolean("Properties.TogglePassivesWithAllBending")) {
			return;
		}

		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer == null)
			return;

		if (!PassiveManager.hasPassive(player, CoreAbility.getAbility(AirSaturation.class)))
			air = 0;
		if (!PassiveManager.hasPassive(player, CoreAbility.getAbility(ChiSaturation.class)))
			chi = 0;

		double max = Math.max(air, chi);
		if (max == 0)
			return;
		else {
			player.setExhaustion(getExhaustion(player, player.getExhaustion(), max));
		}
	}

	/*
	 * public static void checkSpeedPassives(Player player) { if
	 * (!player.isSprinting()) return; int air = AirPassive.getSpeedPower(); int
	 * chi = ChiPassive.getSpeedPower(); int earth =
	 * EarthPassive.getSandRunSpeed();
	 * 
	 * if (ConfigManager.defaultConfig.get().getStringList(
	 * "Properties.DisabledWorlds").contains(player.getWorld().getName())) {
	 * return; }
	 * 
	 * if (Commands.isToggledForAll &&
	 * ConfigManager.defaultConfig.get().getBoolean(
	 * "Properties.TogglePassivesWithAllBending")) { return; }
	 * 
	 * boolean sandbender = true; BendingPlayer bPlayer =
	 * BendingPlayer.getBendingPlayer(player);
	 * 
	 * if (bPlayer == null) return; if (!bPlayer.canBendPassive(Element.EARTH))
	 * sandbender = false; if (!bPlayer.hasSubElement(SubElement.SAND))
	 * sandbender = false; if (!bPlayer.canBendPassive(Element.AIR)) air = 0; if
	 * (!bPlayer.canBendPassive(Element.CHI)) chi = 0;
	 * 
	 * int max = 0;
	 * 
	 * if (sandbender &&
	 * EarthAbility.isSand(player.getLocation().getBlock().getRelative(BlockFace
	 * .DOWN))) { if (CoreAbility.hasAbility(player, AcrobatStance.class)) {
	 * AcrobatStance abil = CoreAbility.getAbility(player, AcrobatStance.class);
	 * max = Math.max(air, chi); max = Math.max(max, abil.getSpeed()); max =
	 * Math.max(max, earth); } else { max = Math.max(air, chi); max =
	 * Math.max(max, earth); } } else { if (CoreAbility.hasAbility(player,
	 * AcrobatStance.class)) { AcrobatStance abil =
	 * CoreAbility.getAbility(player, AcrobatStance.class); max = Math.max(air,
	 * chi); max = Math.max(max, abil.getSpeed()); } else { max = Math.max(air,
	 * chi); } }
	 * 
	 * if (max == 0) return; boolean b = true; if
	 * (player.hasPotionEffect(PotionEffectType.SPEED)) { for (PotionEffect
	 * potion : player.getActivePotionEffects()) { if (potion.getType() ==
	 * PotionEffectType.SPEED) { if (potion.getAmplifier() > max - 1) b = false;
	 * } } } if (b) { player.addPotionEffect(new
	 * PotionEffect(PotionEffectType.SPEED, 15, max-1, true, false), false); } }
	 * 
	 * public static void checkJumpPassives(Player player) { if
	 * (!player.isSprinting()) return; int air = AirPassive.getJumpPower(); int
	 * chi = ChiPassive.getJumpPower();
	 * 
	 * if (ConfigManager.defaultConfig.get().getStringList(
	 * "Properties.DisabledWorlds").contains(player.getWorld().getName())) {
	 * return; }
	 * 
	 * if (Commands.isToggledForAll &&
	 * ConfigManager.defaultConfig.get().getBoolean(
	 * "Properties.TogglePassivesWithAllBending")) { return; }
	 * 
	 * BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
	 * 
	 * if (bPlayer == null) return; if (!bPlayer.canBendPassive(Element.AIR))
	 * air = 0; if (!bPlayer.canBendPassive(Element.CHI)) chi = 0; int max = 0;
	 * if (CoreAbility.hasAbility(player, AcrobatStance.class)) { AcrobatStance
	 * abil = CoreAbility.getAbility(player, AcrobatStance.class); max =
	 * Math.max(air, chi); max = Math.max(max, abil.getSpeed()); } else { max =
	 * Math.max(air, chi); }
	 * 
	 * if (max == 0) return; boolean b = true; if
	 * (player.hasPotionEffect(PotionEffectType.JUMP)) { for (PotionEffect
	 * potion : player.getActivePotionEffects()) { if (potion.getType() ==
	 * PotionEffectType.JUMP) { if (potion.getAmplifier() > max - 1) b = false;
	 * } } } if (b) { player.addPotionEffect(new
	 * PotionEffect(PotionEffectType.JUMP, 15, max-1, true, false), false); } }
	 */
}

package com.projectkorra.projectkorra.waterbending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.EarthArmor;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class WaterPassive {

	public static boolean applyNoFall(Player player) {
		if (Commands.isToggledForAll && ConfigManager.defaultConfig.get().getBoolean("Properties.TogglePassivesWithAllBending")) {
			return false;
		}
		Block block = player.getLocation().getBlock();
		Block fallBlock = block.getRelative(BlockFace.DOWN);
		if (TempBlock.isTempBlock(fallBlock) && (fallBlock.getType().equals(Material.ICE))) {
			return true;
		} else if (WaterAbility.isWaterbendable(player, null, block) && !WaterAbility.isPlant(block)) {
			return true;
		} else if (fallBlock.getType() == Material.AIR) {
			return true;
		} else if ((WaterAbility.isWaterbendable(player, null, fallBlock) && !WaterAbility.isPlant(fallBlock)) || fallBlock.getType() == Material.SNOW_BLOCK) {
			return true;
		}
		return false;
	}

	public static void handlePassive() {
		if (Commands.isToggledForAll && ConfigManager.defaultConfig.get().getBoolean("Properties.TogglePassivesWithAllBending")) {
			return;
		}
		
		double swimSpeed = getSwimSpeed();
		
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			if (bPlayer == null) {
				continue;
			}
			
			String ability = bPlayer.getBoundAbilityName();
			CoreAbility coreAbil = CoreAbility.getAbility(ability);
			if (bPlayer.canBendPassive(Element.WATER)) {
				if (CoreAbility.hasAbility(player, WaterSpout.class) || CoreAbility.hasAbility(player, EarthArmor.class)) {
					continue;
				} else if (CoreAbility.getAbility(player, WaterArms.class) != null) {
					continue;
				} else if (coreAbil == null || (coreAbil != null && !coreAbil.isSneakAbility())) {
					if (player.isSneaking() && WaterAbility.isWater(player.getLocation().getBlock())) {
						player.setVelocity(player.getEyeLocation().getDirection().clone().normalize().multiply(swimSpeed));
					}
				}
			}
		}
	}
	
	public static double getSwimSpeed() {
		return ConfigManager.getConfig().getDouble("Abilities.Water.Passive.SwimSpeedFactor");
	}
}

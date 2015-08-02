package com.projectkorra.ProjectKorra.waterbending;

import com.projectkorra.ProjectKorra.Element;
import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AbilityModuleManager;
import com.projectkorra.ProjectKorra.earthbending.EarthArmor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class WaterPassive {

	private static double swimFactor = ProjectKorra.plugin.getConfig().getDouble("Abilities.Water.Passive.SwimSpeedFactor");

	public static boolean applyNoFall(Player player) {
		Block block = player.getLocation().getBlock();
		Block fallblock = block.getRelative(BlockFace.DOWN);
		if (WaterMethods.isWaterbendable(block, player) && !WaterMethods.isPlant(block))
			return true;
		if (fallblock.getType() == Material.AIR)
			return true;
		if ((WaterMethods.isWaterbendable(fallblock, player) && !WaterMethods.isPlant(fallblock)) || fallblock.getType() == Material.SNOW_BLOCK)
			return true;
		return false;
	}

	@SuppressWarnings("deprecation")
	public static void handlePassive() {
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			String ability = GeneralMethods.getBoundAbility(player);
			if (GeneralMethods.canBendPassive(player.getName(), Element.Water)) {
				if (WaterSpout.instances.containsKey(player) || EarthArmor.instances.containsKey(player)) {
					continue;
				} else if (ability == null || !AbilityModuleManager.shiftabilities.contains(ability)) {
					if (player.isSneaking() && WaterMethods.isWater(player.getLocation().getBlock())) {
						player.setVelocity(player.getEyeLocation().getDirection().clone().normalize().multiply(swimFactor));
					}
				}

				if (player.getLocation().getBlock().isLiquid()) {
					for (Block block : GeneralMethods.getBlocksAroundPoint(player.getLocation(), 2)) {
						if (GeneralMethods.isAdjacentToThreeOrMoreSources(block) && WaterMethods.isWater(block)) {
							byte full = 0x0;
							block.setType(Material.WATER);
							block.setData(full);
						}
					}
				}
			}
		}
	}
}

package com.projectkorra.ProjectKorra.firebending;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.waterbending.Melt;
import com.projectkorra.ProjectKorra.waterbending.WaterMethods;

public class HeatMelt {

	private static final int range = ProjectKorra.plugin.getConfig().getInt("Abilities.Fire.HeatControl.Melt.Range");
	private static final int radius = ProjectKorra.plugin.getConfig().getInt("Abilities.Fire.HeatControl.Melt.Radius");

	public HeatMelt(Player player) {
		Location location = GeneralMethods.getTargetedLocation(player, (int) FireMethods.getFirebendingDayAugment(range, player.getWorld()));
		for (Block block : GeneralMethods.getBlocksAroundPoint(location, (int) FireMethods.getFirebendingDayAugment(radius, player.getWorld()))) {
			if (WaterMethods.isMeltable(block)) {
				Melt.melt(player, block);
			} else if (isHeatable(block)) {
				heat(block);
			}
		}
	}

	@SuppressWarnings("deprecation")
	private static void heat(Block block) {
		if (block.getType() == Material.OBSIDIAN) {
			block.setType(Material.LAVA);
			block.setData((byte) 0x0);
		}
	}

	private static boolean isHeatable(Block block) {
		return false;
	}
}
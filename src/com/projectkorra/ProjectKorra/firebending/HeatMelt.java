package com.projectkorra.ProjectKorra.firebending;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.waterbending.Melt;

public class HeatMelt {

	private static final int range = ProjectKorra.plugin.getConfig().getInt("Abilities.Fire.HeatControl.Melt.Range");
	private static final int radius = ProjectKorra.plugin.getConfig().getInt("Abilities.Fire.HeatControl.Melt.Radius");

	public HeatMelt(Player player) {
		Location location = Methods.getTargetedLocation(player,
				(int) Methods.getFirebendingDayAugment(range, player.getWorld()));
		for (Block block : Methods.getBlocksAroundPoint(location,
				(int) Methods.getFirebendingDayAugment(radius, player.getWorld()))) {
			if (Methods.isMeltable(block)) {
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
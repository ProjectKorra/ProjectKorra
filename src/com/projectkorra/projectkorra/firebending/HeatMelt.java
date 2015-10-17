package com.projectkorra.projectkorra.firebending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.configuration.ConfigLoadable;
import com.projectkorra.projectkorra.waterbending.Melt;
import com.projectkorra.projectkorra.waterbending.WaterMethods;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class HeatMelt implements ConfigLoadable {

	private static int range = config.get().getInt("Abilities.Fire.HeatControl.Melt.Range");
	private static int radius = config.get().getInt("Abilities.Fire.HeatControl.Melt.Radius");

	public HeatMelt(Player player) {
		//reloadVariables();
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

	@Override
	public void reloadVariables() {
		config.get().getInt("Abilities.Fire.HeatControl.Melt.Range");
		radius = config.get().getInt("Abilities.Fire.HeatControl.Melt.Radius");
	}
}

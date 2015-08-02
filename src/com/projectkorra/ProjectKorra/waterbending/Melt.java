package com.projectkorra.ProjectKorra.waterbending;

import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.Utilities.TempBlock;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashSet;

public class Melt {

	private static final int seaLevel = ProjectKorra.plugin.getConfig().getInt("Properties.SeaLevel");

	private static final int defaultrange = FreezeMelt.defaultrange;
	private static final int defaultradius = FreezeMelt.defaultradius;
	private static final int defaultevaporateradius = 3;

	private static final byte full = 0x0;

	public Melt(Player player) {
		if (!WaterMethods.canIcebend(player))
			return;

		int range = (int) WaterMethods.waterbendingNightAugment(defaultrange, player.getWorld());
		int radius = (int) WaterMethods.waterbendingNightAugment(defaultradius, player.getWorld());

		if (AvatarState.isAvatarState(player)) {
			range = AvatarState.getValue(range);
			radius = AvatarState.getValue(radius);
		}
		boolean evaporate = false;
		Location location = GeneralMethods.getTargetedLocation(player, range);
		if (WaterMethods.isWater(player.getTargetBlock((HashSet<Material>) null, range)) && !(player.getEyeLocation().getBlockY() <= 62)) {
			evaporate = true;
			radius = (int) WaterMethods.waterbendingNightAugment(defaultevaporateradius, player.getWorld());
		}
		for (Block block : GeneralMethods.getBlocksAroundPoint(location, radius)) {
			if (evaporate) {
				if (block.getY() > seaLevel)
					evaporate(player, block);
			} else {
				melt(player, block);
			}
		}

	}

	@SuppressWarnings("deprecation")
	public static void melt(Player player, Block block) {
		if (GeneralMethods.isRegionProtectedFromBuild(player, "PhaseChange", block.getLocation()))
			return;
		if (!Wave.canThaw(block)) {
			Wave.thaw(block);
			return;
		}
		if (!Torrent.canThaw(block)) {
			Torrent.thaw(block);
			return;
		}
		WaterWave.thaw(block);
		WaterCombo.thaw(block);
		if (WaterMethods.isMeltable(block) && !TempBlock.isTempBlock(block) && WaterManipulation.canPhysicsChange(block)) {
			if (block.getType() == Material.SNOW) {
				block.setType(Material.AIR);
				return;
			}
			if (FreezeMelt.frozenblocks.containsKey(block)) {
				FreezeMelt.thaw(block);
			} else {
				block.setType(Material.WATER);
				block.setData(full);
			}
		}
	}

	public static void evaporate(Player player, Block block) {
		if (GeneralMethods.isRegionProtectedFromBuild(player, "PhaseChange", block.getLocation()))
			return;
		if (WaterMethods.isWater(block) && !TempBlock.isTempBlock(block) && WaterManipulation.canPhysicsChange(block)) {
			block.setType(Material.AIR);
			block.getWorld().playEffect(block.getLocation(), Effect.SMOKE, 1);
		}
	}

}

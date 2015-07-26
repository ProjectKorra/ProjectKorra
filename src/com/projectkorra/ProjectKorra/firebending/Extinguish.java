package com.projectkorra.ProjectKorra.firebending;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.Element;
import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.airbending.AirBlast;
import com.projectkorra.ProjectKorra.waterbending.WaterMethods;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashSet;

public class Extinguish {

	private static double defaultrange = ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.HeatControl.Extinguish.Range");
	private static double defaultradius = ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.HeatControl.Extinguish.Radius");

	@SuppressWarnings("unused")
	private static byte full = AirBlast.full;

	public Extinguish(Player player) {
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());

		if (bPlayer.isOnCooldown("HeatControl")) return;

		double range = FireMethods.getFirebendingDayAugment(defaultrange, player.getWorld());
		if (WaterMethods.isMeltable(player.getTargetBlock((HashSet<Material>) null, (int) range))) {
			new HeatMelt(player);
			return;
		}
		double radius = FireMethods.getFirebendingDayAugment(defaultradius, player.getWorld());
		for (Block block : GeneralMethods.getBlocksAroundPoint(
				player.getTargetBlock((HashSet<Material>) null, (int) range).getLocation(), radius)) {
			
			Material mat = block.getType();
			if(mat != Material.FIRE 
					/*&& mat != Material.STATIONARY_LAVA
					&& mat != Material.LAVA*/)
				continue;
			if (GeneralMethods.isRegionProtectedFromBuild(player, "Blaze",
					block.getLocation()))
				continue;
			if (block.getType() == Material.FIRE) {
				block.setType(Material.AIR);
				block.getWorld().playEffect(block.getLocation(), Effect.EXTINGUISH, 0);
			} /*else if (block.getType() == Material.STATIONARY_LAVA) {
				block.setType(Material.OBSIDIAN);
				block.getWorld().playEffect(block.getLocation(), Effect.EXTINGUISH, 0);
			} else if (block.getType() == Material.LAVA) {
				if (block.getData() == full) {
					block.setType(Material.OBSIDIAN);
				} else {
					block.setType(Material.COBBLESTONE);
				}
				block.getWorld().playEffect(block.getLocation(), Effect.EXTINGUISH, 0);
			}*/
		}

		bPlayer.addCooldown("HeatControl", GeneralMethods.getGlobalCooldown());
	}

	public static boolean canBurn(Player player) {
		if (GeneralMethods.getBoundAbility(player) != null) {
            if (GeneralMethods.getBoundAbility(player).equalsIgnoreCase("HeatControl")
                    || FireJet.checkTemporaryImmunity(player)) {
                player.setFireTicks(-1);
				return false;
			}
		}

		if (player.getFireTicks() > 80 && GeneralMethods.canBendPassive(player.getName(), Element.Fire)) {
			player.setFireTicks(80);
		}

		// Methods.verbose(player.getFireTicks());

		return true;
	}

	public static String getDescription() {
		return "While this ability is selected, the firebender becomes impervious "
				+ "to fire damage and cannot be ignited. "
				+ "If the user left-clicks with this ability, the targeted area will be "
				+ "extinguished, although it will leave any creature burning engulfed in flames. "
				+ "This ability can also cool lava. If this ability is used while targetting ice or snow, it"
				+ " will instead melt blocks in that area. Finally, sneaking with this ability will cook any food in your hand.";
	}
}
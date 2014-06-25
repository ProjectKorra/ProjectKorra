package com.projectkorra.ProjectKorra.firebending;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.projectkorra.ProjectKorra.Element;
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.airbending.AirBlast;

public class Extinguish {

	private static double defaultrange = ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.HeatControl.Extinguish.Range");
	private static double defaultradius = ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.HeatControl.Extinguish.Radius");
	public static ConcurrentHashMap<String, Long> cooldowns = new ConcurrentHashMap<String, Long>();
	
	private static byte full = AirBlast.full;

	public Extinguish(Player player) {

		if (cooldowns.containsKey(player.getName())) {
			if (cooldowns.get(player.getName()) + ProjectKorra.plugin.getConfig().getLong("Properties.GlobalCooldown") >= System.currentTimeMillis()) {
				return;
			} else {
				cooldowns.remove(player.getName());
			}
		}

		double range = Methods.firebendingDayAugment(defaultrange,
				player.getWorld());
		if (Methods.isMeltable(player.getTargetBlock(null, (int) range))) {
			new HeatMelt(player);
			return;
		}
		double radius = Methods.firebendingDayAugment(defaultradius,
				player.getWorld());
		for (Block block : Methods.getBlocksAroundPoint(
				player.getTargetBlock(null, (int) range).getLocation(), radius)) {
//			if (Methods.isRegionProtectedFromBuild(player, Abilities.Blaze,
//					block.getLocation()))
//				continue;
			if (block.getType() == Material.FIRE) {
				block.setType(Material.AIR);
				block.getWorld().playEffect(block.getLocation(),
						Effect.EXTINGUISH, 0);
			} else if (block.getType() == Material.STATIONARY_LAVA) {
				block.setType(Material.OBSIDIAN);
				block.getWorld().playEffect(block.getLocation(),
						Effect.EXTINGUISH, 0);
			} else if (block.getType() == Material.LAVA) {
				if (block.getData() == full) {
					block.setType(Material.OBSIDIAN);
				} else {
					block.setType(Material.COBBLESTONE);
				}
				block.getWorld().playEffect(block.getLocation(),
						Effect.EXTINGUISH, 0);
			}
		}

		cooldowns.put(player.getName(), System.currentTimeMillis());
	}

	public static boolean canBurn(Player player) {
		if (Methods.getBoundAbility(player) != null) {
			if (Methods.getBoundAbility(player).equalsIgnoreCase("HeatControl") || FireJet.checkTemporaryImmunity(player)) {
				player.setFireTicks(0);
				return false;
			}
		}

		if (player.getFireTicks() > 80
				&& Methods.canBendPassive(player.getName(), Element.Fire)) {
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
package com.projectkorra.projectkorra.firebending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.airbending.AirBlast;
import com.projectkorra.projectkorra.configuration.ConfigLoadable;
import com.projectkorra.projectkorra.waterbending.WaterMethods;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashSet;

/**
 * Used in {@link Cook HeatControl}.
 */
public class Extinguish implements ConfigLoadable {

	private static double defaultrange = config.get().getDouble("Abilities.Fire.HeatControl.Extinguish.Range");
	private static double defaultradius = config.get().getDouble("Abilities.Fire.HeatControl.Extinguish.Radius");

	@SuppressWarnings("unused")
	private static byte full = AirBlast.full;

	public Extinguish(Player player) {
		/* Initial Checks */
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
		if (bPlayer.isOnCooldown("HeatControl"))
			return;
		/* End Initial Checks */
		reloadVariables();

		double range = FireMethods.getFirebendingDayAugment(defaultrange, player.getWorld());
		if (WaterMethods.isMeltable(player.getTargetBlock((HashSet<Material>) null, (int) range))) {
			new HeatMelt(player);
			return;
		}
		double radius = FireMethods.getFirebendingDayAugment(defaultradius, player.getWorld());
		for (Block block : GeneralMethods.getBlocksAroundPoint(player.getTargetBlock((HashSet<Material>) null, (int) range).getLocation(), radius)) {

			Material mat = block.getType();
			if (mat != Material.FIRE
			/*
			 * && mat != Material.STATIONARY_LAVA && mat != Material.LAVA
			 */)
				continue;
			if (GeneralMethods.isRegionProtectedFromBuild(player, "Blaze", block.getLocation()))
				continue;
			if (block.getType() == Material.FIRE) {
				block.setType(Material.AIR);
				block.getWorld().playEffect(block.getLocation(), Effect.EXTINGUISH, 0);
			} /*
			 * else if (block.getType() == Material.STATIONARY_LAVA) {
			 * block.setType(Material.OBSIDIAN);
			 * block.getWorld().playEffect(block.getLocation(),
			 * Effect.EXTINGUISH, 0); } else if (block.getType() ==
			 * Material.LAVA) { if (block.getData() == full) {
			 * block.setType(Material.OBSIDIAN); } else {
			 * block.setType(Material.COBBLESTONE); }
			 * block.getWorld().playEffect(block.getLocation(),
			 * Effect.EXTINGUISH, 0); }
			 */
		}

		bPlayer.addCooldown("HeatControl", GeneralMethods.getGlobalCooldown());
	}

	public static boolean canBurn(Player player) {
		if (GeneralMethods.getBoundAbility(player) != null) {
			if (GeneralMethods.getBoundAbility(player).equalsIgnoreCase("HeatControl") || FireJet.checkTemporaryImmunity(player)) {
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
		return "While this ability is selected, the firebender becomes impervious " + "to fire damage and cannot be ignited. " + "If the user left-clicks with this ability, the targeted area will be " + "extinguished, although it will leave any creature burning engulfed in flames. " + "This ability can also cool lava. If this ability is used while targetting ice or snow, it" + " will instead melt blocks in that area. Finally, sneaking with this ability will cook any food in your hand.";
	}

	@Override
	public void reloadVariables() {
		defaultrange = config.get().getDouble("Abilities.Fire.HeatControl.Extinguish.Range");
		defaultradius = config.get().getDouble("Abilities.Fire.HeatControl.Extinguish.Radius");
	}
}

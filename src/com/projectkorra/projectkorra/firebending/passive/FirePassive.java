package com.projectkorra.projectkorra.firebending.passive;

import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.Tremorsense;
import com.projectkorra.projectkorra.firebending.Illumination;

public class FirePassive {

	public static void handle(final Player player) {
		if (Commands.isToggledForAll && ConfigManager.defaultConfig.get().getBoolean("Properties.TogglePassivesWithAllBending")) {
			return;
		}
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer != null && bPlayer.canBendPassive(CoreAbility.getAbility(Illumination.class)) && bPlayer.canUsePassive(CoreAbility.getAbility(Illumination.class))) {
			if (bPlayer != null && !CoreAbility.hasAbility(player, Illumination.class) && !CoreAbility.hasAbility(player, Tremorsense.class) && bPlayer.canBendIgnoreBinds(CoreAbility.getAbility("Illumination")) && ConfigManager.defaultConfig.get().getBoolean("Abilities.Fire.Illumination.Passive")) {
				if (bPlayer.isIlluminating()) {
					new Illumination(player);
				}
			}
		}
	}
}

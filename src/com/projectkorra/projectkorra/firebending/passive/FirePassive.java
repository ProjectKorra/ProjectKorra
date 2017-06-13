package com.projectkorra.projectkorra.firebending.passive;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.Tremorsense;
import com.projectkorra.projectkorra.firebending.Illumination;

public class FirePassive {

	public static void handlePassive() {
		if (Commands.isToggledForAll && ConfigManager.defaultConfig.get().getBoolean("Properties.TogglePassivesWithAllBending")) {
			return;
		}
		for (Player player : Bukkit.getOnlinePlayers()) {
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			if (bPlayer != null && bPlayer.canBendPassive(Element.FIRE) && bPlayer.canUsePassive(Element.FIRE)) {
				if (bPlayer != null && !CoreAbility.hasAbility(player, Illumination.class) && !CoreAbility.hasAbility(player, Tremorsense.class) && bPlayer.canBendIgnoreBinds(CoreAbility.getAbility("Illumination")) && ConfigManager.defaultConfig.get().getBoolean("Abilities.Fire.Illumination.Passive")) {
					if (bPlayer.isIlluminating()) {
						new Illumination(player);
					}
				}
			}
		}
	}
}
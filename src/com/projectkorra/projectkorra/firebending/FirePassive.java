package com.projectkorra.projectkorra.firebending;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.configuration.ConfigManager;

public class FirePassive {

	public static void handlePassive() {
		if (Commands.isToggledForAll && ConfigManager.defaultConfig.get().getBoolean("Properties.TogglePassivesWithAllBending")) {
			return;
		}
		for (Player player : Bukkit.getOnlinePlayers()) {
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			if (bPlayer != null && bPlayer.canBendPassive(Element.FIRE)) {
				if (player.getFireTicks() > 80) {
					player.setFireTicks(80);
				}
				
				if (CoreAbility.getAbility(player, Illumination.class) == null) {
					new Illumination(player);
				}
			}
		}
	}
}

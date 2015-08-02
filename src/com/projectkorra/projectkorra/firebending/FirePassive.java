package com.projectkorra.projectkorra.firebending;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class FirePassive {

	public static void handlePassive() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (GeneralMethods.canBendPassive(player.getName(), Element.Fire)) {
				if (player.getFireTicks() > 80) {
					player.setFireTicks(80);
				}
			}
		}
	}

}

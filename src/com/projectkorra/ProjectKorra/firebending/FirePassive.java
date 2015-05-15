package com.projectkorra.ProjectKorra.firebending;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.projectkorra.ProjectKorra.Element;
import com.projectkorra.ProjectKorra.GeneralMethods;

public class FirePassive {
	
	public static void handlePassive() {
		for (Player player: Bukkit.getOnlinePlayers()) {
			if (GeneralMethods.canBendPassive(player.getName(), Element.Fire)) {
				if (player.getFireTicks() > 80) {
					player.setFireTicks(80);
				}
			}
		}
	}

}

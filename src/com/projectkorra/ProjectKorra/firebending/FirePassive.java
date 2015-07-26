package com.projectkorra.ProjectKorra.firebending;

import com.projectkorra.ProjectKorra.Element;
import com.projectkorra.ProjectKorra.GeneralMethods;

import org.bukkit.Bukkit;

public class FirePassive {
	
	public static void handlePassive() {
        Bukkit.getOnlinePlayers().stream()
                .filter(player -> GeneralMethods.canBendPassive(player.getName(), Element.Fire))
                .filter(player -> player.getFireTicks() > 80).forEach(player -> player.setFireTicks(80));
    }

}

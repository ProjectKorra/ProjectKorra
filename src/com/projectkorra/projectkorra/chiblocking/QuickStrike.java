package com.projectkorra.projectkorra.chiblocking;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class QuickStrike {
	public static int damage = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.QuickStrike.Damage");
	public static int blockChance = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.QuickStrike.ChiBlockChance");

	public QuickStrike(Player player) {
		if (!isEligible(player))
			return;

		Entity e = GeneralMethods.getTargetedEntity(player, 2, new ArrayList<Entity>());

		if (e == null)
			return;

		GeneralMethods.damageEntity(player, e, damage, "QuickStrike");

		if (e instanceof Player && ChiPassive.willChiBlock(player, (Player)e)) {
			ChiPassive.blockChi((Player) e);
		}
	}

	public boolean isEligible(Player player) {
		if (!GeneralMethods.canBend(player.getName(), "QuickStrike"))
			return false;

		if (GeneralMethods.getBoundAbility(player) == null)
			return false;

		if (!GeneralMethods.getBoundAbility(player).equalsIgnoreCase("QuickStrike"))
			return false;

		if (GeneralMethods.isRegionProtectedFromBuild(player, "QuickStrike", player.getLocation()))
			return false;

		return true;
	}
}

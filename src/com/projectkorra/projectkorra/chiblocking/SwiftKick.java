package com.projectkorra.projectkorra.chiblocking;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class SwiftKick {
	public static int damage = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.SwiftKick.Damage");
	public static int blockChance = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.ChiCombo.ChiBlockChance");

	public SwiftKick(Player player) {
		if (!isEligible(player))
			return;

		Entity e = GeneralMethods.getTargetedEntity(player, 4, new ArrayList<Entity>());

		if (e == null)
			return;

		GeneralMethods.damageEntity(player, e, damage);

		if (GeneralMethods.rand.nextInt(100) < blockChance && e instanceof Player) {
			ChiPassive.blockChi((Player) e);
		}

		GeneralMethods.getBendingPlayer(player.getName()).addCooldown("SwiftKick", 4000);
	}

	@SuppressWarnings("deprecation")
	public boolean isEligible(Player player) {
		if (!GeneralMethods.canBend(player.getName(), "SwiftKick"))
			return false;

		if (GeneralMethods.getBoundAbility(player) == null)
			return false;

		if (!GeneralMethods.getBoundAbility(player).equalsIgnoreCase("SwiftKick"))
			return false;

		if (GeneralMethods.isRegionProtectedFromBuild(player, "SwiftKick", player.getLocation()))
			return false;

		if (GeneralMethods.getBendingPlayer(player.getName()).isOnCooldown("SwiftKick"))
			return false;

		if (player.isOnGround())
			return false;

		return true;
	}
}

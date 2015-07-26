package com.projectkorra.ProjectKorra.chiblocking;

import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.chiblocking.ChiComboManager.ChiCombo;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class QuickStrike {
    public static int damage = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.QuickStrike.Damage");
    public static int blockChance = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.QuickStrike.ChiBlockChance");

    public QuickStrike(Player player) {
        if (!isEligible(player))
            return;

        Entity e = GeneralMethods.getTargetedEntity(player, 2, new ArrayList<>());

        if (e == null)
            return;

        GeneralMethods.damageEntity(player, e, damage);

        if (GeneralMethods.rand.nextInt(100) < blockChance && e instanceof Player) {
            ChiPassive.blockChi((Player) e);
        }

        ChiComboManager.addCombo(player, ChiCombo.QuickStrike);
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

package com.projectkorra.ProjectKorra.chiblocking;

import java.util.ArrayList;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.chiblocking.ChiComboManager.ChiCombo;

public class SwiftKick
{
    public static int damage = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.SwiftKick.Damage");
    public static int blockChance = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.ChiCombo.ChiBlockChance");
    
    public SwiftKick(Player player)
    {
        if (!isEligible(player))
            return;
        
        Entity e = Methods.getTargetedEntity(player, 4, new ArrayList<Entity>());
        
        if (e == null)
            return;
        
        Methods.damageEntity(player, e, damage);
        
        if (Methods.rand.nextInt(100) < blockChance && e instanceof Player)
        {
            ChiPassive.blockChi((Player) e);
        }
        
        Methods.getBendingPlayer(player.getName()).addCooldown("SwiftKick", 4000);
        ChiComboManager.addCombo(player, ChiCombo.SwiftKick);
    }
    
    @SuppressWarnings("deprecation")
    public boolean isEligible(Player player)
    {
        if (!Methods.canBend(player.getName(), "SwiftKick"))
            return false;
        
        if (Methods.getBoundAbility(player) == null)
            return false;
        
        if (!Methods.getBoundAbility(player).equalsIgnoreCase("SwiftKick"))
            return false;
        
        if (Methods.isRegionProtectedFromBuild(player, "SwiftKick", player.getLocation()))
            return false;
        
        if (Methods.getBendingPlayer(player.getName()).isOnCooldown("SwiftKick"))
            return false;
        
        if (player.isOnGround())
            return false;
        
        return true;
    }
}

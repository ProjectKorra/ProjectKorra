package com.projectkorra.ProjectKorra.chiblocking;

import java.util.ArrayList;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Methods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.chiblocking.ChiComboManager.ChiCombo;

public class QuickStrike
{
	public static int damage = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.QuickStrike.Damage");
	public static int blockChance = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.QuickStrike.ChiBlockChance");
	
	public QuickStrike(Player player)
	{
		if(!isEligible(player))
			return;
		
		Entity e = Methods.getTargetedEntity(player, 2, new ArrayList<Entity>());
		
		if(e == null)
			return;
		
		Methods.damageEntity(player, e, damage);
		
		if(Methods.rand.nextInt(100) < blockChance && e instanceof Player)
		{
			ChiPassive.blockChi((Player) e);
		}
		
		ChiComboManager.addCombo(player, ChiCombo.QuickStrike);
	}
	
	public boolean isEligible(Player player)
	{
		if(!Methods.canBend(player.getName(), "QuickStrike"))
			return false;
		
		if(Methods.getBoundAbility(player) == null)
			return false;
		
		if(!Methods.getBoundAbility(player).equalsIgnoreCase("QuickStrike"))
			return false;
		
		if(Methods.isRegionProtectedFromBuild(player, "QuickStrike", player.getLocation()))
			return false;
		
		return true;
	}
}

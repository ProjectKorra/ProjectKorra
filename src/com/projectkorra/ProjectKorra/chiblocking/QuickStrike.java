package com.projectkorra.ProjectKorra.chiblocking;

import java.util.ArrayList;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.chiblocking.ChiComboManager.ChiCombo;

public class QuickStrike
{
	public QuickStrike(Player player)
	{
		if(!isEligible(player))
			return;
		
		Entity e = Methods.getTargetedEntity(player, 2, new ArrayList<Entity>());
		
		if(e == null)
			return;
		
		Methods.damageEntity(player, e, 2);
		
		if(Methods.rand.nextInt(100) < 20 && e instanceof Player)
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

package com.projectkorra.ProjectKorra.chiblocking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;

public class ChiComboManager
{
	public enum ChiCombo
	{
		QuickStrike, SwiftKick;
	}
	
	public static HashMap<Player, List<ChiCombo>> instances = new HashMap<Player, List<ChiCombo>>();
	public static List<List<ChiCombo>> knownCombos = new ArrayList<List<ChiCombo>>();
	public static List<Entity> paralyzed = new ArrayList<Entity>();
	public static HashMap<Entity, Location> paralyzedLocations = new HashMap<Entity, Location>();
	public static long paralysisDuration = ProjectKorra.plugin.getConfig().getLong("Abilities.Chi.ChiCombo.ParalyzeDuration");
	
	public ChiComboManager()
	{
		List<ChiCombo> combo1 = new ArrayList<ChiCombo>();
		combo1.add(ChiCombo.QuickStrike);
		combo1.add(ChiCombo.SwiftKick);
		combo1.add(ChiCombo.QuickStrike);
		combo1.add(ChiCombo.QuickStrike);
		knownCombos.add(combo1);
	}
	
	public static void addCombo(Player player, ChiCombo combo)
	{
		if(!player.hasPermission("bending.ability.ChiCombo"))
			return;
		
		if(!instances.containsKey(player))
			instances.put(player, new ArrayList<ChiCombo>());
		instances.get(player).add(combo);
		
		if(instances.get(player).size() > 4)
			instances.put(player, shiftList(instances.get(player)));
		
		//ProjectKorra.log.info(instances.get(player).toString());
		
		 checkForValidCombo(player);
	}
	
	public static List<ChiCombo> shiftList(List<ChiCombo> list)
	{
		List<ChiCombo> list2 = new ArrayList<ChiCombo>();
		
		for(int i = 1; i < list.size(); i++)
		{
			list2.add(list.get(i));
		}
		
		return list2;
	}
	
	public static boolean checkForValidCombo(Player player)
	{
		List<ChiCombo> combo = instances.get(player);
		
		for(List<ChiCombo> knownCombo : knownCombos)
		{
			int size = knownCombo.size();
			
			//ProjectKorra.log.info("Scanning " + knownCombo.toString());
			
			if(combo.size() < size)
				continue;
			
			boolean isValid = true;
			for(int i = 1; i <= size; i++)
			{
				if(combo.get(combo.size() - i) != (knownCombo.get(knownCombo.size() - i)))
				{
					isValid = false;
					break;
				}
			}
			
			if(isValid)
			{
				//ProjectKorra.log.info("Combo Matched for player "+player.getName());
				
				if(combo.size() == 4
						&& combo.get(0) == ChiCombo.QuickStrike
						&& combo.get(1) == ChiCombo.SwiftKick
						&& combo.get(2) == ChiCombo.QuickStrike
						&& combo.get(3) == ChiCombo.QuickStrike)
				{
					paralyzeTarget(player, paralysisDuration);
				}
				
				instances.remove(player);
				return true;
			}
		}
		
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public static void paralyzeTarget(Player player, long time)
	{
		Entity e = Methods.getTargetedEntity(player, 4, new ArrayList<Entity>());
		
		if(e == null)
			return;
		
		if(e instanceof LivingEntity)
		{
			final LivingEntity le = (LivingEntity) e;
			paralyzed.add(le);
			paralyzedLocations.put(le, le.getLocation());
			
			ProjectKorra.plugin.getServer().getScheduler().scheduleSyncDelayedTask(ProjectKorra.plugin, new BukkitRunnable()
			{
				public void run()
				{
					paralyzed.remove(le);
					paralyzedLocations.remove(le);
				}
			}, (long) ((time / 1000) * 20));
		}
	}
	
	public static void addNewCombo(List<ChiCombo> combo)
	{
		knownCombos.add(combo);
	}
	
	public static boolean isParalyzed(Entity e)
	{
		return paralyzed.contains(e);
	}
	
	public static void handleParalysis()
	{
		for(Entity e : paralyzed)
		{
			if(!(e instanceof Player))
			{
				e.setVelocity(Methods.getDirection(e.getLocation(), paralyzedLocations.get(e)));
			}
			//e.teleport(paralyzedLocations.get(e));
		}
	}
}

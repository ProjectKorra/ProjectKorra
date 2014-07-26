package com.projectkorra.ProjectKorra.Ability;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AvatarState;

public class EnergyBending {

	public static ConcurrentHashMap<Player, EnergyBending> instances = new ConcurrentHashMap<Player, EnergyBending>();

	ConcurrentHashMap<Entity, Location> targetentities = new ConcurrentHashMap<Entity, Location>();

	private static final boolean isEnabled = ProjectKorra.plugin.getConfig().getBoolean("Abilities.EnergyBending.Enabled");
	private int range = ProjectKorra.plugin.getConfig().getInt("Abilities.EnergyBending.Range");
	
	private Player player;

	public EnergyBending(Player player) {
		if (instances.containsKey(player)) {
			remove(player);
			return;
		}
		if (AvatarState.isAvatarState(player)) {
			for (Entity entity : Methods.getEntitiesAroundPoint(player.getLocation(), range)) {
				if(entity instanceof LivingEntity) {
					if(entity instanceof Player) {
						if (Methods.isRegionProtectedFromBuild(player, "Energybending", entity.getLocation())
								|| (AvatarState.isAvatarState((Player) entity)
								|| entity.getEntityId() == player.getEntityId()))
							continue;
					}
					targetentities.put(entity, entity.getLocation().clone());
				}
			}
		} else {
			return;
		}
		this.player = player;
		instances.put(player, this);
	}

	private void progress() {
		if (!player.isSneaking()) {
			remove(player);
			return;
		}
		
		if (!isEnabled) {
			remove(player);
			return;
		}
		
		if (!Methods.canBend(player.getName(), "Energybending")) {
			remove(player);
			return;
		}
		if (Methods.getBoundAbility(player) == null) {
			remove(player);
			return;
		}
		if (!Methods.getBoundAbility(player).equalsIgnoreCase("Energybending")) {
			remove(player);
			return;
		}

		if (AvatarState.isAvatarState(player)) {
			ArrayList<Entity> entities = new ArrayList<Entity>();
			for(Entity entity : Methods.getEntitiesAroundPoint(player.getLocation(), range)) {
				if (Methods.isRegionProtectedFromBuild(player, "Energybending", entity.getLocation()))
					continue;
				if(entity instanceof Player)
					continue;
				if(!AvatarState.isAvatarState((Player) entity))
					continue;
				entities.add(entity);
				if (!targetentities.containsKey(entity)	&& entity instanceof LivingEntity) {
					targetentities.put(entity, entity.getLocation().clone());
				}
				EnergybendPlayer((Player)entity);
			}
			for (Entity entity : targetentities.keySet()) {
				if(!entities.contains(entity))
					targetentities.remove(entity);
			}
		}else{
			return;
		}
	}

	public static void progressAll() {
		for (Player player : instances.keySet()) {
			instances.get(player).progress();
		}
	}
	
	public static void EnergybendPlayer(Player player) {
		BendingPlayer bplayer = Methods.getBendingPlayer(player.getName());
		if(bplayer.isEnergybent()) {
			bplayer.RestoreBendingBPlayer();
		}
		bplayer.EnergybendBPlayer();
	}

	public static void remove(Player player) {
		if (instances.containsKey(player)) {
			instances.remove(player);
		}
	}

	public static boolean isEnergybent(Entity entity) {
		for (Player player : instances.keySet()) {
			if (instances.get(player).targetentities.containsKey(entity)) {
				return true;
			}
		}
		return false;
	}

}
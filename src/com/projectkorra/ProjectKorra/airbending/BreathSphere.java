package com.projectkorra.ProjectKorra.airbending;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.TempPotionEffect;
import com.projectkorra.ProjectKorra.Ability.AvatarState;

public class BreathSphere {

	public static ConcurrentHashMap<Player, BreathSphere> instances = new ConcurrentHashMap<Player, BreathSphere>();

	ConcurrentHashMap<Entity, Location> targetentities = new ConcurrentHashMap<Entity, Location>();

	private static boolean canBeUsedOnUndead = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Air.BreathSphere.CanBeUsedOnUndeadMobs");
	private int range = ProjectKorra.plugin.getConfig().getInt("Abilities.Air.BreathSphere.Range");
	private double damage = ProjectKorra.plugin.getConfig().getDouble("Abilities.Air.BreathSphere.Damage");

	private Player player;

	public BreathSphere(Player player) {
		if (instances.containsKey(player)) {
			remove(player);
			return;
		}
		
		if (AvatarState.isAvatarState(player)) {
			range = AvatarState.getValue(range);
			for (Entity entity : Methods.getEntitiesAroundPoint(player.getLocation(), range)) {
				if (entity instanceof LivingEntity) {
					if (entity instanceof Player) {
						if (Methods.isRegionProtectedFromBuild(player, "Bloodbending", entity.getLocation()) || entity.getEntityId() == player.getEntityId())
							continue;
					}
					Methods.damageEntity(player, entity, 0);
					targetentities.put(entity, entity.getLocation().clone());
				}
			}
		} else {
			Entity target = Methods.getTargetedEntity(player, range, new ArrayList<Entity>());
			if (target == null)
				return;
			if (!(target instanceof LivingEntity)|| Methods.isRegionProtectedFromBuild(player, "Bloodbending", target.getLocation()))
				return;
			if (!canBeUsedOnUndead && isUndead(target)) {
				return;
			}
			Methods.damageEntity(player, target, 0);
			targetentities.put(target, target.getLocation().clone());
		}
		this.player = player;
		instances.put(player, this);
	}

	private void progress() {
		PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, 60, 1);
		PotionEffect nausea = new PotionEffect(PotionEffectType.SLOW, 60, 1);

		if (!player.isSneaking()) {
			remove(player);
			return;
		}

		if (!canBeUsedOnUndead) {
			for (Entity entity: targetentities.keySet()) {
				if (isUndead(entity)) {
					targetentities.remove(entity);
				}
			}
		}

		if (!Methods.canBend(player.getName(), "BreathSphere")) {
			remove(player);
			return;
		}
		if (Methods.getBoundAbility(player) == null) {
			remove(player);
			return;
		}
		if (!Methods.getBoundAbility(player).equalsIgnoreCase("BreathSphere")) {
			remove(player);
			return;
		}

		if (AvatarState.isAvatarState(player)) {
			ArrayList<Entity> entities = new ArrayList<Entity>();
			for (Entity entity : Methods.getEntitiesAroundPoint(player.getLocation(), range)) {
				if (Methods.isRegionProtectedFromBuild(player, "BreathSphere", entity.getLocation()))
					continue;
				entities.add(entity);
				if (!targetentities.containsKey(entity)	&& entity instanceof LivingEntity) {
					Methods.damageEntity(player, entity, 0);
					targetentities.put(entity, entity.getLocation().clone());
				}
				if (entity instanceof LivingEntity) {
					((LivingEntity) entity).damage(damage);
					new TempPotionEffect((LivingEntity) entity, slow);
					new TempPotionEffect((LivingEntity) entity, nausea);
					entity.setFallDistance(0);
					if (entity instanceof Creature) {
						((Creature) entity).setTarget(player);
					}
					if (entity.isDead()) {
						instances.remove(player);
					}
					entity.teleport(entity);
					for(Location airsphere : Methods.getCircle(entity.getLocation(), 3, 3, false, true, 0)) {
						Methods.playAirbendingParticles(airsphere, 1);
					}
				}
			}
			for (Entity entity : targetentities.keySet()) {
				if (!entities.contains(entity))
					targetentities.remove(entity);
			}
		} else {
			for (Entity entity : targetentities.keySet()) {
				if(entity instanceof LivingEntity) {
					((LivingEntity) entity).damage(damage);
					new TempPotionEffect((LivingEntity) entity, slow);
					new TempPotionEffect((LivingEntity) entity, nausea);
					entity.setFallDistance(0);
					if (entity instanceof Creature) {
						((Creature) entity).setTarget(null);
					}
					if (entity.isDead()) {
						instances.remove(player);
					}
					entity.teleport(entity);
					for(Location airsphere : Methods.getCircle(entity.getLocation(), 3, 3, true, true, 0)) {
						Methods.playAirbendingParticles(airsphere, 1);
					}
				}
			}
		}
	}

	public static void progressAll() {
		for (Player player : instances.keySet()) {
			instances.get(player).progress();
		}
	}

	public static void remove(Player player) {
		if (instances.containsKey(player)) {
			instances.remove(player);
		}
	}
	
	public static void breakBreathSphere(Entity entity) {
		for (Player player : instances.keySet()) {
			if (instances.get(player).targetentities.containsKey(entity)) {
				instances.remove(player);
			}
		}
	}

	public static boolean isBreathbent(Entity entity) {
		for (Player player : instances.keySet()) {
			if (instances.get(player).targetentities.containsKey(entity)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isUndead(Entity entity) {
		if (entity == null) return false;
		if (entity.getType() == EntityType.ZOMBIE
				|| entity.getType() == EntityType.BLAZE
				|| entity.getType() == EntityType.GIANT
				|| entity.getType() == EntityType.IRON_GOLEM
				|| entity.getType() == EntityType.MAGMA_CUBE
				|| entity.getType() == EntityType.PIG_ZOMBIE
				|| entity.getType() == EntityType.SKELETON
				|| entity.getType() == EntityType.SLIME
				|| entity.getType() == EntityType.SNOWMAN
				|| entity.getType() == EntityType.ZOMBIE) {
			return true;
		}
		return false;
	}

	public static Location getBreathSphereLocation(Entity entity) {
		for (Player player : instances.keySet()) {
			if (instances.get(player).targetentities.containsKey(entity)) {
				return instances.get(player).targetentities.get(entity);
			}
		}
		return null;
	}
	
	public static boolean isChannelingSphere(Player player){
		if(instances.containsKey(player)) return true;
		return false;
	}
	
	public static void removeAll() {
		instances.clear();
	}

}
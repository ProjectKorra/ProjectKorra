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

public class Suffocate {

	public static ConcurrentHashMap<Player, Suffocate> instances = new ConcurrentHashMap<Player, Suffocate>();

	ConcurrentHashMap<Entity, Location> targetentities = new ConcurrentHashMap<Entity, Location>();

	private static boolean canBeUsedOnUndead = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Air.Suffocate.CanBeUsedOnUndeadMobs");
	private int range = ProjectKorra.plugin.getConfig().getInt("Abilities.Air.Suffocate.Range");
	private double damage = ProjectKorra.plugin.getConfig().getDouble("Abilities.Air.Suffocate.Damage");

	private Player player;
	private long time;
	private long warmup = 2000;
	
	private PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, 60, 1);
	private PotionEffect nausea = new PotionEffect(PotionEffectType.SLOW, 60, 1);

	public Suffocate(Player player) {
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
					if (System.currentTimeMillis() >= time + warmup) {
						Methods.damageEntity(player, entity, 0);
					}
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
			if (System.currentTimeMillis() >= time + warmup) {
				Methods.damageEntity(player, target, 0);
			}
			targetentities.put(target, target.getLocation().clone());
		}
		this.player = player;
		instances.put(player, this);
		time = System.currentTimeMillis();
	}

	private void progress() {

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

		if (!Methods.canBend(player.getName(), "Suffocate")) {
			remove(player);
			return;
		}
		if (Methods.getBoundAbility(player) == null) {
			remove(player);
			return;
		}
		if (!Methods.getBoundAbility(player).equalsIgnoreCase("Suffocate")) {
			remove(player);
			return;
		}

		if (AvatarState.isAvatarState(player)) {
			ArrayList<Entity> entities = new ArrayList<Entity>();
			for (Entity entity : Methods.getEntitiesAroundPoint(player.getLocation(), range)) {
				if (Methods.isRegionProtectedFromBuild(player, "Suffocate", entity.getLocation()))
					continue;
				if (entity.getEntityId() == player.getEntityId()) continue;
				entities.add(entity);
				if (entity.getLocation().distance(player.getLocation()) >= range) {
					breakSuffocate(entity);
				}
				if (!targetentities.containsKey(entity)	&& entity instanceof LivingEntity) {
					if (System.currentTimeMillis() >= time + warmup) {
						Methods.damageEntity(player, entity, damage);
					}
					targetentities.put(entity, entity.getLocation().clone());
				}
				if (entity instanceof LivingEntity) {
					if (Methods.isObstructed(player.getLocation(), entity.getLocation())) {
						breakSuffocate(entity);
					}
					if (System.currentTimeMillis() >= time + warmup) {
						Methods.damageEntity(player, entity, damage);
					}
					new TempPotionEffect((LivingEntity) entity, slow);
					new TempPotionEffect((LivingEntity) entity, nausea);
					entity.setFallDistance(0);
					if (entity instanceof Creature) {
						((Creature) entity).setTarget(player);
					}
					if (entity instanceof Player) {
						if (AvatarState.isAvatarState((Player) entity)) {
							remove(player);
							return;
						}
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
					if (Methods.isObstructed(player.getLocation(), entity.getLocation())) {
						breakSuffocate(entity);
					}
					if (System.currentTimeMillis() >= time + warmup) {
						Methods.damageEntity(player, entity, damage);
					}
					new TempPotionEffect((LivingEntity) entity, slow);
					new TempPotionEffect((LivingEntity) entity, nausea);
					entity.setFallDistance(0);
					if (entity instanceof Creature) {
						((Creature) entity).setTarget(null);
					}
					if (entity instanceof Player) {
						if (AvatarState.isAvatarState((Player) entity)) {
							remove(player);
							return;
						}
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

	public static void breakSuffocate(Entity entity) {
		for (Player player : instances.keySet()) {
			if (instances.get(player).targetentities.containsKey(entity)) {
				instances.remove(player);
			}
		}
	}

	public static boolean isBreathbent(Entity entity) {
		for (Player player : instances.keySet()) {
			if (instances.get(player).targetentities.containsKey(entity)) {
				if (System.currentTimeMillis() >= instances.get(player).time + instances.get(player).warmup) {
					return true;
				}
				return false;
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

	public static Location getSuffocateLocation(Entity entity) {
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
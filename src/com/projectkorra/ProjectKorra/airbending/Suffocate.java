package com.projectkorra.ProjectKorra.airbending;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Commands;
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.TempPotionEffect;
import com.projectkorra.ProjectKorra.Ability.AvatarState;

public class Suffocate {

	public static ConcurrentHashMap<Player, Suffocate> instances = new ConcurrentHashMap<Player, Suffocate>();

	private static boolean canBeUsedOnUndead = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Air.Suffocate.CanBeUsedOnUndeadMobs");
	private int range = ProjectKorra.plugin.getConfig().getInt("Abilities.Air.Suffocate.Range");
	private double damage = ProjectKorra.plugin.getConfig().getDouble("Abilities.Air.Suffocate.Damage");

	private Map<Entity, Location> targets = new HashMap<Entity, Location>();
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
			for (Entity entity: Methods.getEntitiesAroundPoint(player.getLocation(), range)) {
				if (entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId()) {
					targets.put(entity, entity.getLocation());
				}
			}
		} else {
			Entity en = Methods.getTargetedEntity(player, range, new ArrayList<Entity>());
			if (en != null && en instanceof LivingEntity && en.getEntityId() != player.getEntityId()) {
				targets.put(en, en.getLocation());

			}
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

		if (player.isDead()) {
			remove(player);
			return;
		}

		if (!Methods.canBend(player.getName(), "Suffocate")) {
			remove(player);
			return;
		}

		if (Methods.getBoundAbility(player) == null || !Methods.getBoundAbility(player).equalsIgnoreCase("Suffocate")) {
			remove(player);
			return;
		}

		try {
			for (Entity entity: targets.keySet()) {
				if (!targets.keySet().iterator().hasNext()) {
					remove(player);
					return;
				}
				if (targets.isEmpty()) {
					remove(player);
					return;
				}
				if (isUndead(entity) && !canBeUsedOnUndead) {
					breakSuffocate(entity);
					continue;
				}

				if (entity.getLocation().getBlock() != null && Methods.isWater(entity.getLocation().getBlock())) {
					breakSuffocate(entity);
					continue;
				}

				if (Methods.isRegionProtectedFromBuild(player, "Suffocate", entity.getLocation())) {
					remove(player);
					continue;
				}

				if (entity.getLocation().distance(player.getLocation()) >= range) {
					breakSuffocate(entity);
					continue;
				}

				if (Methods.isObstructed(player.getLocation(), entity.getLocation())) {
					breakSuffocate(entity);
					continue;
				}

				if (entity instanceof Player) {
					if (Commands.invincible.contains(((Player) entity).getName())) {
						breakSuffocate(entity);
						continue;
					}
					if (AvatarState.isAvatarState((Player) entity)) {
						breakSuffocate(entity);
						continue;
					}
				}

				if (entity.isDead()) {
					breakSuffocate(entity);
					continue;
				}

				if (entity instanceof Creature) {
					((Creature) entity).setTarget(player);
				}

				for(Location airsphere : Methods.getCircle(entity.getLocation(), 3, 3, false, true, 0)) {
					Methods.playAirbendingParticles(airsphere, 1);
					if (Methods.rand.nextInt(4) == 0) {
						Methods.playAirbendingSound(airsphere);
					}		
				}
				entity.setFallDistance(0);
				new TempPotionEffect((LivingEntity) entity, slow);
				new TempPotionEffect((LivingEntity) entity, nausea);
				if (System.currentTimeMillis() >= time + warmup) {
					Methods.damageEntity(player, entity, damage);
					entity.setVelocity(new Vector(0, 0, 0));
				}
			}
		} catch (ConcurrentModificationException e) {

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
			if (instances.get(player).targets.containsKey(entity)) {
				instances.get(player).targets.remove(entity);
			}
		}
	}

	public static boolean isBreathbent(Entity entity) {
		for (Player player : instances.keySet()) {
			if (instances.get(player).targets.containsKey(entity)) {
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

	public static boolean isChannelingSphere(Player player){
		if(instances.containsKey(player)) return true;
		return false;
	}

	public static void removeAll() {
		instances.clear();
	}

}
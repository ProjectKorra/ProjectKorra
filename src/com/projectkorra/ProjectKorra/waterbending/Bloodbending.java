package com.projectkorra.ProjectKorra.waterbending;

import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.Objects.HorizontalVelocityTracker;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.TempPotionEffect;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Bloodbending {

	public static ConcurrentHashMap<Player, Bloodbending> instances = new ConcurrentHashMap<Player, Bloodbending>();

	ConcurrentHashMap<Entity, Location> targetentities = new ConcurrentHashMap<Entity, Location>();

	private static final double FACTOR = ProjectKorra.plugin.getConfig().getDouble("Abilities.Water.Bloodbending.ThrowFactor");
	private static final boolean onlyUsableAtNight = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Water.Bloodbending.CanOnlyBeUsedAtNight");
	private static boolean canBeUsedOnUndead = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Water.Bloodbending.CanBeUsedOnUndeadMobs");
	private static final boolean onlyUsableDuringMoon = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Water.Bloodbending.CanOnlyBeUsedDuringFullMoon");
	
	private int RANGE = ProjectKorra.plugin.getConfig().getInt("Abilities.Water.Bloodbending.Range");
	private long HOLD_TIME = ProjectKorra.plugin.getConfig().getInt("Abilities.Water.Bloodbending.HoldTime");
	private long COOLDOWN = ProjectKorra.plugin.getConfig().getInt("Abilities.Water.Bloodbending.Cooldown");
	
	private Player player;
	private long time;
	private double factor = FACTOR;
	private int range = RANGE;
	private long holdTime = HOLD_TIME;
	private long cooldown = COOLDOWN;

	public Bloodbending(Player player) {
		if (instances.containsKey(player)) {
			remove(player);
			return;
		}
		
		
		if (onlyUsableAtNight && !Methods.isNight(player.getWorld()) && !Methods.canBloodbendAtAnytime(player)) {
			return;
		}
		
		if (onlyUsableDuringMoon && !Methods.isFullMoon(player.getWorld()) && !Methods.canBloodbendAtAnytime(player)) {
			return;
		}
		
		BendingPlayer bplayer = Methods.getBendingPlayer(player.getName());
		if (bplayer.isOnCooldown("Bloodbending") && !AvatarState.isAvatarState(player)) {
			return;
		}

		range = (int) Methods.waterbendingNightAugment(range, player.getWorld());
		if (AvatarState.isAvatarState(player)) {
			range = AvatarState.getValue(range);
			for (Entity entity : Methods.getEntitiesAroundPoint(player.getLocation(), range)) {
				if (entity instanceof LivingEntity) {
					if (entity instanceof Player) {
						if (Methods.isRegionProtectedFromBuild(player, "Bloodbending", entity.getLocation())
								|| (AvatarState.isAvatarState((Player) entity)
										|| entity.getEntityId() == player.getEntityId()
										|| Methods.canBend(((Player) entity).getName(), "Bloodbending")))
							continue;
					}
					Methods.damageEntity(player, entity, 0);
					Methods.breakBreathbendingHold(entity);
					targetentities.put(entity, entity.getLocation().clone());
				}
			}
		} else {
			Entity target = Methods.getTargetedEntity(player, range, new ArrayList<Entity>());
			if (target == null)
				return;
			if (!(target instanceof LivingEntity)|| Methods.isRegionProtectedFromBuild(player,
					"Bloodbending", target.getLocation()))
				return;
			if (target instanceof Player) {
				if (Methods.canBend(((Player) target).getName(), "Bloodbending")
						|| AvatarState.isAvatarState((Player) target))
					return;
			}
			if (!canBeUsedOnUndead && isUndead(target)) {
				return;
			}
			Methods.damageEntity(player, target, 0);
			Methods.breakBreathbendingHold(target);
			targetentities.put(target, target.getLocation().clone());
		}
		if (targetentities.size() > 0) {
			bplayer.addCooldown("Bloodbending", cooldown);
		}
		this.player = player;
		this.time = System.currentTimeMillis();
		instances.put(player, this);
	}

	public static void launch(Player player) {
		if (instances.containsKey(player))
			instances.get(player).launch();
	}

	private void launch() {
		Location location = player.getLocation();
		for (Entity entity : targetentities.keySet()) {
			double dx, dy, dz;
			Location target = entity.getLocation().clone();
//			dx = target.getX() - location.getX();
//			dy = target.getY() - location.getY();
//			dz = target.getZ() - location.getZ();
//			Vector vector = new Vector(dx, dy, dz);
			Vector vector = Methods.getDirection(location, Methods.getTargetedLocation(player, location.distance(target)));
			vector.normalize();
			entity.setVelocity(vector.multiply(factor));
			new HorizontalVelocityTracker(entity, player, 200);
		}
		remove(player);
	}

	private void progress() {
		PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 60, 1);

		if (!player.isSneaking()) {
			remove(player);
			return;
		}
		
		if (holdTime > 0 && System.currentTimeMillis() - this.time > holdTime) {
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
		
		if (onlyUsableDuringMoon && !Methods.isFullMoon(player.getWorld())) {
			remove(player);
			return;
		}

		if (onlyUsableAtNight && !Methods.isNight(player.getWorld())) {
			remove(player);
			return;
		}

		if (!Methods.canBend(player.getName(), "Bloodbending")) {
			remove(player);
			return;
		}
		if (Methods.getBoundAbility(player) == null) {
			remove(player);
			return;
		}
		if (!Methods.getBoundAbility(player).equalsIgnoreCase("Bloodbending")) {
			remove(player);
			return;
		}

		if (AvatarState.isAvatarState(player)) {
			ArrayList<Entity> entities = new ArrayList<Entity>();
			for (Entity entity : Methods.getEntitiesAroundPoint(player.getLocation(), range)) {
				if (Methods.isRegionProtectedFromBuild(player, "Bloodbending", entity.getLocation()))
					continue;
				if (entity instanceof Player) {
					if (!Methods.canBeBloodbent((Player) entity))
						continue;
				}
				entities.add(entity);
				if (!targetentities.containsKey(entity)	&& entity instanceof LivingEntity) {
					Methods.damageEntity(player, entity, 0);
					targetentities.put(entity, entity.getLocation().clone());
				}
				if (entity instanceof LivingEntity) {
					Location newlocation = entity.getLocation().clone();
					Location location = targetentities.get(entity);
					double distance = location.distance(newlocation);
					double dx, dy, dz;
					dx = location.getX() - newlocation.getX();
					dy = location.getY() - newlocation.getY();
					dz = location.getZ() - newlocation.getZ();
					Vector vector = new Vector(dx, dy, dz);
					if (distance > .5) {
						entity.setVelocity(vector.normalize().multiply(.5));
					} else {
						entity.setVelocity(new Vector(0, 0, 0));
					}
					new TempPotionEffect((LivingEntity) entity, effect);
					entity.setFallDistance(0);
					if (entity instanceof Creature) {
						((Creature) entity).setTarget(null);
					}
					Methods.breakBreathbendingHold(entity);
				}
			}
			for (Entity entity : targetentities.keySet()) {
				if (!entities.contains(entity))
					targetentities.remove(entity);
			}
		} else {
			for (Entity entity : targetentities.keySet()) {
				if (entity instanceof Player) {
					if (!Methods.canBeBloodbent((Player) entity)) {
						targetentities.remove(entity);
						continue;
					}
				}
				Location newlocation = entity.getLocation();
				if (player.getWorld() != newlocation.getWorld()) {
					targetentities.remove(entity);
					continue;
				}
				Location location = Methods.getTargetedLocation(player,
						6 /*(int) targetentities.get(entity).distance(player.getLocation())*/);
				double distance = location.distance(newlocation);
				double dx, dy, dz;
				dx = location.getX() - newlocation.getX();
				dy = location.getY() - newlocation.getY();
				dz = location.getZ() - newlocation.getZ();
				Vector vector = new Vector(dx, dy, dz);
				if (distance > .5) {
					entity.setVelocity(vector.normalize().multiply(.5));
				} else {
					entity.setVelocity(new Vector(0, 0, 0));
				}
				new TempPotionEffect((LivingEntity) entity, effect);
				entity.setFallDistance(0);
				if (entity instanceof Creature) {
					((Creature) entity).setTarget(null);
				}
				Methods.breakBreathbendingHold(entity);
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

	public static boolean isBloodbended(Entity entity) {
		for (Player player : instances.keySet()) {
			if (instances.get(player).targetentities.containsKey(entity)) {
				// if (entity instanceof Player) {
				// if (!Methods.canBeBloodbent((Player) entity))
				// return false;
				// }
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

	public static Location getBloodbendingLocation(Entity entity) {
		for (Player player : instances.keySet()) {
			if (instances.get(player).targetentities.containsKey(entity)) {
				return instances.get(player).targetentities.get(entity);
			}
		}
		return null;
	}

	public Player getPlayer() {
		return player;
	}

	public double getFactor() {
		return factor;
	}

	public void setFactor(double factor) {
		this.factor = factor;
	}

	public int getRange() {
		return range;
	}

	public void setRange(int range) {
		this.range = range;
	}

	public long getHoldTime() {
		return holdTime;
	}

	public void setHoldTime(long holdTime) {
		this.holdTime = holdTime;
	}

	public long getCooldown() {
		return cooldown;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
		if(player != null)
			Methods.getBendingPlayer(player.getName()).addCooldown("Bloodbending", cooldown);
	}

}
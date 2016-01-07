package com.projectkorra.projectkorra.waterbending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.SubElement;
import com.projectkorra.projectkorra.ability.AvatarState;
import com.projectkorra.projectkorra.airbending.AirMethods;
import com.projectkorra.projectkorra.firebending.FireMethods;
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.util.TempPotionEffect;

import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Bloodbending {

	public static ConcurrentHashMap<Player, Bloodbending> instances = new ConcurrentHashMap<Player, Bloodbending>();

	ConcurrentHashMap<Entity, Location> targetentities = new ConcurrentHashMap<Entity, Location>();

	private static final double FACTOR = ProjectKorra.plugin.getConfig().getDouble("Abilities.Water.Bloodbending.ThrowFactor");
	private static final boolean onlyUsableAtNight = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Water.Bloodbending.CanOnlyBeUsedAtNight");
	private static boolean canBeUsedOnUndead = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Water.Bloodbending.CanBeUsedOnUndeadMobs");
	private static final boolean onlyUsableDuringMoon = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Water.Bloodbending.CanOnlyBeUsedDuringFullMoon");
	private boolean canBloodbendBloodbenders = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Water.Bloodbending.CanBloodbendOtherBloodbenders");
	
	private int RANGE = ProjectKorra.plugin.getConfig().getInt("Abilities.Water.Bloodbending.Range");
	private long HOLD_TIME = ProjectKorra.plugin.getConfig().getInt("Abilities.Water.Bloodbending.HoldTime");
	private long COOLDOWN = ProjectKorra.plugin.getConfig().getInt("Abilities.Water.Bloodbending.Cooldown");
	
	private Player player;
	private long time;
	private double factor = FACTOR;
	private int range = RANGE;
	private long holdTime = HOLD_TIME;
	private long cooldown = COOLDOWN;
	
	private Integer[] transparent = {0, 6, 8, 9, 10, 11, 27, 28, 30, 31, 32, 
			37, 38, 39, 40, 50, 51, 55, 59, 63, 64, 
			65, 66, 68, 69, 70, 71, 72, 75, 76, 77, 
			78, 83, 93, 94, 104, 105, 111, 115, 117, 
			132, 141, 142, 143, 147, 148, 149, 150, 
			157, 175, 176, 177, 183, 184, 185, 187, 
			193, 194, 195, 196, 197};

	public Bloodbending(Player player) {
		if (instances.containsKey(player)) {
			remove(player);
			return;
		}
		if (onlyUsableAtNight && !WaterMethods.isNight(player.getWorld()) && !WaterMethods.canBloodbendAtAnytime(player)) {
			return;
		}

		if (onlyUsableDuringMoon && !WaterMethods.isFullMoon(player.getWorld()) && !WaterMethods.canBloodbendAtAnytime(player)) {
			return;
		}

		BendingPlayer bplayer = GeneralMethods.getBendingPlayer(player.getName());
		if (bplayer.isOnCooldown("Bloodbending") && !AvatarState.isAvatarState(player)) {
			return;
		}

		range = (int) WaterMethods.waterbendingNightAugment(range, player.getWorld());
		if (AvatarState.isAvatarState(player)) {
			range += AvatarState.getValue(1.5);
			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(player.getLocation(), range)) {
				if (entity instanceof LivingEntity) {
					if (entity instanceof Player) {
						if (GeneralMethods.isRegionProtectedFromBuild(player, "Bloodbending", entity.getLocation()) || (AvatarState.isAvatarState((Player) entity) || entity.getEntityId() == player.getEntityId() || GeneralMethods.canBend(((Player) entity).getName(), "Bloodbending")))
							continue;
					}
					GeneralMethods.damageEntity(player, entity, 0, "Bloodbending");
					AirMethods.breakBreathbendingHold(entity);
					targetentities.put(entity, entity.getLocation().clone());
				}
			}
		} else {
			List<Entity> entities = new ArrayList<Entity>();
			for (int i = 0; i < 6; i++) {
				Location location = GeneralMethods.getTargetedLocation(player, i, transparent);
				entities = GeneralMethods.getEntitiesAroundPoint(location, 1.7);
				if (entities.contains(player))
					entities.remove(player);
				if (entities != null && !entities.isEmpty() && !entities.contains(player)) {
					break;
				}
			}
			if (entities == null || entities.isEmpty()) {
				return;
			}
			Entity target = entities.get(0);
			//Entity target = GeneralMethods.getTargetedEntity(player, range, new ArrayList<Entity>());
			if (target == null)
				return;
			if (!(target instanceof LivingEntity) || GeneralMethods.isRegionProtectedFromBuild(player, "Bloodbending", target.getLocation()))
				return;
			if (target instanceof Player) {
				if ((GeneralMethods.canBend(((Player) target).getName(), "Bloodbending") && !canBloodbendBloodbenders) || AvatarState.isAvatarState((Player) target))
					if (!FireMethods.isDay(target.getWorld()) || WaterMethods.canBloodbendAtAnytime((Player) target))
						return;
			}
			if (!canBeUsedOnUndead && isUndead(target)) {
				return;
			}
			GeneralMethods.damageEntity(player, target, 0, "Bloodbending");
			HorizontalVelocityTracker.remove(target);
			AirMethods.breakBreathbendingHold(target);
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
			Location target = entity.getLocation().clone();
			Vector vector = GeneralMethods.getDirection(location, GeneralMethods.getTargetedLocation(player, location.distance(target)));
			vector.normalize();
			entity.setVelocity(vector.multiply(factor));
			new HorizontalVelocityTracker(entity, player, 200, "Bloodbending", Element.Water, SubElement.Bloodbending);
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
			for (Entity entity : targetentities.keySet()) {
				if (isUndead(entity)) {
					targetentities.remove(entity);
				}
			}
		}

		if (onlyUsableDuringMoon && !WaterMethods.isFullMoon(player.getWorld()) && !WaterMethods.canBloodbendAtAnytime(player)) {
			remove(player);
			return;
		}

		if (onlyUsableAtNight && !WaterMethods.isNight(player.getWorld()) && !WaterMethods.canBloodbendAtAnytime(player)) {
			remove(player);
			return;
		}

		if (!GeneralMethods.canBend(player.getName(), "Bloodbending")) {
			remove(player);
			return;
		}
		if (GeneralMethods.getBoundAbility(player) == null) {
			remove(player);
			return;
		}
		if (!GeneralMethods.getBoundAbility(player).equalsIgnoreCase("Bloodbending")) {
			remove(player);
			return;
		}

		if (AvatarState.isAvatarState(player)) {
			ArrayList<Entity> entities = new ArrayList<Entity>();
			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(player.getLocation(), range)) {
				if (GeneralMethods.isRegionProtectedFromBuild(player, "Bloodbending", entity.getLocation()))
					continue;
				if (entity instanceof Player) {
					if (!WaterMethods.canBeBloodbent((Player) entity) || entity.getEntityId() == player.getEntityId())
						continue;
				}
				entities.add(entity);
				if (!targetentities.containsKey(entity) && entity instanceof LivingEntity) {
					GeneralMethods.damageEntity(player, entity, 0, "Bloodbending");
					targetentities.put(entity, entity.getLocation().clone());
				}
				if (entity instanceof LivingEntity) {
					Location newlocation = entity.getLocation();
					if (player.getWorld() != newlocation.getWorld()) {
						targetentities.remove(entity);
						continue;
					}
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
					AirMethods.breakBreathbendingHold(entity);
				}
			}
			for (Entity entity : targetentities.keySet()) {
				if (!entities.contains(entity))
					targetentities.remove(entity);
			}
		} else {
			for (Entity entity : targetentities.keySet()) {
				if (entity instanceof Player) {
					if (!WaterMethods.canBeBloodbent((Player) entity)) {
						targetentities.remove(entity);
						continue;
					}
				}
				Location newlocation = entity.getLocation();
				if (player.getWorld() != newlocation.getWorld()) {
					targetentities.remove(entity);
					continue;
				}
				Location location = GeneralMethods.getTargetedLocation(player, 6, transparent);
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
				AirMethods.breakBreathbendingHold(entity);
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
				return true;
			}
		}
		return false;
	}

	public static boolean isUndead(Entity entity) {
		if (entity == null)
			return false;
		if (entity.getType() == EntityType.ZOMBIE || entity.getType() == EntityType.BLAZE || entity.getType() == EntityType.GIANT || entity.getType() == EntityType.IRON_GOLEM || entity.getType() == EntityType.MAGMA_CUBE || entity.getType() == EntityType.PIG_ZOMBIE || entity.getType() == EntityType.SKELETON || entity.getType() == EntityType.SLIME || entity.getType() == EntityType.SNOWMAN || entity.getType() == EntityType.ZOMBIE) {
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
		if (player != null)
			GeneralMethods.getBendingPlayer(player.getName()).addCooldown("Bloodbending", cooldown);
	}

}

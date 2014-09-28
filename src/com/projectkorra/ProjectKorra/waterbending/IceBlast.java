package com.projectkorra.ProjectKorra.waterbending;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.TempBlock;
import com.projectkorra.ProjectKorra.TempPotionEffect;
import com.projectkorra.ProjectKorra.Utilities.ParticleEffect;

public class IceBlast {
	
	private static ConcurrentHashMap<Integer, IceBlast> instances = new ConcurrentHashMap<Integer, IceBlast>();
	private static double defaultrange = ProjectKorra.plugin.getConfig().getInt("Abilities.Water.IceBlast.Range");
	private static int defaultdamage = ProjectKorra.plugin.getConfig().getInt("Abilities.Water.IceBlast.Damage");
	private static int ID = Integer.MIN_VALUE;
	
	private static final long interval = 20;
	private static final byte data = 0;
	private static final double affectingradius = 2;
	private static final double deflectrange = 3;
	
	private int id;
	private double range;
	private boolean prepared = false;
	private boolean settingup = false;
	private boolean progressing = false;
	private long time;
	private Location location;
	private Location firstdestination;
	private Location destination;
	private Block sourceblock;
	private Player player;
	private TempBlock source;
	
	public IceBlast(Player player) {
		block(player);
		range = Methods.waterbendingNightAugment(defaultrange, player.getWorld());
		this.player = player;
		Block sourceblock = Methods.getIceSourceBlock(player, range);

		if (sourceblock == null) {
			return;
        }else if (TempBlock.isTempBlock(sourceblock)) {
            return;
		} else {
			prepare(sourceblock);
		}
	}
	
	private void prepare(Block block) {
		for (IceBlast ice : getInstances(player)) {
			if (ice.prepared) {
				ice.cancel();
			}
		}
		sourceblock = block;
		location = sourceblock.getLocation();
		prepared = true;
		createInstance();
	}
	
	private void createInstance() {
		id = ID++;
		instances.put(id, this);
		if (ID >= Integer.MAX_VALUE) {
			ID = Integer.MIN_VALUE;
		}
	}
	
	private static ArrayList<IceBlast> getInstances(Player player) {
		ArrayList<IceBlast> list = new ArrayList<IceBlast>();
		for (int id : instances.keySet()) {
			IceBlast ice = instances.get(id);
			if (ice.player.equals(player)) {
				list.add(ice);
			}
		}

		return list;
	}
	
	private static void block(Player player) {
		for (int id : instances.keySet()) {
			IceBlast ice = instances.get(id);

			if (ice.player.equals(player))
				continue;

			if (!ice.location.getWorld().equals(player.getWorld()))
				continue;

			if (!ice.progressing)
				continue;

			if (Methods.isRegionProtectedFromBuild(player, "IceBlast", ice.location))
				continue;

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = ice.location;
			if (mloc.distance(location) <= defaultrange
					&& Methods.getDistanceFromLine(vector, location, ice.location) < deflectrange
					&& mloc.distance(location.clone().add(vector)) < 
					mloc.distance(location.clone().add(vector.clone().multiply(-1)))) {
				ice.cancel();
			}

		}
	}
	
	public static void activate(Player player) {

		BendingPlayer bPlayer = Methods.getBendingPlayer(player.getName());

		if (bPlayer.isOnCooldown("IceBlast")) return;

		for (IceBlast ice : getInstances(player)) {
			if (ice.prepared) {
				ice.throwIce();
			}
		}
	}
	
	private void cancel() {
		if (progressing) {
			if (source != null)
				source.revertBlock();
			progressing = false;
		}

		instances.remove(id);
	}
	
	private void returnWater() {
		new WaterReturn(player, sourceblock);
	}
	
	public static void removeAll() {
		for (int id : instances.keySet()) {
			instances.get(id).cancel();
		}

		instances.clear();
	}
	
	private void affect(LivingEntity entity) {
		int damage = (int) Methods.waterbendingNightAugment(defaultdamage, player.getWorld());
		if (entity instanceof Player) {
			BendingPlayer bPlayer = Methods.getBendingPlayer(player.getName());
			if (bPlayer.canBeSlowed()) {
				PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 70, 2);
				new TempPotionEffect(entity, effect);
				bPlayer.slow(10);
				entity.damage(damage, player);
			}
		} else {
			PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 70, 2);
			new TempPotionEffect(entity, effect);
			entity.damage(damage, player);
		}
		Methods.breakBreathbendingHold(entity);
		
		for(Location loc : Methods.getCircle(entity.getLocation(), 6, 7, false, false, 0)) {
			ParticleEffect.SNOW_SHOVEL.display(loc, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0, 10);
		}
	}
	
	private void throwIce() {
		if (!prepared)
			return;
		LivingEntity target = (LivingEntity) Methods.getTargetedEntity(player, range, new ArrayList<Entity>());
		if (target == null) {
			destination = Methods.getTargetedLocation(player, range, Methods.transparentToEarthbending);
		} else {
			destination = target.getEyeLocation();
		}

		location = sourceblock.getLocation();
		if (destination.distance(location) < 1)
			return;
		firstdestination = location.clone();
		if (destination.getY() - location.getY() > 2) {
			firstdestination.setY(destination.getY() - 1);
		} else {
			firstdestination.add(0, 2, 0);
		}
		destination = Methods.getPointOnLine(firstdestination, destination, range);
		progressing = true;
		settingup = true;
		prepared = false;
		
		new TempBlock(sourceblock, Material.AIR, (byte) 0);

		source = new TempBlock(sourceblock, Material.PACKED_ICE, data);
	}
	
	private void progress() {
		if (player.isDead() || !player.isOnline() || !Methods.canBend(player.getName(), "IceBlast")) {
			cancel();
			return;
		}

		if (!player.getWorld().equals(location.getWorld())) {
			cancel();
			return;
		}

		if (player.getEyeLocation().distance(location) >= range) {
			if (progressing) {
				cancel();
				returnWater();
			} else {
				cancel();
			}
			return;
		}

		if ((Methods.getBoundAbility(player) == null || !Methods.getBoundAbility(player).equalsIgnoreCase("IceBlast")) && prepared) {
			cancel();
			return;
		}

		if (System.currentTimeMillis() < time + interval)
			return;

		time = System.currentTimeMillis();

		if (progressing) {

			Vector direction;

			if (location.getBlockY() == firstdestination.getBlockY())
				settingup = false;

			if (location.distance(destination) <= 2) {
				cancel();
				returnWater();
				return;
			}

			if (settingup) {
				direction = Methods.getDirection(location, firstdestination).normalize();
			} else {
				direction = Methods.getDirection(location, destination).normalize();
			}

			location.add(direction);

			Block block = location.getBlock();

			if (block.equals(sourceblock))
				return;

			source.revertBlock();
			source = null;

			if (Methods.isTransparentToEarthbending(player, block) && !block.isLiquid()) {
				Methods.breakBlock(block);
			} else if (!Methods.isWater(block)) {
				cancel();
				returnWater();
				return;
			}

			if (Methods.isRegionProtectedFromBuild(player, "IceBlast", location)) {
				cancel();
				returnWater();
				return;
			}

			for (Entity entity : Methods.getEntitiesAroundPoint(location, affectingradius)) {
				if (entity.getEntityId() != player.getEntityId() && entity instanceof LivingEntity) {
					affect((LivingEntity) entity);
					progressing = false;
					returnWater();
				}
			}

			if (!progressing) {
				cancel();
				return;
			}

			sourceblock = block;
			source = new TempBlock(sourceblock, Material.PACKED_ICE, data);
			
			ParticleEffect.SNOWBALL_POOF.display(location, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0, 100);
			ParticleEffect.SNOW_SHOVEL.display(location, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0, 100);
			if (Methods.rand.nextInt(4) == 0) {
				Methods.playFirebendingSound(location);
			}
			location = location.add(direction.clone());

		} else if (prepared) {
			Methods.playFocusWaterEffect(sourceblock);
		}
	}
	
	public static void progressAll() {
		for (int id : instances.keySet()) {
			instances.get(id).progress();
		}
	}

}

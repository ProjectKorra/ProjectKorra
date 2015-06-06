package com.projectkorra.ProjectKorra.waterbending;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.TempBlock;
import com.projectkorra.ProjectKorra.TempPotionEffect;
import com.projectkorra.ProjectKorra.Utilities.BlockSource;
import com.projectkorra.ProjectKorra.Utilities.ClickType;
import com.projectkorra.ProjectKorra.Utilities.ParticleEffect;
import com.projectkorra.ProjectKorra.airbending.AirMethods;
import com.projectkorra.ProjectKorra.earthbending.EarthMethods;

public class IceBlast {
	
	public static ConcurrentHashMap<Integer, IceBlast> instances = new ConcurrentHashMap<Integer, IceBlast>();
	private static double defaultrange = ProjectKorra.plugin.getConfig().getDouble("Abilities.Water.IceBlast.Range");
	private static int DAMAGE = ProjectKorra.plugin.getConfig().getInt("Abilities.Water.IceBlast.Damage");
	private static int COOLDOWN = ProjectKorra.plugin.getConfig().getInt("Abilities.Water.IceBlast.Cooldown");
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
	public TempBlock source;
	private double defaultdamage = DAMAGE;
	private long cooldown = COOLDOWN;
	
	public IceBlast(Player player) {
		if(!WaterMethods.canIcebend(player))
			return;
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
		if(bPlayer.isOnCooldown("IceBlast")) {
			return;
		}
		
		
		block(player);
		range = WaterMethods.waterbendingNightAugment(defaultrange, player.getWorld());
		this.player = player;
		Block sourceblock = BlockSource.getWaterSourceBlock(player, range, ClickType.SHIFT_DOWN, false, true, false);

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
		if(getInstances(player).isEmpty())
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

			if (GeneralMethods.isRegionProtectedFromBuild(player, "IceBlast", ice.location))
				continue;

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = ice.location;
			if (mloc.distance(location) <= defaultrange
					&& GeneralMethods.getDistanceFromLine(vector, location, ice.location) < deflectrange
					&& mloc.distance(location.clone().add(vector)) < 
					mloc.distance(location.clone().add(vector.clone().multiply(-1)))) {
				ice.cancel();
			}

		}
	}
	
	public static void activate(Player player) {

		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());

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
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
		bPlayer.addCooldown("IceBlast", cooldown);
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
		int damage = (int) WaterMethods.waterbendingNightAugment(defaultdamage, player.getWorld());
		if (entity instanceof Player) {
			BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
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
		AirMethods.breakBreathbendingHold(entity);
		
		for(int x = 0; x < 30; x++) {
			ParticleEffect.ITEM_CRACK.display(new ParticleEffect.ItemData(Material.ICE, (byte)0), new Vector(((Math.random()-0.5)*.5), ((Math.random() - 0.5)*.5), ((Math.random() - 0.5)*.5)), .3f, location, 257.0D);
		}
	}
	
	private void throwIce() {
		if (!prepared)
			return;
		LivingEntity target = (LivingEntity) GeneralMethods.getTargetedEntity(player, range, new ArrayList<Entity>());
		if (target == null) {
			destination = GeneralMethods.getTargetedLocation(player, range, EarthMethods.transparentToEarthbending);
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
		destination = GeneralMethods.getPointOnLine(firstdestination, destination, range);
		progressing = true;
		settingup = true;
		prepared = false;
		
		new TempBlock(sourceblock, Material.AIR, (byte) 0);

		source = new TempBlock(sourceblock, Material.PACKED_ICE, data);
	}
	
	private void progress() {
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
		if (player.isDead() || !player.isOnline() || !GeneralMethods.canBend(player.getName(), "IceBlast") || bPlayer.isOnCooldown("IceBlast")) {
			cancel();
			return;
		}

		if (!player.getWorld().equals(location.getWorld())) {
			cancel();
			return;
		}

		if (player.getEyeLocation().distance(location) >= range) {
			if (progressing) {
				breakParticles(20);
				cancel();
				returnWater();
			} else {
				breakParticles(20);
				cancel();
			}
			return;
		}

		if ((GeneralMethods.getBoundAbility(player) == null || !GeneralMethods.getBoundAbility(player).equalsIgnoreCase("IceBlast")) && prepared) {
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
				direction = GeneralMethods.getDirection(location, firstdestination).normalize();
			} else {
				direction = GeneralMethods.getDirection(location, destination).normalize();
			}

			location.add(direction);

			Block block = location.getBlock();

			if (block.equals(sourceblock))
				return;

			source.revertBlock();
			source = null;

			if (EarthMethods.isTransparentToEarthbending(player, block) && !block.isLiquid()) {
				GeneralMethods.breakBlock(block);
			} else if (!WaterMethods.isWater(block)) {
				breakParticles(20);
				cancel();
				returnWater();
				return;
			}

			if (GeneralMethods.isRegionProtectedFromBuild(player, "IceBlast", location)) {
				cancel();
				returnWater();
				return;
			}

			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, affectingradius)) {
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
			
			for(int x = 0; x < 10; x++) {
				ParticleEffect.ITEM_CRACK.display(new ParticleEffect.ItemData(Material.ICE, (byte)0), new Vector(((Math.random()-0.5)*.5), ((Math.random() - 0.5)*.5), ((Math.random() - 0.5)*.5)), .5f, location, 257.0D);
				ParticleEffect.SNOW_SHOVEL.display(location, (float) (Math.random()-0.5), (float) (Math.random()-0.5), (float) (Math.random()-0.5), 0, 5);
			}
			if (GeneralMethods.rand.nextInt(4) == 0) {
				WaterMethods.playIcebendingSound(location);
			}
			location = location.add(direction.clone());

		} else if (prepared) {
			WaterMethods.playFocusWaterEffect(sourceblock);
		}
	}
	
	public static void progressAll() {
		for (int id : instances.keySet()) {
			instances.get(id).progress();
		}
	}

	public Player getPlayer() {
		return player;
	}

	public double getDefaultdamage() {
		return defaultdamage;
	}

	public void setDefaultdamage(double defaultdamage) {
		this.defaultdamage = defaultdamage;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
		
	}

	public void breakParticles(int amount) {
		for(int x = 0; x < amount; x++) {
			ParticleEffect.ITEM_CRACK.display(new ParticleEffect.ItemData(Material.ICE, (byte)0), new Vector(((Math.random()-0.5)*.5), ((Math.random() - 0.5)*.5), ((Math.random() - 0.5)*.5)), 2f, location, 257.0D);
			ParticleEffect.SNOW_SHOVEL.display(location, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0, 2);
		}
			location.getWorld().playSound(location, Sound.GLASS, 5, 1.3f);
	}

}

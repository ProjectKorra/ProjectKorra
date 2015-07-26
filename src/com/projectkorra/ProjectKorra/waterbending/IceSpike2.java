package com.projectkorra.ProjectKorra.waterbending;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.TempBlock;
import com.projectkorra.ProjectKorra.TempPotionEffect;
import com.projectkorra.ProjectKorra.Utilities.BlockSource;
import com.projectkorra.ProjectKorra.Utilities.ClickType;
import com.projectkorra.ProjectKorra.airbending.AirMethods;
import com.projectkorra.ProjectKorra.earthbending.EarthMethods;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class IceSpike2 {

    public static ConcurrentHashMap<Integer, IceSpike2> instances = new ConcurrentHashMap<>();

	private static double RANGE = ProjectKorra.plugin.getConfig().getLong("Abilities.Water.IceSpike.Projectile.Range");
	private static double DAMAGE = ProjectKorra.plugin.getConfig().getLong("Abilities.Water.IceSpike.Projectile.Damage");
	private static int defaultmod = 2;
	private static int ID = Integer.MIN_VALUE;
	static long slowCooldown = 5000;

	private static final long interval = 20;
	private static final byte data = 0;
	private static final double affectingradius = 2;
	private static final double deflectrange = 3;

	private int id;
	private double range;
	private boolean plantbending = false;
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
	private double defaultrange = RANGE;
	private double defaultdamage = DAMAGE;

	public IceSpike2(Player player) {
		if(!WaterMethods.canIcebend(player))
			return;
		
		block(player);
		if (WaterMethods.canPlantbend(player))
			plantbending = true;
		range = WaterMethods.waterbendingNightAugment(defaultrange, player.getWorld());
		this.player = player;
		Block sourceblock = BlockSource.getWaterSourceBlock(player, range, ClickType.SHIFT_DOWN, 
				true, true, plantbending);

		if (sourceblock == null) {
			new SpikeField(player);
		} else {
			prepare(sourceblock);
		}

	}

	private void prepare(Block block) {
        getInstances(player).stream()
                .filter(ice -> ice.prepared)
                .forEach(IceSpike2::cancel);
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

	private static ArrayList<IceSpike2> getInstances(Player player) {
        ArrayList<IceSpike2> list = new ArrayList<>();
        for (int id : instances.keySet()) {
			IceSpike2 ice = instances.get(id);
			if (ice.player.equals(player)) {
				list.add(ice);
			}
		}

		return list;
	}

	public static void activate(Player player) {
		redirect(player);
		boolean activate = false;

		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());

		if (bPlayer.isOnCooldown("IceSpike")) return;

		for (IceSpike2 ice : getInstances(player)) {
			if (ice.prepared) {
				ice.throwIce();
				activate = true;
			}
		}

		if (!activate) {
			IceSpike spike = new IceSpike(player);
			if (spike.id == 0) {
				waterBottle(player);
			}
		}
	}

	@SuppressWarnings("deprecation")
	private static void waterBottle(Player player) {
		if (WaterReturn.hasWaterBottle(player)) {
			Location eyeloc = player.getEyeLocation();
			Block block = eyeloc.add(eyeloc.getDirection().normalize()).getBlock();
			if (EarthMethods.isTransparentToEarthbending(player, block)
					&& EarthMethods.isTransparentToEarthbending(player, eyeloc.getBlock())) {

                LivingEntity target = (LivingEntity) GeneralMethods.getTargetedEntity(player, RANGE, new ArrayList<>());
                Location destination;
				if (target == null) {
					destination = GeneralMethods.getTargetedLocation(player, RANGE, EarthMethods.transparentToEarthbending);
				} else {
					destination = GeneralMethods.getPointOnLine(player.getEyeLocation(), target.getEyeLocation(), RANGE);
				}

				if (destination.distance(block.getLocation()) < 1)
					return;

				block.setType(Material.WATER);
				block.setData((byte) 0x0);
				IceSpike2 ice = new IceSpike2(player);
				ice.throwIce();

				if (ice.progressing) {
					WaterReturn.emptyWaterBottle(player);
				} else {
					block.setType(Material.AIR);
				}

			}
		}
	}

	private void throwIce() {
		if (!prepared)
			return;
        LivingEntity target = (LivingEntity) GeneralMethods.getTargetedEntity(player, range, new ArrayList<>());
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

		if (WaterMethods.isPlant(sourceblock)) {
			new Plantbending(sourceblock);
			sourceblock.setType(Material.AIR);
		} else if (!GeneralMethods.isAdjacentToThreeOrMoreSources(sourceblock)) {
			sourceblock.setType(Material.AIR);
		}

		source = new TempBlock(sourceblock, Material.ICE, data);
	}

	public static void progressAll() {
		for (int id : instances.keySet()) {
			instances.get(id).progress();
		}
	}

	private void progress() {
		if (player.isDead() || !player.isOnline() || !GeneralMethods.canBend(player.getName(), "IceSpike")) {
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

        if ((GeneralMethods.getBoundAbility(player) == null
                || !GeneralMethods.getBoundAbility(player).equalsIgnoreCase("IceSpike"))
                && prepared) {
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
				cancel();
				returnWater();
				return;
			}

			if (GeneralMethods.isRegionProtectedFromBuild(player, "IceSpike", location)) {
				cancel();
				returnWater();
				return;
			}

            GeneralMethods.getEntitiesAroundPoint(location, affectingradius).stream()
                    .filter(entity -> entity.getEntityId() != player.getEntityId() && entity instanceof LivingEntity)
                    .forEach(entity -> {
                        affect((LivingEntity) entity);
                        progressing = false;
                        returnWater();
                    });

            if (GeneralMethods.rand.nextInt(4) == 0) {
				WaterMethods.playIcebendingSound(location);
			}		

			if (!progressing) {
				cancel();
				return;
			}

			sourceblock = block;
			source = new TempBlock(sourceblock, Material.ICE, data);

		} else if (prepared) {
			WaterMethods.playFocusWaterEffect(sourceblock);
		}
	}

	private void affect(LivingEntity entity) {
		int mod = (int) WaterMethods.waterbendingNightAugment(defaultmod, player.getWorld());
		int damage = (int) WaterMethods.waterbendingNightAugment(defaultdamage, player.getWorld());
		if (entity instanceof Player) {
			BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
			if (bPlayer.canBeSlowed()) {
				PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 70, mod);
				new TempPotionEffect(entity, effect);
				bPlayer.slow(slowCooldown);
				entity.damage(damage, player);
			}
		} else {
			PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 70, mod);
			new TempPotionEffect(entity, effect);
			entity.damage(damage, player);
		}
		AirMethods.breakBreathbendingHold(entity);

	}

	private static void redirect(Player player) {

		for (int id : instances.keySet()) {
			IceSpike2 ice = instances.get(id);

			if (!ice.progressing)
				continue;

			if (!ice.location.getWorld().equals(player.getWorld()))
				continue;

			if (ice.player.equals(player)) {
				Location location;
                Entity target = GeneralMethods.getTargetedEntity(player, ice.defaultrange, new ArrayList<>());
                if (target == null) {
					location = GeneralMethods.getTargetedLocation(player, ice.defaultrange);
				} else {
					location = ((LivingEntity) target).getEyeLocation();
				}
				location = GeneralMethods.getPointOnLine(ice.location, location, ice.defaultrange * 2);
				ice.redirect(location, player);
			}

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = ice.location;
			if (GeneralMethods.isRegionProtectedFromBuild(player, "IceSpike", mloc))
				continue;
			if (mloc.distance(location) <= ice.defaultrange
					&& GeneralMethods.getDistanceFromLine(vector, location, ice.location) < deflectrange
					&& mloc.distance(location.clone().add(vector)) < 
					mloc.distance(location.clone().add(vector.clone().multiply(-1)))) {
				Location loc;
                Entity target = GeneralMethods.getTargetedEntity(player, ice.defaultrange, new ArrayList<>());
                if (target == null) {
					loc = GeneralMethods.getTargetedLocation(player, ice.defaultrange);
				} else {
					loc = ((LivingEntity) target).getEyeLocation();
				}
				loc = GeneralMethods.getPointOnLine(ice.location, loc, ice.defaultrange * 2);
				ice.redirect(loc, player);
			}

		}
	}

	private static void block(Player player) {
		for (int id : instances.keySet()) {
			IceSpike2 ice = instances.get(id);

			if (ice.player.equals(player))
				continue;

			if (!ice.location.getWorld().equals(player.getWorld()))
				continue;

			if (!ice.progressing)
				continue;

			if (GeneralMethods.isRegionProtectedFromBuild(player, "IceSpike",
					ice.location))
				continue;

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = ice.location;
			if (mloc.distance(location) <= ice.defaultrange
					&& GeneralMethods.getDistanceFromLine(vector, location, ice.location) < deflectrange
					&& mloc.distance(location.clone().add(vector)) < 
					mloc.distance(location.clone().add(vector.clone().multiply(-1)))) {
				ice.cancel();
			}

		}
	}

	private void redirect(Location destination, Player player) {
		this.destination = destination;
		this.player = player;
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

	public static boolean isBending(Player player) {
		for (int id : instances.keySet()) {
			if (instances.get(id).player.equals(player))
				return true;
		}
		return false;
	}

	public Player getPlayer() {
		return player;
	}

	public double getDefaultrange() {
		return defaultrange;
	}

	public void setDefaultrange(double defaultrange) {
		this.defaultrange = defaultrange;
	}

	public double getDefaultdamage() {
		return defaultdamage;
	}

	public void setDefaultdamage(double defaultdamage) {
		this.defaultdamage = defaultdamage;
	}
}
package com.projectkorra.projectkorra.object;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.StockAbility;
import com.projectkorra.projectkorra.earthbending.EarthMethods;
import com.projectkorra.projectkorra.event.HorizontalVelocityChangeEvent;
import com.projectkorra.projectkorra.waterbending.WaterMethods;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Carbogen on 2/2/2015.
 */
public class HorizontalVelocityTracker {
	
	public static ConcurrentHashMap<Entity, HorizontalVelocityTracker> instances = new ConcurrentHashMap<Entity, HorizontalVelocityTracker>();
	public boolean hasBeenDamaged = false;
	private long delay;
	private long fireTime;
	private Entity entity;
	private Player instigator;
	private Vector lastVelocity;
	private Vector thisVelocity;
	private Location launchLocation;
	private Location impactLocation;
	private StockAbility abil;
	
	public static String[] abils = {"AirBlast", "AirBurst", "AirSuction", "Bloodbending"};

	public HorizontalVelocityTracker(Entity e, Player instigator, long delay, StockAbility ability) {
		remove(e);
		entity = e;
		this.instigator = instigator;
		fireTime = System.currentTimeMillis();
		this.delay = delay;
		thisVelocity = e.getVelocity().clone();
		launchLocation = e.getLocation().clone();
		impactLocation = launchLocation.clone();
		this.delay = delay;
		abil = ability;
		update();
		instances.put(entity, this);
	}

	public void update() {
		if (System.currentTimeMillis() < fireTime + delay) {
			return;
		}

		lastVelocity = thisVelocity.clone();
		thisVelocity = entity.getVelocity().clone();

		Vector diff = thisVelocity.subtract(lastVelocity);

		List<Block> blocks = GeneralMethods.getBlocksAroundPoint(entity.getLocation(), 1.5);

		for (Block b : blocks) {
			if (WaterMethods.isWater(b)) {
				remove();
				return;
			}
		}
		
		if (thisVelocity.length() < lastVelocity.length()) {
			if ((diff.getX() > 1 || diff.getX() < -1) || (diff.getZ() > 1 || diff.getZ() < -1)) {
				impactLocation = entity.getLocation();
				for (Block b : blocks) {
					if (GeneralMethods.isSolid(b) && (entity.getLocation().getBlock().getRelative(BlockFace.EAST, 1).equals(b) || entity.getLocation().getBlock().getRelative(BlockFace.NORTH, 1).equals(b) || entity.getLocation().getBlock().getRelative(BlockFace.WEST, 1).equals(b) || entity.getLocation().getBlock().getRelative(BlockFace.SOUTH, 1).equals(b))) {
						if (!EarthMethods.isTransparentToEarthbending(instigator, b)) {
							hasBeenDamaged = true;
							ProjectKorra.plugin.getServer().getPluginManager().callEvent(new HorizontalVelocityChangeEvent(entity, instigator, lastVelocity, thisVelocity, diff, launchLocation, impactLocation, abil));
							remove();
							return;
						}
					}
				}
			}
		}
	}

	public static void updateAll() {
		for (Entity e : instances.keySet())
			instances.get(e).update();
	}

	public void remove() {
		instances.remove(entity);
	}

	public static void remove(Entity e) {
		if (instances.containsKey(e))
			instances.remove(e);
	}
	
	public static boolean hasBeenDamagedByHorizontalVelocity(Entity e) {
		if (instances.containsKey(e)) {
			return instances.get(e).hasBeenDamaged;
		}
		return false;
	}
}

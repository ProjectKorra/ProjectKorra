package com.projectkorra.projectkorra.object;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.event.HorizontalVelocityChangeEvent;

/**
 * Created by Carbogen on 2/2/2015.
 */
public class HorizontalVelocityTracker {
	
	public static Map<Entity, HorizontalVelocityTracker> instances = new ConcurrentHashMap<Entity, HorizontalVelocityTracker>();
	public boolean hasBeenDamaged = false;
	public boolean barrier = ConfigManager.defaultConfig.get().getBoolean("Properties.HorizontalCollisionPhysics.DamageOnBarrierBlock");
	private long delay;
	private long fireTime;
	private Entity entity;
	private Player instigator;
	private Vector lastVelocity;
	private Vector thisVelocity;
	private Location launchLocation;
	private Location impactLocation;
	private Ability abil;
	
	public static String[] abils = {"AirBlast", "AirBurst", "AirSuction", "Bloodbending"};

	public HorizontalVelocityTracker(Entity e, Player instigator, long delay, Ability ability) {
		if (!ProjectKorra.plugin.getConfig().getBoolean("Properties.HorizontalCollisionPhysics.Enabled"))
			return;

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
		
		if (entity.isOnGround()) {
			remove();
			return;
		}

		lastVelocity = thisVelocity.clone();
		thisVelocity = entity.getVelocity().clone();

		Vector diff = thisVelocity.subtract(lastVelocity);

		List<Block> blocks = GeneralMethods.getBlocksAroundPoint(entity.getLocation(), 1.5);

		for (Block b : blocks) {
			if (WaterAbility.isWater(b)) {
				remove();
				return;
			}
		}
		
		if (thisVelocity.length() < lastVelocity.length()) {
			if ((diff.getX() > 1 || diff.getX() < -1) || (diff.getZ() > 1 || diff.getZ() < -1)) {
				impactLocation = entity.getLocation();
				for (Block b : blocks) {
					if (b.getType() == Material.BARRIER && barrier == false) return;
					if (GeneralMethods.isSolid(b) && (entity.getLocation().getBlock().getRelative(BlockFace.EAST, 1).equals(b) || entity.getLocation().getBlock().getRelative(BlockFace.NORTH, 1).equals(b) || entity.getLocation().getBlock().getRelative(BlockFace.WEST, 1).equals(b) || entity.getLocation().getBlock().getRelative(BlockFace.SOUTH, 1).equals(b))) {
						if (!ElementalAbility.isTransparent(instigator, b)) {
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
			if (e != null) {
				instances.get(e).update();
			} else {
				instances.remove(e);
			}
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

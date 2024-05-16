package com.projectkorra.projectkorra.object;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.projectkorra.projectkorra.ability.CoreAbility;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
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
	private CoreAbility abil;

	public static String[] abils = { "AirBlast", "AirBurst", "AirSuction", "Bloodbending" };

	public HorizontalVelocityTracker(final Entity e, final Player instigator, final long delay, final CoreAbility ability) {
		if (!ProjectKorra.plugin.getConfig().getBoolean("Properties.HorizontalCollisionPhysics.Enabled")) {
			return;
		}

		if (instances.containsKey(e)) {
			return;
		}

		remove(e);
		this.entity = e;
		this.instigator = instigator;
		this.fireTime = System.currentTimeMillis();
		this.delay = delay;
		this.thisVelocity = e.getVelocity().clone();
		this.launchLocation = e.getLocation().clone();
		this.impactLocation = this.launchLocation.clone();
		this.abil = ability;
		this.update();
		instances.put(this.entity, this);
	}

	public void update() {
		if (System.currentTimeMillis() < this.fireTime + this.delay) {
			return;
		}

		// We want to see if the entity actually has applied velocity. It's a bit difficult to assess
		// this, so the way here is just an approximation, it's not always precise; the tracker won't be removed.
		if (entity.isOnGround() && (this.thisVelocity.length() < 0.8 || entity.getVelocity().getX() == 0.0 || entity.getVelocity().getZ() == 0.0)) {
			this.remove();
			return;
		}

		if (System.currentTimeMillis() > (this.fireTime + 30000)) {
			ProjectKorra.log.info("removed HorizontalVelocityTracker lasting over 30 seconds: " + this.instigator.getName() + " using " + this.abil.getName() + " on " + this.entity);
			this.remove();
			return;
		}

		this.lastVelocity = this.thisVelocity.clone();
		this.thisVelocity = this.entity.getVelocity().clone();

		final Vector diff = this.thisVelocity.subtract(this.lastVelocity);

		if (this.thisVelocity.length() < this.lastVelocity.length()) {
			if ((diff.getX() > 0 || diff.getX() < 0) || (diff.getZ() > 0 || diff.getZ() < 0)) {
				this.impactLocation = this.entity.getLocation();

				if (didHitWall((LivingEntity) this.entity)) {
					this.hasBeenDamaged = true;
					ProjectKorra.plugin.getServer().getPluginManager().callEvent(new HorizontalVelocityChangeEvent(this.entity, this.instigator, this.lastVelocity, this.thisVelocity, diff, this.launchLocation, this.impactLocation, this.abil));
					this.remove();
					return;
				}
			}
		}
	}

	private boolean didHitWall(LivingEntity entity) {
		BlockFace[] faces = { BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST };

		for (int i = 0; i < 2; i++) {
			for (BlockFace face : faces) {
				Block block = entity.getLocation().clone().add(0, i, 0).getBlock().getRelative(face, 1);

				if (block.getType() == Material.BARRIER && !barrier) {
					continue;
				}
				if (GeneralMethods.isSolid(block)) {
					return true;
				}
			}
		}
		return false;
	}

	public static void updateAll() {
		for (final Entity e : instances.keySet()) {
			if (e != null && !e.isDead() && instances.get(e) != null) {
				instances.get(e).update();
			} else {
				instances.remove(e);
			}
		}
	}

	public void remove() {
		instances.remove(this.entity);
	}

	public static void remove(final Entity e) {
		instances.remove(e);
	}

	public static boolean hasBeenDamagedByHorizontalVelocity(final Entity e) {
		if (instances.containsKey(e)) {
			return instances.get(e).hasBeenDamaged;
		}
		return false;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
}

package com.projectkorra.projectkorra.object;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import org.bukkit.Bukkit;
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
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.event.HorizontalVelocityChangeEvent;

/**
 * Created by Carbogen on 2/2/2015.
 */
public class HorizontalVelocityTracker {

	public static Map<Entity, HorizontalVelocityTracker> instances = new ConcurrentHashMap<>();
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
	private Ability ability;

	public static String[] abils = { "AirBlast", "AirBurst", "AirSuction", "Bloodbending" };

	public HorizontalVelocityTracker(final Entity entity, final Player instigator, final long delay, final Ability ability) {
		if (!ProjectKorra.plugin.getConfig().getBoolean("Properties.HorizontalCollisionPhysics.Enabled")) {
			return;
		}

		if (!(entity instanceof LivingEntity)) {
			return;
		}

		remove(entity);
		this.entity = entity;
		this.instigator = instigator;
		this.fireTime = System.currentTimeMillis();
		this.delay = delay;
		this.thisVelocity = entity.getVelocity().clone();
		this.launchLocation = entity.getLocation().clone();
		this.impactLocation = this.launchLocation.clone();
		this.ability = ability;
		this.update();
		instances.put(this.entity, this);
	}

	public void update() {
		if (System.currentTimeMillis() < this.fireTime + this.delay) {
			return;
		}

		if (this.entity.isOnGround()) {
			this.remove();
			return;
		}

		if (System.currentTimeMillis() > (this.fireTime + 30000)) {
			ProjectKorra.log.info("removed HorizontalVelocityTracker lasting over 30 seconds: " + this.instigator.getName() + " using " + this.ability.getName() + " on " + this.entity);
			this.remove();
			return;
		}

		this.lastVelocity = this.thisVelocity.clone();
		this.thisVelocity = this.entity.getVelocity().clone();

		final Vector diff = this.thisVelocity.subtract(this.lastVelocity);

		final List<Block> blocks = GeneralMethods.getBlocksAroundPoint(this.entity.getLocation(), 1.5);

		for (final Block block : blocks) {
			if (ElementalAbility.isWater(block)) {
				this.remove();
				return;
			}
		}

		if (this.thisVelocity.length() < this.lastVelocity.length()) {
			if ((diff.getX() > 1 || diff.getX() < -1) || (diff.getZ() > 1 || diff.getZ() < -1)) {
				this.impactLocation = this.entity.getLocation();
				Block impactBlock = this.impactLocation.getBlock();
				for (final Block block : blocks) {
					if (!this.barrier && block.getType() == Material.BARRIER) {
						return;
					} else if (impactBlock.getY() != block.getY()) {
						continue;
					}
					int locationDifference = Math.abs(impactBlock.getX() - block.getX()) + Math.abs(impactBlock.getZ() - block.getZ());
					if (locationDifference == 1 && GeneralMethods.isSolid(block) && !ElementalAbility.isTransparent(this.instigator, block)) {
						this.hasBeenDamaged = true;
						Bukkit.getPluginManager().callEvent(new HorizontalVelocityChangeEvent(this.entity, this.instigator, this.lastVelocity, this.thisVelocity, diff, this.launchLocation, this.impactLocation, this.ability));
						this.remove();
						return;
					}
				}
			}
		}
	}

	public static void updateAll() {
		for (final Map.Entry<Entity, HorizontalVelocityTracker> entry : instances.entrySet()) {
			final Entity entity = entry.getKey();
			final HorizontalVelocityTracker tracker = entry.getValue();
			if (entity != null && !entity.isDead() && tracker != null) {
				tracker.update();
			} else {
				instances.remove(entity);
			}
		}
	}

	public void remove() {
		instances.remove(this.entity);
	}

	public static void remove(final Entity entity) {
		instances.remove(entity);
	}

	public static boolean hasBeenDamagedByHorizontalVelocity(final Entity entity) {
		HorizontalVelocityTracker tracker = instances.get(entity);
		return tracker != null && tracker.hasBeenDamaged;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
}

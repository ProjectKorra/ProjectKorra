package com.projectkorra.projectkorra.object;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
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

	public static String[] abils = { "AirBlast", "AirBurst", "AirSuction", "Bloodbending" };

	public HorizontalVelocityTracker(final Entity e, final Player instigator, final long delay, final Ability ability) {
		if (!ProjectKorra.plugin.getConfig().getBoolean("Properties.HorizontalCollisionPhysics.Enabled")) {
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

		if (this.entity.isOnGround()) {
			this.remove();
			return;
		}

		if (System.currentTimeMillis() > (this.fireTime + 30000)) {
			ProjectKorra.log.info("removed HorizontalVelocityTracker over 30 seconds: " + this.toString());
			this.remove();
			return;
		}

		this.lastVelocity = this.thisVelocity.clone();
		this.thisVelocity = this.entity.getVelocity().clone();

		final Vector diff = this.thisVelocity.subtract(this.lastVelocity);

		final List<Block> blocks = GeneralMethods.getBlocksAroundPoint(this.entity.getLocation(), 1.5);

		for (final Block b : blocks) {
			if (ElementalAbility.isWater(b)) {
				this.remove();
				return;
			}
		}

		if (this.thisVelocity.length() < this.lastVelocity.length()) {
			if ((diff.getX() > 1 || diff.getX() < -1) || (diff.getZ() > 1 || diff.getZ() < -1)) {
				this.impactLocation = this.entity.getLocation();
				for (final Block b : blocks) {
					if (b.getType() == Material.BARRIER && !this.barrier) {
						return;
					}
					if (GeneralMethods.isSolid(b) && (this.entity.getLocation().getBlock().getRelative(BlockFace.EAST, 1).equals(b) || this.entity.getLocation().getBlock().getRelative(BlockFace.NORTH, 1).equals(b) || this.entity.getLocation().getBlock().getRelative(BlockFace.WEST, 1).equals(b) || this.entity.getLocation().getBlock().getRelative(BlockFace.SOUTH, 1).equals(b))) {
						if (!ElementalAbility.isTransparent(this.instigator, b)) {
							this.hasBeenDamaged = true;
							ProjectKorra.plugin.getServer().getPluginManager().callEvent(new HorizontalVelocityChangeEvent(this.entity, this.instigator, this.lastVelocity, this.thisVelocity, diff, this.launchLocation, this.impactLocation, this.abil));
							this.remove();
							return;
						}
					}
				}
			}
		}
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
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
}

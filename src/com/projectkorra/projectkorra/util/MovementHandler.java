package com.projectkorra.projectkorra.util;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;

/**
 * An object to control how an entity moves. 
 * <br>Current functions include <b>stopping</b>.
 * @author Simplicitee
 *
 */
public class MovementHandler {
	
	public static Set<MovementHandler> handlers = new HashSet<>();
	
	private LivingEntity entity;
	private BukkitRunnable runnable, msg;
	private ResetTask reset = null;
	private CoreAbility ability;

	public MovementHandler(LivingEntity entity, CoreAbility ability) {
		this.entity = entity;
		this.ability = ability;
		handlers.add(this);
	}
	
	/**
	 * This stops the movement of the entity once 
	 * they land on the ground, acting as a "paralyze"
	 * with a duration for how long they should be stopped
	 * @param duration how long the entity should be stopped for <b>(in ticks)</b>.
	 * @param message the message to send to the stopped entity if they are a player
	 * @param ability ability which is stopping the player
	 */
	public void stopWithDuration(long duration, String message) {
		entity.setMetadata("movement:stop", new FixedMetadataValue(ProjectKorra.plugin, ability));
		if (entity instanceof Player) {
			long start = System.currentTimeMillis();
			Player player = (Player) entity;
			runnable = new BukkitRunnable() {

				@Override
				public void run() {
					ActionBar.sendActionBar(message, player);
					if (System.currentTimeMillis() >= start + duration/20*1000) {
						reset();
					}
				}
				
			};
			runnable.runTaskTimer(ProjectKorra.plugin, 0, 1);
		} else {
			runnable = new BukkitRunnable() {
	
				@Override
				public void run() {
					allowMove();
				}
				
			};
			new BukkitRunnable() {
	
				@Override
				public void run() {
					if (entity.isOnGround()) {
						entity.setAI(false);
						cancel();
						runnable.runTaskLater(ProjectKorra.plugin, duration);
					}
				}
				
			}.runTaskTimer(ProjectKorra.plugin, 0, 1);
		}
	}
	
	/**
	 * This stops the movement of the entity once 
	 * they land on the ground, acting as a "paralyze"
	 * @param message the message to send to the stopped entity if they are a player
	 * @param ability ability which is stopping the player
	 */
	public void stop(String message) {
		entity.setMetadata("movement:stop", new FixedMetadataValue(ProjectKorra.plugin, ability));
		if (entity instanceof Player) {
			Player player = (Player) entity;
			msg = new BukkitRunnable() {

				@Override
				public void run() {
					ActionBar.sendActionBar(message, player);
				}
				
			};
			msg.runTaskTimer(ProjectKorra.plugin, 0, 1);
		} else {
			new BukkitRunnable() {
	
				@Override
				public void run() {
					if (entity.isOnGround()) {
						entity.setAI(false);
						cancel();
					}
				}
				
			}.runTaskTimer(ProjectKorra.plugin, 0, 1);
		}
		runnable = null;
	}
	
	private void allowMove() {
		if (!(entity instanceof Player)) {
			entity.setAI(true);
		}
		if (reset != null) {
			reset.run();
		}
		if (entity.hasMetadata("movement:stop")) {
			entity.removeMetadata("movement:stop", ProjectKorra.plugin);
		}
	}
	
	/**
	 * Runs the reseting runnable if not scheduled
	 */
	public void reset() {
		if (runnable != null) {
			runnable.cancel();
		}
		if (msg != null) {
			msg.cancel();
		}
		allowMove();
	}
	
	public CoreAbility getAbility() {
		return ability;
	}
	
	public LivingEntity getEntity() {
		return entity;
	}
	
	public void setResetTask(ResetTask reset) {
		this.reset = reset;
	}
	
	/**
	 * Functional interface, called when the entity is allowed to
	 * move again, therefore "reseting" it's AI
	 * @author Simplicitee
	 *
	 */
	public interface ResetTask {
		public void run();
	}
	
	public static boolean isStopped(Entity entity) {
		return entity.hasMetadata("movement:stop");
	}
	
	public static void resetAll() {
		for (MovementHandler handler : handlers) {
			handler.reset();
		}
	}
	
	public static MovementHandler getFromEntityAndAbility(Entity entity, CoreAbility ability) {
		for (MovementHandler handler : handlers) {
			if (handler.getEntity().getEntityId() == entity.getEntityId() && handler.getAbility().equals(ability)) {
				return handler;
			}
		}
		
		return null;
	}
}

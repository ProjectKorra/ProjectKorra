package com.projectkorra.projectkorra.util;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.projectkorra.ProjectKorra;

/**
 * An object to control how an entity moves. 
 * <br>Current functions include <b>stopping</b>.
 * @author Simplicitee
 *
 */
public class MovementHandler {
	
	private LivingEntity entity;
	private BukkitRunnable runnable;
	private ResetTask reset = null;

	public MovementHandler(LivingEntity entity) {
		this.entity = entity;
	}
	
	/**
	 * This stops the movement of the entity once 
	 * they land on the ground, acting as a "paralyze"
	 * @param duration how long the entity should be stopped for <b>(in ticks)</b>
	 */
	public void stop(long duration, String message) {
		if (entity instanceof Player) {
			long start = System.currentTimeMillis();
			Player player = (Player) entity;
			player.setMetadata("movement:stop", new FixedMetadataValue(ProjectKorra.plugin, 0));
			runnable = new BukkitRunnable() {

				@Override
				public void run() {
					ActionBar.sendActionBar(message, player);
					if (System.currentTimeMillis() >= start + duration/20*1000) {
						player.removeMetadata("movement:stop", ProjectKorra.plugin);
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
	
	private void allowMove() {
		if (!(entity instanceof Player)) {
			entity.setAI(true);
		}
		if (reset != null) {
			reset.run();
		}
	}
	
	/**
	 * Cancels the current task and allows the entity to move freely
	 */
	public void reset() {
		runnable.cancel();
		allowMove();
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
}

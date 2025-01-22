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
 * An object to control how an entity moves. <br>
 * Current functions include <b>stopping</b>.
 *
 * @author Simplicitee
 *
 */
public class MovementHandler {

	public static Set<MovementHandler> handlers = new HashSet<>();

	private final LivingEntity entity;
	private BukkitRunnable runnable, msg;
	private ResetTask reset = null;
	private final CoreAbility ability;

	public MovementHandler(final LivingEntity entity, final CoreAbility ability) {
		this.entity = entity;
		this.ability = ability;
		handlers.add(this);
	}

	/**
	 * This stops the movement of the entity once they land on the ground,
	 * acting as a "paralyze" with a duration for how long they should be
	 * stopped
	 *
	 * @param duration how long the entity should be stopped for <b>(in
	 *            ticks)</b>.
	 * @param message the message to send to the stopped entity if they are a
	 *            player
	 */
	public void stopWithDuration(final long duration, final String message) {
		this.entity.setMetadata("movement:stop", new FixedMetadataValue(ProjectKorra.plugin, this.ability));
		if (this.entity instanceof Player) {
			final long start = System.currentTimeMillis();
			final Player player = (Player) this.entity;
			this.runnable = new BukkitRunnable() {

				@Override
				public void run() {
					ActionBar.sendActionBar(message, player);
					if (System.currentTimeMillis() >= start + duration / 20 * 1000) {
						MovementHandler.this.reset();
					}
				}

			};
			this.runnable.runTaskTimer(ProjectKorra.plugin, 0, 1);
		} else {
			this.runnable = new BukkitRunnable() {

				@Override
				public void run() {
					MovementHandler.this.reset();
				}

			};
			new BukkitRunnable() {

				@Override
				public void run() {
					if (MovementHandler.this.entity.isOnGround()) {
						MovementHandler.this.entity.setAI(false);
						this.cancel();
						MovementHandler.this.runnable.runTaskLater(ProjectKorra.plugin, duration);
					}
				}

			}.runTaskTimer(ProjectKorra.plugin, 0, 1);
		}
	}

	/**
	 * This stops the movement of the entity once they land on the ground,
	 * acting as a "paralyze"
	 *
	 * @param message the message to send to the stopped entity if they are a
	 *            player
	 */
	public void stop(final String message) {
		this.entity.setMetadata("movement:stop", new FixedMetadataValue(ProjectKorra.plugin, this.ability));
		if (this.entity instanceof Player) {
			final Player player = (Player) this.entity;
			this.msg = new BukkitRunnable() {

				@Override
				public void run() {
					ActionBar.sendActionBar(message, player);
				}

			};
			this.msg.runTaskTimer(ProjectKorra.plugin, 0, 1);
		} else {
			new BukkitRunnable() {

				@Override
				public void run() {
					if (MovementHandler.this.entity.isOnGround()) {
						MovementHandler.this.entity.setAI(false);
						this.cancel();
					}
				}

			}.runTaskTimer(ProjectKorra.plugin, 0, 1);
		}
		this.runnable = null;
	}

	/**
	 * Resets any stopped movements and runs the {@link ResetTask} if able.
	 */
	public void reset() {
		if (this.runnable != null) {
			try {
				this.runnable.cancel();
			} catch (final IllegalStateException e) { //if a player hasn't landed on the ground yet this runnable wont be scheduled, and will give an error on server shutdown
				this.runnable = null;
			}
		}
		if (this.msg != null) {
			this.msg.cancel();
		}
		if (!(this.entity instanceof Player)) {
			this.entity.setAI(true);
		}
		if (this.reset != null) {
			this.reset.run();
		}
		if (this.entity.hasMetadata("movement:stop")) {
			this.entity.removeMetadata("movement:stop", ProjectKorra.plugin);
		}
	}

	public CoreAbility getAbility() {
		return this.ability;
	}

	public LivingEntity getEntity() {
		return this.entity;
	}

	public void setResetTask(final ResetTask reset) {
		this.reset = reset;
	}

	/**
	 * Functional interface, called when the entity is allowed to move again,
	 * therefore "reseting" it's AI
	 *
	 * @author Simplicitee
	 *
	 */
	public interface ResetTask {
		public void run();
	}

	/**
	 * Checks if the entity is stopped by an instance of MovementHandler
	 *
	 * @param entity the entity in question of being stopped
	 * @return false if not stopped by an instance of MovementHandler
	 */
	public static boolean isStopped(final Entity entity) {
		return entity.hasMetadata("movement:stop");
	}

	/**
	 * Resets all instances of MovementHandler
	 */
	public static void resetAll() {
		for (final MovementHandler handler : handlers) {
			handler.reset();
		}
	}

	/**
	 * Using an entity and ability, the MovementHandler associated with both
	 * will be found.
	 *
	 * @param entity the entity in question of being stopped
	 * @param ability the ability in question of doing the stopping
	 * @return null if no MovementHandler instance with entity and ability
	 *         found.
	 */
	public static MovementHandler getFromEntityAndAbility(final Entity entity, final CoreAbility ability) {
		for (final MovementHandler handler : handlers) {
			if (handler.getEntity().getEntityId() == entity.getEntityId() && handler.getAbility().equals(ability)) {
				return handler;
			}
		}

		return null;
	}
}

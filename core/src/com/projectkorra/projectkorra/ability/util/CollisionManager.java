package com.projectkorra.projectkorra.ability.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.event.AbilityCollisionEvent;

/**
 * A CollisionManager is used to monitor possible collisions between all
 * CoreAbilities. Use {@link #addCollision(Collision)} to begin monitoring for
 * collision between two abilities, as shown in {@link CollisionInitializer}.
 * <p>
 * Addon developers should use:<br>
 * ProjectKorra.getCollisionInitializer().addCollision(myCoreAbility)
 * ProjectKorra.getCollisionInitializer().addSmallAbility(myCoreAbility)
 * <p>
 * For a CoreAbility to collide properly, the {@link CoreAbility#isCollidable}
 * , {@link CoreAbility#getCollisionRadius},
 * {@link CoreAbility#getLocations}, and {@link CoreAbility#handleCollision}
 * should be overridden if necessary.
 * <p>
 * During a Collision the {@link AbilityCollisionEvent} is called, then if not
 * cancelled, abilityFirst.handleCollision, and finally
 * abilitySecond.handleCollision.
 */
public class CollisionManager {

	/*
	 * If true an ability instance can remove multiple other instances on a
	 * single tick. e.g. 3 Colliding WaterManipulations can all be removed
	 * instantly, rather than just 2.
	 */
	private boolean removeMultipleInstances;

	/*
	 * The amount of ticks in between checking for collisions. Higher values
	 * reduce lag but are less accurate in detection.
	 */
	private long detectionDelay;

	/*
	 * Used for efficiency. The distance that we can guarantee that two
	 * abilities will not collide so that we can stop comparing locations early.
	 * For example, two Torrents that are thousands of blocks apart should not
	 * be fully checked.
	 */
	private double certainNoCollisionDistance;

	private ArrayList<Collision> collisions;
	private BukkitRunnable detectionRunnable;

	public CollisionManager() {
		this.removeMultipleInstances = true;
		this.detectionDelay = 1;
		this.certainNoCollisionDistance = 100;
		this.collisions = new ArrayList<>();
	}

	private void detectCollisions() {
		int activeInstanceCount = 0;

		for (final CoreAbility ability : CoreAbility.getAbilitiesByInstances()) {
			if (!(ability instanceof PassiveAbility)) {
				if (++activeInstanceCount > 1) {
					break;
				}
			}
		}

		if (activeInstanceCount <= 1) {
			return;
		}

		final HashMap<CoreAbility, List<Location>> locationsCache = new HashMap<>();

		for (final Collision collision : this.collisions) {
			final Collection<? extends CoreAbility> instancesFirst = CoreAbility.getAbilities(collision.getAbilityFirst().getClass());
			if (instancesFirst.isEmpty()) {
				continue;
			}
			final Collection<? extends CoreAbility> instancesSecond = CoreAbility.getAbilities(collision.getAbilitySecond().getClass());
			if (instancesSecond.isEmpty()) {
				continue;
			}
			final HashSet<CoreAbility> alreadyCollided = new HashSet<CoreAbility>();
			final double certainNoCollisionDistSquared = Math.pow(this.certainNoCollisionDistance, 2);

			for (final CoreAbility abilityFirst : instancesFirst) {
				if (abilityFirst.getPlayer() == null || alreadyCollided.contains(abilityFirst) || !abilityFirst.isCollidable()) {
					continue;
				}

				if (!locationsCache.containsKey(abilityFirst)) {
					locationsCache.put(abilityFirst, abilityFirst.getLocations());
				}
				final List<Location> locationsFirst = locationsCache.get(abilityFirst);
				if (locationsFirst.isEmpty()) {
					continue;
				}

				for (final CoreAbility abilitySecond : instancesSecond) {
					if (abilitySecond.getPlayer() == null || alreadyCollided.contains(abilitySecond) || !abilitySecond.isCollidable()) {
						continue;
					} else if (abilityFirst.getPlayer().equals(abilitySecond.getPlayer())) {
						continue;
					}

					if (!locationsCache.containsKey(abilitySecond)) {
						locationsCache.put(abilitySecond, abilitySecond.getLocations());
					}
					final List<Location> locationsSecond = locationsCache.get(abilitySecond);
					if (locationsSecond.isEmpty()) {
						continue;
					}

					boolean collided = false;
					boolean certainNoCollision = false; // Used for efficiency.
					Location locationFirst = null;
					Location locationSecond = null;
					final double requiredDist = abilityFirst.getCollisionRadius() + abilitySecond.getCollisionRadius();
					final double requiredDistSquared = Math.pow(requiredDist, 2);

					for (int i = 0; i < locationsFirst.size(); i++) {
						locationFirst = locationsFirst.get(i);
						if (locationFirst == null) {
							continue;
						}
						for (int j = 0; j < locationsSecond.size(); j++) {
							locationSecond = locationsSecond.get(j);
							if (locationSecond == null) {
								continue;
							}

							if (locationFirst.getWorld() != locationSecond.getWorld()) {
								continue;
							}
							final double distSquared = locationFirst.distanceSquared(locationSecond);
							if (distSquared <= requiredDistSquared) {
								collided = true;
								break;
							} else if (distSquared >= certainNoCollisionDistSquared) {
								certainNoCollision = true;
								break;
							}
						}
						if (collided || certainNoCollision) {
							break;
						}
					}

					if (collided) {
						final Collision forwardCollision = new Collision(abilityFirst, abilitySecond, collision.isRemovingFirst(), collision.isRemovingSecond(), locationFirst, locationSecond);
						final Collision reverseCollision = new Collision(abilitySecond, abilityFirst, collision.isRemovingSecond(), collision.isRemovingFirst(), locationSecond, locationFirst);
						final AbilityCollisionEvent event = new AbilityCollisionEvent(forwardCollision);
						Bukkit.getServer().getPluginManager().callEvent(event);
						if (event.isCancelled()) {
							continue;
						}
						abilityFirst.handleCollision(forwardCollision);
						abilitySecond.handleCollision(reverseCollision);
						if (!this.removeMultipleInstances) {
							alreadyCollided.add(abilityFirst);
							alreadyCollided.add(abilitySecond);
							break;
						}
					}
				}
			}
		}
	}

	/**
	 * Adds a "fake" Collision to the CollisionManager so that two abilities can
	 * be checked for collisions. This Collision only needs to define the
	 * abilityFirst, abilitySecond, removeFirst, and removeSecond.
	 *
	 * @param collision a Collision containing two CoreAbility classes
	 */
	public void addCollision(final Collision collision) {
		if (collision == null || collision.getAbilityFirst() == null || collision.getAbilitySecond() == null) {
			return;
		}

		for (int x = 0; x < this.collisions.size(); x++) {
			if (this.collisions.get(x).getAbilityFirst().equals(collision.getAbilityFirst())) {
				if (this.collisions.get(x).getAbilitySecond().equals(collision.getAbilitySecond())) {
					this.collisions.remove(x);
				}
			}
		}

		this.collisions.add(collision);
	}

	/**
	 * Begins a BukkitRunnable to check for Collisions.
	 */
	public void startCollisionDetection() {
		this.stopCollisionDetection();
		this.detectionRunnable = new BukkitRunnable() {
			@Override
			public void run() {
				CollisionManager.this.detectCollisions();
			}
		};
		this.detectionRunnable.runTaskTimer(ProjectKorra.plugin, 0L, this.detectionDelay);
	}

	/**
	 * Stops the collision detecting BukkitRunnable.
	 */
	public void stopCollisionDetection() {
		if (this.detectionRunnable != null) {
			this.detectionRunnable.cancel();
			this.detectionRunnable = null;
		}
	}

	public boolean isRemoveMultipleInstances() {
		return this.removeMultipleInstances;
	}

	public void setRemoveMultipleInstances(final boolean removeMultipleInstances) {
		this.removeMultipleInstances = removeMultipleInstances;
	}

	public long getDetectionDelay() {
		return this.detectionDelay;
	}

	public void setDetectionDelay(final long detectionDelay) {
		this.detectionDelay = detectionDelay;
	}

	public double getCertainNoCollisionDistance() {
		return this.certainNoCollisionDistance;
	}

	public void setCertainNoCollisionDistance(final double certainNoCollisionDistance) {
		this.certainNoCollisionDistance = certainNoCollisionDistance;
	}

	public ArrayList<Collision> getCollisions() {
		return this.collisions;
	}

	public void setCollisions(final ArrayList<Collision> collisions) {
		this.collisions = collisions;
	}

	public BukkitRunnable getDetectionRunnable() {
		return this.detectionRunnable;
	}

	public void setDetectionRunnable(final BukkitRunnable detectionRunnable) {
		this.detectionRunnable = detectionRunnable;
	}

}

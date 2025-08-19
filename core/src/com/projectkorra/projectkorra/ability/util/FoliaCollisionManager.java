package com.projectkorra.projectkorra.ability.util;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.event.AbilityCollisionEvent;
import com.projectkorra.projectkorra.util.ThreadUtil;
import com.projectkorra.projectkorra.versions.LuminolIntermediate;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Although this is named "Folia" collision manager, this currently requires Luminol
 * due to incomplete API in Folia.
 */
public class FoliaCollisionManager {

    private static Map<Long, Set<CoreAbility>> regionMap = new HashMap<>();
    private static Map<Long, CollisionTask> collisionTasks = new ConcurrentHashMap<>();
    private static Map<Long, Map<Class<? extends CoreAbility>, Set<CoreAbility>>> INSTANCES_BY_CLASS = new ConcurrentHashMap<>();

    public static void startTracking(CoreAbility ability) {
        Location location = ability.getLocation();
        if (location == null) {
            location = ability.getPlayer().getLocation();
        }
        Object region = LuminolIntermediate.getRegion(location);
        if (region == null) {
            return;
        }

        long id = LuminolIntermediate.getRegionId(region);

        regionMap.computeIfAbsent(id, k -> new HashSet<>()).add(ability);
        INSTANCES_BY_CLASS.computeIfAbsent(id, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(ability.getClass(), k -> new HashSet<>()).add(ability);

        if (!collisionTasks.containsKey(id)) {
            CollisionTask task = new CollisionTask(region);
            collisionTasks.put(id, task);
            ThreadUtil.ensureLocationDelay(location, task, 1);
        }

        //

    }

    public static void stopTracking(CoreAbility ability) {
        Location location = ability.getLocation();
        if (location == null) {
            location = ability.getPlayer().getLocation();
        }

        Object region = LuminolIntermediate.getRegion(location);
        if (region == null) {
            return;
        }

        long id = LuminolIntermediate.getRegionId(region);

        Set<CoreAbility> abilities = regionMap.get(id);
        if (abilities != null) {
            abilities.remove(ability);
            if (abilities.isEmpty()) {
                regionMap.remove(id);
                collisionTasks.remove(id);
            }
        }
        Map<Class<? extends CoreAbility>, Set<CoreAbility>> instancesByClass = INSTANCES_BY_CLASS.get(id);
        if (instancesByClass != null) {
            Set<CoreAbility> classAbilities = instancesByClass.get(ability.getClass());
            if (classAbilities != null) {
                classAbilities.remove(ability);
                if (classAbilities.isEmpty()) {
                    instancesByClass.remove(ability.getClass());
                    if (instancesByClass.isEmpty()) {
                        INSTANCES_BY_CLASS.remove(id);
                    }
                }
            }
        }
    }

    public static class CollisionTask implements Runnable {

        private Object region;
        private long id;

        public CollisionTask(Object region) {
            this.region = region;
            this.id = LuminolIntermediate.getRegionId(region);
        }

        @Override
        public void run() {
            if (!regionMap.containsKey(id) || regionMap.get(id).isEmpty() || !LuminolIntermediate.isRegionActive(region)) {
                regionMap.remove(id);
                //ProjectKorra.log.info("Removing collision task for region " + region);
                return;
            }

            checkCollisions(id);

            CoreAbility first = regionMap.get(id).iterator().next();
            Location location = first.getLocation();
            if (location == null) location = first.getPlayer().getLocation();

            ThreadUtil.ensureLocationDelay(location, this, 1);
        }
    }

    private static void checkCollisions(long regionId) {
        if (regionMap.get(regionId).size() < 2) {
            return;
        }

        final HashMap<CoreAbility, List<Location>> locationsCache = new HashMap<>();
        for (final Collision collision : ProjectKorra.getCollisionManager().getCollisions()) {
            final Collection<? extends CoreAbility> instancesFirst = INSTANCES_BY_CLASS.get(regionId).get(collision.getAbilityFirst().getClass());
            if (instancesFirst == null || instancesFirst.isEmpty()) {
                continue;
            }
            final Collection<? extends CoreAbility> instancesSecond = INSTANCES_BY_CLASS.get(regionId).get(collision.getAbilitySecond().getClass());
            if (instancesSecond == null || instancesSecond.isEmpty()) {
                continue;
            }
            final HashSet<CoreAbility> alreadyCollided = new HashSet<CoreAbility>();
            final double certainNoCollisionDistSquared = Math.pow(ProjectKorra.getCollisionManager().getCertainNoCollisionDistance(), 2);

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
                        if (!ProjectKorra.getCollisionManager().isRemoveMultipleInstances()) {
                            alreadyCollided.add(abilityFirst);
                            alreadyCollided.add(abilitySecond);
                            break;
                        }
                    }
                }
            }
        }
    }
}

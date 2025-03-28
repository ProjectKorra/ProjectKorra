package com.projectkorra.projectkorra.firebending.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.projectkorra.projectkorra.ability.Ability;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.firebending.HeatControl;
import com.projectkorra.projectkorra.util.DamageHandler;

public class FireDamageTimer {

	private static final int MAX_TICKS = 90;
	private static Ability ability = null;
	private static final double DAMAGE = 1;
	private static final long BUFFER = 30;
	private static final Map<Entity, Player> INSTANCES = new ConcurrentHashMap<>();
	private static final Map<Entity, Long> TIMES = new ConcurrentHashMap<>();

	/**
	 * Deprecated. Use FireDamageTimer#FireDamageTimer(final Entity entity, Final Player source, Ability abil)
	 * instead.
	 * @param entity affected entity.
	 * @param source player who used the fire move.
	 */
	@Deprecated
	public FireDamageTimer(final Entity entity, final Player source) {
		this(entity, source, null, false);
	}
	
	public FireDamageTimer(final Entity entity, final Player source, Ability abil) {
		this(entity, source, abil, false);
	}

	public FireDamageTimer(final Entity entity, final Player source, Ability abil, final boolean affectSelf) {
		if (entity.getEntityId() == source.getEntityId() && !affectSelf) {
			return;
		}

		INSTANCES.put(entity, source);
		ability = abil;
		// TODO: shouldn't this be a static method? Instantiating here does nothing and is always GC'd
		// Alternatively if we made this an instanced system which tracked ability per instance (confused rn why we don't)
		// then it would make sense to keep
	}

	public static boolean isEnflamed(final Entity entity) {
		if (INSTANCES.containsKey(entity)) {
			final Long time = TIMES.get(entity);
			if (time != null && System.currentTimeMillis() < time + BUFFER) {
				return false;
			}
			TIMES.put(entity, System.currentTimeMillis());
			return true;
		}
		return false;
	}
	
	public static void dealFlameDamage(final Entity entity, final double damage) {
		Player source = INSTANCES.get(entity);
        if (!(entity instanceof LivingEntity livingEntity) || source == null) {
            return;
        } else if (entity instanceof Player player && !HeatControl.canBurn(player)) {
            return;
        }

        // damages the entity.
        if (ability == null) {
            DamageHandler.damageEntity(livingEntity, source, damage, CoreAbility.getAbilitiesByElement(Element.FIRE).getFirst(), false, true);
        } else {
            DamageHandler.damageEntity(livingEntity, source, damage, ability, false, true);
        }

        if (entity.getFireTicks() > MAX_TICKS) {
            entity.setFireTicks(MAX_TICKS);
        }
    }

	public static void dealFlameDamage(final Entity entity) {
		dealFlameDamage(entity, DAMAGE);
	}

	public static void handleFlames() {
		for (final Entity entity : INSTANCES.keySet()) {
			if (entity.getFireTicks() <= 0) {
				INSTANCES.remove(entity);
			}
		}
	}

	/**
	 * Util so that players can find benders who were hurt/killed by bending
	 * fire as opened to regular firetick.
	 * @return Map from Entity to Player, entity on fire to player who set them
	 * alight with firebending.
	 */
	public static Map<Entity, Player> getInstances() {
		return INSTANCES;
	}
}

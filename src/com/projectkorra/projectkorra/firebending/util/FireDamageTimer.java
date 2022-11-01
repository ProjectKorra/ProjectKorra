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
	}

	public static boolean isEnflamed(final Entity entity) {
		if (INSTANCES.containsKey(entity)) {
			if (TIMES.containsKey(entity)) {
				final long time = TIMES.get(entity);
				if (System.currentTimeMillis() < time + BUFFER) {
					return false;
				}
			}
			TIMES.put(entity, System.currentTimeMillis());
			return true;
		} else {
			return false;
		}
	}
	
	public static void dealFlameDamage(final Entity entity, final double damage) {
		if (INSTANCES.containsKey(entity) && entity instanceof LivingEntity) {
			if (entity instanceof Player) {
				if (!HeatControl.canBurn((Player) entity)) {
					return;
				}
			}
			final LivingEntity Lentity = (LivingEntity) entity;
			final Player source = INSTANCES.get(entity);
			
			// damages the entity.
			if (ability == null) {
				DamageHandler.damageEntity(Lentity, source, damage, CoreAbility.getAbilitiesByElement(Element.FIRE).get(0), false, true);
			} else {
				DamageHandler.damageEntity(Lentity, source, damage, ability, false, true);
			}
			
			if (entity.getFireTicks() > MAX_TICKS) {
				entity.setFireTicks(MAX_TICKS);
			}
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

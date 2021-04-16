package com.projectkorra.projectkorra.firebending.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.firebending.HeatControl;
import com.projectkorra.projectkorra.util.DamageHandler;

public class FireDamageTimer {

	private static final int MAX_TICKS = 90;
	private static final int DAMAGE = 1;
	private static final long BUFFER = 30;
	private static final Map<Entity, Player> INSTANCES = new ConcurrentHashMap<>();
	private static final Map<Entity, Long> TIMES = new ConcurrentHashMap<>();

	public FireDamageTimer(final Entity entity, final Player source) {
		if (entity.getEntityId() == source.getEntityId()) {
			return;
		}

		INSTANCES.put(entity, source);
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

	public static void dealFlameDamage(final Entity entity) {
		if (INSTANCES.containsKey(entity) && entity instanceof LivingEntity) {
			if (entity instanceof Player) {
				if (!HeatControl.canBurn((Player) entity)) {
					return;
				}
			}
			final LivingEntity Lentity = (LivingEntity) entity;
			final Player source = INSTANCES.get(entity);

			// damages the entity.
			DamageHandler.damageEntity(Lentity, source, DAMAGE, CoreAbility.getAbilitiesByElement(Element.FIRE).get(0));

			if (entity.getFireTicks() > MAX_TICKS) {
				entity.setFireTicks(MAX_TICKS);
			}
		}
	}

	public static void handleFlames() {
		for (final Entity entity : INSTANCES.keySet()) {
			if (entity.getFireTicks() <= 0) {
				INSTANCES.remove(entity);
			}
		}
	}

}

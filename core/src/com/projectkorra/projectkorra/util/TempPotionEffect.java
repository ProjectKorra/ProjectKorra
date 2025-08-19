package com.projectkorra.projectkorra.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;

// TODO: Possibly rewrite how this things scheduling works, it gets desynced if a server has a TPS of anything but 20
public class TempPotionEffect {

	private static final Map<LivingEntity, TempPotionEffect> INSTANCES = new ConcurrentHashMap<>();
	private static final long tick = 21;

	private final List<PotionInfo> infos = new ArrayList<>();
	private final LivingEntity entity;

	public TempPotionEffect(final LivingEntity entity, final PotionEffect effect) {
		this(entity, effect, System.currentTimeMillis());
	}

	public TempPotionEffect(final LivingEntity entity, final PotionEffect effect, final long startTime) {
		this.entity = entity;
		if (INSTANCES.containsKey(entity)) {
			final TempPotionEffect instance = INSTANCES.get(entity);
			instance.infos.add(new PotionInfo(startTime, effect));
			INSTANCES.put(entity, instance);
		} else {
			this.infos.add(new PotionInfo(startTime, effect));
			INSTANCES.put(entity, this);
		}
	}

	public static void progressAll() {
		for (final TempPotionEffect effect : INSTANCES.values()) {
			effect.progress();
		}
	}

	private void addEffect(final PotionEffect effect) {
		PotionEffect existing = this.entity.getPotionEffect(effect.getType());
		if (existing != null) {
			if (existing.getAmplifier() < effect.getAmplifier()) {
				this.entity.removePotionEffect(existing.getType());
				this.entity.addPotionEffect(effect);
				if (existing.getDuration() > effect.getDuration()) {
					scheduleWeakerEffect(effect, existing);
				}
			} else if (effect.getDuration() > existing.getDuration()) {
				scheduleWeakerEffect(existing, effect);
			}
            return;
        }
		this.entity.addPotionEffect(effect);
	}

	private void scheduleWeakerEffect(PotionEffect current, PotionEffect future) {
		final int dt = future.getDuration() - current.getDuration();
		final PotionEffect newEffect = new PotionEffect(future.getType(), dt, future.getAmplifier());
		new TempPotionEffect(this.entity, newEffect, System.currentTimeMillis() + current.getDuration() * tick);
	}

	private void progress() {
		Iterator<PotionInfo> iterator = this.infos.iterator();
		while (iterator.hasNext()) {
			PotionInfo info = iterator.next();
			if (info.startTime() < System.currentTimeMillis()) {
				this.addEffect(info.effect());
				iterator.remove();
			}
		}
		if (this.infos.isEmpty()) {
			INSTANCES.remove(this.entity);
		}
	}

	private record PotionInfo(long startTime, PotionEffect effect) {}
}

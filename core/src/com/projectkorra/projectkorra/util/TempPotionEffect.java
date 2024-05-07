package com.projectkorra.projectkorra.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;

public class TempPotionEffect {

	private static Map<LivingEntity, TempPotionEffect> instances = new ConcurrentHashMap<LivingEntity, TempPotionEffect>();
	private static final long tick = 21;

	private int ID = Integer.MIN_VALUE;
	private final Map<Integer, PotionInfo> infos = new ConcurrentHashMap<Integer, PotionInfo>();
	private final LivingEntity entity;

	public TempPotionEffect(final LivingEntity entity, final PotionEffect effect) {
		this(entity, effect, System.currentTimeMillis());
	}

	public TempPotionEffect(final LivingEntity entity, final PotionEffect effect, final long starttime) {
		this.entity = entity;
		if (instances.containsKey(entity)) {
			final TempPotionEffect instance = instances.get(entity);
			instance.infos.put(instance.ID++, new PotionInfo(starttime, effect));
			instances.put(entity, instance);
		} else {
			this.infos.put(this.ID++, new PotionInfo(starttime, effect));
			instances.put(entity, this);
		}
	}

	public static void progressAll() {
		for (final LivingEntity entity : instances.keySet()) {
			instances.get(entity).progress();
		}
	}

	private void addEffect(final PotionEffect effect) {
		for (final PotionEffect peffect : this.entity.getActivePotionEffects()) {
			if (peffect.getType().equals(effect.getType())) {
				if (peffect.getAmplifier() > effect.getAmplifier()) {
					if (peffect.getDuration() > effect.getDuration()) {
						return;
					} else {
						final int dt = effect.getDuration() - peffect.getDuration();
						final PotionEffect neweffect = new PotionEffect(effect.getType(), dt, effect.getAmplifier());
						new TempPotionEffect(this.entity, neweffect, System.currentTimeMillis() + peffect.getDuration() * tick);
						return;
					}
				} else {
					if (peffect.getDuration() > effect.getDuration()) {
						this.entity.removePotionEffect(peffect.getType());
						this.entity.addPotionEffect(effect);
						final int dt = peffect.getDuration() - effect.getDuration();
						final PotionEffect neweffect = new PotionEffect(peffect.getType(), dt, peffect.getAmplifier());
						new TempPotionEffect(this.entity, neweffect, System.currentTimeMillis() + effect.getDuration() * tick);
						return;
					} else {
						this.entity.removePotionEffect(peffect.getType());
						this.entity.addPotionEffect(effect);
						return;
					}
				}
			}
		}
		this.entity.addPotionEffect(effect);
	}

	private void progress() {
		for (final int id : this.infos.keySet()) {
			final PotionInfo info = this.infos.get(id);
			if (info.getTime() < System.currentTimeMillis()) {
				this.addEffect(info.getEffect());
				this.infos.remove(id);
			}
		}
		if (this.infos.isEmpty() && instances.containsKey(this.entity)) {
			instances.remove(this.entity);
		}
	}

	private class PotionInfo {

		private final long starttime;
		private final PotionEffect effect;

		public PotionInfo(final long starttime, final PotionEffect effect) {
			this.starttime = starttime;
			this.effect = effect;
		}

		public long getTime() {
			return this.starttime;
		}

		public PotionEffect getEffect() {
			return this.effect;
		}

	}

}

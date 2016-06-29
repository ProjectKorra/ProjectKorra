package com.projectkorra.projectkorra.util;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TempPotionEffect {

	private static Map<LivingEntity, TempPotionEffect> instances = new ConcurrentHashMap<LivingEntity, TempPotionEffect>();
	private static final long tick = 21;

	private int ID = Integer.MIN_VALUE;
	private Map<Integer, PotionInfo> infos = new ConcurrentHashMap<Integer, PotionInfo>();
	private LivingEntity entity;

	public TempPotionEffect(LivingEntity entity, PotionEffect effect) {
		this(entity, effect, System.currentTimeMillis());
	}

	public TempPotionEffect(LivingEntity entity, PotionEffect effect, long starttime) {
		this.entity = entity;
		if (instances.containsKey(entity)) {
			TempPotionEffect instance = instances.get(entity);
			instance.infos.put(instance.ID++, new PotionInfo(starttime, effect));
			// instance.effects.put(starttime, effect);
			instances.put(entity, instance);
		} else {
			// effects.put(starttime, effect);
			infos.put(ID++, new PotionInfo(starttime, effect));
			instances.put(entity, this);
		}
	}

	public static void progressAll() {
		for (LivingEntity entity : instances.keySet()) {
			instances.get(entity).progress();
		}
	}

	private void addEffect(PotionEffect effect) {
		for (PotionEffect peffect : entity.getActivePotionEffects()) {
			if (peffect.getType().equals(effect.getType())) {
				if (peffect.getAmplifier() > effect.getAmplifier()) {
					if (peffect.getDuration() > effect.getDuration()) {
						return;
					} else {
						int dt = effect.getDuration() - peffect.getDuration();
						PotionEffect neweffect = new PotionEffect(effect.getType(), dt, effect.getAmplifier());
						new TempPotionEffect(entity, neweffect, System.currentTimeMillis() + peffect.getDuration() * tick);
						return;
					}
				} else {
					if (peffect.getDuration() > effect.getDuration()) {
						entity.removePotionEffect(peffect.getType());
						entity.addPotionEffect(effect);
						int dt = peffect.getDuration() - effect.getDuration();
						PotionEffect neweffect = new PotionEffect(peffect.getType(), dt, peffect.getAmplifier());
						new TempPotionEffect(entity, neweffect, System.currentTimeMillis() + effect.getDuration() * tick);
						return;
					} else {
						entity.removePotionEffect(peffect.getType());
						entity.addPotionEffect(effect);
						return;
					}
				}
			}
		}
		entity.addPotionEffect(effect);
	}

	private void progress() {
		for (int id : infos.keySet()) {
			PotionInfo info = infos.get(id);
			if (info.getTime() < System.currentTimeMillis()) {
				addEffect(info.getEffect());
				infos.remove(id);
			}
		}
		if (infos.isEmpty() && instances.containsKey(entity))
			instances.remove(entity);
	}

	private class PotionInfo {

		private long starttime;
		private PotionEffect effect;

		public PotionInfo(long starttime, PotionEffect effect) {
			this.starttime = starttime;
			this.effect = effect;
		}

		public long getTime() {
			return starttime;
		}

		public PotionEffect getEffect() {
			return effect;
		}

	}

}

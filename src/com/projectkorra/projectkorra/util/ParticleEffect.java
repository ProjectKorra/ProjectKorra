package com.projectkorra.projectkorra.util;

import org.bukkit.Location;
import org.bukkit.Particle;

public enum ParticleEffect {

	BARRIER (Particle.BARRIER),
	BLOCK_CRACK (Particle.BLOCK_CRACK),
	BLOCK_DUST (Particle.BLOCK_DUST),
	BUBBLE_COLUMN_UP (Particle.BUBBLE_COLUMN_UP),
	BUBBLE_POP (Particle.BUBBLE_POP),
	CLOUD (Particle.CLOUD),
	CRIT (Particle.CRIT),
	CRIT_MAGIC (Particle.CRIT_MAGIC),
	CURRENT_DOWN (Particle.CURRENT_DOWN),
	DAMAGE_INDICATOR (Particle.DAMAGE_INDICATOR),
	DOLPHIN (Particle.DOLPHIN),
	DRAGON_BREATH (Particle.DRAGON_BREATH),
	DRIP_LAVA (Particle.DRIP_LAVA),
	DRIP_WATER (Particle.DRIP_WATER),
	ENCHANTMENT_TABLE (Particle.ENCHANTMENT_TABLE),
	END_ROD (Particle.END_ROD),
	EXPLOSION_HUGE (Particle.EXPLOSION_HUGE),
	EXPLOSION_LARGE (Particle.EXPLOSION_LARGE),
	EXPLOSION_NORMAL (Particle.EXPLOSION_NORMAL),
	FALLING_DUST (Particle.FALLING_DUST),
	FIREWORKS_SPARK (Particle.FIREWORKS_SPARK),
	FLAME (Particle.FLAME),
	HEART (Particle.HEART),
	ITEM_CRACK (Particle.ITEM_CRACK),
	LAVA (Particle.LAVA),
	MOB_APPEARANCE (Particle.MOB_APPEARANCE),
	NAUTILUS (Particle.NAUTILUS),
	NOTE (Particle.NOTE),
	PORTAL (Particle.PORTAL),
	REDSTONE (Particle.REDSTONE),
	SLIME (Particle.SLIME),
	SMOKE (Particle.SMOKE_NORMAL),
	SMOKE_LARGE (Particle.SMOKE_LARGE),
	SNOW_SHOVEL (Particle.SNOW_SHOVEL),
	SNOWBALL (Particle.SNOWBALL),
	SPELL (Particle.SPELL),
	SPELL_INSTANT (Particle.SPELL_INSTANT),
	SPELL_MOB (Particle.SPELL_MOB),
	SPELL_MOB_AMBIENT (Particle.SPELL_MOB_AMBIENT),
	SPELL_WITCH (Particle.SPELL_WITCH),
	SPIT (Particle.SPIT),
	SQUID_INK (Particle.SQUID_INK),
	SUSPENDED (Particle.SUSPENDED),
	SUSPENDED_DEPTH (Particle.SUSPENDED_DEPTH),
	SWEEP_ATTACK (Particle.SWEEP_ATTACK),
	TOTEM (Particle.TOTEM),
	TOWN_AURA (Particle.TOWN_AURA),
	VILLAGER_ANGRY (Particle.VILLAGER_ANGRY),
	VILLAGER_HAPPY (Particle.VILLAGER_HAPPY),
	WATER_BUBBLE (Particle.WATER_BUBBLE),
	WATER_DROP (Particle.WATER_DROP),
	WATER_SPLASH (Particle.WATER_SPLASH),
	WATER_WAKE (Particle.WATER_WAKE);
	
	Particle particle;
	Class<?> dataClass;
	
	private ParticleEffect(Particle particle) {
		this.particle = particle;
		this.dataClass = particle.getDataType();
	}
	
	public Particle getParticle() {
		return particle;
	}
	
	public void display(Location loc, int amount, double offsetX, double offsetY, double offsetZ) {
		display(loc, amount, offsetX, offsetY, offsetZ, 0);
	}
	
	public void display(Location loc, int amount, double offsetX, double offsetY, double offsetZ, double speed) {
		loc.getWorld().spawnParticle(particle, loc, amount, offsetX, offsetY, offsetZ, speed);
	}
	
	public void display(Location loc, int amount, double offsetX, double offsetY, double offsetZ, Object data) {
		display(loc, amount, offsetX, offsetY, offsetZ, 0, data);
	}
	
	public void display(Location loc, int amount, double offsetX, double offsetY, double offsetZ, double speed, Object data) {
		if (dataClass.isAssignableFrom(Void.class) || !dataClass.isAssignableFrom(data.getClass()) || data == null) {
			display(loc, amount, offsetX, offsetY, offsetZ);
		} else {
			loc.getWorld().spawnParticle(particle, loc, amount, offsetX, offsetY, offsetZ, speed, data);
		}
	}
}

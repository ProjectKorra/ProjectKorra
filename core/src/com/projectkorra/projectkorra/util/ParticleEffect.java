package com.projectkorra.projectkorra.util;

import com.projectkorra.projectkorra.GeneralMethods;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

/**
 * @deprecated Marked for removal. Use {@link org.bukkit.World#spawnParticle} instead
 */
@Deprecated
public enum ParticleEffect {
	
	ASH (Particle.ASH),
	
	/**
	 * Applicable data: {@link BlockData}
	 */
	BLOCK_CRACK (Particle.BLOCK_CRACK),
	
	/**
	 * Applicable data: {@link BlockData}
	 */
	BLOCK_DUST (Particle.BLOCK_DUST),
	BUBBLE_COLUMN_UP (Particle.BUBBLE_COLUMN_UP),
	BUBBLE_POP (Particle.BUBBLE_POP),
	CAMPFIRE_COSY_SMOKE (Particle.CAMPFIRE_COSY_SMOKE),
	CAMPFIRE_SIGNAL_SMOKE (Particle.CAMPFIRE_SIGNAL_SMOKE),
	CLOUD (Particle.CLOUD),
	COMPOSTER (Particle.COMPOSTER),
	CRIMSON_SPORE (Particle.CRIMSON_SPORE),
	CRIT (Particle.CRIT),
	CRIT_MAGIC (Particle.CRIT_MAGIC), @Deprecated MAGIC_CRIT (Particle.CRIT_MAGIC),
	CURRENT_DOWN (Particle.CURRENT_DOWN),
	DAMAGE_INDICATOR (Particle.DAMAGE_INDICATOR),
	DOLPHIN (Particle.DOLPHIN),
	DRAGON_BREATH (Particle.DRAGON_BREATH),
	DRIP_LAVA (Particle.DRIP_LAVA),
	DRIP_WATER (Particle.DRIP_WATER),
	DRIPPING_HONEY (Particle.DRIPPING_HONEY),
	DRIPPING_OBSIDIAN_TEAR (Particle.DRIPPING_OBSIDIAN_TEAR),
	ENCHANTMENT_TABLE (Particle.ENCHANTMENT_TABLE),
	END_ROD (Particle.END_ROD),
	EXPLOSION_HUGE (Particle.EXPLOSION_HUGE), @Deprecated HUGE_EXPLOSION (Particle.EXPLOSION_HUGE),
	EXPLOSION_LARGE (Particle.EXPLOSION_LARGE), @Deprecated LARGE_EXPLODE (Particle.EXPLOSION_LARGE),
	EXPLOSION_NORMAL (Particle.EXPLOSION_NORMAL), @Deprecated EXPLODE (Particle.EXPLOSION_NORMAL),
	
	/**
	 * Applicable data: {@link BlockData}
	 */
	FALLING_DUST (Particle.FALLING_DUST),
	FALLING_HONEY (Particle.FALLING_HONEY),
	FALLING_LAVA (Particle.FALLING_LAVA),
	FALLING_NECTAR (Particle.FALLING_NECTAR),
	FALLING_OBSIDIAN_TEAR (Particle.FALLING_OBSIDIAN_TEAR),
	FALLING_WATER (Particle.FALLING_WATER),
	FIREWORKS_SPARK (Particle.FIREWORKS_SPARK),
	FLAME (Particle.FLAME),
	FLASH (Particle.FLASH),
	HEART (Particle.HEART),
	
	/**
	 * Applicable data: {@link ItemStack}
	 */
	ITEM_CRACK (Particle.ITEM_CRACK),
	LANDING_HONEY (Particle.LANDING_HONEY),
	LANDING_LAVA (Particle.LANDING_LAVA),
	LANDING_OBSIDIAN_TEAR (Particle.LANDING_OBSIDIAN_TEAR),
	LAVA (Particle.LAVA),
	MOB_APPEARANCE (Particle.MOB_APPEARANCE),
	NAUTILUS (Particle.NAUTILUS),
	NOTE (Particle.NOTE),
	PORTAL (Particle.PORTAL),
	
	/**
	 * Applicable data: {@link DustOptions}
	 */
	REDSTONE (Particle.REDSTONE), @Deprecated RED_DUST (Particle.REDSTONE),
	REVERSE_PORTAL (Particle.REVERSE_PORTAL),
	SLIME (Particle.SLIME),
	SMOKE_NORMAL (Particle.SMOKE_NORMAL), @Deprecated SMOKE (Particle.SMOKE_NORMAL),
	SMOKE_LARGE (Particle.SMOKE_LARGE), @Deprecated LARGE_SMOKE (Particle.SMOKE_LARGE),
	SNEEZE (Particle.SNEEZE),
	SNOW_SHOVEL (Particle.SNOW_SHOVEL),
	SNOWBALL (Particle.SNOWBALL), @Deprecated SNOWBALL_PROOF (Particle.SNOWBALL),
	SOUL (Particle.SOUL),
	SOUL_FIRE_FLAME (Particle.SOUL_FIRE_FLAME),
	SPELL (Particle.SPELL),
	SPELL_INSTANT (Particle.SPELL_INSTANT), @Deprecated INSTANT_SPELL (Particle.SPELL_INSTANT),
	SPELL_MOB (Particle.SPELL_MOB), @Deprecated MOB_SPELL (Particle.SPELL_MOB),
	SPELL_MOB_AMBIENT (GeneralMethods.getMCVersion() >= 1205 ? Particle.SPELL_MOB : Particle.SPELL_MOB_AMBIENT),
	@Deprecated MOB_SPELL_AMBIENT (GeneralMethods.getMCVersion() >= 1205 ? Particle.SPELL_MOB : Particle.SPELL_MOB_AMBIENT),
	SPELL_WITCH (Particle.SPELL_WITCH), @Deprecated WITCH_SPELL (Particle.SPELL_WITCH),
	SPIT (Particle.SPIT),
	SQUID_INK (Particle.SQUID_INK),
	SUSPENDED (Particle.SUSPENDED), @Deprecated SUSPEND (Particle.SUSPENDED),
	SUSPENDED_DEPTH (Particle.SUSPENDED_DEPTH), @Deprecated DEPTH_SUSPEND (Particle.SUSPENDED_DEPTH),
	SWEEP_ATTACK (Particle.SWEEP_ATTACK),
	TOTEM (Particle.TOTEM),
	TOWN_AURA (Particle.TOWN_AURA),
	VILLAGER_ANGRY (Particle.VILLAGER_ANGRY), @Deprecated ANGRY_VILLAGER (Particle.VILLAGER_ANGRY),
	VILLAGER_HAPPY (Particle.VILLAGER_HAPPY), @Deprecated HAPPY_VILLAGER (Particle.VILLAGER_HAPPY),
	WARPED_SPORE (Particle.WARPED_SPORE),
	WATER_BUBBLE (Particle.WATER_BUBBLE), @Deprecated BUBBLE (Particle.WATER_BUBBLE),
	WATER_DROP (Particle.WATER_DROP),
	WATER_SPLASH (Particle.WATER_SPLASH), @Deprecated SPLASH (Particle.WATER_SPLASH),
	WATER_WAKE (Particle.WATER_WAKE), @Deprecated WAKE (Particle.WATER_WAKE),
	WHITE_ASH (Particle.WHITE_ASH);
	
	Particle particle;
	Class<?> dataClass;
	
	private ParticleEffect(Particle particle) {
		this.particle = particle;
		this.dataClass = particle.getDataType();
	}
	
	public Particle getParticle() {
		return particle;
	}
	
	/**
	 * Displays the particle at the specified location without offsets
	 * @param loc Location to display the particle at
	 * @param amount how many of the particle to display
	 */
	public void display(Location loc, int amount) {
		display(loc, amount, 0, 0, 0);
	}
	
	/**
	 * Displays the particle at the specified location with no extra data
	 * @param loc Location to spawn the particle
	 * @param amount how many of the particle to spawn
	 * @param offsetX random offset on the x axis
	 * @param offsetY random offset on the y axis
	 * @param offsetZ random offset on the z axis
	 */
	public void display(Location loc, int amount, double offsetX, double offsetY, double offsetZ) {
		display(loc, amount, offsetX, offsetY, offsetZ, 0);
	}
	
	/**
	 * Displays the particle at the specified location with extra data
	 * @param loc Location to spawn the particle
	 * @param amount how many of the particle to spawn
	 * @param offsetX random offset on the x axis
	 * @param offsetY random offset on the y axis
	 * @param offsetZ random offset on the z axis
	 * @param extra extra data to affect the particle, usually affects speed or does nothing
	 */
	public void display(Location loc, int amount, double offsetX, double offsetY, double offsetZ, double extra) {
		loc.getWorld().spawnParticle(particle, loc, amount, offsetX, offsetY, offsetZ, extra, null, true);
	}
	
	/**
	 * Displays the particle at the specified location with data
	 * @param loc Location to spawn the particle
	 * @param amount how many of the particle to spawn
	 * @param offsetX random offset on the x axis
	 * @param offsetY random offset on the y axis
	 * @param offsetZ random offset on the z axis
	 * @param data data to display the particle with, only applicable on several particle types (check the enum)
	 */
	public void display(Location loc, int amount, double offsetX, double offsetY, double offsetZ, Object data) {
		display(loc, amount, offsetX, offsetY, offsetZ, 0, data);
	}
	
	/**
	 * Displays the particle at the specified location with regular and extra data
	 * @param loc Location to spawn the particle
	 * @param amount how many of the particle to spawn
	 * @param offsetX random offset on the x axis
	 * @param offsetY random offset on the y axis
	 * @param offsetZ random offset on the z axis
	 * @param extra extra data to affect the particle, usually affects speed or does nothing
	 * @param data data to display the particle with, only applicable on several particle types (check the enum)
	 */
	public void display(Location loc, int amount, double offsetX, double offsetY, double offsetZ, double extra, Object data) {
		if (dataClass.isAssignableFrom(Void.class) || data == null || !dataClass.isAssignableFrom(data.getClass())) {
			display(loc, amount, offsetX, offsetY, offsetZ, extra);
		} else {
			loc.getWorld().spawnParticle(particle, loc, amount, offsetX, offsetY, offsetZ, extra, data, true);
		}
	}
}

package com.projectkorra.projectkorra.util;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle.DustTransition;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

public enum ParticleEffect {
	
	ASH,
	
	/**
	 * Uses BLOCK_MARKER with BARRIER block data
	 */
	BARRIER (Particle.BLOCK_MARKER, Material.BARRIER.createBlockData()),
	
	/**
	 * Applicable data: {@link BlockData}, defaults to DIRT block data
	 */
	BLOCK_CRACK (Material.DIRT.createBlockData()),
	
	/**
	 * Applicable data: {@link BlockData}, defaults to DIRT block data
	 */
	BLOCK_DUST (Material.DIRT.createBlockData()),

	/**
	 * Applicable data: {@link BlockData}, defaults to DIRT block data
	 */
	BLOCK_MARKER (Material.DIRT.createBlockData()),
	BUBBLE_COLUMN_UP,
	BUBBLE_POP,
	CAMPFIRE_COSY_SMOKE,
	CAMPFIRE_SIGNAL_SMOKE,
	CLOUD,
	COMPOSTER,
	CRIMSON_SPORE,
	CRIT,
	CRIT_MAGIC,
	CURRENT_DOWN,
	DAMAGE_INDICATOR,
	DOLPHIN,
	DRAGON_BREATH,
	DRIP_LAVA,
	DRIP_WATER,
	DRIPPING_DRIPSTONE_LAVA,
	DRIPPING_DRIPSTONE_WATER,
	DRIPPING_HONEY,
	DRIPPING_OBSIDIAN_TEAR,

	/**
	 * Applicable data: {@link DustTransition}, defaults to a blue -> green transition of size 1
	 */
	DUST_COLOR_TRANSITION (new DustTransition(Color.BLUE, Color.GREEN, 1.0f)),
	ELECTRIC_SPARK,
	ENCHANTMENT_TABLE,
	END_ROD,
	EXPLOSION_HUGE,
	EXPLOSION_LARGE,
	EXPLOSION_NORMAL,
	FALLING_DRIPSTONE_LAVA,
	FALLING_DRIPSTONE_WATER,

	/**
	 * Applicable data: {@link BlockData}, defaults to DIRT block data
	 */
	FALLING_DUST (Material.DIRT.createBlockData()),
	FALLING_HONEY,
	FALLING_LAVA,
	FALLING_NECTAR,
	FALLING_OBSIDIAN_TEAR,
	FALLING_SPORE_BLOSSOM,
	FALLING_WATER,
	FIREWORKS_SPARK,
	FLAME,
	FLASH,
	GLOW,
	GLOW_SQUID_INK,
	HEART,
	
	/**
	 * Applicable data: {@link ItemStack}, defaults to DIRT item stack
	 */
	ITEM_CRACK (new ItemStack(Material.DIRT)),
	LANDING_HONEY,
	LANDING_LAVA,
	LANDING_OBSIDIAN_TEAR,
	LAVA,
	MOB_APPEARANCE,
	NAUTILUS,
	NOTE,
	PORTAL,
	
	/**
	 * Applicable data: {@link DustOptions}, defaults to red color of size 1
	 */
	REDSTONE (new DustOptions(Color.RED, 1.0f)),
	REVERSE_PORTAL,
	SCRAPE,
	SLIME,
	SMOKE_NORMAL, 
	SMOKE_LARGE,
	SNEEZE,
	SNOW_SHOVEL,
	SNOWBALL,
	SNOWFLAKE,
	SOUL,
	SOUL_FIRE_FLAME,
	SPELL,
	SPELL_INSTANT,
	SPELL_MOB,
	SPELL_MOB_AMBIENT,
	SPELL_WITCH,
	SPIT,
	SPORE_BLOSSOM_AIR,
	SQUID_INK,
	SUSPENDED,
	SUSPENDED_DEPTH,
	SWEEP_ATTACK,
	TOTEM,
	TOWN_AURA,

	/**
	 * Applicable data: {@link Vibration}, has no default meaning it requires data each time.
	 * Location and offsets will be ignored by spigot
	 */
	VIBRATION,
	VILLAGER_ANGRY,
	VILLAGER_HAPPY,
	WARPED_SPORE,
	WATER_BUBBLE,
	WATER_DROP,
	WATER_SPLASH,
	WATER_WAKE,
	WAX_OFF,
	WAX_ON,
	WHITE_ASH;
	
	Particle particle;
	Object defaultData = null;
	
	private ParticleEffect() {
		this.particle = Particle.valueOf(this.toString());
	}

	private ParticleEffect(Object data) {
		this();
		this.defaultData = data;
	}

	private ParticleEffect(Particle particle) {
		this.particle = particle;
	}

	private ParticleEffect(Particle particle, Object data) {
		this(particle);
		this.defaultData = data;
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
		display(loc, amount, 0, 0, 0, 0, null);
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
		display(loc, amount, offsetX, offsetY, offsetZ, 0, null);
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
		display(loc, amount, offsetX, offsetY, offsetZ, extra, null);
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
		if (defaultData != null && data == null) {
			data = defaultData;
		}

		loc.getWorld().spawnParticle(particle, loc, amount, offsetX, offsetY, offsetZ, extra, data, true);
	}
}

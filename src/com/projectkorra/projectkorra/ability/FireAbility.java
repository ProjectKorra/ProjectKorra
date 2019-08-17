package com.projectkorra.projectkorra.ability;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;
import com.projectkorra.projectkorra.configuration.configs.properties.FirePropertiesConfig;
import com.projectkorra.projectkorra.firebending.BlazeArc;
import com.projectkorra.projectkorra.util.Information;
import com.projectkorra.projectkorra.util.ParticleEffect;

public abstract class FireAbility<C extends AbilityConfig> extends ElementalAbility<C> {

	private static final Map<Location, Information> TEMP_FIRE = new ConcurrentHashMap<Location, Information>();

	public FireAbility(final C config, final Player player) {
		super(config, player);
	}

	@Override
	public boolean isIgniteAbility() {
		return true;
	}

	@Override
	public boolean isExplosiveAbility() {
		return true;
	}

	@Override
	public Element getElement() {
		return Element.FIRE;
	}

	@Override
	public void handleCollision(final Collision collision) {
		super.handleCollision(collision);
		if (collision.isRemovingFirst()) {
			ParticleEffect.BLOCK_CRACK.display(collision.getLocationFirst(), 10, 1, 1, 1, 0.1, Material.FIRE.createBlockData());
		}
	}

	/**
	 * Returns if fire is allowed to completely replace blocks or if it should
	 * place a temp fire block.
	 */
	public static boolean canFireGrief() {
		return ConfigManager.getConfig(FirePropertiesConfig.class).Griefing;
	}

	/**
	 * Creates a fire block meant to replace other blocks but reverts when the
	 * fire dissipates or is destroyed.
	 */
	public static void createTempFire(final Location loc) {
		if (ElementalAbility.isAir(loc.getBlock().getType())) {
			loc.getBlock().setType(Material.FIRE);
			return;
		}
		Information info = new Information();
		final long time = ConfigManager.getConfig(FirePropertiesConfig.class).RevertTicks + (long) ((new Random()).nextDouble() * ConfigManager.getConfig(FirePropertiesConfig.class).RevertTicks);
		if (TEMP_FIRE.containsKey(loc)) {
			info = TEMP_FIRE.get(loc);
		} else {
			info.setBlock(loc.getBlock());
			info.setLocation(loc);
			info.setState(loc.getBlock().getState());
		}
		info.setTime(time + System.currentTimeMillis());
		loc.getBlock().setType(Material.FIRE);
		TEMP_FIRE.put(loc, info);
	}

	public double getDayFactor(final double value) {
		return this.player != null ? value * getDayFactor() : 1;
	}

	public static double getDayFactor() {
		return ConfigManager.getConfig(FirePropertiesConfig.class).DayFactor;
	}

	/**
	 * Gets the firebending dayfactor from the config multiplied by a specific
	 * value if it is day.
	 *
	 * @param value The value
	 * @param world The world to pass into {@link #isDay(World)}
	 * @return value DayFactor multiplied by specified value when
	 *         {@link #isDay(World)} is true <br />
	 *         else <br />
	 *         value The specified value in the parameters
	 */
	public static double getDayFactor(final double value, final World world) {
		if (isDay(world)) {
			return value * getDayFactor();
		}
		return value;
	}

	public static boolean isIgnitable(final Block block) {
		return block != null ? isIgnitable(block.getType()) : false;
	}

	public static boolean isIgnitable(final Material material) {
		return material.isFlammable() || material.isBurnable();
	}

	/**
	 * This method was used for the old collision detection system. Please see
	 * {@link Collision} for the new system.
	 * <p>
	 * Checks whether a location is within a FireShield.
	 *
	 * @param loc The location to check
	 * @return true If the location is inside a FireShield.
	 */
	@Deprecated
	public static boolean isWithinFireShield(final Location loc) {
		final List<String> list = new ArrayList<String>();
		list.add("FireShield");
		return GeneralMethods.blockAbilities(null, list, loc, 0);
	}

	public static void playCombustionSound(final Location loc) {
		FirePropertiesConfig fire = ConfigManager.getConfig(FirePropertiesConfig.class);
		
		if (fire.PlaySound) {
			loc.getWorld().playSound(loc, fire.CombustionSoundType, fire.CombustionSoundVolume, fire.CombustionSoundPitch);
		}
	}

	public static void playFirebendingParticles(final Location loc, final int amount, final double xOffset, final double yOffset, final double zOffset) {
		ConfigManager.getConfig(FirePropertiesConfig.class).Particles.display(loc, amount, xOffset, yOffset, zOffset);
	}

	public static void playFirebendingSound(final Location loc) {
		FirePropertiesConfig fire = ConfigManager.getConfig(FirePropertiesConfig.class);
		
		if (fire.PlaySound) {
			loc.getWorld().playSound(loc, fire.SoundType, fire.SoundVolume, fire.SoundPitch);
		}
	}

	public static void playLightningbendingParticle(final Location loc) {
		playLightningbendingParticle(loc, Math.random(), Math.random(), Math.random());
	}

	public static void playLightningbendingParticle(final Location loc, final double xOffset, final double yOffset, final double zOffset) {
		GeneralMethods.displayColoredParticle("#01E1FF", loc, 1, xOffset, yOffset, zOffset);
	}

	public static void playLightningbendingSound(final Location loc) {
		FirePropertiesConfig fire = ConfigManager.getConfig(FirePropertiesConfig.class);
		
		if (fire.PlaySound) {
			loc.getWorld().playSound(loc, fire.LightningSoundType, fire.LightningSoundVolume, fire.LightningSoundPitch);
		}
	}

	/** Removes all temp fire that no longer needs to be there */
	public static void removeFire() {
		final Iterator<Location> it = TEMP_FIRE.keySet().iterator();
		while (it.hasNext()) {
			final Location loc = it.next();
			final Information info = TEMP_FIRE.get(loc);
			if (info.getLocation().getBlock().getType() != Material.FIRE && !ElementalAbility.isAir(info.getLocation().getBlock().getType())) {
				revertTempFire(loc);
			} else if (ElementalAbility.isAir(info.getBlock().getType()) || System.currentTimeMillis() > info.getTime()) {
				revertTempFire(loc);
			}
		}
	}

	/**
	 * Revert the temp fire at the location if any is there.
	 *
	 * @param location The Location
	 */
	public static void revertTempFire(final Location location) {
		if (!TEMP_FIRE.containsKey(location)) {
			return;
		}
		final Information info = TEMP_FIRE.get(location);
		if (info.getLocation().getBlock().getType() != Material.FIRE && !ElementalAbility.isAir(info.getLocation().getBlock().getType())) {
			if (info.getState().getType().isBurnable() && !info.getState().getType().isOccluding()) {
				final ItemStack itemStack = new ItemStack(info.getState().getType(), 1);
				info.getState().getBlock().getWorld().dropItemNaturally(info.getLocation(), itemStack);
			}
		} else {
			info.getBlock().setType(info.getState().getType());
			info.getBlock().setBlockData(info.getState().getBlockData());
		}
		TEMP_FIRE.remove(location);
	}

	public static void stopBending() {
		BlazeArc.removeAllCleanup();
		for (final Location loc : TEMP_FIRE.keySet()) {
			revertTempFire(loc);
		}
	}

}

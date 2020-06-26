package com.projectkorra.projectkorra.ability;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.Information;
import com.projectkorra.projectkorra.util.ParticleEffect;

public abstract class FireAbility extends ElementalAbility {

	private static final Map<Location, Information> TEMP_FIRE = new ConcurrentHashMap<Location, Information>();
	private static final Map<Block, Player> SOURCE_PLAYERS = new ConcurrentHashMap<>();
	
	public FireAbility(final Player player) {
		super(player);
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
		Material fireType = this.bPlayer.canUseSubElement(SubElement.BLUE_FIRE) ? Material.SOUL_FIRE : Material.FIRE;
		if (collision.isRemovingFirst()) {
			ParticleEffect.BLOCK_CRACK.display(collision.getLocationFirst(), 10, 1, 1, 1, 0.1, fireType.createBlockData());
		}
	}

	/**
	 * Returns if fire is allowed to completely replace blocks or if it should
	 * place a temp fire block.
	 */
	public static boolean canFireGrief() {
		return getConfig().getBoolean("Properties.Fire.FireGriefing");
	}

	/**
	 * Creates a fire block meant to replace other blocks but reverts when the
	 * fire dissipates or is destroyed.
	 */
	public void createTempFire(final Location loc) {
		createTempFire(loc, getConfig().getLong("Properties.Fire.RevertTicks") + (long) ((new Random()).nextDouble() * getConfig().getLong("Properties.Fire.RevertTicks")));
	}
	
	
	public void createTempFire(final Location loc, final long time) {
		Material fireType = this.getBendingPlayer().canUseSubElement(SubElement.BLUE_FIRE) ? Material.SOUL_FIRE : Material.FIRE;
		
		
		if (ElementalAbility.isAir(loc.getBlock().getType())) {
			loc.getBlock().setType(fireType);
			return;
		}
		Information info = new Information();
		if (TEMP_FIRE.containsKey(loc)) {
			info = TEMP_FIRE.get(loc);
		} else {
			info.setBlock(loc.getBlock());
			info.setLocation(loc);
			info.setState(loc.getBlock().getState());
		}
		info.setTime(time + System.currentTimeMillis());
		loc.getBlock().setType(fireType);
		TEMP_FIRE.put(loc, info);
		SOURCE_PLAYERS.put(loc.getBlock(), this.getPlayer());
	}

	public double getDayFactor(final double value) {
		return this.player != null ? value * getDayFactor() : 1;
	}

	public static double getDayFactor() {
		return getConfig().getDouble("Properties.Fire.DayFactor");
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

	public static ChatColor getSubChatColor() {
		return ChatColor.valueOf(ConfigManager.getConfig().getString("Properties.Chat.Colors.FireSub"));
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
		if (getConfig().getBoolean("Properties.Fire.PlaySound")) {
			final float volume = (float) getConfig().getDouble("Properties.Fire.CombustionSound.Volume");
			final float pitch = (float) getConfig().getDouble("Properties.Fire.CombustionSound.Pitch");

			Sound sound = Sound.ENTITY_FIREWORK_ROCKET_BLAST;

			try {
				sound = Sound.valueOf(getConfig().getString("Properties.Fire.CombustionSound.Sound"));
			} catch (final IllegalArgumentException exception) {
				ProjectKorra.log.warning("Your current value for 'Properties.Fire.CombustionSound.Sound' is not valid.");
			} finally {
				loc.getWorld().playSound(loc, sound, volume, pitch);
			}
		}
	}

	public void playFirebendingParticles(final Location loc, final int amount, final double xOffset, final double yOffset, final double zOffset) {
		if (this.getBendingPlayer().canUseSubElement(SubElement.BLUE_FIRE)) {
			ParticleEffect.SOUL_FIRE_FLAME.display(loc, amount, xOffset, yOffset, zOffset);
		} else {
			ParticleEffect.FLAME.display(loc, amount, xOffset, yOffset, zOffset);
		}
	}

	public static void playFirebendingSound(final Location loc) {
		if (getConfig().getBoolean("Properties.Fire.PlaySound")) {
			final float volume = (float) getConfig().getDouble("Properties.Fire.FireSound.Volume");
			final float pitch = (float) getConfig().getDouble("Properties.Fire.FireSound.Pitch");

			Sound sound = Sound.BLOCK_FIRE_AMBIENT;

			try {
				sound = Sound.valueOf(getConfig().getString("Properties.Fire.FireSound.Sound"));
			} catch (final IllegalArgumentException exception) {
				ProjectKorra.log.warning("Your current value for 'Properties.Fire.FireSound.Sound' is not valid.");
			} finally {
				loc.getWorld().playSound(loc, sound, volume, pitch);
			}
		}
	}

	public static void playLightningbendingParticle(final Location loc) {
		playLightningbendingParticle(loc, Math.random(), Math.random(), Math.random());
	}

	public static void playLightningbendingParticle(final Location loc, final double xOffset, final double yOffset, final double zOffset) {
		GeneralMethods.displayColoredParticle("#01E1FF", loc, 1, xOffset, yOffset, zOffset);
	}

	public static void playLightningbendingSound(final Location loc) {
		if (getConfig().getBoolean("Properties.Fire.PlaySound")) {
			final float volume = (float) getConfig().getDouble("Properties.Fire.LightningSound.Volume");
			final float pitch = (float) getConfig().getDouble("Properties.Fire.LightningSound.Pitch");

			Sound sound = Sound.ENTITY_CREEPER_HURT;

			try {
				sound = Sound.valueOf(getConfig().getString("Properties.Fire.LightningSound.Sound"));
			} catch (final IllegalArgumentException exception) {
				ProjectKorra.log.warning("Your current value for 'Properties.Fire.LightningSound.Sound' is not valid.");
			} finally {
				loc.getWorld().playSound(loc, sound, volume, pitch);
			}
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
		if (!isFire(info.getLocation().getBlock().getType()) && !ElementalAbility.isAir(info.getLocation().getBlock().getType())) {
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
		for (final Location loc : TEMP_FIRE.keySet()) {
			revertTempFire(loc);
		}
	}
	
	public static Map<Location, Information> getTempFire() {
		return TEMP_FIRE;
	}

	public static Map<Block, Player> getSourcePlayers() {
		return SOURCE_PLAYERS;
	}

}

package com.projectkorra.projectkorra.ability;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

public abstract class FireAbility extends ElementalAbility {

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
		if (collision.isRemovingFirst()) {
			ParticleEffect.BLOCK_CRACK.display(collision.getLocationFirst(), 10, 1, 1, 1, 0.1, getFireColor().createBlockData());
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
	 * Creates a fire TempBlock at the given location with a duration
	 * @param loc 
	 * @param bPlayer 
	 * @return created TempBlock
	 */
	public static TempBlock createTempFire(Location loc, BendingPlayer bPlayer) {
		TempBlock tb = new TempBlock(loc.getBlock(), getFireColor(bPlayer));
		tb.setRevertTime(ConfigManager.getConfig().getLong("Properties.Fire.RevertTicks"));
		return tb;
	}
	
	public static TempBlock createTempFire(Block block, BendingPlayer bPlayer) {
		TempBlock tb = new TempBlock(block, getFireColor(bPlayer));
		tb.setRevertTime(ConfigManager.getConfig().getLong("Properties.Fire.RevertTicks"));
		return tb;
	}
	
	public TempBlock createTempFire(Location loc) {
		TempBlock tb = new TempBlock(loc.getBlock(), getFireColor(bPlayer));
		tb.setRevertTime(ConfigManager.getConfig().getLong("Properties.Fire.RevertTicks"));
		return tb;
	}
	
	public TempBlock createTempFire(Block block) {
		TempBlock tb = new TempBlock(block, getFireColor(bPlayer));
		tb.setRevertTime(ConfigManager.getConfig().getLong("Properties.Fire.RevertTicks"));
		return tb;
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
	
	/**
	 * Get the Material based on the color of the given BendingPlayer's firebending
	 * @param bPlayer checked BendingPlayer
	 * @return blue fire if they have blue fire subelement, otherwise normal fire
	 */
	public static Material getFireColor(final BendingPlayer bPlayer) {
		if (bPlayer == null) {
			return Material.FIRE;
		} 
		
		return bPlayer.hasSubElement(Element.BLUE_FIRE) ? Material.SOUL_FIRE : Material.FIRE;
	}
	
	/**
	 * Get the Material based on the color of the ability's BendingPlayer's firebending
	 * @return blue fire if they have blue fire subelement, otherwise normal fire
	 */
	public Material getFireColor() {
		if (bPlayer == null) {
			return Material.FIRE;
		}
		
		return bPlayer.hasSubElement(Element.BLUE_FIRE) ? Material.SOUL_FIRE : Material.FIRE;
	}
	
	/**
	 * Get the ParticleEffect based on the color of the ability's BendingPlayer's firebending
	 * @param bPlayer checked BendingPlayer
	 * @return blue fire if they have blue fire subelement, otherwise normal fire
	 */
	public static ParticleEffect getFireParticle(final BendingPlayer bPlayer) {
		if (bPlayer == null) {
			return ParticleEffect.FLAME;
		}
		
		return bPlayer.hasSubElement(Element.BLUE_FIRE) ? ParticleEffect.SOUL_FLAME : ParticleEffect.FLAME;
	}
	
	/**
	 * Get the ParticleEffect based on the color of the ability's BendingPlayer's firebending
	 * @return blue fire if they have blue fire subelement, otherwise normal fire
	 */
	public ParticleEffect getFireParticle() {
		if (bPlayer == null) {
			return ParticleEffect.FLAME;
		}
		
		return bPlayer.hasSubElement(Element.BLUE_FIRE) ? ParticleEffect.SOUL_FLAME : ParticleEffect.FLAME;
	}

	public static ChatColor getSubChatColor() {
		return ChatColor.valueOf(ConfigManager.getConfig().getString("Properties.Chat.Colors.FireSub"));
	}
	
	/**
	 * Gets the topmost ignitable block within the specified range from the given block
	 * @param block checked block and center of range
	 * @param range y-axis range from given block to check within
	 * @return null if no ignitable block found in the range
	 */
	public static Block getIgnitable(final Block block, final int range) {
		return getIgnitable(block, range, range);
	}
	
	/**
	 * Gets the topmost ignitable block within the specified range from the given block
	 * @param block checked block and center of range
	 * @param up up on the y-axis from the block to check
	 * @param down down on the y-axis from the block to check
	 * @return null if no ignitable block found in the range
	 */
	public static Block getIgnitable(final Block block, final int up, final int down) {
		Block top = GeneralMethods.getTopBlock(block.getLocation(), up, down).getRelative(BlockFace.UP);
		
		if (isIgnitable(top)) {
			return top;
		}
		
		return null;
	}
	
	/**
	 * Checks if the given block can be ignited
	 * @param block checked block
	 * @return true if fire can be placed in a mostly vanilla fashion at the block
	 */
	public static boolean isIgnitable(final Block block) {
		if (block == null) {
			return false;
		} 
		
		return block.isPassable() && !block.isLiquid() && GeneralMethods.isSolid(block.getRelative(BlockFace.DOWN));
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
	
	public void playFirebendingParticles(final Location loc, final int amount, final double xOffset, final double yOffset, final double zOffset, final double speed) {
		getFireParticle().display(loc, amount, xOffset, yOffset, zOffset, speed);
	}
	
	public void playFirebendingParticles(final Location loc, final int amount, final double xOffset, final double yOffset, final double zOffset) {
		getFireParticle().display(loc, amount, xOffset, yOffset, zOffset, 0.012);
	}

	public void playFirebendingParticles(final Location loc, final int amount) {
		getFireParticle().display(loc, amount, 0, 0, 0, 0.012);
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

}

package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.LightManager;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Fire;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class FireAbility extends ElementalAbility {

	private static final Map<Block, Player> SOURCE_PLAYERS = new ConcurrentHashMap<>();
	private static final Set<BlockFace> IGNITE_FACES = new HashSet<>(Arrays.asList(BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.UP));

	public FireAbility(final Player player) {
		super(player);
	}

	@Override
	public boolean isIgniteAbility() {
		return true;
	}

	@Override
	public boolean isExplosiveAbility() {
		return false;
	}

	@Override
	public Element getElement() {
		return Element.FIRE;
	}

	@Override
	public void handleCollision(final Collision collision) {
		super.handleCollision(collision);
		if (collision.isRemovingFirst()) {
			ParticleEffect.BLOCK_CRACK.display(collision.getLocationFirst(), 10, 1, 1, 1, 0.1, getFireType().createBlockData());
		}
	}
	/**
	 * 
	 * @return Material based on whether the player is a Blue Firebender, SOUL_FIRE if true, FIRE if false.
	 */
	public Material getFireType() {
		return getBendingPlayer().canUseSubElement(SubElement.BLUE_FIRE) ? Material.SOUL_FIRE : Material.FIRE;
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
	public void createTempFire(final Location location) {
		createTempFire(location, getConfig().getLong("Properties.Fire.RevertTicks") + (long) ((new Random()).nextDouble() * getConfig().getLong("Properties.Fire.RevertTicks")));
	}

	public void createTempFire(final Location location, final long time) {
		Block block = location.getBlock();
		if (isIgnitable(block)) {
			new TempBlock(block, createFireState(block, getFireType() == Material.SOUL_FIRE), time);
			SOURCE_PLAYERS.put(block, this.getPlayer());
		}
	}

	public double getDayFactor(final double value) {
		return this.player != null ? value * getDayFactor(player.getWorld()) : value;
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
		return isDay(world) ? value * getDayFactor() : value;
	}

	public static double getDayFactor(final World world) {
		return getDayFactor(1, world);
	}

	public static ChatColor getSubChatColor() {
		return ChatColor.valueOf(ConfigManager.getConfig().getString("Properties.Chat.Colors.FireSub"));
	}

	/**
	 * Can fire be placed in the provided block
	 * @param block The block to check
	 * @return True if fire can be placed here
	 */
	public static boolean isIgnitable(final Block block) {
		Block support = block.getRelative(BlockFace.DOWN);
		Location loc = support.getLocation();
		boolean supported = support.getBoundingBox().overlaps(loc.add(0, 0.8, 0).toVector(), loc.add(1, 1, 1).toVector());
		return (!isWater(block) && !block.isLiquid() && GeneralMethods.isTransparent(block)) && ((supported && support.getType().isSolid())
				|| (IGNITE_FACES.stream().map(face -> block.getRelative(face).getType()).anyMatch(FireAbility::isIgnitable)));
	}

	public static boolean isIgnitable(final Material material) {
		return material.isFlammable() || material.isBurnable();
	}

	/**
	 * Create a fire block with the correct blockstate at the given position
	 * @param position The position to test
	 * @param blue If its soul fire or not
	 * @return The fire blockstate
	 */
	public static BlockData createFireState(Block position, boolean blue) {
		Fire fire = (Fire) Material.FIRE.createBlockData();
		
		if (isIgnitable(position) && position.getRelative(BlockFace.DOWN).getType().isSolid()) {
			return (blue) ? Material.SOUL_FIRE.createBlockData() : fire; //Default fire for when there is a solid block bellow
		}

		for (BlockFace face : IGNITE_FACES) {
			fire.setFace(face, isIgnitable(position.getRelative(face)));
		}

		return fire;
	}

	public static void dryWetBlocks(final Block block, final CoreAbility ability, boolean playSound) {
		if (GeneralMethods.isRegionProtectedFromBuild(ability, block.getLocation())) {
			return;
		}
		if (block.getType() == Material.WET_SPONGE) {
			block.setType(Material.SPONGE);

			if (playSound) {
				block.getWorld().playSound(block.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5F, 1);
			}
		} else if (isSnow(block)) {
			block.getWorld().spawnParticle(Particle.BLOCK_DUST, block.getLocation().add(0.5, 0.5, 0.5), 2, 0.5, 0.5, 0.5, 0.1, Material.SNOW_BLOCK.createBlockData());
			block.setType(Material.AIR);

			if (playSound) {
				block.getWorld().playSound(block.getLocation(), Sound.BLOCK_SNOW_BREAK, 1, 1);
			}
		}
	}

	public static void dryWetBlocks(final Block block, final CoreAbility ability) {
		dryWetBlocks(block, ability, false);
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
		return GeneralMethods.blockAbilities(null, List.of("FireShield"), loc, 0);
	}

	public static void playCombustionSound(final Location loc) {
        if (!getConfig().getBoolean("Properties.Fire.PlaySound")) {
            return;
        }

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

	/**
	 * Emits a dynamic firebending light at the specified location. This light will
	 * remain active for a configurable amount of time before fading out. The brightness
	 * and keep-alive time are configurable via the plugin's configuration file.
	 *
	 * <p>The dynamic light feature must be enabled in the configuration for this method to take effect.
	 * Brightness should be between 1 and 15, with 1 being the dimmest and 15 the brightest.
	 * The keep-alive time is specified in milliseconds and determines how long the light persists.
	 *
	 * <p>Configuration file paths:
	 * <ul>
	 *     <li><b>Properties.Fire.DynamicLight.Enabled</b>: Determines if dynamic light is enabled.</li>
	 *     <li><b>Properties.Fire.DynamicLight.Brightness</b>: Sets the light brightness (1-15).</li>
	 *     <li><b>Properties.Fire.DynamicLight.KeepAlive</b>: Specifies how long the light remains before fading (in ms).</li>
	 * </ul>
	 *
	 * @param location the {@link Location} where the firebending light will be emitted
	 * @throws IllegalArgumentException if the brightness is outside the valid range (1-15)
	 */
	public void emitFirebendingLight(final Location location) {
        if (!getConfig().getBoolean("Properties.Fire.DynamicLight.Enabled")) {
            return;
        }

        int brightness = getConfig().getInt("Properties.Fire.DynamicLight.Brightness");
        long keepAlive = getConfig().getLong("Properties.Fire.DynamicLight.KeepAlive");
        if (brightness < 1 || brightness > 15) {
            throw new IllegalArgumentException("Properties.Fire.DynamicLight.Brightness must be between 1 and 15.");
        }

        LightManager.createLight(location).brightness(brightness).timeUntilFadeout(keepAlive).emit();
    }

	public void playFirebendingParticles(final Location loc, final int amount, final double xOffset, final double yOffset, final double zOffset) {
		if (this.getBendingPlayer().canUseSubElement(SubElement.BLUE_FIRE)) {
			ParticleEffect.SOUL_FIRE_FLAME.display(loc, amount, xOffset, yOffset, zOffset);
		} else {
			ParticleEffect.FLAME.display(loc, amount, xOffset, yOffset, zOffset);
		}
	}

	public static void playFirebendingSound(final Location loc) {
        if (!getConfig().getBoolean("Properties.Fire.PlaySound")) {
            return;
        }

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

	public static void playLightningbendingParticle(final Location loc) {
		playLightningbendingParticle(loc, Math.random(), Math.random(), Math.random());
	}

	public static void playLightningbendingParticle(final Location loc, final double xOffset, final double yOffset, final double zOffset) {
		GeneralMethods.displayColoredParticle("#01E1FF", loc, 1, xOffset, yOffset, zOffset);
	}

	public static void playLightningbendingSound(final Location loc) {
        if (!getConfig().getBoolean("Properties.Fire.PlaySound")) {
            return;
        }

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

	public static void playLightningbendingChargingSound(final Location loc) {
        if (!getConfig().getBoolean("Properties.Fire.PlaySound")) {
            return;
        }

        final float volume = (float) getConfig().getDouble("Properties.Fire.LightningCharge.Volume");
        final float pitch = (float) getConfig().getDouble("Properties.Fire.LightningCharge.Pitch");
        Sound sound = Sound.BLOCK_BEEHIVE_WORK;

        try {
            sound = Sound.valueOf(getConfig().getString("Properties.Fire.LightningCharge.Sound"));
        } catch (final IllegalArgumentException exception) {
            ProjectKorra.log.warning("Your current value for 'Properties.Fire.LightningCharge.Sound' is not valid.");
        } finally {
            loc.getWorld().playSound(loc, sound, volume, pitch);
        }
    }
	
	public static void playLightningbendingHitSound(final Location loc) {
        if (!getConfig().getBoolean("Properties.Fire.PlaySound")) {
            return;
        }

        final float volume = (float) getConfig().getDouble("Properties.Fire.LightningHit.Volume");
        final float pitch = (float) getConfig().getDouble("Properties.Fire.LightningHit.Pitch");
        Sound sound = Sound.ENTITY_LIGHTNING_BOLT_THUNDER;

        try {
            sound = Sound.valueOf(getConfig().getString("Properties.Fire.LightningHit.Sound"));
        } catch (final IllegalArgumentException exception) {
            ProjectKorra.log.warning("Your current value for 'Properties.Fire.LightningHit.Sound' is not valid.");
        } finally {
            loc.getWorld().playSound(loc, sound, volume, pitch);
        }
    }

	/**
	 * Apply modifiers to this value. Applies the day factor to it
	 * @param value The value to modify
	 * @return The modified value
	 */
	@Override
	@Deprecated
	public double applyModifiers(double value) {
		return GeneralMethods.applyModifiers(value, getDayFactor(1.0));
	}

	/**
	 * Apply modifiers to this value. Applies the day factor to it
	 * @param value The value to modify
	 * @return The modified value
	 */
	@Deprecated
	public double applyInverseModifiers(double value) {
		return GeneralMethods.applyInverseModifiers(value, getDayFactor(1.0));
	}

	/**
	 * Apply modifiers to this value. Applies the day factor and the blue fire factor (for damage)
	 * @param value The value to modify
	 * @return The modified value
	 */
	@Deprecated
	public double applyModifiersDamage(double value) {
		return GeneralMethods.applyModifiers(value, getDayFactor(1.0), bPlayer.hasElement(Element.BLUE_FIRE) ? getConfig().getDouble("Properties.Fire.BlueFire.DamageFactor", 1.1) : 1);
	}

	/**
	 * Apply modifiers to this value. Applies the day factor and the blue fire factor (for range)
	 * @param value The value to modify
	 * @return The modified value
	 */
	@Deprecated
	public double applyModifiersRange(double value) {
		return GeneralMethods.applyModifiers(value, getDayFactor(1.0), bPlayer.hasElement(Element.BLUE_FIRE) ? getConfig().getDouble("Properties.Fire.BlueFire.RangeFactor", 1.2) : 1);
	}

	/**
	 * Apply modifiers to this value. Applies the day factor and the blue fire factor (for cooldowns)
	 * @param value The value to modify
	 * @return The modified value
	 */
	@Deprecated
	public long applyModifiersCooldown(long value) {
		return GeneralMethods.applyInverseModifiers(value, getDayFactor(1.0), bPlayer.hasElement(Element.BLUE_FIRE) ? 1 / getConfig().getDouble("Properties.Fire.BlueFire.CooldownFactor", 0.9) : 1);
	}

	public static void stopBending() {
		SOURCE_PLAYERS.clear();
	}

	public static Map<Block, Player> getSourcePlayers() {
		return SOURCE_PLAYERS;
	}

}

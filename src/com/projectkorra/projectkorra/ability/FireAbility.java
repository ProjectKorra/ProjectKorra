package com.projectkorra.projectkorra.ability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.projectkorra.projectkorra.ability.functional.Functional;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Fire;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

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
    public void createTempFire(final Location loc) {
        createTempFire(loc, getConfig().getLong("Properties.Fire.RevertTicks") + (long) ((new Random()).nextDouble() * getConfig().getLong("Properties.Fire.RevertTicks")));
    }

    public void createTempFire(final Location loc, final long time) {
        if (isIgnitable(loc.getBlock())) {
            new TempBlock(loc.getBlock(), getFireType().createBlockData(), time);
            SOURCE_PLAYERS.put(loc.getBlock(), this.getPlayer());
        }
    }

    public double getDayFactor(final double value) {
        return (this.player != null ? value * getDayFactor(player.getWorld()) : value);
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
     * {@link #isDay(World)} is true <br />
     * else <br />
     * value The specified value in the parameters
     */
    public static double getDayFactor(final double value, final World world) {
        if (isDay(world)) {
            return value * getDayFactor();
        }
        return value;
    }

    public static double getDayFactor(final World world) {
        return getDayFactor(1, world);
    }

    public static ChatColor getSubChatColor() {
        return ChatColor.valueOf(ConfigManager.getConfig().getString("Properties.Chat.Colors.FireSub"));
    }

    /**
     * Can fire be placed in the provided block
     *
     * @param block The block to check
     * @return True if fire can be placed here
     */
    public static boolean isIgnitable(final Block block) {
        return (block.getRelative(BlockFace.DOWN).getType().isSolid() && !block.getType().isSolid())
                || (GeneralMethods.isTransparent(block) && IGNITE_FACES.stream().map(face -> block.getRelative(face).getType()).anyMatch(FireAbility::isIgnitable));
    }

    public static boolean isIgnitable(final Material material) {
        return material.isFlammable() || material.isBurnable();
    }

    /**
     * Create a fire block with the correct blockstate at the given position
     *
     * @param position The position to test
     * @param blue     If its soul fire or not
     * @return The fire blockstate
     */
    public static BlockData createFireState(Block position, boolean blue) {
        if (blue) return Material.SOUL_FIRE.createBlockData();

        Fire fire = (Fire) Material.FIRE.createBlockData();
        if (position.getRelative(BlockFace.DOWN).getType().isSolid())
            return fire; //Default fire for when there is a solid block bellow
        for (BlockFace face : IGNITE_FACES) {
            if (isIgnitable(position.getRelative(face).getType())) {
                fire.setFace(face, true);
            }
        }
        return fire;
    }

    public static Functional.Particle fireParticles = (ability, location, amount, xOffset, yOffset, zOffset, extra, data) -> {
        if (ability.getBendingPlayer().canUseSubElement(SubElement.BLUE_FIRE)) {
            ParticleEffect.SOUL_FIRE_FLAME.display(location, amount, xOffset, yOffset, zOffset);
        } else {
            ParticleEffect.FLAME.display(location, amount, xOffset, yOffset, zOffset);
        }
    };

    /**
     * Plays firebending particles in a location with given offsets.<br>
     *
     * @param ability The ability these particles are spawned for
     * @param loc     The location to use
     * @param amount  The amount of particles to use
     * @param xOffset The xOffset to use
     * @param yOffset The yOffset to use
     * @param zOffset The zOffset to use
     */
    public static void playFirebendingParticles(CoreAbility ability, final Location loc, final int amount, final double xOffset, final double yOffset, final double zOffset) {
        fireParticles.play(ability, loc, amount, xOffset, yOffset, zOffset, 0, null);
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

    /**
     * Apply modifiers to this value. Applies the day factor to it
     *
     * @param value The value to modify
     * @return The modified value
     */
    @Override
    public double applyModifiers(double value) {
        return GeneralMethods.applyModifiers(value, getDayFactor(1.0));
    }

    /**
     * Apply modifiers to this value. Applies the day factor to it
     *
     * @param value The value to modify
     * @return The modified value
     */
    public double applyInverseModifiers(double value) {
        return GeneralMethods.applyInverseModifiers(value, getDayFactor(1.0));
    }

    /**
     * Apply modifiers to this value. Applies the day factor and the blue fire factor (for damage)
     *
     * @param value The value to modify
     * @return The modified value
     */
    public double applyModifiersDamage(double value) {
        return GeneralMethods.applyModifiers(value, getDayFactor(1.0), bPlayer.hasElement(Element.BLUE_FIRE) ? getConfig().getDouble("Properties.Fire.BlueFire.DamageFactor", 1.1) : 1);
    }

    /**
     * Apply modifiers to this value. Applies the day factor and the blue fire factor (for range)
     *
     * @param value The value to modify
     * @return The modified value
     */
    public double applyModifiersRange(double value) {
        return GeneralMethods.applyModifiers(value, getDayFactor(1.0), bPlayer.hasElement(Element.BLUE_FIRE) ? getConfig().getDouble("Properties.Fire.BlueFire.RangeFactor", 1.2) : 1);
    }

    /**
     * Apply modifiers to this value. Applies the day factor and the blue fire factor (for cooldowns)
     *
     * @param value The value to modify
     * @return The modified value
     */
    public long applyModifiersCooldown(long value) {
        return (long) GeneralMethods.applyInverseModifiers(value, getDayFactor(1.0), bPlayer.hasElement(Element.BLUE_FIRE) ? 1 / getConfig().getDouble("Properties.Fire.BlueFire.CooldownFactor", 0.9) : 1);
    }

    public static void stopBending() {
        SOURCE_PLAYERS.clear();
    }

    public static Map<Block, Player> getSourcePlayers() {
        return SOURCE_PLAYERS;
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

    /**
     * Plays firebending particles in a location with given offsets.<br>
     *
     * @param loc     The location to use
     * @param amount  The amount of particles to use
     * @param xOffset The xOffset to use
     * @param yOffset The yOffset to use
     * @param zOffset The zOffset to use
     * @deprecated <b>Use {@link FireAbility#playFirebendingParticles(CoreAbility, Location, int, double, double, double)} instead.</b>
     */
    @Deprecated
    public void playFirebendingParticles(final Location loc, final int amount, final double xOffset, final double yOffset, final double zOffset) {
        playFirebendingParticles(this, loc, amount, xOffset, yOffset, zOffset);
    }

    /**
     * Plays a single lightning particle in a location.<br>
     *
     * @param loc The location to use
     * @deprecated <b>Use {@link LightningAbility#playLightningbendingParticles(CoreAbility, Location, int)} instead.
     */
    @Deprecated
    public static void playLightningbendingParticle(final Location loc) {
        playLightningbendingParticle(loc, Math.random(), Math.random(), Math.random());
    }

    /**
     * Plays a single lightning particle in a location with given offsets.<br>
     *
     * @param loc     The location to use
     * @param xOffset The xOffset to use
     * @param yOffset The yOffset to use
     * @param zOffset The zOffset to use
     * @deprecated <b>Use {@link LightningAbility#playLightningbendingParticles(CoreAbility, Location, int, double, double, double)} instead.
     */
    @Deprecated
    public static void playLightningbendingParticle(final Location loc, final double xOffset, final double yOffset, final double zOffset) {
        LightningAbility.playLightningbendingParticles(null, loc, 1, xOffset, yOffset, zOffset);
        //GeneralMethods.displayColoredParticle("#01E1FF", loc, 1, xOffset, yOffset, zOffset);
    }

    /**
     * @deprecated <b>Use {@link LightningAbility#playLightningbendingSound(Location)} instead.
     */
    @Deprecated
    public static void playLightningbendingSound(final Location loc) {
        LightningAbility.playLightningbendingSound(loc);
    }

    /**
     * @deprecated <b>Use {@link LightningAbility#playLightningbendingChargingSound(Location)} instead.
     */
    @Deprecated
    public static void playLightningbendingChargingSound(final Location loc) {
        LightningAbility.playLightningbendingChargingSound(loc);
    }

    /**
     * @deprecated <b>Use {@link LightningAbility#playLightningbendingHitSound(Location)} instead.
     */
    @Deprecated
    public static void playLightningbendingHitSound(final Location loc) {
        LightningAbility.playLightningbendingHitSound(loc);
    }

    /**
     * @deprecated <b>Use {@link CombustionAbility#playCombustionSound(Location)} instead.
     */
    @Deprecated
    public static void playCombustionSound(final Location loc) {
        CombustionAbility.playCombustionSound(loc);
    }

}

package com.projectkorra.ProjectKorra.Utilities;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.projectkorra.ProjectKorra.Utilities.ReflectionUtils.PackageType;
import com.projectkorra.ProjectKorra.Utilities.ReflectionUtils.PacketType;

/**
 * <b>ParticleEffect Library</b>
 * <p>
 * This library was created by @DarkBlade12 based on content related to particles of @microgeek (names and packet parameters), it allows you to display all Minecraft particle effects on a Bukkit server
 * <p>
 * You are welcome to use it, modify it and redistribute it under the following conditions:
 * <ul>
 * <li>Don't claim this class as your own
 * <li>Don't remove this disclaimer
 * </ul>
 * <p>
 * <i>It would be nice if you provide credit to me if you use this class in a published project</i>
 *
 * @author DarkBlade12
 * @version 1.5
 */
public enum ParticleEffect {
	/**
	 * A particle effect which is displayed by exploding tnt and creepers:
	 * <ul>
	 * <li>It looks like a crowd of gray balls which are fading away
	 * <li>The speed value has no influence on this particle effect
	 * </ul>
	 */
	HUGE_EXPLOSION("hugeexplosion"),
	/**
	 * A particle effect which is displayed by exploding ghast fireballs and wither skulls:
	 * <ul>
	 * <li>It looks like a gray ball which is fading away
	 * <li>The speed value slightly influences the size of this particle effect
	 * </ul>
	 */
	LARGE_EXPLODE("largeexplode"),
	/**
	 * A particle effect which is displayed by launching fireworks:
	 * <ul>
	 * <li>It looks like a white star which is sparkling
	 * <li>The speed value influences the velocity at which the particle flies off
	 * </ul>
	 */
	FIREWORKS_SPARK("fireworksSpark"),
	/**
	 * A particle effect which is displayed by swimming entities and arrows in water:
	 * <ul>
	 * <li>It looks like a bubble
	 * <li>The speed value influences the velocity at which the particle flies off
	 * </ul>
	 */
	BUBBLE("bubble", true),
	/**
	 * A particle effect which is displayed by water:
	 * <ul>
	 * <li>It looks like a tiny blue square
	 * <li>The speed value has no influence on this particle effect
	 * </ul>
	 */
	SUSPEND("suspend", true),
	/**
	 * A particle effect which is displayed by air when close to bedrock and the in the void:
	 * <ul>
	 * <li>It looks like a tiny gray square
	 * <li>The speed value has no influence on this particle effect
	 * </ul>
	 */
	DEPTH_SUSPEND("depthSuspend"),
	/**
	 * A particle effect which is displayed by mycelium:
	 * <ul>
	 * <li>It looks like a tiny gray square
	 * <li>The speed value has no influence on this particle effect
	 * </ul>
	 */
	TOWN_AURA("townaura"),
	/**
	 * A particle effect which is displayed when landing a critical hit and by arrows:
	 * <ul>
	 * <li>It looks like a light brown cross
	 * <li>The speed value influences the velocity at which the particle flies off
	 * </ul>
	 */
	CRIT("crit"),
	/**
	 * A particle effect which is displayed when landing a hit with an enchanted weapon:
	 * <ul>
	 * <li>It looks like a cyan star
	 * <li>The speed value influences the velocity at which the particle flies off
	 * </ul>
	 */
	MAGIC_CRIT("magicCrit"),
	/**
	 * A particle effect which is displayed by primed tnt, torches, droppers, dispensers, end portals, brewing stands and monster spawners:
	 * <ul>
	 * <li>It looks like a little gray cloud
	 * <li>The speed value influences the velocity at which the particle flies off
	 * </ul>
	 */
	SMOKE("smoke"),
	/**
	 * A particle effect which is displayed by entities with active potion effects:
	 * <ul>
	 * <li>It looks like a colored swirl
	 * <li>The speed value causes the particle to be colored black when set to 0
	 * </ul>
	 */
	MOB_SPELL("mobSpell"),
	/**
	 * A particle effect which is displayed by entities with active potion effects applied through a beacon:
	 * <ul>
	 * <li>It looks like a transparent colored swirl
	 * <li>The speed value causes the particle to be always colored black when set to 0
	 * </ul>
	 */
	MOB_SPELL_AMBIENT("mobSpellAmbient"),
	/**
	 * A particle effect which is displayed when splash potions or bottles o' enchanting hit something:
	 * <ul>
	 * <li>It looks like a white swirl
	 * <li>The speed value causes the particle to only move upwards when set to 0
	 * </ul>
	 */
	SPELL("spell"),
	/**
	 * A particle effect which is displayed when instant splash potions hit something:
	 * <ul>
	 * <li>It looks like a white cross
	 * <li>The speed value causes the particle to only move upwards when set to 0
	 * </ul>
	 */
	INSTANT_SPELL("instantSpell"),
	/**
	 * A particle effect which is displayed by witches:
	 * <ul>
	 * <li>It looks like a purple cross
	 * <li>The speed value causes the particle to only move upwards when set to 0
	 * </ul>
	 */
	WITCH_MAGIC("witchMagic"),
	/**
	 * A particle effect which is displayed by note blocks:
	 * <ul>
	 * <li>It looks like a colored note
	 * <li>The speed value causes the particle to be colored green when set to 0
	 * </ul>
	 */
	NOTE("note"),
	/**
	 * A particle effect which is displayed by nether portals, endermen, ender pearls, eyes of ender, ender chests and dragon eggs:
	 * <ul>
	 * <li>It looks like a purple cloud
	 * <li>The speed value influences the spread of this particle effect
	 * </ul>
	 */
	PORTAL("portal"),
	/**
	 * A particle effect which is displayed by enchantment tables which are nearby bookshelves:
	 * <ul>
	 * <li>It looks like a cryptic white letter
	 * <li>The speed value influences the spread of this particle effect
	 * </ul>
	 */
	ENCHANTMENT_TABLE("enchantmenttable"),
	/**
	 * A particle effect which is displayed by exploding tnt and creepers:
	 * <ul>
	 * <li>It looks like a white cloud
	 * <li>The speed value influences the velocity at which the particle flies off
	 * </ul>
	 */
	EXPLODE("explode"),
	/**
	 * A particle effect which is displayed by torches, active furnaces, magma cubes and monster spawners:
	 * <ul>
	 * <li>It looks like a tiny flame
	 * <li>The speed value influences the velocity at which the particle flies off
	 * </ul>
	 */
	FLAME("flame"),
	/**
	 * A particle effect which is displayed by lava:
	 * <ul>
	 * <li>It looks like a spark
	 * <li>The speed value has no influence on this particle effect
	 * </ul>
	 */
	LAVA("lava"),
	/**
	 * A particle effect which is currently unused:
	 * <ul>
	 * <li>It looks like a transparent gray square
	 * <li>The speed value has no influence on this particle effect
	 * </ul>
	 */
	FOOTSTEP("footstep"),
	/**
	 * A particle effect which is displayed by swimming entities, rain dropping on the ground and shaking wolves:
	 * <ul>
	 * <li>It looks like a blue drop
	 * <li>The speed value has no influence on this particle effect
	 * </ul>
	 */
	SPLASH("splash"),
	/**
	 * A particle effect which is displayed on water when fishing:
	 * <ul>
	 * <li>It looks like a blue droplet
	 * <li>The speed value influences the velocity at which the particle flies off
	 * </ul>
	 */
	WAKE("wake"),
	/**
	 * A particle effect which is displayed by fire, minecarts with furnace and blazes:
	 * <ul>
	 * <li>It looks like a large gray cloud
	 * <li>The speed value influences the velocity at which the particle flies off
	 * </ul>
	 */
	LARGE_SMOKE("largesmoke"),
	/**
	 * A particle effect which is displayed when a mob dies:
	 * <ul>
	 * <li>It looks like a large white cloud
	 * <li>The speed value influences the velocity at which the particle flies off
	 * </ul>
	 */
	CLOUD("cloud"),
	/**
	 * A particle effect which is displayed by redstone ore, powered redstone, redstone torches and redstone repeaters:
	 * <ul>
	 * <li>It looks like a tiny colored cloud
	 * <li>The speed value causes the particle to be colored red when set to 0
	 * </ul>
	 */
	RED_DUST("reddust"),
	/**
	 * A particle effect which is displayed when snowballs or eggs hit something:
	 * <ul>
	 * <li>It looks like a tiny part of the snowball icon
	 * <li>The speed value has no influence on this particle effect
	 * </ul>
	 */
	SNOWBALL_POOF("snowballpoof"),
	/**
	 * A particle effect which is displayed by blocks beneath a water source:
	 * <ul>
	 * <li>It looks like a blue drip
	 * <li>The speed value has no influence on this particle effect
	 * </ul>
	 */
	DRIP_WATER("dripWater"),
	/**
	 * A particle effect which is displayed by blocks beneath a lava source:
	 * <ul>
	 * <li>It looks like an orange drip
	 * <li>The speed value has no influence on this particle effect
	 * </ul>
	 */
	DRIP_LAVA("dripLava"),
	/**
	 * A particle effect which is currently unused:
	 * <ul>
	 * <li>It looks like a tiny white cloud
	 * <li>The speed value influences the velocity at which the particle flies off
	 * </ul>
	 */
	SNOW_SHOVEL("snowshovel"),
	/**
	 * A particle effect which is displayed by slimes:
	 * <ul>
	 * <li>It looks like a tiny part of the slimeball icon
	 * <li>The speed value has no influence on this particle effect
	 * </ul>
	 */
	SLIME("slime"),
	/**
	 * A particle effect which is displayed when breeding and taming animals:
	 * <ul>
	 * <li>It looks like a red heart
	 * <li>The speed value has no influence on this particle effect
	 * </ul>
	 */
	HEART("heart"),
	/**
	 * A particle effect which is displayed when attacking a villager in a village:
	 * <ul>
	 * <li>It looks like a cracked gray heart
	 * <li>The speed value has no influence on this particle effect
	 * </ul>
	 */
	ANGRY_VILLAGER("angryVillager"),
	/**
	 * A particle effect which is displayed when using bone meal and trading with a villager in a village:
	 * <ul>
	 * <li>It looks like a green star
	 * <li>The speed value has no influence on this particle effect
	 * </ul>
	 */
	HAPPY_VILLAGER("happyVillager");

	private static final Map<String, ParticleEffect> NAME_MAP = new HashMap<String, ParticleEffect>();
	private final String name;
	private final boolean requiresWater;

	// Initialize map for quick name lookup
	static {
		for (ParticleEffect effect : values()) {
			NAME_MAP.put(effect.name, effect);
		}
	}

	/**
	 * Construct a new particle effect
	 *
	 * @param name Name of this particle effect
	 * @param requiresWater Indicates whether water is required for this particle effect to display properly
	 */
	private ParticleEffect(String name, boolean requiresWater) {
		this.name = name;
		this.requiresWater = requiresWater;
	}

	/**
	 * Construct a new particle effect with {@link #requiresWater} set to <code>false</code>
	 *
	 * @param name Name of this particle effect
	 * @see #ParticleEffect(String, boolean)
	 */
	private ParticleEffect(String name) {
		this(name, false);
	}

	/**
	 * Returns the name of this particle effect
	 *
	 * @return The name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Determine if water is required for this particle effect to display properly
	 *
	 * @return Whether water is required or not
	 */
	public boolean getRequiresWater() {
		return requiresWater;
	}

	/**
	 * Returns the particle effect with the given name
	 *
	 * @param name Name of the particle effect
	 * @return The particle effect
	 */
	public static ParticleEffect fromName(String name) {
		for (Entry<String, ParticleEffect> entry : NAME_MAP.entrySet()) {
			if (!entry.getKey().equalsIgnoreCase(name)) {
				continue;
			}
			return entry.getValue();
		}
		return null;
	}

	/**
	 * Determine if water is at a certain location
	 *
	 * @param location Location to check
	 * @return Whether water is at this location or not
	 */
	private static boolean isWater(Location location) {
		Material material = location.getBlock().getType();
		return material == Material.WATER || material == Material.STATIONARY_WATER;
	}

	/**
	 * Determine if an id is a block id
	 *
	 * @param id Id to check
	 * @return Whether id is a block or not
	 */
	@SuppressWarnings("deprecation")
	private static boolean isBlock(int id) {
		Material material = Material.getMaterial(id);
		return material != null && material.isBlock();
	}

	/**
	 * Displays a particle effect which is only visible for all players within a certain range in the world of @param center
	 *
	 * @param offsetX Maximum distance particles can fly away from the center on the x-axis
	 * @param offsetY Maximum distance particles can fly away from the center on the y-axis
	 * @param offsetZ Maximum distance particles can fly away from the center on the z-axis
	 * @param speed Display speed of the particles
	 * @param amount Amount of particles
	 * @param center Center location of the effect
	 * @param range Range of the visibility (Maximum range for particles is usually 16, but it can differ for some types)
	 * @throws IllegalArgumentException If the particle effect requires water and none is at the center location
	 * @see ParticleEffectPacket
	 * @see ParticleEffectPacket#sendTo(Location, double)
	 */
	public void display(float offsetX, float offsetY, float offsetZ, float speed, int amount, Location center, double range) throws IllegalArgumentException {
		if (requiresWater && !isWater(center)) {
			throw new IllegalArgumentException("There is no water at the center location");
		}
		new ParticleEffectPacket(name, offsetX, offsetY, offsetZ, speed, amount).sendTo(center, range);
	}

	/**
	 * Displays a particle effect which is only visible for the specified players
	 *
	 * @param offsetX Maximum distance particles can fly away from the center on the x-axis
	 * @param offsetY Maximum distance particles can fly away from the center on the y-axis
	 * @param offsetZ Maximum distance particles can fly away from the center on the z-axis
	 * @param speed Display speed of the particles
	 * @param amount Amount of particles
	 * @param center Center location of the effect
	 * @param players Receivers of the effect
	 * @throws IllegalArgumentException If the particle effect requires water and none is at the center location
	 * @see ParticleEffectPacket
	 * @see ParticleEffectPacket#sendTo(Location, List)
	 */
	public void display(float offsetX, float offsetY, float offsetZ, float speed, int amount, Location center, List<Player> players) throws IllegalArgumentException {
		if (requiresWater && !isWater(center)) {
			throw new IllegalArgumentException("There is no water at the center location");
		}
		new ParticleEffectPacket(name, offsetX, offsetY, offsetZ, speed, amount).sendTo(center, players);
	}

	/**
	 * Displays an icon crack (item break) particle effect which is only visible for all players within a certain range in the world of @param center
	 *
	 * @param id Id of the icon
	 * @param data Data value
	 * @param offsetX Maximum distance particles can fly away from the center on the x-axis
	 * @param offsetY Maximum distance particles can fly away from the center on the y-axis
	 * @param offsetZ Maximum distance particles can fly away from the center on the z-axis
	 * @param speed Display speed of the particles
	 * @param amount Amount of particles
	 * @param center Center location of the effect
	 * @param range Range of the visibility (Maximum range for particles is usually 16, but it can differ for some types)
	 * @see ParticleEffectPacket
	 * @see ParticleEffectPacket#sendTo(Location, double)
	 */
	public static void displayIconCrack(int id, byte data, float offsetX, float offsetY, float offsetZ, float speed, int amount, Location center, double range) {
		new ParticleEffectPacket("iconcrack_" + id + "_" + data, offsetX, offsetY, offsetZ, speed, amount).sendTo(center, range);
	}

	/**
	 * Displays an icon crack (item break) particle effect which is only visible for the specified players
	 *
	 * @param id Id of the icon
	 * @param data Data value
	 * @param offsetX Maximum distance particles can fly away from the center on the x-axis
	 * @param offsetY Maximum distance particles can fly away from the center on the y-axis
	 * @param offsetZ Maximum distance particles can fly away from the center on the z-axis
	 * @param speed Display speed of the particles
	 * @param amount Amount of particles
	 * @param center Center location of the effect
	 * @param players Receivers of the effect
	 * @see ParticleEffectPacket
	 * @see ParticleEffectPacket#sendTo(Location, List)
	 */
	public static void displayIconCrack(int id, byte data, float offsetX, float offsetY, float offsetZ, float speed, int amount, Location center, List<Player> players) {
		new ParticleEffectPacket("iconcrack_" + id + "_" + data, offsetX, offsetY, offsetZ, speed, amount).sendTo(center, players);
	}

	/**
	 * Displays a block crack (block break) particle effect which is only visible for all players within a certain range in the world of @param center
	 *
	 * @param id Id of the block
	 * @param data Data value
	 * @param offsetX Maximum distance particles can fly away from the center on the x-axis
	 * @param offsetY Maximum distance particles can fly away from the center on the y-axis
	 * @param offsetZ Maximum distance particles can fly away from the center on the z-axis
	 * @param amount Amount of particles
	 * @param center Center location of the effect
	 * @param range Range of the visibility (Maximum range for particles is usually 16, but it can differ for some types)
	 * @throws IllegalArgumentException If the specified id is not a block id
	 * @see ParticleEffectPacket
	 * @see ParticleEffectPacket#sendTo(Location, double)
	 */
	public static void displayBlockCrack(int id, byte data, float offsetX, float offsetY, float offsetZ, int amount, Location center, double range) throws IllegalArgumentException {
		if (!isBlock(id)) {
			throw new IllegalArgumentException("Invalid block id");
		}
		new ParticleEffectPacket("blockcrack_" + id + "_" + data, offsetX, offsetY, offsetZ, 0, amount).sendTo(center, range);
	}

	/**
	 * Displays a block crack (block break) particle effect which is only visible for the specified players
	 *
	 * @param id Id of the block
	 * @param data Data value
	 * @param offsetX Maximum distance particles can fly away from the center on the x-axis
	 * @param offsetY Maximum distance particles can fly away from the center on the y-axis
	 * @param offsetZ Maximum distance particles can fly away from the center on the z-axis
	 * @param amount Amount of particles
	 * @param center Center location of the effect
	 * @param players Receivers of the effect
	 * @throws IllegalArgumentException If the specified id is not a block id
	 * @see ParticleEffectPacket
	 * @see ParticleEffectPacket#sendTo(Location, List)
	 */
	public static void displayBlockCrack(int id, byte data, float offsetX, float offsetY, float offsetZ, int amount, Location center, List<Player> players) throws IllegalArgumentException {
		if (!isBlock(id)) {
			throw new IllegalArgumentException("Invalid block id");
		}
		new ParticleEffectPacket("blockcrack_" + id + "_" + data, offsetX, offsetY, offsetZ, 0, amount).sendTo(center, players);
	}

	/**
	 * Displays a block dust particle effect which is only visible for all players within a certain range in the world of @param center
	 *
	 * @param id Id of the block
	 * @param data Data value
	 * @param offsetX Maximum distance particles can fly away from the center on the x-axis
	 * @param offsetY Maximum distance particles can fly away from the center on the y-axis
	 * @param offsetZ Maximum distance particles can fly away from the center on the z-axis
	 * @param speed Display speed of the particles
	 * @param amount Amount of particles
	 * @param center Center location of the effect
	 * @param range Range of the visibility (Maximum range for particles is usually 16, but it can differ for some types)
	 * @throws IllegalArgumentException If the specified id is not a block id
	 * @see ParticleEffectPacket
	 * @see ParticleEffectPacket#sendTo(Location, double)
	 */
	public static void displayBlockDust(int id, byte data, float offsetX, float offsetY, float offsetZ, float speed, int amount, Location center, double range) throws IllegalArgumentException {
		if (!isBlock(id)) {
			throw new IllegalArgumentException("Invalid block id");
		}
		new ParticleEffectPacket("blockdust_" + id + "_" + data, offsetX, offsetY, offsetZ, speed, amount).sendTo(center, range);
	}

	/**
	 * Displays a block dust particle effect which is only visible for the specified players
	 *
	 * @param id Id of the block
	 * @param data Data value
	 * @param offsetX Maximum distance particles can fly away from the center on the x-axis
	 * @param offsetY Maximum distance particles can fly away from the center on the y-axis
	 * @param offsetZ Maximum distance particles can fly away from the center on the z-axis
	 * @param speed Display speed of the particles
	 * @param amount Amount of particles
	 * @param center Center location of the effect
	 * @param players Receivers of the effect
	 * @throws IllegalArgumentException If the specified id is not a block id
	 * @see ParticleEffectPacket
	 * @see ParticleEffectPacket#sendTo(Location, List)
	 */
	public static void displayBlockDust(int id, byte data, float offsetX, float offsetY, float offsetZ, float speed, int amount, Location center, List<Player> players) throws IllegalArgumentException {
		if (!isBlock(id)) {
			throw new IllegalArgumentException("Invalid block id");
		}
		new ParticleEffectPacket("blockdust_" + id + "_" + data, offsetX, offsetY, offsetZ, speed, amount).sendTo(center, players);
	}

	/**
	 * Represents a particle effect packet with all attributes which is used for sending packets to the players
	 * <p>
	 * This class is part of the <b>ParticleEffect Library</b> and follows the same usage conditions
	 *
	 * @author DarkBlade12
	 * @since 1.5
	 */
	public static final class ParticleEffectPacket {
		private static Constructor<?> packetConstructor;
		private static Method getHandle;
		private static Field playerConnection;
		private static Method sendPacket;
		private static boolean initialized;
		private final String name;
		private final float offsetX;
		private final float offsetY;
		private final float offsetZ;
		private final float speed;
		private final int amount;
		private Object packet;

		/**
		 * Construct a new particle effect packet
		 *
		 * @param name Name of the effect
		 * @param offsetX Maximum distance particles can fly away from the center on the x-axis
		 * @param offsetY Maximum distance particles can fly away from the center on the y-axis
		 * @param offsetZ Maximum distance particles can fly away from the center on the z-axis
		 * @param speed Display speed of the particles
		 * @param amount Amount of particles
		 * @throws IllegalArgumentException If the speed is lower than 0 or the amount is lower than 1
		 * @see #initialize()
		 */
		public ParticleEffectPacket(String name, float offsetX, float offsetY, float offsetZ, float speed, int amount) throws IllegalArgumentException {
			initialize();
			if (speed < 0) {
				throw new IllegalArgumentException("The speed is lower than 0");
			}
			if (amount < 1) {
				throw new IllegalArgumentException("The amount is lower than 1");
			}
			this.name = name;
			this.offsetX = offsetX;
			this.offsetY = offsetY;
			this.offsetZ = offsetZ;
			this.speed = speed;
			this.amount = amount;
		}

		/**
		 * Initializes {@link #packetConstructor}, {@link #getHandle}, {@link #playerConnection} and {@link #sendPacket} and sets {@link #initialized} to <code>true</code> if it succeeds
		 * <p>
		 * <b>Note:</b> These fields only have to be initialized once, so it will return if {@link #initialized} is already set to <code>true</code>
		 *
		 * @throws VersionIncompatibleException if accessed packets, fields or methods differ in your bukkit version
		 */
		public static void initialize() throws VersionIncompatibleException {
			if (initialized) {
				return;
			}
			try {
				int version = Integer.parseInt(Character.toString(PackageType.getServerVersion().charAt(3)));
				Class<?> packetClass = PackageType.MINECRAFT_SERVER.getClass(version < 7 ? "Packet63WorldParticles" : PacketType.PLAY_OUT_WORLD_PARTICLES.getName());
				packetConstructor = ReflectionUtils.getConstructor(packetClass);
				getHandle = ReflectionUtils.getMethod("CraftPlayer", PackageType.CRAFTBUKKIT_ENTITY, "getHandle");
				playerConnection = ReflectionUtils.getField("EntityPlayer", PackageType.MINECRAFT_SERVER, false, "playerConnection");
				sendPacket = ReflectionUtils.getMethod(playerConnection.getType(), "sendPacket", PackageType.MINECRAFT_SERVER.getClass("Packet"));
			} catch (Exception exception) {
				throw new VersionIncompatibleException("Your current bukkit version seems to be incompatible with this library", exception);
			}
			initialized = true;
		}

		/**
		 * Determine if {@link #packetConstructor}, {@link #getHandle}, {@link #playerConnection} and {@link #sendPacket} are initialized
		 *
		 * @return Whether these fields are initialized or not
		 * @see #initialize()
		 */
		public static boolean isInitialized() {
			return initialized;
		}

		/**
		 * Sends the packet to a single player and caches it
		 *
		 * @param center Center location of the effect
		 * @param player Receiver of the packet
		 * @throws PacketInstantiationException if instantion fails due to an unknown error
		 * @throws PacketSendingException if sending fails due to an unknown error
		 */
		public void sendTo(Location center, Player player) throws PacketInstantiationException, PacketSendingException {
			if (packet == null) {
				try {
					packet = packetConstructor.newInstance();
					ReflectionUtils.setValue(packet, true, "a", name);
					ReflectionUtils.setValue(packet, true, "b", (float) center.getX());
					ReflectionUtils.setValue(packet, true, "c", (float) center.getY());
					ReflectionUtils.setValue(packet, true, "d", (float) center.getZ());
					ReflectionUtils.setValue(packet, true, "e", offsetX);
					ReflectionUtils.setValue(packet, true, "f", offsetY);
					ReflectionUtils.setValue(packet, true, "g", offsetZ);
					ReflectionUtils.setValue(packet, true, "h", speed);
					ReflectionUtils.setValue(packet, true, "i", amount);
				} catch (Exception exception) {
					throw new PacketInstantiationException("Packet instantiation failed", exception);
				}
			}
			try {
				sendPacket.invoke(playerConnection.get(getHandle.invoke(player)), packet);
			} catch (Exception exception) {
				throw new PacketSendingException("Failed to send the packet to player '" + player.getName() + "'", exception);
			}
		}

		/**
		 * Sends the packet to all players in the list
		 *
		 * @param center Center location of the effect
		 * @param players Receivers of the packet
		 * @throws IllegalArgumentException If the player list is empty
		 * @see #sendTo(Location center, Player player)
		 */
		public void sendTo(Location center, List<Player> players) throws IllegalArgumentException {
			if (players.isEmpty()) {
				throw new IllegalArgumentException("The player list is empty");
			}
			for (Player player : players) {
				sendTo(center, player);
			}
		}

		/**
		 * Sends the packet to all players in a certain range
		 *
		 * @param center Center location of the effect
		 * @param range Range in which players will receive the packet (Maximum range for particles is usually 16, but it can differ for some types)
		 * @throws IllegalArgumentException If the range is lower than 1
		 * @see #sendTo(Location center, Player player)
		 */
		@SuppressWarnings("deprecation")
		public void sendTo(Location center, double range) throws IllegalArgumentException {
			if (range < 1) {
				throw new IllegalArgumentException("The range is lower than 1");
			}
			String worldName = center.getWorld().getName();
			double squared = range * range;
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (!player.getWorld().getName().equals(worldName) || player.getLocation().distanceSquared(center) > squared) {
					continue;
				}
				sendTo(center, player);
			}
		}

		/**
		 * Represents a runtime exception that is thrown if a bukkit version is not compatible with this library
		 * <p>
		 * This class is part of the <b>ParticleEffect Library</b> and follows the same usage conditions
		 *
		 * @author DarkBlade12
		 * @since 1.5
		 */
		private static final class VersionIncompatibleException extends RuntimeException {
			private static final long serialVersionUID = 3203085387160737484L;

			/**
			 * Construct a new version incompatible exception
			 *
			 * @param message Message that will be logged
			 * @param cause Cause of the exception
			 */
			public VersionIncompatibleException(String message, Throwable cause) {
				super(message, cause);
			}
		}

		/**
		 * Represents a runtime exception that is thrown if packet instantiation fails
		 * <p>
		 * This class is part of the <b>ParticleEffect Library</b> and follows the same usage conditions
		 *
		 * @author DarkBlade12
		 * @since 1.4
		 */
		private static final class PacketInstantiationException extends RuntimeException {
			private static final long serialVersionUID = 3203085387160737484L;

			/**
			 * Construct a new packet instantiation exception
			 *
			 * @param message Message that will be logged
			 * @param cause Cause of the exception
			 */
			public PacketInstantiationException(String message, Throwable cause) {
				super(message, cause);
			}
		}

		/**
		 * Represents a runtime exception that is thrown if packet sending fails
		 * <p>
		 * This class is part of the <b>ParticleEffect Library</b> and follows the same usage conditions
		 *
		 * @author DarkBlade12
		 * @since 1.4
		 */
		private static final class PacketSendingException extends RuntimeException {
			private static final long serialVersionUID = 3203085387160737484L;

			/**
			 * Construct a new packet sending exception
			 *
			 * @param message Message that will be logged
			 * @param cause Cause of the exception
			 */
			public PacketSendingException(String message, Throwable cause) {
				super(message, cause);
			}
		}
	}
} 
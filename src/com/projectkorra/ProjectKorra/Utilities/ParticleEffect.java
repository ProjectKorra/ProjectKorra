package com.projectkorra.ProjectKorra.Utilities;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public enum ParticleEffect {

	// == Support for 1.7 Implementation ==
	HUGE_EXPLOSION(
			"hugeexplosion",
			"EXPLOSION_HUGE"),
	LARGE_EXPLODE(
			"largeexplode",
			"EXPLOSION_LARGE"),
	BUBBLE(
			"bubble",
			"WATER_BUBBLE"),
	SUSPEND(
			"suspend",
			"SUSPENDED"),
	DEPTH_SUSPEND(
			"depthSuspend",
			"SUSPENDED_DEPTH"),
	MAGIC_CRIT(
			"magicCrit",
			"CRIT_MAGIC"),
	MOB_SPELL(
			"mobSpell",
			"SPELL_MOB"),
	MOB_SPELL_AMBIENT(
			"mobSpellAmbient",
			"SPELL_MOB_AMBIENT"),
	INSTANT_SPELL(
			"instantSpell",
			"SPELL_INSTANT"),
	WITCH_MAGIC(
			"witchMagic",
			"SPELL_WITCH"),
	EXPLODE(
			"explode",
			"EXPLOSION_NORMAL"),
	SPLASH(
			"splash",
			"WATER_SPLASH"),
	LARGE_SMOKE(
			"largesmoke",
			"SMOKE_LARGE"),
	RED_DUST(
			"reddust",
			"REDSTONE"),
	SNOWBALL_POOF(
			"snowballpoof",
			"SNOWBALL"),
	ANGRY_VILLAGER(
			"angryVillager",
			"VILLAGER_ANGRY"),
	HAPPY_VILLAGER(
			"happerVillager",
			"VILLAGER_HAPPY"),
	// == 1.8 Particles and Supported 1.7 Particles ==
	/**
	 * 1.8 only!
	 * 
	 * @see {@link #EXPLODE}
	 */
	EXPLOSION_NORMAL(
			EXPLODE.getName()),
	/**
	 * 1.8 only!
	 * 
	 * @see {@link #LARGE_EXPLODE}
	 */
	EXPLOSION_LARGE(
			LARGE_EXPLODE.getName()),
	/**
	 * 1.8 only!
	 * 
	 * @see {@link #HUGE_EXPLOSION}
	 */
	EXPLOSION_HUGE(
			HUGE_EXPLOSION.getName()),
	FIREWORKS_SPARK(
			"fireworksSpark"),
	/**
	 * 1.8 only!
	 * 
	 * @see {@link #BUBBLE}
	 */
	WATER_BUBBLE(
			BUBBLE.getName()),
	/**
	 * 1.8 only!
	 * 
	 * @see {@link #SPLASH}
	 */
	WATER_SPLASH(
			SPLASH.getName()),
	/**
	 * 1.8 only!
	 */
	WATER_WAKE,
	/**
	 * 1.8 only!
	 * 
	 * @see {@link #SUSPEND}
	 */
	SUSPENDED(
			SUSPEND.getName()),
	/**
	 * 1.8 only!
	 * 
	 * @see {@link #DEPTH_SUSPEND}
	 */
	SUSPENDED_DEPTH(
			DEPTH_SUSPEND.getName()),
	CRIT(
			"crit"),
	/**
	 * 1.8 only!
	 * 
	 * @see {@link #MAGIC_CRIT}
	 */
	CRIT_MAGIC(
			MAGIC_CRIT.getName()),
	/**
	 * 1.8 only!
	 */
	SMOKE_NORMAL,
	/**
	 * 1.8 only!
	 * 
	 * @see {@link #LARGE_SMOKE}
	 */
	SMOKE_LARGE(
			LARGE_SMOKE.getName()),
	SPELL(
			"spell"),
	/**
	 * 1.8 only!
	 * 
	 * @see {@link #INSTANT_SPELL}
	 */
	SPELL_INSTANT(
			INSTANT_SPELL.getName()),
	/**
	 * 1.8 only!
	 * 
	 * @see {@link #MOB_SPELL}
	 */
	SPELL_MOB(
			MOB_SPELL.getName()),
	/**
	 * 1.8 only!
	 * 
	 * @see {@link #MOB_SPELL_AMBIENT}
	 */
	SPELL_MOB_AMBIENT(
			MOB_SPELL_AMBIENT.getName()),
	/**
	 * 1.8 only!
	 * 
	 * @see {@link #WITCH_MAGIC}
	 */
	SPELL_WITCH(
			WITCH_MAGIC.getName()),
	DRIP_WATER(
			"dripWater"),
	DRIP_LAVA(
			"dripLava"),
	/**
	 * 1.8 only!
	 * 
	 * @see {@link #ANGRY_VILLAGER}
	 */
	VILLAGER_ANGRY(
			ANGRY_VILLAGER.getName()),
	/**
	 * 1.8 only!
	 * 
	 * @see {@link #HAPPY_VILLAGER}
	 */
	VILLAGER_HAPPY(
			HAPPY_VILLAGER.getName()),
	TOWN_AURA(
			"townaura"),
	NOTE(
			"note"),
	PORTAL(
			"portal"),
	ENCHANTMENT_TABLE(
			"enchantmenttable"),
	FLAME(
			"flame"),
	LAVA(
			"lave"),
	FOOTSTEP(
			"footstep"),
	CLOUD(
			"cloud"),
	REDSTONE(
			"reddust"),
	SNOWBALL(
			"snowballpoof"),
	SNOW_SHOVEL(
			"snowshovel"),
	SLIME(
			"slime"),
	HEART(
			"heart"),
	/**
	 * 1.8 only!
	 */
	BARRIER,
	/**
	 * 1.8 only!
	 */
	ITEM_CRACK,
	/**
	 * 1.8 only!
	 */
	BLOCK_CRACK,
	/**
	 * 1.8 only!
	 */
	BLOCK_DUST,
	/**
	 * 1.8 only!
	 */
	WATER_DROP,
	/**
	 * 1.8 only!
	 */
	ITEM_TAKE,
	/**
	 * 1.8 only!
	 */
	MOB_APPEARANCE;

	private String	particleName;
	private String	enumValue;

	ParticleEffect(String particleName, String enumValue) {
		this.particleName = particleName;
		this.enumValue = enumValue;
	}

	ParticleEffect(String particleName) {
		this(particleName, null);
	}

	ParticleEffect() {
		this(null, null);
	}

	public String getName() {
		return this.particleName;
	}

	private static Class<?>	nmsPacketPlayOutParticle	= ReflectionUtilities.getNMSClass("PacketPlayOutWorldParticles");
	private static Class<?>	nmsEnumParticle;
	private static int		particleRange				= 25;

	/**
	 * Modify the maximum Range of particles (only useful for Client versions 1.8+)
	 * 
	 * @param range
	 *            New range
	 */
	public static void setRange(int range) {
		if (range < 0) throw new IllegalArgumentException("Range must be positive!");
		if (range > Integer.MAX_VALUE) throw new IllegalArgumentException("Range is too big!");
		particleRange = range;
	}

	/**
	 * @return The current maximum Range of particles
	 */
	public static int getRange() {
		return particleRange;
	}

	/**
	 * Send the particle to a specific Player
	 * 
	 * @param player
	 *            Receiver of the particle
	 * @param location
	 *            Location of the particle
	 * @param offsetX
	 *            X size of the area to spawn particles in.
	 * @param offsetY
	 *            Y size of the area to spawn particles in.
	 * @param offsetZ
	 *            Z size of the area to spawn particles in.
	 * @param speed
	 *            Speed of the Particle
	 * @param count
	 *            Number of spawned particles
	 * @throws Exception
	 */
	public void sendToPlayer(Player player, Location location, float offsetX, float offsetY, float offsetZ, float speed, int count) throws Exception {
		if (!isPlayerInRange(player, location)) return;
		System.out.println(ReflectionUtilities.getVersion());
		if (ReflectionUtilities.getVersion().contains("v1_8")) {
			try {
				if (nmsEnumParticle == null) nmsEnumParticle = ReflectionUtilities.getNMSClass("EnumParticle");
				Object packet = nmsPacketPlayOutParticle.getConstructor(new Class[] { nmsEnumParticle, boolean.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, int.class, int[].class })
						.newInstance(getEnum(nmsEnumParticle.getName() + "." + (enumValue != null ? enumValue : name().toUpperCase())), true, (float) location.getX(), (float) location.getY(), (float) location.getZ(), offsetX, offsetY, offsetZ, speed, count, new int[] {});
				Object handle = ReflectionUtilities.getHandle(player);
				Object connection = ReflectionUtilities.getField(handle.getClass(), "playerConnection").get(handle);
				ReflectionUtilities.getMethod(connection.getClass(), "sendPacket", new Class[0]).invoke(connection, new Object[] { packet });
			} catch (Exception e) {
				throw new IllegalArgumentException("Unable to send Particle " + name() + ". (Version 1.8): " + e.getMessage());
			}
		} else {
			try {
				if (particleName == null) throw new Exception();
				Object packet = nmsPacketPlayOutParticle.getConstructor(new Class[] { String.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, int.class }).newInstance(particleName, (float) location.getX(), (float) location.getY(), (float) location.getZ(), offsetX, offsetY, offsetZ, speed, count);
				Object handle = ReflectionUtilities.getHandle(player);
				Object connection = ReflectionUtilities.getField(handle.getClass(), "playerConnection").get(handle);
				ReflectionUtilities.getMethod(connection.getClass(), "sendPacket", new Class[0]).invoke(connection, new Object[] { packet });
			} catch (Exception e) {
				throw new IllegalArgumentException("Unable to send Particle " + name() + ". (Invalid Server Version: 1.7) " + e.getMessage());
			}
		}
	}

	/**
	 * Send the particle to a Collection of Players {@link #sendToPlayer(Player, Location, float, float, float, float, int)}
	 * 
	 * @param player
	 *            Receiver of the particle
	 * @param location
	 *            Location of the particle
	 * @param offsetX
	 *            X size of the area to spawn particles in.
	 * @param offsetY
	 *            Y size of the area to spawn particles in.
	 * @param offsetZ
	 *            Z size of the area to spawn particles in.
	 * @param speed
	 *            Speed of the Particle
	 * @param count
	 *            Number of spawned particles
	 */
	public void sendToPlayers(Collection<Player> players, Location location, float offsetX, float offsetY, float offsetZ, float speed, int count) throws Exception {
		for (Player p : players)
			sendToPlayer(p, location, offsetX, offsetY, offsetZ, speed, count);
	}

	/**
	 * Send the particle to an Array of Players {@link #sendToPlayer(Player, Location, float, float, float, float, int)}
	 * 
	 * @param player
	 *            Receiver of the particle
	 * @param location
	 *            Location of the particle
	 * @param offsetX
	 *            X size of the area to spawn particles in.
	 * @param offsetY
	 *            Y size of the area to spawn particles in.
	 * @param offsetZ
	 *            Z size of the area to spawn particles in.
	 * @param speed
	 *            Speed of the Particle
	 * @param count
	 *            Number of spawned particles
	 */
	public void sendToPlayers(Player[] players, Location location, float offsetX, float offsetY, float offsetZ, float speed, int count) throws Exception {
		for (Player p : players)
			sendToPlayer(p, location, offsetX, offsetY, offsetZ, speed, count);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Enum<?> getEnum(String enumFullName) {
		String[] x = enumFullName.split("\\.(?=[^\\.]+$)");
		if (x.length == 2) {
			String enumClassName = x[0];
			String enumName = x[1];
			try {
				Class<Enum> cl = (Class<Enum>) Class.forName(enumClassName);
				return Enum.valueOf(cl, enumName);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private static boolean isPlayerInRange(Player p, Location center) {
		double distance = 0;
		if ((distance = center.distance(p.getLocation())) > Double.MAX_VALUE) return false;
		return distance < particleRange;
	}

	public static class ReflectionUtilities {

		/**
		 * sets a value of an {@link Object} via reflection
		 *
		 * @param instance
		 *            instance the class to use
		 * @param fieldName
		 *            the name of the {@link Field} to modify
		 * @param value
		 *            the value to set
		 * @throws Exception
		 */
		public static void setValue(Object instance, String fieldName, Object value) throws Exception {
			Field field = instance.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(instance, value);
		}

		/**
		 * get a value of an {@link Object}'s {@link Field}
		 *
		 * @param instance
		 *            the target {@link Object}
		 * @param fieldName
		 *            name of the {@link Field}
		 * @return the value of {@link Object} instance's {@link Field} with the name of fieldName
		 * @throws Exception
		 */
		public static Object getValue(Object instance, String fieldName) throws Exception {
			Field field = instance.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return field.get(instance);
		}

		public static String getVersion() {
			String name = Bukkit.getServer().getClass().getPackage().getName();
			String version = name.substring(name.lastIndexOf('.') + 1) + ".";
			return version;
		}

		public static Class<?> getNMSClass(String className) {
			String fullName = "net.minecraft.server." + getVersion() + className;
			Class<?> clazz = null;
			try {
				clazz = Class.forName(fullName);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return clazz;
		}

		public static Class<?> getOBCClass(String className) {
			String fullName = "org.bukkit.craftbukkit." + getVersion() + className;
			Class<?> clazz = null;
			try {
				clazz = Class.forName(fullName);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return clazz;
		}

		public static Object getHandle(Object obj) {
			try {
				return getMethod(obj.getClass(), "getHandle", new Class[0]).invoke(obj, new Object[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		public static Field getField(Class<?> clazz, String name) {
			try {
				Field field = clazz.getDeclaredField(name);
				field.setAccessible(true);
				return field;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		public static Method getMethod(Class<?> clazz, String name, Class<?>... args) {
			for (Method m : clazz.getMethods()) {
				if ((m.getName().equals(name)) && ((args.length == 0) || (ClassListEqual(args, m.getParameterTypes())))) {
					m.setAccessible(true);
					return m;
				}
			}
			return null;
		}

		public static boolean ClassListEqual(Class<?>[] l1, Class<?>[] l2) {
			boolean equal = true;
			if (l1.length != l2.length) {
				return false;
			}
			for (int i = 0; i < l1.length; i++) {
				if (l1[i] != l2[i]) {
					equal = false;
					break;
				}
			}
			return equal;
		}
	}
}
package com.projectkorra.projectkorra.ability;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.jar.JarFile;

import sun.reflect.ReflectionFactory;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import co.aikar.timings.lib.MCTiming;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.Manager;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.util.AbilityLoader;
import com.projectkorra.projectkorra.ability.util.AddonAbilityLoader;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.ability.util.CollisionManager;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager.MultiAbilityInfo;
import com.projectkorra.projectkorra.ability.util.PassiveManager;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.attribute.AttributeModifier;
import com.projectkorra.projectkorra.attribute.AttributePriority;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.event.AbilityEndEvent;
import com.projectkorra.projectkorra.event.AbilityProgressEvent;
import com.projectkorra.projectkorra.event.AbilityStartEvent;
import com.projectkorra.projectkorra.util.FlightHandler;
import com.projectkorra.projectkorra.util.TimeUtil;

/**
 * CoreAbility provides default implementation of an Ability, including methods
 * to control the life cycle of a specific instance. CoreAbility also provides a
 * system to load CoreAbilities within a {@link JavaPlugin}, or located in an
 * external {@link JarFile}.
 * <p>
 * For {@link CollisionManager} and {@link Collision}, a CoreAbility may need to
 * override {@link #isCollidable()}, {@link #getCollisionRadius()},
 * {@link #handleCollision(Collision)}, and {@link #getLocations()}.
 *
 * @see #start()
 * @see #progress()
 * @see #remove()
 * @see #registerAddonAbilities(String)
 * @see #registerPluginAbilities(JavaPlugin, String)
 */
public abstract class CoreAbility implements Ability {

	private static final Set<CoreAbility> INSTANCES = Collections.newSetFromMap(new ConcurrentHashMap<CoreAbility, Boolean>());
	private static final Map<Class<? extends CoreAbility>, Map<UUID, Map<Integer, CoreAbility>>> INSTANCES_BY_PLAYER = new ConcurrentHashMap<>();
	private static final Map<Class<? extends CoreAbility>, Set<CoreAbility>> INSTANCES_BY_CLASS = new ConcurrentHashMap<>();
	private static final Map<String, CoreAbility> ABILITIES_BY_NAME = new ConcurrentSkipListMap<>(); // preserves ordering.
	private static final Map<Class<? extends CoreAbility>, CoreAbility> ABILITIES_BY_CLASS = new ConcurrentHashMap<>();
	private static final double DEFAULT_COLLISION_RADIUS = 0.3;
	private static final List<String> ADDON_PLUGINS = new ArrayList<>();
	private static final Map<Class<? extends CoreAbility>, Map<String, Field>> ATTRIBUTE_FIELDS = new HashMap<>();

	private static int idCounter;
	private static long currentTick;

	protected Player player;
	protected BendingPlayer bPlayer;
	protected FlightHandler flightHandler;

	private final Map<String, Map<AttributePriority, Set<Pair<Number, AttributeModifier>>>> attributeModifiers = new HashMap<>();
	private final Map<String, Object> attributeValues = new HashMap<>();
	private boolean started;
	private boolean removed;
	private boolean hidden;
	private int id;
	private long startTime;
	private long startTick;
	private boolean attributesModified;

	static {
		idCounter = Integer.MIN_VALUE;
	}

	/**
	 * The default constructor is needed to create a fake instance of each
	 * CoreAbility via reflection in {@link #registerAbilities()}. More
	 * specifically, {@link #registerPluginAbilities} calls
	 * getDeclaredConstructor which is only usable with a public default
	 * constructor. Reflection lets us create a list of all of the plugin's
	 * abilities when the plugin first loads.
	 *
	 * @see #ABILITIES_BY_NAME
	 * @see #getAbility(String)
	 */
	public CoreAbility() {
		for (final Field field : this.getClass().getDeclaredFields()) {
			if (field.isAnnotationPresent(Attribute.class)) {
				final Attribute attribute = field.getAnnotation(Attribute.class);
				if (!ATTRIBUTE_FIELDS.containsKey(this.getClass())) {
					ATTRIBUTE_FIELDS.put(this.getClass(), new HashMap<>());
				}
				ATTRIBUTE_FIELDS.get(this.getClass()).put(attribute.value(), field);
			}
		}
	}

	/**
	 * Creates a new CoreAbility instance but does not start it.
	 *
	 * @param player the non-null player that created this instance
	 * @see #start()
	 */
	public CoreAbility(final Player player) {
		if (player == null || !this.isEnabled()) {
			return;
		}

		this.player = player;
		this.bPlayer = BendingPlayer.getBendingPlayer(player);
		this.flightHandler = Manager.getManager(FlightHandler.class);
		this.startTime = System.currentTimeMillis();
		this.started = false;
		this.id = CoreAbility.idCounter;

		if (idCounter == Integer.MAX_VALUE) {
			idCounter = Integer.MIN_VALUE;
		} else {
			idCounter++;
		}
	}

	/**
	 * Causes the ability to begin updating every tick by calling
	 * {@link #progress()} until {@link #remove()} is called. This method cannot
	 * be overridden, and any code that needs to be performed before start
	 * should be handled in the constructor.
	 *
	 * @see #getStartTime()
	 * @see #isStarted()
	 * @see #isRemoved()
	 */
	public void start() {
		if (this.player == null || !this.isEnabled()) {
			return;
		}
		final AbilityStartEvent event = new AbilityStartEvent(this);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			this.remove();
			return;
		}

		this.started = true;
		this.startTime = System.currentTimeMillis();
		this.startTick = getCurrentTick();
		final Class<? extends CoreAbility> clazz = this.getClass();
		final UUID uuid = this.player.getUniqueId();

		if (!INSTANCES_BY_PLAYER.containsKey(clazz)) {
			INSTANCES_BY_PLAYER.put(clazz, new ConcurrentHashMap<UUID, Map<Integer, CoreAbility>>());
		}
		if (!INSTANCES_BY_PLAYER.get(clazz).containsKey(uuid)) {
			INSTANCES_BY_PLAYER.get(clazz).put(uuid, new ConcurrentHashMap<Integer, CoreAbility>());
		}
		if (!INSTANCES_BY_CLASS.containsKey(clazz)) {
			INSTANCES_BY_CLASS.put(clazz, Collections.newSetFromMap(new ConcurrentHashMap<CoreAbility, Boolean>()));
		}

		INSTANCES_BY_PLAYER.get(clazz).get(uuid).put(this.id, this);
		INSTANCES_BY_CLASS.get(clazz).add(this);
		INSTANCES.add(this);
	}

	/**
	 * Causes this CoreAbility instance to be removed, and {@link #progress}
	 * will no longer be called every tick. If this method is overridden then
	 * the new method must call <b>super.remove()</b>.
	 *
	 * {@inheritDoc}
	 *
	 * @see #isRemoved()
	 */
	@Override
	public void remove() {
		if (this.player == null) {
			return;
		}

		Bukkit.getServer().getPluginManager().callEvent(new AbilityEndEvent(this));
		this.removed = true;

		final Map<UUID, Map<Integer, CoreAbility>> classMap = INSTANCES_BY_PLAYER.get(this.getClass());
		if (classMap != null) {
			final Map<Integer, CoreAbility> playerMap = classMap.get(this.player.getUniqueId());
			if (playerMap != null) {
				playerMap.remove(this.id);
				if (playerMap.size() == 0) {
					classMap.remove(this.player.getUniqueId());
				}
			}

			if (classMap.size() == 0) {
				INSTANCES_BY_PLAYER.remove(this.getClass());
			}
		}

		if (INSTANCES_BY_CLASS.containsKey(this.getClass())) {
			INSTANCES_BY_CLASS.get(this.getClass()).remove(this);
		}
		INSTANCES.remove(this);
	}

	/**
	 * Causes {@link #progress()} to be called on every CoreAbility instance
	 * that has been started and has not been removed.
	 */
	public static void progressAll() {
		for (final Set<CoreAbility> setAbils : INSTANCES_BY_CLASS.values()) {
			for (final CoreAbility abil : setAbils) {
				if (abil instanceof PassiveAbility) {
					if (!((PassiveAbility) abil).isProgressable()) {
						continue;
					}

					if (!abil.getPlayer().isOnline()) { // This has to be before isDead as isDead.
						abil.remove(); // will return true if they are offline.
						continue;
					} else if (abil.getPlayer().isDead()) {
						continue;
					}
				} else if (abil.getPlayer().isDead()) {
					abil.remove();
					continue;
				} else if (!abil.getPlayer().isOnline()) {
					abil.remove();
					continue;
				}

				try {
					if (!abil.attributesModified) {
						abil.modifyAttributes();
						abil.attributesModified = true;
					}

					try (MCTiming timing = ProjectKorra.timing(abil.getName()).startTiming()) {
						abil.progress();
					}

					Bukkit.getServer().getPluginManager().callEvent(new AbilityProgressEvent(abil));
				} catch (final Exception e) {
					e.printStackTrace();
					Bukkit.getLogger().severe(abil.toString());
					try {
						abil.getPlayer().sendMessage(ChatColor.YELLOW + "[" + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()) + "] " + ChatColor.RED + "There was an error running " + abil.getName() + ". please notify the server owner describing exactly what you were doing at this moment");
					} catch (final Exception me) {
						Bukkit.getLogger().severe("unable to notify ability user of error");
					}
					try {
						abil.remove();
					} catch (final Exception re) {
						Bukkit.getLogger().severe("unable to fully remove ability of above error");
					}
				}
			}
		}
		currentTick++;
	}

	/**
	 * Removes every CoreAbility instance that has been started but not yet
	 * removed.
	 */
	public static void removeAll() {
		for (final Set<CoreAbility> setAbils : INSTANCES_BY_CLASS.values()) {
			for (final CoreAbility abil : setAbils) {
				abil.remove();
			}
		}

		for (final CoreAbility coreAbility : ABILITIES_BY_NAME.values()) {
			if (coreAbility instanceof AddonAbility) {
				final AddonAbility addon = (AddonAbility) coreAbility;
				addon.stop();
			}
		}
	}

	/**
	 * Returns any T CoreAbility that has been started and not yet removed. May
	 * return null if no such ability exists.
	 *
	 * @param player the player that created the CoreAbility instance
	 * @param clazz the class of the type of CoreAbility
	 * @return a CoreAbility instance or null
	 */
	public static <T extends CoreAbility> T getAbility(final Player player, final Class<T> clazz) {
		final Collection<T> abils = getAbilities(player, clazz);
		if (abils.iterator().hasNext()) {
			return abils.iterator().next();
		}
		return null;
	}

	/**
	 * Returns a "fake" instance for the CoreAbility represented by abilityName.
	 * This method does not look into CoreAbility instances that were created by
	 * Players, instead this method looks at the CoreAbilities that were created
	 * via Reflection by {@link #registerAbilities()} when the plugin was first
	 * loaded.
	 *
	 * <p>
	 * These "fake" instances have a null player, but methods such as
	 * {@link Ability#getName()}, and {@link Ability#getElement()} will still
	 * work, as will checking the type of the ability with instanceof.
	 *
	 * <p>
	 * CoreAbility coreAbil = getAbility(someString); <br>
	 * if (coreAbil instanceof FireAbility && coreAbil.isSneakAbility())
	 *
	 * @param abilityName the name of a loaded CoreAbility
	 * @return a "fake" CoreAbility instance, or null if no such ability exists
	 */
	public static CoreAbility getAbility(final String abilityName) {
		return abilityName != null ? ABILITIES_BY_NAME.get(abilityName.toLowerCase()) : null;
	}

	/**
	 * Returns a "fake" instance for a CoreAbility with the specific class.
	 *
	 * @param clazz the class for the type of CoreAbility to be returned
	 * @return a "fake" CoreAbility instance or null if the ability doesn't exist or <b>isn't enabled</b>
	 */
	public static CoreAbility getAbility(final Class<? extends CoreAbility> clazz) {
		return clazz != null ? ABILITIES_BY_CLASS.get(clazz) : null;
	}

	/**
	 * @return a list of "fake" instances for each ability that was loaded by
	 *         {@link #registerAbilities()}
	 */
	public static ArrayList<CoreAbility> getAbilities() {
		return new ArrayList<CoreAbility>(ABILITIES_BY_CLASS.values());
	}

	/**
	 * @return a list of "fake" instances for each ability that was loaded by
	 *         {@link #registerAbilities()}
	 */
	public static ArrayList<CoreAbility> getAbilitiesByName() {
		return new ArrayList<CoreAbility>(ABILITIES_BY_NAME.values());
	}

	/**
	 * Returns a Collection of all of the player created instances for a
	 * specific type of CoreAbility.
	 *
	 * @param clazz the class for the type of CoreAbilities
	 * @return a Collection of real instances
	 */
	public static <T extends CoreAbility> Collection<T> getAbilities(final Class<T> clazz) {
		if (clazz == null || INSTANCES_BY_CLASS.get(clazz) == null || INSTANCES_BY_CLASS.get(clazz).size() == 0) {
			return Collections.emptySet();
		}
		return (Collection<T>) CoreAbility.INSTANCES_BY_CLASS.get(clazz);
	}

	/**
	 * Returns a Collection of specific CoreAbility instances that were created
	 * by the specified player.
	 *
	 * @param player the player that created the instances
	 * @param clazz the class for the type of CoreAbilities
	 * @return a Collection of real instances
	 */
	public static <T extends CoreAbility> Collection<T> getAbilities(final Player player, final Class<T> clazz) {
		if (player == null || clazz == null || INSTANCES_BY_PLAYER.get(clazz) == null || INSTANCES_BY_PLAYER.get(clazz).get(player.getUniqueId()) == null) {
			return Collections.emptySet();
		}
		return (Collection<T>) INSTANCES_BY_PLAYER.get(clazz).get(player.getUniqueId()).values();
	}

	/**
	 * @return a Collection of all of the CoreAbilities that are currently
	 *         alive. Do not modify this Collection.
	 */
	public static Collection<CoreAbility> getAbilitiesByInstances() {
		return INSTANCES;
	}

	/**
	 * Returns an List of fake instances that were loaded by
	 * {@link #registerAbilities()} filtered by Element.
	 *
	 * @param element the Element of the loaded abilities
	 * @return a list of fake CoreAbility instances
	 */
	public static List<CoreAbility> getAbilitiesByElement(final Element element) {
		final ArrayList<CoreAbility> abilities = new ArrayList<CoreAbility>();
		if (element != null) {
			for (final CoreAbility ability : getAbilities()) {
				if (ability.getElement() == element) {
					abilities.add(ability);
				} else if (ability.getElement() instanceof SubElement) {
					final Element parentElement = ((SubElement) ability.getElement()).getParentElement();
					if (parentElement == element) {
						abilities.add(ability);
					}
				}
			}
		}
		return abilities;
	}

	/**
	 * CoreAbility keeps track of plugins that have registered abilities to use
	 * for bending reload purposes <br>
	 * <b>This isn't a simple list, external use isn't recommended</b>
	 *
	 * @return a list of entrys with the plugin name and path abilities can be
	 *         found at
	 */
	public static List<String> getAddonPlugins() {
		return ADDON_PLUGINS;
	}

	/**
	 * Returns true if the player has an active CoreAbility instance of type T.
	 *
	 * @param player the player that created the T instance
	 * @param clazz the class for the type of CoreAbility
	 */
	public static <T extends CoreAbility> boolean hasAbility(final Player player, final Class<T> clazz) {
		return getAbility(player, clazz) != null;
	}

	/**
	 * Unloads the ability
	 *
	 * @param clazz Ability class to unload
	 */
	public static <T extends CoreAbility> void unloadAbility(final Class<T> clazz) {
		if (!ABILITIES_BY_CLASS.containsKey(clazz)) {
			return;
		}
		final String name = ABILITIES_BY_CLASS.get(clazz).getName();
		for (final CoreAbility abil : INSTANCES) {
			if (abil.getName() == name) {
				abil.remove();
			}
		}
		ABILITIES_BY_CLASS.remove(clazz);
		ABILITIES_BY_NAME.remove(name);
		ProjectKorra.log.info("Unloaded ability: " + name);
	}

	/**
	 * Returns a Set of all of the players that currently have an active
	 * instance of clazz.
	 *
	 * @param clazz the clazz for the type of CoreAbility
	 */
	public static Set<Player> getPlayers(final Class<? extends CoreAbility> clazz) {
		final HashSet<Player> players = new HashSet<>();
		if (clazz != null) {
			final Map<UUID, Map<Integer, CoreAbility>> uuidMap = INSTANCES_BY_PLAYER.get(clazz);
			if (uuidMap != null) {
				for (final UUID uuid : uuidMap.keySet()) {
					final Player uuidPlayer = Bukkit.getPlayer(uuid);
					if (uuidPlayer != null) {
						players.add(uuidPlayer);
					}
				}
			}
		}
		return players;
	}

	/**
	 * Scans and loads plugin CoreAbilities, and Addon CoreAbilities that are
	 * located in a Jar file inside of the /ProjectKorra/Abilities/ folder.
	 */
	public static void registerAbilities() {
		ABILITIES_BY_NAME.clear();
		ABILITIES_BY_CLASS.clear();
		registerPluginAbilities(ProjectKorra.plugin, "com.projectkorra");
		registerAddonAbilities("/Abilities/");
	}

	/**
	 * Scans a JavaPlugin and registers CoreAbility class files. Does not work.
	 * Use {@link CoreAbility.registerPluginAbilities} instead!
	 *
	 * @param plugin a JavaPlugin containing CoreAbility class files
	 * @param packagePrefix a prefix of the package name, used to increase
	 *            performance
	 * @see #getAbilities()
	 * @see #getAbility(String)
	 */
	@Deprecated
	public static void legacyRegisterPluginAbilities(final JavaPlugin plugin, final String packagePrefix) {
		final List<String> disabled = new ArrayList<String>(); // this way multiple classes with the same name only show once.
		if (plugin == null) {
			return;
		}

		final Class<? extends JavaPlugin> pluginClass = plugin.getClass();
		final ClassLoader loader = pluginClass.getClassLoader();

		final ReflectionFactory rf = ReflectionFactory.getReflectionFactory();

		try {
			for (final ClassInfo info : ClassPath.from(loader).getAllClasses()) {
				if (!info.getPackageName().startsWith(packagePrefix)) {
					continue;
				}

				Class<?> clazz = null;
				try {
					clazz = info.load();
					if (!CoreAbility.class.isAssignableFrom(clazz) || clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
						continue;
					}

					final Constructor<?> objDef = CoreAbility.class.getDeclaredConstructor();
					final Constructor<?> intConstr = rf.newConstructorForSerialization(clazz, objDef);
					final CoreAbility ability = (CoreAbility) clazz.cast(intConstr.newInstance());

					if (ability == null || ability.getName() == null) {
						continue;
					} else if (!ability.isEnabled() && !disabled.contains(ability.getName())) {
						plugin.getLogger().info(ability.getName() + " is disabled");
						disabled.add(ability.getName());
						continue;
					}

					final String name = ability.getName();
					ABILITIES_BY_NAME.put(ability.getName().toLowerCase(), ability);
					ABILITIES_BY_CLASS.put(ability.getClass(), ability);

					if (ability instanceof ComboAbility) {
						final ComboAbility combo = (ComboAbility) ability;
						if (combo.getCombination() != null) {
							ComboManager.getComboAbilities().put(name, new ComboManager.ComboAbilityInfo(name, combo.getCombination(), combo));
							ComboManager.getDescriptions().put(name, ability.getDescription());
							ComboManager.getInstructions().put(name, ability.getInstructions());
							String author = "";
							if (ability instanceof AddonAbility) {
								author = ((AddonAbility) ability).getAuthor();
							}
							ComboManager.getAuthors().put(name, author);
						}
					}

					if (ability instanceof MultiAbility) {
						final MultiAbility multiAbil = (MultiAbility) ability;
						MultiAbilityManager.multiAbilityList.add(new MultiAbilityInfo(name, multiAbil.getMultiAbilities()));
					}

					if (ability instanceof PassiveAbility) {
						ability.setHiddenAbility(true);
						PassiveManager.getPassives().put(name, ability);
					}

					if (ability instanceof AddonAbility) {
						final AddonAbility addon = (AddonAbility) ability;
						addon.load();
					}
				} catch (final Exception e) {
					e.printStackTrace();
				} catch (final Error e) {
					e.printStackTrace();
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Scans a JavaPlugin and registers CoreAbility class files.
	 *
	 * @param plugin a JavaPlugin containing CoreAbility class files
	 * @param packageBase a prefix of the package name, used to increase
	 *            performance
	 * @see #getAbilities()
	 * @see #getAbility(String)
	 */
	public static void registerPluginAbilities(final JavaPlugin plugin, final String packageBase) {
		final AbilityLoader<CoreAbility> abilityLoader = new AbilityLoader<CoreAbility>(plugin, packageBase);
		final List<CoreAbility> loadedAbilities = abilityLoader.load(CoreAbility.class, CoreAbility.class);
		final String entry = plugin.getName() + "::" + packageBase;
		if (!ADDON_PLUGINS.contains(entry)) {
			ADDON_PLUGINS.add(entry);
		}

		for (final CoreAbility coreAbil : loadedAbilities) {
			if (!coreAbil.isEnabled()) {
				plugin.getLogger().info(coreAbil.getName() + " is disabled");
				continue;
			}

			final String name = coreAbil.getName();

			if (name == null) {
				plugin.getLogger().warning("Ability " + coreAbil.getClass().getName() + " has no name?");
				continue;
			}

			try {
				ABILITIES_BY_NAME.put(name.toLowerCase(), coreAbil);
				ABILITIES_BY_CLASS.put(coreAbil.getClass(), coreAbil);

				if (coreAbil instanceof ComboAbility) {
					final ComboAbility combo = (ComboAbility) coreAbil;
					if (combo.getCombination() != null) {
						ComboManager.getComboAbilities().put(name, new ComboManager.ComboAbilityInfo(name, combo.getCombination(), combo));
						ComboManager.getDescriptions().put(name, coreAbil.getDescription());
						ComboManager.getInstructions().put(name, coreAbil.getInstructions());
					}
				}

				if (coreAbil instanceof MultiAbility) {
					final MultiAbility multiAbil = (MultiAbility) coreAbil;
					MultiAbilityManager.multiAbilityList.add(new MultiAbilityInfo(name, multiAbil.getMultiAbilities()));
				}

				if (coreAbil instanceof PassiveAbility) {
					PassiveAbility passive = (PassiveAbility) coreAbil;
					coreAbil.setHiddenAbility(true);
					PassiveManager.getPassives().put(name, coreAbil);
					if (!PassiveManager.getPassiveClasses().containsKey(passive)) {
						PassiveManager.getPassiveClasses().put(passive, coreAbil.getClass());
					}
				}
			} catch (Exception | Error e) {
				plugin.getLogger().warning("The ability " + coreAbil.getName() + " was not able to load, if this message shows again please remove it!");
				e.printStackTrace();
				ABILITIES_BY_NAME.remove(name.toLowerCase());
				ABILITIES_BY_CLASS.remove(coreAbil.getClass());
			}
		}
	}

	/**
	 * Scans all of the Jar files inside of /ProjectKorra/folder and registers
	 * all of the CoreAbility class files that were found.
	 *
	 * @param folder the name of the folder to scan
	 * @see #getAbilities()
	 * @see #getAbility(String)
	 */
	public static void registerAddonAbilities(final String folder) {
		final ProjectKorra plugin = ProjectKorra.plugin;
		final File path = new File(plugin.getDataFolder().toString() + folder);
		if (!path.exists()) {
			path.mkdir();
			return;
		}

		final AddonAbilityLoader<CoreAbility> abilityLoader = new AddonAbilityLoader<CoreAbility>(plugin, path);
		final List<CoreAbility> loadedAbilities = abilityLoader.load(CoreAbility.class, CoreAbility.class);

		for (final CoreAbility coreAbil : loadedAbilities) {
			if (!(coreAbil instanceof AddonAbility)) {
				plugin.getLogger().warning(coreAbil.getName() + " is an addon ability and must implement the AddonAbility interface");
				continue;
			} else if (!coreAbil.isEnabled()) {
				plugin.getLogger().info(coreAbil.getName() + " is disabled");
				continue;
			}

			final AddonAbility addon = (AddonAbility) coreAbil;
			final String name = coreAbil.getName();

			try {
				addon.load();
				ABILITIES_BY_NAME.put(name.toLowerCase(), coreAbil);
				ABILITIES_BY_CLASS.put(coreAbil.getClass(), coreAbil);

				if (coreAbil instanceof ComboAbility) {
					final ComboAbility combo = (ComboAbility) coreAbil;
					if (combo.getCombination() != null) {
						ComboManager.getComboAbilities().put(name, new ComboManager.ComboAbilityInfo(name, combo.getCombination(), combo));
						ComboManager.getDescriptions().put(name, coreAbil.getDescription());
						ComboManager.getInstructions().put(name, coreAbil.getInstructions());
						ComboManager.getAuthors().put(name, addon.getAuthor());
					}
				}

				if (coreAbil instanceof MultiAbility) {
					final MultiAbility multiAbil = (MultiAbility) coreAbil;
					MultiAbilityManager.multiAbilityList.add(new MultiAbilityInfo(name, multiAbil.getMultiAbilities()));
				}

				if (coreAbil instanceof PassiveAbility) {
					PassiveAbility passive = (PassiveAbility) coreAbil;
					coreAbil.setHiddenAbility(true);
					PassiveManager.getPassives().put(name, coreAbil);
					if (!PassiveManager.getPassiveClasses().containsKey(passive)) {
						PassiveManager.getPassiveClasses().put(passive, coreAbil.getClass());
					}
				}
			} catch (Exception | Error e) {
				plugin.getLogger().warning("The ability " + coreAbil.getName() + " was not able to load, if this message shows again please remove it!");
				e.printStackTrace();
				addon.stop();
				ABILITIES_BY_NAME.remove(name.toLowerCase());
				ABILITIES_BY_CLASS.remove(coreAbil.getClass());
			}
		}
	}

	public long getStartTime() {
		return this.startTime;
	}

	public long getStartTick() {
		return this.startTick;
	}

	public static long getCurrentTick() {
		return currentTick;
	}

	public long getRunningTicks() {
		return currentTick - this.startTick;
	}

	public boolean isStarted() {
		return this.started;
	}

	public boolean isRemoved() {
		return this.removed;
	}

	public BendingPlayer getBendingPlayer() {
		return this.bPlayer;
	}

	public int getId() {
		return this.id;
	}

	@Override
	public boolean isHiddenAbility() {
		return this.hidden;
	}

	public void setHiddenAbility(final boolean hidden) {
		this.hidden = hidden;
	}

	@Override
	public boolean isEnabled() {
		if (this instanceof AddonAbility) {
			return true;
		}

		String elementName = this.getElement().getName();
		if (this.getElement() instanceof SubElement) {
			elementName = ((SubElement) this.getElement()).getParentElement().getName();
		}

		String tag = null;
		if (this instanceof PassiveAbility) {
			tag = "Abilities." + elementName + ".Passive." + this.getName() + ".Enabled";
		} else {
			tag = "Abilities." + elementName + "." + this.getName() + ".Enabled";
		}

		if (getConfig().isBoolean(tag)) {
			return getConfig().getBoolean(tag);
		} else {
			return true;
		}
	}

	@Override
	public String getInstructions() {

		String elementName = this.getElement().getName();
		if (this.getElement() instanceof SubElement) {
			elementName = ((SubElement) this.getElement()).getParentElement().getName();
		}
		if (this instanceof ComboAbility) {
			elementName = elementName + ".Combo";
		}
		return ConfigManager.languageConfig.get().contains("Abilities." + elementName + "." + this.getName() + ".Instructions") ? ConfigManager.languageConfig.get().getString("Abilities." + elementName + "." + this.getName() + ".Instructions") : "";
	}

	@Override
	public String getDescription() {
		String elementName = this.getElement().getName();
		if (this.getElement() instanceof SubElement) {
			elementName = ((SubElement) this.getElement()).getParentElement().getName();
		}
		if (this instanceof PassiveAbility) {
			return ConfigManager.languageConfig.get().getString("Abilities." + elementName + ".Passive." + this.getName() + ".Description");
		} else if (this instanceof ComboAbility) {
			return ConfigManager.languageConfig.get().getString("Abilities." + elementName + ".Combo." + this.getName() + ".Description");
		}
		return ConfigManager.languageConfig.get().getString("Abilities." + elementName + "." + this.getName() + ".Description");
	}

	public String getMovePreview(final Player player) {
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		String displayedMessage = getMovePreviewWithoutCooldownTimer(player, false);
		if (bPlayer.isOnCooldown(this)) {
			final long cooldown = bPlayer.getCooldown(this.getName()) - System.currentTimeMillis();
			displayedMessage += this.getElement().getColor() + " - " + TimeUtil.formatTime(cooldown);
		}

		return displayedMessage;
	}

	public String getMovePreviewWithoutCooldownTimer(final Player player, boolean forceCooldown) {
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		String displayedMessage = "";
		if (forceCooldown || bPlayer.isOnCooldown(this)) {
			displayedMessage = this.getElement().getColor() + "" + ChatColor.STRIKETHROUGH + this.getName();
		} else {
			if (bPlayer.getStance() != null && bPlayer.getStance().getName().equals(this.getName())) {
				displayedMessage = this.getElement().getColor() + "" + ChatColor.UNDERLINE + this.getName();
			} else {
				displayedMessage = this.getElement().getColor() + this.getName();
			}
		}
		return displayedMessage;
	}

	@Override
	public Player getPlayer() {
		return this.player;
	}

	/**
	 * Changes the player that owns this ability instance. Used for redirection
	 * and other abilities that change the player object.
	 *
	 * @param target The player who now controls the ability
	 */
	public void setPlayer(final Player target) {
		if (target == this.player) {
			return;
		}

		final Class<? extends CoreAbility> clazz = this.getClass();

		// The mapping from player UUID to a map of the player's instances.
		Map<UUID, Map<Integer, CoreAbility>> classMap = INSTANCES_BY_PLAYER.get(clazz);

		if (classMap != null) {
			// The map of AbilityId to Ability for the current player.
			final Map<Integer, CoreAbility> playerMap = classMap.get(this.player.getUniqueId());

			if (playerMap != null) {
				// Remove the ability from the current player's map.
				playerMap.remove(this.id);

				if (playerMap.isEmpty()) {
					// Remove the player's empty ability map from global instances map.
					classMap.remove(this.player.getUniqueId());
				}
			}

			if (classMap.isEmpty()) {
				INSTANCES_BY_PLAYER.remove(this.getClass());
			}
		}

		// Add a new map for the current ability if it doesn't exist in the global map.
		if (!INSTANCES_BY_PLAYER.containsKey(clazz)) {
			INSTANCES_BY_PLAYER.put(clazz, new ConcurrentHashMap<>());
		}

		classMap = INSTANCES_BY_PLAYER.get(clazz);

		// Create an AbilityId to Ability map for the target player if it doesn't exist.
		if (!classMap.containsKey(target.getUniqueId())) {
			classMap.put(target.getUniqueId(), new ConcurrentHashMap<>());
		}

		// Add the current instance to the target player's ability map.
		classMap.get(target.getUniqueId()).put(this.getId(), this);

		this.player = target;

		final BendingPlayer newBendingPlayer = BendingPlayer.getBendingPlayer(target);
		if (newBendingPlayer != null) {
			this.bPlayer = newBendingPlayer;
		}
	}

	/**
	 * Used by the CollisionManager to check if two instances can collide with
	 * each other. For example, an EarthBlast is not collidable right when the
	 * person selects a source block, but it is collidable once the block begins
	 * traveling.
	 *
	 * @return true if the instance is currently collidable
	 * @see CollisionManager
	 */
	public boolean isCollidable() {
		return true;
	}

	/**
	 * The radius for collision of the ability instance. Some circular abilities
	 * are better represented with 1 single Location with a small or large
	 * radius, such as AirShield, FireShield, EarthSmash, WaterManipulation,
	 * EarthBlast, etc. Some abilities consist of multiple Locations with small
	 * radiuses, such as AirSpout, WaterSpout, Torrent, RaiseEarth, AirSwipe,
	 * FireKick, etc.
	 *
	 * @return the radius for a location returned by {@link #getLocations()}
	 * @see CollisionManager
	 */
	public double getCollisionRadius() {
		return DEFAULT_COLLISION_RADIUS;
	}

	/**
	 * Called when this ability instance collides with another. Some abilities
	 * may want advanced behavior on a Collision; e.g. FireCombos only remove
	 * the stream that was hit rather than the entire ability.
	 * <p>
	 * collision.getAbilitySecond() - the ability that we are colliding with
	 * collision.isRemovingFirst() - if this ability should be removed
	 * <p>
	 * This ability should only worry about itself because handleCollision will
	 * be called for the other ability instance as well.
	 *
	 * @param collision with data about the other ability instance
	 * @see CollisionManager
	 */
	public void handleCollision(final Collision collision) {
		if (collision.isRemovingFirst()) {
			this.remove();
		}
	}

	/**
	 * A List of Locations used to represent the ability. Some abilities might
	 * just be 1 Location with a radius, while some might be multiple Locations
	 * with small radiuses.
	 *
	 * @return a List of the ability's locations
	 * @see CollisionManager
	 */
	public List<Location> getLocations() {
		final ArrayList<Location> locations = new ArrayList<>();
		locations.add(this.getLocation());
		return locations;
	}

	public CoreAbility addAttributeModifier(final String attribute, final Number value, final AttributeModifier modification) {
		return this.addAttributeModifier(attribute, value, modification, AttributePriority.MEDIUM);
	}

	public CoreAbility addAttributeModifier(final String attribute, final Number value, final AttributeModifier modificationType, final AttributePriority priority) {
		Validate.notNull(attribute, "attribute cannot be null");
		Validate.notNull(value, "value cannot be null");
		Validate.notNull(modificationType, "modifierMethod cannot be null");
		Validate.notNull(priority, "priority cannot be null");
		Validate.isTrue(ATTRIBUTE_FIELDS.containsKey(this.getClass()) && ATTRIBUTE_FIELDS.get(this.getClass()).containsKey(attribute), "Attribute " + attribute + " is not a defined Attribute for " + this.getName());
		if (!this.attributeModifiers.containsKey(attribute)) {
			this.attributeModifiers.put(attribute, new HashMap<>());
		}
		if (!this.attributeModifiers.get(attribute).containsKey(priority)) {
			this.attributeModifiers.get(attribute).put(priority, new HashSet<>());
		}
		this.attributeModifiers.get(attribute).get(priority).add(Pair.of(value, modificationType));
		return this;
	}

	public CoreAbility setAttribute(final String attribute, final Object value) {
		Validate.notNull(attribute, "attribute cannot be null");
		Validate.notNull(value, "value cannot be null");
		Validate.isTrue(ATTRIBUTE_FIELDS.containsKey(this.getClass()) && ATTRIBUTE_FIELDS.get(this.getClass()).containsKey(attribute), "Attribute " + attribute + " is not a defined Attribute for " + this.getName());
		this.attributeValues.put(attribute, value);
		return this;
	}

	private void modifyAttributes() {
		for (final String attribute : this.attributeModifiers.keySet()) {
			final Field field = ATTRIBUTE_FIELDS.get(this.getClass()).get(attribute);
			final boolean accessibility = field.isAccessible();
			field.setAccessible(true);
			try {
				for (final AttributePriority priority : AttributePriority.values()) {
					if (this.attributeModifiers.get(attribute).containsKey(priority)) {
						for (final Pair<Number, AttributeModifier> pair : this.attributeModifiers.get(attribute).get(priority)) {
							final Object get = field.get(this);
							Validate.isTrue(get instanceof Number, "The field " + field.getName() + " cannot algebraically be modified.");
							final Number oldValue = (Number) field.get(this);
							final Number newValue = pair.getRight().performModification(oldValue, pair.getLeft());
							field.set(this, newValue);
						}
					}
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			} finally {
				field.setAccessible(accessibility);
			}
		}
		this.attributeValues.forEach((attribute, value) -> {
			final Field field = ATTRIBUTE_FIELDS.get(this.getClass()).get(attribute);
			final boolean accessibility = field.isAccessible();
			field.setAccessible(true);
			try {
				field.set(this, value);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			} finally {
				field.setAccessible(accessibility);
			}
		});
	}

	/**
	 * @return the current FileConfiguration for the plugin
	 */
	public static FileConfiguration getConfig() {
		return ConfigManager.getConfig();
	}

	/**
	 * @return the language.yml for the plugin
	 */
	public static FileConfiguration getLanguageConfig() {
		return ConfigManager.languageConfig.get();
	}

	/**
	 * Returns a String used to debug potential CoreAbility memory that can be
	 * caused by a developer forgetting to call {@link #remove()}
	 */
	public static String getDebugString() {
		final StringBuilder sb = new StringBuilder();
		int playerCounter = 0;
		final HashMap<String, Integer> classCounter = new HashMap<>();

		for (final Map<UUID, Map<Integer, CoreAbility>> map1 : INSTANCES_BY_PLAYER.values()) {
			playerCounter++;
			for (final Map<Integer, CoreAbility> map2 : map1.values()) {
				for (final CoreAbility coreAbil : map2.values()) {
					final String simpleName = coreAbil.getClass().getSimpleName();

					if (classCounter.containsKey(simpleName)) {
						classCounter.put(simpleName, classCounter.get(simpleName) + 1);
					} else {
						classCounter.put(simpleName, 1);
					}
				}
			}
		}

		for (final Set<CoreAbility> set : INSTANCES_BY_CLASS.values()) {
			for (final CoreAbility coreAbil : set) {
				final String simpleName = coreAbil.getClass().getSimpleName();
				if (classCounter.containsKey(simpleName)) {
					classCounter.put(simpleName, classCounter.get(simpleName) + 1);
				} else {
					classCounter.put(simpleName, 1);
				}
			}
		}

		sb.append("Class->UUID's in memory: " + playerCounter + "\n");
		sb.append("Abilities in memory:\n");
		for (final String className : classCounter.keySet()) {
			sb.append(className + ": " + classCounter.get(className) + "\n");
		}
		return sb.toString();
	}

	public static double getDefaultCollisionRadius() {
		return DEFAULT_COLLISION_RADIUS;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

}

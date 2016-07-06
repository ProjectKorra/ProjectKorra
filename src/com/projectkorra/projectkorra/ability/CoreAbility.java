package com.projectkorra.projectkorra.ability;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.jar.JarFile;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.util.AbilityLoader;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager.MultiAbilityInfo;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.event.AbilityEndEvent;
import com.projectkorra.projectkorra.event.AbilityProgressEvent;
import com.projectkorra.projectkorra.event.AbilityStartEvent;

import sun.reflect.ReflectionFactory;

/**
 * CoreAbility provides default implementation of an Ability, including methods to control 
 * the life cycle of a specific instance. CoreAbility also provides a system to load CoreAbilities
 * within a {@link JavaPlugin}, or located in an external {@link JarFile}.
 * 
 * @see #start()
 * @see #progress()
 * @see #remove()
 * @see #registerAddonAbilities(String)
 * @see #registerPluginAbilities(JavaPlugin, String)
 */
public abstract class CoreAbility implements Ability {
	
	private static final Map<Class<? extends CoreAbility>, Map<UUID, Map<Integer, CoreAbility>>> INSTANCES = new ConcurrentHashMap<>();
	private static final Map<Class<? extends CoreAbility>, Set<CoreAbility>> INSTANCES_BY_CLASS = new ConcurrentHashMap<>();
	private static final Map<String, CoreAbility> ABILITIES_BY_NAME = new ConcurrentSkipListMap<>();
	
	private static int idCounter;

	protected long startTime;
	protected Player player;
	protected BendingPlayer bPlayer;
	
	private boolean started;
	private boolean removed;
	private int id;

	static {
		idCounter = Integer.MIN_VALUE;
	}
	
	/**
	 * The default constructor is needed to create a fake instance of each CoreAbility via reflection
	 * in {@link #registerAbilities()}. More specifically, {@link #registerPluginAbilities} calls
	 * getDeclaredConstructor which is only usable with a public default constructor. Reflection lets us
	 * create a list of all of the plugin's abilities when the plugin first loads.
	 * 
	 * @see #ABILITIES_BY_NAME
	 * @see #getAbility(String)
	 */
	public CoreAbility() {}

	/**
	 * Creates a new CoreAbility instance but does not start it.
	 * 
	 * @param player the non-null player that created this instance
	 * @see #start()
	 */
	public CoreAbility(Player player) {
		if (player == null) {
			return;
		}
		
		this.player = player;
		this.bPlayer = BendingPlayer.getBendingPlayer(player);
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
	 * Causes the ability to begin updating every tick by calling {@link #progress()} 
	 * until {@link #remove()} is called. This method cannot be overridden, and any code 
	 * that needs to be performed before start should be handled in the constructor.
	 * 
	 * @see #getStartTime()
	 * @see #isStarted()
	 * @see #isRemoved()
	 */
	public final void start() {
		if (player == null) {
			return;
		}
		AbilityStartEvent event = new AbilityStartEvent(this);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if(event.isCancelled()) {
			remove();
			return;
		}
		this.started = true;
		this.startTime = System.currentTimeMillis();
		Class<? extends CoreAbility> clazz = getClass();
		UUID uuid = player.getUniqueId();

		if (!INSTANCES.containsKey(clazz)) {
			INSTANCES.put(clazz, new ConcurrentHashMap<UUID, Map<Integer, CoreAbility>>());
		}
		if (!INSTANCES.get(clazz).containsKey(uuid)) {
			INSTANCES.get(clazz).put(uuid, new ConcurrentHashMap<Integer, CoreAbility>());
		}
		if (!INSTANCES_BY_CLASS.containsKey(clazz)) {
			INSTANCES_BY_CLASS.put(clazz, Collections.newSetFromMap(new ConcurrentHashMap<CoreAbility, Boolean>()));
		}

		INSTANCES.get(clazz).get(uuid).put(this.id, this);
		INSTANCES_BY_CLASS.get(clazz).add(this);
	}

	/**
	 * Causes this CoreAbility instance to be removed, and {@link #progress} will no longer
	 * be called every tick. If this method is overridden then the new method must call 
	 * <b>super.remove()</b>.
	 * 
	 * {@inheritDoc}
	 * @see #isRemoved()
	 */
	@Override
	public void remove() {
		if (player == null) {
			return;
		}
		
		Bukkit.getServer().getPluginManager().callEvent(new AbilityEndEvent(this));
		removed = true;
		
		Map<UUID, Map<Integer, CoreAbility>> classMap = INSTANCES.get(getClass());
		if (classMap != null) {
			Map<Integer, CoreAbility> playerMap = classMap.get(player.getUniqueId());
			if (playerMap != null) {
				playerMap.remove(this.id);
				if (playerMap.size() == 0) {
					classMap.remove(player.getUniqueId());
				}
			}
			
			if (classMap.size() == 0) {
				INSTANCES.remove(getClass());
			}
		}

		if (INSTANCES_BY_CLASS.containsKey(getClass())) {
			INSTANCES_BY_CLASS.get(getClass()).remove(this);
		}
	}

	/**
	 * Causes {@link #progress()} to be called on every CoreAbility instance
	 * that has been started and has not been removed.
	 */
	public static void progressAll() {
		for (Set<CoreAbility> setAbils : INSTANCES_BY_CLASS.values()) {
			for (CoreAbility abil : setAbils) {
				abil.progress();
				Bukkit.getServer().getPluginManager().callEvent(new AbilityProgressEvent(abil));
			}
		}
	}

	/**
	 * Removes every CoreAbility instance that has been started but not yet removed.
	 */
	public static void removeAll() {
		for (Set<CoreAbility> setAbils : INSTANCES_BY_CLASS.values()) {
			for (CoreAbility abil : setAbils) {
				abil.remove();
			}
		}
		
		for (CoreAbility coreAbility : ABILITIES_BY_NAME.values()) {
			if (coreAbility instanceof AddonAbility) {
				AddonAbility addon = (AddonAbility) coreAbility;
				addon.stop();
			}
		}
	}

	/**
	 * Returns any T CoreAbility that has been started and not yet removed. May return null if
	 * no such ability exists.
	 * 
	 * @param player the player that created the CoreAbility instance
	 * @param clazz the class of the type of CoreAbility
	 * @return a CoreAbility instance or null
	 */
	public static <T extends CoreAbility> T getAbility(Player player, Class<T> clazz) {
		Collection<T> abils = getAbilities(player, clazz);
		if (abils.iterator().hasNext()) {
			return abils.iterator().next();
		}
		return null;
	}
	
	/**
	 * Returns a "fake" instance for the CoreAbility represented by abilityName. This method
	 * does not look into CoreAbility instances that were created by Players, instead this
	 * method looks at the CoreAbilities that were created via Reflection by {@link #registerAbilities()}
	 * when the plugin was first loaded.
	 * 
	 * <p>These "fake" instances have a null player, but methods such as
	 * {@link Ability#getName()}, and {@link Ability#getElement()} will still work, as will checking
	 * the type of the ability with instanceof.
	 * 
	 * <p>
	 * CoreAbility coreAbil = getAbility(someString); <br>
	 * if (coreAbil instanceof FireAbility && coreAbil.isSneakAbility())
	 * 
	 * @param abilityName the name of a loaded CoreAbility
	 * @return a "fake" CoreAbility instance, or null if no such ability exists
	 */
	public static CoreAbility getAbility(String abilityName) {
		return abilityName != null ? ABILITIES_BY_NAME.get(abilityName.toLowerCase()) : null;
	}
	
	/**
	 * Returns a list of "fake" instances for each ability that was loaded by {@link #registerAbilities()}
	 */
	public static ArrayList<CoreAbility> getAbilities() {
		return new ArrayList<CoreAbility>(ABILITIES_BY_NAME.values());
	}

	/**
	 * Returns a Collection of all of the player created instances for a specific type of CoreAbility.
	 * 
	 * @param clazz the class for the type of CoreAbilities
	 * @return a Collection of real instances
	 */
	@SuppressWarnings("unchecked")
	public static <T extends CoreAbility> Collection<T> getAbilities(Class<T> clazz) {
		if (clazz == null || INSTANCES_BY_CLASS.get(clazz) == null || INSTANCES_BY_CLASS.get(clazz).size() == 0) {
			return Collections.emptySet();
		}
		return (Collection<T>) CoreAbility.INSTANCES_BY_CLASS.get(clazz);
	}

	/**
	 * Returns a Collection of specific CoreAbility instances that were created by the specified player.
	 * 
	 * @param player the player that created the instances
	 * @param clazz the class for the type of CoreAbilities
	 * @return a Collection of real instances
	 */
	@SuppressWarnings("unchecked")
	public static <T extends CoreAbility> Collection<T> getAbilities(Player player, Class<T> clazz) {
		if (player == null || clazz == null || INSTANCES.get(clazz) == null || INSTANCES.get(clazz).get(player.getUniqueId()) == null) {
			return Collections.emptySet();
		}
		return (Collection<T>) INSTANCES.get(clazz).get(player.getUniqueId()).values();
	}
	
	/**
	 * Returns an List of fake instances that were loaded by {@link #registerAbilities()} filtered by Element.
	 * 
	 * @param element the Element of the loaded abilities
	 * @return a list of fake CoreAbility instances
	 */
	public static List<CoreAbility> getAbilitiesByElement(Element element) {
		ArrayList<CoreAbility> abilities = new ArrayList<CoreAbility>();
		if (element != null) {
			for (CoreAbility ability : getAbilities()) {
				if (ability.getElement() == element) {
					abilities.add(ability);
				} else if (ability.getElement() instanceof SubElement) {
					Element parentElement = ((SubElement) ability.getElement()).getParentElement();
					if (parentElement == element) {
						abilities.add(ability);
					}
				}
			}
		}
		return abilities;
	}
	
	/**
	 * Returns true if the player has an active CoreAbility instance of type T.
	 * 
	 * @param player the player that created the T instance
	 * @param clazz the class for the type of CoreAbility
	 */
	public static <T extends CoreAbility> boolean hasAbility(Player player, Class<T> clazz) {
		return getAbility(player, clazz) != null;
	}
	
	/**
	 * Returns a Set of all of the players that currently have an active instance of clazz.
	 * 
	 * @param clazz the clazz for the type of CoreAbility
	 */
	public static Set<Player> getPlayers(Class<? extends CoreAbility> clazz) {
		HashSet<Player> players = new HashSet<>();
		if (clazz != null) {
			Map<UUID, Map<Integer, CoreAbility>> uuidMap = INSTANCES.get(clazz);
			if (uuidMap != null) {
				for (UUID uuid : uuidMap.keySet()) {
					Player uuidPlayer = Bukkit.getPlayer(uuid);
					if (uuidPlayer != null) {
						players.add(uuidPlayer);
					}
				}
			}
		}
		return players;
	}
	
	/**
	 * Scans and loads plugin CoreAbilities, and Addon CoreAbilities that are located
	 * in a Jar file inside of the /ProjectKorra/Abilities/ folder.
	 */
	public static void registerAbilities() {
		ABILITIES_BY_NAME.clear();
		registerPluginAbilities(ProjectKorra.plugin, "com.projectkorra");
		registerAddonAbilities("/Abilities/");
	}

	/**
	 * Scans a JavaPlugin and registers CoreAbility class files.
	 *  
	 * @param plugin a JavaPlugin containing CoreAbility class files
	 * @param packagePrefix a prefix of the package name, used to increase performance
	 * @see #getAbilities()
	 * @see #getAbility(String)
	 */
	public static void registerPluginAbilities(JavaPlugin plugin, String packagePrefix) {
		List<String> disabled = new ArrayList<String>(); //this way multiple classes with the same name only show once
		if (plugin == null) {
			return;
		}
		
		Class<?> pluginClass = plugin.getClass();
		ClassLoader loader = pluginClass.getClassLoader();
		ReflectionFactory rf = ReflectionFactory.getReflectionFactory();
		
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
					
					Constructor<?> objDef = CoreAbility.class.getDeclaredConstructor();
					Constructor<?> intConstr = rf.newConstructorForSerialization(clazz, objDef);;
					CoreAbility ability = (CoreAbility) clazz.cast(intConstr.newInstance());

					if (ability == null || ability.getName() == null) {
						continue;
					} else if (!ability.isEnabled() && !disabled.contains(ability.getName())) {
						plugin.getLogger().info(ability.getName() + " is disabled");
						disabled.add(ability.getName());
						continue;
					}

					String name = ability.getName();
					ABILITIES_BY_NAME.put(ability.getName().toLowerCase(), ability);

					if (ability instanceof ComboAbility) {
						ComboAbility combo = (ComboAbility) ability;
						if (combo.getCombination() != null) {
							ComboManager.getComboAbilities().put(name, new ComboManager.ComboAbilityInfo(name, combo.getCombination(), combo));
							ComboManager.getDescriptions().put(name, ability.getDescription());
							ComboManager.getInstructions().put(name, combo.getInstructions());
							String author = "";
							if (ability instanceof AddonAbility) {
								author = ((AddonAbility) ability).getAuthor();
							}
							ComboManager.getAuthors().put(name, author);
						}
					}
					
					if (ability instanceof MultiAbility) {
						MultiAbility multiAbil = (MultiAbility) ability;
						MultiAbilityManager.multiAbilityList.add(new MultiAbilityInfo(name, multiAbil.getMultiAbilities()));
					}
					
					if (ability instanceof AddonAbility) {
						AddonAbility addon = (AddonAbility) ability;
						addon.load();
					}
				} catch (Exception e) {
				} catch (Error e) {
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
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
	public static void registerAddonAbilities(String folder) {
		ProjectKorra plugin = ProjectKorra.plugin;
		File path = new File(plugin.getDataFolder().toString() + folder);
		if (!path.exists()) {
			path.mkdir();
			return;
		}
		
		AbilityLoader<CoreAbility> abilityLoader = new AbilityLoader<CoreAbility>(plugin, path);
		List<CoreAbility> loadedAbilities = abilityLoader.load(CoreAbility.class, CoreAbility.class);
		
		for (CoreAbility coreAbil : loadedAbilities) {
			if (!(coreAbil instanceof AddonAbility)) {
				plugin.getLogger().warning(coreAbil.getName() + " is an addon ability and must implement the AddonAbility interface");
				continue;
			} else if (!coreAbil.isEnabled()) {
				plugin.getLogger().info(coreAbil.getName() + " is disabled");
				continue;
			}
			
			AddonAbility addon = (AddonAbility) coreAbil;
			String name = coreAbil.getName();
			
			try {
				addon.load();
				ABILITIES_BY_NAME.put(name.toLowerCase(), coreAbil);
				
				if (coreAbil instanceof ComboAbility) {
					ComboAbility combo = (ComboAbility) coreAbil;
					if (combo.getCombination() != null) {
						ComboManager.getComboAbilities().put(name, new ComboManager.ComboAbilityInfo(name, combo.getCombination(), combo));
						ComboManager.getDescriptions().put(name, coreAbil.getDescription());
						ComboManager.getInstructions().put(name, combo.getInstructions());
						ComboManager.getAuthors().put(name, addon.getAuthor());
					}
				}
				
				if (coreAbil instanceof MultiAbility) {
					MultiAbility multiAbil = (MultiAbility) coreAbil;
					MultiAbilityManager.multiAbilityList.add(new MultiAbilityInfo(name, multiAbil.getMultiAbilities()));
				}
			} catch (Exception | Error e) {
				plugin.getLogger().warning("The ability " + coreAbil.getName() + " was not able to load, if this message shows again please remove it!");
				e.printStackTrace();
				addon.stop();
				ABILITIES_BY_NAME.remove(name.toLowerCase());				
			}
		}
	}
	
	public long getStartTime() {
		return startTime;
	}

	public boolean isStarted() {
		return started;
	}
	
	public boolean isRemoved() {
		return removed;
	}

	public BendingPlayer getBendingPlayer() {
		return bPlayer;
	}

	public int getId() {
		return id;
	}
	
	@Override
	public boolean isHiddenAbility() {
		return false;
	}
	
	@Override
	public boolean isEnabled() {
		if (this instanceof AddonAbility) {
			return true;
		}
		
		String elementName = getElement().getName();
		if (getElement() instanceof SubElement) {
			elementName = ((SubElement) getElement()).getParentElement().getName();
		}
		
		String tag = null;
		if (this instanceof ComboAbility) {
			tag = "Abilities." + elementName + "." + elementName  + "Combo." + getName() + ".Enabled";
		} else {
			tag = "Abilities." + elementName + "." + getName() + ".Enabled";
		}
		
		if (getConfig().isBoolean(tag)) {
			return getConfig().getBoolean(tag);
		} else {
			return true;
		}
	}
	
	@Override
	public String getDescription() {
		String elementName = getElement().getName();
		if (getElement() instanceof SubElement) {
			elementName = ((SubElement) getElement()).getParentElement().getName();
		}
		return ConfigManager.languageConfig.get().getString("Abilities." + elementName + "." + getName() + ".Description");
	}

	@Override
	public Player getPlayer() {
		return player;
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
	 * Returns a String used to debug potential CoreAbility memory that can be caused
	 * by a developer forgetting to call {@link #remove()}
	 */
	public static String getDebugString() {
		StringBuilder sb = new StringBuilder();
		int playerCounter = 0;
		HashMap<String, Integer> classCounter = new HashMap<>();
		
		for (Map<UUID, Map<Integer, CoreAbility>> map1 : INSTANCES.values()) {
			playerCounter++;
			for (Map<Integer, CoreAbility> map2 : map1.values()) {
				for (CoreAbility coreAbil : map2.values()) {
					String simpleName = coreAbil.getClass().getSimpleName();
					
					if (classCounter.containsKey(simpleName)) {
						classCounter.put(simpleName, classCounter.get(simpleName) + 1);
					} else {
						classCounter.put(simpleName, 1);
					}
				}
			}
		}
		
		for (Set<CoreAbility> set : INSTANCES_BY_CLASS.values()) {
			for (CoreAbility coreAbil : set) {
				String simpleName = coreAbil.getClass().getSimpleName();
				if (classCounter.containsKey(simpleName)) {
					classCounter.put(simpleName, classCounter.get(simpleName) + 1);
				} else {
					classCounter.put(simpleName, 1);
				}
			}
		}
		
		sb.append("Class->UUID's in memory: " + playerCounter + "\n");
		sb.append("Abilities in memory:\n");
		for (String className : classCounter.keySet()) {
			sb.append(className + ": " + classCounter.get(className) + "\n");
		}
		return sb.toString();
	}
}

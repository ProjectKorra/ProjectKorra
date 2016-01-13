package com.projectkorra.projectkorra.ability;

import sun.reflect.ReflectionFactory;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.util.AbilityLoader;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager.MultiAbilityInfo;
import com.projectkorra.projectkorra.configuration.ConfigManager;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public abstract class CoreAbility implements Ability {
	
	private static final ConcurrentHashMap<Class<? extends CoreAbility>, ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, CoreAbility>>> INSTANCES = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<Class<? extends CoreAbility>, Set<CoreAbility>> INSTANCES_BY_CLASS = new ConcurrentHashMap<>();
	private static final ConcurrentSkipListMap<String, CoreAbility> ABILITIES_BY_NAME = new ConcurrentSkipListMap<>();
	
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
	
	public CoreAbility() {
		// Need the default constructor for reflection purposes
	}

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

	public void start() {
		if (player == null) {
			return;
		}
		
		this.started = true;
		this.startTime = System.currentTimeMillis();
		Class<? extends CoreAbility> clazz = getClass();
		UUID uuid = player.getUniqueId();

		if (!INSTANCES.containsKey(clazz)) {
			INSTANCES.put(clazz, new ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, CoreAbility>>());
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

	@Override
	public void remove() {
		if (player == null) {
			return;
		}
		
		removed = true;
		
		ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, CoreAbility>> classMap = INSTANCES.get(getClass());
		if (classMap != null) {
			ConcurrentHashMap<Integer, CoreAbility> playerMap = classMap.get(player.getUniqueId());
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

	public static void progressAll() {
		for (Set<CoreAbility> setAbils : INSTANCES_BY_CLASS.values()) {
			for (CoreAbility abil : setAbils) {
				abil.progress();
			}
		}
	}

	public static void removeAll() {
		for (Set<CoreAbility> setAbils : INSTANCES_BY_CLASS.values()) {
			for (CoreAbility abil : setAbils) {
				abil.remove();
			}
		}
	}

	public static void removeAll(Class<? extends CoreAbility> clazz) {
		for (CoreAbility abil : getAbilities(clazz)) {
			abil.remove();
		}
	}

	public static <T extends CoreAbility> T getAbility(Player player, Class<T> clazz) {
		Collection<T> abils = getAbilities(player, clazz);
		if (abils.iterator().hasNext()) {
			return abils.iterator().next();
		}
		return null;
	}
	
	public static CoreAbility getAbility(String abilityName) {
		return abilityName != null ? ABILITIES_BY_NAME.get(abilityName.toLowerCase()) : null;
	}
	
	public static ArrayList<CoreAbility> getAbilities() {
		return new ArrayList<CoreAbility>(ABILITIES_BY_NAME.values());
	}

	@SuppressWarnings("unchecked")
	public static <T extends CoreAbility> Collection<T> getAbilities(Class<T> clazz) {
		if (clazz == null || INSTANCES_BY_CLASS.get(clazz) == null || INSTANCES_BY_CLASS.get(clazz).size() == 0) {
			return Collections.emptySet();
		}
		return (Collection<T>) CoreAbility.INSTANCES_BY_CLASS.get(clazz);
	}

	@SuppressWarnings("unchecked")
	public static <T extends CoreAbility> Collection<T> getAbilities(Player player, Class<T> clazz) {
		if (player == null || clazz == null || INSTANCES.get(clazz) == null || INSTANCES.get(clazz).get(player.getUniqueId()) == null) {
			return Collections.emptySet();
		}
		return (Collection<T>) INSTANCES.get(clazz).get(player.getUniqueId()).values();
	}
	
	public static ArrayList<CoreAbility> getAbilitiesByElement(Element element) {
		ArrayList<CoreAbility> abilities = new ArrayList<CoreAbility>();
		if (element != null) {
			for (CoreAbility ability : getAbilities()) {
				if (ability.getElement() == element) {
					abilities.add(ability);
				} else if (ability instanceof SubAbility) {
					SubAbility subAbil = (SubAbility) ability;
					if (subAbil.getParentElement() == element) {
						abilities.add(ability);
					}
				}
			}
		}
		return abilities;
	}
	
	public static <T extends CoreAbility> boolean hasAbility(Player player, Class<T> clazz) {
		return getAbility(player, clazz) != null;
	}
	
	public static HashSet<Player> getPlayers(Class<? extends CoreAbility> clazz) {
		HashSet<Player> players = new HashSet<>();
		if (clazz != null) {
			ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, CoreAbility>> uuidMap = INSTANCES.get(clazz);
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
	
	public static void registerAbilities() {
		ABILITIES_BY_NAME.clear();
		registerPluginAbilities(ProjectKorra.plugin, "com.projectkorra");
		registerAddonAbilities("/Abilities/");
	}

	public static void registerPluginAbilities(JavaPlugin plugin, String packagePrefix) {
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

					if (ability != null && ability.getName() != null) {
						ABILITIES_BY_NAME.put(ability.getName().toLowerCase(), ability);
					}
				} catch (Exception e) {
				} catch (Error e) {
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void registerAddonAbilities(String folder) {
		ProjectKorra plugin = ProjectKorra.plugin;
		File path = new File(plugin.getDataFolder().toString() + folder);
		AbilityLoader<CoreAbility> abilityLoader = new AbilityLoader<CoreAbility>(plugin, path);
		List<CoreAbility> loadedAbilities = abilityLoader.load(CoreAbility.class, CoreAbility.class);
		
		for (CoreAbility coreAbil : loadedAbilities) {
			if (!(coreAbil instanceof AddonAbility)) {
				plugin.getLogger().warning(coreAbil.getName() + " is an addon ability and must implement the AddonAbility interface");
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
				ABILITIES_BY_NAME.remove(coreAbil.getName(), coreAbil);				
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
	
	public boolean isHiddenAbility() {
		return false;
	}
	
	@Override
	public String getDescription() {
		if (this instanceof SubAbility) {
			return getConfig().getString("Abilities." + ((SubAbility) this).getParentElement().getName() + "." + getName() + ".Description");
		}
		return getConfig().getString("Abilities." + getElement().getName() + "." + getName() + ".Description");
	}

	@Override
	public Player getPlayer() {
		return player;
	}
	
	public static FileConfiguration getConfig() {
		return ConfigManager.getConfig();
	}
	
	public static String getDebugString() {
		StringBuilder sb = new StringBuilder();
		int playerCounter = 0;
		HashMap<String, Integer> classCounter = new HashMap<>();
		
		for (ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, CoreAbility>> map1 : INSTANCES.values()) {
			playerCounter++;
			for (ConcurrentHashMap<Integer, CoreAbility> map2 : map1.values()) {
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
		sb.append("Abilities in memory\n");
		for (String className : classCounter.keySet()) {
			sb.append(className + ": " + classCounter.get(className));
		}
		return sb.toString();
	}
}

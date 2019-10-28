package com.projectkorra.projectkorra.ability;

import co.aikar.timings.lib.MCTiming;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.info.*;
import com.projectkorra.projectkorra.ability.util.AbilityRegistry;
import com.projectkorra.projectkorra.ability.util.AddonAbilityRegistry;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;
import com.projectkorra.projectkorra.element.Element;
import com.projectkorra.projectkorra.element.SubElement;
import com.projectkorra.projectkorra.event.AbilityProgressEvent;
import com.projectkorra.projectkorra.module.Module;
import com.projectkorra.projectkorra.module.ModuleManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class AbilityManager extends Module {

	private final ComboAbilityManager comboAbilityManager;
	private final MultiAbilityManager multiAbilityManager;
	private final PassiveAbilityManager passiveAbilityManager;

	private final Map<String, AbilityInfo> abilities = new HashMap<>();
	private final Map<Class<? extends Ability>, AbilityInfo> abilityInfoMap = new HashMap<>();

	private final Set<Ability> playerAbilitySet = new HashSet<>();
	private final Map<UUID, Map<Class<? extends Ability>, LinkedList<Ability>>> playerAbilityMap = new HashMap<>();

	private final Set<String> addonPlugins = new HashSet<>();

	public AbilityManager() {
		super("Ability");

		this.comboAbilityManager = ModuleManager.getModule(ComboAbilityManager.class);
		this.multiAbilityManager = ModuleManager.getModule(MultiAbilityManager.class);
		this.passiveAbilityManager = ModuleManager.getModule(PassiveAbilityManager.class);

		runTimer(() -> {
			for (Ability ability : playerAbilitySet) {
				if (ability.getInfo() instanceof PassiveAbilityInfo) {
					if (!((PassiveAbilityInfo) ability).isProgressable()) {
						return;
					}

					// This has to be before isDead as isDead will return true if they are offline.
					if (!ability.getPlayer().isOnline()) {
						ability.remove();
						return;
					}

					if (ability.getPlayer().isDead()) {
						return;
					}
				} else if (ability.getPlayer().isDead()) {
					ability.remove();
					continue;
				} else if (!ability.getPlayer().isOnline()) {
					ability.remove();
					continue;
				}

				try {
					ability.tryModifyAttributes();

					try (MCTiming timing = ProjectKorra.timing(ability.getName()).startTiming()) {
						ability.progress();
					}

					getPlugin().getServer().getPluginManager().callEvent(new AbilityProgressEvent(ability));
				} catch (Exception e) {
					e.printStackTrace();
					getPlugin().getLogger().severe(ability.toString());

					try {
						ability.getPlayer().sendMessage(ChatColor.YELLOW + "[" + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()) + "] " + ChatColor.RED + "There was an error running " + ability.getName() + ". please notify the server owner describing exactly what you were doing at this moment");
					} catch (final Exception me) {
						Bukkit.getLogger().severe("unable to notify ability user of error");
					}
					try {
						ability.remove();
					} catch (final Exception re) {
						Bukkit.getLogger().severe("unable to fully remove ability of above error");
					}
				}
			}
			// TODO progress abilities
		}, 1L, 1L);

		registerAbilities();
	}

	/**
	 * Scans and loads plugin CoreAbilities, and Addon CoreAbilities that are
	 * located in a Jar file inside of the /ProjectKorra/Abilities/ folder.
	 */
	public void registerAbilities() {
		this.playerAbilitySet.clear();
		this.playerAbilityMap.clear();

		registerPluginAbilities("com.projectkorra");
		registerAddonAbilities("Abilities");

//		registerAbility(FireBlast.class);
	}

	/**
	 * Scans a JavaPlugin and registers Ability class files.
	 *
	 * @param plugin a JavaPlugin containing Ability class files
	 * @param packageBase a prefix of the package name, used to increase
	 *            performance
	 * @see #getAllAbilityInfo()
	 * @see #getAbility(String)
	 */
	public void registerPluginAbilities(String packageBase) {
		AbilityRegistry<Ability> abilityRegistry = new AbilityRegistry<>(getPlugin(), packageBase);
		List<Class<Ability>> loadedAbilities = abilityRegistry.load(Ability.class, Ability.class);

		String entry = getPlugin().getName() + "::" + packageBase;
		this.addonPlugins.add(entry);

		for (Class<Ability> abilityClass : loadedAbilities) {
			AbilityInfo abilityInfo = getAbilityInfo(abilityClass);

			registerAbility(abilityClass, abilityInfo);
		}
	}

	/**
	 * Scans all of the Jar files inside of /ProjectKorra/folder and registers
	 * all of the Ability class files that were found.
	 *
	 * @param folder the name of the folder to scan
	 * @see #getAllAbilityInfo()
	 * @see #getAbility(String)
	 */
	public void registerAddonAbilities(String folder) {
		File file = new File(getPlugin().getDataFolder(), folder);

		if (!file.exists()) {
			file.mkdir();
			return;
		}

		AddonAbilityRegistry<Ability> abilityRegistery = new AddonAbilityRegistry<>(getPlugin(), file);
		List<Class<Ability>> loadedAbilities = abilityRegistery.load(Ability.class, Ability.class);

		for (Class<Ability> abilityClass : loadedAbilities) {
			AbilityInfo abilityInfo = getAbilityInfo(abilityClass);

			if (!(abilityInfo instanceof AddonAbilityInfo)) {
				throw new AbilityException(abilityClass.getName() + " must have an AddonAbilityInfo");
			}

			registerAbility(abilityClass, abilityInfo);
		}
	}

	private <T extends Ability> void registerAbility(Class<T> abilityClass, AbilityInfo abilityInfo) throws AbilityException {
		AbilityConfig abilityConfig = getAbilityConfig(abilityClass);

		String abilityName = abilityInfo.getName();

		if (abilityName == null) {
			throw new AbilityException("Ability " + abilityClass.getName() + " has no name");
		}

		if (!abilityConfig.Enabled) {
			getPlugin().getLogger().info(abilityName + " is disabled");
			return;
		}

		if (abilityInfo instanceof AddonAbilityInfo) {
			((AddonAbilityInfo) abilityInfo).load();
		}

		if (abilityInfo instanceof ComboAbilityInfo) {
			ComboAbilityInfo comboAbilityInfo = (ComboAbilityInfo) abilityInfo;

			if (comboAbilityInfo.getCombination() == null || comboAbilityInfo.getCombination().size() < 2) {
				getPlugin().getLogger().info(abilityName + " has no combination");
				return;
			}

			this.comboAbilityManager.registerAbility(abilityClass, comboAbilityInfo);
			return;
		}

		if (abilityInfo instanceof MultiAbilityInfo) {
			MultiAbilityInfo multiAbilityInfo = (MultiAbilityInfo) abilityInfo;

			this.multiAbilityManager.registerAbility(abilityClass, multiAbilityInfo);
			return;
		}

		if (abilityInfo instanceof PassiveAbilityInfo) {
			PassiveAbilityInfo passiveAbilityInfo = (PassiveAbilityInfo) abilityInfo;

			// TODO Set Hidden Ability
			this.passiveAbilityManager.registerAbility(abilityClass, passiveAbilityInfo);
			return;
		}

		this.abilities.put(abilityInfo.getName(), abilityInfo);
	}

	private AbilityConfig getAbilityConfig(Class<? extends Ability> abilityClass) throws AbilityException {
		try {
			return ConfigManager.getConfig(((Class<? extends AbilityConfig>) ((ParameterizedType) abilityClass.getGenericSuperclass()).getActualTypeArguments()[1]));
		} catch (Exception e) {
			throw new AbilityException(e);
		}
	}

	public <T extends Ability> T createAbility(Player player, Class<T> abilityClass) throws AbilityException {
		try {
			Constructor<T> constructor = abilityClass.getDeclaredConstructor(Player.class);

			return constructor.newInstance(player);
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new AbilityException(e);
		}
	}

	public void startAbility(Ability ability) {
		if (ability.isStarted()) {
			return;
		}

		this.playerAbilitySet.add(ability);
		this.playerAbilityMap.computeIfAbsent(ability.getPlayer().getUniqueId(), k -> new HashMap<>())
				.computeIfAbsent(ability.getClass(), k -> new LinkedList<>())
				.add(ability);
	}

	protected void removeAbility(Ability ability) {
		if (ability.isRemoved()) {
			return;
		}

		this.playerAbilitySet.remove(ability);
		this.playerAbilityMap.values().removeIf(abilityMap ->
		{
			abilityMap.values().removeIf(abilityList ->
			{
				abilityList.remove(ability);

				return abilityList.isEmpty();
			});

			return abilityMap.isEmpty();
		});
	}

	/**
	 * Removes every {@link Ability} instance that has been started but not yet
	 * removed.
	 */
	public void removeAll() {
		new HashSet<>(this.playerAbilitySet).forEach(Ability::remove);
	}

	public <T extends Ability> boolean hasAbility(Player player, Class<T> ability) {
		Map<Class<? extends Ability>, LinkedList<Ability>> abilities = this.playerAbilityMap.get(player.getUniqueId());

		if (abilities == null || !abilities.containsKey(ability)) {
			return false;
		}

		return !abilities.get(abilities).isEmpty();
	}

	public <T extends Ability> T getAbility(Player player, Class<T> ability) {
		Map<Class<? extends Ability>, LinkedList<Ability>> abilities = this.playerAbilityMap.get(player.getUniqueId());

		if (abilities == null || !abilities.containsKey(ability)) {
			return null;
		}

		return ability.cast(abilities.get(ability).getFirst());
	}

	public <T extends Ability> Collection<T> getAbilities(Player player, Class<T> ability) {
		Map<Class<? extends Ability>, LinkedList<Ability>> abilities = this.playerAbilityMap.get(player.getUniqueId());

		if (abilities == null || !abilities.containsKey(ability)) {
			return null;
		}

		return abilities.get(abilities).stream().map(ability::cast).collect(Collectors.toList());
	}

	public AbilityInfo getAbilityInfo(String abilityName) {
		return this.abilities.get(abilityName);
	}

	public AbilityInfo getAbilityInfo(Class<? extends Ability> abilityClass) {
		return this.abilityInfoMap.computeIfAbsent(abilityClass, k ->
		{
			try {
				return ((Class<? extends AbilityInfo>) ((ParameterizedType) abilityClass.getGenericSuperclass()).getActualTypeArguments()[0]).newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
				return null;
			}
		});
	}

	public List<AbilityInfo> getAbilityInfo() {
		return new ArrayList<>(this.abilities.values());
	}

	public <T extends Ability> LinkedList<T> getAbilities(Class<T> abilityClass) {
		LinkedList<T> abilities = new LinkedList<>();

		this.playerAbilityMap.values().forEach(a -> {
			a.values().forEach(ability -> abilities.add(abilityClass.cast(ability)));
		});

		return abilities;
	}

	public List<Ability> getAbilities() {
		return new ArrayList<>(this.playerAbilitySet);
	}

	public List<AbilityInfo> getAbilities(Element element) {
		return this.abilities.values().stream()
				.filter(ability ->
				{
					if (ability.getElement().equals(element)) {
						return true;
					}

					if (ability.getElement() instanceof SubElement) {
						return ((SubElement) ability.getElement()).getParent().equals(element);
					}

					return false;
				})
				.collect(Collectors.toList());
	}

	/**
	 * {@link AbilityManager} keeps track of plugins that have registered abilities to use
	 * for bending reload purposes <br>
	 * <b>This isn't a simple list, external use isn't recommended</b>
	 *
	 * @return a list of entrys with the plugin name and path abilities can be
	 *         found at
	 */
	public Set<String> getAddonPlugins() {
		return this.addonPlugins;
	}
}

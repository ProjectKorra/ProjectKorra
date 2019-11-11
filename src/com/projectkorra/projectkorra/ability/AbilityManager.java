package com.projectkorra.projectkorra.ability;

import co.aikar.timings.lib.MCTiming;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.api.AddonAbility;
import com.projectkorra.projectkorra.ability.api.ComboAbility;
import com.projectkorra.projectkorra.ability.api.MultiAbility;
import com.projectkorra.projectkorra.ability.api.PassiveAbility;
import com.projectkorra.projectkorra.airbending.passive.AirSaturation;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;
import com.projectkorra.projectkorra.configuration.configs.abilities.air.AirSaturationConfig;
import com.projectkorra.projectkorra.element.Element;
import com.projectkorra.projectkorra.element.SubElement;
import com.projectkorra.projectkorra.event.AbilityProgressEvent;
import com.projectkorra.projectkorra.module.Module;
import com.projectkorra.projectkorra.module.ModuleManager;
import com.projectkorra.projectkorra.util.MultiKeyMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.ParameterizedType;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class AbilityManager extends Module {

	private final ComboAbilityManager comboAbilityManager;
	private final MultiAbilityManager multiAbilityManager;
	private final PassiveAbilityManager passiveAbilityManager;

	private final MultiKeyMap<String, AbilityHandler> handlerMap = new MultiKeyMap<>();

	private final Set<Ability> abilities = new HashSet<>();
	private final Map<UUID, Map<Class<? extends Ability>, LinkedList<Ability>>> abilityMap = new HashMap<>();

	private final MCTiming timing = ProjectKorra.timing("AbilityManager");
	private final Set<String> addonPlugins = new HashSet<>();

	public AbilityManager() {
		super("Ability");

		this.comboAbilityManager = ModuleManager.getModule(ComboAbilityManager.class);
		this.multiAbilityManager = ModuleManager.getModule(MultiAbilityManager.class);
		this.passiveAbilityManager = ModuleManager.getModule(PassiveAbilityManager.class);

		runTimer(() -> {
			try (MCTiming timing = this.timing.startTiming()) {
				for (Ability ability : abilities) {
					if (ability.getHandler() instanceof PassiveAbility) {
						if (!((PassiveAbility) ability.getHandler()).isProgressable()) {
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

						try (MCTiming abilityTiming = ProjectKorra.timing(ability.getName()).startTiming()) {
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
			}
		}, 1L, 1L);

		registerAbilities();
	}

	/**
	 * Scans and loads plugin CoreAbilities, and Addon CoreAbilities that are
	 * located in a Jar file inside of the /ProjectKorra/Abilities/ folder.
	 */
	public void registerAbilities() {
		this.abilities.clear();
		this.abilityMap.clear();

//		registerPluginAbilities(getPlugin(), "com.projectkorra");
//		registerAddonAbilities("Abilities");

		registerAbility(new AirSaturation.AirSaturationHandler(AirSaturation.class, AirSaturationConfig.class));
	}

//	/**
//	 * Scans a JavaPlugin and registers Ability class files.
//	 *
//	 * @param plugin a JavaPlugin containing Ability class files
//	 * @param packageBase a prefix of the package name, used to increase
//	 *            performance
//	 * @see #getAllAbilityInfo()
//	 * @see #getAbility(String)
//	 */
//	public void registerPluginAbilities(JavaPlugin plugin, String packageBase) {
//		AbilityRegistry<Ability> abilityRegistry = new AbilityRegistry<>(plugin, packageBase);
//		List<Class<Ability>> loadedAbilities = abilityRegistry.load(Ability.class, Ability.class);
//
//		String entry = getPlugin().getName() + "::" + packageBase;
//		this.addonPlugins.add(entry);
//
//		for (Class<Ability> abilityClass : loadedAbilities) {
//			AbilityInfo abilityInfo = getAbilityInfo(abilityClass);
//
//			registerAbility(abilityClass, abilityInfo);
//		}
//	}
//
//	/**
//	 * Scans all of the Jar files inside of /ProjectKorra/folder and registers
//	 * all of the Ability class files that were found.
//	 *
//	 * @param folder the name of the folder to scan
//	 * @see #getAllAbilityInfo()
//	 * @see #getAbility(String)
//	 */
//	public void registerAddonAbilities(String folder) {
//		File file = new File(getPlugin().getDataFolder(), folder);
//
//		if (!file.exists()) {
//			file.mkdir();
//			return;
//		}
//
//		AddonAbilityRegistry<Ability> abilityRegistery = new AddonAbilityRegistry<>(getPlugin(), file);
//		List<Class<Ability>> loadedAbilities = abilityRegistery.load(Ability.class, Ability.class);
//
//		for (Class<Ability> abilityClass : loadedAbilities) {
//			AbilityInfo abilityInfo = getAbilityInfo(abilityClass);
//
//			if (!(abilityInfo instanceof AddonAbilityInfo)) {
//				throw new AbilityException(abilityClass.getName() + " must have an AddonAbilityInfo");
//			}
//
//			registerAbility(abilityClass, abilityInfo);
//		}
//	}

	private <T extends AbilityHandler> void registerAbility(T abilityHandler) throws AbilityException {
		if (abilityHandler == null) {
			throw new AbilityException("abilityHandler is null");
		}

		String abilityName = abilityHandler.getName();

		if (abilityName == null) {
			throw new AbilityException("Ability " + abilityHandler.getClass().getName() + " has no name");
		}

		if (!abilityHandler.getConfig().Enabled) {
			getPlugin().getLogger().info(abilityName + " is disabled");
			return;
		}

		if (abilityHandler instanceof AddonAbility) {
			((AddonAbility) abilityHandler).load();
		}

		if (abilityHandler instanceof ComboAbility) {
			ComboAbility comboAbility = (ComboAbility) abilityHandler;

			if (comboAbility.getCombination() == null || comboAbility.getCombination().size() < 2) {
				getPlugin().getLogger().info(abilityName + " has no combination");
				return;
			}

			this.comboAbilityManager.registerAbility(abilityHandler);
			return;
		}

		if (abilityHandler instanceof MultiAbility) {
			this.multiAbilityManager.registerAbility(abilityHandler);
			return;
		}

		if (abilityHandler instanceof PassiveAbility) {
			PassiveAbility passiveAbility = (PassiveAbility) abilityHandler;

			// TODO Set Hidden Ability
			this.passiveAbilityManager.registerAbility(abilityHandler);
			return;
		}

		this.handlerMap.put(abilityName, abilityHandler);
	}

	private AbilityConfig getAbilityConfig(Class<? extends Ability> abilityClass) throws AbilityException {
		try {
			return ConfigManager.getConfig(((Class<? extends AbilityConfig>) ((ParameterizedType) abilityClass.getGenericSuperclass()).getActualTypeArguments()[1]));
		} catch (Exception e) {
			throw new AbilityException(e);
		}
	}

	public void startAbility(Ability ability) {
		if (ability.isStarted()) {
			return;
		}

		this.abilities.add(ability);
		this.abilityMap.computeIfAbsent(ability.getPlayer().getUniqueId(), k -> new HashMap<>())
				.computeIfAbsent(ability.getClass(), k -> new LinkedList<>())
				.add(ability);
	}

	protected void removeAbility(Ability ability) {
		if (ability.isRemoved()) {
			return;
		}

		this.abilities.remove(ability);
		this.abilityMap.values().removeIf(abilityMap ->
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
		new HashSet<>(this.abilities).forEach(Ability::remove);
	}

	public <T extends Ability> boolean hasAbility(Player player, Class<T> ability) {
		Map<Class<? extends Ability>, LinkedList<Ability>> abilities = this.abilityMap.get(player.getUniqueId());

		if (abilities == null || !abilities.containsKey(ability)) {
			return false;
		}

		return !abilities.get(abilities).isEmpty();
	}

	public <T extends Ability> T getAbility(Player player, Class<T> ability) {
		Map<Class<? extends Ability>, LinkedList<Ability>> abilities = this.abilityMap.get(player.getUniqueId());

		if (abilities == null || !abilities.containsKey(ability)) {
			return null;
		}

		return ability.cast(abilities.get(ability).getFirst());
	}

	public <T extends Ability> Collection<T> getAbilities(Player player, Class<T> ability) {
		Map<Class<? extends Ability>, LinkedList<Ability>> abilities = this.abilityMap.get(player.getUniqueId());

		if (abilities == null || !abilities.containsKey(ability)) {
			return null;
		}

		return abilities.get(abilities).stream().map(ability::cast).collect(Collectors.toList());
	}

	public AbilityHandler getHandler(String abilityName) {
		return this.handlerMap.get(abilityName);
	}

	public AbilityHandler getHandler(Class<? extends AbilityHandler> handlerClass) {
		return this.handlerMap.get(handlerClass);
	}

	public List<AbilityHandler> getHandlers() {
		return new ArrayList<>(this.handlerMap.values());
	}

	public <T extends Ability> LinkedList<T> getAbilities(Class<T> abilityClass) {
		LinkedList<T> abilities = new LinkedList<>();

		this.abilityMap.values().forEach(a -> {
			a.values().forEach(ability -> abilities.add(abilityClass.cast(ability)));
		});

		return abilities;
	}

	public List<Ability> getAbilities() {
		return new ArrayList<>(this.abilities);
	}

	public List<AbilityHandler> getHandlers(Element element) {
		return this.handlerMap.values().stream()
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
	public List<String> getAddonPlugins() {
		return new ArrayList<>(this.addonPlugins);
	}
}

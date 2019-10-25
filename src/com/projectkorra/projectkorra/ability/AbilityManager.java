package com.projectkorra.projectkorra.ability;

import co.aikar.timings.lib.MCTiming;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.api.PassiveAbility;
import com.projectkorra.projectkorra.ability.loader.*;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;
import com.projectkorra.projectkorra.element.Element;
import com.projectkorra.projectkorra.element.SubElement;
import com.projectkorra.projectkorra.event.AbilityProgressEvent;
import com.projectkorra.projectkorra.firebending.FireBlast;
import com.projectkorra.projectkorra.module.Module;
import com.projectkorra.projectkorra.module.ModuleManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.ParameterizedType;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class AbilityManager extends Module {

	private final ComboManager comboManager;

	private final Set<Ability> abilitySet = new HashSet<>();
	private final Map<UUID, Map<Class<? extends Ability>, LinkedList<Ability>>> abilityMap = new HashMap<>();

	public AbilityManager() {
		super("Ability");

		this.comboManager = ModuleManager.getModule(ComboManager.class);

		runTimer(() -> {
			for (Ability ability : abilitySet) {
				if (ability instanceof PassiveAbility) {
					if (!((PassiveAbility) ability).isProgressable()) {
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
		this.abilitySet.clear();
		this.abilityMap.clear();

		Ability.registerPluginAbilities(getPlugin(), "com.projectkorra");
		Ability.registerAddonAbilities("/Abilities/");

		registerAbility(FireBlast.class);
	}

	private <T extends Ability> void registerAbility(Class<T> abilityClass) throws IllegalAccessException, InstantiationException {
		AbilityData abilityData = abilityClass.getDeclaredAnnotation(AbilityData.class);

		if (abilityData == null) {
			getPlugin().getLogger().warning("Ability " + abilityClass.getName() + " has no AbilityData annotation");
			return;
		}

		String abilityName = abilityData.name();

		if (abilityName == null) {
			getPlugin().getLogger().warning("Ability " + abilityClass.getName() + " has no name?");
			return;
		}

		AbilityLoader abilityLoader = abilityData.abilityLoader().newInstance();
		AbilityConfig abilityConfig = ConfigManager.getConfig(((Class<? extends AbilityConfig>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]));

		if (!abilityConfig.Enabled) {
			getPlugin().getLogger().info(abilityName + " is disabled");
			return;
		}

		if (abilityLoader instanceof AddonAbilityLoader) {
			((AddonAbilityLoader) abilityLoader).load();
		}

		if (abilityLoader instanceof ComboAbilityLoader) {
			ComboAbilityLoader comboAbilityLoader = (ComboAbilityLoader) abilityLoader;

			if (comboAbilityLoader.getCombination() == null || comboAbilityLoader.getCombination().size() < 2) {
				getPlugin().getLogger().info(abilityName + " has no combination");
				return;
			}

			this.comboManager.registerAbility(abilityClass, abilityData, comboAbilityLoader);

//			ComboManager.getComboAbilities().put(abilityName, new ComboManager.ComboAbilityInfo(abilityName, comboAbilityLoader.getCombination(), ));
//			ComboManager.getDescriptions().put(abilityName, abilityConfig.Description);
//			ComboManager.getInstructions().put(abilityName, abilityConfig.Instructions);
		}

		if (abilityLoader instanceof MultiAbilityLoader) {
			MultiAbilityLoader multiAbilityLoader = (MultiAbilityLoader) abilityLoader;

			MultiAbilityManager.multiAbilityList.add(new MultiAbilityManager.MultiAbilityInfo(abilityName, multiAbilityLoader.getMultiAbilities()));
		}

		if (abilityLoader instanceof PassiveAbilityLoader) {
			PassiveAbilityLoader passiveAbilityLoader = (PassiveAbilityLoader) abilityLoader;

			// TODO Set Hidden Ability
			// TODO Register Passive Ability
//			PassiveManager.getPassives().put(abilityName, ability???)
		}
	}

	public void startAbility(Ability ability) {
		if (ability.isStarted()) {
			return;
		}

		this.abilitySet.add(ability);
		this.abilityMap.computeIfAbsent(ability.getPlayer().getUniqueId(), k -> new HashMap<>())
				.computeIfAbsent(ability.getClass(), k -> new LinkedList<>())
				.add(ability);
	}

	protected void removeAbility(Ability ability) {
		if (ability.isRemoved()) {
			return;
		}

		this.abilitySet.remove(ability);
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
		new HashSet<>(this.abilitySet).forEach(Ability::remove);
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

	public <T extends Ability> LinkedList<T> getAbilities(Class<T> abilityClass) {
		LinkedList<T> abilities = new LinkedList<>();

		this.abilityMap.values().forEach(a -> {
			a.values().forEach(ability -> abilities.add(abilityClass.cast(ability)));
		});

		return abilities;
	}

	public List<Ability> getAbilities(Element element) {
		this.abilitySet.stream()
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
}

package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.ability.abilities.air.airswipe.AirSwipe;
import com.projectkorra.projectkorra.ability.abilities.air.airswipe.AirSwipeConfig;
import com.projectkorra.projectkorra.ability.abilities.air.airswipe.AirSwipeHandler;
import com.projectkorra.projectkorra.ability.api.AddonAbility;
import com.projectkorra.projectkorra.ability.api.ComboAbility;
import com.projectkorra.projectkorra.ability.api.MultiAbility;
import com.projectkorra.projectkorra.ability.api.PassiveAbility;
import com.projectkorra.projectkorra.element.Element;
import com.projectkorra.projectkorra.element.SubElement;
import com.projectkorra.projectkorra.module.Module;
import com.projectkorra.projectkorra.util.MultiKeyMap;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AbilityHandlerManager extends Module {

	private final ComboAbilityManager comboAbilityManager;
	private final MultiAbilityManager multiAbilityManager;
	private final PassiveAbilityManager passiveAbilityManager;

	private final MultiKeyMap<String, AbilityHandler> handlerMap = new MultiKeyMap<>();

	private AbilityHandlerManager() {
		super("Ability Handler");

		this.comboAbilityManager = module(ComboAbilityManager.class);
		this.multiAbilityManager = module(MultiAbilityManager.class);
		this.passiveAbilityManager = module(PassiveAbilityManager.class);

		registerAbilities();
	}

	/**
	 * Scans and loads plugin CoreAbilities, and Addon CoreAbilities that are
	 * located in a Jar file inside of the /ProjectKorra/Abilities/ folder.
	 */
	public void registerAbilities() {
		this.handlerMap.clear();

		//		registerPluginAbilities(getPlugin(), "com.projectkorra");
		//		registerAddonAbilities("Abilities");

		registerAbility(new AirSwipeHandler(AirSwipe.class, AirSwipeConfig.class));
	}

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

			abilityHandler.setHidden(true);
			this.passiveAbilityManager.registerAbility(abilityHandler);
			return;
		}

		this.handlerMap.put(abilityName, abilityHandler);
	}

	public AbilityHandler getHandler(String abilityName) {
		return this.handlerMap.get(abilityName);
	}

	public AbilityHandler getHandler(Class<? extends AbilityHandler> handlerClass) {
		return this.handlerMap.get(handlerClass);
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

	public List<AbilityHandler> getHandlers() {
		return new ArrayList<>(this.handlerMap.values());
	}
}

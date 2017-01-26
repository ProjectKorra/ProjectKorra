package com.projectkorra.projectkorra.ability.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;

public class PassiveManager {

	private static final Map<String, CoreAbility> PASSIVES = new HashMap<>();
	private static final Map<Element, Set<String>> PASSIVES_BY_ELEMENT = new HashMap<>(); // Parent elements INCLUDE subelement passives.

	public static void registerPassives(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return;
		}
		for (CoreAbility ability : CoreAbility.getAbilities()) {
			if (ability instanceof PassiveAbility) {
				if (!hasPassive(player, ability)) {
					continue;
				} else if (CoreAbility.hasAbility(player, ability.getClass())) {
					continue;
					/*
					 * Passive's such as not taking fall damage are managed in
					 * PKListener, so we do not want to create instances of them
					 * here. This just enables the passive to be displayed in /b
					 * d [element]passive
					 */
				} else if (!((PassiveAbility) ability).isInstantiable()) {
					continue;
				}
				Class<?> clazz = null;
				try {
					clazz = Class.forName(ability.getClass().getName());
				}
				catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				Constructor<?> constructor = null;
				try {
					constructor = clazz.getConstructor(Player.class);
				}
				catch (NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
				}
				Object object = null;
				try {
					object = constructor.newInstance(new Object[] { player });
				}
				catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
				((CoreAbility) object).start();
			}
		}
	}

	public static boolean hasPassive(Player player, CoreAbility passive) {
		if (player == null) {
			return false;
		} else if (passive == null) {
			return false;
		}
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		Element element = passive.getElement();
		if (passive.getElement() instanceof SubElement) {
			element = ((SubElement) passive.getElement()).getParentElement();
		}
		if (bPlayer == null) {
			return false;
		} else if (!(passive instanceof PassiveAbility)) {
			return false;
		} else if (!passive.isEnabled()) {
			return false;
		} else if (!bPlayer.canBendPassive(passive.getElement())) {
			return false;
		} else if (!bPlayer.isToggled()) {
			return false;
		} else if (!bPlayer.isElementToggled(element)) {
			return false;
		}
		return true;
	}

	public static Set<String> getPassivesForElement(Element element) {
		if (PASSIVES_BY_ELEMENT.get(element) == null) {
			return new HashSet<>();
		}
		return PASSIVES_BY_ELEMENT.get(element);
	}

	public static Map<String, CoreAbility> getPassives() {
		return PASSIVES;
	}

	public static Map<Element, Set<String>> getPassivesByElement() {
		return PASSIVES_BY_ELEMENT;
	}

}

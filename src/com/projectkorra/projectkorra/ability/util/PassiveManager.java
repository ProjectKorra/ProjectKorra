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
	private static final Map<PassiveAbility, Class<? extends CoreAbility>> PASSIVE_CLASSES = new HashMap<>();

	public static void registerPassives(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return;
		}
		for (CoreAbility ability : PASSIVES.values()) {
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
				}
				
				if (!((PassiveAbility) ability).isInstantiable()) {
					continue;
				}
				
				try {
					Class<? extends CoreAbility> clazz = PASSIVE_CLASSES.get((PassiveAbility) ability);
					Constructor<?> constructor = clazz.getConstructor(Player.class);
					Object object = constructor.newInstance(player);
					((CoreAbility) object).start();
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
				}
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
		} else if (!bPlayer.canBendPassive(passive)) {
			return false;
		} else if (!bPlayer.isToggled()) {
			return false;
		} else if (!bPlayer.isElementToggled(element)) {
			return false;
		}
		return true;
	}

	public static Set<String> getPassivesForElement(Element element) {
		Set<String> passives = new HashSet<>();
		for (CoreAbility passive : PASSIVES.values()) {
			if (passive.getElement() == element) {
				passives.add(passive.getName());
			} else if (passive.getElement() instanceof SubElement) {
				Element check = ((SubElement)passive.getElement()).getParentElement();
				if (check == element) {
					passives.add(passive.getName());
				}
			}
		}
		return passives;
	}

	public static Map<String, CoreAbility> getPassives() {
		return PASSIVES;
	}

	public static Map<PassiveAbility, Class<? extends CoreAbility>> getPassiveClasses() {
		return PASSIVE_CLASSES;
	}

}

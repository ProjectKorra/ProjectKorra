package com.projectkorra.projectkorra.ability.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;

public class PassiveManager {
	
	private static final Map<String, CoreAbility> PASSIVES = new HashMap<>();
	private static final Map<Element, Set<CoreAbility>> PASSIVES_BY_ELEMENT = new HashMap<>();

	public static void registerPassives(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return;
		}
		for (CoreAbility ability : CoreAbility.getAbilities()) {
			if (ability instanceof PassiveAbility) {
				if (!ability.isEnabled()) {
					continue;
				} else if (!bPlayer.canBendPassive(ability.getElement())) {
					continue;
				} else if (CoreAbility.hasAbility(player, ability.getClass())) {
					continue;
				}
				Class<?> clazz = null;
				try {
					clazz = Class.forName(ability.getClass().getName());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				Constructor<?> constructor = null;
				try {
					constructor = clazz.getConstructor(Player.class);
				} catch (NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
				}
				Object object = null;
				try {
					object = constructor.newInstance(new Object[] { player });
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					e.printStackTrace();
				}
				((CoreAbility) object).start();
			}
		}
	}
	
	public static List<String> getPassivesForElement(Element element) {
		List<String> passives = new ArrayList<String>();
		if (PASSIVES_BY_ELEMENT.get(element) == null) {
			return passives;
		}
		for (CoreAbility ability : PASSIVES_BY_ELEMENT.get(element)) {
			passives.add(ability.getName());
		}
		return passives;
	}
	
	public static Map<String, CoreAbility> getPassives() {
		return PASSIVES;
	}
	
	public static Map<Element, Set<CoreAbility>> getPassivesByElement() {
		return PASSIVES_BY_ELEMENT;
	}

}

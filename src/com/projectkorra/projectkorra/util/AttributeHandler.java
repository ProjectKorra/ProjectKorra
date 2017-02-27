package com.projectkorra.projectkorra.util;

import java.lang.reflect.Field;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;

public class AttributeHandler {

	public static boolean setField(CoreAbility ability, String type, Object value) {
		for (Field field : ability.getClass().getDeclaredFields()) {
			Attribute attribute = field.getAnnotation(Attribute.class);
			if (attribute != null && attribute.type().equals(type)) {
				field.setAccessible(true);
				try {
					field.set(ability, value);
					return true;
				}
				catch (IllegalArgumentException e) {
					ProjectKorra.log.warning("Failed to set field '" + field.getName() + "' (" + field.getType().getSimpleName() + ") in " + ability.getClass().getSimpleName() + " to '" + value.toString() + "' (" + value.getClass().getSimpleName() + ")");
				}
				catch (IllegalAccessException e) {
					ProjectKorra.log.warning("Failed to set field '" + field.getName() + "' (" + field.getType().getSimpleName() + ") in " + ability.getClass().getSimpleName() + " to '" + value.toString() + "' (" + value.getClass().getSimpleName() + ")");
				}
			}
		}
		return false;
	}

	public static Object getField(CoreAbility ability, String type) {
		for (Field field : ability.getClass().getFields()) {
			Attribute attribute = field.getAnnotation(Attribute.class);
			if (attribute != null && attribute.type().equals(type)) {
				try {
					return field.get(ability);
				}
				catch (IllegalArgumentException e) {
					e.printStackTrace();
				}
				catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

}

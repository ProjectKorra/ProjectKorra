package com.projectkorra.projectkorra.util;

import java.lang.reflect.Field;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;

public class Attribute {

	public static boolean setField(CoreAbility ability, String field, Object value) {
		try {
			Field _field = ability.getClass().getDeclaredField(field);
			boolean oldVisibility = _field.isAccessible();
			_field.setAccessible(true);
			try {
				_field.set(ability, value);
			}
			catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
				ProjectKorra.log.warning(e.getClass().getName() + ": Failed to set field '" + _field.getName() + "' (" + _field.getType().getSimpleName() + ") in " + ability.getClass().getSimpleName() + " to '" + value.toString() + "' (" + value.getClass().getSimpleName() + ") at");

				return false;
			}
			_field.setAccessible(oldVisibility);
		}
		catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
			ProjectKorra.log.warning(e.getClass().getName() + ": Failed to set field '" + field + "' in " + ability.getClass().getSimpleName() + " to '" + value.toString() + "' (" + value.getClass().getSimpleName() + ")");
			return false;
		}
		return true;
	}

	public static Object getField(CoreAbility ability, String field) {
		try {
			Field _field = ability.getClass().getDeclaredField(field);
			boolean oldVisibility = _field.isAccessible();
			_field.setAccessible(true);
			try {
				Object object = _field.get(ability);
				_field.setAccessible(oldVisibility);
				return object;
			}
			catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
				ProjectKorra.log.warning(e.getClass().getName() + ": Failed to get field '" + _field.getName() + "' in " + ability.getClass().getSimpleName());
				return null;
			}
		}
		catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
			ProjectKorra.log.warning(e.getClass().getName() + ": Failed to get field '" + field + "' in " + ability.getClass().getSimpleName());
			return null;
		}
	}

}

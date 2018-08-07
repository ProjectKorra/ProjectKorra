package com.projectkorra.projectkorra.attribute;

import java.lang.reflect.Field;

import org.bukkit.Bukkit;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.attribute.AttributeModifier.AttributeModifierType;
import com.projectkorra.projectkorra.event.AttributeModifyEvent;

public class Attributes {
	/**
	 * Modifies all abilities of the provided element for the given attribute
	 * and modifier
	 *
	 * @param element The type of abilities being changed. Can be a subelement.
	 * @param attribute What attribute to modify
	 * @param modifier The modifier
	 */
	public static void modify(final Element element, final String attribute, final AttributeModifier modifier) {
		for (final CoreAbility ability : CoreAbility.getAbilitiesByElement(element)) {
			modify(ability, attribute, modifier);
		}
	};

	/**
	 * Modifies all the attribute of the provided ability from the given
	 * attribute and modifier
	 *
	 * @param ability The ability to change
	 * @param attribute What attribute to modify
	 * @param modifier The modifier
	 */
	public static void modify(final CoreAbility ability, final String attribute, final AttributeModifier modifier) {
		if (ability.getPlayer() == null) {
			for (final CoreAbility ability2 : CoreAbility.getAbilities(ability.getClass())) {
				modify(ability2, attribute, modifier);
			}
			return;
		}

		for (final Field field : ability.getClass().getDeclaredFields()) {
			if (field.isAnnotationPresent(Attribute.class)) {
				final Attribute annotation = field.getAnnotation(Attribute.class);
				String attrToTest = ability.getName() + annotation.value();
				if (!annotation.attribute().equals("")) {
					attrToTest = annotation.attribute();
				}

				if (attrToTest.equalsIgnoreCase(attribute)) {
					final boolean flag = field.isAccessible();

					if (!flag) {
						field.setAccessible(true);
					}

					try {
						if (field.getDeclaringClass().equals(Double.TYPE.getClass())) {
							final double oldValue = field.getDouble(ability);
							double newValue = modifier.getType() == AttributeModifierType.MULTIPLY ? oldValue * modifier.getModifier(ability) : oldValue + modifier.getModifier(ability);

							final AttributeModifyEvent event = new AttributeModifyEvent(ability, attribute, oldValue, newValue, modifier);
							Bukkit.getPluginManager().callEvent(event);
							newValue = event.getNewValue();

							field.setDouble(ability, newValue);
						} else if (field.getDeclaringClass().equals(Long.TYPE.getClass())) {
							final long oldValue = field.getLong(ability);
							long newValue = (long) (modifier.getType() == AttributeModifierType.MULTIPLY ? oldValue * modifier.getModifier(ability) : oldValue + modifier.getModifier(ability));

							final AttributeModifyEvent event = new AttributeModifyEvent(ability, attribute, oldValue, newValue, modifier);
							Bukkit.getPluginManager().callEvent(event);
							newValue = (long) event.getNewValue();

							field.setLong(ability, newValue);
						} else if (field.getDeclaringClass().equals(Integer.TYPE.getClass())) {
							final int oldValue = field.getInt(ability);
							int newValue = (int) (modifier.getType() == AttributeModifierType.MULTIPLY ? oldValue * modifier.getModifier(ability) : oldValue + modifier.getModifier(ability));

							final AttributeModifyEvent event = new AttributeModifyEvent(ability, attribute, oldValue, newValue, modifier);
							Bukkit.getPluginManager().callEvent(event);
							newValue = (int) event.getNewValue();

							field.setInt(ability, newValue);
						}

					}
					catch (final IllegalArgumentException e) {
						e.printStackTrace();
					}
					catch (final IllegalAccessException e) {
						e.printStackTrace();
					}

					if (!flag) {
						field.setAccessible(false);
					}
				}
			}
		}
	}

}

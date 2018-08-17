package com.projectkorra.projectkorra.attribute;

import org.apache.commons.lang.Validate;

public enum AttributeModifier {

	ADDITION((oldValue, modifier) -> {
		if (oldValue instanceof Double || modifier instanceof Double) {
			return oldValue.doubleValue() + modifier.doubleValue();
		} else if (oldValue instanceof Float || modifier instanceof Float) {
			return oldValue.floatValue() + modifier.floatValue();
		} else if (oldValue instanceof Long || modifier instanceof Long) {
			return oldValue.longValue() + modifier.longValue();
		} else if (oldValue instanceof Integer || modifier instanceof Integer) {
			return oldValue.intValue() + modifier.intValue();
		}
		return 0;
	}),
	SUBTRACTION((oldValue, modifier) -> {
		if (oldValue instanceof Double || modifier instanceof Double) {
			return oldValue.doubleValue() - modifier.doubleValue();
		} else if (oldValue instanceof Float || modifier instanceof Float) {
			return oldValue.floatValue() - modifier.floatValue();
		} else if (oldValue instanceof Long || modifier instanceof Long) {
			return oldValue.longValue() - modifier.longValue();
		} else if (oldValue instanceof Integer || modifier instanceof Integer) {
			return oldValue.intValue() - modifier.intValue();
		}
		return 0;
	}),
	MULTIPLICATION((oldValue, modifier) -> {
		if (oldValue instanceof Double || modifier instanceof Double) {
			return oldValue.doubleValue() * modifier.doubleValue();
		} else if (oldValue instanceof Float || modifier instanceof Float) {
			return oldValue.floatValue() * modifier.floatValue();
		} else if (oldValue instanceof Long || modifier instanceof Long) {
			return oldValue.longValue() * modifier.longValue();
		} else if (oldValue instanceof Integer || modifier instanceof Integer) {
			return oldValue.intValue() * modifier.intValue();
		}
		return 0;
	}),
	DIVISION((oldValue, modifier) -> {
		if (oldValue instanceof Double || modifier instanceof Double) {
			return oldValue.doubleValue() / modifier.doubleValue();
		} else if (oldValue instanceof Float || modifier instanceof Float) {
			return oldValue.floatValue() / modifier.floatValue();
		} else if (oldValue instanceof Long || modifier instanceof Long) {
			return oldValue.longValue() / modifier.longValue();
		} else if (oldValue instanceof Integer || modifier instanceof Integer) {
			return oldValue.intValue() / modifier.intValue();
		}
		return 0;
	});

	private AttributeModifierMethod modifier;

	private AttributeModifier(AttributeModifierMethod modifier) {
		this.modifier = modifier;
	}

	public AttributeModifierMethod getModifier() {
		return modifier;
	}

	public Number performModification(Number oldValue, Number modifier) {
		Validate.isTrue(!(this == DIVISION && modifier.doubleValue() == 0), "modifier cannot be 0");
		return this.modifier.performModification(oldValue, modifier);
	}

	/**
	 * Functional interface for modifying fields with the {@link Attribute}
	 * annotation
	 */
	@FunctionalInterface
	public interface AttributeModifierMethod {

		public Number performModification(Number oldValue, Number modifier);

	}

}

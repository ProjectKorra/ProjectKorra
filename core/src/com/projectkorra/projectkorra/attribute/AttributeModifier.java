package com.projectkorra.projectkorra.attribute;

public enum AttributeModifier {

	ADDITION((oldValue, modifier) -> {
		if (oldValue instanceof Double) {
			return oldValue.doubleValue() + modifier.doubleValue();
		} else if (oldValue instanceof Float) {
			return oldValue.floatValue() + modifier.floatValue();
		} else if (oldValue instanceof Long) {
			return oldValue.longValue() + modifier.longValue();
		} else if (oldValue instanceof Integer) {
			return oldValue.intValue() + modifier.intValue();
		}
		return 0;
	}), SUBTRACTION((oldValue, modifier) -> {
		if (oldValue instanceof Double) {
			return oldValue.doubleValue() - modifier.doubleValue();
		} else if (oldValue instanceof Float) {
			return oldValue.floatValue() - modifier.floatValue();
		} else if (oldValue instanceof Long) {
			return oldValue.longValue() - modifier.longValue();
		} else if (oldValue instanceof Integer) {
			return oldValue.intValue() - modifier.intValue();
		}
		return 0;
	}), MULTIPLICATION((oldValue, modifier) -> {
		if (oldValue instanceof Double) {
			return oldValue.doubleValue() * modifier.doubleValue();
		} else if (oldValue instanceof Float) {
			return oldValue.floatValue() * modifier.floatValue();
		} else if (oldValue instanceof Long) {
			return (long) (oldValue.longValue() * modifier.doubleValue());
		} else if (oldValue instanceof Integer) {
			return (int) (oldValue.intValue() * modifier.doubleValue());
		}
		return 0;
	}), DIVISION((oldValue, modifier) -> {
		if (oldValue instanceof Double) {
			return oldValue.doubleValue() / modifier.doubleValue();
		} else if (oldValue instanceof Float) {
			return oldValue.floatValue() / modifier.floatValue();
		} else if (oldValue instanceof Long) {
			return (long) (oldValue.longValue() / modifier.doubleValue());
		} else if (oldValue instanceof Integer) {
			return (int) (oldValue.intValue() / modifier.doubleValue());
		}
		return 0;
	}), SET((oldValue, modifier) -> modifier);

	private AttributeModifierMethod modifier;

	AttributeModifier(final AttributeModifierMethod modifier) {
		this.modifier = modifier;
	}

	public AttributeModifierMethod getModifier() {
		return this.modifier;
	}

	public Number performModification(final Number oldValue, final Number modifier) {
		if (this == DIVISION && modifier.doubleValue() == 0) {
			throw new IllegalArgumentException("Attribute modifier for DIVISION cannot be zero!");
		}
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

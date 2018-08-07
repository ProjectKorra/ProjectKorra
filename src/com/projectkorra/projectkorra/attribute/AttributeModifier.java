package com.projectkorra.projectkorra.attribute;

import org.bukkit.plugin.Plugin;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;

public class AttributeModifier {

	private double modifier = 1.0D;
	private final AttributeModifierType type;

	public enum AttributeModifierType {
		MULTIPLY, ADDITION
	};

	public AttributeModifier(final String name, final double modifier, final AttributeModifierType type, final Plugin plugin) {
		this.modifier = modifier;
		this.type = type;
	}

	public AttributeModifier(final String name, final AttributeModifierType type, final Plugin plugin) {
		this(name, 1.0D, type, plugin);
	}

	protected AttributeModifier(final String name, final AttributeModifierType type, final double modifier) {
		this(name, modifier, type, ProjectKorra.plugin);
	}

	/**
	 * Should return the modifier that should be applied to the Attribute. Is
	 * called every time it is applied, so the value doesn't have to be final.
	 * 
	 * @return The modifier
	 */
	public double getModifier(final CoreAbility ability) {
		return this.modifier;
	}

	/**
	 * Returns what type of math should be done with the modifier.
	 * 
	 * @return The modifier type
	 */
	public AttributeModifierType getType() {
		return this.type;
	}

	public static AttributeModifier WATERBENDING_NIGHT = new AttributeModifier("WaterbendingNightModifier", AttributeModifierType.MULTIPLY, 1.0) {
		@Override
		public double getModifier(final CoreAbility ability) {
			return WaterAbility.getNightFactor(ability.getPlayer().getWorld());
		}
	};

	public static AttributeModifier FIREBENDING_DAY = new AttributeModifier("FirebendingDayModifier", AttributeModifierType.MULTIPLY, 1.0) {
		@Override
		public double getModifier(final CoreAbility ability) {
			return FireAbility.getDayFactor(1.0, ability.getPlayer().getWorld());
		}
	};
}

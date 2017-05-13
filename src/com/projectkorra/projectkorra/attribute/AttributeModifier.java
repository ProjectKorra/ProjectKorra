package com.projectkorra.projectkorra.attribute;

import org.bukkit.plugin.Plugin;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;

public class AttributeModifier {
	
	private String name;
	private double modifier = 1.0D;
	private Plugin plugin;
	private AttributeModifierType type;
	
	public enum AttributeModifierType {MULTIPLY, ADDITION};
	
	public AttributeModifier(String name, double modifier, AttributeModifierType type, Plugin plugin) {
		this.name = name;
		this.modifier = modifier;
		this.plugin = plugin;
		this.type = type;
	}
	
	public AttributeModifier(String name, AttributeModifierType type, Plugin plugin) {
		this(name, 1.0D, type, plugin);
	}
	
	protected AttributeModifier(String name, AttributeModifierType type, double modifier) {
		this(name, modifier, type, ProjectKorra.plugin);
	}
	
	/**
	 * Should return the modifier that should be applied to
	 * the Attribute. Is called every time it is applied, so
	 * the value doesn't have to be final.
	 * @return The modifier
	 */
	public double getModifier(CoreAbility ability) {
		return modifier;
	}
	
	/**
	 * Returns what type of math should be done with the
	 * modifier. 
	 * @return The modifier type
	 */
	public AttributeModifierType getType() {
		return type;
	}

	public static AttributeModifier WATERBENDING_NIGHT = new AttributeModifier("WaterbendingNightModifier", AttributeModifierType.MULTIPLY, 1.0) {
		@Override
		public double getModifier(CoreAbility ability) {
			return WaterAbility.getNightFactor(ability.getPlayer().getWorld());
		}
	};
	
	public static AttributeModifier FIREBENDING_DAY = new AttributeModifier("FirebendingDayModifier", AttributeModifierType.MULTIPLY, 1.0) {
		@Override
		public double getModifier(CoreAbility ability) {
			return FireAbility.getDayFactor(1.0, ability.getPlayer().getWorld());
		}
	};
}

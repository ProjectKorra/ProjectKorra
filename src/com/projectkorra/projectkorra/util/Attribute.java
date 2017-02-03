package com.projectkorra.projectkorra.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;

public class Attribute<TYPE> {

	private static Map<CoreAbility, Map<String, Attribute<? extends Object>>> attributes = new HashMap<>();

	public List<AttributeModifier<TYPE>> modifiers;
	public CoreAbility ability;
	public String name;
	public TYPE value;

	public Attribute(CoreAbility ability, String name, TYPE value) {
		this(ability, name, value, new ArrayList<AttributeModifier<TYPE>>());
	}
	
	public Attribute(CoreAbility ability, String name, TYPE value, List<AttributeModifier<TYPE>> modifiers) {
		this.ability = ability;
		this.name = name;
		this.value = value;
		if (!attributes.containsKey(ability)) {
			attributes.put(ability, new HashMap<String, Attribute<? extends Object>>());
		}
		attributes.get(ability).put(name.toLowerCase(), this);
		this.modifiers = new ArrayList<AttributeModifier<TYPE>>(modifiers);
	}
	
	public List<AttributeModifier<TYPE>> getModifiers() {
		return modifiers;
	}
	
	public void addModifier(AttributeModifier<TYPE> modifier) {
		modifiers.add(modifier);
	}

	public CoreAbility getAbility() {
		return ability;
	}
	
	public String getName() {
		return name;
	}
	
	public TYPE getDefault() {
		return value;
	}

	public TYPE getModified(BendingPlayer bPlayer) {
		TYPE modified = value;
		for (AttributeModifier<TYPE> modifier : modifiers) {
			if (!modifier.canModify(bPlayer)) {
				continue;
			}
			value = modifier.newValue(value);
		}

		return modified;
	}
	
	public static Attribute<? extends Object> get(CoreAbility ability, String name) {
		Map<String, Attribute<? extends Object>> map = attributes.containsKey(ability) ? attributes.get(ability) : new HashMap<String, Attribute<? extends Object>>();
		if (map.isEmpty()) {
			return null;
		}
		if (map.containsKey(name.toLowerCase())) {
			return map.get(name.toLowerCase());
		}
		return null;
	}

	public interface AttributeModifier<TYPE> {
		public boolean canModify(BendingPlayer bPlayer);
		public TYPE newValue(TYPE value);
	}
	
	public interface Attributable {

		/**
		 * Registers the {@link Attribute} objects the ability has
		 */
		public void registerAttributes();
	}
}

package com.projectkorra.projectkorra.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;

public class Attribute<TYPE> {

	@SuppressWarnings("rawtypes")
	private static Map<CoreAbility, Map<String, Attribute>> attributes = new HashMap<>();

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
			attributes.put(ability, new HashMap<>());
		}
		attributes.get(ability).put(name.toLowerCase(), this);
		this.modifiers = new ArrayList<>(modifiers);
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
	
	@SuppressWarnings("rawtypes")
	public static Attribute get(CoreAbility ability, String name) {
		Map<String, Attribute> map = attributes.containsKey(ability) ? attributes.get(ability) : new HashMap<>();
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
		public int getPriority();
	}
}

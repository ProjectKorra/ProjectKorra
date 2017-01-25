package com.projectkorra.projectkorra.util.attributes;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.airbending.AirBlast;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.Attribute;

public class AttributeModifiers {

	public AttributeModifiers() {
		load();
	}
	
	@SuppressWarnings("unchecked")
	public void load() {
		Attribute<Double> airBlastPushSelf = Attribute.get(CoreAbility.getAbility(AirBlast.class), "pushFactor");
		airBlastPushSelf.addModifier(new AvatarStateModifier<Double>() {
			@Override
			public Double newValue(Double value) {
				return ConfigManager.getConfig().getDouble("Abilities.Avatar.AvatarState.Air.AirBlast.Push.Self");
			}
		});
		
		Attribute<Double> airBlastPushOthers = Attribute.get(CoreAbility.getAbility(AirBlast.class), "pushFactorForOthers");
		airBlastPushOthers.addModifier(new AvatarStateModifier<Double>() {
			@Override
			public Double newValue(Double value) {
				return ConfigManager.getConfig().getDouble("Abilities.Avatar.AvatarState.Air.AirBlast.Push.Entities");
			}
		});
	}
}

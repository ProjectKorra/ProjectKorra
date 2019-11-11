package com.projectkorra.projectkorra.airbending.util;

import com.projectkorra.projectkorra.ability.api.AddonAbilityInfo;
import com.projectkorra.projectkorra.element.Element;
import com.projectkorra.projectkorra.element.ElementManager;
import com.projectkorra.projectkorra.module.ModuleManager;

public abstract class AirAddonAbilityInfo implements AddonAbilityInfo {

	@Override
	public Element getElement() {
		return ModuleManager.getModule(ElementManager.class).getAir();
	}
}

package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.Element;

public interface SubAbility {

	public Class<? extends Ability> getParentAbility();
	
	public Element getParentElement();
}

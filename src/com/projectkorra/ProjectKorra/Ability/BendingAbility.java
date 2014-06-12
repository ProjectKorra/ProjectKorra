package com.projectkorra.ProjectKorra.Ability;

import com.projectkorra.ProjectKorra.Element;

public interface BendingAbility {

	/*
	 * The Description that will show when /bending help is used.
	 */
	public String getDescription();
	/*
	 * The name of the ability for /bending display
	 */
	public String getAbilityName();
	/*
	 * What element is required to Bend the element?
	 */
	public Element getElement();
	/*
	 * Begins the Ability.
	 */
	public void start();
	/*
	 * Progresses the ability.
	 */
	public void progressAll();
}

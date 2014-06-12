package com.projectkorra.ProjectKorra.Ability;

import com.projectkorra.ProjectKorra.Utilities.AbilityLoadable;

public class AbilityModule extends AbilityLoadable implements Cloneable {

	public AbilityModule(final String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public void onThisLoad() {
		// TODO Auto-generated method stub
		
	}
	
	// Must be overridden
	public String version() {
		return "outdated";
	}
	
	public String getElement() {
		return "";	
	}
	
	public String getAuthor() {
		return "";
	}
	
	public void stop() {
		
	}
	
	public boolean isShiftAbility() {
		return false;
	}
	
	public String getDescription() {
		return "";
	}
	

}

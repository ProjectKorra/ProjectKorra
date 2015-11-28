package com.projectkorra.projectkorra.ability.api;

public interface Ability {
	
	public String getName();
	
	public String getDescription();

	public void progress();

	public void remove();
}

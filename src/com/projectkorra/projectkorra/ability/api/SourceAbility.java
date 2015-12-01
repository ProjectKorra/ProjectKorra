package com.projectkorra.projectkorra.ability.api;

import org.bukkit.block.Block;

public interface SourceAbility {
	
	public Block getSource();
	
	public boolean canAutoSource();
	
	public boolean canDynamicSource();
	
	public boolean canSelfSource();
}

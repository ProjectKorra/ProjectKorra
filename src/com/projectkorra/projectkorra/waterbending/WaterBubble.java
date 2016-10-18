package com.projectkorra.projectkorra.waterbending;

import com.projectkorra.projectkorra.ability.WaterAbility;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *  WaterBubble is currently implemented in AirBubble
 */
public class WaterBubble extends WaterAbility {
		
	public WaterBubble(Player player) {
		super(player);
	}

	@Override
	public String getName() {
		return "WaterBubble";
	}

	@Override
	public void progress() {
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public long getCooldown() {
		return 0;
	}
	
	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

}

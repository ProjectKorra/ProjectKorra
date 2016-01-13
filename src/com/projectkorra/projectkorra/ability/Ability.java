package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.Element;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface Ability {
	
	public void progress();

	public void remove();
	
	public boolean isSneakAbility();
	
	/**
	 * Determines if this ability is considered harmless against other players. A harmless
	 * ability cannot manipulate another player. For example: AirPassive,
	 * WaterSpout, AirSpout, and FireJet.
	 * @return true if the ability is harmless and should be allowed in both PvP and non-PvP zones.
	 */
	public boolean isHarmlessAbility();
	
	public boolean isIgniteAbility();
	
	public boolean isExplosiveAbility();
	
	public boolean isHiddenAbility();
	
	public long getCooldown();
	
	public Player getPlayer();
	
	public String getName();
	
	public String getDescription();
	
	public Element getElement();
	
	public Location getLocation();
	
}

package com.projectkorra.projectkorra.util;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.metadata.FixedMetadataValue;

import com.projectkorra.projectkorra.ProjectKorra;

/**
 * Object to represent an ArmorStand that is not used for normal functionality
 * @author Simplicitee
 *
 */
public class TempArmorStand {
	
	public static Set<TempArmorStand> tempStands = new HashSet<>();
	
	public ArmorStand stand;

	public TempArmorStand(Location loc) {
		stand = loc.getWorld().spawn(loc, ArmorStand.class);
		stand.setMetadata("temparmorstand", new FixedMetadataValue(ProjectKorra.plugin, 0));
	}
	
	public ArmorStand getArmorStand() {
		return stand;
	}
	
	/**
	 * Removes all instances of TempArmorStands and the associated ArmorStands
	 */
	public static void removeAll() {
		for (TempArmorStand temp : tempStands) {
			temp.getArmorStand().remove();
		}
		tempStands.clear();
	}
}

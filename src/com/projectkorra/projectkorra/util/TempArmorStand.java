package com.projectkorra.projectkorra.util;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.metadata.FixedMetadataValue;

import com.projectkorra.projectkorra.ProjectKorra;

/**
 * Object to represent an ArmorStand that is not used for normal functionality
 *
 * @author Simplicitee
 *
 */
public class TempArmorStand {

	private static Set<TempArmorStand> tempStands = new HashSet<>();

	private ArmorStand stand;

	public TempArmorStand(final Location loc) {
		this.stand = loc.getWorld().spawn(loc, ArmorStand.class);
		this.stand.setMetadata("temparmorstand", new FixedMetadataValue(ProjectKorra.plugin, 0));
		tempStands.add(this);
	}

	public ArmorStand getArmorStand() {
		return this.stand;
	}

	/**
	 * Removes all instances of TempArmorStands and the associated ArmorStands
	 */
	public static void removeAll() {
		for (final TempArmorStand temp : tempStands) {
			temp.getArmorStand().remove();
		}
		tempStands.clear();
	}
	
	public static Set<TempArmorStand> getTempStands() {
		return tempStands;
	}
}

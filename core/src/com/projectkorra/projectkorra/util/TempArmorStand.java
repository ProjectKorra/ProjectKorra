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
 */
public class TempArmorStand {

	private static final Set<TempArmorStand> TEMP_STANDS = new HashSet<>();

	private final ArmorStand stand;

	public TempArmorStand(final Location location) {
		this.stand = location.getWorld().spawn(location, ArmorStand.class);
		this.stand.setMetadata("temparmorstand", new FixedMetadataValue(ProjectKorra.plugin, 0));
		TEMP_STANDS.add(this);
	}

	public ArmorStand getArmorStand() {
		return this.stand;
	}

	/**
	 * Removes all instances of TempArmorStands and the associated ArmorStands
	 */
	public static void removeAll() {
		for (final TempArmorStand temp : TEMP_STANDS) {
			temp.stand.remove();
		}
		TEMP_STANDS.clear();
	}
	
	public static Set<TempArmorStand> getTempStands() {
		return TEMP_STANDS;
	}
}

package com.projectkorra.projectkorra.ability.util;

import org.bukkit.Location;

import com.projectkorra.projectkorra.ability.CoreAbility;

/**
 * A Collision is used to represent the collision between two CoreAbility
 * objects.
 * 
 * To register Collisions Addon developers should use:<br>
 * ProjectKorra.getCollisionInitializer().addCollision(myCoreAbility)
 * ProjectKorra.getCollisionInitializer().addSmallAbility(myCoreAbility)
 * 
 * @see CollisionManager
 */
public class Collision {
	private CoreAbility abilityFirst;
	private CoreAbility abilitySecond;
	private boolean removingFirst;
	private boolean removingSecond;
	private Location locationFirst;
	private Location locationSecond;

	public Collision(CoreAbility abilityFirst, CoreAbility abilitySecond, boolean removingFirst, boolean removingSecond, Location locationFirst, Location locationSecond) {
		if (abilityFirst == null || abilitySecond == null) {
			return;
		}

		this.abilityFirst = abilityFirst;
		this.abilitySecond = abilitySecond;
		this.removingFirst = removingFirst;
		this.removingSecond = removingSecond;
		this.locationFirst = locationFirst;
		this.locationSecond = locationSecond;
	}

	public Collision(CoreAbility abilityFirst, CoreAbility abilitySecond, boolean removingFirst, boolean removingSecond) {
		this(abilityFirst, abilitySecond, removingFirst, removingSecond, null, null);
	}

	public CoreAbility getAbilityFirst() {
		return abilityFirst;
	}

	public void setAbilityFirst(CoreAbility abilityFirst) {
		this.abilityFirst = abilityFirst;
	}

	public CoreAbility getAbilitySecond() {
		return abilitySecond;
	}

	public void setAbilitySecond(CoreAbility abilitySecond) {
		this.abilitySecond = abilitySecond;
	}

	public boolean isRemovingFirst() {
		return removingFirst;
	}

	public void setRemovingFirst(boolean removingFirst) {
		this.removingFirst = removingFirst;
	}

	public boolean isRemovingSecond() {
		return removingSecond;
	}

	public void setRemovingSecond(boolean removingSecond) {
		this.removingSecond = removingSecond;
	}

	public Location getLocationFirst() {
		return locationFirst;
	}

	public void setLocationFirst(Location locationFirst) {
		this.locationFirst = locationFirst;
	}

	public Location getLocationSecond() {
		return locationSecond;
	}

	public void setLocationSecond(Location locationSecond) {
		this.locationSecond = locationSecond;
	}

}

package com.projectkorra.projectkorra.ability.util;

import com.projectkorra.projectkorra.ability.Ability;
import org.bukkit.Location;

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
@SuppressWarnings("rawtypes")
public class Collision {
	private Ability abilityFirst;
	private Ability abilitySecond;
	private boolean removingFirst;
	private boolean removingSecond;
	private Location locationFirst;
	private Location locationSecond;

	public Collision(final Ability abilityFirst, final Ability abilitySecond, final boolean removingFirst, final boolean removingSecond, final Location locationFirst, final Location locationSecond) {
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

	public Collision(final Ability abilityFirst, final Ability abilitySecond, final boolean removingFirst, final boolean removingSecond) {
		this(abilityFirst, abilitySecond, removingFirst, removingSecond, null, null);
	}

	public Ability getAbilityFirst() {
		return this.abilityFirst;
	}

	public void setAbilityFirst(final Ability abilityFirst) {
		this.abilityFirst = abilityFirst;
	}

	public Ability getAbilitySecond() {
		return this.abilitySecond;
	}

	public void setAbilitySecond(final Ability abilitySecond) {
		this.abilitySecond = abilitySecond;
	}

	public boolean isRemovingFirst() {
		return this.removingFirst;
	}

	public void setRemovingFirst(final boolean removingFirst) {
		this.removingFirst = removingFirst;
	}

	public boolean isRemovingSecond() {
		return this.removingSecond;
	}

	public void setRemovingSecond(final boolean removingSecond) {
		this.removingSecond = removingSecond;
	}

	public Location getLocationFirst() {
		return this.locationFirst;
	}

	public void setLocationFirst(final Location locationFirst) {
		this.locationFirst = locationFirst;
	}

	public Location getLocationSecond() {
		return this.locationSecond;
	}

	public void setLocationSecond(final Location locationSecond) {
		this.locationSecond = locationSecond;
	}

}

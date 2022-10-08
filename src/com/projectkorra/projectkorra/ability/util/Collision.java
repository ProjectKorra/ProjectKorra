package com.projectkorra.projectkorra.ability.util;

import com.projectkorra.projectkorra.ability.CoreAbility;
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
public class Collision {
	private CoreAbility abilityFirst;
	private CoreAbility abilitySecond;
	private boolean removingFirst;
	private boolean removingSecond;
	private Location locationFirst;
	private Location locationSecond;

	public Collision(final CoreAbility abilityFirst, final CoreAbility abilitySecond, final boolean removingFirst, final boolean removingSecond, final Location locationFirst, final Location locationSecond) {
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

	public Collision(final CoreAbility abilityFirst, final CoreAbility abilitySecond, final boolean removingFirst, final boolean removingSecond) {
		this(abilityFirst, abilitySecond, removingFirst, removingSecond, null, null);
	}

	public CoreAbility getAbilityFirst() {
		return this.abilityFirst;
	}

	public void setAbilityFirst(final CoreAbility abilityFirst) {
		this.abilityFirst = abilityFirst;
	}

	public CoreAbility getAbilitySecond() {
		return this.abilitySecond;
	}

	public void setAbilitySecond(final CoreAbility abilitySecond) {
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

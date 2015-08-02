package com.projectkorra.ProjectKorra.Ability;

import com.projectkorra.ProjectKorra.SubElement;
import com.projectkorra.ProjectKorra.Utilities.AbilityLoadable;

public abstract class AbilityModule extends AbilityLoadable implements Cloneable {
	/**
	 * AbilityModule Constructor.
	 * 
	 * @param name
	 *            The name of the ability.
	 */
	public AbilityModule(final String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Called when the ability is loaded by PK. This is where the developer
	 * registers Listeners and Permissions.
	 */
	public abstract void onThisLoad();

	// Must be overridden

	/**
	 * Accessor Method to get the version of the ability.
	 * 
	 * @return The version of the ability as a String.
	 */
	public abstract String getVersion();

	/**
	 * Accessor Method to get the Element of the ability. It is recommended to
	 * use the Element ENUM to get the returned String. This can be an empty
	 * String, in which case the ability will not belong to any element (such as
	 * AvatarState).
	 * 
	 * @return The Element the ability belongs to.
	 */
	public abstract String getElement();

	/**
	 * Accessor Method to get the name of the author.
	 * 
	 * @return The name of the author.
	 */
	public abstract String getAuthor();

	/**
	 * Accessor Method to get the description of the ability. This String is
	 * sent to any player who runs /pk display ability.
	 * 
	 * @return The Description of the ability.
	 */
	public abstract String getDescription();

	/**
	 * Accessor Method to get whether this ability uses sneaking to operate.
	 * Some features of the ProjectKorra plugin only work when this is false.
	 * (Fast Swimming for Waterbenders)
	 * 
	 * @return Whether or not the ability uses the sneak key.
	 */
	public abstract boolean isShiftAbility();

	/**
	 * Accessor Method to get whether this ability harms entities. AirSpout is
	 * an example of a harmless ability. For AirSpout, this returns true.
	 * IceBlast is an example of a harmful ability. For IceBlast, this returns
	 * false. Torrent is an example of both a harmless and a harmful ability.
	 * For Torrent, this returns false.
	 * 
	 * @return Whether of not the ability can hurt entities.
	 */
	public abstract boolean isHarmlessAbility();

	/**
	 * Accessor Method to get whether this ability can set fire to blocks.
	 * 
	 * @return Whether or not this ability can ignite blocks.
	 */
	public boolean isIgniteAbility() {
		return false;
	}

	/**
	 * Accessor Method to get whether this ability can create explosions.
	 * 
	 * @return Whether or not this ability creates explosions.
	 */
	public boolean isExplodeAbility() {
		return false;
	}

	/**
	 * Void Method called whenever ProjectKorra stops and the ability is
	 * unloaded.
	 * 
	 */
	public void stop() {

	}

	/**
	 * Accessor Method to get which SubElement the ability belongs to. If
	 * isSubAbility() returns true, the developer absolutely must implement this
	 * as well.
	 * 
	 * List of sub-elements:
	 * 
	 * Water: Icebending. Bloodbending. Plantbending. Healing.
	 * 
	 * Earth: Sandbending. Metalbending. Lavabending.
	 * 
	 * Fire: Combustion. Lightning.
	 * 
	 * Air: Flight. SpiritualProjection.
	 * 
	 * @return The SubElement the ability belongs to.
	 */
	public SubElement getSubElement() {
		return null;
	}
}

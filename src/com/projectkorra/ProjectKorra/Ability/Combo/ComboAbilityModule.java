package com.projectkorra.ProjectKorra.Ability.Combo;

import com.projectkorra.ProjectKorra.ComboManager;
import com.projectkorra.ProjectKorra.SubElement;
import com.projectkorra.ProjectKorra.Utilities.AbilityLoadable;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * Created by Carbogen on 2/7/2015.
 */
public abstract class ComboAbilityModule extends AbilityLoadable implements Cloneable {
	/**
	 * AbilityModule Constructor.
	 *
	 * @param name
	 *            The name of the ability.
	 */
	public ComboAbilityModule(final String name) {
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
	 * Accessor Method to get the instructions for using this combo.
	 *
	 * @return The steps for the combo.
	 */
	public abstract String getInstructions();

	/**
	 * Creates a new instance of the combo from a specific player.
	 * ProjectKorra's ComboModuleManager will use this method once the combo
	 * steps have been used by the player.
	 *
	 * @return A new instance of the ability.
	 * @param player
	 *            The player using the combo.
	 */
	public abstract Object createNewComboInstance(Player player);

	/**
	 * Returns the list of abilities which constitute the combo.
	 *
	 * @return An ArrayList containing the combo's steps.
	 */
	public abstract ArrayList<ComboManager.AbilityInformation> getCombination();

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

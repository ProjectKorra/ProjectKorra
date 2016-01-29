package com.projectkorra.projectkorra.ability;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.command.HelpCommand;
import com.projectkorra.projectkorra.earthbending.EarthBlast;
import com.projectkorra.projectkorra.firebending.Blaze;
import com.projectkorra.projectkorra.firebending.FireBlast;
import com.projectkorra.projectkorra.firebending.FireBlastCharged;
import com.projectkorra.projectkorra.firebending.FireBurst;
import com.projectkorra.projectkorra.waterbending.TorrentWave;
import com.projectkorra.projectkorra.waterbending.WaterPassive;

/**
 * The Ability interface defines the set of methods that any CoreAbility, AddonAbility, ComboAbility, or
 * MultiAbility should implement. {@link CoreAbility} provides a default implementation for a few of these
 * methods, but most will need to be specified by each Ability individually.
 */
public interface Ability {

	/**
	 * Causes the ability to be updated.
	 */
	public void progress();

	/**
	 * Causes the ability to be removed from existence.
	 */
	public void remove();

	/**
	 * Determines if this ability uses the {@link PlayerToggleSneakEvent} as a controlling
	 * mechanism. Currently {@link WaterPassive} will not work while the player has a sneak ability
	 * bound.
	 * 
	 * @return true if the ability uses sneak as a controlling mechanism
	 */
	public boolean isSneakAbility();

	/**
	 * Determines if this ability is considered harmless against other players. A harmless ability
	 * cannot manipulate another player. For example: AirPassive, WaterSpout, AirSpout, and FireJet.
	 * 
	 * @return true if the ability is harmless and should be allowed in both PvP and non-PvP zones
	 */
	public boolean isHarmlessAbility();

	/**
	 * Determines if this ability can ignite blocks. For example: {@link Blaze}, {@link FireBlast},
	 * and {@link FireBurst}.
	 */
	public boolean isIgniteAbility();

	/**
	 * Determines if this ability can cause explosions. For example: {@link FireBlastCharged}
	 */
	public boolean isExplosiveAbility();

	/**
	 * A hidden ability is an ability that should not be shown by commands such as <b>/bending
	 * display</b> and <b>/bending help</b>. For example: Combos, MultiAbility sub abilities, and
	 * helper abilities.
	 * 
	 * @return true if the ability should not be displayed to the players
	 */
	public boolean isHiddenAbility();
	
	/**
	 * Returns true if the ability is enabled through the config.yml. Usually the Enabled option
	 * follows the format Abilities.ElementName.AbilityName.Enabled.
	 */
	public boolean isEnabled();

	/**
	 * @return the cooldown for the ability
	 */
	public long getCooldown();

	/**
	 * Returns the player that caused this ability to be initiated. The player can be null in
	 * certain circumstances, for example when calling {@link CoreAbility#getAbility(String)}, or if
	 * an ability decided to set player to null.
	 * 
	 * @return the player that this ability belongs to
	 */
	public Player getPlayer();

	/**
	 * The name of the ability is used for commands such as <b>/bending display</b> and <b>/bending
	 * help</b>. The name is also used for determining the tag for cooldowns
	 * {@link BendingPlayer#addCooldown(Ability)}, therefore if two abilities have the same name
	 * they will also share cooldowns. If two classes share the same name (SurgeWall/SurgeWave) but
	 * need to have independent cooldowns, then {@link BendingPlayer#addCooldown(String, long)}
	 * should be called explicitly.
	 * 
	 * @return Returns the name of the ability
	 */
	public String getName();

	/**
	 * The description of an ability is a few sentences used to describe how the player can fully
	 * utilize the ability. In most cases the description will be specified in the config.yml file
	 * and will be retrieved by accessing the FileConfiguration via {@link CoreAbility#getConfig}.
	 * 
	 * @return the description for this ability
	 * @see HelpCommand
	 * @see CoreAbility#getDescription()
	 */
	public String getDescription();

	/**
	 * Specifies the Element used to represent this type of ability, favoring SubElements over
	 * Elements. For example, a LightningAbility would return {@link Element#LIGHTNING} instead of
	 * {@link Element#FIRE}.
	 * 
	 * @return the most accurate Element that this ability belongs to
	 * @see SubElement#getParentElement
	 */
	public Element getElement();

	/**
	 * Specifies the Location of the ability, which may be slightly inaccurate depending on the
	 * Ability implementation. For example, a {@link TorrentWave} could not be fully specified by a
	 * single location, while it is possible for an {@link EarthBlast}. The location is useful for
	 * making sure that the player is currently in the same world as the ability.
	 * 
	 * @return the location of the Ability
	 * @see BendingPlayer#canBend(CoreAbility)
	 */
	public Location getLocation();

}

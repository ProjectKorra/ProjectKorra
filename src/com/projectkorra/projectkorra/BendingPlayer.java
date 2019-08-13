package com.projectkorra.projectkorra;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.AvatarAbility;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.PassiveManager;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.configuration.better.ConfigManager;
import com.projectkorra.projectkorra.configuration.better.configs.properties.GeneralPropertiesConfig;
import com.projectkorra.projectkorra.earthbending.metal.MetalClips;
import com.projectkorra.projectkorra.event.PlayerCooldownChangeEvent;
import com.projectkorra.projectkorra.event.PlayerCooldownChangeEvent.Result;
import com.projectkorra.projectkorra.storage.DBConnection;
import com.projectkorra.projectkorra.util.Cooldown;
import com.projectkorra.projectkorra.util.DBCooldownManager;
import com.projectkorra.projectkorra.waterbending.blood.Bloodbending;

/**
 * Class that presents a player and stores all bending information about the
 * player.
 */
@SuppressWarnings("rawtypes")
public class BendingPlayer {

	/**
	 * ConcurrentHashMap that contains all instances of BendingPlayer, with UUID
	 * key.
	 */
	private static final Map<UUID, BendingPlayer> PLAYERS = new ConcurrentHashMap<>();

	private boolean permaRemoved;
	private boolean toggled;
	private boolean tremorSense;
	private boolean illumination;
	private boolean chiBlocked;
	private long slowTime;
	private final Player player;
	private final UUID uuid;
	private final String name;
	private ChiAbility stance;
	private final DBCooldownManager cooldownManager;
	private final ArrayList<Element> elements;
	private final ArrayList<SubElement> subelements;
	private HashMap<Integer, String> abilities;
	private final Map<String, Cooldown> cooldowns;
	private final Map<Element, Boolean> toggledElements;

	/**
	 * Creates a new {@link BendingPlayer}.
	 *
	 * @param uuid The unique identifier
	 * @param playerName The playername
	 * @param elements The known elements
	 * @param abilities The known abilities
	 * @param permaRemoved The permanent removed status
	 */
	public BendingPlayer(final UUID uuid, final String playerName, final ArrayList<Element> elements, final ArrayList<SubElement> subelements, final HashMap<Integer, String> abilities, final boolean permaRemoved) {
		this.uuid = uuid;
		this.name = playerName;
		this.cooldownManager = Manager.getManager(DBCooldownManager.class);
		this.elements = elements;
		this.subelements = subelements;
		this.setAbilities(abilities);
		this.permaRemoved = permaRemoved;
		this.player = Bukkit.getPlayer(uuid);
		this.toggled = true;
		this.tremorSense = true;
		this.illumination = true;
		this.chiBlocked = false;
		this.cooldowns = this.loadCooldowns();
		this.toggledElements = new ConcurrentHashMap<Element, Boolean>();
		for (final Element e : Element.getAllElements()) {
			if (!e.equals(Element.AVATAR)) {
				this.toggledElements.put(e, true);
			}
		}

		PLAYERS.put(uuid, this);
		GeneralMethods.loadBendingPlayer(this);
	}

	public void addCooldown(final Ability ability, final long cooldown, final boolean database) {
		this.addCooldown(ability.getName(), cooldown, database);
	}

	public void addCooldown(final Ability ability, final boolean database) {
		this.addCooldown(ability.getName(), ability.getCooldown(), database);
	}

	public void addCooldown(final Ability ability, final long cooldown) {
		this.addCooldown(ability, cooldown, false);
	}

	public void addCooldown(final Ability ability) {
		this.addCooldown(ability, false);
	}

	public void addCooldown(final String ability, final long cooldown) {
		this.addCooldown(ability, cooldown, false);
	}

	/**
	 * Adds an ability to the cooldowns map while firing a
	 * {@link PlayerCooldownChangeEvent}.
	 *
	 * @param ability Name of the ability
	 * @param cooldown The cooldown time
	 * @param database If the value should be saved to the database
	 */
	public void addCooldown(final String ability, final long cooldown, final boolean database) {
		if (cooldown <= 0) {
			return;
		}
		final PlayerCooldownChangeEvent event = new PlayerCooldownChangeEvent(Bukkit.getPlayer(this.uuid), ability, cooldown, Result.ADDED);
		Bukkit.getServer().getPluginManager().callEvent(event);

		if (!event.isCancelled()) {
			this.cooldowns.put(ability, new Cooldown(cooldown + System.currentTimeMillis(), database));

			final Player player = event.getPlayer();

			if (player == null) {
				return;
			}

			final String abilityName = event.getAbility();
			final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

			if (bPlayer.getBoundAbility() != null && bPlayer.getBoundAbility().equals(CoreAbility.getAbility(abilityName))) {
				GeneralMethods.displayMovePreview(player);
			}
		}
	}

	public Map<String, Cooldown> loadCooldowns() {
		final Map<String, Cooldown> cooldowns = new ConcurrentHashMap<>();
		if (ProjectKorra.isDatabaseCooldownsEnabled()) {
			try (ResultSet rs = DBConnection.sql.readQuery("SELECT * FROM pk_cooldowns WHERE uuid = '" + this.uuid.toString() + "'")) {
				while (rs.next()) {
					final int cooldownId = rs.getInt("cooldown_id");
					final long value = rs.getLong("value");
					final String name = this.cooldownManager.getCooldownName(cooldownId);
					cooldowns.put(name, new Cooldown(value, true));
				}
			} catch (final SQLException e) {
				e.printStackTrace();
			}
		}
		return cooldowns;
	}

	public void saveCooldowns() {
		DBConnection.sql.modifyQuery("DELETE FROM pk_cooldowns WHERE uuid = '" + this.uuid.toString() + "'", false);
		for (final Entry<String, Cooldown> entry : this.cooldowns.entrySet()) {
			final String name = entry.getKey();
			final Cooldown cooldown = entry.getValue();
			final int cooldownId = this.cooldownManager.getCooldownId(name, false);
			try (ResultSet rs = DBConnection.sql.readQuery("SELECT value FROM pk_cooldowns WHERE uuid = '" + this.uuid.toString() + "' AND cooldown_id = " + cooldownId)) {
				if (rs.next()) {
					DBConnection.sql.modifyQuery("UPDATE pk_cooldowns SET value = " + cooldown.getCooldown() + " WHERE uuid = '" + this.uuid.toString() + "' AND cooldown_id = " + cooldownId, false);
				} else {
					DBConnection.sql.modifyQuery("INSERT INTO  pk_cooldowns (uuid, cooldown_id, value) VALUES ('" + this.uuid.toString() + "', " + cooldownId + ", " + cooldown.getCooldown() + ")", false);
				}
			} catch (final SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Adds an element to the {@link BendingPlayer}'s known list.
	 *
	 * @param element The element to add.
	 */
	public void addElement(final Element element) {
		this.elements.add(element);
	}

	/**
	 * Adds a subelement to the {@link BendingPlayer}'s known list.
	 *
	 * @param subelement The subelement to add.
	 */
	public void addSubElement(final SubElement subelement) {
		this.subelements.add(subelement);
	}

	/**
	 * Sets chiBlocked to true.
	 */
	public void blockChi() {
		this.chiBlocked = true;
	}

	/**
	 * Checks to see if a Player is effected by BloodBending.
	 *
	 * @return true If {@link ChiMethods#isChiBlocked(String)} is true <br />
	 *         false If player is BloodBender and Bending is toggled on, or if
	 *         player is in AvatarState
	 */
	public boolean canBeBloodbent() {
		if (this.isAvatarState()) {
			if (this.isChiBlocked()) {
				return true;
			} else {
				return false;
			}
		}

		if (this.canBendIgnoreBindsCooldowns(CoreAbility.getAbility("Bloodbending")) && this.isToggled()) {
			return false;
		}

		return true;
	}

	public boolean canBend(final CoreAbility ability) {
		return this.canBend(ability, false, false);
	}

	private boolean canBend(final CoreAbility ability, final boolean ignoreBinds, final boolean ignoreCooldowns) {
		if (ability == null) {
			return false;
		}

		final Location playerLoc = this.player.getLocation();

		if (!this.player.isOnline() || this.player.isDead()) {
			return false;
		} else if (!this.canBind(ability)) {
			return false;
		} else if (ability.getPlayer() != null && ability.getLocation() != null && !ability.getLocation().getWorld().equals(this.player.getWorld())) {
			return false;
		} else if (!ignoreCooldowns && this.isOnCooldown(ability.getName())) {
			return false;
		} else if (!ignoreBinds && (!ability.getName().equals(this.getBoundAbilityName()))) {
			return false;
		} else if (Stream.of(ConfigManager.getConfig(GeneralPropertiesConfig.class).DisabledWorlds).anyMatch(this.player.getWorld().getName()::equalsIgnoreCase)) {
			return false;
		} else if (Commands.isToggledForAll || !this.isToggled() || !this.isElementToggled(ability.getElement())) {
			return false;
		} else if (this.player.getGameMode() == GameMode.SPECTATOR) {
			return false;
		}

		if (!ignoreCooldowns && this.cooldowns.containsKey(this.name)) {
			if (this.cooldowns.get(this.name).getCooldown() + ConfigManager.getConfig(GeneralPropertiesConfig.class).GlobalCooldown >= System.currentTimeMillis()) {
				return false;
			}

			this.cooldowns.remove(this.name);
		}

		if (this.isChiBlocked() || this.isParalyzed() || (this.isBloodbent() && !ability.getName().equalsIgnoreCase("AvatarState")) || this.isControlledByMetalClips()) {
			return false;
		} else if (GeneralMethods.isRegionProtectedFromBuild(this.player, ability.getName(), playerLoc)) {
			return false;
		}

		return true;
	}

	public boolean canBendIgnoreBinds(final CoreAbility ability) {
		return this.canBend(ability, true, false);
	}

	public boolean canBendIgnoreBindsCooldowns(final CoreAbility ability) {
		return this.canBend(ability, true, true);
	}

	public boolean canBendIgnoreCooldowns(final CoreAbility ability) {
		return this.canBend(ability, false, true);
	}

	public boolean canBendPassive(final CoreAbility ability) {
		if (ability == null) {
			return false; // If the passive is disabled.
		}
		final Element element = ability.getElement();
		if (Commands.isToggledForAll && ConfigManager.getConfig(GeneralPropertiesConfig.class).TogglePassivesWithAllBending) {
			return false;
		}


		if (element == null || this.player == null) {
			return false;
		} else if (!this.player.hasPermission("bending." + element.getName() + ".passive")) {
			return false;
		} else if (!this.hasElement(element)) {
			return false;
		} else if (Stream.of(ConfigManager.getConfig(GeneralPropertiesConfig.class).DisabledWorlds).anyMatch(this.player.getWorld().getName()::equalsIgnoreCase)) {
			return false;
		} else if (this.player.getGameMode() == GameMode.SPECTATOR) {
			return false;
		}

		return true;
	}

	public boolean canUsePassive(final CoreAbility ability) {
		final Element element = ability.getElement();
		if (!this.isToggled() || !this.isElementToggled(element)) {
			return false;
		} else if (this.isChiBlocked() || this.isParalyzed() || this.isBloodbent()) {
			return false;
		} else if (GeneralMethods.isRegionProtectedFromBuild(this.player, this.player.getLocation())) {
			return false;
		} else if (this.isOnCooldown(ability)) {
			return false;
		}

		return true;
	}

	public boolean canCurrentlyBendWithWeapons() {
		if (this.getBoundAbility() != null && this.player.getInventory().getItemInMainHand() != null) {
			final boolean hasWeapon = GeneralMethods.isWeapon(this.player.getInventory().getItemInMainHand().getType());
			final boolean noWeaponElement = GeneralMethods.getElementsWithNoWeaponBending().contains(this.getBoundAbility().getElement());

			if (hasWeapon) {
				if (noWeaponElement) {
					return false;
				} else {
					return true;
				}
			}

			return true;
		}

		return false;
	}

	/**
	 * Checks to see if {@link BendingPlayer} can be slowed.
	 *
	 * @return true If player can be slowed
	 */
	public boolean canBeSlowed() {
		return (System.currentTimeMillis() > this.slowTime);
	}

	public boolean canBind(final CoreAbility ability) {
		if (ability == null || !this.player.isOnline() || !ability.isEnabled()) {
			return false;
		} else if (!this.player.hasPermission("bending.ability." + ability.getName())) {
			return false;
		} else if (!this.hasElement(ability.getElement()) && !(ability instanceof AvatarAbility && !((AvatarAbility) ability).requireAvatar())) {
			return false;
		} else if (ability.getElement() instanceof SubElement) {
			final SubElement subElement = (SubElement) ability.getElement();
			if (!this.hasElement(subElement.getParentElement())) {
				return false;
			}

			if (!this.hasSubElement(subElement)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Checks to see if a player can BloodBend.
	 *
	 * @return true If player has permission node "bending.earth.bloodbending"
	 */
	public boolean canBloodbend() {
		return this.subelements.contains(Element.BLOOD);
	}

	public boolean canBloodbendAtAnytime() {
		return this.canBloodbend() && this.player.hasPermission("bending.water.bloodbending.anytime");
	}

	public boolean canCombustionbend() {
		return this.subelements.contains(Element.COMBUSTION);
	}

	public boolean canIcebend() {
		return this.subelements.contains(Element.ICE);
	}

	/**
	 * Checks to see if a player can LavaBend.
	 *
	 * @return true If player has permission node "bending.earth.lavabending"
	 */
	public boolean canLavabend() {
		return this.subelements.contains(Element.LAVA);
	}

	public boolean canLightningbend() {
		return this.subelements.contains(Element.LIGHTNING);
	}

	/**
	 * Checks to see if a player can MetalBend.
	 *
	 * @param player The player to check
	 * @return true If player has permission node "bending.earth.metalbending"
	 */
	public boolean canMetalbend() {
		return this.subelements.contains(Element.METAL);
	}

	/**
	 * Checks to see if a player can PlantBend.
	 *
	 * @param player The player to check
	 * @return true If player has permission node "bending.ability.plantbending"
	 */
	public boolean canPlantbend() {
		return this.subelements.contains(Element.PLANT);
	}

	/**
	 * Checks to see if a player can SandBend.
	 *
	 * @param player The player to check
	 * @return true If player has permission node "bending.earth.sandbending"
	 */
	public boolean canSandbend() {
		return this.subelements.contains(Element.SAND);
	}

	/**
	 * Checks to see if a player can use Flight.
	 *
	 * @return true If player has permission node "bending.air.flight"
	 */
	public boolean canUseFlight() {
		return this.subelements.contains(Element.FLIGHT);
	}

	/**
	 * Checks to see if a player can use SpiritualProjection.
	 *
	 * @param player The player to check
	 * @return true If player has permission node
	 *         "bending.air.spiritualprojection"
	 */
	public boolean canUseSpiritualProjection() {
		return this.subelements.contains(Element.SPIRITUAL);
	}

	/**
	 * Checks to see if a player can use Water Healing.
	 *
	 * @return true If player has permission node "bending.water.healing"
	 */
	public boolean canWaterHeal() {
		return this.subelements.contains(Element.HEALING);
	}

	/**
	 * Checks to see if a player can bend a specific sub element. Used when
	 * checking addon sub elements.
	 *
	 * @param sub SubElement to check for.
	 * @return true If the player has permission to bend that subelement.
	 */
	public boolean canUseSubElement(final SubElement sub) {
		return this.subelements.contains(sub);
	}

	/**
	 * Gets the map of abilities that the {@link BendingPlayer} knows.
	 *
	 * @return map of abilities
	 */
	public HashMap<Integer, String> getAbilities() {
		return this.abilities;
	}

	public static BendingPlayer getBendingPlayer(final OfflinePlayer oPlayer) {
		if (oPlayer == null) {
			return null;
		}

		return BendingPlayer.getPlayers().get(oPlayer.getUniqueId());
	}

	public static BendingPlayer getBendingPlayer(final Player player) {
		if (player == null) {
			return null;
		}

		return getBendingPlayer(player.getName());
	}

	/**
	 * Attempts to get a {@link BendingPlayer} from specified player name. this
	 * method tries to get a {@link Player} object and gets the uuid and then
	 * calls {@link #getBendingPlayer(UUID)}
	 *
	 * @param playerName The name of the Player
	 * @return The BendingPlayer object if {@link BendingPlayer#PLAYERS}
	 *         contains the player name
	 *
	 * @see #getBendingPlayer(UUID)
	 */
	public static BendingPlayer getBendingPlayer(final String playerName) {
		if (playerName == null) {
			return null;
		}

		final Player player = Bukkit.getPlayer(playerName);
		final OfflinePlayer oPlayer = player != null ? Bukkit.getOfflinePlayer(player.getUniqueId()) : null;

		return getBendingPlayer(oPlayer);
	}

	public CoreAbility getBoundAbility() {
		return CoreAbility.getAbility(this.getBoundAbilityName());
	}

	/**
	 * Gets the Ability bound to the slot that the player is in.
	 *
	 * @return The Ability name bounded to the slot
	 */
	public String getBoundAbilityName() {
		final int slot = this.player.getInventory().getHeldItemSlot() + 1;
		final String name = this.getAbilities().get(slot);

		return name != null ? name : "";
	}

	/**
	 * Gets the cooldown time of the ability.
	 *
	 * @param ability The ability to check
	 * @return the cooldown time
	 *         <p>
	 *         or -1 if cooldown doesn't exist
	 *         </p>
	 */
	public long getCooldown(final String ability) {
		if (this.cooldowns.containsKey(ability)) {
			return this.cooldowns.get(ability).getCooldown();
		}

		return -1;
	}

	/**
	 * Gets the map of cooldowns of the {@link BendingPlayer}.
	 *
	 * @return map of cooldowns
	 */
	public Map<String, Cooldown> getCooldowns() {
		return this.cooldowns;
	}

	/**
	 * Gets the list of elements the {@link BendingPlayer} knows.
	 *
	 * @return a list of elements
	 */
	public List<Element> getElements() {
		return this.elements;
	}

	/**
	 * Gets the name of the {@link BendingPlayer}.
	 *
	 * @return the player name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Gets the bukkit Player this {@link BendingPlayer} is wrapping.
	 *
	 * @return Player object this BendingPlayer is wrapping.
	 */
	public Player getPlayer() {
		return this.player;
	}

	/**
	 * Gets the map of {@link BendingPlayer}s.
	 *
	 * @return {@link #PLAYERS}
	 */
	public static Map<UUID, BendingPlayer> getPlayers() {
		return PLAYERS;
	}

	/**
	 * Gets the {@link ChiAbility Chi stance} the player is in
	 *
	 * @return The player's stance object
	 */
	public ChiAbility getStance() {
		return this.stance;
	}

	/**
	 * Gets the list of subelements the {@link BendingPlayer} knows.
	 *
	 * @return a list of subelements
	 */
	public List<SubElement> getSubElements() {
		return this.subelements;
	}

	/**
	 * Gets the unique identifier of the {@link BendingPlayer}.
	 *
	 * @return the uuid
	 */
	public UUID getUUID() {
		return this.uuid;
	}

	/**
	 * Convenience method to {@link #getUUID()} as a string.
	 *
	 * @return string version of uuid
	 */
	public String getUUIDString() {
		return this.uuid.toString();
	}

	/**
	 * Checks to see if the {@link BendingPlayer} knows a specific element.
	 *
	 * @param element The element to check
	 * @return true If the player knows the element
	 */
	public boolean hasElement(final Element element) {
		if (element == null) {
			return false;
		} else if (element == Element.AVATAR) {
			// At the moment we'll allow for both permissions to return true.
			// Later on we can consider deleting the bending.ability.avatarstate option.
			return this.player.hasPermission("bending.avatar") || this.player.hasPermission("bending.ability.AvatarState");
		} else if (!(element instanceof SubElement)) {
			return this.elements.contains(element);
		} else {
			return this.hasSubElement((SubElement) element);
		}
	}

	public boolean hasSubElement(final SubElement sub) {
		if (sub == null) {
			return false;
		} else {
			return this.subelements.contains(sub);
		}
	}

	/**
	 * Returns whether the player has permission to bend the subelement
	 *
	 * @param sub The SubElement
	 */
	public boolean hasSubElementPermission(final SubElement sub) {
		if (sub == null) {
			return false;
		}

		return this.player.hasPermission("bending." + sub.getParentElement().getName().toLowerCase() + "." + sub.getName().toLowerCase() + sub.getType().getBending());
	}

	public boolean isAvatarState() {
		return CoreAbility.hasAbility(this.player, AvatarState.class);
	}

	public boolean isBloodbent() {
		return Bloodbending.isBloodbent(this.player);
	}

	/**
	 * Checks to see if the {@link BendingPlayer} is a bender.
	 *
	 * @return true If the player is a bender.
	 */
	public boolean isBender() {
		return !this.elements.isEmpty();
	}

	/**
	 * Checks to see if the {@link BendingPlayer} is chi blocked.
	 *
	 * @return true If the player is chi blocked
	 */
	public boolean isChiBlocked() {
		return this.chiBlocked;
	}

	public boolean isControlledByMetalClips() {
		return MetalClips.isControlled(this.player);
	}

	public boolean isElementToggled(final Element element) {
		if (element != null && this.toggledElements.containsKey(element)) {
			return this.toggledElements.get(element);
		}

		return true;
	}

	public boolean isOnCooldown(final Ability ability) {
		return this.isOnCooldown(ability.getName());
	}

	/**
	 * Checks to see if a specific ability is on cooldown.
	 *
	 * @param ability The ability name to check
	 * @return true if the cooldown map contains the ability
	 */
	public boolean isOnCooldown(final String ability) {
		if (this.cooldowns.containsKey(ability)) {
			return System.currentTimeMillis() < this.cooldowns.get(ability).getCooldown();
		}

		return false;
	}

	public boolean isParalyzed() {
		return this.player.hasMetadata("movement:stop");
	}

	/**
	 * Checks if the {@link BendingPlayer} is permaremoved.
	 *
	 * @return true If the player is permaremoved
	 */
	public boolean isPermaRemoved() {
		return this.permaRemoved;
	}

	/**
	 * Checks if the {@link BendingPlayer} has bending toggled on.
	 *
	 * @return true If bending is toggled on
	 */
	public boolean isToggled() {
		return this.toggled;
	}

	/**
	 * Checks if the {@link BendingPlayer} is tremor sensing.
	 *
	 * @return true if player is tremor sensing
	 */
	public boolean isTremorSensing() {
		return this.tremorSense;
	}

	/**
	 * Checks if the {@link BendingPlayer} is using illumination.
	 *
	 * @return true if player is using illumination
	 */
	public boolean isIlluminating() {
		return this.illumination;
	}

	public void removeCooldown(final CoreAbility ability) {
		if (ability != null) {
			this.removeCooldown(ability.getName());
		}
	}

	/**
	 * Removes the cooldown of an ability.
	 *
	 * @param ability The ability's cooldown to remove
	 */
	public void removeCooldown(final String ability) {
		if (Bukkit.getPlayer(this.uuid) == null) {
			return;
		}

		final PlayerCooldownChangeEvent event = new PlayerCooldownChangeEvent(Bukkit.getPlayer(this.uuid), ability, 0, Result.REMOVED);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (!event.isCancelled()) {
			this.cooldowns.remove(ability);

			final Player player = event.getPlayer();
			final String abilityName = event.getAbility();
			final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

			if (bPlayer.getBoundAbility() != null && bPlayer.getBoundAbility().equals(CoreAbility.getAbility(abilityName))) {
				GeneralMethods.displayMovePreview(player);
			}
		}
	}

	/**
	 * Sets the {@link BendingPlayer}'s abilities. This method also saves the
	 * abilities to the database.
	 *
	 * @param abilities The abilities to set/save
	 */
	public void setAbilities(final HashMap<Integer, String> abilities) {
		this.abilities = abilities;

		for (int i = 1; i <= 9; i++) {
			DBConnection.sql.modifyQuery("UPDATE pk_players SET slot" + i + " = '" + abilities.get(i) + "' WHERE uuid = '" + this.uuid + "'");
		}
	}

	/**
	 * Sets the {@link BendingPlayer}'s element. If the player had elements
	 * before they will be overwritten.
	 *
	 * @param e The element to set
	 */
	public void setElement(final Element element) {
		this.elements.clear();
		this.elements.add(element);
	}

	/**
	 * Sets the permanent removed state of the {@link BendingPlayer}.
	 *
	 * @param permaRemoved
	 */
	public void setPermaRemoved(final boolean permaRemoved) {
		this.permaRemoved = permaRemoved;
	}

	/**
	 * Sets the player's {@link ChiAbility Chi stance}
	 *
	 * @param stance The player's new stance object
	 */
	public void setStance(final ChiAbility stance) {
		this.stance = stance;
	}

	/**
	 * Slow the {@link BendingPlayer} for a certain amount of time.
	 *
	 * @param cooldown The amount of time to slow.
	 */
	public void slow(final long cooldown) {
		this.slowTime = System.currentTimeMillis() + cooldown;
	}

	/**
	 * Toggles the {@link BendingPlayer}'s bending.
	 */
	public void toggleBending() {
		this.toggled = !this.toggled;
		PassiveManager.registerPassives(this.player);
	}

	public void toggleElement(final Element element) {
		if (element == null) {
			return;
		}

		this.toggledElements.put(element, !this.toggledElements.get(element));
		PassiveManager.registerPassives(this.player);
	}

	/**
	 * Toggles the {@link BendingPlayer}'s tremor sensing.
	 */
	public void toggleTremorSense() {
		this.tremorSense = !this.tremorSense;
	}

	/**
	 * Toggles the {@link BendingPlayer}'s illumination.
	 */
	public void toggleIllumination() {
		this.illumination = !this.illumination;
	}

	/**
	 * Sets the {@link BendingPlayer}'s chi blocked to false.
	 */
	public void unblockChi() {
		this.chiBlocked = false;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
}

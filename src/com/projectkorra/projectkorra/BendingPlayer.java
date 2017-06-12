package com.projectkorra.projectkorra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.AvatarAbility;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.chiblocking.Paralyze;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.metal.MetalClips;
import com.projectkorra.projectkorra.event.PlayerCooldownChangeEvent;
import com.projectkorra.projectkorra.event.PlayerCooldownChangeEvent.Result;
import com.projectkorra.projectkorra.storage.DBConnection;
import com.projectkorra.projectkorra.waterbending.blood.Bloodbending;
import com.projectkorra.spirits.SpiritElement;
import com.projectkorra.spirits.SpiritPlayer;

/**
 * Class that presents a player and stores all bending information about the
 * player.
 */
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
	private Player player;
	private UUID uuid;
	private String name;
	private ChiAbility stance;
	private ArrayList<Element> elements;
	private ArrayList<SubElement> subelements;
	private HashMap<Integer, String> abilities;
	private Map<String, Long> cooldowns;
	private Map<Element, Boolean> toggledElements;

	/**
	 * Creates a new {@link BendingPlayer}.
	 * 
	 * @param uuid The unique identifier
	 * @param playerName The playername
	 * @param elements The known elements
	 * @param abilities The known abilities
	 * @param permaRemoved The permanent removed status
	 */
	public BendingPlayer(UUID uuid, String playerName, ArrayList<Element> elements, ArrayList<SubElement> subelements, HashMap<Integer, String> abilities, boolean permaRemoved) {
		this.uuid = uuid;
		this.name = playerName;
		this.elements = elements;
		this.subelements = subelements;
		this.setAbilities(abilities);
		this.permaRemoved = permaRemoved;
		this.player = Bukkit.getPlayer(uuid);
		this.toggled = true;
		this.tremorSense = true;
		this.illumination = true;
		this.chiBlocked = false;
		cooldowns = new ConcurrentHashMap<String, Long>();
		toggledElements = new ConcurrentHashMap<Element, Boolean>();
		for (Element e : Element.getAllElements()) {
			if (!e.equals(Element.AVATAR)) {
				toggledElements.put(e, true);
			}
		}

		PLAYERS.put(uuid, this);
		GeneralMethods.loadBendingPlayer(this);
	}

	public void addCooldown(Ability ability, long cooldown) {
		addCooldown(ability.getName(), cooldown);
	}

	public void addCooldown(Ability ability) {
		addCooldown(ability, ability.getCooldown());
	}

	/**
	 * Adds an ability to the cooldowns map while firing a
	 * {@link PlayerCooldownChangeEvent}.
	 * 
	 * @param ability Name of the ability
	 * @param cooldown The cooldown time
	 */
	public void addCooldown(String ability, long cooldown) {
		PlayerCooldownChangeEvent event = new PlayerCooldownChangeEvent(Bukkit.getPlayer(uuid), ability, cooldown, Result.ADDED);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (!event.isCancelled()) {
			this.cooldowns.put(ability, cooldown + System.currentTimeMillis());

			Player player = event.getPlayer();
			
			if (player == null) {
				return;
			}
			
			int slot = player.getInventory().getHeldItemSlot() + 1;
			String abilityName = event.getAbility();
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

			if (bPlayer.getBoundAbility() != null && bPlayer.getBoundAbility().equals(CoreAbility.getAbility(abilityName))) {
				GeneralMethods.displayMovePreview(player);
			}
		}
	}

	/**
	 * Adds an element to the {@link BendingPlayer}'s known list.
	 * 
	 * @param element The element to add.
	 */
	public void addElement(Element element) {
		this.elements.add(element);
	}

	/**
	 * Adds a subelement to the {@link BendingPlayer}'s known list.
	 * 
	 * @param subelement The subelement to add.
	 */
	public void addSubElement(SubElement subelement) {
		this.subelements.add(subelement);
	}

	/**
	 * Sets chiBlocked to true.
	 */
	public void blockChi() {
		chiBlocked = true;
	}

	/**
	 * Checks to see if a Player is effected by BloodBending.
	 * 
	 * @return true If {@link ChiMethods#isChiBlocked(String)} is true <br />
	 *         false If player is BloodBender and Bending is toggled on, or if
	 *         player is in AvatarState
	 */
	public boolean canBeBloodbent() {
		if (isAvatarState()) {
			if (isChiBlocked()) {
				return true;
			}
		}
		if (canBendIgnoreBindsCooldowns(CoreAbility.getAbility("Bloodbending")) && !isToggled()) {
			return false;
		}
		return true;
	}

	public boolean canBend(CoreAbility ability) {
		return canBend(ability, false, false);
	}

	private boolean canBend(CoreAbility ability, boolean ignoreBinds, boolean ignoreCooldowns) {
		if (ability == null) {
			return false;
		}

		List<String> disabledWorlds = getConfig().getStringList("Properties.DisabledWorlds");
		Location playerLoc = player.getLocation();

		if (!player.isOnline() || player.isDead()) {
			return false;
		} else if (!canBind(ability)) {
			return false;
		} else if (ability.getPlayer() != null && ability.getLocation() != null && !ability.getLocation().getWorld().equals(player.getWorld())) {
			return false;
		} else if (!ignoreCooldowns && isOnCooldown(ability.getName())) {
			return false;
		} else if (!ignoreBinds && !ability.getName().equals(getBoundAbilityName())) {
			return false;
		} else if (disabledWorlds != null && disabledWorlds.contains(player.getWorld().getName())) {
			return false;
		} else if (Commands.isToggledForAll || !isToggled() || !isElementToggled(ability.getElement())) {
			return false;
		} else if (player.getGameMode() == GameMode.SPECTATOR) {
			return false;
		}

		if (!ignoreCooldowns && cooldowns.containsKey(name)) {
			if (cooldowns.get(name) + getConfig().getLong("Properties.GlobalCooldown") >= System.currentTimeMillis()) {
				return false;
			}
			cooldowns.remove(name);
		}

		if (isChiBlocked() || isParalyzed() || isBloodbent() || isControlledByMetalClips()) {
			return false;
		} else if (GeneralMethods.isRegionProtectedFromBuild(player, ability.getName(), playerLoc)) {
			return false;
		} else if (ability instanceof FireAbility && FireAbility.isSolarEclipse(player.getWorld())) {
			return false;
		} else if (ability instanceof WaterAbility && WaterAbility.isLunarEclipse(player.getWorld())) {
			return false;
		}
		return true;
	}

	public boolean canBendIgnoreBinds(CoreAbility ability) {
		return canBend(ability, true, false);
	}

	public boolean canBendIgnoreBindsCooldowns(CoreAbility ability) {
		return canBend(ability, true, true);
	}

	public boolean canBendIgnoreCooldowns(CoreAbility ability) {
		return canBend(ability, false, true);
	}

	public boolean canBendPassive(Element element) {
		if (Commands.isToggledForAll && ConfigManager.defaultConfig.get().getBoolean("Properties.TogglePassivesWithAllBending")) {
			return false;
		}

		List<String> disabledWorlds = getConfig().getStringList("Properties.DisabledWorlds");

		if (element == null || player == null) {
			return false;
		} else if (!player.hasPermission("bending." + element.getName() + ".passive")) {
			return false;
		} else if (!hasElement(element)) {
			return false;
		} else if (disabledWorlds != null && disabledWorlds.contains(player.getWorld().getName())) {
			return false;
		}
		return true;
	}

	public boolean canUsePassive(Element element) {
		if (!isToggled() || !isElementToggled(element)) {
			return false;
		} else if (isChiBlocked() || isParalyzed() || isBloodbent()) {
			return false;
		} else if (GeneralMethods.isRegionProtectedFromBuild(player, player.getLocation())) {
			return false;
		}
		return true;
	}

	public boolean canCurrentlyBendWithWeapons() {
		if (getBoundAbility() != null && player.getInventory().getItemInMainHand() != null) {
			boolean hasWeapon = GeneralMethods.isWeapon(player.getInventory().getItemInMainHand().getType());
			boolean noWeaponElement = GeneralMethods.getElementsWithNoWeaponBending().contains(getBoundAbility().getElement());

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
		return (System.currentTimeMillis() > slowTime);
	}

	public boolean canBind(CoreAbility ability) {
		if (ability == null || !player.isOnline() || !ability.isEnabled()) {
			return false;
		} else if (!player.hasPermission("bending.ability." + ability.getName())) {
			return false;
		} else if (!hasElement(ability.getElement()) && !(ability instanceof AvatarAbility && !((AvatarAbility) ability).requireAvatar())) {
			return false;
		} else if (ability.getElement() instanceof SubElement) {
			SubElement subElement = (SubElement) ability.getElement();
			if (!hasElement(subElement.getParentElement())) {
				return false;
			}
			if (!hasSubElement(subElement)) {
				return false;
			}
			if (GeneralMethods.hasSpirits()) {
				if (GeneralMethods.hasSpirits()) {
					SpiritPlayer sPlayer = SpiritPlayer.getSpiritPlayer(player);
					if (subElement.equals(SpiritElement.DARK) && sPlayer.isLightSpirit()) {
						return false;
					}
					if (subElement.equals(SpiritElement.LIGHT) && sPlayer.isDarkSpirit()) {
						return false;
					}
				}
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
		return subelements.contains(SubElement.BLOOD);
	}

	public boolean canBloodbendAtAnytime() {
		return canBloodbend() && player.hasPermission("bending.water.bloodbending.anytime");
	}

	public boolean canCombustionbend() {
		return subelements.contains(SubElement.COMBUSTION);
	}

	public boolean canIcebend() {
		return subelements.contains(SubElement.ICE);
	}

	/**
	 * Checks to see if a player can LavaBend.
	 * 
	 * @param player The player to check
	 * @return true If player has permission node "bending.earth.lavabending"
	 */
	public boolean canLavabend() {
		return subelements.contains(SubElement.LAVA);
	}

	public boolean canLightningbend() {
		return subelements.contains(SubElement.LIGHTNING);
	}

	/**
	 * Checks to see if a player can MetalBend.
	 * 
	 * @param player The player to check
	 * @return true If player has permission node "bending.earth.metalbending"
	 */
	public boolean canMetalbend() {
		return subelements.contains(SubElement.METAL);
	}

	/**
	 * Checks to see if a player can PlantBend.
	 * 
	 * @param player The player to check
	 * @return true If player has permission node "bending.ability.plantbending"
	 */
	public boolean canPlantbend() {
		return subelements.contains(SubElement.PLANT);
	}

	/**
	 * Checks to see if a player can SandBend.
	 * 
	 * @param player The player to check
	 * @return true If player has permission node "bending.earth.sandbending"
	 */
	public boolean canSandbend() {
		return subelements.contains(SubElement.SAND);
	}

	/**
	 * Checks to see if a player can use Flight.
	 * 
	 * @return true If player has permission node "bending.air.flight"
	 */
	public boolean canUseFlight() {
		return subelements.contains(SubElement.FLIGHT);
	}

	/**
	 * Checks to see if a player can use SpiritualProjection.
	 * 
	 * @param player The player to check
	 * @return true If player has permission node
	 *         "bending.air.spiritualprojection"
	 */
	public boolean canUseSpiritualProjection() {
		return subelements.contains(SubElement.SPIRITUAL);
	}

	/**
	 * Checks to see if a player can use Water Healing.
	 * 
	 * @return true If player has permission node "bending.water.healing"
	 */
	public boolean canWaterHeal() {
		return subelements.contains(SubElement.HEALING);
	}

	/**
	 * Checks to see if a player can bend a specific sub element. Used when
	 * checking addon sub elements.
	 * 
	 * @param sub SubElement to check for.
	 * @return true If the player has permission to bend that subelement.
	 */
	public boolean canUseSubElement(SubElement sub) {
		return subelements.contains(sub);
	}

	/**
	 * Gets the map of abilities that the {@link BendingPlayer} knows.
	 * 
	 * @return map of abilities
	 */
	public HashMap<Integer, String> getAbilities() {
		return this.abilities;
	}

	public CoreAbility getBoundAbility() {
		return CoreAbility.getAbility(getBoundAbilityName());
	}

	/**
	 * Gets the Ability bound to the slot that the player is in.
	 * 
	 * @return The Ability name bounded to the slot
	 */
	public String getBoundAbilityName() {
		int slot = player.getInventory().getHeldItemSlot() + 1;
		String name = getAbilities().get(slot);
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
	public long getCooldown(String ability) {
		if (cooldowns.containsKey(ability)) {
			return cooldowns.get(ability);
		}
		return -1;
	}

	/**
	 * Gets the map of cooldowns of the {@link BendingPlayer}.
	 * 
	 * @return map of cooldowns
	 */
	public Map<String, Long> getCooldowns() {
		return cooldowns;
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
	 * Gets the {@link ChiAbility Chi stance} the player is in
	 * 
	 * @return The player's stance object
	 */
	public ChiAbility getStance() {
		return stance;
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
	public boolean hasElement(Element element) {
		if (element == null) {
			return false;
		} else if (element == Element.AVATAR) {
			// At the moment we'll allow for both permissions to return true.
			// Later on we can consider deleting the bending.ability.avatarstate option.
			return player.hasPermission("bending.avatar") || player.hasPermission("bending.ability.AvatarState");
		} else if (!(element instanceof SubElement)) {
			return this.elements.contains(element);
		} else {
			return hasSubElement((SubElement) element);
		}
	}

	public boolean hasSubElement(SubElement sub) {
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
	public boolean hasSubElementPermission(SubElement sub) {
		if (sub == null) {
			return false;
		}
		return player.hasPermission("bending." + sub.getParentElement().getName().toLowerCase() + "." + sub.getName().toLowerCase() + sub.getType().getBending());
	}

	public boolean isAvatarState() {
		return CoreAbility.hasAbility(player, AvatarState.class);
	}

	public boolean isBloodbent() {
		return Bloodbending.isBloodbent(player);
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
		return MetalClips.isControlled(player);
	}

	public boolean isElementToggled(Element element) {
		if (element != null && toggledElements.containsKey(element)) {
			return toggledElements.get(element);
		}
		return true;
	}

	public boolean isOnCooldown(Ability ability) {
		return isOnCooldown(ability.getName());
	}

	/**
	 * Checks to see if a specific ability is on cooldown.
	 * 
	 * @param ability The ability name to check
	 * @return true if the cooldown map contains the ability
	 */
	public boolean isOnCooldown(String ability) {
		if (this.cooldowns.containsKey(ability)) {
			return System.currentTimeMillis() < cooldowns.get(ability);
		}
		return false;
	}

	public boolean isParalyzed() {
		return Paralyze.isParalyzed(player);
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

	public void removeCooldown(CoreAbility ability) {
		if (ability != null) {
			removeCooldown(ability.getName());
		}
	}

	/**
	 * Removes the cooldown of an ability.
	 * 
	 * @param ability The ability's cooldown to remove
	 */
	public void removeCooldown(String ability) {
		if (Bukkit.getPlayer(uuid) == null) {
			return;
		}
		PlayerCooldownChangeEvent event = new PlayerCooldownChangeEvent(Bukkit.getPlayer(uuid), ability, 0, Result.REMOVED);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (!event.isCancelled()) {
			this.cooldowns.remove(ability);

			Player player = event.getPlayer();
			int slot = player.getInventory().getHeldItemSlot() + 1;
			String abilityName = event.getAbility();
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

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
	public void setAbilities(HashMap<Integer, String> abilities) {
		this.abilities = abilities;
		for (int i = 1; i <= 9; i++) {
			DBConnection.sql.modifyQuery("UPDATE pk_players SET slot" + i + " = '" + abilities.get(i) + "' WHERE uuid = '" + uuid + "'");
		}
	}

	/**
	 * Sets the {@link BendingPlayer}'s element. If the player had elements
	 * before they will be overwritten.
	 * 
	 * @param e The element to set
	 */
	public void setElement(Element element) {
		this.elements.clear();
		this.elements.add(element);
	}

	/**
	 * Sets the permanent removed state of the {@link BendingPlayer}.
	 * 
	 * @param permaRemoved
	 */
	public void setPermaRemoved(boolean permaRemoved) {
		this.permaRemoved = permaRemoved;
	}

	/**
	 * Sets the player's {@link ChiAbility Chi stance}
	 * 
	 * @param stance The player's new stance object
	 */
	public void setStance(ChiAbility stance) {
		this.stance = stance;
	}

	/**
	 * Slow the {@link BendingPlayer} for a certain amount of time.
	 * 
	 * @param cooldown The amount of time to slow.
	 */
	public void slow(long cooldown) {
		slowTime = System.currentTimeMillis() + cooldown;
	}

	/**
	 * Toggles the {@link BendingPlayer}'s bending.
	 */
	public void toggleBending() {
		toggled = !toggled;
	}

	public void toggleElement(Element element) {
		if (element == null) {
			return;
		}
		toggledElements.put(element, !toggledElements.get(element));
	}

	/**
	 * Toggles the {@link BendingPlayer}'s tremor sensing.
	 */
	public void toggleTremorSense() {
		tremorSense = !tremorSense;
	}

	/**
	 * Toggles the {@link BendingPlayer}'s illumination.
	 */
	public void toggleIllumination() {
		illumination = !illumination;
	}

	/**
	 * Sets the {@link BendingPlayer}'s chi blocked to false.
	 */
	public void unblockChi() {
		chiBlocked = false;
	}

	public static BendingPlayer getBendingPlayer(OfflinePlayer oPlayer) {
		if (oPlayer == null) {
			return null;
		}
		return BendingPlayer.getPlayers().get(oPlayer.getUniqueId());
	}

	public static BendingPlayer getBendingPlayer(Player player) {
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
	public static BendingPlayer getBendingPlayer(String playerName) {
		if (playerName == null) {
			return null;
		}
		Player player = Bukkit.getPlayer(playerName);
		OfflinePlayer oPlayer = player != null ? Bukkit.getOfflinePlayer(player.getUniqueId()) : null;
		return getBendingPlayer(oPlayer);
	}

	private static FileConfiguration getConfig() {
		return ConfigManager.getConfig();
	}

	/**
	 * Gets the map of {@link BendingPlayer}s.
	 * 
	 * @return {@link #PLAYERS}
	 */
	public static Map<UUID, BendingPlayer> getPlayers() {
		return PLAYERS;
	}
}

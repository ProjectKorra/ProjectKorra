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

import com.projectkorra.projectkorra.command.CooldownCommand;
import com.projectkorra.projectkorra.event.PlayerStanceChangeEvent;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.Element.MultiSubElement;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.AvatarAbility;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.PassiveManager;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.board.BendingBoardManager;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.metal.MetalClips;
import com.projectkorra.projectkorra.event.PlayerCooldownChangeEvent;
import com.projectkorra.projectkorra.event.PlayerCooldownChangeEvent.Result;
import com.projectkorra.projectkorra.storage.DBConnection;
import com.projectkorra.projectkorra.util.Cooldown;
import com.projectkorra.projectkorra.util.DBCooldownManager;
import com.projectkorra.projectkorra.waterbending.blood.Bloodbending;
import org.jetbrains.annotations.NotNull;

/**
 * Class that presents a player and stores all bending information about the
 * player.
 */
public class BendingPlayer extends OfflineBendingPlayer {

	private long slowTime;
	private final Player player;
	private ChiAbility stance;

	protected boolean tremorSense;
	protected boolean illumination;
	protected boolean chiBlocked;

	public BendingPlayer(Player player) {
		super(player);

		this.player = player;
		this.tremorSense = true;
		this.illumination = true;
		this.chiBlocked = false;
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
		final PlayerCooldownChangeEvent event = new PlayerCooldownChangeEvent(player, ability, cooldown, Result.ADDED);
		Bukkit.getServer().getPluginManager().callEvent(event);

		if (!event.isCancelled()) {
			this.cooldowns.put(ability, new Cooldown(cooldown + System.currentTimeMillis(), database));

			final Player player = event.getPlayer();

			if (player == null) {
				return;
			}

			final String abilityName = event.getAbility();
			final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

			if (bPlayer != null && bPlayer.getBoundAbility() != null && bPlayer.getBoundAbility().equals(CoreAbility.getAbility(abilityName))) {
				GeneralMethods.displayMovePreview(player);
			}
			
			BendingBoardManager.updateBoard(player, event.getAbility(), true, 0);
			CooldownCommand.addCooldownType(ability);
		}
	}

	public Map<String, Cooldown> loadCooldowns() {
		final Map<String, Cooldown> cooldowns = new ConcurrentHashMap<>();

		return cooldowns;
	}

	//TODO Rewrite cooldowns with the ID system
	private void saveCooldownsForce() {
		DBConnection.sql.modifyQuery("DELETE FROM pk_cooldowns WHERE uuid = '" + this.uuid.toString() + "'", false);
		for (final Entry<String, Cooldown> entry : this.cooldowns.entrySet()) {
			final String name = entry.getKey();
			final Cooldown cooldown = entry.getValue();
			if (!cooldown.isDatabase()) continue;
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

	public void saveCooldowns(boolean async) {
		if (async) Bukkit.getScheduler().runTaskAsynchronously(ProjectKorra.plugin, this::saveCooldownsForce);
		else this.saveCooldownsForce();
	}

	public void saveCooldowns() {
		this.saveCooldowns(true);
	}

	/**
	 * Checks to see if a Player is effected by BloodBending.
	 *
	 * @return true If {@link #isChiBlocked()} is true <br />
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

		final List<String> disabledWorlds = getConfig().getStringList("Properties.DisabledWorlds");
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
		} else if (disabledWorlds.contains(this.player.getWorld().getName())) {
			return false;
		} else if (Commands.isToggledForAll || !this.isToggled() || !this.isElementToggled(ability.getElement())) {
			return false;
		} else if (this.player.getGameMode() == GameMode.SPECTATOR) {
			return false;
		}

		if (!ignoreCooldowns && this.cooldowns.containsKey(ability.getName())) {
			if (this.cooldowns.get(ability.getName()).getCooldown() + getConfig().getLong("Properties.GlobalCooldown") >= System.currentTimeMillis()) {
				return false;
			}

			this.cooldowns.remove(ability.getName());
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
		if (Commands.isToggledForAll && ConfigManager.defaultConfig.get().getBoolean("Properties.TogglePassivesWithAllBending")) {
			return false;
		}

		final List<String> disabledWorlds = getConfig().getStringList("Properties.DisabledWorlds");

		if (element == null || this.player == null) {
			return false;
		} else if (!this.player.hasPermission("bending." + element.getName() + ".passive")) {
			return false;
		} else if (!this.player.hasPermission("bending.ability." + ability.getName())) {
			return false;
		} else if (!this.hasElement(element)) {
			return false;
		} else if (disabledWorlds != null && disabledWorlds.contains(this.player.getWorld().getName())) {
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

	@Override
	public boolean canBloodbendAtAnytime() {
		return this.canBloodbend() && this.player.hasPermission("bending.water.bloodbending.anytime");
	}

	/**
	 * Gets a BendingPlayer instance for the provided player
	 * @param oPlayer The player
	 * @return The BendingPlayer instance
	 */
	public static BendingPlayer getBendingPlayer(@NotNull final OfflinePlayer oPlayer) {
		return OfflineBendingPlayer.ONLINE_PLAYERS.get(oPlayer.getUniqueId());
	}

	/**
	 * Gets a OfflineBendingPlayer instance for the provided player
	 * @param oPlayer The player
	 * @return The OfflineBendingPlayer instance
	 */
	public static OfflineBendingPlayer getOfflineBendingPlayer(@NotNull final OfflinePlayer oPlayer) {
		return OfflineBendingPlayer.PLAYERS.get(oPlayer.getUniqueId());
	}

	/**
	 * Gets a BendingPlayer instance for the provided player
	 * @param player The player
	 * @return The BendingPlayer instance
	 */
	public static BendingPlayer getBendingPlayer(@NotNull final Player player) {
		return getBendingPlayer((OfflinePlayer)player);
	}


	/**
	 * Attempts to get a {@link BendingPlayer} from specified player name. this
	 * method tries to get a {@link Player} object and gets the uuid and then
	 * calls {@link #getBendingPlayer(OfflinePlayer)}
	 *
	 * @param playerName The name of the Player
	 * @return The BendingPlayer object if {@link BendingPlayer#PLAYERS}
	 *         contains the player name
	 *
	 * @see #getBendingPlayer(OfflinePlayer)
	 */
	public static BendingPlayer getBendingPlayer(final String playerName) {
		if (playerName == null) {
			return null;
		}

		final Player player = Bukkit.getPlayer(playerName);
		final OfflinePlayer oPlayer = player != null ? Bukkit.getOfflinePlayer(player.getUniqueId()) : null;

		return getBendingPlayer(oPlayer);
	}

	/**
	 * Attempts to get a {@link OfflineBendingPlayer} from specified player name. this
	 * method tries to get a {@link Player} object and gets the uuid and then
	 * calls {@link #getBendingPlayer(OfflinePlayer)}
	 *
	 * @param playerName The name of the Player
	 * @return The OfflineBendingPlayer object if {@link BendingPlayer#PLAYERS}
	 *         contains the player name
	 *
	 * @see #getBendingPlayer(OfflinePlayer)
	 */
	public static OfflineBendingPlayer getOfflineBendingPlayer(final String playerName) {
		if (playerName == null) {
			return null;
		}

		final OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(playerName);

		return getBendingPlayer(oPlayer);
	}

	private static FileConfiguration getConfig() {
		return ConfigManager.getConfig();
	}

	@Override
	public void uncache() throws IllegalStateException {
		throw new IllegalStateException("Cannot uncache an online BendingPlayer!");
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
		return OfflineBendingPlayer.ONLINE_PLAYERS;
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
	 * Returns whether the player has permission to bend the subelement
	 *
	 * @param sub The SubElement
	 */
	public boolean hasSubElementPermission(final SubElement sub) {
		if (sub == null) {
			return false;
		}

		if (sub instanceof MultiSubElement) {
			for (Element parent : ((MultiSubElement) sub).getParentElements()) {
				if (this.player.hasPermission("bending." + parent.getName().toLowerCase() + "." + sub.getName().toLowerCase() + sub.getType().getBending())) return true;
			}
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

	public boolean isControlledByMetalClips() {
		return MetalClips.isControlled(this.player);
	}

	/**
	 * Checks to see if a specific ability is on cooldown.
	 *
	 * @param ability The ability name to check
	 * @return true if the cooldown map contains the ability
	 */
	@Override
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
	 * Checks to see if the {@link BendingPlayer} is chi blocked.
	 *
	 * @return true If the player is chi blocked
	 */
	public boolean isChiBlocked() {
		return this.chiBlocked;
	}

	/**
	 * Sets the {@link BendingPlayer}'s chi blocked to false.
	 */
	public void unblockChi() {
		this.chiBlocked = false;
	}

	/**
	 * Sets chiBlocked to true.
	 */
	public void blockChi() {
		this.chiBlocked = true;
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
		final PlayerCooldownChangeEvent event = new PlayerCooldownChangeEvent(player, ability, 0, Result.REMOVED);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (!event.isCancelled()) {
			this.cooldowns.remove(ability);

			
			final String abilityName = event.getAbility();

			if (this.getBoundAbility() != null && this.getBoundAbility().equals(CoreAbility.getAbility(abilityName))) {
				GeneralMethods.displayMovePreview(player);
			}

			BendingBoardManager.updateBoard(player, event.getAbility(), false, 0);
		}
	}

	/**
	 * Sets the player's {@link ChiAbility Chi stance}
	 * Also update any previews
	 *
	 * @param stance The player's new stance object
	 */
	public void setStance(final ChiAbility stance) {
		final String oldStance = (this.stance == null) ? "" : this.stance.getName();
		final String newStance = (stance == null) ? "" : stance.getName();
		this.stance = stance;
		GeneralMethods.displayMovePreview(player);
		final PlayerStanceChangeEvent event = new PlayerStanceChangeEvent(Bukkit.getPlayer(this.uuid), oldStance, newStance);
		Bukkit.getServer().getPluginManager().callEvent(event);
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
	@Override
	public void toggleBending() {
		this.toggled = !this.toggled;
		PassiveManager.registerPassives(this.player);
	}

	@Override
	public void toggleElement(final Element element) {
		super.toggleElement(element);
		PassiveManager.registerPassives(this.player);
	}


	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

	@Override
	public void setCurrentSlot(int slot) {
		this.player.getInventory().setHeldItemSlot(slot % 9);
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
}

package com.projectkorra.projectkorra;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.board.BendingBoard;
import com.projectkorra.projectkorra.command.CooldownCommand;
import com.projectkorra.projectkorra.event.BendingPlayerCreationEvent;
import com.projectkorra.projectkorra.event.PlayerStanceChangeEvent;
import com.projectkorra.projectkorra.hooks.CanBendHook;
import com.projectkorra.projectkorra.object.Preset;
import com.projectkorra.projectkorra.region.RegionProtection;
import com.projectkorra.projectkorra.util.OptionalBoolean;
import net.md_5.bungee.api.ChatColor;
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
import com.projectkorra.projectkorra.util.Cooldown;
import com.projectkorra.projectkorra.waterbending.blood.Bloodbending;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

/**
 * Class that presents a player and stores all bending information about the
 * player.
 */
public class BendingPlayer extends OfflineBendingPlayer {

	protected static Map<JavaPlugin, CanBendHook> HOOKS = new HashMap<>();

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

	/**
	 * Adds an ability to the cooldowns map while firing a
	 * {@link PlayerCooldownChangeEvent}.
	 *
	 * @param ability Name of the ability
	 * @param cooldown The cooldown time
	 * @param database If the value should be saved to the database
	 */
	@Override
	public void addCooldown(final String ability, final long cooldown, final boolean database) {
		if (cooldown <= 0) {
			return;
		}
		final PlayerCooldownChangeEvent event = new PlayerCooldownChangeEvent(this.player, ability, cooldown, Result.ADDED);
		Bukkit.getServer().getPluginManager().callEvent(event);

		if (!event.isCancelled()) {
			this.cooldowns.put(ability, new Cooldown(cooldown + System.currentTimeMillis(), database));

			if (this.getBoundAbilityName() != null && this.getBoundAbilityName().equalsIgnoreCase(ability)) {
				GeneralMethods.displayMovePreview(this.player);
			}
			
			BendingBoardManager.updateBoard(this.player, event.getAbility(), true, 0);
			CooldownCommand.addCooldownType(ability);
		}
	}

	public Map<String, Cooldown> loadCooldowns() {
		return new ConcurrentHashMap<>();
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
			return this.isChiBlocked();
		}

		return !this.canBendIgnoreBindsCooldowns(CoreAbility.getAbility("Bloodbending")) || !this.isToggled();
	}

	public boolean canBend(final CoreAbility ability) {
		return this.canBend(ability, false, false);
	}

	private boolean canBend(@NotNull final CoreAbility ability, final boolean ignoreBinds, final boolean ignoreCooldowns) {

		final List<String> disabledWorlds = getConfig().getStringList("Properties.DisabledWorlds");
		final Location playerLoc = this.player.getLocation();

		//Loop through all hooks and test them
		for (JavaPlugin plugin : HOOKS.keySet()) {
			CanBendHook hook = HOOKS.get(plugin);
			try {
				OptionalBoolean bool = hook.canBend(this, ability, ignoreBinds, ignoreCooldowns);
				if (bool.isPresent()) return bool.getValue(); //If the hook didn't return OptionalBoolean.DEFAULT
			} catch (Exception e) {
				ProjectKorra.log.severe("An error occurred while running CanBendHook registered by " + plugin.getName() + ".");
				e.printStackTrace();
			}
		}

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
		} else if (RegionProtection.isRegionProtected(this.player, playerLoc, ability)) {
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
		if (ability == null || !this.isPassiveToggled(ability.getElement()) || !this.isToggledPassives()) {
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
		} else return this.player.getGameMode() != GameMode.SPECTATOR;
	}

	public boolean canUsePassive(final CoreAbility ability) {
		final Element element = ability.getElement();
		if (!this.isToggled() || !this.isElementToggled(element) || !this.isPassiveToggled(element) || !this.isToggledPassives()) {
			return false;
		} else if (this.isChiBlocked() || this.isParalyzed() || this.isBloodbent()) {
			return false;
		} else if (GeneralMethods.isRegionProtectedFromBuild(this.player, this.player.getLocation())) {
			return false;
		} else return !this.isOnCooldown(ability);
	}

	public boolean canCurrentlyBendWithWeapons() {
		if (this.getBoundAbility() != null && this.player.getInventory().getItemInMainHand() != null) {
			final boolean hasWeapon = GeneralMethods.isWeapon(this.player.getInventory().getItemInMainHand().getType());
			final boolean noWeaponElement = GeneralMethods.getElementsWithNoWeaponBending().contains(this.getBoundAbility().getElement());

			if (hasWeapon) {
				return !noWeaponElement;
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
			return this.hasSubElement(subElement);
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
	public static OfflineBendingPlayer getCachedOffline(@NotNull final OfflinePlayer oPlayer) {
		return OfflineBendingPlayer.PLAYERS.get(oPlayer.getUniqueId());
	}

	/**
	 * Gets an offline BendingPlayer instance for the provided player. If the instance is
	 * not already cached, it will load it from the database.
	 * @param oPlayer The offline player
	 * @return A CompletedFuture of the OfflineBendingPlayer instance
	 */
	public static CompletableFuture<OfflineBendingPlayer> getOrLoadOfflineAsync(@NotNull final OfflinePlayer oPlayer) {
		return OfflineBendingPlayer.loadAsync(oPlayer.getUniqueId(), false);
	}

	/**
	 * Gets an offline BendingPlayer instance for the provided player. If the instance is
	 * not already cached, it will load it from the database. Note: This will do it on the
	 * main thread!
	 * @param oPlayer The offline player
	 * @return The OfflineBendingPlayer instance
	 */
	public static OfflineBendingPlayer getOrLoadOffline(@NotNull final OfflinePlayer oPlayer) {
		try {
			return getOrLoadOfflineAsync(oPlayer).get();
		} catch (ExecutionException | InterruptedException e) {
			e.printStackTrace();
			return null;
		}
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

	@Override
	public void uncacheAfter(long time) throws IllegalStateException {
		throw new IllegalStateException("Cannot uncache an online BendingPlayer!");
	}

	@Override
	public int getCurrentSlot() {
		return this.player.getInventory().getHeldItemSlot();
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
	 * @return {@link #ONLINE_PLAYERS}
	 */
	public static Map<UUID, BendingPlayer> getPlayers() {
		return OfflineBendingPlayer.ONLINE_PLAYERS;
	}

	/**
	 * Gets the map of {@link OfflineBendingPlayer}s.
	 *
	 * @return {@link #PLAYERS}
	 */
	public static Map<UUID, OfflineBendingPlayer> getOfflinePlayers() {
		return OfflineBendingPlayer.PLAYERS;
	}

	public static void registerCanBendHook(@NotNull JavaPlugin plugin, @NotNull CanBendHook hook) {
		HOOKS.put(plugin, hook);
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

	/**
	 * Removes the cooldown of an ability.
	 *
	 * @param ability The ability's cooldown to remove
	 */
	@Override
	public void removeCooldown(final String ability) {
		final PlayerCooldownChangeEvent event = new PlayerCooldownChangeEvent(this.player, ability, 0, Result.REMOVED);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (!event.isCancelled()) {
			this.cooldowns.remove(ability);

			
			final String abilityName = event.getAbility();

			if (this.getBoundAbility() != null && this.getBoundAbilityName().equals(abilityName)) {
				GeneralMethods.displayMovePreview(this.player);
			}

			BendingBoardManager.updateBoard(this.player, event.getAbility(), false, 0);
		}
	}

	/**
	 * Removes all cooldowns that have expired.
	 *
	 * We cannot call the {@link #removeCooldown(String)} method here because it
	 * would cause a ConcurrentModificationException. That's why we have to copy
	 * most of the method here so the event is still called, but we remove from
	 * the iterator instead of the map like we do in {@link #removeCooldown(String)}
	 */
	@Override
	protected void removeOldCooldowns() {
		Iterator<Entry<String, Cooldown>> iterator = this.cooldowns.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Cooldown> entry = iterator.next();
			if (System.currentTimeMillis() >= entry.getValue().getCooldown()) {
				final PlayerCooldownChangeEvent event = new PlayerCooldownChangeEvent(this.player, entry.getKey(), 0, Result.REMOVED);
				Bukkit.getServer().getPluginManager().callEvent(event);
				if (!event.isCancelled()) {
					iterator.remove();

					final String abilityName = event.getAbility();

					if (this.getBoundAbility() != null && this.getBoundAbilityName().equals(abilityName)) {
						GeneralMethods.displayMovePreview(this.player);
					}

					BendingBoardManager.updateBoard(this.player, event.getAbility(), false, 0);
				}
			}
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
		GeneralMethods.displayMovePreview(this.player);
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
	public void toggleAllPassives() {
		this.allPassivesToggled = !this.allPassivesToggled;
		PassiveManager.registerPassives(this.player);
	}

	@Override
	public void toggleElement(final Element element) {
		super.toggleElement(element);
		PassiveManager.registerPassives(this.player);
	}

	@Override
	public void togglePassive(final Element element) {
		super.togglePassive(element);
		PassiveManager.registerPassives(this.player);
	}

	public void removeUnusableAbilities() {
		// Remove all active instances of abilities that will become unusable.
		// We need to do this prior to filtering binds in case the player has a MultiAbility running.
		for (final CoreAbility coreAbility : CoreAbility.getAbilities()) {
			final CoreAbility playerAbility = CoreAbility.getAbility(this.player, coreAbility.getClass());
			if (playerAbility != null) {
				if (playerAbility instanceof PassiveAbility && PassiveManager.hasPassive(this.player, playerAbility)) {
					// The player will be able to keep using the given PassiveAbility.
					continue;
				} else if (this.canBend(playerAbility)) {
					// The player will still be able to use this given Ability, do not end it.
					continue;
				}
				playerAbility.remove();
			}
		}

		// Remove all bound abilities that will become unusable.
		final HashMap<Integer, String> slots = this.getAbilities();
		final HashMap<Integer, String> finalAbilities = new HashMap<>();
		for (final int i : slots.keySet()) {
			if (this.canBind(CoreAbility.getAbility(slots.get(i)))) {
				// The player will still be able to use this given Ability, do not remove it from their binds.
				finalAbilities.put(i, slots.get(i));
			}
		}
		this.setAbilities(finalAbilities);
	}

	/**
	 * Do all the stuff we need to after the BendingPlayer instance is loaded
	 */
	protected void postLoad() {
		if (!this.toggled) {
			GeneralMethods.sendBrandingMessage(this.player, ChatColor.YELLOW + ConfigManager.languageConfig.get().getString("Command.Toggle.Reminder"));
		}

		Preset.loadPresets(this.player);

		final boolean chatEnabled = ConfigManager.languageConfig.get().getBoolean("Chat.Enable");

		String prefix = ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', ConfigManager.languageConfig.get().getString("Chat.Prefixes.Nonbender", "")) + " ";
		if (this.player.hasPermission("bending.avatar") || (this.hasElement(Element.AIR) && this.hasElement(Element.EARTH) && this.hasElement(Element.FIRE) && this.hasElement(Element.WATER))) {
			prefix = Element.AVATAR.getPrefix();
		} else if (this.getElements().size() > 0) {
			Element element = this.getElements().get(0);
			prefix = element.getPrefix();
		}

		if (chatEnabled) {
			this.player.setDisplayName(this.player.getName());
			this.player.setDisplayName(prefix + ChatColor.RESET + this.player.getDisplayName());
		}

		// Handle the AirSpout/WaterSpout login glitches.
		if (this.player.getGameMode() != GameMode.CREATIVE) {
			final HashMap<Integer, String> bound = this.getAbilities();
			for (final String str : bound.values()) {
				if (str.equalsIgnoreCase("AirSpout") || str.equalsIgnoreCase("WaterSpout") || str.equalsIgnoreCase("SandSpout")) {
					final Player fplayer = this.player;
					new BukkitRunnable() {
						@Override
						public void run() {
							fplayer.setFlying(false);
							fplayer.setAllowFlight(false);
						}
					}.runTaskLater(ProjectKorra.plugin, 2);
					break;
				}
			}
		}

		this.removeUnusableAbilities();
		this.fixSubelements(); //Grant all subelements for an element if they have 0 subs for that element (that they are allowed)
		this.removeOldCooldowns();
		PassiveManager.registerPassives(this.player);
		BendingBoardManager.getBoard(this.player).ifPresent(BendingBoard::show); //Show the bending board
		BendingBoardManager.changeWorld(this.player); //Hide the board if they spawn in a world with bending disabled

		Bukkit.getServer().getPluginManager().callEvent(new BendingPlayerCreationEvent(this));
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

	/**
	 * Gives all subelements a player doesn't have when they log in. Deprecated
	 * because subelements being a list will be phased out eventually
	 */
	@Deprecated
	public void fixSubelements() {
		boolean save = false;
		for (Element element : this.elements) {
			List<SubElement> currentSubs = this.subelements.stream().filter(sub -> sub.getParentElement() == element).collect(Collectors.toList());
			if (currentSubs.size() > 0) continue;
			for (SubElement sub : Element.getSubElements(element)) {
				if (this.hasSubElementPermission(sub)) {
					this.addSubElement(sub);
					save = true;
				}
			}
		}
		if (save) this.saveSubElements();
	}
}

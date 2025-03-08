package com.projectkorra.projectkorra;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.ability.StanceAbility;
import com.projectkorra.projectkorra.board.BendingBoard;
import com.projectkorra.projectkorra.command.CooldownCommand;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent;
import com.projectkorra.projectkorra.event.PlayerChangeSubElementEvent;
import com.projectkorra.projectkorra.event.PlayerStanceChangeEvent;
import com.projectkorra.projectkorra.firebending.passive.FirePassive;
import com.projectkorra.projectkorra.hooks.CanBendHook;
import com.projectkorra.projectkorra.hooks.CanBindHook;
import com.projectkorra.projectkorra.object.Preset;
import com.projectkorra.projectkorra.region.RegionProtection;
import com.projectkorra.projectkorra.util.ChatUtil;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
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

	protected static Map<JavaPlugin, CanBendHook> BEND_HOOKS = new HashMap<>();
	protected static Map<JavaPlugin, CanBindHook> BIND_HOOKS = new HashMap<>();
	static Set<String> DISABLED_WORLDS = new HashSet<>();

	private long slowTime;
	private final Player player;
	private StanceAbility stance;

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
			this.cooldowns.put(ability, new Cooldown(event.getCooldown() + System.currentTimeMillis(), database));

			if (this.getBoundAbilityName() != null && this.getBoundAbilityName().equalsIgnoreCase(ability)) {
				ChatUtil.displayMovePreview(this.player);
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
		for (JavaPlugin plugin : BEND_HOOKS.keySet()) {
			CanBendHook hook = BEND_HOOKS.get(plugin);
			try {
				Optional<Boolean> bool = hook.canBend(this, ability, ignoreBinds, ignoreCooldowns);
				if (bool.isPresent()) return bool.get(); //If the hook didn't return
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
		} else if (disabledWorlds.contains(this.player.getWorld().getName())) {
			return false;
		} else return this.player.getGameMode() != GameMode.SPECTATOR;
	}

	public boolean canUsePassive(final CoreAbility ability) {
		final Element element = ability.getElement();
		if ((!this.isToggled() && ConfigManager.defaultConfig.get().getBoolean("Properties.TogglePassivesWithAllBending")) || !this.isElementToggled(element) || !this.isPassiveToggled(element) || !this.isToggledPassives()) {
			return false;
		} else if (this.isChiBlocked() || this.isParalyzed() || this.isBloodbent()) {
			return false;
		} else if (RegionProtection.isRegionProtected(this.player, this.player.getLocation(), ability)) {
			return false;
		} else return !this.isOnCooldown(ability);
	}

	public boolean canCurrentlyBendWithWeapons() {
		if (this.getBoundAbility() != null) {
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

	/**
	 * Check if the {@link BendingPlayer} can bend in the world they are in
	 */
	public boolean canBendInWorld() {
		return !DISABLED_WORLDS.contains(this.getPlayer().getWorld().getName());
	}

	/**
	 * Check if the {@link BendingPlayer} can bind the provided {@link CoreAbility}
	 * @param ability The {@link CoreAbility} to check
	 * @return True if they can bind it
	 */
	public boolean canBind(final CoreAbility ability) {
		//Loop through all hooks and test them
		for (JavaPlugin plugin : BIND_HOOKS.keySet()) {
			CanBindHook hook = BIND_HOOKS.get(plugin);
			try {
				Optional<Boolean> bool = hook.canBind(this, ability);
				if (bool.isPresent()) return bool.get(); //If the hook didn't return
			} catch (Exception e) {
				ProjectKorra.log.severe("An error occurred while running CanBindHook registered by " + plugin.getName() + ".");
				e.printStackTrace();
			}
		}

		if (ability == null || !this.player.isOnline() || !ability.isEnabled()) {
			return false;
		} else if (!this.player.hasPermission("bending.ability." + ability.getName())) {
			return false;
		} else if (!this.hasElement(ability.getElement()) && !(ability instanceof AvatarAbility && !((AvatarAbility) ability).requireAvatar())) {
			return false;
		} else if (ability.getElement() instanceof SubElement) {
			final SubElement subElement = (SubElement) ability.getElement();
			if (subElement instanceof MultiSubElement) {
				for (Element parent : ((MultiSubElement) subElement).getParentElements()) {
					if (!this.hasElement(parent)) return false;
				}
			} else if (!this.hasElement(subElement.getParentElement())) {
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

	/**
	 * Registers a new {@link CanBendHook} for the specified plugin. The hook changes the {@link #canBend(CoreAbility)} method
	 * @param plugin The plugin registering the hook
	 * @param hook The hook to register
	 */
	public static void registerCanBendHook(@NotNull JavaPlugin plugin, @NotNull CanBendHook hook) {
		BEND_HOOKS.put(plugin, hook);
	}

	/**
	 * Registers a new {@link CanBindHook} for the specified plugin. The hook changes the {@link #canBind(CoreAbility)} method
	 * @param plugin The plugin registering the hook
	 * @param hook The hook to register
	 */
	public static void registerCanBindHook(@NotNull JavaPlugin plugin, @NotNull CanBindHook hook) {
		BIND_HOOKS.put(plugin, hook);
	}

	/**
	 * Gets the {@link ChiAbility Chi stance} the player is in
	 *
	 * @return The player's stance object
	 */
	public StanceAbility getStance() {
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
		if (this.isAvatarState() && !ConfigManager.avatarStateConfig.get().getBoolean("AvatarState.CanBeChiblocked")) return;

		this.chiBlocked = true;
	}

	/**
	 * Checks if the {@link BendingPlayer} can be chiblocked. Will return false if they are already chiblocked
	 */
	public boolean canBeChiblocked() {
		return (!this.isAvatarState() || ConfigManager.avatarStateConfig.get().getBoolean("AvatarState.CanBeChiblocked")) && !this.isChiBlocked();
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
	 * Check if bending is disabled in the provided world
	 * @param world The world to check
	 * @return True if bending is disabled in the world
	 */
	public static boolean isWorldDisabled(World world) {
		return DISABLED_WORLDS.contains(world.getName());
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
				ChatUtil.displayMovePreview(this.player);
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
						ChatUtil.displayMovePreview(this.player);
					}

					BendingBoardManager.updateBoard(this.player, event.getAbility(), false, 0);
				}
			}
		}
	}

	/**
	 * Sets the player's {@link StanceAbility stance}
	 * Also update any previews
	 *
	 * @param stance The player's new stance object
	 */
	public void setStance(final StanceAbility stance) {
		final String oldStance = (this.stance == null) ? "" : this.stance.getStanceName();
		final String newStance = (stance == null) ? "" : stance.getStanceName();
		final PlayerStanceChangeEvent event = new PlayerStanceChangeEvent(this.player, oldStance, newStance);
		Bukkit.getServer().getPluginManager().callEvent(event);

		if (!event.isCancelled()) {
			this.stance = stance;
			ChatUtil.displayMovePreview(this.player);
		}
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

	/**
	 * Recalculate temporary elements the player has. This will remove any that have expired, as well as send
	 * the player a message about them
	 * @param wasOffline Whether the player was previously offline. E.g. they just logged in
	 */
	public void recalculateTempElements(boolean wasOffline) {
		String expired = ConfigManager.languageConfig.get().getString("Commands.Temp.Expired" + (wasOffline ? "Offline" : ""));
		String expiredAvatar = ConfigManager.languageConfig.get().getString("Commands.Temp.ExpiredAvatar" + (wasOffline ? "Offline" : ""));

		Iterator<SubElement> subIterator = this.tempSubElements.keySet().iterator();
		SubElement subElement;
		while (subIterator.hasNext() && (subElement = subIterator.next()) != null) {
			long time = tempSubElements.get(subElement);

			if (time == -1L) continue; //The subelement expiry is connected to the parent element, so skip it as it is handled bellow

			String message = expired;

			if (System.currentTimeMillis() >= time) {
				PlayerChangeSubElementEvent subEvent = new PlayerChangeSubElementEvent(null, this.player, subElement, PlayerChangeSubElementEvent.Result.TEMP_EXPIRE);
				Bukkit.getServer().getPluginManager().callEvent(subEvent);
				if (subEvent.isCancelled()) {
					continue;
				}

				ChatUtil.sendBrandingMessage(player, ChatUtil.color(ChatColor.YELLOW + message
						.replace("{element}", subElement.getColor() + subElement.getName())
						.replace("{bending}", subElement.getType().getBending())
						.replace("{bender}", subElement.getType().getBender())
						.replace("{bend}", subElement.getType().getBend())));
				subIterator.remove();
			}
		}

		Iterator<Element> elementIterator = tempElements.keySet().iterator();
		Element element;
		while (elementIterator.hasNext() && (element = elementIterator.next()) != null) {
			long time = tempElements.get(element);

			String message = expired;
			if (element == Element.AVATAR) message = expiredAvatar;

			if (System.currentTimeMillis() >= time) {
				PlayerChangeElementEvent event = new PlayerChangeElementEvent(null, this.player, element, PlayerChangeElementEvent.Result.TEMP_EXPIRE);
				Bukkit.getServer().getPluginManager().callEvent(event);
				if (event.isCancelled()) {
					continue;
				}

				ChatUtil.sendBrandingMessage(player, ChatUtil.color(ChatColor.YELLOW + message
						.replace("{element}", element.getColor() + element.getName())
						.replace("{bending}", element.getType().getBending())
						.replace("{bender}", element.getType().getBender())
						.replace("{bend}", element.getType().getBend())));
				elementIterator.remove();

				if (element == Element.AVATAR) {
					//Remove all subelements if the player loses Avatar

					Iterator<SubElement> subIterator1 = this.tempSubElements.keySet().iterator();
					SubElement s1;
					while (subIterator1.hasNext() && (s1 = subIterator1.next()) != null) {
						//Only remove if the subelement is connected to the parent element's time
						if (this.tempSubElements.get(s1) != -1L || !s1.getParentElement().isAvatarElement()) continue;

						if (!this.hasTempElement(s1.getParentElement())) {
							PlayerChangeSubElementEvent subEvent = new PlayerChangeSubElementEvent(null, this.player, s1, PlayerChangeSubElementEvent.Result.TEMP_PARENT_EXPIRE);
							Bukkit.getServer().getPluginManager().callEvent(subEvent);
							if (subEvent.isCancelled()) {
								continue;
							}
							subIterator1.remove();
						}
					}

				} else {
					//Remove all subelements if the player loses the element
					Iterator<SubElement> subIterator1 = this.tempSubElements.keySet().iterator();
					SubElement s1;
					while (subIterator1.hasNext() && (s1 = subIterator1.next()) != null) {
						if (this.tempSubElements.get(s1) != -1L) continue; //Only remove if the subelement is connected to the parent element's time

						if (!this.hasElement(s1.getParentElement())) {
							PlayerChangeSubElementEvent subEvent = new PlayerChangeSubElementEvent(null, this.player, s1, PlayerChangeSubElementEvent.Result.TEMP_PARENT_EXPIRE);
							Bukkit.getServer().getPluginManager().callEvent(subEvent);
							if (subEvent.isCancelled()) {
								continue;
							}
							subIterator1.remove();
						}
					}
				}
			}
		}

		if (this.tempElements.size() > 0 || this.tempSubElements.size() > 0) {
			Map<Element, Long> tempMap = new HashMap<>(this.tempElements);
			tempMap.putAll(this.tempSubElements);
			Optional<Long> shortestTime = tempMap.values().stream().filter(l -> l >= System.currentTimeMillis()).min(Comparator.comparingLong(Long::longValue));

			if (!shortestTime.isPresent()) {
				ProjectKorra.log.severe("Failed to find the shortest time for " + this.player.getName() + "'s temp elements!");
				this.removeUnusableAbilities();

				saveTempElements();
				return;
			}

			long shortestTimeLong = shortestTime.get();

			TEMP_ELEMENTS.removeIf(pair -> pair.getLeft().getUniqueId().equals(this.getUUID()));
			TEMP_ELEMENTS.add(new ImmutablePair<>(player, shortestTimeLong));
		}

		this.removeUnusableAbilities();

		saveTempElements();
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
			ChatUtil.sendBrandingMessage(this.player, ChatColor.YELLOW + ConfigManager.languageConfig.get().getString("Command.Toggle.Reminder"));
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
		this.recalculateTempElements(true); //Remove all temp elements that have expired and send messages about them
		PassiveManager.registerPassives(this.player);
		FirePassive.handle(player);

		//Show the bending board 1 tick later. We do it 1 tick later because postLoad() is called BEFORE the player is loaded into the map,
		//and the board needs to see the player in the map to initialize
		Bukkit.getScheduler().runTaskLater(ProjectKorra.plugin, () -> {
			BendingBoardManager.getBoard(this.player).ifPresent(BendingBoard::show);
			//Hide the board if they spawn in a world with bending disabled
			BendingBoardManager.changeWorld(this.player);
		}, 1L);
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
	 * Gives all subelements a player doesn't have when they log in.
	 */
	public void fixSubelements() {
		boolean save = false;
		for (Element element : this.elements) {
			long currentSubs = this.subelements.stream().filter(sub -> sub.getParentElement() == element).count();
			if (currentSubs > 0) continue;
			for (SubElement sub : Element.getSubElements(element)) {
				if (this.hasSubElementPermission(sub)) {
					this.addSubElement(sub);
					save = true;
				}
			}
		}

		Map<Element, Long> newSubs = new HashMap<>();

		for (Element tempElement : this.tempElements.keySet()) {
			if (tempElement instanceof SubElement) continue; //Ignore subelements, we are only fixing the parent elements' subs
			long expireTime = this.tempElements.get(tempElement);

			if (expireTime < System.currentTimeMillis()) { //If it still hasn't expired
				long currentSubs = this.tempElements.keySet().stream()
						.filter(element -> element instanceof SubElement)
						.map(element -> (SubElement)element)
						.filter(sub -> sub.getParentElement() == tempElement)
						.count();

				if (currentSubs > 0) continue;

				if (tempElement == Element.AVATAR) { //If they are avatar, add all subs to tempelements
					Set<Element> tempElements = Arrays.stream(Element.getAllElements()).filter(Element::isAvatarElement).collect(Collectors.toSet());

					for (Element element : tempElements) {
						for (SubElement sub : Element.getSubElements(element)) {
							if (this.hasSubElementPermission(sub) && !this.getSubElements().contains(sub)) {
								newSubs.put(sub, -1L); //Set the expiry to -1 to indicate that the time is linked to the parent element
								save = true;
							}
						}
					}

					continue;
				}

				for (SubElement sub : Element.getSubElements(tempElement)) {
					if (this.hasSubElementPermission(sub) && !this.hasSubElement(sub)) {
						newSubs.put(sub, -1L); //Set the expiry to -1 to indicate that the time is linked to the parent element
						save = true;
					}
				}
			}
		}

		if (save) {
			this.tempElements.putAll(newSubs); //Adds the new temp subs that should be assigned
			this.saveSubElements();
		}
	}
}

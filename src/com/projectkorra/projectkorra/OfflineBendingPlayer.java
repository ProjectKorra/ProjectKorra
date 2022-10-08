package com.projectkorra.projectkorra;

import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.command.CooldownCommand;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.event.PlayerBindChangeEvent;
import com.projectkorra.projectkorra.storage.DBConnection;
import com.projectkorra.projectkorra.util.ChatUtil;
import com.projectkorra.projectkorra.util.Cooldown;
import com.projectkorra.projectkorra.util.DBCooldownManager;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import com.projectkorra.projectkorra.Element.SubElement;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class OfflineBendingPlayer {

    /**
     * ConcurrentHashMap that contains all instances of ALL BendingPlayer, with UUID
     * key.
     */
    protected static final Map<UUID, OfflineBendingPlayer> PLAYERS = new ConcurrentHashMap<>();

    /**
     * ConcurrentHashMap that contains all instances of online BendingPlayer, with UUID
     * key.
     */
    protected static final Map<UUID, BendingPlayer> ONLINE_PLAYERS = new ConcurrentHashMap<>();

    protected final OfflinePlayer player;
    protected final UUID uuid;
    protected boolean permaRemoved;
    protected boolean toggled;
    protected boolean allPassivesToggled;
    protected boolean loading;

    protected final List<Element> elements = new ArrayList<>();
    protected final List<SubElement> subelements = new ArrayList<>();
    protected HashMap<Integer, String> abilities = new HashMap<>();
    protected final Map<String, Cooldown> cooldowns = new HashMap<>();
    protected final Set<Element> toggledElements = new HashSet<>();
    protected final Set<Element> toggledPassives = new HashSet<>();
    protected final DBCooldownManager cooldownManager;

    private int currentSlot;
    private long lastAccessed;
    private long uncacheTime = 30_000;
    private BukkitTask uncache;

    public OfflineBendingPlayer(@NotNull OfflinePlayer player) {
        this.player = player;
        this.uuid = player.getUniqueId();
        this.toggled = true;
        this.allPassivesToggled = true;
        this.loading = true;

        this.cooldownManager = Manager.getManager(DBCooldownManager.class);
        this.lastAccessed = System.currentTimeMillis();
    }

    public OfflineBendingPlayer(@NotNull UUID playerUUID) {
        this(Bukkit.getOfflinePlayer(playerUUID));
    }

    protected static CompletableFuture<OfflineBendingPlayer> loadAsync(@NotNull final UUID uuid, boolean onStartup) {
        CompletableFuture<OfflineBendingPlayer> future = new CompletableFuture<>();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);

        //If we already have the players data cached from an OfflineBendingPlayer instance
        if (PLAYERS.get(uuid) != null) {
            OfflineBendingPlayer oBendingPlayer = PLAYERS.get(uuid); //Get cached instance
            if (offlinePlayer.isOnline() && !(oBendingPlayer instanceof BendingPlayer)) {
                oBendingPlayer = convertToOnline(oBendingPlayer); //Convert to online instance
                ((BendingPlayer)oBendingPlayer).postLoad();
            }
            if (!(oBendingPlayer instanceof BendingPlayer)) {
                oBendingPlayer.lastAccessed = System.currentTimeMillis();
            }
            future.complete(oBendingPlayer);
            return future;
        }

        Runnable runnable = () -> {
            OfflineBendingPlayer bPlayer = new OfflineBendingPlayer(offlinePlayer);
            if (offlinePlayer.isOnline()) {
                bPlayer = new BendingPlayer(((Player)offlinePlayer));
                ONLINE_PLAYERS.put(uuid, (BendingPlayer)bPlayer);
            }

            PLAYERS.put(uuid, bPlayer);

            final ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM pk_players WHERE uuid = '" + uuid.toString() + "'");
            try {
                if (!rs2.next()) { // Data doesn't exist, we want a completely new player.
                    DBConnection.sql.modifyQuery("INSERT INTO pk_players (uuid, player, slot1, slot2, slot3, slot4, slot5, slot6, slot7, slot8, slot9) VALUES ('" + uuid.toString() + "', '" + offlinePlayer.getName() + "', 'null', 'null', 'null', 'null', 'null', 'null', 'null', 'null', 'null')");
                    Bukkit.getScheduler().runTask(ProjectKorra.plugin, () -> ProjectKorra.log.info("Created new BendingPlayer for " + offlinePlayer.getName()));
                    OfflineBendingPlayer newPlayer;
                    if (offlinePlayer.isOnline()) {
                        newPlayer = new BendingPlayer((Player)offlinePlayer);
                        //Call postLoad() on the main thread and wait for it to complete
                        Bukkit.getScheduler().callSyncMethod(ProjectKorra.plugin, () -> {
                            ((BendingPlayer)newPlayer).postLoad();
                            return true;
                        }).get();
                        ONLINE_PLAYERS.put(uuid, (BendingPlayer) newPlayer);
                    } else {
                        newPlayer = new OfflineBendingPlayer(offlinePlayer);
                    }
                    PLAYERS.put(uuid, newPlayer);
                    future.complete(newPlayer);
                } else {
                    // The player has at least played before.
                    final String player2 = rs2.getString("player");
                    if (!offlinePlayer.getName().equalsIgnoreCase(player2)) {
                        DBConnection.sql.modifyQuery("UPDATE pk_players SET player = '" + offlinePlayer.getName() + "' WHERE uuid = '" + uuid.toString() + "'");
                        // They have changed names.
                        ProjectKorra.log.info("Updating Player Name for " + offlinePlayer.getName());
                    }
                    final String subelementField = rs2.getString("subelement");
                    final String elementField = rs2.getString("element");
                    final String permaremovedField = rs2.getString("permaremoved");

                    //Load the elements
                    if (elementField != null && !elementField.equalsIgnoreCase("NULL")) {
                        final boolean hasAddon = elementField.contains(";");
                        final String[] split = elementField.split(";");
                        if (split.length > 0 && !split[0].equals("")) { // Player has an element.
                            if (split[0].contains("a")) {
                                bPlayer.elements.add(Element.AIR);
                            }
                            if (split[0].contains("w")) {
                                bPlayer.elements.add(Element.WATER);
                            }
                            if (split[0].contains("e")) {
                                bPlayer.elements.add(Element.EARTH);
                            }
                            if (split[0].contains("f")) {
                                bPlayer.elements.add(Element.FIRE);
                            }
                            if (split[0].contains("c")) {
                                bPlayer.elements.add(Element.CHI);
                            }
                        }
                        if (hasAddon) {
                            /*
                             * Because plugins which depend on ProjectKorra
                             * would be loaded after ProjectKorra, addon
                             * elements would = null. To work around this, we
                             * keep trying to load in the elements from the
                             * database until it successfully loads everything
                             * in, or it times out.
                             */
                            final CopyOnWriteArrayList<String> addonClone = new CopyOnWriteArrayList<>(Arrays.asList(split[split.length - 1].split(",")));
                            final long startTime = System.currentTimeMillis();
                            final long timeoutLength = 5_000; // How long until it should time out attempting to load addons in.
                            OfflineBendingPlayer finalBPlayer = bPlayer;
                            Predicate<List<String>> func = (elements) -> {
                                if (System.currentTimeMillis() - startTime > timeoutLength) {
                                    ProjectKorra.log.severe("ProjectKorra has timed out after attempting to load in the following addon elements: " + addonClone.toString());
                                    ProjectKorra.log.severe("These elements have taken too long to load in, resulting in users having lost these element.");
                                    return true;
                                } else {
                                    ProjectKorra.log.info("Attempting to load in the following addon elements... " + elements.toString());
                                    for (final String addon : elements) {
                                        if (Element.getElement(addon) != null) {
                                            finalBPlayer.elements.add(Element.getElement(addon));
                                            elements.remove(addon);
                                        }
                                    }
                                    if (elements.isEmpty()) {
                                        ProjectKorra.log.info("Successfully loaded in all addon elements!");
                                        return true;
                                    }
                                }
                                return false;
                            };

                            if (onStartup) { //If we are doing this on startup, addon elements aren't loaded yet. So do this async
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        if (func.test(addonClone)) {
                                            this.cancel();
                                        }
                                    }
                                }.runTaskTimer(ProjectKorra.plugin, 0, 5);
                            } else func.test(addonClone); //Addon elements should be loaded so
                        }
                    }

                    //Load subelements
                    if (subelementField != null && !subelementField.equalsIgnoreCase("NULL")) {
                        final boolean hasAddon = subelementField.contains(";");
                        final String[] split = subelementField.split(";");

                        //If the subelements aren't defined, we give them now
                        if (subelementField.equals("-")) {
                            boolean shouldSave = false;
                            if (offlinePlayer instanceof Player) { //Only if the player is online though
                                subloop:
                                for (final SubElement sub : Element.getAllSubElements()) {
                                    if (sub instanceof Element.MultiSubElement) { //If it's a multisub, check if they have any of the parent element and perm for the sub of that parent
                                        for (Element parent : ((Element.MultiSubElement) sub).getParentElements()) {
                                            if (((Player) offlinePlayer).hasPermission("bending." + parent.getName() + "." + sub.getName() + sub.getType().getBending()) && bPlayer.elements.contains(sub.getParentElement())) {
                                                bPlayer.subelements.add(sub);
                                                continue subloop;
                                            }
                                        }
                                    } else if (((Player)offlinePlayer).hasPermission("bending." + sub.getParentElement().getName().toLowerCase() + "." + sub.getName().toLowerCase() + sub.getType().getBending())
                                            && bPlayer.elements.contains(sub.getParentElement())) {
                                        bPlayer.subelements.add(sub);
                                        shouldSave = true;
                                    }
                                }
                                if (shouldSave) bPlayer.saveSubElements();
                            }
                        } else if (split.length > 0 && !split[0].equals("")) {
                            if (split[0].contains("m")) {
                                bPlayer.subelements.add(Element.METAL);
                            }
                            if (split[0].contains("v")) {
                                bPlayer.subelements.add(Element.LAVA);
                            }
                            if (split[0].contains("s")) {
                                bPlayer.subelements.add(Element.SAND);
                            }
                            if (split[0].contains("c")) {
                                bPlayer.subelements.add(Element.COMBUSTION);
                            }
                            if (split[0].contains("l")) {
                                bPlayer.subelements.add(Element.LIGHTNING);
                            }
                            if (split[0].contains("t")) {
                                bPlayer.subelements.add(Element.SPIRITUAL);
                            }
                            if (split[0].contains("f")) {
                                bPlayer.subelements.add(Element.FLIGHT);
                            }
                            if (split[0].contains("i")) {
                                bPlayer.subelements.add(Element.ICE);
                            }
                            if (split[0].contains("h")) {
                                bPlayer.subelements.add(Element.HEALING);
                            }
                            if (split[0].contains("b")) {
                                bPlayer.subelements.add(Element.BLOOD);
                            }
                            if (split[0].contains("p")) {
                                bPlayer.subelements.add(Element.PLANT);
                            }
                            if (split[0].contains("r")) {
                                bPlayer.subelements.add(Element.BLUE_FIRE);
                            }
                        }
                        if (hasAddon) {
                            final CopyOnWriteArrayList<String> addonClone = new CopyOnWriteArrayList<String>(Arrays.asList(split[split.length - 1].split(",")));
                            final long startTime = System.currentTimeMillis();
                            final long timeoutLength = 5_000; // How long until it should time out attempting to load addons in.
                            OfflineBendingPlayer finalBPlayer1 = bPlayer;
                            Predicate<List<String>> func = (elements) -> {
                                if (System.currentTimeMillis() - startTime > timeoutLength) {
                                    ProjectKorra.log.severe("ProjectKorra has timed out after attempting to load in the following addon subelements: " + addonClone.toString());
                                    ProjectKorra.log.severe("These subelements have taken too long to load in, resulting in users having lost these subelement.");
                                    return true;
                                } else {
                                    ProjectKorra.log.info("Attempting to load in the following addon subelements... " + elements.toString());
                                    for (final String addon : elements) {
                                        if (Element.getElement(addon) != null && Element.getElement(addon) instanceof SubElement) {
                                            finalBPlayer1.subelements.add((SubElement) Element.getElement(addon));
                                            elements.remove(addon);
                                        }
                                    }

                                    if (elements.isEmpty()) {
                                        ProjectKorra.log.info("Successfully loaded in all addon subelements!");
                                        return true;
                                    }
                                    return false;
                                }
                            };
                            if (onStartup) { //If we are doing this on startup, addon elements aren't loaded yet. So do this async
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        if (func.test(addonClone)) {
                                            this.cancel();
                                        }
                                    }
                                }.runTaskTimer(ProjectKorra.plugin, 0, 5);
                            } else func.test(addonClone); //Addon elements should be loaded by now
                        }
                    }

                    //Load the abilities
                    final ConcurrentHashMap<Integer, String> abilitiesClone = new ConcurrentHashMap<>();
                    for (int i = 1; i <= 9; i++) {
                        final String ability = rs2.getString("slot" + i);
                        abilitiesClone.put(i, ability);
                    }
                    final long startTime = System.currentTimeMillis();
                    final long timeoutLength = 5_000; // How long until it should time out attempting to load addons in.
                    OfflineBendingPlayer finalBPlayer2 = bPlayer;
                    Predicate<Map<Integer, String>> func = (abils) -> {
                        if (System.currentTimeMillis() - startTime > timeoutLength) {
                            ProjectKorra.log.severe("ProjectKorra has timed out after attempting to load in the following abilities: " + abilitiesClone.toString());
                            ProjectKorra.log.severe("These abilities have taken too long to load in, resulting in users having lost these abilities.");
                            return true;
                        } else {
                            for (final Map.Entry<Integer, String> set : abils.entrySet()) {
                                if (set.getValue() == null || set.getValue().equalsIgnoreCase("null")) {
                                    abils.remove(set.getKey());
                                } else if (CoreAbility.getAbility(set.getValue()) != null && CoreAbility.getAbility(set.getValue()).isEnabled()) {
                                    finalBPlayer2.abilities.put(set.getKey(), set.getValue());
                                    abils.remove(set.getKey());
                                }
                            }

                            if (abils.isEmpty()) {
                                ProjectKorra.log.info("Successfully loaded in all abilities!");
                                return true;
                            }
                            return false;
                        }
                    };
                    if (onStartup) { //If we are doing this on startup, addon elements aren't loaded yet. So do this async
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (func.test(abilitiesClone)) {
                                    this.cancel();
                                }
                            }
                        }.runTaskTimer(ProjectKorra.plugin, 0, 5);
                    } else func.test(abilitiesClone); //Addon elements should be loaded by now

                    //Load permaRemove
                    if (permaremovedField != null && permaremovedField.equalsIgnoreCase("true")) bPlayer.permaRemoved = true;

                    //Load cooldowns
                    if (ProjectKorra.isDatabaseCooldownsEnabled()) {
                        try (ResultSet rs = DBConnection.sql.readQuery("SELECT * FROM pk_cooldowns WHERE uuid = '" + uuid.toString() + "'")) {
                            while (rs.next()) {
                                final int cooldownId = rs.getInt("cooldown_id");
                                final long value = rs.getLong("value");
                                final String name = bPlayer.cooldownManager.getCooldownName(cooldownId);
                                bPlayer.cooldowns.put(name, new Cooldown(value, true));
                            }
                        } catch (final SQLException e) {
                            e.printStackTrace();
                        }
                    }

                    bPlayer.loading = false;
                    //Call postLoad() on the main thread and wait for it to complete
                    if (bPlayer instanceof BendingPlayer) {
                        BendingPlayer finalBPlayer3 = (BendingPlayer) bPlayer;
                        Bukkit.getScheduler().callSyncMethod(ProjectKorra.plugin, () -> {
                            finalBPlayer3.postLoad();
                            return true;
                        }).get();
                    } else {
                        bPlayer.uncacheAfter(30_000);
                    }

                    future.complete(bPlayer);
                }
            } catch (final SQLException | ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
                future.cancel(true);
            }
        };

        if (!Bukkit.isPrimaryThread()) runnable.run();
        else Bukkit.getScheduler().runTaskAsynchronously(ProjectKorra.plugin, runnable);

        return future;
    }

    /**
     * Saves the subelements of a BendingPlayer to the database.
     */
    public void saveSubElements() {
        final StringBuilder subs = new StringBuilder();
        if (this.hasSubElement(Element.METAL)) {
            subs.append("m");
        }
        if (this.hasSubElement(Element.LAVA)) {
            subs.append("v");
        }
        if (this.hasSubElement(Element.SAND)) {
            subs.append("s");
        }
        if (this.hasSubElement(Element.COMBUSTION)) {
            subs.append("c");
        }
        if (this.hasSubElement(Element.LIGHTNING)) {
            subs.append("l");
        }
        if (this.hasSubElement(Element.SPIRITUAL)) {
            subs.append("t");
        }
        if (this.hasSubElement(Element.FLIGHT)) {
            subs.append("f");
        }
        if (this.hasSubElement(Element.ICE)) {
            subs.append("i");
        }
        if (this.hasSubElement(Element.HEALING)) {
            subs.append("h");
        }
        if (this.hasSubElement(Element.BLOOD)) {
            subs.append("b");
        }
        if (this.hasSubElement(Element.PLANT)) {
            subs.append("p");
        }
        if (this.hasSubElement(Element.BLUE_FIRE)) {
            subs.append("r");
        }
        boolean hasAddon = false;
        List<SubElement> addonSubs = Arrays.asList(Element.getAddonSubElements());
        for (final Element element : this.getSubElements()) {
            if (addonSubs.contains(element)) {
                if (!hasAddon) {
                    hasAddon = true;
                    subs.append(";");
                }
                subs.append(element.getName() + ",");
            }
        }

        if (subs.length() == 0) {
            subs.append("NULL");
        }

        DBConnection.sql.modifyQuery("UPDATE pk_players SET subelement = '" + subs.toString() + "' WHERE uuid = '" + uuid + "'");
    }

    /**
     * Saves the elements of a BendingPlayer to the database.
     */
    public void saveElements() {
        final StringBuilder elements = new StringBuilder();
        if (this.hasElement(Element.AIR)) {
            elements.append("a");
        }
        if (this.hasElement(Element.WATER)) {
            elements.append("w");
        }
        if (this.hasElement(Element.EARTH)) {
            elements.append("e");
        }
        if (this.hasElement(Element.FIRE)) {
            elements.append("f");
        }
        if (this.hasElement(Element.CHI)) {
            elements.append("c");
        }
        boolean hasAddon = false;
        List<Element> addonElements = Arrays.asList(Element.getAddonElements());
        for (final Element element : this.getElements()) {
            if (addonElements.contains(element)) {
                if (!hasAddon) {
                    hasAddon = true;
                    elements.append(";");
                }
                elements.append(element.getName() + ",");
            }
        }

        if (elements.length() == 0) {
            elements.append("NULL");
        }

        DBConnection.sql.modifyQuery("UPDATE pk_players SET element = '" + elements.toString() + "' WHERE uuid = '" + uuid + "'");
    }

    /**
     * Binds an ability to the hotbar slot that the player is on.
     *
     * @param ability The ability name to bind
     * @see #bindAbility(String, int)
     */
    public void bindAbility(final String ability) {
        bindAbility(ability, getCurrentSlot() + 1);
    }

    /**
     * Binds a Ability to a specific hotbar slot.
     *
     * @param ability The ability name to bind
     * @param slot The slot to bind on
     * @see #bindAbility(String)
     */
    public void bindAbility(final String ability, final int slot) {
        boolean realPlayer = this instanceof BendingPlayer;
        if (realPlayer && MultiAbilityManager.playerAbilities.containsKey((Player)this.getPlayer())) {
            ChatUtil.sendBrandingMessage((Player)this.getPlayer(), ChatColor.RED + ConfigManager.languageConfig.get().getString("Commands.Bind.CantEditBinds"));
            return;
        }

        final CoreAbility coreAbil = CoreAbility.getAbility(ability);
        if (coreAbil == null) return;
        final String fixedName = coreAbil.getName();

        if (realPlayer) {
            PlayerBindChangeEvent event = new PlayerBindChangeEvent((Player)this.getPlayer(), fixedName, slot, ability != null, false);
            ProjectKorra.plugin.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
        }


        this.getAbilities().put(slot, fixedName);

        if (realPlayer) {
            ChatUtil.sendBrandingMessage((Player)this.getPlayer(), coreAbil.getElement().getColor() + ConfigManager.languageConfig.get().getString("Commands.Bind.SuccessfullyBound").replace("{ability}", fixedName).replace("{slot}", String.valueOf(slot)));
        }

        this.saveAbility(fixedName, slot);
    }

    /**
     * Save the bound ability in the slot to the database
     * @param ability The ability to save
     * @param slot The slot we are saving
     */
    public void saveAbility(final String ability, final int slot) {
        // Temp code to block modifications of binds, Should be replaced when bind event is added.
        if (this instanceof BendingPlayer && MultiAbilityManager.playerAbilities.containsKey((Player)this.getPlayer())) {
            return;
        }

        DBConnection.sql.modifyQuery("UPDATE pk_players SET slot" + slot + " = '" + (this.abilities.get(slot) == null ? null : abilities.get(slot)) + "' WHERE uuid = '" + uuid + "'");
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
     * Gets the name of the {@link BendingPlayer}.
     *
     * @return the player name
     */
    public String getName() {
        return this.player.getName();
    }

    /**
     * Gets the Ability bound to the slot that the player is in.
     *
     * @return The Ability name bounded to the slot
     */
    public String getBoundAbilityName() {
        final int slot = getCurrentSlot() + 1;
        final String name = this.getAbilities().get(slot);

        return name != null ? name : "";
    }

    public int getCurrentSlot() {
        return this.currentSlot;
    }

    /**
     * Sets the currently held slot
     * @param slot The slot number
     */
    public void setCurrentSlot(int slot) {
        this.currentSlot = slot;
    }

    /**
     * Gets the map of abilities that the {@link BendingPlayer} knows.
     *
     * @return map of abilities
     */
    public HashMap<Integer, String> getAbilities() {
        return this.abilities;
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
     * Checks to see if a player can bend a specific sub element. Used when
     * checking addon sub elements.
     *
     * @param sub SubElement to check for.
     * @return true If the player has permission to bend that subelement.
     */
    public boolean canUseSubElement(final SubElement sub) {
        return this.subelements.contains(sub);
    }

    public CoreAbility getBoundAbility() {
        return CoreAbility.getAbility(this.getBoundAbilityName());
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
     * Removes the cooldown of an ability.
     *
     * @param ability The ability's cooldown to remove
     */
    public void removeCooldown(final String ability) {
        this.cooldowns.remove(ability);
    }

    /**
     * Removes the cooldown of an ability
     * @param ability The ability whose cooldown to remove
     */
    public void removeCooldown(@NotNull final CoreAbility ability) {
        this.removeCooldown(ability.getName());
    }

    /**
     * Remove all cooldowns that have expired
     */
    protected void removeOldCooldowns() {
        this.cooldowns.entrySet().removeIf(entry -> System.currentTimeMillis() >= entry.getValue().getCooldown());
    }

    /**
     * Gets the map of cooldowns of the {@link BendingPlayer}.
     *
     * @return map of cooldowns
     */
    public Map<String, Cooldown> getCooldowns() {
        return this.cooldowns;
    }

    public boolean isOnCooldown(@NotNull final Ability ability) {
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
            return true;
        }

        return false;
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
     * Applies a cooldown for an ability to the current BendingPlayer.
     * @param ability The ability to apply the cooldown to
     * @param cooldown The cooldown time
     * @param database Whether or not to save the cooldown to the database
     */
    public void addCooldown(final String ability, final long cooldown, final boolean database) {
        if (cooldown <= 0) {
            return;
        }

        this.cooldowns.put(ability, new Cooldown(cooldown + System.currentTimeMillis(), database));

        CooldownCommand.addCooldownType(ability);
    }

    //TODO Rewrite cooldowns with the ID system
    private void saveCooldownsForce() {
        DBConnection.sql.modifyQuery("DELETE FROM pk_cooldowns WHERE uuid = '" + this.uuid.toString() + "'", false);
        for (final Map.Entry<String, Cooldown> entry : this.cooldowns.entrySet()) {
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
     * @return Returns true if this BendingPlayer is fully loaded
     */
    public boolean isLoaded() {
        return !this.loading;
    }

    /**
     * Checks to see if the {@link BendingPlayer} knows a specific element.
     *
     * @param element The element to check
     * @return true If the player knows the element
     */
    public boolean hasElement(@NotNull final Element element) {
        if (element == Element.AVATAR) {
            // At the moment we'll allow for both permissions to return true.
            // Later on we can consider deleting the bending.ability.avatarstate option.
            return this.player instanceof Player && ((Player)this.player).hasPermission("bending.avatar");
        } else if (!(element instanceof SubElement)) {
            return this.elements.contains(element);
        } else {
            return this.hasSubElement((SubElement) element);
        }
    }

    /**
     * Checks to see if the {@link BendingPlayer} has a specific subelement.
     *
     * @param sub The subelement to check
     * @return true If the player knows the element
     */
    public boolean hasSubElement(@NotNull final SubElement sub) {
        return this.subelements.contains(sub);
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
     * Checks if the {@link BendingPlayer} has the specified element toggled on
     *
     * @param element The element to check
     * @return true if the element is toggled on
     */
    public boolean isElementToggled(final Element element) {
        return !this.toggledElements.contains(element);
    }

    /**
     * Checks if the {@link BendingPlayer} has the specified element's passives toggled on
     *
     * @param element The element to check
     * @return true if the element's passives are toggled on
     */
    public boolean isPassiveToggled(final Element element) {
        return !this.toggledPassives.contains(element);
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
     * Checks if the {@link BendingPlayer} has bending passives toggled on.
     *
     * @return true If passives are toggled on
     */
    public boolean isToggledPassives() {
        return this.allPassivesToggled;
    }

    /**
     * Sets the {@link BendingPlayer}'s abilities. This method also saves the
     * abilities to the database.
     *
     * @param abilities The abilities to set/save
     */
    public void setAbilities(@NotNull final HashMap<Integer, String> abilities) {
        if (this.abilities.equals(abilities)) return;

        this.abilities = abilities;

        for (int i = 1; i <= 9; i++) {
            DBConnection.sql.modifyQuery("UPDATE pk_players SET slot" + i + " = '" + abilities.get(i) + "' WHERE uuid = '" + this.uuid + "'");
        }
    }

    /**
     * Sets the {@link BendingPlayer}'s element. If the player had elements
     * before they will be overwritten.
     *
     * @param element The element to set
     */
    public void setElement(@NotNull final Element element) {
        this.elements.clear();
        this.elements.add(element);
    }

    /**
     * Sets the permanent removed state of the {@link BendingPlayer}.
     *
     * @param permaRemoved If they should be permaremoved
     */
    public void setPermaRemoved(final boolean permaRemoved) {
        this.permaRemoved = permaRemoved;
        DBConnection.sql.modifyQuery("UPDATE pk_players SET permaremoved = '" + (permaRemoved ? "true" : "false") + "' WHERE uuid = '" + uuid + "'");
    }

    public void toggleBending() {
        this.toggled = !this.toggled;
    }

    public void toggleAllPassives() {
        this.allPassivesToggled = !this.allPassivesToggled;
    }

    public void toggleElement(@NotNull final Element element) {
        if (this.toggledElements.contains(element)) {
            this.toggledElements.remove(element);
        } else {
            this.toggledElements.add(element);
        }
    }

    public void togglePassive(@NotNull final Element element) {
        if (this.toggledPassives.contains(element)) {
            this.toggledPassives.remove(element);
        } else {
            this.toggledPassives.add(element);
        }
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
        return false;
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
     * @return true If player has permission node "bending.earth.metalbending"
     */
    public boolean canMetalbend() {
        return this.subelements.contains(Element.METAL);
    }

    /**
     * Checks to see if a player can PlantBend.
     *
     * @return true If player has permission node "bending.ability.plantbending"
     */
    public boolean canPlantbend() {
        return this.subelements.contains(Element.PLANT);
    }

    /**
     * Checks to see if a player can SandBend.
     *
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

    public OfflinePlayer getPlayer() {
        return this.player;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    protected static BendingPlayer convertToOnline(@NotNull OfflineBendingPlayer offlineBendingPlayer) {
        Player player = Bukkit.getPlayer(offlineBendingPlayer.getUUID());
        if (player == null) {
            return null;
        }
        BendingPlayer bendingPlayer = new BendingPlayer(player);
        bendingPlayer.abilities = offlineBendingPlayer.abilities;
        bendingPlayer.elements.addAll(offlineBendingPlayer.elements);
        bendingPlayer.subelements.addAll(offlineBendingPlayer.subelements);
        bendingPlayer.toggledElements.addAll(offlineBendingPlayer.toggledElements);
        bendingPlayer.toggledPassives.addAll(offlineBendingPlayer.toggledPassives);
        bendingPlayer.toggled = offlineBendingPlayer.toggled;
        bendingPlayer.allPassivesToggled = offlineBendingPlayer.allPassivesToggled;
        bendingPlayer.permaRemoved = offlineBendingPlayer.permaRemoved;
        bendingPlayer.cooldowns.putAll(offlineBendingPlayer.cooldowns);
        bendingPlayer.loading = false;

        if (offlineBendingPlayer.uncache != null) {
            offlineBendingPlayer.uncache.cancel();
        }

        PLAYERS.put(player.getUniqueId(), bendingPlayer);
        ONLINE_PLAYERS.put(player.getUniqueId(), bendingPlayer);

        return bendingPlayer;
    }

    protected static OfflineBendingPlayer convertToOffline(@NotNull BendingPlayer bendingPlayer) {
        if (bendingPlayer.getPlayer() != null && bendingPlayer.getPlayer().isOnline()) return bendingPlayer;

        OfflineBendingPlayer offlineBendingPlayer = new OfflineBendingPlayer(bendingPlayer.getPlayer());
        offlineBendingPlayer.abilities = bendingPlayer.abilities;
        offlineBendingPlayer.elements.addAll(bendingPlayer.elements);
        offlineBendingPlayer.subelements.addAll(bendingPlayer.subelements);
        offlineBendingPlayer.toggledElements.addAll(bendingPlayer.toggledElements);
        offlineBendingPlayer.toggledPassives.addAll(bendingPlayer.toggledPassives);
        offlineBendingPlayer.toggled = bendingPlayer.toggled;
        offlineBendingPlayer.allPassivesToggled = bendingPlayer.allPassivesToggled;
        offlineBendingPlayer.permaRemoved = bendingPlayer.permaRemoved;
        offlineBendingPlayer.cooldowns.putAll(bendingPlayer.cooldowns);
        offlineBendingPlayer.loading = false;
        offlineBendingPlayer.lastAccessed = System.currentTimeMillis();

        if (bendingPlayer.getPlayer() == null || !bendingPlayer.getPlayer().isOnline()) ONLINE_PLAYERS.remove(bendingPlayer.getUUID());
        PLAYERS.put(bendingPlayer.getUUID(), offlineBendingPlayer);

        return offlineBendingPlayer;
    }

    /**
     * Uncaches this instance of an Offline BendingPlayer.
     */
    public void uncache() {
        if (this.player.isOnline() || this instanceof BendingPlayer) return;

        long remaining = (this.lastAccessed + this.uncacheTime) - System.currentTimeMillis();

        if (remaining >= 500) { //If there is at least half a second to go, delay the uncache
            if (this.uncache != null) this.uncache.cancel(); //Cancel existing task
            this.uncache = Bukkit.getScheduler().runTaskLater(ProjectKorra.plugin, this::uncache, remaining / 50);
            return;
        }

        PLAYERS.remove(this.player.getUniqueId());
        ONLINE_PLAYERS.remove(this.player.getUniqueId());
    }

    /**
     * Uncaches this instance of an offline BendingPlayer
     * @param time The amount of milliseconds to wait before uncaching
     */
    public void uncacheAfter(long time) {
        this.uncacheTime = time;
        this.lastAccessed = System.currentTimeMillis();
        uncache();
    }
}

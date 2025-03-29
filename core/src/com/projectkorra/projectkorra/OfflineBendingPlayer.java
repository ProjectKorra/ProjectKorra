package com.projectkorra.projectkorra;

import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.command.CooldownCommand;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.event.BendingPlayerLoadEvent;
import com.projectkorra.projectkorra.event.PlayerBindChangeEvent;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent;
import com.projectkorra.projectkorra.event.PlayerChangeSubElementEvent;
import com.projectkorra.projectkorra.storage.DBConnection;
import com.projectkorra.projectkorra.util.ChatUtil;
import com.projectkorra.projectkorra.util.Cooldown;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import com.projectkorra.projectkorra.Element.SubElement;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    /**
     * Queue of all the temporary elements, sorted by expiry time. Only for online players
     */
    protected static final PriorityQueue<Pair<Player, Long>> TEMP_ELEMENTS = new PriorityQueue(Comparator.comparingLong(Pair<Player, Long>::getRight));

    /**
     * Map of all the players that are currently loading
     */
    private static final Map<UUID, CompletableFuture<OfflineBendingPlayer>> LOADING = new ConcurrentHashMap<>();

    protected final OfflinePlayer player;
    protected final UUID uuid;
    protected boolean permaRemoved;
    protected boolean toggled;
    protected boolean allPassivesToggled;
    protected boolean loading;

    protected final List<Element> elements = new ArrayList<>();
    protected final List<SubElement> subelements = new ArrayList<>();
    protected Map<Element, Long> tempElements = new HashMap<>();
    protected Map<SubElement, Long> tempSubElements = new HashMap<>();
    protected HashMap<Integer, String> abilities = new HashMap<>();
    protected final Map<String, Cooldown> cooldowns = new HashMap<>();
    protected final Set<Element> toggledElements = new HashSet<>();
    protected final Set<Element> toggledPassives = new HashSet<>();



    private int currentSlot;
    private long lastAccessed;
    private long uncacheTime = 30_000; //This is the default time to unload after when the data is accessed by code, NOT when logging out
    private BukkitTask uncache;

    public OfflineBendingPlayer(@NotNull OfflinePlayer player) {
        this.player = player;
        this.uuid = player.getUniqueId();
        this.toggled = true;
        this.allPassivesToggled = true;
        this.loading = true;

        this.lastAccessed = System.currentTimeMillis();
    }

    public OfflineBendingPlayer(@NotNull UUID playerUUID) {
        this(Bukkit.getOfflinePlayer(playerUUID));
    }

    protected static CompletableFuture<OfflineBendingPlayer> loadAsync(@NotNull final UUID uuid, boolean onStartup) {
        CompletableFuture<OfflineBendingPlayer> future = new CompletableFuture<>();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);

        if (LOADING.containsKey(uuid)) { //If it is already loading, return the loading one
            return LOADING.get(uuid);
        }

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

        LOADING.put(uuid, future); //Put the future in the loading map

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
                    Bukkit.getScheduler().callSyncMethod(ProjectKorra.plugin, ()
                            -> {
                        Bukkit.getPluginManager().callEvent(new BendingPlayerLoadEvent(newPlayer));
                        return true;
                    });
                    future.complete(newPlayer);
                    LOADING.remove(uuid);
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
                                final String name = rs.getString("cooldown");
                                final long value = rs.getLong("value");
                                bPlayer.cooldowns.put(name, new Cooldown(value, true));
                            }
                        } catch (final SQLException e) {
                            e.printStackTrace();
                        }
                    }

                    //Load tempelements from the database
                   try (ResultSet rs3 = DBConnection.sql.readQuery("SELECT * FROM pk_temp_elements WHERE uuid = '" + uuid.toString() + "'")) {
                       Map<Element, Long> elements = new HashMap<>();
                       Map<SubElement, Long> subElements = new HashMap<>();

                       while (rs3.next()) {
                            Element element = Element.getElement(rs3.getString("element"));
                            long time = rs3.getLong("expiry");

                            if (element instanceof SubElement) subElements.put((SubElement) element, time);
                            else elements.put(element, time);
                       }

                       bPlayer.tempElements = elements;
                       bPlayer.tempSubElements = subElements;
                   } catch (SQLException e) {
                       e.printStackTrace();
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

                    OfflineBendingPlayer finalBPlayer4 = bPlayer;
                    Bukkit.getScheduler().runTask(ProjectKorra.plugin, () -> {
                        Bukkit.getPluginManager().callEvent(new BendingPlayerLoadEvent(finalBPlayer4));
                        LOADING.remove(uuid);
                        future.complete(finalBPlayer4);
                    });
                }
            } catch (final SQLException | ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
                LOADING.remove(uuid);
                future.cancel(true);
            }
        };

        Bukkit.getScheduler().runTaskAsynchronously(ProjectKorra.plugin, runnable);

        return future;
    }

    /**
     * Saves the subelements of a BendingPlayer to the database.
     */
    public void saveSubElements() {
        Bukkit.getScheduler().runTaskLater(ProjectKorra.plugin, () -> {
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
        }, 1L);
    }

    /**
     * Saves the elements of a BendingPlayer to the database.
     */
    public void saveElements() {
        Bukkit.getScheduler().runTaskLater(ProjectKorra.plugin, () -> {
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
        }, 1L);
    }

    /**
     * Saves all temporary elements to the database
     */
    public void saveTempElements() {
        Bukkit.getScheduler().runTaskLater(ProjectKorra.plugin, () -> {

            try {
                DBConnection.sql.getConnection().setAutoCommit(false);
                DBConnection.sql.modifyQuery("DELETE FROM pk_temp_elements WHERE uuid = '" + uuid + "'");
                DBConnection.sql.getConnection().commit(); //Force the delete statement to go through before the next SQL statement
                for (Element e : this.tempElements.keySet()) {
                    DBConnection.sql.modifyQuery("INSERT INTO pk_temp_elements (uuid, element, expiry) VALUES ('" + uuid + "', '" + e.getName() + "', " + this.tempElements.get(e) + ")");
                }
                for (Element e : this.tempSubElements.keySet()) {
                    DBConnection.sql.modifyQuery("INSERT INTO pk_temp_elements (uuid, element, expiry) VALUES ('" + uuid + "', '" + e.getName() + "', " + this.tempSubElements.get(e) + ")");
                }
                DBConnection.sql.getConnection().commit(); //Force the delete statement to go through before the next SQL statement
                DBConnection.sql.getConnection().setAutoCommit(true);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }


        }, 1L);
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
     * Binds an Ability to a specific hotbar slot.
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

        PlayerBindChangeEvent event = new PlayerBindChangeEvent(this.getPlayer(), fixedName, slot, ability != null, false);
        ProjectKorra.plugin.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
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
     * Get the list of temporary elements and subelements the {@link BendingPlayer} has.
     *
     * @return a map of temporary elements and subelements
     */
    public Map<Element, Long> getTempElements() {
        return this.tempElements;
    }

    /**
     * Get the list of temporary subelements the {@link BendingPlayer} has.
     *
     * Temporary subelements are a bit more confusing than elements. If a subelement's expirary
     * is set to -1, it means it is linked with the temporary parent element. When that parent
     * element is removed, the sub should be removed as well.
     *
     * @return a map of temporary subelements
     */
    public Map<SubElement, Long> getTempSubElements() {
        return this.tempSubElements;
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

    /**
     * Commits cooldowns to the database
     */
    private void saveCooldownsForce() {
        DBConnection.sql.modifyQuery("DELETE FROM pk_cooldowns WHERE uuid = '" + this.uuid.toString() + "'", false);
        for (final Map.Entry<String, Cooldown> entry : this.cooldowns.entrySet()) {
            final String name = entry.getKey();
            final Cooldown cooldown = entry.getValue();
            if (!cooldown.isDatabase()) continue;
            try (ResultSet rs = DBConnection.sql.readQuery("SELECT value FROM pk_cooldowns WHERE uuid = '" + this.uuid.toString() + "' AND cooldown = '" + name + "'")) {
                if (rs.next()) {
                    DBConnection.sql.modifyQuery("UPDATE pk_cooldowns SET value = " + cooldown.getCooldown() + " WHERE uuid = '" + this.uuid.toString() + "' AND cooldown = '" + name + "'", false);
                } else {
                    DBConnection.sql.modifyQuery("INSERT INTO  pk_cooldowns (uuid, cooldown, value) VALUES ('" + this.uuid.toString() + "', '" + name + "', " + cooldown.getCooldown() + ")", false);
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
     * Returns true if this BendingPlayer is fully loaded
     * @return True if the player is fully loaded
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
        } else if (hasTempElement(element)) {
            return true;
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
        return this.subelements.contains(sub) || hasTempSubElement(sub);
    }

    /**
     * Checks to see if the {@link BendingPlayer} has a temporary element.
     * @param element The element to check
     * @return true If the player has the element
     */
    public boolean hasTempElement(@NotNull final Element element) {
        if (element.isAvatarElement() && hasTempElement(Element.AVATAR)) return true;

        if (element instanceof SubElement) return this.hasTempSubElement((SubElement) element);
        return this.tempElements.containsKey(element) && this.tempElements.get(element) > System.currentTimeMillis();
    }

    /**
     * Checks to see if the {@link BendingPlayer} has a temporary subelement. Includes subelements tied to temp parent elements
     * @param sub The subelement to check
     * @return true If the player has the subelement
     */
    public boolean hasTempSubElement(@NotNull final SubElement sub) {
        return this.tempSubElements.containsKey(sub) && (this.tempSubElements.get(sub) == -1 || //-1 means that the time is linked to the parent element
                this.tempSubElements.get(sub) > System.currentTimeMillis());
    }

    /**
     * Checks to see if the {@link BendingPlayer} has a temporary subelement that is not linked to a parent element
     * @param sub The subelement to check
     * @return true If the player has the subelement
     */
    public boolean hasTempSubElementExcludeParents(@NotNull final SubElement sub) {
        return this.tempSubElements.containsKey(sub) && this.tempSubElements.get(sub) > System.currentTimeMillis();
    }

    /**
     * Checks to see if the {@link BendingPlayer} has any temporary elements that have not expired.
     * @return true If the player has any temporary elements
     */
    public boolean hasTempElements() {
        Map<Element, Long> tempMap = new HashMap<>(this.tempElements);
        tempMap.putAll(this.tempSubElements);
        return tempMap.entrySet().stream().anyMatch(entry -> entry.getValue() > System.currentTimeMillis());
    }

    /**
     * Gets the time that a temporary element expires. If the element is not temporary, it will return -1.
     * If the element has already expired but the user isn't online, it will return 0.
     * @param element The element to check
     * @return The time the element expires, or -1 if it is not temporary
     */
    public long getTempElementTime(@NotNull final Element element) {
        if (element instanceof SubElement) return this.getTempSubElementTime((SubElement) element);
        return this.tempElements.getOrDefault(element, -1L);
    }

    /**
     * Gets the time that a temporary subelement expires. If the subelement is not temporary, it will return -1.
     * If the subelement has already expired but the user isn't online, it will return 0.
     * @param sub The subelement to check
     * @return The time the subelement expires, or -1 if it is not temporary
     */
    public long getTempSubElementTime(@NotNull final SubElement sub) {
        return this.tempSubElements.getOrDefault(sub, -1L);
    }

    /**
     * Gets the time that a temporary element expires, relative to now. If the element is not temporary, it will return 0.
     * @param element The element to check
     * @return The relative time the element expires, or 0 if it is not temporary
     */
    public long getTempElementRelativeTime(@NotNull final Element element) {
        if (element instanceof SubElement) return this.getTempSubElementTime((SubElement) element);

        long time = this.tempElements.getOrDefault(element, 0L);
        if (time == 0) return time;
        return time - System.currentTimeMillis();
    }

    /**
     * Gets the time that a temporary subelement expires, relative to now. If the subelement is not temporary, it will return 0.
     * @param sub The subelement to check
     * @return The relative time the subelement expires, or 0 if it is not temporary
     */
    public long getTempSubElementRelativeTime(@NotNull final SubElement sub) {
        long time = this.tempSubElements.getOrDefault(sub, 0L);
        if (time == 0) return time;
        return time - System.currentTimeMillis();
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
     * Checks to see if this {@link OfflineBendingPlayer} is an instanceof {@link BendingPlayer}.
     * @return Is the player online
     */
    public boolean isOnline() {
        return this instanceof BendingPlayer;
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
        return this.subelements.contains(Element.BLOOD) || this.hasTempSubElement(Element.BLOOD); //If they have bloodbending OR temporary bloodbending that hasn't expired
    }

    public boolean canBloodbendAtAnytime() {
        return false; //Offline players can't do it at anytime because OfflinePlayers have no permissions
    }

    public boolean canCombustionbend() {
        return this.subelements.contains(Element.COMBUSTION) || this.hasTempSubElement(Element.COMBUSTION); //If they have combustionbending OR temporary combustionbending that hasn't expired
    }

    public boolean canIcebend() {
        return this.subelements.contains(Element.ICE) || this.hasTempSubElement(Element.ICE); //If they have icebending OR temporary icebending that hasn't expired
    }

    /**
     * Checks to see if a player can LavaBend.
     *
     * @return true If player has permission node "bending.earth.lavabending"
     */
    public boolean canLavabend() {
        return this.subelements.contains(Element.LAVA) || this.hasTempSubElement(Element.LAVA); //If they have lavabending OR temporary lavabending that hasn't expired
    }

    public boolean canLightningbend() {
        return this.subelements.contains(Element.LIGHTNING) || this.hasTempSubElement(Element.LIGHTNING); //If they have lightningbending OR temporary lightningbending that hasn't expired
    }

    /**
     * Checks to see if a player can MetalBend.
     *
     * @return true If player has permission node "bending.earth.metalbending"
     */
    public boolean canMetalbend() {
        return this.subelements.contains(Element.METAL) || this.hasTempSubElement(Element.METAL); //If they have metalbending OR temporary metalbending that hasn't expired
    }

    /**
     * Checks to see if a player can PlantBend.
     *
     * @return true If player has permission node "bending.ability.plantbending"
     */
    public boolean canPlantbend() {
        return this.subelements.contains(Element.PLANT) || this.hasTempSubElement(Element.PLANT); //If they have plantbending OR temporary plantbending that hasn't expired
    }

    /**
     * Checks to see if a player can SandBend.
     *
     * @return true If player has permission node "bending.earth.sandbending"
     */
    public boolean canSandbend() {
        return this.subelements.contains(Element.SAND) || this.hasTempSubElement(Element.SAND); //If they have sandbending OR temporary sandbending that hasn't expired
    }

    /**
     * Checks to see if a player can use Flight.
     *
     * @return true If player has permission node "bending.air.flight"
     */
    public boolean canUseFlight() {
        return this.subelements.contains(Element.FLIGHT) || this.hasTempSubElement(Element.FLIGHT); //If they have flight OR temporary flight that hasn't expired
    }

    /**
     * Checks to see if a player can use SpiritualProjection.
     *
     * @return true If player has permission node
     *         "bending.air.spiritualprojection"
     */
    public boolean canUseSpiritualProjection() {
        return this.subelements.contains(Element.SPIRITUAL) || this.hasTempSubElement(Element.SPIRITUAL); //If they have spiritual projection OR temporary spiritual projection that hasn't expired
    }

    /**
     * Checks to see if a player can use Water Healing.
     *
     * @return true If player has permission node "bending.water.healing"
     */
    public boolean canWaterHeal() {
        return this.subelements.contains(Element.HEALING) || this.hasTempSubElement(Element.HEALING); //If they have water healing OR temporary water healing that hasn't expired
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
        bendingPlayer.tempElements.putAll(offlineBendingPlayer.tempElements);
        bendingPlayer.tempSubElements.putAll(offlineBendingPlayer.tempSubElements);
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

        Bukkit.getScheduler().callSyncMethod(ProjectKorra.plugin, () -> {
            Bukkit.getPluginManager().callEvent(new BendingPlayerLoadEvent(bendingPlayer));
            return true;
        });

        return bendingPlayer;
    }

    protected static OfflineBendingPlayer convertToOffline(@NotNull BendingPlayer bendingPlayer) {
        if (bendingPlayer.getPlayer() != null && bendingPlayer.getPlayer().isOnline()) return bendingPlayer;

        OfflineBendingPlayer offlineBendingPlayer = new OfflineBendingPlayer(bendingPlayer.getPlayer());
        offlineBendingPlayer.abilities = bendingPlayer.abilities;
        offlineBendingPlayer.elements.addAll(bendingPlayer.elements);
        offlineBendingPlayer.subelements.addAll(bendingPlayer.subelements);
        offlineBendingPlayer.tempElements.putAll(bendingPlayer.tempElements);
        offlineBendingPlayer.tempSubElements.putAll(bendingPlayer.tempSubElements);
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

        TEMP_ELEMENTS.removeIf(pair -> pair.getLeft().getUniqueId().equals(bendingPlayer.getUUID()));

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
        LOADING.remove(this.player.getUniqueId());
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

    /**
     * Adds a temporary element to the player. Does not check for existing temporary elements. Does not send messages.
     * @param element The element to add. Null for all elements.
     * @param sender The sender of the command. Can be null.
     * @param time The time in milliseconds to add the element for. Not the expiry time.
     * @return true If the element was added successfully
     */
    public boolean addTempElement(@Nullable Element element, @Nullable CommandSender sender, long time) {
        if (element == null) { //All elements
            boolean added = false;
            for (Element e1 : Element.getAllElements()) {
                if (e1.equals(Element.AVATAR)) continue;
                if (addTempElement(e1, sender, time)) added = true;
            }
            return added;
        }

        boolean sub = element instanceof SubElement;

        long expiry = time + System.currentTimeMillis();

        //Check the event isn't cancelled
        Cancellable event = sub ? new PlayerChangeSubElementEvent(sender, this.player, (SubElement) element, PlayerChangeSubElementEvent.Result.TEMP_ADD) :
                new PlayerChangeElementEvent(sender, this.player, element, PlayerChangeElementEvent.Result.TEMP_ADD);
        Bukkit.getPluginManager().callEvent((Event) event);
        if (event.isCancelled()) return false;

        if (element instanceof SubElement) {
            this.getTempSubElements().put((SubElement) element, expiry);
        } else {
            this.getTempElements().put(element, expiry);

            if (this.isOnline()) {
                if (element == Element.AVATAR) { //Give all subs for all parent elements marked as avatar elements
                    for (final Element e : Element.getAllElements()) {
                        if (e.equals(Element.AVATAR)) continue;
                        if (!e.isAvatarElement()) continue;

                        for (final SubElement subElement : Element.getSubElements(e)) {
                            if (((BendingPlayer)this).hasSubElementPermission(subElement) && !this.getSubElements().contains(subElement)) {
                                PlayerChangeSubElementEvent subEvent = new PlayerChangeSubElementEvent(sender, this.player, subElement, PlayerChangeSubElementEvent.Result.TEMP_PARENT_ADD);
                                Bukkit.getPluginManager().callEvent(subEvent);
                                if (subEvent.isCancelled()) continue; //Continue for subs which shouldn't be added due to the event cancelling

                                this.getTempSubElements().put(subElement, -1L); //Set the expiry to -1 to indicate that the time is linked to the parent element
                            }
                        }
                    }
                } else { //Not the avatar element
                    for (final SubElement subElement : Element.getSubElements(element)) {
                        if (((BendingPlayer) this).hasSubElementPermission(subElement) && !this.getSubElements().contains(subElement)) {
                            PlayerChangeSubElementEvent subEvent = new PlayerChangeSubElementEvent(sender, this.player, subElement, PlayerChangeSubElementEvent.Result.TEMP_PARENT_ADD);
                            Bukkit.getPluginManager().callEvent(subEvent);
                            if (subEvent.isCancelled()) continue; //Continue for subs which shouldn't be added due to the event cancelling

                            this.getTempSubElements().put(subElement, -1L); //Set the expiry to -1 to indicate that the time is linked to the parent element
                        }
                    }
                }
            }
        }

        if (isOnline()) ((BendingPlayer) this).recalculateTempElements(false);
        else saveTempElements();

        return true;
    }

    /**
     * Sets the expiry time of a temporary element. Will add/remove temp elements as needed. Does not send messages.
     * @param element The element to set. Null for all elements.
     * @param sender The sender of the command. Can be null.
     * @param time The time in milliseconds from now that the element should expiry should be set to. Negative time
     *             will reduce the expiry time. 0 will remove the element.
     * @return true If the element expiry was successfully set
     */
    public boolean setTempElement(@Nullable Element element, @Nullable CommandSender sender, long time) {
        if (element == null) { //All elements
            boolean added = false;
            for (Element e1 : Element.getAllElements()) {
                if (e1.equals(Element.AVATAR)) continue;
                if (setTempElement(e1, sender, time)) added = true;
            }
            return added;
        }

        boolean sub = element instanceof SubElement;

        boolean remove = time == 0 || (this.hasTempElement(element) && this.getTempElementRelativeTime(element) <= time);
        boolean add = time > 0 && !this.hasTempElement(element);

        long expiry = time + System.currentTimeMillis();

        if (add || remove) {
            //Check the event isn't cancelled
            Cancellable event = sub ? new PlayerChangeSubElementEvent(sender, this.player, (SubElement) element, add ? PlayerChangeSubElementEvent.Result.TEMP_ADD : PlayerChangeSubElementEvent.Result.TEMP_REMOVE) :
                    new PlayerChangeElementEvent(sender, this.player, element, add ? PlayerChangeElementEvent.Result.TEMP_ADD : PlayerChangeElementEvent.Result.TEMP_REMOVE);
            Bukkit.getPluginManager().callEvent((Event) event);
            if (event.isCancelled()) return false;

            if (add) return this.addTempElement(element, sender, time);
            else return this.removeTempElement(element, sender);
        }

        if (element instanceof SubElement) {
            this.getTempSubElements().put((SubElement) element, expiry);
        } else {
            this.getTempElements().put(element, expiry);
        }

        if (isOnline()) ((BendingPlayer) this).recalculateTempElements(false);
        else saveTempElements();

        return true;
    }

    /**
     * Removes a temporary element from the player. Does not send messages.
     * @param element The element to remove. Null for all elements.
     * @param sender The sender of the command. Can be null.
     * @return true If the element was removed successfully
     */
    public boolean removeTempElement(@Nullable Element element, @Nullable CommandSender sender) {
        if (element == null) { //All elements
            boolean removed = false;
            for (Element e1 : Element.getAllElements()) {
                if (e1.equals(Element.AVATAR)) continue;
                if (removeTempElement(e1, sender)) removed = true;
            }
            for (Element e1 : Element.getAllSubElements()) {
                if (removeTempElement(e1, sender)) removed = true;
            }
            return removed;
        }

        if (!this.hasTempElement(element)) return false;

        //Check the event isn't cancelled
        Cancellable event = element instanceof SubElement ? new PlayerChangeSubElementEvent(sender, this.getPlayer(), (SubElement) element, PlayerChangeSubElementEvent.Result.TEMP_REMOVE) :
                new PlayerChangeElementEvent(sender, this.getPlayer(), element, PlayerChangeElementEvent.Result.TEMP_REMOVE);
        Bukkit.getPluginManager().callEvent((Event) event);
        if (event.isCancelled()) return false;

        if (element instanceof SubElement) {
            if (this.isOnline()) {
                this.getTempSubElements().remove(element);
            } else {										    	        	//Mark it to be removed when the player logs in next. Allows
                this.getTempSubElements().put((SubElement) element, 0L); 	//the player to see that it was removed when they were offline
            }
        } else { //For parent elements
            if (this.isOnline()) {
                this.getTempElements().remove(element);
            } else {										    	//Mark it to be removed when the player logs in next. Allows
                this.getTempElements().put(element, 0L); 		//the player to see that it was removed when they were offline
            }

            if (element == Element.AVATAR) {
                Iterator<SubElement> subIterator1 = this.getTempSubElements().keySet().iterator();
                SubElement s1;
                while (subIterator1.hasNext() && (s1 = subIterator1.next()) != null) {
                    //Only remove if the subelement is connected to the parent element's time and is an avatar element
                    if (this.getTempSubElements().get(s1) != -1L || !s1.getParentElement().isAvatarElement()) continue;

                    if (!this.hasTempElement(s1.getParentElement())) {
                        PlayerChangeSubElementEvent subEvent = new PlayerChangeSubElementEvent(sender, this.getPlayer(), s1, PlayerChangeSubElementEvent.Result.TEMP_PARENT_REMOVE);
                        Bukkit.getPluginManager().callEvent(subEvent);
                        if (subEvent.isCancelled()) continue; //Continue for subs which shouldn't be added due to the event cancelling

                        subIterator1.remove(); //Remove the subelement
                    }
                }
            } else {
                //Remove all subs that are tied to the parent element
                for (SubElement tempSub : this.getTempSubElements().keySet()) {
                    long expiry = this.getTempSubElements().get(tempSub);

                    if (tempSub.getParentElement().equals(element) && expiry == -1L) { //If the sub expiry is linked to the parent element
                        PlayerChangeSubElementEvent subEvent = new PlayerChangeSubElementEvent(sender, this.getPlayer(), tempSub, PlayerChangeSubElementEvent.Result.TEMP_PARENT_REMOVE);
                        Bukkit.getPluginManager().callEvent(subEvent);
                        if (subEvent.isCancelled()) continue; //Continue for subs which shouldn't be added due to the event cancelling

                        this.getTempSubElements().remove(tempSub);
                    }
                }
            }
        }

        if (isOnline()) ((BendingPlayer) this).recalculateTempElements(false);
        else saveTempElements();

        return true;
    }

}

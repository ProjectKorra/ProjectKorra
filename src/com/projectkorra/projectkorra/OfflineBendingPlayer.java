package com.projectkorra.projectkorra;

import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.storage.DBConnection;
import com.projectkorra.projectkorra.util.Cooldown;
import com.projectkorra.projectkorra.util.DBCooldownManager;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.bukkit.Bukkit;
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
import java.util.function.Predicate;

import com.projectkorra.projectkorra.Element.SubElement;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.units.qual.C;

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
    protected boolean loading;

    protected final List<Element> elements = new ArrayList<>();
    protected final List<SubElement> subelements = new ArrayList<>();
    protected HashMap<Integer, String> abilities = new HashMap<>();
    protected final Map<String, Cooldown> cooldowns = new HashMap<>();
    protected final Set<Element> toggledElements = new HashSet<>();
    protected final DBCooldownManager cooldownManager;

    private int currentSlot;


    public OfflineBendingPlayer(OfflinePlayer player) {
        this.player = player;
        this.uuid = player.getUniqueId();
        this.toggled = true;
        this.loading = true;

        this.cooldownManager = Manager.getManager(DBCooldownManager.class);
    }

    public OfflineBendingPlayer(UUID playerUUID) {
        this(Bukkit.getOfflinePlayer(playerUUID));
    }

    public static CompletableFuture<OfflineBendingPlayer> loadAsync(final UUID uuid, boolean onStartup) {
        CompletableFuture<OfflineBendingPlayer> future = new CompletableFuture<>();
        Runnable runnable = () -> {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
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
                    if (bPlayer instanceof BendingPlayer) {
                        GeneralMethods.loadBendingPlayer((BendingPlayer) bPlayer);
                    }
                    future.complete(bPlayer);
                }
            } catch (final SQLException ex) {
                ex.printStackTrace();
            }
        };

        if (!Bukkit.isPrimaryThread()) runnable.run();
        else Bukkit.getScheduler().runTask(ProjectKorra.plugin, runnable);

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

    public String getBoundAbilityName() {
        final int slot = this.currentSlot + 1;
        final String name = this.getAbilities().get(slot);

        return name != null ? name : "";
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
     * Gets the map of cooldowns of the {@link BendingPlayer}.
     *
     * @return map of cooldowns
     */
    public Map<String, Cooldown> getCooldowns() {
        return this.cooldowns;
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
            return true;
        }

        return false;
    }

    /**
     * @return Returns true if this BendingPlayer is fully loaded
     */
    public boolean isLoaded() {
        return !loading;
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
            return this.player instanceof Player && ((Player)this.player).hasPermission("bending.avatar");
        } else if (!(element instanceof SubElement)) {
            return this.elements.contains(element);
        } else {
            return this.hasSubElement((SubElement) element);
        }
    }

    public boolean hasSubElement(final SubElement sub) {
        if (sub == null) {
            return false;
        }
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

    public boolean isElementToggled(final Element element) {
        return !this.toggledElements.contains(element);
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
     * @param element The element to set
     */
    public void setElement(final Element element) {
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

    public void toggleElement(final Element element) {
        if (element == null) {
            return;
        }

        if (this.toggledElements.contains(element)) {
            this.toggledElements.remove(element);
        } else {
            this.toggledElements.add(element);
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
        return player;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    protected static BendingPlayer convertToOnline(OfflineBendingPlayer offlineBendingPlayer) {
        Player player = Bukkit.getPlayer(offlineBendingPlayer.getUUID());
        if (player == null) {
            return null;
        }
        BendingPlayer bendingPlayer = new BendingPlayer(player);
        bendingPlayer.abilities = offlineBendingPlayer.abilities;
        bendingPlayer.elements.addAll(offlineBendingPlayer.elements);
        bendingPlayer.subelements.addAll(offlineBendingPlayer.subelements);
        bendingPlayer.toggledElements.addAll(offlineBendingPlayer.toggledElements);
        bendingPlayer.toggled = offlineBendingPlayer.toggled;
        bendingPlayer.permaRemoved = offlineBendingPlayer.permaRemoved;
        bendingPlayer.cooldowns.putAll(offlineBendingPlayer.cooldowns);
        bendingPlayer.loading = false;

        PLAYERS.put(player.getUniqueId(), bendingPlayer);

        return bendingPlayer;
    }

    protected static OfflineBendingPlayer convertToOnline(BendingPlayer bendingPlayer) {
        OfflineBendingPlayer offlineBendingPlayer = new BendingPlayer(bendingPlayer.getPlayer());
        offlineBendingPlayer.abilities = bendingPlayer.abilities;
        offlineBendingPlayer.elements.addAll(bendingPlayer.elements);
        offlineBendingPlayer.subelements.addAll(bendingPlayer.subelements);
        offlineBendingPlayer.toggledElements.addAll(bendingPlayer.toggledElements);
        offlineBendingPlayer.toggled = bendingPlayer.toggled;
        offlineBendingPlayer.permaRemoved = bendingPlayer.permaRemoved;
        offlineBendingPlayer.cooldowns.putAll(bendingPlayer.cooldowns);
        offlineBendingPlayer.loading = false;

        ONLINE_PLAYERS.remove(bendingPlayer.getUUID());
        PLAYERS.put(bendingPlayer.getUUID(), offlineBendingPlayer);

        return offlineBendingPlayer;
    }

    /**
     * Uncaches this OfflineBendingPlayer after the provided amount of milliseconds
     * have passed.
     * @param offlineBendingPlayer The player to uncache
     * @param time The amount of milliseconds to wait before uncaching
     */
    public static void uncacheAfter(OfflineBendingPlayer offlineBendingPlayer, long time) {
        if (offlineBendingPlayer.getPlayer().isOnline()) return;

        int ticks = (int) (time / 50) + 1;
        Bukkit.getScheduler().runTaskLater(ProjectKorra.plugin, offlineBendingPlayer::uncache, ticks);
    }

    /**
     * Uncaches this instance of an Offline BendingPlayer.
     */
    public void uncache() {
        if (this.player.isOnline() || this instanceof BendingPlayer) return;

        PLAYERS.remove(this.player.getUniqueId());
        ONLINE_PLAYERS.remove(this.player.getUniqueId());
    }
}

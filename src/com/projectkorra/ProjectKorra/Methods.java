package com.projectkorra.ProjectKorra;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.Protection;
import com.massivecraft.factions.engine.EngineMain;
import com.massivecraft.massivecore.ps.PS;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.PlayerCache;
import com.palmergames.bukkit.towny.object.PlayerCache.TownBlockStatus;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import com.palmergames.bukkit.towny.war.flagwar.TownyWar;
import com.palmergames.bukkit.towny.war.flagwar.TownyWarConfig;
import com.projectkorra.ProjectKorra.Ability.AbilityModule;
import com.projectkorra.ProjectKorra.Ability.AbilityModuleManager;
import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.Ability.StockAbilities;
import com.projectkorra.ProjectKorra.Ability.Combo.ComboAbilityModule;
import com.projectkorra.ProjectKorra.Ability.Combo.ComboModuleManager;
import com.projectkorra.ProjectKorra.Utilities.ParticleEffect;
import com.projectkorra.ProjectKorra.airbending.AirBlast;
import com.projectkorra.ProjectKorra.airbending.AirBubble;
import com.projectkorra.ProjectKorra.airbending.AirBurst;
import com.projectkorra.ProjectKorra.airbending.AirCombo;
import com.projectkorra.ProjectKorra.airbending.AirScooter;
import com.projectkorra.ProjectKorra.airbending.AirShield;
import com.projectkorra.ProjectKorra.airbending.AirSpout;
import com.projectkorra.ProjectKorra.airbending.AirSuction;
import com.projectkorra.ProjectKorra.airbending.AirSwipe;
import com.projectkorra.ProjectKorra.airbending.Suffocate;
import com.projectkorra.ProjectKorra.airbending.Tornado;
import com.projectkorra.ProjectKorra.chiblocking.AcrobatStance;
import com.projectkorra.ProjectKorra.chiblocking.Paralyze;
import com.projectkorra.ProjectKorra.chiblocking.RapidPunch;
import com.projectkorra.ProjectKorra.chiblocking.WarriorStance;
import com.projectkorra.ProjectKorra.earthbending.Catapult;
import com.projectkorra.ProjectKorra.earthbending.CompactColumn;
import com.projectkorra.ProjectKorra.earthbending.EarthArmor;
import com.projectkorra.ProjectKorra.earthbending.EarthBlast;
import com.projectkorra.ProjectKorra.earthbending.EarthColumn;
import com.projectkorra.ProjectKorra.earthbending.EarthPassive;
import com.projectkorra.ProjectKorra.earthbending.EarthSmash;
import com.projectkorra.ProjectKorra.earthbending.EarthTunnel;
import com.projectkorra.ProjectKorra.earthbending.LavaFlow;
import com.projectkorra.ProjectKorra.earthbending.MetalClips;
import com.projectkorra.ProjectKorra.earthbending.Shockwave;
import com.projectkorra.ProjectKorra.earthbending.Tremorsense;
import com.projectkorra.ProjectKorra.firebending.Combustion;
import com.projectkorra.ProjectKorra.firebending.Cook;
import com.projectkorra.ProjectKorra.firebending.FireBlast;
import com.projectkorra.ProjectKorra.firebending.FireBurst;
import com.projectkorra.ProjectKorra.firebending.FireCombo;
import com.projectkorra.ProjectKorra.firebending.FireJet;
import com.projectkorra.ProjectKorra.firebending.FireShield;
import com.projectkorra.ProjectKorra.firebending.FireStream;
import com.projectkorra.ProjectKorra.firebending.Fireball;
import com.projectkorra.ProjectKorra.firebending.Illumination;
import com.projectkorra.ProjectKorra.firebending.Lightning;
import com.projectkorra.ProjectKorra.firebending.WallOfFire;
import com.projectkorra.ProjectKorra.waterbending.Bloodbending;
import com.projectkorra.ProjectKorra.waterbending.FreezeMelt;
import com.projectkorra.ProjectKorra.waterbending.IceSpike;
import com.projectkorra.ProjectKorra.waterbending.IceSpike2;
import com.projectkorra.ProjectKorra.waterbending.OctopusForm;
import com.projectkorra.ProjectKorra.waterbending.Plantbending;
import com.projectkorra.ProjectKorra.waterbending.WaterCombo;
import com.projectkorra.ProjectKorra.waterbending.WaterManipulation;
import com.projectkorra.ProjectKorra.waterbending.WaterReturn;
import com.projectkorra.ProjectKorra.waterbending.WaterSpout;
import com.projectkorra.ProjectKorra.waterbending.WaterWall;
import com.projectkorra.ProjectKorra.waterbending.WaterWave;
import com.projectkorra.ProjectKorra.waterbending.Wave;
import com.projectkorra.rpg.RPGMethods;
import com.projectkorra.rpg.WorldEvents;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class Methods {
    
    static ProjectKorra plugin;
    private static FileConfiguration config = ProjectKorra.plugin.getConfig();
    
    public static Random rand = new Random();
    public static double CACHE_TIME = config.getDouble("Properties.RegionProtection.CacheBlockTime");
    
    private static final ItemStack pickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
    
    public static ConcurrentHashMap<Block, Information> movedearth = new ConcurrentHashMap<Block, Information>();
    public static ConcurrentHashMap<Integer, Information> tempair = new ConcurrentHashMap<Integer, Information>();
    public static ConcurrentHashMap<String, Long> cooldowns = new ConcurrentHashMap<String, Long>();
    // Represents PlayerName, previously checked blocks, and whether they were
    // true or false
    public static ConcurrentHashMap<String, ConcurrentHashMap<Block, BlockCacheElement>> blockProtectionCache = new ConcurrentHashMap<String, ConcurrentHashMap<Block, BlockCacheElement>>();
    public static ArrayList<Block> tempnophysics = new ArrayList<Block>();
    public static HashSet<Block> tempNoEarthbending = new HashSet<Block>();
    private static Integer[] plantIds = { 6, 18, 31, 32, 37, 38, 39, 40, 59, 81, 83, 86, 99, 100, 103, 104, 105, 106, 111, 161, 175 };
    
    public static Integer[] transparentToEarthbending = { 0, 6, 8, 9, 10, 11, 30, 31, 32, 37, 38, 39, 40, 50, 51, 59, 78, 83, 106, 175 };
    
    public static Integer[] nonOpaque = { 0, 6, 8, 9, 10, 11, 27, 28, 30, 31, 32, 37, 38, 39, 40, 50, 51, 55, 59, 66, 68, 69, 70, 72,
            75, 76, 77, 78, 83, 90, 93, 94, 104, 105, 106, 111, 115, 119, 127, 131, 132, 175 };
    
    /**
     * Checks to see if an AbilityExists. Uses method
     * {@link #getAbility(String)} to check if it exists.
     * 
     * @param string
     *            Ability Name
     * @return true if ability exists
     */
    public static boolean abilityExists(String string) {
        for (String st : AbilityModuleManager.abilities) {
            if (string.equalsIgnoreCase(st))
                return true;
        }
        return false;
    }
    
    public static boolean comboExists(String string)
    {
        for (ComboAbilityModule c : ComboModuleManager.combo)
            if (string.equalsIgnoreCase(c.getName()))
                return true;
        
        return false;
    }
    
    public ComboAbilityModule getCombo(String name)
    {
        for (ComboAbilityModule c : ComboModuleManager.combo)
            if (name.equalsIgnoreCase(c.getName()))
                return c;
        
        return null;
    }
    
    public static boolean isDisabledStockAbility(String string) {
        for (String st : AbilityModuleManager.disabledStockAbilities) {
            if (string.equalsIgnoreCase(st))
                return true;
        }
        return false;
    }
    
    public static void addTempAirBlock(Block block) {
        if (movedearth.containsKey(block)) {
            Information info = movedearth.get(block);
            block.setType(Material.AIR);
            info.setTime(System.currentTimeMillis());
            movedearth.remove(block);
            tempair.put(info.getID(), info);
        } else {
            Information info = new Information();
            info.setBlock(block);
            info.setState(block.getState());
            info.setTime(System.currentTimeMillis());
            block.setType(Material.AIR);
            tempair.put(info.getID(), info);
        }
        
    }
    
    /**
     * Binds a Ability to the hotbar slot that the player is on.
     * 
     * @param player
     *            The player to bind to
     * @param ability
     *            The ability name to Bind
     * @see {@link #bindAbility(Player, String, int)}
     */
    public static void bindAbility(Player player, String ability) {
        int slot = player.getInventory().getHeldItemSlot() + 1;
        bindAbility(player, ability, slot);
    }
    
    /**
     * Binds a Ability to a specific hotbar slot.
     * 
     * @param player
     *            The player to bind to
     * @param ability
     * @param slot
     * @see {@link #bindAbility(Player, String)}
     */
    public static void bindAbility(Player player, String ability, int slot) {
        BendingPlayer bPlayer = getBendingPlayer(player.getName());
        bPlayer.getAbilities().put(slot, ability);
        if (isAirAbility(ability)) {
            player.sendMessage(getAirColor() + "Succesfully bound " + ability + " to slot " + slot);
        }
        else if (isWaterAbility(ability)) {
            player.sendMessage(getWaterColor() + "Succesfully bound " + ability + " to slot " + slot);
        }
        else if (isEarthAbility(ability)) {
            player.sendMessage(getEarthColor() + "Succesfully bound " + ability + " to slot " + slot);
        }
        else if (isFireAbility(ability)) {
            player.sendMessage(getFireColor() + "Succesfully bound " + ability + " to slot " + slot);
        }
        else if (isChiAbility(ability)) {
            player.sendMessage(getChiColor() + "Succesfully bound " + ability + " to slot " + slot);
        } else {
            player.sendMessage(getAvatarColor() + "Successfully bound " + ability + " to slot " + slot);
        }
        
        saveAbility(bPlayer, slot, ability);
    }
    
    /**
     * Breaks a block and sets it to {@link Material#AIR AIR}.
     * 
     * @param block
     *            The block to break
     */
    public static void breakBlock(Block block) {
        block.breakNaturally(new ItemStack(Material.AIR));
    }
    
    /**
     * Checks to see if a Player is effected by BloodBending.
     * 
     * @param player
     *            The player to check
     *            <p>
     * @return true If {@link #isChiBlocked(String)} is true <br />
     *         false If player is BloodBender and Bending is toggled on, or if
     *         player is in AvatarState
     *         </p>
     */
    public static boolean canBeBloodbent(Player player) {
        if (AvatarState.isAvatarState(player))
            if (isChiBlocked(player.getName()))
                return true;
        if (canBend(player.getName(), "Bloodbending") && !Methods.getBendingPlayer(player.getName()).isToggled)
            return false;
        return true;
    }
    
    public static boolean canBind(String player, String ability) {
        Player p = Bukkit.getPlayer(player);
        if (p == null)
            return false;
        if (!p.hasPermission("bending.ability." + ability))
            return false;
        if (isAirAbility(ability) && !isBender(player, Element.Air))
            return false;
        if (isWaterAbility(ability) && !isBender(player, Element.Water))
            return false;
        if (isEarthAbility(ability) && !isBender(player, Element.Earth))
            return false;
        if (isFireAbility(ability) && !isBender(player, Element.Fire))
            return false;
        if (isChiAbility(ability) && !isBender(player, Element.Chi))
            return false;
        return true;
    }
    
    /**
     * Checks to see if a Player can bend a specific Ability.
     * 
     * @param player
     *            The player name to check
     * @param ability
     *            The Ability name to check
     * @return true If player can bend specified ability and has the permissions
     *         to do so
     */
    public static boolean canBend(String player, String ability) {
        BendingPlayer bPlayer = getBendingPlayer(player);
        Player p = Bukkit.getPlayer(player);
        if (bPlayer == null)
            return false;
        if (plugin.getConfig().getStringList("Properties.DisabledWorlds") != null
                && plugin.getConfig().getStringList("Properties.DisabledWorlds").contains(p.getWorld().getName()))
            return false;
        if (Commands.isToggledForAll)
            return false;
        if (!bPlayer.isToggled)
            return false;
        if (p == null)
            return false;
        if (cooldowns.containsKey(p.getName())) {
            if (cooldowns.get(p.getName()) + ProjectKorra.plugin.getConfig().getLong("Properties.GlobalCooldown") >= System.currentTimeMillis()) {
                return false;
            }
            cooldowns.remove(p.getName());
        }
        if (bPlayer.blockedChi)
            return false;
        // if (bPlayer.isChiBlocked()) return false;
        if (!p.hasPermission("bending.ability." + ability))
            return false;
        if (isAirAbility(ability) && !isBender(player, Element.Air))
            return false;
        if (isWaterAbility(ability) && !isBender(player, Element.Water))
            return false;
        if (isEarthAbility(ability) && !isBender(player, Element.Earth))
            return false;
        if (isFireAbility(ability) && !isBender(player, Element.Fire))
            return false;
        if (isChiAbility(ability) && !isBender(player, Element.Chi))
            return false;
        
        // if (isFlightAbility(ability) &&
        // !canAirFlight(plugin.getServer().getPlayer(player))) return false;
        // if (isSpiritualProjectionAbility(ability) &&
        // !canUseSpiritualProjection(plugin.getServer().getPlayer(player)))
        // return false;
        // if (isCombustionbendingAbility(ability) &&
        // !canCombustionbend(plugin.getServer().getPlayer(player))) return
        // false;
        // if (isLightningbendingAbility(ability) &&
        // !canLightningbend(plugin.getServer().getPlayer(player))) return
        // false;
        // if (isSandbendingAbility(ability) &&
        // !canSandbend(plugin.getServer().getPlayer(player))) return false;
        // if (isMetalbendingAbility(ability) &&
        // !canMetalbend(plugin.getServer().getPlayer(player))) return false;
        // if (isLavabendingAbility(ability) &&
        // !canLavabend(plugin.getServer().getPlayer(player))) return false;
        // if (isIcebendingAbility(ability) &&
        // !canIcebend(plugin.getServer().getPlayer(player))) return false;
        // if (isHealingAbility(ability) &&
        // !canWaterHeal(plugin.getServer().getPlayer(player))) return false;
        // if (isPlantbendingAbility(ability) &&
        // !canPlantbend(plugin.getServer().getPlayer(player))) return false;
        // if (isBloodbendingAbility(ability) &&
        // !canBloodbend(plugin.getServer().getPlayer(player))) return false;
        
        if (isRegionProtectedFromBuild(p, ability, p.getLocation()))
            return false;
        if (Paralyze.isParalyzed(p) || Bloodbending.isBloodbended(p))
            return false;
        if (MetalClips.isControlled(p))
            return false;
        if (BendingManager.events.get(p.getWorld()) != null && BendingManager.events.get(p.getWorld()).equalsIgnoreCase("SolarEclipse")
                && isFireAbility(ability))
            return false;
        if (BendingManager.events.get(p.getWorld()) != null && BendingManager.events.get(p.getWorld()).equalsIgnoreCase("LunarEclipse")
                && isWaterAbility(ability))
            return false;
        return true;
    }
    
    public static boolean canBendPassive(String player, Element element) {
        BendingPlayer bPlayer = getBendingPlayer(player);
        Player p = Bukkit.getPlayer(player);
        if (bPlayer == null)
            return false;
        if (p == null)
            return false;
        if (!p.hasPermission("bending." + element.toString().toLowerCase() + ".passive"))
            return false;
        if (!bPlayer.isToggled)
            return false;
        if (!bPlayer.hasElement(element))
            return false;
        if (isRegionProtectedFromBuild(p, null, p.getLocation()))
            return false;
        if (bPlayer.blockedChi)
            return false;
        return true;
    }
    
    /**
     * Checks to see if a player can BloodBend.
     * 
     * @param player
     *            The player to check
     * @return true If player has permission node "bending.earth.bloodbending"
     */
    public static boolean canBloodbend(Player player) {
        if (player.hasPermission("bending.water.bloodbending"))
            return true;
        return false;
    }
    
    public static boolean canBloodbendAtAnytime(Player player)
    {
        if (canBloodbend(player) && player.hasPermission("bending.water.bloodbending.anytime"))
            return true;
        return false;
    }
    
    public static boolean canIcebend(Player player)
    {
        if (player.hasPermission("bending.water.icebending"))
            return true;
        return false;
    }
    
    public static boolean canWaterHeal(Player player)
    {
        if (player.hasPermission("bending.water.healing"))
            return true;
        return false;
    }
    
    public static boolean canCombustionbend(Player player)
    {
        if (player.hasPermission("bending.fire.combustionbending"))
            return true;
        return false;
    }
    
    public static boolean canLightningbend(Player player)
    {
        if (player.hasPermission("bending.fire.lightningbending"))
            return true;
        return false;
    }
    
    public static boolean canAirFlight(Player player)
    {
        if (player.hasPermission("bending.air.flight"))
            return true;
        return false;
    }
    
    public static boolean canUseSpiritualProjection(Player player)
    {
        if (player.hasPermission("bending.air.spiritualprojection"))
            return true;
        return false;
    }
    
    public static boolean canSandbend(Player player)
    {
        if (player.hasPermission("bending.earth.sandbending"))
            return true;
        return false;
    }
    
    /**
     * Checks to see if a player can MetalBend.
     * 
     * @param player
     *            The player to check
     * @return true If player has permission node "bending.earth.metalbending"
     */
    public static boolean canMetalbend(Player player) {
        if (player.hasPermission("bending.earth.metalbending"))
            return true;
        return false;
    }
    
    public static boolean canLavabend(Player player) {
        return player.hasPermission("bending.earth.lavabending");
    }
    
    public static boolean isSubAbility(String ability) {
        if (AbilityModuleManager.subabilities.contains(ability))
            return true;
        return false;
    }
    
    /**
     * Checks to see if a player can PlantBend.
     * 
     * @param player
     *            The player to check
     * @return true If player has permission node "bending.ability.plantbending"
     */
    public static boolean canPlantbend(Player player) {
        return player.hasPermission("bending.water.plantbending");
    }
    
    /**
     * Creates a {@link BendingPlayer} with the data from the database. This
     * runs when a player logs in.
     * 
     * @param uuid
     *            The UUID of the player
     * @param player
     *            The player name
     * @throws SQLException
     */
    public static void createBendingPlayer(final UUID uuid, final String player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                createBendingPlayerAsynchronously(uuid, player);
            }
        }.runTaskAsynchronously(ProjectKorra.plugin);
    }
    
    private static void createBendingPlayerAsynchronously(final UUID uuid, final String player) {
        ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM pk_players WHERE uuid = '" + uuid.toString() + "'");
        try {
            if (!rs2.next()) { // Data doesn't exist, we want a completely new
                               // player.
                new BendingPlayer(uuid, player, new ArrayList<Element>(), new HashMap<Integer, String>(), false);
                DBConnection.sql.modifyQuery("INSERT INTO pk_players (uuid, player) VALUES ('" + uuid.toString() + "', '" + player + "')");
                ProjectKorra.log.info("Created new BendingPlayer for " + player);
            } else {
                // The player has at least played before.
                String player2 = rs2.getString("player");
                if (!player.equalsIgnoreCase(player2)) {
                    DBConnection.sql.modifyQuery("UPDATE pk_players SET player = '" + player + "' WHERE uuid = '" + uuid.toString() + "'");
                    // They have changed names.
                    
                    ProjectKorra.log.info("Updating Player Name for " + player);
                }
                
                String element = rs2.getString("element");
                String permaremoved = rs2.getString("permaremoved");
                boolean p = false;
                final ArrayList<Element> elements = new ArrayList<Element>();
                if (element != null) { // Player has an element.
                    if (element.contains("a"))
                        elements.add(Element.Air);
                    if (element.contains("w"))
                        elements.add(Element.Water);
                    if (element.contains("e"))
                        elements.add(Element.Earth);
                    if (element.contains("f"))
                        elements.add(Element.Fire);
                    if (element.contains("c"))
                        elements.add(Element.Chi);
                }
                
                final HashMap<Integer, String> abilities = new HashMap<Integer, String>();
                for (int i = 1; i <= 9; i++) {
                    String slot = rs2.getString("slot" + i);
                    
                    if (slot != null) {
                        abilities.put(i, slot);
                    }
                }
                
                p = (permaremoved == null ? false : (permaremoved.equals("true") ? true : (permaremoved.equals("false") ? false : p)));
                
                final boolean boolean_p = p;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        new BendingPlayer(uuid, player, elements, abilities, boolean_p);
                    }
                }.runTask(ProjectKorra.plugin);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Damages an Entity by amount of damage specified. Starts a
     * {@link EntityDamageByEntityEvent}.
     * 
     * @param player
     *            The player dealing the damage
     * @param entity
     *            The entity that is receiving the damage
     * @param damage
     *            The amount of damage to deal
     */
    @SuppressWarnings("deprecation")
    public static void damageEntity(Player player, Entity entity, double damage) {
        if (entity instanceof LivingEntity) {
            if (entity instanceof Player && Commands.invincible.contains(((Player) entity).getName())) {
                return;
            }
            
            EntityDamageEvent damageEvent = new EntityDamageEvent(entity, DamageCause.CUSTOM, damage);
            
            Bukkit.getPluginManager().callEvent(damageEvent);
            
            if (damageEvent.isCancelled()) {
                return;
            }
            
            EntityDamageByEntityEvent byEntityEvent = new EntityDamageByEntityEvent(player, entity, DamageCause.CUSTOM, damageEvent.getDamage());
            
            Bukkit.getPluginManager().callEvent(byEntityEvent);
            
            if (byEntityEvent.isCancelled()) {
                return;
            }
            
            ((LivingEntity) entity).damage(byEntityEvent.getDamage());
            ((LivingEntity) entity).setLastDamageCause(byEntityEvent);
        }
    }
    
    /**
     * Deserializes the configuration file "bendingPlayers.yml" of the old
     * BendingPlugin and creates a converted.yml ready for conversion.
     * 
     * @throws IOException
     *             If the "bendingPlayers.yml" file is not found
     */
    public static void deserializeFile() {
        File readFile = new File(".", "bendingPlayers.yml");
        File writeFile = new File(".", "converted.yml");
        if (readFile.exists()) {
            try (
                    DataInputStream input = new DataInputStream(new FileInputStream(readFile));
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    
                    DataOutputStream output = new DataOutputStream(new FileOutputStream(writeFile));
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));) {
                
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().contains("==: BendingPlayer")) {
                        writer.write(line + "\n");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Drops a {@code Collection<ItemStack>} of items on a specified block.
     * 
     * @param block
     *            The block to drop items on.
     * @param items
     *            The items to drop.
     */
    public static void dropItems(Block block, Collection<ItemStack> items) {
        for (ItemStack item : items) {
            block.getWorld().dropItem(block.getLocation(), item);
        }
    }
    
    /**
     * Gets the ability from specified ability name.
     * 
     * @param string
     *            The ability name
     * @return Ability name if found in {@link AbilityModuleManager#abilities}
     *         <p>
     *         else null
     *         </p>
     */
    public static String getAbility(String string) {
        for (String st : AbilityModuleManager.abilities) {
            if (st.equalsIgnoreCase(string)) {
                return st;
            }
        }
        return null;
    }
    
    /**
     * Gets the Element color from the Ability name specified.
     * 
     * @param ability
     *            The ability name
     *            <p>
     * @return {@link #getChiColor()} <br />
     *         {@link #getAirColor()} <br />
     *         {@link #getWaterColor()} <br />
     *         {@link #getEarthColor()} <br />
     *         {@link #getFireColor()} <br />
     *         else {@link #getAvatarColor()}
     *         </p>
     */
    public static ChatColor getAbilityColor(String ability) {
        if (AbilityModuleManager.chiabilities.contains(ability)) {
            return getChiColor();
        }
        
        if (AbilityModuleManager.airbendingabilities.contains(ability)) {
            if (AbilityModuleManager.subabilities.contains(ability)) {
                return getSubBendingColor(Element.Air);
            }
            
            return getAirColor();
        }
        
        if (AbilityModuleManager.waterbendingabilities.contains(ability))
        {
            if (AbilityModuleManager.subabilities.contains(ability)) {
                return getSubBendingColor(Element.Water);
            }
            
            return getWaterColor();
        }
        
        if (AbilityModuleManager.earthbendingabilities.contains(ability))
        {
            if (AbilityModuleManager.subabilities.contains(ability)) {
                return getSubBendingColor(Element.Earth);
            }
            
            return getEarthColor();
        }
        
        if (AbilityModuleManager.firebendingabilities.contains(ability))
        {
            if (AbilityModuleManager.subabilities.contains(ability)) {
                return getSubBendingColor(Element.Fire);
            }
            
            return getFireColor();
        }
        
        return getAvatarColor();
    }
    
    /**
     * Gets the AirColor from the config.
     * 
     * @return Config specified ChatColor
     */
    public static ChatColor getAirColor() {
        return ChatColor.valueOf(plugin.getConfig().getString("Properties.Chat.Colors.Air"));
    }
    
    /**
     * Gets the AvatarColor from the config.
     * 
     * @return Config specified ChatColor
     */
    public static ChatColor getAvatarColor() {
        return ChatColor.valueOf(plugin.getConfig().getString("Properties.Chat.Colors.Avatar"));
    }
    
    /**
     * Gets a {@link BendingPlayer} from specified player name.
     * 
     * @param player
     *            The name of the Player
     * @return The BendingPlayer object if {@link BendingPlayer#players}
     *         contains the player name
     */
    public static BendingPlayer getBendingPlayer(String player) {
        return BendingPlayer.players.get(player);
    }
    
    /**
     * Gets a {@code List<Blocks>} within the specified radius around the
     * specified location.
     * 
     * @param location
     *            The base location
     * @param radius
     *            The block radius from location to include within the list of
     *            blocks
     * @return The list of Blocks
     */
    public static List<Block> getBlocksAroundPoint(Location location, double radius) {
        List<Block> blocks = new ArrayList<Block>();
        
        int xorg = location.getBlockX();
        int yorg = location.getBlockY();
        int zorg = location.getBlockZ();
        
        int r = (int) radius * 4;
        
        for (int x = xorg - r; x <= xorg + r; x++) {
            for (int y = yorg - r; y <= yorg + r; y++) {
                for (int z = zorg - r; z <= zorg + r; z++) {
                    Block block = location.getWorld().getBlockAt(x, y, z);
                    if (block.getLocation().distanceSquared(location) <= radius * radius) {
                        blocks.add(block);
                    }
                }
            }
        }
        
        return blocks;
    }
    
    /**
     * Gets the Ability bound to the slot that the player is in.
     * 
     * @param player
     *            The player to check
     * @return The Ability name bounded to the slot
     *         <p>
     *         else null
     *         </p>
     */
    public static String getBoundAbility(Player player) {
        BendingPlayer bPlayer = getBendingPlayer(player.getName());
        
        if (bPlayer == null) {
            return null;
        }
        
        int slot = player.getInventory().getHeldItemSlot() + 1;
        return bPlayer.getAbilities().get(slot);
    }
    
    public static long getGlobalCooldown() {
        return plugin.getConfig().getLong("Properties.GlobalCooldown");
    }
    
    public static BlockFace getCardinalDirection(Vector vector) {
        BlockFace[] faces = { BlockFace.NORTH, BlockFace.NORTH_EAST,
                BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH,
                BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST };
        Vector n, ne, e, se, s, sw, w, nw;
        w = new Vector(-1, 0, 0);
        n = new Vector(0, 0, -1);
        s = n.clone().multiply(-1);
        e = w.clone().multiply(-1);
        ne = n.clone().add(e.clone()).normalize();
        se = s.clone().add(e.clone()).normalize();
        nw = n.clone().add(w.clone()).normalize();
        sw = s.clone().add(w.clone()).normalize();
        
        Vector[] vectors = { n, ne, e, se, s, sw, w, nw };
        
        double comp = 0;
        int besti = 0;
        for (int i = 0; i < vectors.length; i++) {
            double dot = vector.dot(vectors[i]);
            if (dot > comp) {
                comp = dot;
                besti = i;
            }
        }
        
        return faces[besti];
        
    }
    
    /**
     * Gets the ChiColor from the config.
     * 
     * @return Config specified ChatColor
     */
    public static ChatColor getChiColor() {
        return ChatColor.valueOf(plugin.getConfig().getString("Properties.Chat.Colors.Chi"));
    }
    
    public static List<Location> getCircle(Location loc, int radius, int height, boolean hollow, boolean sphere, int plusY) {
        List<Location> circleblocks = new ArrayList<Location>();
        int cx = loc.getBlockX();
        int cy = loc.getBlockY();
        int cz = loc.getBlockZ();
        
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                for (int y = (sphere ? cy - radius : cy); y < (sphere ? cy + radius : cy + height); y++) {
                    double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0);
                    
                    if (dist < radius * radius && !(hollow && dist < (radius - 1) * (radius - 1))) {
                        Location l = new Location(loc.getWorld(), x, y + plusY, z);
                        circleblocks.add(l);
                    }
                }
            }
        }
        
        return circleblocks;
    }
    
    public static Vector getDirection(Location location, Location destination) {
        double x1, y1, z1;
        double x0, y0, z0;
        
        x1 = destination.getX();
        y1 = destination.getY();
        z1 = destination.getZ();
        
        x0 = location.getX();
        y0 = location.getY();
        z0 = location.getZ();
        
        return new Vector(x1 - x0, y1 - y0, z1 - z0);
        
    }
    
    public static double getDistanceFromLine(Vector line, Location pointonline, Location point) {
        
        Vector AP = new Vector();
        double Ax, Ay, Az;
        Ax = pointonline.getX();
        Ay = pointonline.getY();
        Az = pointonline.getZ();
        
        double Px, Py, Pz;
        Px = point.getX();
        Py = point.getY();
        Pz = point.getZ();
        
        AP.setX(Px - Ax);
        AP.setY(Py - Ay);
        AP.setZ(Pz - Az);
        
        return (AP.crossProduct(line).length()) / (line.length());
    }
    
    /**
     * Gets a {@code Collection<ItemStack>} of item drops from a single block.
     * 
     * @param block
     *            The single block
     * @param type
     *            The Material type to change the block into
     * @param data
     *            The block data to change the block into
     * @param breakitem
     *            Unused
     * @return The item drops from the specified block
     */
    @SuppressWarnings("deprecation")
    public static Collection<ItemStack> getDrops(Block block, Material type, byte data, ItemStack breakitem) {
        BlockState tempstate = block.getState();
        block.setType(type);
        block.setData(data);
        Collection<ItemStack> item = block.getDrops();
        tempstate.update(true);
        return item;
    }
    
    public static int getEarthbendableBlocksLength(Player player, Block block, Vector direction, int maxlength) {
        Location location = block.getLocation();
        direction = direction.normalize();
        double j;
        for (int i = 0; i <= maxlength; i++) {
            j = (double) i;
            if (!isEarthbendable(player, location.clone().add(direction.clone().multiply(j)).getBlock())) {
                return i;
            }
        }
        return maxlength;
    }
    
    /**
     * Gets the EarthColor from the config.
     * 
     * @return Config specified ChatColor
     */
    public static ChatColor getEarthColor() {
        return ChatColor.valueOf(plugin.getConfig().getString("Properties.Chat.Colors.Earth"));
    }
    
    @SuppressWarnings("deprecation")
    public static Block getEarthSourceBlock(Player player, double range) {
        Block testblock = player.getTargetBlock(getTransparentEarthbending(), (int) range);
        
        if (isEarthbendable(player, testblock)) {
            return testblock;
        }
        
        Location location = player.getEyeLocation();
        Vector vector = location.getDirection().clone().normalize();
        for (double i = 0; i <= range; i++) {
            Block block = location.clone().add(vector.clone().multiply(i)).getBlock();
            
            if (isRegionProtectedFromBuild(player, "RaiseEarth", location)) {
                continue;
            }
            
            if (isEarthbendable(player, block)) {
                return block;
            }
        }
        return null;
    }
    
    /**
     * Gets a {@code List<Entity>} of entities around a specified radius from
     * the specified area
     * 
     * @param location
     *            The base location
     * @param radius
     *            The radius of blocks to look for entities from the location
     * @return A list of entities around a point
     */
    public static List<Entity> getEntitiesAroundPoint(Location location, double radius) {
        
        List<Entity> entities = location.getWorld().getEntities();
        List<Entity> list = location.getWorld().getEntities();
        
        for (Entity entity : entities) {
            if (entity.getWorld() != location.getWorld()) {
                list.remove(entity);
            } else if (entity.getLocation().distance(location) > radius) {
                list.remove(entity);
            }
        }
        
        return list;
        
    }
    
    /**
     * Gets the firebending dayfactor from the config multiplied by a specific
     * value if it is day.
     * 
     * @param value
     *            The value
     * @param world
     *            The world to pass into {@link #isDay(World)}
     *            <p>
     * @return value DayFactor multiplied by specified value when
     *         {@link #isDay(World)} is true <br />
     *         else <br />
     *         value The specified value in the parameters
     *         </p>
     * @see {@link #getFirebendingDayAugment(World)}
     */
    public static double getFirebendingDayAugment(double value, World world) {
        if (isDay(world)) {
            if (Methods.hasRPG()) {
                if (BendingManager.events.get(world).equalsIgnoreCase(WorldEvents.SozinsComet.toString())) {
                    return RPGMethods.getFactor(WorldEvents.SozinsComet) * value;
                } else if (BendingManager.events.get(world).equalsIgnoreCase(WorldEvents.SolarEclipse.toString())) {
                    return RPGMethods.getFactor(WorldEvents.SolarEclipse) * value;
                } else {
                    return value * plugin.getConfig().getDouble("Properties.Fire.DayFactor");
                }
            } else {
                return value * plugin.getConfig().getDouble("Properties.Fire.DayFactor");
            }
        }
        return value;
    }
    
    /**
     * Gets the firebending dayfactor from the config if it is day.
     * 
     * @param world
     *            The world to pass into {@link #isDay(World)}
     *            <p>
     * @return value DayFactor multiplied by specified value when
     *         {@link #isDay(World)} is true <br />
     *         else <br />
     *         value The value of 1
     *         </p>
     * @see {@link #getFirebendingDayAugment(double, World)}
     */
    @Deprecated
    public static double getFirebendingDayAugment(World world) {
        if (isDay(world)) {
            return plugin.getConfig().getDouble("Properties.Fire.DayFactor");
        }
        
        return 1;
    }
    
    /**
     * Gets the FireColor from the config.
     * 
     * @return Config specified ChatColor
     */
    public static ChatColor getFireColor() {
        return ChatColor.valueOf(plugin.getConfig().getString("Properties.Chat.Colors.Fire"));
    }
    
    @SuppressWarnings("incomplete-switch")
    public static int getIntCardinalDirection(Vector vector) {
        BlockFace face = getCardinalDirection(vector);
        
        switch (face) {
            case SOUTH:
                return 7;
            case SOUTH_WEST:
                return 6;
            case WEST:
                return 3;
            case NORTH_WEST:
                return 0;
            case NORTH:
                return 1;
            case NORTH_EAST:
                return 2;
            case EAST:
                return 5;
            case SOUTH_EAST:
                return 8;
        }
        
        return 4;
        
    }
    
    /**
     * Gets the MetalBendingColor from the config.
     * 
     * @return Config specified ChatColor
     */
    public static ChatColor getMetalbendingColor() {
        return ChatColor.valueOf(plugin.getConfig().getString("Properties.Chat.Colors.Metalbending"));
    }
    
    @SuppressWarnings("incomplete-switch")
    public static ChatColor getSubBendingColor(Element element)
    {
        switch (element) {
            case Fire:
                return ChatColor.valueOf(plugin.getConfig().getString("Properties.Chat.Colors.FireSub"));
            case Air:
                return ChatColor.valueOf(plugin.getConfig().getString("Properties.Chat.Colors.AirSub"));
            case Water:
                return ChatColor.valueOf(plugin.getConfig().getString("Properties.Chat.Colors.WaterSub"));
            case Earth:
                return ChatColor.valueOf(plugin.getConfig().getString("Properties.Chat.Colors.EarthSub"));
        }
        
        return getAvatarColor();
    }
    
    public static Vector getOrthogonalVector(Vector axis, double degrees, double length) {
        
        Vector ortho = new Vector(axis.getY(), -axis.getX(), 0);
        ortho = ortho.normalize();
        ortho = ortho.multiply(length);
        
        return rotateVectorAroundVector(axis, ortho, degrees);
    }
    
    public static Location getPointOnLine(Location origin, Location target, double distance) {
        return origin.clone().add(getDirection(origin, target).normalize().multiply(distance));
        
    }
    
    @SuppressWarnings("unused")
    public static Entity getTargetedEntity(Player player, double range, List<Entity> avoid) {
        double longestr = range + 1;
        Entity target = null;
        Location origin = player.getEyeLocation();
        Vector direction = player.getEyeLocation().getDirection().normalize();
        
        for (Entity entity : origin.getWorld().getEntities()) {
            if (avoid.contains(entity)) {
                continue;
            }
            
            if (entity.getLocation().distanceSquared(origin) < longestr * longestr
                    && getDistanceFromLine(direction, origin, entity.getLocation()) < 2
                    && (entity instanceof LivingEntity)
                    && entity.getEntityId() != player.getEntityId()
                    && entity.getLocation().distanceSquared(origin.clone().add(direction)) <
                    entity.getLocation().distanceSquared(origin.clone().add(direction.clone().multiply(-1)))) {
                
                target = entity;
                longestr = entity.getLocation().distance(origin);
            }
        }
        
        if (target != null) {
            List<Block> blklist = new ArrayList<Block>();
            blklist = Methods.getBlocksAlongLine(player.getLocation(), target.getLocation(), player.getWorld());
            for (Block isair : blklist) {
                if (Methods.isObstructed(origin, target.getLocation())) {
                    target = null;
                    break;
                }
            }
        }
        
        return target;
    }
    
    public static List<Block> getBlocksAlongLine(Location ploc, Location tloc, World w) {
        List<Block> blocks = new ArrayList<Block>();
        
        // Next we will name each coordinate
        int x1 = ploc.getBlockX();
        int y1 = ploc.getBlockY();
        int z1 = ploc.getBlockZ();
        
        int x2 = tloc.getBlockX();
        int y2 = tloc.getBlockY();
        int z2 = tloc.getBlockZ();
        
        int xMin = Math.min(x1, x2);
        int xMax = Math.max(x1, x2);
        
        int yMin = Math.min(y1, y2);
        int yMax = Math.max(y1, y2);
        
        int zMin = Math.min(z1, z2);
        int zMax = Math.max(z1, z2);
        
        // Now it's time for the loop
        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                for (int z = zMin; z <= zMax; z++) {
                    Block b = new Location(w, x, y, z).getBlock();
                    blocks.add(b);
                }
            }
        }
        
        // And last but not least, we return with the list
        return blocks;
    }
    
    @SuppressWarnings("deprecation")
    public static Location getTargetedLocation(Player player, double originselectrange, Integer... nonOpaque2) {
        Location origin = player.getEyeLocation();
        Vector direction = origin.getDirection();
        
        HashSet<Byte> trans = null;
        
        if (nonOpaque2 != null) {
            trans = new HashSet<Byte>();
            trans.add((byte) 0);
            
            for (int i : nonOpaque2) {
                trans.add((byte) i);
            }
        }
        
        Block block = player.getTargetBlock(trans, (int) originselectrange + 1);
        double distance = block.getLocation().distance(origin) - 1.5;
        Location location = origin.add(direction.multiply(distance));
        
        return location;
    }
    
    public static Location getTargetedLocation(Player player, int range) {
        return getTargetedLocation(player, range, 0);
    }
    
    public static HashSet<Byte> getTransparentEarthbending() {
        HashSet<Byte> set = new HashSet<Byte>();
        
        for (int i : transparentToEarthbending) {
            set.add((byte) i);
        }
        
        return set;
    }
    
    public static double getWaterbendingNightAugment(World world) {
        if (hasRPG()) {
            if (isNight(world)) {
                if (BendingManager.events.get(world).equalsIgnoreCase(WorldEvents.LunarEclipse.toString())) {
                    return RPGMethods.getFactor(WorldEvents.LunarEclipse);
                } else if (BendingManager.events.get(world).equalsIgnoreCase("FullMoon")) {
                    return plugin.getConfig().getDouble("Properties.Water.FullMoonFactor");
                }
                return plugin.getConfig().getDouble("Properties.Water.NightFactor");
            } else {
                return 1;
            }
        } else {
            // Bukkit.getServer().broadcastMessage("RPG NOT DETECTED");
            
            if (isNight(world) && BendingManager.events.get(world).equalsIgnoreCase("FullMoon")) {
                return plugin.getConfig().getDouble("Properties.Water.FullMoonFactor");
            }
            
            if (isNight(world)) {
                return plugin.getConfig().getDouble("Properties.Water.NightFactor");
            }
            
            return 1;
        }
    }
    
    /**
     * Gets the WaterColor from the config.
     * 
     * @return Config specified ChatColor
     */
    public static ChatColor getWaterColor() {
        return ChatColor.valueOf(plugin.getConfig().getString("Properties.Chat.Colors.Water"));
    }
    
    @SuppressWarnings("deprecation")
    public static Block getWaterSourceBlock(Player player, double range, boolean plantbending) {
        Location location = player.getEyeLocation();
        Vector vector = location.getDirection().clone().normalize();
        
        for (double i = 0; i <= range; i++) {
            Block block = location.clone().add(vector.clone().multiply(i)).getBlock();
            
            if (isRegionProtectedFromBuild(player, "WaterManipulation", location)) {
                continue;
            }
            
            if (isWaterbendable(block, player) && (!isPlant(block) || plantbending)) {
                if (TempBlock.isTempBlock(block)) {
                    TempBlock tb = TempBlock.get(block);
                    byte full = 0x0;
                    if (tb.state.getRawData() != full && (tb.state.getType() != Material.WATER || tb.state.getType() != Material.STATIONARY_WATER)) {
                        continue;
                    }
                }
                return block;
            }
        }
        return null;
    }
    
    @SuppressWarnings("deprecation")
    public static Block getLavaSourceBlock(Player player, double range) {
        Location location = player.getEyeLocation();
        Vector vector = location.getDirection().clone().normalize();
        for (double i = 0; i <= range; i++) {
            Block block = location.clone().add(vector.clone().multiply(i)).getBlock();
            
            if (isRegionProtectedFromBuild(player, "LavaSurge", location)) {
                continue;
            }
            
            if (isLavabendable(block, player)) {
                if (TempBlock.isTempBlock(block)) {
                    TempBlock tb = TempBlock.get(block);
                    byte full = 0x0;
                    if (tb.state.getRawData() != full && (tb.state.getType() != Material.LAVA || tb.state.getType() != Material.STATIONARY_LAVA)) {
                        continue;
                    }
                }
                return block;
            }
        }
        return null;
    }
    
    public static Block getIceSourceBlock(Player player, double range) {
        Location location = player.getEyeLocation();
        Vector vector = location.getDirection().clone().normalize();
        for (double i = 0; i <= range; i++) {
            Block block = location.clone().add(vector.clone().multiply(i)).getBlock();
            
            if (isRegionProtectedFromBuild(player, "IceBlast", location)) {
                continue;
            }
            
            if (isIcebendable(block)) {
                if (TempBlock.isTempBlock(block)) {
                    continue;
                }
                
                return block;
            }
        }
        return null;
    }
    
    public static boolean hasPermission(Player player, String ability) {
        if (player.hasPermission("bending.ability." + ability) && canBind(player.getName(), ability))
            return true;
        return false;
    }
    
    public static boolean isAbilityInstalled(String name, String author) {
        String ability = getAbility(name);
        
        if (ability == null) {
            return false;
        }
        
        if (AbilityModuleManager.authors.get(name).equalsIgnoreCase(author)) {
            return true;
        }
        
        return false;
    }
    
    public static boolean isAdjacentToFrozenBlock(Block block) {
        BlockFace[] faces = { BlockFace.DOWN, BlockFace.UP, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH };
        boolean adjacent = false;
        
        for (BlockFace face : faces) {
            if (FreezeMelt.frozenblocks.containsKey((block.getRelative(face)))) {
                adjacent = true;
            }
        }
        
        return adjacent;
    }
    
    @SuppressWarnings("deprecation")
    public static boolean isAdjacentToThreeOrMoreSources(Block block) {
        if (TempBlock.isTempBlock(block)) {
            return false;
        }
        
        int sources = 0;
        byte full = 0x0;
        BlockFace[] faces = { BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH };
        for (BlockFace face : faces) {
            Block blocki = block.getRelative(face);
            
            if ((blocki.getType() == Material.LAVA || blocki.getType() == Material.STATIONARY_LAVA) && blocki.getData() == full
                    && EarthPassive.canPhysicsChange(blocki)) {
                sources++;
            }
            
            if ((blocki.getType() == Material.WATER || blocki.getType() == Material.STATIONARY_WATER) && blocki.getData() == full
                    && WaterManipulation.canPhysicsChange(blocki)) {
                sources++;
            }
            
            // if (FreezeMelt.frozenblocks.containsKey(blocki)) {
            // if (FreezeMelt.frozenblocks.get(blocki) == full)
            // sources++;
            // } else if (blocki.getType() == Material.ICE) {
            // sources++;
            // }
        }
        
        if (sources >= 2) {
            return true;
        }
        
        return false;
    }
    
    public static boolean isAirAbility(String ability) {
        return AbilityModuleManager.airbendingabilities.contains(ability);
    }
    
    public static boolean isBender(String player, Element element) {
        BendingPlayer bPlayer = getBendingPlayer(player);
        
        if (bPlayer == null) {
            return false;
        }
        
        if (bPlayer.hasElement(element)) {
            return true;
        }
        
        return false;
    }
    
    public static boolean isCombustionbendingAbility(String ability) {
        return AbilityModuleManager.combustionabilities.contains(ability);
    }
    
    public static boolean isLightningbendingAbility(String ability) {
        return AbilityModuleManager.lightningabilities.contains(ability);
    }
    
    public static boolean isHealingAbility(String ability) {
        return AbilityModuleManager.healingabilities.contains(ability);
    }
    
    public static boolean isIcebendingAbility(String ability) {
        return AbilityModuleManager.iceabilities.contains(ability);
    }
    
    public static boolean isPlantbendingAbility(String ability) {
        return AbilityModuleManager.plantabilities.contains(ability);
    }
    
    public static boolean isBloodbendingAbility(String ability) {
        return AbilityModuleManager.bloodabilities.contains(ability);
    }
    
    public static boolean isFlightAbility(String ability) {
        return AbilityModuleManager.flightabilities.contains(ability);
    }
    
    public static boolean isSpiritualProjectionAbility(String ability) {
        return AbilityModuleManager.spiritualprojectionabilities.contains(ability);
    }
    
    public static boolean isLavabendingAbility(String ability) {
        return AbilityModuleManager.lavaabilities.contains(ability);
    }
    
    public static boolean isMetalbendingAbility(String ability) {
        return AbilityModuleManager.metalabilities.contains(ability);
    }
    
    public static boolean isSandbendingAbility(String ability) {
        return AbilityModuleManager.sandabilities.contains(ability);
    }
    
    public static boolean isChiAbility(String ability) {
        return AbilityModuleManager.chiabilities.contains(ability);
    }
    
    public static boolean isChiBlocked(String player) {
        if (Methods.getBendingPlayer(player) != null) {
            return Methods.getBendingPlayer(player).isChiBlocked();
        }
        
        return false;
    }
    
    public static boolean isDay(World world) {
        long time = world.getTime() % 24000;
        
        if (world.getEnvironment() != Environment.NORMAL) {
            return true;
        }
        
        return time >= 23500 || time <= 12500;
    }
    
    public static boolean isEarthAbility(String ability) {
        return AbilityModuleManager.earthbendingabilities.contains(ability);
    }
    
    public static boolean isEarthbendable(Player player, Block block) {
        return isEarthbendable(player, "RaiseEarth", block);
    }
    
    public static boolean isMetal(Block block) {
        Material material = block.getType();
        return ProjectKorra.plugin.getConfig().getStringList("Properties.Earth.MetalBlocks").contains(material.toString());
    }
    
    public static double getMetalAugment(double value) {
        return value * ProjectKorra.plugin.getConfig().getDouble("Properties.Earth.MetalPowerFactor");
    }
    
    public static boolean isEarthbendable(Player player, String ability, Block block) {
        Material material = block.getType();
        boolean valid = false;
        for (String s : ProjectKorra.plugin.getConfig().getStringList("Properties.Earth.EarthbendableBlocks")) {
            if (material == Material.getMaterial(s)) {
                valid = true;
                break;
            }
        }
        if (isMetal(block) && canMetalbend(player)) {
            valid = true;
        }
        
        if (!valid) {
            return false;
        }
        
        if (tempNoEarthbending.contains(block)) {
            return false;
        }
        
        if (!isRegionProtectedFromBuild(player, ability, block.getLocation())) {
            return true;
        }
        
        return false;
    }
    
    public static boolean isFireAbility(String ability) {
        return AbilityModuleManager.firebendingabilities.contains(ability);
    }
    
    public static boolean isFullMoon(World world) {
        long days = world.getFullTime() / 24000;
        long phase = days % 8;
        
        if (phase == 0) {
            return true;
        }
        
        return false;
    }
    
    public static boolean isHarmlessAbility(String ability) {
        return AbilityModuleManager.harmlessabilities.contains(ability);
    }
    
    public static boolean isImportEnabled() {
        return plugin.getConfig().getBoolean("Properties.ImportEnabled");
    }
    
    public static boolean isMeltable(Block block) {
        if (block.getType() == Material.ICE || block.getType() == Material.SNOW) {
            return true;
        }
        
        return false;
    }
    
    public static boolean isMetalBlock(Block block) {
        Material type = block.getType();
        return type == Material.GOLD_BLOCK
                || type == Material.IRON_BLOCK
                || type == Material.IRON_ORE
                || type == Material.GOLD_ORE
                || type == Material.QUARTZ_BLOCK
                || type == Material.QUARTZ_ORE;
    }
    
    public static boolean isNight(World world) {
        long time = world.getTime() % 24000;
        
        return world.getEnvironment() == Environment.NORMAL && time >= 12950 && time <= 23050;
    }
    
    @SuppressWarnings("deprecation")
    public static boolean isObstructed(Location location1, Location location2) {
        Vector loc1 = location1.toVector();
        Vector loc2 = location2.toVector();
        
        Vector direction = loc2.subtract(loc1);
        direction.normalize();
        
        Location loc;
        
        double max = location1.distance(location2);
        
        for (double i = 0; i <= max; i++) {
            loc = location1.clone().add(direction.clone().multiply(i));
            Material type = loc.getBlock().getType();
            if (type != Material.AIR && !Arrays.asList(transparentToEarthbending).contains(type.getId())) {
                return true;
            }
        }
        
        return false;
    }
    
    @SuppressWarnings("deprecation")
    public static boolean isPlant(Block block) {
        return Arrays.asList(plantIds).contains(block.getTypeId());
    }
    
    /*
     * isRegionProtectedFromBuild is one of the most server intensive methods in
     * the plugin. It uses a blockCache that keeps track of recent blocks that
     * may have already been checked. Abilities like TremorSense call this
     * ability 5 times per tick even though it only needs to check a single
     * block, instead of doing all 5 of those checks this method will now look
     * in the map first.
     */
    public static boolean isRegionProtectedFromBuild(Player player, String ability, Location loc) {
        if (!blockProtectionCache.containsKey(player.getName())) {
            blockProtectionCache.put(player.getName(), new ConcurrentHashMap<Block, BlockCacheElement>());
        }
        
        ConcurrentHashMap<Block, BlockCacheElement> blockMap = blockProtectionCache.get(player.getName());
        Block block = loc.getBlock();
        if (blockMap.containsKey(block)) {
            BlockCacheElement elem = blockMap.get(block);
            
            // both abilities must be equal to each other to use the cache
            if ((ability == null && elem.getAbility() == null) || (ability != null && elem.getAbility() != null && elem.getAbility().equals(ability))) {
                return elem.isAllowed();
            }
        }
        
        boolean value = isRegionProtectedFromBuildPostCache(player, ability, loc);
        blockMap.put(block, new BlockCacheElement(player, block, ability, value, System.currentTimeMillis()));
        return value;
    }
    
    public static boolean isRegionProtectedFromBuildPostCache(Player player, String ability, Location loc) {
        
        boolean allowharmless = plugin.getConfig().getBoolean("Properties.RegionProtection.AllowHarmlessAbilities");
        boolean respectWorldGuard = plugin.getConfig().getBoolean("Properties.RegionProtection.RespectWorldGuard");
        boolean respectPreciousStones = plugin.getConfig().getBoolean("Properties.RegionProtection.RespectPreciousStones");
        boolean respectFactions = plugin.getConfig().getBoolean("Properties.RegionProtection.RespectFactions");
        boolean respectTowny = plugin.getConfig().getBoolean("Properties.RegionProtection.RespectTowny");
        boolean respectGriefPrevention = plugin.getConfig().getBoolean("Properties.RegionProtection.RespectGriefPrevention");
        boolean respectLWC = plugin.getConfig().getBoolean("Properties.RegionProtection.RespectLWC");
        
        Set<String> ignite = AbilityModuleManager.igniteabilities;
        Set<String> explode = AbilityModuleManager.explodeabilities;
        
        if (ability == null && allowharmless) {
            return false;
        }
        
        if (isHarmlessAbility(ability) && allowharmless) {
            return false;
        }
        
        PluginManager pm = Bukkit.getPluginManager();
        
        Plugin wgp = pm.getPlugin("WorldGuard");
        Plugin psp = pm.getPlugin("PreciousStones");
        Plugin fcp = pm.getPlugin("Factions");
        Plugin twnp = pm.getPlugin("Towny");
        Plugin gpp = pm.getPlugin("GriefPrevention");
        Plugin massivecore = pm.getPlugin("MassiveCore");
        Plugin lwc = pm.getPlugin("LWC");
        
        for (Location location : new Location[] { loc, player.getLocation() }) {
            World world = location.getWorld();
            
            if (lwc != null && respectLWC) {
                LWCPlugin lwcp = (LWCPlugin) lwc;
                LWC lwc2 = lwcp.getLWC();
                Protection protection = lwc2.getProtectionCache().getProtection(location.getBlock());
                if (protection != null) {
                    if (!lwc2.canAccessProtection(player, protection)) {
                        return true;
                    }
                }
            }
            if (wgp != null && respectWorldGuard && !player.hasPermission("worldguard.region.bypass." + world.getName())) {
                WorldGuardPlugin wg = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
                
                if (!player.isOnline()) {
                    return true;
                }
                
                if (ignite.contains(ability)) {
                    if (!wg.hasPermission(player, "worldguard.override.lighter") && wg.getGlobalStateManager().get(world).blockLighter) {
                        return true;
                    }
                }
                
                if (explode.contains(ability)) {
                    if (wg.getGlobalStateManager().get(location.getWorld()).blockTNTExplosions || !wg.canBuild(player, location)) {
                        return true;
                    }
                }
                
                if (!wg.canBuild(player, location.getBlock())) {
                    return true;
                }
            }
            
            if (psp != null && respectPreciousStones) {
                PreciousStones ps = (PreciousStones) psp;
                
                if (ignite.contains(ability)) {
                    if (ps.getForceFieldManager().hasSourceField(location, FieldFlag.PREVENT_FIRE)) {
                        return true;
                    }
                }
                if (explode.contains(ability)) {
                    if (ps.getForceFieldManager().hasSourceField(location, FieldFlag.PREVENT_EXPLOSIONS)) {
                        return true;
                    }
                }
                
                // if (ps.getForceFieldManager().hasSourceField(location,
                // FieldFlag.PREVENT_PLACE))
                // return true;
                
                if (!PreciousStones.API().canBreak(player, location)) {
                    return true;
                }
            }
            
            if (fcp != null && massivecore != null && respectFactions) {
                return !EngineMain.canPlayerBuildAt(player, PS.valueOf(loc.getBlock()), false);
            }
            
            if (twnp != null && respectTowny) {
                Towny twn = (Towny) twnp;
                
                WorldCoord worldCoord;
                
                try {
                    TownyWorld tWorld = TownyUniverse.getDataSource().getWorld(world.getName());
                    worldCoord = new WorldCoord(tWorld.getName(), Coord.parseCoord(location));
                    
                    boolean bBuild = PlayerCacheUtil.getCachePermission(player, location, 3, (byte) 0, TownyPermission.ActionType.BUILD);
                    
                    // if (ignite.contains(ability)) {
                    //
                    // }
                    
                    // if (explode.contains(ability)) {
                    //
                    // }
                    
                    if (!bBuild) {
                        PlayerCache cache = twn.getCache(player);
                        TownBlockStatus status = cache.getStatus();
                        
                        if (((status == TownBlockStatus.ENEMY) && TownyWarConfig.isAllowingAttacks())) {
                            try {
                                TownyWar.callAttackCellEvent(twn, player, location.getBlock(), worldCoord);
                            } catch (Exception e) {
                                TownyMessaging.sendErrorMsg(player, e.getMessage());
                            }
                            
                            return true;
                        } else if (status != TownBlockStatus.WARZONE) {
                            return true;
                        }
                        
                        if ((cache.hasBlockErrMsg())) {
                            TownyMessaging.sendErrorMsg(player, cache.getBlockErrMsg());
                        }
                    }
                    
                } catch (Exception e1) {
                    TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_not_configured"));
                }
                
            }
            
            if (gpp != null && respectGriefPrevention) {
                Material type = player.getWorld().getBlockAt(location).getType();
                
                if (type == null) {
                    type = Material.AIR;
                }
                
                // String reason = GriefPrevention.instance.allowBuild(player,
                // location, null); // NOT WORKING with WorldGuard 6.0 BETA 4
                String reason = GriefPrevention.instance.allowBuild(player, location);
                // WORKING with WorldGuard 6.0 BETA 4
                
                // if (ignite.contains(ability)) {
                //
                // }
                
                // if (explode.contains(ability)) {
                //
                // }
                
                if (reason != null) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    @SuppressWarnings("deprecation")
    public static boolean isSolid(Block block) {
        return !Arrays.asList(nonOpaque).contains(block.getTypeId());
    }
    
    public static boolean isTransparentToEarthbending(Player player, Block block) {
        return isTransparentToEarthbending(player, "RaiseEarth", block);
    }
    
    @SuppressWarnings("deprecation")
    public static boolean isTransparentToEarthbending(Player player, String ability, Block block) {
        if (!Arrays.asList(transparentToEarthbending).contains(block.getTypeId())) {
            return false;
        }
        
        return !isRegionProtectedFromBuild(player, ability, block.getLocation());
    }
    
    public static boolean isWater(Block block) {
        return block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER;
    }
    
    public static boolean isLava(Block block) {
        return block.getType() == Material.LAVA || block.getType() == Material.STATIONARY_LAVA;
    }
    
    public static boolean isWaterAbility(String ability) {
        return AbilityModuleManager.waterbendingabilities.contains(ability);
    }
    
    @SuppressWarnings("deprecation")
    public static boolean isWaterbendable(Block block, Player player) {
        byte full = 0x0;
        
        if (TempBlock.isTempBlock(block))
            return false;
        if ((block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER) && block.getData() == full)
            return true;
        if (block.getType() == Material.ICE || block.getType() == Material.SNOW)
            return true;
        if (block.getType() == Material.PACKED_ICE && plugin.getConfig().getBoolean("Properties.Water.CanBendPackedIce"))
            return true;
        if (canPlantbend(player) && isPlant(block))
            return true;
        
        return false;
    }
    
    @SuppressWarnings("deprecation")
    public static boolean isLavabendable(Block block, Player player) {
        byte full = 0x0;
        if (TempBlock.isTempBlock(block)) {
            TempBlock tblock = TempBlock.instances.get(block);
            if (tblock == null || !LavaFlow.TEMP_LAVA_BLOCKS.contains(tblock)) {
                return false;
            }
        }
        
        return (block.getType() == Material.LAVA || block.getType() == Material.STATIONARY_LAVA) && block.getData() == full;
    }
    
    public static boolean isIcebendable(Block block) {
        return (block.getType() == Material.ICE)
                || (block.getType() == Material.PACKED_ICE && plugin.getConfig().getBoolean("Properties.Water.CanBendPackedIce"));
    }
    
    public static boolean isWeapon(Material mat) {
        return mat != null && (mat == Material.WOOD_AXE || mat == Material.WOOD_PICKAXE
                || mat == Material.WOOD_SPADE || mat == Material.WOOD_SWORD
                
                || mat == Material.STONE_AXE || mat == Material.STONE_PICKAXE
                || mat == Material.STONE_SPADE || mat == Material.STONE_SWORD
                
                || mat == Material.IRON_AXE || mat == Material.IRON_PICKAXE
                || mat == Material.IRON_SWORD || mat == Material.IRON_SPADE
                
                || mat == Material.DIAMOND_AXE || mat == Material.DIAMOND_PICKAXE
                || mat == Material.DIAMOND_SWORD || mat == Material.DIAMOND_SPADE);
    }
    
    public static void moveEarth(Player player, Block block, Vector direction, int chainlength) {
        moveEarth(player, block, direction, chainlength, true);
    }
    
    public static boolean moveEarth(Player player, Block block, Vector direction, int chainlength, boolean throwplayer) {
        if (isEarthbendable(player, block) && !isRegionProtectedFromBuild(player, "RaiseEarth", block.getLocation())) {
            boolean up = false;
            boolean down = false;
            Vector norm = direction.clone().normalize();
            
            if (norm.dot(new Vector(0, 1, 0)) == 1) {
                up = true;
            } else if (norm.dot(new Vector(0, -1, 0)) == 1) {
                down = true;
            }
            
            Vector negnorm = norm.clone().multiply(-1);
            
            Location location = block.getLocation();
            
            ArrayList<Block> blocks = new ArrayList<Block>();
            for (double j = -2; j <= chainlength; j++) {
                Block checkblock = location.clone().add(negnorm.clone().multiply(j)).getBlock();
                
                if (!tempnophysics.contains(checkblock)) {
                    blocks.add(checkblock);
                    tempnophysics.add(checkblock);
                }
            }
            
            Block affectedblock = location.clone().add(norm).getBlock();
            if (EarthPassive.isPassiveSand(block)) {
                EarthPassive.revertSand(block);
            }
            
            if (affectedblock == null) {
                return false;
            }
            
            if (isTransparentToEarthbending(player, affectedblock)) {
                if (throwplayer) {
                    for (Entity entity : getEntitiesAroundPoint(affectedblock.getLocation(), 1.75)) {
                        if (entity instanceof LivingEntity) {
                            LivingEntity lentity = (LivingEntity) entity;
                            
                            if (lentity.getEyeLocation().getBlockX() == affectedblock.getX() && lentity.getEyeLocation().getBlockZ() == affectedblock.getZ()) {
                                if (!(entity instanceof FallingBlock)) {
                                    entity.setVelocity(norm.clone().multiply(0.75));
                                }
                            }
                        } else if (entity.getLocation().getBlockX() == affectedblock.getX() && entity.getLocation().getBlockZ() == affectedblock.getZ()) {
                            if (!(entity instanceof FallingBlock)) {
                                entity.setVelocity(norm.clone().multiply(0.75));
                            }
                        }
                    }
                    
                }
                
                if (up) {
                    Block topblock = affectedblock.getRelative(BlockFace.UP);
                    if (topblock.getType() != Material.AIR) {
                        breakBlock(affectedblock);
                    } else if (!affectedblock.isLiquid() && affectedblock.getType() != Material.AIR) {
                        moveEarthBlock(affectedblock, topblock);
                    }
                } else {
                    breakBlock(affectedblock);
                }
                
                moveEarthBlock(block, affectedblock);
                playEarthbendingSound(block.getLocation());
                
                for (double i = 1; i < chainlength; i++) {
                    affectedblock = location.clone().add(negnorm.getX() * i, negnorm.getY() * i, negnorm.getZ() * i).getBlock();
                    if (!isEarthbendable(player, affectedblock)) {
                        if (down && isTransparentToEarthbending(player, affectedblock) && !affectedblock.isLiquid() && affectedblock.getType() != Material.AIR) {
                            moveEarthBlock(affectedblock, block);
                        }
                        
                        break;
                    }
                    
                    if (EarthPassive.isPassiveSand(affectedblock)) {
                        EarthPassive.revertSand(affectedblock);
                    }
                    
                    if (block == null) {
                        for (Block checkblock : blocks) {
                            tempnophysics.remove(checkblock);
                        }
                        return false;
                    }
                    
                    moveEarthBlock(affectedblock, block);
                    block = affectedblock;
                }
                
                int i = chainlength;
                affectedblock = location.clone().add(negnorm.getX() * i, negnorm.getY() * i, negnorm.getZ() * i).getBlock();
                
                if (!isEarthbendable(player, affectedblock) && down && isTransparentToEarthbending(player, affectedblock) && !affectedblock.isLiquid()) {
                    moveEarthBlock(affectedblock, block);
                }
            } else {
                for (Block checkblock : blocks) {
                    tempnophysics.remove(checkblock);
                }
                
                return false;
            }
            
            for (Block checkblock : blocks) {
                tempnophysics.remove(checkblock);
            }
            
            return true;
        }
        return false;
    }
    
    public static void moveEarth(Player player, Location location, Vector direction, int chainlength) {
        moveEarth(player, location, direction, chainlength, true);
    }
    
    public static void moveEarth(Player player, Location location, Vector direction, int chainlength, boolean throwplayer) {
        Block block = location.getBlock();
        moveEarth(player, block, direction, chainlength, throwplayer);
    }
    
    @SuppressWarnings("deprecation")
    public static void moveEarthBlock(Block source, Block target) {
        byte full = 0x0;
        Information info;
        if (movedearth.containsKey(source)) {
            info = movedearth.get(source);
            info.setTime(System.currentTimeMillis());
            movedearth.remove(source);
            movedearth.put(target, info);
        } else {
            info = new Information();
            info.setBlock(source);
            info.setTime(System.currentTimeMillis());
            info.setState(source.getState());
            movedearth.put(target, info);
        }
        
        if (isAdjacentToThreeOrMoreSources(source)) {
            source.setType(Material.WATER);
            source.setData(full);
        } else {
            source.setType(Material.AIR);
        }
        
        if (info.getState().getType() == Material.SAND) {
            target.setType(Material.SANDSTONE);
        } else {
            target.setType(info.getState().getType());
            target.setData(info.getState().getRawData());
        }
    }
    
    public static ParticleEffect getAirbendingParticles() {
        String particle = plugin.getConfig().getString("Properties.Air.Particles");
        
        if (particle == null) {
            return ParticleEffect.CLOUD;
        } else if (particle.equalsIgnoreCase("spell")) {
            return ParticleEffect.SPELL;
        } else if (particle.equalsIgnoreCase("blacksmoke")) {
            return ParticleEffect.SMOKE;
        } else if (particle.equalsIgnoreCase("smoke")) {
            return ParticleEffect.CLOUD;
        } else {
            return ParticleEffect.CLOUD;
        }
    }
    
    public static Collection<Player> getPlayersAroundPoint(Location location, double distance) {
        Collection<Player> players = new HashSet<Player>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getLocation().distance(location) <= distance) {
                players.add(player);
            }
        }
        return players;
    }
    
    public static void playAirbendingParticles(Location loc, int amount) {
        playAirbendingParticles(loc, amount, (float) Math.random(), (float) Math.random(), (float) Math.random());
    }
    
    public static void playAirbendingParticles(Location loc, int amount, float xOffset, float yOffset, float zOffset) {
        String particle = plugin.getConfig().getString("Properties.Air.Particles");
        ParticleEffect effect = null;
        
        if (particle != null) {
            if (particle.equalsIgnoreCase("spell")) {
                effect = ParticleEffect.SPELL;
            } else if (particle.equalsIgnoreCase("blacksmoke")) {
                effect = ParticleEffect.SMOKE;
            } else if (particle.equalsIgnoreCase("smoke")) {
                effect = ParticleEffect.CLOUD;
            }
        } else {
            effect = ParticleEffect.CLOUD;
        }
        
        if (effect == null) {
            for (int i = 0; i < amount; i++) {
                ParticleEffect.CLOUD.display(loc, xOffset, yOffset, (float) Math.random(), 0, 1);
            }
        } else {
            for (int i = 0; i < amount; i++) {
                effect.display(loc, xOffset, yOffset, zOffset, 0, 1);
            }
        }
    }
    
    public static void playLightningbendingParticle(Location loc) {
        playLightningbendingParticle(loc, (float) Math.random(), (float) Math.random(), (float) Math.random());
    }
    
    public static void playLightningbendingParticle(Location loc, float xOffset, float yOffset, float zOffset) {
        loc.setX(loc.getX() + Math.random() * (xOffset / 2 - -(xOffset / 2)));
        loc.setY(loc.getY() + Math.random() * (yOffset / 2 - -(yOffset / 2)));
        loc.setZ(loc.getZ() + Math.random() * (zOffset / 2 - -(zOffset / 2)));
        Methods.displayColoredParticle(loc, "#01E1FF");
    }
    
    public static void displayColoredParticle(Location loc, String hexVal) {
        int R = 0;
        int G = 0;
        int B = 0;
        
        if (hexVal.length() <= 6) {
            R = Integer.valueOf(hexVal.substring(0, 2), 16);
            G = Integer.valueOf(hexVal.substring(2, 4), 16);
            B = Integer.valueOf(hexVal.substring(4, 6), 16);
            
            if (R <= 0) {
                R = 1;
            }
        } else if (hexVal.length() <= 7 && hexVal.substring(0, 1).equals("#")) {
            R = Integer.valueOf(hexVal.substring(1, 3), 16);
            G = Integer.valueOf(hexVal.substring(3, 5), 16);
            B = Integer.valueOf(hexVal.substring(5, 7), 16);
            
            if (R <= 0) {
                R = 1;
            }
        }
        
        ParticleEffect.RED_DUST.display((float) R, (float) G, (float) B, 0.004F, 0, loc, 256D);
        
    }
    
    public static void displayColoredParticle(Location loc, String hexVal, float xOffset, float yOffset, float zOffset) {
        int R = 0;
        int G = 0;
        int B = 0;
        
        if (hexVal.length() <= 6) {
            R = Integer.valueOf(hexVal.substring(0, 2), 16);
            G = Integer.valueOf(hexVal.substring(2, 4), 16);
            B = Integer.valueOf(hexVal.substring(4, 6), 16);
            
            if (R <= 0) {
                R = 1;
            }
        } else if (hexVal.length() <= 7 && hexVal.substring(0, 1).equals("#")) {
            R = Integer.valueOf(hexVal.substring(1, 3), 16);
            G = Integer.valueOf(hexVal.substring(3, 5), 16);
            B = Integer.valueOf(hexVal.substring(5, 7), 16);
            
            if (R <= 0) {
                R = 1;
            }
        }
        
        loc.setX(loc.getX() + Math.random() * (xOffset / 2 - -(xOffset / 2)));
        loc.setY(loc.getY() + Math.random() * (yOffset / 2 - -(yOffset / 2)));
        loc.setZ(loc.getZ() + Math.random() * (zOffset / 2 - -(zOffset / 2)));
        
        ParticleEffect.RED_DUST.display((float) R, (float) G, (float) B, 0.004F, 0, loc, 256D);
        
    }
    
    public static void displayColoredParticle(Location loc, ParticleEffect type, String hexVal, float xOffset, float yOffset, float zOffset) {
        int R = 0;
        int G = 0;
        int B = 0;
        
        if (hexVal.length() <= 6) {
            R = Integer.valueOf(hexVal.substring(0, 2), 16);
            G = Integer.valueOf(hexVal.substring(2, 4), 16);
            B = Integer.valueOf(hexVal.substring(4, 6), 16);
            
            if (R <= 0) {
                R = 1;
            }
        }
        else if (hexVal.length() <= 7 && hexVal.substring(0, 1).equals("#")) {
            R = Integer.valueOf(hexVal.substring(1, 3), 16);
            G = Integer.valueOf(hexVal.substring(3, 5), 16);
            B = Integer.valueOf(hexVal.substring(5, 7), 16);
            
            if (R <= 0) {
                R = 1;
            }
        }
        
        loc.setX(loc.getX() + Math.random() * (xOffset / 2 - -(xOffset / 2)));
        loc.setY(loc.getY() + Math.random() * (yOffset / 2 - -(yOffset / 2)));
        loc.setZ(loc.getZ() + Math.random() * (zOffset / 2 - -(zOffset / 2)));
        
        if (type == ParticleEffect.RED_DUST || type == ParticleEffect.REDSTONE) {
            ParticleEffect.RED_DUST.display((float) R, (float) G, (float) B, 0.004F, 0, loc, 256D);
        } else if (type == ParticleEffect.SPELL_MOB || type == ParticleEffect.MOB_SPELL) {
            ParticleEffect.SPELL_MOB.display((float) 255 - R, (float) 255 - G, (float) 255 - B, 1, 0, loc, 256D);
        } else if (type == ParticleEffect.SPELL_MOB_AMBIENT || type == ParticleEffect.MOB_SPELL_AMBIENT) {
            ParticleEffect.SPELL_MOB_AMBIENT.display((float) 255 - R, (float) 255 - G, (float) 255 - B, 1, 0, loc, 256D);
        } else {
            ParticleEffect.RED_DUST.display((float) 0, (float) 0, (float) 0, 0.004F, 0, loc, 256D);
        }
    }
    
    public static void displayParticleVector(Location loc, ParticleEffect type, float xTrans, float yTrans, float zTrans) {
        if (type == ParticleEffect.FIREWORKS_SPARK) {
            ParticleEffect.FIREWORKS_SPARK.display((float) xTrans, (float) yTrans, (float) zTrans, 0.09F, 0, loc, 256D);
        } else if (type == ParticleEffect.SMOKE || type == ParticleEffect.SMOKE_NORMAL) {
            ParticleEffect.SMOKE.display((float) xTrans, (float) yTrans, (float) zTrans, 0.04F, 0, loc, 256D);
        } else if (type == ParticleEffect.LARGE_SMOKE || type == ParticleEffect.SMOKE_LARGE) {
            ParticleEffect.LARGE_SMOKE.display((float) xTrans, (float) yTrans, (float) zTrans, 0.04F, 0, loc, 256D);
        } else if (type == ParticleEffect.ENCHANTMENT_TABLE) {
            ParticleEffect.ENCHANTMENT_TABLE.display((float) xTrans, (float) yTrans, (float) zTrans, 0.5F, 0, loc, 256D);
        } else if (type == ParticleEffect.PORTAL) {
            ParticleEffect.PORTAL.display((float) xTrans, (float) yTrans, (float) zTrans, 0.5F, 0, loc, 256D);
        } else if (type == ParticleEffect.FLAME) {
            ParticleEffect.FLAME.display((float) xTrans, (float) yTrans, (float) zTrans, 0.04F, 0, loc, 256D);
        } else if (type == ParticleEffect.CLOUD) {
            ParticleEffect.CLOUD.display((float) xTrans, (float) yTrans, (float) zTrans, 0.04F, 0, loc, 256D);
        } else if (type == ParticleEffect.SNOW_SHOVEL) {
            ParticleEffect.SNOW_SHOVEL.display((float) xTrans, (float) yTrans, (float) zTrans, 0.2F, 0, loc, 256D);
        } else {
            ParticleEffect.RED_DUST.display((float) 0, (float) 0, (float) 0, 0.004F, 0, loc, 256D);
        }
    }
    
    public static void playFocusWaterEffect(Block block) {
        block.getWorld().playEffect(block.getLocation(), Effect.SMOKE, 4, 20);
    }
    
    public static void reloadPlugin() {
        // for (Player player: Bukkit.getOnlinePlayers()) {
        // Methods.saveBendingPlayer(player.getName());
        // }
        DBConnection.sql.close();
        plugin.reloadConfig();
        Methods.stopBending();
        DBConnection.host = plugin.getConfig().getString("Storage.MySQL.host");
        DBConnection.port = plugin.getConfig().getInt("Storage.MySQL.port");
        DBConnection.pass = plugin.getConfig().getString("Storage.MySQL.pass");
        DBConnection.db = plugin.getConfig().getString("Storage.MySQL.db");
        DBConnection.user = plugin.getConfig().getString("Storage.MySQL.user");
        DBConnection.init();
        for (Player player : Bukkit.getOnlinePlayers()) {
            Methods.createBendingPlayer(player.getUniqueId(), player.getName());
        }
    }
    
    public static void removeAllEarthbendedBlocks() {
        for (Block block : movedearth.keySet()) {
            revertBlock(block);
        }
        
        for (Integer i : tempair.keySet()) {
            revertAirBlock(i, true);
        }
    }
    
    @SuppressWarnings("deprecation")
    public static void removeBlock(Block block) {
        if (isAdjacentToThreeOrMoreSources(block)) {
            block.setType(Material.WATER);
            block.setData((byte) 0x0);
        } else {
            block.setType(Material.AIR);
        }
    }
    
    public static void removeRevertIndex(Block block) {
        if (movedearth.containsKey(block)) {
            Information info = movedearth.get(block);
            
            if (block.getType() == Material.SANDSTONE && info.getType() == Material.SAND) {
                block.setType(Material.SAND);
            }
            
            if (EarthColumn.blockInAllAffectedBlocks(block)) {
                EarthColumn.revertBlock(block);
            }
            
            EarthColumn.resetBlock(block);
            
            movedearth.remove(block);
        }
    }
    
    public static void removeSpouts(Location location, double radius, Player sourceplayer) {
        WaterSpout.removeSpouts(location, radius, sourceplayer);
        AirSpout.removeSpouts(location, radius, sourceplayer);
    }
    
    public static void removeSpouts(Location location, Player sourceplayer) {
        removeSpouts(location, 1.5, sourceplayer);
    }
    
    public static void removeUnusableAbilities(String player) {
        BendingPlayer bPlayer = getBendingPlayer(player);
        HashMap<Integer, String> slots = bPlayer.getAbilities();
        HashMap<Integer, String> finalabilities = new HashMap<Integer, String>();
        try {
            for (int i : slots.keySet()) {
                if (canBend(player, slots.get(i))) {
                    finalabilities.put(i, slots.get(i));
                }
            }
            
            bPlayer.setAbilities(finalabilities);
        } catch (Exception ex) {
            
        }
    }
    
    public static void revertAirBlock(int i) {
        revertAirBlock(i, false);
    }
    
    @SuppressWarnings("deprecation")
    public static void revertAirBlock(int i, boolean force) {
        if (!tempair.containsKey(i)) {
            return;
        }
        
        Information info = tempair.get(i);
        Block block = info.getState().getBlock();
        if (block.getType() != Material.AIR && !block.isLiquid()) {
            if (force || !movedearth.containsKey(block)) {
                dropItems(block, getDrops(block, info.getState().getType(), info.getState().getRawData(), pickaxe));
                tempair.remove(i);
            } else {
                info.setTime(info.getTime() + 10000);
            }
            
            return;
        } else {
            info.getState().update(true);
            tempair.remove(i);
        }
    }
    
    @SuppressWarnings("deprecation")
    public static boolean revertBlock(Block block) {
        byte full = 0x0;
        if (!ProjectKorra.plugin.getConfig().getBoolean("Properties.Earth.RevertEarthbending")) {
            movedearth.remove(block);
            return false;
        }
        
        if (movedearth.containsKey(block)) {
            Information info = movedearth.get(block);
            Block sourceblock = info.getState().getBlock();
            
            if (info.getState().getType() == Material.AIR) {
                movedearth.remove(block);
                return true;
            }
            
            if (block.equals(sourceblock)) {
                info.getState().update(true);
                if (EarthColumn.blockInAllAffectedBlocks(sourceblock))
                    EarthColumn.revertBlock(sourceblock);
                if (EarthColumn.blockInAllAffectedBlocks(block))
                    EarthColumn.revertBlock(block);
                EarthColumn.resetBlock(sourceblock);
                EarthColumn.resetBlock(block);
                movedearth.remove(block);
                return true;
            }
            
            if (movedearth.containsKey(sourceblock)) {
                addTempAirBlock(block);
                movedearth.remove(block);
                return true;
            }
            
            if (sourceblock.getType() == Material.AIR || sourceblock.isLiquid()) {
                info.getState().update(true);
            } else {
                dropItems(block, getDrops(block, info.getState().getType(), info.getState().getRawData(), pickaxe));
            }
            
            if (isAdjacentToThreeOrMoreSources(block)) {
                block.setType(Material.WATER);
                block.setData(full);
            } else {
                block.setType(Material.AIR);
            }
            
            if (EarthColumn.blockInAllAffectedBlocks(sourceblock)) {
                EarthColumn.revertBlock(sourceblock);
            }
            
            if (EarthColumn.blockInAllAffectedBlocks(block)) {
                EarthColumn.revertBlock(block);
            }
            
            EarthColumn.resetBlock(sourceblock);
            EarthColumn.resetBlock(block);
            movedearth.remove(block);
        }
        return true;
    }
    
    public static Vector rotateVectorAroundVector(Vector axis, Vector rotator, double degrees) {
        double angle = Math.toRadians(degrees);
        Vector rotation = axis.clone();
        Vector rotate = rotator.clone();
        rotation = rotation.normalize();
        
        Vector thirdaxis = rotation.crossProduct(rotate).normalize().multiply(rotate.length());
        
        return rotate.multiply(Math.cos(angle)).add(thirdaxis.multiply(Math.sin(angle)));
    }
    
    public static void saveElements(BendingPlayer bPlayer) {
        if (bPlayer == null) {
            return;
        }
        
        String uuid = bPlayer.uuid.toString();
        
        StringBuilder elements = new StringBuilder();
        if (bPlayer.hasElement(Element.Air))
            elements.append("a");
        if (bPlayer.hasElement(Element.Water))
            elements.append("w");
        if (bPlayer.hasElement(Element.Earth))
            elements.append("e");
        if (bPlayer.hasElement(Element.Fire))
            elements.append("f");
        if (bPlayer.hasElement(Element.Chi))
            elements.append("c");
        
        DBConnection.sql.modifyQuery("UPDATE pk_players SET element = '" + elements + "' WHERE uuid = '" + uuid + "'");
    }
    
    public static void saveAbility(BendingPlayer bPlayer, int slot, String ability) {
        if (bPlayer == null) {
            return;
        }
        
        String uuid = bPlayer.uuid.toString();
        
        HashMap<Integer, String> abilities = bPlayer.getAbilities();
        
        DBConnection.sql.modifyQuery("UPDATE pk_players SET slot" + slot + " = '" + (abilities.get(slot) == null ? null : abilities.get(slot))
                + "' WHERE uuid = '" + uuid + "'");
    }
    
    public static void savePermaRemoved(BendingPlayer bPlayer) {
        if (bPlayer == null) {
            return;
        }
        
        String uuid = bPlayer.uuid.toString();
        
        boolean permaRemoved = bPlayer.permaRemoved;
        DBConnection.sql.modifyQuery("UPDATE pk_players SET permaremoved = '" + (permaRemoved ? "true" : "false") + "' WHERE uuid = '" + uuid + "'");
    }
    
    public static void stopBending() {
        List<AbilityModule> abilities = AbilityModuleManager.ability;
        for (AbilityModule ab : abilities) {
            ab.stop();
        }
        
        ArrayList<ComboManager.ComboAbility> combos = ComboManager.comboAbilityList;
        for (ComboManager.ComboAbility c : combos) {
            if (c.getComboType() instanceof ComboAbilityModule) {
                ((ComboAbilityModule) c.getComboType()).stop();
            }
        }
        
        AirBlast.removeAll();
        AirBubble.removeAll();
        AirShield.instances.clear();
        AirSuction.instances.clear();
        AirScooter.removeAll();
        AirSpout.removeAll();
        AirSwipe.instances.clear();
        Tornado.instances.clear();
        AirBurst.removeAll();
        Suffocate.removeAll();
        AirCombo.removeAll();
        com.projectkorra.ProjectKorra.airbending.FlightAbility.removeAll();
        
        Catapult.removeAll();
        CompactColumn.removeAll();
        EarthBlast.removeAll();
        EarthColumn.removeAll();
        EarthPassive.removeAll();
        EarthArmor.removeAll();
        EarthTunnel.instances.clear();
        Shockwave.removeAll();
        Tremorsense.removeAll();
        LavaFlow.removeAll();
        EarthSmash.removeAll();
        
        FreezeMelt.removeAll();
        IceSpike.removeAll();
        IceSpike2.removeAll();
        WaterManipulation.removeAll();
        WaterSpout.removeAll();
        WaterWall.removeAll();
        Wave.removeAll();
        Plantbending.regrowAll();
        OctopusForm.removeAll();
        Bloodbending.instances.clear();
        WaterWave.removeAll();
        WaterCombo.removeAll();
        
        FireStream.removeAll();
        Fireball.removeAll();
        WallOfFire.instances.clear();
        Lightning.instances.clear();
        FireShield.removeAll();
        FireBlast.removeAll();
        FireBurst.removeAll();
        FireJet.instances.clear();
        Cook.removeAll();
        Illumination.removeAll();
        FireCombo.removeAll();
        
        RapidPunch.instances.clear();
        WarriorStance.instances.clear();
        AcrobatStance.instances.clear();
        
        Flight.removeAll();
        WaterReturn.removeAll();
        TempBlock.removeAll();
        
        if (ProjectKorra.plugin.getConfig().getBoolean("Properties.Earth.RevertEarthbending")) {
            removeAllEarthbendedBlocks();
        }
        
        EarthPassive.removeAll();
    }
    
    public static void setVelocity(Entity entity, Vector velocity) {
        if (entity instanceof TNTPrimed) {
            if (plugin.getConfig().getBoolean("Properties.BendingAffectFallingSand.TNT"))
                entity.setVelocity(velocity.multiply(plugin.getConfig().getDouble("Properties.BendingAffectFallingSand.TNTStrengthMultiplier")));
            return;
        }
        
        if (entity instanceof FallingBlock) {
            if (plugin.getConfig().getBoolean("Properties.BendingAffectFallingSand.Normal"))
                entity.setVelocity(velocity.multiply(plugin.getConfig().getDouble("Properties.BendingAffectFallingSand.NormalStrengthMultiplier")));
            return;
        }
        
        entity.setVelocity(velocity);
    }
    
    public static double waterbendingNightAugment(double value, World world) {
        if (isNight(world)) {
            if (hasRPG()) {
                if (BendingManager.events.get(world).equalsIgnoreCase(WorldEvents.LunarEclipse.toString())) {
                    return RPGMethods.getFactor(WorldEvents.LunarEclipse) * value;
                } else if (BendingManager.events.get(world).equalsIgnoreCase("FullMoon")) {
                    return plugin.getConfig().getDouble("Properties.Water.FullMoonFactor") * value;
                } else {
                    return value;
                }
            } else {
                if (isFullMoon(world)) {
                    return plugin.getConfig().getDouble("Properties.Water.FullMoonFactor") * value;
                } else {
                    return plugin.getConfig().getDouble("Properties.Water.NightFactor") * value;
                }
            }
        } else {
            return value;
        }
    }
    
    public Methods(ProjectKorra plugin) {
        Methods.plugin = plugin;
    }
    
    public static boolean isNegativeEffect(PotionEffectType effect) {
        if (effect.equals(PotionEffectType.POISON))
            return true;
        if (effect.equals(PotionEffectType.BLINDNESS))
            return true;
        if (effect.equals(PotionEffectType.CONFUSION))
            return true;
        if (effect.equals(PotionEffectType.HARM))
            return true;
        if (effect.equals(PotionEffectType.HUNGER))
            return true;
        if (effect.equals(PotionEffectType.SLOW))
            return true;
        if (effect.equals(PotionEffectType.SLOW_DIGGING))
            return true;
        if (effect.equals(PotionEffectType.WEAKNESS))
            return true;
        if (effect.equals(PotionEffectType.WITHER))
            return true;
        return false;
    }
    
    public static boolean isPositiveEffect(PotionEffectType effect) {
        if (effect.equals(PotionEffectType.ABSORPTION))
            return true;
        if (effect.equals(PotionEffectType.DAMAGE_RESISTANCE))
            return true;
        if (effect.equals(PotionEffectType.FAST_DIGGING))
            return true;
        if (effect.equals(PotionEffectType.FIRE_RESISTANCE))
            return true;
        if (effect.equals(PotionEffectType.HEAL))
            return true;
        if (effect.equals(PotionEffectType.HEALTH_BOOST))
            return true;
        if (effect.equals(PotionEffectType.INCREASE_DAMAGE))
            return true;
        if (effect.equals(PotionEffectType.JUMP))
            return true;
        if (effect.equals(PotionEffectType.NIGHT_VISION))
            return true;
        if (effect.equals(PotionEffectType.REGENERATION))
            return true;
        if (effect.equals(PotionEffectType.SATURATION))
            return true;
        if (effect.equals(PotionEffectType.SPEED))
            return true;
        if (effect.equals(PotionEffectType.WATER_BREATHING))
            return true;
        return false;
    }
    
    public static boolean isNeutralEffect(PotionEffectType effect) {
        return effect.equals(PotionEffectType.INVISIBILITY);
    }
    
    public static void breakBreathbendingHold(Entity entity) {
        if (Suffocate.isBreathbent(entity)) {
            Suffocate.breakSuffocate(entity);
            return;
        }
        
        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (Suffocate.isChannelingSphere(player)) {
                Suffocate.remove(player);
            }
        }
    }
    
    public static FallingBlock spawnFallingBlock(Location loc, int type) {
        return spawnFallingBlock(loc, type, (byte) 0);
    }
    
    public static FallingBlock spawnFallingBlock(Location loc, Material type) {
        return spawnFallingBlock(loc, type, (byte) 0);
    }
    
    @SuppressWarnings("deprecation")
    public static FallingBlock spawnFallingBlock(Location loc, int type, byte data) {
        return loc.getWorld().spawnFallingBlock(loc, type, data);
    }
    
    @SuppressWarnings("deprecation")
    public static FallingBlock spawnFallingBlock(Location loc, Material type, byte data) {
        return loc.getWorld().spawnFallingBlock(loc, type, data);
    }
    
    public static void playFirebendingParticles(Location loc) {
        loc.getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES, 0, 15);
    }
    
    public static void playFirebendingSound(Location loc) {
        if (plugin.getConfig().getBoolean("Properties.Fire.PlaySound")) {
            loc.getWorld().playSound(loc, Sound.FIRE, 1, 10);
        }
    }
    
    public static void playCombustionSound(Location loc) {
        if (plugin.getConfig().getBoolean("Properties.Fire.PlaySound")) {
            loc.getWorld().playSound(loc, Sound.FIREWORK_BLAST, 1, -1);
        }
    }
    
    public static void playEarthbendingSound(Location loc) {
        if (plugin.getConfig().getBoolean("Properties.Earth.PlaySound")) {
            loc.getWorld().playEffect(loc, Effect.GHAST_SHOOT, 0, 10);
        }
    }
    
    public static void playMetalbendingSound(Location loc) {
        if (plugin.getConfig().getBoolean("Properties.Earth.PlaySound")) {
            loc.getWorld().playSound(loc, Sound.IRONGOLEM_HIT, 1, 10);
        }
    }
    
    public static void playWaterbendingSound(Location loc) {
        if (plugin.getConfig().getBoolean("Properties.Water.PlaySound")) {
            loc.getWorld().playSound(loc, Sound.WATER, 1, 10);
        }
    }
    
    public static void playIcebendingSound(Location loc) {
        if (plugin.getConfig().getBoolean("Properties.Water.PlaySound")) {
            loc.getWorld().playSound(loc, Sound.FIRE_IGNITE, 10, 4);
        }
    }
    
    public static void playAirbendingSound(Location loc) {
        if (plugin.getConfig().getBoolean("Properties.Air.PlaySound")) {
            loc.getWorld().playSound(loc, Sound.CREEPER_HISS, 1, 5);
        }
    }
    
    public static void playAvatarSound(Location loc) {
        loc.getWorld().playSound(loc, Sound.ANVIL_LAND, 1, 10);
    }
    
    public static Block getTopBlock(Location loc, int range) {
        return getTopBlock(loc, range, range);
    }
    
    public static Block getTopBlock(Location loc, int positiveY, int negativeY)
    {
        /**
         * Returns the top block based around loc. PositiveY is the maximum
         * amount of distance it will check upward. Similarly, negativeY is for
         * downward.
         */
        Block block = loc.getBlock();
        Block blockHolder = block;
        int y = 0;
        // Only one of these while statements will go
        while (blockHolder.getType() != Material.AIR && Math.abs(y) < Math.abs(positiveY)) {
            y++;
            Block tempBlock = loc.clone().add(0, y, 0).getBlock();
            
            if (tempBlock.getType() == Material.AIR) {
                return blockHolder;
            }
            
            blockHolder = tempBlock;
        }
        
        while (blockHolder.getType() == Material.AIR && Math.abs(y) < Math.abs(negativeY)) {
            y--;
            blockHolder = loc.clone().add(0, y, 0).getBlock();
            
            if (blockHolder.getType() != Material.AIR) {
                return blockHolder;
            }
            
        }
        return null;
    }
    
    public static Vector rotateXZ(Vector vec, double theta)
    {
        /**
         * Rotates a vector around the Y plane.
         */
        Vector vec2 = vec.clone();
        double x = vec2.getX();
        double z = vec2.getZ();
        vec2.setX(x * Math.cos(Math.toRadians(theta)) - z * Math.sin(Math.toRadians(theta)));
        vec2.setZ(x * Math.sin(Math.toRadians(theta)) + z * Math.cos(Math.toRadians(theta)));
        return vec2;
    }
    
    public static int getMaxPresets(Player player) {
        if (player.isOp()) {
            return 500;
        }
        
        int cap = 0;
        for (int i = 0; i <= 500; i++) {
            if (player.hasPermission("bending.command.presets.create." + i)) {
                cap = i;
            }
        }
        return cap;
    }
    
    public static boolean blockAbilities(Player player, List<String> abilitiesToBlock, Location loc, double radius) {
        /**
         * Cycles through a list of ability names to check if any instances of
         * the abilities exist at a specific location. If an instance of the
         * ability is found then it will be removed, with the exception
         * FireShield, and AirShield.
         */
        boolean hasBlocked = false;
        for (String ability : abilitiesToBlock) {
            if (ability.equalsIgnoreCase("FireBlast")) {
                hasBlocked = FireBlast.annihilateBlasts(loc, radius, player) || hasBlocked;
            } else if (ability.equalsIgnoreCase("EarthBlast")) {
                hasBlocked = EarthBlast.annihilateBlasts(loc, radius, player) || hasBlocked;
            } else if (ability.equalsIgnoreCase("WaterManipulation")) {
                hasBlocked = WaterManipulation.annihilateBlasts(loc, radius, player) || hasBlocked;
            } else if (ability.equalsIgnoreCase("AirSwipe")) {
                hasBlocked = AirSwipe.removeSwipesAroundPoint(loc, radius) || hasBlocked;
            } else if (ability.equalsIgnoreCase("Combustion")) {
                hasBlocked = Combustion.removeAroundPoint(loc, radius) || hasBlocked;
            } else if (ability.equalsIgnoreCase("FireShield")) {
                hasBlocked = FireShield.isWithinShield(loc) || hasBlocked;
            } else if (ability.equalsIgnoreCase("AirShield")) {
                hasBlocked = AirShield.isWithinShield(loc) || hasBlocked;
            } else if (ability.equalsIgnoreCase("WaterSpout")) {
                hasBlocked = WaterSpout.removeSpouts(loc, radius, player) || hasBlocked;
            } else if (ability.equalsIgnoreCase("AirSpout")) {
                hasBlocked = AirSpout.removeSpouts(loc, radius, player) || hasBlocked;
            } else if (ability.equalsIgnoreCase("Twister")) {
                hasBlocked = AirCombo.removeAroundPoint(player, "Twister", loc, radius) || hasBlocked;
            } else if (ability.equalsIgnoreCase("AirStream")) {
                hasBlocked = AirCombo.removeAroundPoint(player, "AirStream", loc, radius) || hasBlocked;
            } else if (ability.equalsIgnoreCase("AirSweep")) {
                hasBlocked = AirCombo.removeAroundPoint(player, "AirSweep", loc, radius) || hasBlocked;
            } else if (ability.equalsIgnoreCase("FireKick")) {
                hasBlocked = FireCombo.removeAroundPoint(player, "FireKick", loc, radius) || hasBlocked;
            } else if (ability.equalsIgnoreCase("FireSpin")) {
                hasBlocked = FireCombo.removeAroundPoint(player, "FireSpin", loc, radius) || hasBlocked;
            } else if (ability.equalsIgnoreCase("FireWheel")) {
                hasBlocked = FireCombo.removeAroundPoint(player, "FireWheel", loc, radius) || hasBlocked;
            }
        }
        return hasBlocked;
    }
    
    public static boolean isWithinShields(Location loc) {
        List<String> list = new ArrayList<String>();
        list.add("FireShield");
        list.add("AirShield");
        return blockAbilities(null, list, loc, 0);
    }
    
    public static boolean hasRPG() {
        return Bukkit.getServer().getPluginManager().getPlugin("ProjectKorraRPG") != null;
    }
    
    public static Plugin getRPG() {
        return (hasRPG() ? Bukkit.getServer().getPluginManager().getPlugin("ProjectKorraRPG") : null);
    }
    
    public static boolean hasItems() {
        return Bukkit.getServer().getPluginManager().getPlugin("ProjectKorraItems") != null;
    }
    
    public static Plugin getItems() {
        return (hasItems() ? Bukkit.getServer().getPluginManager().getPlugin("ProjectKorraItems") : null);
    }
    
    public static void writeToDebug(String message) {
        try {
            File dataFolder = plugin.getDataFolder();
            
            if (!dataFolder.exists()) {
                dataFolder.mkdir();
            }
            
            File saveTo = new File(plugin.getDataFolder(), "debug.txt");
            
            if (!saveTo.exists()) {
                saveTo.createNewFile();
            }
            
            FileWriter fw = new FileWriter(saveTo, true);
            PrintWriter pw = new PrintWriter(fw);
            pw.println(message);
            pw.flush();
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static String getCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
    
    public static void runDebug() {
        File debugFile = new File(plugin.getDataFolder(), "debug.txt");
        
        if (debugFile.exists()) {
            debugFile.delete(); // We're starting brand new.
        }
        
        writeToDebug("ProjectKorra Debug: Paste this on http://pastie.org and put it in your bug report thread.");
        writeToDebug("====================");
        writeToDebug("");
        writeToDebug("");
        writeToDebug("Date Created: " + getCurrentDate());
        writeToDebug("Bukkit Version: " + Bukkit.getServer().getVersion());
        writeToDebug("");
        writeToDebug("ProjectKorra (Core) Information");
        writeToDebug("====================");
        writeToDebug("Version: " + plugin.getDescription().getVersion());
        writeToDebug("Author: " + plugin.getDescription().getAuthors());
        
        if (hasRPG()) {
            writeToDebug("");
            writeToDebug("ProjectKorra (RPG) Information");
            writeToDebug("====================");
            writeToDebug("Version: " + getRPG().getDescription().getVersion());
            writeToDebug("Author: " + getRPG().getDescription().getAuthors());
        }
        
        if (hasItems()) {
            writeToDebug("");
            writeToDebug("ProjectKorra (Items) Information");
            writeToDebug("====================");
            writeToDebug("Version: " + getItems().getDescription().getVersion());
            writeToDebug("Author: " + getItems().getDescription().getAuthors());
        }
        
        writeToDebug("");
        writeToDebug("Ability Information");
        writeToDebug("====================");
        
        for (String ability : AbilityModuleManager.abilities) {
            if (StockAbilities.isStockAbility(ability) && !Methods.isDisabledStockAbility(ability)) {
                writeToDebug(ability + " - STOCK ABILITY");
            } else {
                writeToDebug(ability + " - UNOFFICIAL ABILITY");
            }
        }
        
        writeToDebug("");
        writeToDebug("Supported Plugins");
        writeToDebug("====================");
        
        boolean respectWorldGuard = plugin.getConfig().getBoolean("Properties.RegionProtection.RespectWorldGuard");
        boolean respectPreciousStones = plugin.getConfig().getBoolean("Properties.RegionProtection.RespectPreciousStones");
        boolean respectFactions = plugin.getConfig().getBoolean("Properties.RegionProtection.RespectFactions");
        boolean respectTowny = plugin.getConfig().getBoolean("Properties.RegionProtection.RespectTowny");
        boolean respectGriefPrevention = plugin.getConfig().getBoolean("Properties.RegionProtection.RespectGriefPrevention");
        boolean respectLWC = plugin.getConfig().getBoolean("Properties.RegionProtection.RespectLWC");
        PluginManager pm = Bukkit.getPluginManager();
        
        Plugin wgp = pm.getPlugin("WorldGuard");
        Plugin psp = pm.getPlugin("PreciousStones");
        Plugin fcp = pm.getPlugin("Factions");
        Plugin twnp = pm.getPlugin("Towny");
        Plugin gpp = pm.getPlugin("GriefPrevention");
        Plugin massivecore = pm.getPlugin("MassiveCore");
        Plugin lwc = pm.getPlugin("LWC");
        
        if (wgp != null && respectWorldGuard) {
            writeToDebug("WorldGuard v" + wgp.getDescription().getVersion());
        }
        
        if (psp != null && respectPreciousStones) {
            writeToDebug("PreciousStones v" + psp.getDescription().getVersion());
        }
        
        if (fcp != null && respectFactions) {
            writeToDebug("Factions v" + fcp.getDescription().getVersion());
        }
        
        if (massivecore != null && respectFactions) {
            writeToDebug("MassiveCore v" + massivecore.getDescription().getVersion());
        }
        
        if (twnp != null && respectTowny) {
            writeToDebug("Towny v" + twnp.getDescription().getVersion());
        }
        
        if (gpp != null && respectGriefPrevention) {
            writeToDebug("GriefPrevention v" + gpp.getDescription().getVersion());
        }
        
        if (lwc != null && respectLWC) {
            writeToDebug("LWC v" + lwc.getDescription().getVersion());
        }
        
        writeToDebug("");
        writeToDebug("Plugins Hooking Into ProjectKorra (Core)");
        writeToDebug("====================");
        
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin.getDescription().getDepend() != null && plugin.getDescription().getDepend().contains("ProjectKorra")) {
                writeToDebug(plugin.getDescription().getName() + " v" + plugin.getDescription().getVersion());
            }
        }
    }
    
    public static boolean canFly(Player player, boolean first, boolean hovering) {
        BendingPlayer bender = getBendingPlayer(player.getName());
        
        if (!player.isOnline()) {
            return false;
        }
        
        if (!player.isSneaking()) {
            if (first) {
            } else if (!hovering) {
                return false;
            }
        }
        if (bender.isChiBlocked())
            return false;
        if (!player.isOnline())
            return false;
        if (bender.isPermaRemoved())
            return false;
        if (!bender.getElements().contains(Element.Air))
            return false;
        if (!canBend(player.getName(), "Flight"))
            return false;
        if (!getBoundAbility(player).equalsIgnoreCase("Flight"))
            return false;
        if (isRegionProtectedFromBuild(player, "Flight", player.getLocation()))
            return false;
        if (player.getLocation().subtract(0, 0.5, 0).getBlock().getType() != Material.AIR)
            return false;
        return true;
    }
    
    public static class BlockCacheElement {
        private Player player;
        private Block block;
        private String ability;
        private boolean allowed;
        private long time;
        
        public BlockCacheElement(Player player, Block block, String ability, boolean allowed, long time) {
            this.player = player;
            this.block = block;
            this.ability = ability;
            this.allowed = allowed;
            this.time = time;
        }
        
        public Player getPlayer() {
            return player;
        }
        
        public void setPlayer(Player player) {
            this.player = player;
        }
        
        public Block getBlock() {
            return block;
        }
        
        public void setBlock(Block block) {
            this.block = block;
        }
        
        public long getTime() {
            return time;
        }
        
        public void setTime(long time) {
            this.time = time;
        }
        
        public boolean isAllowed() {
            return allowed;
        }
        
        public void setAllowed(boolean allowed) {
            this.allowed = allowed;
        }
        
        public String getAbility() {
            return ability;
        }
        
        public void setAbility(String ability) {
            this.ability = ability;
        }
        
    }
    
    public static void startCacheCleaner(final double period) {
        new BukkitRunnable() {
            public void run() {
                for (ConcurrentHashMap<Block, BlockCacheElement> map : blockProtectionCache.values()) {
                    for (Iterator<Block> i = map.keySet().iterator(); i.hasNext();) {
                        Block key = i.next();
                        BlockCacheElement value = map.get(key);
                        
                        if (System.currentTimeMillis() - value.getTime() > period) {
                            map.remove(key);
                        }
                    }
                }
            }
        }.runTaskTimer(ProjectKorra.plugin, 0, (long) (period / 20));
    }
    
    /** Checks if an entity is Undead **/
    public static boolean isUndead(Entity entity) {
        return entity != null && (entity.getType() == EntityType.ZOMBIE
                || entity.getType() == EntityType.BLAZE
                || entity.getType() == EntityType.GIANT
                || entity.getType() == EntityType.IRON_GOLEM
                || entity.getType() == EntityType.MAGMA_CUBE
                || entity.getType() == EntityType.PIG_ZOMBIE
                || entity.getType() == EntityType.SKELETON
                || entity.getType() == EntityType.SLIME
                || entity.getType() == EntityType.SNOWMAN
                || entity.getType() == EntityType.ZOMBIE);
    }
    
}

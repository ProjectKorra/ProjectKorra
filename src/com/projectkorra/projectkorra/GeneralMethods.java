package com.projectkorra.projectkorra;

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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.FallingSand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
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
import com.projectkorra.projectkorra.ability.AbilityModule;
import com.projectkorra.projectkorra.ability.AbilityModuleManager;
import com.projectkorra.projectkorra.ability.StockAbility;
import com.projectkorra.projectkorra.ability.combo.ComboAbilityModule;
import com.projectkorra.projectkorra.ability.combo.ComboManager;
import com.projectkorra.projectkorra.ability.combo.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.ability.combo.ComboManager.ComboAbility;
import com.projectkorra.projectkorra.ability.combo.ComboModuleManager;
import com.projectkorra.projectkorra.ability.multiability.MultiAbilityManager;
import com.projectkorra.projectkorra.ability.multiability.MultiAbilityModuleManager;
import com.projectkorra.projectkorra.airbending.AirCombo;
import com.projectkorra.projectkorra.airbending.AirMethods;
import com.projectkorra.projectkorra.airbending.AirShield;
import com.projectkorra.projectkorra.airbending.AirSpout;
import com.projectkorra.projectkorra.airbending.AirSwipe;
import com.projectkorra.projectkorra.chiblocking.ChiMethods;
import com.projectkorra.projectkorra.chiblocking.Paralyze;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.EarthBlast;
import com.projectkorra.projectkorra.earthbending.EarthMethods;
import com.projectkorra.projectkorra.earthbending.EarthPassive;
import com.projectkorra.projectkorra.earthbending.MetalClips;
import com.projectkorra.projectkorra.event.BendingReloadEvent;
import com.projectkorra.projectkorra.event.PlayerBendingDeathEvent;
import com.projectkorra.projectkorra.firebending.Combustion;
import com.projectkorra.projectkorra.firebending.FireBlast;
import com.projectkorra.projectkorra.firebending.FireCombo;
import com.projectkorra.projectkorra.firebending.FireMethods;
import com.projectkorra.projectkorra.firebending.FireShield;
import com.projectkorra.projectkorra.object.Preset;
import com.projectkorra.projectkorra.storage.DBConnection;
import com.projectkorra.projectkorra.util.CraftingRecipes;
import com.projectkorra.projectkorra.util.Flight;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.Bloodbending;
import com.projectkorra.projectkorra.waterbending.FreezeMelt;
import com.projectkorra.projectkorra.waterbending.WaterCombo;
import com.projectkorra.projectkorra.waterbending.WaterManipulation;
import com.projectkorra.projectkorra.waterbending.WaterMethods;
import com.projectkorra.projectkorra.waterbending.WaterSpout;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;

@SuppressWarnings("deprecation")
public class GeneralMethods {

	static ProjectKorra plugin;

	private static FileConfiguration config = ProjectKorra.plugin.getConfig();
	public static Random rand = new Random();

	public static double CACHE_TIME = config.getDouble("Properties.RegionProtection.CacheBlockTime");
	public static ConcurrentHashMap<String, Long> cooldowns = new ConcurrentHashMap<String, Long>();

	// Represents PlayerName, previously checked blocks, and whether they were true or false
	public static ConcurrentHashMap<String, ConcurrentHashMap<Block, BlockCacheElement>> blockProtectionCache = new ConcurrentHashMap<String, ConcurrentHashMap<Block, BlockCacheElement>>();

	public static Integer[] nonOpaque = { 0, 6, 8, 9, 10, 11, 27, 28, 30, 31, 32, 37, 38, 39, 40, 50, 51, 55, 59, 66, 68, 69, 70, 72, 75, 76, 77, 78, 83, 90, 93, 94, 104, 105, 106, 111, 115, 119, 127, 131, 132, 175 };

	// Stands for toggled = false while logging out
	public static List<UUID> toggedOut = new ArrayList<UUID>();

	public GeneralMethods(ProjectKorra plugin) {
		GeneralMethods.plugin = plugin;
		new AirMethods(plugin);
		new ChiMethods(plugin);
		new EarthMethods(plugin);
		new FireMethods(plugin);
		new WaterMethods(plugin);
	}

	/**
	 * Checks to see if an AbilityExists. Uses method
	 * {@link #getAbility(String)} to check if it exists.
	 * 
	 * @param string Ability Name
	 * @return true if ability exists
	 */
	public static boolean abilityExists(String string) {
		for (String st : AbilityModuleManager.abilities) {
			if (string.equalsIgnoreCase(st))
				return true;
		}
		return false;
	}

	/**
	 * Binds a Ability to the hotbar slot that the player is on.
	 * 
	 * @param player The player to bind to
	 * @param ability The ability name to Bind
	 * @see #bindAbility(Player, String, int)
	 */
	public static void bindAbility(Player player, String ability) {
		int slot = player.getInventory().getHeldItemSlot() + 1;
		bindAbility(player, ability, slot);
	}

	/**
	 * Binds a Ability to a specific hotbar slot.
	 * 
	 * @param player The player to bind to
	 * @param ability
	 * @param slot
	 * @see #bindAbility(Player, String)
	 */
	public static void bindAbility(Player player, String ability, int slot) {
		if (MultiAbilityManager.playerAbilities.containsKey(player)) {
			player.sendMessage(ChatColor.RED + "You can't edit your binds right now!");
			return;
		}

		BendingPlayer bPlayer = getBendingPlayer(player.getName());
		bPlayer.getAbilities().put(slot, ability);
		if (AirMethods.isAirAbility(ability)) {
			player.sendMessage(AirMethods.getAirColor() + "Succesfully bound " + ability + " to slot " + slot);
		} else if (WaterMethods.isWaterAbility(ability)) {
			player.sendMessage(WaterMethods.getWaterColor() + "Succesfully bound " + ability + " to slot " + slot);
		} else if (EarthMethods.isEarthAbility(ability)) {
			player.sendMessage(EarthMethods.getEarthColor() + "Succesfully bound " + ability + " to slot " + slot);
		} else if (FireMethods.isFireAbility(ability)) {
			player.sendMessage(FireMethods.getFireColor() + "Succesfully bound " + ability + " to slot " + slot);
		} else if (ChiMethods.isChiAbility(ability)) {
			player.sendMessage(ChiMethods.getChiColor() + "Succesfully bound " + ability + " to slot " + slot);
		} else {
			player.sendMessage(getAvatarColor() + "Successfully bound " + ability + " to slot " + slot);
		}
		saveAbility(bPlayer, slot, ability);
	}

	/**
	 * Cycles through a list of ability names to check if any instances of the
	 * abilities exist at a specific location. If an instance of the ability is
	 * found then it will be removed, with the exception FireShield, and
	 * AirShield.
	 */
	public static boolean blockAbilities(Player player, List<String> abilitiesToBlock, Location loc, double radius) {
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

	/**
	 * Breaks a block and sets it to {@link Material#AIR AIR}.
	 * 
	 * @param block The block to break
	 */
	public static void breakBlock(Block block) {
		block.breakNaturally(new ItemStack(Material.AIR));
	}

	/**
	 * Checks to see if a Player can bend a specific Ability.
	 * 
	 * @param player The player name to check
	 * @param ability The Ability name to check
	 * @return true If player can bend specified ability and has the permissions
	 *         to do so
	 */
	public static boolean canBend(String player, String ability) {
		BendingPlayer bPlayer = getBendingPlayer(player);
		Player p = Bukkit.getPlayer(player);
		if (bPlayer == null)
			return false;
		if (plugin.getConfig().getStringList("Properties.DisabledWorlds") != null && plugin.getConfig().getStringList("Properties.DisabledWorlds").contains(p.getWorld().getName()))
			return false;
		if (Commands.isToggledForAll)
			return false;
		if (!bPlayer.isToggled())
			return false;
		if (p == null)
			return false;
		if (cooldowns.containsKey(p.getName())) {
			if (cooldowns.get(p.getName()) + ProjectKorra.plugin.getConfig().getLong("Properties.GlobalCooldown") >= System.currentTimeMillis()) {
				return false;
			}
			cooldowns.remove(p.getName());
		}
		if (bPlayer.isChiBlocked())
			return false;
		if (!p.hasPermission("bending.ability." + ability))
			return false;
		if (!canBind(player, ability))
			return false;
		
		if (isRegionProtectedFromBuild(p, ability, p.getLocation()))
			return false;
		if (Paralyze.isParalyzed(p) || Bloodbending.isBloodbended(p))
			return false;
		if (MetalClips.isControlled(p))
			return false;
		if (BendingManager.events.get(p.getWorld()) != null && BendingManager.events.get(p.getWorld()).equalsIgnoreCase("SolarEclipse") && FireMethods.isFireAbility(ability))
			return false;
		if (BendingManager.events.get(p.getWorld()) != null && BendingManager.events.get(p.getWorld()).equalsIgnoreCase("LunarEclipse") && WaterMethods.isWaterAbility(ability))
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
		if (!bPlayer.isToggled())
			return false;
		if (!bPlayer.hasElement(element))
			return false;
		if (isRegionProtectedFromBuild(p, null, p.getLocation()))
			return false;
		if (bPlayer.isChiBlocked())
			return false;
		return true;
	}

	public static boolean canBind(String player, String ability) {
		Player p = Bukkit.getPlayer(player);
		if (p == null)
			return false;
		if (!p.hasPermission("bending.ability." + ability))
			return false;
		if (AirMethods.isAirAbility(ability) && !isBender(player, Element.Air))
			return false;
		if (WaterMethods.isWaterAbility(ability) && !isBender(player, Element.Water))
			return false;
		if (EarthMethods.isEarthAbility(ability) && !isBender(player, Element.Earth))
			return false;
		if (FireMethods.isFireAbility(ability) && !isBender(player, Element.Fire))
			return false;
		if (ChiMethods.isChiAbility(ability) && !isBender(player, Element.Chi))
			return false;
		
		if (!EarthMethods.canLavabend(p) && EarthMethods.isLavabendingAbility(ability)) 
			return false;
		else if (!EarthMethods.canMetalbend(p) && EarthMethods.isMetalbendingAbility(ability)) 
			return false;
		else if (!EarthMethods.canSandbend(p) && EarthMethods.isSandbendingAbility(ability)) 
			return false;
		else if (!AirMethods.canAirFlight(p) && AirMethods.isFlightAbility(ability)) 
			return false;
		else if (!AirMethods.canUseSpiritualProjection(p) && AirMethods.isSpiritualProjectionAbility(ability)) 
			return false;
		else if (!FireMethods.canCombustionbend(p) && FireMethods.isCombustionbendingAbility(ability)) 
			return false;
		else if (!FireMethods.canLightningbend(p) && FireMethods.isLightningbendingAbility(ability)) 
			return false;
		else if (!WaterMethods.canBloodbend(p) && WaterMethods.isBloodbendingAbility(ability)) 
			return false;
		else if (!WaterMethods.canIcebend(p) && WaterMethods.isIcebendingAbility(ability)) 
			return false;
		else if (!WaterMethods.canWaterHeal(p) && WaterMethods.isHealingAbility(ability)) 
			return false;
		else if (!WaterMethods.canPlantbend(p) && WaterMethods.isPlantbendingAbility(ability)) 
			return false;
		return true;
	}

	public static boolean canView(Player player, String ability) {
		return player.hasPermission("bending.ability." + ability);
	}

	public static boolean comboExists(String string) {
		/*Previous method only returned non-stock combos. Reason we use descriptions is because that
		 * contains all valid combos. Not technical ones like IceBulletLeftClick, etc.*/
		for (String s : ComboManager.descriptions.keySet()) {
			if (s.equalsIgnoreCase(string))
				return true;
		}
		return false;
	}

	/**
	 * Creates a {@link BendingPlayer} with the data from the database. This
	 * runs when a player logs in.
	 * 
	 * @param uuid The UUID of the player
	 * @param player The player name
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

				p = (permaremoved != null && (permaremoved.equals("true")));

				final boolean boolean_p = p;
				new BukkitRunnable() {
					@Override
					public void run() {
						new BendingPlayer(uuid, player, elements, abilities, boolean_p);
					}
				}.runTask(ProjectKorra.plugin);
			}
		}
		catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Damages an Entity by amount of damage specified. Starts a
	 * {@link EntityDamageByEntityEvent}.
	 * 
	 * @param player The player dealing the damage
	 * @param entity The entity that is receiving the damage
	 * @param damage The amount of damage to deal
	 */
	public static void damageEntity(Player player, Entity entity, double damage) {
		damageEntity(player, entity, damage, null);
	}

	/**
	 * Damages an Entity by amount of damage specified. Starts a
	 * {@link EntityDamageByEntityEvent}.
	 * 
	 * @param player The player dealing the damage
	 * @param entity The entity that is receiving the damage
	 * @param damage The amount of damage to deal
	 * @param ability The ability that is used to damage the entity
	 */
	public static void damageEntity(Player player, Entity entity, double damage, String ability) {
		if (entity instanceof LivingEntity) {
			if (entity instanceof Player) {
				if (Commands.invincible.contains(entity.getName()))
					return;
			}
			if (Bukkit.getPluginManager().isPluginEnabled("NoCheatPlus")) {
				NCPExemptionManager.exemptPermanently(player, CheckType.FIGHT_REACH);
			}
			if (((LivingEntity) entity).getHealth() - damage <= 0 && entity instanceof Player && !entity.isDead()) {
				PlayerBendingDeathEvent event = new PlayerBendingDeathEvent((Player) entity, player, ability, damage);
				Bukkit.getServer().getPluginManager().callEvent(event);
			}
			((LivingEntity) entity).damage(damage, player);
			entity.setLastDamageCause(new EntityDamageByEntityEvent(player, entity, DamageCause.CUSTOM, damage));
			if (Bukkit.getPluginManager().isPluginEnabled("NoCheatPlus")) {
				NCPExemptionManager.unexempt(player);
			}
		}
	}

	/**
	 * Deserializes the configuration file "bendingPlayers.yml" of the old
	 * BendingPlugin and creates a converted.yml ready for conversion.
	 * 
	 * @throws IOException If the "bendingPlayers.yml" file is not found
	 */
	public static void deserializeFile() {
		File readFile = new File(".", "bendingPlayers.yml");
		File writeFile = new File(".", "converted.yml");
		if (readFile.exists()) {
			try (
					DataInputStream input = new DataInputStream(new FileInputStream(readFile)); 
					BufferedReader reader = new BufferedReader(new InputStreamReader(input));

					DataOutputStream output = new DataOutputStream(new FileOutputStream(writeFile)); 
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output))
				) {

				String line;
				while ((line = reader.readLine()) != null) {
					if (!line.trim().contains("==: BendingPlayer")) {
						writer.write(line + "\n");
					}
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void displayColoredParticle(Location loc, ParticleEffect type, String hexVal, float xOffset, float yOffset, float zOffset) {
		int R = 0;
		int G = 0;
		int B = 0;

		if (hexVal.length() <= 6) {
			R = Integer.valueOf(hexVal.substring(0, 2), 16);
			G = Integer.valueOf(hexVal.substring(2, 4), 16);
			B = Integer.valueOf(hexVal.substring(4, 6), 16);
			if (R <= 0)
				R = 1;
		} else if (hexVal.length() <= 7 && hexVal.substring(0, 1).equals("#")) {
			R = Integer.valueOf(hexVal.substring(1, 3), 16);
			G = Integer.valueOf(hexVal.substring(3, 5), 16);
			B = Integer.valueOf(hexVal.substring(5, 7), 16);
			if (R <= 0)
				R = 1;
		}

		loc.setX(loc.getX() + Math.random() * (xOffset / 2 - -(xOffset / 2)));
		loc.setY(loc.getY() + Math.random() * (yOffset / 2 - -(yOffset / 2)));
		loc.setZ(loc.getZ() + Math.random() * (zOffset / 2 - -(zOffset / 2)));

		if (type == ParticleEffect.RED_DUST || type == ParticleEffect.REDSTONE)
			ParticleEffect.RED_DUST.display((float) R, (float) G, (float) B, 0.004F, 0, loc, 257D);
		else if (type == ParticleEffect.SPELL_MOB || type == ParticleEffect.MOB_SPELL)
			ParticleEffect.SPELL_MOB.display((float) 255 - R, (float) 255 - G, (float) 255 - B, 1, 0, loc, 257D);
		else if (type == ParticleEffect.SPELL_MOB_AMBIENT || type == ParticleEffect.MOB_SPELL_AMBIENT)
			ParticleEffect.SPELL_MOB_AMBIENT.display((float) 255 - R, (float) 255 - G, (float) 255 - B, 1, 0, loc, 257D);
		else
			ParticleEffect.RED_DUST.display((float) 0, (float) 0, (float) 0, 0.004F, 0, loc, 257D);
	}

	public static void displayColoredParticle(Location loc, String hexVal) {
		int R = 0;
		int G = 0;
		int B = 0;

		if (hexVal.length() <= 6) {
			R = Integer.valueOf(hexVal.substring(0, 2), 16);
			G = Integer.valueOf(hexVal.substring(2, 4), 16);
			B = Integer.valueOf(hexVal.substring(4, 6), 16);
			if (R <= 0)
				R = 1;
		} else if (hexVal.length() <= 7 && hexVal.substring(0, 1).equals("#")) {
			R = Integer.valueOf(hexVal.substring(1, 3), 16);
			G = Integer.valueOf(hexVal.substring(3, 5), 16);
			B = Integer.valueOf(hexVal.substring(5, 7), 16);
			if (R <= 0)
				R = 1;
		}
		ParticleEffect.RED_DUST.display((float) R, (float) G, (float) B, 0.004F, 0, loc, 257D);
	}

	public static void displayColoredParticle(Location loc, String hexVal, float xOffset, float yOffset, float zOffset) {
		int R = 0;
		int G = 0;
		int B = 0;

		if (hexVal.length() <= 6) {
			R = Integer.valueOf(hexVal.substring(0, 2), 16);
			G = Integer.valueOf(hexVal.substring(2, 4), 16);
			B = Integer.valueOf(hexVal.substring(4, 6), 16);
			if (R <= 0)
				R = 1;
		} else if (hexVal.length() <= 7 && hexVal.substring(0, 1).equals("#")) {
			R = Integer.valueOf(hexVal.substring(1, 3), 16);
			G = Integer.valueOf(hexVal.substring(3, 5), 16);
			B = Integer.valueOf(hexVal.substring(5, 7), 16);
			if (R <= 0)
				R = 1;
		}

		loc.setX(loc.getX() + Math.random() * (xOffset / 2 - -(xOffset / 2)));
		loc.setY(loc.getY() + Math.random() * (yOffset / 2 - -(yOffset / 2)));
		loc.setZ(loc.getZ() + Math.random() * (zOffset / 2 - -(zOffset / 2)));

		ParticleEffect.RED_DUST.display((float) R, (float) G, (float) B, 0.004F, 0, loc, 257D);
	}

	public static void displayParticleVector(Location loc, ParticleEffect type, float xTrans, float yTrans, float zTrans) {
		if (type == ParticleEffect.FIREWORKS_SPARK)
			ParticleEffect.FIREWORKS_SPARK.display(xTrans, yTrans, zTrans, 0.09F, 0, loc, 257D);
		else if (type == ParticleEffect.SMOKE || type == ParticleEffect.SMOKE_NORMAL)
			ParticleEffect.SMOKE.display(xTrans, yTrans, zTrans, 0.04F, 0, loc, 257D);
		else if (type == ParticleEffect.LARGE_SMOKE || type == ParticleEffect.SMOKE_LARGE)
			ParticleEffect.LARGE_SMOKE.display(xTrans, yTrans, zTrans, 0.04F, 0, loc, 257D);
		else if (type == ParticleEffect.ENCHANTMENT_TABLE)
			ParticleEffect.ENCHANTMENT_TABLE.display(xTrans, yTrans, zTrans, 0.5F, 0, loc, 257D);
		else if (type == ParticleEffect.PORTAL)
			ParticleEffect.PORTAL.display(xTrans, yTrans, zTrans, 0.5F, 0, loc, 257D);
		else if (type == ParticleEffect.FLAME)
			ParticleEffect.FLAME.display(xTrans, yTrans, zTrans, 0.04F, 0, loc, 257D);
		else if (type == ParticleEffect.CLOUD)
			ParticleEffect.CLOUD.display(xTrans, yTrans, zTrans, 0.04F, 0, loc, 257D);
		else if (type == ParticleEffect.SNOW_SHOVEL)
			ParticleEffect.SNOW_SHOVEL.display(xTrans, yTrans, zTrans, 0.2F, 0, loc, 257D);
		else
			ParticleEffect.RED_DUST.display((float) 0, (float) 0, (float) 0, 0.004F, 0, loc, 257D);
	}

	/**
	 * Drops a {@code Collection<ItemStack>} of items on a specified block.
	 * 
	 * @param block The block to drop items on.
	 * @param items The items to drop.
	 */
	public static void dropItems(Block block, Collection<ItemStack> items) {
		for (ItemStack item : items)
			block.getWorld().dropItem(block.getLocation(), item);
	}

	/**
	 * Gets the ability from specified ability name.
	 * 
	 * @param string The ability name
	 * @return Ability name if found in {@link AbilityModuleManager#abilities}
	 *         <p>
	 *         else null
	 *         </p>
	 */
	public static String getAbility(String string) {
		for (String st : AbilityModuleManager.abilities) {
			if (st.equalsIgnoreCase(string))
				return st;
		}
		return null;
	}

	/**
	 * Gets the Element color from the Ability name specified.
	 * 
	 * @param ability The ability name
	 * @return {@link ChiMethods#getChiColor()} <br />
	 *         {@link AirMethods#getAirColor()} <br />
	 *         {@link WaterMethods#getWaterColor()} <br />
	 *         {@link EarthMethods#getEarthColor()} <br />
	 *         {@link FireMethods#getFireColor()} <br />
	 *         else {@link #getAvatarColor()}
	 */
	public static ChatColor getAbilityColor(String ability) {
		if (AbilityModuleManager.chiabilities.contains(ability))
			return ChiMethods.getChiColor();
		if (AbilityModuleManager.airbendingabilities.contains(ability)) {
			if (AbilityModuleManager.subabilities.contains(ability))
				return getSubBendingColor(Element.Air);
			return AirMethods.getAirColor();
		}
		if (AbilityModuleManager.waterbendingabilities.contains(ability)) {
			if (AbilityModuleManager.subabilities.contains(ability))
				return getSubBendingColor(Element.Water);
			return WaterMethods.getWaterColor();
		}
		if (AbilityModuleManager.earthbendingabilities.contains(ability)) {
			if (AbilityModuleManager.subabilities.contains(ability))
				return getSubBendingColor(Element.Earth);
			return EarthMethods.getEarthColor();
		}
		if (AbilityModuleManager.firebendingabilities.contains(ability)) {
			if (AbilityModuleManager.subabilities.contains(ability))
				return getSubBendingColor(Element.Fire);
			return FireMethods.getFireColor();
		}

		else
			return getAvatarColor();
	}

	/**
	 * Returns the element an ability belongs to.
	 * 
	 * @param ability
	 * @return the element
	 */
	public static Element getAbilityElement(String ability) {
		if (AbilityModuleManager.airbendingabilities.contains(ability))
			return Element.Air;
		if (AbilityModuleManager.earthbendingabilities.contains(ability))
			return Element.Earth;
		if (AbilityModuleManager.firebendingabilities.contains(ability))
			return Element.Fire;
		if (AbilityModuleManager.waterbendingabilities.contains(ability))
			return Element.Water;
		if (AbilityModuleManager.chiabilities.contains(ability))
			return Element.Chi;
		return null;
	}
	
	
	/**
	 * Returns the subelement of the ability if applicable.
	 * 
	 * @param ability
	 * @return SubElement
	 * */
	public static SubElement getAbilitySubElement(String ability) {
		if (AbilityModuleManager.bloodabilities.contains(ability)) return SubElement.Bloodbending;
		if (AbilityModuleManager.iceabilities.contains(ability)) return SubElement.Icebending;
		if (AbilityModuleManager.plantabilities.contains(ability)) return SubElement.Plantbending;
		if (AbilityModuleManager.healingabilities.contains(ability)) return SubElement.Healing;
		if (AbilityModuleManager.sandabilities.contains(ability)) return SubElement.Sandbending;
		if (AbilityModuleManager.metalabilities.contains(ability)) return SubElement.Metalbending;
		if (AbilityModuleManager.lavaabilities.contains(ability)) return SubElement.Lavabending;
		if (AbilityModuleManager.lightningabilities.contains(ability)) return SubElement.Lightning;
		if (AbilityModuleManager.combustionabilities.contains(ability)) return SubElement.Combustion;
		if (AbilityModuleManager.spiritualprojectionabilities.contains(ability)) return SubElement.SpiritualProjection;
		if (AbilityModuleManager.flightabilities.contains(ability)) return SubElement.Flight;
		return null;
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
	 * Attempts to get a {@link BendingPlayer} from specified player name. this
	 * method tries to get a {@link Player} object and gets the uuid and then
	 * calls {@link #getBendingPlayer(UUID)}
	 * 
	 * @param playerName The name of the Player
	 * @return The BendingPlayer object if {@link BendingPlayer#players}
	 *         contains the player name
	 * 
	 * @see #getBendingPlayer(UUID)
	 */
	public static BendingPlayer getBendingPlayer(String playerName) {
		OfflinePlayer player = Bukkit.getPlayer(playerName);
		if (player == null) {
			player = Bukkit.getOfflinePlayer(playerName);
		}
		return getBendingPlayer(player.getUniqueId());
	}

	public static BendingPlayer getBendingPlayer(UUID uuid) {
		return BendingPlayer.getPlayers().get(uuid);
	}

	public static BendingPlayer getBendingPlayer(Player player) {
		return getBendingPlayer(player.getUniqueId());
	}

	public static List<Block> getBlocksAlongLine(Location ploc, Location tloc, World w) {
		List<Block> blocks = new ArrayList<Block>();

		//Next we will name each coordinate
		int x1 = ploc.getBlockX();
		int y1 = ploc.getBlockY();
		int z1 = ploc.getBlockZ();

		int x2 = tloc.getBlockX();
		int y2 = tloc.getBlockY();
		int z2 = tloc.getBlockZ();

		//Then we create the following integers
		int xMin, yMin, zMin;
		int xMax, yMax, zMax;
		int x, y, z;

		//Now we need to make sure xMin is always lower then xMax
		if (x1 > x2) { //If x1 is a higher number then x2
			xMin = x2;
			xMax = x1;
		} else {
			xMin = x1;
			xMax = x2;
		}
		//Same with Y
		if (y1 > y2) {
			yMin = y2;
			yMax = y1;
		} else {
			yMin = y1;
			yMax = y2;
		}

		//And Z
		if (z1 > z2) {
			zMin = z2;
			zMax = z1;
		} else {
			zMin = z1;
			zMax = z2;
		}

		//Now it's time for the loop
		for (x = xMin; x <= xMax; x++) {
			for (y = yMin; y <= yMax; y++) {
				for (z = zMin; z <= zMax; z++) {
					Block b = new Location(w, x, y, z).getBlock();
					blocks.add(b);
				}
			}
		}

		//And last but not least, we return with the list
		return blocks;
	}

	/**
	 * Gets a {@code List<Blocks>} within the specified radius around the
	 * specified location.
	 * 
	 * @param location The base location
	 * @param radius The block radius from location to include within the list
	 *            of blocks
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
					if (block.getLocation().distance(location) <= radius) {
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
	 * @param player The player to check
	 * @return The Ability name bounded to the slot
	 *         <p>
	 *         else null
	 *         </p>
	 */
	public static String getBoundAbility(Player player) {
		BendingPlayer bPlayer = getBendingPlayer(player.getName());
		if (bPlayer == null)
			return null;
		int slot = player.getInventory().getHeldItemSlot() + 1;
		return bPlayer.getAbilities().get(slot);
	}

	public static BlockFace getCardinalDirection(Vector vector) {
		BlockFace[] faces = { BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST };
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
	
	/**Returns the ChatColor that should be associated with the combo name.
	 * @param combo
	 * @return The ChatColor to be used*/
	public static ChatColor getComboColor(String combo) {
		for (ComboAbility comboability : ComboManager.comboAbilityList) {
			if (!comboability.getName().equalsIgnoreCase(combo)) {
				continue;
			}
			
			if (!ComboManager.descriptions.containsKey(comboability.getName())) {
				return ChatColor.STRIKETHROUGH; //This is so we know it shouldn't be used. Should not come up anyway.
			}
			
			if (comboability.getComboType() instanceof ComboAbilityModule){
				ComboAbilityModule module = (ComboAbilityModule) comboability.getComboType();
				if (module.getSubElement() != null) {
					if (module.getSubElement() == SubElement.Bloodbending || module.getSubElement() == SubElement.Icebending || module.getSubElement() == SubElement.Plantbending || module.getSubElement() == SubElement.Healing) 
						return WaterMethods.getWaterSubColor();
					else if (module.getSubElement() == SubElement.Lightning || module.getSubElement() == SubElement.Combustion)
						return FireMethods.getFireSubColor();
					else if (module.getSubElement() == SubElement.Sandbending || module.getSubElement() == SubElement.Metalbending || module.getSubElement() == SubElement.Lavabending) 
						return EarthMethods.getEarthSubColor();
					else if (module.getSubElement() == SubElement.Flight || module.getSubElement() == SubElement.SpiritualProjection) 
						return AirMethods.getAirSubColor();
				}
				if (module.getElement().equalsIgnoreCase(Element.Water.toString())) return WaterMethods.getWaterColor();
				else if (module.getElement().equalsIgnoreCase(Element.Earth.toString())) return EarthMethods.getEarthColor();
				else if (module.getElement().equalsIgnoreCase(Element.Fire.toString())) return FireMethods.getFireColor();
				else if (module.getElement().equalsIgnoreCase(Element.Air.toString())) return AirMethods.getAirColor();
				else if (module.getElement().equalsIgnoreCase(Element.Chi.toString())) return ChiMethods.getChiColor();
				else return getAvatarColor();
			}
			else if (combo.equalsIgnoreCase("IceBullet") || combo.equalsIgnoreCase("IceWave")) {
				return WaterMethods.getWaterSubColor();
			}
			else if (comboability.getComboType().equals(WaterCombo.class)){
				return WaterMethods.getWaterColor();
			}
			else if (comboability.getComboType().equals(FireCombo.class)){
				return FireMethods.getFireColor();
			}
			else if (comboability.getComboType().equals(AirCombo.class)){
				return AirMethods.getAirColor();
			}
			else {
				Element element = null;
				for (AbilityInformation abilityinfo : comboability.getAbilities()) {
					Element currElement = getAbilityElement(abilityinfo.getAbilityName());
					if (currElement == null) return getAvatarColor();
					else if (element == null) element = currElement;
					if (getAbilitySubElement(abilityinfo.getAbilityName()) != null) {
						SubElement sub = getAbilitySubElement(abilityinfo.getAbilityName());
						if (sub == SubElement.Bloodbending || sub == SubElement.Icebending || sub == SubElement.Plantbending || sub == SubElement.Healing) 
							return WaterMethods.getWaterSubColor();
						else if (sub == SubElement.Lightning || sub == SubElement.Combustion)
							return FireMethods.getFireSubColor();
						else if (sub == SubElement.Sandbending || sub == SubElement.Metalbending || sub == SubElement.Lavabending) 
							return EarthMethods.getEarthSubColor();
						else if (sub == SubElement.Flight || sub == SubElement.SpiritualProjection) 
							return AirMethods.getAirSubColor();
					}
				}
				if (element == Element.Air) return AirMethods.getAirColor();
				if (element == Element.Earth) return EarthMethods.getEarthColor();
				if (element == Element.Fire) return FireMethods.getFireColor();
				if (element == Element.Water) return WaterMethods.getWaterColor();
				if (element == Element.Chi) return ChiMethods.getChiColor();
				return getAvatarColor();
			}
		}
		return getAvatarColor();
	}
	/**Returns the correct element for the combo*/
	public static Element getComboElement(String combo) {
		Iterator<ComboAbility> it = ComboManager.comboAbilityList.iterator();
		Element element = null;
		while (it.hasNext()) {
			ComboAbility comboability = it.next();
			if (!comboability.getName().equalsIgnoreCase(combo)) {
				continue;
			}
			for (AbilityInformation abilityinfo : comboability.getAbilities())
			{
				Element abilityelement = getAbilityElement(abilityinfo.getAbilityName());
				if (abilityelement == null) return null;
				else if (element == null) element = abilityelement;
				else if (element != abilityelement) return null;
			}
			return element;
		}
		return null;
	}

	public static String getCurrentDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
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
	 * @param block The single block
	 * @param type The Material type to change the block into
	 * @param data The block data to change the block into
	 * @param breakitem Unused
	 * @return The item drops fromt the specified block
	 */
	public static Collection<ItemStack> getDrops(Block block, Material type, byte data, ItemStack breakitem) {
		BlockState tempstate = block.getState();
		block.setType(type);
		block.setData(data);
		Collection<ItemStack> item = block.getDrops();
		tempstate.update(true);
		return item;
	}

	/**
	 * Gets a {@code List<Entity>} of entities around a specified radius from
	 * the specified area
	 * 
	 * @param location The base location
	 * @param radius The radius of blocks to look for entities from the location
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

	public static long getGlobalCooldown() {
		return plugin.getConfig().getLong("Properties.GlobalCooldown");
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

	public static Plugin getItems() {
		if (hasItems()) {
			return Bukkit.getServer().getPluginManager().getPlugin("ProjectKorraItems");
		}
		return null;
	}

	/**
	 * Returns the last ability used by a player. Also checks if a combo was
	 * used.
	 * 
	 * @param player The player to check
	 * @return name of last ability used
	 */
	public static String getLastUsedAbility(Player player, boolean checkCombos) {
		List<AbilityInformation> lastUsedAbility = ComboManager.getRecentlyUsedAbilities(player, 1);
		if (!lastUsedAbility.isEmpty()) {
			if (ComboManager.checkForValidCombo(player) != null && checkCombos) {
				return ComboManager.checkForValidCombo(player).getName();
			} else {
				return lastUsedAbility.get(0).getAbilityName();
			}
		}
		return null;
	}

	/**
	 * Gets a location with a specified distance away from the left side of a
	 * location.
	 * 
	 * @param location The origin location
	 * @param distance The distance to the left
	 * @return the location of the distance to the left
	 */
	public static Location getLeftSide(Location location, double distance) {
		float angle = location.getYaw() / 60;
		return location.clone().add(new Vector(Math.cos(angle), 0, Math.sin(angle)).normalize().multiply(distance));
	}

	public static int getMaxPresets(Player player) {
		if (player.isOp())
			return 500;
		int cap = 0;
		for (int i = 0; i <= 500; i++) {
			if (player.hasPermission("bending.command.presets.create." + i))
				cap = i;
		}
		return cap;
	}

	public static Vector getOrthogonalVector(Vector axis, double degrees, double length) {
		Vector ortho = new Vector(axis.getY(), -axis.getX(), 0);
		ortho = ortho.normalize();
		ortho = ortho.multiply(length);

		return rotateVectorAroundVector(axis, ortho, degrees);
	}

	public static Collection<Player> getPlayersAroundPoint(Location location, double distance) {
		Collection<Player> players = new HashSet<Player>();
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.getLocation().getWorld().equals(location.getWorld())) {
				if (player.getLocation().distance(location) <= distance) {
					players.add(player);
				}
			}
		}
		return players;
	}

	public static Location getPointOnLine(Location origin, Location target, double distance) {
		return origin.clone().add(getDirection(origin, target).normalize().multiply(distance));
	}

	/**
	 * Returns a location with a specified distance away from the right side of
	 * a location.
	 * 
	 * @param location The origin location
	 * @param distance The distance to the right
	 * @return the location of the distance to the right
	 */
	public static Location getRightSide(Location location, double distance) {
		float angle = location.getYaw() / 60;
		return location.clone().subtract(new Vector(Math.cos(angle), 0, Math.sin(angle)).normalize().multiply(distance));
	}

	public static Plugin getRPG() {
		if (hasRPG()) {
			return Bukkit.getServer().getPluginManager().getPlugin("ProjectKorraRPG");
		}
		return null;
	}

	@SuppressWarnings("incomplete-switch")
	public static ChatColor getSubBendingColor(Element element) {
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

	@SuppressWarnings("unused")
	public static Entity getTargetedEntity(Player player, double range, List<Entity> avoid) {
		double longestr = range + 1;
		Entity target = null;
		Location origin = player.getEyeLocation();
		Vector direction = player.getEyeLocation().getDirection().normalize();
		for (Entity entity : origin.getWorld().getEntities()) {
			if (avoid.contains(entity))
				continue;
			if (entity.getLocation().distance(origin) < longestr && getDistanceFromLine(direction, origin, entity.getLocation()) < 2 && (entity instanceof LivingEntity) && entity.getEntityId() != player.getEntityId() && entity.getLocation().distance(origin.clone().add(direction)) < entity.getLocation().distance(origin.clone().add(direction.clone().multiply(-1)))) {
				target = entity;
				longestr = entity.getLocation().distance(origin);
			}
		}
		if (target != null) {
			List<Block> blocklist = new ArrayList<Block>();
			blocklist = GeneralMethods.getBlocksAlongLine(player.getLocation(), target.getLocation(), player.getWorld());
			for (Block isAir : blocklist) {
				if (GeneralMethods.isObstructed(origin, target.getLocation())) {
					target = null;
					break;
				}
			}
		}
		return target;
	}

	public static Location getTargetedLocation(Player player, double originselectrange, Integer... nonOpaque2) {
		Location origin = player.getEyeLocation();
		Vector direction = origin.getDirection();

		HashSet<Byte> trans = new HashSet<Byte>();
		trans.add((byte) 0);

		if (nonOpaque2 == null) {
			trans = null;
		} else {
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

	public static Block getTopBlock(Location loc, int range) {
		return getTopBlock(loc, range, range);
	}

	/**
	 * Returns the top block based around loc. PositiveY is the maximum amount
	 * of distance it will check upward. Similarly, negativeY is for downward.
	 */
	public static Block getTopBlock(Location loc, int positiveY, int negativeY) {
		Block blockHolder = loc.getBlock();
		int y = 0;
		//Only one of these while statements will go
		while (blockHolder.getType() != Material.AIR && Math.abs(y) < Math.abs(positiveY)) {
			y++;
			Block tempBlock = loc.clone().add(0, y, 0).getBlock();
			if (tempBlock.getType() == Material.AIR)
				return blockHolder;
			blockHolder = tempBlock;
		}

		while (blockHolder.getType() == Material.AIR && Math.abs(y) < Math.abs(negativeY)) {
			y--;
			blockHolder = loc.clone().add(0, y, 0).getBlock();
			if (blockHolder.getType() != Material.AIR)
				return blockHolder;
		}
		return null;
	}

	public static boolean hasItems() {
		return Bukkit.getServer().getPluginManager().getPlugin("ProjectKorraItems") != null;
	}

	public static boolean hasPermission(Player player, String ability) {
		return player.hasPermission("bending.ability." + ability) && canBind(player.getName(), ability);
	}

	public static boolean hasRPG() {
		return Bukkit.getServer().getPluginManager().getPlugin("ProjectKorraRPG") != null;
	}

	public static boolean isAbilityInstalled(String name, String author) {
		String ability = getAbility(name);
		return ability != null && AbilityModuleManager.authors.get(name).equalsIgnoreCase(author);
	}

	public static boolean isAdjacentToThreeOrMoreSources(Block block) {
		if (TempBlock.isTempBlock(block))
			return false;
		int sources = 0;
		byte full = 0x0;
		BlockFace[] faces = { BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH };
		for (BlockFace face : faces) {
			Block blocki = block.getRelative(face);
			if ((blocki.getType() == Material.LAVA || blocki.getType() == Material.STATIONARY_LAVA) && blocki.getData() == full && EarthPassive.canPhysicsChange(blocki))
				sources++;
			if ((blocki.getType() == Material.WATER || blocki.getType() == Material.STATIONARY_WATER) && blocki.getData() == full && WaterManipulation.canPhysicsChange(blocki))
				sources++;
			if (FreezeMelt.frozenblocks.containsKey(blocki)) {
				//if (FreezeMelt.frozenblocks.get(blocki) == full)
				//sources++;
			} else if (blocki.getType() == Material.ICE) {
				//sources++;
			}
		}
		return sources >= 2;
	}

	public static boolean isBender(String player, Element element) {
		BendingPlayer bPlayer = getBendingPlayer(player);
		return bPlayer != null && bPlayer.hasElement(element);
	}

	public static boolean isDisabledStockAbility(String string) {
		for (String st : AbilityModuleManager.disabledStockAbilities) {
			if (string.equalsIgnoreCase(st))
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
			if (type != Material.AIR && !(Arrays.asList(EarthMethods.getTransparentEarthbending()).contains(type.getId()) || WaterMethods.isWater(loc.getBlock())))
				return true;
		}
		return false;
	}

	/**
	 * isRegionProtectedFromBuild is one of the most server intensive methods in
	 * the plugin. It uses a blockCache that keeps track of recent blocks that
	 * may have already been checked. Abilities like TremorSense call this
	 * ability 5 times per tick even though it only needs to check a single
	 * block, instead of doing all 5 of those checks this method will now look
	 * in the map first.
	 */
	public static boolean isRegionProtectedFromBuild(Player player, String ability, Location loc) {
		if (!blockProtectionCache.containsKey(player.getName()))
			blockProtectionCache.put(player.getName(), new ConcurrentHashMap<Block, BlockCacheElement>());

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

		if (ability == null && allowharmless)
			return false;
		if (isHarmlessAbility(ability) && allowharmless)
			return false;

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
				if (!player.isOnline())
					return true;

				if (ignite.contains(ability)) {
					if (!wg.hasPermission(player, "worldguard.override.lighter")) {
						if (wg.getGlobalStateManager().get(world).blockLighter)
							return true;
					}
				}
				if (explode.contains(ability)) {
					if (wg.getGlobalStateManager().get(location.getWorld()).blockTNTExplosions)
						return true;
					if (!wg.getRegionContainer().createQuery().testBuild(location, player, DefaultFlag.TNT))
						return true;
				}

				if (!wg.canBuild(player, location.getBlock())) {
					return true;
				}
			}

			if (psp != null && respectPreciousStones) {
				PreciousStones ps = (PreciousStones) psp;

				if (ignite.contains(ability)) {
					if (ps.getForceFieldManager().hasSourceField(location, FieldFlag.PREVENT_FIRE))
						return true;
				}
				if (explode.contains(ability)) {
					if (ps.getForceFieldManager().hasSourceField(location, FieldFlag.PREVENT_EXPLOSIONS))
						return true;
				}

				//				if (ps.getForceFieldManager().hasSourceField(location,
				//						FieldFlag.PREVENT_PLACE))
				//					return true;

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

					if (ignite.contains(ability)) {

					}

					if (explode.contains(ability)) {

					}

					if (!bBuild) {
						PlayerCache cache = twn.getCache(player);
						TownBlockStatus status = cache.getStatus();

						if (((status == TownBlockStatus.ENEMY) && TownyWarConfig.isAllowingAttacks())) {
							try {
								TownyWar.callAttackCellEvent(twn, player, location.getBlock(), worldCoord);
							}
							catch (Exception e) {
								TownyMessaging.sendErrorMsg(player, e.getMessage());
							}
							return true;
						} else if (status == TownBlockStatus.WARZONE) {

						} else {
							return true;
						}

						if ((cache.hasBlockErrMsg()))
							TownyMessaging.sendErrorMsg(player, cache.getBlockErrMsg());
					}
				}
				catch (Exception e1) {
					TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_not_configured"));
				}
			}

			if (gpp != null && respectGriefPrevention) {
				Material type = player.getWorld().getBlockAt(location).getType();
				if (type == null)
					type = Material.AIR;
				String reason = GriefPrevention.instance.allowBuild(player, location); // WORKING with WorldGuard 6.0 BETA 4

				Claim claim = GriefPrevention.instance.dataStore.getClaimAt(loc, true, null);
				if (ignite.contains(ability)) {

				}

				if (explode.contains(ability)) {

				}

				if (reason != null && claim.siegeData != null)
					return true;
			}
		}
		return false;
	}

	public static boolean isSolid(Block block) {
		return !Arrays.asList(nonOpaque).contains(block.getTypeId());
	}

	public static boolean isSubAbility(String ability) {
		return AbilityModuleManager.subabilities.contains(ability);
	}

	/** Checks if an entity is Undead **/
	public static boolean isUndead(Entity entity) {
		return entity != null
				&& (entity.getType() == EntityType.ZOMBIE
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

	public static boolean isWeapon(Material mat) {
		return mat != null
				&& (mat == Material.WOOD_AXE
				|| mat == Material.WOOD_PICKAXE
				|| mat == Material.WOOD_SPADE
				|| mat == Material.WOOD_SWORD
				|| mat == Material.STONE_AXE
				|| mat == Material.STONE_PICKAXE
				|| mat == Material.STONE_SPADE
				|| mat == Material.STONE_SWORD
				|| mat == Material.IRON_AXE
				|| mat == Material.IRON_PICKAXE
				|| mat == Material.IRON_SWORD
				|| mat == Material.IRON_SPADE
				|| mat == Material.DIAMOND_AXE
				|| mat == Material.DIAMOND_PICKAXE
				|| mat == Material.DIAMOND_SWORD
				|| mat == Material.DIAMOND_SPADE);
	}

	public static void playAvatarSound(Location loc) {
		loc.getWorld().playSound(loc, Sound.ANVIL_LAND, 1, 10);
	}

	public static void reloadPlugin() {
		ProjectKorra.log.info("Reloading ProjectKorra and configuration");
		BendingReloadEvent event = new BendingReloadEvent();
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (DBConnection.isOpen) {
			DBConnection.sql.close();
		}
		GeneralMethods.stopBending();
		ConfigManager.defaultConfig.reload();
		ConfigManager.deathMsgConfig.reload();
		BendingManager.getInstance().reloadVariables();
		new AbilityModuleManager(plugin);
		new ComboManager();
		new MultiAbilityModuleManager();
		new CraftingRecipes(plugin);
		DBConnection.host = plugin.getConfig().getString("Storage.MySQL.host");
		DBConnection.port = plugin.getConfig().getInt("Storage.MySQL.port");
		DBConnection.pass = plugin.getConfig().getString("Storage.MySQL.pass");
		DBConnection.db = plugin.getConfig().getString("Storage.MySQL.db");
		DBConnection.user = plugin.getConfig().getString("Storage.MySQL.user");
		DBConnection.init();
		if (!DBConnection.isOpen()) {
			ProjectKorra.log.severe("Unable to enable ProjectKorra due to the database not being open");
			stopPlugin();
		}
		for (Player player : Bukkit.getOnlinePlayers()) {
			GeneralMethods.createBendingPlayer(player.getUniqueId(), player.getName());
			Preset.loadPresets(player);
		}
		plugin.updater.checkUpdate();
		ProjectKorra.log.info("Reload complete");
	}

	public static void removeBlock(Block block) {
		if (isAdjacentToThreeOrMoreSources(block)) {
			block.setType(Material.WATER);
			block.setData((byte) 0x0);
		} else {
			block.setType(Material.AIR);
		}
	}

	public static void removeUnusableAbilities(String player) {
		BendingPlayer bPlayer = getBendingPlayer(player);
		HashMap<Integer, String> slots = bPlayer.getAbilities();
		HashMap<Integer, String> finalabilities = new HashMap<Integer, String>();
		for (int i : slots.keySet()) {
			if (canBend(player, slots.get(i))) {
				finalabilities.put(i, slots.get(i));
			}
		}
		bPlayer.setAbilities(finalabilities);
	}

	public static Vector rotateVectorAroundVector(Vector axis, Vector rotator, double degrees) {
		double angle = Math.toRadians(degrees);
		Vector rotation = axis.clone();
		Vector rotate = rotator.clone();
		rotation = rotation.normalize();

		Vector thirdaxis = rotation.crossProduct(rotate).normalize().multiply(rotate.length());

		return rotate.multiply(Math.cos(angle)).add(thirdaxis.multiply(Math.sin(angle)));
	}

	/**
	 * Rotates a vector around the Y plane.
	 */
	public static Vector rotateXZ(Vector vec, double theta) {
		Vector vec2 = vec.clone();
		double x = vec2.getX();
		double z = vec2.getZ();
		vec2.setX(x * Math.cos(Math.toRadians(theta)) - z * Math.sin(Math.toRadians(theta)));
		vec2.setZ(x * Math.sin(Math.toRadians(theta)) + z * Math.cos(Math.toRadians(theta)));
		return vec2;
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
		ArrayList<String> stockAbils = new ArrayList<String>();
		ArrayList<String> unofficialAbils = new ArrayList<String>();
		for (String ability : AbilityModuleManager.abilities) {
			if (StockAbility.isStockAbility(ability))
				stockAbils.add(ability);
			else
				unofficialAbils.add(ability);
		}
		if (!stockAbils.isEmpty()) {
			Collections.sort(stockAbils);
			for(String ability : stockAbils) {
				writeToDebug(ability + " - STOCK");
			}
		}
		if (!unofficialAbils.isEmpty()) {
			Collections.sort(unofficialAbils);
			for(String ability : unofficialAbils) {
				writeToDebug(ability + " - UNOFFICAL");
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

	public static void saveAbility(BendingPlayer bPlayer, int slot, String ability) {
		if (bPlayer == null)
			return;
		String uuid = bPlayer.getUUIDString();

		//Temp code to block modifications of binds, Should be replaced when bind event is added.
		if (MultiAbilityManager.playerAbilities.containsKey(Bukkit.getPlayer(bPlayer.getUUID())))
			return;
		HashMap<Integer, String> abilities = bPlayer.getAbilities();

		DBConnection.sql.modifyQuery("UPDATE pk_players SET slot" + slot + " = '" + (abilities.get(slot) == null ? null : abilities.get(slot)) + "' WHERE uuid = '" + uuid + "'");
	}

	public static void saveElements(BendingPlayer bPlayer) {
		if (bPlayer == null)
			return;
		String uuid = bPlayer.getUUIDString();

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

	public static void savePermaRemoved(BendingPlayer bPlayer) {
		if (bPlayer == null)
			return;
		String uuid = bPlayer.getUUIDString();
		boolean permaRemoved = bPlayer.isPermaRemoved();
		DBConnection.sql.modifyQuery("UPDATE pk_players SET permaremoved = '" + (permaRemoved ? "true" : "false") + "' WHERE uuid = '" + uuid + "'");
	}

	public static void setVelocity(Entity entity, Vector velocity) {
		if (entity instanceof TNTPrimed) {
			if (plugin.getConfig().getBoolean("Properties.BendingAffectFallingSand.TNT"))
				entity.setVelocity(velocity.multiply(plugin.getConfig().getDouble("Properties.BendingAffectFallingSand.TNTStrengthMultiplier")));
			return;
		}
		if (entity instanceof FallingSand) {
			if (plugin.getConfig().getBoolean("Properties.BendingAffectFallingSand.Normal"))
				entity.setVelocity(velocity.multiply(plugin.getConfig().getDouble("Properties.BendingAffectFallingSand.NormalStrengthMultiplier")));
			return;
		}
		entity.setVelocity(velocity);
	}

	public static FallingBlock spawnFallingBlock(Location loc, int type) {
		return spawnFallingBlock(loc, type, (byte) 0);
	}

	public static FallingBlock spawnFallingBlock(Location loc, int type, byte data) {
		return loc.getWorld().spawnFallingBlock(loc, type, data);
	}

	public static FallingBlock spawnFallingBlock(Location loc, Material type) {
		return spawnFallingBlock(loc, type, (byte) 0);
	}

	public static FallingBlock spawnFallingBlock(Location loc, Material type, byte data) {
		return loc.getWorld().spawnFallingBlock(loc, type, data);
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

	public static void stopBending() {
		List<AbilityModule> abilities = AbilityModuleManager.ability;
		for (AbilityModule ab : abilities) {
			ab.stop();
		}

		HashSet<ComboManager.ComboAbility> combos = ComboManager.comboAbilityList;
		for (ComboManager.ComboAbility c : combos)
			if (c.getComboType() instanceof ComboAbilityModule)
				((ComboAbilityModule) c.getComboType()).stop();

		AirMethods.stopBending();
		EarthMethods.stopBending();
		WaterMethods.stopBending();
		FireMethods.stopBending();
		ChiMethods.stopBending();

		Flight.removeAll();
		TempBlock.removeAll();
		MultiAbilityManager.removeAll();
	}

	public static void stopPlugin() {
		plugin.getServer().getPluginManager().disablePlugin(plugin);
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

		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ComboAbilityModule getCombo(String name) {
		for (ComboAbilityModule c : ComboModuleManager.combo)
			if (name.equalsIgnoreCase(c.getName()))
				return c;
		return null;
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

		public String getAbility() {
			return ability;
		}

		public Block getBlock() {
			return block;
		}

		public Player getPlayer() {
			return player;
		}

		public long getTime() {
			return time;
		}

		public boolean isAllowed() {
			return allowed;
		}

		public void setAbility(String ability) {
			this.ability = ability;
		}

		public void setAllowed(boolean allowed) {
			this.allowed = allowed;
		}

		public void setBlock(Block block) {
			this.block = block;
		}

		public void setPlayer(Player player) {
			this.player = player;
		}

		public void setTime(long time) {
			this.time = time;
		}

	}
}

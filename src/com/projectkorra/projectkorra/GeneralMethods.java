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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.kingdoms.constants.StructureType;
import org.kingdoms.constants.kingdom.Kingdom;
import org.kingdoms.constants.land.Land;
import org.kingdoms.constants.land.SimpleChunkLocation;
import org.kingdoms.constants.land.SimpleLocation;
import org.kingdoms.constants.player.KingdomPlayer;
import org.kingdoms.manager.game.GameManagement;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import com.google.common.reflect.ClassPath;
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
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.ability.util.CollisionInitializer;
import com.projectkorra.projectkorra.ability.util.CollisionManager;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.ability.util.PassiveManager;
import com.projectkorra.projectkorra.airbending.AirBlast;
import com.projectkorra.projectkorra.airbending.AirShield;
import com.projectkorra.projectkorra.airbending.AirSpout;
import com.projectkorra.projectkorra.airbending.AirSuction;
import com.projectkorra.projectkorra.airbending.AirSwipe;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.EarthBlast;
import com.projectkorra.projectkorra.earthbending.passive.EarthPassive;
import com.projectkorra.projectkorra.event.BendingPlayerCreationEvent;
import com.projectkorra.projectkorra.event.BendingReloadEvent;
import com.projectkorra.projectkorra.event.BindChangeEvent;
import com.projectkorra.projectkorra.firebending.FireBlast;
import com.projectkorra.projectkorra.firebending.FireShield;
import com.projectkorra.projectkorra.firebending.combustion.Combustion;
import com.projectkorra.projectkorra.object.Preset;
import com.projectkorra.projectkorra.storage.DBConnection;
import com.projectkorra.projectkorra.util.ActionBar;
import com.projectkorra.projectkorra.util.BlockCacheElement;
import com.projectkorra.projectkorra.util.MovementHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.ReflectionHandler;
import com.projectkorra.projectkorra.util.ReflectionHandler.PackageType;
import com.projectkorra.projectkorra.util.TempArmor;
import com.projectkorra.projectkorra.util.TempArmorStand;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.util.TimeUtil;
import com.projectkorra.projectkorra.waterbending.WaterManipulation;
import com.projectkorra.projectkorra.waterbending.WaterSpout;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;

public class GeneralMethods {

	public static final Material[] NON_OPAQUE = { Material.AIR, Material.SAPLING, Material.WATER, Material.STATIONARY_WATER, Material.LAVA, Material.STATIONARY_LAVA, Material.POWERED_RAIL, Material.DETECTOR_RAIL, Material.WEB, Material.LONG_GRASS, Material.DEAD_BUSH, Material.YELLOW_FLOWER, Material.RED_ROSE, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.TORCH, Material.FIRE, Material.REDSTONE_WIRE, Material.CROPS, Material.LADDER, Material.RAILS, Material.SIGN_POST, Material.LEVER, Material.STONE_PLATE, Material.WOOD_PLATE, Material.REDSTONE_TORCH_OFF, Material.REDSTONE_TORCH_ON, Material.STONE_BUTTON, Material.SNOW, Material.SUGAR_CANE_BLOCK, Material.PORTAL, Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON, Material.PUMPKIN_STEM, Material.MELON_STEM, Material.VINE, Material.WATER_LILY, Material.NETHER_STALK, Material.ENDER_PORTAL, Material.COCOA, Material.TRIPWIRE_HOOK, Material.TRIPWIRE, Material.FLOWER_POT, Material.CARROT, Material.POTATO, Material.WOOD_BUTTON, Material.GOLD_PLATE, Material.IRON_PLATE, Material.REDSTONE_COMPARATOR_OFF, Material.REDSTONE_COMPARATOR_ON, Material.DAYLIGHT_DETECTOR, Material.CARPET, Material.DOUBLE_PLANT, Material.STANDING_BANNER, Material.WALL_BANNER, Material.DAYLIGHT_DETECTOR_INVERTED, Material.END_ROD, Material.CHORUS_PLANT, Material.CHORUS_FLOWER, Material.BEETROOT_BLOCK, Material.END_GATEWAY };
	public static final Material[] INTERACTABLE_MATERIALS = { Material.ACACIA_DOOR, Material.ACACIA_FENCE_GATE, Material.ANVIL, Material.BEACON, Material.BED_BLOCK, Material.BIRCH_DOOR, Material.BIRCH_FENCE_GATE, Material.BOAT, Material.BREWING_STAND, Material.BURNING_FURNACE, Material.CAKE_BLOCK, Material.CHEST, Material.COMMAND, Material.DARK_OAK_DOOR, Material.DARK_OAK_FENCE_GATE, Material.DISPENSER, Material.DRAGON_EGG, Material.DROPPER, Material.ENCHANTMENT_TABLE, Material.ENDER_CHEST, Material.ENDER_PORTAL_FRAME, Material.FENCE_GATE, Material.FURNACE, Material.HOPPER, Material.HOPPER_MINECART, Material.COMMAND_MINECART, Material.JUKEBOX, Material.JUNGLE_DOOR, Material.JUNGLE_FENCE_GATE, Material.LEVER, Material.MINECART, Material.NOTE_BLOCK, Material.SPRUCE_DOOR, Material.SPRUCE_FENCE_GATE, Material.STONE_BUTTON, Material.TRAPPED_CHEST, Material.TRAP_DOOR, Material.WOOD_BUTTON, Material.WOOD_DOOR, Material.WORKBENCH };

	// Represents PlayerName, previously checked blocks, and whether they were true or false
	private static final Map<String, Map<Block, BlockCacheElement>> BLOCK_CACHE = new ConcurrentHashMap<>();
	private static final ArrayList<Ability> INVINCIBLE = new ArrayList<>();
	private static ProjectKorra plugin;

	private static Method getAbsorption;
	private static Method setAbsorption;

	public GeneralMethods(final ProjectKorra plugin) {
		GeneralMethods.plugin = plugin;

		try {
			getAbsorption = ReflectionHandler.getMethod("EntityHuman", PackageType.MINECRAFT_SERVER, "getAbsorptionHearts");
			setAbsorption = ReflectionHandler.getMethod("EntityHuman", PackageType.MINECRAFT_SERVER, "setAbsorptionHearts", Float.class);
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks to see if an AbilityExists. Uses method
	 * {@link #getAbility(String)} to check if it exists.
	 *
	 * @param string Ability Name
	 * @return true if ability exists
	 */
	public static boolean abilityExists(final String string) {
		return CoreAbility.getAbility(string) != null;
	}

	/**
	 * Binds a Ability to the hotbar slot that the player is on.
	 *
	 * @param player The player to bind to
	 * @param ability The ability name to Bind
	 * @see #bindAbility(Player, String, int)
	 */
	public static void bindAbility(final Player player, final String ability) {
		final int slot = player.getInventory().getHeldItemSlot() + 1;
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
	public static void bindAbility(final Player player, final String ability, final int slot) {
		if (MultiAbilityManager.playerAbilities.containsKey(player)) {
			GeneralMethods.sendBrandingMessage(player, ChatColor.RED + "You can't edit your binds right now!");
			return;
		}

		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player.getName());
		final CoreAbility coreAbil = CoreAbility.getAbility(ability);

		if (bPlayer == null) {
			return;
		}
		bPlayer.getAbilities().put(slot, ability);

		if (coreAbil != null) {
			GeneralMethods.sendBrandingMessage(player, coreAbil.getElement().getColor() + ConfigManager.languageConfig.get().getString("Commands.Bind.SuccessfullyBound").replace("{ability}", ability).replace("{slot}", String.valueOf(slot)));
		}
		saveAbility(bPlayer, slot, ability);
	}

	/**
	 * This method was used for the old collision detection system. Please see
	 * {@link Collision} for the new system.
	 * <p>
	 * Cycles through a list of ability names to check if any instances of the
	 * abilities exist at a specific location. If an instance of the ability is
	 * found then it will be removed, with the exception FireShield, and
	 * AirShield.
	 */
	@Deprecated
	public static boolean blockAbilities(final Player player, final List<String> abilitiesToBlock, final Location loc, final double radius) {
		boolean hasBlocked = false;
		for (final String ability : abilitiesToBlock) {
			if (ability.equalsIgnoreCase("FireBlast")) {
				hasBlocked = FireBlast.annihilateBlasts(loc, radius, player) || hasBlocked;
			} else if (ability.equalsIgnoreCase("EarthBlast")) {
				hasBlocked = EarthBlast.annihilateBlasts(loc, radius, player) || hasBlocked;
			} else if (ability.equalsIgnoreCase("WaterManipulation")) {
				hasBlocked = WaterManipulation.annihilateBlasts(loc, radius, player) || hasBlocked;
			} else if (ability.equalsIgnoreCase("AirSwipe")) {
				hasBlocked = AirSwipe.removeSwipesAroundPoint(loc, radius) || hasBlocked;
			} else if (ability.equalsIgnoreCase("AirBlast")) {
				hasBlocked = AirBlast.removeAirBlastsAroundPoint(loc, radius) || hasBlocked;
			} else if (ability.equalsIgnoreCase("AirSuction")) {
				hasBlocked = AirSuction.removeAirSuctionsAroundPoint(loc, radius) || hasBlocked;
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
				// hasBlocked = AirCombo.removeAroundPoint(player, "Twister", loc, radius) || hasBlocked;
			} else if (ability.equalsIgnoreCase("AirStream")) {
				// hasBlocked = AirCombo.removeAroundPoint(player, "AirStream", loc, radius) || hasBlocked;
			} else if (ability.equalsIgnoreCase("AirSweep")) {
				// hasBlocked = AirCombo.removeAroundPoint(player, "AirSweep", loc, radius) || hasBlocked;
			} else if (ability.equalsIgnoreCase("FireKick")) {
				// hasBlocked = FireCombo.removeAroundPoint(player, "FireKick", loc, radius) || hasBlocked;
			} else if (ability.equalsIgnoreCase("FireSpin")) {
				// hasBlocked = FireCombo.removeAroundPoint(player, "FireSpin", loc, radius) || hasBlocked;
			} else if (ability.equalsIgnoreCase("FireWheel")) {
				// hasBlocked = FireCombo.removeAroundPoint(player, "FireWheel", loc, radius) || hasBlocked;
			}
		}
		return hasBlocked;
	}

	/**
	 * Breaks a block and sets it to {@link Material#AIR AIR}.
	 *
	 * @param block The block to break
	 */
	public static void breakBlock(final Block block) {
		block.breakNaturally(new ItemStack(Material.AIR));
	}

	public static boolean canView(final Player player, final String ability) {
		return player.hasPermission("bending.ability." + ability);
	}

	/**
	 * Checks if there is a diagonal wall in front of the location in the given
	 * direction, in any 2 dimensions and all 3
	 *
	 * @param location spot to check
	 * @param direction which way to check
	 * @return true if diagonal wall is found
	 */
	public static boolean checkDiagonalWall(final Location location, final Vector direction) {
		final boolean[] xyzsolid = { false, false, false };
		for (int i = 0; i < 3; i++) {
			double value;
			if (i == 0) {
				value = direction.getX();
			} else if (i == 1) {
				value = direction.getY();
			} else {
				value = direction.getZ();
			}
			final BlockFace face = GeneralMethods.getBlockFaceFromValue(i, value);
			if (face == null) {
				continue;
			}
			xyzsolid[i] = location.getBlock().getRelative(face).getType().isSolid();
		}
		final boolean a = xyzsolid[0] && xyzsolid[2];
		final boolean b = xyzsolid[0] && xyzsolid[1];
		final boolean c = xyzsolid[1] && xyzsolid[2];
		return (a || b || c || (a && b));
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
		final ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM pk_players WHERE uuid = '" + uuid.toString() + "'");
		try {
			if (!rs2.next()) { // Data doesn't exist, we want a completely new player.
				DBConnection.sql.modifyQuery("INSERT INTO pk_players (uuid, player, slot1, slot2, slot3, slot4, slot5, slot6, slot7, slot8, slot9) VALUES ('" + uuid.toString() + "', '" + player + "', 'null', 'null', 'null', 'null', 'null', 'null', 'null', 'null', 'null')");
				new BendingPlayer(uuid, player, new ArrayList<Element>(), new ArrayList<SubElement>(), new HashMap<Integer, String>(), false);
				ProjectKorra.log.info("Created new BendingPlayer for " + player);
			} else {
				// The player has at least played before.
				final String player2 = rs2.getString("player");
				if (!player.equalsIgnoreCase(player2)) {
					DBConnection.sql.modifyQuery("UPDATE pk_players SET player = '" + player + "' WHERE uuid = '" + uuid.toString() + "'");
					// They have changed names.
					ProjectKorra.log.info("Updating Player Name for " + player);
				}
				final String subelement = rs2.getString("subelement");
				final String element = rs2.getString("element");
				final String permaremoved = rs2.getString("permaremoved");
				boolean p = false;
				final ArrayList<Element> elements = new ArrayList<Element>();
				if (element != null && !element.equals("NULL")) {
					final boolean hasAddon = element.contains(";");
					final String[] split = element.split(";");
					if (split[0] != null) { // Player has an element.
						if (split[0].contains("a")) {
							elements.add(Element.AIR);
						}
						if (split[0].contains("w")) {
							elements.add(Element.WATER);
						}
						if (split[0].contains("e")) {
							elements.add(Element.EARTH);
						}
						if (split[0].contains("f")) {
							elements.add(Element.FIRE);
						}
						if (split[0].contains("c")) {
							elements.add(Element.CHI);
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
							final CopyOnWriteArrayList<String> addonClone = new CopyOnWriteArrayList<String>(Arrays.asList(split[split.length - 1].split(",")));
							final long startTime = System.currentTimeMillis();
							final long timeoutLength = 30000; // How long until it should time out attempting to load addons in.
							new BukkitRunnable() {
								@Override
								public void run() {
									if (addonClone.isEmpty()) {
										ProjectKorra.log.info("Successfully loaded in all addon elements!");
										this.cancel();
									} else if (System.currentTimeMillis() - startTime > timeoutLength) {
										ProjectKorra.log.severe("ProjectKorra has timed out after attempting to load in the following addon elements: " + addonClone.toString());
										ProjectKorra.log.severe("These elements have taken too long to load in, resulting in users having lost these element.");
										this.cancel();
									} else {
										ProjectKorra.log.info("Attempting to load in the following addon elements... " + addonClone.toString());
										for (final String addon : addonClone) {
											if (Element.getElement(addon) != null) {
												elements.add(Element.getElement(addon));
												addonClone.remove(addon);
											}
										}
									}
								}
							}.runTaskTimer(ProjectKorra.plugin, 0, 20);
						}
					}
				}
				final ArrayList<SubElement> subelements = new ArrayList<SubElement>();
				boolean shouldSave = false;
				if (subelement != null && !subelement.equals("NULL")) {
					final boolean hasAddon = subelement.contains(";");
					final String[] split = subelement.split(";");
					if (subelement.equals("-")) {
						final Player playero = Bukkit.getPlayer(uuid);
						for (final SubElement sub : Element.getAllSubElements()) {
							if ((playero != null && playero.hasPermission("bending." + sub.getParentElement().getName().toLowerCase() + "." + sub.getName().toLowerCase() + sub.getType().getBending())) && elements.contains(sub.getParentElement())) {
								subelements.add(sub);
								shouldSave = true && playero != null;
							}
						}
					} else if (split[0] != null) {
						if (split[0].contains("m")) {
							subelements.add(Element.METAL);
						}
						if (split[0].contains("v")) {
							subelements.add(Element.LAVA);
						}
						if (split[0].contains("s")) {
							subelements.add(Element.SAND);
						}
						if (split[0].contains("c")) {
							subelements.add(Element.COMBUSTION);
						}
						if (split[0].contains("l")) {
							subelements.add(Element.LIGHTNING);
						}
						if (split[0].contains("t")) {
							subelements.add(Element.SPIRITUAL);
						}
						if (split[0].contains("f")) {
							subelements.add(Element.FLIGHT);
						}
						if (split[0].contains("i")) {
							subelements.add(Element.ICE);
						}
						if (split[0].contains("h")) {
							subelements.add(Element.HEALING);
						}
						if (split[0].contains("b")) {
							subelements.add(Element.BLOOD);
						}
						if (split[0].contains("p")) {
							subelements.add(Element.PLANT);
						}
						if (hasAddon) {
							final CopyOnWriteArrayList<String> addonClone = new CopyOnWriteArrayList<String>(Arrays.asList(split[split.length - 1].split(",")));
							final long startTime = System.currentTimeMillis();
							final long timeoutLength = 30000; // How long until it should time out attempting to load addons in.
							new BukkitRunnable() {
								@Override
								public void run() {
									if (addonClone.isEmpty()) {
										ProjectKorra.log.info("Successfully loaded in all addon subelements!");
										this.cancel();
									} else if (System.currentTimeMillis() - startTime > timeoutLength) {
										ProjectKorra.log.severe("ProjectKorra has timed out after attempting to load in the following addon subelements: " + addonClone.toString());
										ProjectKorra.log.severe("These subelements have taken too long to load in, resulting in users having lost these subelement.");
										this.cancel();
									} else {
										ProjectKorra.log.info("Attempting to load in the following addon subelements... " + addonClone.toString());
										for (final String addon : addonClone) {
											if (Element.getElement(addon) != null && Element.getElement(addon) instanceof SubElement) {
												subelements.add((SubElement) Element.getElement(addon));
												addonClone.remove(addon);
											}
										}
									}
								}
							}.runTaskTimer(ProjectKorra.plugin, 0, 20);
						}
					}
				}

				final HashMap<Integer, String> abilities = new HashMap<Integer, String>();
				final ConcurrentHashMap<Integer, String> abilitiesClone = new ConcurrentHashMap<Integer, String>(abilities);
				for (int i = 1; i <= 9; i++) {
					final String ability = rs2.getString("slot" + i);
					abilitiesClone.put(i, ability);
				}
				final long startTime = System.currentTimeMillis();
				final long timeoutLength = 30000; // How long until it should time out attempting to load addons in.
				new BukkitRunnable() {
					@Override
					public void run() {
						if (abilitiesClone.isEmpty()) {
							// All abilities loaded.
							this.cancel();
						} else if (System.currentTimeMillis() - startTime > timeoutLength) {
							ProjectKorra.log.severe("ProjectKorra has timed out after attempting to load in the following external abilities: " + abilitiesClone.values().toString());
							ProjectKorra.log.severe("These abilities have taken too long to load in, resulting in users having lost them if bound.");
							this.cancel();
						} else {
							for (final int slot : abilitiesClone.keySet()) {
								final String ability = abilitiesClone.get(slot);
								if (ability.equalsIgnoreCase("null")) {
									abilitiesClone.remove(slot);
									continue;
								} else if (CoreAbility.getAbility(ability) != null && CoreAbility.getAbility(ability).isEnabled()) {
									abilities.put(slot, ability);
									abilitiesClone.remove(slot);
									continue;
								}
							}
						}
					}
				}.runTaskTimer(ProjectKorra.plugin, 0, 20);

				p = (permaremoved != null && (permaremoved.equals("true")));

				final boolean boolean_p = p;
				final boolean shouldSave_ = shouldSave;
				new BukkitRunnable() {
					@Override
					public void run() {
						new BendingPlayer(uuid, player, elements, subelements, abilities, boolean_p);
						if (shouldSave_) {
							saveSubElements(BendingPlayer.getBendingPlayer(player));
						}
					}
				}.runTask(ProjectKorra.plugin);
			}
		}
		catch (final SQLException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Deserializes the configuration file "bendingPlayers.yml" of the old
	 * BendingPlugin and creates a converted.yml ready for conversion.
	 *
	 * @throws IOException If the "bendingPlayers.yml" file is not found
	 */
	public static void deserializeFile() {
		final File readFile = new File(".", "bendingPlayers.yml");
		final File writeFile = new File(".", "converted.yml");
		if (readFile.exists()) {
			try (DataInputStream input = new DataInputStream(new FileInputStream(readFile)); BufferedReader reader = new BufferedReader(new InputStreamReader(input));

					DataOutputStream output = new DataOutputStream(new FileOutputStream(writeFile)); BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output))) {

				String line;
				while ((line = reader.readLine()) != null) {
					if (!line.trim().contains("==: BendingPlayer")) {
						writer.write(line + "\n");
					}
				}
			}
			catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void displayColoredParticle(final Location loc, ParticleEffect type, final String hexVal, final float xOffset, final float yOffset, final float zOffset) {
		int r = 0;
		int g = 0;
		int b = 0;
		if (hexVal.length() <= 6) {
			r = Integer.valueOf(hexVal.substring(0, 2), 16).intValue();
			g = Integer.valueOf(hexVal.substring(2, 4), 16).intValue();
			b = Integer.valueOf(hexVal.substring(4, 6), 16).intValue();
		} else if (hexVal.length() <= 7 && hexVal.substring(0, 1).equals("#")) {
			r = Integer.valueOf(hexVal.substring(1, 3), 16).intValue();
			g = Integer.valueOf(hexVal.substring(3, 5), 16).intValue();
			b = Integer.valueOf(hexVal.substring(5, 7), 16).intValue();
		}
		float red = r / 255.0F;
		final float green = g / 255.0F;
		final float blue = b / 255.0F;
		if (red <= 0) {
			red = 1 / 255.0F;
		}
		loc.setX(loc.getX() + Math.random() * xOffset);
		loc.setY(loc.getY() + Math.random() * yOffset);
		loc.setZ(loc.getZ() + Math.random() * zOffset);

		if (type != ParticleEffect.RED_DUST && type != ParticleEffect.REDSTONE && type != ParticleEffect.SPELL_MOB && type != ParticleEffect.MOB_SPELL && type != ParticleEffect.SPELL_MOB_AMBIENT && type != ParticleEffect.MOB_SPELL_AMBIENT) {
			type = ParticleEffect.RED_DUST;
		}
		type.display(red, green, blue, 1F, 0, loc, 255.0);
	}

	public static void displayColoredParticle(final Location loc, final String hexVal) {
		displayColoredParticle(loc, ParticleEffect.RED_DUST, hexVal, 0, 0, 0);
	}

	public static void displayColoredParticle(final Location loc, final String hexVal, final float xOffset, final float yOffset, final float zOffset) {
		displayColoredParticle(loc, ParticleEffect.RED_DUST, hexVal, xOffset, yOffset, zOffset);
	}

	public static void displayParticleVector(final Location loc, final ParticleEffect type, final float xTrans, final float yTrans, final float zTrans) {
		if (type == ParticleEffect.FIREWORKS_SPARK) {
			ParticleEffect.FIREWORKS_SPARK.display(xTrans, yTrans, zTrans, 0.09F, 0, loc, 257D);
		} else if (type == ParticleEffect.SMOKE || type == ParticleEffect.SMOKE_NORMAL) {
			ParticleEffect.SMOKE.display(xTrans, yTrans, zTrans, 0.04F, 0, loc, 257D);
		} else if (type == ParticleEffect.LARGE_SMOKE || type == ParticleEffect.SMOKE_LARGE) {
			ParticleEffect.LARGE_SMOKE.display(xTrans, yTrans, zTrans, 0.04F, 0, loc, 257D);
		} else if (type == ParticleEffect.ENCHANTMENT_TABLE) {
			ParticleEffect.ENCHANTMENT_TABLE.display(xTrans, yTrans, zTrans, 0.5F, 0, loc, 257D);
		} else if (type == ParticleEffect.PORTAL) {
			ParticleEffect.PORTAL.display(xTrans, yTrans, zTrans, 0.5F, 0, loc, 257D);
		} else if (type == ParticleEffect.FLAME) {
			ParticleEffect.FLAME.display(xTrans, yTrans, zTrans, 0.04F, 0, loc, 257D);
		} else if (type == ParticleEffect.CLOUD) {
			ParticleEffect.CLOUD.display(xTrans, yTrans, zTrans, 0.04F, 0, loc, 257D);
		} else if (type == ParticleEffect.SNOW_SHOVEL) {
			ParticleEffect.SNOW_SHOVEL.display(xTrans, yTrans, zTrans, 0.2F, 0, loc, 257D);
		} else {
			ParticleEffect.RED_DUST.display(0, 0, 0, 0.004F, 0, loc, 257D);
		}
	}

	/**
	 * Drops a {@code Collection<ItemStack>} of items on a specified block.
	 *
	 * @param block The block to drop items on.
	 * @param items The items to drop.
	 */
	public static void dropItems(final Block block, final Collection<ItemStack> items) {
		for (final ItemStack item : items) {
			block.getWorld().dropItem(block.getLocation(), item);
		}
	}

	public static void displayMovePreview(final Player player) {
		displayMovePreview(player, player.getInventory().getHeldItemSlot() + 1);
	}

	public static void displayMovePreview(final Player player, final int slot) {
		if (!ConfigManager.defaultConfig.get().getBoolean("Properties.BendingPreview")) {
			return;
		}

		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return;
		}
		String displayedMessage = bPlayer.getAbilities().get(slot);
		final CoreAbility ability = CoreAbility.getAbility(displayedMessage);

		if (ability != null && bPlayer != null) {
			if (bPlayer.isOnCooldown(ability)) {
				final long cooldown = bPlayer.getCooldown(ability.getName()) - System.currentTimeMillis();
				displayedMessage = ability.getElement().getColor() + "" + ChatColor.STRIKETHROUGH + ability.getName() + "" + ability.getElement().getColor() + " - " + TimeUtil.formatTime(cooldown);
			} else {
				if (bPlayer.getStance() != null && bPlayer.getStance().getName().equals(ability.getName())) {
					displayedMessage = ability.getElement().getColor() + "" + ChatColor.UNDERLINE + ability.getName();
				} else {
					displayedMessage = ability.getElement().getColor() + ability.getName();
				}
			}
		} else if (displayedMessage == null || displayedMessage.isEmpty() || displayedMessage.equals("")) {
			displayedMessage = "";
		}

		ActionBar.sendActionBar(displayedMessage, player);
	}

	public static float getAbsorbationHealth(final Player player) {

		try {
			final Object entityplayer = ActionBar.getHandle.invoke(player);
			final Object hearts = getAbsorption.invoke(entityplayer);
			return (float) hearts;
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static void setAbsorbationHealth(final Player player, final float hearts) {

		try {
			final Object entityplayer = ActionBar.getHandle.invoke(player);
			setAbsorption.invoke(entityplayer, hearts);
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This gets the BlockFace in the specified dimension of a certain value
	 *
	 * @param xyz 0 for x, 1 for y, 2 for z
	 * @param value vector value for which direction to check
	 * @return BlockFace for block in specified dimension and value
	 */
	public static BlockFace getBlockFaceFromValue(final int xyz, final double value) {
		switch (xyz) {
			case 0:
				if (value > 0) {
					return BlockFace.EAST;
				} else if (value < 0) {
					return BlockFace.WEST;
				} else {
					return BlockFace.SELF;
				}
			case 1:
				if (value > 0) {
					return BlockFace.UP;
				} else if (value < 0) {
					return BlockFace.DOWN;
				} else {
					return BlockFace.SELF;
				}
			case 2:
				if (value > 0) {
					return BlockFace.SOUTH;
				} else if (value < 0) {
					return BlockFace.NORTH;
				} else {
					return BlockFace.SELF;
				}
			default:
				return null;
		}
	}

	public static List<Block> getBlocksAlongLine(final Location ploc, final Location tloc, final World w) {
		final List<Block> blocks = new ArrayList<Block>();

		// Next we will name each coordinate
		final int x1 = ploc.getBlockX();
		final int y1 = ploc.getBlockY();
		final int z1 = ploc.getBlockZ();

		final int x2 = tloc.getBlockX();
		final int y2 = tloc.getBlockY();
		final int z2 = tloc.getBlockZ();

		// Then we create the following integers
		int xMin, yMin, zMin;
		int xMax, yMax, zMax;
		int x, y, z;

		// Now we need to make sure xMin is always lower then xMax
		if (x1 > x2) { // If x1 is a higher number then x2
			xMin = x2;
			xMax = x1;
		} else {
			xMin = x1;
			xMax = x2;
		}
		// Same with Y
		if (y1 > y2) {
			yMin = y2;
			yMax = y1;
		} else {
			yMin = y1;
			yMax = y2;
		}

		// And Z
		if (z1 > z2) {
			zMin = z2;
			zMax = z1;
		} else {
			zMin = z1;
			zMax = z2;
		}

		// Now it's time for the loop
		for (x = xMin; x <= xMax; x++) {
			for (y = yMin; y <= yMax; y++) {
				for (z = zMin; z <= zMax; z++) {
					final Block b = new Location(w, x, y, z).getBlock();
					blocks.add(b);
				}
			}
		}

		// And last but not least, we return with the list
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
	public static List<Block> getBlocksAroundPoint(final Location location, final double radius) {
		final List<Block> blocks = new ArrayList<Block>();

		final int xorg = location.getBlockX();
		final int yorg = location.getBlockY();
		final int zorg = location.getBlockZ();

		final int r = (int) radius * 4;

		for (int x = xorg - r; x <= xorg + r; x++) {
			for (int y = yorg - r; y <= yorg + r; y++) {
				for (int z = zorg - r; z <= zorg + r; z++) {
					final Block block = location.getWorld().getBlockAt(x, y, z);
					if (block.getLocation().distanceSquared(location) <= radius * radius) {
						blocks.add(block);
					}
				}
			}
		}
		return blocks;
	}

	public static BlockFace getCardinalDirection(final Vector vector) {
		final BlockFace[] faces = { BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST };
		Vector n, ne, e, se, s, sw, w, nw;
		w = new Vector(-1, 0, 0);
		n = new Vector(0, 0, -1);
		s = n.clone().multiply(-1);
		e = w.clone().multiply(-1);
		ne = n.clone().add(e.clone()).normalize();
		se = s.clone().add(e.clone()).normalize();
		nw = n.clone().add(w.clone()).normalize();
		sw = s.clone().add(w.clone()).normalize();

		final Vector[] vectors = { n, ne, e, se, s, sw, w, nw };

		double comp = 0;
		int besti = 0;
		for (int i = 0; i < vectors.length; i++) {
			final double dot = vector.dot(vectors[i]);
			if (dot > comp) {
				comp = dot;
				besti = i;
			}
		}
		return faces[besti];
	}

	public static List<Location> getCircle(final Location loc, final int radius, final int height, final boolean hollow, final boolean sphere, final int plusY) {
		final List<Location> circleblocks = new ArrayList<Location>();
		final int cx = loc.getBlockX();
		final int cy = loc.getBlockY();
		final int cz = loc.getBlockZ();

		for (int x = cx - radius; x <= cx + radius; x++) {
			for (int z = cz - radius; z <= cz + radius; z++) {
				for (int y = (sphere ? cy - radius : cy); y < (sphere ? cy + radius : cy + height); y++) {
					final double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0);

					if (dist < radius * radius && !(hollow && dist < (radius - 1) * (radius - 1))) {
						final Location l = new Location(loc.getWorld(), x, y + plusY, z);
						circleblocks.add(l);
					}
				}
			}
		}
		return circleblocks;
	}

	public static String getCurrentDate() {
		final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		final Date date = new Date();
		return dateFormat.format(date);
	}

	public static Vector getDirection(final Location location, final Location destination) {
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

	public static double getDistanceFromLine(final Vector line, final Location pointonline, final Location point) {
		final Vector AP = new Vector();
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
	public static Collection<ItemStack> getDrops(final Block block, final Material type, final byte data, final ItemStack breakitem) {
		final BlockState tempstate = block.getState();
		block.setType(type);
		block.setData(data);
		final Collection<ItemStack> item = block.getDrops();
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
	public static List<Entity> getEntitiesAroundPoint(final Location location, final double radius) {
		final List<Entity> entities = new ArrayList<Entity>();
		final World world = location.getWorld();

		// To find chunks we use chunk coordinates (not block coordinates!)
		final int smallX = (int) (location.getX() - radius) >> 4;
		final int bigX = (int) (location.getX() + radius) >> 4;
		final int smallZ = (int) (location.getZ() - radius) >> 4;
		final int bigZ = (int) (location.getZ() + radius) >> 4;

		for (int x = smallX; x <= bigX; x++) {
			for (int z = smallZ; z <= bigZ; z++) {
				if (world.isChunkLoaded(x, z)) {
					entities.addAll(Arrays.asList(world.getChunkAt(x, z).getEntities()));
				}
			}
		}

		final Iterator<Entity> entityIterator = entities.iterator();
		while (entityIterator.hasNext()) {
			final Entity e = entityIterator.next();
			if (e.getWorld().equals(location.getWorld()) && e.getLocation().distanceSquared(location) > radius * radius) {
				entityIterator.remove();
			} else if (e instanceof Player && (((Player) e).isDead() || ((Player) e).getGameMode().equals(GameMode.SPECTATOR))) {
				entityIterator.remove();
			}
		}

		return entities;
	}

	public static long getGlobalCooldown() {
		return ConfigManager.defaultConfig.get().getLong("Properties.GlobalCooldown");
	}

	/**
	 *
	 * @param one One location being tested
	 * @param two Another location being tested
	 * @return The horizontal distance between two locations
	 */
	public static double getHorizontalDistance(final Location one, final Location two) {
		final double x = one.getX() - two.getX();
		final double z = one.getZ() - two.getZ();
		return Math.sqrt((x * x) + (z * z));
	}

	public static int getIntCardinalDirection(final Vector vector) {
		final BlockFace face = getCardinalDirection(vector);

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
			default:
				return 4;
		}
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
	public static String getLastUsedAbility(final Player player, final boolean checkCombos) {
		final List<AbilityInformation> lastUsedAbility = ComboManager.getRecentlyUsedAbilities(player, 1);
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
	public static Location getLeftSide(final Location location, final double distance) {
		final float angle = location.getYaw() / 60;
		return location.clone().add(new Vector(Math.cos(angle), 0, Math.sin(angle)).normalize().multiply(distance));
	}

	public static int getMaxPresets(final Player player) {
		if (player.isOp()) {
			return 100;
		}
		int cap = 0;
		for (int i = 0; i <= 10; i++) {
			if (player.hasPermission("bending.command.preset.create." + i)) {
				cap = i;
			}
		}
		return cap;
	}

	public static Vector getOrthogonalVector(final Vector axis, final double degrees, final double length) {
		Vector ortho = new Vector(axis.getY(), -axis.getX(), 0);
		ortho = ortho.normalize();
		ortho = ortho.multiply(length);

		return rotateVectorAroundVector(axis, ortho, degrees);
	}

	public static Collection<Player> getPlayersAroundPoint(final Location location, final double distance) {
		final Collection<Player> players = new HashSet<Player>();
		for (final Player player : Bukkit.getOnlinePlayers()) {
			if (player.getLocation().getWorld().equals(location.getWorld())) {
				if (player.getLocation().distanceSquared(location) <= distance * distance) {
					players.add(player);
				}
			}
		}
		return players;
	}

	public static Location getPointOnLine(final Location origin, final Location target, final double distance) {
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
	public static Location getRightSide(final Location location, final double distance) {
		final float angle = location.getYaw() / 60;
		return location.clone().subtract(new Vector(Math.cos(angle), 0, Math.sin(angle)).normalize().multiply(distance));
	}

	public static Plugin getProbending() {
		if (hasProbending()) {
			return Bukkit.getServer().getPluginManager().getPlugin("Probending");
		}
		return null;
	}

	public static Plugin getRPG() {
		if (hasRPG()) {
			return Bukkit.getServer().getPluginManager().getPlugin("ProjectKorraRPG");
		}
		return null;
	}

	public static Plugin getSpirits() {
		if (hasSpirits()) {
			return Bukkit.getServer().getPluginManager().getPlugin("ProjectKorraSpirits");
		}
		return null;
	}

	public static Entity getTargetedEntity(final Player player, final double range, final List<Entity> avoid) {
		double longestr = range + 1;
		Entity target = null;
		final Location origin = player.getEyeLocation();
		final Vector direction = player.getEyeLocation().getDirection().normalize();
		for (final Entity entity : origin.getWorld().getEntities()) {
			if (entity instanceof Player) {
				if (((Player) entity).isDead() || ((Player) entity).getGameMode().equals(GameMode.SPECTATOR)) {
					continue;
				}
			}
			if (avoid.contains(entity)) {
				continue;
			}
			if (entity.getWorld().equals(origin.getWorld())) {
				if (entity.getLocation().distanceSquared(origin) < longestr * longestr && getDistanceFromLine(direction, origin, entity.getLocation()) < 2 && (entity instanceof LivingEntity) && entity.getEntityId() != player.getEntityId() && entity.getLocation().distanceSquared(origin.clone().add(direction)) < entity.getLocation().distanceSquared(origin.clone().add(direction.clone().multiply(-1)))) {
					target = entity;
					longestr = entity.getLocation().distance(origin);
				}
			}
		}
		if (target != null) {
			if (GeneralMethods.isObstructed(origin, target.getLocation())) {
				target = null;
			}
		}
		return target;
	}

	public static Entity getTargetedEntity(final Player player, final double range) {
		return getTargetedEntity(player, range, new ArrayList<Entity>());
	}

	public static Location getTargetedLocation(final Player player, final double originselectrange, final Material... nonOpaque2) {
		final Location origin = player.getEyeLocation();
		final Vector direction = origin.getDirection();

		HashSet<Material> trans = new HashSet<Material>();
		trans.add(Material.AIR);

		if (nonOpaque2 == null) {
			trans = null;
		} else {
			for (final Material material : nonOpaque2) {
				trans.add(material);
			}
		}

		final Block block = player.getTargetBlock(trans, (int) originselectrange + 1);
		double distance = originselectrange;
		if (block.getWorld().equals(origin.getWorld())) {
			distance = block.getLocation().distance(origin) - 1.5;
		}
		final Location location = origin.add(direction.multiply(distance));

		return location;
	}

	public static Location getTargetedLocation(final Player player, final int range) {
		return getTargetedLocation(player, range, Material.AIR);
	}

	public static Block getTopBlock(final Location loc, final int range) {
		return getTopBlock(loc, range, range);
	}

	/**
	 * Returns the top block based around loc. PositiveY is the maximum amount
	 * of distance it will check upward. Similarly, negativeY is for downward.
	 */
	public static Block getTopBlock(final Location loc, final int positiveY, final int negativeY) {
		Block blockHolder = loc.getBlock();
		int y = 0;
		// Only one of these while statements will go
		while (blockHolder.getType() != Material.AIR && Math.abs(y) < Math.abs(positiveY)) {
			y++;
			final Block tempBlock = loc.clone().add(0, y, 0).getBlock();
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
		return blockHolder;
	}

	public static Block getBottomBlock(final Location loc, final int positiveY, final int negativeY) {
		Block blockHolder = loc.getBlock();
		int y = 0;
		// Only one of these while statements will go
		while (blockHolder.getType() != Material.AIR && Math.abs(y) < Math.abs(negativeY)) {
			y--;
			final Block tempblock = loc.clone().add(0, y, 0).getBlock();
			if (tempblock.getType() == Material.AIR) {
				return blockHolder;
			}

			blockHolder = tempblock;
		}

		while (blockHolder.getType() != Material.AIR && Math.abs(y) < Math.abs(positiveY)) {
			y++;
			blockHolder = loc.clone().add(0, y, 0).getBlock();
			if (blockHolder.getType() == Material.AIR) {
				return blockHolder;
			}
		}

		return blockHolder;
	}

	public static ArrayList<Element> getElementsWithNoWeaponBending() {
		final ArrayList<Element> elements = new ArrayList<Element>();

		if (!plugin.getConfig().getBoolean("Properties.Air.CanBendWithWeapons")) {
			elements.add(Element.AIR);
		}
		if (!plugin.getConfig().getBoolean("Properties.Water.CanBendWithWeapons")) {
			elements.add(Element.WATER);
		}
		if (!plugin.getConfig().getBoolean("Properties.Earth.CanBendWithWeapons")) {
			elements.add(Element.EARTH);
		}
		if (!plugin.getConfig().getBoolean("Properties.Fire.CanBendWithWeapons")) {
			elements.add(Element.FIRE);
		}
		if (!plugin.getConfig().getBoolean("Properties.Chi.CanBendWithWeapons")) {
			elements.add(Element.CHI);
		}

		return elements;
	}

	public static boolean hasItems() {
		return Bukkit.getServer().getPluginManager().getPlugin("ProjectKorraItems") != null;
	}

	public static boolean hasProbending() {
		return Bukkit.getServer().getPluginManager().getPlugin("Probending") != null;
	}

	public static boolean hasRPG() {
		return Bukkit.getServer().getPluginManager().getPlugin("ProjectKorraRPG") != null;
	}

	public static boolean hasSpirits() {
		return Bukkit.getServer().getPluginManager().getPlugin("ProjectKorraSpirits") != null;
	}

	public static boolean isAdjacentToThreeOrMoreSources(final Block block) {
		if (block == null || (TempBlock.isTempBlock(block) && WaterAbility.isBendableWaterTempBlock(block))) {
			return false;
		}
		int sources = 0;
		final byte full = 0x0;
		final BlockFace[] faces = { BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH };
		for (final BlockFace face : faces) {
			final Block blocki = block.getRelative(face);
			if ((blocki.getType() == Material.LAVA || blocki.getType() == Material.STATIONARY_LAVA) && blocki.getData() == full && EarthPassive.canPhysicsChange(blocki)) {
				sources++;
			}
			if ((ElementalAbility.isWater(blocki) || ElementalAbility.isIce(blocki)) && blocki.getData() == full && WaterManipulation.canPhysicsChange(blocki)) {
				sources++;
			}
		}
		return sources >= 2;
	}

	public static boolean isImportEnabled() {
		return ConfigManager.defaultConfig.get().getBoolean("Properties.ImportEnabled");
	}

	public static boolean isInteractable(final Block block) {
		return Arrays.asList(INTERACTABLE_MATERIALS).contains(block.getType());
	}

	public static boolean isObstructed(final Location location1, final Location location2) {
		final Vector loc1 = location1.toVector();
		final Vector loc2 = location2.toVector();

		final Vector direction = loc2.subtract(loc1);
		direction.normalize();

		Location loc;

		double max = 0;
		if (location1.getWorld().equals(location2.getWorld())) {
			max = location1.distance(location2);
		}

		for (double i = 0; i <= max; i++) {
			loc = location1.clone().add(direction.clone().multiply(i));
			final Material type = loc.getBlock().getType();
			if (type != Material.AIR && !(ElementalAbility.getTransparentMaterialSet().contains(type) || ElementalAbility.isWater(loc.getBlock()))) {
				return true;
			}
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
	public static boolean isRegionProtectedFromBuild(final Player player, final String ability, final Location loc) {
		if (!BLOCK_CACHE.containsKey(player.getName())) {
			BLOCK_CACHE.put(player.getName(), new ConcurrentHashMap<Block, BlockCacheElement>());
		}

		final Map<Block, BlockCacheElement> blockMap = BLOCK_CACHE.get(player.getName());
		final Block block = loc.getBlock();
		if (blockMap.containsKey(block)) {
			final BlockCacheElement elem = blockMap.get(block);

			// both abilities must be equal to each other to use the cache
			if ((ability == null && elem.getAbility() == null) || (ability != null && elem.getAbility() != null && elem.getAbility().equals(ability))) {
				return elem.isAllowed();
			}
		}

		final boolean value = isRegionProtectedFromBuildPostCache(player, ability, loc);
		blockMap.put(block, new BlockCacheElement(player, block, ability, value, System.currentTimeMillis()));
		return value;
	}

	public static boolean isRegionProtectedFromBuild(final Ability ability, final Location loc) {
		return isRegionProtectedFromBuild(ability.getPlayer(), ability.getName(), loc);
	}

	public static boolean isRegionProtectedFromBuild(final Player player, final Location loc) {
		return isRegionProtectedFromBuild(player, null, loc);
	}

	public static boolean isRegionProtectedFromBuildPostCache(final Player player, final String ability, final Location loc) {
		final boolean allowHarmless = ConfigManager.defaultConfig.get().getBoolean("Properties.RegionProtection.AllowHarmlessAbilities");
		final boolean respectWorldGuard = ConfigManager.defaultConfig.get().getBoolean("Properties.RegionProtection.RespectWorldGuard");
		final boolean respectPreciousStones = ConfigManager.defaultConfig.get().getBoolean("Properties.RegionProtection.RespectPreciousStones");
		final boolean respectFactions = ConfigManager.defaultConfig.get().getBoolean("Properties.RegionProtection.RespectFactions");
		final boolean respectTowny = ConfigManager.defaultConfig.get().getBoolean("Properties.RegionProtection.RespectTowny");
		final boolean respectGriefPrevention = ConfigManager.defaultConfig.get().getBoolean("Properties.RegionProtection.RespectGriefPrevention");
		final boolean respectLWC = ConfigManager.defaultConfig.get().getBoolean("Properties.RegionProtection.RespectLWC");
		final boolean respectResidence = ConfigManager.defaultConfig.get().getBoolean("Properties.RegionProtection.Residence.Respect");
		final boolean respectKingdoms = ConfigManager.defaultConfig.get().getBoolean("Properties.RegionProtection.Kingdoms");

		boolean isIgnite = false;
		boolean isExplosive = false;
		boolean isHarmless = false;
		final CoreAbility coreAbil = CoreAbility.getAbility(ability);
		if (coreAbil != null) {
			isIgnite = coreAbil.isIgniteAbility();
			isExplosive = coreAbil.isExplosiveAbility();
			isHarmless = coreAbil.isHarmlessAbility();
		}

		if (ability == null && allowHarmless) {
			return false;
		}
		if (isHarmless && allowHarmless) {
			return false;
		}

		final PluginManager pm = Bukkit.getPluginManager();

		final Plugin wgp = pm.getPlugin("WorldGuard");
		final Plugin psp = pm.getPlugin("PreciousStones");
		final Plugin fcp = pm.getPlugin("Factions");
		final Plugin twnp = pm.getPlugin("Towny");
		final Plugin gpp = pm.getPlugin("GriefPrevention");
		final Plugin massivecore = pm.getPlugin("MassiveCore");
		final Plugin lwc = pm.getPlugin("LWC");
		final Plugin residence = pm.getPlugin("Residence");
		final Plugin kingdoms = pm.getPlugin("Kingdoms");

		for (final Location location : new Location[] { loc, player.getLocation() }) {
			final World world = location.getWorld();

			if (lwc != null && respectLWC) {
				final LWCPlugin lwcp = (LWCPlugin) lwc;
				final LWC lwc2 = lwcp.getLWC();
				final Protection protection = lwc2.getProtectionCache().getProtection(location.getBlock());
				if (protection != null) {
					if (!lwc2.canAccessProtection(player, protection)) {
						return true;
					}
				}
			}
			if (wgp != null && respectWorldGuard && !player.hasPermission("worldguard.region.bypass." + world.getName())) {
				final WorldGuardPlugin wg = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
				if (!player.isOnline()) {
					return true;
				}

				if (isIgnite) {
					if (!wg.hasPermission(player, "worldguard.override.lighter")) {
						if (wg.getGlobalStateManager().get(world).blockLighter) {
							return true;
						}
					}
				}
				if (isExplosive) {
					if (wg.getGlobalStateManager().get(location.getWorld()).blockTNTExplosions) {
						return true;
					}
					if (!wg.getRegionContainer().createQuery().testBuild(location, player, DefaultFlag.TNT)) {
						return true;
					}
				}

				if (!wg.canBuild(player, location.getBlock())) {
					return true;
				}
			}

			if (psp != null && respectPreciousStones) {
				final PreciousStones ps = (PreciousStones) psp;

				if (isIgnite) {
					if (ps.getForceFieldManager().hasSourceField(location, FieldFlag.PREVENT_FIRE)) {
						return true;
					}
				}
				if (isExplosive) {
					if (ps.getForceFieldManager().hasSourceField(location, FieldFlag.PREVENT_EXPLOSIONS)) {
						return true;
					}
				}

				if (!PreciousStones.API().canBreak(player, location)) {
					return true;
				}
			}

			if (fcp != null && massivecore != null && respectFactions) {
				if (!EngineMain.canPlayerBuildAt(player, PS.valueOf(loc.getBlock()), false)) {
					return true;
				}
			}

			if (twnp != null && respectTowny) {
				final Towny twn = (Towny) twnp;

				WorldCoord worldCoord;

				try {
					final TownyWorld tWorld = TownyUniverse.getDataSource().getWorld(world.getName());
					worldCoord = new WorldCoord(tWorld.getName(), Coord.parseCoord(location));

					final boolean bBuild = PlayerCacheUtil.getCachePermission(player, location, 3, (byte) 0, TownyPermission.ActionType.BUILD);

					if (!bBuild) {
						final PlayerCache cache = twn.getCache(player);
						final TownBlockStatus status = cache.getStatus();

						if (((status == TownBlockStatus.ENEMY) && TownyWarConfig.isAllowingAttacks())) {
							try {
								TownyWar.callAttackCellEvent(twn, player, location.getBlock(), worldCoord);
							}
							catch (final Exception e) {
								TownyMessaging.sendErrorMsg(player, e.getMessage());
							}
							return true;
						} else if (status == TownBlockStatus.WARZONE) {

						} else {
							return true;
						}

						if ((cache.hasBlockErrMsg())) {
							TownyMessaging.sendErrorMsg(player, cache.getBlockErrMsg());
						}
					}
				}
				catch (final Exception e1) {
					TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_not_configured"));
				}
			}

			if (gpp != null && respectGriefPrevention) {
				Material type = player.getWorld().getBlockAt(location).getType();
				if (type == null) {
					type = Material.AIR;
				}
				final String reason = GriefPrevention.instance.allowBuild(player, location); // WORKING with WorldGuard 6.0 BETA 4.

				final Claim claim = GriefPrevention.instance.dataStore.getClaimAt(loc, true, null);

				if (reason != null && claim != null) {
					return true;
				}
			}

			if (residence != null && respectResidence) {
				final ClaimedResidence res = Residence.getResidenceManager().getByLoc(loc);
				if (res != null) {
					final ResidencePermissions perms = res.getPermissions();
					if (perms.playerHas(player.getName(), ConfigManager.defaultConfig.get().getString("Properities.RegionProtection.Residence.Flag"), true)) {
						return true;
					}
				}
			}

			if (kingdoms != null && respectKingdoms) {
				final SimpleLocation location_ = new SimpleLocation(loc);
				final SimpleChunkLocation chunk = location_.toSimpleChunk();
				final Land land = GameManagement.getLandManager().getOrLoadLand(chunk);

				if (land.getOwner() != null) {
					final KingdomPlayer kp = GameManagement.getPlayerManager().getSession(player);

					if (!kp.isAdminMode()) {
						if (land.getOwner().equals("SafeZone")) {
							return true;
						} else if (kp.getKingdom() == null) { // If the player isn't in a kingdom but it's claimed land.
							return true;
						} else {
							final Kingdom kingdom = kp.getKingdom();
							final String kingdomName = kingdom.getKingdomName();
							if (!kingdomName.equals(land.getOwner())) // If the player's kingdom doesn't match.
							{
								return true;
							}

							// If it's within the nexus area, test for higher permission.
							if (land.getStructure() != null && land.getStructure().getType() == StructureType.NEXUS) {
								if (!kp.getRank().isHigherOrEqualTo(kingdom.getPermissionsInfo().getBuildInNexus())) {
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	public static boolean isSolid(final Block block) {
		return !Arrays.asList(NON_OPAQUE).contains(block.getType());
	}

	/** Checks if an entity is Undead **/
	public static boolean isUndead(final Entity entity) {
		return entity != null && (entity.getType() == EntityType.ZOMBIE || entity.getType() == EntityType.BLAZE || entity.getType() == EntityType.GIANT || entity.getType() == EntityType.IRON_GOLEM || entity.getType() == EntityType.MAGMA_CUBE || entity.getType() == EntityType.PIG_ZOMBIE || entity.getType() == EntityType.SKELETON || entity.getType() == EntityType.SLIME || entity.getType() == EntityType.SNOWMAN || entity.getType() == EntityType.ZOMBIE);
	}

	public static boolean isWeapon(final Material mat) {
		return mat != null && (mat == Material.WOOD_AXE || mat == Material.WOOD_PICKAXE || mat == Material.WOOD_SPADE || mat == Material.WOOD_SWORD || mat == Material.STONE_AXE || mat == Material.STONE_PICKAXE || mat == Material.STONE_SPADE || mat == Material.STONE_SWORD || mat == Material.IRON_AXE || mat == Material.IRON_PICKAXE || mat == Material.IRON_SWORD || mat == Material.IRON_SPADE || mat == Material.DIAMOND_AXE || mat == Material.DIAMOND_PICKAXE || mat == Material.DIAMOND_SWORD || mat == Material.DIAMOND_SPADE || mat == Material.GOLD_AXE || mat == Material.GOLD_HOE || mat == Material.GOLD_SWORD || mat == Material.GOLD_PICKAXE || mat == Material.GOLD_SPADE);
	}

	public static void loadBendingPlayer(final BendingPlayer pl) {
		final Player player = Bukkit.getPlayer(pl.getUUID());
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer == null) {
			return;
		}

		if (PKListener.getToggledOut().contains(player.getUniqueId())) {
			bPlayer.toggleBending();
			player.sendMessage(ChatColor.YELLOW + "Reminder, you toggled your bending before signing off. Enable it again with /bending toggle.");
		}

		Preset.loadPresets(player);
		Element element = null;
		String prefix = "";

		final boolean chatEnabled = ConfigManager.languageConfig.get().getBoolean("Chat.Enable");
		if (bPlayer.getElements().size() > 1) {
			prefix = Element.AVATAR.getPrefix();
		} else if (bPlayer.getElements().size() == 1) {
			element = bPlayer.getElements().get(0);
			prefix = element.getPrefix();
		} else {
			prefix = ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', ConfigManager.languageConfig.get().getString("Chat.Prefixes.Nonbender")) + " ";
		}

		if (chatEnabled) {
			player.setDisplayName(player.getName());
			player.setDisplayName(prefix + ChatColor.RESET + player.getDisplayName());
		}

		// Handle the AirSpout/WaterSpout login glitches.
		if (player.getGameMode() != GameMode.CREATIVE) {
			final HashMap<Integer, String> bound = bPlayer.getAbilities();
			for (final String str : bound.values()) {
				if (str.equalsIgnoreCase("AirSpout") || str.equalsIgnoreCase("WaterSpout") || str.equalsIgnoreCase("SandSpout")) {
					final Player fplayer = player;
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
		Bukkit.getServer().getPluginManager().callEvent(new BendingPlayerCreationEvent(bPlayer));
	}

	public static void reloadPlugin(final CommandSender sender) {
		ProjectKorra.log.info("Reloading ProjectKorra and configuration");
		final BendingReloadEvent event = new BendingReloadEvent(sender);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			sender.sendMessage(ChatColor.RED + "Reload event cancelled");
			return;
		}
		if (DBConnection.isOpen()) {
			DBConnection.sql.close();
		}
		GeneralMethods.stopBending();
		ConfigManager.defaultConfig.reload();
		ConfigManager.languageConfig.reload();
		ConfigManager.presetConfig.reload();
		Preset.loadExternalPresets();
		new MultiAbilityManager();
		new ComboManager();
		// Stop the previous collision detection task before creating new manager.
		ProjectKorra.collisionManager.stopCollisionDetection();
		ProjectKorra.collisionManager = new CollisionManager();
		ProjectKorra.collisionInitializer = new CollisionInitializer(ProjectKorra.collisionManager);
		CoreAbility.registerAbilities();
		reloadAddonPlugins();
		ProjectKorra.collisionInitializer.initializeDefaultCollisions(); // must be called after abilities have been registered.
		ProjectKorra.collisionManager.startCollisionDetection();

		DBConnection.init();

		if (!DBConnection.isOpen()) {
			ProjectKorra.log.severe("Unable to enable ProjectKorra due to the database not being open");
			stopPlugin();
		}
		for (final Player player : Bukkit.getOnlinePlayers()) {
			Preset.unloadPreset(player);
			GeneralMethods.createBendingPlayer(player.getUniqueId(), player.getName());
			PassiveManager.registerPassives(player);
		}
		plugin.updater.checkUpdate();
		ProjectKorra.log.info("Reload complete");
	}

	public static void reloadAddonPlugins() {
		for (int i = CoreAbility.getAddonPlugins().size() - 1; i > -1; i--) {
			final String entry = CoreAbility.getAddonPlugins().get(i);
			final String[] split = entry.split("::");
			if (Bukkit.getServer().getPluginManager().isPluginEnabled(split[0])) {
				CoreAbility.registerPluginAbilities((JavaPlugin) Bukkit.getServer().getPluginManager().getPlugin(split[0]), split[1]);
			} else {
				CoreAbility.getAddonPlugins().remove(i);
			}
		}
	}

	public static void removeBlock(final Block block) {
		if (isAdjacentToThreeOrMoreSources(block)) {
			block.setType(Material.WATER);
			block.setData((byte) 0x0);
		} else {
			block.setType(Material.AIR);
		}
	}

	public static void removeUnusableAbilities(final String player) {
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return;
		}

		// Remove all active instances of abilities that will become unusable.
		// We need to do this prior to filtering binds in case the player has a MultiAbility running.
		for (final CoreAbility coreAbility : CoreAbility.getAbilities()) {
			final CoreAbility playerAbility = CoreAbility.getAbility(bPlayer.getPlayer(), coreAbility.getClass());
			if (playerAbility != null) {
				if (playerAbility instanceof PassiveAbility && PassiveManager.hasPassive(bPlayer.getPlayer(), playerAbility)) {
					// The player will be able to keep using the given PassiveAbility.
					continue;
				} else if (bPlayer.canBend(playerAbility)) {
					// The player will still be able to use this given Ability, do not end it.
					continue;
				}

				playerAbility.remove();
			}
		}

		// Remove all bound abilities that will become unusable.
		final HashMap<Integer, String> slots = bPlayer.getAbilities();
		final HashMap<Integer, String> finalAbilities = new HashMap<Integer, String>();
		for (final int i : slots.keySet()) {
			if (bPlayer.canBind(CoreAbility.getAbility(slots.get(i)))) {
				// The player will still be able to use this given Ability, do not remove it from their binds.
				finalAbilities.put(i, slots.get(i));
			}
		}

		bPlayer.setAbilities(finalAbilities);
	}

	public static Vector rotateVectorAroundVector(final Vector axis, final Vector rotator, final double degrees) {
		final double angle = Math.toRadians(degrees);
		Vector rotation = axis.clone();
		final Vector rotate = rotator.clone();
		rotation = rotation.normalize();

		final Vector thirdaxis = rotation.crossProduct(rotate).normalize().multiply(rotate.length());

		return rotate.multiply(Math.cos(angle)).add(thirdaxis.multiply(Math.sin(angle)));
	}

	/**
	 * Rotates a vector around the Y plane.
	 */
	public static Vector rotateXZ(final Vector vec, final double theta) {
		final Vector vec2 = vec.clone();
		final double x = vec2.getX();
		final double z = vec2.getZ();
		vec2.setX(x * Math.cos(Math.toRadians(theta)) - z * Math.sin(Math.toRadians(theta)));
		vec2.setZ(x * Math.sin(Math.toRadians(theta)) + z * Math.cos(Math.toRadians(theta)));
		return vec2;
	}

	public static void runDebug() {
		final File debugFile = new File(plugin.getDataFolder(), "debug.txt");
		if (debugFile.exists()) {
			debugFile.delete(); // We're starting brand new.
		}
		writeToDebug("ProjectKorra Debug: Paste this on http://pastie.org and put it in your bug report thread.");
		writeToDebug("====================");
		writeToDebug("");
		writeToDebug("");
		writeToDebug("Date Created: " + getCurrentDate());
		writeToDebug("Java Version: " + Runtime.class.getPackage().getImplementationVersion());
		writeToDebug("Bukkit Version: " + Bukkit.getServer().getVersion());
		writeToDebug("");
		writeToDebug("ProjectKorra (Core) Information");
		writeToDebug("====================");
		writeToDebug("Version: " + plugin.getDescription().getVersion());
		writeToDebug("Author: " + plugin.getDescription().getAuthors());
		final List<String> officialSidePlugins = new ArrayList<String>();
		if (hasRPG()) {
			officialSidePlugins.add("ProjectKorra RPG v" + getRPG().getDescription().getVersion());
		}
		if (hasItems()) {
			officialSidePlugins.add("ProjectKorra Items v" + getItems().getDescription().getVersion());
		}
		if (hasSpirits()) {
			officialSidePlugins.add("ProjectKorra Spirits v" + getSpirits().getDescription().getVersion());
		}
		if (hasProbending()) {
			officialSidePlugins.add("Probending v" + getProbending().getDescription().getVersion());
		}
		if (!officialSidePlugins.isEmpty()) {
			writeToDebug("");
			writeToDebug("ProjectKorra (Side Plugin) Information");
			writeToDebug("====================");
			for (final String line : officialSidePlugins) {
				writeToDebug(line);
			}
		}

		writeToDebug("");
		writeToDebug("Supported Plugins");
		writeToDebug("====================");

		final boolean respectWorldGuard = ConfigManager.defaultConfig.get().getBoolean("Properties.RegionProtection.RespectWorldGuard");
		final boolean respectPreciousStones = ConfigManager.defaultConfig.get().getBoolean("Properties.RegionProtection.RespectPreciousStones");
		final boolean respectFactions = ConfigManager.defaultConfig.get().getBoolean("Properties.RegionProtection.RespectFactions");
		final boolean respectTowny = ConfigManager.defaultConfig.get().getBoolean("Properties.RegionProtection.RespectTowny");
		final boolean respectGriefPrevention = ConfigManager.defaultConfig.get().getBoolean("Properties.RegionProtection.RespectGriefPrevention");
		final boolean respectLWC = ConfigManager.defaultConfig.get().getBoolean("Properties.RegionProtection.RespectLWC");
		final PluginManager pm = Bukkit.getPluginManager();

		final Plugin wgp = pm.getPlugin("WorldGuard");
		final Plugin psp = pm.getPlugin("PreciousStones");
		final Plugin fcp = pm.getPlugin("Factions");
		final Plugin twnp = pm.getPlugin("Towny");
		final Plugin gpp = pm.getPlugin("GriefPrevention");
		final Plugin massivecore = pm.getPlugin("MassiveCore");
		final Plugin lwc = pm.getPlugin("LWC");

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

		final String[] pkPlugins = new String[] { "projectkorrarpg", "projectkorraitems", "projectkorraspirits", "probending" };
		for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
			if (plugin.getDescription().getDepend() != null && plugin.getDescription().getDepend().contains("ProjectKorra") && !Arrays.asList(pkPlugins).contains(plugin.getName().toLowerCase())) {
				writeToDebug(plugin.getDescription().getName() + " v" + plugin.getDescription().getVersion());
			}
		}

		writeToDebug("");
		writeToDebug("Ability Information");
		writeToDebug("====================");
		final ArrayList<String> stockAbils = new ArrayList<String>();
		final ArrayList<String> unofficialAbils = new ArrayList<String>();
		for (final CoreAbility ability : CoreAbility.getAbilities()) {
			if (ability.getClass().getPackage().getName().startsWith("com.projectkorra")) {
				stockAbils.add(ability.getName());
			} else {
				unofficialAbils.add(ability.getName());
			}
		}
		if (!stockAbils.isEmpty()) {
			Collections.sort(stockAbils);
			for (final String ability : stockAbils) {
				writeToDebug(ability + " - STOCK");
			}
		}
		if (!unofficialAbils.isEmpty()) {
			Collections.sort(unofficialAbils);
			for (final String ability : unofficialAbils) {
				writeToDebug(ability + " - UNOFFICAL");
			}
		}

		writeToDebug("");
		writeToDebug("Collection Sizes");
		writeToDebug("====================");
		final ClassLoader loader = ProjectKorra.class.getClassLoader();
		try {
			for (final ClassPath.ClassInfo info : ClassPath.from(loader).getTopLevelClasses()) {
				if (info.getName().startsWith("com.projectkorra.")) {
					final Class<?> clazz = info.load();
					for (final Field field : clazz.getDeclaredFields()) {
						final String simpleName = clazz.getSimpleName();
						field.setAccessible(true);
						try {
							final Object obj = field.get(null);
							if (obj instanceof Collection) {
								writeToDebug(simpleName + ": " + field.getName() + " size=" + ((Collection<?>) obj).size());
							} else if (obj instanceof Map) {
								writeToDebug(simpleName + ": " + field.getName() + " size=" + ((Map<?, ?>) obj).size());
							}
						}
						catch (final Exception e) {

						}
					}
				}
			}
		}
		catch (final IOException e) {
			e.printStackTrace();
		}

		writeToDebug("");
		writeToDebug("CoreAbility Debugger");
		writeToDebug("====================");
		for (final String line : CoreAbility.getDebugString().split("\\n")) {
			writeToDebug(line);
		}

	}

	public static void saveAbility(final BendingPlayer bPlayer, final int slot, final String ability) {
		if (bPlayer == null) {
			return;
		}
		final String uuid = bPlayer.getUUIDString();

		final BindChangeEvent event = new BindChangeEvent(Bukkit.getPlayer(UUID.fromString(uuid)), ability, slot, false);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return;
		}
		// Temp code to block modifications of binds, Should be replaced when bind event is added.
		if (MultiAbilityManager.playerAbilities.containsKey(Bukkit.getPlayer(bPlayer.getUUID()))) {
			return;
		}
		final HashMap<Integer, String> abilities = bPlayer.getAbilities();

		DBConnection.sql.modifyQuery("UPDATE pk_players SET slot" + slot + " = '" + (abilities.get(slot) == null ? null : abilities.get(slot)) + "' WHERE uuid = '" + uuid + "'");
	}

	public static void saveElements(final BendingPlayer bPlayer) {
		if (bPlayer == null) {
			return;
		}
		final String uuid = bPlayer.getUUIDString();

		final StringBuilder elements = new StringBuilder();
		if (bPlayer.hasElement(Element.AIR)) {
			elements.append("a");
		}
		if (bPlayer.hasElement(Element.WATER)) {
			elements.append("w");
		}
		if (bPlayer.hasElement(Element.EARTH)) {
			elements.append("e");
		}
		if (bPlayer.hasElement(Element.FIRE)) {
			elements.append("f");
		}
		if (bPlayer.hasElement(Element.CHI)) {
			elements.append("c");
		}
		boolean hasAddon = false;
		for (final Element element : bPlayer.getElements()) {
			if (Arrays.asList(Element.getAddonElements()).contains(element)) {
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

	public static void saveSubElements(final BendingPlayer bPlayer) {
		if (bPlayer == null) {
			return;
		}
		final String uuid = bPlayer.getUUIDString();

		final StringBuilder subs = new StringBuilder();
		if (bPlayer.hasSubElement(Element.METAL)) {
			subs.append("m");
		}
		if (bPlayer.hasSubElement(Element.LAVA)) {
			subs.append("v");
		}
		if (bPlayer.hasSubElement(Element.SAND)) {
			subs.append("s");
		}
		if (bPlayer.hasSubElement(Element.COMBUSTION)) {
			subs.append("c");
		}
		if (bPlayer.hasSubElement(Element.LIGHTNING)) {
			subs.append("l");
		}
		if (bPlayer.hasSubElement(Element.SPIRITUAL)) {
			subs.append("t");
		}
		if (bPlayer.hasSubElement(Element.FLIGHT)) {
			subs.append("f");
		}
		if (bPlayer.hasSubElement(Element.ICE)) {
			subs.append("i");
		}
		if (bPlayer.hasSubElement(Element.HEALING)) {
			subs.append("h");
		}
		if (bPlayer.hasSubElement(Element.BLOOD)) {
			subs.append("b");
		}
		if (bPlayer.hasSubElement(Element.PLANT)) {
			subs.append("p");
		}
		boolean hasAddon = false;
		for (final Element element : bPlayer.getSubElements()) {
			if (Arrays.asList(Element.getAddonSubElements()).contains(element)) {
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

	public static void savePermaRemoved(final BendingPlayer bPlayer) {
		if (bPlayer == null) {
			return;
		}
		final String uuid = bPlayer.getUUIDString();
		final boolean permaRemoved = bPlayer.isPermaRemoved();
		DBConnection.sql.modifyQuery("UPDATE pk_players SET permaremoved = '" + (permaRemoved ? "true" : "false") + "' WHERE uuid = '" + uuid + "'");
	}

	public static void setVelocity(final Entity entity, final Vector velocity) {
		if (entity instanceof TNTPrimed) {
			if (ConfigManager.defaultConfig.get().getBoolean("Properties.BendingAffectFallingSand.TNT")) {
				entity.setVelocity(velocity.multiply(ConfigManager.defaultConfig.get().getDouble("Properties.BendingAffectFallingSand.TNTStrengthMultiplier")));
			}
			return;
		}
		if (entity instanceof FallingBlock) {
			if (ConfigManager.defaultConfig.get().getBoolean("Properties.BendingAffectFallingSand.Normal")) {
				entity.setVelocity(velocity.multiply(ConfigManager.defaultConfig.get().getDouble("Properties.BendingAffectFallingSand.NormalStrengthMultiplier")));
			}
			return;
		}

		// Attempt to stop velocity from going over the packet cap.
		if (velocity.getX() > 4) {
			velocity.setX(4);
		} else if (velocity.getX() < -4) {
			velocity.setX(-4);
		}

		if (velocity.getY() > 4) {
			velocity.setY(4);
		} else if (velocity.getY() < -4) {
			velocity.setY(-4);
		}

		if (velocity.getZ() > 4) {
			velocity.setZ(4);
		} else if (velocity.getZ() < -4) {
			velocity.setZ(-4);
		}

		entity.setVelocity(velocity);
	}

	public static FallingBlock spawnFallingBlock(final Location loc, final int type) {
		return spawnFallingBlock(loc, type, (byte) 0);
	}

	public static FallingBlock spawnFallingBlock(final Location loc, final int type, final byte data) {
		return loc.getWorld().spawnFallingBlock(loc, type, data);
	}

	public static FallingBlock spawnFallingBlock(final Location loc, final Material type) {
		return spawnFallingBlock(loc, type, (byte) 0);
	}

	public static FallingBlock spawnFallingBlock(final Location loc, final Material type, final byte data) {
		return loc.getWorld().spawnFallingBlock(loc, type, data);
	}

	public static void sendBrandingMessage(final CommandSender sender, final String message) {
		ChatColor color;
		try {
			color = ChatColor.valueOf(ConfigManager.languageConfig.get().getString("Chat.Branding.Color").toUpperCase());
		}
		catch (final IllegalArgumentException exception) {
			color = ChatColor.GOLD;
		}

		final String prefix = ChatColor.translateAlternateColorCodes('&', ConfigManager.languageConfig.get().getString("Chat.Branding.ChatPrefix.Prefix")) + color + "ProjectKorra" + ChatColor.translateAlternateColorCodes('&', ConfigManager.languageConfig.get().getString("Chat.Branding.ChatPrefix.Suffix"));
		if (!(sender instanceof Player)) {
			sender.sendMessage(prefix + message);
		} else {
			final TextComponent prefixComponent = new TextComponent(prefix);
			prefixComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://projectkorra.com/"));
			prefixComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(color + "Bending brought to you by ProjectKorra!\n" + color + "Click for more info.").create()));

			/*
			 * The commented code below does not work due to an issue with
			 * Spigot. In the mean time, we'll have to use this incredibly
			 * 'hacky' method to force the color on the new line.
			 */
			String lastColor = "";
			String newMessage = "";
			for (int i = 0; i < message.split("").length; i++) {
				final String c = message.split("")[i];
				if (c.equalsIgnoreCase("§")) {
					lastColor = "§" + message.split("")[i + 1];
					newMessage = newMessage + c;
				} else if (c.equalsIgnoreCase(" ")) { // Add color every word
					newMessage = newMessage + " " + lastColor;
				} else {
					newMessage = newMessage + c;
				}
			}

			final TextComponent messageComponent = new TextComponent(newMessage);
			((Player) sender).spigot().sendMessage(new TextComponent(prefixComponent, messageComponent));
			/*
			 * boolean prefixSent = false; for (String msg :
			 * message.split("\n")) { if (!prefixSent) { TextComponent
			 * messageComponent = new TextComponent(msg); ((Player)
			 * sender).spigot().sendMessage(new TextComponent(prefixComponent,
			 * messageComponent)); prefixSent = true; } else {
			 * sender.sendMessage(msg); } }
			 */

		}
	}

	public static void startCacheCleaner(final double period) {
		new BukkitRunnable() {
			@Override
			public void run() {
				for (final Map<Block, BlockCacheElement> map : BLOCK_CACHE.values()) {
					for (final Block key : map.keySet()) {
						final BlockCacheElement value = map.get(key);

						if (System.currentTimeMillis() - value.getTime() > period) {
							map.remove(key);
						}
					}
				}
			}
		}.runTaskTimer(ProjectKorra.plugin, 0, (long) (period / 20));
	}

	public static void stopBending() {
		for (final CoreAbility ability : CoreAbility.getAbilities()) {
			if (ability instanceof AddonAbility) {
				((AddonAbility) ability).stop();
			}
		}

		CoreAbility.removeAll();
		EarthAbility.stopBending();
		WaterAbility.stopBending();
		FireAbility.stopBending();

		TempBlock.removeAll();
		TempArmor.revertAll();
		TempArmorStand.removeAll();
		MovementHandler.resetAll();
		MultiAbilityManager.removeAll();
		if (!INVINCIBLE.isEmpty()) {
			INVINCIBLE.clear();
		}
	}

	public static void stopPlugin() {
		plugin.getServer().getPluginManager().disablePlugin(plugin);
	}

	public static void writeToDebug(final String message) {
		try {
			final File dataFolder = plugin.getDataFolder();
			if (!dataFolder.exists()) {
				dataFolder.mkdir();
			}

			final File saveTo = new File(plugin.getDataFolder(), "debug.txt");
			if (!saveTo.exists()) {
				saveTo.createNewFile();
			}

			final FileWriter fw = new FileWriter(saveTo, true);
			final PrintWriter pw = new PrintWriter(fw);
			pw.println(message);
			pw.flush();
			pw.close();

		}
		catch (final IOException e) {
			e.printStackTrace();
		}
	}
}

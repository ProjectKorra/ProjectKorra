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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.api.ResidenceInterface;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import com.google.common.reflect.ClassPath;
import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.Protection;
import com.palmergames.bukkit.towny.object.TownyPermission.ActionType;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.kingdoms.constants.kingdom.Kingdom;
import org.kingdoms.constants.kingdom.model.KingdomRelation;
import org.kingdoms.constants.land.Land;
import org.kingdoms.constants.land.structures.managers.Regulator;
import org.kingdoms.constants.land.structures.managers.Regulator.Attribute;
import org.kingdoms.constants.player.DefaultKingdomPermission;
import org.kingdoms.constants.player.KingdomPlayer;

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
import com.projectkorra.projectkorra.board.BendingBoardManager;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.EarthBlast;
import com.projectkorra.projectkorra.earthbending.passive.EarthPassive;
import com.projectkorra.projectkorra.event.AbilityVelocityAffectEntityEvent;
import com.projectkorra.projectkorra.event.BendingPlayerCreationEvent;
import com.projectkorra.projectkorra.event.BendingReloadEvent;
import com.projectkorra.projectkorra.event.PlayerBindChangeEvent;
import com.projectkorra.projectkorra.firebending.FireBlast;
import com.projectkorra.projectkorra.firebending.FireShield;
import com.projectkorra.projectkorra.firebending.combustion.Combustion;
import com.projectkorra.projectkorra.object.Preset;
import com.projectkorra.projectkorra.storage.DBConnection;
import com.projectkorra.projectkorra.util.ActionBar;
import com.projectkorra.projectkorra.util.BlockCacheElement;
import com.projectkorra.projectkorra.util.ColoredParticle;
import com.projectkorra.projectkorra.util.MovementHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.ReflectionHandler;
import com.projectkorra.projectkorra.util.ReflectionHandler.PackageType;
import com.projectkorra.projectkorra.util.TempArmor;
import com.projectkorra.projectkorra.util.TempArmorStand;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.WaterManipulation;
import com.projectkorra.projectkorra.waterbending.WaterSpout;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.API.RedProtectAPI;
import me.markeh.factionsframework.entities.FPlayer;
import me.markeh.factionsframework.entities.FPlayers;
import me.markeh.factionsframework.entities.Faction;
import me.markeh.factionsframework.entities.Factions;
import me.markeh.factionsframework.enums.Rel;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class GeneralMethods {

	// Represents PlayerName, previously checked blocks, and whether they were true or false
	private static final Map<String, Map<Block, BlockCacheElement>> BLOCK_CACHE = new ConcurrentHashMap<>();
	private static final ArrayList<Ability> INVINCIBLE = new ArrayList<>();
	private static ProjectKorra plugin;

	private static Method getAbsorption;
	private static Method setAbsorption;
	private static Method getHandle;

	public GeneralMethods(final ProjectKorra plugin) {
		GeneralMethods.plugin = plugin;

		try {
			getAbsorption = ReflectionHandler.getMethod("EntityHuman", PackageType.MINECRAFT_SERVER, "getAbsorptionHearts");
			setAbsorption = ReflectionHandler.getMethod("EntityHuman", PackageType.MINECRAFT_SERVER, "setAbsorptionHearts", Float.class);
			getHandle = ReflectionHandler.getMethod("CraftPlayer", PackageType.CRAFTBUKKIT_ENTITY, "getHandle");
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks to see if an AbilityExists. Uses method
	 * {@link CoreAbility#getAbility(String)} to check if it exists.
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

	public static int compareArmor(Material first, Material second) {
		return getArmorTier(first) - getArmorTier(second);
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
				new BukkitRunnable() {
					@Override
					public void run() {
						new BendingPlayer(uuid, player, new ArrayList<Element>(), new ArrayList<SubElement>(), new HashMap<Integer, String>(), false);
						ProjectKorra.log.info("Created new BendingPlayer for " + player);
					}
				}.runTask(ProjectKorra.plugin);
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
						if (split[0].contains("r")) {
							subelements.add(Element.BLUE_FIRE);
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
		} catch (final SQLException ex) {
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
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Deprecated
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
		loc.setX(loc.getX() + (Math.random() * 2 - 1) * xOffset);
		loc.setY(loc.getY() + (Math.random() * 2 - 1) * yOffset);
		loc.setZ(loc.getZ() + (Math.random() * 2 - 1) * zOffset);

		if (type != ParticleEffect.RED_DUST && type != ParticleEffect.REDSTONE && type != ParticleEffect.SPELL_MOB && type != ParticleEffect.MOB_SPELL && type != ParticleEffect.SPELL_MOB_AMBIENT && type != ParticleEffect.MOB_SPELL_AMBIENT) {
			type = ParticleEffect.RED_DUST;
		}
		type.display(loc, 0, red, green, blue);
	}

	@Deprecated
	public static void displayColoredParticle(final Location loc, final String hexVal) {
		displayColoredParticle(loc, ParticleEffect.RED_DUST, hexVal, 0, 0, 0);
	}

	@Deprecated
	public static void displayColoredParticle(final Location loc, final String hexVal, final float xOffset, final float yOffset, final float zOffset) {
		displayColoredParticle(loc, ParticleEffect.RED_DUST, hexVal, xOffset, yOffset, zOffset);
	}

	public static void displayColoredParticle(String hexVal, final Location loc, final int amount, final double offsetX, final double offsetY, final double offsetZ) {
		int r = 0;
		int g = 0;
		int b = 0;

		if (hexVal.startsWith("#")) {
			hexVal = hexVal.substring(1);
		}

		if (hexVal.length() <= 6) {
			r = Integer.valueOf(hexVal.substring(0, 2), 16).intValue();
			g = Integer.valueOf(hexVal.substring(2, 4), 16).intValue();
			b = Integer.valueOf(hexVal.substring(4, 6), 16).intValue();
		}

		new ColoredParticle(Color.fromRGB(r, g, b), 1F).display(loc, amount, offsetX, offsetY, offsetZ);
	}

	public static void displayColoredParticle(final String hexVal, final Location loc) {
		displayColoredParticle(hexVal, loc, 1, 0, 0, 0);
	}

	/**
	 * Drops a {@code Collection<ItemStack>} of items on a specified block.
	 *
	 * @param block The block to drop items on.
	 * @param items The items to drop.
	 */
	public static void dropItems(final Block block, final Collection<ItemStack> items) {
		for (final ItemStack item : items) {
			if(item.getType() != Material.AIR) {
				block.getWorld().dropItem(block.getLocation(), item);
			}
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
			displayedMessage = ability.getMovePreview(player);
		} else if (displayedMessage == null || displayedMessage.isEmpty() || displayedMessage.equals("")) {
			displayedMessage = "";
		}

		ActionBar.sendActionBar(displayedMessage, player);
	}

	public static float getAbsorbationHealth(final Player player) {

		try {
			final Object entityplayer = getHandle.invoke(player);
			final Object hearts = getAbsorption.invoke(entityplayer);
			return (float) hearts;
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static void setAbsorbationHealth(final Player player, final float hearts) {

		try {
			final Object entityplayer = getHandle.invoke(player);
			setAbsorption.invoke(entityplayer, hearts);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public static int getArmorTier(Material mat) {
		switch (mat) {
			case NETHERITE_HELMET:
			case NETHERITE_CHESTPLATE:
			case NETHERITE_LEGGINGS:
			case NETHERITE_BOOTS:
				return 7;
			case DIAMOND_HELMET:
			case DIAMOND_CHESTPLATE:
			case DIAMOND_LEGGINGS:
			case DIAMOND_BOOTS:
				return 6;
			case TURTLE_HELMET:
				return 5;
			case IRON_HELMET:
			case IRON_CHESTPLATE:
			case IRON_LEGGINGS:
			case IRON_BOOTS:
				return 4;
			case CHAINMAIL_HELMET:
			case CHAINMAIL_CHESTPLATE:
			case CHAINMAIL_LEGGINGS:
			case CHAINMAIL_BOOTS:
				return 3;
			case GOLDEN_HELMET:
			case GOLDEN_CHESTPLATE:
			case GOLDEN_LEGGINGS:
			case GOLDEN_BOOTS:
				return 2;
			case LEATHER_HELMET:
			case LEATHER_CHESTPLATE:
			case LEATHER_LEGGINGS:
			case LEATHER_BOOTS:
				return 1;
			default:
				return 0;
		}
	}

	public static int getArmorIndex(Material mat) {
		switch (mat) {
			case NETHERITE_HELMET:
			case DIAMOND_HELMET:
			case TURTLE_HELMET:
			case IRON_HELMET:
			case CHAINMAIL_HELMET:
			case GOLDEN_HELMET:
			case LEATHER_HELMET:
				return 3;
			case NETHERITE_CHESTPLATE:
			case DIAMOND_CHESTPLATE:
			case IRON_CHESTPLATE:
			case CHAINMAIL_CHESTPLATE:
			case GOLDEN_CHESTPLATE:
			case LEATHER_CHESTPLATE:
				return 2;
			case NETHERITE_LEGGINGS:
			case DIAMOND_LEGGINGS:
			case IRON_LEGGINGS:
			case CHAINMAIL_LEGGINGS:
			case GOLDEN_LEGGINGS:
			case LEATHER_LEGGINGS:
				return 1;
			case NETHERITE_BOOTS:
			case DIAMOND_BOOTS:
			case IRON_BOOTS:
			case CHAINMAIL_BOOTS:
			case GOLDEN_BOOTS:
			case LEATHER_BOOTS:
				return 0;
			default:
				return -1;
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

	/**
	 * Gets the closest entity within the specified radius around a point
	 * @param center point to check around
	 * @param radius distance from center to check within
	 * @return null if not found
	 */
	public static Entity getClosestEntity(Location center, double radius) {
		Entity found = null;
		Double distance = null;

		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(center, radius)) {
			double check = center.distanceSquared(entity.getLocation());

			if (distance == null || check < distance) {
				found = entity;
				distance = check;
			}
		}

		return found;
	}

	/**
	 * Gets the closest LivingEntity within the specified radius around a point
	 * @param center point to check around
	 * @param radius distance from center to check within
	 * @return null if not found
	 */
	public static LivingEntity getClosestLivingEntity(Location center, double radius) {
		LivingEntity le = null;
		Double distance = null;

		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(center, radius)) {
			double check = center.distanceSquared(entity.getLocation());

			if (entity instanceof LivingEntity && (distance == null || check < distance)) {
				le = (LivingEntity) entity;
				distance = check;
			}
		}

		return le;
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
	 * @return The item drops fromt the specified block
	 */
	public static Collection<ItemStack> getDrops(final Block block, final Material type, final BlockData data) {
		final BlockState tempstate = block.getState();
		block.setType(type);
		block.setBlockData(data);
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
		return new ArrayList<>(location.getWorld().getNearbyEntities(location, radius, radius, radius, entity -> !(entity.isDead() || (entity instanceof Player && ((Player) entity).getGameMode().equals(GameMode.SPECTATOR)))));
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
		final int max = ConfigManager.getConfig().getInt("Properties.MaxPresets");
		if (player.isOp()) {
			return max;
		}
		for (int i = max; i > 0; i--) {
			if (player.hasPermission("bending.command.preset.create." + i)) {
				return i;
			}
		}
		return 0;
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

	public static Location getMainHandLocation(final Player player) {
		Location loc;
		if (player.getMainHand() == MainHand.LEFT) {
			loc = GeneralMethods.getLeftSide(player.getLocation(), .55).add(0, 1.2, 0);
		} else {
			loc = GeneralMethods.getRightSide(player.getLocation(), .55).add(0, 1.2, 0);
		}
		return loc;
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

	public static BlockData getLavaData(final int level) {
		return Material.LAVA.createBlockData(d -> ((Levelled) d).setLevel((level < 0 || level > ((Levelled) d).getMaximumLevel()) ? 0 : level));
	}

	public static BlockData getWaterData(final int level) {
		return Material.WATER.createBlockData(d -> ((Levelled) d).setLevel((level < 0 || level > ((Levelled) d).getMaximumLevel()) ? 0 : level));
	}

	public static Entity getTargetedEntity(final Player player, final double range, final List<Entity> avoid) {
		double longestr = range + 1;
		Entity target = null;
		final Location origin = player.getEyeLocation();
		final Vector direction = player.getEyeLocation().getDirection().normalize();
		for (final Entity entity : getEntitiesAroundPoint(origin, range)) {
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

	public static Location getTargetedLocation(final Player player, final double range, final boolean ignoreTempBlocks, final boolean checkDiagonals, final Material... nonOpaque2) {
		final Location origin = player.getEyeLocation();
		final Vector direction = origin.getDirection();

		final HashSet<Material> trans = new HashSet<Material>();
		trans.add(Material.AIR);
		trans.add(Material.CAVE_AIR);
		trans.add(Material.VOID_AIR);

		if (nonOpaque2 != null) {
			for (final Material material : nonOpaque2) {
				trans.add(material);
			}
		}

		final Location location = origin.clone();
		final Vector vec = direction.normalize().multiply(0.2);

		for (double i = 0; i < range; i += 0.2) {
			location.add(vec);

			if (checkDiagonals && checkDiagonalWall(location, vec)) {
				location.subtract(vec);
				break;
			}

			final Block block = location.getBlock();

			if (trans.contains(block.getType())) {
				continue;
			} else if (ignoreTempBlocks && (TempBlock.isTempBlock(block) && !WaterAbility.isBendableWaterTempBlock(block) && !EarthAbility.isBendableEarthTempBlock(block))) {
				continue;
			} else {
				location.subtract(vec);
				break;
			}
		}

		return location;
	}

	public static Location getTargetedLocation(final Player player, final double range, final boolean ignoreTempBlocks, final Material... nonOpaque2) {
		return getTargetedLocation(player, range, ignoreTempBlocks, true, nonOpaque2);
	}

	public static Location getTargetedLocation(final Player player, final double range, final Material... nonOpaque2) {
		return getTargetedLocation(player, range, false, nonOpaque2);
	}

	public static Location getTargetedLocation(final Player player, final int range) {
		return getTargetedLocation(player, range, false);
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
		while (!ElementalAbility.isAir(blockHolder.getType()) && Math.abs(y) < Math.abs(positiveY)) {
			y++;
			final Block tempBlock = loc.clone().add(0, y, 0).getBlock();
			if (ElementalAbility.isAir(tempBlock.getType())) {
				return blockHolder;
			}
			blockHolder = tempBlock;
		}

		while (ElementalAbility.isAir(blockHolder.getType()) && Math.abs(y) < Math.abs(negativeY)) {
			y--;
			blockHolder = loc.clone().add(0, y, 0).getBlock();
			if (!ElementalAbility.isAir(blockHolder.getType())) {
				return blockHolder;
			}
		}
		return blockHolder;
	}

	public static Block getBottomBlock(final Location loc, final int positiveY, final int negativeY) {
		Block blockHolder = loc.getBlock();
		int y = 0;
		// Only one of these while statements will go
		while (!ElementalAbility.isAir(blockHolder.getType()) && Math.abs(y) < Math.abs(negativeY)) {
			y--;
			final Block tempblock = loc.clone().add(0, y, 0).getBlock();
			if (ElementalAbility.isAir(tempblock.getType())) {
				return blockHolder;
			}

			blockHolder = tempblock;
		}

		while (!ElementalAbility.isAir(blockHolder.getType()) && Math.abs(y) < Math.abs(positiveY)) {
			y++;
			blockHolder = loc.clone().add(0, y, 0).getBlock();
			if (ElementalAbility.isAir(blockHolder.getType())) {
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

	public static boolean isArmor(Material mat) {
		switch (mat) {
			case NETHERITE_HELMET:
			case NETHERITE_CHESTPLATE:
			case NETHERITE_LEGGINGS:
			case NETHERITE_BOOTS:
			case DIAMOND_HELMET:
			case DIAMOND_CHESTPLATE:
			case DIAMOND_LEGGINGS:
			case DIAMOND_BOOTS:
			case TURTLE_HELMET:
			case IRON_HELMET:
			case IRON_CHESTPLATE:
			case IRON_LEGGINGS:
			case IRON_BOOTS:
			case CHAINMAIL_HELMET:
			case CHAINMAIL_CHESTPLATE:
			case CHAINMAIL_LEGGINGS:
			case CHAINMAIL_BOOTS:
			case GOLDEN_HELMET:
			case GOLDEN_CHESTPLATE:
			case GOLDEN_LEGGINGS:
			case GOLDEN_BOOTS:
			case LEATHER_HELMET:
			case LEATHER_CHESTPLATE:
			case LEATHER_LEGGINGS:
			case LEATHER_BOOTS:
				return true;
			default:
				return false;
		}
	}

	public static boolean isAdjacentToThreeOrMoreSources(final Block block) {
		return isAdjacentToThreeOrMoreSources(block, false);
	}

	public static boolean isAdjacentToThreeOrMoreSources(final Block block, final boolean lava) {
		if (block == null || (TempBlock.isTempBlock(block) && (!lava && !WaterAbility.isBendableWaterTempBlock(block)))) {
			return false;
		}
		int sources = 0;
		final BlockFace[] faces = { BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH };
		for (final BlockFace face : faces) {
			final Block blocki = block.getRelative(face);
			if (lava) {
				if (!(blocki.getType() == Material.LAVA && EarthPassive.canPhysicsChange(blocki))) {
					continue;
				}
			} else {
				if (!((ElementalAbility.isWater(blocki) || ElementalAbility.isIce(blocki)) && WaterManipulation.canPhysicsChange(blocki))) {
					continue;
				}
			}

			//At this point it should either be water or lava
			if (blocki.getBlockData() instanceof Levelled) {
				final Levelled level = (Levelled) blocki.getBlockData();
				if (level.getLevel() == 0) {
					sources++;
				}
			} else { //ice
				sources++;
			}
		}
		return sources >= 2;
	}

	public static boolean isImportEnabled() {
		return ConfigManager.defaultConfig.get().getBoolean("Properties.ImportEnabled");
	}

	public static boolean isInteractable(final Block block) {
		return isInteractable(block.getType());
	}

	public static boolean isInteractable(final Material material) {
		return material.isInteractable();
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
		//final boolean respectPreciousStones = ConfigManager.defaultConfig.get().getBoolean("Properties.RegionProtection.RespectPreciousStones");
		final boolean respectFactions = ConfigManager.defaultConfig.get().getBoolean("Properties.RegionProtection.RespectFactions");
		final boolean respectTowny = ConfigManager.defaultConfig.get().getBoolean("Properties.RegionProtection.RespectTowny");
		final boolean respectGriefPrevention = ConfigManager.defaultConfig.get().getBoolean("Properties.RegionProtection.RespectGriefPrevention");
		final boolean respectLWC = ConfigManager.defaultConfig.get().getBoolean("Properties.RegionProtection.RespectLWC");
		final boolean respectResidence = ConfigManager.defaultConfig.get().getBoolean("Properties.RegionProtection.Residence.Respect");
		final boolean respectKingdoms = ConfigManager.defaultConfig.get().getBoolean("Properties.RegionProtection.Kingdoms.Respect");
		final boolean respectRedProtect = ConfigManager.defaultConfig.get().getBoolean("Properties.RegionProtection.RespectRedProtect");

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
		//final Plugin psp = pm.getPlugin("PreciousStones");
		final Plugin facsfw = pm.getPlugin("FactionsFramework");
		final Plugin twnp = pm.getPlugin("Towny");
		final Plugin gpp = pm.getPlugin("GriefPrevention");
		final Plugin lwc = pm.getPlugin("LWC");
		final Plugin residence = pm.getPlugin("Residence");
		final Plugin kingdoms = pm.getPlugin("Kingdoms");
		final Plugin redprotect = pm.getPlugin("RedProtect");

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
				final WorldGuard wg = WorldGuard.getInstance();
				if (!player.isOnline()) {
					return true;
				}
				if (isIgnite) {
					if (!player.hasPermission("worldguard.override.lighter")) {
						if (wg.getPlatform().getGlobalStateManager().get(BukkitAdapter.adapt(world)).blockLighter) {
							return true;
						}
					}
				}

				if (isExplosive) {
					if (wg.getPlatform().getGlobalStateManager().get(BukkitAdapter.adapt(location.getWorld())).blockTNTExplosions) {
						return true;
					}
					final StateFlag.State tntflag = wg.getPlatform().getRegionContainer().createQuery().queryState(BukkitAdapter.adapt(location), WorldGuardPlugin.inst().wrapPlayer(player), Flags.TNT);
					if (tntflag != null && tntflag.equals(StateFlag.State.DENY)) {
						return true;
					}
				}
				final StateFlag bendingflag = (StateFlag) WorldGuard.getInstance().getFlagRegistry().get("bending");
				if (bendingflag != null) {
					final StateFlag.State bendingflagstate = wg.getPlatform().getRegionContainer().createQuery().queryState(BukkitAdapter.adapt(location), WorldGuardPlugin.inst().wrapPlayer(player), bendingflag);
					if (bendingflagstate == null && !wg.getPlatform().getRegionContainer().createQuery().testState(BukkitAdapter.adapt(location), WorldGuardPlugin.inst().wrapPlayer(player), Flags.BUILD)) {
						return true;
					}
					if (bendingflagstate != null && bendingflagstate.equals(StateFlag.State.DENY)) {
						return true;
					}
				} else {
					if (!wg.getPlatform().getRegionContainer().createQuery().testState(BukkitAdapter.adapt(location), WorldGuardPlugin.inst().wrapPlayer(player), Flags.BUILD)) {
						return true;
					}
				}
			}

			if (facsfw != null && respectFactions) {
				final FPlayer fPlayer = FPlayers.getBySender(player);
				final Faction faction = Factions.getFactionAt(location);
				final Rel relation = fPlayer.getRelationTo(faction);

				if (!(faction.isNone() || fPlayer.getFaction().equals(faction) || relation == Rel.ALLY)) {
					return true;
				}
			}

			if (twnp != null && respectTowny) {
				if (!PlayerCacheUtil.getCachePermission(player, location, Material.DIRT, ActionType.BUILD)) {
					return true;
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
				final ResidenceInterface res = Residence.getInstance().getResidenceManagerAPI();
				final ClaimedResidence claim = res.getByLoc(location);
				if (claim != null) {
					final ResidencePermissions perms = claim.getPermissions();
					if (!perms.hasApplicableFlag(player.getName(), ConfigManager.getConfig().getString("Properties.RegionProtection.Residence.Flag"))) {
						return true;
					}
				}
			}

			if (kingdoms != null && respectKingdoms) {
				final KingdomPlayer kPlayer = KingdomPlayer.getKingdomPlayer(player);
				final Land land = Land.getLand(location);
				final boolean protectDuringInvasions = ConfigManager.getConfig().getBoolean("Properties.RegionProtection.Kingdoms.ProtectDuringInvasions");
				if (land != null) {
					final Kingdom kingdom = land.getKingdom();
					if (kPlayer.isAdmin()
							|| (!protectDuringInvasions && !land.getInvasions().isEmpty() && land.getInvasions().values().stream().anyMatch(i -> i.getInvader().equals(kPlayer))) // Protection during invasions is off, and player is currently invading; allow
							|| (land.getStructure() != null && land.getStructure() instanceof Regulator && ((Regulator) land.getStructure()).hasAttribute(player, Attribute.BUILD))) { // There is a regulator on site which allows the player to build; allow
						return false;
					}
					if (!kPlayer.hasKingdom() // Player has no kingdom; deny
							|| (kPlayer.getKingdom().equals(kingdom) && !kPlayer.hasPermission(DefaultKingdomPermission.BUILD)) // Player is a member of this kingdom but cannot build here; deny
							|| (!kPlayer.getKingdom().equals(kingdom) && !kPlayer.getKingdom().hasAttribute(kingdom, KingdomRelation.Attribute.BUILD))) { // Player is not a member of this kingdom and cannot build here; deny
						return true;
					}
				}
			}

			if (redprotect != null && respectRedProtect) {
				final RedProtectAPI api = RedProtect.get().getAPI();
				final Region region = api.getRegion(location);
				if (!(region != null && region.canBuild(player))) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isSameArmor(Material a, Material b) {
		int ai = getArmorIndex(a), bi = getArmorIndex(b);

		if (ai == -1 || bi == -1) {
			return false;
		}

		return ai == bi;
	}

	public static boolean isSolid(final Block block) {
		return isSolid(block.getType());
	}

	public static boolean isSolid(final Material material) {
		return material.isSolid();
	}

	public static boolean isTransparent(final Block block) {
		return isTransparent(block.getType());
	}

	public static boolean isTransparent(final Material material) {
		return !material.isOccluding() && !material.isSolid();
	}

	/** Checks if an entity is Undead **/
	public static boolean isUndead(final Entity entity) {
		if (entity == null) {
			return false;
		}

		switch (entity.getType()) {
			case SKELETON:
			case STRAY:
			case WITHER_SKELETON:
			case WITHER:
			case ZOMBIE:
			case HUSK:
			case ZOMBIE_VILLAGER:
			case ZOMBIFIED_PIGLIN:
			case ZOGLIN:
			case DROWNED:
			case ZOMBIE_HORSE:
			case SKELETON_HORSE:
			case PHANTOM:
				return true;
			default:
				return false;
		}
	}

	public static boolean isWeapon(final Material mat) {
	
		switch(mat) {
			case BOW:
			case CROSSBOW:
			case DIAMOND_AXE:
			case DIAMOND_HOE:
			case DIAMOND_PICKAXE:
			case DIAMOND_SHOVEL:
			case DIAMOND_SWORD:
			case GOLDEN_AXE:
			case GOLDEN_HOE:
			case GOLDEN_PICKAXE:
			case GOLDEN_SHOVEL:
			case GOLDEN_SWORD:
			case IRON_AXE:
			case IRON_HOE:
			case IRON_PICKAXE:
			case IRON_SHOVEL:
			case IRON_SWORD:
			case NETHERITE_AXE:
			case NETHERITE_HOE:
			case NETHERITE_PICKAXE:
			case NETHERITE_SHOVEL:
			case NETHERITE_SWORD:
			case STONE_AXE:
			case STONE_HOE:
			case STONE_PICKAXE:
			case STONE_SHOVEL:
			case STONE_SWORD:
			case TRIDENT:
			case WOODEN_AXE:
			case WOODEN_HOE:
			case WOODEN_PICKAXE:
			case WOODEN_SHOVEL:
			case WOODEN_SWORD:
				return true;
			default:
				return false;
		}
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

		prefix = ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', ConfigManager.languageConfig.get().getString("Chat.Prefixes.Nonbender")) + " ";
		if (player.hasPermission("bending.avatar") || (bPlayer.hasElement(Element.AIR) && bPlayer.hasElement(Element.EARTH) && bPlayer.hasElement(Element.FIRE) && bPlayer.hasElement(Element.WATER))) {
			prefix = Element.AVATAR.getPrefix();
		} else if (bPlayer.getElements().size() > 0) {
			element = bPlayer.getElements().get(0);
			prefix = element.getPrefix();
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
		BendingBoardManager.reload();
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
		if (isAdjacentToThreeOrMoreSources(block, false)) {
			block.setType(Material.WATER);
			if (block.getBlockData() instanceof Levelled) {
				((Levelled) block.getBlockData()).setLevel(1);
			}
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
		final boolean respectResidence = ConfigManager.defaultConfig.get().getBoolean("Properties.RegionProtection.Residence.Respect");
		final boolean respectKingdoms = ConfigManager.defaultConfig.get().getBoolean("Properties.RegionProtection.Kingdoms.Respect");
		final boolean respectRedProtect = ConfigManager.defaultConfig.get().getBoolean("Properties.RegionProtection.RedProtect");
		final PluginManager pm = Bukkit.getPluginManager();

		final Plugin wgp = pm.getPlugin("WorldGuard");
		final Plugin psp = pm.getPlugin("PreciousStones");
		final Plugin fcp = pm.getPlugin("FactionsFramework");
		final Plugin twnp = pm.getPlugin("Towny");
		final Plugin gpp = pm.getPlugin("GriefPrevention");
		final Plugin lwc = pm.getPlugin("LWC");
		final Plugin residence = pm.getPlugin("Residence");
		final Plugin kingdoms = pm.getPlugin("Kingdoms");
		final Plugin redprotect = pm.getPlugin("RedProtect");

		if (wgp != null && respectWorldGuard) {
			writeToDebug("WorldGuard v" + wgp.getDescription().getVersion());
		}
		if (psp != null && respectPreciousStones) {
			writeToDebug("PreciousStones v" + psp.getDescription().getVersion());
		}
		if (fcp != null && respectFactions) {
			writeToDebug("FactionsFramework v" + fcp.getDescription().getVersion());
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
		if (residence != null && respectResidence) {
			writeToDebug("Residence v" + residence.getDescription().getVersion());
		}
		if (kingdoms != null && respectKingdoms) {
			writeToDebug("Kingdoms v" + kingdoms.getDescription().getVersion());
		}
		if (redprotect != null && respectRedProtect) {
			writeToDebug("RedProtect v" + redprotect.getDescription().getVersion());
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
				if (info.getName().startsWith("com.projectkorra.") && !info.getName().contains("hooks")) {
					try {
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
							} catch (final Exception e) {

							}
						}
					}  catch (Exception e) {
						continue;
					}

				}
			}
		} catch (final IOException e) {
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

		final PlayerBindChangeEvent event = new PlayerBindChangeEvent(Bukkit.getPlayer(UUID.fromString(uuid)), ability, slot, false);
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
		if (bPlayer.hasSubElement(Element.BLUE_FIRE)) {
			subs.append("r");
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

	public static FallingBlock spawnFallingBlock(final Location loc, final Material type) {
		return spawnFallingBlock(loc, type, type.createBlockData());
	}

	public static FallingBlock spawnFallingBlock(final Location loc, final Material type, final BlockData data) {
		return loc.getWorld().spawnFallingBlock(loc, data);
	}

	public static boolean playerHeadIsInBlock(final Player player, final Block block) {
		return playerHeadIsInBlock(player, block, false);
	}

	public static boolean playerHeadIsInBlock(final Player player, final Block block, final boolean exact) {
		double checkDistance;
		if (exact) {
			checkDistance = 0.5;
		} else {
			checkDistance = 0.75;
		}
		return (player.getEyeLocation().getBlockY() == block.getLocation().getBlockY() && (Math.abs(player.getEyeLocation().getX() - block.getLocation().add(0.5, 0.0, 0.5).getX()) < checkDistance) && (Math.abs(player.getEyeLocation().getZ() - block.getLocation().add(0.5, 0.0, 0.5).getZ()) < checkDistance));
	}

	public static boolean playerFeetIsInBlock(final Player player, final Block block) {
		return playerFeetIsInBlock(player, block, false);
	}

	public static boolean playerFeetIsInBlock(final Player player, final Block block, final boolean exact) {
		double checkDistance;
		if (exact) {
			checkDistance = 0.5;
		} else {
			checkDistance = 0.75;
		}
		return (player.getLocation().getBlockY() == block.getLocation().getBlockY() && (Math.abs(player.getLocation().getX() - block.getLocation().add(0.5, 0.0, 0.5).getX()) < checkDistance) && (Math.abs(player.getLocation().getZ() - block.getLocation().add(0.5, 0.0, 0.5).getZ()) < checkDistance));
	}

	public static void sendBrandingMessage(final CommandSender sender, final String message) {
		ChatColor color;
		try {
			color = ChatColor.valueOf(ConfigManager.languageConfig.get().getString("Chat.Branding.Color").toUpperCase());
		} catch (final IllegalArgumentException exception) {
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
			if (saveTo.exists()) {
				saveTo.delete();
			}
			saveTo.createNewFile();

			final FileWriter fw = new FileWriter(saveTo, true);
			final PrintWriter pw = new PrintWriter(fw);
			pw.println(message);
			pw.flush();
			pw.close();

		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean locationEqualsIgnoreDirection(final Location loc1, final Location loc2) {
		return loc1.getWorld().equals(loc2.getWorld()) && loc1.getX() == loc2.getX() && loc1.getY() == loc2.getY() && loc1.getZ() == loc2.getZ();
	}

	public static boolean isLightEmitting(final Material material) {
		switch (material) {
			case GLOWSTONE:
			case TORCH:
			case SEA_LANTERN:
			case BEACON:
			case REDSTONE_LAMP:
			case REDSTONE_TORCH:
			case MAGMA_BLOCK:
			case LAVA:
			case JACK_O_LANTERN:
			case CRYING_OBSIDIAN:
			case SHROOMLIGHT:
			case CAMPFIRE:
			case SOUL_CAMPFIRE:
			case SOUL_TORCH:
			case LANTERN:
			case SOUL_LANTERN:
			case CONDUIT:
			case RESPAWN_ANCHOR:
			case BROWN_MUSHROOM:
			case BREWING_STAND:
			case ENDER_CHEST:
			case END_PORTAL_FRAME:
			case END_ROD:
				return true;
			default:
				return false;
		}
	}

	@Deprecated
	public static void setVelocity(Entity entity, Vector vector) {
		setVelocity(null,entity,vector);
	}
	
	public static void setVelocity(Ability ability, Entity entity, Vector vector) {
		final AbilityVelocityAffectEntityEvent event = new AbilityVelocityAffectEntityEvent(ability, entity, vector);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) 
			return;
		
		Vector velocity = event.getVelocity();
		if(velocity == null || Double.isNaN(velocity.length()))
		    return;
		
		if (entity instanceof TNTPrimed) {
			if (ConfigManager.defaultConfig.get().getBoolean("Properties.BendingAffectFallingSand.TNT")) {
				velocity.multiply(ConfigManager.defaultConfig.get().getDouble("Properties.BendingAffectFallingSand.TNTStrengthMultiplier"));
			}
		} else if (entity instanceof FallingBlock) {
			if (ConfigManager.defaultConfig.get().getBoolean("Properties.BendingAffectFallingSand.Normal")) {
				velocity.multiply(ConfigManager.defaultConfig.get().getDouble("Properties.BendingAffectFallingSand.NormalStrengthMultiplier"));
			}
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

		event.getAffected().setVelocity(velocity);
	}
}

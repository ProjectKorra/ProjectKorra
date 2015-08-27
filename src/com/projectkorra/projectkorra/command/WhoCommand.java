package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.airbending.AirMethods;
import com.projectkorra.projectkorra.chiblocking.ChiMethods;
import com.projectkorra.projectkorra.earthbending.EarthMethods;
import com.projectkorra.projectkorra.firebending.FireMethods;
import com.projectkorra.projectkorra.storage.DBConnection;
import com.projectkorra.projectkorra.waterbending.WaterMethods;
import com.projectkorra.rpg.RPGMethods;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Executor for /bending who. Extends {@link PKCommand}.
 */
public class WhoCommand extends PKCommand {
	/**
	 * Map storage of all ProjectKorra staffs' UUIDs and titles
	 */
	Map<String, String> staff = new HashMap<String, String>();

	public WhoCommand() {
		super("who", "/bending who <Player>", "This command will tell you what element all players that are online are (If you don't specify a player) or give you information about the player that you specify.", new String[] { "who", "w" });

		staff.put("8621211e-283b-46f5-87bc-95a66d68880e", ChatColor.RED + "ProjectKorra Founder"); // MistPhizzle

		staff.put("a197291a-cd78-43bb-aa38-52b7c82bc68c", ChatColor.DARK_PURPLE + "ProjectKorra Lead Developer"); // OmniCypher

		staff.put("929b14fc-aaf1-4f0f-84c2-f20c55493f53", ChatColor.GREEN + "ProjectKorra Head Concept Designer"); // Vidcom

		staff.put("15d1a5a7-76ef-49c3-b193-039b27c47e30", ChatColor.GREEN + "ProjectKorra Digital Director"); // Kiam

		staff.put("96f40c81-dd5d-46b6-9afe-365114d4a082", ChatColor.DARK_PURPLE + "ProjectKorra Developer"); // Coolade
		staff.put("833a7132-a9ec-4f0a-ad9c-c3d6b8a1c7eb", ChatColor.DARK_PURPLE + "ProjectKorra Developer"); // Jacklin213
		staff.put("4eb6315e-9dd1-49f7-b582-c1170e497ab0", ChatColor.DARK_PURPLE + "ProjectKorra Developer"); // jedk1
		staff.put("5031c4e3-8103-49ea-b531-0d6ae71bad69", ChatColor.DARK_PURPLE + "ProjectKorra Developer"); // Simplicitee
		staff.put("d7757be8-86de-4898-ab4f-2b1b2fbc3dfa", ChatColor.DARK_PURPLE + "ProjectKorra Developer"); // StrangeOne101
		staff.put("a9673c93-9186-367a-96c4-e111a3bbd1b1", ChatColor.DARK_PURPLE + "ProjectKorra Developer"); // kingbirdy

		staff.put("623df34e-9cd4-438d-b07c-1905e1fc46b6", ChatColor.GREEN + "ProjectKorra Concept Designer"); // Loony
		staff.put("80f9072f-e37e-4adc-8675-1ba6af87d63b", ChatColor.GREEN + "ProjectKorra Concept Designer"); // Cross
		staff.put("ce889d32-c2a0-4765-969f-9ca54d0bd34a", ChatColor.GREEN + "ProjectKorra Concept Designer"); // ashe36
		staff.put("7daead36-d285-4640-848a-2f105334b792", ChatColor.GREEN + "ProjectKorra Concept Designer"); // Fuzzy
		staff.put("f30c871e-cd60-446b-b219-e31e00e16857", ChatColor.GREEN + "ProjectKorra Concept Designer"); // Gangksta
		staff.put("38217173-8a32-4ba7-9fe1-dd4fed031a74", ChatColor.GREEN + "ProjectKorra Concept Designer"); // Fly

		staff.put("1553482a-5e86-4270-9262-b57c11151074", ChatColor.GOLD + "ProjectKorra Community Moderator"); // Pickle9775
		staff.put("3d5bc713-ab8b-4125-b5ba-a1c1c2400b2c", ChatColor.GOLD + "ProjectKorra Community Moderator"); // Gold

		staff.put("2ab334d1-9691-4994-a624-209c7b4f220b", ChatColor.BLUE + "ProjectKorra Digital Team"); // Austygen
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if (!hasPermission(sender) || !correctLength(sender, args.size(), 0, 1)) {
			return;
		} else if (args.size() == 1) {
			whoPlayer(sender, args.get(0));
		} else if (args.size() == 0) {
			List<String> players = new ArrayList<String>();
			for (Player player : Bukkit.getOnlinePlayers()) {
				String playerName = player.getName();

				BendingPlayer bp = GeneralMethods.getBendingPlayer(playerName);
				if (bp == null) {
					GeneralMethods.createBendingPlayer(player.getUniqueId(), player.getName());
					bp = GeneralMethods.getBendingPlayer(player);
				}
				if (bp.getElements().size() > 1) {
					players.add(GeneralMethods.getAvatarColor() + playerName);
					continue;
				} else if (bp.getElements().size() == 0) {
					players.add(playerName);
					continue;
				} else if (GeneralMethods.isBender(playerName, Element.Air)) {
					players.add(AirMethods.getAirColor() + playerName);
					continue;
				} else if (GeneralMethods.isBender(playerName, Element.Water)) {
					players.add(WaterMethods.getWaterColor() + playerName);
					continue;
				} else if (GeneralMethods.isBender(playerName, Element.Earth)) {
					players.add(EarthMethods.getEarthColor() + playerName);
					continue;
				} else if (GeneralMethods.isBender(playerName, Element.Chi)) {
					players.add(ChiMethods.getChiColor() + playerName);
					continue;
				} else if (GeneralMethods.isBender(playerName, Element.Fire)) {
					players.add(FireMethods.getFireColor() + playerName);
					continue;
				}
			}
			if (players.isEmpty()) {
				sender.sendMessage(ChatColor.RED + "There is no one online.");
			} else {
				for (String st : players) {
					sender.sendMessage(st);
				}
			}
		}
	}

	/**
	 * Sends information on the given player to the CommandSender.
	 * 
	 * @param sender The CommandSender to display the information to
	 * @param playerName The Player to look up
	 */
	private void whoPlayer(CommandSender sender, String playerName) {
		Player player = Bukkit.getPlayer(playerName);
		if (player != null) {
			sender.sendMessage(playerName + " - ");
			if (GeneralMethods.isBender(playerName, Element.Air)) {
				sender.sendMessage(AirMethods.getAirColor() + "- Airbender");
				if (AirMethods.canAirFlight(player)) {
					sender.sendMessage(GeneralMethods.getSubBendingColor(Element.Air) + "    Can Fly");
				}
				if (AirMethods.canUseSpiritualProjection(player)) {
					sender.sendMessage(GeneralMethods.getSubBendingColor(Element.Air) + "    Can use Spiritual Projection");
				}
			}
			if (GeneralMethods.isBender(playerName, Element.Water)) {
				sender.sendMessage(WaterMethods.getWaterColor() + "- Waterbender");
				if (WaterMethods.canPlantbend(player)) {
					sender.sendMessage(GeneralMethods.getSubBendingColor(Element.Water) + "    Can Plantbend");
				}
				if (WaterMethods.canBloodbend(player)) {
					if (WaterMethods.canBloodbendAtAnytime(player)) {
						sender.sendMessage(GeneralMethods.getSubBendingColor(Element.Water) + "    Can Bloodbend anytime, on any day");
					} else {
						sender.sendMessage(GeneralMethods.getSubBendingColor(Element.Water) + "    Can Bloodbend");
					}
				}
				if (WaterMethods.canIcebend(player)) {
					sender.sendMessage(GeneralMethods.getSubBendingColor(Element.Water) + "    Can Icebend");
				}
				if (WaterMethods.canWaterHeal(player)) {
					sender.sendMessage(GeneralMethods.getSubBendingColor(Element.Water) + "    Can Heal");
				}
			}
			if (GeneralMethods.isBender(playerName, Element.Earth)) {
				sender.sendMessage(EarthMethods.getEarthColor() + "- Earthbender");
				if (EarthMethods.canMetalbend(player)) {
					sender.sendMessage(GeneralMethods.getSubBendingColor(Element.Earth) + "    Can Metalbend");
				}
				if (EarthMethods.canLavabend(player)) {
					sender.sendMessage(GeneralMethods.getSubBendingColor(Element.Earth) + "    Can Lavabend");
				}
				if (EarthMethods.canSandbend(player)) {
					sender.sendMessage(GeneralMethods.getSubBendingColor(Element.Earth) + "    Can Sandbend");
				}
			}
			if (GeneralMethods.isBender(playerName, Element.Fire)) {
				sender.sendMessage(FireMethods.getFireColor() + "- Firebender");
				if (FireMethods.canCombustionbend(player)) {
					sender.sendMessage(GeneralMethods.getSubBendingColor(Element.Fire) + "    Can Combustionbend");
				}
				if (FireMethods.canLightningbend(player)) {
					sender.sendMessage(GeneralMethods.getSubBendingColor(Element.Fire) + "    Can Lightningbend");
				}
			}
			if (GeneralMethods.isBender(playerName, Element.Chi)) {
				sender.sendMessage(ChiMethods.getChiColor() + "- ChiBlocker");
			}
			BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(playerName);
			UUID uuid = player.getUniqueId();
			if (bPlayer != null) {
				sender.sendMessage("Abilities: ");
				for (int i = 1; i <= 9; i++) {
					String ability = bPlayer.getAbilities().get(i);
					if (ability == null || ability.equalsIgnoreCase("null")) {
						continue;
					} else {
						sender.sendMessage(i + " - " + GeneralMethods.getAbilityColor(ability) + ability);
					}
				}
			}

			if (GeneralMethods.hasRPG()) {
				if (RPGMethods.isCurrentAvatar(player.getUniqueId())) {
					sender.sendMessage(GeneralMethods.getAvatarColor() + "Current Avatar");
				} else if (RPGMethods.hasBeenAvatar(player.getUniqueId())) {
					sender.sendMessage(GeneralMethods.getAvatarColor() + "Former Avatar");
				}
			}

			if (staff.containsKey(uuid)) {
				sender.sendMessage(staff.get(uuid));
			}
		} else {
			sender.sendMessage(ChatColor.GREEN + "You are running a lookup of an offline player, this may take a second.");

			new BukkitRunnable() {
				@Override
				public void run() {
					ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM pk_players WHERE player = '" + playerName + "'");
					try {
						final List<String> messages = new ArrayList<String>();

						if (rs2.next()) {
							UUID uuid = UUID.fromString(rs2.getString("uuid"));
							String element = rs2.getString("element");

							messages.add(playerName + " - ");
							if (element.contains("a"))
								messages.add(AirMethods.getAirColor() + "- Airbender");
							if (element.contains("w"))
								messages.add(WaterMethods.getWaterColor() + "- Waterbender");
							if (element.contains("e"))
								messages.add(EarthMethods.getEarthColor() + "- Earthbender");
							if (element.contains("f"))
								messages.add(FireMethods.getFireColor() + "- Firebender");
							if (element.contains("c"))
								messages.add(ChiMethods.getChiColor() + "- Chiblocker");

							if (GeneralMethods.hasRPG()) {
								if (RPGMethods.isCurrentAvatar(uuid)) {
									messages.add(GeneralMethods.getAvatarColor() + "Current Avatar");
								} else if (RPGMethods.hasBeenAvatar(uuid)) {
									messages.add(GeneralMethods.getAvatarColor() + "Former Avatar");
								}
							}

							if (staff.containsKey(uuid)) {
								messages.add(staff.get(uuid));
							}
						} else {
							messages.add(ChatColor.RED + "We could not find any player in your database with that username. Are you sure it is typed correctly?");
						}

						new BukkitRunnable() {
							@Override
							public void run() {
								for (String message : messages) {
									sender.sendMessage(message);
								}
							}
						}.runTask(ProjectKorra.plugin);
					}
					catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}.runTaskAsynchronously(ProjectKorra.plugin);
		}
	}
}

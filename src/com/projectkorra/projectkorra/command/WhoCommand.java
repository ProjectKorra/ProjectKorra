package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.airbending.AirMethods;
import com.projectkorra.projectkorra.chiblocking.ChiMethods;
import com.projectkorra.projectkorra.earthbending.EarthMethods;
import com.projectkorra.projectkorra.firebending.FireMethods;
import com.projectkorra.projectkorra.waterbending.WaterMethods;
import com.projectkorra.rpg.RPGMethods;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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
		super("who", "/bending who [Player/Page]", "This command will tell you what element all players that are online are (If you don't specify a player) or give you information about the player that you specify.", new String[] { "who", "w" });

		staff.put("8621211e-283b-46f5-87bc-95a66d68880e", ChatColor.RED + "ProjectKorra Founder"); // MistPhizzle

		staff.put("a197291a-cd78-43bb-aa38-52b7c82bc68c", ChatColor.DARK_PURPLE + "ProjectKorra Lead Developer"); // OmniCypher

		staff.put("929b14fc-aaf1-4f0f-84c2-f20c55493f53", ChatColor.GREEN + "ProjectKorra Head Concept Designer"); // Vidcom

		staff.put("15d1a5a7-76ef-49c3-b193-039b27c47e30", ChatColor.BLUE + "ProjectKorra Digital Director"); // Kiam
		
		staff.put("1553482a-5e86-4270-9262-b57c11151074", ChatColor.GOLD + "ProjectKorra Head Community Moderator"); // Pickle9775

		staff.put("96f40c81-dd5d-46b6-9afe-365114d4a082", ChatColor.DARK_PURPLE + "ProjectKorra Developer"); // Coolade
		staff.put("833a7132-a9ec-4f0a-ad9c-c3d6b8a1c7eb", ChatColor.DARK_PURPLE + "ProjectKorra Developer"); // Jacklin213
		staff.put("4eb6315e-9dd1-49f7-b582-c1170e497ab0", ChatColor.DARK_PURPLE + "ProjectKorra Developer"); // jedk1
		staff.put("5031c4e3-8103-49ea-b531-0d6ae71bad69", ChatColor.DARK_PURPLE + "ProjectKorra Developer"); // Simplicitee
		staff.put("d7757be8-86de-4898-ab4f-2b1b2fbc3dfa", ChatColor.DARK_PURPLE + "ProjectKorra Developer"); // StrangeOne101
		staff.put("3b5bdfab-8ae1-4794-b160-4f33f31fde99", ChatColor.DARK_PURPLE + "ProjectKorra Developer"); // kingbirdy
		staff.put("dedf335b-d282-47ab-8ffc-a80121661cd1", ChatColor.DARK_PURPLE + "ProjectKorra Developer"); // grasshopperMatt

		staff.put("623df34e-9cd4-438d-b07c-1905e1fc46b6", ChatColor.GREEN + "ProjectKorra Concept Designer"); // Loony
		//staff.put("80f9072f-e37e-4adc-8675-1ba6af87d63b", ChatColor.GREEN + "ProjectKorra Concept Designer"); // Cross
		//staff.put("57205eec-96bd-4aa3-b73f-c6627429beb2", ChatColor.GREEN + "ProjectKorra Concept Designer"); // ashe36
		//staff.put("7daead36-d285-4640-848a-2f105334b792", ChatColor.GREEN + "ProjectKorra Concept Designer"); // Fuzzy
		//staff.put("f30c871e-cd60-446b-b219-e31e00e16857", ChatColor.GREEN + "ProjectKorra Concept Designer"); // Gangksta
		staff.put("38217173-8a32-4ba7-9fe1-dd4fed031a74", ChatColor.GREEN + "ProjectKorra Concept Designer"); // Easte

		staff.put("3d5bc713-ab8b-4125-b5ba-a1c1c2400b2c", ChatColor.GOLD + "ProjectKorra Community Moderator"); // Gold

		//staff.put("2ab334d1-9691-4994-a624-209c7b4f220b", ChatColor.BLUE + "ProjectKorra Digital Team"); // Austygen
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if (!hasPermission(sender) || !correctLength(sender, args.size(), 0, 1)) {
			return;
		} else if (args.size() == 1 && args.get(0).length() > 2) {
			whoPlayer(sender, args.get(0));
		} else if (args.size() == 0 || args.size() == 1) {
			int page = 1;
			if (args.size() == 1 && isNumeric(args.get(0))) {
				page = Integer.valueOf(args.get(0));
			}
			List<String> players = new ArrayList<String>();
			for (Player player : Bukkit.getOnlinePlayers()) {
				String playerName = player.getName();
				String result = "";
				BendingPlayer bp = GeneralMethods.getBendingPlayer(playerName);
				if (bp == null) {
					GeneralMethods.createBendingPlayer(player.getUniqueId(), player.getName());
					bp = GeneralMethods.getBendingPlayer(player.getName());
				}
				if (bp.hasElement(Element.Air)) {
					result = ChatColor.WHITE + playerName + " - " + ((!bp.isElementToggled(Element.Air) || !bp.isToggled()) ? ChatColor.translateAlternateColorCodes('&', "&7&mA") : AirMethods.getAirColor() + "A");
				}
				if (bp.hasElement(Element.Earth)) {
					if (result == "") {
						result = ChatColor.WHITE + playerName + " - " + ((!bp.isElementToggled(Element.Earth) || !bp.isToggled()) ? ChatColor.translateAlternateColorCodes('&', "&a&mE") : EarthMethods.getEarthColor() + "E");
					} else {
						result = result + ChatColor.WHITE + " | " + ((!bp.isElementToggled(Element.Earth) || !bp.isToggled()) ? ChatColor.translateAlternateColorCodes('&', "&a&mE") : EarthMethods.getEarthColor() + "E");
					}
				}
				if (bp.hasElement(Element.Fire)) {
					if (result == "") {
						result = ChatColor.WHITE + playerName + " - " + ((!bp.isElementToggled(Element.Fire) || !bp.isToggled()) ? ChatColor.translateAlternateColorCodes('&', "&c&mF") : FireMethods.getFireColor() + "F");
					} else {
						result = result + ChatColor.WHITE + " | " + ((!bp.isElementToggled(Element.Fire) || !bp.isToggled()) ? ChatColor.translateAlternateColorCodes('&', "&c&mF") : FireMethods.getFireColor() + "F");
					}
				}
				if (bp.hasElement(Element.Water)) {
					if (result == "") {
						result = ChatColor.WHITE + playerName + " - " + ((!bp.isElementToggled(Element.Water) || !bp.isToggled()) ? ChatColor.translateAlternateColorCodes('&', "&b&mW") : WaterMethods.getWaterColor() + "W");
					} else {
						result = result + ChatColor.WHITE + " | " + ((!bp.isElementToggled(Element.Water) || !bp.isToggled()) ? ChatColor.translateAlternateColorCodes('&', "&b&mW") : WaterMethods.getWaterColor() + "W");
					}
				}
				if (bp.hasElement(Element.Chi)) {
					if (result == "") {
						result = ChatColor.WHITE + playerName + " - " + ((!bp.isElementToggled(Element.Chi) || !bp.isToggled()) ? ChatColor.translateAlternateColorCodes('&', "&6&mC") : ChiMethods.getChiColor() + "C");
					} else {
						result = result + ChatColor.WHITE + " | " + ((!bp.isElementToggled(Element.Chi) || !bp.isToggled()) ? ChatColor.translateAlternateColorCodes('&', "&6&mC") : ChiMethods.getChiColor() + "C");
					}
				}
				if (staff.containsKey(player.getUniqueId().toString())) {
					if (result == "") {
						result = ChatColor.WHITE + playerName + " | " + staff.get(player.getUniqueId().toString());
					} else {
						result = result + ChatColor.WHITE + " | " + staff.get(player.getUniqueId().toString());
					}
				}
				if (result == ""){
					result = ChatColor.WHITE + playerName;
				}
				players.add(result);
			}
			if (players.isEmpty()) {
				sender.sendMessage(ChatColor.RED + "There is no one online.");
			} else {
				//for (String st : players) {
				//	sender.sendMessage(st);
				//}
				for (String s : getPage(players, ChatColor.GOLD + "Players:", page, true)) {
					sender.sendMessage(s);
				}
			}
		}
	}

	/**
	 * Sends information on the given player to the CommandSender.
	 * 
	 * @param sender
	 *            The CommandSender to display the information to
	 * @param playerName
	 *            The Player to look up
	 */
	private void whoPlayer(final CommandSender sender, final String playerName) {
		//Player player = Bukkit.getPlayer(playerName);
		@SuppressWarnings("deprecation")
		final OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
		BendingPlayer bplayer = GeneralMethods.getBendingPlayer(player.getName());
		if (player == null || !player.hasPlayedBefore()) {
			sender.sendMessage(ChatColor.RED + "Player not found!");
			return;
		}
		if (!player.isOnline() && !BendingPlayer.getPlayers().containsKey(player.getUniqueId())) {
			sender.sendMessage(player.getName() + ChatColor.GRAY + " is currently offline. A lookup is currently being done (this might take a few seconds).");
		}
		
		Player player_ = (Player) (player.isOnline() ? player : null);

		if (!BendingPlayer.getPlayers().containsKey(player.getUniqueId())) {
			GeneralMethods.createBendingPlayer(player.getUniqueId(), playerName);
			BukkitRunnable runnable = new BukkitRunnable() {
				@Override
				public void run() {
					int count = 0;
					final long delay = 200L;
					while (!BendingPlayer.getPlayers().containsKey(player.getUniqueId())) {
						if (count > 5 * (1000 / delay)) { //After 5 seconds of waiting, tell the user the database is busy and to try again in a few seconds.
							sender.sendMessage(ChatColor.DARK_RED + "The database appears to busy at the moment. Please wait a few seconds and try again.");
							break;
						}
						count++;
						try {
							Thread.sleep(delay);
						}
						catch (InterruptedException e) {
							e.printStackTrace();
							sender.sendMessage(ChatColor.DARK_RED + "The database appears to busy at the moment. Please wait a few seconds and try again.");
							break;
						}
					}
					whoPlayer(sender, playerName);
				}
			};
			runnable.runTaskAsynchronously(ProjectKorra.plugin);
			return;
		}
		if (BendingPlayer.getPlayers().containsKey(player.getUniqueId())) {
			sender.sendMessage(player.getName() + (!player.isOnline() ? ChatColor.RESET + " (Offline)" : "") + " - ");
			if (GeneralMethods.isBender(playerName, Element.Air)) {
				if(bplayer.isElementToggled(Element.Air)) {
					sender.sendMessage(AirMethods.getAirColor() + "- Airbender");
				} else {
					sender.sendMessage(AirMethods.getAirColor() + "" + ChatColor.STRIKETHROUGH + "- Airbender");
				}
				if (player_ != null && AirMethods.canAirFlight((Player) player)) {
					sender.sendMessage(GeneralMethods.getSubBendingColor(Element.Air) + "    Can Fly");
				}
				if (player_ != null && AirMethods.canUseSpiritualProjection((Player) player)) {
					sender.sendMessage(GeneralMethods.getSubBendingColor(Element.Air) + "    Can use Spiritual Projection");
				}
			}
			if (GeneralMethods.isBender(playerName, Element.Water)) {
				if(bplayer.isElementToggled(Element.Water)) {
					sender.sendMessage(WaterMethods.getWaterColor() + "- Waterbender");
				} else {
					sender.sendMessage(WaterMethods.getWaterColor() + "" + ChatColor.STRIKETHROUGH + "- Waterbender");
				}
				if (player_ != null && WaterMethods.canPlantbend((Player) player)) {
					sender.sendMessage(GeneralMethods.getSubBendingColor(Element.Water) + "    Can Plantbend");
				}
				if (player_ != null && WaterMethods.canBloodbend((Player) player)) {
					if (WaterMethods.canBloodbendAtAnytime((Player) player)) {
						sender.sendMessage(GeneralMethods.getSubBendingColor(Element.Water) + "    Can Bloodbend anytime, on any day");
					} else {
						sender.sendMessage(GeneralMethods.getSubBendingColor(Element.Water) + "    Can Bloodbend");
					}
				}
				if (player_ != null && WaterMethods.canIcebend((Player) player)) {
					sender.sendMessage(GeneralMethods.getSubBendingColor(Element.Water) + "    Can Icebend");
				}
				if (player_ != null && WaterMethods.canWaterHeal((Player) player)) {
					sender.sendMessage(GeneralMethods.getSubBendingColor(Element.Water) + "    Can Heal");
				}
			}
			if (GeneralMethods.isBender(playerName, Element.Earth)) {
				if(bplayer.isElementToggled(Element.Earth)) {
					sender.sendMessage(EarthMethods.getEarthColor() + "- Earthbender");
				} else {
					sender.sendMessage(EarthMethods.getEarthColor() + "" + ChatColor.STRIKETHROUGH + "- Earthbender");
				}
				if (player_ != null && EarthMethods.canMetalbend((Player) player)) {
					sender.sendMessage(GeneralMethods.getSubBendingColor(Element.Earth) + "    Can Metalbend");
				}
				if (player_ != null && EarthMethods.canLavabend((Player) player)) {
					sender.sendMessage(GeneralMethods.getSubBendingColor(Element.Earth) + "    Can Lavabend");
				}
				if (player_ != null && EarthMethods.canSandbend((Player) player)) {
					sender.sendMessage(GeneralMethods.getSubBendingColor(Element.Earth) + "    Can Sandbend");
				}
			}
			if (GeneralMethods.isBender(playerName, Element.Fire)) {
				if(bplayer.isElementToggled(Element.Fire)) {
					sender.sendMessage(FireMethods.getFireColor() + "- Firebender");
				} else {
					sender.sendMessage(FireMethods.getFireColor() + "" + ChatColor.STRIKETHROUGH + "- Firebender");
				}
				if (player_ != null && FireMethods.canCombustionbend((Player) player)) {
					sender.sendMessage(GeneralMethods.getSubBendingColor(Element.Fire) + "    Can Combustionbend");
				}
				if (player_ != null && FireMethods.canLightningbend((Player) player)) {
					sender.sendMessage(GeneralMethods.getSubBendingColor(Element.Fire) + "    Can Lightningbend");
				}
			}
			if (GeneralMethods.isBender(playerName, Element.Chi)) {
				if(bplayer.isElementToggled(Element.Chi)) {
					sender.sendMessage(ChiMethods.getChiColor() + "- Chiblocker");
				} else {
					sender.sendMessage(ChiMethods.getChiColor() + "" + ChatColor.STRIKETHROUGH + "- Chiblocker");
				}
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

			if (staff.containsKey(uuid.toString())) {
				sender.sendMessage(staff.get(uuid.toString()));
			}
		}
	}
}

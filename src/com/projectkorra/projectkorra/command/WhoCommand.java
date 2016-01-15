package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
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
		super("who", "/bending who [Player]", "This command will tell you what element all players that are online are (If you don't specify a player) or give you information about the player that you specify.", new String[] { "who", "w" });

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
		staff.put("a9673c93-9186-367a-96c4-e111a3bbd1b1", ChatColor.DARK_PURPLE + "ProjectKorra Developer"); // kingbirdy
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
		} else if (args.size() == 1) {
			whoPlayer(sender, args.get(0));
		} else if (args.size() == 0) {
			List<String> players = new ArrayList<String>();
			for (Player player : Bukkit.getOnlinePlayers()) {
				String playerName = player.getName();
				String result = ChatColor.WHITE + playerName;
				BendingPlayer bp = BendingPlayer.getBendingPlayer(playerName);
				
				if (bp == null) {
					GeneralMethods.createBendingPlayer(player.getUniqueId(), player.getName());
					bp = BendingPlayer.getBendingPlayer(player.getName());
				}
				if (bp.hasElement(Element.AIR)) {
					result = ChatColor.WHITE + playerName + " - " + ((!bp.isElementToggled(Element.AIR) || !bp.isToggled()) ? ChatColor.translateAlternateColorCodes('&', "&7&mA") : Element.AIR.getColor() + "A");
				}
				if (bp.hasElement(Element.EARTH)) {
					if (result == "") {
						result = ChatColor.WHITE + playerName + " - " + ((!bp.isElementToggled(Element.EARTH) || !bp.isToggled()) ? ChatColor.translateAlternateColorCodes('&', "&a&mE") : Element.EARTH.getColor() + "E");
					} else {
						result = result + ChatColor.WHITE + " | " + ((!bp.isElementToggled(Element.EARTH) || !bp.isToggled()) ? ChatColor.translateAlternateColorCodes('&', "&a&mE") : Element.EARTH.getColor() + "E");
					}
				}
				if (bp.hasElement(Element.FIRE)) {
					if (result == "") {
						result = ChatColor.WHITE + playerName + " - " + ((!bp.isElementToggled(Element.FIRE) || !bp.isToggled()) ? ChatColor.translateAlternateColorCodes('&', "&c&mF") : Element.FIRE.getColor() + "F");
					} else {
						result = result + ChatColor.WHITE + " | " + ((!bp.isElementToggled(Element.FIRE) || !bp.isToggled()) ? ChatColor.translateAlternateColorCodes('&', "&c&mF") : Element.FIRE.getColor() + "F");
					}
				}
				if (bp.hasElement(Element.WATER)) {
					if (result == "") {
						result = ChatColor.WHITE + playerName + " - " + ((!bp.isElementToggled(Element.WATER) || !bp.isToggled()) ? ChatColor.translateAlternateColorCodes('&', "&b&mW") : Element.WATER.getColor() + "W");
					} else {
						result = result + ChatColor.WHITE + " | " + ((!bp.isElementToggled(Element.WATER) || !bp.isToggled()) ? ChatColor.translateAlternateColorCodes('&', "&b&mW") : Element.WATER.getColor() + "W");
					}
				}
				if (bp.hasElement(Element.CHI)) {
					if (result == "") {
						result = ChatColor.WHITE + playerName + " - " + ((!bp.isElementToggled(Element.CHI) || !bp.isToggled()) ? ChatColor.translateAlternateColorCodes('&', "&6&mC") : Element.CHI.getColor() + "C");
					} else {
						result = result + ChatColor.WHITE + " | " + ((!bp.isElementToggled(Element.CHI) || !bp.isToggled()) ? ChatColor.translateAlternateColorCodes('&', "&6&mC") : Element.CHI.getColor() + "C");
					}
				}
				if (staff.containsKey(player.getUniqueId().toString())) {
					if (result == "") {
						result = ChatColor.WHITE + playerName + staff.get(player.getUniqueId().toString());
					} else {
						result = result + ChatColor.WHITE + " | " + staff.get(player.getUniqueId().toString());
					}
				}
				players.add(result);
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
	 * @param sender
	 *            The CommandSender to display the information to
	 * @param playerName
	 *            The Player to look up
	 */
	private void whoPlayer(final CommandSender sender, final String playerName) {
		//Player player = Bukkit.getPlayer(playerName);
		@SuppressWarnings("deprecation")
		final OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
		if (player == null || !player.hasPlayedBefore()) {
			sender.sendMessage(ChatColor.RED + "Player not found!");
			return;
		}
		if (!player.isOnline() && !BendingPlayer.getPlayers().containsKey(player.getUniqueId())) {
			sender.sendMessage(player.getName() + ChatColor.GRAY + " is currently offline. A lookup is currently being done (this might take a few seconds).");
		}
		
		Player player_ = (Player) (player.isOnline() ? player : null);
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer == null) {
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
		
		bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer != null) {
			sender.sendMessage(player.getName() + (!player.isOnline() ? ChatColor.RESET + " (Offline)" : "") + " - ");
			if (bPlayer.hasElement(Element.AIR)) {
				if(bPlayer.isElementToggled(Element.AIR)) {
					sender.sendMessage(Element.AIR.getColor() + "- Airbender");
				} else {
					sender.sendMessage(Element.AIR.getColor() + "" + ChatColor.STRIKETHROUGH + "- Airbender");
				}
				
				if (player_ != null && bPlayer.canUseFlight()) {
					sender.sendMessage(Element.FLIGHT.getColor() + "    Can Fly");
				}
				if (player_ != null && bPlayer.canUseSpiritualProjection()) {
					sender.sendMessage(Element.SPIRITUAL.getColor() + "    Can use Spiritual Projection");
				}
			}
			if (bPlayer.hasElement(Element.WATER)) {
				if(bPlayer.isElementToggled(Element.WATER)) {
					sender.sendMessage(Element.WATER.getColor() + "- Waterbender");
				} else {
					sender.sendMessage(Element.WATER.getColor() + "" + ChatColor.STRIKETHROUGH + "- Waterbender");
				}
				
				if (player_ != null && bPlayer.canPlantbend()) {
					sender.sendMessage(Element.PLANT.getColor() + "    Can Plantbend");
				}
				if (player_ != null && bPlayer.canBloodbend()) {
					if (bPlayer.canBloodbendAtAnytime()) {
						sender.sendMessage(Element.BLOOD.getColor() + "    Can Bloodbend anytime, on any day");
					} else {
						sender.sendMessage(Element.BLOOD.getColor() + "    Can Bloodbend");
					}
				}
				if (player_ != null && bPlayer.canIcebend()) {
					sender.sendMessage(Element.ICE.getColor() + "    Can Icebend");
				}
				if (player_ != null && bPlayer.canWaterHeal()) {
					sender.sendMessage(Element.HEALING.getColor() + "    Can Heal");
				}
			}
			if (bPlayer.hasElement(Element.EARTH)) {
				if(bPlayer.isElementToggled(Element.EARTH)) {
					sender.sendMessage(Element.EARTH.getColor() + "- Earthbender");
				} else {
					sender.sendMessage(Element.EARTH.getColor() + "" + ChatColor.STRIKETHROUGH + "- Earthbender");
				}
				
				if (player_ != null && bPlayer.canMetalbend()) {
					sender.sendMessage(Element.METAL.getColor() + "    Can Metalbend");
				}
				if (player_ != null && bPlayer.canLavabend()) {
					sender.sendMessage(Element.LAVA.getColor() + "    Can Lavabend");
				}
				if (player_ != null && bPlayer.canSandbend()) {
					sender.sendMessage(Element.SAND.getColor() + "    Can Sandbend");
				}
			}
			if (bPlayer.hasElement(Element.FIRE)) {
				if(bPlayer.isElementToggled(Element.FIRE)) {
					sender.sendMessage(Element.FIRE.getColor() + "- Firebender");
				} else {
					sender.sendMessage(Element.FIRE.getColor() + "" + ChatColor.STRIKETHROUGH + "- Firebender");
				}
				
				if (player_ != null && bPlayer.canCombustionbend()) {
					sender.sendMessage(Element.COMBUSTION.getColor() + "    Can Combustionbend");
				}
				if (player_ != null && bPlayer.canLightningbend()) {
					sender.sendMessage(Element.LIGHTNING.getColor() + "    Can Lightningbend");
				}
			}
			if (bPlayer.hasElement(Element.CHI)) {
				if(bPlayer.isElementToggled(Element.CHI)) {
					sender.sendMessage(Element.CHI.getColor() + "- Chibender");
				} else {
					sender.sendMessage(Element.CHI.getColor() + "" + ChatColor.STRIKETHROUGH + "- Chibender");
				}
			}
			
			UUID uuid = player.getUniqueId();
			if (bPlayer != null) {
				sender.sendMessage("Abilities: ");
				for (int i = 1; i <= 9; i++) {
					String ability = bPlayer.getAbilities().get(i);
					CoreAbility coreAbil = CoreAbility.getAbility(ability);
					if (coreAbil == null) {
						continue;
					} else {
						sender.sendMessage(i + " - " + coreAbil.getElement().getColor() + ability);
					}
				}
			}

			if (GeneralMethods.hasRPG()) {
				if (RPGMethods.isCurrentAvatar(player.getUniqueId())) {
					sender.sendMessage(Element.AVATAR.getColor() + "Current Avatar");
				} else if (RPGMethods.hasBeenAvatar(player.getUniqueId())) {
					sender.sendMessage(Element.AVATAR.getColor() + "Former Avatar");
				}
			}

			if (staff.containsKey(uuid.toString())) {
				sender.sendMessage(staff.get(uuid.toString()));
			}
		}
	}
}

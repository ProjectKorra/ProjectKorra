package com.projectkorra.projectkorra.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.ElementType;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.rpg.RPGMethods;
import com.projectkorra.spirits.SpiritElement;
import com.projectkorra.spirits.SpiritPlayer;

/**
 * Executor for /bending who. Extends {@link PKCommand}.
 */
public class WhoCommand extends PKCommand {
	/**
	 * Map storage of all ProjectKorra staffs' UUIDs and titles
	 */
	Map<String, String> staff = new HashMap<String, String>(), playerInfoWords = new HashMap<String, String>();
	
	private String databaseOverload, noPlayersOnline, playerOffline;
	
	public WhoCommand() {
		super("who", "/bending who [Page/Player]", ConfigManager.languageConfig.get().getString("Commands.Who.Description"), new String[] { "who", "w" });
		
		databaseOverload = ConfigManager.languageConfig.get().getString("Commands.Who.DatabaseOverload");
		noPlayersOnline = ConfigManager.languageConfig.get().getString("Commands.Who.NoPlayersOnline");
		playerOffline = ConfigManager.languageConfig.get().getString("Commands.Who.PlayerOffline");
		
		staff.put("8621211e-283b-46f5-87bc-95a66d68880e", ChatColor.RED + "ProjectKorra Founder"); // MistPhizzle

		staff.put("a197291a-cd78-43bb-aa38-52b7c82bc68c", ChatColor.DARK_PURPLE + "ProjectKorra Lead Developer"); // OmniCypher

		staff.put("15d1a5a7-76ef-49c3-b193-039b27c47e30", ChatColor.GREEN + "ProjectKorra Administrator"); // Kiam
		
		staff.put("1553482a-5e86-4270-9262-b57c11151074", ChatColor.GOLD + "ProjectKorra Head Community Moderator"); // Pickle9775

		staff.put("96f40c81-dd5d-46b6-9afe-365114d4a082", ChatColor.DARK_PURPLE + "ProjectKorra Developer"); // Coolade
		staff.put("833a7132-a9ec-4f0a-ad9c-c3d6b8a1c7eb", ChatColor.DARK_PURPLE + "ProjectKorra Developer"); // Jacklin213
		staff.put("d7757be8-86de-4898-ab4f-2b1b2fbc3dfa", ChatColor.DARK_PURPLE + "ProjectKorra Developer"); // StrangeOne101
		staff.put("3b5bdfab-8ae1-4794-b160-4f33f31fde99", ChatColor.DARK_PURPLE + "ProjectKorra Developer"); // kingbirdy
		staff.put("dedf335b-d282-47ab-8ffc-a80121661cd1", ChatColor.DARK_PURPLE + "ProjectKorra Developer"); // grasshopperMatt
		staff.put("679a6396-6a31-4898-8130-044f34bef743", ChatColor.DARK_PURPLE + "ProjectKorra Developer"); // savior67
		staff.put("1c30007f-f8ef-4b4e-aff0-787aa1bc09a3", ChatColor.DARK_PURPLE + "ProjectKorra Developer"); // Sorin
		staff.put("dd578a4f-d35e-4fed-94db-9d5a627ff962", ChatColor.DARK_PURPLE + "ProjectKorra Developer"); // Sobki
		staff.put("ed8c05af-eb43-4b71-9be3-c2ccca6c849a", ChatColor.DARK_PURPLE + "ProjectKorra Developer"); // Matan

		staff.put("623df34e-9cd4-438d-b07c-1905e1fc46b6", ChatColor.GREEN + "ProjectKorra Concept Designer"); // Loony
		staff.put("3c484e61-7876-46c0-98c9-88c7834dc96c", ChatColor.GREEN + "ProjectKorra Concept Designer"); // SamuraiSnowman (Zmeduna)
		
		staff.put("3d5bc713-ab8b-4125-b5ba-a1c1c2400b2c", ChatColor.GOLD + "ProjectKorra Community Moderator"); // Gold
		staff.put("38217173-8a32-4ba7-9fe1-dd4fed031a74", ChatColor.GOLD + "ProjectKorra Community Moderator"); // Easte
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
				BendingPlayer bp = BendingPlayer.getBendingPlayer(playerName);
				
				if (bp == null) {
					GeneralMethods.createBendingPlayer(player.getUniqueId(), player.getName());
					bp = BendingPlayer.getBendingPlayer(player.getName());
				}
				for (Element element : bp.getElements()) {
					if (result == "") {
						result = ChatColor.WHITE + playerName + " - " + (((!bp.isElementToggled(element) || !bp.isToggled()) ? element.getColor() + "" + ChatColor.STRIKETHROUGH : element.getColor()) + element.getName().substring(0, 1));
					} else {
						result = result + ChatColor.WHITE + " | " + (((!bp.isElementToggled(element) || !bp.isToggled()) ? element.getColor() + "" + ChatColor.STRIKETHROUGH : element.getColor()) + element.getName().substring(0, 1));
					}
				}
				if (staff.containsKey(player.getUniqueId().toString())) {
					if (result == "") {
						result = ChatColor.WHITE + playerName + " | " + staff.get(player.getUniqueId().toString());
					} else {
						result = result + ChatColor.WHITE + " | " + staff.get(player.getUniqueId().toString());
					}
				}
				if (result == "") {
					result = ChatColor.WHITE + playerName;
				}
				players.add(result);
			}
			if (players.isEmpty()) {
				sender.sendMessage(ChatColor.RED + noPlayersOnline);
			} else {
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
		if (player == null || !player.hasPlayedBefore() && !player.isOnline()) {
			sender.sendMessage(ChatColor.RED + "Player not found!");
			return;
		}
		if (!player.isOnline() && !BendingPlayer.getPlayers().containsKey(player.getUniqueId())) {
			sender.sendMessage(ChatColor.GRAY + playerOffline.replace("{player}", ChatColor.WHITE + player.getName() + ChatColor.GRAY)
			.replace("{target}", ChatColor.WHITE + player.getName() + ChatColor.GRAY));
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
							sender.sendMessage(ChatColor.DARK_RED + databaseOverload);
							break;
						}
						count++;
						try {
							Thread.sleep(delay);
						}
						catch (InterruptedException e) {
							e.printStackTrace();
							sender.sendMessage(ChatColor.DARK_RED + databaseOverload);
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
				
				if (player_ != null) {
					if (bPlayer.canUseFlight()) {
						sender.sendMessage(Element.FLIGHT.getColor() + "    Can Fly");
					}
					if (bPlayer.canUseSpiritualProjection()) {
						sender.sendMessage(Element.SPIRITUAL.getColor() + "    Can use Spiritual Projection");
					}
					for (SubElement se : Element.getAddonSubElements(Element.AIR)) {
						if (bPlayer.canUseSubElement(se)) {
							sender.sendMessage(se.getColor() + "    Can " + (!se.getType().equals(ElementType.NO_SUFFIX) ? "" : "use ") + se.getName() + se.getType().getBend());
						}
					}
				}
			}
			if (bPlayer.hasElement(Element.WATER)) {
				if(bPlayer.isElementToggled(Element.WATER)) {
					sender.sendMessage(Element.WATER.getColor() + "- Waterbender");
				} else {
					sender.sendMessage(Element.WATER.getColor() + "" + ChatColor.STRIKETHROUGH + "- Waterbender");
				}
				
				if (player_ != null) {
					if (bPlayer.canPlantbend()) {
						sender.sendMessage(Element.PLANT.getColor() + "    Can Plantbend");
					}
					if (bPlayer.canBloodbend()) {
						if (bPlayer.canBloodbendAtAnytime()) {
							sender.sendMessage(Element.BLOOD.getColor() + "    Can Bloodbend anytime, on any day");
						} else {
							sender.sendMessage(Element.BLOOD.getColor() + "    Can Bloodbend");
						}
					}
					if (bPlayer.canIcebend()) {
						sender.sendMessage(Element.ICE.getColor() + "    Can Icebend");
					}
					if (bPlayer.canWaterHeal()) {
						sender.sendMessage(Element.HEALING.getColor() + "    Can Heal");
					}
					for (SubElement se : Element.getAddonSubElements(Element.WATER)) {
						if (bPlayer.canUseSubElement(se)) {
							sender.sendMessage(se.getColor() + "    Can " + (!se.getType().equals(ElementType.NO_SUFFIX) ? "" : "use ") + se.getName() + se.getType().getBend());
						}
					}
				}
			}
			if (bPlayer.hasElement(Element.EARTH)) {
				if(bPlayer.isElementToggled(Element.EARTH)) {
					sender.sendMessage(Element.EARTH.getColor() + "- Earthbender");
				} else {
					sender.sendMessage(Element.EARTH.getColor() + "" + ChatColor.STRIKETHROUGH + "- Earthbender");
				}
				
				if (player_ != null) {
					if (bPlayer.canMetalbend()) {
						sender.sendMessage(Element.METAL.getColor() + "    Can Metalbend");
					}
					if (bPlayer.canLavabend()) {
						sender.sendMessage(Element.LAVA.getColor() + "    Can Lavabend");
					}
					if (bPlayer.canSandbend()) {
						sender.sendMessage(Element.SAND.getColor() + "    Can Sandbend");
					}
					for (SubElement se : Element.getAddonSubElements(Element.EARTH)) {
						if (bPlayer.canUseSubElement(se)) {
							sender.sendMessage(se.getColor() + "    Can " + (!se.getType().equals(ElementType.NO_SUFFIX) ? "" : "use ") + se.getName() + se.getType().getBend());
						}
					}
				}
			}
			if (bPlayer.hasElement(Element.FIRE)) {
				if(bPlayer.isElementToggled(Element.FIRE)) {
					sender.sendMessage(Element.FIRE.getColor() + "- Firebender");
				} else {
					sender.sendMessage(Element.FIRE.getColor() + "" + ChatColor.STRIKETHROUGH + "- Firebender");
				}
				
				if (player_ != null) {
					if (bPlayer.canCombustionbend()) {
						sender.sendMessage(Element.COMBUSTION.getColor() + "    Can Combustionbend");
					}
					if (bPlayer.canLightningbend()) {
						sender.sendMessage(Element.LIGHTNING.getColor() + "    Can Lightningbend");
					}
					for (SubElement se : Element.getAddonSubElements(Element.FIRE)) {
						if (bPlayer.canUseSubElement(se)) {
							sender.sendMessage(se.getColor() + "    Can " + (!se.getType().equals(ElementType.NO_SUFFIX) ? "" : "use ") + se.getName() + se.getType().getBend());
						}
					}
				}
			}
			if (bPlayer.hasElement(Element.CHI)) {
				if(bPlayer.isElementToggled(Element.CHI)) {
					sender.sendMessage(Element.CHI.getColor() + "- Chiblocker");
				} else {
					sender.sendMessage(Element.CHI.getColor() + "" + ChatColor.STRIKETHROUGH + "- Chiblocker");
				}
			}
			for (Element element : Element.getAddonElements()) {
				if (bPlayer.hasElement(element)) {
					sender.sendMessage(element.getColor() + "" + (bPlayer.isElementToggled(element) ? "" : ChatColor.STRIKETHROUGH) + "- " + element.getName() + (element.getType() != null ? element.getType().getBender() : ""));
					if (player_ != null) {
						for (SubElement subelement : Element.getSubElements(element)) {
							if (GeneralMethods.hasSpirits()) {
								SpiritPlayer sPlayer = SpiritPlayer.getSpiritPlayer(player_);
								if (subelement.equals(SpiritElement.DARK) && sPlayer.isLightSpirit()) {
									sender.sendMessage(subelement.getColor() + "    Is " + sPlayer.getSpirit().getName() + element.getName());
								}
								if (subelement.equals(SpiritElement.LIGHT) && sPlayer.isDarkSpirit()) {
									sender.sendMessage(subelement.getColor() + "    Is " + sPlayer.getSpirit().getName() + element.getName());
								}
								if (sPlayer.isSpirit()) {
									continue;
								}
							}
							if (bPlayer.canUseSubElement(subelement)) {
								sender.sendMessage(subelement.getColor() + "    Can " + (!subelement.getType().equals(ElementType.NO_SUFFIX) ? "" : "use ") + subelement.getName() + subelement.getType().getBend());
							}
						}
					}
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
	
	@Override
	protected List<String> getTabCompletion(CommandSender sender, List<String> args) {
		if (args.size() >= 1 || !sender.hasPermission("bending.command.who")) return new ArrayList<String>();
		List<String> l = new ArrayList<String>();
		for (Player p : Bukkit.getOnlinePlayers()) {
			l.add(p.getName());
		}
		return l;
	}
}

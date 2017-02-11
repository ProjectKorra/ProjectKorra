package com.projectkorra.projectkorra.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
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
	final Map<String, String> staff = new HashMap<String, String>(),
			playerInfoWords = new HashMap<String, String>();

	private String databaseOverload, noPlayersOnline, playerOffline;

	public WhoCommand() {
		super("who", "/bending who [Page/Player]", ConfigManager.languageConfig.get().getString("Commands.Who.Description"), new String[] { "who", "w" });

		databaseOverload = ConfigManager.languageConfig.get().getString("Commands.Who.DatabaseOverload");
		noPlayersOnline = ConfigManager.languageConfig.get().getString("Commands.Who.NoPlayersOnline");
		playerOffline = ConfigManager.languageConfig.get().getString("Commands.Who.PlayerOffline");

		new BukkitRunnable() {
			public void run() {
				Map<String, String> updatedstaff = new HashMap<String, String>();
				try {

					// Create a URL for the desired page
					URLConnection url = new URL("http://www.projectkorra.com/staff.txt").openConnection();
					url.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");

					// Read all the text returned by the server
					BufferedReader in = new BufferedReader(new InputStreamReader(url.getInputStream(), Charset.forName("UTF-8")));
					String unparsed;
					while ((unparsed = in.readLine()) != null) {
						String[] staffEntry = unparsed.split("/");
						if (staffEntry.length >= 2) {
							updatedstaff.put(staffEntry[0], ChatColor.translateAlternateColorCodes('&', staffEntry[1]));
						}
					}
					in.close();
					staff.clear();
					staff.putAll(updatedstaff);
				}
				catch (SocketException e) {
					ProjectKorra.log.info("Could not update staff list.");
				}
				catch (MalformedURLException e) {
					e.printStackTrace();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.runTaskTimerAsynchronously(ProjectKorra.plugin, 0, 20 * 60 * 60);
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
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + noPlayersOnline);
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
	 * @param sender The CommandSender to display the information to
	 * @param playerName The Player to look up
	 */
	private void whoPlayer(final CommandSender sender, final String playerName) {
		//Player player = Bukkit.getPlayer(playerName);
		@SuppressWarnings("deprecation")
		final OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
		if (player == null || !player.hasPlayedBefore() && !player.isOnline()) {
			GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + "Player not found!");
			return;
		}
		if (!player.isOnline() && !BendingPlayer.getPlayers().containsKey(player.getUniqueId())) {
			GeneralMethods.sendBrandingMessage(sender, ChatColor.GRAY + playerOffline.replace("{player}", ChatColor.WHITE + player.getName() + ChatColor.GRAY).replace("{target}", ChatColor.WHITE + player.getName() + ChatColor.GRAY));
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
							GeneralMethods.sendBrandingMessage(sender, ChatColor.DARK_RED + databaseOverload);
							break;
						}
						count++;
						try {
							Thread.sleep(delay);
						}
						catch (InterruptedException e) {
							e.printStackTrace();
							GeneralMethods.sendBrandingMessage(sender, ChatColor.DARK_RED + databaseOverload);
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
				if (bPlayer.isElementToggled(Element.AIR)) {
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
				if (bPlayer.isElementToggled(Element.WATER)) {
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
				if (bPlayer.isElementToggled(Element.EARTH)) {
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
				if (bPlayer.isElementToggled(Element.FIRE)) {
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
				if (bPlayer.isElementToggled(Element.CHI)) {
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
		if (args.size() >= 1 || !sender.hasPermission("bending.command.who"))
			return new ArrayList<String>();
		List<String> l = new ArrayList<String>();
		for (Player p : Bukkit.getOnlinePlayers()) {
			l.add(p.getName());
		}
		return l;
	}
}

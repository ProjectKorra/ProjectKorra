package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AbilityHandler;
import com.projectkorra.projectkorra.configuration.configs.commands.WhoCommandConfig;
import com.projectkorra.projectkorra.element.Element;
import com.projectkorra.projectkorra.element.ElementManager;
import com.projectkorra.projectkorra.element.SubElement;
import com.projectkorra.projectkorra.player.BendingPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Executor for /bending who. Extends {@link PKCommand}.
 */
@SuppressWarnings({ "rawtypes", "deprecation" })
public class WhoCommand extends PKCommand<WhoCommandConfig> {
	/**
	 * Map storage of all ProjectKorra staffs' UUIDs and titles
	 */
	final Map<String, String> staff = Collections.synchronizedMap(new HashMap<>());

	private final String databaseOverload, noPlayersOnline, playerOffline;

	public WhoCommand(final WhoCommandConfig config) {
		super(config, "who", "/bending who [Page/Player]", config.Description, new String[] { "who", "w" });

		this.databaseOverload = config.DatabaseOverload;
		this.noPlayersOnline = config.NoPlayersOnline;
		this.playerOffline = config.PlayerOffline;

		new BukkitRunnable() {
			@Override
			public void run() {
				final Map<String, String> updatedstaff = new HashMap<String, String>();
				try {

					// Create a URL for the desired page.
					final URLConnection url = new URL("https://projectkorra.com/staff.txt").openConnection();
					url.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");

					// Read all the text returned by the server.
					final BufferedReader in = new BufferedReader(new InputStreamReader(url.getInputStream(), Charset.forName("UTF-8")));
					String unparsed;
					while ((unparsed = in.readLine()) != null) {
						final String[] staffEntry = unparsed.split("/");
						if (staffEntry.length >= 2) {
							updatedstaff.put(staffEntry[0], ChatColor.translateAlternateColorCodes('&', staffEntry[1]));
						}
					}
					in.close();
					WhoCommand.this.staff.clear();
					WhoCommand.this.staff.putAll(updatedstaff);
				} catch (final SocketException e) {
					ProjectKorra.log.info("Could not update staff list.");
				} catch (final MalformedURLException e) {
					e.printStackTrace();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}.runTaskTimerAsynchronously(ProjectKorra.plugin, 0, 20 * 60 * 60);
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.hasPermission(sender) || !this.correctLength(sender, args.size(), 0, 1)) {
			return;
		} else if (args.size() == 1 && args.get(0).length() > 2) {
			this.whoPlayer(sender, args.get(0));
		} else if (args.size() == 0 || args.size() == 1) {
			int page = 1;
			if (args.size() == 1 && this.isNumeric(args.get(0))) {
				page = Integer.valueOf(args.get(0));
			}
			final List<String> players = new ArrayList<String>();
			for (final Player player : Bukkit.getOnlinePlayers()) {
				final String playerName = player.getName();
				String result = "";
				BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);

				for (final Element element : bendingPlayer.getElements()) {
					if (result == "") {
						result = ChatColor.WHITE + playerName + " - " + (((!bendingPlayer.isElementToggled(element) || !bendingPlayer.isToggled()) ? element.getColor() + "" + ChatColor.STRIKETHROUGH : element.getColor()) + element.getName().substring(0, 1));
					} else {
						result = result + ChatColor.WHITE + " | " + (((!bendingPlayer.isElementToggled(element) || !bendingPlayer.isToggled()) ? element.getColor() + "" + ChatColor.STRIKETHROUGH : element.getColor()) + element.getName().substring(0, 1));
					}
				}
				if (this.staff.containsKey(player.getUniqueId().toString())) {
					if (result == "") {
						result = ChatColor.WHITE + playerName + " | " + this.staff.get(player.getUniqueId().toString());
					} else {
						result = result + ChatColor.WHITE + " | " + this.staff.get(player.getUniqueId().toString());
					}
				}
				if (result == "") {
					result = ChatColor.WHITE + playerName;
				}
				players.add(result);
			}
			if (players.isEmpty()) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.noPlayersOnline);
			} else {
				boolean firstMessage = true;

				for (final String s : this.getPage(players, ChatColor.GOLD + "Players:", page, true)) {
					if (firstMessage) {
						GeneralMethods.sendBrandingMessage(sender, s);
						firstMessage = false;
					} else {
						sender.sendMessage(s);
					}
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
		final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

		if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline()) {
			GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + "Player not found!");
			return;
		}

		BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(offlinePlayer.getUniqueId());

		if (bendingPlayer != null) {
			someWhoPlayerMethod(sender, offlinePlayer, bendingPlayer);
			return;
		}

		GeneralMethods.sendBrandingMessage(sender, ChatColor.GRAY + this.playerOffline.replace("{player}", ChatColor.WHITE + offlinePlayer.getName() + ChatColor.GRAY).replace("{target}", ChatColor.WHITE + offlinePlayer.getName() + ChatColor.GRAY));

		this.bendingPlayerManager.loadBendingPlayer(offlinePlayer.getUniqueId(), loadedBendingPlayer -> {
			if (loadedBendingPlayer == null) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.DARK_RED + WhoCommand.this.databaseOverload);
				return;
			}

			someWhoPlayerMethod(sender, offlinePlayer, loadedBendingPlayer);
		});
	}

	private void someWhoPlayerMethod(CommandSender sender, OfflinePlayer player, BendingPlayer bendingPlayer) {
		sender.sendMessage(player.getName() + (!player.isOnline() ? ChatColor.RESET + " (Offline)" : ""));

		Element element;
		Set<Element> checked = new HashSet<>();

		checked.add(element = this.elementManager.getAir());
		if (bendingPlayer.hasElement(element)) {
			if (bendingPlayer.isElementToggled(element)) {
				sender.sendMessage(element.getColor() + "- Airbender");
			} else {
				sender.sendMessage(element.getColor() + "" + ChatColor.STRIKETHROUGH + "- Airbender");
			}

			if (player.isOnline()) {
				if (bendingPlayer.canUseFlight()) {
					sender.sendMessage(this.elementManager.getFlight().getColor() + "    Can Fly");
				}

				if (bendingPlayer.canUseSpiritual()) {
					sender.sendMessage(this.elementManager.getSpiritual().getColor() + "    Can use Spiritual Projection");
				}

				for (SubElement subElement : this.elementManager.getSubElements(element)) {
					if (bendingPlayer.hasElement(subElement)) {
						sender.sendMessage(subElement.getColor() + "    Can " + (!subElement.getType().equals(ElementManager.ElementType.NO_SUFFIX) ? "" : "use ") + subElement.getName() + subElement.getType().getBend());
					}
				}
			}
		}

		checked.add(element = this.elementManager.getWater());
		if (bendingPlayer.hasElement(element)) {
			if (bendingPlayer.isElementToggled(element)) {
				sender.sendMessage(element.getColor() + "- Waterbender");
			} else {
				sender.sendMessage(element.getColor() + "" + ChatColor.STRIKETHROUGH + "- Waterbender");
			}

			if (player.isOnline()) {
				if (bendingPlayer.canPlantbend()) {
					sender.sendMessage(this.elementManager.getPlant().getColor() + "    Can Plantbend");
				}

				if (bendingPlayer.canBloodbend()) {
					if (bendingPlayer.canBloodbendAtAnytime()) {
						sender.sendMessage(this.elementManager.getBlood().getColor() + "    Can Bloodbend anytime, on any day");
					} else {
						sender.sendMessage(this.elementManager.getBlood().getColor() + "    Can Bloodbend");
					}
				}

				if (bendingPlayer.canIcebend()) {
					sender.sendMessage(this.elementManager.getIce().getColor() + "    Can Icebend");
				}

				if (bendingPlayer.canUseHealing()) {
					sender.sendMessage(this.elementManager.getHealing().getColor() + "    Can Heal");
				}

				for (SubElement subElement : this.elementManager.getSubElements(element)) {
					if (bendingPlayer.hasElement(subElement)) {
						sender.sendMessage(subElement.getColor() + "    Can " + (!subElement.getType().equals(ElementManager.ElementType.NO_SUFFIX) ? "" : "use ") + subElement.getName() + subElement.getType().getBend());
					}
				}
			}
		}

		checked.add(element = this.elementManager.getEarth());
		if (bendingPlayer.hasElement(element)) {
			if (bendingPlayer.isElementToggled(element)) {
				sender.sendMessage(element.getColor() + "- Earthbender");
			} else {
				sender.sendMessage(element.getColor() + "" + ChatColor.STRIKETHROUGH + "- Earthbender");
			}

			if (player.isOnline()) {
				if (bendingPlayer.canMetalbend()) {
					sender.sendMessage(this.elementManager.getMetal().getColor() + "    Can Metalbend");
				}

				if (bendingPlayer.canLavabend()) {
					sender.sendMessage(this.elementManager.getLava().getColor() + "    Can Lavabend");
				}

				if (bendingPlayer.canSandbend()) {
					sender.sendMessage(this.elementManager.getSand().getColor() + "    Can Sandbend");
				}

				for (SubElement subElement : this.elementManager.getSubElements(element)) {
					if (bendingPlayer.hasElement(subElement)) {
						sender.sendMessage(subElement.getColor() + "    Can " + (!subElement.getType().equals(ElementManager.ElementType.NO_SUFFIX) ? "" : "use ") + subElement.getName() + subElement.getType().getBend());
					}
				}
			}
		}

		checked.add(element = this.elementManager.getFire());
		if (bendingPlayer.hasElement(element)) {
			if (bendingPlayer.isElementToggled(element)) {
				sender.sendMessage(element.getColor() + "- Firebender");
			} else {
				sender.sendMessage(element.getColor() + "" + ChatColor.STRIKETHROUGH + "- Firebender");
			}

			if (player.isOnline()) {
				if (bendingPlayer.canCombustionbend()) {
					sender.sendMessage(this.elementManager.getCombustion().getColor() + "    Can Combustionbend");
				}

				if (bendingPlayer.canUseLightning()) {
					sender.sendMessage(this.elementManager.getLightning().getColor() + "    Can Lightningbend");
				}

				for (SubElement subElement : this.elementManager.getSubElements(element)) {
					if (bendingPlayer.hasElement(subElement)) {
						sender.sendMessage(subElement.getColor() + "    Can " + (!subElement.getType().equals(ElementManager.ElementType.NO_SUFFIX) ? "" : "use ") + subElement.getName() + subElement.getType().getBend());
					}
				}
			}
		}

		checked.add(element = this.elementManager.getChi());
		if (bendingPlayer.hasElement(element)) {
			if (bendingPlayer.isElementToggled(element)) {
				sender.sendMessage(element.getColor() + "- Chiblocker");
			} else {
				sender.sendMessage(element.getColor() + "" + ChatColor.STRIKETHROUGH + "- Chiblocker");
			}
		}

		for (Element e : this.elementManager.getElements()) {
			if (checked.contains(e)) {
				continue;
			}

			if (bendingPlayer.hasElement(e)) {
				sender.sendMessage(e.getColor() + "" + (bendingPlayer.isElementToggled(e) ? "" : ChatColor.STRIKETHROUGH) + "- " + e.getName() + (e.getType() != null ? e.getType().getBender() : ""));
				if (player.isOnline()) {
					for (SubElement subElement : this.elementManager.getSubElements(e)) {
						if (bendingPlayer.hasElement(subElement)) {
							sender.sendMessage(subElement.getColor() + "    Can " + (!subElement.getType().equals(ElementManager.ElementType.NO_SUFFIX) ? "" : "use ") + subElement.getName() + subElement.getType().getBend());
						}
					}
				}
			}
		}

		final UUID uuid = player.getUniqueId();
		sender.sendMessage("Abilities: ");
		for (int i = 0; i < 9; i++) {
			String abilityName = bendingPlayer.getAbility(i);
			AbilityHandler abilityHandler = this.abilityHandlerManager.getHandler(abilityName);

			if (abilityHandler == null) {
				continue;
			}

			sender.sendMessage((i + 1) + " - " + abilityHandler.getElement().getColor() + abilityName);
		}

		if (this.staff.containsKey(uuid.toString())) {
			sender.sendMessage(this.staff.get(uuid.toString()));
		}
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (args.size() >= 1 || !sender.hasPermission("bending.command.who")) {
			return new ArrayList<>();
		}
		final List<String> l = new ArrayList<>();
		for (final Player p : Bukkit.getOnlinePlayers()) {
			l.add(p.getName());
		}
		return l;
	}
}

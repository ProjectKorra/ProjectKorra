package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.board.BendingBoardManager;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CopyCommand extends PKCommand {

	private final String playerNotFound;
	private final String copied;
	private final String failedToBindAll;
	private final String copiedOther;
	private final String cantEditBinds;
	private final String cantEditBindsOther;

	public CopyCommand() {
		super("copy", "/bending copy <Player> [Player]", ConfigManager.languageConfig.get().getString("Commands.Copy.Description"), new String[] { "copy", "co" });

		this.playerNotFound = ConfigManager.languageConfig.get().getString("Commands.Copy.PlayerNotFound");
		this.copied = ConfigManager.languageConfig.get().getString("Commands.Copy.SuccessfullyCopied");
		this.failedToBindAll = ConfigManager.languageConfig.get().getString("Commands.Copy.FailedToBindAll");
		this.copiedOther = ConfigManager.languageConfig.get().getString("Commands.Copy.Other.SuccessfullyCopied");
		this.cantEditBinds = ConfigManager.languageConfig.get().getString("Commands.Copy.CantEditBinds");
		this.cantEditBindsOther = ConfigManager.languageConfig.get().getString("Commands.Copy.Other.CantEditBinds");
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.correctLength(sender, args.size(), 1, 2)) {
			return;
		} else if (args.size() == 1) {
			if (!this.hasPermission(sender) || !this.isPlayer(sender)) {
				return;
			}

			final OfflinePlayer player = Bukkit.getOfflinePlayer(args.get(0));

			if (!player.isOnline() && !player.hasPlayedBefore()) {
				ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.playerNotFound);
				return;
			}

			if (player instanceof Player && MultiAbilityManager.hasMultiAbilityBound((Player) player)) {
				ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.cantEditBinds);
				return;
			}

			this.assignAbilities(sender, player, (Player) sender, true).thenAccept(boundAll -> {
				ChatUtil.sendBrandingMessage(sender, ChatColor.GREEN + this.copied.replace("{target}", ChatColor.YELLOW + player.getName() + ChatColor.GREEN));
				if (!boundAll) {
					ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.failedToBindAll);
				}
			});
		} else if (args.size() == 2) {
			if (!this.hasPermission(sender, "assign")) {
				ChatUtil.sendBrandingMessage(sender, super.noPermissionMessage);
				return;
			}

			final Player orig = ProjectKorra.plugin.getServer().getPlayer(args.get(0));
			final Player target = ProjectKorra.plugin.getServer().getPlayer(args.get(1));

			if (MultiAbilityManager.hasMultiAbilityBound(target)) {
				ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.cantEditBindsOther.replace("{target}", ChatColor.YELLOW + target.getName() + ChatColor.RED));
				return;
			}

			if ((orig == null || !orig.isOnline()) || (target == null || !target.isOnline())) {
				ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.playerNotFound);
				return;
			}

			this.assignAbilities(sender, orig, target, false).thenAccept(boundAll -> {
				ChatUtil.sendBrandingMessage(sender, ChatColor.GREEN + this.copiedOther.replace("{target1}", ChatColor.YELLOW + target.getName() + ChatColor.GREEN).replace("{target2}", ChatColor.YELLOW + orig.getName() + ChatColor.GREEN));
				ChatUtil.sendBrandingMessage(target, ChatColor.GREEN + this.copied.replace("{target}", ChatColor.YELLOW + orig.getName() + ChatColor.GREEN));
				if (!boundAll) {
					ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.failedToBindAll);
				}
			});
		}
	}

	private CompletableFuture<Boolean> assignAbilities(final CommandSender sender, final OfflinePlayer player, final Player player2, final boolean self) {

		BendingPlayer target = BendingPlayer.getBendingPlayer(player2);
		CompletableFuture<Boolean> future = new CompletableFuture<>();
		BendingPlayer.getOrLoadOfflineAsync(player).thenAccept(orig -> {
			if (orig.isPermaRemoved()) {
				if (self) {
					ChatUtil.sendBrandingMessage(sender, ChatColor.RED + ConfigManager.languageConfig.get().getString("Commands.Preset.BendingPermanentlyRemoved"));
				} else {
					ChatUtil.sendBrandingMessage(sender, ChatColor.RED + ConfigManager.languageConfig.get().getString("Commands.Preset.Other.BendingPermanentlyRemoved"));
				}
				future.complete(false);
			}

			final HashMap<Integer, String> abilities = (HashMap<Integer, String>) orig.getAbilities().clone();
			boolean boundAll = true;
			for (int i = 1; i <= 9; i++) {
				final CoreAbility coreAbil = CoreAbility.getAbility(abilities.get(i));
				if (coreAbil != null && !target.canBind(coreAbil)) {
					abilities.remove(i);
					boundAll = false;
				}
			}
			target.setAbilities(abilities);
			BendingBoardManager.updateAllSlots(player2);
			future.complete(boundAll);
		});


		return future;
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (!sender.hasPermission("bending.command.copy") || args.size() >= 2 || (args.size() >= 1 && !sender.hasPermission("bending.command.copy.assign"))) {
			return new ArrayList<String>(); // Return nothing.
		}
		return getOnlinePlayerNames(sender);
	}

}

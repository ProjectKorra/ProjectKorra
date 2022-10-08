package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.event.PlayerBindChangeEvent;
import com.projectkorra.projectkorra.util.ChatUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Executor for /bending clear. Extends {@link PKCommand}.
 */
public class ClearCommand extends PKCommand {

	private final String cantEditBinds;
	private final String cleared;
	private final String wrongNumber;
	private final String clearedSlot;
	private final String alreadyEmpty;

	public ClearCommand() {
		super("clear", "/bending clear [Slot]", ConfigManager.languageConfig.get().getString("Commands.Clear.Description"), new String[] { "clear", "cl", "c" });

		this.cantEditBinds = ConfigManager.languageConfig.get().getString("Commands.Clear.CantEditBinds");
		this.cleared = ConfigManager.languageConfig.get().getString("Commands.Clear.Cleared");
		this.wrongNumber = ConfigManager.languageConfig.get().getString("Commands.Clear.WrongNumber");
		this.clearedSlot = ConfigManager.languageConfig.get().getString("Commands.Clear.ClearedSlot");
		this.alreadyEmpty = ConfigManager.languageConfig.get().getString("Commands.Clear.AlreadyEmpty");
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.hasPermission(sender) || !this.correctLength(sender, args.size(), 0, 1) || !this.isPlayer(sender)) {
			return;
		} else if (MultiAbilityManager.hasMultiAbilityBound((Player) sender)) {
			ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.cantEditBinds);
			return;
		}

		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(sender.getName());
		if (args.size() == 0) {
			for (int i = 1; i <= 9; i++) {
				if (!bPlayer.getAbilities().containsKey(i)) {
					continue;
				}

				PlayerBindChangeEvent event = new PlayerBindChangeEvent(bPlayer.getPlayer(), bPlayer.getAbilities().get(i), i, false, false);
				ProjectKorra.plugin.getServer().getPluginManager().callEvent(event);
				
				if (event.isCancelled()) {
					continue;
				}

				bPlayer.getAbilities().remove(i);
				bPlayer.saveAbility(null, i);
			}
			ChatUtil.sendBrandingMessage(sender, ChatColor.YELLOW + this.cleared);
		} else if (args.size() == 1) {
			try {
				final int slot = Integer.parseInt(args.get(0));
				if (slot < 1 || slot > 9) {
					ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.wrongNumber);
				}

				if (bPlayer.getAbilities().get(slot) != null) {
					PlayerBindChangeEvent event = new PlayerBindChangeEvent(bPlayer.getPlayer(), bPlayer.getAbilities().get(slot), slot, false, false);
					ProjectKorra.plugin.getServer().getPluginManager().callEvent(event);
					
					if (event.isCancelled()) {
						return;
					}

					bPlayer.getAbilities().remove(slot);
					bPlayer.saveAbility(null, slot);
					ChatUtil.sendBrandingMessage(sender, ChatColor.YELLOW + this.clearedSlot.replace("{slot}", String.valueOf(slot)));
				} else {
					ChatUtil.sendBrandingMessage(sender, ChatColor.YELLOW + this.alreadyEmpty);
				}
			} catch (final NumberFormatException e) {
				ChatUtil.sendBrandingMessage(sender, ChatColor.RED + this.wrongNumber);
			}
		}
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (args.size() >= 1 || !sender.hasPermission("bending.command.clear")) {
			return new ArrayList<String>();
		}
		return Arrays.asList("123456789".split(""));
	}

}

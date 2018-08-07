package com.projectkorra.projectkorra.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.configuration.ConfigManager;

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
			GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.cantEditBinds);
			return;
		}

		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(sender.getName());
		if (bPlayer == null) {
			GeneralMethods.createBendingPlayer(((Player) sender).getUniqueId(), sender.getName());
			bPlayer = BendingPlayer.getBendingPlayer(sender.getName());
		}
		if (args.size() == 0) {
			bPlayer.getAbilities().clear();
			for (int i = 1; i <= 9; i++) {
				GeneralMethods.saveAbility(bPlayer, i, null);
			}
			GeneralMethods.sendBrandingMessage(sender, ChatColor.YELLOW + this.cleared);
		} else if (args.size() == 1) {
			try {
				final int slot = Integer.parseInt(args.get(0));
				if (slot < 1 || slot > 9) {
					GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.wrongNumber);
				}
				if (bPlayer.getAbilities().get(slot) != null) {
					bPlayer.getAbilities().remove(slot);
					GeneralMethods.saveAbility(bPlayer, slot, null);
					GeneralMethods.sendBrandingMessage(sender, ChatColor.YELLOW + this.clearedSlot.replace("{slot}", String.valueOf(slot)));
				} else {
					GeneralMethods.sendBrandingMessage(sender, ChatColor.YELLOW + this.alreadyEmpty);
				}
			}
			catch (final NumberFormatException e) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.wrongNumber);
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

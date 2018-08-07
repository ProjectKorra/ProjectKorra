package com.projectkorra.projectkorra.command;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.configuration.ConfigManager;

/**
 * Executor for /bending invincible. Extends {@link PKCommand}.
 */
public class InvincibleCommand extends PKCommand {

	public InvincibleCommand() {
		super("invincible", "/bending invincible", ConfigManager.languageConfig.get().getString("Commands.Invincible.Description"), new String[] { "invincible", "inv", "i" });

	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.hasPermission(sender) || !this.isPlayer(sender) || !this.correctLength(sender, args.size(), 0, 0)) {
			return;
		}

		if (!Commands.invincible.contains(sender.getName())) {
			Commands.invincible.add(sender.getName());
			GeneralMethods.sendBrandingMessage(sender, ChatColor.GREEN + ConfigManager.languageConfig.get().getString("Commands.Invincible.ToggledOn"));
		} else {
			Commands.invincible.remove(sender.getName());
			GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + ConfigManager.languageConfig.get().getString("Commands.Invincible.ToggledOff"));
		}
	}

}

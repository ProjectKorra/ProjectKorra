package com.projectkorra.projectkorra.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.projectkorra.projectkorra.configuration.ConfigManager;

import java.util.List;

/**
 * Executor for /bending invincible. Extends {@link PKCommand}.
 */
public class InvincibleCommand extends PKCommand {

	public InvincibleCommand() {
		super("invincible", "/bending invincible", ConfigManager.languageConfig.get().getString("Commands.Invincible.Description"), new String[] { "invincible", "inv" });

	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if (!hasPermission(sender) || !isPlayer(sender) || !correctLength(sender, args.size(), 0, 0)) {
			return;
		}

		if (!Commands.invincible.contains(sender.getName())) {
			Commands.invincible.add(sender.getName());
			sender.sendMessage(ChatColor.GREEN + ConfigManager.languageConfig.get().getString("Commands.Invincible.ToggledOn"));
		} else {
			Commands.invincible.remove(sender.getName());
			sender.sendMessage(ChatColor.RED + ConfigManager.languageConfig.get().getString("Commands.Invincible.ToggledOff"));
		}
	}

}

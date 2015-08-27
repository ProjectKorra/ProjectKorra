package com.projectkorra.projectkorra.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Executor for /bending invincible. Extends {@link PKCommand}.
 */
public class InvincibleCommand extends PKCommand {

	public InvincibleCommand() {
		super("invincible", "/bending invincible", "This command will make you impervious to all Bending damage. Once you use this command, you will stay invincible until you log off or use this command again.", new String[] { "invincible", "inv" });

	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if (!hasPermission(sender) || !isPlayer(sender) || !correctLength(sender, args.size(), 0, 0)) {
			return;
		}

		if (!Commands.invincible.contains(sender.getName())) {
			Commands.invincible.add(sender.getName());
			sender.sendMessage(ChatColor.GREEN + "You are now invincible to all bending damage and effects. Use this command again to disable this.");
		} else {
			Commands.invincible.remove(sender.getName());
			sender.sendMessage(ChatColor.RED + "You are no longer invincible to all bending damage and effects.");
		}
	}

}

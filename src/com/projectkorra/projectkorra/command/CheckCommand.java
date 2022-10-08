package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Executor for /bending check. Extends {@link PKCommand}.
 */
public class CheckCommand extends PKCommand {

	private final String newVersionAvailable;
	private final String curVersion;
	private final String newVersion;
	private final String upToDate;

	public CheckCommand() {
		super("check", "/bending check", ConfigManager.languageConfig.get().getString("Commands.Check.Description"), new String[] { "check", "chk" });

		this.newVersionAvailable = ConfigManager.languageConfig.get().getString("Commands.Check.NewVersionAvailable");
		this.curVersion = ConfigManager.languageConfig.get().getString("Commands.Check.CurrentVersion");
		this.newVersion = ConfigManager.languageConfig.get().getString("Commands.Check.LatestVersion");
		this.upToDate = ConfigManager.languageConfig.get().getString("Commands.Check.UpToDate");
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.hasPermission(sender)) {
			return;
		} else if (args.size() > 0) {
			this.help(sender, false);
			return;
		}
		if (!ProjectKorra.plugin.updater.isEnabled()) {
			sender.sendMessage(ChatColor.YELLOW + "The update checker has been disabled in the config. Please enable it in order to use this command.");
		} else if (ProjectKorra.plugin.updater.updateAvailable()) {
			sender.sendMessage(ChatColor.GREEN + this.newVersionAvailable.replace("ProjectKorra", ChatColor.GOLD + "ProjectKorra" + ChatColor.GREEN));
			sender.sendMessage(ChatColor.YELLOW + this.curVersion.replace("{version}", ChatColor.RED + ProjectKorra.plugin.updater.getCurrentVersion() + ChatColor.YELLOW));
			sender.sendMessage(ChatColor.YELLOW + this.newVersion.replace("{version}", ChatColor.GOLD + ProjectKorra.plugin.updater.getUpdateVersion() + ChatColor.YELLOW));
		} else {
			sender.sendMessage(ChatColor.YELLOW + this.upToDate.replace("ProjectKorra", ChatColor.GOLD + "ProjectKorra" + ChatColor.YELLOW));
		}
	}

}

package com.projectkorra.projectkorra.command;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.configuration.better.configs.commands.CheckCommandConfig;

/**
 * Executor for /bending check. Extends {@link PKCommand}.
 */
public class CheckCommand extends PKCommand<CheckCommandConfig> {

	private final String newVersionAvailable;
	private final String curVersion;
	private final String newVersion;
	private final String upToDate;

	public CheckCommand(final CheckCommandConfig config) {
		super(config, "check", "/bending check", config.Description, new String[] { "check", "chk" });

		this.newVersionAvailable = config.NewVersionAvailable;
		this.curVersion = config.CurrentVersion;
		this.newVersion = config.LatestVersion;
		this.upToDate = config.UpToDate;
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

package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.commands.PresetCommandConfig;
import com.projectkorra.projectkorra.configuration.configs.properties.CommandPropertiesConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Executor for /bending preset. Extends {@link PKCommand}.
 */
public class PresetCommand extends PKCommand<PresetCommandConfig> {

	private static final String[] createaliases = { "create", "c", "save" };
	private static final String[] deletealiases = { "delete", "d", "del" };
	private static final String[] listaliases = { "list", "l" };
	private static final String[] bindaliases = { "bind", "b" };

	private final String noPresets;
	private final String noPresetName;
	private final String deletePreset;
	private final String noPresetNameExternal;
	private final String bendingRemoved;
	private final String bound;
	private final String failedToBindAll;
	private final String bendingRemovedOther;
	private final String boundOtherConfirm;
	private final String succesfullyCopied;
	private final String reachedMax;
	private final String alreadyExists;
	private final String createdNewPreset;
	private final String cantEditBinds;

	public PresetCommand(final PresetCommandConfig config) {
		super(config, "preset", "/bending preset <Bind/Create/Delete/List> [Preset]", config.Description, new String[] { "preset", "presets", "pre", "set", "p" });

		this.noPresets = config.NoPresets;
		this.noPresetName = config.NoPresetName;
		this.deletePreset = config.Delete;
		this.noPresetNameExternal = config.NoPresetName_External;
		this.bendingRemoved = ConfigManager.getConfig(CommandPropertiesConfig.class).BendingPermanentlyRemoved;
		this.bound = config.SuccessfullyBound;
		this.failedToBindAll = config.FailedToBindAll;
		this.bendingRemovedOther = ConfigManager.getConfig(CommandPropertiesConfig.class).BendingPermanentlyRemoved_Other;
		this.boundOtherConfirm = config.SuccessfullyBound_Other;
		this.succesfullyCopied = config.SuccessfullyCopied;
		this.reachedMax = config.MaxPresets;
		this.alreadyExists = config.AlreadyExists;
		this.createdNewPreset = config.Created;
		this.cantEditBinds = config.CantEditBinds;
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.isPlayer(sender) || !this.correctLength(sender, args.size(), 1, 3)) {
			return;
		}
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (args.size() >= 3 || !sender.hasPermission("bending.command.preset") || !(sender instanceof Player)) {
			return new ArrayList<String>();
		}
		final List<String> l = new ArrayList<String>();
		if (args.size() == 0) {
			l.add("create");
			l.add("delete");
			l.add("list");
			l.add("bind");
			return l;
		} else if (args.size() == 2 && Arrays.asList(new String[] { "delete", "d", "del", "bind", "b" }).contains(args.get(0).toLowerCase())) {

		}
		return l;
	}
}

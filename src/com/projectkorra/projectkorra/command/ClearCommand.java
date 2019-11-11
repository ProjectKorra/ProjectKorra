package com.projectkorra.projectkorra.command;

import com.google.common.primitives.Ints;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.bind.AbilityBindManager;
import com.projectkorra.projectkorra.configuration.configs.commands.ClearCommandConfig;
import com.projectkorra.projectkorra.player.BendingPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Executor for /bending clear. Extends {@link PKCommand}.
 */
public class ClearCommand extends PKCommand<ClearCommandConfig> {

	private final String cantEditBinds;
	private final String cleared;
	private final String wrongNumber;
	private final String clearedSlot;
	private final String alreadyEmpty;

	public ClearCommand(final ClearCommandConfig config) {
		super(config, "clear", "/bending clear [Slot]", config.Description, new String[] { "clear", "cl", "c" });

		this.cantEditBinds = config.CantEditBinds;
		this.cleared = config.Cleared;
		this.wrongNumber = config.WrongNumber;
		this.clearedSlot = config.ClearedSlot;
		this.alreadyEmpty = config.AlreadyEmpty;
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.hasPermission(sender) || !this.correctLength(sender, args.size(), 0, 1) || !this.isPlayer(sender)) {
			return;
		}

		Player player = (Player) sender;

		if (args.isEmpty()) {
			if (this.abilityBindManager.clearAbilities(player) == AbilityBindManager.Result.SUCCESS) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.YELLOW + this.cleared);
			}

			return;
		}

		Integer slot = Ints.tryParse(args.get(0));

		if (slot == null || slot < 1 || slot > 9) {
			GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.wrongNumber);
			return;
		}

		slot =- 1;

		AbilityBindManager.Result result = this.abilityBindManager.unbindAbility(player, slot);

		switch (result) {
			case SUCCESS:
				GeneralMethods.sendBrandingMessage(sender, ChatColor.YELLOW + this.clearedSlot.replace("{slot}", String.valueOf(slot)));
				break;
			case ALREADY_EMPTY:
				GeneralMethods.sendBrandingMessage(sender, ChatColor.YELLOW + this.alreadyEmpty);
				break;
		}
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (args.size() >= 1 || !sender.hasPermission("bending.command.clear")) {
			return new ArrayList<>();
		}
		return Arrays.asList("123456789".split(""));
	}

}

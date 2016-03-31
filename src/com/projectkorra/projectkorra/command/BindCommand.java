package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Executor for /bending bind. Extends {@link PKCommand}.
 */
public class BindCommand extends PKCommand {

	private String abilityDoesntExist;
	private String wrongNumber;
	private String loadingInfo;
	private String toggledElementOff;

	public BindCommand() {
		super("bind", "/bending bind <Ability> [Slot]", ConfigManager.languageConfig.get().getString("Commands.Bind.Description"), new String[]{ "bind", "b" });
		
		this.abilityDoesntExist = ConfigManager.languageConfig.get().getString("Commands.Bind.AbilityDoesntExist");
		this.wrongNumber = ConfigManager.languageConfig.get().getString("Commands.Bind.WrongNumber");
		this.loadingInfo = ConfigManager.languageConfig.get().getString("Commands.Bind.LoadingInfo");
		this.toggledElementOff = ConfigManager.languageConfig.get().getString("Commands.Bind.ToggledElementOff");
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if (!hasPermission(sender) || !correctLength(sender, args.size(), 1, 2) || !isPlayer(sender)) {
			return;
		}

		CoreAbility coreAbil = CoreAbility.getAbility(args.get(0));
		if (coreAbil == null || coreAbil.isHiddenAbility()) {
			sender.sendMessage(ChatColor.RED + abilityDoesntExist);
			return;
		}
		
		// bending bind [Ability]
		if (args.size() == 1) {
			bind(sender, args.get(0), ((Player) sender).getInventory().getHeldItemSlot()+1);
		}

		// bending bind [ability] [#]
		if (args.size() == 2) {
			bind(sender, args.get(0), Integer.parseInt(args.get(1)));
		}
	}

	private void bind(CommandSender sender, String ability, int slot) {
		if (!(sender instanceof Player)) {
			return;
		} else if (slot < 1 || slot > 9) {
			sender.sendMessage(ChatColor.RED + wrongNumber);
			return;
		}

		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer((Player) sender);
		CoreAbility coreAbil = CoreAbility.getAbility(ability);
		if (bPlayer == null) {
			sender.sendMessage(ChatColor.RED + loadingInfo);
			return;
		} else if (coreAbil == null || !bPlayer.canBind(coreAbil)) {
			sender.sendMessage(ChatColor.RED + super.noPermissionMessage);
			return;
		} else if (!bPlayer.isElementToggled(coreAbil.getElement())) {
			sender.sendMessage(ChatColor.RED + toggledElementOff);
		}
		
		String name = coreAbil != null ? coreAbil.getName() : null;
		GeneralMethods.bindAbility((Player) sender, name, slot);
	}
}

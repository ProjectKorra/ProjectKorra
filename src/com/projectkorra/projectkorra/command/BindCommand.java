package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Executor for /bending bind. Extends {@link PKCommand}.
 */
public class BindCommand extends PKCommand {

	public BindCommand() {
		super("bind", "/bending bind [Ability] <#>", "This command will bind an ability to the slot you specify (if you specify one), or the slot currently selected in your hotbar (If you do not specify a Slot #).", new String[]{ "bind", "b" });
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if (!hasPermission(sender) || !correctLength(sender, args.size(), 1, 2) || !isPlayer(sender)) {
			return;
		}

		CoreAbility coreAbil = CoreAbility.getAbility(args.get(0));
		if (coreAbil == null || coreAbil.isHiddenAbility()) {
			sender.sendMessage(ChatColor.RED + "That ability doesn't exist.");
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
			sender.sendMessage(ChatColor.RED + "Slot must be an integer between 1 and 9.");
			return;
		}

		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer((Player) sender);
		CoreAbility coreAbil = CoreAbility.getAbility(ability);
		if (bPlayer == null) {
			sender.sendMessage(ChatColor.RED + "Please wait one moment while we load your bending information.");
			return;
		} else if (coreAbil == null || !bPlayer.canBind(coreAbil)) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to bend this ability.");
			return;
		} else if (!bPlayer.isElementToggled(coreAbil.getElement())) {
			sender.sendMessage(ChatColor.RED + "You have that ability's element toggled off currently.");
		}
		
		String name = coreAbil != null ? coreAbil.getName() : null;
		GeneralMethods.bindAbility((Player) sender, name, slot);
	}
}

package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Executor for /bending bind. Extends {@link PKCommand}.
 */
public class BindCommand extends PKCommand {

	private String abilityDoesntExist;
	private String wrongNumber;
	private String loadingInfo;
	private String toggledElementOff;
	private String noElement;
	private String noElementAE;
	private String noSubElement;

	public BindCommand() {
		super("bind", "/bending bind <Ability> [Slot]", ConfigManager.languageConfig.get().getString("Commands.Bind.Description"), new String[] { "bind", "b" });

		this.abilityDoesntExist = ConfigManager.languageConfig.get().getString("Commands.Bind.AbilityDoesntExist");
		this.wrongNumber = ConfigManager.languageConfig.get().getString("Commands.Bind.WrongNumber");
		this.loadingInfo = ConfigManager.languageConfig.get().getString("Commands.Bind.LoadingInfo");
		this.toggledElementOff = ConfigManager.languageConfig.get().getString("Commands.Bind.ElementToggledOff");
		this.noElement = ConfigManager.languageConfig.get().getString("Commands.Bind.NoElement");
		this.noElementAE = ConfigManager.languageConfig.get().getString("Commands.Bind.NoElementAE");
		this.noSubElement = ConfigManager.languageConfig.get().getString("Commands.Bind.NoSubElement");
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if (!hasPermission(sender) || !correctLength(sender, args.size(), 1, 2) || !isPlayer(sender)) {
			return;
		}

		CoreAbility coreAbil = CoreAbility.getAbility(args.get(0));
		if (coreAbil == null || coreAbil.isHiddenAbility() || !coreAbil.isEnabled()) {
			GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + abilityDoesntExist.replace("{ability}", args.get(0)));
			return;
		}

		// bending bind [Ability]
		if (args.size() == 1) {
			bind(sender, args.get(0), ((Player) sender).getInventory().getHeldItemSlot() + 1);
		}

		// bending bind [ability] [#]
		if (args.size() == 2) {
			try {
				bind(sender, args.get(0), Integer.parseInt(args.get(1)));
			}
			catch (NumberFormatException ex) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + wrongNumber);
			}
		}
	}

	private void bind(CommandSender sender, String ability, int slot) {
		if (!(sender instanceof Player)) {
			return;
		} else if (slot < 1 || slot > 9) {
			GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + wrongNumber);
			return;
		}

		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer((Player) sender);
		CoreAbility coreAbil = CoreAbility.getAbility(ability);
		if (bPlayer == null) {
			GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + loadingInfo);
			return;
		} else if (coreAbil == null || !bPlayer.canBind(coreAbil)) {
			if (coreAbil != null && coreAbil.getElement() != Element.AVATAR && !bPlayer.hasElement(coreAbil.getElement())) {
				if (coreAbil.getElement() instanceof SubElement) {
					SubElement sub = (SubElement) coreAbil.getElement();
					if (!bPlayer.hasElement(sub.getParentElement())) {
						GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + ("AEIOUaeiou".indexOf(sub.getParentElement().getName().charAt(0)) > -1 ? this.noElementAE : this.noElement).replace("{element}", sub.getParentElement().getName() + sub.getParentElement().getType().getBender()));
					} else {
						GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.noSubElement.replace("{subelement}", coreAbil.getElement().getName() + coreAbil.getElement().getType().getBending()));
					}
				} else {
					GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + ("AEIOUaeiou".indexOf(coreAbil.getElement().getName().charAt(0)) > -1 ? this.noElementAE : this.noElement).replace("{element}", coreAbil.getElement().getName() + coreAbil.getElement().getType().getBender()));
				}
			} else {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + super.noPermissionMessage);
			}
			return;
		} else if (!bPlayer.isElementToggled(coreAbil.getElement())) {
			GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + toggledElementOff);
		}

		String name = coreAbil != null ? coreAbil.getName() : null;
		GeneralMethods.bindAbility((Player) sender, name, slot);
	}

	@Override
	protected List<String> getTabCompletion(CommandSender sender, List<String> args) {
		if (args.size() >= 2 || !sender.hasPermission("bending.command.bind") || !(sender instanceof Player))
			return new ArrayList<String>();

		List<String> abilities = new ArrayList<String>();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(sender.getName());
		if (args.size() == 0) {
			if (bPlayer != null) {
				for (CoreAbility coreAbil : CoreAbility.getAbilities()) {
					if (!coreAbil.isHiddenAbility() && bPlayer.canBind(coreAbil)) {
						abilities.add(coreAbil.getName());
					}
				}
			}
		} else {
			abilities = Arrays.asList("123456789".split(""));
		}

		Collections.sort(abilities);
		return abilities;
	}
}

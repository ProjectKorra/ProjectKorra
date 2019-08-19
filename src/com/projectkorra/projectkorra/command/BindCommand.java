package com.projectkorra.projectkorra.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.configuration.configs.commands.BindCommandConfig;

/**
 * Executor for /bending bind. Extends {@link PKCommand}.
 */
@SuppressWarnings("rawtypes")
public class BindCommand extends PKCommand<BindCommandConfig> {

	private final String abilityDoesntExist;
	private final String wrongNumber;
	private final String loadingInfo;
	private final String toggledElementOff;
	private final String noElement;
	private final String noElementVowel;
	private final String noSubElement;
	private final String noSubElementVowel;
	private final String unbindable;

	public BindCommand(final BindCommandConfig config) {
		super(config, "bind", "/bending bind <Ability> [Slot]", config.Description, new String[] { "bind", "b" });

		this.abilityDoesntExist = config.AbilityDoesntExistMessage;
		this.wrongNumber = config.WrongNumberMessage;
		this.loadingInfo = config.LoadingInfoMessage;
		this.toggledElementOff = config.ElementToggledOffMessage;
		this.noElement = config.NoElementMessage;
		this.noElementVowel = config.NoElementMessageVowel;
		this.noSubElement = config.NoSubElementMessage;
		this.noSubElementVowel = config.NoSubElementMessageVowel;
		this.unbindable = config.UnbindableMessage;
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.hasPermission(sender) || !this.correctLength(sender, args.size(), 1, 2) || !this.isPlayer(sender)) {
			return;
		}

		final CoreAbility coreAbil = CoreAbility.getAbility(args.get(0));
		if (coreAbil == null || !coreAbil.isEnabled()) {
			GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.abilityDoesntExist.replace("{ability}", args.get(0)));
			return;
		} else if (coreAbil instanceof PassiveAbility || coreAbil instanceof ComboAbility || coreAbil.isHiddenAbility()) {
			GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.unbindable.replace("{ability}", args.get(0)));
			return;
		}

		// bending bind [Ability].
		if (args.size() == 1) {
			this.bind(sender, args.get(0), ((Player) sender).getInventory().getHeldItemSlot() + 1);
		}

		// bending bind [ability] [#].
		if (args.size() == 2) {
			try {
				this.bind(sender, args.get(0), Integer.parseInt(args.get(1)));
			} catch (final NumberFormatException ex) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.wrongNumber);
			}
		}
	}

	private void bind(final CommandSender sender, final String ability, final int slot) {
		if (!(sender instanceof Player)) {
			return;
		} else if (slot < 1 || slot > 9) {
			GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.wrongNumber);
			return;
		}

		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer((Player) sender);
		final CoreAbility coreAbil = CoreAbility.getAbility(ability);
		if (bPlayer == null) {
			GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.loadingInfo);
			return;
		} else if (coreAbil == null || !bPlayer.canBind(coreAbil)) {
			if (coreAbil != null && coreAbil.getElement() != Element.AVATAR && !bPlayer.hasElement(coreAbil.getElement())) {
				if (coreAbil.getElement() instanceof SubElement) {
					final SubElement sub = (SubElement) coreAbil.getElement();
					if (!bPlayer.hasElement(sub.getParentElement())) {
						if (GeneralMethods.isVowel(ChatColor.stripColor(sub.getParentElement().getName()).charAt(0))) {
							GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.noElementVowel.replace("{element}", sub.getParentElement().getName() + sub.getParentElement().getType().getBender()));
						} else {
							GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.noElement.replace("{element}", sub.getParentElement().getName() + sub.getParentElement().getType().getBender()));
						}
					} else {
						if (GeneralMethods.isVowel(ChatColor.stripColor(sub.getName()).charAt(0))) {
							GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.noSubElementVowel.replace("{subelement}", coreAbil.getElement().getName() + coreAbil.getElement().getType().getBending()));
						} else {
							GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.noSubElement.replace("{subelement}", coreAbil.getElement().getName() + coreAbil.getElement().getType().getBending()));
						}
					}
				} else {
					if (GeneralMethods.isVowel(ChatColor.stripColor(coreAbil.getElement().getName()).charAt(0))) {
						GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.noElementVowel.replace("{element}", coreAbil.getElement().getName() + coreAbil.getElement().getType().getBender()));
					} else {
						GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.noElement.replace("{element}", coreAbil.getElement().getName() + coreAbil.getElement().getType().getBender()));
					}
				}
			} else {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + super.noPermissionMessage);
			}
			return;
		} else if (!bPlayer.isElementToggled(coreAbil.getElement())) {
			GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.toggledElementOff);
		}

		final String name = coreAbil != null ? coreAbil.getName() : null;
		GeneralMethods.bindAbility((Player) sender, name, slot - 1);
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (args.size() >= 2 || !sender.hasPermission("bending.command.bind") || !(sender instanceof Player)) {
			return new ArrayList<String>();
		}

		List<String> abilities = new ArrayList<>();
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(sender.getName());
		if (args.size() == 0) {
			if (bPlayer != null) {
				for (final CoreAbility coreAbil : CoreAbility.getAbilities()) {
					if (!coreAbil.isHiddenAbility() && bPlayer.canBind(coreAbil) && !(coreAbil instanceof PassiveAbility || coreAbil instanceof ComboAbility) && !abilities.contains(coreAbil.getName())) {
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

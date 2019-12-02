package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AbilityHandler;
import com.projectkorra.projectkorra.ability.api.ComboAbility;
import com.projectkorra.projectkorra.ability.api.PassiveAbility;
import com.projectkorra.projectkorra.ability.bind.AbilityBindManager;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.commands.BindCommandConfig;
import com.projectkorra.projectkorra.element.Element;
import com.projectkorra.projectkorra.element.SubElement;
import com.projectkorra.projectkorra.player.BendingPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

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

		String abilityName = args.get(0);
		AbilityHandler abilityHandler = this.abilityHandlerManager.getHandler(abilityName);

		if (abilityHandler == null) {
			GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.abilityDoesntExist.replace("{ability}", args.get(0)));
			return;
		}

		//		} else if (coreAbil instanceof PassiveAbility || coreAbil instanceof ComboAbility || coreAbil.isHiddenAbility()) {
		//			GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.unbindable.replace("{ability}", args.get(0)));
		//			return;
		//		}

		// bending bind [Ability].
		if (args.size() == 1) {
			this.bind(sender, abilityHandler, ((Player) sender).getInventory().getHeldItemSlot() + 1);
		}

		// bending bind [ability] [#].
		if (args.size() == 2) {
			try {
				this.bind(sender, abilityHandler, Integer.parseInt(args.get(1)));
			} catch (final NumberFormatException ex) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.wrongNumber);
			}
		}
	}

	private void bind(final CommandSender sender, final AbilityHandler abilityHandler, final int slot) {
		if (!(sender instanceof Player)) {
			return;
		} else if (slot < 1 || slot > 9) {
			GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.wrongNumber);
			return;
		}

		Player player = (Player) sender;
		BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);

		Element element = abilityHandler.getElement();

		//		if (bPlayer == null) {
		//			GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.loadingInfo);
		//			return;
		//		}

		if (bendingPlayer.canBind(abilityHandler)) {
			if (!bendingPlayer.isElementToggled(element)) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.toggledElementOff);
			}

			if (this.abilityBindManager.bindAbility(player, abilityHandler.getName(), slot) == AbilityBindManager.Result.SUCCESS) {
				GeneralMethods.sendBrandingMessage(player, element.getColor() + ConfigManager.getConfig(BindCommandConfig.class).SuccessfullyBoundMessage.replace("{ability}", abilityHandler.getName()).replace("{slot}", String.valueOf(slot + 1)));
			}
			return;
		}

		if (element.equals(this.elementManager.getAvatar()) || bendingPlayer.hasElement(element)) {
			GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + super.noPermissionMessage);
			return;
		}

		if (!(element instanceof SubElement)) {
			if (GeneralMethods.isVowel(ChatColor.stripColor(element.getName()).charAt(0))) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.noElementVowel.replace("{element}", element.getName() + element.getType().getBender()));
			} else {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.noElement.replace("{element}", element.getName() + element.getType().getBender()));
			}

			return;
		}

		SubElement subElement = (SubElement) element;

		if (bendingPlayer.hasElement(subElement.getParent())) {
			if (GeneralMethods.isVowel(ChatColor.stripColor(subElement.getName()).charAt(0))) {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.noSubElementVowel.replace("{subelement}", subElement.getName() + subElement.getType().getBending()));
			} else {
				GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.noSubElement.replace("{subelement}", subElement.getName() + subElement.getType().getBending()));
			}

			return;
		}

		if (GeneralMethods.isVowel(ChatColor.stripColor(subElement.getParent().getName()).charAt(0))) {
			GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.noElementVowel.replace("{element}", subElement.getParent().getName() + subElement.getParent().getType().getBender()));
		} else {
			GeneralMethods.sendBrandingMessage(sender, ChatColor.RED + this.noElement.replace("{element}", subElement.getParent().getName() + subElement.getParent().getType().getBender()));
		}
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (args.size() >= 2 || !sender.hasPermission("bending.command.bind") || !(sender instanceof Player)) {
			return new ArrayList<String>();
		}

		Player player = (Player) sender;
		BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);

		if (args.size() > 0) {
			return Arrays.asList("123456789".split(""));
		}

		Set<String> abilitySet = new HashSet<>();

		for (AbilityHandler abilityHandler : this.abilityHandlerManager.getHandlers()) {
			if (!abilityHandler.isHidden() && bendingPlayer.canBind(abilityHandler) && !(abilityHandler instanceof PassiveAbility || abilityHandler instanceof ComboAbility && !abilitySet.contains(abilityHandler.getName()))) {
				abilitySet.add(abilityHandler.getName());
			}
		}

		List<String> abilityList = new ArrayList<>(abilitySet);
		Collections.sort(abilityList);

		return abilityList;
	}
}

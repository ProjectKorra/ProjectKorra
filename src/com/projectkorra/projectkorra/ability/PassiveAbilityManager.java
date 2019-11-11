package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.ability.api.PassiveAbility;
import com.projectkorra.projectkorra.element.Element;
import com.projectkorra.projectkorra.element.SubElement;
import com.projectkorra.projectkorra.module.Module;
import com.projectkorra.projectkorra.module.ModuleManager;
import com.projectkorra.projectkorra.player.BendingPlayer;
import com.projectkorra.projectkorra.player.BendingPlayerManager;
import com.projectkorra.projectkorra.util.MultiKeyMap;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PassiveAbilityManager extends Module {

	private final BendingPlayerManager bendingPlayerManager;
	private final AbilityManager abilityManager;

	private final MultiKeyMap<String, AbilityHandler> handlerMap = new MultiKeyMap<>();

	private PassiveAbilityManager() {
		super("Passive Ability");

		this.bendingPlayerManager = ModuleManager.getModule(BendingPlayerManager.class);
		this.abilityManager = ModuleManager.getModule(AbilityManager.class);
	}

	public void registerAbility(AbilityHandler abilityHandler) {
		this.handlerMap.put(abilityHandler.getName(), abilityHandler);
	}

	public void registerPassives(Player player) {
		this.handlerMap.values().forEach(abilityHandler -> {
			if (!canUsePassive(player, abilityHandler)) {
				return;
			}

			if (this.abilityManager.hasAbility(player, abilityHandler.getAbility())) {
				return;
			}

			if (!((PassiveAbility) abilityHandler).isInstantiable()) {
				return;
			}

			Ability ability = abilityHandler.newInstance(player);
			ability.start();
		});
	}

	public boolean canUsePassive(Player player, AbilityHandler abilityHandler) {
		BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);
//		AbilityHandler abilityHandler = this.handlerMap.get(handlerClass);

//		if (abilityHandler == null) {
//			return false;
//		}

		Element element = abilityHandler.getElement();

		if (element instanceof SubElement) {
			element = ((SubElement) element).getParent();
		}

		//		if (!bendingPlayer.canBendPassive(abilityClass)) {
		//			return false;
		//		}

		if (!bendingPlayer.isToggled()) {
			return false;
		}

		if (!bendingPlayer.isElementToggled(element)) {
			return false;
		}

		return true;
	}

	public AbilityHandler getHandler(String abbilityName) {
		return this.handlerMap.get(abbilityName);
	}

	public AbilityHandler getHandler(Class<? extends AbilityHandler> handlerClass) {
		return this.handlerMap.get(handlerClass);
	}

	public List<AbilityHandler> getPassives(Element element) {
		List<AbilityHandler> handlerList = new ArrayList<>();

		this.handlerMap.values().forEach(abilityHandler -> {

			Element passiveElement = abilityHandler.getElement();

			if (passiveElement instanceof SubElement) {
				passiveElement = ((SubElement) passiveElement).getParent();
			}

			if (passiveElement.equals(element)) {
				handlerList.add(abilityHandler);
			}
		});

		return handlerList;
	}
}

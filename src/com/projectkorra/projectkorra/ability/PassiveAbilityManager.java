package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.ability.loader.PassiveAbilityLoader;
import com.projectkorra.projectkorra.element.Element;
import com.projectkorra.projectkorra.element.SubElement;
import com.projectkorra.projectkorra.module.Module;
import com.projectkorra.projectkorra.module.ModuleManager;
import com.projectkorra.projectkorra.player.BendingPlayer;
import com.projectkorra.projectkorra.player.BendingPlayerManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PassiveAbilityManager extends Module {

	private final BendingPlayerManager bendingPlayerManager;
	private final AbilityManager abilityManager;

	private final Map<Class<? extends Ability>, PassiveAbilityLoader> abilities = new HashMap<>();

	private PassiveAbilityManager() {
		super("Passive Ability");

		this.bendingPlayerManager = ModuleManager.getModule(BendingPlayerManager.class);
		this.abilityManager = ModuleManager.getModule(AbilityManager.class);
	}

	public void registerAbility(Class<? extends Ability> abilityClass, PassiveAbilityLoader passiveAbilityLoader) {
		this.abilities.put(abilityClass, passiveAbilityLoader);
	}

	public void registerPassives(Player player) {
		this.abilities.forEach((abilityClass, passiveAbilityLoader) -> {
			if (!canUsePassive(player, abilityClass)) {
				return;
			}

			if (this.abilityManager.hasAbility(player, abilityClass)) {
				return;
			}

			if (!passiveAbilityLoader.isInstantiable()) {
				return;
			}

			Ability ability = this.abilityManager.createAbility(player, abilityClass);
			ability.start();
		});
	}

	private boolean canUsePassive(Player player, Class<? extends Ability> abilityClass) {
		BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);
		PassiveAbilityLoader passiveAbilityLoader = this.abilities.get(abilityClass);

		if (passiveAbilityLoader == null) {
			return false;
		}

		Element element = passiveAbilityLoader.getElement();

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

	public List<Class<? extends Ability>> getPassivesForElement(Element element) {
		List<Class<? extends Ability>> abilities = new ArrayList<>();

		this.abilities.forEach((abilityClass, passiveAbilityLoader) -> {

			Element passiveElement = passiveAbilityLoader.getElement();

			if (passiveElement instanceof SubElement) {
				passiveElement = ((SubElement) passiveElement).getParent();
			}

			if (passiveElement.equals(element)) {
				abilities.add(abilityClass);
			}
		});

		return abilities;
	}
}

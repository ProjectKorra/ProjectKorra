package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.api.MultiAbility;
import com.projectkorra.projectkorra.ability.api.PlayerBindChangeEvent;
import com.projectkorra.projectkorra.ability.bind.AbilityBindManager;
import com.projectkorra.projectkorra.event.AbilityEndEvent;
import com.projectkorra.projectkorra.event.PlayerSwingEvent;
import com.projectkorra.projectkorra.module.Module;
import com.projectkorra.projectkorra.module.ModuleManager;
import com.projectkorra.projectkorra.player.BendingPlayer;
import com.projectkorra.projectkorra.player.BendingPlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemHeldEvent;

import java.util.*;
import java.util.stream.Collectors;

public class MultiAbilityManager extends Module {

	private final BendingPlayerManager bendingPlayerManager;
	private final AbilityManager abilityManager;
	private final AbilityBindManager abilityBindManager;

//	private final Map<String, MultiAbility> abilities = new HashMap<>();
//	private final Map<String, Class<? extends Ability>> multiAbilities = new HashMap<>();

	private final Map<String, MultiAbilityInfo> multiAbilityMap = new HashMap<>();
	private final Map<String, AbilityHandler> handlerMap = new HashMap<>();

	private final Map<UUID, Class<? extends Ability>> playerMultiAbility = new HashMap<>();
	private final Map<UUID, String[]> playerAbilities = new HashMap<>();

	private MultiAbilityManager() {
		super("Multi Ability");

		this.bendingPlayerManager = ModuleManager.getModule(BendingPlayerManager.class);
		this.abilityManager = ModuleManager.getModule(AbilityManager.class);
		this.abilityBindManager = ModuleManager.getModule(AbilityBindManager.class);
	}

	public void registerAbility(AbilityHandler abilityHandler) {
		MultiAbility multiAbility = (MultiAbility) abilityHandler;

		Map<String, AbilityHandler> handlerMap = multiAbility.getAbilities().stream().collect(Collectors.toMap(AbilityHandler::getName, h -> h));

		MultiAbilityInfo multiAbilityInfo = new MultiAbilityInfo(abilityHandler, new ArrayList<>(handlerMap.keySet()));

		this.multiAbilityMap.put(abilityHandler.getName(), multiAbilityInfo);
		this.handlerMap.putAll(handlerMap);
	}

	@EventHandler
	public void onSwing(PlayerSwingEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bendingPlayer = event.getBendingPlayer();

		String abilityName = event.getAbilityName();
		MultiAbilityInfo multiAbilityInfo = this.multiAbilityMap.get(abilityName);

		if (multiAbilityInfo == null) {
			return;
		}

		this.playerMultiAbility.put(player.getUniqueId(), multiAbilityInfo.abilityHandler.getAbility());
		this.playerAbilities.put(player.getUniqueId(), bendingPlayer.getAbilities());

		multiAbilityInfo.abilityHandler.newInstance(player);

		// TODO Allow AbilityBindManager to create 'temp' abilities which are not stored
		for (int slot = 0; slot < multiAbilityInfo.abilities.size(); slot++) {
			String multiAbility = multiAbilityInfo.abilities.get(slot);

			if (!player.hasPermission("bending.ability." + multiAbilityInfo.abilityHandler.getName() + "." + multiAbility)) {
				continue;
			}

			this.abilityBindManager.bindAbility(player, multiAbility, slot, false);
		}

//		String[] abilities = multiAbilityInfo.abilities.stream()
//				.filter(name -> player.hasPermission("bending.ability." + multiAbilityInfo.abilityHandler.getName() + "." + name))
//				.toArray(String[]::new);

//		bendingPlayer.setAbilities(abilities);
		player.getInventory().setHeldItemSlot(0);
	}

	@EventHandler
	public void onMultiAbilitySwing(PlayerSwingEvent event) {
		Player player = event.getPlayer();

		String abilityName = event.getAbilityName();
		AbilityHandler abilityHandler = this.handlerMap.get(abilityName);

		if (abilityHandler == null) {
			return;
		}

		abilityHandler.newInstance(player);
	}

	@EventHandler
	public void onAbilityEnd(AbilityEndEvent event) {
		Ability ability = event.getAbility();

		Player player = ability.getPlayer();

		Class<? extends Ability> multiAbility = this.playerMultiAbility.get(player.getUniqueId());

		if (multiAbility == null || !ability.getClass().equals(multiAbility)) {
			return;
		}

		this.playerMultiAbility.remove(player.getUniqueId());
		String[] abilities = this.playerAbilities.remove(player.getUniqueId());

		this.abilityBindManager.setAbilities(player, abilities);
	}

	@EventHandler
	public void onSlotChange(PlayerItemHeldEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);

		if (!this.playerAbilities.containsKey(player.getUniqueId())) {
			return;
		}

		int abilities = bendingPlayer.getAbilities().length;

		if (event.getNewSlot() < abilities) {
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerBindAbility(PlayerBindChangeEvent event) {
		if (!this.playerAbilities.containsKey(event.getPlayer().getUniqueId())) {
			return;
		}

		event.setCancelled(true);
		GeneralMethods.sendBrandingMessage(event.getPlayer(), ChatColor.RED + "You can't edit your binds right now!");
	}

	public class MultiAbilityInfo {
		private final AbilityHandler abilityHandler;
		private final List<String> abilities;

		MultiAbilityInfo(AbilityHandler abilityHandler, List<String> abilities) {
			this.abilityHandler = abilityHandler;
			this.abilities = abilities;
		}
	}
}

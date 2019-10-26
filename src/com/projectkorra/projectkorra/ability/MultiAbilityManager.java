package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.ability.api.PlayerBindAbilityEvent;
import com.projectkorra.projectkorra.ability.loader.MultiAbilityLoader;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class MultiAbilityManager extends Module {

	private final BendingPlayerManager bendingPlayerManager;
	private final AbilityManager abilityManager;

	private final Map<String, MultiAbility> abilities = new HashMap<>();

	private final Map<UUID, Class<? extends Ability>> playerMultiAbility = new HashMap<>();
	private final Map<UUID, List<String>> playerAbilities = new HashMap<>();

	private MultiAbilityManager() {
		super("Multi Ability");

		this.bendingPlayerManager = ModuleManager.getModule(BendingPlayerManager.class);
		this.abilityManager = ModuleManager.getModule(AbilityManager.class);
	}

	public void registerAbility(Class<? extends Ability> abilityClass, AbilityData abilityData, MultiAbilityLoader multiAbilityLoader) {
		List<String> abilities = multiAbilityLoader.getAbilities().stream()
				.map(ability -> ability.getDeclaredAnnotation(AbilityData.class))
				.map(AbilityData::name)
				.collect(Collectors.toList());

		// TODO Exception handling for multi abilities with missing AbilityData annotations

		MultiAbility multiAbility = new MultiAbility(abilityClass, abilityData.name(), abilities);

		this.abilities.put(abilityData.name(), multiAbility);
	}

	@EventHandler
	public void onSwing(PlayerSwingEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bendingPlayer = event.getBendingPlayer();

		String abilityName = event.getAbilityName();
		MultiAbility multiAbility = this.abilities.get(abilityName);

		if (multiAbility == null) {
			return;
		}

		this.playerMultiAbility.put(player.getUniqueId(), multiAbility.abilityClass);
		this.playerAbilities.put(player.getUniqueId(), bendingPlayer.getAbilities());

		Ability ability = this.abilityManager.createAbility(player, multiAbility.abilityClass);

		String[] abilities = multiAbility.abilities.stream()
				.filter(name -> player.hasPermission("bending.ability." + multiAbility.abilityName + "." + name))
				.toArray(String[]::new);

		bendingPlayer.setAbilities(abilities);
		player.getInventory().setHeldItemSlot(0);
	}

	@EventHandler
	public void onAbilityEnd(AbilityEndEvent event) {
		Ability ability = event.getAbility();

		Player player = ability.getPlayer();
		BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);

		Class<? extends Ability> multiAbility = this.playerMultiAbility.get(player.getUniqueId());

		if (multiAbility == null || !ability.getClass().equals(multiAbility)) {
			return;
		}

		this.playerMultiAbility.remove(player.getUniqueId());
		List<String> abilities = this.playerAbilities.remove(player.getUniqueId());

		bendingPlayer.setAbilities(abilities.toArray(new String[0]));
	}

	@EventHandler
	public void onSlotChange(PlayerItemHeldEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);

		if (!this.playerAbilities.containsKey(player.getUniqueId())) {
			return;
		}

		int abilities = bendingPlayer.getAbilities().size();

		if (event.getNewSlot() < abilities) {
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerBindAbility(PlayerBindAbilityEvent event) {
		if (!this.playerAbilities.containsKey(event.getPlayer().getUniqueId())) {
			return;
		}

		event.setCancelled(true);
		event.setCancelMessage(ChatColor.RED + "You can't edit your binds right now!");
	}

	public class MultiAbility {
		private final Class<? extends Ability> abilityClass;
		private final String abilityName;
		private final List<String> abilities;

		MultiAbility(Class<? extends Ability> abilityClass, String abilityName, List<String> abilities) {
			this.abilityClass = abilityClass;
			this.abilityName = abilityName;
			this.abilities = abilities;
		}
	}
}

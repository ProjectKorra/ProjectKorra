package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.api.PlayerBindChangeEvent;
import com.projectkorra.projectkorra.ability.info.AbilityInfo;
import com.projectkorra.projectkorra.ability.info.MultiAbilityInfo;
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

public class MultiAbilityManager extends Module {

	private final BendingPlayerManager bendingPlayerManager;
	private final AbilityManager abilityManager;

	private final Map<String, MultiAbility> abilities = new HashMap<>();
	private final Map<String, Class<? extends Ability>> multiAbilities = new HashMap<>();

	private final Map<UUID, Class<? extends Ability>> playerMultiAbility = new HashMap<>();
	private final Map<UUID, List<String>> playerAbilities = new HashMap<>();

	private MultiAbilityManager() {
		super("Multi Ability");

		this.bendingPlayerManager = ModuleManager.getModule(BendingPlayerManager.class);
		this.abilityManager = ModuleManager.getModule(AbilityManager.class);
	}

	public void registerAbility(Class<? extends Ability> abilityClass, MultiAbilityInfo multiAbilityInfo) {
		List<Class<? extends Ability>> abilities = multiAbilityInfo.getAbilities();

		Map<String, Class<? extends Ability>> abilitiesByName = new HashMap<>();

		for (Class<? extends Ability> ability : abilities) {
			AbilityInfo info = this.abilityManager.getAbilityInfo(ability);

			abilitiesByName.put(info.getName(), ability);
		}

		MultiAbility multiAbility = new MultiAbility(abilityClass, multiAbilityInfo.getName(), abilitiesByName.keySet());

		this.abilities.put(multiAbilityInfo.getName(), multiAbility);
		this.multiAbilities.putAll(abilitiesByName);
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
	public void onMultiAbilitySwing(PlayerSwingEvent event) {
		Player player = event.getPlayer();

		String abilityName = event.getAbilityName();
		Class<? extends Ability> abilityClass = this.multiAbilities.get(abilityName);

		if (abilityClass == null) {
			return;
		}

		Ability ability = this.abilityManager.createAbility(player, abilityClass);
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
	public void onPlayerBindAbility(PlayerBindChangeEvent event) {
		if (!this.playerAbilities.containsKey(event.getPlayer().getUniqueId())) {
			return;
		}

		event.setCancelled(true);
		GeneralMethods.sendBrandingMessage(event.getPlayer(), ChatColor.RED + "You can't edit your binds right now!");
	}

	public class MultiAbility {
		private final Class<? extends Ability> abilityClass;
		private final String abilityName;
		private final Set<String> abilities;

		MultiAbility(Class<? extends Ability> abilityClass, String abilityName, Set<String> abilities) {
			this.abilityClass = abilityClass;
			this.abilityName = abilityName;
			this.abilities = abilities;
		}
	}
}

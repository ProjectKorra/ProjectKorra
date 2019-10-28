package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.ability.info.ComboAbilityInfo;
import com.projectkorra.projectkorra.module.Module;
import com.projectkorra.projectkorra.module.ModuleManager;
import com.projectkorra.projectkorra.player.BendingPlayer;
import com.projectkorra.projectkorra.player.BendingPlayerManager;
import com.projectkorra.projectkorra.util.ClickType;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ComboAbilityManager extends Module {

	private final BendingPlayerManager bendingPlayerManager;
	private final AbilityManager abilityManager;

	private final Map<UUID, LinkedList<Combination>> recentlyUsed = new HashMap<>();
	private final List<ComboAbility> abilities = new ArrayList<>();

	private final long combinationMax = 8;

	private ComboAbilityManager() {
		super("Combo Ability");

		this.bendingPlayerManager = ModuleManager.getModule(BendingPlayerManager.class);
		this.abilityManager = ModuleManager.getModule(AbilityManager.class);

		this.abilities.clear();
	}

	public void registerAbility(Class<? extends Ability> abilityClass, ComboAbilityInfo comboAbilityInfo) {
		ComboAbility comboAbility = new ComboAbility(abilityClass, comboAbilityInfo.getName(), comboAbilityInfo.getCombination());

		this.abilities.add(comboAbility);
	}

	private void processComboAbility(Player player, ClickType clickType) {
		BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);

		String abilityName = bendingPlayer.getBoundAbility();

		LinkedList<Combination> recentlyUsed = this.recentlyUsed.computeIfAbsent(player.getUniqueId(), k -> new LinkedList<>());

		recentlyUsed.addFirst(new Combination(abilityName, clickType));

		if (recentlyUsed.size() > this.combinationMax) {
			recentlyUsed.removeLast();
		}

		ComboAbility comboAbility = getComboAbility(recentlyUsed);

		if (comboAbility == null) {
			return;
		}

		if (!player.hasPermission("bending.ability." + comboAbility.abilityName)) {
			return;
		}

		this.abilityManager.createAbility(player, comboAbility.abilityClass);
	}

	private ComboAbility getComboAbility(LinkedList<Combination> recentlyUsed) {
		for (ComboAbility comboAbility : this.abilities) {
			int comboSize = comboAbility.combinations.size();

			if (recentlyUsed.size() < comboSize) {
				continue;
			}

			if (recentlyUsed.subList(0, comboSize).equals(comboAbility.combinations)) {
				return comboAbility;
			}
		}

		return null;
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);

		if (event.getHand() != EquipmentSlot.HAND) {
			return;
		}

		if (!bendingPlayer.canCurrentlyBendWithWeapons()) {
			return;
		}

		boolean rightClick = event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR;
		boolean leftClick = event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR;

		if (leftClick) {
			processComboAbility(player, ClickType.LEFT_CLICK);
			return;
		}

		if (rightClick) {
			ClickType clickType = event.getClickedBlock() != null ? ClickType.RIGHT_CLICK_BLOCK : ClickType.RIGHT_CLICK;
			processComboAbility(player, clickType);
			return;
		}
	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event) {
		if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof LivingEntity)) {
			return;
		}

		Player player = (Player) event.getDamager();
		BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);

		if (!bendingPlayer.canCurrentlyBendWithWeapons()) {
			return;
		}

		processComboAbility(player, ClickType.LEFT_CLICK_ENTITY);
	}

	@EventHandler
	public void onInteractEntity(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);

		if (!bendingPlayer.canCurrentlyBendWithWeapons()) {
			return;
		}

		processComboAbility(player, ClickType.RIGHT_CLICK_ENTITY);
	}

	@EventHandler
	public void onSneak(PlayerToggleSneakEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);

		if (!bendingPlayer.canCurrentlyBendWithWeapons()) {
			return;
		}

		ClickType clickType = event.isSneaking() ? ClickType.SHIFT_DOWN : ClickType.SHIFT_UP;
		processComboAbility(player, clickType);
	}

	@EventHandler
	public void onSwapHandItems(PlayerSwapHandItemsEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);

		ItemStack mainHand = event.getMainHandItem();
		ItemStack offHand = event.getOffHandItem();

		if (mainHand.getType() != Material.AIR) {
			return;
		}

		if (offHand != null || offHand.getType() != Material.AIR) {
			return;
		}

		processComboAbility(player, ClickType.OFFHAND_TRIGGER);
	}

	public class Combination {
		private final String abilityName;
		private final ClickType clickType;

		Combination(String abilityName, ClickType clickType) {
			this.abilityName = abilityName;
			this.clickType = clickType;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Combination)) {
				return false;
			}

			Combination combination = (Combination) obj;

			return this.abilityName.equals(combination.abilityName) && this.clickType == combination.clickType;
		}
	}

	private class ComboAbility {
		private final Class<? extends Ability> abilityClass;
		private final String abilityName;
		private final List<Combination> combinations;

		ComboAbility(Class<? extends Ability> abilityClass, String abilityName, List<Combination> combinations) {
			this.abilityClass = abilityClass;
			this.abilityName = abilityName;
			this.combinations = combinations;
		}
	}
}

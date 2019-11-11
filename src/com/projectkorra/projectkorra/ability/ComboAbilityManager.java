package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.ability.api.ComboAbility;
import com.projectkorra.projectkorra.element.Element;
import com.projectkorra.projectkorra.module.Module;
import com.projectkorra.projectkorra.module.ModuleManager;
import com.projectkorra.projectkorra.player.BendingPlayer;
import com.projectkorra.projectkorra.player.BendingPlayerManager;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.MultiKeyMap;
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
import java.util.stream.Collectors;

public class ComboAbilityManager extends Module {

	private final BendingPlayerManager bendingPlayerManager;
	private final AbilityManager abilityManager;

	private final MultiKeyMap<String, AbilityHandler> handlerMap = new MultiKeyMap<>();

	private final List<ComboAbilityInfo> comboAbilities = new ArrayList<>();

	private final Map<UUID, LinkedList<Combination>> recentlyUsed = new HashMap<>();

	private final long combinationMax = 8;

	private ComboAbilityManager() {
		super("Combo Ability");

		this.bendingPlayerManager = ModuleManager.getModule(BendingPlayerManager.class);
		this.abilityManager = ModuleManager.getModule(AbilityManager.class);
	}

	public void registerAbility(AbilityHandler abilityHandler) {
		ComboAbility comboAbility = (ComboAbility) abilityHandler;

		ComboAbilityInfo comboAbilityInfo = new ComboAbilityInfo(abilityHandler, comboAbility.getCombination());

		this.handlerMap.put(abilityHandler.getName(), abilityHandler);
		this.comboAbilities.add(comboAbilityInfo);
	}

	private void processComboAbility(Player player, ClickType clickType) {
		BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);

		String abilityName = bendingPlayer.getBoundAbility();

		LinkedList<Combination> recentlyUsed = this.recentlyUsed.computeIfAbsent(player.getUniqueId(), k -> new LinkedList<>());

		recentlyUsed.addFirst(new Combination(abilityName, clickType));

		if (recentlyUsed.size() > this.combinationMax) {
			recentlyUsed.removeLast();
		}

		ComboAbilityInfo comboAbilityInfo = getAbility(recentlyUsed);

		if (comboAbilityInfo == null) {
			return;
		}

		if (!player.hasPermission("bending.ability." + comboAbilityInfo.abilityHandler.getName())) {
			return;
		}

		comboAbilityInfo.abilityHandler.newInstance(player);
	}

	private ComboAbilityInfo getAbility(LinkedList<Combination> recentlyUsed) {
		for (ComboAbilityInfo comboAbilityInfo : this.comboAbilities) {
			int comboSize = comboAbilityInfo.combinationList.size();

			if (recentlyUsed.size() < comboSize) {
				continue;
			}

			if (recentlyUsed.subList(0, comboSize).equals(comboAbilityInfo.combinationList)) {
				return comboAbilityInfo;
			}
		}

		return null;
	}

	public AbilityHandler getHandler(String abilityName) {
		return this.handlerMap.get(abilityName);
	}

	public AbilityHandler getHandler(Class<? extends AbilityHandler> handlerClass) {
		return this.handlerMap.get(handlerClass);
	}

	public List<AbilityHandler> getHandlers(Element element) {
		return this.handlerMap.values().stream()
				.filter(comboAbilityInfo -> comboAbilityInfo.getElement().equals(element))
				.collect(Collectors.toList());
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

	private class ComboAbilityInfo {
		private final AbilityHandler abilityHandler;
		private final List<Combination> combinationList;

		ComboAbilityInfo(AbilityHandler abilityHandler, List<Combination> combinationList) {
			this.abilityHandler = abilityHandler;
			this.combinationList = combinationList;
		}
	}
}

package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.ability.loader.ComboAbilityLoader;
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ComboManager extends Module {

	private final BendingPlayerManager bendingPlayerManager;

	private final Map<UUID, LinkedList<Combination>> recentlyUsed = new HashMap<>();
	private final List<ComboAbilityInfo> comboAbilities = new ArrayList<>();

	private final long combinationMax = 8;

	private ComboManager() {
		super("Combo Ability");

		this.bendingPlayerManager = ModuleManager.getModule(BendingPlayerManager.class);

		this.comboAbilities.clear();
	}

	public void registerAbility(Class<? extends Ability> abilityClass, AbilityData abilityData, ComboAbilityLoader comboAbilityLoader) {
		ComboAbilityInfo comboAbilityInfo = new ComboAbilityInfo(abilityClass, abilityData.name(), comboAbilityLoader.getCombination());

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

		ComboAbilityInfo comboAbilityInfo = getComboAbiblity(recentlyUsed);

		if (comboAbilityInfo == null) {
			return;
		}

		if (!player.hasPermission("bending.ability." + comboAbilityInfo.abilityName)) {
			return;
		}

		try {
			Class<? extends Ability> abilityClass = comboAbilityInfo.abilityClass;
			Constructor<? extends Ability> constructor = abilityClass.getDeclaredConstructor(Player.class);

			Ability ability = constructor.newInstance(player);

		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}

	}

	private ComboAbilityInfo getComboAbiblity(LinkedList<Combination> recentlyUsed) {
		for (ComboAbilityInfo comboAbilityInfo : this.comboAbilities) {
			LinkedList<Combination> abilityCombinations = comboAbilityInfo.combinations;

			int comboSize = abilityCombinations.size();

			if (recentlyUsed.size() < comboSize) {
				continue;
			}

			if (recentlyUsed.subList(0, comboSize).equals(abilityCombinations)) {
				return comboAbilityInfo;
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

	private class ComboAbilityInfo {
		private final Class<? extends Ability> abilityClass;
		private final String abilityName;
		private final LinkedList<Combination> combinations;

		ComboAbilityInfo(Class<? extends Ability> abilityClass, String abilityName, LinkedList<Combination> combinations) {
			this.abilityClass = abilityClass;
			this.abilityName = abilityName;
			this.combinations = combinations;
		}
	}

}

package com.projectkorra.ProjectKorra.firebending;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.projectkorra.ProjectKorra.Methods;

public class Cook {

	private static ConcurrentHashMap<Player, Cook> instances = new ConcurrentHashMap<Player, Cook>();

	private static final long cooktime = 2000;
	private static final Material[] cookables = { Material.RAW_BEEF,
		Material.RAW_CHICKEN, Material.RAW_FISH, Material.PORK,
		Material.POTATO_ITEM };

	private Player player;
	private ItemStack items;
	private long time;

	public Cook(Player player) {
		this.player = player;
		items = player.getItemInHand();
		time = System.currentTimeMillis();
		if (isCookable(items.getType())) {
			instances.put(player, this);
		}
	}

	private void progress() {
		if (player.isDead() || !player.isOnline()) {
			cancel();
			return;
		}

		if (Methods.getBoundAbility(player) == null) {
			cancel();
			return;
		}
		if (!player.isSneaking()
				|| !Methods.getBoundAbility(player).equalsIgnoreCase("HeatControl")) {
			cancel();
			return;
		}

		if (!items.equals(player.getItemInHand())) {
			time = System.currentTimeMillis();
			items = player.getItemInHand();
		}

		if (!isCookable(items.getType())) {
			cancel();
			return;
		}

		if (System.currentTimeMillis() > time + cooktime) {
			cook();
			time = System.currentTimeMillis();
		}

		player.getWorld().playEffect(player.getEyeLocation(),
				Effect.MOBSPAWNER_FLAMES, 0, 10);
	}

	private void cancel() {
		instances.remove(player);
	}

	private static boolean isCookable(Material material) {
		return Arrays.asList(cookables).contains(material);
	}

	private void cook() {
		Material cooked = getCooked(items.getType());
		ItemStack newitem = new ItemStack(cooked);
		HashMap<Integer, ItemStack> cantfit = player.getInventory().addItem(
				newitem);
		for (int id : cantfit.keySet()) {
			player.getWorld()
			.dropItem(player.getEyeLocation(), cantfit.get(id));
		}
		int amount = items.getAmount();
		if (amount == 1) {
			player.getInventory()
			.clear(player.getInventory().getHeldItemSlot());
			// items.setType(Material.AIR);
		} else {
			items.setAmount(amount - 1);
		}
	}

	private Material getCooked(Material material) {
		Material cooked = Material.AIR;
		switch (material) {
		case RAW_BEEF:
			cooked = Material.COOKED_BEEF;
			break;
		case RAW_FISH:
			cooked = Material.COOKED_FISH;
			break;
		case RAW_CHICKEN:
			cooked = Material.COOKED_CHICKEN;
			break;
		case PORK:
			cooked = Material.GRILLED_PORK;
			break;
		case POTATO_ITEM:
			cooked = Material.BAKED_POTATO;
			break;
		}
		return cooked;
	}

	public static void progressAll() {
		for (Player player : instances.keySet()) {
			instances.get(player).progress();
		}
	}

	public static void removeAll() {
		instances.clear();
	}

}
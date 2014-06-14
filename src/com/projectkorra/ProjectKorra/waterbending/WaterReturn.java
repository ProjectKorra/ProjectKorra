package com.projectkorra.ProjectKorra.waterbending;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.TempBlock;
import com.projectkorra.abilities.IceSpike.IceSpike2;
import com.projectkorra.abilities.OctopusForm.OctopusFormAbility;
import com.projectkorra.abilities.Surge.WaterWall;
import com.projectkorra.abilities.Surge.WaveAbility;
import com.projectkorra.abilities.WaterManipulation.WaterManipulationAbility;

public class WaterReturn {

	private static ConcurrentHashMap<Player, WaterReturn> instances = new ConcurrentHashMap<Player, WaterReturn>();
	// private static int ID = Integer.MIN_VALUE;
	private static long interval = 50;

	private static final byte full = 0x0;
	private static double range = 30;

	private Player player;
	// private int id;
	private Location location;
	private TempBlock block;
	private long time;

	public WaterReturn(Player player, Block block) {
		if (instances.containsKey(player))
			return;
		this.player = player;
		location = block.getLocation();
		//		if (!Tools.isRegionProtectedFromBuild(player,
		//				Abilities.WaterManipulation, location)
		//				&& Tools.canBend(player, Abilities.WaterManipulation)) {
		if (Methods.isAbilityInstalled("WaterManipulation", "orion304") && Methods.canBend(player.getName(), "WaterManipulation")) {
			if (Methods.isTransparentToEarthbending(player, block)
					&& !block.isLiquid())
				this.block = new TempBlock(block, Material.WATER, full);
		}
		//		}
		// if (ID >= Integer.MAX_VALUE) {
		// ID = Integer.MIN_VALUE;
		// }
		// id = ID++;
		instances.put(player, this);
	}

	private void progress() {
		if (!hasEmptyWaterBottle()) {
			remove();
			return;
		}

		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}

		if (player.getWorld() != location.getWorld()) {
			remove();
			return;
		}

		if (System.currentTimeMillis() < time + interval)
			return;

		time = System.currentTimeMillis();

		Vector direction = Methods
				.getDirection(location, player.getEyeLocation()).normalize();
		location = location.clone().add(direction);

		if (location == null || block == null) {
			remove();
			return;
		}

		if (location.getBlock().equals(block.getLocation().getBlock()))
			return;

		//		if (Tools.isRegionProtectedFromBuild(player,
		//				Abilities.WaterManipulation, location)) {
		//			remove();
		//			return;
		//		}

		if (location.distance(player.getEyeLocation()) > Methods
				.waterbendingNightAugment(range, player.getWorld())) {
			remove();
			return;
		}

		if (location.distance(player.getEyeLocation()) <= 1.5) {
			fillBottle();
			return;
		}

		Block newblock = location.getBlock();
		if (Methods.isTransparentToEarthbending(player, newblock)
				&& !newblock.isLiquid()) {
			block.revertBlock();
			block = new TempBlock(newblock, Material.WATER, full);
		} else {
			remove();
			return;
		}

	}

	private void remove() {
		if (block != null) {
			block.revertBlock();
			block = null;
		}
		instances.remove(player);
	}

	private boolean hasEmptyWaterBottle() {
		PlayerInventory inventory = player.getInventory();
		if (inventory.contains(Material.GLASS_BOTTLE)) {
			return true;
		}
		return false;
	}

	private void fillBottle() {
		PlayerInventory inventory = player.getInventory();
		if (inventory.contains(Material.GLASS_BOTTLE)) {
			int index = inventory.first(Material.GLASS_BOTTLE);
			ItemStack item = inventory.getItem(index);
			if (item.getAmount() == 1) {
				inventory.setItem(index, new ItemStack(Material.POTION));
			} else {
				item.setAmount(item.getAmount() - 1);
				inventory.setItem(index, item);
				HashMap<Integer, ItemStack> leftover = inventory
						.addItem(new ItemStack(Material.POTION));
				for (int left : leftover.keySet()) {
					player.getWorld().dropItemNaturally(player.getLocation(),
							leftover.get(left));
				}
			}
		}

		remove();
	}

	private static boolean isBending(Player player) {
		if (Methods.isAbilityInstalled("WaterManipulation", "orion304")) {
			for (int id : WaterManipulationAbility.instances.keySet()) {
				WaterManipulationAbility.instances.get(id);
				if (WaterManipulationAbility.player.equals(player))
					return true;
			}
		}
		if (Methods.isAbilityInstalled("OctopusForm", "orion304")) {
			if (OctopusFormAbility.instances.containsKey(player))
				return true;
		}
		if (Methods.isAbilityInstalled("Surge", "orion304")) {
			for (int id : WaveAbility.instances.keySet()) {
				if (WaveAbility.instances.get(id).player.equals(player))
					return true;
			}
			for (int id : WaterWall.instances.keySet()) {
				if (WaterWall.instances.get(id).player.equals(player))
					return true;
			}
		}

		if (Methods.isAbilityInstalled("IceSpike", "orion304")) {
			if (IceSpike2.isBending(player))
				return true;
		}
		return false;
	}

	public static boolean hasWaterBottle(Player player) {
		if (instances.containsKey(player))
			return false;
		if (isBending(player))
			return false;
		PlayerInventory inventory = player.getInventory();
		return (inventory.contains(new ItemStack(Material.POTION), 1));
	}

	public static void emptyWaterBottle(Player player) {
		PlayerInventory inventory = player.getInventory();
		int index = inventory.first(new ItemStack(Material.POTION));
		if (index != -1) {
			ItemStack item = inventory.getItem(index);
			if (item.getAmount() == 1) {
				inventory.setItem(index, new ItemStack(Material.GLASS_BOTTLE));
			} else {
				item.setAmount(item.getAmount() - 1);
				inventory.setItem(index, item);
				HashMap<Integer, ItemStack> leftover = inventory
						.addItem(new ItemStack(Material.GLASS_BOTTLE));
				for (int left : leftover.keySet()) {
					player.getWorld().dropItemNaturally(player.getLocation(),
							leftover.get(left));
				}
			}
		}
	}

	public static void progressAll() {
		for (Player player : instances.keySet()) {
			instances.get(player).progress();
		}
	}

	public static void removeAll() {
		for (Player player : instances.keySet()) {
			WaterReturn wr = instances.get(player);
			if (wr.block != null)
				wr.block.revertBlock();
		}
		instances.clear();
	}

}
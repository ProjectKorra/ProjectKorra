package com.projectkorra.ProjectKorra.earthbending;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.TempBlock;
import com.projectkorra.ProjectKorra.TempPotionEffect;

public class EarthArmor {

	private static long duration = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.EarthArmor.Duration");
	private static int strength = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.EarthArmor.Strength");
	private static long cooldown = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.EarthArmor.Cooldown");
	private static int range = 7;

	private Player player;
	private Block headblock, legsblock;
	private Location headblocklocation, legsblocklocation;
	private Material headtype, legstype;
	private byte headdata, legsdata;
	private long time, starttime;
	private boolean formed = false;
	private boolean complete = false;
	public ItemStack[] oldarmor;

	private static long interval = 2000;
	public static ConcurrentHashMap<Player, EarthArmor> instances = new ConcurrentHashMap<Player, EarthArmor>();
	public static Map<String, Long> cooldowns = new ConcurrentHashMap<String, Long>();

	public EarthArmor(Player player) {
		if (instances.containsKey(player)) {
			return;
		}

		if (cooldowns.containsKey(player.getName())) {
			if (cooldowns.get(player.getName()) + cooldown >= System.currentTimeMillis()) {
				return;
			} else {
				cooldowns.remove(player.getName());
			}
		}

		this.player = player;
		headblock = player.getTargetBlock(Methods.getTransparentEarthbending(),
				range);
		if (Methods.getEarthbendableBlocksLength(player, headblock, new Vector(0,
				-1, 0), 2) >= 2) {
			legsblock = headblock.getRelative(BlockFace.DOWN);
			headtype = headblock.getType();
			legstype = legsblock.getType();
			headdata = headblock.getData();
			legsdata = legsblock.getData();
			headblocklocation = headblock.getLocation();
			legsblocklocation = legsblock.getLocation();
			Block oldheadblock, oldlegsblock;
			oldheadblock = headblock;
			oldlegsblock = legsblock;
			if (!moveBlocks())
				return;
			if (ProjectKorra.plugin.getConfig().getBoolean("Properties.Earth.RevertEarthbending")) {
				Methods.addTempAirBlock(oldheadblock);
				Methods.addTempAirBlock(oldlegsblock);
			} else {
				Methods.removeBlock(oldheadblock);
				Methods.removeBlock(oldlegsblock);
			}
			instances.put(player, this);
		}
	}

	private boolean moveBlocks() {
		if (player.getWorld() != headblock.getWorld()) {
			cancel();
			return false;
		}

		Location headlocation = player.getEyeLocation();
		Location legslocation = player.getLocation();
		Vector headdirection = headlocation.toVector()
				.subtract(headblocklocation.toVector()).normalize()
				.multiply(.5);
		Vector legsdirection = legslocation.toVector()
				.subtract(legsblocklocation.toVector()).normalize()
				.multiply(.5);

		Block newheadblock = headblock;
		Block newlegsblock = legsblock;

		if (!headlocation.getBlock().equals(headblock)) {
			headblocklocation = headblocklocation.clone().add(headdirection);
			newheadblock = headblocklocation.getBlock();
		}
		if (!legslocation.getBlock().equals(legsblock)) {
			legsblocklocation = legsblocklocation.clone().add(legsdirection);
			newlegsblock = legsblocklocation.getBlock();
		}

		if (Methods.isTransparentToEarthbending(player, newheadblock)
				&& !newheadblock.isLiquid()) {
			Methods.breakBlock(newheadblock);
		} else if (!Methods.isEarthbendable(player, newheadblock)
				&& !newheadblock.isLiquid()
				&& newheadblock.getType() != Material.AIR) {
			cancel();
			return false;
		}

		if (Methods.isTransparentToEarthbending(player, newlegsblock)
				&& !newlegsblock.isLiquid()) {
			Methods.breakBlock(newlegsblock);
		} else if (!Methods.isEarthbendable(player, newlegsblock)
				&& !newlegsblock.isLiquid()
				&& newlegsblock.getType() != Material.AIR) {
			cancel();
			return false;
		}

		// if ((!Methods.isEarthbendable(player, newlegsblock)
		// && !newlegsblock.isLiquid() && newlegsblock.getType() !=
		// Material.AIR)
		// || (!Methods.isEarthbendable(player, newheadblock)
		// && !newheadblock.isLiquid() && newheadblock.getType() !=
		// Material.AIR)) {
		// cancel();
		// return false;
		// }

		if (headblock.getLocation().distance(player.getEyeLocation()) > range
				|| legsblock.getLocation().distance(player.getLocation()) > range) {
			cancel();
			return false;
		}

		if (!newheadblock.equals(headblock)) {
			new TempBlock(newheadblock, headtype, headdata);
			if (TempBlock.isTempBlock(headblock))
				TempBlock.revertBlock(headblock, Material.AIR);
		}

		if (!newlegsblock.equals(legsblock)) {
			new TempBlock(newlegsblock, legstype, legsdata);
			if (TempBlock.isTempBlock(legsblock))
				TempBlock.revertBlock(legsblock, Material.AIR);
		}

		headblock = newheadblock;
		legsblock = newlegsblock;

		return true;
	}

	private void cancel() {
		if (ProjectKorra.plugin.getConfig().getBoolean("Properties.Earth.RevertEarthbending")) {
			if (TempBlock.isTempBlock(headblock))
				TempBlock.revertBlock(headblock, Material.AIR);
			if (TempBlock.isTempBlock(legsblock))
				TempBlock.revertBlock(legsblock, Material.AIR);
		} else {
			headblock.breakNaturally();
			legsblock.breakNaturally();
		}
		if (instances.containsKey(player))
			instances.remove(player);
	}

	private boolean inPosition() {
		if (headblock.equals(player.getEyeLocation().getBlock())
				&& legsblock.equals(player.getLocation().getBlock())) {
			return true;
		}
		return false;
	}

	private void formArmor() {
		if (TempBlock.isTempBlock(headblock))
			TempBlock.revertBlock(headblock, Material.AIR);
		if (TempBlock.isTempBlock(legsblock))
			TempBlock.revertBlock(legsblock, Material.AIR);

		oldarmor = player.getInventory().getArmorContents();
		ItemStack armors[] = { new ItemStack(Material.LEATHER_BOOTS, 1),
				new ItemStack(Material.LEATHER_LEGGINGS, 1),
				new ItemStack(Material.LEATHER_CHESTPLATE, 1),
				new ItemStack(Material.LEATHER_HELMET, 1) };
		player.getInventory().setArmorContents(armors);
		PotionEffect resistance = new PotionEffect(
				PotionEffectType.DAMAGE_RESISTANCE, (int) duration / 50,
				strength - 1);
		new TempPotionEffect(player, resistance);
		// player.addPotionEffect(new PotionEffect(
		// PotionEffectType.DAMAGE_RESISTANCE, (int) duration / 50,
		// strength - 1));
		formed = true;
		starttime = System.currentTimeMillis();
	}

	public static void moveArmor(Player player) {
		if (!instances.containsKey(player))
			return;
		EarthArmor eartharmor = instances.get(player);

		if (player.isDead() || !player.isOnline()) {
			eartharmor.cancel();
			eartharmor.removeEffect();
			return;
		}

		if (eartharmor.formed) {
			if (System.currentTimeMillis() > eartharmor.starttime + duration
					&& !eartharmor.complete) {
				eartharmor.complete = true;
				eartharmor.removeEffect();
				return;
			}
			if (System.currentTimeMillis() > eartharmor.starttime + cooldown) {
				instances.remove(player);
				return;
			}
		} else if (System.currentTimeMillis() > eartharmor.time + interval) {
			if (!eartharmor.moveBlocks())
				return;

			if (eartharmor.inPosition()) {
				eartharmor.formArmor();
			}
		}

	}

	private void removeEffect() {
		player.getInventory().setArmorContents(oldarmor);
		// player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
		// instances.remove(player);
	}

	public static void removeEffect(Player player) {
		if (!instances.containsKey(player))
			return;
		instances.get(player).removeEffect();
	}

	public static void removeAll() {
		for (Player player : instances.keySet()) {
			EarthArmor eartharmor = instances.get(player);
			eartharmor.cancel();
			eartharmor.removeEffect();
		}
	}

	public static String getDescription() {
		return "This ability encases the earthbender in temporary armor. To use, click on a block that is earthbendable. If there is another block under"
				+ " it that is earthbendable, the block will fly to you and grant you temporary armor and damage reduction. This ability has a long cooldown.";
	}

	public static boolean canRemoveArmor(Player player) {
		if (instances.containsKey(player)) {
			EarthArmor eartharmor = instances.get(player);
			if (System.currentTimeMillis() < eartharmor.starttime + duration)
				return false;
		}
		return true;
	}
}
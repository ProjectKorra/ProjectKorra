package com.projectkorra.projectkorra.waterbending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.util.HashMap;

public class WaterReturn extends WaterAbility {

	private long time;
	private long interval;
	private double range;
	private Location location;
	private TempBlock block;
	
	public WaterReturn(Player player, Block block) {
		super(player);
		if (hasAbility(player, WaterReturn.class)) {
			return;
		}

		this.location = block.getLocation();
		this.range = 30;
		this.interval = 50;
		
		this.range = getNightFactor(range);
		
		if (bPlayer.canBendIgnoreBindsCooldowns(this)) {
			if (isTransparent(player, block) && ((TempBlock.isTempBlock(block) && block.isLiquid()) || !block.isLiquid()) && hasEmptyWaterBottle()) {
				this.block = new TempBlock(block, Material.WATER, (byte) 0);
			}
		}
		start();
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			remove();
			return;
		} else if (!hasEmptyWaterBottle()) {
			remove();
			return;
		} else if (System.currentTimeMillis() < time + interval) {
			return;
		}

		Vector direction = GeneralMethods.getDirection(location, player.getEyeLocation()).normalize();
		time = System.currentTimeMillis();
		location = location.clone().add(direction);

		if (location == null || block == null) {
			remove();
			return;
		} else if (location.getBlock().equals(block.getLocation().getBlock())) {
			return;
		}

		if (location.distanceSquared(player.getEyeLocation()) > range * range) {
			remove();
			return;
		} else if (location.distanceSquared(player.getEyeLocation()) <= 1.5 * 1.5) {
			fillBottle();
			return;
		}

		Block newblock = location.getBlock();
		if (isTransparent(player, newblock) && !newblock.isLiquid()) {
			block.revertBlock();
			block = new TempBlock(newblock, Material.WATER, (byte) 0);
		} else if (isTransparent(player, newblock)) {
			if (isWater(newblock)) {
				ParticleEffect.WATER_BUBBLE.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0f, 5, newblock.getLocation().clone().add(.5,.5,.5), 257D);
			}
		} else {
			remove();
			return;
		}

	}

	@Override
	public void remove() {
		super.remove();
		if (block != null) {
			block.revertBlock();
		}
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
				HashMap<Integer, ItemStack> leftover = inventory.addItem(new ItemStack(Material.POTION));
				for (int left : leftover.keySet()) {
					player.getWorld().dropItemNaturally(player.getLocation(), leftover.get(left));
				}
			}
		}
		remove();
	}

	private static boolean isBending(Player player) {
		if (hasAbility(player, WaterManipulation.class)
				|| hasAbility(player, WaterManipulation.class)
				|| hasAbility(player, OctopusForm.class)
				// || hasAbility(player, SurgeWave.class) NOTE: ONLY DISABLED TO PREVENT BOTTLEBENDING FROM BEING DISABLED FOREVER. ONCE BOTTLEBENDING HAS BEEN RECODED IN 1.9, THIS NEEDS TO BE READDED TO THE NEW SYSTEM.
				|| hasAbility(player, SurgeWall.class)
				|| hasAbility(player, IceSpikeBlast.class)) {
			return true;
		}
		return false;
	}

	public static boolean hasWaterBottle(Player player) {
		if (hasAbility(player, WaterReturn.class) || isBending(player)) {
			return false;
		}
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
				HashMap<Integer, ItemStack> leftover = inventory.addItem(new ItemStack(Material.GLASS_BOTTLE));
				
				for (int left : leftover.keySet()) {
					player.getWorld().dropItemNaturally(player.getLocation(), leftover.get(left));
				}
			}
		}
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	@Override
	public Location getLocation() {
		return location;
	}
	
	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public TempBlock getBlock() {
		return block;
	}

	public void setBlock(TempBlock block) {
		this.block = block;
	}

	@Override
	public String getName() {
		return "WaterReturn";
	}

	@Override
	public long getCooldown() {
		return 0;
	}
	
	@Override
	public boolean isHiddenAbility() {
		return true;
	}
	
}

package com.projectkorra.projectkorra.waterbending.util;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.configuration.better.configs.abilities.EmptyAbilityConfig;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.OctopusForm;
import com.projectkorra.projectkorra.waterbending.SurgeWall;
import com.projectkorra.projectkorra.waterbending.WaterManipulation;
import com.projectkorra.projectkorra.waterbending.ice.IceSpikeBlast;

@SuppressWarnings("deprecation")
public class WaterReturn extends WaterAbility<EmptyAbilityConfig> {

	private long time;
	private long interval;
	private double range;
	private Location location;
	private TempBlock block;

	public WaterReturn(final Player player, final Block block) {
		super(new EmptyAbilityConfig(), player);
		if (hasAbility(player, WaterReturn.class)) {
			return;
		}

		this.location = block.getLocation();
		this.range = 30;
		this.interval = 50;

		this.range = this.getNightFactor(this.range);

		if (this.bPlayer.canBendIgnoreBindsCooldowns(this)) {
			if (isTransparent(player, block) && ((TempBlock.isTempBlock(block) && block.isLiquid()) || !block.isLiquid()) && this.hasEmptyWaterBottle()) {
				this.block = new TempBlock(block, Material.WATER, GeneralMethods.getWaterData(0));
			}
		}
		this.start();
	}

	@Override
	public void progress() {
		if (!this.bPlayer.canBendIgnoreBindsCooldowns(this)) {
			this.remove();
			return;
		} else if (!this.hasEmptyWaterBottle()) {
			this.remove();
			return;
		} else if (System.currentTimeMillis() < this.time + this.interval) {
			return;
		}

		final Vector direction = GeneralMethods.getDirection(this.location, this.player.getEyeLocation()).normalize();
		this.time = System.currentTimeMillis();
		this.location = this.location.clone().add(direction);

		if (this.location == null || this.block == null) {
			this.remove();
			return;
		} else if (this.location.getBlock().equals(this.block.getLocation().getBlock())) {
			return;
		}

		if (this.location.distanceSquared(this.player.getEyeLocation()) > this.range * this.range) {
			this.remove();
			return;
		} else if (this.location.distanceSquared(this.player.getEyeLocation()) <= 1.5 * 1.5) {
			this.fillBottle();
			return;
		}

		final Block newblock = this.location.getBlock();
		if (isTransparent(this.player, newblock) && !newblock.isLiquid()) {
			this.block.revertBlock();
			this.block = new TempBlock(newblock, Material.WATER, GeneralMethods.getWaterData(0));
		} else if (isTransparent(this.player, newblock)) {
			if (isWater(newblock)) {
				ParticleEffect.WATER_BUBBLE.display(newblock.getLocation().clone().add(.5, .5, .5), 5, Math.random(), Math.random(), Math.random(), 0);
			}
		} else {
			this.remove();
			return;
		}

	}

	@Override
	public void remove() {
		super.remove();
		if (this.block != null) {
			this.block.revertBlock();
		}
	}

	private boolean hasEmptyWaterBottle() {
		final PlayerInventory inventory = this.player.getInventory();
		if (inventory.contains(Material.GLASS_BOTTLE)) {
			return true;
		}
		return false;
	}

	private void fillBottle() {
		final PlayerInventory inventory = this.player.getInventory();
		if (inventory.contains(Material.GLASS_BOTTLE)) {
			final int index = inventory.first(Material.GLASS_BOTTLE);
			final ItemStack item = inventory.getItem(index);

			final ItemStack water = waterBottleItem();

			if (item.getAmount() == 1) {
				inventory.setItem(index, water);
			} else {
				item.setAmount(item.getAmount() - 1);
				inventory.setItem(index, item);
				final HashMap<Integer, ItemStack> leftover = inventory.addItem(water);
				for (final int left : leftover.keySet()) {
					this.player.getWorld().dropItemNaturally(this.player.getLocation(), leftover.get(left));
				}
			}
		}
		this.remove();
	}

	private static boolean isBending(final Player player) {
		if (hasAbility(player, WaterManipulation.class) || hasAbility(player, WaterManipulation.class) || hasAbility(player, OctopusForm.class)
		// || hasAbility(player, SurgeWave.class) NOTE: ONLY DISABLED TO
		// PREVENT BOTTLEBENDING FROM BEING DISABLED FOREVER. ONCE
		// BOTTLEBENDING HAS BEEN RECODED IN 1.9, THIS NEEDS TO BE
		// READDED TO THE NEW SYSTEM.
				|| hasAbility(player, SurgeWall.class) || hasAbility(player, IceSpikeBlast.class)) {
			return true;
		}
		return false;
	}

	public static boolean hasWaterBottle(final Player player) {
		if (hasAbility(player, WaterReturn.class) || isBending(player)) {
			return false;
		}
		final PlayerInventory inventory = player.getInventory();
		if (inventory.contains(Material.POTION)) {
			final ItemStack item = inventory.getItem(inventory.first(Material.POTION));
			final PotionMeta meta = (PotionMeta) item.getItemMeta();
			return meta.getBasePotionData().getType() == PotionType.WATER;
		}
		return false;
	}

	public static void emptyWaterBottle(final Player player) {
		final PlayerInventory inventory = player.getInventory();
		int index = inventory.first(Material.POTION);

		// Check that the first one found is actually a WATER bottle. We aren't implementing potion bending just yet.
		if (index != -1 && !((PotionMeta) inventory.getItem(index).getItemMeta()).getBasePotionData().getType().equals(PotionType.WATER)) {
			for (int i = 0; i < inventory.getSize(); i++) {
				if (inventory.getItem(i).getType() == Material.POTION) {
					final PotionMeta meta = (PotionMeta) inventory.getItem(i).getItemMeta();
					if (meta.getBasePotionData().getType().equals(PotionType.WATER)) {
						index = i;
						break;
					}
				}
			}
		}

		if (index != -1) {
			final ItemStack item = inventory.getItem(index);
			if (item.getAmount() == 1) {
				inventory.setItem(index, new ItemStack(Material.GLASS_BOTTLE));
			} else {
				item.setAmount(item.getAmount() - 1);
				inventory.setItem(index, item);
				final HashMap<Integer, ItemStack> leftover = inventory.addItem(new ItemStack(Material.GLASS_BOTTLE));

				for (final int left : leftover.keySet()) {
					player.getWorld().dropItemNaturally(player.getLocation(), leftover.get(left));
				}
			}
		}
	}

	public static ItemStack waterBottleItem() {
		final ItemStack water = new ItemStack(Material.POTION);
		final PotionMeta meta = (PotionMeta) water.getItemMeta();

		meta.setBasePotionData(new PotionData(PotionType.WATER));
		water.setItemMeta(meta);

		return water;
	}

	public long getTime() {
		return this.time;
	}

	public void setTime(final long time) {
		this.time = time;
	}

	public long getInterval() {
		return this.interval;
	}

	public void setInterval(final long interval) {
		this.interval = interval;
	}

	public double getRange() {
		return this.range;
	}

	public void setRange(final double range) {
		this.range = range;
	}

	@Override
	public Location getLocation() {
		return this.location;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

	public TempBlock getBlock() {
		return this.block;
	}

	public void setBlock(final TempBlock block) {
		this.block = block;
	}

	@Override
	public String getName() {
		return "Bottlebending";
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

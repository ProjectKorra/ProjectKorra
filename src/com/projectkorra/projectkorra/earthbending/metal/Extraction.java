package com.projectkorra.projectkorra.earthbending.metal;

import java.util.HashSet;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.MetalAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.TempBlock;

public class Extraction extends MetalAbility {

	@Attribute("DoubleChance")
	private int doubleChance;
	@Attribute("TripleChance")
	private int tripleChance;
	@Attribute(Attribute.SELECT_RANGE)
	private int selectRange;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	private Block originBlock;

	public Extraction(final Player player) {
		super(player);

		this.doubleChance = getConfig().getInt("Abilities.Earth.Extraction.DoubleLootChance");
		this.tripleChance = getConfig().getInt("Abilities.Earth.Extraction.TripleLootChance");
		this.cooldown = getConfig().getLong("Abilities.Earth.Extraction.Cooldown");
		this.selectRange = getConfig().getInt("Abilities.Earth.Extraction.SelectRange");

		if (!this.bPlayer.canBend(this)) {
			return;
		}

		this.originBlock = player.getTargetBlock((HashSet<Material>) null, this.selectRange);
		if (this.originBlock == null) {
			return;
		}

		if (!GeneralMethods.isRegionProtectedFromBuild(this, this.originBlock.getLocation()) && !TempBlock.isTempBlock(this.originBlock)) {
			final Material material = this.originBlock.getType();
			Material type = null;

			switch (material) {
				case IRON_ORE:
					this.originBlock.setType(Material.STONE);
					player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.IRON_INGOT, this.getAmount()));
					type = Material.STONE;
					break;
				case GOLD_ORE:
					this.originBlock.setType(Material.STONE);
					player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.GOLD_INGOT, this.getAmount()));
					type = Material.STONE;
					break;
				case NETHER_QUARTZ_ORE:
					this.originBlock.setType(Material.NETHERRACK);
					player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.QUARTZ, this.getAmount()));
					type = Material.NETHERRACK;
					break;
				default:
					return;
			}

			if (type != null) {
				/*
				 * Update the block from Methods.movedearth to Stone otherwise
				 * players can use RaiseEarth > Extraction > Collapse to dupe
				 * the material from the block.
				 */
				if (getMovedEarth().containsKey(this.originBlock)) {
					getMovedEarth().remove(this.originBlock);
				}
			}

			playMetalbendingSound(this.originBlock.getLocation());
			this.start();
			this.bPlayer.addCooldown(this);
			this.remove();
		}

	}

	private int getAmount() {
		final Random rand = new Random();
		return rand.nextInt(99) + 1 <= this.tripleChance ? 3 : rand.nextInt(99) + 1 <= this.doubleChance ? 2 : 1;
	}

	@Override
	public String getName() {
		return "Extraction";
	}

	@Override
	public void progress() {}

	@Override
	public Location getLocation() {
		if (this.originBlock != null) {
			return this.originBlock.getLocation();
		} else if (this.player != null) {
			return this.player.getLocation();
		}
		return null;
	}

	@Override
	public long getCooldown() {
		return this.cooldown;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public int getDoubleChance() {
		return this.doubleChance;
	}

	public void setDoubleChance(final int doubleChance) {
		this.doubleChance = doubleChance;
	}

	public int getTripleChance() {
		return this.tripleChance;
	}

	public void setTripleChance(final int tripleChance) {
		this.tripleChance = tripleChance;
	}

	public int getSelectRange() {
		return this.selectRange;
	}

	public void setSelectRange(final int selectRange) {
		this.selectRange = selectRange;
	}

	public Block getOriginBlock() {
		return this.originBlock;
	}

	public void setOriginBlock(final Block originBlock) {
		this.originBlock = originBlock;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

}

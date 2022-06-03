package com.projectkorra.projectkorra.earthbending.metal;

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

		this.originBlock = player.getTargetBlock(null, this.selectRange);

		if (!GeneralMethods.isRegionProtectedFromBuild(this, this.originBlock.getLocation()) && !TempBlock.isTempBlock(this.originBlock)) {
			this.start();
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
	public void progress() {
		Material type;
		ItemStack item;

		switch (this.originBlock.getType()) {
		case IRON_ORE:
			type = Material.STONE;
			item = new ItemStack(Material.RAW_IRON, this.getAmount() * 2);
			break;
		case GOLD_ORE:
			type = Material.STONE;
			item = new ItemStack(Material.RAW_GOLD, this.getAmount() * 2);
			break;
		case NETHER_QUARTZ_ORE:
			type = Material.NETHERRACK;
			item = new ItemStack(Material.QUARTZ, this.getAmount());
			break;
		case NETHER_GOLD_ORE:
			type = Material.NETHERRACK;
			item = new ItemStack(Material.GOLD_NUGGET, this.getAmount() * 6);
			break;
		case GILDED_BLACKSTONE:
			type = Material.BLACKSTONE;
			item = new ItemStack(Material.GOLD_NUGGET, this.getAmount() * 5);
			break;
		case COPPER_ORE:
			type = Material.STONE;
			item = new ItemStack(Material.RAW_COPPER, this.getAmount() * 2);
			break;
		case DEEPSLATE_COPPER_ORE:
			type = Material.DEEPSLATE;
			item = new ItemStack(Material.RAW_COPPER, this.getAmount() * 2);
			break;
		case DEEPSLATE_GOLD_ORE:
			type = Material.DEEPSLATE;
			item = new ItemStack(Material.RAW_GOLD, this.getAmount() * 2);
			break;
		case DEEPSLATE_IRON_ORE:
			type = Material.DEEPSLATE;
			item = new ItemStack(Material.RAW_IRON, this.getAmount() * 2);
			break;
		default:
			return;
		}

		this.originBlock.setType(type);
		player.getWorld().dropItem(player.getLocation(), item);

		/*
		 * Update the block from EarthAbility.getMovedEarth() to Stone otherwise
		 * players can use RaiseEarth > Extraction > Collapse to dupe
		 * the material from the block.
		 */
		getMovedEarth().remove(this.originBlock);

		playMetalbendingSound(this.originBlock.getLocation());
		this.bPlayer.addCooldown(this);
		this.remove();
	}

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
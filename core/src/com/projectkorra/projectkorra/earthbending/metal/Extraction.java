package com.projectkorra.projectkorra.earthbending.metal;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.MetalAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.region.RegionProtection;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;

public class Extraction extends MetalAbility {

	@Attribute("DoubleChance")
	private double doubleChance;
	@Attribute("TripleChance")
	private double tripleChance;
	@Attribute(Attribute.SELECT_RANGE)
	private int selectRange;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	private int ironGolemDrops;
	private double ironGolemDamage;
	private Material ironGolemDropMaterial;
	private Block originBlock;

	//Whether the server is on at least 1.17 or not. Used to change between raw iron and iron ingots
	private final boolean is117;
	private final Material iron;
	private final Material gold;
	private final Material copper;
	private final Material deepslate;

	public Extraction(final Player player) {
		super(player);

		this.doubleChance = getConfig().getDouble("Abilities.Earth.Extraction.DoubleLootChance");
		this.tripleChance = getConfig().getDouble("Abilities.Earth.Extraction.TripleLootChance");
		this.cooldown = getConfig().getLong("Abilities.Earth.Extraction.Cooldown");
		this.selectRange = getConfig().getInt("Abilities.Earth.Extraction.SelectRange");
		this.ironGolemDrops = getConfig().getInt("Abilities.Earth.Extraction.IronGolem.Drops");
		this.ironGolemDamage = getConfig().getDouble("Abilities.Earth.Extraction.IronGolem.Damage");
		this.ironGolemDropMaterial = Material.IRON_NUGGET;

		this.is117 = GeneralMethods.getMCVersion() >= 1170;
		this.iron = is117 ? Material.getMaterial("RAW_IRON") : Material.IRON_INGOT;
		this.gold = is117 ? Material.getMaterial("RAW_GOLD") : Material.GOLD_INGOT;
		this.copper = Material.getMaterial("RAW_COPPER");
		this.deepslate = Material.getMaterial("DEEPSLATE");

		if (!this.bPlayer.canBend(this)) {
			return;
		}

		this.originBlock = player.getTargetBlock(null, this.selectRange);

		if (!RegionProtection.isRegionProtected(this, this.originBlock.getLocation()) && !TempBlock.isTempBlock(this.originBlock)) {
			this.start();
		}
	}

	private int getAmount() {
		return getAmount(1);
	}

	private int getAmount(int max) {
		final Random rand = new Random();
		int chanceMultiplier = rand.nextDouble() * 100 <= this.tripleChance ? 2 : (rand.nextDouble() * 100 <= this.doubleChance ? 1 : 0);
		int min = chanceMultiplier * max + 1;
		return rand.nextInt(max) + min;
	}

	@Override
	public String getName() {
		return "Extraction";
	}

	@Override
	public void progress() {
		Material type;
		ItemStack item;

		Entity entity = GeneralMethods.getTargetedEntity(this.player, this.selectRange);

		if (entity != null && entity.getType() == EntityType.IRON_GOLEM) {
			player.getWorld().dropItem(player.getLocation(), new ItemStack(this.ironGolemDropMaterial, this.ironGolemDrops));
			DamageHandler.damageEntity(entity, this.ironGolemDamage, this);

			playMetalbendingSound(this.originBlock.getLocation());
			this.bPlayer.addCooldown(this);
			this.remove();
			return;
		}

		switch (this.originBlock.getType().name()) {
		case "IRON_ORE":
			type = Material.STONE;
			item = new ItemStack(iron, this.getAmount(is117 ? 2 : 1 ));
			break;
		case "DEEPSLATE_IRON_ORE":
			type = deepslate;
			item = new ItemStack(iron, this.getAmount(2));
			break;
		case "GOLD_ORE":
			type = Material.STONE;
			item = new ItemStack(gold, this.getAmount( is117 ? 2 : 1 ));
			break;
		case "DEEPSLATE_GOLD_ORE":
			type = deepslate;
			item = new ItemStack(gold, this.getAmount(2));
			break;
		case "NETHER_QUARTZ_ORE":
			type = Material.NETHERRACK;
			item = new ItemStack(Material.QUARTZ, this.getAmount());
			break;
		case "NETHER_GOLD_ORE":
			type = Material.NETHERRACK;
			item = new ItemStack(Material.GOLD_NUGGET, this.getAmount(6));
			break;
		case "GILDED_BLACKSTONE":
			type = Material.BLACKSTONE;
			item = new ItemStack(Material.GOLD_NUGGET, this.getAmount(5));
			break;
		case "COPPER_ORE":
			type = Material.STONE;
			item = new ItemStack(copper, this.getAmount(2));
			break;
		case "DEEPSLATE_COPPER_ORE":
			type = deepslate;
			item = new ItemStack(copper, this.getAmount(2));
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

	public double getDoubleChance() {
		return this.doubleChance;
	}

	public void setDoubleChance(final int doubleChance) {
		this.doubleChance = doubleChance;
	}

	public double getTripleChance() {
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
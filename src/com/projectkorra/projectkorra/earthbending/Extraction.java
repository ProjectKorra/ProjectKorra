package com.projectkorra.projectkorra.earthbending;

import java.util.HashSet;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.MetalAbility;

public class Extraction extends MetalAbility {

	private int doubleChance;
	private int tripleChance;
	private int selectRange;
	private long cooldown;
	private Block originBlock;
	
	public Extraction(Player player) {
		super(player);
		
		this.doubleChance = getConfig().getInt("Abilities.Earth.Extraction.DoubleLootChance");
		this.tripleChance = getConfig().getInt("Abilities.Earth.Extraction.TripleLootChance");
		this.cooldown = getConfig().getLong("Abilities.Earth.Extraction.Cooldown");
		this.selectRange = getConfig().getInt("Abilities.Earth.Extraction.SelectRange");
		
		if (!bPlayer.canBend(this)) {
			return;
		}

		originBlock = player.getTargetBlock((HashSet<Material>) null, selectRange);
		if (originBlock == null) {
			return;
		}
		if (!GeneralMethods.isRegionProtectedFromBuild(this, originBlock.getLocation())) {
			if (bPlayer.canMetalbend() && bPlayer.canBend(this)) {
				Material type = null;

				switch (originBlock.getType()) {
					case IRON_ORE:
						originBlock.setType(Material.STONE);
						player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.IRON_INGOT, getAmount()));
						type = Material.STONE;
						break;
					case GOLD_ORE:
						originBlock.setType(Material.STONE);
						player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.GOLD_INGOT, getAmount()));
						type = Material.STONE;
						break;
					case QUARTZ_ORE:
						originBlock.setType(Material.NETHERRACK);
						player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.QUARTZ, getAmount()));
						type = Material.NETHERRACK;
						break;
					default:
						break;
				}

				if (type != null) {
					/*
					 * Update the block from Methods.movedearth to Stone
					 * otherwise players can use RaiseEarth > Extraction >
					 * Collapse to dupe the material from the block.
					 */
					if (getMovedEarth().containsKey(originBlock)) {
						getMovedEarth().remove(originBlock);
					}
				}

				playMetalbendingSound(originBlock.getLocation());
				start();
				bPlayer.addCooldown(this);
				remove();
			}
		}

	}

	private int getAmount() {
		Random rand = new Random();
		return rand.nextInt(99) + 1 <= tripleChance ? 3 : rand.nextInt(99) + 1 <= doubleChance ? 2 : 1;
	}

	@Override
	public String getName() {
		return "Extraction";
	}

	@Override
	public void progress() {}

	@Override
	public Location getLocation() {
		if (originBlock != null) {
			return originBlock.getLocation();
		} else if (player != null) {
			return player.getLocation();
		}
		return null;
	}

	@Override
	public long getCooldown() {
		return cooldown;
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
		return doubleChance;
	}

	public void setDoubleChance(int doubleChance) {
		this.doubleChance = doubleChance;
	}

	public int getTripleChance() {
		return tripleChance;
	}

	public void setTripleChance(int tripleChance) {
		this.tripleChance = tripleChance;
	}

	public int getSelectRange() {
		return selectRange;
	}

	public void setSelectRange(int selectRange) {
		this.selectRange = selectRange;
	}

	public Block getOriginBlock() {
		return originBlock;
	}

	public void setOriginBlock(Block originBlock) {
		this.originBlock = originBlock;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}
	
}

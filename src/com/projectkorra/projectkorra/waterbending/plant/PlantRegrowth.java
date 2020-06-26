package com.projectkorra.projectkorra.waterbending.plant;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.PlantAbility;
import com.projectkorra.projectkorra.util.TempBlock;

public class PlantRegrowth extends PlantAbility {
	
	private long time;
	private long regrowTime;
	private TempBlock bottomPlant;
	private TempBlock topPlant;
	private Block block;

	public PlantRegrowth(final Player player, final Block block) {
		super(player);
		this.block = block;
		
		this.regrowTime = getConfig().getLong("Abilities.Water.Plantbending.RegrowTime");
		if (this.regrowTime != 0) {
			if (block.getBlockData() instanceof Bisected) {
				
				Bisected bData = (Bisected) block.getBlockData();
				
				if(bData.getHalf() == Bisected.Half.TOP) {
					this.topPlant = new TempBlock(block, Material.AIR);
					this.bottomPlant = new TempBlock(block.getRelative(BlockFace.DOWN), Material.AIR);
				} else {
					this.bottomPlant = new TempBlock(block, Material.AIR);
					this.topPlant = new TempBlock(block.getRelative(BlockFace.UP), Material.AIR);
				}
			} else {
				this.bottomPlant = new TempBlock(block, Material.AIR);
			}

			this.time = System.currentTimeMillis() + this.regrowTime / 2 + (long) (Math.random() * this.regrowTime) / 2;
			this.start();
		}
	}

	@Override
	public void remove() {
		super.remove();
		bottomPlant.revertBlock();
		topPlant.revertBlock();
	}

	@Override
	public void progress() {
		if (this.time < System.currentTimeMillis()) {
			this.remove();
		}
	}

	@Override
	public String getName() {
		return "PlantRegrowth";
	}

	@Override
	public Location getLocation() {
		return this.block != null ? this.block.getLocation() : null;
	}

	@Override
	public boolean isHiddenAbility() {
		return true;
	}

	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	public long getTime() {
		return this.time;
	}

	public void setTime(final long time) {
		this.time = time;
	}

	public long getRegrowTime() {
		return this.regrowTime;
	}

	public void setRegrowTime(final long regrowTime) {
		this.regrowTime = regrowTime;
	}

	public Block getBlock() {
		return this.block;
	}

	public void setBlock(final Block block) {
		this.block = block;
	}

	public TempBlock getPlant1() {
		return bottomPlant;
	}

	public void setPlant1(TempBlock plant1) {
		this.bottomPlant = plant1;
	}

}

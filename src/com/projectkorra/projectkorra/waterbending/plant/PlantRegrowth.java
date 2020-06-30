package com.projectkorra.projectkorra.waterbending.plant;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.util.TempBlock;

public class PlantRegrowth {
	
	private Player player;
	private long regrowTime;
	private TempBlock bottomPlant;
	private TempBlock topPlant;
	private Block block;

	public PlantRegrowth(final Player player, final Block block) {
		this.block = block;
		this.player = player;
		this.regrowTime = ProjectKorra.plugin.getConfig().getLong("Abilities.Water.Plantbending.RegrowTime");
		if (this.regrowTime != 0) {

			if (block.getBlockData() instanceof Bisected) {

				Bisected bData = (Bisected) block.getBlockData();

				if (bData.getHalf() == Bisected.Half.TOP) {
					this.topPlant = new TempBlock(block, Material.AIR.createBlockData(), regrowTime);
					this.bottomPlant = new TempBlock(block.getRelative(BlockFace.DOWN), Material.AIR.createBlockData(), regrowTime);
				} else {
					this.bottomPlant = new TempBlock(block,  Material.AIR.createBlockData(), regrowTime);
					this.topPlant = new TempBlock(block.getRelative(BlockFace.UP),  Material.AIR.createBlockData(), regrowTime);
				}
			} else {
				this.topPlant = null;
				this.bottomPlant = new TempBlock(block, Material.AIR);
			}

		} else {
			Block top, bottom;
			if (block.getBlockData() instanceof Bisected) {

				Bisected bData = (Bisected) block.getBlockData();

				if (bData.getHalf() == Bisected.Half.TOP) {
					top = block;
					bottom = block.getRelative(BlockFace.DOWN);
				} else {
					bottom = block;
					top = block.getRelative(BlockFace.UP);
				}
			} else {
				top = null;
				bottom = block;
			}

			bottom.setType(Material.AIR);
			if (top != null) {
				top.setType(Material.AIR);;
			}
		}
	}

	public long getRegrowTime() {
		return this.regrowTime;
	}

	public TempBlock getBottomPlant() {
		return bottomPlant;
	}

	public TempBlock getTopPlant() {
		return topPlant;
	}

	public Block getBlock() {
		return block;
	}

	public Player getPlayer() {
		return player;
	}
}

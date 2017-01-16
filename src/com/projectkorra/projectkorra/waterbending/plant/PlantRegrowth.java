package com.projectkorra.projectkorra.waterbending.plant;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.PlantAbility;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class PlantRegrowth extends PlantAbility {

	private byte data;
	private long time;
	private long regrowTime;
	private Material type;
	private Block block;

	@SuppressWarnings("deprecation")
	public PlantRegrowth(Player player, Block block) {
		super(player);

		this.regrowTime = getConfig().getLong("Abilities.Water.Plantbending.RegrowTime");
		if (regrowTime != 0) {
			this.block = block;
			this.type = block.getType();
			this.data = block.getData();

			if (block.getType() == Material.DOUBLE_PLANT) {
				if (block.getRelative(BlockFace.DOWN).getType() == Material.DOUBLE_PLANT) {
					this.block = block.getRelative(BlockFace.DOWN);
					this.data = block.getRelative(BlockFace.DOWN).getData();

					block.getRelative(BlockFace.DOWN).setType(Material.AIR);
					block.setType(Material.AIR);
				} else {
					block.setType(Material.AIR);
					block.getRelative(BlockFace.UP).setType(Material.AIR);
				}
			}

			time = System.currentTimeMillis() + regrowTime / 2 + (long) (Math.random() * (double) regrowTime) / 2;
			start();
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public void remove() {
		super.remove();
		if (block.getType() == Material.AIR) {
			block.setType(type);
			block.setData(data);
			if (type == Material.DOUBLE_PLANT) {
				block.getRelative(BlockFace.UP).setType(Material.DOUBLE_PLANT);
				block.getRelative(BlockFace.UP).setData((byte) 10);
			}

		} else {
			GeneralMethods.dropItems(block, GeneralMethods.getDrops(block, type, data, null));
		}
	}

	@Override
	public void progress() {
		if (time < System.currentTimeMillis()) {
			remove();
		}
	}

	@Override
	public String getName() {
		return "PlantRegrowth";
	}

	@Override
	public Location getLocation() {
		return block != null ? block.getLocation() : null;
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

	public byte getData() {
		return data;
	}

	public void setData(byte data) {
		this.data = data;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getRegrowTime() {
		return regrowTime;
	}

	public void setRegrowTime(long regrowTime) {
		this.regrowTime = regrowTime;
	}

	public Material getType() {
		return type;
	}

	public void setType(Material type) {
		this.type = type;
	}

	public Block getBlock() {
		return block;
	}

	public void setBlock(Block block) {
		this.block = block;
	}

}

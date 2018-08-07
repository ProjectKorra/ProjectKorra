package com.projectkorra.projectkorra.waterbending.plant;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.PlantAbility;

public class PlantRegrowth extends PlantAbility {

	private byte data;
	private long time;
	private long regrowTime;
	private Material type;
	private Block block;

	public PlantRegrowth(final Player player, final Block block) {
		super(player);

		this.regrowTime = getConfig().getLong("Abilities.Water.Plantbending.RegrowTime");
		if (this.regrowTime != 0) {
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

			this.time = System.currentTimeMillis() + this.regrowTime / 2 + (long) (Math.random() * this.regrowTime) / 2;
			this.start();
		}
	}

	@Override
	public void remove() {
		super.remove();
		if (this.block.getType() == Material.AIR) {
			this.block.setType(this.type);
			this.block.setData(this.data);
			if (this.type == Material.DOUBLE_PLANT) {
				this.block.getRelative(BlockFace.UP).setType(Material.DOUBLE_PLANT);
				this.block.getRelative(BlockFace.UP).setData((byte) 10);
			}

		} else {
			GeneralMethods.dropItems(this.block, GeneralMethods.getDrops(this.block, this.type, this.data, null));
		}
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

	public byte getData() {
		return this.data;
	}

	public void setData(final byte data) {
		this.data = data;
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

	public Material getType() {
		return this.type;
	}

	public void setType(final Material type) {
		this.type = type;
	}

	public Block getBlock() {
		return this.block;
	}

	public void setBlock(final Block block) {
		this.block = block;
	}

}

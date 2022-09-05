package com.projectkorra.projectkorra.waterbending.plant;

import com.projectkorra.projectkorra.util.TempBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.PlantAbility;

public class PlantRegrowth extends PlantAbility {

	private BlockData data;
	private long time;
	private long regrowTime;
	private Material type;
	private Block block;

	public PlantRegrowth(final Player player, final Block block) {
		super(player);

		this.regrowTime = getConfig().getLong("Abilities.Water.Plantbending.RegrowTime");
		if (this.regrowTime != 0) {
			this.time = System.currentTimeMillis() + this.regrowTime / 2 + (long) (Math.random() * this.regrowTime) / 2;

			new TempBlock(block, Material.AIR.createBlockData(), this.time);
		}
	}

	@Override
	public void progress() {}

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

	public BlockData getData() {
		return this.data;
	}

	public void setData(final BlockData data) {
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
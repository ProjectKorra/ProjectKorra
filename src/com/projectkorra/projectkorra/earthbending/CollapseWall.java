package com.projectkorra.projectkorra.earthbending;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;

public class CollapseWall extends EarthAbility {

	private int selectRange;
	private int height;
	private long cooldown;
	private double radius;
	private Location location;
	private Map<Block, Block> blocks;
	private Map<Block, Integer> baseBlocks;

	public CollapseWall(final Player player) {
		super(player);

		if (!this.bPlayer.canBend(this) || this.bPlayer.isOnCooldown("CollapseWall")) {
			return;
		}

		this.selectRange = getConfig().getInt("Abilities.Earth.Collapse.SelectRange");
		this.height = getConfig().getInt("Abilities.Earth.Collapse.Wall.Height");
		this.radius = getConfig().getDouble("Abilities.Earth.Collapse.Radius");
		this.cooldown = getConfig().getLong("Abilities.Earth.Collapse.Wall.Cooldown");
		this.blocks = new ConcurrentHashMap<>();
		this.baseBlocks = new ConcurrentHashMap<>();

		if (this.bPlayer.isAvatarState()) {
			this.height = getConfig().getInt("Abilities.Avatar.AvatarState.Earth.Collapse.Wall.Height");
		}

		final Block sblock = BlockSource.getEarthSourceBlock(player, this.selectRange, ClickType.SHIFT_DOWN);
		if (sblock == null) {
			this.location = this.getTargetEarthBlock(this.selectRange).getLocation();
		} else {
			this.location = sblock.getLocation();
		}

		for (final Block block : GeneralMethods.getBlocksAroundPoint(this.location, this.radius)) {
			if (this.isEarthbendable(block) && !this.blocks.containsKey(block) && block.getY() >= this.location.getBlockY()) {
				this.getAffectedBlocks(block);
			}
		}

		if (!this.baseBlocks.isEmpty()) {
			this.bPlayer.addCooldown("CollapseWall", this.cooldown);
		}
		for (final Block block : this.baseBlocks.keySet()) {
			new Collapse(player, block.getLocation());
		}
	}

	private void getAffectedBlocks(final Block block) {
		int tall = 0;
		Block baseBlock = block;
		final ArrayList<Block> bendableBlocks = new ArrayList<Block>();
		bendableBlocks.add(block);

		for (int i = 1; i <= this.height; i++) {
			final Block blocki = block.getRelative(BlockFace.DOWN, i);
			if (this.isEarthbendable(blocki)) {
				baseBlock = blocki;
				bendableBlocks.add(blocki);
				tall++;
			} else {
				break;
			}
		}

		this.baseBlocks.put(baseBlock, tall);
		for (final Block blocki : bendableBlocks) {
			this.blocks.put(blocki, baseBlock);
		}
	}

	@Override
	public String getName() {
		return "Collapse";
	}

	@Override
	public void progress() {
	}

	@Override
	public Location getLocation() {
		return this.location;
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

	public int getSelectRange() {
		return this.selectRange;
	}

	public void setSelectRange(final int selectRange) {
		this.selectRange = selectRange;
	}

	public int getHeight() {
		return this.height;
	}

	public void setHeight(final int height) {
		this.height = height;
	}

	public double getRadius() {
		return this.radius;
	}

	public void setRadius(final double radius) {
		this.radius = radius;
	}

	public Map<Block, Block> getBlocks() {
		return this.blocks;
	}

	public Map<Block, Integer> getBaseBlocks() {
		return this.baseBlocks;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

}

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
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.configuration.better.configs.abilities.earth.CollapseConfig;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;

public class CollapseWall extends EarthAbility<CollapseConfig> {

	@Attribute(Attribute.SELECT_RANGE)
	private int selectRange;
	@Attribute(Attribute.HEIGHT)
	private int height;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.RADIUS)
	private double radius;
	private Location location;
	private Map<Block, Block> blocks;
	private Map<Block, Integer> baseBlocks;

	public CollapseWall(final CollapseConfig config, final Player player) {
		super(config, player);

		if (!this.bPlayer.canBend(this) || this.bPlayer.isOnCooldown("CollapseWall")) {
			return;
		}

		this.selectRange = config.SelectRange;
		this.height = config.WallConfig.Height;
		this.radius = config.WallConfig.Radius;
		this.cooldown = config.WallConfig.Cooldown;
		this.blocks = new ConcurrentHashMap<>();
		this.baseBlocks = new ConcurrentHashMap<>();

		if (this.bPlayer.isAvatarState()) {
			this.height = config.WallConfig.AvatarState_Height;
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
			new Collapse(config, player, block.getLocation());
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
	public void progress() {}

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

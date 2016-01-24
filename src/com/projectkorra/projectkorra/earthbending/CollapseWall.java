package com.projectkorra.projectkorra.earthbending;

import java.util.ArrayList;
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
	private ConcurrentHashMap<Block, Block> blocks;
	private ConcurrentHashMap<Block, Integer> baseBlocks;
	
	public CollapseWall(Player player) {
		super(player);
		
		if (!bPlayer.canBend(this) || bPlayer.isOnCooldown("CollapseWall")) {
			return;
		}

		this.selectRange = getConfig().getInt("Abilities.Earth.Collapse.SelectRange");
		this.height = getConfig().getInt("Abilities.Earth.Collapse.Wall.Height");
		this.radius = getConfig().getDouble("Abilities.Earth.Collapse.Radius");
		this.cooldown = getConfig().getLong("Abilities.Earth.Collapse.Wall.Cooldown");
		this.blocks = new ConcurrentHashMap<>();
		this.baseBlocks = new ConcurrentHashMap<>();

		Block sblock = BlockSource.getEarthSourceBlock(player, selectRange, ClickType.SHIFT_DOWN);
		if (sblock == null) {
			location = getTargetEarthBlock(selectRange).getLocation();
		} else {
			location = sblock.getLocation();
		}
		
		for (Block block : GeneralMethods.getBlocksAroundPoint(location, radius)) {
			if (isEarthbendable(block) && !blocks.containsKey(block) && block.getY() >= location.getBlockY()) {
				getAffectedBlocks(block);
			}
		}

		if (!baseBlocks.isEmpty()) {
			bPlayer.addCooldown("CollapseWall", cooldown);
		}
		for (Block block : baseBlocks.keySet()) {
			new Collapse(player, block.getLocation());
		}
	}

	private void getAffectedBlocks(Block block) {
		int tall = 0;
		Block baseBlock = block;
		ArrayList<Block> bendableBlocks = new ArrayList<Block>();
		bendableBlocks.add(block);

		for (int i = 1; i <= height; i++) {
			Block blocki = block.getRelative(BlockFace.DOWN, i);
			if (isEarthbendable(blocki)) {
				baseBlock = blocki;
				bendableBlocks.add(blocki);
				tall++;
			} else {
				break;
			}
		}
		
		baseBlocks.put(baseBlock, tall);
		for (Block blocki : bendableBlocks) {
			blocks.put(blocki, baseBlock);
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
		return location;
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

	public int getSelectRange() {
		return selectRange;
	}

	public void setSelectRange(int selectRange) {
		this.selectRange = selectRange;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public ConcurrentHashMap<Block, Block> getBlocks() {
		return blocks;
	}

	public ConcurrentHashMap<Block, Integer> getBaseBlocks() {
		return baseBlocks;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

}

package com.projectkorra.projectkorra.util;

import com.projectkorra.projectkorra.util.BlockSource.BlockSourceType;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BlockSourceInformation {
	private Player player;
	private Block block;
	private BlockSourceType sourceType;
	private ClickType clickType;
	private long creationTime;

	public BlockSourceInformation(Player player, Block block, BlockSourceType sourceType, ClickType clickType) {
		this.player = player;
		this.block = block;
		this.sourceType = sourceType;
		this.creationTime = System.currentTimeMillis();
		this.clickType = clickType;
	}

	public Block getBlock() {
		return block;
	}

	public void setBlock(Block block) {
		this.block = block;
	}

	public BlockSourceType getSourceType() {
		return sourceType;
	}

	public void setSourceType(BlockSourceType sourceType) {
		this.sourceType = sourceType;
	}

	public long getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public ClickType getClickType() {
		return clickType;
	}

	public void setClickType(ClickType clickType) {
		this.clickType = clickType;
	}
}

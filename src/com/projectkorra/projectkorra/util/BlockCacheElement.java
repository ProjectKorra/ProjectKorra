package com.projectkorra.projectkorra.util;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BlockCacheElement {
	private Player player;
	private Block block;
	private String ability;
	private boolean allowed;
	private long time;

	public BlockCacheElement(Player player, Block block, String ability, boolean allowed, long time) {
		this.player = player;
		this.block = block;
		this.ability = ability;
		this.allowed = allowed;
		this.time = time;
	}

	public String getAbility() {
		return ability;
	}

	public Block getBlock() {
		return block;
	}

	public Player getPlayer() {
		return player;
	}

	public long getTime() {
		return time;
	}

	public boolean isAllowed() {
		return allowed;
	}

	public void setAbility(String ability) {
		this.ability = ability;
	}

	public void setAllowed(boolean allowed) {
		this.allowed = allowed;
	}

	public void setBlock(Block block) {
		this.block = block;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public void setTime(long time) {
		this.time = time;
	}

}

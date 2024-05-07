package com.projectkorra.projectkorra.util;

import com.projectkorra.projectkorra.ability.CoreAbility;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BlockCacheElement {

	private Player player;
	private Block block;
	private CoreAbility ability;
	private boolean allowed;
	private long time;

	public BlockCacheElement(final Player player, final Block block, final CoreAbility ability, final boolean allowed, final long time) {
		this.player = player;
		this.block = block;
		this.ability = ability;
		this.allowed = allowed;
		this.time = time;
	}

	public CoreAbility getAbility() {
		return this.ability;
	}

	public Block getBlock() {
		return this.block;
	}

	public Player getPlayer() {
		return this.player;
	}

	public long getTime() {
		return this.time;
	}

	public boolean isAllowed() {
		return this.allowed;
	}

	public void setAbility(final CoreAbility ability) {
		this.ability = ability;
	}

	public void setAllowed(final boolean allowed) {
		this.allowed = allowed;
	}

	public void setBlock(final Block block) {
		this.block = block;
	}

	public void setPlayer(final Player player) {
		this.player = player;
	}

	public void setTime(final long time) {
		this.time = time;
	}

}

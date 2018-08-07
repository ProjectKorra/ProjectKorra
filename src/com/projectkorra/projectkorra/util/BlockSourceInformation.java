package com.projectkorra.projectkorra.util;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.util.BlockSource.BlockSourceType;

/**
 * The information for a bending source block.
 *
 * @author kingbirdy
 */
public class BlockSourceInformation {
	private Player player;
	private Block block;
	private BlockSourceType sourceType;
	private ClickType clickType;
	private long creationTime;

	/**
	 * Creates a new BlockSourceInformation.
	 *
	 * @param player The player the source belongs to
	 * @param block The source block
	 * @param sourceType What {@link BlockSourceType source type} the block is
	 * @param clickType
	 */
	public BlockSourceInformation(final Player player, final Block block, final BlockSourceType sourceType, final ClickType clickType) {
		this.player = player;
		this.block = block;
		this.sourceType = sourceType;
		this.creationTime = System.currentTimeMillis();
		this.clickType = clickType;
	}

	/**
	 * Gets the source block.
	 *
	 * @return The source block
	 */
	public Block getBlock() {
		return this.block;
	}

	/**
	 * Sets a new source block.
	 *
	 * @param block The new source block.
	 */
	public void setBlock(final Block block) {
		this.block = block;
	}

	/**
	 * Get what {@link BlockSourceType source type} the source is.
	 *
	 * @return The block's source type
	 */
	public BlockSourceType getSourceType() {
		return this.sourceType;
	}

	/**
	 * Sets the source type.
	 *
	 * @param sourceType The new source type.
	 */
	public void setSourceType(final BlockSourceType sourceType) {
		this.sourceType = sourceType;
	}

	/**
	 * Gets when the source was created.
	 *
	 * @return The source's creation time
	 */
	public long getCreationTime() {
		return this.creationTime;
	}

	/**
	 * Sets the source's creation time.
	 *
	 * @param creationTime The new creation time
	 */
	public void setCreationTime(final long creationTime) {
		this.creationTime = creationTime;
	}

	/**
	 * Get the player the source belongs to.
	 *
	 * @return The player the source belongs to
	 */
	public Player getPlayer() {
		return this.player;
	}

	/**
	 * Sets the player the source belongs to.
	 *
	 * @param player The player the source will belong to
	 */
	public void setPlayer(final Player player) {
		this.player = player;
	}

	/**
	 * Gets the {@link ClickType} used to select the source.
	 *
	 * @return The ClickType used to select the source
	 */
	public ClickType getClickType() {
		return this.clickType;
	}

	/**
	 * Sets the source's {@link ClickType}.
	 *
	 * @param clickType The ClickType to set
	 */
	public void setClickType(final ClickType clickType) {
		this.clickType = clickType;
	}
}

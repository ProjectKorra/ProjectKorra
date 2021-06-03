package com.projectkorra.projectkorra.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Called as a test event for checking if other plugins cancel the block breaking
 * @see {@link com.projectkorra.projectkorra.GeneralMethods#isRegionProtectedFromBuildPostCache}
 */
public class SimulatedBlockBreakEvent extends BlockBreakEvent {

	public SimulatedBlockBreakEvent(Block theBlock, Player player) {
		super(theBlock, player);
	}

}

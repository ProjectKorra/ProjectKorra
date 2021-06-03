package com.projectkorra.projectkorra.event;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Called as a test event for checking if other plugins cancel the block placing
 * @see {@link com.projectkorra.projectkorra.GeneralMethods#isRegionProtectedFromBuildPostCache}
 */
public class SimulatedBlockPlaceEvent extends BlockPlaceEvent {

	public SimulatedBlockPlaceEvent(Block placedBlock, BlockState replacedBlockState, Block placedAgainst, ItemStack itemInHand, Player thePlayer, boolean canBuild, EquipmentSlot hand) {
		super(placedBlock, replacedBlockState, placedAgainst, itemInHand, thePlayer, canBuild, hand);
	}

}

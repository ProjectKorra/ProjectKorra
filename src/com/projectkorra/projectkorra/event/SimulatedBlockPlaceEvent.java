package com.projectkorra.projectkorra.event;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Called as a test event for checking if other plugins cancel the block placing
 * @see {@link com.projectkorra.projectkorra.GeneralMethods#isRegionProtectedFromBuildPostCache}
 */
public class SimulatedBlockPlaceEvent extends BlockPlaceEvent {

	public SimulatedBlockPlaceEvent(Block block, Player player) {
		super(block, block.getState(), block.getRelative(BlockFace.DOWN), new ItemStack(Material.PETRIFIED_OAK_SLAB), player, true, EquipmentSlot.HAND);
	}

}

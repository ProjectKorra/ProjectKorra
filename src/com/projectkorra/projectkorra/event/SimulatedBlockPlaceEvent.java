package com.projectkorra.projectkorra.event;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Called as a test event for checking if other plugins cancel the block placing
 * @see {@link com.projectkorra.projectkorra.GeneralMethods#isRegionProtectedFromBuildPostCache}
 */
public class SimulatedBlockPlaceEvent extends BlockPlaceEvent {
	
	private static final ItemStack ITEM_KEY = new ItemStack(Material.PETRIFIED_OAK_SLAB);

	public SimulatedBlockPlaceEvent(Block block, Player player) {
		super(block, block.getState(), block.getRelative(BlockFace.DOWN), ITEM_KEY, player, true, EquipmentSlot.HAND);
	}

}

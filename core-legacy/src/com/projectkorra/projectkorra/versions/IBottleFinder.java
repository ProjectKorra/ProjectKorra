package com.projectkorra.projectkorra.versions;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public interface IBottleFinder {

    int findWaterBottle(final PlayerInventory inventory);

    ItemStack createWaterBottle();
}
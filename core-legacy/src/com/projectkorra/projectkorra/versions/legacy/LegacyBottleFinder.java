package com.projectkorra.projectkorra.versions.legacy;

import com.projectkorra.projectkorra.versions.IBottleFinder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

public class LegacyBottleFinder implements IBottleFinder {

    @Override
    public int findWaterBottle(PlayerInventory inventory) {
        int index = inventory.first(Material.POTION);

        // Check that the first one found is actually a WATER bottle. We aren't implementing potion bending just yet.
        if (index != -1) {
            int aux = index;
            index = -1;
            for (int i = aux; i < inventory.getSize(); i++) {
                if (inventory.getItem(i) != null && inventory.getItem(i).getType() == Material.POTION && inventory.getItem(i).hasItemMeta()) {
                    final PotionMeta meta = (PotionMeta) inventory.getItem(i).getItemMeta();
                    if (meta.getBasePotionData().getType().equals(PotionType.WATER)) {
                        index = i;
                        break;
                    }
                }
            }
        }

        return index;
    }

    @Override
    public ItemStack createWaterBottle() {
        final ItemStack water = new ItemStack(Material.POTION);
        final PotionMeta meta = (PotionMeta) water.getItemMeta();

        meta.setBasePotionData(new PotionData(PotionType.WATER));
        water.setItemMeta(meta);

        return water;
    }
}

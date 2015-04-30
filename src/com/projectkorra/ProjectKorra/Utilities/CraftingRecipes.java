package com.projectkorra.ProjectKorra.Utilities;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ShapedRecipe;

import com.projectkorra.ProjectKorra.ProjectKorra;

public class CraftingRecipes {
    
    static ProjectKorra plugin;
    
    public CraftingRecipes(ProjectKorra plugin) {
        CraftingRecipes.plugin = plugin;
        registerRecipes();
    }
    
    static FileConfiguration config = ProjectKorra.plugin.getConfig();
    
    public static void registerRecipes() {
        if (config.getBoolean("Properties.CustomItems.GrapplingHook.Enable")) {
            plugin.getServer().addRecipe(ironHookRecipe);
            plugin.getServer().addRecipe(goldHookRecipe);
        }
    }
    
    static ShapedRecipe ironHookRecipe = new ShapedRecipe(GrapplingHookAPI.createHook(config.getInt("Properties.CustomItems.GrapplingHook.IronUses")))
            .shape(" **", " &*", "   ")
            .setIngredient('*', Material.IRON_INGOT)
            .setIngredient('&', Material.FISHING_ROD);
    
    static ShapedRecipe goldHookRecipe = new ShapedRecipe(GrapplingHookAPI.createHook(config.getInt("Properties.CustomItems.GrapplingHook.GoldUses")))
            .shape(" **", " &*", "   ")
            .setIngredient('*', Material.GOLD_INGOT)
            .setIngredient('&', Material.FISHING_ROD);
    
}

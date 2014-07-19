package com.projectkorra.ProjectKorra.earthbending;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;

public class Extraction {
	
	private static Map<String, Long> cooldowns = new HashMap<String, Long>();
	
	private long cooldown = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.Extraction.Cooldown");
	private static int doublechance = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.Extraction.DoubleLootChance");
	private static int triplechance = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.Extraction.TripleLootChance");
	
	public Extraction(Player player) {
		if (cooldowns.containsKey(player.getName())) {
			if (cooldowns.get(player.getName()) + cooldown >= System.currentTimeMillis()) {
				return;
			} else {
				cooldowns.remove(player.getName());
			}
		}
		
		Block block = player.getTargetBlock(null, 5);
		if (block == null) {
			return;
		}
		if (!Methods.isRegionProtectedFromBuild(player, "Extraction", block.getLocation())) {
			if (Methods.canMetalbend(player) && Methods.canBend(player.getName(), "Extraction")) {
				Random rand = new Random();
				if (rand.nextInt(99) + 1 <= triplechance) {
					if (block.getType() == Material.IRON_ORE) {
						block.setType(Material.STONE);
						player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.IRON_INGOT, 3));
					}
					if (block.getType() == Material.GOLD_ORE){
						block.setType(Material.STONE);
						player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.GOLD_INGOT, 3));
					}
					if (block.getType() == Material.QUARTZ_ORE) {
						block.setType(Material.NETHERRACK);
						player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.QUARTZ, 3));
					}
					cooldowns.put(player.getName(), System.currentTimeMillis());
					return;
				}
				else if (rand.nextInt(99) + 1 <= doublechance) {
					if (block.getType() == Material.IRON_ORE) {
						block.setType(Material.STONE);
						player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.IRON_INGOT, 2));
					}
					if (block.getType() == Material.GOLD_ORE){
						block.setType(Material.STONE);
						player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.GOLD_INGOT, 2));
					}
					if (block.getType() == Material.QUARTZ_ORE) {
						block.setType(Material.NETHERRACK);
						player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.QUARTZ, 2));
					}
					cooldowns.put(player.getName(), System.currentTimeMillis());
					return;
				} else {
					if (block.getType() == Material.IRON_ORE) {
						block.setType(Material.STONE);
						player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.IRON_INGOT));
					}
					if (block.getType() == Material.GOLD_ORE){
						block.setType(Material.STONE);
						player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.GOLD_INGOT));
					}
					if (block.getType() == Material.QUARTZ_ORE) {
						block.setType(Material.NETHERRACK);
						player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.QUARTZ));
					}
					cooldowns.put(player.getName(), System.currentTimeMillis());
					return;
				}
			}
		}
	}

}

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
				switch(block.getType()) {
				case IRON_ORE:
					block.setType(Material.STONE);
					player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.IRON_INGOT, getAmount()));
					break;
				case GOLD_ORE:
					block.setType(Material.STONE);
					player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.GOLD_INGOT, getAmount()));
					break;
				case QUARTZ_ORE:
					block.setType(Material.NETHERRACK);
					player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.QUARTZ, getAmount()));
					break;
				default:
					break; // shouldn't happen.
				}
				cooldowns.put(player.getName(), System.currentTimeMillis());
			}
		}

	}
	
	private int getAmount() {
		Random rand = new Random();
		return rand.nextInt(99) + 1 <= triplechance ? 3 : rand.nextInt(99) + 1 <= doublechance ? 2: 0;
	}

}

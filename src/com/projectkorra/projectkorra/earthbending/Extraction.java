package com.projectkorra.projectkorra.earthbending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Random;

public class Extraction {

	private long cooldown = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.Extraction.Cooldown");
	private static int doublechance = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.Extraction.DoubleLootChance");
	private static int triplechance = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.Extraction.TripleLootChance");

	public Extraction(Player player) {
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
		if (bPlayer.isOnCooldown("Extraction"))
			return;

		Block block = player.getTargetBlock((HashSet<Material>) null, 5);
		if (block == null) {
			return;
		}
		if (!GeneralMethods.isRegionProtectedFromBuild(player, "Extraction", block.getLocation())) {
			if (EarthMethods.canMetalbend(player) && GeneralMethods.canBend(player.getName(), "Extraction")) {
				Material type = null;

				switch (block.getType()) {
					case IRON_ORE:
						block.setType(Material.STONE);
						player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.IRON_INGOT, getAmount()));
						type = Material.STONE;
						break;
					case GOLD_ORE:
						block.setType(Material.STONE);
						player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.GOLD_INGOT, getAmount()));
						type = Material.STONE;
						break;
					case QUARTZ_ORE:
						block.setType(Material.NETHERRACK);
						player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.QUARTZ, getAmount()));
						type = Material.NETHERRACK;
						break;
					default:
						break; // shouldn't happen.
				}

				if (type != null) {
					/*
					 * Update the block from Methods.movedearth to Stone
					 * otherwise players can use RaiseEarth > Extraction >
					 * Collapse to dupe the material from the block.
					 */
					if (EarthMethods.movedearth.containsKey(block)) {
						EarthMethods.movedearth.remove(block);
					}
				}

				EarthMethods.playMetalbendingSound(block.getLocation());
				bPlayer.addCooldown("Extraction", cooldown);
			}
		}

	}

	private int getAmount() {
		Random rand = new Random();
		return rand.nextInt(99) + 1 <= triplechance ? 3 : rand.nextInt(99) + 1 <= doublechance ? 2 : 1;
	}

}

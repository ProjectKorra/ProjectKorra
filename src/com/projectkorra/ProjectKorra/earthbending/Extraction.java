package com.projectkorra.ProjectKorra.earthbending;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;

public class Extraction {

	private long cooldown = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.Extraction.Cooldown");
	private static int doublechance = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.Extraction.DoubleLootChance");
	private static int triplechance = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.Extraction.TripleLootChance");

	public Extraction(Player player) {
		BendingPlayer bPlayer = Methods.getBendingPlayer(player.getName());
		if (bPlayer.isOnCooldown("Extraction")) return;

		Block block = player.getTargetBlock(null, 5);
		if (block == null) {
			return;
		}
		if (!Methods.isRegionProtectedFromBuild(player, "Extraction", block.getLocation())) {
			if (Methods.canMetalbend(player) && Methods.canBend(player.getName(), "Extraction")) {
				Material type = null;
				
				switch(block.getType()) {
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
				
				if(type != null) {
					/* Update the block from Methods.movedearth to Stone otherwise
					 * players can use RaiseEarth > Extraction > Collapse
					 * to dupe the material from the block.
					 * */
					if(Methods.movedearth.containsKey(block)) {
						Methods.movedearth.remove(block);
					}
				}
				
				Methods.playMetalbendingSound(block.getLocation());
				bPlayer.addCooldown("Extraction", cooldown);
			}
		}

	}
	
	private int getAmount() {
		Random rand = new Random();
		return rand.nextInt(99) + 1 <= triplechance ? 3 : rand.nextInt(99) + 1 <= doublechance ? 2: 1;
	}

}

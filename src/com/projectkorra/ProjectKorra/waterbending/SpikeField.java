package com.projectkorra.ProjectKorra.waterbending;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;

public class SpikeField {

	private static long cooldown = ProjectKorra.plugin.getConfig().getLong("Abilities.Water.IceSpike.Cooldown");

	private static int radius = 6;
	public static int numofspikes = ((radius * 2) * (radius * 2)) / 16;

	Random ran = new Random();
	private int damage = 2;
	private Vector thrown = new Vector(0, 1, 0);

	public SpikeField(Player p) {
		BendingPlayer bPlayer = Methods.getBendingPlayer(p.getName());

		if (bPlayer.isOnCooldown("IceSpike")) return;
		int locX = p.getLocation().getBlockX();
		int locY = p.getLocation().getBlockY();
		int locZ = p.getLocation().getBlockZ();
		List<Block> iceblocks = new ArrayList<Block>();
		for (int x = -(radius - 1); x <= (radius - 1); x++) {
			for (int z = -(radius - 1); z <= (radius - 1); z++) {
				for (int y = -1; y <= 1; y++) {
					Block testblock = p.getWorld().getBlockAt(locX + x,	locY + y, locZ + z);
					if (testblock.getType() == Material.ICE
							&& testblock.getRelative(BlockFace.UP).getType() == Material.AIR
							&& !(testblock.getX() == p.getEyeLocation()
							.getBlock().getX() && testblock.getZ() == p
							.getEyeLocation().getBlock().getZ())) {
						iceblocks.add(testblock);
					}
				}
			}
		}

		List<Entity> entities = Methods.getEntitiesAroundPoint(p.getLocation(),	radius);

		for (int i = 0; i < numofspikes; i++) {
			if (iceblocks.isEmpty())
				return;

			Entity target = null;
			Block targetblock = null;
			for (Entity entity : entities) {
				if (entity instanceof LivingEntity && entity.getEntityId() != p.getEntityId()) {
					for (Block block : iceblocks) {
						if (block.getX() == entity.getLocation().getBlockX() && block.getZ() == entity.getLocation().getBlockZ()) {
							target = entity;
							targetblock = block;
							break;
						}
					}
				} else {
					continue;
				}
			}

			if (target != null) {
				entities.remove(target);
			} else {
				targetblock = iceblocks.get(ran.nextInt(iceblocks.size()));
			}
			if (targetblock.getRelative(BlockFace.UP).getType() != Material.ICE) {
				new IceSpike(p, targetblock.getLocation(), damage, thrown, cooldown);
				bPlayer.addCooldown("IceSpike", cooldown);
				iceblocks.remove(targetblock);
			}
		}
	}
}
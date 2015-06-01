package com.projectkorra.ProjectKorra.waterbending;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;

public class Plantbending {

	private static ConcurrentHashMap<Integer, Plantbending> instances = new ConcurrentHashMap<Integer, Plantbending>();

	private static final long regrowtime = ProjectKorra.plugin.getConfig().getLong("Abilities.Water.Plantbending.RegrowTime");
	private static int ID = Integer.MIN_VALUE;

	private Block block;
	private Material type;
	private byte data;
	private long time;
	private int id;

	@SuppressWarnings("deprecation")
	public Plantbending(Block block) {
		if (regrowtime != 0) {
			this.block = block;
			type = block.getType();
			data = block.getData();
			time = System.currentTimeMillis() + regrowtime / 2 + (long) (Math.random() * (double) regrowtime) / 2;
			id = ID;
			instances.put(id, this);
			if (ID >= Integer.MAX_VALUE) {
				ID = Integer.MIN_VALUE;
			} else {
				ID++;
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void revert() {
		if (block.getType() == Material.AIR) {
			block.setType(type);
			block.setData(data);
		} else {
			GeneralMethods.dropItems(block, GeneralMethods.getDrops(block, type, data, null));
		}
		instances.remove(id);
	}

	public static void regrow() {
		for (int id : instances.keySet()) {
			Plantbending plantbending = instances.get(id);
			if (plantbending.time < System.currentTimeMillis()) {
				plantbending.revert();
			}
		}
	}

	public static void regrowAll() {
		for (int id : instances.keySet())
			instances.get(id).revert();
	}
}

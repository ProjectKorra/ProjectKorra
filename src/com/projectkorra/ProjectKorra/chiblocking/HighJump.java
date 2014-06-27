package com.projectkorra.ProjectKorra.chiblocking;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;

public class HighJump {

	private int jumpheight = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.HighJump.Height");
	private long cooldown = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.HighJump.Cooldown");

	 private Map<String, Long> cooldowns = new HashMap<String, Long>();

	public HighJump(Player p) {
		if (cooldowns.containsKey(p.getName())) {
			if (cooldowns.get(p.getName()) + cooldown >= System.currentTimeMillis()) {
				return;
			} else {
				cooldowns.remove(p.getName());
			}
		}

		jump(p);
	}

	private void jump(Player p) {
		if (!Methods.isSolid(p.getLocation().getBlock()
				.getRelative(BlockFace.DOWN)))
			return;
		Vector vec = p.getVelocity();
		vec.setY(jumpheight);
		p.setVelocity(vec);
		// cooldowns.put(p.getName(), System.currentTimeMillis());
		cooldowns.put(p.getName(), System.currentTimeMillis());
		return;
	}

	public static String getDescription() {
		return "To use this ability, simply click. You will jump quite high. This ability has a short cooldown.";
	}
}

package com.projectkorra.ProjectKorra.chiblocking;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;

public class HighJump {

	private int jumpheight = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.HighJump.Height");
	private long cooldown = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.HighJump.Cooldown");

	public HighJump(Player p) {
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(p.getName());
		
		if (bPlayer.isOnCooldown("HighJump")) return;
		jump(p);
		bPlayer.addCooldown("HighJump", cooldown);
		
	}

	private void jump(Player p) {
		if (!GeneralMethods.isSolid(p.getLocation().getBlock()
				.getRelative(BlockFace.DOWN)))
			return;
		Vector vec = p.getVelocity();
		vec.setY(jumpheight);
		p.setVelocity(vec);
		return;
	}

	public static String getDescription() {
		return "To use this ability, simply click. You will jump quite high. This ability has a short cooldown.";
	}
}

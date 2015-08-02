package com.projectkorra.projectkorra.chiblocking;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.waterbending.WaterArmsWhip;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class HighJump {

	private int jumpheight = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.HighJump.Height");
	private long cooldown = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.HighJump.Cooldown");

	public HighJump(Player p) {
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(p.getName());

		if (bPlayer.isOnCooldown("HighJump"))
			return;
		if (WaterArmsWhip.grabbedEntities.containsKey(p)) {
			WaterArmsWhip waw = WaterArmsWhip.instances.get(WaterArmsWhip.grabbedEntities.get(p));
			if (waw != null) {
				waw.setGrabbed(false);
			}
		}
		jump(p);
		bPlayer.addCooldown("HighJump", cooldown);

	}

	private void jump(Player p) {
		if (!GeneralMethods.isSolid(p.getLocation().getBlock().getRelative(BlockFace.DOWN)))
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

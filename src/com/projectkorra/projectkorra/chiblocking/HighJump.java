package com.projectkorra.projectkorra.chiblocking;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.waterbending.multiabilities.WaterArmsWhip;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class HighJump extends ChiAbility {

	@Attribute(Attribute.HEIGHT)
	private int height;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;

	public HighJump(Player player) {
		super(player);
		if (!bPlayer.canBend(this)) {
			return;
		}
		this.height = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.HighJump.Height");
		this.cooldown = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.HighJump.Cooldown");
		start();
	}

	private void jump(Player p) {
		if (!GeneralMethods.isSolid(p.getLocation().getBlock().getRelative(BlockFace.DOWN))) {
			remove();
			return;
		}
		Vector vec = p.getVelocity();
		vec.setY(height);
		p.setVelocity(vec);
		bPlayer.addCooldown(this);
		return;
	}

	@Override
	public void progress() {
		if (bPlayer.isOnCooldown(this)) {
			remove();
			return;
		}
		jump(player);
		WaterArmsWhip waw = WaterArmsWhip.getGrabbedEntities().get(player);
		if (waw != null) {
			waw.setGrabbed(false);
		}

	}

	@Override
	public String getName() {
		return "HighJump";
	}

	@Override
	public Location getLocation() {
		return player != null ? player.getLocation() : null;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

}

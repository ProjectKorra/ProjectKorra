package com.projectkorra.projectkorra.chiblocking;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.waterbending.multiabilities.WaterArmsWhip;

public class HighJump extends ChiAbility {

	@Attribute(Attribute.HEIGHT)
	private int height;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;

	public HighJump(final Player player) {
		super(player);
		if (!this.bPlayer.canBend(this)) {
			return;
		}
		this.height = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.HighJump.Height");
		this.cooldown = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.HighJump.Cooldown");
		this.start();
	}

	private void jump(final Player p) {
		if (!GeneralMethods.isSolid(p.getLocation().getBlock().getRelative(BlockFace.DOWN))) {
			this.remove();
			return;
		}
		final Vector vec = p.getVelocity();
		vec.setY(this.height);
		GeneralMethods.setVelocity((Ability)this,p, vec);
		this.bPlayer.addCooldown(this);
		return;
	}

	@Override
	public void progress() {
		if (this.bPlayer.isOnCooldown(this)) {
			this.remove();
			return;
		}
		this.jump(this.player);
		final WaterArmsWhip waw = WaterArmsWhip.getGrabbedEntities().get(this.player);
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
		return this.player != null ? this.player.getLocation() : null;
	}

	@Override
	public long getCooldown() {
		return this.cooldown;
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
		return this.height;
	}

	public void setHeight(final int height) {
		this.height = height;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

}

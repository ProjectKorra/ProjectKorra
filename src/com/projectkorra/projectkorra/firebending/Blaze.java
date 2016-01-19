package com.projectkorra.projectkorra.firebending;

import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.avatar.AvatarState;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Blaze extends FireAbility {
	
	private int arc;
	private long cooldown;
	private double range;
	private double speed;
	
	public Blaze(Player player) {
		super(player);
		
		this.speed = 2;
		this.cooldown = getConfig().getLong("Abilities.Fire.Blaze.Cooldown");
		this.arc = getConfig().getInt("Abilities.Fire.Blaze.Arc");
		this.range = getConfig().getDouble("Abilities.Fire.Blaze.Range");
		
		if (!bPlayer.canBend(this) || bPlayer.isOnCooldown("BlazeArc")) {
			return;
		}
		
		this.range = getDayFactor(range);
		this.range = AvatarState.getValue(range, player);
		this.arc = (int) getDayFactor(arc);
		Location location = player.getLocation();

		for (int i = -arc; i <= arc; i += speed) {
			double angle = Math.toRadians(i);
			Vector direction = player.getEyeLocation().getDirection().clone();
			double x, z, vx, vz;
			
			x = direction.getX();
			z = direction.getZ();

			vx = x * Math.cos(angle) - z * Math.sin(angle);
			vz = x * Math.sin(angle) + z * Math.cos(angle);

			direction.setX(vx);
			direction.setZ(vz);

			new BlazeArc(player, location, direction, range);
		}

		start();
		bPlayer.addCooldown("BlazeArc", cooldown);
		remove();
	}

	@Override
	public String getName() {
		return "Blaze";
	}

	@Override
	public void progress() {
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
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

}

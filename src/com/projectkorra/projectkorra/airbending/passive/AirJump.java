package com.projectkorra.projectkorra.airbending.passive;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.util.Attribute;
import com.projectkorra.projectkorra.util.Attribute.Attributable;

public class AirJump extends AirAbility implements PassiveAbility, Attributable{
	
	private static List<Player> jumped = new ArrayList<>();
	
	private double speed;
	private long cooldown;
	private static Attribute<Double> speedA;
	private static Attribute<Long> cooldownA;

	public AirJump(Player player) {
		super(player);
		if (!bPlayer.canBendIgnoreBinds(this)) {
			return;
		}
		
		speed = speedA.getModified(bPlayer);
		cooldown = cooldownA.getModified(bPlayer);
		player.setVelocity(player.getLocation().getDirection().clone().normalize().multiply(speed));
		player.setAllowFlight(false);
		start();
	}

	@Override
	public void progress() {
		remove();
		bPlayer.addCooldown(this);
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public String getName() {
		return "AirJump";
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	@Override
	public boolean isInstantiable() {
		return true;
	}

	@Override
	public void registerAttributes() {
		speedA = new Attribute<Double>(this, "speed", getConfig().getDouble("Abilities.Air.Passive.AirJump.Speed"));
		cooldownA = new Attribute<Long>(this, "cooldown", getConfig().getLong("Abilities.Air.Passive.AirJump.Cooldown"));
	}

	public static List<Player> getJumped() {
		return jumped;
	}
}

package com.projectkorra.projectkorra.firebending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.PKMethods;
import com.projectkorra.projectkorra.ability.FireAbility;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashSet;

public class HeatControlExtinguish extends FireAbility {

	private double range;
	private double radius;
	private long cooldown;
	private Location location;
	
	public HeatControlExtinguish(Player player) {
		super(player);
		
		if (!bPlayer.canBend(this) || bPlayer.isOnCooldown("HeatControlExtinguish")) {
			return;
		}

		this.range = getConfig().getDouble("Abilities.Fire.HeatControl.Extinguish.Range");
		this.radius = getConfig().getDouble("Abilities.Fire.HeatControl.Extinguish.Radius");
		this.cooldown = getConfig().getLong("Abilities.Fire.HeatControl.Extinguish.Cooldown");
		
		this.range = getDayFactor(this.range);
		this.radius = getDayFactor(this.radius);
		if (isMeltable(player.getTargetBlock((HashSet<Material>) null, (int) range))) {
			new HeatControlMelt(player);
			return;
		}
		
		location = player.getTargetBlock((HashSet<Material>) null, (int) range).getLocation();
		for (Block block : PKMethods.getBlocksAroundPoint(location, radius)) {
			Material mat = block.getType();
			if (mat != Material.FIRE) {
				continue;
			} else if (PKMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
				continue;
			} else if (block.getType() == Material.FIRE) {
				block.setType(Material.AIR);
				block.getWorld().playEffect(block.getLocation(), Effect.EXTINGUISH, 0);
			}
		}

		bPlayer.addCooldown("HeatControlExtinguish", cooldown);
	}

	public static boolean canBurn(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return true;
		} else if (bPlayer.getBoundAbilityName().equals("HeatControl") || hasAbility(player, FireJet.class)) {
			player.setFireTicks(-1);
			return false;
		} else if (player.getFireTicks() > 80 && bPlayer.canBendPassive(Element.FIRE)) {
			player.setFireTicks(80);
		}
		return true;
	}

	@Override
	public String getName() {
		return "HeatControl";
	}

	@Override
	public void progress() { 
	}

	@Override
	public Location getLocation() {
		return location;
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

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	
}

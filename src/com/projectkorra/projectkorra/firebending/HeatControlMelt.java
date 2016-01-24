package com.projectkorra.projectkorra.firebending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.waterbending.PhaseChangeMelt;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class HeatControlMelt extends FireAbility {

	private double range = getConfig().getDouble("Abilities.Fire.HeatControl.Melt.Range");
	private double radius = getConfig().getDouble("Abilities.Fire.HeatControl.Melt.Radius");
	private Location location;
	
	public HeatControlMelt(Player player) {
		super(player);
		
		this.range = getConfig().getDouble("Abilities.Fire.HeatControl.Melt.Range");
		this.radius = getConfig().getDouble("Abilities.Fire.HeatControl.Melt.Radius");
		
		this.range = getDayFactor(range);
		this.radius = getDayFactor(radius);
		
		location = GeneralMethods.getTargetedLocation(player, range);
		for (Block block : GeneralMethods.getBlocksAroundPoint(location, radius)) {
			if (isMeltable(block)) {
				PhaseChangeMelt.melt(player, block);
			} else if (isHeatable(block)) {
				heat(block);
			}
		}
	}

	@SuppressWarnings("deprecation")
	private static void heat(Block block) {
		if (block.getType() == Material.OBSIDIAN) {
			block.setType(Material.LAVA);
			block.setData((byte) 0x0);
		}
	}

	private static boolean isHeatable(Block block) {
		return false;
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
		return 0;
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

	public void setLocation(Location location) {
		this.location = location;
	}

}

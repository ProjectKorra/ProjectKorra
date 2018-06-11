package com.projectkorra.projectkorra.earthbending;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.EarthAbility;

public class EarthDome extends EarthAbility {
	
	public Location center;
	public double radius;
	public int height;
	public Set<Block> checked = new HashSet<>();
	public Set<Block> corners = new HashSet<>();

	public EarthDome(Player player, Location center) {
		super(player);
		
		if (bPlayer.isOnCooldown("EarthDome")) {
			return;
		}
		
		this.center = center;
		this.radius = getConfig().getDouble("Abilities.Earth.EarthDome.Radius");
		this.height = getConfig().getInt("Abilities.Earth.EarthDome.Height");
		
		for (int i = 0; i < 2; i++) {
			for (Location check : getCircle(center, radius+i, 10)) {
				Block b = check.getBlock();
				if (checked.contains(b)) {
					continue;
				}
				
				b = getAppropriateBlock(b);
				if (b == null) {
					continue;
				}
				
				new RaiseEarth(player, b.getLocation(), Math.round(height-i));
				checked.add(b);
			}
			
		}
		
		bPlayer.addCooldown("EarthDome", getCooldown());
	}
	
	public EarthDome(Player player) {
		this(player, player.getLocation().clone().subtract(0, 1, 0));
	}
	
	private Block getAppropriateBlock(Block block) {
		if (!GeneralMethods.isSolid(block.getRelative(BlockFace.UP)) && GeneralMethods.isSolid(block)) {
			return block;
		}
		Block top = GeneralMethods.getTopBlock(block.getLocation(), 2);
		if (GeneralMethods.isSolid(top.getRelative(BlockFace.UP))) {
			return null;
		}
		return top;
	}
	
	private List<Location> getCircle(Location center, double radius, double interval) {
	    List<Location> result = new ArrayList<>();
	    interval = Math.toRadians(Math.abs(interval));
	    for (double theta = 0; theta < 2 * Math.PI; theta += interval) {
	        double x = Math.cos(theta) * (radius+(Math.random()/3.1));
	        double z = Math.sin(theta) * (radius+(Math.random()/3.1));
	        result.add(center.clone().add(x, 0, z));
	    }
	    return result;
	}
	
	@Override
	public void progress() {}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public long getCooldown() {
		return getConfig().getLong("Abilities.Earth.EarthDome.Cooldown");
	}

	@Override
	public String getName() {
		return "EarthDomeHidden";
	}

	@Override
	public Location getLocation() {
		return center;
	}
	
	@Override
	public boolean isHiddenAbility() {
		return true;
	}

}
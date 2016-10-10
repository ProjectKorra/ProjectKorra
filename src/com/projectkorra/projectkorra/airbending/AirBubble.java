package com.projectkorra.projectkorra.airbending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.waterbending.WaterManipulation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AirBubble extends AirAbility {

	private boolean waterBubble;
	private double radius;
	private double airRadius;
	private double waterRadius;
	private Map<Block, BlockState> waterOrigins;

	public AirBubble(Player player) {
		super(player);
		
		this.radius = 0;
		this.airRadius = getConfig().getDouble("Abilities.Air.AirBubble.Radius");
		this.waterRadius = getConfig().getDouble("Abilities.Water.WaterBubble.Radius");
		this.waterOrigins = new ConcurrentHashMap<>();
		start();
	}

	public static boolean canFlowTo(Block block) {
		for (AirBubble airBubble : getAbilities(AirBubble.class)) {
			if (airBubble.blockInBubble(block)) {
				return false;
			}
		}
		return true;
	}

	public static void handleBubbles() {
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			if (bPlayer == null) {
				continue;
			}
			
			String name = bPlayer.getBoundAbilityName();
			if (name.equalsIgnoreCase("AirBubble") | name.equalsIgnoreCase("WaterBubble")) {
				if (!hasAbility(player, AirBubble.class) && player.isSneaking()) {
					AirBubble airBubble = new AirBubble(player);
					if (name.equalsIgnoreCase("WaterBubble")) {
						airBubble.waterBubble = true;
					}
				}
			}
		}
	}

	public boolean blockInBubble(Block block) {
		if (block.getWorld() != player.getWorld()) {
			return false;
		} else if (block.getLocation().distanceSquared(player.getLocation()) <= radius * radius) {
			return true;
		}
		return false;
	}

	@Override
	public void progress() {
		if (!player.isSneaking()) {
			remove();
			return;
		} else if (!waterBubble && !bPlayer.canBend(this)) {
			remove();
			return;
		} else if (waterBubble && !bPlayer.canBend(getAbility("WaterBubble"))) {
			remove();
			return;
		} else {
			pushWater();
		}
	}

	private void pushWater() {
		if (bPlayer.hasElement(Element.AIR)) {
			radius = airRadius;
		} else {
			radius = waterRadius;
		}
		
		if (airRadius > radius && bPlayer.hasElement(Element.AIR)) {
			radius = airRadius;
		}

		Location location = player.getLocation();

		for (Block block : waterOrigins.keySet()) {
			if (block.getWorld() != location.getWorld()) {
				if (block.getType() == Material.AIR || isWater(block)) {
					waterOrigins.get(block).update(true);
				}
				waterOrigins.remove(block);
			} else if (block.getLocation().distanceSquared(location) > radius * radius) {
				if (block.getType() == Material.AIR || isWater(block)) {
					waterOrigins.get(block).update(true);
				}
				waterOrigins.remove(block);
			}
		}

		for (Block block : GeneralMethods.getBlocksAroundPoint(location, radius)) {
			if (waterOrigins.containsKey(block)) {
				continue;
			} else if (!isWater(block)) {
				continue;
			} else if (GeneralMethods.isRegionProtectedFromBuild(player, "AirBubble", block.getLocation())) {
				continue;
			} else if (block.getType() == Material.STATIONARY_WATER || block.getType() == Material.WATER) {
				if (WaterManipulation.canBubbleWater(block)) {
					waterOrigins.put(block, block.getState());
					block.setType(Material.AIR);
				}
			}
		}
		
		WaterAbility.removeWaterSpouts(location, radius, player);
	}

	@Override
	public void remove() {
		super.remove();
		for (Block block : waterOrigins.keySet()) {
			if (block.getType() == Material.AIR || block.isLiquid()) {
				waterOrigins.get(block).update(true);
			}
		}
	}

	@Override
	public String getName() {
		return waterBubble ? "WaterBubble" : "AirBubble";
	}

	@Override
	public Location getLocation() {
		return player != null ? player.getLocation() : null;
	}

	@Override
	public long getCooldown() {
		return 0;
	}
	
	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public boolean isWaterBubble() {
		return waterBubble;
	}

	public void setWaterBubble(boolean waterBubble) {
		this.waterBubble = waterBubble;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getAirRadius() {
		return airRadius;
	}

	public void setAirRadius(double airRadius) {
		this.airRadius = airRadius;
	}

	public double getWaterRadius() {
		return waterRadius;
	}

	public void setWaterRadius(double waterRadius) {
		this.waterRadius = waterRadius;
	}

	public Map<Block, BlockState> getWaterOrigins() {
		return waterOrigins;
	}

}

package com.projectkorra.projectkorra.airbending;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.StockAbility;
import com.projectkorra.projectkorra.ability.api.CoreAbility;
import com.projectkorra.projectkorra.waterbending.WaterManipulation;
import com.projectkorra.projectkorra.waterbending.WaterMethods;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;

public class AirBubble extends CoreAbility {

	private static double DEFAULT_AIR_RADIUS = config.get().getDouble("Abilities.Air.AirBubble.Radius");
	private static double DEFAULT_WATER_RADIUS = config.get().getDouble("Abilities.Water.WaterBubble.Radius");

	private Player player;
	private double radius;
	private double defaultAirRadius = DEFAULT_AIR_RADIUS;
	private double defaultWaterRadius = DEFAULT_WATER_RADIUS;
	private ConcurrentHashMap<Block, BlockState> waterorigins;

	public AirBubble(Player player) {
		reloadVariables();
		this.player = player;
		waterorigins = new ConcurrentHashMap<Block, BlockState>();
		//instances.put(uuid, this);
		putInstance(player, this);
	}

	public static boolean canFlowTo(Block block) {
		for (Integer id : getInstances(StockAbility.AirBubble).keySet()) {
			if (((AirBubble) getInstances(StockAbility.AirBubble).get(id)).blockInBubble(block)) {
				return false;
			}
		}
		return true;
	}

	public static String getDescription() {
		return "To use, the bender must merely have the ability selected." + " All water around the user in a small bubble will vanish," + " replacing itself once the user either gets too far away or selects a different ability.";
	}

	public static void handleBubbles(Server server) {
		for (Player player : server.getOnlinePlayers()) {
			if (GeneralMethods.getBoundAbility(player) != null) {
				if (GeneralMethods.getBoundAbility(player).equalsIgnoreCase("AirBubble") || GeneralMethods.getBoundAbility(player).equalsIgnoreCase("WaterBubble")) {
					if (!containsPlayer(player, AirBubble.class) && player.isSneaking()) {
						new AirBubble(player);
					}
				}
			}
		}

		CoreAbility.progressAll(StockAbility.AirBubble);
	}

	public boolean blockInBubble(Block block) {
		if (block.getWorld() != player.getWorld()) {
			return false;
		}
		if (block.getLocation().distance(player.getLocation()) <= radius) {
			return true;
		}
		return false;
	}

	public double getDefaultAirRadius() {
		return defaultAirRadius;
	}

	public double getDefaultWaterRadius() {
		return defaultWaterRadius;
	}

	public Player getPlayer() {
		return player;
	}

	public double getRadius() {
		return radius;
	}

	@Override
	public StockAbility getStockAbility() {
		return StockAbility.AirBubble;
	}

	@Override
	public boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return false;
		}

		if (!player.isSneaking()) {
			remove();
			return false;
		}
		if (GeneralMethods.getBoundAbility(player) != null) {
			if (GeneralMethods.getBoundAbility(player).equalsIgnoreCase("AirBubble") && GeneralMethods.canBend(player.getName(), "AirBubble")) {
				pushWater();
				return false;
			}
			if (GeneralMethods.getBoundAbility(player).equalsIgnoreCase("WaterBubble") && GeneralMethods.canBend(player.getName(), "WaterBubble")) {
				pushWater();
				return false;
			}
		}

		remove();
		return true;
	}

	private void pushWater() {
		if (GeneralMethods.isBender(player.getName(), Element.Air)) {
			radius = defaultAirRadius;
		} else {
			radius = defaultWaterRadius;
		}
		if (GeneralMethods.isBender(player.getName(), Element.Water) && WaterMethods.isNight(player.getWorld())) {
			radius = WaterMethods.waterbendingNightAugment(defaultWaterRadius, player.getWorld());
		}
		if (defaultAirRadius > radius && GeneralMethods.isBender(player.getName(), Element.Air))
			radius = defaultAirRadius;
		Location location = player.getLocation();

		for (Block block : waterorigins.keySet()) {
			if (block.getWorld() != location.getWorld()) {
				if (block.getType() == Material.AIR || WaterMethods.isWater(block))
					waterorigins.get(block).update(true);
				waterorigins.remove(block);
			} else if (block.getLocation().distance(location) > radius) {
				if (block.getType() == Material.AIR || WaterMethods.isWater(block))
					waterorigins.get(block).update(true);
				waterorigins.remove(block);
			}
		}

		for (Block block : GeneralMethods.getBlocksAroundPoint(location, radius)) {
			if (waterorigins.containsKey(block))
				continue;
			if (!WaterMethods.isWater(block))
				continue;
			if (GeneralMethods.isRegionProtectedFromBuild(player, "AirBubble", block.getLocation()))
				continue;
			if (block.getType() == Material.STATIONARY_WATER || block.getType() == Material.WATER) {
				if (WaterManipulation.canBubbleWater(block)) {
					waterorigins.put(block, block.getState());
					block.setType(Material.AIR);
				}
			}

		}

	}

	@Override
	public void reloadVariables() {
		DEFAULT_AIR_RADIUS = config.get().getDouble("Abilities.Air.AirBubble.Radius");
		DEFAULT_WATER_RADIUS = config.get().getDouble("Abilities.Water.WaterBubble.Radius");
		defaultAirRadius = DEFAULT_AIR_RADIUS;
		defaultWaterRadius = DEFAULT_WATER_RADIUS;
	}

	public void remove() {
		for (Block block : waterorigins.keySet()) {
			// byte data = waterorigins.get(block);
			// byte data = 0x0;
			// block = block.getLocation().getBlock();
			// if (block.getType() == Material.AIR) {
			// block.setType(Material.WATER);
			// block.setData(data);
			// }
			if (block.getType() == Material.AIR || block.isLiquid())
				waterorigins.get(block).update(true);
		}
		//instances.remove(uuid);
		super.remove();
	}

	public void setDefaultAirRadius(double defaultAirRadius) {
		this.defaultAirRadius = defaultAirRadius;
	}

	public void setDefaultWaterRadius(double defaultWaterRadius) {
		this.defaultWaterRadius = defaultWaterRadius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

}

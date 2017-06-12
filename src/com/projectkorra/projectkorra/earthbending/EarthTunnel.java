package com.projectkorra.projectkorra.earthbending;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.TempBlock;

public class EarthTunnel extends EarthAbility {

	private long interval;
	private long time;
	private double depth;
	private double radius;
	private double angle;
	private double maxRadius;
	private double range;
	private double radiusIncrement;
	private boolean revert;
 	private boolean dropLootIfNotRevert;
	private Block block;
	private Location origin;
	private Location location;
	private Vector direction;

	public static Map<TempBlock, Long> airBlocks = new ConcurrentHashMap<TempBlock, Long>();

	public EarthTunnel(Player player) {
		super(player);

		this.maxRadius = getConfig().getDouble("Abilities.Earth.EarthTunnel.MaxRadius");
		this.range = getConfig().getDouble("Abilities.Earth.EarthTunnel.Range");
		this.radius = getConfig().getDouble("Abilities.Earth.EarthTunnel.Radius");
		this.interval = getConfig().getLong("Abilities.Earth.EarthTunnel.Interval");
		this.revert = getConfig().getBoolean("Abilities.Earth.EarthTunnel.Revert");
		this.dropLootIfNotRevert = getConfig().getBoolean("Abilities.Earth.EarthTunnel.DropLootIfNotRevert");
		
		this.time = System.currentTimeMillis();

		this.location = player.getEyeLocation().clone();
		this.origin = player.getTargetBlock((HashSet<Material>) null, (int) range).getLocation();
		this.block = origin.getBlock();
		this.direction = location.getDirection().clone().normalize();
		this.depth = 0;
		if (origin.getWorld().equals(location.getWorld())) {
			this.depth = Math.max(0, origin.distance(location) - 1);
		}
		this.angle = 0;

		if (!bPlayer.canBend(this)) {
			return;
		}
		if (bPlayer.isAvatarState()) {
			this.maxRadius = getConfig().getDouble("Abilities.Avatar.AvatarState.Earth.EarthTunnel.Radius");
		}

		this.radiusIncrement = radius;
		
		start();
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			remove();
			return;
		}

		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();
			if (Math.abs(Math.toDegrees(player.getEyeLocation().getDirection().angle(direction))) > 20 || !player.isSneaking()) {
				remove();
				return;
			} else {
				while ((!isEarth(block) && !isSand(block))) {
					if (!isTransparent(block)) {
						remove();
						return;
					}

					if (angle >= 360) {
						angle = 0;
						if (radius >= maxRadius) {
							radius = radiusIncrement;
							if (depth >= range) {
								remove();
								return;
							} else {
								depth += 0.5;
							}
						} else {
							radius += radiusIncrement;
						}
					} else {
						angle += 20;
					}

					Vector vec = GeneralMethods.getOrthogonalVector(direction, angle, radius);
					block = location.clone().add(direction.clone().normalize().multiply(depth)).add(vec).getBlock();
				}

				if (revert) {
					if (getMovedEarth().containsKey(block)) {
						block.setType(Material.AIR);
					} else {
						airBlocks.put(new TempBlock(block, Material.AIR, (byte) 0), System.currentTimeMillis());
						if (isPlant(block.getRelative(BlockFace.UP)) || isSnow(block.getRelative(BlockFace.UP))) {
							Block above = block.getRelative(BlockFace.UP);
							Block above2 = above.getRelative(BlockFace.UP);
							if (isPlant(above) || isSnow(above)) {
								airBlocks.put(new TempBlock(above, Material.AIR, (byte) 0), System.currentTimeMillis());
								if (isPlant(above2) && above2.getType().equals(Material.DOUBLE_PLANT)) {
									airBlocks.put(new TempBlock(above2, Material.AIR, (byte) 0), System.currentTimeMillis());
								}
							}
						}
					}
				} else {
					if (dropLootIfNotRevert) {
						block.breakNaturally();
					} else {
						block.setType(Material.AIR);
					}
				}
			}
		}
	}

	@Override
	public String getName() {
		return "EarthTunnel";
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
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public List<Location> getLocations() {
		ArrayList<Location> locations = new ArrayList<>();
		for (TempBlock tblock : airBlocks.keySet()) {
			locations.add(tblock.getLocation());
		}
		return locations;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public double getDepth() {
		return depth;
	}

	public void setDepth(double depth) {
		this.depth = depth;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}

	public double getMaxRadius() {
		return maxRadius;
	}

	public void setMaxRadius(double maxRadius) {
		this.maxRadius = maxRadius;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getRadiusIncrement() {
		return radiusIncrement;
	}

	public void setRadiusIncrement(double radiusIncrement) {
		this.radiusIncrement = radiusIncrement;
	}

	public Block getBlock() {
		return block;
	}

	public void setBlock(Block block) {
		this.block = block;
	}

	public Location getOrigin() {
		return origin;
	}

	public void setOrigin(Location origin) {
		this.origin = origin;
	}

	public Vector getDirection() {
		return direction;
	}

	public void setDirection(Vector direction) {
		this.direction = direction;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public static void revertAirBlocks() {
		if (ConfigManager.defaultConfig.get().getBoolean("Abilities.Earth.EarthTunnel.Revert")) {
			for (TempBlock tempBlock : EarthTunnel.airBlocks.keySet()) {
				if (EarthTunnel.airBlocks.get(tempBlock) + ConfigManager.defaultConfig.get().getLong("Properties.Earth.RevertCheckTime") <= System.currentTimeMillis()) {
					tempBlock.revertBlock();
					EarthTunnel.airBlocks.remove(tempBlock);
				}
			}
		}
	}

}

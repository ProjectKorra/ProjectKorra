package com.projectkorra.projectkorra.earthbending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.EarthAbility;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;

public class EarthTunnel extends EarthAbility {
	
	private long interval;
	private long time;
	private long cooldown;
	private double depth;
	private double radius;
	private double angle;
	private double maxRadius;
	private double range;
	private double radiusIncrement;
	private Block block;
	private Location origin;
	private Location location;
	private Vector direction;

	public EarthTunnel(Player player) {
		super(player);
		
		this.maxRadius = getConfig().getDouble("Abilities.Earth.EarthTunnel.MaxRadius");
		this.range = getConfig().getDouble("Abilities.Earth.EarthTunnel.Range");
		this.radius = getConfig().getDouble("Abilities.Earth.EarthTunnel.Radius");
		this.interval = getConfig().getLong("Abilities.Earth.EarthTunnel.Interval");
		this.cooldown = 0;
		this.radiusIncrement = radius;
		this.time = System.currentTimeMillis();
		
		this.location = player.getEyeLocation().clone();
		this.origin = player.getTargetBlock((HashSet<Material>) null, (int) range).getLocation();
		this.block = origin.getBlock();
		this.direction = location.getDirection().clone().normalize();
		this.depth = Math.max(0, origin.distance(location) - 1);
		this.angle = 0;

		if (!bPlayer.canBend(this)) {
			return;
		}
		
		start();
		bPlayer.addCooldown(this);
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
				while (!isEarthbendable(block)) {
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

				if (isEarthRevertOn()) {
					addTempAirBlock(block);
				} else {
					block.breakNaturally();
				}
				return;
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

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	
}

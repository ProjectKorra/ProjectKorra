package com.projectkorra.ProjectKorra.earthbending;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;

public class EarthTunnel {

	public static ConcurrentHashMap<Player, EarthTunnel> instances = new ConcurrentHashMap<Player, EarthTunnel>();

	private static final double maxradius = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.EarthTunnel.MaxRadius");
	private static final double range = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.EarthTunnel.Range");
	private static final double radiusinc = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.EarthTunnel.Radius");
	// private static final double speed = 10;

	private static boolean revert = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Earth.EarthTunnel.Revert");
	private static final long interval = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.EarthTunnel.Interval");

	private Player player;
	private Block block;
	private Location origin, location;
	private Vector direction;
	private double depth, radius, angle;
	private long time;

	public EarthTunnel(Player player) {
		this.player = player;
		location = player.getEyeLocation().clone();
		origin = player.getTargetBlock(null, (int) range).getLocation();
		block = origin.getBlock();
		direction = location.getDirection().clone().normalize();
		depth = origin.distance(location) - 1;
		if (depth < 0)
			depth = 0;
		angle = 0;
		radius = radiusinc;
		// ortho = new Vector(direction.getY(), -direction.getX(),
		// 0).normalize();
		// Methods.verbose(ortho.clone().dot(direction));
		time = System.currentTimeMillis();

		instances.put(player, this);
	}

	public boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			instances.remove(player);
			return false;
		}
		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();
			// Methods.verbose("progressing");
			if (Math.abs(Math.toDegrees(player.getEyeLocation().getDirection()
					.angle(direction))) > 20
					|| !player.isSneaking()) {
				instances.remove(player);
				return false;
			} else {
				while (!Methods.isEarthbendable(player, block)) {
					// Methods.verbose("going");
					if (!Methods.isTransparentToEarthbending(player, block)) {
						// Methods.verbose("false! at" + angle + " " + radius +
						// " "
						// + depth);
						instances.remove(player);
						return false;
					}
					if (angle >= 360) {
						angle = 0;
						if (radius >= maxradius) {
							radius = radiusinc;
							if (depth >= range) {
								instances.remove(player);
								return false;
							} else {
								depth += .5;
							}
						} else {
							radius += radiusinc;
						}
					} else {
						angle += 20;
					}
					// block.setType(Material.GLASS);
					Vector vec = Methods.getOrthogonalVector(direction, angle,
							radius);
					block = location.clone()
							.add(direction.clone().normalize().multiply(depth))
							.add(vec).getBlock();
				}

				if (revert) {
					Methods.addTempAirBlock(block);
				} else {
					block.breakNaturally();
				}

				return true;
			}
		} else {
			return false;
		}
	}

	public static boolean progress(Player player) {
		return instances.get(player).progress();
	}

	public static String getDescription() {
		return "Earth Tunnel is a completely utility ability for earthbenders. "
				+ "To use, simply sneak (default: shift) in the direction you want to tunnel. "
				+ "You will slowly begin tunneling in the direction you're facing for as long as you "
				+ "sneak or if the tunnel has been dug long enough. This ability will be interupted "
				+ "if it hits a block that cannot be earthbent.";
	}

}
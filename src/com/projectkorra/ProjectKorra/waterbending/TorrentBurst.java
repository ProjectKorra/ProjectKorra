package com.projectkorra.ProjectKorra.waterbending;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.TempBlock;

public class TorrentBurst {

	public static ConcurrentHashMap<Integer, TorrentBurst> instances = new ConcurrentHashMap<Integer, TorrentBurst>();

	private static int ID = Integer.MIN_VALUE;
	private static double defaultmaxradius = ProjectKorra.plugin.getConfig().getDouble("Abilities.Water.Torrent.Wave.Radius");
	private static double dr = 0.5;
	private static double defaultfactor = ProjectKorra.plugin.getConfig().getDouble("Abilities.Water.Torrent.Wave.Knockback");
	private static double MAX_HEIGHT = ProjectKorra.plugin.getConfig().getDouble("Abilities.Water.Torrent.Wave.Height");
	private static long interval = Torrent.interval;

	//	private static final byte full = 0x0;
	// private static final Vector reference = new Vector(1, 0, 0);

	private int id;
	private long time;
	private double radius = dr;
	private double maxradius = defaultmaxradius;
	private double factor = defaultfactor;
	private double maxheight = MAX_HEIGHT;
	private Location origin;
	private Player player;
	private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Double>> heights = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Double>>();
	private ArrayList<TempBlock> blocks = new ArrayList<TempBlock>();
	private ArrayList<Entity> affectedentities = new ArrayList<Entity>();

	public TorrentBurst(Player player) {
		this(player, player.getEyeLocation(), dr);
	}

	public TorrentBurst(Player player, Location location) {
		this(player, location, dr);
	}

	public TorrentBurst(Player player, double radius) {
		this(player, player.getEyeLocation(), radius);
	}

	public TorrentBurst(Player player, Location location, double radius) {
		this.player = player;
		World world = player.getWorld();
		origin = location.clone();
		time = System.currentTimeMillis();
		id = ID++;
		factor = Methods.waterbendingNightAugment(factor, world);
		maxradius = Methods.waterbendingNightAugment(maxradius, world);
		this.radius = radius;
		if (ID >= Integer.MAX_VALUE) {
			ID = Integer.MIN_VALUE;
		}
		initializeHeightsMap();
		instances.put(id, this);
	}

	private void initializeHeightsMap() {
		for (int i = -1; i <= maxheight; i++) {
			ConcurrentHashMap<Integer, Double> angles = new ConcurrentHashMap<Integer, Double>();
			double dtheta = Math.toDegrees(1 / (maxradius + 2));
			int j = 0;
			for (double theta = 0; theta < 360; theta += dtheta) {
				angles.put(j, theta);
				j++;
			}
			heights.put(i, angles);
		}
	}

	private void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}

		if (!Methods.canBend(player.getName(), "Torrent")) {
			remove();
			return;
		}

		if (System.currentTimeMillis() > time + interval) {
			if (radius < maxradius) {
				radius += dr;
			} else {
				remove();
				returnWater();
				return;
			}

			formBurst();

			time = System.currentTimeMillis();

		}
	}

	private void formBurst() {
		for (TempBlock tempBlock : blocks) {
			tempBlock.revertBlock();
		}

		blocks.clear();

		affectedentities.clear();

		ArrayList<Entity> indexlist = new ArrayList<Entity>();
		indexlist.addAll(Methods.getEntitiesAroundPoint(origin, radius + 2));

		ArrayList<Block> torrentblocks = new ArrayList<Block>();

		if (indexlist.contains(player))
			indexlist.remove(player);

		for (int id : heights.keySet()) {
			ConcurrentHashMap<Integer, Double> angles = heights.get(id);
			for (int index : angles.keySet()) {
				double angle = angles.get(index);
				double theta = Math.toRadians(angle);
				double dx = Math.cos(theta) * radius;
				double dy = id;
				double dz = Math.sin(theta) * radius;
				Location location = origin.clone().add(dx, dy, dz);
				Block block = location.getBlock();
				if (torrentblocks.contains(block))
					continue;
				if (Methods.isTransparentToEarthbending(player,	block)) {
					TempBlock tempBlock = new TempBlock(block, Material.STATIONARY_WATER, (byte) 8);
					blocks.add(tempBlock);
					torrentblocks.add(block);
				} else {
					angles.remove(index);
					continue;
				}
				for (Entity entity : indexlist) {
					if (!affectedentities.contains(entity)) {
						if (entity.getLocation().distance(location) <= 2) {
							affectedentities.add(entity);
							affect(entity);
						}
					}
				}
				
				for(Block sound : torrentblocks) {
					if (Methods.rand.nextInt(50) == 0) {
						Methods.playWaterbendingSound(sound.getLocation());
					}		
				}
			}
			if (angles.isEmpty())
				heights.remove(id);
		}
		if (heights.isEmpty())
			remove();
	}

	private void affect(Entity entity) {
		Vector direction = Methods.getDirection(origin, entity.getLocation());
		direction.setY(0);
		direction.normalize();
		entity.setVelocity(entity.getVelocity().clone().add(direction.multiply(factor)));
	}

	private void remove() {
		for (TempBlock block : blocks) {
			block.revertBlock();
		}
		instances.remove(id);
	}

	private void returnWater() {
		Location location = new Location(origin.getWorld(), origin.getX() + radius, origin.getY(), origin.getZ());
		if (!location.getWorld().equals(player.getWorld()))
			return;
		if (location.distance(player.getLocation()) > maxradius + 5)
			return;
		new WaterReturn(player, location.getBlock());
	}

	public static void progressAll() {
		for (int id : instances.keySet())
			instances.get(id).progress();
	}

	public static void removeAll() {
		for (int id : instances.keySet())
			instances.get(id).remove();
	}

	public double getMaxradius() {
		return maxradius;
	}

	public void setMaxradius(double maxradius) {
		this.maxradius = maxradius;
	}

	public double getFactor() {
		return factor;
	}

	public void setFactor(double factor) {
		this.factor = factor;
	}

	public double getMaxheight() {
		return maxheight;
	}

	public void setMaxheight(double maxheight) {
		this.maxheight = maxheight;
	}

	public Player getPlayer() {
		return player;
	}
}

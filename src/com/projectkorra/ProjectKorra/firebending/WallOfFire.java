package com.projectkorra.ProjectKorra.firebending;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AvatarState;

public class WallOfFire {

	private Player player;

	private static double maxangle = 50;

	public static FileConfiguration config = ProjectKorra.plugin.getConfig();
	private static int range = config.getInt("Abilities.Fire.WallOfFire.Range");
	private int height = config.getInt("Abilities.Fire.WallOfFire.Height");
	private int width = config.getInt("Abilities.Fire.WallOfFire.Width");
	private long duration = config.getLong("Abilities.Fire.WallOfFire.Duration");
	private int damage = config.getInt("Abilities.Fire.WallOfFire.Damage");
	private static long interval = 250;
	private static long cooldown = config.getLong("Abilities.Fire.WallOfFire.Cooldown");
	public static ConcurrentHashMap<Player, WallOfFire> instances = new ConcurrentHashMap<Player, WallOfFire>();
	private static long damageinterval = config.getLong("Abilities.Fire.WallOfFire.Interval");
	private static Map<String, Long> cooldowns = new HashMap<String, Long>();

	private Location origin;
	private long time, starttime;
	private boolean active = true;
	private int damagetick = 0, intervaltick = 0;
	private List<Block> blocks = new ArrayList<Block>();

	public WallOfFire(Player player) {
		if (instances.containsKey(player) && !AvatarState.isAvatarState(player)) {
			return;
		}
		
		if (cooldowns.containsKey(player.getName())) {
			if (cooldowns.get(player.getName()) + cooldown >= System.currentTimeMillis()) {
				return;
			} else {
				cooldowns.remove(player.getName());
			}
		}

		this.player = player;

		origin = Methods.getTargetedLocation(player, range);

		World world = player.getWorld();

		if (Methods.isDay(player.getWorld())) {
			width = (int) Methods.firebendingDayAugment((double) width, world);
			height = (int) Methods.firebendingDayAugment((double) height, world);
			duration = (long) Methods.firebendingDayAugment((double) duration,
					world);
			damage = (int) Methods.firebendingDayAugment((double) damage, world);
		}

		time = System.currentTimeMillis();
		starttime = time;

		Block block = origin.getBlock();

		if (block.isLiquid() || Methods.isSolid(block)) {
			return;
		}

		Vector direction = player.getEyeLocation().getDirection();
		Vector compare = direction.clone();
		compare.setY(0);
		// double angle = direction.angle(compare);
		// Methods.verbose(Math.toDegrees(angle));

		if (Math.abs(direction.angle(compare)) > Math.toRadians(maxangle)) {
			return;
		}

		initializeBlocks();

		instances.put(player, this);
	}

	private void progress() {
		time = System.currentTimeMillis();

		if (time - starttime > cooldown) {
			instances.remove(player);
			return;
		}

		if (!active)
			return;

		if (time - starttime > duration) {
			active = false;
			return;
		}

		if (time - starttime > intervaltick * interval) {
			intervaltick++;
			display();
		}

		if (time - starttime > damagetick * damageinterval) {
			damagetick++;
			damage();
		}

	}

	private void initializeBlocks() {
		Vector direction = player.getEyeLocation().getDirection();
		direction = direction.normalize();

		Vector ortholr = Methods.getOrthogonalVector(direction, 0, 1);
		ortholr = ortholr.normalize();

		Vector orthoud = Methods.getOrthogonalVector(direction, 90, 1);
		orthoud = orthoud.normalize();

		double w = (double) width;
		double h = (double) height;

		for (double i = -w; i <= w; i++) {
			for (double j = -h; j <= h; j++) {
				Location location = origin.clone().add(
						orthoud.clone().multiply(j));
				location = location.add(ortholr.clone().multiply(i));
//				if (Methods.isRegionProtectedFromBuild(player,
//						Abilities.WallOfFire, location))
//					continue;
				Block block = location.getBlock();
				if (!blocks.contains(block))
					blocks.add(block);
			}
		}

	}

	private void display() {
		for (Block block : blocks) {
			block.getWorld().playEffect(block.getLocation(),
					Effect.MOBSPAWNER_FLAMES, 0, 15);
		}
	}

	private void damage() {
		double radius = height;
		if (radius < width)
			radius = width;
		radius = radius + 1;
		List<Entity> entities = Methods.getEntitiesAroundPoint(origin, radius);
		if (entities.contains(player))
			entities.remove(player);
		for (Entity entity : entities) {
//			if (Methods.isRegionProtectedFromBuild(player, Abilities.WallOfFire,
//					entity.getLocation()))
//				continue;
			for (Block block : blocks) {
				if (entity.getLocation().distance(block.getLocation()) <= 1.5) {
					affect(entity);
					break;
				}
			}
		}
	}

	private void affect(Entity entity) {
		entity.setFireTicks(50);
		entity.setVelocity(new Vector(0, 0, 0));
		if (entity instanceof LivingEntity) {
			Methods.damageEntity(player, entity, damage);
			new Enflamed(entity, player);
		}
	}

	// public WallOfFire(Player player) {
	// if (ID >= Integer.MAX_VALUE) {
	// ID = Integer.MIN_VALUE;
	// }
	// id = ID++;
	// this.player = player;
	// instances.put(id, this);
	// World world = player.getWorld();
	// if (cooldowns.containsKey(player)) {
	// if (cooldowns.get(player) + cooldown <= System.currentTimeMillis()) {
	// if (Methods.isDay(player.getWorld())) {
	// width = (int) Methods.firebendingDayAugment((double) width,
	// world);
	// height = (int) Methods.firebendingDayAugment((double) height,
	// world);
	// duration = (long) Methods.firebendingDayAugment(
	// (double) duration, world);
	// damage = (int) Methods.firebendingDayAugment((double) damage,
	// world);
	// }
	// WallOfFireStart(player);
	// }
	// } else {
	//
	// if (Methods.isDay(player.getWorld())) {
	// width = (int) Methods
	// .firebendingDayAugment((double) width, world);
	// height = (int) Methods.firebendingDayAugment((double) height,
	// world);
	// duration = (long) Methods.firebendingDayAugment(
	// (double) duration, world);
	// damage = (int) Methods.firebendingDayAugment((double) damage,
	// world);
	// }
	// WallOfFireStart(player);
	// }
	// }
	//
	// public void WallOfFireStart(Player p) {
	// durations.put(p, System.currentTimeMillis());
	// intervals.put(p, System.currentTimeMillis());
	// Block tblock = p.getTargetBlock(null, range).getRelative(BlockFace.UP);
	// Location loc = tblock.getLocation();
	// locations.put(p, loc);
	// playerlocations.put(p, p.getLocation());
	// cooldowns.put(p, System.currentTimeMillis());
	// if (tblock.getType() != Material.AIR
	// && !FireStream.isIgnitable(player, tblock)) {
	// instances.remove(id);
	// durations.remove(p);
	// cooldowns.remove(p);
	// }
	// if (cooldowns.containsKey(p) && AvatarState.isAvatarState(p))
	// cooldowns.remove(p);
	// }
	//
	// public static void manageWallOfFire(int ID) {
	// if (instances.containsKey(ID)) {
	// WallOfFire wof = instances.get(ID);
	// Player p = instances.get(ID).player;
	//
	// int damage = wof.damage;
	// int width = wof.width;
	// int height = wof.height;
	// long duration = wof.duration;
	//
	// if (durations.containsKey(p)) {
	// if (durations.get(p) + duration >= System.currentTimeMillis()) {
	//
	// if (intervals.containsKey(p)) {
	// if (intervals.get(p) + interval <= System
	// .currentTimeMillis()) {
	//
	// List<Location> blocks = new ArrayList<Location>();
	// Location loc = locations.get(p);
	// Location yaw = playerlocations.get(p);
	// intervals.put(p, System.currentTimeMillis());
	// Vector direction = yaw.getDirection().normalize();
	// double ox, oy, oz;
	// ox = -direction.getZ();
	// oy = 0;
	// oz = direction.getX();
	// Vector orth = new Vector(ox, oy, oz);
	// orth = orth.normalize();
	// blocks.add(loc);
	// for (int i = -width; i <= width; i++) {
	// Block block = loc.getWorld().getBlockAt(
	// loc.clone().add(
	// orth.clone().multiply(
	// (double) i)));
	// if (FireStream.isIgnitable(p, block))
	// block.setType(Material.AIR);
	// for (int y = block.getY(); y <= block.getY()
	// + height; y++) {
	// Location loca = new Location(
	// block.getWorld(), block.getX(),
	// (int) y, block.getZ());
	// if (Methods.isRegionProtectedFromBuild(p,
	// Abilities.WallOfFire, loca))
	// continue;
	// blocks.add(loca);
	// block.getWorld().playEffect(loca,
	// Effect.MOBSPAWNER_FLAMES, 1, 20);
	// blockslocation.put(p, blocks);
	// }
	// }
	// }
	// }
	// if (blockslocation.containsKey(p)) {
	// for (Location loca : blockslocation.get(p)) {
	// FireBlast.removeFireBlastsAroundPoint(loca, 2);
	// for (Entity en : Methods.getEntitiesAroundPoint(
	// locations.get(p), width + 2)) {
	// if (en instanceof Projectile) {
	// if (loca.distance(en.getLocation()) <= 3) {
	// // Methods.damageEntity(p, en, damage);
	// en.setVelocity(en.getVelocity()
	// .normalize().setX(0).setZ(0)
	// .multiply(0.1));
	// en.setFireTicks(40);
	// }
	// }
	// }
	// for (Entity en : Methods.getEntitiesAroundPoint(loca,
	// 2)) {
	// if (!damaged.containsKey(en))
	// damaged.put(en, System.currentTimeMillis()
	// + damageinterval);
	// if (damaged.get(en) + damageinterval <= System
	// .currentTimeMillis()) {
	// Methods.damageEntity(p, en, damage);
	// en.setVelocity(new Vector((en.getLocation()
	// .getX() - loca.getBlock()
	// .getLocation().getX()) * 0.2, 0.1,
	// (en.getLocation().getZ() - loca
	// .getBlock().getLocation()
	// .getZ()) * 0.2));
	// en.setFireTicks(81);
	// new Enflamed(en, p);
	// damaged.put(en, System.currentTimeMillis());
	// }
	// }
	// }
	// }
	// }
	// }
	// }
	// }

	public static String getDescription() {
		return "To use this ability, click at a location. A wall of fire "
				+ "will appear at this location, igniting enemies caught in it "
				+ "and blocking projectiles.";
	}

	public static void manage() {
		for (Player player : instances.keySet()) {
			instances.get(player).progress();
		}
	}
}

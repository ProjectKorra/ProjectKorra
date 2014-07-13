package com.projectkorra.ProjectKorra.waterbending;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.TempBlock;
import com.projectkorra.ProjectKorra.Ability.AvatarState;


public class Torrent {

	private static ConcurrentHashMap<Player, Torrent> instances = new ConcurrentHashMap<Player, Torrent>();
	private static ConcurrentHashMap<TempBlock, Player> frozenblocks = new ConcurrentHashMap<TempBlock, Player>();

	static long interval = 30;
	private static int defaultrange = 20;
	private static int selectrange = 10;
	private static double radius = 3;
	static double range = 25;
	private static int damage = 2;
	private static int deflectdamage = 1;
	private static double factor = 1;
	private static int maxlayer = 3;
	private static double ylimit = 0.2;

	private static final byte full = 0x0;

	private double startangle = 0;
	private Block sourceblock;
	private TempBlock source;
	private Location location;
	private Player player;
	private long time;
	private double angle = 20;
	private int layer = 0;

	private ArrayList<TempBlock> blocks = new ArrayList<TempBlock>();
	private ArrayList<TempBlock> launchblocks = new ArrayList<TempBlock>();

	private ArrayList<Entity> hurtentities = new ArrayList<Entity>();

	private boolean sourceselected = false;
	private boolean settingup = false;
	private boolean forming = false;
	private boolean formed = false;
	private boolean launch = false;
	private boolean launching = false;
	private boolean freeze = false;

	public Torrent(Player player) {
		if (instances.containsKey(player)) {
			Torrent torrent = instances.get(player);
			if (!torrent.sourceselected) {
				instances.get(player).use();
				return;
			}
		}
		this.player = player;
		time = System.currentTimeMillis();
		sourceblock = Methods.getWaterSourceBlock(player, selectrange,
				Methods.canPlantbend(player));
		if (sourceblock != null) {
			sourceselected = true;
			instances.put(player, this);
		}
	}

	private void freeze() {
		if (layer == 0)
			return;
		if (!Methods.canBend(player.getName(), "PhaseChange"))
			return;
		List<Block> ice = Methods.getBlocksAroundPoint(location, layer);
		for (Block block : ice) {
			if (Methods.isTransparentToEarthbending(player, block)
					&& block.getType() != Material.ICE) {
				TempBlock tblock = new TempBlock(block, Material.ICE, (byte) 0);
				frozenblocks.put(tblock, player);
			}
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

		if (Methods.getBoundAbility(player) == null) {
			remove();
			if (location != null)
				returnWater(location);
			return;
		}
		if (!Methods.getBoundAbility(player).equalsIgnoreCase("Torrent")) {
			remove();
			if (location != null)
				returnWater(location);
			return;
		}

		if (System.currentTimeMillis() > time + interval) {
			time = System.currentTimeMillis();

			if (sourceselected) {
				if (!sourceblock.getWorld().equals(player.getWorld())) {
					remove();
					return;
				}

				if (sourceblock.getLocation().distance(player.getLocation()) > selectrange) {
					remove();
					return;
				}

				if (player.isSneaking()) {
					sourceselected = false;
					settingup = true;
					if (Methods.isPlant(sourceblock)) {
						new Plantbending(sourceblock);
						sourceblock.setType(Material.AIR);
					} else if (!Methods.isAdjacentToThreeOrMoreSources(sourceblock)) {
						sourceblock.setType(Material.AIR);
					}
					source = new TempBlock(sourceblock, Material.STATIONARY_WATER, (byte) 8);
					location = sourceblock.getLocation();
				} else {
					Methods.playFocusWaterEffect(sourceblock);
					return;
				}
			}

			if (settingup) {
				if (!player.isSneaking()) {
					remove();
					returnWater(source.getLocation());
					return;
				}
				Location eyeloc = player.getEyeLocation();
				double startangle = player.getEyeLocation().getDirection()
						.angle(new Vector(1, 0, 0));
				double dx = radius * Math.cos(startangle);
				double dy = 0;
				double dz = radius * Math.sin(startangle);
				Location setup = eyeloc.clone().add(dx, dy, dz);

				if (!location.getWorld().equals(player.getWorld())) {
					remove();
					return;
				}

				if (location.distance(setup) > defaultrange) {
					remove();
					return;
				}

				if (location.getBlockY() > setup.getBlockY()) {
					Vector direction = new Vector(0, -1, 0);
					location = location.clone().add(direction);
				} else if (location.getBlockY() < setup.getBlockY()) {
					Vector direction = new Vector(0, 1, 0);
					location = location.clone().add(direction);
				} else {
					Vector direction = Methods.getDirection(location, setup)
							.normalize();
					location = location.clone().add(direction);
				}

				if (location.distance(setup) <= 1) {
					settingup = false;
					source.revertBlock();
					source = null;
					forming = true;
				} else {
					if (!location.getBlock().equals(
							source.getLocation().getBlock())) {
						source.revertBlock();
						source = null;
						Block block = location.getBlock();
						if (!Methods.isTransparentToEarthbending(player, block)
								|| block.isLiquid()) {
							remove();
							return;
						}
						source = new TempBlock(location.getBlock(),
								Material.STATIONARY_WATER, (byte) 8);
					}
				}
			}

			if (forming && !player.isSneaking()) {
				remove();
				returnWater(player.getEyeLocation().add(radius, 0, 0));
				return;
			}

			if (forming || formed) {
				if (angle < 220) {
					angle += 20;
				} else {
					forming = false;
					formed = true;
				}
				formRing();
				if (blocks.isEmpty()) {
					remove();
					return;
				}
			}

			if (formed && !player.isSneaking() && !launch) {
				new TorrentBurst(player, radius);
				remove();
				return;
			}

			if (launch && formed) {
				launching = true;
				launch = false;
				formed = false;
				if (!launch()) {
					remove();
					return;
				}
			}

			if (launching) {
				if (!player.isSneaking()) {
					remove();
					return;
				}
				if (!launch()) {
					remove();
					return;
				}

			}
		}

	}

	private boolean launch() {
		if (launchblocks.isEmpty() && blocks.isEmpty()) {
			return false;
		}

		if (launchblocks.isEmpty()) {
			clearRing();
			// double startangle = Math.toDegrees(player.getEyeLocation()
			// .getDirection().angle(new Vector(1, 0, 0)));
			Location loc = player.getEyeLocation();
			ArrayList<Block> doneblocks = new ArrayList<Block>();
			for (double theta = startangle; theta < angle + startangle; theta += 20) {
				double phi = Math.toRadians(theta);
				double dx = Math.cos(phi) * radius;
				double dy = 0;
				double dz = Math.sin(phi) * radius;
				Location blockloc = loc.clone().add(dx, dy, dz);
				if (Math.abs(theta - startangle) < 10)
					location = blockloc.clone();
				Block block = blockloc.getBlock();
				if (!doneblocks.contains(block) && !Methods.isRegionProtectedFromBuild(player, "Torrent", blockloc)) {
					if (Methods.isTransparentToEarthbending(player, block)
							&& !block.isLiquid()) {
						launchblocks.add(new TempBlock(block, Material.STATIONARY_WATER,
								(byte) 8));
						doneblocks.add(block);
					} else if (!Methods
							.isTransparentToEarthbending(player, block))
						break;
				}
			}
			if (launchblocks.isEmpty()) {
				return false;
			} else {
				return true;
			}
		}

		Entity target = Methods.getTargetedEntity(player, range, hurtentities);
		Location targetloc = player.getTargetBlock(
				Methods.getTransparentEarthbending(), (int) range).getLocation();
		// Location targetloc = Methods.getTargetedLocation(player, range,
		// Methods.transparentEarthbending);
		if (target != null) {
			targetloc = target.getLocation();
		}

		ArrayList<TempBlock> newblocks = new ArrayList<TempBlock>();

		List<Entity> entities = Methods.getEntitiesAroundPoint(
				player.getLocation(), range + 5);
		List<Entity> affectedentities = new ArrayList<Entity>();

		Block realblock = launchblocks.get(0).getBlock();

		Vector dir = Methods.getDirection(location, targetloc).normalize();

		if (target != null) {
			targetloc = location.clone().add(dir.clone().multiply(10));
		}

		// Methods.verbose(layer);
		if (layer == 0)
			location = location.clone().add(dir);

		Block b = location.getBlock();

		// player.sendBlockChange(location, 20, (byte) 0);

		if (location.distance(player.getLocation()) > range || Methods.isRegionProtectedFromBuild(player, "Torrent", location)) {
			if (layer < maxlayer)
				if (freeze || layer < 1)
					layer++;
			if (launchblocks.size() == 1) {
				remove();
				returnWater(location);
				return false;
			}
		} else if (!Methods.isTransparentToEarthbending(player, b)) {
			// b.setType(Material.GLASS);
			if (layer < maxlayer) {
				// Methods.verbose(layer);
				if (layer == 0)
					hurtentities.clear();
				if (freeze || layer < 1)
					layer++;
			}
			if (freeze) {
				freeze();
			} else if (launchblocks.size() == 1) {
				remove();
				returnWater(realblock.getLocation());
				return false;
			}
		} else {
			if (b.equals(realblock) && layer == 0) {
				// Methods.verbose(dir);
				return true;
			}
			if (b.getLocation().distance(targetloc) > 1) {
				newblocks.add(new TempBlock(b, Material.STATIONARY_WATER, (byte) 8));
			} else {
				if (layer < maxlayer) {
					if (layer == 0)
						hurtentities.clear();
					if (freeze || layer < 1)
						layer++;
				}
				if (freeze) {
					freeze();
				}
			}
		}

		for (int i = 0; i < launchblocks.size(); i++) {
			TempBlock block = launchblocks.get(i);
			if (i == launchblocks.size() - 1) {
				block.revertBlock();
			} else {
				newblocks.add(block);
				for (Entity entity : entities) {
					if (entity.getWorld() != block.getBlock().getWorld())
						continue;
					if (entity.getLocation().distance(block.getLocation()) <= 1.5
							&& !affectedentities.contains(entity)) {
						if (i == 0) {
							affect(entity, dir);
						} else {
							affect(entity,
									Methods.getDirection(
											block.getLocation(),
											launchblocks.get(i - 1)
											.getLocation()).normalize());
						}
						affectedentities.add(entity);
					}
				}
			}
		}

		launchblocks.clear();
		launchblocks.addAll(newblocks);

		if (launchblocks.isEmpty())
			return false;

		return true;
	}

	private void formRing() {
		clearRing();
		// double startangle = Math.toDegrees(player.getEyeLocation()
		// .getDirection().angle(new Vector(1, 0, 0)));
		startangle += 30;
		Location loc = player.getEyeLocation();
		ArrayList<Block> doneblocks = new ArrayList<Block>();
		List<Entity> entities = Methods.getEntitiesAroundPoint(loc, radius + 2);
		List<Entity> affectedentities = new ArrayList<Entity>();
		for (double theta = startangle; theta < angle + startangle; theta += 20) {
			double phi = Math.toRadians(theta);
			double dx = Math.cos(phi) * radius;
			double dy = 0;
			double dz = Math.sin(phi) * radius;
			Location blockloc = loc.clone().add(dx, dy, dz);
			Block block = blockloc.getBlock();
			if (!doneblocks.contains(block)) {
				if (Methods.isTransparentToEarthbending(player, block)
						&& !block.isLiquid()) {
					blocks.add(new TempBlock(block, Material.STATIONARY_WATER, (byte) 8));
					doneblocks.add(block);
					for (Entity entity : entities) {
						if (entity.getWorld() != blockloc.getWorld())
							continue;
						if (!affectedentities.contains(entity)
								&& entity.getLocation().distance(blockloc) <= 1.5) {
							deflect(entity);
						}
					}
				}
			}
		}
	}

	private void clearRing() {
		for (TempBlock block : blocks) {
			block.revertBlock();
		}
		blocks.clear();
	}

	private void remove() {
		clearRing();
		for (TempBlock block : launchblocks)
			block.revertBlock();
		launchblocks.clear();
		if (source != null)
			source.revertBlock();
		instances.remove(player);
	}

	private void returnWater(Location location) {
		new WaterReturn(player, location.getBlock());
	}

	public static void use(Player player) {
		if (instances.containsKey(player)) {
			instances.get(player).use();
		}
	}

	public static void create(Player player) {
		if (instances.containsKey(player))
			return;
		if (WaterReturn.hasWaterBottle(player)) {
			Location eyeloc = player.getEyeLocation();
			Block block = eyeloc.add(eyeloc.getDirection().normalize())
					.getBlock();
			if (Methods.isTransparentToEarthbending(player, block)
					&& Methods.isTransparentToEarthbending(player,
							eyeloc.getBlock())) {
				block.setType(Material.WATER);
				block.setData(full);
				Torrent tor = new Torrent(player);
				if (tor.sourceselected || tor.settingup) {
					WaterReturn.emptyWaterBottle(player);
				} else {
					block.setType(Material.AIR);
				}
			}
		}
	}

	private void use() {
		launch = true;
		if (launching)
			freeze = true;
	}

	private void deflect(Entity entity) {
		if (entity.getEntityId() == player.getEntityId())
			return;
		double x, z, vx, vz, mag;
		double angle = 50;
		angle = Math.toRadians(angle);

		x = entity.getLocation().getX() - player.getLocation().getX();
		z = entity.getLocation().getZ() - player.getLocation().getZ();

		mag = Math.sqrt(x * x + z * z);

		vx = (x * Math.cos(angle) - z * Math.sin(angle)) / mag;
		vz = (x * Math.sin(angle) + z * Math.cos(angle)) / mag;

		Vector vec = new Vector(vx, 0, vz).normalize().multiply(factor);

		Vector velocity = entity.getVelocity();
		if (AvatarState.isAvatarState(player)) {
			velocity.setX(AvatarState.getValue(vec.getX()));
			velocity.setZ(AvatarState.getValue(vec.getZ()));
		} else {
			velocity.setX(vec.getX());
			velocity.setZ(vec.getY());
		}

		entity.setVelocity(velocity);
		entity.setFallDistance(0);
		if (entity instanceof LivingEntity) {
			World world = player.getWorld();
			int damagedealt = deflectdamage;
			if (Methods.isNight(world)) {
				damagedealt = (int) (Methods.getWaterbendingNightAugment(world) * (double) deflectdamage);
			}
			Methods.damageEntity(player, entity, "Torrent", damagedealt);
//			Methods.damageEntity(player, entity, damagedealt);
		}
	}

	private void affect(Entity entity, Vector direction) {
		if (entity.getEntityId() == player.getEntityId())
			return;
		if (direction.getY() > ylimit) {
			direction.setY(ylimit);
		}
		if (!freeze)
			entity.setVelocity(direction.multiply(factor));
		if (entity instanceof LivingEntity && !hurtentities.contains(entity)) {
			World world = player.getWorld();
			int damagedealt = damage;
			if (Methods.isNight(world)) {
				damagedealt = (int) (Methods.getWaterbendingNightAugment(world) * (double) damage);
			}
			// if (((LivingEntity) entity).getNoDamageTicks() == 0) {
			Methods.damageEntity(player, entity, "Torrent", damagedealt);
//			Methods.damageEntity(player, entity, damagedealt);
			// Methods.verbose("Hit! Health at "
			// + ((LivingEntity) entity).getHealth());
			hurtentities.add(entity);
			// }
			((LivingEntity) entity).setNoDamageTicks(0);
		}
	}

	public static void progressAll() {
		for (Player player : instances.keySet())
			instances.get(player).progress();

		for (TempBlock block : frozenblocks.keySet()) {
			Player player = frozenblocks.get(block);
			if (block.getBlock().getType() != Material.ICE) {
				frozenblocks.remove(block);
				continue;
			}
			if (block.getBlock().getWorld() != player.getWorld()) {
				thaw(block);
				continue;
			}
			if (block.getLocation().distance(player.getLocation()) > range
					|| !Methods.canBend(player.getName(), "Torrent")) {
				thaw(block);
			}
		}
	}

	public static void thaw(Block block) {
		if (TempBlock.isTempBlock(block)) {
			TempBlock tblock = TempBlock.get(block);
			if (frozenblocks.containsKey(tblock))
				thaw(tblock);
		}
	}

	public static void thaw(TempBlock block) {
		block.revertBlock();
		frozenblocks.remove(block);
	}

	public static boolean canThaw(Block block) {
		if (TempBlock.isTempBlock(block)) {
			TempBlock tblock = TempBlock.get(block);
			return !frozenblocks.containsKey(tblock);
		}
		return true;
	}

	public static void removeAll() {
		for (Player player : instances.keySet())
			instances.get(player).remove();

		for (TempBlock block : frozenblocks.keySet())
			thaw(block);

	}

	public static boolean wasBrokenFor(Player player, Block block) {
		if (instances.containsKey(player)) {
			Torrent torrent = instances.get(player);
			if (torrent.sourceblock == null)
				return false;
			if (torrent.sourceblock.equals(block))
				return true;
		}
		return false;
	}

	public static String getDescription() {
		return "Torrent is one of the strongest moves in a waterbender's arsenal. To use, first click a source block to select it; then hold shift to begin streaming the water around you. Water flowing around you this way will damage and knock back nearby enemies and projectiles. If you release shift during this, you will create a large wave that expands outwards from you, launching anything in its path back. Instead, if you click you release the water and channel it to flow towards your cursor. Anything caught in the blast will be tossed about violently and take damage. Finally, if you click again when the water is torrenting, it will freeze the area around it when it is obstructed.";
	}

}
package com.projectkorra.ProjectKorra.waterbending;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.TempPotionEffect;

public class IceSpike {

	public static ConcurrentHashMap<Integer, IceSpike> instances = new ConcurrentHashMap<Integer, IceSpike>();
	public static Map<String, Long> cooldowns = new HashMap<String, Long>();
	public ConcurrentHashMap<Player, Long> removeTimers = new ConcurrentHashMap<Player, Long>();
	private static ConcurrentHashMap<Block, Block> alreadydoneblocks = new ConcurrentHashMap<Block, Block>();
	private static ConcurrentHashMap<Block, Integer> baseblocks = new ConcurrentHashMap<Block, Integer>();
	
	public static long removeTimer = 500;
	public static long cooldown = ProjectKorra.plugin.getConfig().getLong("Abilities.Water.IceSpike.Cooldown");
	public static final int standardheight = ProjectKorra.plugin.getConfig().getInt("Abilities.Water.IceSpike.Height");
	private static double range = ProjectKorra.plugin.getConfig().getDouble("Abilities.Water.IceSpike.Range");

	private static int ID = Integer.MIN_VALUE;
	private static double speed = 25;
	private static long interval = (long) (1000. / speed);
	private static final Vector direction = new Vector(0, 1, 0);

	private Location origin;
	private Location location;
	private Block block;
	private Player player;
	private int progress = 0;
	private double damage = ProjectKorra.plugin.getConfig().getDouble("Abilities.Water.IceSpike.Damage");
	private long time;
	int id;
	int height = 2;
	private Vector thrown = new Vector(0, ProjectKorra.plugin.getConfig().getDouble("Abilities.Water.IceSpike.ThrowingMult"), 0);
	private ConcurrentHashMap<Block, Block> affectedblocks = new ConcurrentHashMap<Block, Block>();
	private List<LivingEntity> damaged = new ArrayList<LivingEntity>();

	public IceSpike(Player player) {
		if (cooldowns.containsKey(player)) {
			if (cooldowns.get(player) + cooldown >= System.currentTimeMillis()) {
				return;
			} else {
				cooldowns.remove(player.getName());
			}
		}
		try {
			this.player = player;

			double lowestdistance = range + 1;
			Entity closestentity = null;
			for (Entity entity : Methods.getEntitiesAroundPoint(player.getLocation(), range)) {
				if (Methods.getDistanceFromLine(player.getLocation().getDirection(), player.getLocation(), entity.getLocation()) <= 2
						&& (entity instanceof LivingEntity)
						&& (entity.getEntityId() != player.getEntityId())) {
					double distance = player.getLocation().distance(entity.getLocation());
					if (distance < lowestdistance) {
						closestentity = entity;
						lowestdistance = distance;
					}
				}
			}
			if (closestentity != null) {
				Block temptestingblock = closestentity.getLocation().getBlock().getRelative(BlockFace.DOWN, 1);
				// if (temptestingblock.getType() == Material.ICE){
				this.block = temptestingblock;
				// }
			} else {
				this.block = player.getTargetBlock(null, (int) range);
			}
			origin = block.getLocation();
			location = origin.clone();

		} catch (IllegalStateException e) {
			return;
		}

		loadAffectedBlocks();

		if (height != 0) {
			if (canInstantiate()) {
				id = ID;
				instances.put(id, this);
				if (ID >= Integer.MAX_VALUE) {
					ID = Integer.MIN_VALUE;
				}
				ID++;
				time = System.currentTimeMillis() - interval;
				cooldowns.put(player.getName(), System.currentTimeMillis());
			}
		}
	}

	public IceSpike(Player player, Location origin, int damage,	Vector throwing, long aoecooldown) {
		cooldown = aoecooldown;
		this.player = player;
		this.origin = origin;
		this.damage = damage;
		this.thrown = throwing;
		location = origin.clone();
		block = location.getBlock();

		loadAffectedBlocks();

		if (block.getType() == Material.ICE) {
			if (canInstantiate()) {
				id = ID;
				instances.put(id, this);
				if (ID >= Integer.MAX_VALUE) {
					ID = Integer.MIN_VALUE;
				}
				ID++;
				time = System.currentTimeMillis() - interval;
			}
		}
	}

	private void loadAffectedBlocks() {
		affectedblocks.clear();
		Block thisblock;
		for (int i = 1; i <= height; i++) {
			thisblock = block.getWorld().getBlockAt(location.clone().add(direction.clone().multiply(i)));
			affectedblocks.put(thisblock, thisblock);
		}
	}

	private boolean blockInAffectedBlocks(Block block) {
		if (affectedblocks.containsKey(block)) {
			return true;
		}
		return false;
	}

	public static boolean blockInAllAffectedBlocks(Block block) {
		for (int ID : instances.keySet()) {
			if (instances.get(ID).blockInAffectedBlocks(block))
				return true;
		}
		return false;
	}

	public static void revertBlock(Block block) {
		for (int ID : instances.keySet()) {
			if (instances.get(ID).blockInAffectedBlocks(block)) {
				instances.get(ID).affectedblocks.remove(block);
			}
		}
	}

	private boolean canInstantiate() {
		if (block.getType() != Material.ICE)
			return false;
		for (Block block : affectedblocks.keySet()) {
			if (blockInAllAffectedBlocks(block)
					|| alreadydoneblocks.containsKey(block)
					|| block.getType() != Material.AIR
					|| (block.getX() == player.getEyeLocation().getBlock()
					.getX() && block.getZ() == player.getEyeLocation()
					.getBlock().getZ())) {
				return false;
			}
		}
		return true;
	}
	
	public static void progressAll() {
		for (int ID : instances.keySet()) {
			instances.get(ID).progress();
		}
	}

	private boolean progress() {
		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();
			if (progress < height) {
				moveEarth();
				removeTimers.put(player, System.currentTimeMillis());
			} else {
				if (removeTimers.get(player) + removeTimer <= System.currentTimeMillis()) {
					baseblocks.put(location.clone().add(direction.clone().multiply(-1 * (height))).getBlock(),(height - 1));
					if (!revertblocks()) {
						instances.remove(id);
					}
				}

				return false;
			}
		}
		return true;
	}

	private boolean moveEarth() {
		progress++;
		Block affectedblock = location.clone().add(direction).getBlock();
		location = location.add(direction);
		if (Methods.isRegionProtectedFromBuild(player, "IceSpike", location))
			return false;
		for (Entity en : Methods.getEntitiesAroundPoint(location, 1.4)) {
			if (en instanceof LivingEntity && en != player && !damaged.contains(((LivingEntity) en))) {
				LivingEntity le = (LivingEntity) en;
				affect(le);
				// le.setVelocity(thrown);
				// le.damage(damage);
				// damaged.add(le);
				// Methods.verbose(damage + " Hp:" + le.getHealth());
			}
		}
		affectedblock.setType(Material.ICE);
		loadAffectedBlocks();

		if (location.distance(origin) >= height) {
			return false;
		}

		return true;
	}

	private void affect(LivingEntity entity) {
		entity.setVelocity(thrown);
		entity.damage(damage);
		damaged.add(entity);
		long slowCooldown = IceSpike2.slowCooldown;
		int mod = 2;
		if (entity instanceof Player) {
			BendingPlayer bPlayer = Methods.getBendingPlayer(player.getName());
			if (bPlayer.canBeSlowed()) {
				PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 70, mod);
				new TempPotionEffect(entity, effect);
				bPlayer.slow(slowCooldown);
			}
		} else {
			PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 70, mod);
			new TempPotionEffect(entity, effect);
		}
		Methods.breakBreathbendingHold(entity);

	}

	public static boolean blockIsBase(Block block) {
		if (baseblocks.containsKey(block)) {
			return true;
		}
		return false;
	}

	public static void removeBlockBase(Block block) {
		if (baseblocks.containsKey(block)) {
			baseblocks.remove(block);
		}

	}

	public static void removeAll() {
		for (int ID : instances.keySet()) {
			instances.remove(ID);
		}
	}

	public boolean revertblocks() {
		Vector direction = new Vector(0, -1, 0);
		location.getBlock().setType(Material.AIR);// .clone().add(direction).getBlock().setType(Material.AIR);
		location.add(direction);
		if (blockIsBase(location.getBlock()))
			return false;
		return true;
	}

	public static String getDescription() {
		return "This ability has many functions. Clicking while targetting ice, or an entity over some ice, "
				+ "will raise a spike of ice up, damaging and slowing the target. Tapping sneak (shift) while"
				+ " selecting a water source will select that source that can then be fired with a click. Firing"
				+ " this will launch a spike of ice at your target, dealing a bit of damage and slowing the target. "
				+ "If you sneak (shift) while not selecting a source, many ice spikes will erupt from around you, "
				+ "damaging and slowing those targets.";
	}

}
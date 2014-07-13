package com.projectkorra.ProjectKorra.firebending;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.earthbending.EarthBlast;
import com.projectkorra.ProjectKorra.waterbending.Plantbending;
import com.projectkorra.ProjectKorra.waterbending.WaterManipulation;

public class FireBlast {

	public static ConcurrentHashMap<Integer, FireBlast> instances = new ConcurrentHashMap<Integer, FireBlast>();
	// private static ConcurrentHashMap<Player, Long> timers = new
	// ConcurrentHashMap<Player, Long>();
	// static final long soonesttime = ConfigManager.fireBlastCooldown;

	private static int ID = Integer.MIN_VALUE;
	static final int maxticks = 10000;

	private static double speed = ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.FireBlast.Speed");
	public static double affectingradius = 2;
	private static double pushfactor = ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.FireBlast.Push");
	private static boolean canPowerFurnace = true;
	static boolean dissipate = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Fire.FireBlast.Dissipate");
	// public static long interval = 2000;
	public static byte full = 0x0;

	private Location location;
	private List<Block> safe = new ArrayList<Block>();
	private Location origin;
	private Vector direction;
	private Player player;
	private int id;
	private double speedfactor;
	private int ticks = 0;
	private double damage = ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.FireBlast.Damage");
	double range = ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.FireBlast.Range");
	long cooldown = ProjectKorra.plugin.getConfig().getLong("Abilities.Fire.FireBlast.Cooldown");

	public static Map<String, Long> cooldowns = new HashMap<String, Long>();
	// private ArrayList<Block> affectedlevers = new ArrayList<Block>();

	// private long time;

	public FireBlast(Player player) {
		// if (timers.containsKey(player)) {
		// if (System.currentTimeMillis() < timers.get(player) + soonesttime) {
		// return;
		// }
		// }
		
		if (cooldowns.containsKey(player.getName())) {
			if (cooldowns.get(player.getName()) + cooldown >= System.currentTimeMillis()) {
				return;
			} else {
				cooldowns.remove(player.getName());
			}
		}
		
		if (player.getEyeLocation().getBlock().isLiquid()
				|| Fireball.isCharging(player)) {
			return;
		}
		range = Methods.firebendingDayAugment(range, player.getWorld());
		// timers.put(player, System.currentTimeMillis());
		this.player = player;
		location = player.getEyeLocation();
		origin = player.getEyeLocation();
		direction = player.getEyeLocation().getDirection().normalize();
		location = location.add(direction.clone());
		id = ID;
		instances.put(id, this);
		cooldowns.put(player.getName(), System.currentTimeMillis());
		if (ID == Integer.MAX_VALUE)
			ID = Integer.MIN_VALUE;
		ID++;
		// time = System.currentTimeMillis();
		// timers.put(player, System.currentTimeMillis());
	}

	public FireBlast(Location location, Vector direction, Player player,
			double damage, List<Block> safeblocks) {
		if (location.getBlock().isLiquid()) {
			return;
		}
		safe = safeblocks;
		range = Methods.firebendingDayAugment(range, player.getWorld());
		// timers.put(player, System.currentTimeMillis());
		this.player = player;
		this.location = location.clone();
		origin = location.clone();
		this.direction = direction.clone().normalize();
		this.damage *= 1.5;
		id = ID;
		instances.put(id, this);
		if (ID == Integer.MAX_VALUE)
			ID = Integer.MIN_VALUE;
		ID++;
	}

	public boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			instances.remove(id);
			return false;
		}

		if (Methods.isRegionProtectedFromBuild(player, "Blaze", location)) {
			instances.remove(id);
			return false;
		}

		speedfactor = speed * (ProjectKorra.time_step / 1000.);

		ticks++;

		if (ticks > maxticks) {
			instances.remove(id);
			return false;
		}

		Block block = location.getBlock();
		if (Methods.isSolid(block) || block.isLiquid()) {
			if (block.getType() == Material.FURNACE && canPowerFurnace) {
			} else if (FireStream.isIgnitable(player,
					block.getRelative(BlockFace.UP))) {
				ignite(location);
			}
			instances.remove(id);
			return false;
		}

		if (location.distance(origin) > range) {
			instances.remove(id);
			return false;
		}

		Methods.removeSpouts(location, player);

		double radius = FireBlast.affectingradius;
		Player source = player;
		if (EarthBlast.annihilateBlasts(location, radius, source)
				|| WaterManipulation.annihilateBlasts(location, radius, source)
				|| FireBlast.annihilateBlasts(location, radius, source)) {
			instances.remove(id);
			return false;
		}

		for (Entity entity : Methods.getEntitiesAroundPoint(location,
				affectingradius)) {
			affect(entity);
			if (entity instanceof LivingEntity) {
				break;
				// }
			}
		}

		advanceLocation();

		return true;
	}

	private void advanceLocation() {
		location.getWorld().playEffect(location, Effect.MOBSPAWNER_FLAMES, 0,
				(int) range);
		location = location.add(direction.clone().multiply(speedfactor));
	}

	private void ignite(Location location) {
		for (Block block : Methods
				.getBlocksAroundPoint(location, affectingradius)) {
			if (FireStream.isIgnitable(player, block) && !safe.contains(block)) {
				if (Methods.isPlant(block))
					new Plantbending(block);
				block.setType(Material.FIRE);
				if (dissipate) {
					FireStream.ignitedblocks.put(block, player);
					FireStream.ignitedtimes.put(block,
							System.currentTimeMillis());
				}
			}
		}
	}

	public static boolean progress(int ID) {
		if (instances.containsKey(ID))
			return instances.get(ID).progress();
		return false;
	}

	public static void progressAll() {
		for (int id : instances.keySet()) {
			progress(id);
		}
	}

	private void affect(Entity entity) {
		if (entity.getEntityId() != player.getEntityId()) {
			if (AvatarState.isAvatarState(player)) {
				entity.setVelocity(direction.clone().multiply(
						AvatarState.getValue(pushfactor)));
			} else {
				entity.setVelocity(direction.clone().multiply(pushfactor));
			}
			if (entity instanceof LivingEntity) {
				entity.setFireTicks(50);
				Methods.damageEntity(player, entity,  "FireBlast",  Methods
						.firebendingDayAugment((double) damage,
								entity.getWorld()));
				new Enflamed(entity, player);
				instances.remove(id);
			}
		}
	}

	public static void removeFireBlastsAroundPoint(Location location,
			double radius) {
		for (int id : instances.keySet()) {
			Location fireblastlocation = instances.get(id).location;
			if (location.getWorld() == fireblastlocation.getWorld()) {
				if (location.distance(fireblastlocation) <= radius)
					instances.remove(id);
			}
		}
		Fireball.removeFireballsAroundPoint(location, radius);
	}

	public static boolean annihilateBlasts(Location location, double radius,
			Player source) {
		boolean broke = false;
		for (int id : instances.keySet()) {
			FireBlast blast = instances.get(id);
			Location fireblastlocation = blast.location;
			if (location.getWorld() == fireblastlocation.getWorld()
					&& !blast.player.equals(source)) {
				if (location.distance(fireblastlocation) <= radius) {
					instances.remove(id);
					broke = true;
				}
			}
		}
		if (Fireball.annihilateBlasts(location, radius, source))
			broke = true;
		return broke;
	}

	public static void removeAll() {
		for (int id : instances.keySet()) {
			instances.remove(id);
		}
	}

	public static String getDescription() {
		return "FireBlast is the most fundamental bending technique of a firebender. "
				+ "To use, simply left-click in a direction. A blast of fire will be created at your fingertips. "
				+ "If this blast contacts an enemy, it will dissipate and engulf them in flames, "
				+ "doing additional damage and knocking them back slightly. "
				+ "If the blast hits terrain, it will ignite the nearby area. "
				+ "Additionally, if you hold sneak, you will charge up the fireblast. "
				+ "If you release it when it's charged, it will instead launch a powerful "
				+ "fireball that explodes on contact.";
	}

}
package com.projectkorra.ProjectKorra.waterbending;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.TempBlock;
import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.Utilities.ParticleEffect;
import com.projectkorra.ProjectKorra.chiblocking.Paralyze;
import com.projectkorra.ProjectKorra.firebending.FireCombo;
import com.projectkorra.ProjectKorra.firebending.FireCombo.FireComboStream;

public class WaterCombo {
	public static enum AbilityState {
		ICE_PILLAR_RISING, ICE_BULLET_FORMING
	}

	private static boolean enabled = ProjectKorra.plugin.getConfig()
	 .getBoolean("Abilities.Water.WaterCombo.Enabled");
	public static long ICE_WAVE_COOLDOWN = ProjectKorra.plugin.getConfig().getLong(
			"Abilities.Water.WaterCombo.IceWave.Cooldown");

	public static double ICE_PILLAR_HEIGHT = 8;
	public static double ICE_PILLAR_RADIUS = 1.5;
	public static double ICE_PILLAR_DAMAGE = 4;
	public static double ICE_PILLAR_RANGE = 10;
	public static long ICE_PILLAR_COOLDOWN = 500;

	public static double ICE_BULLET_RADIUS = ProjectKorra.plugin.getConfig().getDouble(
			"Abilities.Water.WaterCombo.IceBullet.Radius");
	public static double ICE_BULLET_DAMAGE = ProjectKorra.plugin.getConfig().getDouble(
			"Abilities.Water.WaterCombo.IceBullet.Damage");
	public static double ICE_BULLET_RANGE = ProjectKorra.plugin.getConfig().getDouble(
			"Abilities.Water.WaterCombo.IceBullet.Range");
	public static double ICE_BULLET_ANIM_SPEED = ProjectKorra.plugin.getConfig().getDouble(
			"Abilities.Water.WaterCombo.IceBullet.AnimationSpeed");
	public static int ICE_BULLET_MAX_SHOTS = ProjectKorra.plugin.getConfig().getInt(
			"Abilities.Water.WaterCombo.IceBullet.MaxShots");
	public static long ICE_BULLET_COOLDOWN = ProjectKorra.plugin.getConfig().getLong(
			"Abilities.Water.WaterCombo.IceBullet.Cooldown");
	public static long ICE_BULLET_SHOOT_TIME = ProjectKorra.plugin.getConfig().getLong(
			"Abilities.Water.WaterCombo.IceBullet.ShootTime");

	public static ArrayList<WaterCombo> instances = new ArrayList<WaterCombo>();
	public static ConcurrentHashMap<Block, TempBlock> frozenBlocks = new ConcurrentHashMap<Block, TempBlock>();

	private Player player;
	private BendingPlayer bplayer;
	private String ability;

	private long time;
	private Location origin;
	private Location currentLoc;
	private Location destination;
	private Vector direction;
	private AbilityState state;
	private int progressCounter = 0;
	private int leftClicks = 0, rightClicks = 0;
	private double damage = 0, speed = 0, range = 0, knockback = 0, radius = 0, shootTime = 0, maxShots = 0;
	private double shots = 0;
	private long cooldown = 0;
	private WaterSourceGrabber waterGrabber;
	private ArrayList<Entity> affectedEntities = new ArrayList<Entity>();
	private ArrayList<BukkitRunnable> tasks = new ArrayList<BukkitRunnable>();
	private ConcurrentHashMap<Block, TempBlock> affectedBlocks = new ConcurrentHashMap<Block, TempBlock>();

	public WaterCombo(Player player, String ability) {
		if (!enabled || !player.hasPermission("bending.ability.WaterCombo"))
			return;
		if (Methods.isRegionProtectedFromBuild(player, "WaterManipulation",
				player.getLocation()))
			return;
		time = System.currentTimeMillis();
		this.player = player;
		this.ability = ability;
		this.bplayer = Methods.getBendingPlayer(player.getName());

		if (Methods.isChiBlocked(player.getName())
				|| Bloodbending.isBloodbended(player)
				|| Paralyze.isParalyzed(player)) {
			return;
		}

		if (ability.equalsIgnoreCase("IceWave")) {
			cooldown = ICE_WAVE_COOLDOWN;
		} else if (ability.equalsIgnoreCase("IcePillar")) {
			damage = ICE_PILLAR_DAMAGE;
			range = ICE_PILLAR_RANGE;
			radius = ICE_PILLAR_RADIUS;
			cooldown = ICE_WAVE_COOLDOWN;
		} else if (ability.equalsIgnoreCase("IceBullet")) {
			damage = ICE_BULLET_DAMAGE;
			range = ICE_BULLET_RANGE;
			radius = ICE_BULLET_RADIUS;
			cooldown = ICE_BULLET_COOLDOWN;
			shootTime = ICE_BULLET_SHOOT_TIME;
			maxShots = ICE_BULLET_MAX_SHOTS;
			speed = 1;
		}
		double aug = Methods.getWaterbendingNightAugment(player.getWorld());
		if(aug > 1)
			aug = 1 + (aug - 1) / 3;
		damage *= aug;
		range *= aug;
		shootTime *= aug;
		maxShots *= aug;
		radius *= aug;
		if (AvatarState.isAvatarState(player)) {
			cooldown = 0;
			damage = AvatarState.getValue(damage);
			range = AvatarState.getValue(range);
			shootTime = AvatarState.getValue(shootTime);
			maxShots = AvatarState.getValue(maxShots);
			knockback = knockback * 1.3;
		}
		
		if(ability.equalsIgnoreCase("IceBulletLeftClick") || ability.equalsIgnoreCase("IceBulletRightClick"))
		{
			ArrayList<WaterCombo> bullets = getWaterCombo(player, "IceBullet");
			if(bullets.size() == 0)
				return;
			for(WaterCombo bullet : bullets)
			{
				if(ability.equalsIgnoreCase("IceBulletLeftClick"))
				{
					if(bullet.leftClicks <= bullet.rightClicks)
						bullet.leftClicks += 1;
				}
				else if(bullet.leftClicks >= bullet.rightClicks)
					bullet.rightClicks += 1;
			}
		}
		instances.add(this);
	}

	public void progress() {
		progressCounter++;
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}

		if (ability.equalsIgnoreCase("IceWave")) {
			if (origin == null
					&& WaterWave.containsType(player,
							WaterWave.AbilityType.RELEASE)) {
				if (bplayer.isOnCooldown("IceWave")
						&& !AvatarState.isAvatarState(player)) {
					remove();
					return;
				}
				bplayer.addCooldown("IceWave", cooldown);
				origin = player.getLocation();
				WaterWave wave = WaterWave.getType(player,
						WaterWave.AbilityType.RELEASE).get(0);
				wave.setIceWave(true);
			} else if (!WaterWave.containsType(player,
					WaterWave.AbilityType.RELEASE)) {
				remove();
				return;
			}
		} else if (ability.equalsIgnoreCase("IcePillar")) {
			// ABILITY NOT USED or Finished because RuneFist is creating a
			// similar ability
			if (progressCounter > 0) {
				remove();
				return;
			}
			if (origin == null) {
				if (bplayer.isOnCooldown("IcePillar")
						&& !AvatarState.isAvatarState(player)) {
					remove();
					return;
				}
				origin = player.getLocation();
				Entity ent = Methods.getTargetedEntity(player, range,
						new ArrayList<Entity>());
				if (ent == null || !(ent instanceof LivingEntity)) {
					remove();
					return;
				}

				Location startingLoc = Methods.getTopBlock(
						ent.getLocation().add(0, -1, 0), (int) range)
						.getLocation();
				if (startingLoc == null) {
					remove();
					return;
				}
				startingLoc.setX(ent.getLocation().getX());
				startingLoc.setZ(ent.getLocation().getZ());
				int badBlocks = 0;
				for (double x = -radius; x <= radius; x++)
					for (double z = -radius; z <= radius; z++) {
						Location tmpLoc = startingLoc.clone().add(x, 0, z);
						if (tmpLoc.distance(startingLoc) > radius)
							continue;

						Block block = Methods.getTopBlock(tmpLoc, (int) range,
								(int) range);
						if (!Methods.isWaterbendable(block, player))
							badBlocks++;
					}
				//Bukkit.broadcastMessage("Bad Blocks:" + badBlocks);
				if (badBlocks > 5) {
					remove();
					return;
				}
				this.origin = startingLoc;
				this.currentLoc = origin.clone();
				this.state = AbilityState.ICE_PILLAR_RISING;
				bplayer.addCooldown("IcePillar", cooldown);
			} else if (this.state == AbilityState.ICE_PILLAR_RISING) {
				if (Math.abs(currentLoc.distance(origin)) > ICE_PILLAR_HEIGHT) {
					remove();
					return;
				}
				for (double x = -radius; x <= radius; x++)
					for (double z = -radius; z <= radius; z++) {
						Block block = currentLoc.clone().add(x, 0, z)
								.getBlock();
						if (Methods.isWaterbendable(block, player)
								|| block.getType() == Material.AIR)
							if (block.getLocation().distance(currentLoc) > radius)
								continue;
						if (Methods.isRegionProtectedFromBuild(player,
								"WaterManipulation", block.getLocation()))
							continue;

						TempBlock tblock = new TempBlock(block, Material.ICE,
								(byte) 0);
						frozenBlocks.put(block, tblock);
					}
				currentLoc.add(0, 1, 0);
			}
		} else if (ability.equalsIgnoreCase("IceBullet")) {
			if(shots > maxShots || !player.isSneaking()){
				remove();
				return;
			}
			if (origin == null) {
				if (bplayer.isOnCooldown("IceBullet")
						&& !AvatarState.isAvatarState(player)) {
					remove();
					return;
				}
				Block waterBlock = Methods.getWaterSourceBlock(player, range,
						true);
				if (waterBlock == null) {
					remove();
					return;
				}
				this.time = 0;
				origin = waterBlock.getLocation();
				currentLoc = origin.clone();
				state = AbilityState.ICE_BULLET_FORMING;
				bplayer.addCooldown("IceBullet", cooldown);
				direction = new Vector(1, 0, 1);
				waterGrabber = new WaterSourceGrabber(player, origin.clone());
			} else if (waterGrabber.getState() == WaterSourceGrabber.AnimationState.FAILED) {
				remove();
				return;
			} else if (waterGrabber.getState() == WaterSourceGrabber.AnimationState.FINISHED) {
				if(this.time == 0)
					this.time = System.currentTimeMillis();
				long timeDiff = System.currentTimeMillis() - this.time;
				double animSpeed = ICE_BULLET_ANIM_SPEED;
				if(this.state == AbilityState.ICE_BULLET_FORMING)
				{
					if(timeDiff < 1000 * animSpeed)
					{
						double steps = radius * ((timeDiff + 100) / (1000.0 * animSpeed));
						revertBlocks();
						for(double i = 0; i < steps; i++)
						{
							drawWaterCircle(player.getEyeLocation().clone().add(0, i, 0), 360, 5, radius - i);
							drawWaterCircle(player.getEyeLocation().clone().add(0, -i, 0), 360, 5, radius - i);
						}
					}
					else if(timeDiff < 2500 * animSpeed)
					{
						revertBlocks();
						for(double i = 0; i < radius; i++)
						{
							drawWaterCircle(player.getEyeLocation().clone().add(0, i, 0), 360, 5, radius - i, Material.ICE, (byte) 0);
							drawWaterCircle(player.getEyeLocation().clone().add(0, -i, 0), 360, 5, radius - i, Material.ICE, (byte) 0);
						}
					}
					
					if(timeDiff < shootTime)
					{
						if(shots < rightClicks + leftClicks)
						{
							shots++;
							Vector vec = player.getEyeLocation().getDirection().normalize();
							Location loc = player.getEyeLocation().add(vec.clone().multiply(radius + 1.3));
							FireComboStream fs = new FireComboStream(null, vec,
									loc, range, speed);
							fs.setDensity(10);
							fs.setSpread(0.1F);
							fs.setUseNewParticles(true);
							fs.setParticleEffect(ParticleEffect.SNOW_SHOVEL);
							fs.setCollides(false);
							fs.runTaskTimer(ProjectKorra.plugin, (long) (0), 1L);
							tasks.add(fs);
						}
						manageShots();
					}
					else
						remove();
				}
			} else {
				waterGrabber.progress();
			}
		}
	}
	
	public void manageShots() {
		for (int i = 0; i < tasks.size(); i++) {
			if (((FireComboStream) tasks.get(i)).isCancelled()) {
				tasks.remove(i);
				i--;
			}
		}
		for (int i = 0; i < tasks.size(); i++) {
			FireComboStream fstream = (FireComboStream) tasks.get(i);
			Location loc = fstream.getLocation();

			if (!Methods.isTransparentToEarthbending(player,
					loc.clone().add(0, 0.2, 0).getBlock())) {
				fstream.remove();
				return;
			}
			if (i % 2 == 0) {
				for (Entity entity : Methods.getEntitiesAroundPoint(loc, 1.5)) {
					if (Methods.isRegionProtectedFromBuild(player, "WaterManipulation",
							entity.getLocation())) {
						remove();
						return;
					}
					/*if (!entity.equals(player)
							&& !affectedEntities.contains(entity)) {
						affectedEntities.add(entity);*/
					if(!entity.equals(player)) {
						if (knockback != 0) {
							Vector force = fstream.getDirection();
							entity.setVelocity(force.multiply(knockback));
						}
						if (damage != 0)
							if (entity instanceof LivingEntity)
								Methods.damageEntity(player, entity, damage);
					}
				}

				if (Methods.blockAbilities(player, FireCombo.abilitiesToBlock,
						loc, 1)) {
					fstream.remove();
				}
			}
		}
	}
	public void createBlock(Block block, Material mat) {
		createBlock(block, mat, (byte) 0);
	}

	public void createBlock(Block block, Material mat, byte data) {
		affectedBlocks.put(block, new TempBlock(block, mat, data));
	}

	public void revertBlocks() {
		Enumeration<Block> keys = affectedBlocks.keys();
		while (keys.hasMoreElements()) {
			Block block = keys.nextElement();
			affectedBlocks.get(block).revertBlock();
			affectedBlocks.remove(block);
		}
	}
	public void drawWaterCircle(Location loc, double theta, double increment, double radius){
		drawWaterCircle(loc, theta, increment, radius, Material.STATIONARY_WATER, (byte) 0);
	}
	public void drawWaterCircle(Location loc, double theta, double increment, double radius, Material mat, byte data) {
		double rotateSpeed = theta;
		direction = Methods.rotateXZ(direction, rotateSpeed);
		for (double i = 0; i < theta; i += increment) {
			Vector dir = Methods.rotateXZ(direction, i - theta / 2).normalize()
					.multiply(radius);
			dir.setY(0);
			Block block = loc.clone().add(dir).getBlock();
			currentLoc = block.getLocation();
			if (block.getType() == Material.AIR
					&& !Methods.isRegionProtectedFromBuild(player,
							"WaterManipulation", block.getLocation()))
				createBlock(block, mat, data);
		}
	}

	public void remove() {
		instances.remove(this);
		for (BukkitRunnable task : tasks)
			task.cancel();
		revertBlocks();
		if(waterGrabber != null)
			waterGrabber.remove();
	}

	public static void progressAll() {
		for (int i = instances.size() - 1; i >= 0; i--)
			instances.get(i).progress();
	}

	public static void removeAll() {
		for (int i = instances.size() - 1; i >= 0; i--) {
			instances.get(i).remove();
		}
	}

	public Player getPlayer() {
		return player;
	}

	public static ArrayList<WaterCombo> getWaterCombo(Player player) {
		ArrayList<WaterCombo> list = new ArrayList<WaterCombo>();
		for (WaterCombo combo : instances)
			if (combo.player != null && combo.player == player)
				list.add(combo);
		return list;
	}

	public static ArrayList<WaterCombo> getWaterCombo(Player player,
			String ability) {
		ArrayList<WaterCombo> list = new ArrayList<WaterCombo>();
		for (WaterCombo combo : instances)
			if (combo.player != null && combo.player == player
					&& ability != null && combo.ability.equalsIgnoreCase(ability))
				list.add(combo);
		return list;
	}

	public static boolean removeAroundPoint(Player player, String ability,
			Location loc, double radius) {
		boolean removed = false;
		for (int i = 0; i < instances.size(); i++) {
			WaterCombo combo = instances.get(i);
			if (combo.getPlayer().equals(player))
				continue;

			if (ability.equalsIgnoreCase("Twister")
					&& combo.ability.equalsIgnoreCase("Twister")) {
				if (combo.currentLoc != null
						&& Math.abs(combo.currentLoc.distance(loc)) <= radius) {
					instances.remove(combo);
					removed = true;
				}
			}
		}
		return removed;
	}

	public static boolean canThaw(Block block) {
		return frozenBlocks.containsKey(block);
	}

	public static void thaw(Block block) {
		if (frozenBlocks.containsKey(block)) {
			frozenBlocks.get(block).revertBlock();
			frozenBlocks.remove(block);
		}
	}
}
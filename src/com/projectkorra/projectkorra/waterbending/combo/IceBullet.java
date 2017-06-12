package com.projectkorra.projectkorra.waterbending.combo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.firebending.combo.FireCombo.FireComboStream;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.util.WaterSourceGrabber;

public class IceBullet extends IceAbility implements ComboAbility {

	public static enum AbilityState {
		ICE_PILLAR_RISING, ICE_BULLET_FORMING
	}
	
	private int leftClicks;
	private int rightClicks;
	private double damage;
	private double speed;
	private double range;
	private double knockback;
	private double radius;
	private double shootTime;
	private double shots;
	private double maxShots;
	private double animationSpeed;
	private long cooldown;
	private long time;
	private AbilityState state;
	private Location origin;
	private Location location;
	private Vector direction;
	private WaterSourceGrabber waterGrabber;
	private ArrayList<BukkitRunnable> tasks;
	private ConcurrentHashMap<Block, TempBlock> affectedBlocks;
	
	public IceBullet(Player player, String name) {
		super(player);
		
		this.time = System.currentTimeMillis();
		this.tasks = new ArrayList<>();
		this.affectedBlocks = new ConcurrentHashMap<>();

		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		}
		
		this.damage = getConfig().getDouble("Abilities.Water.WaterCombo.IceBullet.Damage");
		this.range = getConfig().getDouble("Abilities.Water.WaterCombo.IceBullet.Range");
		this.radius = getConfig().getDouble("Abilities.Water.WaterCombo.IceBullet.Radius");
		this.cooldown = getConfig().getLong("Abilities.Water.WaterCombo.IceBullet.Cooldown");
		this.shootTime = getConfig().getLong("Abilities.Water.WaterCombo.IceBullet.ShootTime");
		this.maxShots = getConfig().getInt("Abilities.Water.WaterCombo.IceBullet.MaxShots");
		this.animationSpeed = getConfig().getDouble("Abilities.Water.WaterCombo.IceBullet.AnimationSpeed");
		this.speed = 1;
		
		double aug = getNightFactor(player.getWorld());
		if (aug > 1) {
			aug = 1 + (aug - 1) / 3;
		}

		this.damage *= aug;
		this.range *= aug;
		this.shootTime *= aug;
		this.maxShots *= aug;
		this.radius *= aug;

		if (bPlayer.isAvatarState()) {
			this.cooldown = 0;
			this.damage = AvatarState.getValue(damage);
			this.range = AvatarState.getValue(range);
			this.shootTime = AvatarState.getValue(shootTime);
			this.maxShots = AvatarState.getValue(maxShots);
			this.knockback = knockback * 1.3;
		}

		if (name.equalsIgnoreCase("IceBulletLeftClick") || name.equalsIgnoreCase("IceBulletRightClick")) {
			Collection<IceBullet> bullets = CoreAbility.getAbilities(player, IceBullet.class);
			if (bullets.size() == 0) {
				return;
			}
			for (IceBullet bullet : bullets) {
				if (name.equalsIgnoreCase("IceBulletLeftClick")) {
					if (bullet.leftClicks <= bullet.rightClicks) {
						bullet.leftClicks += 1;
					}
				} else if (bullet.leftClicks >= bullet.rightClicks) {
					bullet.rightClicks += 1;
				}
			}
			return;
		}

		start();
	}

	@Override
	public String getName() {
		return "IceBullet";
	}

	@Override
	public boolean isCollidable() {
		return true;
	}

	@Override
	public void handleCollision(Collision collision) {
		if (collision.isRemovingFirst()) {
			ArrayList<BukkitRunnable> newTasks = new ArrayList<>();
			double collisionDistanceSquared = Math.pow(getCollisionRadius() + collision.getAbilitySecond().getCollisionRadius(), 2);
			// Remove all of the streams that are by this specific ourLocation.
			// Don't just do a single stream at a time or this algorithm becomes O(n^2) with
			// Collision's detection algorithm.
			for (BukkitRunnable task : getTasks()) {
				if (task instanceof FireComboStream) {
					FireComboStream stream = (FireComboStream) task;
					if (stream.getLocation().distanceSquared(collision.getLocationSecond()) > collisionDistanceSquared) {
						newTasks.add(stream);
					} else {
						stream.cancel();
					}
				} else {
					newTasks.add(task);
				}
			}
			setTasks(newTasks);
		}
	}

	@Override
	public List<Location> getLocations() {
		ArrayList<Location> locations = new ArrayList<>();
		for (BukkitRunnable task : getTasks()) {
			if (task instanceof FireComboStream) {
				FireComboStream stream = (FireComboStream) task;
				locations.add(stream.getLocation());
			}
		}
		return locations;
	}

	public static class IceBulletLeftClick extends IceBullet {

		public IceBulletLeftClick(Player player) {
			super(player, "IceBulletLeftClick");
		}

		@Override
		public String getName() {
			return "IceBullet";
		}

	}

	public static class IceBulletRightClick extends IceBullet {

		public IceBulletRightClick(Player player) {
			super(player, "IceBulletRightClick");
		}

		@Override
		public String getName() {
			return "IceBullet";
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

			if (!isTransparent(player, loc.clone().add(0, 0.2, 0).getBlock())) {
				fstream.remove();
				return;
			}
			if (i % 2 == 0) {
				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(loc, 1.5)) {
					if (GeneralMethods.isRegionProtectedFromBuild(player, "WaterManipulation", entity.getLocation())) {
						remove();
						return;
					}

					if (!entity.equals(player)) {
						if (knockback != 0) {
							Vector force = fstream.getDirection();
							entity.setVelocity(force.multiply(knockback));
						}
						if (damage != 0) {
							if (entity instanceof LivingEntity) {
								DamageHandler.damageEntity(entity, damage, this);
							}
						}
					}
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
	
	public void drawWaterCircle(Location loc, double theta, double increment, double radius) {
		drawWaterCircle(loc, theta, increment, radius, Material.STATIONARY_WATER, (byte) 0);
	}

	public void drawWaterCircle(Location loc, double theta, double increment, double radius, Material mat, byte data) {
		double rotateSpeed = theta;
		direction = GeneralMethods.rotateXZ(direction, rotateSpeed);

		for (double i = 0; i < theta; i += increment) {
			Vector dir = GeneralMethods.rotateXZ(direction, i - theta / 2).normalize().multiply(radius);
			dir.setY(0);
			Block block = loc.clone().add(dir).getBlock();
			location = block.getLocation();

			if (block.getType() == Material.AIR && !GeneralMethods.isRegionProtectedFromBuild(player, "WaterManipulation", block.getLocation())) {
				createBlock(block, mat, data);
			}
		}
	}
	
	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		} 
		
		if (shots > maxShots || !player.isSneaking()) {
			remove();
			return;
		}

		if (origin == null) {
			if (bPlayer.isOnCooldown("IceBullet") && !bPlayer.isAvatarState()) {
				remove();
				return;
			}

			Block waterBlock = BlockSource.getWaterSourceBlock(player, range, ClickType.LEFT_CLICK, true, true, bPlayer.canPlantbend());
			if (waterBlock == null) {
				remove();
				return;
			}

			time = 0;
			origin = waterBlock.getLocation();
			location = origin.clone();
			state = AbilityState.ICE_BULLET_FORMING;
			bPlayer.addCooldown("IceBullet", cooldown);
			direction = new Vector(1, 0, 1);
			waterGrabber = new WaterSourceGrabber(player, origin.clone());
		} else if (waterGrabber.getState() == WaterSourceGrabber.AnimationState.FAILED) {
			remove();
			return;
		} else if (waterGrabber.getState() == WaterSourceGrabber.AnimationState.FINISHED) {
			if (this.time == 0) {
				this.time = System.currentTimeMillis();
			}

			long timeDiff = System.currentTimeMillis() - this.time;
			if (this.state == AbilityState.ICE_BULLET_FORMING) {
				if (timeDiff < 1000 * animationSpeed) {
					double steps = radius * ((timeDiff + 100) / (1000.0 * animationSpeed));
					revertBlocks();
					for (double i = 0; i < steps; i++) {
						drawWaterCircle(player.getEyeLocation().clone().add(0, i, 0), 360, 5, radius - i);
						drawWaterCircle(player.getEyeLocation().clone().add(0, -i, 0), 360, 5, radius - i);
					}
				} else if (timeDiff < 2500 * animationSpeed) {
					revertBlocks();
					for (double i = 0; i < radius; i++) {
						drawWaterCircle(player.getEyeLocation().clone().add(0, i, 0), 360, 5, radius - i, Material.ICE, (byte) 0);
						drawWaterCircle(player.getEyeLocation().clone().add(0, -i, 0), 360, 5, radius - i, Material.ICE, (byte) 0);
					}
				}

				if (timeDiff < shootTime) {
					if (shots < rightClicks + leftClicks) {
						shots++;
						Vector vec = player.getEyeLocation().getDirection().normalize();
						Location loc = player.getEyeLocation().add(vec.clone().multiply(radius + 1.3));
						FireComboStream fs = new FireComboStream(null, vec, loc, range, speed, "IceBullet");

						fs.setDensity(10);
						fs.setSpread(0.1F);
						fs.setUseNewParticles(true);
						fs.setParticleEffect(ParticleEffect.SNOW_SHOVEL);
						fs.setCollides(false);
						fs.runTaskTimer(ProjectKorra.plugin, (0), 1L);
						tasks.add(fs);
					}
					manageShots();
				} else {
					remove();
					return;
				}
			}
		} else {
			waterGrabber.progress();
		}
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
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return null;
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		return null;
	}
	
	@Override
	public void remove() {
		super.remove();
		for (BukkitRunnable task : tasks) {
			task.cancel();
		}
		revertBlocks();
		if (waterGrabber != null) {
			waterGrabber.remove();
		}

		bPlayer.addCooldown(this);

	}
	
	public void revertBlocks() {
		Enumeration<Block> keys = affectedBlocks.keys();
		while (keys.hasMoreElements()) {
			Block block = keys.nextElement();
			affectedBlocks.get(block).revertBlock();
			affectedBlocks.remove(block);
		}
	}
	
	public int getLeftClicks() {
		return leftClicks;
	}

	public void setLeftClicks(int leftClicks) {
		this.leftClicks = leftClicks;
	}

	public int getRightClicks() {
		return rightClicks;
	}

	public void setRightClicks(int rightClicks) {
		this.rightClicks = rightClicks;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getKnockback() {
		return knockback;
	}

	public void setKnockback(double knockback) {
		this.knockback = knockback;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getShootTime() {
		return shootTime;
	}

	public void setShootTime(double shootTime) {
		this.shootTime = shootTime;
	}

	public double getShots() {
		return shots;
	}

	public void setShots(double shots) {
		this.shots = shots;
	}

	public double getMaxShots() {
		return maxShots;
	}

	public void setMaxShots(double maxShots) {
		this.maxShots = maxShots;
	}

	public double getAnimationSpeed() {
		return animationSpeed;
	}

	public void setAnimationSpeed(double animationSpeed) {
		this.animationSpeed = animationSpeed;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public AbilityState getState() {
		return state;
	}

	public void setState(AbilityState state) {
		this.state = state;
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

	public WaterSourceGrabber getWaterGrabber() {
		return waterGrabber;
	}

	public void setWaterGrabber(WaterSourceGrabber waterGrabber) {
		this.waterGrabber = waterGrabber;
	}

	public ArrayList<BukkitRunnable> getTasks() {
		return tasks;
	}

	public void setTasks(ArrayList<BukkitRunnable> tasks) {
		this.tasks = tasks;
	}

	public Map<Block, TempBlock> getAffectedBlocks() {
		return affectedBlocks;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
}

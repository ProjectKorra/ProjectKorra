package com.projectkorra.projectkorra.waterbending;

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

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.firebending.FireCombo;
import com.projectkorra.projectkorra.firebending.FireCombo.FireComboStream;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

/*
 * TODO: Combo classes should eventually be rewritten so that each combo is treated
 * as an individual ability. In the mean time, we will just place "fake"
 * classes so that CoreAbility will register each ability. 
 */
public class WaterCombo extends IceAbility implements ComboAbility {

	public static enum AbilityState {
		ICE_PILLAR_RISING, ICE_BULLET_FORMING
	}

	private static final ConcurrentHashMap<Block, TempBlock> FROZEN_BLOCKS = new ConcurrentHashMap<>();

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
	private String name;
	private Location origin;
	private Location location;
	private Vector direction;
	private WaterSourceGrabber waterGrabber;
	private ArrayList<BukkitRunnable> tasks;
	private ConcurrentHashMap<Block, TempBlock> affectedBlocks;

	public WaterCombo(Player player, String name) {
		super(player);

		this.time = System.currentTimeMillis();
		this.name = name;
		this.tasks = new ArrayList<>();
		this.affectedBlocks = new ConcurrentHashMap<>();

		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		}

		if (name.equalsIgnoreCase("IceWave")) {
			
			if (bPlayer.isOnCooldown("IceWave") && !bPlayer.isAvatarState()) {
				remove();
				return;
			}
			
			this.cooldown = getConfig().getLong("Abilities.Water.WaterCombo.IceWave.Cooldown");
		} else if (name.equalsIgnoreCase("IceBullet")) {
			this.damage = getConfig().getDouble("Abilities.Water.WaterCombo.IceBullet.Damage");
			this.range = getConfig().getDouble("Abilities.Water.WaterCombo.IceBullet.Range");
			this.radius = getConfig().getDouble("Abilities.Water.WaterCombo.IceBullet.Radius");
			this.cooldown = getConfig().getLong("Abilities.Water.WaterCombo.IceBullet.Cooldown");
			this.shootTime = getConfig().getLong("Abilities.Water.WaterCombo.IceBullet.ShootTime");
			this.maxShots = getConfig().getInt("Abilities.Water.WaterCombo.IceBullet.MaxShots");
			this.animationSpeed = getConfig().getDouble("Abilities.Water.WaterCombo.IceBullet.AnimationSpeed");
			this.speed = 1;
		}
		
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
			ArrayList<WaterCombo> bullets = getWaterCombo(player, "IceBullet");
			if (bullets.size() == 0) {
				return;
			}
			for (WaterCombo bullet : bullets) {
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

				if (GeneralMethods.blockAbilities(player, FireCombo.getBlockableAbilities(), loc, 1)) {
					fstream.remove();
				}
			}
		}
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		} else if (name.equalsIgnoreCase("IceWave")) {
			if (origin == null && WaterSpoutWave.containsType(player, WaterSpoutWave.AbilityType.RELEASE)) {
				bPlayer.addCooldown("IceWave", cooldown);
				origin = player.getLocation();
				
				WaterSpoutWave wave = WaterSpoutWave.getType(player, WaterSpoutWave.AbilityType.RELEASE).get(0);
				wave.setIceWave(true);
			} else if (!WaterSpoutWave.containsType(player, WaterSpoutWave.AbilityType.RELEASE)) {
				remove();
				return;
			}
		} else if (name.equalsIgnoreCase("IceBullet")) {
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
		
		if (name == "IceWave") {
			bPlayer.addCooldown("WaterWave", getConfig().getLong("Abilities.Water.WaterSpout.Wave.Cooldown"));
		}
	}

	public void revertBlocks() {
		Enumeration<Block> keys = affectedBlocks.keys();
		while (keys.hasMoreElements()) {
			Block block = keys.nextElement();
			affectedBlocks.get(block).revertBlock();
			affectedBlocks.remove(block);
		}
	}

	public static boolean canThaw(Block block) {
		return FROZEN_BLOCKS.containsKey(block);
	}

	public static ArrayList<WaterCombo> getWaterCombo(Player player, String ability) {
		ArrayList<WaterCombo> list = new ArrayList<WaterCombo>();
		if (player == null || ability == null) {
			return list;
		}
		for (WaterCombo combo : getAbilities(player, WaterCombo.class)) {
			if (player.equals(combo.player) && combo.name.equalsIgnoreCase(ability)) {
				list.add(combo);
			}
		}
		return list;
	}

	public static void thaw(Block block) {
		if (FROZEN_BLOCKS.containsKey(block)) {
			FROZEN_BLOCKS.get(block).revertBlock();
			FROZEN_BLOCKS.remove(block);
		}
	}

	@Override
	public String getName() {
		return name != null ? name : "WaterCombo";
	}

	@Override
	public Location getLocation() {
		return location != null ? location : origin;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}
	
	@Override
	public boolean isHiddenAbility() {
		return true;
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
	public String getInstructions() {
		return null;
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return null;
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		return null;
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

	public static ConcurrentHashMap<Block, TempBlock> getFrozenBlocks() {
		return FROZEN_BLOCKS;
	}

	public ConcurrentHashMap<Block, TempBlock> getAffectedBlocks() {
		return affectedBlocks;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	
	public class IceWave extends WaterCombo {

		public IceWave(Player player, String name) {
			super(player, "IceWave");
		}
		
		@Override
		public String getName() {
			return "IceWave";
		}
		
	}
	
	public class IceBullet extends WaterCombo {

		public IceBullet(Player player, String name) {
			super(player, "IceBullet");
		}
		
		@Override
		public String getName() {
			return "IceBullet";
		}
		
	}
	
}
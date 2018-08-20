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
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.firebending.combo.FireComboStream;
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
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute(Attribute.RADIUS)
	private double radius;
	private double shootTime;
	private double shots;
	@Attribute("MaxShots")
	private double maxShots;
	private double animationSpeed;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	private long time;
	private String name;
	private AbilityState state;
	private Location origin;
	private Location location;
	private Vector direction;
	private WaterSourceGrabber waterGrabber;
	private ArrayList<BukkitRunnable> tasks;
	private ConcurrentHashMap<Block, TempBlock> affectedBlocks;

	public IceBullet(final Player player) {
		super(player);

		this.time = System.currentTimeMillis();
		this.tasks = new ArrayList<>();
		this.affectedBlocks = new ConcurrentHashMap<>();

		if (!this.bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		}

		this.damage = getConfig().getDouble("Abilities.Water.IceBullet.Damage");
		this.range = getConfig().getDouble("Abilities.Water.IceBullet.Range");
		this.radius = getConfig().getDouble("Abilities.Water.IceBullet.Radius");
		this.cooldown = getConfig().getLong("Abilities.Water.IceBullet.Cooldown");
		this.shootTime = getConfig().getLong("Abilities.Water.IceBullet.ShootTime");
		this.maxShots = getConfig().getInt("Abilities.Water.IceBullet.MaxShots");
		this.animationSpeed = getConfig().getDouble("Abilities.Water.IceBullet.AnimationSpeed");
		this.speed = 1;
		this.name = this.getName();

		double aug = getNightFactor(player.getWorld());
		if (aug > 1) {
			aug = 1 + (aug - 1) / 3;
		}

		this.damage *= aug;
		this.range *= aug;
		this.shootTime *= aug;
		this.maxShots *= aug;
		this.radius *= aug;

		if (this.bPlayer.isAvatarState()) {
			this.cooldown = 0;
			this.damage = AvatarState.getValue(this.damage);
			this.range = AvatarState.getValue(this.range);
			this.shootTime = AvatarState.getValue(this.shootTime);
			this.maxShots = AvatarState.getValue(this.maxShots);
		}

		this.start();
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
	public void handleCollision(final Collision collision) {
		if (collision.isRemovingFirst()) {
			final ArrayList<BukkitRunnable> newTasks = new ArrayList<>();
			final double collisionDistanceSquared = Math.pow(this.getCollisionRadius() + collision.getAbilitySecond().getCollisionRadius(), 2);
			// Remove all of the streams that are by this specific ourLocation.
			// Don't just do a single stream at a time or this algorithm becomes O(n^2) with Collision's detection algorithm.
			for (final BukkitRunnable task : this.getTasks()) {
				if (task instanceof FireComboStream) {
					final FireComboStream stream = (FireComboStream) task;
					if (stream.getLocation().distanceSquared(collision.getLocationSecond()) > collisionDistanceSquared) {
						newTasks.add(stream);
					} else {
						stream.cancel();
					}
				} else {
					newTasks.add(task);
				}
			}
			this.setTasks(newTasks);
		}
	}

	@Override
	public List<Location> getLocations() {
		final ArrayList<Location> locations = new ArrayList<>();
		for (final BukkitRunnable task : this.getTasks()) {
			if (task instanceof FireComboStream) {
				final FireComboStream stream = (FireComboStream) task;
				locations.add(stream.getLocation());
			}
		}
		return locations;
	}

	public static class IceBulletLeftClick extends IceBullet {

		public IceBulletLeftClick(final Player player) {
			super(player);
			this.setName("IceBulletLeftClick");
		}

		@Override
		public String getName() {
			return "IceBullet";
		}

	}

	public static class IceBulletRightClick extends IceBullet {

		public IceBulletRightClick(final Player player) {
			super(player);
			this.setName("IceBulletRightClick");
		}

		@Override
		public String getName() {
			return "IceBullet";
		}

	}

	public void manageShots() {
		for (int i = 0; i < this.tasks.size(); i++) {
			if (((FireComboStream) this.tasks.get(i)).isCancelled()) {
				this.tasks.remove(i);
				i--;
			}
		}

		for (int i = 0; i < this.tasks.size(); i++) {
			final FireComboStream fstream = (FireComboStream) this.tasks.get(i);
			final Location loc = fstream.getLocation();

			if (!isTransparent(this.player, loc.clone().add(0, 0.2, 0).getBlock())) {
				fstream.remove();
				return;
			}
			if (i % 2 == 0) {
				for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(loc, 1.5)) {
					if (GeneralMethods.isRegionProtectedFromBuild(this.player, "WaterManipulation", entity.getLocation())) {
						this.remove();
						return;
					}

					if (!entity.equals(this.player)) {
						if (this.damage != 0) {
							if (entity instanceof LivingEntity) {
								DamageHandler.damageEntity(entity, this.damage, this);
							}
						}
					}
				}
			}
		}
	}

	public void createBlock(final Block block, final Material mat) {
		this.createBlock(block, mat, (byte) 0);
	}

	public void createBlock(final Block block, final Material mat, final byte data) {
		this.affectedBlocks.put(block, new TempBlock(block, mat, data));
	}

	public void drawWaterCircle(final Location loc, final double theta, final double increment, final double radius) {
		this.drawWaterCircle(loc, theta, increment, radius, Material.STATIONARY_WATER, (byte) 0);
	}

	public void drawWaterCircle(final Location loc, final double theta, final double increment, final double radius, final Material mat, final byte data) {
		final double rotateSpeed = theta;
		this.direction = GeneralMethods.rotateXZ(this.direction, rotateSpeed);

		for (double i = 0; i < theta; i += increment) {
			final Vector dir = GeneralMethods.rotateXZ(this.direction, i - theta / 2).normalize().multiply(radius);
			dir.setY(0);
			final Block block = loc.clone().add(dir).getBlock();
			this.location = block.getLocation();

			if (block.getType() == Material.AIR && !GeneralMethods.isRegionProtectedFromBuild(this.player, "WaterManipulation", block.getLocation())) {
				this.createBlock(block, mat, data);
			}
		}
	}

	@Override
	public void progress() {
		if (this.player.isDead() || !this.player.isOnline()) {
			this.remove();
			return;
		}

		if (this.shots > this.maxShots || !this.player.isSneaking()) {
			this.remove();
			return;
		}

		if (this.name.equalsIgnoreCase("IceBulletLeftClick") || this.name.equalsIgnoreCase("IceBulletRightClick")) {
			final Collection<IceBullet> bullets = CoreAbility.getAbilities(this.player, IceBullet.class);
			if (bullets.size() == 0) {
				return;
			}
			for (final IceBullet bullet : bullets) {
				if (this.name.equalsIgnoreCase("IceBulletLeftClick")) {
					if (bullet.leftClicks <= bullet.rightClicks) {
						bullet.leftClicks += 1;
					}
				} else if (bullet.leftClicks >= bullet.rightClicks) {
					bullet.rightClicks += 1;
				}
			}
			return;
		}

		if (this.origin == null) {
			if (this.bPlayer.isOnCooldown("IceBullet") && !this.bPlayer.isAvatarState()) {
				this.remove();
				return;
			}

			final Block waterBlock = BlockSource.getWaterSourceBlock(this.player, this.range, ClickType.LEFT_CLICK, true, true, this.bPlayer.canPlantbend());
			if (waterBlock == null) {
				this.remove();
				return;
			}

			this.time = 0;
			this.origin = waterBlock.getLocation();
			this.location = this.origin.clone();
			this.state = AbilityState.ICE_BULLET_FORMING;
			this.bPlayer.addCooldown("IceBullet", this.cooldown);
			this.direction = new Vector(1, 0, 1);
			this.waterGrabber = new WaterSourceGrabber(this.player, this.origin.clone());
		} else if (this.waterGrabber.getState() == WaterSourceGrabber.AnimationState.FAILED) {
			this.remove();
			return;
		} else if (this.waterGrabber.getState() == WaterSourceGrabber.AnimationState.FINISHED) {
			if (this.time == 0) {
				this.time = System.currentTimeMillis();
			}

			final long timeDiff = System.currentTimeMillis() - this.time;
			if (this.state == AbilityState.ICE_BULLET_FORMING) {
				if (timeDiff < 1000 * this.animationSpeed) {
					final double steps = this.radius * ((timeDiff + 100) / (1000.0 * this.animationSpeed));
					this.revertBlocks();
					for (double i = 0; i < steps; i++) {
						this.drawWaterCircle(this.player.getEyeLocation().clone().add(0, i, 0), 360, 5, this.radius - i);
						this.drawWaterCircle(this.player.getEyeLocation().clone().add(0, -i, 0), 360, 5, this.radius - i);
					}
				} else if (timeDiff < 2500 * this.animationSpeed) {
					this.revertBlocks();
					for (double i = 0; i < this.radius; i++) {
						this.drawWaterCircle(this.player.getEyeLocation().clone().add(0, i, 0), 360, 5, this.radius - i, Material.ICE, (byte) 0);
						this.drawWaterCircle(this.player.getEyeLocation().clone().add(0, -i, 0), 360, 5, this.radius - i, Material.ICE, (byte) 0);
					}
				}

				if (timeDiff < this.shootTime) {
					if (this.shots < this.rightClicks + this.leftClicks) {
						this.shots++;
						final Vector vec = this.player.getEyeLocation().getDirection().normalize();
						final Location loc = this.player.getEyeLocation().add(vec.clone().multiply(this.radius + 1.3));
						final FireComboStream fs = new FireComboStream(this.player, this, vec, loc, this.range, this.speed);

						fs.setDensity(10);
						fs.setSpread(0.1F);
						fs.setUseNewParticles(true);
						fs.setParticleEffect(ParticleEffect.SNOW_SHOVEL);
						fs.setCollides(false);
						fs.runTaskTimer(ProjectKorra.plugin, (0), 1L);
						this.tasks.add(fs);
					}
					this.manageShots();
				} else {
					this.remove();
					return;
				}
			}
		} else {
			this.waterGrabber.progress();
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
		return this.cooldown;
	}

	@Override
	public Location getLocation() {
		return this.location;
	}

	@Override
	public Object createNewComboInstance(final Player player) {
		return new IceBullet(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		final ArrayList<AbilityInformation> iceBullet = new ArrayList<>();
		iceBullet.add(new AbilityInformation("WaterBubble", ClickType.SHIFT_DOWN));
		iceBullet.add(new AbilityInformation("WaterBubble", ClickType.SHIFT_UP));
		iceBullet.add(new AbilityInformation("IceBlast", ClickType.SHIFT_DOWN));
		return iceBullet;
	}

	@Override
	public void remove() {
		super.remove();
		for (final BukkitRunnable task : this.tasks) {
			task.cancel();
		}
		this.revertBlocks();
		if (this.waterGrabber != null) {
			this.waterGrabber.remove();
		}

		this.bPlayer.addCooldown(this);

	}

	public void revertBlocks() {
		final Enumeration<Block> keys = this.affectedBlocks.keys();
		while (keys.hasMoreElements()) {
			final Block block = keys.nextElement();
			this.affectedBlocks.get(block).revertBlock();
			this.affectedBlocks.remove(block);
		}
	}

	public int getLeftClicks() {
		return this.leftClicks;
	}

	public void setLeftClicks(final int leftClicks) {
		this.leftClicks = leftClicks;
	}

	public int getRightClicks() {
		return this.rightClicks;
	}

	public void setRightClicks(final int rightClicks) {
		this.rightClicks = rightClicks;
	}

	public double getDamage() {
		return this.damage;
	}

	public void setDamage(final double damage) {
		this.damage = damage;
	}

	public double getSpeed() {
		return this.speed;
	}

	public void setSpeed(final double speed) {
		this.speed = speed;
	}

	public double getRange() {
		return this.range;
	}

	public void setRange(final double range) {
		this.range = range;
	}

	public double getRadius() {
		return this.radius;
	}

	public void setRadius(final double radius) {
		this.radius = radius;
	}

	public double getShootTime() {
		return this.shootTime;
	}

	public void setShootTime(final double shootTime) {
		this.shootTime = shootTime;
	}

	public double getShots() {
		return this.shots;
	}

	public void setShots(final double shots) {
		this.shots = shots;
	}

	public double getMaxShots() {
		return this.maxShots;
	}

	public void setMaxShots(final double maxShots) {
		this.maxShots = maxShots;
	}

	public double getAnimationSpeed() {
		return this.animationSpeed;
	}

	public void setAnimationSpeed(final double animationSpeed) {
		this.animationSpeed = animationSpeed;
	}

	public long getTime() {
		return this.time;
	}

	public void setTime(final long time) {
		this.time = time;
	}

	public AbilityState getState() {
		return this.state;
	}

	public void setState(final AbilityState state) {
		this.state = state;
	}

	public Location getOrigin() {
		return this.origin;
	}

	public void setOrigin(final Location origin) {
		this.origin = origin;
	}

	public Vector getDirection() {
		return this.direction;
	}

	public void setDirection(final Vector direction) {
		this.direction = direction;
	}

	public WaterSourceGrabber getWaterGrabber() {
		return this.waterGrabber;
	}

	public void setWaterGrabber(final WaterSourceGrabber waterGrabber) {
		this.waterGrabber = waterGrabber;
	}

	public ArrayList<BukkitRunnable> getTasks() {
		return this.tasks;
	}

	public void setTasks(final ArrayList<BukkitRunnable> tasks) {
		this.tasks = tasks;
	}

	public Map<Block, TempBlock> getAffectedBlocks() {
		return this.affectedBlocks;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public String getInstructions() {
		return "WaterBubble (Tap Shift) > IceBlast (Hold Shift) > Wait for ice to Form > Then alternate between Left and Right click with IceBlast";
	}
}

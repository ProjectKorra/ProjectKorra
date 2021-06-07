package com.projectkorra.projectkorra.firebending;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlastFurnace;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Campfire;
import org.bukkit.block.Furnace;
import org.bukkit.block.Smoker;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.BlueFireAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.firebending.util.FireDamageTimer;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.waterbending.plant.PlantRegrowth;

public class FireBlast extends FireAbility {

	private static final int MAX_TICKS = 10000;

	@Attribute("PowerFurnace")
	private boolean powerFurnace;
	private boolean showParticles;
	private boolean dissipate;
	private boolean isFireBurst = false;
	private boolean fireBurstIgnite;
	private int ticks;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	private double speedFactor;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.SPEED)
	private double speed;
	private double collisionRadius;
	@Attribute(Attribute.FIRE_TICK)
	private double fireTicks;
	@Attribute(Attribute.KNOCKBACK)
	private double knockback;
	private double flameRadius;
	private Random random;
	private Location location;
	private Location origin;
	private Vector direction;
	private List<Block> safeBlocks;

	public FireBlast(final Location location, final Vector direction, final Player player, final int damage, final List<Block> safeBlocks) {
		super(player);

		if (location.getBlock().isLiquid()) {
			return;
		}

		this.setFields();
		this.safeBlocks = safeBlocks;
		this.damage = damage;
		this.location = location.clone();
		this.origin = location.clone();
		this.direction = direction.clone().normalize();

		// The following code determines the total additive modifier between Blue Fire & Day Modifiers
		this.applyModifiers(this.damage, this.range);

		this.start();
	}
	
	public FireBlast(final Player player) {
		super(player);

		if (this.bPlayer.isOnCooldown("FireBlast")) {
			return;
		} else if (player.getEyeLocation().getBlock().isLiquid() || FireBlastCharged.isCharging(player)) {
			return;
		}

		this.setFields();
		this.isFireBurst = false;
		this.damage = getConfig().getDouble("Abilities.Fire.FireBlast.Damage");
		this.safeBlocks = new ArrayList<>();
		this.location = player.getEyeLocation();
		this.origin = player.getEyeLocation();
		this.direction = player.getEyeLocation().getDirection().normalize();
		this.location = this.location.add(this.direction.clone());
		
		// The following code determines the total additive modifier between Blue Fire & Day Modifiers
		this.applyModifiers(this.damage, this.range);

		this.start();
		this.bPlayer.addCooldown("FireBlast", this.cooldown);
	}

	private void applyModifiers(double damage, double range) {
		double damageMod = 0;
		double rangeMod = 0;

		damageMod = this.getDayFactor(damage) - damage;
		rangeMod = this.getDayFactor(range) - range;

		damageMod = bPlayer.canUseSubElement(SubElement.BLUE_FIRE) ? (BlueFireAbility.getDamageFactor() * damage - damage) + damageMod : damageMod;
		rangeMod = bPlayer.canUseSubElement(SubElement.BLUE_FIRE) ? (BlueFireAbility.getRangeFactor() * range - range) + rangeMod : rangeMod;

		this.range += rangeMod;
		this.damage += damageMod;
	}

	private void setFields() {
		this.isFireBurst = true;
		this.powerFurnace = true;
		this.showParticles = true;
		this.fireBurstIgnite = getConfig().getBoolean("Abilities.Fire.FireBurst.Ignite");
		this.dissipate = getConfig().getBoolean("Abilities.Fire.FireBlast.Dissipate");
		this.cooldown = getConfig().getLong("Abilities.Fire.FireBlast.Cooldown");
		this.range = getConfig().getDouble("Abilities.Fire.FireBlast.Range");
		this.speed = getConfig().getDouble("Abilities.Fire.FireBlast.Speed");
		this.collisionRadius = getConfig().getDouble("Abilities.Fire.FireBlast.CollisionRadius");
		this.fireTicks = getConfig().getDouble("Abilities.Fire.FireBlast.FireTicks");
		this.knockback = getConfig().getDouble("Abilities.Fire.FireBlast.Knockback");
		this.flameRadius = getConfig().getDouble("Abilities.Fire.FireBlast.FlameParticleRadius");
		this.random = new Random();
	}

	private void advanceLocation() {
		if (this.isFireBurst) {
			this.flameRadius += 0.06;
		}

		if (this.showParticles) {
			playFirebendingParticles(this.location, 6, this.flameRadius, this.flameRadius, this.flameRadius);
		}

		if (GeneralMethods.checkDiagonalWall(this.location, this.direction)) {
			this.remove();
			return;
		}

		
		BlockIterator blocks = new BlockIterator(this.getLocation().getWorld(), this.location.toVector(), this.direction, 0, (int) Math.ceil(this.direction.clone().multiply(speedFactor).length()));

		while (blocks.hasNext() && checkLocation(blocks.next()));
		
		this.location.add(this.direction.clone().multiply(speedFactor));

		if (this.random.nextInt(4) == 0) {
			playFirebendingSound(this.location);
		}
	}

	public boolean checkLocation(Block block) {
		if (block.isLiquid()) {
			this.remove();
			return false;
		}
		if (!block.isPassable()) {
			if (block.getType() == Material.FURNACE && this.powerFurnace) {
				final Furnace furnace = (Furnace) block.getState();
				furnace.setBurnTime((short) 800);
				furnace.update();
			} else if (block.getType() == Material.SMOKER && this.powerFurnace) {
				final Smoker smoker = (Smoker) block.getState();
				smoker.setBurnTime((short) 800);
				smoker.update();
			} else if (block.getType() == Material.BLAST_FURNACE && this.powerFurnace) {
				final BlastFurnace blastF = (BlastFurnace) block.getState();
				blastF.setBurnTime((short) 800);
				blastF.update();
			} else if (block instanceof Campfire) {
				final Campfire campfire = (Campfire) block.getBlockData();
				if(!campfire.isLit()) {
					if(block.getType() != Material.SOUL_CAMPFIRE || bPlayer.canUseSubElement(SubElement.BLUE_FIRE)) {
						campfire.setLit(true);
					}
				}
			} else if (isIgnitable(block.getRelative(BlockFace.UP))) {
				if ((this.isFireBurst && this.fireBurstIgnite) || !this.isFireBurst) {
					this.ignite(this.location);
				}
			}
			this.remove();
			return false;
		}
		return true;
	}
	private void affect(final Entity entity) {
		if (entity.getUniqueId() != this.player.getUniqueId() && !GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation()) && !((entity instanceof Player) && Commands.invincible.contains(((Player) entity).getName()))) {
			if (this.bPlayer.isAvatarState()) {
				GeneralMethods.setVelocity(this, entity, this.direction.clone().multiply(AvatarState.getValue(this.knockback)));
			} else {
				GeneralMethods.setVelocity(this, entity, this.direction.clone().multiply(this.knockback));
			}
			if (entity instanceof LivingEntity) {
				entity.setFireTicks((int) (this.fireTicks * 20));
				DamageHandler.damageEntity(entity, this.damage, this);
				AirAbility.breakBreathbendingHold(entity);
				new FireDamageTimer(entity, this.player);
				this.remove();
			}
		}
	}

	private void ignite(final Location location) {
		for (final Block block : GeneralMethods.getBlocksAroundPoint(location, this.collisionRadius)) {
			if (isIgnitable(block) && !this.safeBlocks.contains(block) && !GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
				if (canFireGrief()) {
					if (isPlant(block) || isSnow(block)) {
						new PlantRegrowth(this.player, block);
					}
				}
				createTempFire(block.getLocation());	
			}
		}
	}

	@Override
	public void progress() {
		if (!this.bPlayer.canBendIgnoreBindsCooldowns(this) || GeneralMethods.isRegionProtectedFromBuild(this, this.location)) {
			this.remove();
			return;
		}

		this.speedFactor = this.speed * (ProjectKorra.time_step / 1000.0);
		this.ticks++;

		if (this.ticks > MAX_TICKS) {
			this.remove();
			return;
		}

		if (this.location.distanceSquared(this.origin) > this.range * this.range) {
			this.remove();
			return;
		}

		Entity entity = GeneralMethods.getClosestEntity(this.location, this.collisionRadius);
		if (entity != null) {
			this.affect(entity);
		}

		this.advanceLocation();
	}

	/**
	 * This method was used for the old collision detection system. Please see
	 * {@link Collision} for the new system.
	 */
	@Deprecated
	public static boolean annihilateBlasts(final Location location, final double radius, final Player source) {
		boolean broke = false;
		for (final FireBlast blast : getAbilities(FireBlast.class)) {
			final Location fireBlastLocation = blast.location;
			if (location.getWorld().equals(fireBlastLocation.getWorld()) && !blast.player.equals(source)) {
				if (location.distanceSquared(fireBlastLocation) <= radius * radius) {
					blast.remove();
					broke = true;
				}
			}
		}
		if (FireBlastCharged.annihilateBlasts(location, radius, source)) {
			broke = true;
		}
		return broke;
	}

	public static ArrayList<FireBlast> getAroundPoint(final Location location, final double radius) {
		final ArrayList<FireBlast> list = new ArrayList<FireBlast>();
		for (final FireBlast fireBlast : getAbilities(FireBlast.class)) {
			final Location fireblastlocation = fireBlast.location;
			if (location.getWorld().equals(fireblastlocation.getWorld())) {
				if (location.distanceSquared(fireblastlocation) <= radius * radius) {
					list.add(fireBlast);
				}
			}
		}
		return list;
	}

	public static void removeFireBlastsAroundPoint(final Location location, final double radius) {
		for (final FireBlast fireBlast : getAbilities(FireBlast.class)) {
			final Location fireBlastLocation = fireBlast.location;
			if (location.getWorld().equals(fireBlastLocation.getWorld())) {
				if (location.distanceSquared(fireBlastLocation) <= radius * radius) {
					fireBlast.remove();
				}
			}
		}
		FireBlastCharged.removeFireballsAroundPoint(location, radius);
	}

	@Override
	public String getName() {
		return this.isFireBurst ? "FireBurst" : "FireBlast";
	}

	@Override
	public Location getLocation() {
		return this.location != null ? this.location : this.origin;
	}

	@Override
	public long getCooldown() {
		return this.cooldown;
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
	public double getCollisionRadius() {
		return this.collisionRadius;
	}

	public boolean isPowerFurnace() {
		return this.powerFurnace;
	}

	public void setPowerFurnace(final boolean powerFurnace) {
		this.powerFurnace = powerFurnace;
	}

	public boolean isShowParticles() {
		return this.showParticles;
	}

	public void setShowParticles(final boolean showParticles) {
		this.showParticles = showParticles;
	}

	public boolean isDissipate() {
		return this.dissipate;
	}

	public void setDissipate(final boolean dissipate) {
		this.dissipate = dissipate;
	}

	public int getTicks() {
		return this.ticks;
	}

	public void setTicks(final int ticks) {
		this.ticks = ticks;
	}

	public double getSpeedFactor() {
		return this.speedFactor;
	}

	public void setSpeedFactor(final double speedFactor) {
		this.speedFactor = speedFactor;
	}

	public double getRange() {
		return this.range;
	}

	public void setRange(final double range) {
		this.range = range;
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

	public void setCollisionRadius(final double collisionRadius) {
		this.collisionRadius = collisionRadius;
	}

	public double getFireTicks() {
		return this.fireTicks;
	}

	public void setFireTicks(final double fireTicks) {
		this.fireTicks = fireTicks;
	}

	public double getPushFactor() {
		return this.knockback;
	}

	public void setPushFactor(final double pushFactor) {
		this.knockback = pushFactor;
	}

	public Random getRandom() {
		return this.random;
	}

	public void setRandom(final Random random) {
		this.random = random;
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

	public static int getMaxTicks() {
		return MAX_TICKS;
	}

	public List<Block> getSafeBlocks() {
		return this.safeBlocks;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

	public boolean isFireBurst() {
		return this.isFireBurst;
	}

	public void setFireBurst(final boolean isFireBurst) {
		this.isFireBurst = isFireBurst;
	}

}

package com.projectkorra.projectkorra.earthbending;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.earthbending.passive.DensityShift;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;

public class EarthBlast extends EarthAbility {
	private boolean isProgressing;
	private boolean isAtDestination;
	private boolean isSettingUp;
	private boolean canHitSelf;
	private long time;
	private long interval;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.KNOCKBACK)
	private double pushFactor;
	@Attribute(Attribute.SELECT_RANGE)
	private double selectRange;
	@Attribute("DeflectRange")
	private double deflectRange;
	private double collisionRadius;
	private Material sourceType;
	private Location location;
	private Location destination;
	private Location firstDestination;
	private Block sourceBlock;

	public EarthBlast(final Player player) {
		super(player);

		this.isProgressing = false;
		this.isAtDestination = false;
		this.isSettingUp = true;
		this.deflectRange = getConfig().getDouble("Abilities.Earth.EarthBlast.DeflectRange");
		this.collisionRadius = getConfig().getDouble("Abilities.Earth.EarthBlast.CollisionRadius");
		this.cooldown = getConfig().getLong("Abilities.Earth.EarthBlast.Cooldown");
		this.canHitSelf = getConfig().getBoolean("Abilities.Earth.EarthBlast.CanHitSelf");
		this.range = getConfig().getDouble("Abilities.Earth.EarthBlast.Range");
		this.damage = getConfig().getDouble("Abilities.Earth.EarthBlast.Damage");
		this.speed = getConfig().getDouble("Abilities.Earth.EarthBlast.Speed");
		this.pushFactor = getConfig().getDouble("Abilities.Earth.EarthBlast.Push");
		this.selectRange = getConfig().getDouble("Abilities.Earth.EarthBlast.SelectRange");
		this.time = System.currentTimeMillis();
		this.interval = (long) (1000.0 / this.speed);

		if (this.bPlayer.isAvatarState()) {
			this.cooldown = getConfig().getLong("Abilities.Avatar.AvatarState.Earth.EarthBlast.Cooldown");
			this.damage = getConfig().getDouble("Abilities.Avatar.AvatarState.Earth.EarthBlast.Damage");
		}

		if (this.prepare()) {
			this.start();
			this.time = System.currentTimeMillis();
		}
	}

	private void checkForCollision() {
		for (final EarthBlast blast : getAbilities(EarthBlast.class)) {
			if (blast.player.equals(this.player)) {
				continue;
			} else if (!blast.location.getWorld().equals(this.player.getWorld())) {
				continue;
			} else if (!blast.isProgressing) {
				continue;
			} else if (GeneralMethods.isRegionProtectedFromBuild(this, blast.location)) {
				continue;
			}

			final Location location = this.player.getEyeLocation();
			final Vector vector = location.getDirection();
			final Location mloc = blast.location;
			if (mloc.distanceSquared(location) <= this.range * this.range && GeneralMethods.getDistanceFromLine(vector, location, blast.location) < this.deflectRange && mloc.distanceSquared(location.clone().add(vector)) < mloc.distanceSquared(location.clone().add(vector.clone().multiply(-1)))) {
				blast.remove();
				this.remove();
				return;
			}
		}
	}

	private void focusBlock() {
		if (DensityShift.isPassiveSand(this.sourceBlock)) {
			DensityShift.revertSand(this.sourceBlock);
		}

		if (this.sourceBlock.getType() == Material.SAND) {
			this.sourceType = Material.SAND;
			this.sourceBlock.setType(Material.SANDSTONE);
		} else if (this.sourceBlock.getType() == Material.RED_SAND) {
			this.sourceType = Material.RED_SAND;
			this.sourceBlock.setType(Material.RED_SANDSTONE);
		} else if (this.sourceBlock.getType() == Material.STONE) {
			this.sourceBlock.setType(Material.COBBLESTONE);
			this.sourceType = Material.STONE;
		} else {
			this.sourceType = this.sourceBlock.getType();
			this.sourceBlock.setType(Material.STONE);
		}

		this.location = this.sourceBlock.getLocation();
	}

	private Location getTargetLocation() {
		final Entity target = GeneralMethods.getTargetedEntity(this.player, this.range, new ArrayList<Entity>());
		Location location;
		final Material[] trans = new Material[getTransparentMaterials().length + this.getEarthbendableBlocks().size()];
		int i = 0;
		for (int j = 0; j < getTransparentMaterials().length; j++) {
			trans[j] = getTransparentMaterials()[j];
			i++;
		}
		for (int j = 0; j < this.getEarthbendableBlocks().size(); j++) {
			try {
				trans[i] = Material.valueOf(this.getEarthbendableBlocks().get(j));
			} catch (final IllegalArgumentException e) {
				continue;
			}
			i++;
		}

		if (target == null) {
			location = GeneralMethods.getTargetedLocation(this.player, this.range, true, trans);
		} else {
			location = ((LivingEntity) target).getEyeLocation();
		}

		return location;
	}

	public boolean prepare() {
		final Block block = BlockSource.getEarthSourceBlock(this.player, this.range, ClickType.SHIFT_DOWN);
		if (block == null || !this.isEarthbendable(block)) {
			return false;
		} else if (TempBlock.isTempBlock(block) && !EarthAbility.isBendableEarthTempBlock(block)) {
			return false;
		}

		boolean selectedABlockInUse = false;
		for (final EarthBlast blast : getAbilities(this.player, EarthBlast.class)) {
			if (!blast.isProgressing) {
				blast.remove();
			} else if (blast.isProgressing && block.equals(blast.sourceBlock)) {
				selectedABlockInUse = true;
			}
		}

		if (selectedABlockInUse) {
			return false;
		}

		this.checkForCollision();

		if (block.getLocation().distanceSquared(this.player.getLocation()) > this.selectRange * this.selectRange) {
			return false;
		}

		this.sourceBlock = block;
		this.focusBlock();
		return true;
	}

	@Override
	public void progress() {
		if (!this.bPlayer.canBendIgnoreBindsCooldowns(this)) {
			this.remove();
			return;
		}

		if (System.currentTimeMillis() - this.time >= this.interval) {
			this.time = System.currentTimeMillis();

			if (this.isAtDestination) {
				this.remove();
				return;
			} else if (!this.isEarthbendable(this.sourceBlock) && this.sourceBlock.getType() != Material.COBBLESTONE) {
				this.remove();
				return;
			}

			if (!this.isProgressing && !this.isAtDestination) {
				if (this.sourceBlock == null || !this.bPlayer.getBoundAbilityName().equals(this.getName())) {
					this.remove();
					return;
				} else if (!this.player.getWorld().equals(this.sourceBlock.getWorld())) {
					this.remove();
					return;
				} else if (this.sourceBlock.getLocation().distanceSquared(this.player.getLocation()) > this.selectRange * this.selectRange) {
					this.remove();
					return;
				}
			}

			if (this.isAtDestination) {
				this.remove();
				return;
			} else {
				if (!this.isProgressing) {
					return;
				}

				if (this.sourceBlock.getY() == this.firstDestination.getBlockY()) {
					this.isSettingUp = false;
				}

				Vector direction;
				if (this.isSettingUp) {
					direction = GeneralMethods.getDirection(this.location, this.firstDestination).normalize();
				} else {
					direction = GeneralMethods.getDirection(this.location, this.destination).normalize();
				}

				this.location = this.location.clone().add(direction);
				Block block = this.location.getBlock();

				if (block.getLocation().equals(this.sourceBlock.getLocation())) {
					this.location = this.location.clone().add(direction);
					block = this.location.getBlock();
				}

				if (this.isTransparent(block) && !block.isLiquid()) {
					GeneralMethods.breakBlock(block);
				} else if (!this.isSettingUp) {
					this.remove();
					return;
				} else {
					this.location = this.location.clone().subtract(direction);
					direction = GeneralMethods.getDirection(this.location, this.destination).normalize();
					this.location = this.location.clone().add(direction);

					Block block2 = this.location.getBlock();
					if (block2.getLocation().equals(this.sourceBlock.getLocation())) {
						this.location = this.location.clone().add(direction);
						block2 = this.location.getBlock();
					}

					if (this.isTransparent(block) && !block.isLiquid()) {
						GeneralMethods.breakBlock(block);
					} else {
						this.remove();
						return;
					}
				}

				for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(this.location, this.collisionRadius)) {
					if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation())) {
						continue;
					}

					if (entity instanceof LivingEntity && (entity.getEntityId() != this.player.getEntityId() || this.canHitSelf)) {
						AirAbility.breakBreathbendingHold(entity);

						final Location location = this.player.getEyeLocation();
						final Vector vector = location.getDirection();
						GeneralMethods.setVelocity(this, entity, vector.normalize().multiply(this.pushFactor));
						double damage = this.damage;

						if (isMetal(this.sourceBlock) && this.bPlayer.canMetalbend()) {
							damage = getMetalAugment(damage);
						}

						DamageHandler.damageEntity(entity, damage, this);
						this.isProgressing = false;
					}
				}

				if (!this.isProgressing) {
					this.remove();
					return;
				}

				if (isEarthRevertOn()) {
					this.sourceBlock.setType(this.sourceType);

					moveEarthBlock(this.sourceBlock, block);

					if (block.getType() == Material.SAND) {
						block.setType(Material.SANDSTONE);
					}

					if (block.getType() == Material.GRAVEL) {
						block.setType(Material.STONE);
					}
				} else {
					block.setType(this.sourceType);
					this.sourceBlock.setType(Material.AIR);
				}

				this.sourceBlock = block;

				if (this.location.distanceSquared(this.destination) < 1) {
					if (this.sourceType == Material.SAND || this.sourceType == Material.GRAVEL) {
						this.isProgressing = false;
						if (this.sourceBlock.getType() == Material.RED_SANDSTONE) {
							this.sourceType = Material.SAND;
							this.sourceBlock.setType(this.sourceType);
						} else {
							this.sourceBlock.setType(this.sourceType);
						}
					}

					this.isAtDestination = true;
					this.isProgressing = false;
				}

				return;
			}
		}
	}

	private void redirect(final Player player, final Location targetlocation) {
		if (this.isProgressing) {
			if (this.location.distanceSquared(player.getLocation()) <= this.range * this.range) {
				this.isSettingUp = false;
				this.destination = targetlocation;
			}
		}
	}

	@Override
	public void remove() {
		super.remove();
		if (this.destination != null && this.sourceBlock != null) {
			this.sourceBlock.setType(Material.AIR);
		} else if (this.sourceBlock != null) {
			this.sourceBlock.setType(this.sourceType);
		}
	}

	public void throwEarth() {
		if (this.sourceBlock == null || !this.sourceBlock.getWorld().equals(this.player.getWorld())) {
			return;
		}

		if (getMovedEarth().containsKey(this.sourceBlock)) {
			if (!isEarthRevertOn()) {
				removeRevertIndex(this.sourceBlock);
			}
		}

		final Entity target = GeneralMethods.getTargetedEntity(this.player, this.range, new ArrayList<Entity>());
		if (target != null) {
			this.destination = target.getLocation();
		} else {
			this.destination = this.getTargetLocation();
		}

		if (this.sourceBlock == null) {
			return;
		}
		this.location = this.sourceBlock.getLocation();
		if (this.destination.distanceSquared(this.location) < 1) {
			return;
		}

		this.firstDestination = this.location.clone();
		if (this.destination.getY() - this.location.getY() > 2) {
			this.firstDestination.setY(this.destination.getY() - 1);
		} else if (this.location.getY() > player.getEyeLocation().getY() && this.location.getBlock().getRelative(BlockFace.UP).isPassable()) {
			this.firstDestination.subtract(0, 2, 0);
		} else if (this.location.getBlock().getRelative(BlockFace.UP).isPassable() && this.location.getBlock().getRelative(BlockFace.UP, 2).isPassable()) {
			this.firstDestination.add(0, 2, 0);
		} else {
			this.firstDestination.add(GeneralMethods.getDirection(this.location, this.destination).normalize().setY(0));
		}

		if (this.destination.distanceSquared(this.location) <= 1) {
			this.isProgressing = false;
			this.destination = null;
		} else {
			this.isProgressing = true;
			playEarthbendingSound(this.sourceBlock.getLocation());

			final Material currentType = this.sourceBlock.getType();
			this.sourceBlock.setType(this.sourceType);
			if (isEarthRevertOn()) {
				addTempAirBlock(this.sourceBlock);
			} else {
				this.sourceBlock.breakNaturally();
			}

			this.sourceBlock.setType(currentType);
		}
	}

	/**
	 * This method was used for the old collision detection system. Please see
	 * {@link Collision} for the new system.
	 */
	@Deprecated
	public static boolean annihilateBlasts(final Location location, final double radius, final Player source) {
		boolean broke = false;
		for (final EarthBlast blast : getAbilities(EarthBlast.class)) {
			if (blast.location.getWorld().equals(location.getWorld()) && !source.equals(blast.player)) {
				if (blast.location.distanceSquared(location) <= radius * radius) {
					blast.remove();
					broke = true;
				}
			}
		}

		return broke;
	}

	public static ArrayList<EarthBlast> getAroundPoint(final Location location, final double radius) {
		final ArrayList<EarthBlast> list = new ArrayList<EarthBlast>();
		for (final EarthBlast blast : getAbilities(EarthBlast.class)) {
			if (blast.location.getWorld().equals(location.getWorld())) {
				if (blast.location.distanceSquared(location) <= radius * radius) {
					list.add(blast);
				}
			}
		}

		return list;
	}

	public static EarthBlast getBlastFromSource(final Block block) {
		for (final EarthBlast blast : getAbilities(EarthBlast.class)) {
			if (blast.sourceBlock.equals(block)) {
				return blast;
			}
		}

		return null;
	}

	private static void redirectTargettedBlasts(final Player player, final ArrayList<EarthBlast> ignore) {
		for (final EarthBlast blast : getAbilities(EarthBlast.class)) {
			if (!blast.isProgressing || ignore.contains(blast)) {
				continue;
			} else if (!blast.location.getWorld().equals(player.getWorld())) {
				continue;
			} else if (GeneralMethods.isRegionProtectedFromBuild(blast, blast.location)) {
				continue;
			} else if (blast.player.equals(player)) {
				blast.redirect(player, blast.getTargetLocation());
			}

			final Location location = player.getEyeLocation();
			final Vector vector = location.getDirection();
			final Location mloc = blast.location;

			if (mloc.distanceSquared(location) <= blast.range * blast.range && GeneralMethods.getDistanceFromLine(vector, location, blast.location) < blast.deflectRange && mloc.distanceSquared(location.clone().add(vector)) < mloc.distanceSquared(location.clone().add(vector.clone().multiply(-1)))) {
				blast.redirect(player, blast.getTargetLocation());
			}
		}
	}

	public static void removeAroundPoint(final Location location, final double radius) {
		for (final EarthBlast blast : getAbilities(EarthBlast.class)) {
			if (blast.location.getWorld().equals(location.getWorld())) {
				if (blast.location.distanceSquared(location) <= radius * radius) {
					blast.remove();
				}
			}
		}
	}

	public static void throwEarth(final Player player) {
		final ArrayList<EarthBlast> ignore = new ArrayList<EarthBlast>();
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		EarthBlast earthBlast = null;

		if (bPlayer == null) {
			return;
		}

		for (final EarthBlast blast : getAbilities(player, EarthBlast.class)) {
			if (!blast.isProgressing && bPlayer.canBend(blast)) {
				blast.throwEarth();
				ignore.add(blast);
				earthBlast = blast;
			}
		}

		if (earthBlast != null) {
			bPlayer.addCooldown(earthBlast);
		}

		redirectTargettedBlasts(player, ignore);
	}

	@Override
	public String getName() {
		return "EarthBlast";
	}

	@Override
	public Location getLocation() {
		return this.location;
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
	public boolean isCollidable() {
		return this.isProgressing;
	}

	@Override
	public double getCollisionRadius() {
		return this.collisionRadius;
	}

	public boolean isProgressing() {
		return this.isProgressing;
	}

	public void setProgressing(final boolean isProgressing) {
		this.isProgressing = isProgressing;
	}

	public boolean isAtDestination() {
		return this.isAtDestination;
	}

	public void setAtDestination(final boolean isAtDestination) {
		this.isAtDestination = isAtDestination;
	}

	public boolean isSettingUp() {
		return this.isSettingUp;
	}

	public void setSettingUp(final boolean isSettingUp) {
		this.isSettingUp = isSettingUp;
	}

	public boolean isCanHitSelf() {
		return this.canHitSelf;
	}

	public void setCanHitSelf(final boolean canHitSelf) {
		this.canHitSelf = canHitSelf;
	}

	public long getTime() {
		return this.time;
	}

	public void setTime(final long time) {
		this.time = time;
	}

	public long getInterval() {
		return this.interval;
	}

	public void setInterval(final long interval) {
		this.interval = interval;
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

	public double getPushFactor() {
		return this.pushFactor;
	}

	public void setPushFactor(final double pushFactor) {
		this.pushFactor = pushFactor;
	}

	public double getSelectRange() {
		return this.selectRange;
	}

	public void setSelectRange(final double selectRange) {
		this.selectRange = selectRange;
	}

	public double getDeflectRange() {
		return this.deflectRange;
	}

	public void setDeflectRange(final double deflectRange) {
		this.deflectRange = deflectRange;
	}

	public void setCollisionRadius(final double collisionRadius) {
		this.collisionRadius = collisionRadius;
	}

	public Material getSourcetype() {
		return this.sourceType;
	}

	public void setSourcetype(final Material sourcetype) {
		this.sourceType = sourcetype;
	}

	public Location getDestination() {
		return this.destination;
	}

	public void setDestination(final Location destination) {
		this.destination = destination;
	}

	public Location getFirstDestination() {
		return this.firstDestination;
	}

	public void setFirstDestination(final Location firstDestination) {
		this.firstDestination = firstDestination;
	}

	public Block getSourceBlock() {
		return this.sourceBlock;
	}

	public void setSourceBlock(final Block sourceBlock) {
		this.sourceBlock = sourceBlock;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}
}

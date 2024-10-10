package com.projectkorra.projectkorra.waterbending.ice;

import java.util.Random;

import com.projectkorra.projectkorra.attribute.markers.DayNightFactor;
import com.projectkorra.projectkorra.region.RegionProtection;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.util.TempPotionEffect;
import com.projectkorra.projectkorra.waterbending.plant.PlantRegrowth;
import com.projectkorra.projectkorra.waterbending.util.WaterReturn;

public class IceSpikeBlast extends IceAbility {

	private boolean prepared;
	private boolean settingUp;
	private boolean progressing;
	private byte data;
	@Attribute("SlowPotency")
	private int slowPotency;
	@Attribute("Slow" + Attribute.DURATION) @DayNightFactor
	private int slowDuration;
	private long time;
	private long interval;
	@Attribute(Attribute.COOLDOWN) @DayNightFactor(invert = true)
	private long cooldown;
	@Attribute("Slow" + Attribute.COOLDOWN) @DayNightFactor(invert = true)
	private long slowCooldown;
	@Attribute(Attribute.RANGE) @DayNightFactor
	private double range;
	@Attribute(Attribute.DAMAGE) @DayNightFactor
	private double damage;
	private double collisionRadius;
	@Attribute("Deflect" + Attribute.RANGE) @DayNightFactor
	private double deflectRange;
	private Block sourceBlock;
	private Location location;
	private Location firstDestination;
	private Location destination;
	private TempBlock source;
	private Material sourceType;

	public IceSpikeBlast(final Player player) {
		super(player);

		if (this.bPlayer.isOnCooldown("IceSpikeBlast")) {
			return;
		}

		this.data = 0;
		this.interval = getConfig().getLong("Abilities.Water.IceSpike.Blast.Interval");
		this.slowCooldown = getConfig().getLong("Abilities.Water.IceSpike.Blast.SlowCooldown");
		this.collisionRadius = getConfig().getDouble("Abilities.Water.IceSpike.Blast.CollisionRadius");
		this.deflectRange = getConfig().getDouble("Abilities.Water.IceSpike.Blast.DeflectRange");
		this.range = getConfig().getDouble("Abilities.Water.IceSpike.Blast.Range");
		this.damage = getConfig().getDouble("Abilities.Water.IceSpike.Blast.Damage");
		this.cooldown = getConfig().getLong("Abilities.Water.IceSpike.Blast.Cooldown");
		this.slowPotency = getConfig().getInt("Abilities.Water.IceSpike.Blast.SlowPotency");
		this.slowDuration = getConfig().getInt("Abilities.Water.IceSpike.Blast.SlowDuration");

		if (!this.bPlayer.canBend(this) || !this.bPlayer.canIcebend()) {
			return;
		}

		block(player);
		this.sourceBlock = getWaterSourceBlock(player, this.range, this.bPlayer.canPlantbend());
		if (this.sourceBlock == null) {
			this.sourceBlock = getIceSourceBlock(player, this.range);
		}

		if (this.sourceBlock == null) {
			new IceSpikePillarField(player);
		} else if (RegionProtection.isRegionProtected(this, this.sourceBlock.getLocation())) {
			return;
		} else {
			this.prepare(this.sourceBlock);
		}
	}

	private void affect(final LivingEntity entity) {
		if (entity instanceof Player) {
			final BendingPlayer targetBPlayer = BendingPlayer.getBendingPlayer((Player) entity);
			if (targetBPlayer == null) {
				return;
			}
			if (targetBPlayer.canBeSlowed()) {
				final PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, this.slowDuration, this.slowPotency);
				new TempPotionEffect(entity, effect);
				targetBPlayer.slow(this.slowCooldown);
				DamageHandler.damageEntity(entity, this.damage, this);
			}
		} else {
			final PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, this.slowDuration, this.slowPotency);
			new TempPotionEffect(entity, effect);
			DamageHandler.damageEntity(entity, this.damage, this);
		}
		AirAbility.breakBreathbendingHold(entity);
	}

	private void prepare(final Block block) {
		for (final IceSpikeBlast iceSpike : getAbilities(this.player, IceSpikeBlast.class)) {
			if (iceSpike.prepared) {
				iceSpike.remove();
			}
		}

		this.sourceBlock = block;
		if (!isIce(block)) {
			this.sourceType = Material.ICE;
		} else {
			this.sourceType = block.getType();
		}
		this.location = this.sourceBlock.getLocation();
		this.prepared = true;
		this.start();
	}

	@Override
	public void progress() {
		if (!this.bPlayer.canBendIgnoreBindsCooldowns(this)) {
			this.remove();
			return;
		} else if (this.player.getEyeLocation().distanceSquared(this.location) >= this.range * this.range) {
			if (this.progressing) {
				this.remove();
				this.returnWater();
			} else {
				this.remove();
			}
			return;
		} else if (!this.bPlayer.getBoundAbilityName().equals(this.getName()) && this.prepared) {
			this.remove();
			return;
		} else if (this.prepared && !isWaterbendable(this.sourceBlock)) {
			this.remove();
			return;
		}
		
		if (System.currentTimeMillis() < this.time + this.interval) {
			return;
		}

		this.time = System.currentTimeMillis();

		if (this.progressing) {
			Vector direction;
			if (this.location.getBlockY() == this.firstDestination.getBlockY()) {
				this.settingUp = false;
			}

			if (this.location.distanceSquared(this.destination) <= 4) {
				this.remove();
				this.returnWater();
				return;
			}

			if (this.settingUp) {
				direction = GeneralMethods.getDirection(this.location, this.firstDestination).normalize();
			} else {
				direction = GeneralMethods.getDirection(this.location, this.destination).normalize();
			}

			this.location.add(direction);
			final Block block = this.location.getBlock();
			if (block.equals(this.sourceBlock)) {
				return;
			}

			if (isTransparent(this.player, block) && !block.isLiquid()) {
				GeneralMethods.breakBlock(block);
			} else if (!isWater(block)) {
				this.remove();
				this.returnWater();
				return;
			}

			if (GeneralMethods.isRegionProtectedFromBuild(this, this.location)) {
				this.remove();
				this.returnWater();
				return;
			}

			for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(this.location, this.collisionRadius + 0.5)) {
				if (entity.getEntityId() != this.player.getEntityId() && entity instanceof LivingEntity) {
					this.affect((LivingEntity) entity);
					this.progressing = false;
					this.returnWater();
				}
			}

			if ((new Random()).nextInt(4) == 0) {
				playIcebendingSound(this.location);
			}

			if (!this.progressing) {
				this.remove();
				return;
			}

			this.sourceBlock = block;
			this.source = new TempBlock(this.sourceBlock, this.sourceType.createBlockData(), this);
			this.source.setRevertTime(130);
		} else if (this.prepared) {
			if (this.sourceBlock != null) {
				playFocusWaterEffect(this.sourceBlock);
			}
		}
	}

	private void redirect(final Location destination, final Player player) {
		this.destination = destination;
		this.setPlayer(player);
	}

	@Override
	public void remove() {
		super.remove();
		if (this.source != null) {
			this.source.revertBlock();
		}
		this.progressing = false;
	}

	private void returnWater() {
		new WaterReturn(this.player, this.location.getBlock());
	}

	private void throwIce() {
		if (!this.prepared) {
			return;
		}

		final LivingEntity target = (LivingEntity) GeneralMethods.getTargetedEntity(this.player, this.range);
		if (target == null) {
			this.destination = GeneralMethods.getTargetedLocation(this.player, this.range, true, getTransparentMaterials());
		} else {
			this.destination = target.getLocation();
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
		} else {
			this.firstDestination.add(0, 2, 0);
		}

		this.destination = GeneralMethods.getPointOnLine(this.firstDestination, this.destination, this.range);
		this.progressing = true;
		this.settingUp = true;
		this.prepared = false;

		if (isPlant(this.sourceBlock) || isSnow(this.sourceBlock)) {
			new PlantRegrowth(this.player, this.sourceBlock);
			this.sourceBlock.setType(Material.AIR);
		} else if (isWater(this.sourceBlock)) {
			if (!GeneralMethods.isAdjacentToThreeOrMoreSources(this.sourceBlock)) {
				this.sourceBlock.setType(Material.AIR);
			}
		} else if (TempBlock.isTempBlock(this.sourceBlock)) {
			final TempBlock tb = TempBlock.get(this.sourceBlock);
			if (isBendableWaterTempBlock(tb)) {
				tb.revertBlock();
			}
		} else if (isCauldron(this.sourceBlock) || isTransformableBlock(this.sourceBlock)) {
			updateSourceBlock(this.sourceBlock);
		}
	}

	public static void activate(final Player player) {
		redirect(player);
		boolean activate = false;
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer == null) {
			return;
		}

		if (bPlayer.isOnCooldown("IceSpikeBlast")) {
			return;
		}

		for (final IceSpikeBlast ice : getAbilities(player, IceSpikeBlast.class)) {
			if (ice.prepared) {
				ice.throwIce();
				bPlayer.addCooldown("IceSpikeBlast", ice.getCooldown());
				activate = true;
			}
		}

		if (!activate && !getPlayers(IceSpikeBlast.class).contains(player)) {
			final IceSpikePillar spike = new IceSpikePillar(player);
			if (!spike.isStarted()) {
				waterBottle(player);
			}
		}
	}

	private static void block(final Player player) {
		for (final IceSpikeBlast iceSpike : getAbilities(IceSpikeBlast.class)) {
			if (iceSpike.player.equals(player)) {
				continue;
			} else if (!iceSpike.location.getWorld().equals(player.getWorld())) {
				continue;
			} else if (!iceSpike.progressing) {
				continue;
			}
			if (GeneralMethods.isRegionProtectedFromBuild(iceSpike, iceSpike.location)) {
				continue;
			}

			final Location location = player.getEyeLocation();
			final Vector vector = location.getDirection();
			final Location mloc = iceSpike.location;
			if (mloc.distanceSquared(location) <= iceSpike.range * iceSpike.range && GeneralMethods.getDistanceFromLine(vector, location, iceSpike.location) < iceSpike.deflectRange && mloc.distanceSquared(location.clone().add(vector)) < mloc.distanceSquared(location.clone().add(vector.clone().multiply(-1)))) {
				iceSpike.remove();
			}
		}
	}

	private static void redirect(final Player player) {
		for (final IceSpikeBlast iceSpike : getAbilities(IceSpikeBlast.class)) {
			if (!iceSpike.progressing) {
				continue;
			} else if (!iceSpike.location.getWorld().equals(player.getWorld())) {
				continue;
			}

			if (iceSpike.player.equals(player)) {
				Location location;
				final Entity target = GeneralMethods.getTargetedEntity(player, iceSpike.range);
				if (target == null) {
					location = GeneralMethods.getTargetedLocation(player, iceSpike.range);
				} else {
					location = ((LivingEntity) target).getEyeLocation();
				}
				location = GeneralMethods.getPointOnLine(iceSpike.location, location, iceSpike.range * 2);
				iceSpike.redirect(location, player);
			}

			final Location location = player.getEyeLocation();
			final Vector vector = location.getDirection();
			final Location mloc = iceSpike.location;

			if (GeneralMethods.isRegionProtectedFromBuild(iceSpike, mloc)) {
				continue;
			} else if (mloc.distanceSquared(location) <= iceSpike.range * iceSpike.range && GeneralMethods.getDistanceFromLine(vector, location, iceSpike.location) < iceSpike.deflectRange && mloc.distanceSquared(location.clone().add(vector)) < mloc.distanceSquared(location.clone().add(vector.clone().multiply(-1)))) {
				Location loc;
				final Entity target = GeneralMethods.getTargetedEntity(player, iceSpike.range);
				if (target == null) {
					loc = GeneralMethods.getTargetedLocation(player, iceSpike.range, true);
				} else {
					loc = ((LivingEntity) target).getEyeLocation();
				}
				loc = GeneralMethods.getPointOnLine(iceSpike.location, loc, iceSpike.range * 2);
				iceSpike.redirect(loc, player);
			}
		}
	}

	private static void waterBottle(final Player player) {
		final long range = getConfig().getLong("Abilities.Water.IceSpike.Projectile.Range");

		if (WaterReturn.hasWaterBottle(player)) {
			final Location eyeLoc = player.getEyeLocation();
			final Block block = eyeLoc.add(eyeLoc.getDirection().normalize()).getBlock();

			if (isTransparent(player, block) && isTransparent(player, eyeLoc.getBlock())) {
				final LivingEntity target = (LivingEntity) GeneralMethods.getTargetedEntity(player, range);
				Location destination;

				if (target == null) {
					destination = GeneralMethods.getTargetedLocation(player, range, getTransparentMaterials());
				} else {
					destination = GeneralMethods.getPointOnLine(player.getEyeLocation(), target.getEyeLocation(), range);
				}

				if (destination.distanceSquared(block.getLocation()) < 1) {
					return;
				}

				final BlockState state = block.getState();
				block.setType(Material.WATER);
				block.setBlockData(GeneralMethods.getWaterData(0));
				final IceSpikeBlast iceSpike = new IceSpikeBlast(player);
				iceSpike.throwIce();
				iceSpike.sourceBlock = null;

				if (iceSpike.progressing) {
					WaterReturn.emptyWaterBottle(player);
				}
				block.setType(state.getType());
				block.setBlockData(state.getBlockData());

			}
		}
	}

	@Override
	public String getName() {
		return "IceSpike";
	}

	@Override
	public Location getLocation() {
		if (this.location != null) {
			return this.location;
		} else if (this.sourceBlock != null) {
			return this.sourceBlock.getLocation();
		}
		return this.player != null ? this.player.getLocation() : null;
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
		return this.progressing;
	}

	@Override
	public double getCollisionRadius() {
		return this.collisionRadius;
	}

	public boolean isPrepared() {
		return this.prepared;
	}

	public void setPrepared(final boolean prepared) {
		this.prepared = prepared;
	}

	public boolean isSettingUp() {
		return this.settingUp;
	}

	public void setSettingUp(final boolean settingUp) {
		this.settingUp = settingUp;
	}

	public boolean isProgressing() {
		return this.progressing;
	}

	public void setProgressing(final boolean progressing) {
		this.progressing = progressing;
	}

	public byte getData() {
		return this.data;
	}

	public void setData(final byte data) {
		this.data = data;
	}

	public int getSlowPotency() {
		return this.slowPotency;
	}

	public void setSlowPotency(final int slowPotency) {
		this.slowPotency = slowPotency;
	}

	public int getSlowDuration() {
		return this.slowDuration;
	}

	public void setSlowDuration(final int slowDuration) {
		this.slowDuration = slowDuration;
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

	public long getSlowCooldown() {
		return this.slowCooldown;
	}

	public void setSlowCooldown(final long slowCooldown) {
		this.slowCooldown = slowCooldown;
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

	public void setCollisionRadius(final double collisionRadius) {
		this.collisionRadius = collisionRadius;
	}

	public double getDeflectRange() {
		return this.deflectRange;
	}

	public void setDeflectRange(final double deflectRange) {
		this.deflectRange = deflectRange;
	}

	public Block getSourceBlock() {
		return this.sourceBlock;
	}

	public void setSourceBlock(final Block sourceBlock) {
		this.sourceBlock = sourceBlock;
	}

	public Location getFirstDestination() {
		return this.firstDestination;
	}

	public void setFirstDestination(final Location firstDestination) {
		this.firstDestination = firstDestination;
	}

	public Location getDestination() {
		return this.destination;
	}

	public void setDestination(final Location destination) {
		this.destination = destination;
	}

	public TempBlock getSource() {
		return this.source;
	}

	public void setSource(final TempBlock source) {
		this.source = source;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

}

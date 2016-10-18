package com.projectkorra.projectkorra.waterbending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.util.TempPotionEffect;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Random;

public class IceSpikeBlast extends IceAbility {

	private boolean prepared;
	private boolean settingUp;
	private boolean progressing;
	private byte data;
	private int slowPower;
	private int slowDuration;
	private long time;
	private long interval;
	private long cooldown;
	private long slowCooldown;
	private double range;
	private double damage;
	private double collisionRadius;
	private double deflectRange;
	private Block sourceBlock;
	private Location location;
	private Location firstDestination;
	private Location destination;
	private TempBlock source;

	public IceSpikeBlast(Player player) {
		super(player);
		
		if (bPlayer.isOnCooldown("IceSpikeBlast")) {
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
		this.slowPower = getConfig().getInt("Abilities.Water.IceSpike.Blast.SlowPower");
		this.slowDuration = getConfig().getInt("Abilities.Water.IceSpike.Blast.SlowDuration");
		
		if (!bPlayer.canBend(this) || !bPlayer.canIcebend()) {
			return;
		}

		block(player);	
		this.range = getNightFactor(range);
		this.damage = getNightFactor(damage);
		this.slowPower = (int) getNightFactor(slowPower);
		sourceBlock = getWaterSourceBlock(player, range, bPlayer.canPlantbend());
		if (sourceBlock == null) {
			sourceBlock = getIceSourceBlock(player, range);
		}

		if (sourceBlock == null) {
			new IceSpikePillarField(player);
		} else if (GeneralMethods.isRegionProtectedFromBuild(this, sourceBlock.getLocation())) {
			return;
		} else {
			prepare(sourceBlock);
		}
	}

	private void affect(LivingEntity entity) {
		if (entity instanceof Player) {
			BendingPlayer targetBPlayer = BendingPlayer.getBendingPlayer((Player) entity);
			if (targetBPlayer == null) {
				return;
			}
			if (targetBPlayer.canBeSlowed()) {
				PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, slowDuration, slowPower);
				new TempPotionEffect(entity, effect);
				targetBPlayer.slow(slowCooldown);
				DamageHandler.damageEntity(entity, damage, this);
			}
		} else {
			PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, slowDuration, slowPower);
			new TempPotionEffect(entity, effect);
			DamageHandler.damageEntity(entity, damage, this);
		}
		AirAbility.breakBreathbendingHold(entity);
	}

	private void prepare(Block block) {
		for (IceSpikeBlast iceSpike : getAbilities(player, IceSpikeBlast.class)) {
			if (iceSpike.prepared) {
				iceSpike.remove();
			}
		}
		
		sourceBlock = block;
		location = sourceBlock.getLocation();
		prepared = true;
		start();
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			remove();
			return;
		} else if (player.getEyeLocation().distanceSquared(location) >= range * range) {
			if (progressing) {
				remove();
				returnWater();
			} else {
				remove();
			}
			return;
		} else if (!bPlayer.getBoundAbilityName().equals(getName()) && prepared) {
			remove();
			return;
		}

		if (System.currentTimeMillis() < time + interval) {
			return;
		}

		time = System.currentTimeMillis();

		if (progressing) {
			Vector direction;
			if (location.getBlockY() == firstDestination.getBlockY()) {
				settingUp = false;
			}

			if (location.distanceSquared(destination) <= 4) {
				remove();
				returnWater();
				return;
			}

			if (settingUp) {
				direction = GeneralMethods.getDirection(location, firstDestination).normalize();
			} else {
				direction = GeneralMethods.getDirection(location, destination).normalize();
			}

			location.add(direction);
			Block block = location.getBlock();
			if (block.equals(sourceBlock)) {
				return;
			}

			if (source != null) {
				source.revertBlock();
			}
			source = null;

			if (isTransparent(player, block) && !block.isLiquid()) {
				GeneralMethods.breakBlock(block);
			} else if (!isWater(block)) {
				remove();
				returnWater();
				return;
			}

			if (GeneralMethods.isRegionProtectedFromBuild(this, location)) {
				remove();
				returnWater();
				return;
			}

			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, collisionRadius)) {
				if (entity.getEntityId() != player.getEntityId() && entity instanceof LivingEntity) {
					affect((LivingEntity) entity);
					progressing = false;
					returnWater();
				}
			}

			if ((new Random()).nextInt(4) == 0) {
				playIcebendingSound(location);
			}

			if (!progressing) {
				remove();
				return;
			}

			sourceBlock = block;
			source = new TempBlock(sourceBlock, Material.ICE, data);
		} else if (prepared) {
			if (sourceBlock != null)
				playFocusWaterEffect(sourceBlock);
		}
	}

	private void redirect(Location destination, Player player) {
		this.destination = destination;
		this.player = player;
	}

	@Override
	public void remove() {
		super.remove();
		if (progressing) {
			if (source != null) {
				source.revertBlock();
			}
			progressing = false;
		}
	}

	private void returnWater() {
		new WaterReturn(player, location.getBlock());
	}

	private void throwIce() {
		if (!prepared) {
			return;
		}
		
		LivingEntity target = (LivingEntity) GeneralMethods.getTargetedEntity(player, range);
		if (target == null) {
			destination = GeneralMethods.getTargetedLocation(player, range, getTransparentMaterial());
		} else {
			destination = target.getEyeLocation();
		}

		if (sourceBlock == null) {
			return;
		}
		location = sourceBlock.getLocation();
		if (destination.distanceSquared(location) < 1) {
			return;
		}
		
		firstDestination = location.clone();
		if (destination.getY() - location.getY() > 2) {
			firstDestination.setY(destination.getY() - 1);
		} else {
			firstDestination.add(0, 2, 0);
		}
		
		destination = GeneralMethods.getPointOnLine(firstDestination, destination, range);
		progressing = true;
		settingUp = true;
		prepared = false;

		if (isPlant(sourceBlock) || isSnow(sourceBlock)) {
			new PlantRegrowth(player, sourceBlock);
			sourceBlock.setType(Material.AIR);
		}


	}

	public static void activate(Player player) {
		redirect(player);
		boolean activate = false;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		
		if (bPlayer == null) {
			return;
		}
		
		if (bPlayer.isOnCooldown("IceSpikeBlast")) {
			return;
		}
		
		for (IceSpikeBlast ice : getAbilities(player, IceSpikeBlast.class)) {
			if (ice.prepared) {
				ice.throwIce();
				bPlayer.addCooldown("IceSpikeBlast", ice.getCooldown());
				activate = true;
			}
		}
		
		if (!activate && !getPlayers(IceSpikeBlast.class).contains(player)) {
			IceSpikePillar spike = new IceSpikePillar(player);
			if (!spike.isStarted()) {
				waterBottle(player);
			}
		}
	}

	private static void block(Player player) {
		for (IceSpikeBlast iceSpike : getAbilities(IceSpikeBlast.class)) {
			if (iceSpike.player.equals(player)) {
				continue;
			} else if (!iceSpike.location.getWorld().equals(player.getWorld())) {
				continue;
			} else if (!iceSpike.progressing) {
				continue;
			} if (GeneralMethods.isRegionProtectedFromBuild(iceSpike, iceSpike.location)) {
				continue;
			}

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = iceSpike.location;
			if (mloc.distanceSquared(location) <= iceSpike.range * iceSpike.range 
					&& GeneralMethods.getDistanceFromLine(vector, location, iceSpike.location) < iceSpike.deflectRange 
					&& mloc.distanceSquared(location.clone().add(vector)) < mloc.distanceSquared(location.clone().add(vector.clone().multiply(-1)))) {
				iceSpike.remove();
			}
		}
	}

	private static void redirect(Player player) {
		for (IceSpikeBlast iceSpike : getAbilities(IceSpikeBlast.class)) {
			if (!iceSpike.progressing) {
				continue;
			} else if (!iceSpike.location.getWorld().equals(player.getWorld())) {
				continue;
			}

			if (iceSpike.player.equals(player)) {
				Location location;
				Entity target = GeneralMethods.getTargetedEntity(player, iceSpike.range);
				if (target == null) {
					location = GeneralMethods.getTargetedLocation(player, iceSpike.range);
				} else {
					location = ((LivingEntity) target).getEyeLocation();
				}
				location = GeneralMethods.getPointOnLine(iceSpike.location, location, iceSpike.range * 2);
				iceSpike.redirect(location, player);
			}

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = iceSpike.location;
			
			if (GeneralMethods.isRegionProtectedFromBuild(iceSpike, mloc)) {
				continue;
			} else if (mloc.distanceSquared(location) <= iceSpike.range * iceSpike.range 
					&& GeneralMethods.getDistanceFromLine(vector, location, iceSpike.location) < iceSpike.deflectRange 
					&& mloc.distanceSquared(location.clone().add(vector)) < mloc.distanceSquared(location.clone().add(vector.clone().multiply(-1)))) {
				Location loc;
				Entity target = GeneralMethods.getTargetedEntity(player, iceSpike.range);
				if (target == null) {
					loc = GeneralMethods.getTargetedLocation(player, iceSpike.range);
				} else {
					loc = ((LivingEntity) target).getEyeLocation();
				}
				loc = GeneralMethods.getPointOnLine(iceSpike.location, loc, iceSpike.range * 2);
				iceSpike.redirect(loc, player);
			}

		}
	}

	@SuppressWarnings("deprecation")
	private static void waterBottle(Player player) {
		long range = getConfig().getLong("Abilities.Water.IceSpike.Projectile.Range");
		
		if (WaterReturn.hasWaterBottle(player)) {
			Location eyeLoc = player.getEyeLocation();
			Block block = eyeLoc.add(eyeLoc.getDirection().normalize()).getBlock();
			
			if (isTransparent(player, block) && isTransparent(player, eyeLoc.getBlock())) {
				LivingEntity target = (LivingEntity) GeneralMethods.getTargetedEntity(player, range);
				Location destination;
			
				if (target == null) {
					destination = GeneralMethods.getTargetedLocation(player, range, getTransparentMaterial());
				} else {
					destination = GeneralMethods.getPointOnLine(player.getEyeLocation(), target.getEyeLocation(), range);
				}

				if (destination.distanceSquared(block.getLocation()) < 1) {
					return;
				}

				MaterialData data = block.getState().getData();
				block.setType(Material.WATER);
				block.setData((byte)0);
				IceSpikeBlast iceSpike = new IceSpikeBlast(player);
				iceSpike.throwIce();
				iceSpike.sourceBlock = null;

				if (iceSpike.progressing) {
					WaterReturn.emptyWaterBottle(player);
				} 
				block.setType(data.getItemType());
				block.setData(data.getData());
				
			}
		}
	}

	@Override
	public String getName() {
		return "IceSpike";
	}

	@Override
	public Location getLocation() {
		if (location != null) {
			return location;
		} else if (sourceBlock != null) {
			return sourceBlock.getLocation();
		}
		return player != null ? player.getLocation() : null;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}
	
	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public boolean isPrepared() {
		return prepared;
	}

	public void setPrepared(boolean prepared) {
		this.prepared = prepared;
	}

	public boolean isSettingUp() {
		return settingUp;
	}

	public void setSettingUp(boolean settingUp) {
		this.settingUp = settingUp;
	}

	public boolean isProgressing() {
		return progressing;
	}

	public void setProgressing(boolean progressing) {
		this.progressing = progressing;
	}

	public byte getData() {
		return data;
	}

	public void setData(byte data) {
		this.data = data;
	}

	public int getSlowPower() {
		return slowPower;
	}

	public void setSlowPower(int slowPower) {
		this.slowPower = slowPower;
	}

	public int getSlowDuration() {
		return slowDuration;
	}

	public void setSlowDuration(int slowDuration) {
		this.slowDuration = slowDuration;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public long getSlowCooldown() {
		return slowCooldown;
	}

	public void setSlowCooldown(long slowCooldown) {
		this.slowCooldown = slowCooldown;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public double getCollisionRadius() {
		return collisionRadius;
	}

	public void setCollisionRadius(double collisionRadius) {
		this.collisionRadius = collisionRadius;
	}

	public double getDeflectRange() {
		return deflectRange;
	}

	public void setDeflectRange(double deflectRange) {
		this.deflectRange = deflectRange;
	}

	public Block getSourceBlock() {
		return sourceBlock;
	}

	public void setSourceBlock(Block sourceBlock) {
		this.sourceBlock = sourceBlock;
	}

	public Location getFirstDestination() {
		return firstDestination;
	}

	public void setFirstDestination(Location firstDestination) {
		this.firstDestination = firstDestination;
	}

	public Location getDestination() {
		return destination;
	}

	public void setDestination(Location destination) {
		this.destination = destination;
	}

	public TempBlock getSource() {
		return source;
	}

	public void setSource(TempBlock source) {
		this.source = source;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	
}

package com.projectkorra.projectkorra.waterbending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.util.TempPotionEffect;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Random;

public class IceBlast extends IceAbility {
	
	private boolean prepared;
	private boolean settingUp;
	private boolean progressing;
	private byte data;
	private long time;
	private long cooldown;
	private long interval;
	private double range;
	private double damage;
	private double collisionRadius;
	private double deflectRange;
	private Block sourceBlock;
	private Location location;
	private Location firstDestination;
	private Location destination;
	public TempBlock source;
	
	public IceBlast(Player player) {
		super(player);
		
		this.data = 0;
		this.interval = getConfig().getLong("Abilities.Water.IceBlast.Interval");
		this.collisionRadius = getConfig().getDouble("Abilities.Water.IceBlast.CollisionRadius");
		this.deflectRange = getConfig().getDouble("Abilities.Water.IceBlast.DeflectRange");
		this.range = getConfig().getDouble("Abilities.Water.IceBlast.Range");
		this.damage = getConfig().getInt("Abilities.Water.IceBlast.Damage");
		this.cooldown = getConfig().getInt("Abilities.Water.IceBlast.Cooldown");
		
		this.damage = getNightFactor(damage, player.getWorld());
		
		if (!bPlayer.canBend(this) || !bPlayer.canIcebend()) {
			return;
		}

		block(player);
		range = getNightFactor(range, player.getWorld());
		Block sourceBlock = BlockSource.getWaterSourceBlock(player, range, ClickType.SHIFT_DOWN, false, true, false, false, false);

		if (sourceBlock == null) {
			return;
		} else if (TempBlock.isTempBlock(sourceBlock) || GeneralMethods.isRegionProtectedFromBuild(this, sourceBlock.getLocation())) {
			return;
		} else {
			prepare(sourceBlock);
		}
	}

	private void prepare(Block block) {
		for (IceBlast iceBlast : getAbilities(player, IceBlast.class)) {
			if (iceBlast.prepared) {
				iceBlast.remove();
			}
		}

		sourceBlock = block;
		location = sourceBlock.getLocation();
		prepared = true;
		
		if (getAbilities(player, IceBlast.class).isEmpty()) {
			start();
		}
	}

	private static void block(Player player) {
		for (IceBlast iceBlast : getAbilities(IceBlast.class)) {
			if (!iceBlast.location.getWorld().equals(player.getWorld())) {
				continue;
			} else if (!iceBlast.progressing) {
				continue;
			} else if (iceBlast.getPlayer().equals(player)) {
				continue;
			} else if (GeneralMethods.isRegionProtectedFromBuild(iceBlast, iceBlast.location)) {
				continue;
			}

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = iceBlast.location;
			
			if (mloc.distanceSquared(location) <= iceBlast.range * iceBlast.range
					&& GeneralMethods.getDistanceFromLine(vector, location, iceBlast.location) < iceBlast.deflectRange 
					&& mloc.distanceSquared(location.clone().add(vector)) < mloc.distanceSquared(location.clone().add(vector.clone().multiply(-1)))) {
				iceBlast.remove();
			}
		}
	}

	public static void activate(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer != null && bPlayer.isOnCooldown("IceBlast")) {
			return;
		}

		for (IceBlast ice : getAbilities(IceBlast.class)) {
			if (ice.prepared) {
				ice.throwIce();
			}
		}
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
		
		if (player.isOnline()) {
			if (bPlayer != null) {
				bPlayer.addCooldown(this);
			}
		}
	}

	private void returnWater() {
		new WaterReturn(player, sourceBlock);
	}

	private void affect(LivingEntity entity) {
		if (entity instanceof Player) {
			if (bPlayer.canBeSlowed()) {
				PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 70, 2);
				new TempPotionEffect(entity, effect);
				bPlayer.slow(10);
				DamageHandler.damageEntity(entity, damage, this);
			}
		} else {
			PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 70, 2);
			new TempPotionEffect(entity, effect);
			DamageHandler.damageEntity(entity, damage, this);
		}
		AirAbility.breakBreathbendingHold(entity);

		for (int x = 0; x < 30; x++) {
			ParticleEffect.ITEM_CRACK.display(new ParticleEffect.ItemData(Material.ICE, (byte) 0), new Vector(((Math.random() - 0.5) * .5), ((Math.random() - 0.5) * .5), ((Math.random() - 0.5) * .5)), .3f, location, 257.0D);
		}
	}

	private void throwIce() {
		if (!prepared) {
			return;
		}
		
		LivingEntity target = (LivingEntity) GeneralMethods.getTargetedEntity(player, range, new ArrayList<Entity>());
		if (target == null) {
			destination = GeneralMethods.getTargetedLocation(player, range, getTransparentMaterial());
		} else {
			destination = target.getEyeLocation();
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

		new TempBlock(sourceBlock, Material.AIR, (byte) 0);
		source = new TempBlock(sourceBlock, Material.PACKED_ICE, data);
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBinds(this)) {
			remove();
			return;
		}

		if (player.getEyeLocation().distanceSquared(location) >= range * range) {
			if (progressing) {
				breakParticles(20);
				remove();
				returnWater();
			} else {
				breakParticles(20);
				remove();
			}
			return;
		}

		if (!bPlayer.getBoundAbilityName().equalsIgnoreCase(getName()) && prepared) {
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

			source.revertBlock();
			source = null;

			if (isTransparent(player, block) && !block.isLiquid()) {
				GeneralMethods.breakBlock(block);
			} else if (!isWater(block)) {
				breakParticles(20);
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

			if (!progressing) {
				remove();
				return;
			}

			sourceBlock = block;
			source = new TempBlock(sourceBlock, Material.PACKED_ICE, data);

			for (int x = 0; x < 10; x++) {
				ParticleEffect.ITEM_CRACK.display(new ParticleEffect.ItemData(Material.ICE, (byte) 0), new Vector(((Math.random() - 0.5) * .5), ((Math.random() - 0.5) * .5), ((Math.random() - 0.5) * .5)), .5f, location, 257.0D);
				ParticleEffect.SNOW_SHOVEL.display(location, (float) (Math.random() - 0.5), (float) (Math.random() - 0.5), (float) (Math.random() - 0.5), 0, 5);
			}
			if ((new Random()).nextInt(4) == 0) {
				playIcebendingSound(location);
			}
			location = location.add(direction.clone());
		} else if (prepared) {
			playFocusWaterEffect(sourceBlock);
		}
	}

	public void breakParticles(int amount) {
		for (int x = 0; x < amount; x++) {
			ParticleEffect.ITEM_CRACK.display(new ParticleEffect.ItemData(Material.ICE, (byte) 0), new Vector(((Math.random() - 0.5) * .5), ((Math.random() - 0.5) * .5), ((Math.random() - 0.5) * .5)), 2f, location, 257.0D);
			ParticleEffect.SNOW_SHOVEL.display(location, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0, 2);
		}
		location.getWorld().playSound(location, Sound.GLASS, 5, 1.3f);
	}

	@Override
	public String getName() {
		return "IceBlast";
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

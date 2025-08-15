package com.projectkorra.projectkorra.waterbending.ice;

import java.util.ArrayList;
import java.util.Random;

import com.projectkorra.projectkorra.attribute.markers.DayNightFactor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.util.TempPotionEffect;
import com.projectkorra.projectkorra.waterbending.util.WaterReturn;

public class IceBlast extends IceAbility {

	private boolean prepared;
	private boolean settingUp;
	private boolean progressing;
	private byte data;
	private long time;
	@Attribute(Attribute.COOLDOWN) @DayNightFactor(invert = true)
	private long cooldown;
	@Attribute("Slow" + Attribute.COOLDOWN) @DayNightFactor(invert = true)
	private long slowCooldown;
	private long interval;
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
	private boolean allowSnow;
	public TempBlock source;

	public IceBlast(final Player player) {
		super(player);

		this.data = 0;
		this.interval = getConfig().getLong("Abilities.Water.IceBlast.Interval");
		this.collisionRadius = getConfig().getDouble("Abilities.Water.IceBlast.CollisionRadius");
		this.deflectRange = getConfig().getDouble("Abilities.Water.IceBlast.DeflectRange");
		this.range = getConfig().getDouble("Abilities.Water.IceBlast.Range");
		this.damage = getConfig().getInt("Abilities.Water.IceBlast.Damage");
		this.cooldown = getConfig().getInt("Abilities.Water.IceBlast.Cooldown");
		this.slowCooldown = getConfig().getLong("Abilities.Water.IceBlast.SlowCooldown");
		this.allowSnow = getConfig().getBoolean("Abilities.Water.IceBlast.AllowSnow");

		if (!this.bPlayer.canBend(this) || !this.bPlayer.canIcebend()) {
			return;
		}

		block(player);
		final Block sourceBlock = BlockSource.getWaterSourceBlock(player, this.range, ClickType.SHIFT_DOWN, false, true, false, this.allowSnow, false);
		final IceBlast oldAbil = getAbility(player, IceBlast.class);
		if (oldAbil != null) {
			oldAbil.setSourceBlock(sourceBlock == null ? oldAbil.getSourceBlock() : sourceBlock);
			return;
		}

		if (sourceBlock == null) {
			return;
		} else if (GeneralMethods.isRegionProtectedFromBuild(this, sourceBlock.getLocation())) {
			return;
		} else {
			this.prepare(sourceBlock);
		}
	}

	private void prepare(final Block block) {
		for (final IceBlast iceBlast : getAbilities(this.player, IceBlast.class)) {
			if (iceBlast.prepared) {
				iceBlast.remove();
			}
		}

		this.sourceBlock = block;
		this.location = this.sourceBlock.getLocation();
		this.prepared = true;

		if (getAbilities(this.player, IceBlast.class).isEmpty()) {
			this.start();
		}
	}

	private static void block(final Player player) {
		for (final IceBlast iceBlast : getAbilities(IceBlast.class)) {
			if (!iceBlast.location.getWorld().equals(player.getWorld())) {
				continue;
			} else if (!iceBlast.progressing) {
				continue;
			} else if (iceBlast.getPlayer().equals(player)) {
				continue;
			} else if (GeneralMethods.isRegionProtectedFromBuild(iceBlast, iceBlast.location)) {
				continue;
			}

			final Location location = player.getEyeLocation();
			final Vector vector = location.getDirection();
			final Location mloc = iceBlast.location;

			if (mloc.distanceSquared(location) <= iceBlast.range * iceBlast.range && GeneralMethods.getDistanceFromLine(vector, location, iceBlast.location) < iceBlast.deflectRange && mloc.distanceSquared(location.clone().add(vector)) < mloc.distanceSquared(location.clone().add(vector.clone().multiply(-1)))) {
				iceBlast.remove();
			}
		}
	}

	public static void activate(final Player player) {
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer != null && bPlayer.isOnCooldown("IceBlast")) {
			return;
		}

		for (final IceBlast ice : getAbilities(IceBlast.class)) {
			if (ice.prepared) {
				ice.throwIce();
			}
		}
	}

	@Override
	public void remove() {
		super.remove();
		if (this.progressing) {
			if (this.source != null) {
				this.source.revertBlock();
			}
			this.progressing = false;
		}

		if (this.player.isOnline()) {
			if (this.bPlayer != null) {
				this.bPlayer.addCooldown(this);
			}
		}
	}

	private void returnWater() {
		new WaterReturn(this.player, this.sourceBlock);
	}

	private void affect(final LivingEntity entity) {
		DamageHandler.damageEntity(entity, this.damage, this);
		if (entity instanceof Player) {
			if (this.bPlayer.canBeSlowed()) {
				final PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 70, 2);
				new TempPotionEffect(entity, effect);
				this.bPlayer.slow(this.slowCooldown);
			}
		} else {
			final PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 70, 2);
			new TempPotionEffect(entity, effect);
		}
		AirAbility.breakBreathbendingHold(entity);

		for (int x = 0; x < 30; x++) {
			ParticleEffect.ITEM_CRACK.display(this.location, 5, Math.random() / 4, Math.random() / 4, Math.random() / 4, new ItemStack(Material.ICE));
		}
	}

	private void throwIce() {
		if (!this.prepared) {
			return;
		}

		final LivingEntity target = (LivingEntity) GeneralMethods.getTargetedEntity(this.player, this.range, new ArrayList<Entity>());
		if (target == null) {
			this.destination = GeneralMethods.getTargetedLocation(this.player, this.range, getTransparentMaterials());
		} else {
			this.destination = target.getEyeLocation();
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

		if (TempBlock.isTempBlock(this.sourceBlock)) {
			TempBlock.get(this.sourceBlock).setType(Material.PACKED_ICE);
			this.source = TempBlock.get(this.sourceBlock);
		} else {
			new TempBlock(this.sourceBlock, Material.AIR);
			this.source = new TempBlock(this.sourceBlock, Material.PACKED_ICE);
		}
	}

	@Override
	public void progress() {
		if (!this.bPlayer.canBendIgnoreBinds(this)) {
			this.remove();
			return;
		}

		if (this.player.getEyeLocation().distanceSquared(this.location) >= this.range * this.range) {
			if (this.progressing) {
				this.breakParticles(20);
				this.remove();
				this.returnWater();
			} else {
				this.breakParticles(20);
				this.remove();
			}
			return;
		}

		if (!this.bPlayer.getBoundAbilityName().equalsIgnoreCase(this.getName()) && this.prepared) {
			this.remove();
			return;
		}
		if (this.prepared && !isWaterbendable(this.sourceBlock)) {
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

			this.source.revertBlock();
			this.source = null;

			if (isTransparent(this.player, block) && !block.isLiquid()) {
				GeneralMethods.breakBlock(block);
			} else if (!isWater(block)) {
				this.breakParticles(20);
				this.remove();
				this.returnWater();
				return;
			}

			if (GeneralMethods.isRegionProtectedFromBuild(this, this.location)) {
				this.remove();
				this.returnWater();
				return;
			}

			for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(this.location, this.collisionRadius)) {
				if (entity.getEntityId() != this.player.getEntityId() && entity instanceof LivingEntity) {
					this.affect((LivingEntity) entity);
					this.progressing = false;
					this.returnWater();
				}
			}

			if (!this.progressing) {
				this.remove();
				return;
			}

			this.sourceBlock = block;
			if (TempBlock.isTempBlock(this.sourceBlock)) {
				TempBlock.get(this.sourceBlock).setType(Material.PACKED_ICE);
				this.source = TempBlock.get(this.sourceBlock);
			} else {
				this.source = new TempBlock(this.sourceBlock, Material.PACKED_ICE);
			}

			for (int x = 0; x < 10; x++) {
				ParticleEffect.ITEM_CRACK.display(this.location, 5, Math.random() / 2, Math.random() / 2, Math.random() / 2, new ItemStack(Material.ICE));
				ParticleEffect.SNOW_SHOVEL.display(this.location, 5, Math.random() / 2, Math.random() / 2, Math.random() / 2, 0);
			}
			if ((new Random()).nextInt(4) == 0) {
				playIcebendingSound(this.location);
			}
			this.location = this.location.add(direction.clone());
		} else if (this.prepared) {
			playFocusWaterEffect(this.sourceBlock);
		}
	}

	public void breakParticles(final int amount) {
		for (int x = 0; x < amount; x++) {
			ParticleEffect.ITEM_CRACK.display(this.location, 2, Math.random(), Math.random(), Math.random(), new ItemStack(Material.ICE));
			ParticleEffect.SNOW_SHOVEL.display(this.location, 2, Math.random(), Math.random(), Math.random(), 0);
		}
		this.location.getWorld().playSound(this.location, Sound.BLOCK_GLASS_BREAK, 5, 1.3f);
	}

	@Override
	public String getName() {
		return "IceBlast";
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

	public void setCollisionRadius(final double collisionRadius) {
		this.collisionRadius = collisionRadius;
	}

	public double getDeflectRange() {
		return this.deflectRange;
	}

	public void setDeflectRange(final double deflectRange) {
		this.deflectRange = deflectRange;
	}

	@Override
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
	
	public long getSlowCooldown() {
		return this.slowCooldown;
	}

	public void setSlowCooldown(final long slowCooldown) {
		this.slowCooldown = slowCooldown;
	}

}

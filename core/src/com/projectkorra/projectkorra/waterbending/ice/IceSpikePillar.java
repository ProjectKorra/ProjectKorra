package com.projectkorra.projectkorra.waterbending.ice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.projectkorra.projectkorra.attribute.markers.DayNightFactor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.util.TempPotionEffect;

public class IceSpikePillar extends IceAbility {

	/** The list of blocks IceSpike uses */
	private final Map<Block, TempBlock> ice_blocks = new HashMap<Block, TempBlock>();

	@Attribute(Attribute.HEIGHT) @DayNightFactor
	private int height;
	private int progress;
	@Attribute("SlowPotency")
	private int slowPower;
	@Attribute("Slow" + Attribute.DURATION)
	private int slowDuration;
	@Attribute(Attribute.COOLDOWN) @DayNightFactor(invert = true)
	private long cooldown;
	private long time;
	private long removeTimestamp;
	@Attribute(Attribute.DURATION) @DayNightFactor
	private long duration;
	private long interval;
	@Attribute("Slow" + Attribute.COOLDOWN) @DayNightFactor(invert = true)
	private long slowCooldown;
	@Attribute(Attribute.DAMAGE) @DayNightFactor
	private double damage;
	@Attribute(Attribute.RANGE) @DayNightFactor
	private double range;
	@Attribute(Attribute.SPEED) @DayNightFactor
	private double speed;
	private Block source_block; // The block clicked on.
	private Block base_block; // The block at the bottom of the pillar.
	private Location origin;
	private Location location;
	@Attribute(Attribute.KNOCKUP)
	private double thrownForce;
	private Vector direction;
	private ArrayList<LivingEntity> damaged;
	protected boolean inField = false; // If it's part of a field or not.

	public IceSpikePillar(final Player player) {
		super(player);
		this.setFields();

		if (this.bPlayer.isOnCooldown("IceSpikePillar")) {
			return;
		}

		this.recalculateAttributes();

		try {
			double lowestDistance = this.range + 1;
			Entity closestEntity = null;
			for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(player.getLocation(), this.range)) {
				if (GeneralMethods.getDistanceFromLine(player.getLocation().getDirection(), player.getLocation(), entity.getLocation()) <= 2 && (entity instanceof LivingEntity) && (entity.getEntityId() != player.getEntityId())) {
					double distance = 0;
					if (player.getWorld().equals(entity.getWorld())) {
						distance = player.getLocation().distance(entity.getLocation());
					}
					if (distance < lowestDistance) {
						closestEntity = entity;
						lowestDistance = distance;
					}
				}
			}

			if (closestEntity != null) {
				final Block tempTestingBlock = closestEntity.getLocation().getBlock().getRelative(BlockFace.DOWN, 1);
				this.source_block = tempTestingBlock;
			} else {
				this.source_block = WaterAbility.getIceSourceBlock(player, this.range);
				if (this.source_block == null) {
					return;
				}
			}
			this.origin = this.source_block.getLocation();
			this.location = this.origin.clone();
		} catch (final IllegalStateException e) {
			return;
		}

		if (this.height != 0) {
			if (this.canInstantiate()) {
				this.start();
				this.time = System.currentTimeMillis() - this.interval;
				this.bPlayer.addCooldown("IceSpikePillar", this.cooldown);
			}
		}
	}

	public IceSpikePillar(final Player player, final Location origin, final int damage, final double throwing, final long aoecooldown) {
		super(player);
		this.setFields();

		this.cooldown = aoecooldown;
		this.player = player;
		this.origin = origin;
		this.damage = damage;
		this.thrownForce = throwing;
		this.location = origin.clone();
		this.source_block = this.location.getBlock();

		if (this.isIcebendable(this.source_block)) {
			if (this.canInstantiate()) {
				this.start();
				this.time = System.currentTimeMillis() - this.interval;
			}
		}
	}

	private void setFields() {
		this.direction = new Vector(0, 1, 0);
		this.speed = getConfig().getDouble("Abilities.Water.IceSpike.Speed");
		this.slowCooldown = getConfig().getLong("Abilities.Water.IceSpike.SlowCooldown");
		this.slowPower = getConfig().getInt("Abilities.Water.IceSpike.SlowPower");
		this.slowDuration = getConfig().getInt("Abilities.Water.IceSpike.SlowDuration");
		this.damage = getConfig().getDouble("Abilities.Water.IceSpike.Damage");
		this.range = getConfig().getDouble("Abilities.Water.IceSpike.Range");
		this.cooldown = getConfig().getLong("Abilities.Water.IceSpike.Cooldown");
		this.height = getConfig().getInt("Abilities.Water.IceSpike.Height");
		this.thrownForce = getConfig().getDouble("Abilities.Water.IceSpike.Push");
		this.damaged = new ArrayList<>();

		this.interval = (long) (1000. / this.speed);
	}

	/**
	 * Reverts the block if it's part of IceSpike
	 *
	 * @param block The Block
	 * @return If the block was removed or not
	 */
	public static boolean revertBlock(final Block block) {
		for (final IceSpikePillar iceSpike : getAbilities(IceSpikePillar.class)) {
			if (iceSpike.ice_blocks.containsKey(block)) {
				iceSpike.ice_blocks.get(block).revertBlock();
				iceSpike.ice_blocks.remove(block);
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks to see if this move can start. Checks things like if there is
	 * enough space to form, if the source isn't a TempBlock, etc.
	 */
	private boolean canInstantiate() {
		if (!this.isIcebendable(this.source_block.getType())) {
			return false;
		}

		Block b;
		for (int i = 1; i <= this.height; i++) {
			b = this.source_block.getWorld().getBlockAt(this.location.clone().add(this.direction.clone().multiply(i)));
			if (!ElementalAbility.isAir(b.getType())) {
				return false;
			}

			if (b.getX() == this.player.getEyeLocation().getBlock().getX() && b.getZ() == this.player.getEyeLocation().getBlock().getZ()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void progress() {
		if (System.currentTimeMillis() - this.time >= this.interval) {
			this.time = System.currentTimeMillis();
			if (this.progress < this.height) {
				this.risePillar();
				this.removeTimestamp = System.currentTimeMillis();
			} else {
				// If it's time to remove.
				if (this.removeTimestamp != 0 && this.removeTimestamp + this.duration <= System.currentTimeMillis()) {
					if (!this.sinkPillar()) {
						this.remove();
						return;
					}
				}
			}
		}
	}

	/**
	 * Makes the pillar rise by 1 block.
	 *
	 * @return If the block was placed successfully.
	 */
	private boolean risePillar() {
		this.progress++;
		final Block affectedBlock = this.location.clone().add(this.direction).getBlock();
		this.location = this.location.add(this.direction);

		if (GeneralMethods.isRegionProtectedFromBuild(this, this.location)) {
			return false;
		}

		for (final Entity en : GeneralMethods.getEntitiesAroundPoint(this.location, 1.4)) {
			if (en instanceof LivingEntity && en != this.player && !this.damaged.contains((en))) {
				final LivingEntity le = (LivingEntity) en;
				this.affect(le);
			}
		}

		final TempBlock b = new TempBlock(affectedBlock, Material.ICE);
		this.ice_blocks.put(affectedBlock, b);

		if (!this.inField || new Random().nextInt((int) ((this.height + 1) * 1.5)) == 0) {
			playIcebendingSound(this.source_block.getLocation());
		}

		return true;
	}

	private void affect(final LivingEntity entity) {
		GeneralMethods.setVelocity(this, entity, new Vector(0, this.thrownForce, 0));
		DamageHandler.damageEntity(entity, this.damage, this);
		this.damaged.add(entity);

		if (entity instanceof Player) {
			if (this.bPlayer.canBeSlowed()) {
				final PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, this.slowDuration, this.slowPower);
				new TempPotionEffect(entity, effect);
				this.bPlayer.slow(this.slowCooldown);
			}
		} else {
			final PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, this.slowDuration, this.slowPower);
			new TempPotionEffect(entity, effect);
		}
		AirAbility.breakBreathbendingHold(entity);
	}

	/**
	 * The reverse of risePillar(). Makes the pillar sink
	 *
	 * @return If the move should continue progressing.
	 */
	public boolean sinkPillar() {
		final Vector direction = this.direction.clone().multiply(-1);
		if (this.ice_blocks.containsKey(this.location.getBlock())) {
			this.ice_blocks.get(this.location.getBlock()).revertBlock();
			this.ice_blocks.remove(this.location.getBlock());
			this.location.add(direction);

			if (this.source_block.equals(this.location.getBlock())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String getName() {
		return "IceSpike";
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public int getHeight() {
		return this.height;
	}

	public void setHeight(final int height) {
		this.height = height;
	}

	public int getProgress() {
		return this.progress;
	}

	public void setProgress(final int progress) {
		this.progress = progress;
	}

	public int getSlowPower() {
		return this.slowPower;
	}

	public void setSlowPower(final int slowPower) {
		this.slowPower = slowPower;
	}

	public int getSlowDuration() {
		return this.slowDuration;
	}

	public void setSlowDuration(final int slowDuration) {
		this.slowDuration = slowDuration;
	}

	@Override
	public long getCooldown() {
		return this.cooldown;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

	public long getTime() {
		return this.time;
	}

	public void setTime(final long time) {
		this.time = time;
	}

	public long getRemoveTimestamp() {
		return this.removeTimestamp;
	}

	public void setRemoveTimestamp(final long removeTimestamp) {
		this.removeTimestamp = removeTimestamp;
	}

	public long getDuration() {
		return this.duration;
	}

	public void setDuration(final long duration) {
		this.duration = duration;
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

	public double getDamage() {
		return this.damage;
	}

	public void setDamage(final double damage) {
		this.damage = damage;
	}

	public double getRange() {
		return this.range;
	}

	public void setRange(final double range) {
		this.range = range;
	}

	public double getSpeed() {
		return this.speed;
	}

	public void setSpeed(final double speed) {
		this.speed = speed;
	}

	public Block getBlock() {
		return this.source_block;
	}

	public void setBlock(final Block block) {
		this.source_block = block;
	}

	public Location getOrigin() {
		return this.origin;
	}

	public void setOrigin(final Location origin) {
		this.origin = origin;
	}

	@Override
	public Location getLocation() {
		return this.location;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

	public double getThrownForce() {
		return this.thrownForce;
	}

	public void setThrownForce(final double thrownForce) {
		this.thrownForce = thrownForce;
	}

	public Vector getDirection() {
		return this.direction;
	}

	public void setDirection(final Vector direction) {
		this.direction = direction;
	}

	public Map<Block, TempBlock> getIceBlocks() {
		return this.ice_blocks;
	}

	public Block getBaseBlock() {
		return this.base_block;
	}

}

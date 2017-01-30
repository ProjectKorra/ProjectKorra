package com.projectkorra.projectkorra.waterbending.ice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.util.TempPotionEffect;

public class IceSpikePillar extends IceAbility {

	/** The list of blocks IceSpike uses */
	private Map<Block, TempBlock> ice_blocks = new HashMap<Block, TempBlock>();

	private int height;
	private int progress;
	private int slowPower;
	private int slowDuration;
	private long cooldown;
	private long time;
	private long removeTimestamp;
	private long removeTimer;
	private long interval;
	private long slowCooldown;
	private double damage;
	private double range;
	private double speed;
	private Block source_block; //The block clicked on
	private Block base_block; //The block at the bottom of the pillar
	private Location origin;
	private Location location;
	private Vector thrownForce;
	private Vector direction;
	private ArrayList<LivingEntity> damaged;
	protected boolean inField = false; //If it's part of a field or not. 

	public IceSpikePillar(Player player) {
		super(player);
		setFields();

		if (bPlayer.isOnCooldown("IceSpikePillar")) {
			return;
		}

		try {
			double lowestDistance = range + 1;
			Entity closestEntity = null;
			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(player.getLocation(), range)) {
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
				Block tempTestingBlock = closestEntity.getLocation().getBlock().getRelative(BlockFace.DOWN, 1);
				this.source_block = tempTestingBlock;
			} else {
				this.source_block = WaterAbility.getIceSourceBlock(player, range);
				if (this.source_block == null) {
					return;
				}
			}
			origin = source_block.getLocation();
			location = origin.clone();
		}
		catch (IllegalStateException e) {
			return;
		}

		if (height != 0) {
			if (canInstantiate()) {
				start();
				time = System.currentTimeMillis() - interval;
				bPlayer.addCooldown("IceSpikePillar", cooldown);
			}
		}
	}

	public IceSpikePillar(Player player, Location origin, int damage, Vector throwing, long aoecooldown) {
		super(player);
		setFields();

		this.cooldown = aoecooldown;
		this.player = player;
		this.origin = origin;
		this.damage = damage;
		this.thrownForce = throwing;
		this.location = origin.clone();
		this.source_block = location.getBlock();

		if (isIcebendable(source_block)) {
			if (canInstantiate()) {
				start();
				time = System.currentTimeMillis() - interval;
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
		this.thrownForce = new Vector(0, getConfig().getDouble("Abilities.Water.IceSpike.Push"), 0);
		this.damaged = new ArrayList<>();

		this.interval = (long) (1000. / speed);
	}

	/**
	 * Reverts the block if it's part of IceSpike
	 * 
	 * @param block The Block
	 * @return If the block was removed or not
	 */
	public static boolean revertBlock(Block block) {
		for (IceSpikePillar iceSpike : getAbilities(IceSpikePillar.class)) {
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
		if (!isIcebendable(source_block.getType())) {
			return false;
		}

		Block b;
		for (int i = 1; i <= height; i++) {
			b = source_block.getWorld().getBlockAt(location.clone().add(direction.clone().multiply(i)));
			if (b.getType() != Material.AIR) {
				return false;
			}

			if (b.getX() == player.getEyeLocation().getBlock().getX() && b.getZ() == player.getEyeLocation().getBlock().getZ()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void progress() {
		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();
			if (progress < height) {
				risePillar();
				removeTimestamp = System.currentTimeMillis();
			} else {
				//If it's time to remove
				if (removeTimestamp != 0 && removeTimestamp + removeTimer <= System.currentTimeMillis()) {
					if (!sinkPillar()) {
						remove();
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
		progress++;
		Block affectedBlock = location.clone().add(direction).getBlock();
		location = location.add(direction);

		if (GeneralMethods.isRegionProtectedFromBuild(this, location)) {
			return false;
		}

		for (Entity en : GeneralMethods.getEntitiesAroundPoint(location, 1.4)) {
			if (en instanceof LivingEntity && en != player && !damaged.contains((en))) {
				LivingEntity le = (LivingEntity) en;
				affect(le);
			}
		}

		TempBlock b = new TempBlock(affectedBlock, Material.ICE, (byte) 0);
		ice_blocks.put(affectedBlock, b);

		if (!inField || new Random().nextInt((int) ((height + 1) * 1.5)) == 0) {
			playIcebendingSound(source_block.getLocation());
		}

		return true;
	}

	private void affect(LivingEntity entity) {
		entity.setVelocity(thrownForce);
		DamageHandler.damageEntity(entity, damage, this);
		damaged.add(entity);

		if (entity instanceof Player) {
			if (bPlayer.canBeSlowed()) {
				PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, slowDuration, slowPower);
				new TempPotionEffect(entity, effect);
				bPlayer.slow(slowCooldown);
			}
		} else {
			PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, slowDuration, slowPower);
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
		Vector direction = this.direction.clone().multiply(-1);
		if (ice_blocks.containsKey(location.getBlock())) {
			ice_blocks.get(location.getBlock()).revertBlock();
			ice_blocks.remove(location.getBlock());
			location.add(direction);

			if (source_block.equals(location.getBlock())) {
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
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
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

	@Override
	public long getCooldown() {
		return cooldown;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getRemoveTimestamp() {
		return removeTimestamp;
	}

	public void setRemoveTimestamp(long removeTimestamp) {
		this.removeTimestamp = removeTimestamp;
	}

	public long getRemoveTimer() {
		return removeTimer;
	}

	public void setRemoveTimer(long removeTimer) {
		this.removeTimer = removeTimer;
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

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public Block getBlock() {
		return source_block;
	}

	public void setBlock(Block block) {
		this.source_block = block;
	}

	public Location getOrigin() {
		return origin;
	}

	public void setOrigin(Location origin) {
		this.origin = origin;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Vector getThrownForce() {
		return thrownForce;
	}

	public void setThrownForce(Vector thrownForce) {
		this.thrownForce = thrownForce;
	}

	public Vector getDirection() {
		return direction;
	}

	public void setDirection(Vector direction) {
		this.direction = direction;
	}

	public Map<Block, TempBlock> getIceBlocks() {
		return ice_blocks;
	}

	public Block getBaseBlock() {
		return base_block;
	}

}

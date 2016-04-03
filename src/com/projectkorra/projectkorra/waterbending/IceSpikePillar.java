package com.projectkorra.projectkorra.waterbending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempPotionEffect;

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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class IceSpikePillar extends IceAbility {

	private static final ConcurrentHashMap<Block, Block> ALREADY_DONE_BLOCKS = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<Block, Integer> BASE_BLOCKS = new ConcurrentHashMap<>();

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
	private Block block;
	private Location origin;
	private Location location;
	private Vector thrownForce;
	private Vector direction;
	private ConcurrentHashMap<Block, Block> affectedBlocks;
	private ArrayList<LivingEntity> damaged;
	protected boolean inField = false;

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
				if (GeneralMethods.getDistanceFromLine(player.getLocation().getDirection(), player.getLocation(), entity.getLocation()) <= 2 
						&& (entity instanceof LivingEntity) 
						&& (entity.getEntityId() != player.getEntityId())) {
					double distance = player.getLocation().distance(entity.getLocation());
					if (distance < lowestDistance) {
						closestEntity = entity;
						lowestDistance = distance;
					}
				}
			}
			
			if (closestEntity != null) {
				Block tempTestingBlock = closestEntity.getLocation().getBlock().getRelative(BlockFace.DOWN, 1);
				this.block = tempTestingBlock;
			} else {
				this.block = player.getTargetBlock((HashSet<Material>) null, (int) range);
			}
			origin = block.getLocation();
			location = origin.clone();
		} catch (IllegalStateException e) {
			return;
		}

		loadAffectedBlocks();

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
		this.block = location.getBlock();

		loadAffectedBlocks();

		if (isIcebendable(block)) {
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
		this.affectedBlocks = new ConcurrentHashMap<>();
		this.damaged = new ArrayList<>();
		
		this.interval = (long) (1000. / speed);
	}

	private void loadAffectedBlocks() {
		affectedBlocks.clear();
		Block thisBlock;
		for (int i = 1; i <= height; i++) {
			thisBlock = block.getWorld().getBlockAt(location.clone().add(direction.clone().multiply(i)));
			affectedBlocks.put(thisBlock, thisBlock);
		}
	}

	private boolean blockInAffectedBlocks(Block block) {
		return affectedBlocks.containsKey(block);
	}

	public static boolean blockInAllAffectedBlocks(Block block) {
		for (IceSpikePillar iceSpike : getAbilities(IceSpikePillar.class)) {
			if (iceSpike.blockInAffectedBlocks(block)) {
				return true;
			}
		}
		return false;
	}

	public static void revertBlock(Block block) {
		for (IceSpikePillar iceSpike : getAbilities(IceSpikePillar.class)) {
			iceSpike.affectedBlocks.remove(block);
		}
	}

	private boolean canInstantiate() {
		if (!isIcebendable(block.getType())) {
			return false;
		}
		for (Block block : affectedBlocks.keySet()) {
			if (blockInAllAffectedBlocks(block) || ALREADY_DONE_BLOCKS.containsKey(block) 
					|| block.getType() != Material.AIR 
					|| (block.getX() == player.getEyeLocation().getBlock().getX() && block.getZ() == player.getEyeLocation().getBlock().getZ())) {
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
				if (removeTimestamp != 0 && removeTimestamp + removeTimer <= System.currentTimeMillis()) {
					BASE_BLOCKS.put(location.clone().add(direction.clone().multiply(-1 * (height))).getBlock(), (height - 1));
					if (!revertblocks()) {
						remove();
						return;
					}
				}
			}
		}
	}

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
		
		affectedBlock.setType(Material.ICE);
		if (!inField || new Random().nextInt((int) ((height + 1) * 1.5)) == 0) {
			playIcebendingSound(block.getLocation());
		}
		loadAffectedBlocks();

		if (location.distanceSquared(origin) >= height * height) {
			return false;
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

	public static boolean blockIsBase(Block block) {
		return block != null ? BASE_BLOCKS.containsKey(block) : null;
	}

	public static void removeBlockBase(Block block) {
		if (block != null) {
			BASE_BLOCKS.remove(block);
		}
	}

	public boolean revertblocks() {
		Vector direction = new Vector(0, -1, 0);
		location.getBlock().setType(Material.AIR);
		location.add(direction);
		
		if (blockIsBase(location.getBlock())) {
			return false;
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
		return block;
	}

	public void setBlock(Block block) {
		this.block = block;
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

	public static ConcurrentHashMap<Block, Block> getAlreadyDoneBlocks() {
		return ALREADY_DONE_BLOCKS;
	}

	public static ConcurrentHashMap<Block, Integer> getBaseBlocks() {
		return BASE_BLOCKS;
	}
		
}

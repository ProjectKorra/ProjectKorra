package com.projectkorra.projectkorra.waterbending;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.firebending.FireBlast;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.plant.PlantRegrowth;
import com.projectkorra.projectkorra.waterbending.util.WaterReturn;

public class SurgeWave extends WaterAbility {

	private boolean freezing;
	private boolean activateFreeze;
	private boolean progressing;
	private boolean canHitSelf;
	private long time;
	private long cooldown;
	private long interval;
	private long iceRevertTime;
	private double currentRadius;
	private double maxRadius;
	private double range;
	private double selectRange;
	private double pushFactor;
	private double verticalFactor;
	private double maxFreezeRadius;
	private Block sourceBlock;
	private Location location;
	private Location targetDestination;
	private Location frozenLocation;
	private Vector targetDirection;
	private Map<Block, Block> waveBlocks;
	private Map<Block, Material> frozenBlocks;

	public SurgeWave(Player player) {
		super(player);

		SurgeWave wave = getAbility(player, SurgeWave.class);
		if (wave != null) {
			if (wave.progressing && !wave.freezing) {
				wave.freezing = true;
				return;
			}
		}

		this.canHitSelf = true;
		this.currentRadius = 1;
		this.cooldown = getConfig().getLong("Abilities.Water.Surge.Wave.Cooldown");
		this.interval = getConfig().getLong("Abilities.Water.Surge.Wave.Interval");
		this.maxRadius = getConfig().getDouble("Abilities.Water.Surge.Wave.Radius");
		this.pushFactor = getConfig().getDouble("Abilities.Water.Surge.Wave.HorizontalPush");
		this.verticalFactor = getConfig().getDouble("Abilities.Water.Surge.Wave.VerticalPush");
		this.maxFreezeRadius = getConfig().getDouble("Abilities.Water.Surge.Wave.MaxFreezeRadius");
		this.iceRevertTime = getConfig().getLong("Abilities.Water.Surge.Wave.IceRevertTime");
		this.range = getConfig().getDouble("Abilities.Water.Surge.Wave.Range");
		this.selectRange = getConfig().getDouble("Abilities.Water.Surge.Wave.SelectRange");
		this.waveBlocks = new ConcurrentHashMap<>();
		this.frozenBlocks = new ConcurrentHashMap<>();

		if (bPlayer.isAvatarState()) {
			maxRadius = getConfig().getDouble("Abilities.Avatar.AvatarState.Water.Surge.Wave.Radius");
		}
		maxRadius = getNightFactor(maxRadius);

		if (prepare()) {
			wave = getAbility(player, SurgeWave.class);
			if (wave != null) {
				wave.remove();
			}
			start();
			time = System.currentTimeMillis();
		}
	}

	private void addWater(Block block) {
		if (GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
			return;
		} else if (!TempBlock.isTempBlock(block)) {
			new TempBlock(block, Material.STATIONARY_WATER, (byte) 8);
			waveBlocks.put(block, block);
		}
	}

	private void cancelPrevious() {
		SurgeWave oldWave = getAbility(player, SurgeWave.class);
		if (oldWave != null) {
			oldWave.remove();
		}
	}

	private void clearWave() {
		for (Block block : waveBlocks.keySet()) {
			TempBlock.revertBlock(block, Material.AIR);
		}
		waveBlocks.clear();
	}

	private void finalRemoveWater(Block block) {
		if (waveBlocks.containsKey(block)) {
			TempBlock.revertBlock(block, Material.AIR);
			waveBlocks.remove(block);
		}
	}

	private void focusBlock() {
		location = sourceBlock.getLocation();
	}

	private void freeze() {
		clearWave();
		if (!bPlayer.canIcebend()) {
			return;
		}

		double freezeradius = currentRadius;
		if (freezeradius > maxFreezeRadius) {
			freezeradius = maxFreezeRadius;
		}

		for (Block block : GeneralMethods.getBlocksAroundPoint(frozenLocation, freezeradius)) {
			if (GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation()) || GeneralMethods.isRegionProtectedFromBuild(player, "PhaseChange", block.getLocation())) {
				continue;
			} else if (TempBlock.isTempBlock(block)) {
				continue;
			}

			Block oldBlock = block;
			if (block.getType() == Material.AIR || block.getType() == Material.SNOW || isWater(block)) {
				TempBlock tblock = new TempBlock(block, Material.ICE, (byte) 0);
				tblock.setRevertTime(iceRevertTime + (new Random().nextInt(1000)));
				frozenBlocks.put(block, oldBlock.getType());
			}
			if (isPlant(block) && block.getType() != Material.LEAVES) {
				block.breakNaturally();
				TempBlock tblock = new TempBlock(block, Material.ICE, (byte) 0);
				tblock.setRevertTime(iceRevertTime + (new Random().nextInt(1000)));
				frozenBlocks.put(block, oldBlock.getType());
			}
			for (Block sound : frozenBlocks.keySet()) {
				if ((new Random()).nextInt(4) == 0) {
					playWaterbendingSound(sound.getLocation());
				}
			}
		}
	}

	private Vector getDirection(Location location, Location destination) {
		double x1, y1, z1;
		double x0, y0, z0;

		x1 = destination.getX();
		y1 = destination.getY();
		z1 = destination.getZ();

		x0 = location.getX();
		y0 = location.getY();
		z0 = location.getZ();

		return new Vector(x1 - x0, y1 - y0, z1 - z0);
	}

	@SuppressWarnings("deprecation")
	public void moveWater() {
		if (bPlayer.isOnCooldown("SurgeWave")) {
			return;
		}
		bPlayer.addCooldown("SurgeWave", cooldown);

		if (sourceBlock != null) {
			if (!sourceBlock.getWorld().equals(player.getWorld())) {
				return;
			}

			range = getNightFactor(range);
			if (bPlayer.isAvatarState()) {
				pushFactor = AvatarState.getValue(pushFactor);
			}

			Entity target = GeneralMethods.getTargetedEntity(player, range);
			if (target == null) {
				targetDestination = player.getTargetBlock(getTransparentMaterialSet(), (int) range).getLocation();
			} else {
				targetDestination = ((LivingEntity) target).getEyeLocation();
			}

			if (targetDestination.distanceSquared(location) <= 1) {
				progressing = false;
				targetDestination = null;
			} else {
				progressing = true;
				targetDirection = getDirection(sourceBlock.getLocation(), targetDestination).normalize();
				targetDestination = location.clone().add(targetDirection.clone().multiply(range));

				if (isPlant(sourceBlock) || isSnow(sourceBlock)) {
					new PlantRegrowth(player, sourceBlock);
					sourceBlock.setType(Material.AIR);
				}
				addWater(sourceBlock);
			}
		}
	}

	public boolean prepare() {
		cancelPrevious();
		Block block = BlockSource.getWaterSourceBlock(player, selectRange, ClickType.SHIFT_DOWN, true, true, bPlayer.canPlantbend());
		if (block != null && !GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
			sourceBlock = block;
			focusBlock();
			return true;
		}
		return false;
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			remove();
			return;
		}

		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();
			if (!progressing && !bPlayer.getBoundAbilityName().equalsIgnoreCase(getName())) {
				remove();
				return;
			} else if (!progressing) {
				sourceBlock.getWorld().playEffect(location, Effect.SMOKE, 4, (int) range);
				return;
			}

			if (activateFreeze) {
				if (location.distanceSquared(player.getLocation()) > range * range) {
					progressing = false;
					remove();
					return;
				}
			} else {
				Vector direction = targetDirection;
				location = location.clone().add(direction);
				Block blockl = location.getBlock();
				ArrayList<Block> blocks = new ArrayList<Block>();

				if (!GeneralMethods.isRegionProtectedFromBuild(this, location) && (((blockl.getType() == Material.AIR || blockl.getType() == Material.FIRE || isPlant(blockl) || isWater(blockl) || isWaterbendable(player, blockl))) && blockl.getType() != Material.LEAVES)) {
					for (double i = 0; i <= currentRadius; i += .5) {
						for (double angle = 0; angle < 360; angle += 10) {
							Vector vec = GeneralMethods.getOrthogonalVector(targetDirection, angle, i);
							Block block = location.clone().add(vec).getBlock();

							if (!blocks.contains(block) && (block.getType() == Material.AIR || block.getType() == Material.FIRE) || isWaterbendable(block)) {
								blocks.add(block);
								FireBlast.removeFireBlastsAroundPoint(block.getLocation(), 2);
							}

							if ((new Random()).nextInt(15) == 0) {
								playWaterbendingSound(location);
							}
						}
					}
				}

				for (Block block : waveBlocks.keySet()) {
					if (!blocks.contains(block)) {
						finalRemoveWater(block);
					}
				}
				for (Block block : blocks) {
					if (!waveBlocks.containsKey(block)) {
						addWater(block);
					}
				}

				if (waveBlocks.isEmpty()) {
					location = location.subtract(direction);
					remove();
					progressing = false;
					return;
				}

				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2 * currentRadius)) {
					boolean knockback = false;
					for (Block block : waveBlocks.keySet()) {
						if (entity.getLocation().distanceSquared(block.getLocation()) <= 4) {
							if (entity instanceof LivingEntity && freezing && entity.getEntityId() != player.getEntityId()) {
								activateFreeze = true;
								frozenLocation = entity.getLocation();
								freeze();
								break;
							}
							if (entity.getEntityId() != player.getEntityId() || canHitSelf) {
								knockback = true;
							}
						}
					}
					if (knockback) {
						Vector dir = direction.clone();
						dir.setY(dir.getY() * verticalFactor);
						GeneralMethods.setVelocity(entity, entity.getVelocity().clone().add(dir.clone().multiply(getNightFactor(pushFactor))));

						entity.setFallDistance(0);
						if (entity.getFireTicks() > 0) {
							entity.getWorld().playEffect(entity.getLocation(), Effect.EXTINGUISH, 0);
						}
						entity.setFireTicks(0);
						AirAbility.breakBreathbendingHold(entity);
					}
				}

				if (!progressing) {
					remove();
					return;
				}

				if (location.distanceSquared(targetDestination) < 1) {
					progressing = false;
					remove();
					returnWater();
					return;
				}
				if (currentRadius < maxRadius) {
					currentRadius += 0.5;
				}
			}
		}
	}

	@Override
	public void remove() {
		super.remove();
		thaw();
		returnWater();
		if (waveBlocks != null) {
			for (Block block : waveBlocks.keySet()) {
				finalRemoveWater(block);
			}
		}
	}

	public void returnWater() {
		if (location != null && player.isOnline()) {
			new WaterReturn(player, location.getBlock());
		}
	}

	private void thaw() {
		if (frozenBlocks != null) {
			for (Block block : frozenBlocks.keySet()) {
				TempBlock.revertBlock(block, frozenBlocks.get(block));
				frozenBlocks.remove(block);
			}
		}
	}

	public static boolean canThaw(Block block) {
		for (SurgeWave surgeWave : getAbilities(SurgeWave.class)) {
			if (surgeWave.frozenBlocks.containsKey(block)) {
				return false;
			}
		}
		return true;
	}

	public static void removeAllCleanup() {
		for (SurgeWave surgeWave : getAbilities(SurgeWave.class)) {
			for (Block block : surgeWave.waveBlocks.keySet()) {
				block.setType(Material.AIR);
				surgeWave.waveBlocks.remove(block);
			}
			for (Block block : surgeWave.frozenBlocks.keySet()) {
				TempBlock.revertBlock(block, Material.AIR);
				surgeWave.frozenBlocks.remove(block);
			}
		}
	}

	public static boolean isBlockWave(Block block) {
		for (SurgeWave surgeWave : getAbilities(SurgeWave.class)) {
			if (surgeWave.waveBlocks.containsKey(block)) {
				return true;
			}
		}
		return false;
	}

	public static void thaw(Block block) {
		for (SurgeWave surgeWave : getAbilities(SurgeWave.class)) {
			if (surgeWave.frozenBlocks.containsKey(block)) {
				TempBlock.revertBlock(block, Material.AIR);
				surgeWave.frozenBlocks.remove(block);
			}
		}
	}

	@Override
	public String getName() {
		return "Surge";
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

	@Override
	public boolean isCollidable() {
		return progressing || activateFreeze;
	}

	@Override
	public List<Location> getLocations() {
		ArrayList<Location> locations = new ArrayList<>();
		for (Block block : waveBlocks.keySet()) {
			locations.add(block.getLocation());
		}
		for (Block block : frozenBlocks.keySet()) {
			locations.add(block.getLocation());
		}
		return locations;
	}

	public boolean isFreezing() {
		return freezing;
	}

	public void setFreezing(boolean freezing) {
		this.freezing = freezing;
	}

	public boolean isActivateFreeze() {
		return activateFreeze;
	}

	public void setActivateFreeze(boolean activateFreeze) {
		this.activateFreeze = activateFreeze;
	}

	public boolean isProgressing() {
		return progressing;
	}

	public void setProgressing(boolean progressing) {
		this.progressing = progressing;
	}

	public boolean isCanHitSelf() {
		return canHitSelf;
	}

	public void setCanHitSelf(boolean canHitSelf) {
		this.canHitSelf = canHitSelf;
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

	public double getCurrentRadius() {
		return currentRadius;
	}

	public void setCurrentRadius(double currentRadius) {
		this.currentRadius = currentRadius;
	}

	public double getMaxRadius() {
		return maxRadius;
	}

	public void setMaxRadius(double maxRadius) {
		this.maxRadius = maxRadius;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getPushFactor() {
		return pushFactor;
	}

	public void setPushFactor(double pushFactor) {
		this.pushFactor = pushFactor;
	}

	public double getVerticalFactor() {
		return verticalFactor;
	}

	public void setVerticalFactor(double verticalFactor) {
		this.verticalFactor = verticalFactor;
	}

	public double getMaxFreezeRadius() {
		return maxFreezeRadius;
	}

	public void setMaxFreezeRadius(double maxFreezeRadius) {
		this.maxFreezeRadius = maxFreezeRadius;
	}

	public Block getSourceBlock() {
		return sourceBlock;
	}

	public void setSourceBlock(Block sourceBlock) {
		this.sourceBlock = sourceBlock;
	}

	public Location getTargetDestination() {
		return targetDestination;
	}

	public void setTargetDestination(Location targetDestination) {
		this.targetDestination = targetDestination;
	}

	public Location getFrozenLocation() {
		return frozenLocation;
	}

	public void setFrozenLocation(Location frozenLocation) {
		this.frozenLocation = frozenLocation;
	}

	public Vector getTargetDirection() {
		return targetDirection;
	}

	public void setTargetDirection(Vector targetDirection) {
		this.targetDirection = targetDirection;
	}

	public Map<Block, Block> getWaveBlocks() {
		return waveBlocks;
	}

	public Map<Block, Material> getFrozenBlocks() {
		return frozenBlocks;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

}

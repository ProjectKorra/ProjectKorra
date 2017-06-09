package com.projectkorra.projectkorra.earthbending.lava;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.LavaAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.firebending.FireBlast;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.BlockSource.BlockSourceType;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class LavaSurgeWave extends LavaAbility {

	private boolean progressing;
	private boolean canHitSelf;
	private long time;
	private long cooldown;
	private double range;
	private double radius;
	private double maxRadius;
	private double horizontalPush;
	private double verticalPush;
	private double interval;
	private Location location;
	private Block sourceBlock;
	private Location targetDestination;
	private Vector targetDirection;
	private ConcurrentHashMap<Block, Block> waveBlocks;
	private ConcurrentHashMap<Block, Block> frozenBlocks;

	public LavaSurgeWave(Player player) {
		super(player);

		this.progressing = false;
		this.canHitSelf = true;
		this.range = 20;
		this.radius = 1;
		this.interval = 30;
		this.cooldown = GeneralMethods.getGlobalCooldown();
		this.maxRadius = getConfig().getDouble("Abilities.Earth.LavaSurge.Radius");
		this.horizontalPush = getConfig().getDouble("Abilities.Earth.LavaSurge.HorizontalPush");
		this.verticalPush = getConfig().getDouble("Abilities.Earth.LavaSurge.VerticalPush");
		this.waveBlocks = new ConcurrentHashMap<Block, Block>();
		this.frozenBlocks = new ConcurrentHashMap<Block, Block>();

		if (bPlayer.isAvatarState()) {
			range = AvatarState.getValue(range);
			maxRadius = AvatarState.getValue(maxRadius);
			horizontalPush = AvatarState.getValue(horizontalPush);
			verticalPush = AvatarState.getValue(verticalPush);
		}

		if (prepare()) {
			LavaSurgeWave wave = getAbility(player, LavaSurgeWave.class);
			if (wave != null) {
				wave.remove();
			}
			start();
			time = System.currentTimeMillis();
		}
	}

	public boolean prepare() {
		cancelPrevious();
		Block block = BlockSource.getSourceBlock(player, range, BlockSourceType.LAVA, ClickType.SHIFT_DOWN);

		if (block != null) {
			sourceBlock = block;
			focusBlock();
			return true;
		}
		return false;
	}

	private void cancelPrevious() {
		LavaSurgeWave oldWave = getAbility(player, LavaSurgeWave.class);
		if (oldWave != null) {
			if (oldWave.progressing) {
				oldWave.breakBlock();
			} else {
				oldWave.remove();
			}
		}
	}

	private void focusBlock() {
		location = sourceBlock.getLocation();
	}

	public void moveLava() {
		if (bPlayer.isOnCooldown(this)) {
			return;
		}

		bPlayer.addCooldown(this);
		if (sourceBlock != null) {
			if (!sourceBlock.getWorld().equals(player.getWorld())) {
				return;
			}

			Entity target = GeneralMethods.getTargetedEntity(player, range);
			if (target == null) {
				targetDestination = getTargetEarthBlock((int) range).getLocation();
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

				if (!GeneralMethods.isAdjacentToThreeOrMoreSources(sourceBlock)) {
					sourceBlock.setType(Material.AIR);
				}
				addLava(sourceBlock);
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

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			breakBlock();
			return;
		}

		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();
			if (!progressing) {
				sourceBlock.getWorld().playEffect(location, Effect.SMOKE, 4, (int) range);
				return;
			}

			Vector direction = targetDirection;
			location = location.clone().add(direction);
			Block blockl = location.getBlock();
			ArrayList<Block> blocks = new ArrayList<Block>();

			if (!GeneralMethods.isRegionProtectedFromBuild(this, location) && blockl.getType() != Material.LEAVES && (blockl.getType() == Material.AIR || blockl.getType() == Material.FIRE || WaterAbility.isPlant(blockl) || isLava(blockl))) {
				for (double i = 0; i <= radius; i += 0.5) {
					for (double angle = 0; angle < 360; angle += 10) {
						Vector vec = GeneralMethods.getOrthogonalVector(targetDirection, angle, i);
						Block block = location.clone().add(vec).getBlock();

						if (!blocks.contains(block) && (block.getType() == Material.AIR || block.getType() == Material.FIRE) || isLavabendable(block)) {
							blocks.add(block);
							FireBlast.removeFireBlastsAroundPoint(block.getLocation(), 2);
						}
					}
				}
			}

			for (Block block : waveBlocks.keySet()) {
				if (!blocks.contains(block)) {
					finalRemoveLava(block);
				}
			}
			for (Block block : blocks) {
				if (!waveBlocks.containsKey(block)) {
					addLava(block);
				}
			}
			if (waveBlocks.isEmpty()) {
				breakBlock();
				progressing = false;
				return;
			}

			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2 * radius)) {
				boolean knockback = false;
				for (Block block : waveBlocks.keySet()) {
					if (entity.getLocation().distanceSquared(block.getLocation()) <= 2 * 2) {
						if (entity.getEntityId() != player.getEntityId() || canHitSelf) {
							knockback = true;
						}
					}
				}
				if (knockback) {
					Vector dir = direction.clone();
					dir.setY(dir.getY() * verticalPush);
					entity.setVelocity(entity.getVelocity().clone().add(dir.clone().multiply(horizontalPush)));
					entity.setFallDistance(0);

					if (entity.getFireTicks() > 0) {
						entity.getWorld().playEffect(entity.getLocation(), Effect.EXTINGUISH, 0);
					}
					entity.setFireTicks(0);
				}
			}

			if (!progressing) {
				breakBlock();
				return;
			}
			if (location.distanceSquared(targetDestination) < 1) {
				progressing = false;
				breakBlock();
				return;
			}
			if (radius < maxRadius) {
				radius += .5;
			}
			return;
		}

		return;
	}

	private void breakBlock() {
		for (Block block : waveBlocks.keySet()) {
			finalRemoveLava(block);
		}
		remove();
	}

	private void finalRemoveLava(Block block) {
		if (waveBlocks.containsKey(block)) {
			TempBlock.revertBlock(block, Material.AIR);
			waveBlocks.remove(block);
		}
	}

	private void addLava(Block block) {
		if (GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
			return;
		} else if (!TempBlock.isTempBlock(block)) {
			new TempBlock(block, Material.STATIONARY_LAVA, (byte) 8);
			waveBlocks.put(block, block);
		}
	}

	public static boolean isBlockInWave(Block block) {
		for (LavaSurgeWave lavaWave : getAbilities(LavaSurgeWave.class)) {
			if (block.getWorld().equals(lavaWave.location.getWorld()) && block.getLocation().distance(lavaWave.location) <= 2 * lavaWave.radius) {
				return true;
			}
		}
		return false;
	}

	public static boolean isBlockWave(Block block) {
		for (LavaSurgeWave lavaWave : getAbilities(LavaSurgeWave.class)) {
			if (lavaWave.waveBlocks.containsKey(block)) {
				return true;
			}
		}
		return false;
	}

	public static void launch(Player player) {
		LavaSurgeWave lavaWave = getAbility(player, LavaSurgeWave.class);
		if (lavaWave != null) {
			lavaWave.moveLava();
		}
	}

	public static void cleanup() {
		for (LavaSurgeWave lavaWave : getAbilities(LavaSurgeWave.class)) {
			for (Block block : lavaWave.waveBlocks.keySet()) {
				block.setType(Material.AIR);
				lavaWave.waveBlocks.remove(block);
			}
			for (Block block : lavaWave.frozenBlocks.keySet()) {
				block.setType(Material.AIR);
				lavaWave.frozenBlocks.remove(block);
			}
		}
	}

	@Override
	public String getName() {
		return "LavaSurgeWave";
	}

	@Override
	public Location getLocation() {
		return location;
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
	public boolean isHiddenAbility() {
		return true;
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

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getMaxRadius() {
		return maxRadius;
	}

	public void setMaxRadius(double maxRadius) {
		this.maxRadius = maxRadius;
	}

	public double getHorizontalPush() {
		return horizontalPush;
	}

	public void setHorizontalPush(double horizontalPush) {
		this.horizontalPush = horizontalPush;
	}

	public double getVerticalPush() {
		return verticalPush;
	}

	public void setVerticalPush(double verticalPush) {
		this.verticalPush = verticalPush;
	}

	public double getInterval() {
		return interval;
	}

	public void setInterval(double interval) {
		this.interval = interval;
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

	public Vector getTargetDirection() {
		return targetDirection;
	}

	public void setTargetDirection(Vector targetDirection) {
		this.targetDirection = targetDirection;
	}

	public ConcurrentHashMap<Block, Block> getWaveBlocks() {
		return waveBlocks;
	}

	public ConcurrentHashMap<Block, Block> getFrozenBlocks() {
		return frozenBlocks;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

}

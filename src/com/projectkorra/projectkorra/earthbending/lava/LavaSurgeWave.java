package com.projectkorra.projectkorra.earthbending.lava;

import java.util.ArrayList;
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
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.LavaAbility;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.firebending.FireBlast;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.BlockSource.BlockSourceType;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.region.RegionProtection;

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
	private final ConcurrentHashMap<Block, Block> waveBlocks;
	private final ConcurrentHashMap<Block, Block> frozenBlocks;

	public LavaSurgeWave(final Player player) {
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

		if (this.bPlayer.isAvatarState()) {
			this.range = AvatarState.getValue(this.range);
			this.maxRadius = AvatarState.getValue(this.maxRadius);
			this.horizontalPush = AvatarState.getValue(this.horizontalPush);
			this.verticalPush = AvatarState.getValue(this.verticalPush);
		}

		if (this.prepare()) {
			final LavaSurgeWave wave = getAbility(player, LavaSurgeWave.class);
			if (wave != null) {
				wave.remove();
			}
			this.start();
			this.time = System.currentTimeMillis();
		}
	}

	public boolean prepare() {
		this.cancelPrevious();
		final Block block = BlockSource.getSourceBlock(this.player, this.range, BlockSourceType.LAVA, ClickType.SHIFT_DOWN);

		if (block != null) {
			this.sourceBlock = block;
			this.focusBlock();
			return true;
		}
		return false;
	}

	private void cancelPrevious() {
		final LavaSurgeWave oldWave = getAbility(this.player, LavaSurgeWave.class);
		if (oldWave != null) {
			if (oldWave.progressing) {
				oldWave.breakBlock();
			} else {
				oldWave.remove();
			}
		}
	}

	private void focusBlock() {
		this.location = this.sourceBlock.getLocation();
	}

	public void moveLava() {
		if (this.bPlayer.isOnCooldown(this)) {
			return;
		}

		this.bPlayer.addCooldown(this);
		if (this.sourceBlock != null) {
			if (!this.sourceBlock.getWorld().equals(this.player.getWorld())) {
				return;
			}

			final Entity target = GeneralMethods.getTargetedEntity(this.player, this.range);
			if (target == null) {
				this.targetDestination = this.getTargetEarthBlock((int) this.range).getLocation();
			} else {
				this.targetDestination = ((LivingEntity) target).getEyeLocation();
			}

			if (this.targetDestination.distanceSquared(this.location) <= 1) {
				this.progressing = false;
				this.targetDestination = null;
			} else {
				this.progressing = true;
				this.targetDirection = this.getDirection(this.sourceBlock.getLocation(), this.targetDestination).normalize();
				this.targetDestination = this.location.clone().add(this.targetDirection.clone().multiply(this.range));

				if (!GeneralMethods.isAdjacentToThreeOrMoreSources(this.sourceBlock)) {
					this.sourceBlock.setType(Material.AIR);
				}
				this.addLava(this.sourceBlock);
			}
		}
	}

	private Vector getDirection(final Location location, final Location destination) {
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
		if (!this.bPlayer.canBendIgnoreCooldowns(this)) {
			this.breakBlock();
			return;
		}

		if (System.currentTimeMillis() - this.time >= this.interval) {
			this.time = System.currentTimeMillis();
			if (!this.progressing) {
				this.sourceBlock.getWorld().playEffect(this.location, Effect.SMOKE, 4, (int) this.range);
				return;
			}

			final Vector direction = this.targetDirection;
			this.location = this.location.clone().add(direction);
			final Block blockl = this.location.getBlock();
			final ArrayList<Block> blocks = new ArrayList<Block>();

			if (!RegionProtection.isRegionProtected(this, this.location) && (ElementalAbility.isAir(blockl.getType()) || blockl.getType() == Material.FIRE || ElementalAbility.isPlant(blockl) || isLava(blockl))) {
				for (double i = 0; i <= this.radius; i += 0.5) {
					for (double angle = 0; angle < 360; angle += 10) {
						final Vector vec = GeneralMethods.getOrthogonalVector(this.targetDirection, angle, i);
						final Block block = this.location.clone().add(vec).getBlock();

						if (!blocks.contains(block) && (ElementalAbility.isAir(block.getType()) || block.getType() == Material.FIRE) || this.isLavabendable(block)) {
							blocks.add(block);
							FireBlast.removeFireBlastsAroundPoint(block.getLocation(), 2);
						}
					}
				}
			}

			for (final Block block : this.waveBlocks.keySet()) {
				if (!blocks.contains(block)) {
					this.finalRemoveLava(block);
				}
			}
			for (final Block block : blocks) {
				if (!this.waveBlocks.containsKey(block)) {
					this.addLava(block);
				}
			}
			if (this.waveBlocks.isEmpty()) {
				this.breakBlock();
				this.progressing = false;
				return;
			}

			for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(this.location, 2 * this.radius)) {
				boolean knockback = false;
				for (final Block block : this.waveBlocks.keySet()) {
					if (entity.getLocation().distanceSquared(block.getLocation()) <= 2 * 2) {
						if (entity.getEntityId() != this.player.getEntityId() || this.canHitSelf) {
							knockback = true;
						}
					}
				}
				if (knockback) {
					final Vector dir = direction.clone();
					dir.setY(dir.getY() * this.verticalPush);
					GeneralMethods.setVelocity(this, entity, entity.getVelocity().clone().add(dir.clone().multiply(this.horizontalPush)));
					entity.setFallDistance(0);

					if (entity.getFireTicks() > 0) {
						entity.getWorld().playEffect(entity.getLocation(), Effect.EXTINGUISH, 0);
					}
					entity.setFireTicks(0);
				}
			}

			if (!this.progressing) {
				this.breakBlock();
				return;
			}
			if (this.location.distanceSquared(this.targetDestination) < 1) {
				this.progressing = false;
				this.breakBlock();
				return;
			}
			if (this.radius < this.maxRadius) {
				this.radius += .5;
			}
			return;
		}

		return;
	}

	private void breakBlock() {
		for (final Block block : this.waveBlocks.keySet()) {
			this.finalRemoveLava(block);
		}
		this.remove();
	}

	private void finalRemoveLava(final Block block) {
		if (this.waveBlocks.containsKey(block)) {
			TempBlock.revertBlock(block, Material.AIR);
			this.waveBlocks.remove(block);
		}
	}

	private void addLava(final Block block) {
		if (RegionProtection.isRegionProtected(this, block.getLocation())) {
			return;
		} else if (!TempBlock.isTempBlock(block)) {
			new TempBlock(block, Material.LAVA);
			this.waveBlocks.put(block, block);
		}
	}

	public static boolean isBlockInWave(final Block block) {
		for (final LavaSurgeWave lavaWave : getAbilities(LavaSurgeWave.class)) {
			if (block.getWorld().equals(lavaWave.location.getWorld()) && block.getLocation().distance(lavaWave.location) <= 2 * lavaWave.radius) {
				return true;
			}
		}
		return false;
	}

	public static boolean isBlockWave(final Block block) {
		for (final LavaSurgeWave lavaWave : getAbilities(LavaSurgeWave.class)) {
			if (lavaWave.waveBlocks.containsKey(block)) {
				return true;
			}
		}
		return false;
	}

	public static void launch(final Player player) {
		final LavaSurgeWave lavaWave = getAbility(player, LavaSurgeWave.class);
		if (lavaWave != null) {
			lavaWave.moveLava();
		}
	}

	public static void cleanup() {
		for (final LavaSurgeWave lavaWave : getAbilities(LavaSurgeWave.class)) {
			for (final Block block : lavaWave.waveBlocks.keySet()) {
				block.setType(Material.AIR);
				lavaWave.waveBlocks.remove(block);
			}
			for (final Block block : lavaWave.frozenBlocks.keySet()) {
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
	public boolean isHiddenAbility() {
		return true;
	}

	public boolean isProgressing() {
		return this.progressing;
	}

	public void setProgressing(final boolean progressing) {
		this.progressing = progressing;
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

	public double getRange() {
		return this.range;
	}

	public void setRange(final double range) {
		this.range = range;
	}

	public double getRadius() {
		return this.radius;
	}

	public void setRadius(final double radius) {
		this.radius = radius;
	}

	public double getMaxRadius() {
		return this.maxRadius;
	}

	public void setMaxRadius(final double maxRadius) {
		this.maxRadius = maxRadius;
	}

	public double getHorizontalPush() {
		return this.horizontalPush;
	}

	public void setHorizontalPush(final double horizontalPush) {
		this.horizontalPush = horizontalPush;
	}

	public double getVerticalPush() {
		return this.verticalPush;
	}

	public void setVerticalPush(final double verticalPush) {
		this.verticalPush = verticalPush;
	}

	public double getInterval() {
		return this.interval;
	}

	public void setInterval(final double interval) {
		this.interval = interval;
	}

	public Block getSourceBlock() {
		return this.sourceBlock;
	}

	public void setSourceBlock(final Block sourceBlock) {
		this.sourceBlock = sourceBlock;
	}

	public Location getTargetDestination() {
		return this.targetDestination;
	}

	public void setTargetDestination(final Location targetDestination) {
		this.targetDestination = targetDestination;
	}

	public Vector getTargetDirection() {
		return this.targetDirection;
	}

	public void setTargetDirection(final Vector targetDirection) {
		this.targetDirection = targetDirection;
	}

	public ConcurrentHashMap<Block, Block> getWaveBlocks() {
		return this.waveBlocks;
	}

	public ConcurrentHashMap<Block, Block> getFrozenBlocks() {
		return this.frozenBlocks;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

}

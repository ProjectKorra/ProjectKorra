package com.projectkorra.projectkorra.earthbending.lava;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.LavaAbility;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

public class LavaSurge extends LavaAbility {

	private static final HashSet<FallingBlock> ALL_FALLING_BLOCKS = new HashSet<>();

	private boolean hasSurgeStarted;
	private boolean isFractureOpen;
	private boolean canSourceBeEarth;
	private int fallingBlocksCount;
	private int maxBlocks;
	private int particleInterval;
	private int fallingBlockInterval;
	private long time;
	private long lastTime;
	private long cooldown;
	private double impactDamage;
	private double fractureRadius;
	private double prepareRange;
	private double travelRange;
	private Block sourceBlock;
	private Random random;
	private Vector direction;
	private Location startLocation;
	private ArrayList<FallingBlock> fallingBlocks;
	private ArrayList<Block> fracture;
	private ArrayList<TempBlock> fractureTempBlocks;
	private ArrayList<TempBlock> movingLava;
	private Map<FallingBlock, TempBlock> lavaBlocks;
	private ListIterator<Block> listIterator;

	public LavaSurge(final Player player) {
		super(player);

		this.impactDamage = getConfig().getInt("Abilities.Earth.LavaSurge.Damage");
		this.cooldown = getConfig().getLong("Abilities.Earth.LavaSurge.Cooldown");
		this.fractureRadius = getConfig().getDouble("Abilities.Earth.LavaSurge.FractureRadius");
		this.prepareRange = getConfig().getInt("Abilities.Earth.LavaSurge.PrepareRange");
		this.travelRange = getConfig().getInt("Abilities.Earth.LavaSurge.TravelRange");
		this.maxBlocks = getConfig().getInt("Abilities.Earth.LavaSurge.MaxLavaWaves");
		this.canSourceBeEarth = getConfig().getBoolean("Abilities.Earth.LavaSurge.SourceCanBeEarth");
		this.particleInterval = 100;
		this.fallingBlockInterval = 100;

		this.random = new Random();
		this.fallingBlocks = new ArrayList<>();
		this.fracture = new ArrayList<>();
		this.fractureTempBlocks = new ArrayList<>();
		this.movingLava = new ArrayList<>();
		this.lavaBlocks = new ConcurrentHashMap<>();

		if (!this.isEligible()) {
			return;
		} else if (this.bPlayer.isOnCooldown(this)) {
			return;
		}

		this.lastTime = System.currentTimeMillis();

		if (this.prepare()) {
			this.start();
		}
	}

	public boolean isEligible() {
		return this.bPlayer.canBend(this) && this.bPlayer.canLavabend();
	}

	public boolean prepare() {
		final Block targetBlock = BlockSource.getEarthSourceBlock(this.player, this.prepareRange, ClickType.SHIFT_DOWN);

		if (targetBlock == null || !(targetBlock.getRelative(BlockFace.UP).getType() == Material.AIR) && !isLava(targetBlock.getRelative(BlockFace.UP))) {
			return false;
		}

		final LavaSurge otherSurge = getAbility(this.player, this.getClass());
		if (otherSurge != null) {
			otherSurge.revertFracture();
		}

		if ((this.canSourceBeEarth && this.isEarthbendable(targetBlock)) || this.isLavabendable(targetBlock)) {
			this.startLocation = targetBlock.getLocation().add(0, 1, 0);
			this.sourceBlock = targetBlock;
			return true;
		}

		return false;
	}

	public void launch() {
		Location targetLocation = GeneralMethods.getTargetedLocation(this.player, this.travelRange * 2);

		try {
			targetLocation = GeneralMethods.getTargetedEntity(this.player, this.travelRange * 2, null).getLocation();
		}
		catch (final NullPointerException e) {
		}

		if (targetLocation == null) {
			this.remove();
			return;
		}

		this.time = System.currentTimeMillis();
		this.direction = GeneralMethods.getDirection(this.startLocation, targetLocation).multiply(0.07);

		if (this.direction.getY() < 0) {
			this.direction.setY(0);
		}

		if (this.canSourceBeEarth) {
			this.openFracture();
		} else {
			this.skipFracture();
		}
	}

	public void openFracture() {
		final List<Block> affectedBlocks = GeneralMethods.getBlocksAroundPoint(this.sourceBlock.getLocation(), this.fractureRadius);

		for (final Block b : affectedBlocks) {
			if (this.isEarthbendable(b)) {
				this.fracture.add(b);
			}
		}

		this.listIterator = this.fracture.listIterator();
		this.isFractureOpen = true;
		this.bPlayer.addCooldown(this);
	}

	public void skipFracture() {
		this.listIterator = this.fracture.listIterator();
		this.isFractureOpen = true;
	}

	public void revertFracture() {
		for (final TempBlock tb : this.fractureTempBlocks) {
			tb.revertBlock();
		}
		this.fracture.clear();
	}

	@Override
	public void remove() {
		super.remove();
		this.revertFracture();
	}

	public boolean canMoveThrough(final Block block) {
		if (this.isTransparent(this.startLocation.getBlock()) || this.isEarthbendable(this.startLocation.getBlock()) || this.isLavabendable(this.startLocation.getBlock())) {
			return true;
		}
		return false;
	}

	public void removeLava() {
		for (final TempBlock tb : this.lavaBlocks.values()) {
			tb.revertBlock();
		}
		this.movingLava.clear();
	}

	@Override
	public void progress() {
		final long curTime = System.currentTimeMillis();
		if (!this.player.isOnline() || this.player.isDead()) {
			this.remove();
			return;
		} else if (!this.hasSurgeStarted && !this.bPlayer.getBoundAbilityName().equals(this.getName())) {
			this.remove();
			return;
		}

		if (!this.hasSurgeStarted && this.sourceBlock != null && curTime > this.lastTime + this.particleInterval) {
			this.lastTime = curTime;
			ParticleEffect.LAVA.display(this.sourceBlock.getLocation(), 0, 0, 0, 0, 1);
		} else if (this.hasSurgeStarted && curTime > this.lastTime + this.particleInterval) {
			this.lastTime = curTime;
			for (final FallingBlock fblock : this.fallingBlocks) {
				ParticleEffect.LAVA.display(fblock.getLocation(), 0, 0, 0, 0, 1);
			}
		}

		if (this.isFractureOpen && !this.hasSurgeStarted) {
			if (!this.listIterator.hasNext()) {
				this.hasSurgeStarted = true;
			} else {
				final Block b = this.listIterator.next();
				playEarthbendingSound(b.getLocation());

				for (int i = 0; i < 2; i++) {
					final TempBlock tb = new TempBlock(b, Material.LAVA, GeneralMethods.getLavaData(0));
					this.fractureTempBlocks.add(tb);
				}
			}
		}

		if (this.hasSurgeStarted) {
			if (this.fallingBlocksCount >= this.maxBlocks) {
				return;
			}

			if (curTime > this.time + (this.fallingBlockInterval * this.fallingBlocksCount)) {
				final FallingBlock fbs = GeneralMethods.spawnFallingBlock(this.sourceBlock.getLocation().add(0, 1, 0), Material.MAGMA_BLOCK, Material.MAGMA_BLOCK.createBlockData());
				this.fallingBlocks.add(fbs);
				ALL_FALLING_BLOCKS.add(fbs);
				double x = this.random.nextDouble() / 5;
				double z = this.random.nextDouble() / 5;

				x = (this.random.nextBoolean()) ? -x : x;
				z = (this.random.nextBoolean()) ? -z : z;

				fbs.setVelocity(this.direction.clone().add(new Vector(x, 0.2, z)).multiply(1.2));
				fbs.setDropItem(false);

				for (final Block b : this.fracture) {
					if (this.random.nextBoolean() && b != this.sourceBlock) {
						final FallingBlock fb = GeneralMethods.spawnFallingBlock(b.getLocation().add(new Vector(0, 1, 0)), Material.MAGMA_BLOCK, Material.MAGMA_BLOCK.createBlockData());
						ALL_FALLING_BLOCKS.add(fb);
						this.fallingBlocks.add(fb);
						fb.setVelocity(this.direction.clone().add(new Vector(this.random.nextDouble() / 10, 0.1, this.random.nextDouble() / 10)).multiply(1.2));
						fb.setDropItem(false);
					}
				}

				this.fallingBlocksCount++;
			}

			for (final FallingBlock fb : this.fallingBlocks) {
				for (final Entity e : GeneralMethods.getEntitiesAroundPoint(fb.getLocation(), 2)) {
					if (e instanceof LivingEntity) {
						if (e.getEntityId() != this.player.getEntityId()) {
							DamageHandler.damageEntity(e, this.impactDamage, this);
							e.setFireTicks(100);
							GeneralMethods.setVelocity(e, this.direction.clone());
						}
					}
				}
			}
		}
	}

	@Override
	public String getName() {
		return "LavaSurge";
	}

	@Override
	public Location getLocation() {
		return this.startLocation;
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
		return true; // disabled
	}

	public static HashSet<FallingBlock> getAllFallingBlocks() {
		return ALL_FALLING_BLOCKS;
	}

	public boolean isHasSurgeStarted() {
		return this.hasSurgeStarted;
	}

	public void setHasSurgeStarted(final boolean hasSurgeStarted) {
		this.hasSurgeStarted = hasSurgeStarted;
	}

	public boolean isFractureOpen() {
		return this.isFractureOpen;
	}

	public void setFractureOpen(final boolean isFractureOpen) {
		this.isFractureOpen = isFractureOpen;
	}

	public boolean isCanSourceBeEarth() {
		return this.canSourceBeEarth;
	}

	public void setCanSourceBeEarth(final boolean canSourceBeEarth) {
		this.canSourceBeEarth = canSourceBeEarth;
	}

	public int getFallingBlocksCount() {
		return this.fallingBlocksCount;
	}

	public void setFallingBlocksCount(final int fallingBlocksCount) {
		this.fallingBlocksCount = fallingBlocksCount;
	}

	public int getMaxBlocks() {
		return this.maxBlocks;
	}

	public void setMaxBlocks(final int maxBlocks) {
		this.maxBlocks = maxBlocks;
	}

	public int getParticleInterval() {
		return this.particleInterval;
	}

	public void setParticleInterval(final int particleInterval) {
		this.particleInterval = particleInterval;
	}

	public int getFallingBlockInterval() {
		return this.fallingBlockInterval;
	}

	public void setFallingBlockInterval(final int fallingBlockInterval) {
		this.fallingBlockInterval = fallingBlockInterval;
	}

	public long getTime() {
		return this.time;
	}

	public void setTime(final long time) {
		this.time = time;
	}

	public long getLastTime() {
		return this.lastTime;
	}

	public void setLastTime(final long lastTime) {
		this.lastTime = lastTime;
	}

	public double getImpactDamage() {
		return this.impactDamage;
	}

	public void setImpactDamage(final double impactDamage) {
		this.impactDamage = impactDamage;
	}

	public double getFractureRadius() {
		return this.fractureRadius;
	}

	public void setFractureRadius(final double fractureRadius) {
		this.fractureRadius = fractureRadius;
	}

	public double getPrepareRange() {
		return this.prepareRange;
	}

	public void setPrepareRange(final double prepareRange) {
		this.prepareRange = prepareRange;
	}

	public double getTravelRange() {
		return this.travelRange;
	}

	public void setTravelRange(final double travelRange) {
		this.travelRange = travelRange;
	}

	public Block getSourceBlock() {
		return this.sourceBlock;
	}

	public void setSourceBlock(final Block sourceBlock) {
		this.sourceBlock = sourceBlock;
	}

	public Random getRandom() {
		return this.random;
	}

	public void setRandom(final Random random) {
		this.random = random;
	}

	public Vector getDirection() {
		return this.direction;
	}

	public void setDirection(final Vector direction) {
		this.direction = direction;
	}

	public Location getStartLocation() {
		return this.startLocation;
	}

	public void setStartLocation(final Location startLocation) {
		this.startLocation = startLocation;
	}

	public ListIterator<Block> getListIterator() {
		return this.listIterator;
	}

	public void setListIterator(final ListIterator<Block> listIterator) {
		this.listIterator = listIterator;
	}

	public ArrayList<FallingBlock> getFallingBlocks() {
		return this.fallingBlocks;
	}

	public ArrayList<Block> getFracture() {
		return this.fracture;
	}

	public ArrayList<TempBlock> getFractureTempBlocks() {
		return this.fractureTempBlocks;
	}

	public ArrayList<TempBlock> getMovingLava() {
		return this.movingLava;
	}

	public Map<FallingBlock, TempBlock> getLavaBlocks() {
		return this.lavaBlocks;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

}

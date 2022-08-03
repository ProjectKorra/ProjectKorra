package com.projectkorra.projectkorra.waterbending;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import net.jafama.FastMath;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

public class WaterSpout extends WaterAbility {

	private static final Map<Block, Block> AFFECTED_BLOCKS = new ConcurrentHashMap<Block, Block>();
	private final List<TempBlock> blocks = new ArrayList<TempBlock>();

	@Attribute("CanBendOnPackedIce")
	private boolean canBendOnPackedIce;
	private boolean useParticles;
	private boolean useBlockSpiral;
	private int angle;
	private long time;
	private long interval;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;
	private long startTime;
	private double rotation;
	private double height;
	@Attribute(Attribute.HEIGHT)
	private double maxHeight;
	private Block base;
	private TempBlock baseBlock;

	public WaterSpout(final Player player) {
		super(player);

		final WaterSpout oldSpout = getAbility(player, WaterSpout.class);
		if (oldSpout != null) {
			oldSpout.remove();
			return;
		}

		this.canBendOnPackedIce = getConfig().getStringList("Properties.Water.IceBlocks").contains(Material.PACKED_ICE.toString());
		this.useParticles = getConfig().getBoolean("Abilities.Water.WaterSpout.Particles");
		this.useBlockSpiral = getConfig().getBoolean("Abilities.Water.WaterSpout.BlockSpiral");
		this.cooldown = applyInverseModifiers(getConfig().getLong("Abilities.Water.WaterSpout.Cooldown"));
		this.height = applyModifiers(getConfig().getDouble("Abilities.Water.WaterSpout.Height"));
		this.interval = getConfig().getLong("Abilities.Water.WaterSpout.Interval");
		this.duration = getConfig().getLong("Abilities.Water.WaterSpout.Duration");
		this.startTime = System.currentTimeMillis();

		this.maxHeight = this.getNightFactor(this.height);
		final WaterSpoutWave spoutWave = new WaterSpoutWave(player, WaterSpoutWave.AbilityType.CLICK);
		if (spoutWave.isStarted() && !spoutWave.isRemoved()) {
			return;
		}

		Block topBlock = GeneralMethods.getTopBlock(player.getLocation(), (int) -this.getNightFactor(this.height), (int) -this.getNightFactor(this.height));
		if (topBlock == null) {
			topBlock = player.getLocation().getBlock();
		}

		if (!isWater(topBlock) && !this.isIcebendable(topBlock) && !isSnow(topBlock)) {
			return;
		} else if (topBlock.getType() == Material.PACKED_ICE && !this.canBendOnPackedIce) {
			return;
		}

		final double heightRemoveThreshold = 2;
		if (!this.isWithinMaxSpoutHeight(topBlock.getLocation(), heightRemoveThreshold)) {
			return;
		}
		this.flightHandler.createInstance(player, this.getName());
		player.setAllowFlight(true);
		this.spoutableWaterHeight(player.getLocation()); // Sets base.
		this.start();
	}

	private void displayWaterSpiral(final Location location) {
		if (!this.useBlockSpiral) {
			return;
		}

		final double maxHeight = this.player.getLocation().getY() - location.getY() - .5;
		double height = 0;
		this.rotation += .4;
		int i = 0;

		while (height < maxHeight) {
			i += 20;
			height += .4;
			final double angle = (i * Math.PI / 180);
			final double x = 1 * FastMath.cos(angle + this.rotation);
			final double z = 1 * FastMath.sin(angle + this.rotation);

			final Location loc = location.clone().getBlock().getLocation().add(.5, .5, .5);
			loc.add(x, height, z);

			final Block block = loc.getBlock();
			if ((!TempBlock.isTempBlock(block)) && (ElementalAbility.isAir(block.getType()) || !GeneralMethods.isSolid(block))) {
				this.blocks.add(new TempBlock(block, GeneralMethods.getWaterData(7)));
				AFFECTED_BLOCKS.put(block, block);
			}
		}
	}

	@Override
	public void progress() {
		for (final TempBlock tb : this.blocks) {
			AFFECTED_BLOCKS.remove(tb.getBlock());
			tb.revertBlock();
		}
		if (this.player.isDead() || !this.player.isOnline() || !this.bPlayer.canBendIgnoreBindsCooldowns(this)) {
			this.remove();
			return;
		} else if (this.duration != 0 && System.currentTimeMillis() > this.startTime + this.duration) {
			this.bPlayer.addCooldown(this);
			this.remove();
			return;
		} else {
			this.blocks.clear();
			this.player.setFallDistance(0);
			this.player.setSprinting(false);
			if ((new Random()).nextInt(10) == 0) {
				playWaterbendingSound(this.player.getLocation());
			}

			this.player.removePotionEffect(PotionEffectType.SPEED);

			Location location = this.player.getLocation().clone().add(0, .2, 0);
			Block block = location.clone().getBlock();
			final double height = this.spoutableWaterHeight(location);

			if (height != -1) {
				location = this.base.getLocation();
				final double heightRemoveThreshold = 2;
				if (!this.isWithinMaxSpoutHeight(location, heightRemoveThreshold)) {
					this.bPlayer.addCooldown(this);
					this.remove();
					return;
				}
				for (int i = 1; i <= height; i++) {

					block = location.clone().add(0, i, 0).getBlock();

					if (!TempBlock.isTempBlock(block)) {
						this.blocks.add(new TempBlock(block, Material.WATER));
						AFFECTED_BLOCKS.put(block, block);
					}
					this.rotateParticles(block);
				}

				this.displayWaterSpiral(location.clone().add(.5, 0, .5));
				if (this.player.getLocation().getBlockY() > block.getY()) {
					if (this.player.isFlying()) {
						this.player.setFlying(false);
					}
				} else {
					if (!this.player.isFlying()) {
						this.player.setAllowFlight(true);
						this.player.setFlying(true);
					}
				}
			} else {
				this.bPlayer.addCooldown(this);
				this.remove();
				return;
			}
		}
	}

	@Override
	public void remove() {
		super.remove();
		this.revertBaseBlock();
		for (final TempBlock tb : this.blocks) {
			AFFECTED_BLOCKS.remove(tb.getBlock());
			tb.revertBlock();
		}
		this.flightHandler.removeInstance(this.player, this.getName());
	}

	public void revertBaseBlock() {
		if (this.baseBlock != null) {
			this.baseBlock.revertBlock();
			this.baseBlock = null;
		}
	}

	private boolean isWithinMaxSpoutHeight(final Location baseBlockLocation, final double threshold) {
		if (baseBlockLocation == null) {
			return false;
		}
		final double playerHeight = this.player.getLocation().getY();
		if (playerHeight > baseBlockLocation.getY() + this.maxHeight + threshold) {
			return false;
		}
		return true;
	}

	public void rotateParticles(final Block block) {
		if (!this.useParticles) {
			return;
		}

		if (System.currentTimeMillis() >= this.time + this.interval) {
			this.time = System.currentTimeMillis();

			Location location = block.getLocation();
			final Location playerLoc = this.player.getLocation();

			location = new Location(location.getWorld(), playerLoc.getX(), location.getY(), playerLoc.getZ());

			double dy = playerLoc.getY() - block.getY();
			if (dy > this.height) {
				dy = this.height;
			}

			final double[] directions = { -0.5, 0.325, 0.25, 0.125, 0.0, 0.125, 0.25, 0.325, 0.5 };
			int index = this.angle;
			this.angle++;
			if (this.angle >= directions.length) {
				this.angle = 0;
			}
			for (int i = 1; i <= dy; i++) {
				index += 1;
				if (index >= directions.length) {
					index = 0;
				}

				final Location effectLoc2 = new Location(location.getWorld(), location.getX(), block.getY() + i, location.getZ());
				ParticleEffect.WATER_SPLASH.display(effectLoc2, 5, directions[index], directions[index], directions[index]);
			}
		}
	}

	private double spoutableWaterHeight(final Location location) {
		double newHeight = this.height;
		if (isNight(this.player.getWorld())) {
			newHeight = this.getNightFactor(newHeight);
		}

		this.maxHeight = newHeight + 5;
		Block blocki;

		for (int i = 0; i < this.maxHeight; i++) {

			blocki = location.clone().add(0, -i, 0).getBlock();
			if (GeneralMethods.isRegionProtectedFromBuild(this, blocki.getLocation())) {
				return -1;
			}

			if (TempBlock.get(blocki) == null || !this.blocks.contains(TempBlock.get(blocki))) {
				if (isWater(blocki)) {
					if (!TempBlock.isTempBlock(blocki)) {
						this.revertBaseBlock();
					}

					this.base = blocki;
					if (i > newHeight) {
						return newHeight;
					}
					return i;
				}

				if (this.isIcebendable(blocki) || isSnow(blocki)) {
					if (this.isIcebendable(blocki)) {
						if (blocki.getType() == Material.PACKED_ICE && !this.canBendOnPackedIce) {
							this.remove();
							return -1;
						}
					}

					if (!TempBlock.isTempBlock(blocki)) {
						this.revertBaseBlock();
						this.baseBlock = new TempBlock(blocki, Material.WATER);
					}

					this.base = blocki;
					if (i > newHeight) {
						return newHeight;
					}
					return i;
				}

				if ((!ElementalAbility.isAir(blocki.getType()) && (!isPlant(blocki) || !this.bPlayer.canPlantbend()))) {
					this.revertBaseBlock();
					return -1;
				}
			}
		}
		return -1;
	}

	/**
	 * This method was used for the old collision detection system. Please see
	 * {@link Collision} for the new system.
	 */
	@Deprecated
	public static boolean removeSpouts(final Location loc0, final double radius, final Player sourcePlayer) {
		boolean removed = false;
		for (final WaterSpout spout : getAbilities(WaterSpout.class)) {
			if (!spout.player.equals(sourcePlayer)) {
				final Location top = spout.getLocation();
				final Location base = spout.getBase().getLocation();
				final double dist = top.getBlockY() - base.getBlockY();
				for (double d = 0; d <= dist; d += 0.5) {
					final Location spoutLoc = base.clone().add(0, d, 0);
					if (loc0.getWorld().equals(spoutLoc.getWorld()) && loc0.distance(spoutLoc) <= radius) {
						removed = true;
						spout.remove();
					}
				}
			}
		}
		return removed;
	}

	@Override
	public String getName() {
		return "WaterSpout";
	}

	@Override
	public Location getLocation() {
		return this.player != null ? this.player.getLocation() : null;
	}

	@Override
	public long getCooldown() {
		return this.cooldown;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public boolean isCollidable() {
		return true;
	}

	@Override
	public List<Location> getLocations() {
		if (this.getBase() == null) {
			return new ArrayList<>();
		}
		final ArrayList<Location> locations = new ArrayList<>();
		final Location top = this.getLocation();
		final Location iterLoc = this.getBase().getLocation();
		final double ySpacing = 2;
		while (iterLoc.getY() <= top.getY()) {
			locations.add(iterLoc.clone());
			iterLoc.add(0, ySpacing, 0);
		}
		return locations;
	}

	public boolean isCanBendOnPackedIce() {
		return this.canBendOnPackedIce;
	}

	public void setCanBendOnPackedIce(final boolean canBendOnPackedIce) {
		this.canBendOnPackedIce = canBendOnPackedIce;
	}

	public boolean isUseParticles() {
		return this.useParticles;
	}

	public void setUseParticles(final boolean useParticles) {
		this.useParticles = useParticles;
	}

	public boolean isUseBlockSpiral() {
		return this.useBlockSpiral;
	}

	public void setUseBlockSpiral(final boolean useBlockSpiral) {
		this.useBlockSpiral = useBlockSpiral;
	}

	public int getAngle() {
		return this.angle;
	}

	public void setAngle(final int angle) {
		this.angle = angle;
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

	public double getRotation() {
		return this.rotation;
	}

	public void setRotation(final double rotation) {
		this.rotation = rotation;
	}

	public double getHeight() {
		return this.height;
	}

	public void setHeight(final double height) {
		this.height = height;
	}

	public Block getBase() {
		return this.base;
	}

	public void setBase(final Block base) {
		this.base = base;
	}

	public TempBlock getBaseBlock() {
		return this.baseBlock;
	}

	public void setBaseBlock(final TempBlock baseBlock) {
		this.baseBlock = baseBlock;
	}

	public static Map<Block, Block> getAffectedBlocks() {
		return AFFECTED_BLOCKS;
	}

}

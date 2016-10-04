package com.projectkorra.projectkorra.waterbending;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.util.Flight;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

public class WaterSpout extends WaterAbility {

	private static final Map<Block, Block> AFFECTED_BLOCKS = new ConcurrentHashMap<Block, Block>();
	private List<TempBlock> blocks = new ArrayList<TempBlock>();

	private boolean canBendOnPackedIce;
	private boolean useParticles;
	private boolean useBlockSpiral;
	private int angle;
	private long time;
	private long interval;
	private double rotation;
	private double height;
	private double maxHeight;
	private Block base;
	private TempBlock baseBlock;
	private boolean canFly;
	private boolean hadFly;

	public WaterSpout(Player player) {
		super(player);

		WaterSpout oldSpout = getAbility(player, WaterSpout.class);
		if (oldSpout != null) {
			oldSpout.remove();
			return;
		}

		this.canBendOnPackedIce = getConfig().getBoolean("Properties.Water.CanBendPackedIce");
		this.useParticles = getConfig().getBoolean("Abilities.Water.WaterSpout.Particles");
		this.useBlockSpiral = getConfig().getBoolean("Abilities.Water.WaterSpout.BlockSpiral");
		this.height = getConfig().getDouble("Abilities.Water.WaterSpout.Height");
		this.interval = getConfig().getLong("Abilities.Water.WaterSpout.Interval");

		hadFly = player.isFlying();
		canFly = player.getAllowFlight();
		maxHeight = getNightFactor(height);
		WaterSpoutWave spoutWave = new WaterSpoutWave(player, WaterSpoutWave.AbilityType.CLICK);
		if (spoutWave.isStarted() && !spoutWave.isRemoved()) {
			return;
		}

		Block topBlock = GeneralMethods.getTopBlock(player.getLocation(), (int) -getNightFactor(height), (int) -getNightFactor(height));
		if (topBlock == null) {
			topBlock = player.getLocation().getBlock();
		}

		if (!isWater(topBlock) && !isIcebendable(topBlock) && !isSnow(topBlock)) {
			return;
		} else if (topBlock.getType() == Material.PACKED_ICE && !canBendOnPackedIce) {
			return;
		}

		double heightRemoveThreshold = 2;
		if (!isWithinMaxSpoutHeight(topBlock.getLocation(), heightRemoveThreshold)) {
			return;
		}

		new Flight(player);
		player.setAllowFlight(true);
		start();
	}

	private void displayWaterSpiral(Location location) {
		if (!useBlockSpiral) {
			return;
		}

		double maxHeight = player.getLocation().getY() - location.getY() - .5;
		double height = 0;
		rotation += .4;
		int i = 0;

		while (height < maxHeight) {
			i += 20;
			height += .4;
			double angle = (i * Math.PI / 180);
			double x = 1 * Math.cos(angle + rotation);
			double z = 1 * Math.sin(angle + rotation);

			Location loc = location.clone().getBlock().getLocation().add(.5, .5, .5);
			loc.add(x, height, z);

			Block block = loc.getBlock();
			if ((!TempBlock.isTempBlock(block)) && (block.getType().equals(Material.AIR) || !GeneralMethods.isSolid(block))) {
				blocks.add(new TempBlock(block, Material.STATIONARY_WATER, (byte) 1));
				AFFECTED_BLOCKS.put(block, block);
			}
		}
	}

	@Override
	public void progress() {
		for (TempBlock tb : blocks) {
			AFFECTED_BLOCKS.remove(tb.getBlock());
			tb.revertBlock();
		}
		if (player.isDead() || !player.isOnline() || !bPlayer.canBendIgnoreBindsCooldowns(this)) {
			remove();
			return;
		} else {
			blocks.clear();
			player.setFallDistance(0);
			player.setSprinting(false);
			if ((new Random()).nextInt(10) == 0) {
				playWaterbendingSound(player.getLocation());
			}

			player.removePotionEffect(PotionEffectType.SPEED);

			Location location = player.getLocation().clone().add(0, .2, 0);
			Block block = location.clone().getBlock();
			double height = spoutableWaterHeight(location);

			if (height != -1) {
				location = base.getLocation();
				double heightRemoveThreshold = 2;
				if (!isWithinMaxSpoutHeight(location, heightRemoveThreshold)) {
					remove();
					return;
				}
				for (int i = 1; i <= height; i++) {

					block = location.clone().add(0, i, 0).getBlock();

					if (!TempBlock.isTempBlock(block)) {
						blocks.add(new TempBlock(block, Material.STATIONARY_WATER, (byte) 8));
						AFFECTED_BLOCKS.put(block, block);
					}
					rotateParticles(block);
				}

				displayWaterSpiral(location.clone().add(.5, 0, .5));
				if (player.getLocation().getBlockY() > block.getY()) {
					player.setFlying(false);
				} else {
					new Flight(player);
					player.setAllowFlight(true);
					player.setFlying(true);
				}
			} else {
				remove();
				return;
			}
		}
	}

	@Override
	public void remove() {
		super.remove();
		revertBaseBlock();
		for (TempBlock tb : blocks) {
			AFFECTED_BLOCKS.remove(tb.getBlock());
			tb.revertBlock();
		}
		player.setAllowFlight(canFly);
		player.setFlying(hadFly);
	}

	public void revertBaseBlock() {
		if (baseBlock != null) {
			baseBlock.revertBlock();
			baseBlock = null;
		}
	}

	private boolean isWithinMaxSpoutHeight(Location baseBlockLocation, double threshold) {
		if (baseBlockLocation == null) {
			return false;
		}
		double playerHeight = player.getLocation().getY();
		if (playerHeight > baseBlockLocation.getY() + maxHeight + threshold) {
			return false;
		}
		return true;
	}

	public void rotateParticles(Block block) {
		if (!useParticles) {
			return;
		}

		if (System.currentTimeMillis() >= time + interval) {
			time = System.currentTimeMillis();

			Location location = block.getLocation();
			Location playerLoc = player.getLocation();

			location = new Location(location.getWorld(), playerLoc.getX(), location.getY(), playerLoc.getZ());

			double dy = playerLoc.getY() - block.getY();
			if (dy > height) {
				dy = height;
			}

			float[] directions = { -0.5f, 0.325f, 0.25f, 0.125f, 0.f, 0.125f, 0.25f, 0.325f, 0.5f };
			int index = angle;
			angle++;
			if (angle >= directions.length) {
				angle = 0;
			}
			for (int i = 1; i <= dy; i++) {
				index += 1;
				if (index >= directions.length) {
					index = 0;
				}

				Location effectLoc2 = new Location(location.getWorld(), location.getX(), block.getY() + i, location.getZ());
				ParticleEffect.WATER_SPLASH.display(effectLoc2, directions[index], directions[index], directions[index], 5, (int) (height + 5));
			}
		}
	}

	private double spoutableWaterHeight(Location location) {
		double newHeight = height;
		if (isNight(player.getWorld())) {
			newHeight = getNightFactor(newHeight);
		}

		this.maxHeight = newHeight + 5;
		Block blocki;

		for (int i = 0; i < maxHeight; i++) {

			blocki = location.clone().add(0, -i, 0).getBlock();
			if (GeneralMethods.isRegionProtectedFromBuild(this, blocki.getLocation())) {
				return -1;
			}

			if (!blocks.contains(blocki)) {
				if (isWater(blocki)) {
					if (!TempBlock.isTempBlock(blocki)) {
						revertBaseBlock();
					}

					base = blocki;
					if (i > newHeight) {
						return newHeight;
					}
					return i;
				}

				if (isIcebendable(blocki) || isSnow(blocki)) {
					if (isIcebendable(blocki)) {
						if (blocki.getType() == Material.PACKED_ICE && !canBendOnPackedIce) {
							remove();
							return -1;
						}
					}

					if (!TempBlock.isTempBlock(blocki)) {
						revertBaseBlock();
						baseBlock = new TempBlock(blocki, Material.STATIONARY_WATER, (byte) 8);
					}

					base = blocki;
					if (i > newHeight) {
						return newHeight;
					}
					return i;
				}

				if ((blocki.getType() != Material.AIR && (!isPlant(blocki) || !bPlayer.canPlantbend()))) {
					revertBaseBlock();
					return -1;
				}
			}
		}
		return -1;
	}

	public static boolean removeSpouts(Location loc0, double radius, Player sourcePlayer) {
		boolean removed = false;
		for (WaterSpout spout : getAbilities(WaterSpout.class)) {
			if (!spout.player.equals(sourcePlayer)) {
				Location top = spout.getLocation();
				Location base = spout.getBase().getLocation();
				double dist = top.getBlockY() - base.getBlockY();
				for (double d = 0; d <= dist; d += 0.5) {
					Location spoutl = base.clone().add(0, d, 0);
					if (loc0.distance(spoutl) <= radius) {
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
		return player != null ? player.getLocation() : null;
	}

	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	public boolean isCanBendOnPackedIce() {
		return canBendOnPackedIce;
	}

	public void setCanBendOnPackedIce(boolean canBendOnPackedIce) {
		this.canBendOnPackedIce = canBendOnPackedIce;
	}

	public boolean isUseParticles() {
		return useParticles;
	}

	public void setUseParticles(boolean useParticles) {
		this.useParticles = useParticles;
	}

	public boolean isUseBlockSpiral() {
		return useBlockSpiral;
	}

	public void setUseBlockSpiral(boolean useBlockSpiral) {
		this.useBlockSpiral = useBlockSpiral;
	}

	public int getAngle() {
		return angle;
	}

	public void setAngle(int angle) {
		this.angle = angle;
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

	public double getRotation() {
		return rotation;
	}

	public void setRotation(double rotation) {
		this.rotation = rotation;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public Block getBase() {
		return base;
	}

	public void setBase(Block base) {
		this.base = base;
	}

	public TempBlock getBaseBlock() {
		return baseBlock;
	}

	public void setBaseBlock(TempBlock baseBlock) {
		this.baseBlock = baseBlock;
	}

	public static Map<Block, Block> getAffectedBlocks() {
		return AFFECTED_BLOCKS;
	}

}
package com.projectkorra.projectkorra.earthbending;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.configuration.better.configs.abilities.earth.EarthTunnelConfig;
import com.projectkorra.projectkorra.util.TempBlock;

public class EarthTunnel extends EarthAbility<EarthTunnelConfig> {

	private long interval;
	private int blocksPerInterval;
	private long time;
	@Attribute("Depth")
	private double depth;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	private long revertTime;
	private double radius;
	private double angle;
	@Attribute(Attribute.RADIUS)
	private double maxRadius;
	@Attribute(Attribute.RANGE)
	private double range;
	private double radiusIncrement;
	private boolean revert;
	private boolean dropLootIfNotRevert;
	private boolean ignoreOres;
	private Block block;
	private Location origin;
	private Location location;
	private Vector direction;

	public EarthTunnel(final EarthTunnelConfig config, final Player player) {
		super(config, player);

		this.cooldown = config.Cooldown;
		this.maxRadius = config.MaxRadius;
		this.range = config.Range;
		this.radius = config.InitialRadius;
		this.interval = config.Interval;
		this.blocksPerInterval = config.BlocksPerInterval;
		this.revert = config.Revert;
		this.dropLootIfNotRevert = config.DropLootIfNotRevert;
		this.ignoreOres = config.IgnoreOres;
		this.revertTime = config.RevertCheckTime;

		this.time = System.currentTimeMillis();

		this.location = player.getEyeLocation().clone();
		this.origin = player.getTargetBlock((HashSet<Material>) null, (int) this.range).getLocation();
		this.block = this.origin.getBlock();
		this.direction = this.location.getDirection().clone().normalize();
		this.depth = 0;
		if (this.origin.getWorld().equals(this.location.getWorld())) {
			this.depth = Math.max(0, this.origin.distance(this.location) - 1);
		}
		this.angle = 0;

		if (!this.bPlayer.canBend(this) || (!EarthAbility.isEarthbendable(player, this.block) && !ElementalAbility.isTransparent(player, "EarthTunnel", this.block))) {
			return;
		}
		if (GeneralMethods.isRegionProtectedFromBuild(this, this.block.getLocation())) {
			return;
		}
		if (this.bPlayer.isAvatarState()) {
			this.maxRadius = config.AvatarState_MaxRadius;
		}

		this.radiusIncrement = this.radius;

		this.start();
	}

	@Override
	public void progress() {
		if (!this.bPlayer.canBend(this)) {
			this.bPlayer.addCooldown(this);
			this.remove();
			return;
		}

		if (System.currentTimeMillis() - this.time >= this.interval) {
			this.time = System.currentTimeMillis();
			for (int i = 1; i <= this.blocksPerInterval; i++) {
				if (Math.abs(Math.toDegrees(this.player.getEyeLocation().getDirection().angle(this.direction))) > 20 || !this.player.isSneaking()) {
					this.bPlayer.addCooldown(this);
					this.remove();
					return;
				} else {
					while ((!isEarth(this.block) && !isSand(this.block)) || (this.ignoreOres && this.isOre(this.block))) {
						if (!this.isTransparent(this.block) && (this.ignoreOres && !this.isOre(this.block))) {
							this.remove();
							return;
						}

						if (this.angle >= 360) {
							this.angle = 0;
							if (this.radius >= this.maxRadius) {
								this.radius = this.radiusIncrement;
								if (this.depth >= this.range) {
									this.bPlayer.addCooldown(this);
									this.remove();
									return;
								} else {
									this.depth += 0.5;
								}
							} else {
								this.radius += this.radiusIncrement;
							}
						} else {
							this.angle += 20;
						}

						final Vector vec = GeneralMethods.getOrthogonalVector(this.direction, this.angle, this.radius);
						this.block = this.location.clone().add(this.direction.clone().normalize().multiply(this.depth)).add(vec).getBlock();
					}

					if (GeneralMethods.isRegionProtectedFromBuild(this, this.block.getLocation())) {
						this.bPlayer.addCooldown(this);
						this.remove();
						return;
					}

					if (this.revert) {
						if (getMovedEarth().containsKey(this.block)) {
							this.block.setType(Material.AIR);
						} else {
							new TempBlock(this.block, Material.AIR).setRevertTime(revertTime);
							if (isPlant(this.block.getRelative(BlockFace.UP)) || isSnow(this.block.getRelative(BlockFace.UP))) {
								final Block above = this.block.getRelative(BlockFace.UP);
								final Block above2 = above.getRelative(BlockFace.UP);
								if (isPlant(above) || isSnow(above)) {
									new TempBlock(above, Material.AIR).setRevertTime(revertTime);
									if (isPlant(above2) && above2.getType().equals(Material.TALL_GRASS)) {
										new TempBlock(above2, Material.AIR).setRevertTime(revertTime);
									}
								}
							}
						}
					} else {
						if (this.dropLootIfNotRevert) {
							this.block.breakNaturally();
						} else {
							this.block.setType(Material.AIR);
						}
					}
				}
			}
		}
	}

	private boolean isOre(final Block block) {
		switch (block.getType()) {
			case COAL_ORE:
			case IRON_ORE:
			case GOLD_ORE:
			case LAPIS_ORE:
			case REDSTONE_ORE:
			case DIAMOND_ORE:
			case EMERALD_ORE:
			case NETHER_QUARTZ_ORE:
				return true;
			default: 
				return false;
		}
	}

	@Override
	public String getName() {
		return "EarthTunnel";
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

	public long getInterval() {
		return this.interval;
	}

	public void setInterval(final long interval) {
		this.interval = interval;
	}

	public long getTime() {
		return this.time;
	}

	public void setTime(final long time) {
		this.time = time;
	}

	public double getDepth() {
		return this.depth;
	}

	public void setDepth(final double depth) {
		this.depth = depth;
	}

	public double getRadius() {
		return this.radius;
	}

	public void setRadius(final double radius) {
		this.radius = radius;
	}

	public double getAngle() {
		return this.angle;
	}

	public void setAngle(final double angle) {
		this.angle = angle;
	}

	public double getMaxRadius() {
		return this.maxRadius;
	}

	public void setMaxRadius(final double maxRadius) {
		this.maxRadius = maxRadius;
	}

	public double getRange() {
		return this.range;
	}

	public void setRange(final double range) {
		this.range = range;
	}

	public double getRadiusIncrement() {
		return this.radiusIncrement;
	}

	public void setRadiusIncrement(final double radiusIncrement) {
		this.radiusIncrement = radiusIncrement;
	}

	public Block getBlock() {
		return this.block;
	}

	public void setBlock(final Block block) {
		this.block = block;
	}

	public Location getOrigin() {
		return this.origin;
	}

	public void setOrigin(final Location origin) {
		this.origin = origin;
	}

	public Vector getDirection() {
		return this.direction;
	}

	public void setDirection(final Vector direction) {
		this.direction = direction;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

}

package com.projectkorra.projectkorra.waterbending;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.util.WaterReturn;

public class TorrentWave extends WaterAbility {

	private long time;
	private long interval;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	private double radius;
	@Attribute(Attribute.RADIUS)
	private double maxRadius;
	@Attribute(Attribute.KNOCKBACK)
	private double knockback;
	@Attribute(Attribute.HEIGHT)
	private double maxHeight;
	@Attribute("Grow" + Attribute.SPEED)
	private double growSpeed;
	private Location origin;
	private ArrayList<TempBlock> blocks;
	private ArrayList<Entity> affectedEntities;
	private Map<Integer, ConcurrentHashMap<Integer, Double>> heights;

	public TorrentWave(final Player player, final double radius) {
		this(player, player.getEyeLocation(), radius);
	}

	public TorrentWave(final Player player, final Location location, final double radius) {
		super(player);

		if (this.bPlayer.isOnCooldown("TorrentWave")) {
			return;
		}

		this.radius = radius;
		this.interval = getConfig().getLong("Abilities.Water.Torrent.Wave.Interval");
		this.maxHeight = applyModifiers(getConfig().getDouble("Abilities.Water.Torrent.Wave.Height"));
		this.maxRadius = applyModifiers(getConfig().getDouble("Abilities.Water.Torrent.Wave.Radius"));
		this.knockback = applyModifiers(getConfig().getDouble("Abilities.Water.Torrent.Wave.Knockback"));
		this.cooldown = applyInverseModifiers(getConfig().getLong("Abilities.Water.Torrent.Wave.Cooldown"));
		this.growSpeed = applyModifiers(getConfig().getDouble("Abilities.Water.Torrent.Wave.GrowSpeed"));
		this.origin = location.clone();
		this.time = System.currentTimeMillis();
		this.heights = new ConcurrentHashMap<>();
		this.blocks = new ArrayList<>();
		this.affectedEntities = new ArrayList<>();

		//this.knockback = this.getNightFactor(this.knockback);
		//this.maxRadius = this.getNightFactor(this.maxRadius);

		this.initializeHeightsMap();
		this.start();
		this.bPlayer.addCooldown("TorrentWave", this.cooldown);
	}

	private void initializeHeightsMap() {
		for (int i = -1; i <= this.maxHeight; i++) {
			final ConcurrentHashMap<Integer, Double> angles = new ConcurrentHashMap<>();
			final double dtheta = Math.toDegrees(1 / (this.maxRadius + 2));
			int j = 0;

			for (double theta = 0; theta < 360; theta += dtheta) {
				angles.put(j, theta);
				j++;
			}
			this.heights.put(i, angles);
		}
	}

	@Override
	public void progress() {
		if (!this.bPlayer.canBendIgnoreBindsCooldowns(this)) {
			this.remove();
			return;

		}

		if (System.currentTimeMillis() > this.time + this.interval) {
			if (this.radius < this.maxRadius) {
				this.radius += this.growSpeed;
			} else {
				this.remove();
				this.returnWater();
				return;
			}
			this.formBurst();
			this.time = System.currentTimeMillis();
		}
	}

	private void formBurst() {
		for (final TempBlock tempBlock : this.blocks) {
			tempBlock.revertBlock();
		}

		this.blocks.clear();
		this.affectedEntities.clear();

		final ArrayList<Entity> indexList = new ArrayList<>(GeneralMethods.getEntitiesAroundPoint(this.origin, this.radius + 2));
		final ArrayList<Block> torrentBlocks = new ArrayList<>();

		indexList.remove(this.player);

		for (final int id : this.heights.keySet()) {
			final ConcurrentHashMap<Integer, Double> angles = this.heights.get(id);
			for (final int index : angles.keySet()) {
				final double angle = angles.get(index);
				final double theta = Math.toRadians(angle);
				final double dx = Math.cos(theta) * this.radius;
				final double dy = id;
				final double dz = Math.sin(theta) * this.radius;

				final Location location = this.origin.clone().add(dx, dy, dz);
				final Block block = location.getBlock();

				if (torrentBlocks.contains(block)) {
					continue;
				}

				if (isTransparent(this.player, block)) {
					final TempBlock tempBlock = new TempBlock(block, Material.WATER);
					this.blocks.add(tempBlock);
					torrentBlocks.add(block);
				} else {
					angles.remove(index);
					continue;
				}

				for (final Entity entity : indexList) {
					if (!this.affectedEntities.contains(entity)) {
						if (entity.getLocation().distanceSquared(location) <= 4) {
							if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation()) || ((entity instanceof Player) && Commands.invincible.contains(((Player) entity).getName()))) {
								continue;
							}
							this.affectedEntities.add(entity);
							this.affect(entity);
						}
					}
				}

				final Random random = new Random();
				for (final Block sound : torrentBlocks) {
					if (random.nextInt(50) == 0) {
						playWaterbendingSound(sound.getLocation());
					}
				}
			}
			if (angles.isEmpty()) {
				this.heights.remove(id);
			}
		}
		if (this.heights.isEmpty()) {
			this.remove();
		}
	}

	private void affect(final Entity entity) {
		final Vector direction = GeneralMethods.getDirection(this.origin, entity.getLocation());
		direction.setY(0);
		direction.normalize();
		GeneralMethods.setVelocity(this, entity, entity.getVelocity().clone().add(direction.multiply(this.knockback)));
	}

	@Override
	public void remove() {
		super.remove();
		for (final TempBlock block : this.blocks) {
			block.revertBlock();
		}
	}

	private void returnWater() {
		final Location location = new Location(this.origin.getWorld(), this.origin.getX() + this.radius, this.origin.getY(), this.origin.getZ());
		if (!location.getWorld().equals(this.player.getWorld())) {
			return;
		}
		final double radiusOffsetSquared = (this.maxRadius + 5) * (this.maxRadius + 5);
		if (location.distanceSquared(this.player.getLocation()) > radiusOffsetSquared) {
			return;
		}
		new WaterReturn(this.player, location.getBlock());
	}

	@Override
	public String getName() {
		return "Torrent";
	}

	@Override
	public Location getLocation() {
		return this.origin;
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
	public List<Location> getLocations() {
		final ArrayList<Location> locations = new ArrayList<>();
		for (final TempBlock tblock : this.blocks) {
			locations.add(tblock.getLocation());
		}
		return locations;
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

	public double getKnockback() {
		return this.knockback;
	}

	public void setKnockback(final double knockback) {
		this.knockback = knockback;
	}

	public double getMaxHeight() {
		return this.maxHeight;
	}

	public void setMaxHeight(final double maxHeight) {
		this.maxHeight = maxHeight;
	}

	public double getGrowSpeed() {
		return this.growSpeed;
	}

	public void setGrowSpeed(final double growSpeed) {
		this.growSpeed = growSpeed;
	}

	public Location getOrigin() {
		return this.origin;
	}

	public void setOrigin(final Location origin) {
		this.origin = origin;
	}

	public ArrayList<TempBlock> getBlocks() {
		return this.blocks;
	}

	public ArrayList<Entity> getAffectedEntities() {
		return this.affectedEntities;
	}

	public Map<Integer, ConcurrentHashMap<Integer, Double>> getHeights() {
		return this.heights;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

}

package com.projectkorra.projectkorra.firebending;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.waterbending.plant.PlantRegrowth;

public class BlazeArc extends FireAbility {

	private static final long DISSIPATE_REMOVE_TIME = 400;
	private static final Material[] OVERWRITABLE_MATERIALS = { Material.SAPLING, Material.LONG_GRASS, Material.DEAD_BUSH, Material.YELLOW_FLOWER, Material.RED_ROSE, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.FIRE, Material.SNOW, Material.TORCH };
	private static final Map<Block, Player> IGNITED_BLOCKS = new ConcurrentHashMap<Block, Player>();
	private static final Map<Block, Long> IGNITED_TIMES = new ConcurrentHashMap<Block, Long>();
	private static final Map<Location, MaterialData> REPLACED_BLOCKS = new ConcurrentHashMap<Location, MaterialData>();

	private long time;
	private long interval;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute(Attribute.SPEED)
	private double speed;
	private Location origin;
	private Location location;
	private Vector direction;

	public BlazeArc(final Player player, final Location location, final Vector direction, final double range) {
		super(player);
		this.range = this.getDayFactor(range);
		this.speed = getConfig().getLong("Abilities.Fire.Blaze.Speed");
		this.interval = (long) (1000. / this.speed);
		this.origin = location.clone();
		this.location = this.origin.clone();

		this.direction = direction.clone();
		this.direction.setY(0);
		this.direction = this.direction.clone().normalize();
		this.location = this.location.clone().add(this.direction);

		this.time = System.currentTimeMillis();
		this.start();
	}

	private void ignite(final Block block) {
		if (block.getType() != Material.FIRE && block.getType() != Material.AIR) {
			if (canFireGrief()) {
				if (isPlant(block) || isSnow(block)) {
					new PlantRegrowth(this.player, block);
				}
			} else if (block.getType() != Material.FIRE) {
				REPLACED_BLOCKS.put(block.getLocation(), block.getState().getData());
			}
		}

		block.setType(Material.FIRE);
		IGNITED_BLOCKS.put(block, this.player);
		IGNITED_TIMES.put(block, System.currentTimeMillis());
	}

	@Override
	public void progress() {
		if (!this.bPlayer.canBendIgnoreBindsCooldowns(this)) {
			this.remove();
			return;
		} else if (System.currentTimeMillis() - this.time >= this.interval) {
			this.location = this.location.clone().add(this.direction);
			this.time = System.currentTimeMillis();

			final Block block = this.location.getBlock();
			if (block.getType() == Material.FIRE) {
				return;
			}

			if (this.location.distanceSquared(this.origin) > this.range * this.range) {
				this.remove();
				return;
			} else if (GeneralMethods.isRegionProtectedFromBuild(this, this.location)) {
				return;
			}

			if (isIgnitable(this.player, block)) {
				this.ignite(block);
			} else if (isIgnitable(this.player, block.getRelative(BlockFace.DOWN))) {
				this.ignite(block.getRelative(BlockFace.DOWN));
				this.location = block.getRelative(BlockFace.DOWN).getLocation();
			} else if (isIgnitable(this.player, block.getRelative(BlockFace.UP))) {
				this.ignite(block.getRelative(BlockFace.UP));
				this.location = block.getRelative(BlockFace.UP).getLocation();
			} else {
				this.remove();
				return;
			}
		}
	}

	public static void dissipateAll() {
		if (DISSIPATE_REMOVE_TIME != 0) {
			for (final Block block : IGNITED_TIMES.keySet()) {
				if (block.getType() != Material.FIRE) {
					removeBlock(block);
				} else {
					final long time = IGNITED_TIMES.get(block);
					if (System.currentTimeMillis() > time + DISSIPATE_REMOVE_TIME) {
						block.setType(Material.AIR);
						removeBlock(block);
					}
				}
			}
		}
	}

	public static void handleDissipation() {
		for (final Block block : IGNITED_BLOCKS.keySet()) {
			if (block.getType() != Material.FIRE) {
				IGNITED_BLOCKS.remove(block);
			}
		}
	}

	public static boolean isIgnitable(final Player player, final Block block) {
		if (block.getType() == Material.FIRE) {
			return true;
		} else if (Arrays.asList(OVERWRITABLE_MATERIALS).contains(block.getType())) {
			return true;
		} else if (block.getType() != Material.AIR) {
			return false;
		}

		final Block belowBlock = block.getRelative(BlockFace.DOWN);
		return isIgnitable(belowBlock);
	}

	public static void removeAllCleanup() {
		for (final Block block : IGNITED_BLOCKS.keySet()) {
			removeBlock(block);
		}
	}

	public static void removeAroundPoint(final Location location, final double radius) {
		for (final BlazeArc stream : getAbilities(BlazeArc.class)) {
			if (stream.location.getWorld().equals(location.getWorld())) {
				if (stream.location.distanceSquared(location) <= radius * radius) {
					stream.remove();
				}
			}
		}
	}

	public static void removeBlock(final Block block) {
		if (IGNITED_BLOCKS.containsKey(block)) {
			IGNITED_BLOCKS.remove(block);
		}
		if (IGNITED_TIMES.containsKey(block)) {
			IGNITED_TIMES.remove(block);
		}
		if (REPLACED_BLOCKS.containsKey(block.getLocation())) {
			block.setType(REPLACED_BLOCKS.get(block.getLocation()).getItemType());
			block.setData(REPLACED_BLOCKS.get(block.getLocation()).getData());
			REPLACED_BLOCKS.remove(block.getLocation());
		}
	}

	@Override
	public String getName() {
		return "Blaze";
	}

	@Override
	public Location getLocation() {
		if (this.location != null) {
			return this.location;
		}
		return this.origin;
	}

	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
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

	public double getRange() {
		return this.range;
	}

	public void setRange(final double range) {
		this.range = range;
	}

	public double getSpeed() {
		return this.speed;
	}

	public void setSpeed(final double speed) {
		this.speed = speed;
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

	public static long getDissipateRemoveTime() {
		return DISSIPATE_REMOVE_TIME;
	}

	public static Material[] getOverwritableMaterials() {
		return OVERWRITABLE_MATERIALS;
	}

	public static Map<Block, Player> getIgnitedBlocks() {
		return IGNITED_BLOCKS;
	}

	public static Map<Block, Long> getIgnitedTimes() {
		return IGNITED_TIMES;
	}

	public static Map<Location, MaterialData> getReplacedBlocks() {
		return REPLACED_BLOCKS;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

}

package com.projectkorra.projectkorra.firebending;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.configuration.better.configs.abilities.fire.BlazeConfig;
import com.projectkorra.projectkorra.waterbending.plant.PlantRegrowth;

public class BlazeArc extends FireAbility<BlazeConfig> {

	private static final long DISSIPATE_REMOVE_TIME = 400;
	private static final Map<Block, Player> IGNITED_BLOCKS = new ConcurrentHashMap<>();
	private static final Map<Block, Long> IGNITED_TIMES = new ConcurrentHashMap<>();
	private static final Map<Location, BlockState> REPLACED_BLOCKS = new ConcurrentHashMap<>();

	private long time;
	private long interval;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute(Attribute.SPEED)
	private double speed;
	private Location origin;
	private Location location;
	private Vector direction;

	public BlazeArc(final BlazeConfig config, final Player player, final Location location, final Vector direction, final double range) {
		super(config, player);
		this.range = this.getDayFactor(range);
		this.speed = config.Speed;
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
		if (block.getType() != Material.FIRE && !isAir(block.getType())) {
			if (canFireGrief()) {
				if (isPlant(block) || isSnow(block)) {
					new PlantRegrowth(this.player, block);
				}
			} else if (block.getType() != Material.FIRE) {
				REPLACED_BLOCKS.put(block.getLocation(), block.getState());
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

			final Block ignitable = getIgnitable(block);
			if (ignitable != null) {
				this.ignite(ignitable);
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

	public static Block getIgnitable(final Block block) {
		Block top = block;

		for (int i = 0; i < 2; i++) {
			if (GeneralMethods.isSolid(top.getRelative(BlockFace.DOWN))) {
				break;
			}

			top = top.getRelative(BlockFace.DOWN);
		}

		if (top.getType() == Material.FIRE) {
			return top;
		} else if (top.getType().isBurnable()) {
			return top;
		} else if (isAir(top.getType())) {
			return top;
		} else {
			return null;
		}
	}

	public static boolean isIgnitable(final Player player, final Block block) {
		if (!BendingPlayer.getBendingPlayer(player).hasElement(Element.FIRE)) {
			return false;
		} else if (!GeneralMethods.isSolid(block.getRelative(BlockFace.DOWN))) {
			return false;
		} else if (block.getType() == Material.FIRE) {
			return true;
		} else if (block.getType().isBurnable()) {
			return true;
		} else if (isAir(block.getType())) {
			return true;
		} else {
			return false;
		}
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
			block.setType(REPLACED_BLOCKS.get(block.getLocation()).getType());
			block.setBlockData(REPLACED_BLOCKS.get(block.getLocation()).getBlockData());
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

	public static Map<Block, Player> getIgnitedBlocks() {
		return IGNITED_BLOCKS;
	}

	public static Map<Block, Long> getIgnitedTimes() {
		return IGNITED_TIMES;
	}

	public static Map<Location, BlockState> getReplacedBlocks() {
		return REPLACED_BLOCKS;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

}

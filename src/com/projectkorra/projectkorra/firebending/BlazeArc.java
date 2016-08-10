package com.projectkorra.projectkorra.firebending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.waterbending.PlantRegrowth;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class BlazeArc extends FireAbility {
	
	private static final long DISSIPATE_REMOVE_TIME = 400;
	private static final Material[] OVERWRITABLE_MATERIALS = { Material.SAPLING, Material.LONG_GRASS, Material.DEAD_BUSH, 
			Material.YELLOW_FLOWER, Material.RED_ROSE, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, 
			Material.FIRE, Material.SNOW, Material.TORCH };
	private static final ConcurrentHashMap<Block, Player> IGNITED_BLOCKS = new ConcurrentHashMap<Block, Player>();
	private static final ConcurrentHashMap<Block, Long> IGNITED_TIMES = new ConcurrentHashMap<Block, Long>();
	private static final ConcurrentHashMap<Location, MaterialData> REPLACED_BLOCKS = new ConcurrentHashMap<Location, MaterialData>();
	
	private long time;
	private long interval;
	private double range;
	private double speed;
	private Location origin;
	private Location location;
	private Vector direction;
	
	public BlazeArc(Player player, Location location, Vector direction, double range) {
		super(player);
		this.range = getDayFactor(range);
		this.speed = getConfig().getLong("Abilities.Fire.Blaze.Speed");
		this.interval = (long) (1000. / speed);
		this.origin = location.clone();
		this.location = origin.clone();
		
		this.direction = direction.clone();
		this.direction.setY(0);
		this.direction = this.direction.clone().normalize();
		this.location = this.location.clone().add(this.direction);
		
		this.time = System.currentTimeMillis();
		start();
	}

	private void ignite(Block block) {
		if (block.getType() != Material.FIRE && block.getType() != Material.AIR) {
			if (canFireGrief()) {
				if (isPlant(block) || isSnow(block)) {
					new PlantRegrowth(player, block);
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
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			remove();
			return;
		} else if (System.currentTimeMillis() - time >= interval) {
			location = location.clone().add(direction);
			time = System.currentTimeMillis();
			
			Block block = location.getBlock();
			if (block.getType() == Material.FIRE) {
				return;
			}
			
			if (location.distanceSquared(origin) > range * range) {
				remove();
				return;
			} else if (GeneralMethods.isRegionProtectedFromBuild(this, location)) {
				return;
			}
			
			if (isIgnitable(player, block)) {
				ignite(block);
			} else if (isIgnitable(player, block.getRelative(BlockFace.DOWN))) {
				ignite(block.getRelative(BlockFace.DOWN));
				location = block.getRelative(BlockFace.DOWN).getLocation();
			} else if (isIgnitable(player, block.getRelative(BlockFace.UP))) {
				ignite(block.getRelative(BlockFace.UP));
				location = block.getRelative(BlockFace.UP).getLocation();
			} else {
				remove();
				return;
			}
		}
	}

	public static void dissipateAll() {
		if (DISSIPATE_REMOVE_TIME != 0) {
			for (Block block : IGNITED_TIMES.keySet()) {
				if (block.getType() != Material.FIRE) {
					removeBlock(block);
				} else {
					long time = IGNITED_TIMES.get(block);
					if (System.currentTimeMillis() > time + DISSIPATE_REMOVE_TIME) {
						block.setType(Material.AIR);
						removeBlock(block);
					}
				}
			}
		}
	}

	public static void handleDissipation() {
		for (Block block : IGNITED_BLOCKS.keySet()) {
			if (block.getType() != Material.FIRE) {
				IGNITED_BLOCKS.remove(block);
			}
		}
	}

	public static boolean isIgnitable(Player player, Block block) {
		if (block.getType() == Material.FIRE) {
			return true;
		} else if (Arrays.asList(OVERWRITABLE_MATERIALS).contains(block.getType())) {
			return true;
		} else if (block.getType() != Material.AIR) {
			return false;
		}

		Block belowBlock = block.getRelative(BlockFace.DOWN);
		return isIgnitable(belowBlock);
	}

	public static void removeAllCleanup() {
		for (Block block : IGNITED_BLOCKS.keySet()) {
			removeBlock(block);
		}
	}

	public static void removeAroundPoint(Location location, double radius) {
		for (BlazeArc stream : getAbilities(BlazeArc.class)) {
			if (stream.location.getWorld().equals(location.getWorld())) {
				if (stream.location.distanceSquared(location) <= radius * radius) {
					stream.remove();
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public static void removeBlock(Block block) {
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
		if (location != null) {
			return location;
		}
		return origin;
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

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public Location getOrigin() {
		return origin;
	}

	public void setOrigin(Location origin) {
		this.origin = origin;
	}

	public Vector getDirection() {
		return direction;
	}

	public void setDirection(Vector direction) {
		this.direction = direction;
	}

	public static long getDissipateRemoveTime() {
		return DISSIPATE_REMOVE_TIME;
	}

	public static Material[] getOverwritableMaterials() {
		return OVERWRITABLE_MATERIALS;
	}

	public static ConcurrentHashMap<Block, Player> getIgnitedBlocks() {
		return IGNITED_BLOCKS;
	}

	public static ConcurrentHashMap<Block, Long> getIgnitedTimes() {
		return IGNITED_TIMES;
	}

	public static ConcurrentHashMap<Location, MaterialData> getReplacedBlocks() {
		return REPLACED_BLOCKS;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	
}

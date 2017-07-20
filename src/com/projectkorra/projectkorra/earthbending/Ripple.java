package com.projectkorra.projectkorra.earthbending;

import java.util.ArrayList;
import java.util.Map;
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
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.util.DamageHandler;

public class Ripple extends EarthAbility {

	private static final Map<Integer[], Block> BLOCKS = new ConcurrentHashMap<Integer[], Block>();

	private int step;
	private int maxStep;
	private double range;
	private double damage;
	private double knockback;
	private Vector direction;
	private Location origin;
	private Location location;
	private Block block1;
	private Block block2;
	private Block block3;
	private Block block4;
	private ArrayList<Location> locations = new ArrayList<Location>();
	private ArrayList<Entity> entities = new ArrayList<Entity>();

	public Ripple(Player player, Vector direction) {
		super(player);
		initialize(player, getInitialLocation(player, direction), direction);
	}

	public Ripple(Player player, Location origin, Vector direction) {
		super(player);
		initialize(player, origin, direction);
	}

	private void initialize(Player player, Location origin, Vector direction) {
		if (origin == null) {
			return;
		}

		this.range = getConfig().getDouble("Abilities.Earth.Shockwave.Range");
		this.damage = getConfig().getDouble("Abilities.Earth.Shockwave.Damage");
		this.knockback = getConfig().getDouble("Abilities.Earth.Shockwave.Knockback");
		this.direction = direction.clone().normalize();
		this.origin = origin.clone();
		this.location = origin.clone();
		this.locations = new ArrayList<>();
		this.entities = new ArrayList<>();

		if (bPlayer.isAvatarState()) {
			range = getConfig().getDouble("Abilities.Avatar.AvatarState.Earth.Shockwave.Range");
			damage = getConfig().getDouble("Abilities.Avatar.AvatarState.Earth.Shockwave.Damage");
			knockback = getConfig().getDouble("Abilities.Avatar.AvatarState.Earth.Shockwave.Knockback");
		}

		initializeLocations();
		maxStep = locations.size();

		if (isEarthbendable(origin.getBlock())) {
			start();
		}
	}

	private Location getInitialLocation(Player player, Vector direction) {
		Location location = player.getLocation().clone().add(0, -1, 0);
		direction = direction.normalize();
		Block block1 = location.getBlock();

		while (location.getBlock().equals(block1)) {
			location = location.clone().add(direction);
		}

		for (int i : new int[] { 1, 2, 3, 0, -1 }) {
			Location loc;
			loc = location.clone().add(0, i, 0);
			Block topBlock = loc.getBlock();
			Block botBlock = loc.clone().add(0, -1, 0).getBlock();

			if (isTransparent(topBlock) && isEarthbendable(botBlock)) {
				location = loc.clone().add(0, -1, 0);
				return location;
			}
		}

		return null;
	}

	@Override
	public void progress() {
		if (step < maxStep) {
			Location newlocation = locations.get(step);
			Block block = location.getBlock();
			location = newlocation.clone();

			if (!newlocation.getBlock().equals(block)) {
				block1 = block2;
				block2 = block3;
				block3 = block4;
				block4 = newlocation.getBlock();

				if (block1 != null)
					if (hasAnyMoved(block1)) {
						block1 = null;
					}
				if (block2 != null)
					if (hasAnyMoved(block2)) {
						block2 = null;
					}
				if (block3 != null)
					if (hasAnyMoved(block3)) {
						block3 = null;
					}
				if (block4 != null)
					if (hasAnyMoved(block4)) {
						block4 = null;
					}

				if (step == 0) {
					if (increase(block4)) {
						block4 = block4.getRelative(BlockFace.UP);
					}
				} else if (step == 1) {
					if (increase(block3)) {
						block3 = block3.getRelative(BlockFace.UP);
					}
					if (increase(block4)) {
						block4 = block4.getRelative(BlockFace.UP);
					}
				} else if (step == 2) {
					if (decrease(block2)) {
						block2 = block2.getRelative(BlockFace.DOWN);
					}
					if (increase(block3)) {
						block3 = block3.getRelative(BlockFace.UP);
					}
					if (increase(block4)) {
						block4 = block4.getRelative(BlockFace.UP);
					}
				} else {
					if (decrease(block1)) {
						block1 = block1.getRelative(BlockFace.DOWN);
					}
					if (decrease(block2)) {
						block2 = block2.getRelative(BlockFace.DOWN);
					}
					if (increase(block3)) {
						block3 = block3.getRelative(BlockFace.UP);
					}
					if (increase(block4)) {
						block4 = block4.getRelative(BlockFace.UP);
					}
				}
			}
		} else if (step == maxStep) {
			if (decrease(block2)) {
				block2 = block2.getRelative(BlockFace.DOWN);
			}
			if (decrease(block3)) {
				block3 = block3.getRelative(BlockFace.DOWN);
			}
			if (increase(block4)) {
				block4 = block4.getRelative(BlockFace.UP);
			}
		} else if (step == maxStep + 1) {
			if (decrease(block3)) {
				block3 = block3.getRelative(BlockFace.DOWN);
			}
			if (decrease(block4)) {
				block4 = block4.getRelative(BlockFace.DOWN);
			}
		} else if (step == maxStep + 2) {
			if (decrease(block4)) {
				block4 = block4.getRelative(BlockFace.DOWN);
			}
			remove();
		}

		step += 1;
		for (Entity entity : entities) {
			affect(entity);
		}
		entities.clear();
	}

	private void initializeLocations() {
		Location location = origin.clone();
		locations.add(location);

		while (location.distanceSquared(origin) < range * range) {
			location = location.clone().add(direction);
			for (int i : new int[] { 1, 2, 3, 0, -1 }) {
				Location loc;
				loc = location.clone().add(0, i, 0);
				Block topblock = loc.getBlock();
				Block botblock = loc.clone().add(0, -1, 0).getBlock();

				if (isTransparent(topblock) && !topblock.isLiquid() && isEarthbendable(botblock) && botblock.getType() != Material.STATIONARY_LAVA) {
					location = loc.clone().add(0, -1, 0);
					locations.add(location);
					break;
				} else if (i == -1) {
					return;
				}
			}
		}
	}

	private boolean decrease(Block block) {
		if (block == null) {
			return false;
		} else if (hasAnyMoved(block)) {
			return false;
		}

		setMoved(block);
		Block botBlock = block.getRelative(BlockFace.DOWN);
		int length = 1;

		if (isEarthbendable(botBlock)) {
			length = 2;
			block = botBlock;
		}
		return moveEarth(block, new Vector(0, -1, 0), length, false);
	}

	private boolean increase(Block block) {
		if (block == null) {
			return false;
		} else if (hasAnyMoved(block)) {
			return false;
		}

		setMoved(block);
		Block botblock = block.getRelative(BlockFace.DOWN);
		int length = 1;

		if (isEarthbendable(botblock)) {
			length = 2;
		}
		if (moveEarth(block, new Vector(0, 1, 0), length, false)) {
			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(block.getLocation().clone().add(0, 1, 0), 2)) {
				if (entity.getEntityId() != player.getEntityId() && !entities.contains(entity)) {
					if (!(entity instanceof FallingBlock)) {
						entities.add(entity);
					}
				}
			}
			return true;
		}
		return false;
	}

	private void affect(Entity entity) {
		if (entity instanceof LivingEntity) {
			DamageHandler.damageEntity(entity, damage, this);
		}

		Vector vector = direction.clone();
		vector.setY(.5);
		double knock = bPlayer.isAvatarState() ? AvatarState.getValue(knockback) : knockback;
		entity.setVelocity(vector.clone().normalize().multiply(knock));
		AirAbility.breakBreathbendingHold(entity);
	}

	private static void setMoved(Block block) {
		int x = block.getX();
		int z = block.getZ();
		Integer[] pair = new Integer[] { x, z };
		BLOCKS.put(pair, block);
	}

	private static boolean hasAnyMoved(Block block) {
		int x = block.getX();
		int z = block.getZ();
		Integer[] pair = new Integer[] { x, z };
		if (BLOCKS.containsKey(pair)) {
			return true;
		}
		return false;
	}

	public static void progressAllCleanup() {
		BLOCKS.clear();
	}

	public static Map<Integer[], Block> getBlocks() {
		return BLOCKS;
	}

	@Override
	public String getName() {
		return "Shockwave";
	}

	@Override
	public Location getLocation() {
		return location;
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

	@Override
	public ArrayList<Location> getLocations() {
		return locations;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public int getMaxStep() {
		return maxStep;
	}

	public void setMaxStep(int maxStep) {
		this.maxStep = maxStep;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public double getKnockback() {
		return knockback;
	}

	public void setKnockback(double knockback) {
		this.knockback = knockback;
	}

	public Vector getDirection() {
		return direction;
	}

	public void setDirection(Vector direction) {
		this.direction = direction;
	}

	public Location getOrigin() {
		return origin;
	}

	public void setOrigin(Location origin) {
		this.origin = origin;
	}

	public Block getBlock1() {
		return block1;
	}

	public void setBlock1(Block block1) {
		this.block1 = block1;
	}

	public Block getBlock2() {
		return block2;
	}

	public void setBlock2(Block block2) {
		this.block2 = block2;
	}

	public Block getBlock3() {
		return block3;
	}

	public void setBlock3(Block block3) {
		this.block3 = block3;
	}

	public Block getBlock4() {
		return block4;
	}

	public void setBlock4(Block block4) {
		this.block4 = block4;
	}

	public ArrayList<Entity> getEntities() {
		return entities;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

}

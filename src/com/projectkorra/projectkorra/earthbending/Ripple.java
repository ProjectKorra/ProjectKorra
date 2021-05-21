package com.projectkorra.projectkorra.earthbending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.util.DamageHandler;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Ripple {

	private static final Map<Integer[], Block> BLOCKS = new ConcurrentHashMap<Integer[], Block>();

	private Player player;
	private Shockwave shockwave;
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

	public Ripple(Shockwave shockwave, final Vector direction, double range, double damage, double knockback) {
		this.initialize(shockwave, this.getInitialLocation(shockwave.getPlayer(), direction), direction, range, damage, knockback);
	}

	public Ripple(Shockwave shockwave, final Location origin, final Vector direction, double range, double damage, double knockback) {
		this.initialize(shockwave, origin, direction, range, damage, knockback);
	}

	private void initialize(Shockwave shockwave, final Location origin, final Vector direction, double range, double damage, double knockback) {
		if (origin == null) {
			return;
		}
		this.shockwave = shockwave;
		this.player = shockwave.getPlayer();
		this.range = range;
		this.damage = damage;
		this.knockback = knockback;
		this.direction = direction.clone().normalize();
		this.origin = origin.clone();
		this.location = origin.clone();
		this.locations = new ArrayList<>();
		this.entities = new ArrayList<>();

		this.initializeLocations();
		this.maxStep = this.locations.size();

		if (!EarthAbility.isEarthbendable(player, origin.getBlock())) {
			remove = true;
		}
	}

	private Location getInitialLocation(final Player player, Vector direction) {
		Location location = player.getLocation().clone().add(0, -1, 0);
		direction = direction.normalize();
		final Block block1 = location.getBlock();

		while (location.getBlock().equals(block1)) {
			location = location.clone().add(direction);
		}

		for (final int i : new int[] { 1, 2, 3, 0, -1 }) {
			Location loc;
			loc = location.clone().add(0, i, 0);
			final Block topBlock = loc.getBlock();
			final Block botBlock = loc.clone().add(0, -1, 0).getBlock();

			if (GeneralMethods.isTransparent(topBlock) && EarthAbility.isEarthbendable(player, botBlock)) {
				location = loc.clone().add(0, -1, 0);
				return location;
			}
		}

		return null;
	}

	private boolean remove = false;

	public boolean isToBeRemoved() {
		return remove;
	}

	public void progress(boolean damage) {
		if (this.step < this.maxStep && !remove) {
			final Location newlocation = this.locations.get(this.step);
			final Block block = this.location.getBlock();
			this.location = newlocation.clone();

			if (!newlocation.getBlock().equals(block)) {
				this.block1 = this.block2;
				this.block2 = this.block3;
				this.block3 = this.block4;
				this.block4 = newlocation.getBlock();

				if (this.block1 != null) {
					if (hasAnyMoved(this.block1)) {
						this.block1 = null;
					}
				}
				if (this.block2 != null) {
					if (hasAnyMoved(this.block2)) {
						this.block2 = null;
					}
				}
				if (this.block3 != null) {
					if (hasAnyMoved(this.block3)) {
						this.block3 = null;
					}
				}
				if (this.block4 != null) {
					if (hasAnyMoved(this.block4)) {
						this.block4 = null;
					}
				}

				if (this.step == 0) {
					if (this.increase(this.block4)) {
						this.block4 = this.block4.getRelative(BlockFace.UP);
					}
				} else if (this.step == 1) {
					if (this.increase(this.block3)) {
						this.block3 = this.block3.getRelative(BlockFace.UP);
					}
					if (this.increase(this.block4)) {
						this.block4 = this.block4.getRelative(BlockFace.UP);
					}
				} else if (this.step == 2) {
					if (this.decrease(this.block2)) {
						this.block2 = this.block2.getRelative(BlockFace.DOWN);
					}
					if (this.increase(this.block3)) {
						this.block3 = this.block3.getRelative(BlockFace.UP);
					}
					if (this.increase(this.block4)) {
						this.block4 = this.block4.getRelative(BlockFace.UP);
					}
				} else {
					if (this.decrease(this.block1)) {
						this.block1 = this.block1.getRelative(BlockFace.DOWN);
					}
					if (this.decrease(this.block2)) {
						this.block2 = this.block2.getRelative(BlockFace.DOWN);
					}
					if (this.increase(this.block3)) {
						this.block3 = this.block3.getRelative(BlockFace.UP);
					}
					if (this.increase(this.block4)) {
						this.block4 = this.block4.getRelative(BlockFace.UP);
					}
				}
			}
		} else if (this.step == this.maxStep) {
			if (this.decrease(this.block2)) {
				this.block2 = this.block2.getRelative(BlockFace.DOWN);
			}
			if (this.decrease(this.block3)) {
				this.block3 = this.block3.getRelative(BlockFace.DOWN);
			}
			if (this.increase(this.block4)) {
				this.block4 = this.block4.getRelative(BlockFace.UP);
			}
		} else if (this.step == this.maxStep + 1) {
			if (this.decrease(this.block3)) {
				this.block3 = this.block3.getRelative(BlockFace.DOWN);
			}
			if (this.decrease(this.block4)) {
				this.block4 = this.block4.getRelative(BlockFace.DOWN);
			}
		} else if (this.step == this.maxStep + 2) {
			if (this.decrease(this.block4)) {
				this.block4 = this.block4.getRelative(BlockFace.DOWN);
			}
			remove = true;
		}

		this.step += 1;
		for (final Entity entity : this.entities) {
			this.affect(entity, damage);
		}
		this.entities.clear();
	}

	private void initializeLocations() {
		Location location = this.origin.clone();
		this.locations.add(location);

		while (location.distanceSquared(this.origin) < this.range * this.range) {
			location = location.clone().add(this.direction);
			for (final int i : new int[] { 1, 2, 3, 0, -1 }) {
				Location loc;
				loc = location.clone().add(0, i, 0);
				final Block topblock = loc.getBlock();
				final Block botblock = loc.clone().add(0, -1, 0).getBlock();

				if (GeneralMethods.isTransparent(topblock) && !topblock.isLiquid() && EarthAbility.isEarthbendable(player, botblock)) {
					location = loc.clone().add(0, -1, 0);
					this.locations.add(location);
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
		final Block botBlock = block.getRelative(BlockFace.DOWN);
		int length = 1;

		if (EarthAbility.isEarthbendable(player, botBlock)) {
			length = 2;
			block = botBlock;
		}
		return shockwave.moveEarth(block, new Vector(0, -1, 0), length, false);
	}

	private boolean increase(final Block block) {
		if (block == null) {
			return false;
		} else if (hasAnyMoved(block)) {
			return false;
		}

		setMoved(block);
		final Block botblock = block.getRelative(BlockFace.DOWN);
		int length = 1;

		if (shockwave.isEarthbendable(botblock)) {
			length = 2;
		}
		if (shockwave.moveEarth(block, new Vector(0, 1, 0), length, false)) {
			for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(block.getLocation().clone().add(0, 1, 0), 2)) {
				if (entity.getEntityId() != this.player.getEntityId() && !this.entities.contains(entity)) {
					if (!(entity instanceof FallingBlock)) {
						this.entities.add(entity);
					}
				}
			}
			return true;
		}
		return false;
	}

	private void affect(final Entity entity, boolean damage) {
		if (GeneralMethods.isRegionProtectedFromBuild(shockwave, entity.getLocation()) || ((entity instanceof Player) && Commands.invincible.contains(((Player) entity).getName()))) {
			return;
		}
		if (damage && entity instanceof LivingEntity) {
			DamageHandler.damageEntity(entity, this.damage, shockwave);
		}

		final Vector vector = this.direction.clone();
		vector.setY(.5);
		final double knock = knockback;
		GeneralMethods.setVelocity(entity, vector.clone().normalize().multiply(knock));
		AirAbility.breakBreathbendingHold(entity);
	}

	private static void setMoved(final Block block) {
		final int x = block.getX();
		final int z = block.getZ();
		final Integer[] pair = new Integer[] { x, z };
		BLOCKS.put(pair, block);
	}

	private static boolean hasAnyMoved(final Block block) {
		final int x = block.getX();
		final int z = block.getZ();
		final Integer[] pair = new Integer[] { x, z };
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

	public int getStep() {
		return this.step;
	}

	public void setStep(final int step) {
		this.step = step;
	}

	public int getMaxStep() {
		return this.maxStep;
	}

	public void setMaxStep(final int maxStep) {
		this.maxStep = maxStep;
	}

	public double getRange() {
		return this.range;
	}

	public void setRange(final double range) {
		this.range = range;
	}

	public double getDamage() {
		return this.damage;
	}

	public void setDamage(final double damage) {
		this.damage = damage;
	}

	public double getKnockback() {
		return this.knockback;
	}

	public void setKnockback(final double knockback) {
		this.knockback = knockback;
	}

	public Vector getDirection() {
		return this.direction;
	}

	public void setDirection(final Vector direction) {
		this.direction = direction;
	}

	public Location getOrigin() {
		return this.origin;
	}

	public void setOrigin(final Location origin) {
		this.origin = origin;
	}

	public Block getBlock1() {
		return this.block1;
	}

	public void setBlock1(final Block block1) {
		this.block1 = block1;
	}

	public Block getBlock2() {
		return this.block2;
	}

	public void setBlock2(final Block block2) {
		this.block2 = block2;
	}

	public Block getBlock3() {
		return this.block3;
	}

	public void setBlock3(final Block block3) {
		this.block3 = block3;
	}

	public Block getBlock4() {
		return this.block4;
	}

	public void setBlock4(final Block block4) {
		this.block4 = block4;
	}

	public ArrayList<Entity> getEntities() {
		return this.entities;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

}
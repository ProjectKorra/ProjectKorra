package com.projectkorra.ProjectKorra.earthbending;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.TempBlock;
import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.Utilities.BlockSource;
import com.projectkorra.ProjectKorra.Utilities.BlockSource.BlockSourceType;
import com.projectkorra.ProjectKorra.Utilities.ClickType;
import com.projectkorra.ProjectKorra.firebending.FireBlast;
import com.projectkorra.ProjectKorra.waterbending.WaterMethods;

public class LavaWave {
	public static ConcurrentHashMap<Integer, LavaWave> instances = new ConcurrentHashMap<Integer, LavaWave>();
	private static final double defaultmaxradius = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.LavaSurge.Radius");
	private static final double defaultfactor = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.LavaSurge.HorizontalPush");
	private static final double upfactor = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.LavaSurge.VerticalPush");
	private static final long interval = 30;
	@SuppressWarnings("unused")
	private static final byte full = 0x0;
	static double defaultrange = 20;
	Player player;
	private Location location = null;
	private Block sourceblock = null;
	private Location targetdestination = null;
	private Vector targetdirection = null;
	private ConcurrentHashMap<Block, Block> wave = new ConcurrentHashMap<Block, Block>();
	private ConcurrentHashMap<Block, Block> frozenblocks = new ConcurrentHashMap<Block, Block>();
	private long time;
	private double radius = 1;
	private double maxradius = defaultmaxradius;
	private double factor = defaultfactor;
	double range = defaultrange;
	boolean progressing = false;
	boolean canhitself = true;

	public LavaWave(Player player) {
		this.player = player;

		if (AvatarState.isAvatarState(player)) {
			maxradius = AvatarState.getValue(maxradius);
		}
		if (prepare()) {
			if (instances.containsKey(player.getEntityId())) {
				instances.get(player.getEntityId()).cancel();
			}
			instances.put(player.getEntityId(), this);
			time = System.currentTimeMillis();
		}
	}

	public boolean prepare() {
		cancelPrevious();
		// Block block = player.getTargetBlock(null, (int) range);
		Block block = BlockSource.getSourceBlock(player, range, BlockSourceType.LAVA, ClickType.SHIFT_DOWN);
		if (block != null) {
			sourceblock = block;
			focusBlock();
			return true;
		}
		return false;
	}

	private void cancelPrevious() {
		if (instances.containsKey(player.getEntityId())) {
			LavaWave old = instances.get(player.getEntityId());
			if (old.progressing) {
				old.breakBlock();
			} else {
				old.cancel();
			}
		}
	}

	public void cancel() {
		unfocusBlock();
	}

	private void focusBlock() {
		location = sourceblock.getLocation();
	}

	private void unfocusBlock() {
		instances.remove(player.getEntityId());
	}

	@SuppressWarnings("deprecation")
	public void moveLava() {
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
		if (bPlayer.isOnCooldown("LavaSurge")) return;

		bPlayer.addCooldown("LavaSurge", GeneralMethods.getGlobalCooldown());
		if (sourceblock != null) {
			if (!sourceblock.getWorld().equals(player.getWorld())) {
				return;
			}
			if (AvatarState.isAvatarState(player)) factor = AvatarState.getValue(factor);
			Entity target = GeneralMethods.getTargetedEntity(player, range, new ArrayList<Entity>());
			if (target == null) {
				targetdestination = player.getTargetBlock(EarthMethods.getTransparentEarthbending(), (int) range).getLocation();
			} else {
				targetdestination = ((LivingEntity) target).getEyeLocation();
			}
			if (targetdestination.distance(location) <= 1) {
				progressing = false;
				targetdestination = null;
			} else {
				progressing = true;
				targetdirection = getDirection(sourceblock.getLocation(), targetdestination).normalize();
				targetdestination = location.clone().add(targetdirection.clone().multiply(range));
				if (!GeneralMethods.isAdjacentToThreeOrMoreSources(sourceblock)) {
					sourceblock.setType(Material.AIR);
				}
				addLava(sourceblock);
			}
		}
	}

	private Vector getDirection(Location location, Location destination) {
		double x1, y1, z1;
		double x0, y0, z0;
		x1 = destination.getX();
		y1 = destination.getY();
		z1 = destination.getZ();
		x0 = location.getX();
		y0 = location.getY();
		z0 = location.getZ();
		return new Vector(x1 - x0, y1 - y0, z1 - z0);
	}

	public static void progressAll() {
		for (int ID : instances.keySet()) {
			instances.get(ID).progress();
		}
	}

	private boolean progress() {
		if (player.isDead() || !player.isOnline() || !GeneralMethods.canBend(player.getName(), "LavaSurge")) {
			breakBlock();
			return false;
		}
		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();
			if (GeneralMethods.getBoundAbility(player) == null) {
				unfocusBlock();
				return false;
			}
			if (!progressing && !GeneralMethods.getBoundAbility(player).equalsIgnoreCase("LavaSurge")) {
				unfocusBlock();
				return false;
			}
			if (!progressing) {
				sourceblock.getWorld().playEffect(location, Effect.SMOKE, 4, (int) range);
				return false;
			}
			if (location.getWorld() != player.getWorld()) {
				breakBlock();
				return false;
			}
			Vector direction = targetdirection;
			location = location.clone().add(direction);
			Block blockl = location.getBlock();
			ArrayList<Block> blocks = new ArrayList<Block>();
			if (!GeneralMethods.isRegionProtectedFromBuild(player, "LavaSurge", location) && (((blockl.getType() == Material.AIR || blockl.getType() == Material.FIRE || WaterMethods.isPlant(blockl) || EarthMethods.isLava(blockl) || EarthMethods.isLavabendable(blockl, player))) && blockl.getType() != Material.LEAVES)) {
				for (double i = 0; i <= radius; i += .5) {
					for (double angle = 0; angle < 360; angle += 10) {
						Vector vec = GeneralMethods.getOrthogonalVector(targetdirection, angle, i);
						Block block = location.clone().add(vec).getBlock();
						if (!blocks.contains(block) && (block.getType() == Material.AIR || block.getType() == Material.FIRE) || EarthMethods.isLavabendable(block, player)) {
							blocks.add(block);
							FireBlast.removeFireBlastsAroundPoint(block.getLocation(), 2);
						}
					}

				}
			}
			for (Block block : wave.keySet()) {
				if (!blocks.contains(block)) finalRemoveLava(block);
			}
			for (Block block : blocks) {
				if (!wave.containsKey(block)) addLava(block);
			}
			if (wave.isEmpty()) {
				breakBlock();
				progressing = false;
				return false;
			}
			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2 * radius)) {
				boolean knockback = false;
				for (Block block : wave.keySet()) {
					if (entity.getLocation().distance(block.getLocation()) <= 2) {
						if (entity.getEntityId() != player.getEntityId() || canhitself) knockback = true;
					}
				}
				if (knockback) {
					Vector dir = direction.clone();
					dir.setY(dir.getY() * upfactor);
					entity.setVelocity(entity.getVelocity().clone().add(dir.clone().multiply(factor)));
					entity.setFallDistance(0);
					if (entity.getFireTicks() > 0) entity.getWorld().playEffect(entity.getLocation(), Effect.EXTINGUISH, 0);
					entity.setFireTicks(0);
				}
			}
			if (!progressing) {
				breakBlock();
				return false;
			}
			if (location.distance(targetdestination) < 1) {
				progressing = false;
				breakBlock();
				return false;
			}
			if (radius < maxradius) radius += .5;
			return true;
		}

		return false;
	}

	private void breakBlock() {
		for (Block block : wave.keySet()) {
			finalRemoveLava(block);
		}
		instances.remove(player.getEntityId());
	}

	private void finalRemoveLava(Block block) {
		if (wave.containsKey(block)) {
			TempBlock.revertBlock(block, Material.AIR);
			wave.remove(block);
		}
	}

	private void addLava(Block block) {
		if (GeneralMethods.isRegionProtectedFromBuild(player, "LavaSurge", block.getLocation())) return;
		if (!TempBlock.isTempBlock(block)) {
			new TempBlock(block, Material.STATIONARY_LAVA, (byte) 8);
			wave.put(block, block);
		}
	}

	@SuppressWarnings("unused")
	private void clearWave() {
		for (Block block : wave.keySet()) {
			TempBlock.revertBlock(block, Material.AIR);
		}
		wave.clear();
	}

	public static void moveLava(Player player) {
		if (instances.containsKey(player.getEntityId())) {
			instances.get(player.getEntityId()).moveLava();
		}
	}

	public static boolean isBlockInWave(Block block) {
		for (int ID : instances.keySet()) {
			if (block.getLocation().distance(instances.get(ID).location) <= 2 * instances.get(ID).radius) {
				return true;
			}
			return false;
		}
		return false;
	}

	public static boolean isBlockWave(Block block) {
		for (int ID : instances.keySet()) {
			if (instances.get(ID).wave.containsKey(block)) return true;
		}
		return false;
	}

	public static void launch(Player player) {
		moveLava(player);
	}

	public static void removeAll() {
		for (int id : instances.keySet()) {
			for (Block block : instances.get(id).wave.keySet()) {
				block.setType(Material.AIR);
				instances.get(id).wave.remove(block);
			}
			for (Block block : instances.get(id).frozenblocks.keySet()) {
				block.setType(Material.AIR);
				instances.get(id).frozenblocks.remove(block);
			}
		}
	}
}

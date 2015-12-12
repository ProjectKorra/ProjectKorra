package com.projectkorra.projectkorra.waterbending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AvatarState;
import com.projectkorra.projectkorra.airbending.AirMethods;
import com.projectkorra.projectkorra.earthbending.EarthMethods;
import com.projectkorra.projectkorra.firebending.FireBlast;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Wave {

	public static ConcurrentHashMap<Integer, Wave> instances = new ConcurrentHashMap<Integer, Wave>();

	private static final double defaultmaxradius = ProjectKorra.plugin.getConfig().getDouble("Abilities.Water.Surge.Wave.Radius");
	private static final double defaultfactor = ProjectKorra.plugin.getConfig().getDouble("Abilities.Water.Surge.Wave.HorizontalPush");
	private static final double defaultupfactor = ProjectKorra.plugin.getConfig().getDouble("Abilities.Water.Surge.Wave.VerticalPush");
	private static final double MAX_FREEZE_RADIUS = 7;

	private static final long interval = 30;
	@SuppressWarnings("unused")
	private static final byte full = 0x0;
	static int defaultrange = ProjectKorra.plugin.getConfig().getInt("Abilities.Water.Surge.Wave.SelectRange");

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
	private double upfactor = defaultupfactor;
	private double maxfreezeradius = MAX_FREEZE_RADIUS;
	private boolean freeze = false;
	private boolean activatefreeze = false;
	private Location frozenlocation;
	int range = defaultrange;
	int maxrange = ProjectKorra.plugin.getConfig().getInt("Abilities.Water.Surge.Wave.Range");
	boolean progressing = false;
	boolean canhitself = true;

	public Wave(Player player) {
		this.player = player;

		if (instances.containsKey(player.getEntityId())) {
			if (instances.get(player.getEntityId()).progressing && !instances.get(player.getEntityId()).freeze) {
				instances.get(player.getEntityId()).freeze = true;
				return;
			}
		}

		if (AvatarState.isAvatarState(player)) {
			maxradius = AvatarState.getValue(maxradius);
		}
		maxradius = WaterMethods.waterbendingNightAugment(maxradius, player.getWorld());
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
		Block block = BlockSource.getWaterSourceBlock(player, range, range, ClickType.SHIFT_DOWN, false, true, true, WaterMethods.canPlantbend(player), WaterMethods.canPlantbend(player));
		if (block != null) {
			sourceblock = block;
			focusBlock();
			return true;
		}
		return false;
	}

	private void cancelPrevious() {
		if (instances.containsKey(player.getEntityId())) {
			Wave old = instances.get(player.getEntityId());
			if (old.progressing) {
				old.breakBlock();
				old.thaw();
				old.returnWater(old.location);
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
	public void moveWater() {
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());

		if (bPlayer.isOnCooldown("Surge"))
			return;
		bPlayer.addCooldown("Surge", GeneralMethods.getGlobalCooldown());
		if (sourceblock != null) {
			if (!sourceblock.getWorld().equals(player.getWorld())) {
				return;
			}
			if (AvatarState.isAvatarState(player))
				factor = AvatarState.getValue(factor);
			Entity target = GeneralMethods.getTargetedEntity(player, maxrange, new ArrayList<Entity>());
			if (target == null) {
				targetdestination = player.getTargetBlock(EarthMethods.getTransparentEarthbending(), maxrange).getLocation();
			} else {
				targetdestination = ((LivingEntity) target).getEyeLocation();
			}
			if (targetdestination.distance(location) <= 1) {
				progressing = false;
				targetdestination = null;
			} else {
				progressing = true;
				targetdirection = getDirection(sourceblock.getLocation(), targetdestination).normalize();
				targetdestination = location.clone().add(targetdirection.clone().multiply(maxrange));
				if (WaterMethods.isPlant(sourceblock))
					new Plantbending(sourceblock);
				if (!GeneralMethods.isAdjacentToThreeOrMoreSources(sourceblock)) {
					sourceblock.setType(Material.AIR);
				}
				addWater(sourceblock);

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
		if (player.isDead() || !player.isOnline() || !GeneralMethods.canBend(player.getName(), "Surge")) {
			breakBlock();
			thaw();
			// instances.remove(player.getEntityId());
			return false;
		}
		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();

			if (GeneralMethods.getBoundAbility(player) == null) {
				unfocusBlock();
				thaw();
				breakBlock();
				returnWater(location);
				return false;
			}
			if (!progressing && !GeneralMethods.getBoundAbility(player).equalsIgnoreCase("Surge")) {
				unfocusBlock();
				return false;
			}

			if (!progressing) {
				sourceblock.getWorld().playEffect(location, Effect.SMOKE, 4, range);
				return false;
			}

			if (location.getWorld() != player.getWorld()) {
				thaw();
				breakBlock();
				return false;
			}

			if (activatefreeze) {
				if (location.distance(player.getLocation()) > maxrange) {
					progressing = false;
					thaw();
					breakBlock();
					return false;
				}
				if (GeneralMethods.getBoundAbility(player) == null) {
					progressing = false;
					thaw();
					breakBlock();
					returnWater(location);
					return false;
				}
				if (!GeneralMethods.canBend(player.getName(), "Surge")) {
					progressing = false;
					thaw();
					breakBlock();
					returnWater(location);
					return false;
				}

			} else {

				Vector direction = targetdirection;

				location = location.clone().add(direction);
				Block blockl = location.getBlock();

				ArrayList<Block> blocks = new ArrayList<Block>();

				if (!GeneralMethods.isRegionProtectedFromBuild(player, "Surge", location) && (((blockl.getType() == Material.AIR || blockl.getType() == Material.FIRE || WaterMethods.isPlant(blockl) || WaterMethods.isWater(blockl) || WaterMethods.isWaterbendable(blockl, player))) && blockl.getType() != Material.LEAVES)) {

					for (double i = 0; i <= radius; i += .5) {
						for (double angle = 0; angle < 360; angle += 10) {
							Vector vec = GeneralMethods.getOrthogonalVector(targetdirection, angle, i);
							Block block = location.clone().add(vec).getBlock();
							if (!blocks.contains(block) && (block.getType() == Material.AIR || block.getType() == Material.FIRE) || WaterMethods.isWaterbendable(block, player)) {
								blocks.add(block);
								FireBlast.removeFireBlastsAroundPoint(block.getLocation(), 2);
							}

							if (GeneralMethods.rand.nextInt(15) == 0) {
								WaterMethods.playWaterbendingSound(location);
							}
							// if (!blocks.contains(block)
							// && (Methods.isPlant(block) && block.getType() !=
							// Material.LEAVES)) {
							// blocks.add(block);
							// block.breakNaturally();
							// }
						}
					}
				}

				for (Block block : wave.keySet()) {
					if (!blocks.contains(block))
						finalRemoveWater(block);
				}

				for (Block block : blocks) {
					if (!wave.containsKey(block))
						addWater(block);
				}

				if (wave.isEmpty()) {
					// blockl.setType(Material.GLOWSTONE);
					breakBlock();
					returnWater(location.subtract(direction));
					progressing = false;
					return false;
				}

				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2 * radius)) {

					boolean knockback = false;
					for (Block block : wave.keySet()) {
						if (entity.getLocation().distance(block.getLocation()) <= 2) {
							if (entity instanceof LivingEntity && freeze && entity.getEntityId() != player.getEntityId()) {
								activatefreeze = true;
								frozenlocation = entity.getLocation();
								freeze();
								break;
							}
							if (entity.getEntityId() != player.getEntityId() || canhitself)
								knockback = true;
						}
					}
					if (knockback) {
						Vector dir = direction.clone();
						dir.setY(dir.getY() * upfactor);
						GeneralMethods.setVelocity(entity, entity.getVelocity().clone().add(dir.clone().multiply(WaterMethods.waterbendingNightAugment(factor, player.getWorld()))));
						entity.setFallDistance(0);
						if (entity.getFireTicks() > 0)
							entity.getWorld().playEffect(entity.getLocation(), Effect.EXTINGUISH, 0);
						entity.setFireTicks(0);
						AirMethods.breakBreathbendingHold(entity);
					}

				}

				if (!progressing) {
					breakBlock();
					return false;
				}

				if (location.distance(targetdestination) < 1) {
					progressing = false;
					breakBlock();
					returnWater(location);
					return false;
				}

				if (radius < maxradius)
					radius += .5;

				return true;
			}
		}

		return false;

	}

	private void breakBlock() {
		for (Block block : wave.keySet()) {
			finalRemoveWater(block);
		}
		instances.remove(player.getEntityId());
	}

	private void finalRemoveWater(Block block) {
		if (wave.containsKey(block)) {
			// block.setType(Material.WATER);
			// block.setData(half);
			// if (!Methods.adjacentToThreeOrMoreSources(block) || radius > 1) {
			// block.setType(Material.AIR);
			// }
			TempBlock.revertBlock(block, Material.AIR);
			wave.remove(block);
		}
	}

	private void addWater(Block block) {
		if (GeneralMethods.isRegionProtectedFromBuild(player, "Surge", block.getLocation()))
			return;
		if (!TempBlock.isTempBlock(block)) {
			new TempBlock(block, Material.STATIONARY_WATER, (byte) 8);
			// new TempBlock(block, Material.ICE, (byte) 0);
			wave.put(block, block);
		}
		// block.setType(Material.WATER);
		// block.setData(full);
		// wave.put(block, block);
	}

	private void clearWave() {
		for (Block block : wave.keySet()) {
			TempBlock.revertBlock(block, Material.AIR);
		}
		wave.clear();
	}

	public static void moveWater(Player player) {
		if (instances.containsKey(player.getEntityId())) {
			instances.get(player.getEntityId()).moveWater();
		}
	}

	public static boolean progress(int ID) {
		return instances.get(ID).progress();
	}

	public static boolean isBlockWave(Block block) {
		for (int ID : instances.keySet()) {
			if (instances.get(ID).wave.containsKey(block))
				return true;
		}
		return false;
	}

	public static void launch(Player player) {
		moveWater(player);
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

	private void freeze() {

		clearWave();

		if (!WaterMethods.canIcebend(player))
			return;

		double freezeradius = radius;
		if (freezeradius > maxfreezeradius) {
			freezeradius = maxfreezeradius;
		}

		for (Block block : GeneralMethods.getBlocksAroundPoint(frozenlocation, freezeradius)) {
			if (GeneralMethods.isRegionProtectedFromBuild(player, "Surge", block.getLocation()) || GeneralMethods.isRegionProtectedFromBuild(player, "PhaseChange", block.getLocation()))
				continue;
			if (TempBlock.isTempBlock(block))
				continue;
			if (block.getType() == Material.AIR || block.getType() == Material.SNOW) {
				// block.setType(Material.ICE);
				new TempBlock(block, Material.ICE, (byte) 0);
				frozenblocks.put(block, block);
			}
			if (WaterMethods.isWater(block)) {
				FreezeMelt.freeze(player, block);
			}
			if (WaterMethods.isPlant(block) && block.getType() != Material.LEAVES) {
				block.breakNaturally();
				// block.setType(Material.ICE);
				new TempBlock(block, Material.ICE, (byte) 0);
				frozenblocks.put(block, block);
			}
			for (Block sound : frozenblocks.keySet()) {
				if (GeneralMethods.rand.nextInt(4) == 0) {
					WaterMethods.playWaterbendingSound(sound.getLocation());
				}
			}
		}
	}

	private void thaw() {
		for (Block block : frozenblocks.keySet()) {
			// if (block.getType() == Material.ICE) {
			// // block.setType(Material.WATER);
			// // block.setData((byte) 0x7);
			// block.setType(Material.AIR);
			// }
			TempBlock.revertBlock(block, Material.AIR);
			frozenblocks.remove(block);
		}
	}

	public static void thaw(Block block) {
		for (int id : instances.keySet()) {
			if (instances.get(id).frozenblocks.containsKey(block)) {
				// if (block.getType() == Material.ICE) {
				// // block.setType(Material.WATER);
				// // block.setData((byte) 0x7);
				// block.setType(Material.AIR);
				// }
				TempBlock.revertBlock(block, Material.AIR);
				instances.get(id).frozenblocks.remove(block);
			}
		}
	}

	public static boolean canThaw(Block block) {
		for (int id : instances.keySet()) {
			if (instances.get(id).frozenblocks.containsKey(block)) {
				return false;
			}
		}
		return true;
	}

	void returnWater(Location location) {
		if (location != null) {
			new WaterReturn(player, location.getBlock());
		}
	}

	public static String getDescription() {
		return "To use, place your cursor over a waterbendable object " + "(water, ice, plants if you have plantbending) and tap sneak " + "(default: shift). Smoke will appear where you've selected, " + "indicating the origin of your ability. After you have selected an origin, " + "simply left-click in any direction and you will see your water spout off in that " + "direction and form a large wave, knocking back all within its path. " + "If you look towards a creature when you use this ability, it will target that creature. " + "Additionally, tapping sneak while the wave is en route will cause that wave to encase the " + "first target it hits in ice.";
	}

	public Player getPlayer() {
		return player;
	}

	public double getMaxradius() {
		return maxradius;
	}

	public void setMaxradius(double maxradius) {
		this.maxradius = maxradius;
	}

	public double getFactor() {
		return factor;
	}

	public void setFactor(double factor) {
		this.factor = factor;
	}

	public double getUpfactor() {
		return upfactor;
	}

	public void setUpfactor(double upfactor) {
		this.upfactor = upfactor;
	}

	public double getMaxfreezeradius() {
		return maxfreezeradius;
	}

	public void setMaxfreezeradius(double maxfreezeradius) {
		this.maxfreezeradius = maxfreezeradius;
	}

}
package com.projectkorra.ProjectKorra.waterbending;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.Methods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.TempBlock;
import com.projectkorra.projectkorra.Ability.AvatarState;

public class OctopusForm {

	public static ConcurrentHashMap<Player, OctopusForm> instances = new ConcurrentHashMap<Player, OctopusForm>();

	private static int RANGE = ProjectKorra.plugin.getConfig().getInt("Abilities.Water.OctopusForm.Range");
	private static double ATTACK_RANGE = ProjectKorra.plugin.getConfig().getInt("Abilities.Water.OctopusForm.AttackRange");
	private static int DAMAGE = ProjectKorra.plugin.getConfig().getInt("Abilities.Water.OctopusForm.Damage");
	private static long INTERVAL = ProjectKorra.plugin.getConfig().getLong("Abilities.Water.OctopusForm.FormDelay");
	private static double KNOCKBACK = ProjectKorra.plugin.getConfig().getDouble("Abilities.Water.OctopusForm.Knockback");
	static double RADIUS = ProjectKorra.plugin.getConfig().getDouble("Abilities.Water.OctopusForm.Radius");
	private static final byte full = 0x0;

	private Player player;
	private Block sourceblock;
	private Location sourcelocation;
	private TempBlock source;
	private long time;
	private double startangle;
	private double angle;
	private double y = 0;
	private double dta = 45;
	private int animstep = 1, step = 1, inc = 3;
	private ArrayList<TempBlock> blocks = new ArrayList<TempBlock>();
	private ArrayList<TempBlock> newblocks = new ArrayList<TempBlock>();
	// private static ArrayList<TempBlock> water = new ArrayList<TempBlock>();
	private boolean sourceselected = false;
	private boolean settingup = false;
	private boolean forming = false;
	private boolean formed = false;
	private int range = RANGE;
	private double attackRange = ATTACK_RANGE;
	private int damage = DAMAGE;
	private long interval = INTERVAL;
	private double knockback = KNOCKBACK;
	private double radius = RADIUS;

	public OctopusForm(Player player) {
		if (instances.containsKey(player)) {
			if (instances.get(player).formed) {
				instances.get(player).attack();
				return;
			} else if (!instances.get(player).sourceselected) {
				return;
			}
		}
		this.player = player;
		time = System.currentTimeMillis();
		sourceblock = Methods.getWaterSourceBlock(player, range, true);
		if (sourceblock != null) {
			sourcelocation = sourceblock.getLocation();
			sourceselected = true;
			instances.put(player, this);
		}
	}

	private void incrementStep() {
		if (sourceselected) {
			sourceselected = false;
			settingup = true;
		} else if (settingup) {
			settingup = false;
			forming = true;
		} else if (forming) {
			forming = false;
			formed = true;
		}
	}

	@SuppressWarnings("deprecation")
	public static void form(Player player) {
		if (instances.containsKey(player)) {
			instances.get(player).form();
		} else if (WaterReturn.hasWaterBottle(player)) {
			Location eyeloc = player.getEyeLocation();
			Block block = eyeloc.add(eyeloc.getDirection().normalize()).getBlock();
			if (Methods.isTransparentToEarthbending(player, block)
					&& Methods.isTransparentToEarthbending(player, eyeloc.getBlock())) {
				block.setType(Material.WATER);
				block.setData(full);
				OctopusForm form = new OctopusForm(player);
				form.form();
				if (form.formed || form.forming || form.settingup) {
					WaterReturn.emptyWaterBottle(player);
				} else {
					block.setType(Material.AIR);
				}
			}
		}
	}

	private void form() {
		incrementStep();
		if (Methods.isPlant(sourceblock)) {
			new Plantbending(sourceblock);
			sourceblock.setType(Material.AIR);
		} else if (!Methods.isAdjacentToThreeOrMoreSources(sourceblock)) {
			sourceblock.setType(Material.AIR);
		}
		source = new TempBlock(sourceblock, Material.STATIONARY_WATER, (byte) 8);
	}

	private void attack() {
		if (!formed)
			return;
		double tentacleangle = (new Vector(1, 0, 0)).angle(player.getEyeLocation().getDirection()) + dta / 2;

		for (double tangle = tentacleangle; tangle < tentacleangle + 360; tangle += dta) {
			double phi = Math.toRadians(tangle);
			affect(player.getLocation().clone().add(
					new Vector(radius * Math.cos(phi), 1, radius * Math.sin(phi))));
		}
	}

	private void affect(Location location) {
		for (Entity entity : Methods.getEntitiesAroundPoint(location, attackRange)) {
			if (entity.getEntityId() == player.getEntityId())
				continue;
			if (Methods.isRegionProtectedFromBuild(player, "OctopusForm", entity.getLocation()))
				continue;
			// if (Torrent.canThaw(entity.getLocation().getBlock())
			// || Wave.canThaw(entity.getLocation().getBlock()))
			// continue;
			if (Methods.isObstructed(location, entity.getLocation()))
				continue;
			double knock = AvatarState.isAvatarState(player) ? AvatarState.getValue(knockback) : knockback;
			entity.setVelocity(Methods.getDirection(player.getLocation(), location).normalize().multiply(knock));
			if (entity instanceof LivingEntity)
				Methods.damageEntity(player, entity, damage);
				Methods.breakBreathbendingHold(entity);
		}
	}

	public static void progressAll() {
		for (Player player : instances.keySet()) {
			instances.get(player).progress();
		}
		// replaceWater();
	}

	private void progress() {
		if (!Methods.canBend(player.getName(), "OctopusForm")) {
			remove();
			returnWater();
			return;
		}
		
		if (Methods.getBoundAbility(player) == null) {
			remove();
			returnWater();
			return;
		}
		
		if ((!player.isSneaking() && !sourceselected) || !Methods.getBoundAbility(player).equalsIgnoreCase("OctopusForm")) {
			remove();
			returnWater();
			return;
		}

		if (!sourceblock.getWorld().equals(player.getWorld())) {
			remove();
			return;
		}

		if (sourceblock.getLocation().distance(player.getLocation()) > range && sourceselected) {
			remove();
			return;
		}

		if (System.currentTimeMillis() > time + interval) {
			time = System.currentTimeMillis();

			Location location = player.getLocation();

			if (sourceselected) {
				Methods.playFocusWaterEffect(sourceblock);
			} else if (settingup) {
				if (sourceblock.getY() < location.getBlockY()) {
					source.revertBlock();
					source = null;
					Block newblock = sourceblock.getRelative(BlockFace.UP);
					sourcelocation = newblock.getLocation();
					if (!Methods.isSolid(newblock)) {
						source = new TempBlock(newblock, Material.STATIONARY_WATER, (byte) 8);
						sourceblock = newblock;
					} else {
						remove();
						returnWater();
					}
				} else if (sourceblock.getY() > location.getBlockY()) {
					source.revertBlock();
					source = null;
					Block newblock = sourceblock.getRelative(BlockFace.DOWN);
					sourcelocation = newblock.getLocation();
					if (!Methods.isSolid(newblock)) {
						source = new TempBlock(newblock, Material.STATIONARY_WATER, (byte) 8);
						sourceblock = newblock;
					} else {
						remove();
						returnWater();
					}
				} else if (sourcelocation.distance(location) > radius) {
					Vector vector = Methods.getDirection(sourcelocation, location.getBlock().getLocation()).normalize();
					sourcelocation.add(vector);
					Block newblock = sourcelocation.getBlock();
					if (!newblock.equals(sourceblock)) {
						source.revertBlock();
						source = null;
						if (!Methods.isSolid(newblock)) {
							source = new TempBlock(newblock, Material.STATIONARY_WATER, (byte) 8);
							sourceblock = newblock;
						}
					}
				} else {
					incrementStep();
					if (source != null) source.revertBlock();
					source = null;
					Vector vector = new Vector(1, 0, 0);
					startangle = vector.angle(Methods.getDirection(	sourceblock.getLocation(), location));
					angle = startangle;
				}
			} else if (forming) {

				if (angle - startangle >= 360) {
					y += 1;
				} else {
					angle += 20;
				}
				if (Methods.rand.nextInt(4) == 0) {
					Methods.playWaterbendingSound(player.getLocation());
				}		
				formOctopus();
				if (y == 2) {
					incrementStep();
				}
			} else if (formed) {
				if (Methods.rand.nextInt(7) == 0) {
					Methods.playWaterbendingSound(player.getLocation());
				}		
				step += 1;
				if (step % inc == 0)
					animstep += 1;
				if (animstep > 8)
					animstep = 1;
				formOctopus();
			} else {
				remove();
			}
		}
	}

	private void formOctopus() {
		Location location = player.getLocation();
		newblocks.clear();

		ArrayList<Block> doneblocks = new ArrayList<Block>();

		for (double theta = startangle; theta < startangle + angle; theta += 10) {
			double rtheta = Math.toRadians(theta);
			Block block = location.clone().add(
					new Vector(radius * Math.cos(rtheta), 0, radius	* Math.sin(rtheta))).getBlock();
			if (!doneblocks.contains(block)) {
				addWater(block);
				doneblocks.add(block);
			}
		}

		Vector eyedir = player.getEyeLocation().getDirection();
		eyedir.setY(0);

		double tentacleangle = Math.toDegrees((new Vector(1, 0, 0)).angle(eyedir)) + dta / 2;
		int astep = animstep;
		for (double tangle = tentacleangle; tangle < tentacleangle + 360; tangle += dta) {
			astep += 1;
			double phi = Math.toRadians(tangle);
			tentacle(location.clone().add(new Vector(radius * Math.cos(phi), 0, radius * Math.sin(phi))), astep);
		}

		for (TempBlock block : blocks) {
			if (!newblocks.contains(block))
				block.revertBlock();
		}

		blocks.clear();

		blocks.addAll(newblocks);

		if (blocks.isEmpty())
			remove();
	}

	private void tentacle(Location base, int animationstep) {
		if (!TempBlock.isTempBlock(base.getBlock()))
			return;
		if (!blocks.contains(TempBlock.get(base.getBlock())))
			return;

		Vector direction = Methods.getDirection(player.getLocation(), base);
		direction.setY(0);
		direction.normalize();

		if (animationstep > 8) {
			animationstep = animationstep % 8;
		}

		if (y >= 1) {

			Block baseblock = base.clone().add(0, 1, 0).getBlock();

			if (animationstep == 1) {
				addWater(baseblock);
			} else if (animationstep == 2 || animationstep == 8) {
				addWater(baseblock);
			} else {
				addWater(base.clone().add(direction.getX(), 1, direction.getZ()).getBlock());
			}

		}

		if (y == 2) {

			Block baseblock = base.clone().add(0, 2, 0).getBlock();

			if (animationstep == 1) {
				addWater(base.clone().add(-direction.getX(), 2, -direction.getZ()).getBlock());
			} else if (animationstep == 3 || animationstep == 7	|| animationstep == 2 || animationstep == 8) {
				addWater(baseblock);
			} else if (animationstep == 4 || animationstep == 6) {
				addWater(base.clone().add(direction.getX(), 2, direction.getZ()).getBlock());
			} else {
				addWater(base.clone().add(2 * direction.getX(), 2, 2 * direction.getZ()).getBlock());
			}

		}
	}

	private void addWater(Block block) {
		clearNearbyWater(block);
		if (Methods.isRegionProtectedFromBuild(player, "OctopusForm", block.getLocation()))
			return;
		if (TempBlock.isTempBlock(block)) {
			TempBlock tblock = TempBlock.get(block);
			if (!newblocks.contains(tblock)) {
				if (!blocks.contains(tblock))
					tblock.setType(Material.WATER, full);
				newblocks.add(tblock);
			}
		} else if (Methods.isWaterbendable(block, player) || block.getType() == Material.FIRE || block.getType() == Material.AIR) {
			newblocks.add(new TempBlock(block, Material.STATIONARY_WATER, (byte) 8));
		}
	}

	// private static void replaceWater() {
	// boolean replace = true;
	// ArrayList<TempBlock> newwater = new ArrayList<TempBlock>();
	// for (TempBlock block : water) {
	// for (Player player : instances.keySet()) {
	// if (block.getLocation().distance(player.getLocation()) < 5) {
	// replace = false;
	// break;
	// }
	// }
	// if (replace) {
	// block.revertBlock();
	// } else {
	// newwater.add(block);
	// }
	// }
	// water.clear();
	// water.addAll(newwater);
	// }

	private void clearNearbyWater(Block block) {
		BlockFace[] faces = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST,	BlockFace.WEST, BlockFace.DOWN };
		for (BlockFace face : faces) {
			Block rel = block.getRelative(face);
			if (Methods.isWater(rel) && !TempBlock.isTempBlock(rel)) {
				FreezeMelt.freeze(player, rel);
				// water.add(new TempBlock(rel, Material.AIR, (byte) 0));
			}
		}
	}

	// private static boolean blockIsTouchingWater(Block block) {
	// BlockFace[] faces = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST,
	// BlockFace.WEST, BlockFace.DOWN };
	// for (BlockFace face : faces) {
	// Block rel = block.getRelative(face);
	// if (Methods.isWater(rel) && !TempBlock.isTempBlock(rel))
	// return true;
	// }
	// return false;
	// }

	public static boolean wasBrokenFor(Player player, Block block) {
		if (instances.containsKey(player)) {
			OctopusForm form = instances.get(player);
			if (form.sourceblock == null)
				return false;
			if (form.sourceblock.equals(block))
				return true;
		}
		return false;
	}

	private void remove() {
		if (source != null)
			source.revertBlock();
		for (TempBlock block : blocks)
			block.revertBlock();
		instances.remove(player);
	}

	private void returnWater() {
		if (source != null) {
			source.revertBlock();
			new WaterReturn(player, source.getLocation().getBlock());
			source = null;
		} else {
			Location location = player.getLocation();
			double rtheta = Math.toRadians(startangle);
			Block block = location.clone().add(new Vector(radius * Math.cos(rtheta), 0, radius * Math.sin(rtheta))).getBlock();
			new WaterReturn(player, block);
		}
	}

	public static void removeAll() {
		for (Player player : instances.keySet()) {
			instances.get(player).remove();
		}

		// for (TempBlock block : water)
		// block.revertBlock();
	}

	public static String getDescription() {
		return "This ability allows the waterbender to manipulate a large quantity of water into a form resembling that of an octopus. "
				+ "To use, click to select a water source. Then, hold sneak to channel this ability. "
				+ "While channeling, the water will form itself around you and has a chance to block incoming attacks. "
				+ "Additionally, you can click while channeling to attack things near you, dealing damage and knocking them back. "
				+ "Releasing shift at any time will dissipate the form.";
	}

	public Player getPlayer() {
		return player;
	}

	public int getRange() {
		return range;
	}

	public void setRange(int range) {
		this.range = range;
	}

	public int getDamage() {
		return damage;
	}

	public void setDamage(int damage) {
		this.damage = damage;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public double getKnockback() {
		return knockback;
	}

	public void setKnockback(double knockback) {
		this.knockback = knockback;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getAttackRange() {
		return attackRange;
	}

	public void setAttackRange(double attackRange) {
		this.attackRange = attackRange;
	}
	
	

}
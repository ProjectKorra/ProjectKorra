package com.projectkorra.projectkorra.waterbending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Random;

public class OctopusForm extends WaterAbility {

	private static final byte FULL = 8;

	private boolean sourceSelected;
	private boolean settingUp;
	private boolean forming;
	private boolean formed;
	private int range;
	private int damage;
	private int currentAnimationStep;
	private int stepCounter;
	private int totalStepCount;
	private long time;
	private long interval;
	private long cooldown;
	private double attackRange;
	private double knockback;
	private double radius;
	private double startAngle;
	private double angle;
	private double currentFormHeight;
	private double angleIncrement;
	private Block sourceBlock;
	private TempBlock source;	
	private Location sourceLocation;
	private ArrayList<TempBlock> blocks;
	private ArrayList<TempBlock> newBlocks;
	
	public OctopusForm(Player player) {
		super(player);
		
		OctopusForm oldOctopus = getAbility(player, OctopusForm.class);
		if (oldOctopus != null) {
			if (oldOctopus.formed) {
				oldOctopus.attack();
				return;
			} else if (oldOctopus.sourceSelected) {
				oldOctopus.remove();
			}
		}
		
		if (!bPlayer.canBend(this)) {
			remove();
			return;
		}
		
		this.sourceSelected = false;
		this.settingUp = false;
		this.forming = false;
		this.formed = false;
		this.currentAnimationStep = 1;
		this.stepCounter = 1;
		this.totalStepCount = 3;
		this.range = getConfig().getInt("Abilities.Water.OctopusForm.Range");
		this.damage = getConfig().getInt("Abilities.Water.OctopusForm.Damage");
		this.interval = getConfig().getLong("Abilities.Water.OctopusForm.FormDelay");
		this.attackRange = getConfig().getInt("Abilities.Water.OctopusForm.AttackRange");
		this.knockback = getConfig().getDouble("Abilities.Water.OctopusForm.Knockback");
		this.radius = getConfig().getDouble("Abilities.Water.OctopusForm.Radius");
		this.cooldown = getConfig().getLong("Abilities.Water.OctopusForm.Cooldown");
		this.angleIncrement = getConfig().getDouble("Abilities.Water.OctopusForm.AngleIncrement");
		this.currentFormHeight = 0;
		this.blocks = new ArrayList<TempBlock>();
		this.newBlocks = new ArrayList<TempBlock>();
		this.time = System.currentTimeMillis();
		if(!player.isSneaking()) {
			this.sourceBlock = BlockSource.getWaterSourceBlock(player, range, ClickType.LEFT_CLICK, true, true, bPlayer.canPlantbend());
		}
		
		if (sourceBlock != null) {
			sourceLocation = sourceBlock.getLocation();
			sourceSelected = true;
			start();
		}
	}

	private void incrementStep() {
		if (sourceSelected) {
			sourceSelected = false;
			settingUp = true;
			bPlayer.addCooldown(this);
		} else if (settingUp) {
			settingUp = false;
			forming = true;
		} else if (forming) {
			forming = false;
			formed = true;
			bPlayer.addCooldown(this);
		}
	}

	@SuppressWarnings("deprecation")
	public static void form(Player player) {
		OctopusForm oldForm = getAbility(player, OctopusForm.class);
		
		if (oldForm != null) {
			oldForm.form();
		} else if (WaterReturn.hasWaterBottle(player)) {
			Location eyeLoc = player.getEyeLocation();
			Block block = eyeLoc.add(eyeLoc.getDirection().normalize()).getBlock();
			
			if (isTransparent(player, block) && isTransparent(player, eyeLoc.getBlock())) {
				block.setType(Material.WATER);
				block.setData(FULL);
				OctopusForm form = new OctopusForm(player);
				form.form();
				
				if (form.formed || form.forming || form.settingUp) {
					WaterReturn.emptyWaterBottle(player);
				} else {
					block.setType(Material.AIR);
				}
			}
		}
	}

	private void form() {
		incrementStep();
		if (isPlant(sourceBlock) || isSnow(sourceBlock)) {
			new PlantRegrowth(player, sourceBlock);
			sourceBlock.setType(Material.AIR);
		} else if (!GeneralMethods.isAdjacentToThreeOrMoreSources(sourceBlock)) {
			sourceBlock.setType(Material.AIR);
		}
		source = new TempBlock(sourceBlock, Material.STATIONARY_WATER, (byte) 8);
	}

	private void attack() {
		if (!formed) {
			return;
		}
		double tentacleAngle = (new Vector(1, 0, 0)).angle(player.getEyeLocation().getDirection()) + angleIncrement / 2;

		for (double tangle = tentacleAngle; tangle < tentacleAngle + 360; tangle += angleIncrement) {
			double phi = Math.toRadians(tangle);
			affect(player.getLocation().clone().add(new Vector(radius * Math.cos(phi), 1, radius * Math.sin(phi))));
		}
	}

	private void affect(Location location) {
		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, attackRange)) {
			if (entity.getEntityId() == player.getEntityId()) {
				continue;
			} else if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation())) {
				continue;
			} else if (GeneralMethods.isObstructed(location, entity.getLocation())) {
				continue;
			}
			
			double knock = bPlayer.isAvatarState() ? AvatarState.getValue(knockback) : knockback;
			entity.setVelocity(GeneralMethods.getDirection(player.getLocation(), location).normalize().multiply(knock));
			
			if (entity instanceof LivingEntity) {
				DamageHandler.damageEntity(entity, damage, this);
			}
			AirAbility.breakBreathbendingHold(entity);
		}
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			remove();
			return;
		} else if (!player.isSneaking() && !sourceSelected) {
			remove();
			return;
		} else if (sourceBlock.getLocation().distanceSquared(player.getLocation()) > range * range && sourceSelected) {
			remove();
			return;
		}
		
		Random random = new Random();

		if (System.currentTimeMillis() > time + interval) {
			time = System.currentTimeMillis();
			Location location = player.getLocation();

			if (sourceSelected) {
				playFocusWaterEffect(sourceBlock);
			} else if (settingUp) {
				if (sourceBlock.getY() < location.getBlockY()) {
					source.revertBlock();
					source = null;
					Block newBlock = sourceBlock.getRelative(BlockFace.UP);
					sourceLocation = newBlock.getLocation();
					
					if (!GeneralMethods.isSolid(newBlock)) {
						source = new TempBlock(newBlock, Material.STATIONARY_WATER, (byte) 8);
						sourceBlock = newBlock;
					} else {
						remove();
						return;
					}
				} else if (sourceBlock.getY() > location.getBlockY()) {
					source.revertBlock();
					source = null;
					Block newBlock = sourceBlock.getRelative(BlockFace.DOWN);
					sourceLocation = newBlock.getLocation();
					
					if (!GeneralMethods.isSolid(newBlock)) {
						source = new TempBlock(newBlock, Material.STATIONARY_WATER, (byte) 8);
						sourceBlock = newBlock;
					} else {
						remove();
						return;
					}
				} else if (sourceLocation.distanceSquared(location) > radius * radius) {
					Vector vector = GeneralMethods.getDirection(sourceLocation, location.getBlock().getLocation()).normalize();
					sourceLocation.add(vector);
					Block newBlock = sourceLocation.getBlock();
					
					if (!newBlock.equals(sourceBlock)) {
						if (source != null) {
							source.revertBlock();
						}
						if (!GeneralMethods.isSolid(newBlock)) {
							source = new TempBlock(newBlock, Material.STATIONARY_WATER, (byte) 8);
							sourceBlock = newBlock;
						}
					}
				} else {
					incrementStep();
					if (source != null) {
						source.revertBlock();
					}
					
					source = null;
					Vector vector = new Vector(1, 0, 0);
					startAngle = vector.angle(GeneralMethods.getDirection(sourceBlock.getLocation(), location));
					angle = startAngle;
				}
			} else if (forming) {
				if (angle - startAngle >= 360) {
					currentFormHeight += 1;
				} else {
					angle += 20;
				}
				
				if (random.nextInt(4) == 0) {
					playWaterbendingSound(player.getLocation());
				}
				
				formOctopus();
				if (currentFormHeight == 2) {
					incrementStep();
				}
			} else if (formed) {
				if (random.nextInt(7) == 0) {
					playWaterbendingSound(player.getLocation());
				}
				
				stepCounter += 1;
				if (stepCounter % totalStepCount == 0) {
					currentAnimationStep += 1;
				}
				if (currentAnimationStep > 8) {
					currentAnimationStep = 1;
				}
				formOctopus();
			} else {
				remove();
				return;
			}
		}
	}

	private void formOctopus() {
		Location location = player.getLocation();
		newBlocks.clear();
		ArrayList<Block> doneBlocks = new ArrayList<Block>();

		for (double theta = startAngle; theta < startAngle + angle; theta += 10) {
			double rtheta = Math.toRadians(theta);
			Block block = location.clone().add(new Vector(radius * Math.cos(rtheta), 0, radius * Math.sin(rtheta))).getBlock();
			if (!doneBlocks.contains(block)) {
				addBaseWater(block);
				doneBlocks.add(block);
			}
		}
		for (int i = 0; i < 9; i++) {
			freezeBellow(player.getLocation().add(i / 3 - 1, 0, i % 3 - 1).getBlock());
		}

		Vector eyeDir = player.getEyeLocation().getDirection();
		eyeDir.setY(0);

		double tentacleAngle = Math.toDegrees((new Vector(1, 0, 0)).angle(eyeDir)) + angleIncrement / 2;
		int astep = currentAnimationStep;
		for (double tangle = tentacleAngle; tangle < tentacleAngle + 360; tangle += angleIncrement) {
			astep += 1;
			double phi = Math.toRadians(tangle);
			tentacle(location.clone().add(new Vector(radius * Math.cos(phi), 0, radius * Math.sin(phi))), astep);
		}

		for (TempBlock block : blocks) {
			if (!newBlocks.contains(block)) {
				block.revertBlock();
			}
		}

		blocks.clear();
		blocks.addAll(newBlocks);

		if (blocks.isEmpty()) {
			remove();
		}
	}

	private void tentacle(Location base, int animationstep) {
		if (!TempBlock.isTempBlock(base.getBlock())) {
			return;
		} else if (!blocks.contains(TempBlock.get(base.getBlock()))) {
			return;
		}

		Vector direction = GeneralMethods.getDirection(player.getLocation(), base);
		direction.setY(0);
		direction.normalize();

		if (animationstep > 8) {
			animationstep = animationstep % 8;
		}

		if (currentFormHeight >= 1) {
			Block baseBlock = base.clone().add(0, 1, 0).getBlock();
			if (animationstep == 1) {
				addWater(baseBlock);
			} else if (animationstep == 2 || animationstep == 8) {
				addWater(baseBlock);
			} else {
				addWater(base.clone().add(direction.getX(), 1, direction.getZ()).getBlock());
			}
		}

		if (currentFormHeight == 2) {
			Block baseBlock = base.clone().add(0, 2, 0).getBlock();
			if (animationstep == 1) {
				addWater(base.clone().add(-direction.getX(), 2, -direction.getZ()).getBlock());
			} else if (animationstep == 3 || animationstep == 7 || animationstep == 2 || animationstep == 8) {
				addWater(baseBlock);
			} else if (animationstep == 4 || animationstep == 6) {
				addWater(base.clone().add(direction.getX(), 2, direction.getZ()).getBlock());
			} else {
				addWater(base.clone().add(2 * direction.getX(), 2, 2 * direction.getZ()).getBlock());
			}

		}
	}

	private void addWater(Block block) {
		if (GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
			return;
		}
		
		if (TempBlock.isTempBlock(block)) {
			TempBlock tblock = TempBlock.get(block);
			if (!newBlocks.contains(tblock)) {
				if (!blocks.contains(tblock)) {
					tblock.setType(Material.WATER, FULL);
				}
				if (isWater(block) && !TempBlock.isTempBlock(block)) {
					ParticleEffect.WATER_BUBBLE.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0f, 5, block.getLocation().clone().add(0.5, 0.5, 0.5), 257D);
				} 
				newBlocks.add(tblock);
			}
		} else if (isWaterbendable(player, block) || block.getType() == Material.FIRE || block.getType() == Material.AIR) {
			if (isWater(block) && !TempBlock.isTempBlock(block)) {
				ParticleEffect.WATER_BUBBLE.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0f, 5, block.getLocation().clone().add(0.5, 0.5, 0.5), 257D);
			} 
			newBlocks.add(new TempBlock(block, Material.STATIONARY_WATER, (byte) 8));
		}
	}
	
	private void addBaseWater(Block block) {
		freezeBellow(block);
		addWater(block);
	}

	private void freezeBellow(Block block) {
		if (isWater(block.getRelative(BlockFace.DOWN)) && !GeneralMethods.isSolid(block) && !isWater(block)) {//&& !TempBlock.isTempBlock(block)) {
			PhaseChangeFreeze.freeze(player, block.getRelative(BlockFace.DOWN));
		}
	}

	public static boolean wasBrokenFor(Player player, Block block) {
		OctopusForm form = getAbility(player, OctopusForm.class);
		if (form != null) {
			if (form.sourceBlock == null) {
				return false;
			} else if (form.sourceBlock.equals(block)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void remove() {
		super.remove();
		returnWater();
		
		if (source != null) {
			source.revertBlock();
		}
		for (TempBlock block : blocks) {
			block.revertBlock();
		}
	}

	private void returnWater() {
		if (source != null) {
			source.revertBlock();
			new WaterReturn(player, source.getLocation().getBlock());
			source = null;
		} else {
			Location location = player.getLocation();
			double rtheta = Math.toRadians(startAngle);
			Block block = location.clone().add(new Vector(radius * Math.cos(rtheta), 0, radius * Math.sin(rtheta))).getBlock();
			new WaterReturn(player, block);
		}
	}

	@Override
	public Location getLocation() {
		if (sourceBlock != null) {
			return sourceBlock.getLocation();
		} else if (sourceLocation != null) {
			return sourceLocation;
		}
		return player != null ? player.getLocation() : null;
	}

	@Override
	public String getName() {
		return "OctopusForm";
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}
	
	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public boolean isSourceSelected() {
		return sourceSelected;
	}

	public void setSourceSelected(boolean sourceSelected) {
		this.sourceSelected = sourceSelected;
	}

	public boolean isSettingUp() {
		return settingUp;
	}

	public void setSettingUp(boolean settingUp) {
		this.settingUp = settingUp;
	}

	public boolean isForming() {
		return forming;
	}

	public void setForming(boolean forming) {
		this.forming = forming;
	}

	public boolean isFormed() {
		return formed;
	}

	public void setFormed(boolean formed) {
		this.formed = formed;
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

	public int getCurrentAnimationStep() {
		return currentAnimationStep;
	}

	public void setCurrentAnimationStep(int currentAnimationStep) {
		this.currentAnimationStep = currentAnimationStep;
	}

	public int getStepCounter() {
		return stepCounter;
	}

	public void setStepCounter(int stepCounter) {
		this.stepCounter = stepCounter;
	}

	public int getTotalStepCount() {
		return totalStepCount;
	}

	public void setTotalStepCount(int totalStepCount) {
		this.totalStepCount = totalStepCount;
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

	public double getAttackRange() {
		return attackRange;
	}

	public void setAttackRange(double attackRange) {
		this.attackRange = attackRange;
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

	public double getStartAngle() {
		return startAngle;
	}

	public void setStartAngle(double startAngle) {
		this.startAngle = startAngle;
	}

	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}

	public double getCurrentFormHeight() {
		return currentFormHeight;
	}

	public void setCurrentFormHeight(double currentFormHeight) {
		this.currentFormHeight = currentFormHeight;
	}

	public double getAngleIncrement() {
		return angleIncrement;
	}

	public void setAngleIncrement(double angleIncrement) {
		this.angleIncrement = angleIncrement;
	}

	public Block getSourceBlock() {
		return sourceBlock;
	}

	public void setSourceBlock(Block sourceBlock) {
		this.sourceBlock = sourceBlock;
	}

	public TempBlock getSource() {
		return source;
	}

	public void setSource(TempBlock source) {
		this.source = source;
	}

	public Location getSourceLocation() {
		return sourceLocation;
	}

	public void setSourceLocation(Location sourceLocation) {
		this.sourceLocation = sourceLocation;
	}

	public ArrayList<TempBlock> getBlocks() {
		return blocks;
	}

	public ArrayList<TempBlock> getNewBlocks() {
		return newBlocks;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}
	
}

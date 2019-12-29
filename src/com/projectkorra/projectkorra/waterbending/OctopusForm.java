package com.projectkorra.projectkorra.waterbending;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.ice.PhaseChange;
import com.projectkorra.projectkorra.waterbending.ice.PhaseChange.PhaseChangeType;
import com.projectkorra.projectkorra.waterbending.plant.PlantRegrowth;
import com.projectkorra.projectkorra.waterbending.util.WaterReturn;

public class OctopusForm extends WaterAbility {

	private boolean sourceSelected;
	private boolean settingUp;
	private boolean forming;
	private boolean formed;
	@Attribute(Attribute.RANGE)
	private int range;
	@Attribute(Attribute.DAMAGE)
	private int damage;
	private int currentAnimationStep;
	private int stepCounter;
	private int totalStepCount;
	private long time;
	private long startTime;
	private long interval;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute("Attack" + Attribute.RANGE)
	private double attackRange;
	@Attribute("Usage" + Attribute.COOLDOWN)
	private long usageCooldown;
	@Attribute(Attribute.KNOCKBACK)
	private double knockback;
	@Attribute(Attribute.RADIUS)
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
	private PhaseChange pc;

	public OctopusForm(final Player player) {
		super(player);

		final OctopusForm oldOctopus = getAbility(player, OctopusForm.class);
		if (oldOctopus != null) {
			if (oldOctopus.formed) {
				oldOctopus.attack();
				return;
			} else if (oldOctopus.sourceSelected) {
				oldOctopus.remove();
			}
		}

		if (!this.bPlayer.canBend(this)) {
			if (oldOctopus != null) {
				oldOctopus.remove();
			}
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
		this.usageCooldown = getConfig().getInt("Abilities.Water.OctopusForm.UsageCooldown");
		this.knockback = getConfig().getDouble("Abilities.Water.OctopusForm.Knockback");
		this.radius = getConfig().getDouble("Abilities.Water.OctopusForm.Radius");
		this.cooldown = getConfig().getLong("Abilities.Water.OctopusForm.Cooldown");
		this.duration = getConfig().getLong("Abilities.Water.OctopusForm.Duration");
		this.angleIncrement = getConfig().getDouble("Abilities.Water.OctopusForm.AngleIncrement");
		this.currentFormHeight = 0;
		this.blocks = new ArrayList<TempBlock>();
		this.newBlocks = new ArrayList<TempBlock>();
		if (hasAbility(player, PhaseChange.class)) {
			this.pc = getAbility(player, PhaseChange.class);
		} else {
			this.pc = new PhaseChange(player, PhaseChangeType.CUSTOM);
		}

		if (this.bPlayer.isAvatarState()) {
			this.damage = getConfig().getInt("Abilities.Avatar.AvatarState.Water.OctopusForm.Damage");
			this.attackRange = getConfig().getInt("Abilities.Avatar.AvatarState.Water.OctopusForm.AttackRange");
			this.knockback = getConfig().getDouble("Abilities.Avatar.AvatarState.Water.OctopusForm.Knockback");
			this.radius = getConfig().getDouble("Abilities.Avatar.AvatarState.Water.OctopusForm.Radius");
		}
		this.time = System.currentTimeMillis();
		this.startTime = System.currentTimeMillis();
		if (!player.isSneaking()) {
			this.sourceBlock = BlockSource.getWaterSourceBlock(player, this.range, ClickType.LEFT_CLICK, true, true, this.bPlayer.canPlantbend());
		}

		if (this.sourceBlock != null) {
			this.sourceLocation = this.sourceBlock.getLocation();
			this.sourceSelected = true;
			this.start();
		}
	}

	private void incrementStep() {
		if (this.sourceSelected) {
			this.sourceSelected = false;
			this.settingUp = true;
		} else if (this.settingUp) {
			this.settingUp = false;
			this.forming = true;
		} else if (this.forming) {
			this.forming = false;
			this.formed = true;
		}
	}

	public static void form(final Player player) {
		final OctopusForm oldForm = getAbility(player, OctopusForm.class);

		if (oldForm != null) {
			oldForm.form();
		} else if (WaterReturn.hasWaterBottle(player)) {
			final Location eyeLoc = player.getEyeLocation();
			final Block block = eyeLoc.add(eyeLoc.getDirection().normalize()).getBlock();

			if (isTransparent(player, block) && isTransparent(player, eyeLoc.getBlock())) {
				block.setType(Material.WATER);
				block.setBlockData(GeneralMethods.getWaterData(0));
				final OctopusForm form = new OctopusForm(player);
				form.setSourceBlock(block);
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
		this.incrementStep();
		if (isPlant(this.sourceBlock) || isSnow(this.sourceBlock)) {
			new PlantRegrowth(this.player, this.sourceBlock);
			this.sourceBlock.setType(Material.AIR);
		} else if (!GeneralMethods.isAdjacentToThreeOrMoreSources(this.sourceBlock) && this.sourceBlock != null) {
			this.sourceBlock.setType(Material.AIR);
		}
		this.source = new TempBlock(this.sourceBlock, Material.WATER, GeneralMethods.getWaterData(0));
	}

	private void attack() {
		if (!this.formed || this.bPlayer.isOnCooldown("OctopusAttack")) {
			return;
		}
		this.bPlayer.addCooldown("OctopusAttack", this.usageCooldown);
		final double tentacleAngle = (new Vector(1, 0, 0)).angle(this.player.getEyeLocation().getDirection()) + this.angleIncrement / 2;

		for (double tangle = tentacleAngle; tangle < tentacleAngle + 360; tangle += this.angleIncrement) {
			final double phi = Math.toRadians(tangle);
			this.affect(this.player.getLocation().clone().add(new Vector(this.radius * Math.cos(phi), 1, this.radius * Math.sin(phi))));
		}
	}

	private void affect(final Location location) {
		for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(location, this.attackRange)) {
			if (entity.getEntityId() == this.player.getEntityId()) {
				continue;
			} else if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation())) {
				continue;
			} else if (GeneralMethods.isObstructed(location, entity.getLocation())) {
				continue;
			}

			final double knock = this.bPlayer.isAvatarState() ? AvatarState.getValue(this.knockback) : this.knockback;
			entity.setVelocity(GeneralMethods.getDirection(this.player.getLocation(), location).normalize().multiply(knock));

			if (entity instanceof LivingEntity) {
				DamageHandler.damageEntity(entity, this.damage, this);
			}
			AirAbility.breakBreathbendingHold(entity);
		}
	}

	@Override
	public boolean allowBreakPlants() {
		return false;
	}

	@Override
	public void progress() {
		if (!this.bPlayer.canBendIgnoreCooldowns(this)) {
			this.remove();
			return;
		} else if (!this.player.isSneaking() && !this.sourceSelected) {
			this.remove();
			return;
		} else if (this.sourceBlock.getLocation().distanceSquared(this.player.getLocation()) > this.range * this.range && this.sourceSelected) {
			this.remove();
			return;
		} else if (this.duration != 0 && System.currentTimeMillis() > this.startTime + this.duration) {
			this.bPlayer.addCooldown(this);
			this.remove();
			return;
		}
		Block block = this.sourceBlock.getLocation().getBlock();
		if (!isWaterbendable(block) && !this.settingUp && !this.forming && !this.formed) {
			this.remove();
			return;
		}

		final Random random = new Random();

		if (System.currentTimeMillis() > this.time + this.interval) {
			this.time = System.currentTimeMillis();
			final Location location = this.player.getLocation();

			if (this.sourceSelected) {
				playFocusWaterEffect(this.sourceBlock);
			} else if (this.settingUp) {
				if (this.sourceBlock.getY() < location.getBlockY()) {
					this.source.revertBlock();
					this.source = null;
					final Block newBlock = this.sourceBlock.getRelative(BlockFace.UP);
					this.sourceLocation = newBlock.getLocation();

					if (!GeneralMethods.isSolid(newBlock)) {
						this.source = new TempBlock(newBlock, Material.WATER, GeneralMethods.getWaterData(0));
						this.sourceBlock = newBlock;
					} else {
						this.remove();
						return;
					}
				} else if (this.sourceBlock.getY() > location.getBlockY()) {
					this.source.revertBlock();
					this.source = null;
					final Block newBlock = this.sourceBlock.getRelative(BlockFace.DOWN);
					this.sourceLocation = newBlock.getLocation();

					if (!GeneralMethods.isSolid(newBlock)) {
						this.source = new TempBlock(newBlock, Material.WATER, GeneralMethods.getWaterData(0));
						this.sourceBlock = newBlock;
					} else {
						this.remove();
						return;
					}
				} else if (this.sourceLocation.distanceSquared(location) > this.radius * this.radius) {
					final Vector vector = GeneralMethods.getDirection(this.sourceLocation, location.getBlock().getLocation()).normalize();
					this.sourceLocation.add(vector);
					final Block newBlock = this.sourceLocation.getBlock();

					if (!newBlock.equals(this.sourceBlock)) {
						if (this.source != null) {
							this.source.revertBlock();
						}
						if (!GeneralMethods.isSolid(newBlock)) {
							this.source = new TempBlock(newBlock, Material.WATER, GeneralMethods.getWaterData(0));
							this.sourceBlock = newBlock;
						}
					}
				} else {
					this.incrementStep();
					if (this.source != null) {
						this.source.revertBlock();
					}

					this.source = null;
					final Vector vector = new Vector(1, 0, 0);
					this.startAngle = vector.angle(GeneralMethods.getDirection(this.sourceBlock.getLocation(), location));
					this.angle = this.startAngle;
				}
			} else if (this.forming) {
				if (this.angle - this.startAngle >= 360) {
					this.currentFormHeight += 1;
				} else {
					this.angle += 20;
				}

				if (random.nextInt(4) == 0) {
					playWaterbendingSound(this.player.getLocation());
				}

				this.formOctopus();
				if (this.currentFormHeight == 2) {
					this.incrementStep();
				}
			} else if (this.formed) {
				if (random.nextInt(7) == 0) {
					playWaterbendingSound(this.player.getLocation());
				}

				this.stepCounter += 1;
				if (this.stepCounter % this.totalStepCount == 0) {
					this.currentAnimationStep += 1;
				}
				if (this.currentAnimationStep > 8) {
					this.currentAnimationStep = 1;
				}
				this.formOctopus();
			} else {
				this.remove();
				return;
			}
		}
	}

	private void formOctopus() {
		final Location location = this.player.getLocation();
		this.newBlocks.clear();
		final ArrayList<Block> doneBlocks = new ArrayList<Block>();

		for (double theta = this.startAngle; theta < this.startAngle + this.angle; theta += 10) {
			final double rtheta = Math.toRadians(theta);
			final Block block = location.clone().add(new Vector(this.radius * Math.cos(rtheta), 0, this.radius * Math.sin(rtheta))).getBlock();
			if (!doneBlocks.contains(block)) {
				this.addBaseWater(block);
				doneBlocks.add(block);
			}
		}
		for (int i = 0; i < 9; i++) {
			this.freezeBelow(this.player.getLocation().add(i / 3 - 1, 0, i / 3 - 1).getBlock());
		}

		final Vector eyeDir = this.player.getEyeLocation().getDirection();
		eyeDir.setY(0);

		final double tentacleAngle = Math.toDegrees((new Vector(1, 0, 0)).angle(eyeDir)) + this.angleIncrement / 2;
		int astep = this.currentAnimationStep;
		for (double tangle = tentacleAngle; tangle < tentacleAngle + 360; tangle += this.angleIncrement) {
			astep += 1;
			final double phi = Math.toRadians(tangle);
			this.tentacle(location.clone().add(new Vector(this.radius * Math.cos(phi), 0, this.radius * Math.sin(phi))), astep);
		}

		for (final TempBlock block : this.blocks) {
			if (!this.newBlocks.contains(block)) {
				block.revertBlock();
			}
		}

		this.blocks.clear();
		this.blocks.addAll(this.newBlocks);

		if (this.blocks.isEmpty()) {
			this.remove();
		}
	}

	private void tentacle(final Location base, int animationstep) {
		if (!TempBlock.isTempBlock(base.getBlock())) {
			return;
		} else if (!this.blocks.contains(TempBlock.get(base.getBlock()))) {
			return;
		}

		final Vector direction = GeneralMethods.getDirection(this.player.getLocation(), base);
		direction.setY(0);
		direction.normalize();

		if (animationstep > 8) {
			animationstep = animationstep % 8;
		}

		if (this.currentFormHeight >= 1) {
			final Block baseBlock = base.clone().add(0, 1, 0).getBlock();
			if (animationstep == 1) {
				this.addWater(baseBlock);
			} else if (animationstep == 2 || animationstep == 8) {
				this.addWater(baseBlock);
			} else {
				this.addWater(base.clone().add(direction.getX(), 1, direction.getZ()).getBlock());
			}
		}

		if (this.currentFormHeight == 2) {
			final Block baseBlock = base.clone().add(0, 2, 0).getBlock();
			if (animationstep == 1) {
				this.addWater(base.clone().add(-direction.getX(), 2, -direction.getZ()).getBlock());
			} else if (animationstep == 3 || animationstep == 7 || animationstep == 2 || animationstep == 8) {
				this.addWater(baseBlock);
			} else if (animationstep == 4 || animationstep == 6) {
				this.addWater(base.clone().add(direction.getX(), 2, direction.getZ()).getBlock());
			} else {
				this.addWater(base.clone().add(2 * direction.getX(), 2, 2 * direction.getZ()).getBlock());
			}

		}
	}

	private void addWater(final Block block) {
		if (GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
			return;
		}

		if (TempBlock.isTempBlock(block)) {
			final TempBlock tblock = TempBlock.get(block);
			if (!this.newBlocks.contains(tblock) && !this.blocks.contains(tblock) && isBendableWaterTempBlock(tblock)) {
				if (!SurgeWave.canThaw(block)) {
					SurgeWave.thaw(block);
				}
				tblock.setType(Material.WATER, GeneralMethods.getWaterData(0));
				this.newBlocks.add(tblock);
			} else if (this.blocks.contains(tblock)) {
				this.newBlocks.add(tblock);
			}
		} else if (this.isWaterbendable(this.player, block) || block.getType() == Material.FIRE || isAir(block.getType())) {
			if (isWater(block) && !TempBlock.isTempBlock(block)) {
				ParticleEffect.WATER_BUBBLE.display(block.getLocation().clone().add(0.5, 0.5, 0.5), 5, Math.random(), Math.random(), Math.random(), 0);
			}
			this.newBlocks.add(new TempBlock(block, Material.WATER, GeneralMethods.getWaterData(0)));
		}
	}

	private void addBaseWater(final Block block) {
		this.freezeBelow(block);
		this.addWater(block);
	}

	private void freezeBelow(final Block block) {
		final Block toFreeze = block.getRelative(BlockFace.DOWN);
		if (isWater(toFreeze) && !TempBlock.isTempBlock(toFreeze)) {
			this.pc.freeze(toFreeze);
		}
	}

	public static boolean wasBrokenFor(final Player player, final Block block) {
		final OctopusForm form = getAbility(player, OctopusForm.class);
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
		this.returnWater();

		if (this.source != null) {
			this.source.revertBlock();
		}
		for (final TempBlock block : this.blocks) {
			block.revertBlock();
		}
		new BukkitRunnable() {

			@Override
			public void run() {
				OctopusForm.this.pc.remove();
			}

		}.runTaskLater(ProjectKorra.plugin, 1000);
	}

	private void returnWater() {
		if (this.source != null) {
			this.source.revertBlock();
			new WaterReturn(this.player, this.source.getLocation().getBlock());
			this.source = null;
		} else {
			final Location location = this.player.getLocation();
			final double rtheta = Math.toRadians(this.startAngle);
			final Block block = location.clone().add(new Vector(this.radius * Math.cos(rtheta), 0, this.radius * Math.sin(rtheta))).getBlock();
			new WaterReturn(this.player, block);
		}
	}

	@Override
	public Location getLocation() {
		if (this.sourceBlock != null) {
			return this.sourceBlock.getLocation();
		} else if (this.sourceLocation != null) {
			return this.sourceLocation;
		}
		return this.player != null ? this.player.getLocation() : null;
	}

	@Override
	public String getName() {
		return "OctopusForm";
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
	public double getCollisionRadius() {
		return this.getRadius();
	}

	public boolean isSourceSelected() {
		return this.sourceSelected;
	}

	public void setSourceSelected(final boolean sourceSelected) {
		this.sourceSelected = sourceSelected;
	}

	public boolean isSettingUp() {
		return this.settingUp;
	}

	public void setSettingUp(final boolean settingUp) {
		this.settingUp = settingUp;
	}

	public boolean isForming() {
		return this.forming;
	}

	public void setForming(final boolean forming) {
		this.forming = forming;
	}

	public boolean isFormed() {
		return this.formed;
	}

	public void setFormed(final boolean formed) {
		this.formed = formed;
	}

	public int getRange() {
		return this.range;
	}

	public void setRange(final int range) {
		this.range = range;
	}

	public int getDamage() {
		return this.damage;
	}

	public void setDamage(final int damage) {
		this.damage = damage;
	}

	public int getCurrentAnimationStep() {
		return this.currentAnimationStep;
	}

	public void setCurrentAnimationStep(final int currentAnimationStep) {
		this.currentAnimationStep = currentAnimationStep;
	}

	public int getStepCounter() {
		return this.stepCounter;
	}

	public void setStepCounter(final int stepCounter) {
		this.stepCounter = stepCounter;
	}

	public int getTotalStepCount() {
		return this.totalStepCount;
	}

	public void setTotalStepCount(final int totalStepCount) {
		this.totalStepCount = totalStepCount;
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

	public double getAttackRange() {
		return this.attackRange;
	}

	public void setAttackRange(final double attackRange) {
		this.attackRange = attackRange;
	}

	public double getKnockback() {
		return this.knockback;
	}

	public void setKnockback(final double knockback) {
		this.knockback = knockback;
	}

	public double getRadius() {
		return this.radius;
	}

	public void setRadius(final double radius) {
		this.radius = radius;
	}

	public double getStartAngle() {
		return this.startAngle;
	}

	public void setStartAngle(final double startAngle) {
		this.startAngle = startAngle;
	}

	public double getAngle() {
		return this.angle;
	}

	public void setAngle(final double angle) {
		this.angle = angle;
	}

	public double getCurrentFormHeight() {
		return this.currentFormHeight;
	}

	public void setCurrentFormHeight(final double currentFormHeight) {
		this.currentFormHeight = currentFormHeight;
	}

	public double getAngleIncrement() {
		return this.angleIncrement;
	}

	public void setAngleIncrement(final double angleIncrement) {
		this.angleIncrement = angleIncrement;
	}

	public Block getSourceBlock() {
		return this.sourceBlock;
	}

	public void setSourceBlock(final Block sourceBlock) {
		this.sourceBlock = sourceBlock;
	}

	public TempBlock getSource() {
		return this.source;
	}

	public void setSource(final TempBlock source) {
		this.source = source;
	}

	public Location getSourceLocation() {
		return this.sourceLocation;
	}

	public void setSourceLocation(final Location sourceLocation) {
		this.sourceLocation = sourceLocation;
	}

	public ArrayList<TempBlock> getBlocks() {
		return this.blocks;
	}

	public ArrayList<TempBlock> getNewBlocks() {
		return this.newBlocks;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

}

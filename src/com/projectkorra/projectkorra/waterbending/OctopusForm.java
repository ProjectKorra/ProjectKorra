package com.projectkorra.projectkorra.waterbending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.ice.PhaseChange;
import com.projectkorra.projectkorra.waterbending.util.WaterReturn;
import com.projectkorra.projectkorra.waterbending.util.WaterSource;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Finn Bon
 */
public class OctopusForm extends WaterAbility {

	private static final Vector POSITIVE_X = new Vector(1, 0, 0);
	private static final int TOTAL_STEP_COUNT = 3;

	public enum State {
		SOURCE_SELECTED,
		PULLING_SOURCE,
		FORMING,
		FORMED
	}

	private State state;
	private WaterSource source;
	private Location location;
	private long time;
	private TempBlock sourceTempBlock;
	private double angle, startAngle;
	private int currentFormHeight;
	private int stepCounter, currentAnimationStep;
	private List<TempBlock> animationBlocks;
	private PhaseChange phaseChange;

	private double sourceRange;
	private double damage;
	private long interval;
	private double attackRange;
	private int usageCooldown;
	private double knockback;
	private double radius;
	private long cooldown;
	private long duration;
	private double angleIncrement;

	private void setFields() {
		this.sourceRange = getConfig().getInt("Abilities.Water.OctopusForm.Range");
		this.damage = getConfig().getInt("Abilities.Water.OctopusForm.Damage");
		this.interval = getConfig().getLong("Abilities.Water.OctopusForm.FormDelay");
		this.attackRange = getConfig().getInt("Abilities.Water.OctopusForm.AttackRange");
		this.usageCooldown = getConfig().getInt("Abilities.Water.OctopusForm.UsageCooldown");
		this.knockback = getConfig().getDouble("Abilities.Water.OctopusForm.Knockback");
		this.radius = getConfig().getDouble("Abilities.Water.OctopusForm.Radius");
		this.cooldown = getConfig().getLong("Abilities.Water.OctopusForm.Cooldown");
		this.duration = getConfig().getLong("Abilities.Water.OctopusForm.Duration");
		this.angleIncrement = getConfig().getDouble("Abilities.Water.OctopusForm.AngleIncrement");

		animationBlocks = new LinkedList<>();
	}

	public OctopusForm(Player player, boolean selectSourceManually) {
		super(player);

		setFields();

		OctopusForm existingOctopusForm = getAbility(player, getClass());
		if (existingOctopusForm != null) {
			if (existingOctopusForm.state == State.SOURCE_SELECTED) {
				existingOctopusForm.remove();
			} else {
				existingOctopusForm.onLeftClick();
				return;
			}
		}

		if (bPlayer.isOnCooldown(getName())) {
			return;
		}

		// try to find a valid source for this torrent
		if (selectSourceManually) {
			// check for source in line of sight
			source = WaterSource.findManualSource(player, sourceRange, bPlayer.canPlantbend());
			state = State.SOURCE_SELECTED;
		} else {
			// find auto source
			source = WaterSource.findAutoSource(player, sourceRange, bPlayer.canPlantbend());
			state = State.PULLING_SOURCE;
		}

		if (source == null) {
			return;
		}

		if (state == State.PULLING_SOURCE) {
			location = source.use();
		}

		time = System.currentTimeMillis();

		if (hasAbility(player, PhaseChange.class)) {
			this.phaseChange = getAbility(player, PhaseChange.class);
		} else {
			this.phaseChange = new PhaseChange(player, PhaseChange.PhaseChangeType.CUSTOM);
		}

		start();
	}

	private void onLeftClick() {
		if (state != State.FORMED) return;
		attack();
	}

	public static void onSneak(Player player) {
		OctopusForm existingOctopusForm = getAbility(player, OctopusForm.class);
		if (existingOctopusForm == null) {
			new OctopusForm(player, false);
		}
	}

	private void handleSourceSelected() {
		if (!source.isValid(sourceRange)) {
			remove();
			return;
		}
		source.playIndicator();
		if (player.isSneaking()) {
			state = State.PULLING_SOURCE;
			location = source.use();
			sourceTempBlock = new TempBlock(location.getBlock(), Material.WATER);
		}
	}

	private void handlePullingSource() {
		if (!player.isSneaking()) {
			remove();
			return;
		}
		Location target = player.getLocation();

		double heightDiff = target.getBlockY() - this.location.getBlockY();
		heightDiff = heightDiff == 0 ? 0 : Math.abs(heightDiff) / heightDiff;

		if (heightDiff != 0) {
			this.location.add(0, heightDiff, 0);
		} else {
			this.location.add(GeneralMethods.getDirection(this.location, target).normalize());
		}

		// for whatever reason, progressing the location was not enough to move it outside of the old block, so skip all checks until next iteration
		if (this.sourceTempBlock != null && this.location.getBlock() == this.sourceTempBlock.getBlock()) {
			return;
		}

		if (GeneralMethods.isSolid(this.location.getBlock())) {
			remove();
			return;
		}

		if (sourceTempBlock != null) sourceTempBlock.revertBlock();

		if (this.location.distanceSquared(target) < radius * radius) {
			state = State.FORMING;
			sourceTempBlock = null;
			this.startAngle = POSITIVE_X.angle(GeneralMethods.getDirection(location, target));
			this.angle = this.startAngle;
		} else {
			this.sourceTempBlock = new TempBlock(location.getBlock(), Material.WATER);
		}

	}

	private void handleForm() {
		if (!player.isSneaking()) {
			remove();
			return;
		}
		this.location = player.getLocation();
		if (this.angle - this.startAngle >= 360) {
			if (currentFormHeight < 2) currentFormHeight++;
		} else {
			this.angle += 20;
		}

		if (ThreadLocalRandom.current().nextInt(state == State.FORMING ? 4 : 7) == 0) {
			playWaterbendingSound(this.player.getLocation());
		}

		this.formOctopus();

		if(currentFormHeight < 2) return;

		this.stepCounter += 1;
		if (this.stepCounter % TOTAL_STEP_COUNT == 0) {
			this.currentAnimationStep += 1;
		}
		if (this.currentAnimationStep > 8) {
			this.currentAnimationStep = 1;
		}

	}

	private void formOctopus() {
		animationBlocks.forEach(TempBlock::revertBlock);
		animationBlocks.clear();

		Location location = this.player.getLocation();
		ArrayList<Block> doneBlocks = new ArrayList<Block>();

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

		if (this.animationBlocks.isEmpty()) {
			this.remove();
		}
	}

	private void tentacle(Location base, int animationstep) {
		if (!TempBlock.isTempBlock(base.getBlock())) {
			return;
		} else if (!this.animationBlocks.contains(TempBlock.get(base.getBlock()))) {
			return;
		}

		Vector direction = GeneralMethods.getDirection(this.player.getLocation(), base);
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

	private void addBaseWater(Block block) {
		this.freezeBelow(block);
		this.addWater(block);
	}

	private void addWater(Block block) {
		if (GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
			return;
		}

		TempBlock tempBlock = TempBlock.get(block);
		if (TempBlock.isTempBlock(block) && tempBlock != null) {
			if (!this.animationBlocks.contains(tempBlock) && isBendableWaterTempBlock(tempBlock)) {
				if (!SurgeWave.canThaw(block)) {
					SurgeWave.thaw(block);
				}
				tempBlock.setType(Material.WATER);
				this.animationBlocks.add(tempBlock);
			}
		} else if (this.isWaterbendable(this.player, block) || FireAbility.isFire(block.getType()) || isAir(block.getType())) {
			if (isWater(block) && !TempBlock.isTempBlock(block)) {
				ParticleEffect.WATER_BUBBLE.display(block.getLocation().clone().add(0.5, 0.5, 0.5), 5, Math.random(), Math.random(), Math.random(), 0);
			}
			this.animationBlocks.add(new TempBlock(block, GeneralMethods.getWaterData(0)));
		}
	}

	private void freezeBelow(final Block block) {
		final Block toFreeze = block.getRelative(BlockFace.DOWN);
		if (isWater(toFreeze) && !TempBlock.isTempBlock(toFreeze)) {
			this.phaseChange.freeze(toFreeze);
		}
	}

	private void attack() {
		if (this.bPlayer.isOnCooldown("OctopusAttack")) {
			return;
		}
		this.bPlayer.addCooldown("OctopusAttack", this.usageCooldown);
		double tentacleAngle = (new Vector(1, 0, 0)).angle(this.player.getEyeLocation().getDirection()) + this.angleIncrement / 2;

		for (double tangle = tentacleAngle; tangle < tentacleAngle + 360; tangle += this.angleIncrement) {
			double phi = Math.toRadians(tangle);
			this.affect(this.player.getLocation().clone().add(new Vector(this.radius * Math.cos(phi), 1, this.radius * Math.sin(phi))));
		}
	}

	private void affect(Location location) {
		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, this.attackRange)) {
			if (entity.getEntityId() == this.player.getEntityId()) {
				continue;
			} else if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation())) {
				continue;
			} else if (GeneralMethods.isObstructed(location, entity.getLocation())) {
				continue;
			}

			double knock = this.bPlayer.isAvatarState() ? AvatarState.getValue(this.knockback) : this.knockback;
			entity.setVelocity(GeneralMethods.getDirection(this.player.getLocation(), location).normalize().multiply(knock));

			if (entity instanceof LivingEntity) {
				DamageHandler.damageEntity(entity, this.damage, this);
			}
			AirAbility.breakBreathbendingHold(entity);
		}
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		if (
			location != null &&
			location.getWorld() != player.getLocation().getWorld()
		) {
			remove();
			return;
		}
		if (canBeSource() ? !bPlayer.canBendIgnoreBinds(this) : !bPlayer.canBend(this)) {
			remove();
			return;
		}
		switch (state) {
			case SOURCE_SELECTED:
				handleSourceSelected();
				break;
			case PULLING_SOURCE:
				handlePullingSource();
				break;
			case FORMING:
			case FORMED:
				handleForm();
				break;
		}
	}

	@Override
	public void remove() {
		super.remove();
		this.returnWater();

		if (sourceTempBlock != null) {
			this.sourceTempBlock.revertBlock();
		}

		for (TempBlock block : this.animationBlocks) {
			block.revertBlock();
		}

		new BukkitRunnable() {

			@Override
			public void run() {
				OctopusForm.this.phaseChange.remove();
			}

		}.runTaskLater(ProjectKorra.plugin, 1000);
	}

	private void returnWater() {
		if (isBeingReused()) return;
		if (this.sourceTempBlock != null) {
			new WaterReturn(this.player, this.sourceTempBlock.getLocation().getBlock());
		} else {
			Location location = this.player.getLocation();
			double theta = Math.toRadians(this.startAngle);
			Block block = location.clone().add(new Vector(this.radius * Math.cos(theta), 0, this.radius * Math.sin(theta))).getBlock();
			new WaterReturn(this.player, block);
		}
	}

	@Override
	public boolean canBeSource() {
		return state == State.FORMED || state == State.FORMING;
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
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public String getName() {
		return "OctopusForm";
	}

	@Override
	public Location getLocation() {
		return location;
	}
}

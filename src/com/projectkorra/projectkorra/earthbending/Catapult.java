package com.projectkorra.projectkorra.earthbending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.util.ParticleEffect;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Random;

public class Catapult extends EarthAbility {

	private double stageTimeMult;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	private Location origin;
	private Location target;

	private int stage;
	private long stageStart;
	private boolean charging;
	private boolean activationHandled;
	private Vector up;
	private double angle;
	private boolean cancelWithAngle;
	private BlockData bentBlockData;

	public Catapult(final Player player, final boolean sneak) {
		super(player);
		this.setFields();
		final Block b = player.getLocation().getBlock().getRelative(BlockFace.DOWN, 1);
		if (!(isEarth(b) || isSand(b) || isMetal(b))) {
			return;
		}

		this.bentBlockData = b.getBlockData();

		if (!this.bPlayer.canBend(this)) {
			return;
		}

		if (this.bPlayer.isAvatarState()) {
			this.cooldown = getConfig().getLong("Abilities.Avatar.AvatarState.Earth.Catapult.Cooldown");
		}

		this.charging = sneak;
		this.start();
	}

	private void setFields() {
		this.stageTimeMult = getConfig().getDouble("Abilities.Earth.Catapult.StageTimeMult");
		this.cooldown = getConfig().getLong("Abilities.Earth.Catapult.Cooldown");
		this.angle = Math.toRadians(getConfig().getDouble("Abilities.Earth.Catapult.Angle"));
		this.cancelWithAngle = getConfig().getBoolean("Abilities.Earth.Catapult.CancelWithAngle");
		this.activationHandled = false;
		this.stage = 1;
		this.stageStart = System.currentTimeMillis();
		this.up = new Vector(0, 1, 0);
	}

	private void moveEarth(final Vector apply, final Vector direction) {
		for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(this.origin, 2)) {
			if (entity.getEntityId() != this.player.getEntityId()) {
				if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation()) || ((entity instanceof Player) && Commands.invincible.contains(((Player) entity).getName()))) {
					continue;
				}
				GeneralMethods.setVelocity(this, entity, apply);
			}
		}
		this.moveEarth(this.origin.clone().subtract(direction), direction, 3, false);
	}

	@Override
	public void progress() {
		if (!this.bPlayer.canBendIgnoreBindsCooldowns(this)) {
			this.remove();
			return;
		}

		final Block b = this.player.getLocation().getBlock().getRelative(BlockFace.DOWN, 1);
		if (!(isEarth(b) || isSand(b) || isMetal(b))) {
			this.remove();
			return;
		}

		this.bentBlockData = b.getBlockData();

		if (this.charging) {
			if (this.stage == 4 || !this.player.isSneaking()) {
				this.charging = false;
			} else {
				if ((System.currentTimeMillis() - this.stageStart) >= ((Math.max(0, this.stageTimeMult * (this.stage - 1))) * 1000)) {
					this.stage++;
					this.stageStart = System.currentTimeMillis();
					final Random random = new Random();
					ParticleEffect.BLOCK_DUST.display(this.player.getLocation(), 15, random.nextFloat(), random.nextFloat(), random.nextFloat(), this.bentBlockData);
					ParticleEffect.BLOCK_DUST.display(this.player.getLocation().add(0, 0.5, 0), 10, random.nextFloat(), random.nextFloat(), random.nextFloat(), this.bentBlockData);
					this.player.getWorld().playEffect(this.player.getLocation(), Effect.GHAST_SHOOT, 0, 10);
				}
			}
			return;
		}

		Vector direction = null;
		if (!this.activationHandled) {
			this.origin = this.player.getLocation().clone();
			direction = this.player.getEyeLocation().getDirection().clone().normalize();

			if (!this.bPlayer.canBend(this)) {
				this.activationHandled = true;
				this.remove();
				return;
			}
			this.activationHandled = true;
			this.bPlayer.addCooldown(this);
		}

		if (this.up.angle(this.player.getEyeLocation().getDirection()) > this.angle) {
			if (this.cancelWithAngle) {
				this.remove();
				return;
			}
			direction = this.up;
		}

		final Location tar = this.origin.clone().add(direction.clone().normalize().multiply(this.stage + 0.5));
		this.target = tar;
		final Vector apply = this.target.clone().toVector().subtract(this.origin.clone().toVector());
		GeneralMethods.setVelocity(this, this.player, apply);
		this.moveEarth(apply, direction);
		this.remove();
	}

	@Override
	public String getName() {
		return "Catapult";
	}

	@Override
	public Location getLocation() {
		if (this.player != null) {
			return this.player.getLocation();
		}
		return null;
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

	public Location getOrigin() {
		return this.origin;
	}

	public void setOrigin(final Location origin) {
		this.origin = origin;
	}

	public Location getTarget() {
		return this.target;
	}

	public void setTarget(final Location target) {
		this.target = target;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}
}

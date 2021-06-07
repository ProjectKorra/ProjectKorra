package com.projectkorra.projectkorra.firebending.combo;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.firebending.util.FireDamageTimer;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

/***
 * Is only here for legacy purposes. All fire combos used to use a form of this
 * stream for all their progress methods. If someone else was reliant on that,
 * they can use this ability instead.
 */
public class FireComboStream extends BukkitRunnable {
	private boolean useNewParticles;
	private boolean cancelled;
	private boolean collides;
	private boolean singlePoint;
	private int density;
	private int checkCollisionDelay;
	private int checkCollisionCounter;
	private float spread;
	private double collisionRadius;
	private final double speed;
	private final double distance;
	private double damage;
	private double fireTicks;
	private double knockback;
	ParticleEffect particleEffect;
	private final Player player;
	private final BendingPlayer bPlayer;
	private final CoreAbility coreAbility;
	private final Vector direction;
	private final Location initialLocation;
	private final Location location;

	public FireComboStream(final Player player, final CoreAbility coreAbility, final Vector direction, final Location location, final double distance, final double speed) {
		this.useNewParticles = false;
		this.cancelled = false;
		this.collides = true;
		this.singlePoint = false;
		this.density = 1;
		this.checkCollisionDelay = 1;
		this.checkCollisionCounter = 0;
		this.spread = 0;
		this.collisionRadius = 2;
		this.player = player;
		this.bPlayer = BendingPlayer.getBendingPlayer(player);
		this.particleEffect = bPlayer.canUseSubElement(SubElement.BLUE_FIRE) ? ParticleEffect.SOUL_FIRE_FLAME : ParticleEffect.FLAME;
		this.coreAbility = coreAbility;
		this.direction = direction;
		this.speed = speed;
		this.initialLocation = location.clone();
		this.location = location.clone();
		this.distance = distance;
	}

	@Override
	public void run() {
		final Block block = this.location.getBlock();
		if (!ElementalAbility.isAir(block.getRelative(BlockFace.UP).getType()) && !ElementalAbility.isPlant(block)) {
			this.remove();
			return;
		}
		for (int i = 0; i < this.density; i++) {
			if (this.useNewParticles) {
				this.particleEffect.display(this.location, 1, this.spread, this.spread, this.spread);
			} else {
				this.location.getWorld().playEffect(this.location, Effect.MOBSPAWNER_FLAMES, 0, 15);
			}
		}

		if (GeneralMethods.checkDiagonalWall(this.location, this.direction)) {
			this.remove();
			return;
		}

		this.location.add(this.direction.normalize().multiply(this.speed));
		
		try {
			this.location.checkFinite();
		} catch (IllegalArgumentException e) {
			this.remove();
			return;
		}
		
		if (this.initialLocation.distanceSquared(this.location) > this.distance * this.distance || !Double.isFinite(this.collisionRadius)) {
			this.remove();
			return;
		} else if (this.collides && this.checkCollisionCounter % this.checkCollisionDelay == 0) {
			for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(this.location, this.collisionRadius)) {
				if (entity instanceof LivingEntity && !entity.equals(this.coreAbility.getPlayer()) && !entity.isDead()) {
					this.collision((LivingEntity) entity, this.direction, this.coreAbility);
				}
			}
		}

		this.checkCollisionCounter++;
		if (this.singlePoint) {
			this.remove();
		}
	}

	public void collision(final LivingEntity entity, final Vector direction, final CoreAbility coreAbility) {
		if (GeneralMethods.isRegionProtectedFromBuild(this.player, "Blaze", entity.getLocation())) {
			return;
		}
		entity.getLocation().getWorld().playSound(entity.getLocation(), Sound.ENTITY_VILLAGER_HURT, 0.3f, 0.3f);

		if (coreAbility.getName().equalsIgnoreCase("FireKick")) {
			final FireKick fireKick = CoreAbility.getAbility(this.player, FireKick.class);

			if (!fireKick.getAffectedEntities().contains(entity)) {
				fireKick.getAffectedEntities().add(entity);
				DamageHandler.damageEntity(entity, this.damage, coreAbility);
			}
		} else if (coreAbility.getName().equalsIgnoreCase("FireSpin")) {
			final FireSpin fireSpin = CoreAbility.getAbility(this.player, FireSpin.class);

			if (entity instanceof Player) {
				if (Commands.invincible.contains(((Player) entity).getName())) {
					return;
				}
			}
			if (!fireSpin.getAffectedEntities().contains(entity)) {
				fireSpin.getAffectedEntities().add(entity);
				final double newKnockback = this.bPlayer.isAvatarState() ? this.knockback + 0.5 : this.knockback;
				DamageHandler.damageEntity(entity, this.damage, coreAbility);
				GeneralMethods.setVelocity(coreAbility, entity, direction.normalize().multiply(newKnockback));
			}
		} else if (coreAbility.getName().equalsIgnoreCase("JetBlaze")) {
			final JetBlaze jetBlaze = CoreAbility.getAbility(this.player, JetBlaze.class);

			if (!jetBlaze.getAffectedEntities().contains(entity)) {
				jetBlaze.getAffectedEntities().add(entity);
				DamageHandler.damageEntity(entity, this.damage, coreAbility);
				entity.setFireTicks((int) (this.fireTicks * 20));
				new FireDamageTimer(entity, this.player);
			}
		} else if (coreAbility.getName().equalsIgnoreCase("FireWheel")) {
			final FireWheel fireWheel = CoreAbility.getAbility(this.player, FireWheel.class);

			if (!fireWheel.getAffectedEntities().contains(entity)) {
				fireWheel.getAffectedEntities().add(entity);
				DamageHandler.damageEntity(entity, this.damage, coreAbility);
				entity.setFireTicks((int) (this.fireTicks * 20));
				new FireDamageTimer(entity, this.player);
				this.remove();
			}
		}
	}

	@Override
	public void cancel() {
		this.remove();
	}

	public Vector getDirection() {
		return this.direction.clone();
	}

	public Location getLocation() {
		return this.location;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	public void remove() {
		super.cancel();
		this.cancelled = true;
	}

	public CoreAbility getAbility() {
		return this.coreAbility;
	}

	public void setCheckCollisionDelay(final int delay) {
		this.checkCollisionDelay = delay;
	}

	public void setCollides(final boolean b) {
		this.collides = b;
	}

	public void setCollisionRadius(final double radius) {
		this.collisionRadius = radius;
	}

	public void setDensity(final int density) {
		this.density = density;
	}

	public void setDamage(final double damage) {
		this.damage = damage;
	}

	public void setKnockback(final double knockback) {
		this.knockback = knockback;
	}

	public void setFireTicks(final double fireTicks) {
		this.fireTicks = fireTicks;
	}

	public void setParticleEffect(final ParticleEffect effect) {
		this.particleEffect = effect;
	}

	public void setSinglePoint(final boolean b) {
		this.singlePoint = b;
	}

	public void setSpread(final float spread) {
		this.spread = spread;
	}

	public void setUseNewParticles(final boolean b) {
		this.useNewParticles = b;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
}

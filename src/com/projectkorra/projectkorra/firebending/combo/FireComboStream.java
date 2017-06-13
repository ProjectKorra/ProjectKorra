package com.projectkorra.projectkorra.firebending.combo;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.firebending.util.FireDamageTimer;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

/***
 * Is only here for legacy purposes. All fire combos
 * used to use a form of this stream for all their
 * progress methods. If someone else was reliant on
 * that, they can use this ability instead.
 */
public class FireComboStream extends BukkitRunnable  {
	private boolean useNewParticles;
	private boolean cancelled;
	private boolean collides;
	private boolean singlePoint;
	private int density;
	private int checkCollisionDelay;
	private int checkCollisionCounter;
	private float spread;
	private double collisionRadius;
	private double speed;
	private double distance;
	private double damage;
	private double fireTicks;
	private double knockback;
	ParticleEffect particleEffect;
	private Player player;
	private BendingPlayer bPlayer;
	private CoreAbility coreAbility;
	private Vector direction;
	private Location initialLocation;
	private Location location;
	
	public FireComboStream(Player player, CoreAbility coreAbility, Vector direction, Location location, double distance, double speed) {
		this.useNewParticles = false;
		this.cancelled = false;
		this.collides = true;
		this.singlePoint = false;
		this.density = 1;
		this.checkCollisionDelay = 1;
		this.checkCollisionCounter = 0;
		this.spread = 0;
		this.collisionRadius = 2;
		this.particleEffect = ParticleEffect.FLAME;
		this.player = player;
		this.bPlayer = BendingPlayer.getBendingPlayer(player);
		this.coreAbility = coreAbility;
		this.direction = direction;
		this.speed = speed;
		this.initialLocation = location.clone();
		this.location = location.clone();
		this.distance = distance;
	}
	
	@Override
	public void run() {
		Block block = location.getBlock();
		if (block.getRelative(BlockFace.UP).getType() != Material.AIR && !ElementalAbility.isPlant(block)) {
			remove();
			return;
		}
		for (int i = 0; i < density; i++) {
			if (useNewParticles) {
				particleEffect.display(location, spread, spread, spread, 0, 1);
			} else {
				location.getWorld().playEffect(location, Effect.MOBSPAWNER_FLAMES, 0, 15);
			}
		}

		location.add(direction.normalize().multiply(speed));
		if (initialLocation.distanceSquared(location) > distance * distance) {
			remove();
			return;
		} else if (collides && checkCollisionCounter % checkCollisionDelay == 0) {
			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, collisionRadius)) {
				if (entity instanceof LivingEntity && !entity.equals(coreAbility.getPlayer())) {
					collision((LivingEntity) entity, direction, coreAbility);
				}
			}
		}

		checkCollisionCounter++;
		if (singlePoint) {
			remove();
		}
	}
	
	public void collision(LivingEntity entity, Vector direction, CoreAbility coreAbility) {
		if (GeneralMethods.isRegionProtectedFromBuild(player, "Blaze", entity.getLocation())) {
			return;
		}
		entity.getLocation().getWorld().playSound(entity.getLocation(), Sound.ENTITY_VILLAGER_HURT, 0.3f, 0.3f);

		if (coreAbility.getName().equalsIgnoreCase("FireKick")) {
			FireKick fireKick = CoreAbility.getAbility(player, FireKick.class);
			
			if (!fireKick.getAffectedEntities().contains(entity)) {
				fireKick.getAffectedEntities().add(entity);
				DamageHandler.damageEntity(entity, damage, coreAbility);
				coreAbility.remove();
			}
		} else if (coreAbility.getName().equalsIgnoreCase("FireSpin")) {
			FireSpin fireSpin = (FireSpin) CoreAbility.getAbility(player, FireSpin.class);
			
			if (entity instanceof Player) {
				if (Commands.invincible.contains(((Player) entity).getName())) {
					return;
				}
			}
			if (!fireSpin.getAffectedEntities().contains(entity)) {
				fireSpin.getAffectedEntities().add(entity);
				double newKnockback = bPlayer.isAvatarState() ? knockback + 0.5 : knockback;
				DamageHandler.damageEntity(entity, damage, coreAbility);
				entity.setVelocity(direction.normalize().multiply(newKnockback));
				coreAbility.remove();
			}
		} else if (coreAbility.getName().equalsIgnoreCase("JetBlaze")) {
			JetBlaze jetBlaze = (JetBlaze) CoreAbility.getAbility(player, JetBlaze.class);
			
			if (!jetBlaze.getAffectedEntities().contains(entity)) {
				jetBlaze.getAffectedEntities().add(entity);
				DamageHandler.damageEntity(entity, damage, coreAbility);
				entity.setFireTicks((int) (fireTicks * 20));
				new FireDamageTimer(entity, player);
			}
		} else if (coreAbility.getName().equalsIgnoreCase("FireWheel")) {
			FireWheel fireWheel = (FireWheel) CoreAbility.getAbility(player, FireWheel.class);
			
			if (!fireWheel.getAffectedEntities().contains(entity)) {
				fireWheel.getAffectedEntities().add(entity);
				DamageHandler.damageEntity(entity, damage, coreAbility);
				entity.setFireTicks((int) (fireTicks * 20));
				new FireDamageTimer(entity, player);
				this.remove();
			}
		}
	}
	
	@Override
	public void cancel() {
		remove();
	}

	public Vector getDirection() {
		return this.direction.clone();
	}

	public Location getLocation() {
		return this.location;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void remove() {
		super.cancel();
		this.cancelled = true;
	}
	
	public CoreAbility getAbility() {
		return coreAbility;
	}

	public void setCheckCollisionDelay(int delay) {
		this.checkCollisionDelay = delay;
	}

	public void setCollides(boolean b) {
		this.collides = b;
	}

	public void setCollisionRadius(double radius) {
		this.collisionRadius = radius;
	}

	public void setDensity(int density) {
		this.density = density;
	}
	
	public void setDamage(double damage) {
		this.damage = damage;
	}
	
	public void setKnockback(double knockback) {
		this.knockback = knockback;
	}
	
	public void setFireTicks(double fireTicks) {
		this.fireTicks = fireTicks;
	}

	public void setParticleEffect(ParticleEffect effect) {
		this.particleEffect = effect;
	}

	public void setSinglePoint(boolean b) {
		this.singlePoint = b;
	}

	public void setSpread(float spread) {
		this.spread = spread;
	}

	public void setUseNewParticles(boolean b) {
		useNewParticles = b;
	}
}

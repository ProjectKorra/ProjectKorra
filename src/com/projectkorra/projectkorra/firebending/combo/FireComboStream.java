package com.projectkorra.projectkorra.firebending.combo;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.util.ParticleEffect;

@Deprecated
/***
 * Is only here for legacy purposes. All fire combos
 * used to use a form of this stream for all their
 * progress methods. If someone else was reliant on
 * that, they can use this ability instead.
 */
public class FireComboStream extends FireAbility {

	public FireCombo baseAbility;
	
	private boolean useNewParticles;
	private boolean collides;
	private boolean singlePoint;
	private int density;
	private int checkCollisionDelay;
	private int checkCollisionCounter;
	private float spread;
	private double collisionRadius;
	private double speed;
	private double distance;
	ParticleEffect particleEffect;
	private FireComboLegacy fireCombo;
	private Vector direction;
	private Location initialLocation;
	private Location location;
	private FireStreamCollision collision;
	
	public FireComboStream(Player player, FireCombo base, Vector direction, Location location, double distance, double speed) {
		super(player);
		
		this.baseAbility = base;
		
		this.useNewParticles = false;
		this.collides = true;
		this.singlePoint = false;
		this.density = 1;
		this.checkCollisionDelay = 1;
		this.checkCollisionCounter = 0;
		this.spread = 0;
		this.collisionRadius = 2;
		this.particleEffect = ParticleEffect.FLAME;
		this.direction = direction;
		this.speed = speed;
		this.initialLocation = location.clone();
		this.location = location.clone();
		this.distance = distance;
	}

	@Override
	public void progress() {
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
				if (entity instanceof LivingEntity && !entity.equals(fireCombo.getPlayer())) {
					if (collision != null) {
						collision.run();
					}
				}
			}
		}

		checkCollisionCounter++;
		if (singlePoint) {
			remove();
		}
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public String getName() {
		return "FireComboStream";
	}

	@Override
	public Location getLocation() {
		return location;
	}
	
	@Override
	public boolean isHiddenAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}
	
	public Vector getDirection() {
		return this.direction.clone();
	}

	public FireCombo getBaseAbility() {
		return this.baseAbility;
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
	
	public abstract class FireStreamCollision implements Runnable {
		
		protected FireComboStream stream;
		public FireStreamCollision(FireComboStream stream) {
			this.stream = stream;
		}
		@Override
		public abstract void run();
		
	}
	
	public FireStreamCollision getCollision() {
		return collision;
	}
	
	public void setCollision(FireStreamCollision collision) {
		this.collision = collision;
	}
}

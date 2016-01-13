package com.projectkorra.projectkorra.firebending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.util.ParticleEffect;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class FireBlastCharged extends FireAbility {

	private static ConcurrentHashMap<Entity, FireBlastCharged> EXPLOSIONS = new ConcurrentHashMap<Entity, FireBlastCharged>();

	private boolean charged;
	private boolean launched;
	private boolean canDamageBlocks;
	private boolean dissipate;
	private long time;
	private long chargeTime;
	private long interval;	
	private double maxDamage;
	private double range;
	private double radius;
	private double damageRadius;
	private double explosionRadius;
	private double innerRadius;
	private double fireTicks;
	private TNTPrimed explosion;
	private Location origin;
	private Location location;
	private Vector direction;
	
	public FireBlastCharged(Player player) {
		super(player);
		
		this.charged = false;
		this.launched = false;
		this.canDamageBlocks = getConfig().getBoolean("Abilities.Fire.FireBlast.Charged.DamageBlocks");
		this.dissipate = getConfig().getBoolean("Abilities.Fire.FireBlast.Dissipate");
		this.chargeTime = getConfig().getLong("Abilities.Fire.FireBlast.Charged.ChargeTime");
		this.time = System.currentTimeMillis();
		this.interval = 25;
		this.radius = 2;
		this.maxDamage = getConfig().getDouble("Abilities.Fire.FireBlast.Charged.Damage");
		this.range = getConfig().getDouble("Abilities.Fire.FireBlast.Charged.Range");
		this.damageRadius = getConfig().getDouble("Abilities.Fire.FireBlast.Charged.DamageRadius");
		this.explosionRadius = getConfig().getDouble("Abilities.Fire.FireBlast.Charged.ExplosionRadius");
		this.fireTicks = getConfig().getDouble("Abilities.Fire.FireBlast.Charged.FireTicks");
		this.innerRadius = damageRadius / 2;
		
		if (isDay(player.getWorld())) {
			this.chargeTime = (long) (chargeTime / getDayFactor());
		}
		if (bPlayer.isAvatarState()) {
			this.chargeTime = 0;
			this.maxDamage = AvatarState.getValue(maxDamage);
		}
		
		this.range = getDayFactor(range);
		if (!player.getEyeLocation().getBlock().isLiquid()) {
			start();
		}
	}

	public static boolean annihilateBlasts(Location location, double radius, Player source) {
		boolean broke = false;
		for (FireBlastCharged chargedBlast : getAbilities(FireBlastCharged.class)) {
			if (!chargedBlast.launched) {
				continue;
			}
			
			Location fireBlastLocation = chargedBlast.location;
			if (location.getWorld().equals(fireBlastLocation.getWorld()) && !source.equals(chargedBlast.player)) {
				if (location.distanceSquared(fireBlastLocation) <= radius * radius) {
					chargedBlast.explode();
					broke = true;
				}
			}
		}
		return broke;
	}

	public static FireBlastCharged getFireball(Entity entity) {
		return entity != null ? EXPLOSIONS.get(entity) : null;
	}

	public static boolean isCharging(Player player) {
		for (FireBlastCharged chargedBlast : getAbilities(player, FireBlastCharged.class)) {
			if (!chargedBlast.launched) {
				return true;
			}
		}
		return false;
	}

	public static void removeFireballsAroundPoint(Location location, double radius) {
		for (FireBlastCharged fireball : getAbilities(FireBlastCharged.class)) {
			if (!fireball.launched) {
				continue;
			}
			Location fireblastlocation = fireball.location;
			if (location.getWorld().equals(fireblastlocation.getWorld())) {
				if (location.distanceSquared(fireblastlocation) <= radius * radius) {
					fireball.remove();
				}
			}
		}
	}

	public void dealDamage(Entity entity) {
		if (explosion == null) {
			return;
		}
		
		double distance = entity.getLocation().distance(explosion.getLocation());
		if (distance > damageRadius) {
			return;
		} else if (distance < innerRadius) {
			GeneralMethods.damageEntity(player, entity, maxDamage, "FireBlast");
			return;
		}
		
		double slope = -(maxDamage * .5) / (damageRadius - innerRadius);
		double damage = slope * (distance - innerRadius) + maxDamage;
		GeneralMethods.damageEntity(this, entity, damage);
		AirAbility.breakBreathbendingHold(entity);
	}

	public void explode() {
		boolean explode = true;
		for (Block block : GeneralMethods.getBlocksAroundPoint(location, 3)) {
			if (GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
				explode = false;
				break;
			}
		}
		
		if (explode) {
			if (canDamageBlocks && explosionRadius > 0) {
				explosion = player.getWorld().spawn(location, TNTPrimed.class);
				explosion.setFuseTicks(0);
				double yield = explosionRadius;
				
				if (!bPlayer.isAvatarState()) {
					yield = getDayFactor(yield, player.getWorld());
				} else {
					yield = AvatarState.getValue(yield);
				}
				
				if (!bPlayer.isAvatarState()) {
					if (isDay(player.getWorld())) {
						yield = (float) getDayFactor(yield);
					}
				} else {
					yield = AvatarState.getValue(yield);
				}
				explosion.setYield((float) yield);
				EXPLOSIONS.put(explosion, this);
			} else {
				List<Entity> entities = GeneralMethods.getEntitiesAroundPoint(location, damageRadius);
				for (Entity entity : entities) {
					if (entity instanceof LivingEntity) {
						double slope = -(maxDamage * .5) / (damageRadius - innerRadius);
						double damage = slope * (entity.getLocation().distance(location) - innerRadius) + maxDamage;
						GeneralMethods.damageEntity(this, entity, damage);
					}
				}
				location.getWorld().playSound(location, Sound.EXPLODE, 5, 1);
				ParticleEffect.EXPLOSION_HUGE.display(new Vector(0, 0, 0), 0, location, 256);
			}
		}

		ignite(location);
		remove();
	}

	private void executeFireball() {
		for (Block block : GeneralMethods.getBlocksAroundPoint(location, radius)) {
			ParticleEffect.FLAME.display(block.getLocation(), 0.5F, 0.5F, 0.5F, 0, 5);
			ParticleEffect.SMOKE.display(block.getLocation(), 0.5F, 0.5F, 0.5F, 0, 2);
			if ((new Random()).nextInt(4) == 0) {
				playFirebendingSound(location);
			}

		}

		boolean exploded = false;
		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2 * radius)) {
			if (entity.getEntityId() == player.getEntityId()
					|| GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation())) {
				continue;
			}
			entity.setFireTicks((int) (fireTicks * 20));
			if (entity instanceof LivingEntity) {
				if (!exploded) {
					explode();
					exploded = true;
				}
				dealDamage(entity);
			}
		}
	}

	private void ignite(Location location) {
		for (Block block : GeneralMethods.getBlocksAroundPoint(location, radius)) {
			if (BlazeArc.isIgnitable(player, block)) {
				if (block.getType() != Material.FIRE) {
					BlazeArc.getReplacedBlocks().put(block.getLocation(), block.getState().getData());
				}
				block.setType(Material.FIRE);
				if (dissipate) {
					BlazeArc.getIgnitedBlocks().put(block, player);
					BlazeArc.getIgnitedTimes().put(block, System.currentTimeMillis());
				}
			}
		}
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBindsCooldowns(this) && !launched) {
			remove();
			return;
		} else if (!player.isSneaking() && !charged) {
			remove();
			return;
		}
		
		if (System.currentTimeMillis() > startTime + chargeTime) {
			charged = true;
		}
		if (!player.isSneaking() && !launched) {
			launched = true;
			location = player.getEyeLocation();
			origin = location.clone();
			direction = location.getDirection().normalize().multiply(radius);
		}

		if (System.currentTimeMillis() > time + interval) {
			if (launched) {
				if (GeneralMethods.isRegionProtectedFromBuild(this, location)) {
					remove();
					return;
				}
			}

			time = System.currentTimeMillis();

			if (!launched && !charged) {
				return;
			} else if (!launched) {
				player.getWorld().playEffect(player.getEyeLocation(), Effect.MOBSPAWNER_FLAMES, 0, 3);
				return;
			}

			location = location.clone().add(direction);
			if (location.distanceSquared(origin) > range * range) {
				remove();
				return;
			}

			if (GeneralMethods.isSolid(location.getBlock())) {
				explode();
				return;
			} else if (location.getBlock().isLiquid()) {
				remove();
				return;
			}
			executeFireball();
		}
	}

	@Override
	public String getName() {
		return "FireBlast";
	}

	@Override
	public Location getLocation() {
		return location != null ? location : origin;
	}

	@Override
	public long getCooldown() {
		return 0;
	}
	
	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

}

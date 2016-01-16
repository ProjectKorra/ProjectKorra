package com.projectkorra.projectkorra.firebending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.earthbending.EarthBlast;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.waterbending.WaterManipulation;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Random;

public class FireShield extends FireAbility {
	
	private boolean shield;
	private boolean ignite;
	private long time;
	private long duration;
	private long interval;
	private long cooldown;
	private double radius;
	private double discRadius;
	private double fireTicks;
	private Random random;

	public FireShield(Player player) {
		this(player, false);
	}

	public FireShield(Player player, boolean shield) {
		super(player);
		
		this.shield = shield;
		this.ignite = true;
		this.interval = getConfig().getLong("Abilities.Fire.FireShield.Interval");
		this.cooldown = shield ? 0 : getConfig().getLong("Abilities.Fire.FireShield.Cooldown");
		this.duration = getConfig().getLong("Abilities.Fire.FireShield.Duration");
		this.radius = getConfig().getDouble("Abilities.Fire.FireShield.Radius");
		this.discRadius = getConfig().getDouble("Abilities.Fire.FireShield.DiscRadius");
		this.fireTicks = getConfig().getDouble("Abilities.Fire.FireShield.FireTicks");
		this.random = new Random();
		
		if (hasAbility(player, FireShield.class) || bPlayer.isOnCooldown("FireShield")) {
			return;
		} else if (!player.getEyeLocation().getBlock().isLiquid()) {
			time = System.currentTimeMillis();
			start();
			if (!shield) {
				bPlayer.addCooldown(this);
			}
		}
	}

	public static boolean isWithinShield(Location loc) {
		for (FireShield fshield : getAbilities(FireShield.class)) {
			Location playerLoc = fshield.player.getLocation();

			if (fshield.shield) {
				if (!playerLoc.getWorld().equals(loc.getWorld())) {
					return false;
				} else if (playerLoc.distanceSquared(loc) <= fshield.radius * fshield.radius) {
					return true;
				}
			} else {
				Location tempLoc = playerLoc.clone().add(playerLoc.multiply(fshield.discRadius));
				if (!tempLoc.getWorld().equals(loc.getWorld())) {
					return false;
				} else if (tempLoc.distance(loc) <= fshield.discRadius * fshield.discRadius) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			remove();
			return;
		} else if (!player.isSneaking() && shield) {
			remove();
			return;
		} else if (System.currentTimeMillis() > startTime + duration && !shield) {
			remove();
			return;
		}

		if (System.currentTimeMillis() > time + interval) {
			time = System.currentTimeMillis();

			if (shield) {
				ArrayList<Block> blocks = new ArrayList<>();
				Location location = player.getEyeLocation().clone();

				for (double theta = 0; theta < 180; theta += 20) {
					for (double phi = 0; phi < 360; phi += 20) {
						double rphi = Math.toRadians(phi);
						double rtheta = Math.toRadians(theta);
						
						Block block = location .clone() .add(radius * Math.cos(rphi) * Math.sin(rtheta), radius * Math.cos(rtheta),
										radius * Math.sin(rphi) * Math.sin(rtheta)).getBlock();
						if (!blocks.contains(block) && !GeneralMethods.isSolid(block) && !block.isLiquid()) {
							blocks.add(block);
						}
					}
				}

				for (Block block : blocks) {
					if (!GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
						if (random.nextInt(3) == 0) {
							ParticleEffect.SMOKE.display(block.getLocation(), 0.6F, 0.6F, 0.6F, 0, 1);
						}
						ParticleEffect.FLAME.display(block.getLocation(), 0.6F, 0.6F, 0.6F, 0, 1);
						if (random.nextInt(7) == 0) {
							playFirebendingSound(block.getLocation());
						}
					}
				}

				for (Block testblock : GeneralMethods.getBlocksAroundPoint(player.getLocation(), radius)) {
					if (testblock.getType() == Material.FIRE) {
						testblock.setType(Material.AIR);
						testblock.getWorld().playEffect(testblock.getLocation(), Effect.EXTINGUISH, 0);
					}
				}

				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, radius)) {
					if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation())) {
						continue;
					} else if (player.getEntityId() != entity.getEntityId() && ignite) {
						entity.setFireTicks(120);
						new FireDamageTimer(entity, player);
					}
				}

				FireBlast.removeFireBlastsAroundPoint(location, radius + 1);
				BlazeArc.removeAroundPoint(location, radius + 1);
				Combustion.removeAroundPoint(location, radius + 1);
			} else {
				ArrayList<Block> blocks = new ArrayList<>();
				Location location = player.getEyeLocation().clone();
				Vector direction = location.getDirection();
				location = location.clone().add(direction.multiply(radius));

				for (double theta = 0; theta < 360; theta += 20) {
					Vector vector = GeneralMethods.getOrthogonalVector(direction, theta, discRadius);
					Block block = location.clone().add(vector).getBlock();
					if (!blocks.contains(block) && !GeneralMethods.isSolid(block) && !block.isLiquid()) {
						blocks.add(block);
					}
				}

				for (Block block : blocks) {
					if (!GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
						if (random.nextInt(1) == 0) {
							ParticleEffect.SMOKE.display(block.getLocation(), 0.6F, 0.6F, 0.6F, 0, 1);
						}
						ParticleEffect.FLAME.display(block.getLocation(), 0.6F, 0.6F, 0.6F, 0, 3);
						if (random.nextInt(4) == 0) {
							playFirebendingSound(block.getLocation());
						}
					}
				}

				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, discRadius)) {
					if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation())) {
						continue;
					}
					if (player.getEntityId() != entity.getEntityId() && ignite) {
						entity.setFireTicks((int) (fireTicks * 20));
						if (!(entity instanceof LivingEntity)) {
							entity.remove();
						}
					}
				}

				FireBlast.removeFireBlastsAroundPoint(location, discRadius);
				WaterManipulation.removeAroundPoint(location, discRadius);
				EarthBlast.removeAroundPoint(location, discRadius);
				BlazeArc.removeAroundPoint(location, discRadius);
				Combustion.removeAroundPoint(location, discRadius);
				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, discRadius)) {
					if (entity instanceof Projectile) {
						entity.remove();
					}
				}
			}
		}
	}

	@Override
	public String getName() {
		return "FireShield";
	}

	@Override
	public Location getLocation() {
		return player != null ? player.getLocation() : null;
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

	public boolean isShield() {
		return shield;
	}

	public void setShield(boolean shield) {
		this.shield = shield;
	}

	public boolean isIgnite() {
		return ignite;
	}

	public void setIgnite(boolean ignite) {
		this.ignite = ignite;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getDiscRadius() {
		return discRadius;
	}

	public void setDiscRadius(double discRadius) {
		this.discRadius = discRadius;
	}

	public double getFireTicks() {
		return fireTicks;
	}

	public void setFireTicks(double fireTicks) {
		this.fireTicks = fireTicks;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}
		
}

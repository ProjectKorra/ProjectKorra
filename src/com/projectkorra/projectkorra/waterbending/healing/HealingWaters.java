package com.projectkorra.projectkorra.waterbending.healing;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.HealingAbility;
import com.projectkorra.projectkorra.chiblocking.Smokescreen;
import com.projectkorra.projectkorra.util.ActionBar;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.util.WaterReturn;

public class HealingWaters extends HealingAbility {

	// Configurable Variables

	private long cooldown;
	private double range;
	private long interval;
	private long chargeTime;
	private int power;
	private int potDuration;
	private long duration;

	// Instance related and predefined variables.

	private LivingEntity target;
	private Location origin;
	private Location location;
	private long lastCheck;
	private double angle = 0;
	private double yAngle = (3*Math.PI)/2;
	private boolean charged = false;
	private boolean bottle = false;
	private boolean hasReached = false;
	private boolean healingSelf = true;
	private String hex;

	public HealingWaters(Player player) {
		super(player);

		setFields();
		this.origin = GeneralMethods.getRightSide(player.getLocation(), 0.5).add(0, 1.1, 0);
		this.location = origin.clone();
		this.lastCheck = System.currentTimeMillis();
		this.bottle = (isInWater(player) ? false : WaterReturn.hasWaterBottle(player));
		this.target = player;
		
		start();
	}

	public void setFields() {
		cooldown = getConfig().getLong("Abilities.Water.HealingWaters.Cooldown");
		range = getConfig().getDouble("Abilities.Water.HealingWaters.Range");
		interval = getConfig().getLong("Abilities.Water.HealingWaters.Interval");
		chargeTime = getConfig().getLong("Abilities.Water.HealingWaters.ChargeTime");
		power = getConfig().getInt("Abilities.Water.HealingWaters.Power");
		potDuration = getConfig().getInt("Abilities.Water.HealingWaters.HealingDuration");
		duration = getConfig().getLong("Abilities.Water.HealingWaters.Duration");
		hex = "00ffff";
	}

	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		}
		
		if (player.getLocation().distance(target.getLocation()) > range) {
			remove();
			return;
		}
		
		if (duration > 0) {
			if (System.currentTimeMillis() >= getStartTime() + duration) {
				remove();
				return;
			}
		}

		if (!player.isSneaking()) {
			remove();
			return;
		}

		if (!isInWater(player) && !bottle) {
			remove();
			return;
		}

		if (!healingSelf) {
			Entity e = GeneralMethods.getTargetedEntity(player, range);
			if (e != null) {
				if (e instanceof LivingEntity && e.getEntityId() != target.getEntityId()) {
					LivingEntity le = (LivingEntity) e;
					if (le.getHealth() < le.getMaxHealth()) {
						target = le;
						hasReached = false;
					} else {
						target = player;
					}
				}
			} else {
				target = player;
			}
			
			if (target.getEntityId() == player.getEntityId()) {
				hasReached = false;
			}
		} else {
			if (target.getEntityId() != player.getEntityId()) {
				target = player;
			}
		}
		
		if (charged) {
			if (!hasReached) {
				project();
			} else {
				if (System.currentTimeMillis() - lastCheck >= interval) {
					lastCheck = System.currentTimeMillis();
					affect();
				}
				
				displayParticles();
			}
		} else {
			if (System.currentTimeMillis() >= getStartTime() + chargeTime) {
				this.charged = true;
				if (bottle && !isInWater(player)) {
					WaterReturn.emptyWaterBottle(player);
				}
			}
		}
	}

	private void affect() {
		if (target instanceof Player) {
			if (!((Player)target).isOnline()) {
				target = player;
				return;
			}
		}
		
		if (target.isDead()) {
			target = player;
			return;
		}
		
		if (target.getHealth() < target.getMaxHealth()) {
			target.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, potDuration, power, false, false));
			AirAbility.breakBreathbendingHold(target);
		} else {
			target = player;
			return;
		}

		for (PotionEffect effect : target.getActivePotionEffects()) {
			if (isNegativeEffect(effect.getType())) {
				if (target instanceof Player) {
					Player ePlayer = (Player) target;
					if ((effect.getType() == PotionEffectType.BLINDNESS) && Smokescreen.getBlindedTimes().containsKey(player.getName())) {
						Smokescreen.removeFromHashMap(ePlayer);
					}
				}
				target.removePotionEffect(effect.getType());
			}
		}
	}

	private boolean isInWater(Player player) {
		Block block = player.getLocation().getBlock();
		return isWater(block) && !TempBlock.isTempBlock(block);
	}

	private void displayParticles() {
		double radius = target.getHeight()/2;
		Location display = target.getLocation().clone().add(0, radius, 0);
		double increment = Math.PI/15;
		angle += increment;
		yAngle += increment/(1 + Math.random());
		double x = 0.5 * Math.cos(angle);
		double z = 0.5 * Math.sin(angle);
		double y = radius * Math.sin(yAngle);
		display.add(x, y, z);
		GeneralMethods.displayColoredParticle(display, hex);
	}

	private void project() {
		double factor = 0.2;
		
		if (!healingSelf && target.getEntityId() == player.getEntityId()) {
			GeneralMethods.displayColoredParticle(GeneralMethods.getRightSide(player.getLocation(), 0.3).add(0, 0.45, 0), hex);
			GeneralMethods.displayColoredParticle(GeneralMethods.getLeftSide(player.getLocation(), 0.3).add(0, 0.45, 0), hex);
		} else if (healingSelf && player.getHealth() >= player.getMaxHealth()) {
			GeneralMethods.displayColoredParticle(GeneralMethods.getRightSide(player.getLocation(), 0.3).add(0, 0.45, 0), hex);
			GeneralMethods.displayColoredParticle(GeneralMethods.getLeftSide(player.getLocation(), 0.3).add(0, 0.45, 0), hex);
		} else {
			Location targetLoc = target.getLocation().clone().add(0, 1, 0);
			if (location.getWorld().equals(targetLoc.getWorld())) {
				Vector vec = GeneralMethods.getDirection(location, targetLoc).normalize();
		
				if (location.getWorld().equals(targetLoc.getWorld())) {
					location.add(vec.clone().multiply(factor));
					if (location.distance(targetLoc) <= 0.5) {
						hasReached = true;
					} else {
						hasReached = false;
					}
				}
		
				GeneralMethods.displayColoredParticle(location, hex);
			}
		}
	}
	
	public void switchMode() {
		this.healingSelf = !healingSelf;
		String message = "Healing (s)";
		if (healingSelf) {
			target = player;
			message = message.replace("(s)", "Self");
		} else {
			message = message.replace("(s)", "Target");
		}
		ActionBar.sendActionBar(Element.HEALING.getColor() + message, player);
	}

	@Override
	public void remove() {
		super.remove();
		if (bottle && charged) {
			if (target == null) {
				target = player;
			}
			new WaterReturn(player, target.getLocation().getBlock());
		}
		bPlayer.addCooldown(this);
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public String getName() {
		return "HealingWaters";
	}

	@Override
	public Location getLocation() {
		return location;
	}

}

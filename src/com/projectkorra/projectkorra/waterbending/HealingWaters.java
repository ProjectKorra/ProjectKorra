package com.projectkorra.projectkorra.waterbending;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.HealingAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.chiblocking.Smokescreen;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

public class HealingWaters extends HealingAbility {
	
	// Configurable Variables
	
	private long cooldown;
	private double range;
	private long interval;
	private long chargeTime;
	private int power;
	private int duration;
	private boolean enableParticles;
	
	// Instance related and predefined variables.
	
	private Player player;
	private LivingEntity target;
	private Location location;
	private long currTime;
	private int pstage;
	private int tstage;
	private int lstage;
	private boolean healing;
	private boolean healingSelf;
	private String hex;

	public HealingWaters(Player player) {
		super(player);
		
		if (!bPlayer.canBend(this)) {
			remove();
			return;
		}
		
		setFields();
		this.player = player;
		this.location = player.getLocation();
		this.currTime = System.currentTimeMillis();
		this.pstage = 0;
		this.tstage = 0;
		this.lstage = 0;
		
		start();
	}
	
	public void setFields() {
		
		cooldown = getConfig().getLong("Abilities.Water.HealingWaters.Cooldown");
		range = getConfig().getDouble("Abilities.Water.HealingWaters.Range");
		interval = getConfig().getLong("Abilities.Water.HealingWaters.Interval");
		chargeTime = getConfig().getLong("Abilities.Water.HealingWaters.ChargeTime");
		power = getConfig().getInt("Abilities.Water.HealingWaters.Power");
		duration = getConfig().getInt("Abilities.Water.HealingWaters.Duration");
		enableParticles = getConfig().getBoolean("Abilities.Water.HealingWaters.EnableParticles");
		hex = "00ffff";
	}

	@Override
	public void progress() {
		
		if (!bPlayer.canBend(this)) {
			remove();
			return;
		}
		
		if (!player.isSneaking()) {
			remove();
			return;
		}
		
		if (!inWater(player)) {
			remove();
			return;
		}
		
		// If ability is charging.
		if (System.currentTimeMillis() < startTime + chargeTime) {
			
			ParticleEffect.SMOKE.display(player.getLocation().clone().add(player.getLocation().getDirection()).add(0, 1.5, 0), 0, 0, 0, 0, 1);
			return;
		}
		
		// Try to heal them with 'interval' millisecond intervals.
		if (System.currentTimeMillis() - currTime >= interval) {
			
			currTime = System.currentTimeMillis(); 
			heal(player);
		} 
		
		// Display healing particles.
		if (healing && enableParticles) {
			if (healingSelf) {
				displayHealingParticlesSelf();
			} else {
				displayHealingParticlesOther();
			}
		}
	}
	
	private void heal(Player player) {
		Entity target = GeneralMethods.getTargetedEntity(player, range);
		if (target == null || !(target instanceof LivingEntity)) {
			giveHP(player);
		} else {
			giveHP((LivingEntity) target);
		}
	}
	
	private void giveHP(Player player) {
		if (!player.isDead()) {
			if (player.getHealth() < player.getMaxHealth()) {
				applyHealing(player);
			} else {
				healing = false;
			}
		}
		
		for (PotionEffect effect : player.getActivePotionEffects()) {
			if (isNegativeEffect(effect.getType())) {
				if ((effect.getType() == PotionEffectType.BLINDNESS) && Smokescreen.getBlindedTimes().containsKey(player.getName())) {
					return;
				}
				player.removePotionEffect(effect.getType());
			}
		}
	}
	
	private void giveHP(LivingEntity livingEntity) {
		if (!livingEntity.isDead()) {
			if (livingEntity.getHealth() < livingEntity.getMaxHealth()) {
				applyHealing(livingEntity);
			} else {
				healing = false;
			}
		}
		
		for (PotionEffect effect : livingEntity.getActivePotionEffects()) {
			if (WaterAbility.isNegativeEffect(effect.getType())) {
				livingEntity.removePotionEffect(effect.getType());
			}
		}
	}
	
	private void applyHealing(Player player) {
		if (!GeneralMethods.isRegionProtectedFromBuild(player, "HealingWaters", player.getLocation())) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, power));
			AirAbility.breakBreathbendingHold(player);
			healing = true;
			healingSelf = true;
		}
	}
	
	private void applyHealing(LivingEntity livingEntity) {
		if (livingEntity.getHealth() < livingEntity.getMaxHealth()) {
			livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, 1));
			AirAbility.breakBreathbendingHold(livingEntity);
			this.target = livingEntity;
			healing = true;
			healingSelf = false;
		}
	}
	
	private boolean inWater(Player player) {
		Block block = player.getLocation().getBlock();
		return isWater(block) && !TempBlock.isTempBlock(block);
	}
	
	public void displayHealingParticlesSelf() {
		
		Location centre = player.getLocation().clone().add(0, 1, 0);
		double increment = (2 * Math.PI) / 36;
		double angle = pstage * increment;
		double x = centre.getX() + (0.75 * Math.cos(angle));
		double z = centre.getZ() + (0.75 * Math.sin(angle));
		GeneralMethods.displayColoredParticle(new Location(centre.getWorld(), x, centre.getY(), z), hex);
			
		if (pstage >= 36) {
			pstage = 0;
		}
		pstage++;
	}
	
	public void displayHealingParticlesOther() {
		
		if (target != null) {
			
			Location centre = target.getLocation().clone().add(0, 1, 0);
			double increment = (2 * Math.PI) / 36;
			double angle = tstage * increment;
			double x = centre.getX() + (0.75 * Math.cos(angle));
			double z = centre.getZ() + (0.75 * Math.sin(angle));
			
			GeneralMethods.displayColoredParticle(new Location(centre.getWorld(), x, centre.getY() + (0.75 * Math.cos(angle)), z), hex);
			GeneralMethods.displayColoredParticle(new Location(centre.getWorld(), x, centre.getY() + (0.75 * -Math.cos(angle)), z), hex);
			
			if (tstage >= 36) {
				tstage = 0;
			}
			tstage++;
			
			float f = 0.3F;
			
			double distance = player.getLocation().distance(target.getLocation());
			Vector vec = new Vector(
					target.getLocation().getX() - player.getLocation().getX(),
					target.getLocation().getY() - player.getLocation().getY(),
					target.getLocation().getZ() - player.getLocation().getZ()).normalize();
			
			if (lstage < distance) {
				GeneralMethods.displayColoredParticle(player.getLocation().clone().add(vec.clone().multiply(f * lstage)), hex);
				lstage++;
			}
			
		}
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
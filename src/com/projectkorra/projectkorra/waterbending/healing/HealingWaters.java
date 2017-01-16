package com.projectkorra.projectkorra.waterbending.healing;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.HealingAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.chiblocking.Smokescreen;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.util.WaterReturn;

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
	private Location origin;
	private Location location;
	private long currTime;
	private int pstage;
	private int tstage1;
	private int tstage2;
	private boolean healing = false;
	private boolean healingSelf = false;
	public boolean charged = false;
	private boolean bottle = false;
	private boolean hasReached = false;
	private String hex;

	public HealingWaters(Player player) {
		super(player);

		if (!bPlayer.canBend(this)) {
			remove();
			return;
		}

		setFields();
		this.player = player;
		this.origin = player.getLocation().clone().add(player.getLocation().getDirection()).add(0, 1.5, 0);
		this.location = origin.clone();
		this.currTime = System.currentTimeMillis();
		this.pstage = 0;
		this.tstage1 = 0;
		this.tstage2 = 18;

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

		if (!inWater(player) && !WaterReturn.hasWaterBottle(player) && !charged) {
			remove();
			return;
		}

		if (WaterReturn.hasWaterBottle(player)) {
			bottle = true;
		}

		// If ability is is charged, set charged = true. If not, play charging particles.
		if (System.currentTimeMillis() >= getStartTime() + chargeTime) {
			if (!charged) {
				this.charged = true;
				WaterReturn.emptyWaterBottle(player);
			}
		} else {
			GeneralMethods.displayColoredParticle(origin, hex);
		}

		// If the ability is charged, try healing.
		if (charged) {

			if (target != null) {
				displayHealingWater(target);
			} else {
				displayHealingWater((LivingEntity) player);
			}

			// Try to heal themselves/target with 'interval' millisecond intervals.
			if (System.currentTimeMillis() - currTime >= interval) {

				heal(player);
				currTime = System.currentTimeMillis();
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
	}

	public void click() {
		Entity target = GeneralMethods.getTargetedEntity(player, range);
		if (target != null && !target.equals(this.target) && target instanceof LivingEntity) {
			hasReached = false;
			this.target = (LivingEntity) target;
		} else if (target != null && target.equals(this.target) && target instanceof LivingEntity) {
			hasReached = false;
			this.target = null;
		}
	}

	private void heal(Player player) {
		Entity target = GeneralMethods.getTargetedEntity(player, range);
		if (target != null && this.target != null && target instanceof LivingEntity) {
			if (this.target.getEntityId() == target.getEntityId() && hasReached) {
				giveHP((LivingEntity) this.target);
			}
		} else {
			if (hasReached) {
				giveHP(player);
				this.target = null;
			}
		}
	}

	private void giveHP(Player player) {
		if (!player.isDead() && player.getHealth() < player.getMaxHealth()) {
			applyHealing(player);
		} else {
			healing = false;
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
		if (!livingEntity.isDead() && livingEntity.getHealth() < livingEntity.getMaxHealth()) {
			applyHealing(livingEntity);
		} else {
			healing = false;
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
			livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, power));
			AirAbility.breakBreathbendingHold(livingEntity);
			healing = true;
			healingSelf = false;
		}
	}

	private boolean inWater(Player player) {
		Block block = player.getLocation().getBlock();
		return isWater(block) && !TempBlock.isTempBlock(block);
	}

	public void displayHealingParticlesSelf() {
		if (hasReached) {
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
	}

	public void displayHealingParticlesOther() {
		if (target != null) {
			if (hasReached) {
				Location centre = target.getLocation().clone().add(0, 1, 0);
				double increment = (2 * Math.PI) / 36;
				double angle1 = tstage1 * increment;
				double angle2 = tstage2 * increment;
				double x1 = centre.getX() + (0.75 * Math.cos(angle1));
				double z1 = centre.getZ() + (0.75 * Math.sin(angle1));
				double x2 = centre.getX() + (0.75 * Math.cos(angle2));
				double z2 = centre.getZ() + (0.75 * Math.sin(angle2));

				GeneralMethods.displayColoredParticle(new Location(centre.getWorld(), x1, centre.getY() + (0.75 * Math.cos(angle1)), z1), hex);
				GeneralMethods.displayColoredParticle(new Location(centre.getWorld(), x2, centre.getY() + (0.75 * -Math.cos(angle2)), z2), hex);

				if (tstage1 >= 36) {
					tstage1 = 0;
				}
				tstage1++;

				if (tstage2 >= 36) {
					tstage2 = 0;
				}
				tstage2++;
			}

		}
	}

	public void displayHealingWater(LivingEntity target) {
		double factor = 0.2;

		Location targetLoc = target.getLocation().clone().add(0, 1, 0);
		double distance = 0;
		if (location.getWorld().equals(targetLoc.getWorld())) {
			distance = location.distance(targetLoc);
		}
		Vector vec = new Vector(targetLoc.getX() - location.getX(), targetLoc.getY() - location.getY(), targetLoc.getZ() - location.getZ()).normalize();

		if (location.getWorld().equals(targetLoc.getWorld()) && location.distance(targetLoc) <= distance) {
			location = location.clone().add(vec.clone().multiply(factor));
			if (location.distance(targetLoc) <= 0.5) {
				hasReached = true;
			} else {
				hasReached = false;
			}
		}

		GeneralMethods.displayColoredParticle(location, hex);
	}

	private void fillBottle() {
		PlayerInventory inventory = player.getInventory();
		if (inventory.contains(Material.GLASS_BOTTLE)) {
			int index = inventory.first(Material.GLASS_BOTTLE);
			ItemStack item = inventory.getItem(index);

			if (item.getAmount() == 1) {
				inventory.setItem(index, new ItemStack(Material.POTION));
			} else {
				item.setAmount(item.getAmount() - 1);
				inventory.setItem(index, item);
				HashMap<Integer, ItemStack> leftover = inventory.addItem(new ItemStack(Material.POTION));
				for (int left : leftover.keySet()) {
					player.getWorld().dropItemNaturally(player.getLocation(), leftover.get(left));
				}
			}
		}
	}

	@Override
	public void remove() {
		if (bottle && charged) {
			fillBottle();
		}
		super.remove();
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

package com.projectkorra.projectkorra.firebending.combo;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.firebending.FireJet;
import com.projectkorra.projectkorra.firebending.util.FireDamageTimer;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

public class JetBlaze extends FireCombo {

	private double speed;
	private long cooldown;
	private long duration;
	private ArrayList<LivingEntity> affectedEntities;
	private double damage;
	private double fireTicks;
	private int progressCounter;
	
	public JetBlaze(Player player) {
		super(player);
		
		if (bPlayer.isOnCooldown("JetBlaze") && !bPlayer.isAvatarState()) {
			remove();
			return;
		}
		
		this.affectedEntities = new ArrayList<LivingEntity>();
		
		this.speed = getConfig().getDouble("Abilities.Fire.FireCombo.JetBlaze.Speed");
		this.cooldown = getConfig().getLong("Abilities.Fire.FireCombo.JetBlaze.Cooldown");
		this.duration = getConfig().getLong("Abilities.Fire.FireCombo.JetBlaze.Duration");
		this.damage = getConfig().getDouble("Abilities.Fire.FireCombo.JetBlaze.Damage");
		this.fireTicks = getConfig().getDouble("Abilities.Fire.FireCombo.JetBlaze.FireTicks");
		
		bPlayer.addCooldown(this);
		
		start();
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return new JetBlaze(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> jetBlaze = new ArrayList<>();
		jetBlaze.add(new AbilityInformation("FireJet", ClickType.SHIFT_DOWN));
		jetBlaze.add(new AbilityInformation("FireJet", ClickType.SHIFT_UP));
		jetBlaze.add(new AbilityInformation("FireJet", ClickType.SHIFT_DOWN));
		jetBlaze.add(new AbilityInformation("FireJet", ClickType.SHIFT_UP));
		jetBlaze.add(new AbilityInformation("Blaze", ClickType.SHIFT_DOWN));
		jetBlaze.add(new AbilityInformation("Blaze", ClickType.SHIFT_UP));
		jetBlaze.add(new AbilityInformation("FireJet", ClickType.LEFT_CLICK));
		return jetBlaze;
	}

	@Override
	public void progress() {
		if (System.currentTimeMillis() - getStartTime() > duration) {
			remove();
			return;
		}
		
		FireJet fireJet = getAbility(player, FireJet.class);
		if (fireJet != null) {
			fireJet.setSpeed(speed);
			
			ParticleEffect.SMOKE_LARGE.display(1.0F, 1.0F, 1.0F, 1.0F, 8, player.getLocation(), 80);
			ParticleEffect.FLAME.display(1.0F, 1.0F, 1.0F, 1.0F, 8, player.getLocation(), 80);
			
			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(player.getLocation(), 2)) {
				if (entity instanceof LivingEntity && !entity.equals(player)) {
					if (!affectedEntities.contains(entity)) {
						affectedEntities.add((LivingEntity) entity);
						DamageHandler.damageEntity(entity, damage, this);
						entity.setFireTicks((int) (fireTicks * 20));
						new FireDamageTimer(entity, player);
					}
				}
			}
			
			if (progressCounter % 4 == 0) {
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1, 0F);
			}
			
			progressCounter++;

		} else {
			remove();
			return;
		}
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public String getName() {
		return "JetBlast";
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}

}

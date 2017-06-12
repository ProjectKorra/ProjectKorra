package com.projectkorra.projectkorra.firebending.combo;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.firebending.FireJet;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.ParticleEffect;

public class JetBlast extends FireCombo {

	private double speed;
	private long cooldown;
	private long duration;
	
	public JetBlast(Player player) {
		super(player);
		
		if (bPlayer.isOnCooldown("JetBlast") && !bPlayer.isAvatarState()) {
			remove();
			return;
		}
		
		this.speed = getConfig().getDouble("Abilities.Fire.FireCombo.JetBlast.Speed");
		this.cooldown = getConfig().getLong("Abilities.Fire.FireCombo.JetBlast.Cooldown");
		this.duration = getConfig().getLong("Abilities.Fire.FireCombo.JetBlast.Duration");

		bPlayer.addCooldown(this);
		ParticleEffect.LARGE_EXPLODE.display(player.getLocation(), 0.0F, 0.0F, 0.0F, 0, 1);
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 15, 0F);
		
		start();
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return new JetBlast(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> jetBlast = new ArrayList<>();
		jetBlast.add(new AbilityInformation("FireJet", ClickType.SHIFT_DOWN));
		jetBlast.add(new AbilityInformation("FireJet", ClickType.SHIFT_UP));
		jetBlast.add(new AbilityInformation("FireJet", ClickType.SHIFT_DOWN));
		jetBlast.add(new AbilityInformation("FireJet", ClickType.SHIFT_UP));
		jetBlast.add(new AbilityInformation("FireShield", ClickType.SHIFT_DOWN));
		jetBlast.add(new AbilityInformation("FireShield", ClickType.SHIFT_UP));
		jetBlast.add(new AbilityInformation("FireJet", ClickType.LEFT_CLICK));
		return jetBlast;
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
			
			ParticleEffect.FLAME.display(player.getEyeLocation().getDirection().clone().normalize().multiply(-0.1), 0.3f, player.getLocation(), 80);
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

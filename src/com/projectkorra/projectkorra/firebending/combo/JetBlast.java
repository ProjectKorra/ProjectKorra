package com.projectkorra.projectkorra.firebending.combo;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.firebending.FireJet;
import com.projectkorra.projectkorra.util.ParticleEffect;

public class JetBlast extends FireAbility implements ComboAbility {

	private boolean firstTime;
	private long time;
	private long cooldown;
	private double damage;
	private double speed;
	private double range;
	private ArrayList<FireComboStream> tasks;
	private long duration;
	
	public JetBlast(Player player) {
		super(player);

		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		}

		this.firstTime = true;
		this.time = System.currentTimeMillis();
		this.tasks = new ArrayList<>();

		this.speed = getConfig().getDouble("Abilities.Fire.FireCombo.JetBlast.Speed");
		this.cooldown = getConfig().getLong("Abilities.Fire.FireCombo.JetBlast.Cooldown");
		this.duration = getConfig().getLong("Abilities.Fire.FireCombo.JetBlast.Duration");

		if (bPlayer.isAvatarState()) {
			this.cooldown = 0;
			this.damage = AvatarState.getValue(damage);
			this.range = AvatarState.getValue(range);
		}
		
		start();
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return null;
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		return null;
	}

	@Override
	public void progress() {
		if (System.currentTimeMillis() - time > duration) {
			remove();
			return;
		} else if (hasAbility(player, FireJet.class)) {
			if (firstTime) {
				if (bPlayer.isOnCooldown("JetBlast") && !bPlayer.isAvatarState()) {
					remove();
					return;
				}

				bPlayer.addCooldown("JetBlast", cooldown);
				firstTime = false;
				float spread = 0F;
				ParticleEffect.LARGE_EXPLODE.display(player.getLocation(), spread, spread, spread, 0, 1);
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 15, 0F);
			}
			FireJet fj = getAbility(player, FireJet.class);
			fj.setSpeed(speed);
			
			FireComboStream fs = new FireComboStream(player, this, player.getVelocity().clone().multiply(-1), player.getLocation(), 3, 0.5);

			fs.setDensity(1);
			fs.setSpread(0.9F);
			fs.setUseNewParticles(true);
			fs.setCollides(false);
			fs.runTaskTimer(ProjectKorra.plugin, 0, 1L);
			tasks.add(fs);
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

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

}

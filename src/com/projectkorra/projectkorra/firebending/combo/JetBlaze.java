package com.projectkorra.projectkorra.firebending.combo;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.firebending.FireJet;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.ParticleEffect;

public class JetBlaze extends FireAbility implements ComboAbility {

	private boolean firstTime;
	private int progressCounter;
	private long time;
	private long cooldown;
	private double damage;
	private double speed;
	private double fireTicks;
	private Vector direction;
	private ArrayList<LivingEntity> affectedEntities;
	private ArrayList<FireComboStream> tasks;
	private long duration;
	
	public JetBlaze(Player player) {
		super(player);

		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		}

		this.firstTime = true;
		this.time = System.currentTimeMillis();
		this.affectedEntities = new ArrayList<>();
		this.tasks = new ArrayList<>();

		this.damage = getConfig().getDouble("Abilities.Fire.JetBlaze.Damage");
		this.duration = getConfig().getLong("Abilities.Fire.JetBlaze.Duration");
		this.speed = getConfig().getDouble("Abilities.Fire.JetBlaze.Speed");
		this.cooldown = getConfig().getLong("Abilities.Fire.JetBlaze.Cooldown");
		this.fireTicks = getConfig().getDouble("Abilities.Fire.JetBlaze.FireTicks");
		
		if (bPlayer.isAvatarState()) {
			this.cooldown = 0;
			this.damage = getConfig().getDouble("Abilities.Avatar.AvatarState.Fire.JetBlaze.Damage");
			this.fireTicks = getConfig().getDouble("Abilities.Avatar.AvatarState.Fire.JetBlaze.FireTicks");
		}
		
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
		if (firstTime) {
			if (bPlayer.isOnCooldown("JetBlaze") && !bPlayer.isAvatarState()) {
				remove();
				return;
			}
			bPlayer.addCooldown("JetBlaze", cooldown);
			firstTime = false;
		} else if (System.currentTimeMillis() - time > duration) {
			remove();
			return;
		} else if (hasAbility(player, FireJet.class)) {
			direction = player.getVelocity().clone().multiply(-1);
			FireJet fj = getAbility(player, FireJet.class);
			fj.setSpeed(speed);
			fj.setDuration(duration);

			FireComboStream fs = new FireComboStream(player, this, direction, player.getLocation(), 5, 1);
			fs.setDensity(8);
			fs.setSpread(1.0F);
			fs.setUseNewParticles(true);
			fs.setCollisionRadius(3);
			fs.setParticleEffect(ParticleEffect.LARGE_SMOKE);
			fs.setDamage(damage);
			fs.setFireTicks(fireTicks);
			fs.runTaskTimer(ProjectKorra.plugin, 0, 1L);
			tasks.add(fs);
			if (progressCounter % 4 == 0) {
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1, 0F);
			}
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
		return "JetBlaze";
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}
	
	public ArrayList<LivingEntity> getAffectedEntities() {
		return affectedEntities;
	}
	
	@Override
	public String getInstructions() {
		return "FireJet (Tap Shift) > FireJet (Tap Shift) > Blaze (Tap Shift) > FireJet";
	}
}
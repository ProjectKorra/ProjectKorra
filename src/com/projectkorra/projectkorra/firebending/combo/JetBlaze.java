package com.projectkorra.projectkorra.firebending.combo;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.api.ComboAbility;
import com.projectkorra.projectkorra.ability.api.FireAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.abilities.fire.JetBlazeConfig;
import com.projectkorra.projectkorra.firebending.FireJet;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.ParticleEffect;

public class JetBlaze extends FireAbility<JetBlazeConfig> implements ComboAbility {

	private boolean firstTime;
	private int progressCounter;
	private long time;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.FIRE_TICK)
	private double fireTicks;
	private Vector direction;
	private ArrayList<LivingEntity> affectedEntities;
	private ArrayList<FireComboStream> tasks;
	@Attribute(Attribute.DURATION)
	private long duration;

	public JetBlaze(final JetBlazeConfig config, final Player player) {
		super(config, player);

		if (!this.bPlayer.canBendIgnoreBinds(this)) {
			return;
		}

		this.firstTime = true;
		this.time = System.currentTimeMillis();
		this.affectedEntities = new ArrayList<>();
		this.tasks = new ArrayList<>();

		this.damage = config.Damage;
		this.duration = config.Duration;
		this.speed = config.Speed;
		this.cooldown = config.Cooldown;
		this.fireTicks = config.FireTicks;

		if (this.bPlayer.isAvatarState()) {
			this.cooldown = 0;
			this.damage = config.AvatarState_Damage;
			this.fireTicks = config.AvatarState_FireTicks;
		}

		this.start();
	}

	@Override
	public Object createNewComboInstance(final Player player) {
		return new JetBlaze(ConfigManager.getConfig(JetBlazeConfig.class), player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		final ArrayList<AbilityInformation> jetBlaze = new ArrayList<>();
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
		if (this.firstTime) {
			if (this.bPlayer.isOnCooldown("JetBlaze") && !this.bPlayer.isAvatarState()) {
				this.remove();
				return;
			}
			this.firstTime = false;
		} else if (System.currentTimeMillis() - this.time > this.duration) {
			this.remove();
			return;
		} else if (hasAbility(this.player, FireJet.class)) {
			this.direction = this.player.getVelocity().clone().multiply(-1);
			final FireJet fj = getAbility(this.player, FireJet.class);
			fj.setSpeed(this.speed);
			fj.setDuration(this.duration);

			final FireComboStream fs = new FireComboStream(this.player, this, this.direction, this.player.getLocation(), 5, 1);
			fs.setDensity(8);
			fs.setSpread(1.0F);
			fs.setUseNewParticles(true);
			fs.setCollisionRadius(2);
			fs.setParticleEffect(ParticleEffect.SMOKE_LARGE);
			fs.setDamage(this.damage);
			fs.setFireTicks(this.fireTicks);
			fs.runTaskTimer(ProjectKorra.plugin, 0, 1L);
			this.tasks.add(fs);
			if (this.progressCounter % 4 == 0) {
				this.player.getWorld().playSound(this.player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1, 0F);
			}
		}
	}

	@Override
	public void remove() {
		for (final FireComboStream task : this.tasks) {
			task.remove();
		}
		super.remove();
		this.bPlayer.addCooldown("JetBlaze", this.cooldown);
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public long getCooldown() {
		return this.cooldown;
	}

	@Override
	public String getName() {
		return "JetBlaze";
	}

	@Override
	public Location getLocation() {
		return this.player.getLocation();
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public ArrayList<LivingEntity> getAffectedEntities() {
		return this.affectedEntities;
	}
	
	@Override
	public Class<JetBlazeConfig> getConfigType() {
		return JetBlazeConfig.class;
	}
}
